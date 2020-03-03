package com.wma.framework.driver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.wma.framework.api.ApiTestCaseReader;
import com.wma.framework.api.RESTOperations;
import com.wma.framework.api.model.ApiTestCase;
import com.wma.framework.api.model.ApiTestCase.ApiTestSteps;
import com.wma.framework.api.model.ResourceParameter;
import com.wma.framework.common.ConfigProvider;
import com.wma.framework.report.ReportCustomiser;
import com.wma.framework.report.TestRailAndExtentReporter;
import com.wma.framework.util.DatabaseReader;
import com.aventstack.extentreports.Status;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.restassured.response.Response;

public class ApiDriverScript {

	private static Logger log = Logger.getLogger(ApiDriverScript.class);
	private TestRailAndExtentReporter tcLogger;
	private Map<String, String> globalVariables;
	private Map<ApiTestCase.ApiTestSteps, Response> responsesOfSteps;
	private Map<ApiTestCase.ApiTestSteps, ResultSet> dbQueryResultOfSteps;
	private Map<String, String> paramsWithValues;
	private List<ApiTestCase> testCases;
	private boolean STOP_EXECUTION_ON_STEP_FAILURE = false;

	public void start() throws Exception {
		log.info("Executing API Driver Script");

		// Initialize the config setting from the Test Controller file
		ConfigProvider config = ConfigProvider.getInstance();

		File myFile = new File(config.getResultFolder());
		// Create results folder if doesn't exist already
		if (!myFile.exists())
			myFile.mkdirs();

		// Fetch the API end point url
		String endPoint = config.getAppUrl();

		// Initialize the API Test Case reader object which will read the testCases
		ApiTestCaseReader tcReader = new ApiTestCaseReader(config);

		// Read the test cases from the excel
		testCases = tcReader.readApiTestCases();

		// Read global variables
		globalVariables = tcReader.getGlobalVariables();

		for (int iteration = 1; iteration <= config.getIterations(); iteration++) {

			// iterate through all the test cases
			for (ApiTestCase testCase : testCases) {
				// if the test case is already passed, do not execute it
				if (((ApiTestCase) testCases).getExecutionStatus())
					continue;

				log.info("Started execution of " + ((ApiTestCase) testCases).getTest_ID() + " "
						+ ((ApiTestCase) testCases).getTestCase_Name());
				// Create Test Case in the report
				tcLogger = TestRailAndExtentReporter.getInstance(config, ((ApiTestCase) testCases).getTestCase_Name(),
						"");

				// To store their parameter values which can be used in the Resource path
				paramsWithValues = new HashMap<>();

				// Store responses of each step and use it next steps if required
				responsesOfSteps = new HashMap<>();

				// Store database query Result of each step and use it in next steps if required
				dbQueryResultOfSteps = new HashMap<>();
				try {
					// Execute each test step
					for (ApiTestCase.ApiTestSteps testStep : ((ApiTestCase) testCases).getTestSteps()) {
						// Do not execute step which is marked as NO in the filed Execute Step
						
						if (testStep.getExecute_Step().equalsIgnoreCase("No"))
							continue;

						// Log the step which is currently executed in the report
						tcLogger.log(Status.INFO, testStep.getStep_NUmber() + " : " + testStep.getStep_Description());

						// Store the query result in this object for each step and use it whenever
						// required
						ResultSet dbQueryResult = null;

						// Execute database query if there is query provided in the Database_Query field
						if (!testStep.getDatabase_query().equals("")) {
							// Execute database query
							dbQueryResult = getdatabaseQueryresult(config, testStep);
							dbQueryResultOfSteps.put(testStep, dbQueryResult);
						}

						// Fetch the resource, json, method values provided for the step
						String resource = testStep.getResource();
						String json = testStep.getJSON_Content();
						String method = testStep.getRequest_Type();

						// Get parameter names from the resource
						List<String> params = new ArrayList<>();
						if (resource.contains("{"))
							params = getParameterFromResource(resource);

						if (!params.isEmpty()) {

							List<String> paramsFoundInGlobal = new ArrayList<>();
							for (String param : params) {
								if (globalVariables.containsKey(param)) {
									resource.replaceAll(param, globalVariables.get(param));
									paramsFoundInGlobal.add(param);
								}
							}

							params.removeAll(paramsFoundInGlobal);
							if (!params.isEmpty())
								resource = formatResourceParameters(testStep, dbQueryResult, resource, params);
						} // End of if(!param.isEmpty())

						// Format JSON, by replacing the variables with proper value from DB Query
						// or Response of previous step
						List<String> jsonParams = getParameterFromJson(json);
						if (!jsonParams.isEmpty())
							json = formatJsonValues(json, jsonParams);

						// Store the response object
						Response res;

						// Set the end point for the APIs
						RESTOperations rest = new RESTOperations(endPoint);

						// Remove braces { } from the resource
						resource = resource.replaceAll(Pattern.quote("{"), "").replaceAll(Pattern.quote("}"), "");

						// Excute specific HTTP method based on the method in the step
						switch (method.toUpperCase()) {
						case "GET":
							tcLogger.log(Status.INFO, "Executing GET method with resource : " + resource);
							res = rest.get(resource);
							break;

						case "POST":
							tcLogger.log(Status.INFO,
									"Executing POST method with resource : " + resource + " \nand JSON : " + json);
							res = rest.post(resource, json);
							break;

						case "PUT":
							tcLogger.log(Status.INFO,
									"Executing PUT method with resource : " + resource + " \nand JSON : " + json);
							res = rest.put(resource, json);
							break;

						case "DELETE":
							tcLogger.log(Status.INFO,
									"EXECUTING DELETE method with response : " + resource + " \nand JSON : " + json);
							res = rest.delete(resource, json);
							break;

						default:
							tcLogger.log(Status.FAIL, "Invalid Request Type (HTTP Method)");
							testStep.setExecution_Status(false);
							throw new Exception("Invalid Request Type (HTTP Method)");
						}

						if (res.getStatusCode() >= 200 && res.getStatusCode() < 300)
							tcLogger.log(Status.PASS, method.toUpperCase()
									+ " method executed Succesfully \nHTTP Status Code : " + res.getStatusCode());
						else {
							tcLogger.log(Status.FAIL,
									"Some error occurred while accessing the api.\nHTTP Status Code : "
											+ res.getStatusCode() + "<br/>Message : " + res.body().asString());
							testStep.setExecution_Status(false);
						}
						// Store the response of current step in the Map object, so that it can be used
						// in later steps
						responsesOfSteps.put(testStep, res);

						// Validations
						if (!testStep.getExpected_Status_Code().isEmpty()) {
							if (Integer.parseInt(testStep.getExpected_Status_Code()) == res.getStatusCode())
								tcLogger.log(Status.PASS, "As expected , HTTP Status code is " + res.getStatusCode());
							else {
								tcLogger.log(Status.FAIL, "Expected status code is not displayed");
								testStep.setExecution_Status(false);
							}
						}

						// Validation of the expected contents in the respons body
						if (!testStep.getContent_Expected_In_Response().isEmpty()) {
							String[] temp = testStep.getContent_Expected_In_Response().split(Pattern.quote(";"));

							for (String str : temp) {
								String key = str.split(Pattern.quote(":"))[0];
								String expectedValue = str.split(Pattern.quote(":"))[1].trim();

								List<String> msges = getParameterFromJson(expectedValue);
								if (!msges.isEmpty())
									expectedValue = formatJsonValues(expectedValue, msges);

								String actualValue = ((Object) res.body().jsonPath().get(key)).toString().trim();
								if (expectedValue.toLowerCase().equalsIgnoreCase(actualValue))
									tcLogger.log(Status.PASS,
											"Below Assertion Passed<br/>" + key + ":" + expectedValue);
								else {
									tcLogger.log(Status.FAIL,
											"Below Assertion Failed<br/>" + key + ":" + expectedValue);
									testStep.setExecution_Status(false);
								}
							}

						} // End of validation of expected contents in the response body

						// Execute script assertions if any
						if (!testStep.getScript_Validation().isEmpty()) {
							if (executeScriptAssertion(testStep))
								tcLogger.log(Status.PASS, "All Assertions Passed");
							else {
								tcLogger.log(Status.FAIL, "It seems that few Assertions failed");
								testStep.setExecution_Status(false);
							}
						}

						// When STOP_ON_STEP_FAILURE is set to true, the execution of the test case
						// should be stopped on failure of any step
						if (STOP_EXECUTION_ON_STEP_FAILURE && !testStep.getExecution_Status()) {
							log.info("*****************************");
							log.info("Due to failure of the step, Test Case execution has been stopped");
							log.info("Next Test Case execution will be started...");
							log.info("*****************************");
							break;
						}
					} // End of TEst Steps
				} // End of Try
				catch (Exception e) {
					e.printStackTrace();
				} // End of Catch

				// mark test case as pass/fail
				if (tcLogger.getCurrentExtentTest().getStatus().equals(Status.PASS))
					testCase.setExecutionStatus(true);

				// Write the report on Hard Drive
				tcLogger.flush();
			} // End of Test Cases

			// Set the iteration details for th current iteration (Re-run)
			TestRailAndExtentReporter.addIterationDetails(iteration);

			// Customize the default Extent Report
			ReportCustomiser.customise();

		} // End of Iterations loop
	} // start method ends

