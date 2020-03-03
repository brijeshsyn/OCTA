package com.wma.framework.api.model;

import java.util.List;

/**
 * To represent an API Test Case
 * 
 * @author singhb
 *
 */
public class ApiTestCase {
	private String Test_ID;
	private String TestCase_Name;
	private String Execute_Test;
	private String Prerequisite_TestID;
	private List<ApiTestSteps> testSteps;
	private boolean executionStatus;

	public String getTest_ID() {
		return Test_ID;
	}

	public void setTest_ID(String test_ID) {
		this.Test_ID = test_ID;		
	}

	public String getTestCase_Name() {
		return TestCase_Name;
	}	

	public void setTestCase_Name(String testCase_Name) {
		TestCase_Name = testCase_Name;
	}

	public String getExecute_Test() {
		return Execute_Test;
	}

	public void setExecute_Test(String execute_Test) {
		Execute_Test = execute_Test;
	}

	public String getPrerequisite_testID() {
		return Prerequisite_TestID;
	}
	public void setPrerequisite_TestID(String prerequisite_TestID) {
		Prerequisite_TestID = prerequisite_TestID;
	}

	public List<ApiTestSteps> getTestSteps() {
		return testSteps;
	}

	public void setTestSteps(List<ApiTestSteps> testSteps) {
		this.testSteps = testSteps;
	}

	public void setExecutionStatus(boolean status) {
		this.executionStatus = status;
	}

	public boolean getExecutionStatus() {
		return this.executionStatus;
	}

	public class ApiTestSteps {
		private String Step_Number;
		private String Execute_Step;
		private String Step_description;
		private String Resource;
		private String Parameters;
		private String Request_Type;
		private String Database_Query;
		private String JSON_Content;
		private String Fields_To_Compare;
		private String Expected_Status_Code;
		private String Content_Expected_In_Response;
		private String Script_Validation;
		private boolean Execution_Status = true;

		public String getStep_NUmber() {
			return Step_Number;
		}

		public void setStep_Number(String step_Number) {
			this.Step_Number = step_Number;
		}

		public String getExecute_Step() {
			return Execute_Step;
		}

		public void setExecute_Step(String execute_Step) {
			this.Execute_Step = execute_Step;
		}

		public String getStep_Description() {
			return Step_description;
		}	

		public void setStep_Description(String step_Description) {
			this.Step_description = step_Description;
		}

		public String getResource() {
			return Resource;
		}

		public void setResource(String resource) {
			this.Resource = resource;
		}

		public String getParameters() {
			return Parameters;
		}

		public void setParameters(String parameters) {
			this.Parameters = parameters;
		}

		public String getRequest_Type() {
			return Request_Type;
		}

		public void setRequest_Type(String request_Type) {
			this.Request_Type = request_Type;
		}

		public String getDatabase_query() {
			return Database_Query;
		}

		public void setDatabase_Query(String database_Query) {
			this.Database_Query = database_Query;
		}

		public String getJSON_Content() {
			return JSON_Content;
		}

		public void setJSON_Content(String JSON_Content) {
			this.JSON_Content = JSON_Content;
		}

		public String getFields_To_Compare() {
			return Fields_To_Compare;
		}

		public void setFields_To_Compare(String fields_To_Compare) {
			this.Fields_To_Compare = fields_To_Compare;
		}

		public String getExpected_Status_Code() {
			return Expected_Status_Code;
		}

		public void setExpected_Status_Code(String expected_Status_Code) {
			this.Expected_Status_Code = expected_Status_Code;
		}

		public String getContent_Expected_In_Response() {
			return Content_Expected_In_Response;
		}

		public void setContent_Expected_In_Response(String content_Expected_In_Response) {
			this.Content_Expected_In_Response = content_Expected_In_Response;
		}

		public String getScript_Validation() {
			return Script_Validation;
		}

		public void setScript_Validation(String script_Validation) {
			this.Script_Validation = script_Validation;
		}

		public boolean getExecution_Status() {
			return Execution_Status;
		}

		public void setExecution_Status(boolean execution_Status) {
			this.Execution_Status = execution_Status;
		}

		public String toString() {
			return "[" + Step_Number + "," + Execute_Step + "," + Step_description + "," + Execute_Step + "," 
					+ Parameters + "," + Request_Type + "," + Database_Query + "," + JSON_Content + "," 
					+ Fields_To_Compare + "," + Expected_Status_Code + "," + Content_Expected_In_Response + "," 
					+ Script_Validation + "]";
		}
	}

	public String toString() {
		return "[" + Test_ID + "," + TestCase_Name + "," + Execute_Test + "," + Prerequisite_TestID + ","
				+ testSteps + ",";
	}


}	