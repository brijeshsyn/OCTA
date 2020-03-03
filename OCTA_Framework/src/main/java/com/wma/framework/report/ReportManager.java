package com.wma.framework.report;

import java.io.File;

import org.apache.log4j.Logger;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.wma.framework.common.*;

/**
* ReportManager class is responsible for the initialization of the Extent Report and provide the access to the <pre>ExtentReports</pre> object
* @author singhb
*
*/
public abstract class ReportManager {
	private static Logger log = Logger.getLogger(ReportManager.class);
	private static ExtentReports tsmReport;
	
	protected static ExtentReports getExtent(ConfigProvider config) {
		if (tsmReport != null)
			return tsmReport;
		tsmReport = new ExtentReports();
		tsmReport.attachReporter(getHtmlReporter(config));
		return tsmReport;
	}
	
	/**
	 * To create and get the instance of ExtentHtmlReporter
	 * @param config
	 * @return Returns the instance of ExtentHtmlReporter
	 */
     protected static ExtentHtmlReporter getHtmlReporter(ConfigProvider config) {
    	 ExtentHtmlReporter htmlReporter;
    	 String filePath = config.getResultFolder() + File.separator + config.getProduct() + "_Automation_Report_" + config.getTimeStamp() + ".html";
    	 log.info("Html Report : " + filePath);
    	 htmlReporter = new ExtentHtmlReporter(filePath);
    	 
    	 // make the charts visible on report open
    	 htmlReporter.config().setJS("$(document).ready(function() {\r\n" +
    	                             	" $(\"a[view='dashboard-view']\").click();\r\n" + 
    			 						"});");
    	 htmlReporter.config().setChartVisibilityOnOpen(true);
    	 htmlReporter.config().setTheme(Theme.DARK);
    	 htmlReporter.config().setDocumentTitle(config.getProduct() + "Report");
    	 htmlReporter.config().setReportName(config.getProduct() + " AUtomation Execution Report");
    	 return htmlReporter;
      }
     
 }