	// To format the resource string by replacing the variables with proper values
	// from either DB Query ot Response of previous step
	private String formatResourceParameters(ApiTestCase.ApiTestSteps testStep, ResultSet dbQueryResult, String resource,
			List<String> params) throws SQLException {

		// When resource contains parameters
		// Fetch parameter values from response of previous step or database query
		if (!testStep.getParameters().isEmpty()) {

			// Configure parameters object
			String[] temp = testStep.getParameters().split(Pattern.quote("||"));
			if (testStep.getParameters().toLowerCase().contains("query")
					|| testStep.getParameters().toLowerCase().contains("response")) {
				List<ResourceParameter> resourceParams = new ArrayList<>();
				List<String> globalVariableParam = new ArrayList<>();
				List<String> keyValuePairedParams = new ArrayList<>();

				// Categorize the parameters
				for (String tmp : temp) {
					if (tmp.toLowerCase().contains("global"))
						globalVariableParam.add(tmp);

					else if (tmp.toLowerCase().contains("query") || tmp.toLowerCase().contains("response")) {
						String parameterName = tmp.split(Pattern.quote("="))[0];
						String temp1[] = tmp.split(Pattern.quote("="))[1].replaceAll(Pattern.quote("("), "") // Remove {
								.replaceAll(Pattern.quote(")"), "") // Remove }
								.replaceAll(Pattern.quote("$"), "") // Remove $
								.split(Pattern.quote("#")); // Split by #

						// Create object of ResourceParameter
						ResourceParameter rp = new ResourceParameter();

						// Set values for the object
						rp.setName(parameterName).setStepNumberReference(temp1[0]).setQueryOrResponse(temp1[1])
								.setKey(temp1[2]);

						// Add the object in the list
						resourceParams.add(rp);
					} else
						keyValuePairedParams.add(tmp);
				}

				// Configure paramWithValues with Global Parameters
				for (String str : globalVariableParam) {
					String[] tmp = str.split(Pattern.quote("="));
					paramsWithValues.put(tmp[0], globalVariables.get(tmp[1]));
				}

				// Configure parameters from Response/Query
				for (ResourceParameter resourceParam : resourceParams) {
					// Fetch parameter value from Database query
					if (resourceParam.getQueryOrResponse().toLowerCase().contains("query")) {
						ResultSet rs = null;

						// Fetch data from current steps db query result, if the StepNumber mentioned
						// is equal to current step
						if (resourceParam.getStepNumberReference().contains(testStep.getStep_NUmber()))
							rs = dbQueryResult;

						// Else fetch data from prvious step's db query result which is stores in
						// required step
						for (ApiTestSteps step : dbQueryResultOfSteps.keySet()) {
							if (step.getStep_NUmber().equalsIgnoreCase(resourceParam.getStepNumberReference())) {
								rs = dbQueryResultOfSteps.get(step);
								break;
							}
						}

						if (rs != null) {
							((ResultSet) rs).next();
							paramsWithValues.put(resourceParam.getName(),
									((ResultSet) rs).getString(resourceParam.getKey()));
						} else
							log.error("Invalid Parameter key");
					}
					// Fetch parameter value from response of previous steps, which is stores in
					// responsesOfSteps Object
					else if (testStep.getParameters().toLowerCase().contains("response")) {
						Response res = null;
						for (ApiTestSteps step : responsesOfSteps.keySet()) {
							if (step.getStep_NUmber().equalsIgnoreCase(resourceParam.getStepNumberReference())) {
								res = responsesOfSteps.get(step);
								break;
							}
						}
						if (res != null) {
							String value = null;
							try {
								value = getValueFromJson(res.body().asString(), resourceParam.getKey());
							} catch (Exception e) {
								value = ((Object) res.body().jsonPath().get(resourceParam.getKey())).toString();
							}
							paramsWithValues.put(resourceParam.getName(), value);

						} else
							log.error("Invalid Parameter key");
					}
				}
			}
			else {
				for (String tmp : temp) {
					String[] keyValue = tmp.split(Pattern.quote("="));
					paramsWithValues.put(keyValue[0], keyValue[1]);
				}
			}
		}
		// Check whether parameter is present in paramWithValue object
		// Replace the parameter value in the resource with the value present in the
		// paramWithValue object
		resource = addValueInResourceForParameters(resource, params);
		return resource;
	}

