package com.wma.framework.driver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.FailurePolicy;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import com.wma.framework.common.ConfigProvider;
import com.wma.framework.common.TestCase;
import com.wma.framework.common.TestRailTestCase;
import com.wma.framework.report.ReportCustomiser;
import com.wma.framework.report.TestRailAndExtentReporter;
import com.wma.framework.util.ExcelUtilities;
import com.wma.framework.util.TextFileUtils;

public class DriverScript {
	
	private static Logger log = Logger.getLogger(DriverScript.class);
	private ConfigProvider config;
	private String packageName;
	private String productHomeDir = System.getProperty("user.dir"); 
 
	public DriverScript(ConfigProvider config, String scriptPackage) {
		this.config = config;
		this.packageName = scriptPackage;
	}
	
	/**
	 * To start the executio nof teh test case 
	 */
	public void startExecution() {
		File myFile = new File(config.getResultFolder());
		
		if(!myFile.exists())
			myFile.mkdirs();
		
	    String testRail = config.getTestRail();
	    
	    //Creating a copy of the TestData file in teh result folder
	    createCopyOfTestFile();
	    
	    //Create the testing suite and start the execution. The below loop executes the test cases and looks for failed cases,
	    //runs the failed test cases for the number of iteration given in the TestController file
	    for(int i=1; i<= config.getIterations(); i++) {
	    	List<TestCase> listTestCases = readTestCases(config);
	    	 
	    	List<TestRailTestCase> listTestRailTestCases = new ArrayList<>();
	    	if(testRail.equalsIgnoreCase("Yes")) {
	    		//To get latest file for the logged in user in testtrail folder 
	    		File latestfile = TextFileUtils.getLatestFileForSysUserFromDir(config.getTestRailFilePath());
	    		String testRailMainFileName = config.getTestRailFilePath() + System.getProperty("file.separator") + latestfile.getName();
	    		
	    		listTestRailTestCases = readTestRailtestCases(config, testRailMainFileName);
	    	}
	    	
	    	//Check whether there is any test case to be executed or not
	    	if(listTestCases.isEmpty() && listTestRailTestCases.isEmpty()) {
	    		log.info("There is no test case to execute");
	    		break;
	    	}
	    	
	    	//Creating TestNG suite 
	    	List<XmlSuite> suites = new ArrayList<>();
	    	XmlSuite suite = new XmlSuite();
	    	String sSuiteName = "Suite";
	    	suite.setName(sSuiteName);
	    	suite.setConfigFailurePolicy(FailurePolicy.CONTINUE);
	    	
	    	// Set parallel execution property for teh test suite 
	    	if(config.isParallelExecution()) {
	    		suite.setParallel(ParallelMode.TESTS);
	    		suite.setThreadCount(config.getThreadCount());
	    	}
	    	
	    	if(testRail.equalsIgnoreCase("No") && !listTestCases.isEmpty())
                configureTestNgForTestFromExcel(listTestCases, suite);
            else if(testRail.equalsIgnoreCase("Yes") && !listTestRailTestCases.isEmpty())    
            	configureTestNgForTestsRail(listTestRailTestCases, suite);
            else {
            	log.info("There are no test cases to execute");
            	break;
            }
             
            //Creating TestNG object and adding suites to it 
            suites.add(suite);
            TestNG tng = new TestNG();
            tng.setXmlSuites(suites);
            log.info("before running testing for iteration" + i);
            tng.run();
            log.info("after running testing for iteration" + i);
            
            //Set the iteration details for the current iteration (Re-run)
            TestRailAndExtentReporter.addIterationDetails(i);
	          
            // Customize the default Extent Report
            ReportCustomiser.customise();
	    }
	    log.info("Execution completed successfully");
	    System.exit(0);
	}
	
