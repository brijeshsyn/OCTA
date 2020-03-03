package com.wma.framework.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wma.framework.api.model.ApiTestCase;
import com.wma.framework.api.model.ApiTestCase.ApiTestSteps;
import com.wma.framework.api.model.ApiTestFields;
import com.wma.framework.common.ConfigProvider;
import com.wma.framework.util.ExcelUtilities;

/**
 * To read the test cases from the test data file 
 * 
 * @author singhb
 * 
 */
public class ApiTestCaseReader {
	private final String API_TEST_FILE;
	private final String SHEET_NAME;

	/**
	 * To initialize the object with default instance of ConfigProvider
	 */
	public ApiTestCaseReader() {
		//config = ConfigProvider.getInstance();
		API_TEST_FILE  = "C:\\FD_QA_1\\Counterparty\\Resources\\TestData\\CounterpartyTestData.xlsx";//config.getTestDataFilePath();
		SHEET_NAME = "Counterparty";//config.getProduct();
	}

	/**
	 * To initialize the object with given instance of ConfigProvider
	 */
	public ApiTestCaseReader(ConfigProvider config) {
		API_TEST_FILE = config.getTestDataFilePath();
		SHEET_NAME = config.getProduct();
	}

	/**
	 * Read the test cases and return in a list of ApiTestCase objects 
	 * 
	 * @return
	 */
	public List<ApiTestCase> readApiTestCases() {
		List<ApiTestCase> apiTCs = new ArrayList<>();
		ExcelUtilities excel = new ExcelUtilities(API_TEST_FILE);
		int rows = excel.getRowCount(SHEET_NAME);

		//Fetch the first row 
		Map<String, String> rowData = excel.getRowData(1, SHEET_NAME);

		//Iterate through all the rows 
		for(int i=1; i<rows;) {

			ApiTestCase apiTest = new ApiTestCase();
			List<ApiTestSteps> testSteps = new ArrayList<>();

			//Reading the steps for the test case
			do {
				//When test id is non-blank, add details (test_id, test_case_name, etc.. ) in the object 
				if(!rowData.get(ApiTestFields.TEST_ID.toString()).equals("")) {
					apiTest = new ApiTestCase();
					apiTest.setTest_ID(rowData.get(ApiTestFields.TEST_ID.toString()));
					apiTest.setTestCase_Name(rowData.get(ApiTestFields.TEST_CASE_NAME.toString()));
					apiTest.setExecute_Test(rowData.get(ApiTestFields.EXECUTE_TEST.toString()));
					apiTest.setPrerequisite_TestID(rowData.get(ApiTestFields.PREREQUISITE_TEST_ID.toString()));
				}
				//add test step details
				ApiTestCase.ApiTestSteps testStep = apiTest.new ApiTestSteps();
				testStep.setStep_Number(rowData.get(ApiTestFields.STEP_NUMBER.toString()));
				testStep.setExecute_Step(rowData.get(ApiTestFields.EXECUTE_STEP.toString()));
				testStep.setStep_Description(rowData.get(ApiTestFields.STEP_DESCRIPTION.toString()));
				testStep.setResource(rowData.get(ApiTestFields.RESOURCE.toString()));
				testStep.setParameters(rowData.get(ApiTestFields.PARAMETERS.toString()));
				testStep.setRequest_Type(rowData.get(ApiTestFields.REQUEST_TYPE.toString()));
				testStep.setDatabase_Query(rowData.get(ApiTestFields.DATABASE_QUERY.toString()));
				testStep.setJSON_Content(rowData.get(ApiTestFields.JSON_CONTENT.toString()));
				testStep.setFields_To_Compare(rowData.get(ApiTestFields.FIELDS_TO_COMPARE.toString()));
				testStep.setExpected_Status_Code(rowData.get(ApiTestFields.EXPECTED_STATUS_CODE.toString()));
				testStep.setContent_Expected_In_Response(rowData.get(ApiTestFields.CONTENT_EXPECTED_IN_RESPONSE.toString()));
				testStep.setScript_Validation(rowData.get(ApiTestFields.SCRIPT_VALIDATION.toString()));


				if(!testStep.getStep_NUmber().equals(""))
					testSteps.add(testStep);


			} while(++i<rows && (rowData = excel.getRowData(i, SHEET_NAME)).get(ApiTestFields.TEST_ID.toString()).equals("")); //Keep reading next rows until we get the next non-blank TEST_ID


			//Add the test steps in the test case object 
			apiTest.setTestSteps(testSteps);

			//Add the test case in the list	
			if(apiTest.getExecute_Test().equalsIgnoreCase("Yes"))
				apiTCs.add(apiTest);
		}
		addPreRequisiteTestSteps(apiTCs);

		return apiTCs;

	}

	/**
	 * To read the global variables ffrom the test data file 
	 * @return
	 */
	public Map<String, String> getGlobalVariables() {
		String sheetName = "Globalvariables";
		Map<String, String> globalVariables = new HashMap<>();
		ExcelUtilities excel = new ExcelUtilities(API_TEST_FILE);
		int rows = excel.getRowCount(SHEET_NAME);

		for(int i=1; i<=rows; i++) {
			Map<String, String> rowData = excel.getRowData(i, sheetName);
			globalVariables.put(rowData.get("Variables.get"), rowData.get("Value"));
		}

		return globalVariables;
	}

	private void addPreRequisiteTestSteps(List<ApiTestCase> apiTCs) {
		for(ApiTestCase testCase : apiTCs) {
			if(testCase.getPrerequisite_testID().equals(""))
				continue;
			else if(testCase.getPrerequisite_testID().equalsIgnoreCase(testCase.getTest_ID())) {
				System.err.println("The Pre-Requisite Test Id can not be as the Test Id of the test case");
				continue;
			}

			String preRequisiteTestId = testCase.getPrerequisite_testID();
			ApiTestCase preRequisiteTestCase = null;

			boolean flag = false;

			for(ApiTestCase test : apiTCs) {
				if(test.getTest_ID().equalsIgnoreCase(preRequisiteTestId)) {
					flag = true;
					preRequisiteTestCase = test;
					break;
				}
			}

			if(flag) {
				if(preRequisiteTestCase.getPrerequisite_testID().equalsIgnoreCase(testCase.getTest_ID())) {
					System.err.println("Circular dependency error!!!\nPlease sure that there is no pair of Test Cases which depend on each other");
					System.err.println("TestId : " + testCase.getTest_ID() + " and TestId : " + preRequisiteTestCase.getTest_ID() + ", are inter dependent");
					continue;
				}
				testCase.getTestSteps().addAll(0, preRequisiteTestCase.getTestSteps());
			}
			else	
				System.err.println("The specified PreRequisite Test Id (" + preRequisiteTestId + ") is invalid for Test Case "	+ testCase.getTest_ID());
		}	
	}

	public static void main(String[] args) {
		ApiTestCaseReader test = new ApiTestCaseReader();
		List<ApiTestCase> apiTCs = test.readApiTestCases();
		System.out.println(apiTCs);
	}
}