	// Format resource contents and replace the place holders with values
	private String addValueInResourceForParameters(String resource, List<String> params) {
		for (int i = 0; i < params.size(); i++) {
			String parameterName = params.get(i);
			if (paramsWithValues.containsKey(parameterName))
				resource = resource.replaceAll(parameterName, paramsWithValues.get(parameterName));

		}
		return resource;
	}

	// To execute the dtabase query of the test step and return the Result of the
	// query
	private ResultSet getdatabaseQueryresult(ConfigProvider config, ApiTestCase.ApiTestSteps testStep) {
		ResultSet dbQueryResult = null;
		DatabaseReader dbReader = new DatabaseReader();
		Connection con;
		try {
			con = dbReader.getSqlDbConnection(config.getDbServerName());
			Statement stmt = con.createStatement();
			dbQueryResult = stmt.executeQuery(testStep.getDatabase_query());
		} catch (ClassNotFoundException | SQLException e) {
			log.error("Query could not be executed successfully");
			log.error(e.getMessage());
			e.printStackTrace();
		}

		return dbQueryResult;
	}

	// Fetch the lis tof parameters (Place Holders) required in the Resource
	private List<String> getParameterFromResource(String resource) {
		List<String> params = new ArrayList<>();
		String[] temp = resource.split(Pattern.quote("/"));
		for (String str : temp) {
			if (str.contains("{"))
				params.add(str.replace("{", "").replace("}", ""));
		}
		return params;
	}

