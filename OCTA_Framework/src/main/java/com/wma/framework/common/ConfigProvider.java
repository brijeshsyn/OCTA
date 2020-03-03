package com.wma.framework.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.sun.jna.platform.win32.Secur32;
import com.sun.jna.platform.win32.Secur32Util;
import com.wma.framework.util.ExcelUtilities;

public class ConfigProvider {
	private static Logger log = Logger.getLogger(ConfigProvider.class);
	private static ConfigProvider config = null;
	private static final String USER_DETAILS_SHEET_NAME = "UserDetails";
	private final String product;
	private final String appUrl;
	private final String envName;
	private final int iterations;
	private final String testRail;
	private final String executionType;
	private final String browserName;
	private final String logLocation;
	private final String testDataFile;
	private final String testRailFilePath;
	private final int defaultTimeOut;
	private final String resultFolder;
	private final String timeStamp;
	private final String screenshotFolder;
	private final Date executionStartedAt;
	private static String frameworkFolderPath;
	private final boolean parallelExecution;
	private final int threadCount;
	private final String appiumConfigFilePath;
	private final String dbServerName;
	private final String remoteWebDriverConfigFilePath;
	private String userName;
	private String password;

	private static String rootDir = System.getProperty("user.dir");
	private static String fileSeparator = System.getProperty("file.separator");	
	private String sytemUserName = System.getProperty("user.name");

	private String year = new SimpleDateFormat("yyyy").format(Calendar.getInstance().getTime());
	private String month = new SimpleDateFormat("MMM").format(Calendar.getInstance().getTime());
	private String day = new SimpleDateFormat("dd").format(Calendar.getInstance().getTime());
	
	public static ConfigProvider getInstance() {
		if (config == null) {
			try {

			} catch (Exception e) {
				log.error(e);
				return null;
			}

			File file = new File(rootDir + fileSeparator + "Resources" + fileSeparator + "Configuration" + fileSeparator
					+ "wmaConfig.properties");
			String testControllerFile = "";

			try {
				testControllerFile = file.getCanonicalPath();
				log.info("Test Configuration file location : " + testControllerFile);
				if(!file.exists()) {
					String msg = "wmaConfig.properties file could not be found at location : " + testControllerFile;
					log.error(msg);
					System.err.print(msg);
					System.exit(1);
				}

				Properties prop = new Properties();
				prop.load(new FileInputStream(new File(testControllerFile)));
				config = new ConfigProvider(prop);
				config.setUserPassword(config.getTestDataFilePath());
				log.info("Configured Username : " + config.getUserName());
			} catch(NullPointerException e) {
				log.error("Execution is being stopped...");
				System.exit(1);
			} catch(IOException e) {
				log.error(e);
			}
			return config;
		}
		return config;
	}

	private ConfigProvider(Properties prop) {
		configureLog4jAppender();
		log.info("Initialization of configurations...");

		this.executionStartedAt = new Date();
		this.product = prop.getProperty("ProductName");
		this.appUrl = prop.getProperty("App_Url");
		this.envName = prop.getProperty("Env_Name");

		this.iterations = parseInt(prop.getProperty("Iterations"));

		this.testRail = prop.getProperty("TestRail");
		this.executionType = prop.getProperty("ExecutionType");
		this.browserName = prop.getProperty("BrowserName");
		this.logLocation = prop.getProperty("LogLocation");
		this.testDataFile = prop.getProperty("TestDataFile");
		this.testRailFilePath = prop.getProperty("TestRailFilePath");
		this.defaultTimeOut = parseInt(prop.getProperty("DefaultTimeOut"));

		if(!this.logLocation.isEmpty())
			this.resultFolder = this.logLocation;
		else {
			this.resultFolder = rootDir + fileSeparator + "Resources" + fileSeparator + "Result" + fileSeparator + year
					+ fileSeparator + month + fileSeparator + day;
			log.info("Log location is not provided in the config file. therefore setting it to the default location : "
		            + this.resultFolder); 
		}
		
		this.timeStamp = new SimpleDateFormat("HHmmssa").format(Calendar.getInstance().getTime());
		this.screenshotFolder = this.resultFolder + fileSeparator + "screenshots";
		this.parallelExecution = prop.getProperty("ParallelExecution").equalsIgnoreCase("Yes") ? true : false;
		this.threadCount = parallelExecution ? parseInt(prop.getProperty("ThreadCount")) : -1;
		this.appiumConfigFilePath = frameworkFolderPath + fileSeparator + "Resources" + fileSeparator + "Configuration"
				+ fileSeparator + "appium.properties";
		this.dbServerName = prop.getProperty("DB_ServerName");
		this.remoteWebDriverConfigFilePath = frameworkFolderPath + fileSeparator + "Resources" + fileSeparator 
				+ "Configuration" + fileSeparator + "remoteDriverConfig.properties";
		
		log.info("Environment :" + this.envName);
		log.info("# Iterations : " + this.iterations);
		log.info("Browser : " + this.browserName);
		log.info("Parallel Execution " + parallelExecution);
		if (parallelExecution)
			log.info("Thread Count : " + threadCount);
		log.info("TestRail : " + testRail);
		log.info("TestData file : " + testDataFile);
		
	}
	private void setUserPassword(String filePath) {
		ExcelUtilities excel = new ExcelUtilities(filePath);
		if(excel.isSheetPresent(USER_DETAILS_SHEET_NAME)) {
			Map<String, String> rowData = excel.getRowData(1, USER_DETAILS_SHEET_NAME);
			password = rowData.get("Password");
			userName = rowData.get("UserId");
		}
		else 
			System.err.print("Sheet Named : " + USER_DETAILS_SHEET_NAME + " doesn't exist in the test data file");
	}

