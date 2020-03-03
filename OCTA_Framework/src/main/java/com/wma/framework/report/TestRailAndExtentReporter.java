package com.wma.framework.report;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.wma.framework.common.ConfigProvider;
import com.wma.framework.util.ExcelUtilities;
import com.wma.framework.util.ScreenShot;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.MediaEntityModelProvider;
import com.aventstack.extentreports.Status;

/**
 * <p>This class will be responsible for generating report in Extent along with TestRail.</p>
 * <p>Usage example:-</p>
 * <code><ul>
 * <li>TestRailAndExtentReporter logger = TestRailAndExtentReporter.getInstance(config, testCaseTitle, testDescription);</li>
 * <li>logger.log(Status.PASS, "Step description");</li>
 * <li>logger.log(Status.INFO, "step description");</li>
 * </ul></code>
 *
 */
public class TestRailAndExtentReporter extends ReportManager {
	private static Logger log = Logger.getLogger(TestRailAndExtentReporter.class);
	private ExtentReports extentReport;
	private ExtentTest extentLogger;
	private final String TAB_SPACE = "    ";
	private String testRailComments = "#Comments#\n"+ TAB_SPACE + "Status \t\t Timestamp \t\t\t Details\n";
	private static TestRailAndExtentReporter instance = null;
	private static List<ExtentTest> tests = new ArrayList<>();
	private static List<ExtentTest> allExecutedTests = new ArrayList<>();
	private static Map<Integer, List<ExtentTest>> iterations = new HashMap<>();
	private String sTestCaseTitle = "";
	private ConfigProvider config;
	
	private TestRailAndExtentReporter(ConfigProvider config, String strTestCaseTitle, String description) {
		extentReport = getExtent(config);
		this.config = config;
		this.sTestCaseTitle = strTestCaseTitle;
		//Check whether the test case is already present in the reports. Remove it if is present
		if(!tests.isEmpty()) {
			int index = isTestPresentInReports(strTestCaseTitle);
			if(index >=0) {
				extentReport.removeTest(tests.get(index));
				tests.remove(index);
			}
		}
	    
		extentLogger = extentReport.createTest(strTestCaseTitle, description);
		tests.add(extentLogger);
		allExecutedTests.add(extentLogger);
		log.info("New Test Case created.." + strTestCaseTitle);
	}
	
	/**
	 * Whenever a new test case execution starts, a new install the class is created
	 * 
	 * @param config
	 * @param strTestCaseTitle
	 * @param description
	 * @return
	 */
	
	public static TestRailAndExtentReporter getInstance(ConfigProvider config, String strTestCaseTitle, String description) {
		if(instance == null)
			return new TestRailAndExtentReporter(config, strTestCaseTitle, description);
	   else 
			return instance;
	}
	
	/**
	 * This is method with should be called every time a log is to be added in the HTML as well as TestRail report
	 * 
	 * @param status 
	 * @param description
	 * @throws IOException
	 */
	public void log(Status status, String description) {
		log.info(description);
		if(status.equals(Status.FAIL) && config.getExecutionType().equalsIgnoreCase("UI")) {
			ExcelUtilities excel = new ExcelUtilities(config.getResultTestDataFilePath());
			Map<String, String> testData = excel.getRowWhere(config.getProduct(), "TestCaseTitle", this.sTestCaseTitle);
			String screenshotFile = ScreenShot.getScreenShot(testData.get("TestCaseFunction"));
			String[] temp = screenshotFile.split(Pattern.quote(File.separator));
			screenshotFile = temp[temp.length-2] + File.separator + temp[temp.length-1];
			try {
				MediaEntityModelProvider mediaModel = MediaEntityBuilder.createScreenCaptureFromPath(screenshotFile).build();
				extentLogger.log(status, description, mediaModel);
			} catch (IOException e) {
				extentLogger.log(status, description);
				e.printStackTrace();
			}
		}
		
		else
			extentLogger.log(status, description);
		
		String timeStamp = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ssa").format(Calendar.getInstance().getTime());
		testRailComments += "\n___\n";
		if(status.equals(Status.FAIL))
			testRailComments += "#" + status.toString() + "\t\t" + timeStamp + " \t " + description;
		else
			testRailComments += TAB_SPACE + status.toString() + "\t\t" + timeStamp + " \t " + description;
}
	
/**
 * This method gives the comments for Test Rail. This comments is formattED in a way which will provide the view same as extent HTML eport
 * 
 * @return
 */
	public String getTestRailComments() {
		return testRailComments;
	}
	
	/**
	 *Write the log contents on the HDD
	 */
	public void flush() {
		extentReport.flush();
	}
	 
	private static int isTestPresentInReports(String strTestCaseTitle) {
		int index = -1;
		for(ExtentTest test : tests) {
			index++;
			String testName = test.getModel().getName();
			if(testName.equalsIgnoreCase(strTestCaseTitle))
				return index;
		}
		return -1;
	}
	
	/**
	 * Below two methods are going to be used for iteration details population in the report
	 * Both the methods will be called after each iteration, so that first method will list of all TestCases executed in that iteration
	 * And after each iteration the list will be cleaned.
	 */	
	public static void addIterationDetails(int iteration) {
		List<ExtentTest> executedTests = new ArrayList<>();
		executedTests.addAll(allExecutedTests);
		iterations.put(iteration, executedTests);
		allExecutedTests.clear();
	}
	
	/**
	 * It returns the details of the test cases executed for the current iteration
	 * This method is specially created to be used in the Report Customizer class
	 */
	public static Map<Integer, List<ExtentTest>> getAllIteration() {
		return iterations;
	}
 	 
	/**
	 * Gives the reference of the test case currently being executed
	 */
	public ExtentTest getCurrentExtentTest() {
		return this.extentLogger;
	}
}