	private String formatJsonValues(String json, List<String> params) {
		Map<String, String> paramsWithValues = new HashMap<>();
		for (String param : params) {

			// Fetch the step number reference and key for which value would be searched
			String[] tmp = param.replaceAll(Pattern.quote("$"), "").replaceAll(Pattern.quote("("), "")
					.replaceAll(Pattern.quote("}"), "").split(Pattern.quote("#"));
			String stepNumber = tmp[0];
			String key = tmp[2];

			// fetch data from database query, it param contains Query keyword
			if (param.contains("query")) {
				ResultSet result = null;
				for (ApiTestCase.ApiTestSteps step : dbQueryResultOfSteps.keySet())
					if (step.getStep_NUmber().equalsIgnoreCase(stepNumber)) {
						result = dbQueryResultOfSteps.get(step);
						break;
					}

				if (result == null) {
					log.error("Invalid Step Number reference in parameter : " + param);
					return null;
				}

				try {
					result.next();
					paramsWithValues.put(param, result.getString(key));
				} catch (SQLException e) {
					log.error(
							"It seems, there are no result set returned by the query used in the step: " + stepNumber);
				}

			}
			// Fetch data from Response, if param contains Response keyword
			else if (param.toLowerCase().contains("response")) {
				Response res = null;
				for (ApiTestCase.ApiTestSteps step : responsesOfSteps.keySet())
					if (step.getStep_NUmber().equalsIgnoreCase(stepNumber)) {
						res = responsesOfSteps.get(step);
						break;
					}

				if (res == null) {
					log.error("Invalid Step Number reference in parameter : " + param);
					return null;
				}

				String value = "";
				try {
					value = ((Object) res.body().jsonPath().get(key)).toString();
				} catch (Exception e) {
					log.error(e.getMessage());
					log.error(res.body().asString());
				}
				paramsWithValues.put(param, value);
			}
		}

		// Replace the parameters from the JSON
		for (String str : params) {
			if (!paramsWithValues.containsKey(str))
				log.error("Value could be not be found for Param : " + str);
			else
				json = json.replaceAll(Pattern.quote(str), paramsWithValues.get(str));
		}

		return json;
	}