	/**
	 * Configure the testNg Suit for the test cases, fetched from Excel
	 * @param listTestCases
	 * @param suite
	 */
	private void configureTestNgForTestFromExcel(List<TestCase> listTestCases, XmlSuite suite) {
		int count = 0;
		Map<String, String> testClasses = getAllTestClasses();
		for(TestCase testCase : listTestCases) {
			//Creating Test And Adding Class to it.
			XmlTest test = new XmlTest(suite);
			test.setName("Test_" + count++);
			test.addParameter("TCTitle", testCase.getTcTitle());
			List<XmlClass> classes = new ArrayList<>();
			try {
				String testClass = testClasses.get(testCase.getTcFunction().trim());
				XmlClass xmlClass = new XmlClass(testClass);
				log.info(testCase.getTcTitle() + " " + testClass);
				classes.add(xmlClass);
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
			test.setXmlClasses(classes);
		}
		log.info("Total Test Case Count : " + listTestCases.size());
	}
	
	/**
	 * Configure the testNg Suit for the test cases, fetched from the Test Rail
	 * @param listTestRailTestCases
	 * @param suite
	 */
	private void configureTestNgForTestsRail(List<TestRailTestCase> listTestRailTestCases, XmlSuite suite) {
		int count = 0;
		Map<String, String> testClasses = getAllTestClasses();
		for(TestRailTestCase testRailTestCase : listTestRailTestCases) {
			//Creating Test for each test class And Adding Class to it.
			XmlTest test = new XmlTest(suite);
			test.setName("Test_"+count++);
			//Adding parameters required to run based on Test Rail test cases
			test.addParameter("RunId", testRailTestCase.getsRunId());
			test.addParameter("CaseID", testRailTestCase.getCaseId());
			test.addParameter("TCID", testRailTestCase.getTcId());
			test.addParameter("TCTitle", testRailTestCase.getTcTitle());
			
			List<XmlClass> classes = new ArrayList<>();
			try {
				String testClass = testClasses.get(testRailTestCase.getTcFunction().trim());
				XmlClass xmlClass = new XmlClass(testClass);
				//XmlClass xmlClass = new XmlClasspackageName + testRailTestCase.getTcFunction().trim());
				log.info(testRailTestCase.getTcTitle() + " " + packageName + testRailTestCase.getTcFunction());
				classes.add(xmlClass);
			}
			catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
		    }
			test.setXmlClasses(classes);
		}
		log.info("Total Test Case Count : " + listTestRailTestCases.size());
	}
	
	/**
	 * To read the Test Cases from the excel (ProductionTestData.xlsx)
	 * @param config
	 * @return Returns the list of Test Case
	 * @author singhb
	 */
	private List<TestCase> readTestCases(ConfigProvider config) {
		log.info("Reading test cases from Excel...");
		String fileSeparator = System.getProperty("file.separator");
		List<TestCase> testCases = new ArrayList<>();
		String testDataFile = config.getTestDataFile().split(Pattern.quote("."))[0];
		String testFile = config.getResultFolder() + fileSeparator + testDataFile + "_" + config.getTimeStamp() + ".xlsx";
		ExcelUtilities excel = new ExcelUtilities(testFile);
		String sheetName = config.getProduct();
		int totalTcCount = excel.getRowCount(sheetName);
		for(int i=1; i<totalTcCount; i++) {
			Map<String, String> tc = excel.getRowData(i, sheetName);
			if(tc.isEmpty())
				continue;
			
			if(tc.get("Execute").equalsIgnoreCase("Yes") && !(tc.get("Status").equalsIgnoreCase("Pass"))) {
				TestCase testCase = new TestCase();
				testCase.setEnabled(tc.get("Enabled"))
                .setTcFunction(tc.get("TestCaseFunction"))
                .setTcId(tc.get("TCID"))
                .setTcTitle(tc.get("TestCaseTitle"))
                .setTestStatus(tc.get("Status"));
                testCases.add(testCase);
			}
		}
                
        return testCases;
	}
	