	/*
	 * Configre the Log4J Appender, so that the logs can be shown on Console as well as stored on a file
	 */
	private void configureLog4jAppender() {
		ConsoleAppender console = new ConsoleAppender();	//Create Appender
		String PATTERN = "%d %-5p [%t] %C{2} {%L:%L} - %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.INFO);
		console.activateOptions();

		// add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);		
	}

	private int parseInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch(NumberFormatException e) {
			log.error("Invalid number format");
			return 0;
		}
	}

	public static String getUserDetailsSheetName() {
		return USER_DETAILS_SHEET_NAME;
	}

	public String getProduct() {
		return product;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public String getEnvName() {
		return envName;
	}

	public int getIterations() {
		return iterations;
	}

	public String getTestRail() {
		return testRail;
	}

	public String getExecutionType() {
		return executionType;
	}

	public String getBrowserName() {
		return browserName;
	}

	public String getLogLocation() {
		return logLocation;
	}

	public String getTestDataFile() {
		return testDataFile;
	}

	public String getTestRailFilePath() {
		return testRailFilePath;
	}

	public int getDefaultTimeOut() {
		return defaultTimeOut;
	}

	public String getResultFolder() {
		return resultFolder;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public String getScreenshotFolder() {
		return screenshotFolder;
	}

	public Date getExecutionStartedAt() {
		return executionStartedAt;
	}

	public String getFrameworkFolderPath() {
		return frameworkFolderPath;
	}

	public boolean isParallelExecution() {
		return parallelExecution;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public String getAppiumConfigFilePath() {
		return appiumConfigFilePath;
	}

	public String getDbServerName() {
		return dbServerName;
	}

	public String getRemoteWebDriverConfigFilePath() {
		return remoteWebDriverConfigFilePath;
	}

	public String getPassword() {
		return password;
	}

	public static String getRootDir() {
		return rootDir;
	}

	public static String getFileSeparator() {
		return fileSeparator;
	}

	public String getUserName() {
		return userName;
	}

	public String getSytemUserName() {
		return sytemUserName;
	}
	
	public String getResultTestDataFilePath() {
		String file = testDataFile.split(Pattern.quote("."))[0];
		return getResultFolder() + fileSeparator + file + "_" + getTimeStamp() + ".xlsx";
	}
	
	public String getTestDataFilePath() {
		return System.getProperty("user.dir") + fileSeparator + "Resources" + fileSeparator + "TestData" + fileSeparator + testDataFile;
	}
	
	public String getComputerName() {
		Map<String, String> env = System.getenv();
		if(env.containsKey("COMPUTERNAME"))
			return env.get("COMPUTERNAME");
		else if(env.containsKey("HOSTNAME"))
			return env.get("HOSTNAME");
		else
			return "Unknown Computer";
	}
	
	public String getUserFirstAndLastName() {
		return Secur32Util.getUserNameEx(Secur32.EXTENDED_NAME_FORMAT.NameDisplay);
	}

	public void runWindowsCommand(String commandForTunnel) {
		// TODO Auto-generated method stub
		
	}

}
