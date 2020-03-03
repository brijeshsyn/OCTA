package com.wma.framework.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.aventstack.extentreports.Status;
import com.wma.framework.report.TestRailAndExtentReporter;
import com.wma.framework.testrail.APIClient;
import com.wma.framework.testrail.APIException;
import com.wma.framework.util.ExcelUtilities;



/**
 * InitTest class is, the class which should be inherited/extended by each Test
 * Script class which will contain the test cases. The TestScirpt class can use
 * the methods of BasicWebTest class for some basic operations. It initializes
 * the test execution in the BeforeSuite ethod, and does the tear down in the 
 * AfterSuite method 
 *
 * @author singhb
 *
 */
public abstract class InitTest {
	private static Logger log = Logger.getLogger(InitTest.class);

	private static APIClient client;
	private WebDriver driver;

	@BeforeSuite
	public void beforeSuite() {
		log.info("Inside BeforeSuite Method");
		log.info("Test Initialization...");

		if (ConfigProvider.getInstance().getTestRail().equalsIgnoreCase("Yes")) {
			client = new APIClient("http://testtrail/index.php");
			client.setUser(System.getProperty("user.name"));
			client.setPassword(ConfigProvider.getInstance().getPassword());
		}
	}

	@AfterSuite
	public void afterSuit() {
		log.info("After Suite...");
	}
	/**
	 * To initialize the WebDriver
	 * 
	 * @return
	 */
	public WebDriver getWebDriver() {
		if (driver == null || driver.toString().contains("null")) {
			driver = WebDriverFactory.getWebDriver();
			driver.get(config().getAppUrl());
		}
		return driver;
	}

	/**
	 * To get non default (Chromr) WebDriver instance
	 * 
	 * @return
	 */
	public WebDriver getChromeWebDriver() {
		return WebDriverFactory.getExplicitInstanceOfChromeDriver();
	}

	/**
	 * To initialize the ConfigProvider
	 * 
	 * @return
	 */
	public ConfigProvider config() {
		return ConfigProvider.getInstance();
	}

	/**
	 * To close the web driver instance
	 */
	public void quitDriver() {
		WebDriverFactory.quitWebDriver();
		driver = null;
	}

	/** 
	 * To close the non-default web driver instance
	 */
	public void quitChromeDriver() {
		WebDriverFactory.getExplicitInstanceOfChromeDriver();
	}

	/**
	 * To get a Row of data into a key , value form
	 * 
	 * @param colName
	 * @param value
	 * @return
	 */
	public Map<String, String> getTestDataWhere(String colName, String value) {
		ExcelUtilities excel = new ExcelUtilities(ConfigProvider.getInstance().getTestDataFilePath());
		return excel.getRowWhere(config().getProduct(), colName, value);
	}

	/**
	 * To get a Row of data into a key, value form 
	 * 
	 * @param testCaseTitle 
	 * @return
	 */
	public Map<String, String> getTestDataWhere(String testCaseTitle) {
		return getTestDataWhere("TestCaseTitle", testCaseTitle);
	}
	
	/**
	 * This is used to update the final status of the test case. It is meant to be 
	 * used in finally block of every testscript 
	 * 
	 * @param runId
	 * @param caseId
	 * @param data
	 * @param executeStatus
	 * @param singhb
	 */
	public void finallyMethod(String strTestCaseTitle, String runId, String caseId, Boolean executeStatus,
			TestRailAndExtentReporter tcLogger) {
		log.info("Executing Finally Method");
		Map<String, Object> data = new HashMap<String, Object>();
		if (!executeStatus) {
			data.put("status_id", new Integer(5));
			tcLogger.log(Status.FAIL, strTestCaseTitle + "Test case is FAILED");
			log.error("Test Case Failed");
			// updateTestStatus(strTestCaseTitle, "Fail");
		} else {
			data.put("status_id", new Integer(1));
			tcLogger.log(Status.PASS, strTestCaseTitle + " Test CAse is PASSED");
			log.info("Test Case Passed");
			// updateTestStatus(strTestCaseTitle, "Pass");
		}

		if(config().getTestRail().equalsIgnoreCase("Yes")) {
			data.put("comment", tcLogger.getTestRailComments());
			try {
				log.info("Updating log in Test Rail");
				JSONObject r = (JSONObject) client.sendPost("add_result_for_case/" + runId + "/" + caseId, data);
				log.info("Logs updated in Test Rail \n" + r);
			} catch (IOException | APIException e) {
				log.error("Input/Output Exception...");
				e.printStackTrace();
			}
		}
		Assert.assertTrue(executeStatus);
	}
}