	// Fetch list of variable parameters configures in Json
	private List<String> getParameterFromJson(String json) {
		int start = 0;
		int end = 0;

		Map<Integer, Integer> startEnd = new HashMap<>();
		boolean started = false;
		for (int i = 0; i < json.length(); i++) {

			if (json.charAt(i) == '$' && json.charAt(i + 1) == '{') {
				start = i;
			} else if (started && start > 0 && json.charAt(i) == '}') {
				end = i + 1;
				startEnd.put(start, end);
				i = end;
				started = false;
			}
		}

		List<String> params = new ArrayList<>();
		for (Integer i : startEnd.keySet()) {
			String param = json.substring(i, startEnd.get(i));
			System.out.println(param);
			params.add(param);
		}

		return params;
	}

	// Script assertions are executed through this method
	// Using the concept of Reflections in Java, we fetch the class and the method
	// and execute them
	// It is assumed that the value passed in the Script Validation field will be
	// 'className,methodName'
	private boolean executeScriptAssertion(ApiTestCase.ApiTestSteps step) {
		String temp[] = step.getScript_Validation().split(Pattern.quote("."));
		String className = temp[0];
		String methodName = temp[1];
		String packageName = "com.api.assertions";

		Boolean returnObject = false;
		try {
			Class<?> myClass = Class.forName(packageName + "." + className);
			Method[] methods = myClass.getMethods();
			for (Method method : methods)
				if (methodName.equalsIgnoreCase(method.getName()))
					returnObject = (Boolean) method.invoke(myClass.newInstance(), step, responsesOfSteps,
							dbQueryResultOfSteps, tcLogger);

			return returnObject.booleanValue();

		} catch (ClassNotFoundException e) {
			log.error("Please check the class name");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			log.error("It seems, the method doesn't accept few mandatory parameters");
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		return false;
	}

	private String getValueFromJson(String json, String nodePath) {
		DocumentContext jsonContext = JsonPath.parse(json);
		return jsonContext.read(nodePath).toString();
	}

	/**
	 * Set the flag to true, if we want the test case execution to be stopped on
	 * failure on any step
	 * 
	 * @param flag
	 */
	public void setStopTestCaseExecutionOnStepFailure(boolean flag) {
		this.STOP_EXECUTION_ON_STEP_FAILURE = flag;
	}

}