	/**
	 * To read the test Cases from excel and test rail txt. It creates a mapping for each test case in the test rail txt 
	 * to the test cases in the excel file 
	 * @param config
	 * @param sTestRailFile
	 * @param Returns the list of test case 
	 * @author singhb
	 */
	private List<TestRailTestCase> readTestRailtestCases(ConfigProvider config, String sTestRailFile) {
		log.info("Reading test cases from Test Rail...");
		String testDataFile = config.getTestDataFile().split(Pattern.quote("."))[0];
		String testFile = config.getResultFolder() + "\\" + testDataFile + "_" + config.getTimeStamp() + ".xlsx";
		
		List<TestRailTestCase> testCases = new ArrayList<>();
		Map<String, String> tcMap = new HashMap<>();
	    List<String> lines = new ArrayList<>();
	    try {
	    	lines = Files.readAllLines(Paths.get(sTestRailFile), Charset.defaultCharset());
	    } catch (IOException e) {
	    	log.error(e.getMessage());
	    	e.printStackTrace();
	    	return new ArrayList<>();
	    }
	    
	    if(lines.isEmpty()) {
	    	String sheetName = config.getProduct();
	    	ExcelUtilities excel = new ExcelUtilities(testFile);
	    	int totalTcCount = excel.getRowCount(sheetName);
	    	for(int i=1; i<=totalTcCount; i++) {
	    		Map<String, String> tc = excel.getRowData(i, sheetName);
	    		if(tc.get("Execute").equalsIgnoreCase("Yes") && !(tc.get("Status").equalsIgnoreCase("Pass"))) {
	    			tcMap.put(tc.get("TestCaseTitle").trim(), tc.get("TestCaseFunction").trim());
	    		}
	    	}
	    	
	    	for(String line : lines) {
	    		String[] values = line.split(",");
	    		String tcTitle ="";
	    		if(values.length > 4) {
	    			for(int i=3; i<values.length; i++)
	    				tcTitle += ","+values[i];
	    			tcTitle = tcTitle.replaceFirst(Pattern.quote(","), "");
	    		} else
	    			tcTitle = tcTitle.replaceFirst(Pattern.quote(","),"");
	    		if(tcMap.containsKey(tcTitle)) {
	    			TestRailTestCase testCase = new TestRailTestCase();
	    			testCase.setRunId(values[0])
	    			.setCaseId(values[1])
	    			.setTcId(values[2])
	    			.setTcTitle(tcTitle)
	    			.setTcFunction(tcMap.get(tcTitle).trim());
	    			testCases.add(testCase);
	    		}
	    	}
	    }
	    
	    return testCases;
	   
	}
	
	/**
	 * Create a copy of the Testdata excel file in the Reports/Result folder
	 * The test case is read is read from this copy and the status is also update in this copy of test data 
	 */
	private void createCopyOfTestFile() {
		String fileSeparator = System.getProperty("file.separator");
	    
		try {
			File destDir = new File(config.getResultFolder());
			File srcFile = new File(config.getTestDataFilePath());
			FileUtils.copyFileToDirectory(srcFile, destDir);
			File newFile = new File(config.getResultFolder() + fileSeparator + config.getTestDataFile());
			boolean b = newFile.renameTo(new File(config.getResultFolder() + fileSeparator + config.getTestDataFile().split(Pattern.quote("."))[0] + "_" + config.getTimeStamp() + ".xlsx"));
			if(b)
				log.info("File copied and Renamed successfully");
			else
				log.error("File could not be copied or renamed");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// Search all the class files from the project folder 
	public Map<String, String> getAllTestClasses() {
		Map<String, String> testClasses = new HashMap<>();
		String fileSeparator = System.getProperty("file.separator");
		String srcFolder = productHomeDir + fileSeparator + "src\\main\\java" + fileSeparator;
		
		if(!new File(srcFolder).exists())
			srcFolder = srcFolder.replaceAll("src", "classes");
		if(!new File(srcFolder).exists())
			log.error("The specified folder for the class files , doesn't exist");
		
	String dir = srcFolder + packageName.replace(".", System.getProperty("file.separator"));
	
	File[] directories = new File(dir).listFiles(File::isDirectory);
	if(directories==null) {
		dir = dir.replace("src", "classes");
        directories = new File(dir).listFiles(File::isDirectory);
	}
	
	if(directories.length > 0) {
		for(File d : directories) {
			setClasses(testClasses, srcFolder, d);
	}
 }
 
	 setClasses(testClasses, srcFolder, new File(dir));
	 
	 return testClasses;
}

	// Set the test script class names with their package names 
	//test script class name is the key and the same class with package included is the value 
	private void setClasses(Map<String, String> testClasses, String srcFolder, File d) {
		File[] files = d.listFiles(File::isFile);
		for(File f: files ) {
			String key = f.getName().replace(".java", "").replace(".class", "");
			String value = f.getPath().replace(srcFolder, "").replace(System.getProperty("file.separator"), ".").replace(".java", "").replace(".class", "");
			testClasses.put(key, value);
		}
	}

}
