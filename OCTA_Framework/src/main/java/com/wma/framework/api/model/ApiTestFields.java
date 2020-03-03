package com.wma.framework.api.model;

/**
 * These are the CONSTANTS mapped with the fields from the TestData excel sheet 
 * @author singhb
 * 
 */
public enum ApiTestFields {
    TEST_ID("Test_ID"),
    TEST_CASE_NAME("TEstCaseTitle"),
    EXECUTE_TEST("Execute_Test"),
    PREREQUISITE_TEST_ID("Prerequisite_TestID"),
    STEP_NUMBER("Step_Number"),
    EXECUTE_STEP("Execute_step"),
    STEP_DESCRIPTION("Step_Description"),
    RESOURCE("Resource"),
    PARAMETERS("Parameters"),
    REQUEST_TYPE("Request_Type"),
    DATABASE_QUERY("Database_Query"),
    JSON_CONTENT("JSON_Content"),
    FIELDS_TO_COMPARE("Fields_To_Compare"),
    EXPECTED_STATUS_CODE("Expectede_Status_Code"),
    CONTENT_EXPECTED_IN_RESPONSE("Content_Expected_In_Response"),
    SCRIPT_VALIDATION("Script_Validation"),
    
    ;
	
    private String FIELD_NAME;
    
    ApiTestFields(String fieldName) {
    	this.FIELD_NAME = fieldName;
    }
    
    public String toString() {
    	return FIELD_NAME;
    }
    
}    
   