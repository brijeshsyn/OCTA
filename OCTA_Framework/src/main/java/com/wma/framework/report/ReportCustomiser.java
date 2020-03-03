package com.wma.framework.report;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.wma.framework.common.ConfigProvider;
import com.wma.framework.util.TextFileUtils;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

public class ReportCustomiser {
	private static Logger log = Logger.getLogger(ReportCustomiser.class);
	private final File reportFile;
	private ConfigProvider config;
	private Document doc;
	private Map<Integer, List<com.aventstack.extentreports.ExtentTest>> iterations;

	private ReportCustomiser() throws IOException {
		config = ConfigProvider.getInstance();
		iterations = TestRailAndExtentReporter.getAllIteration();
		reportFile = TextFileUtils.getLatestFile(config.getResultFolder(), "html");
		doc = Jsoup.parse(reportFile, "UTF-8");
	}

	/**
	 * This method returns the list of Iteration with the details like iteration#,
	 * testcase count, failed TCs, passedTCs etc....
	 * 
	 * @return
	 */
	private List<ReportIteration> getAllIterationDetails() {
		// Number of iterations/Return is nothing but the number of entries in the
		// iterations object
		int totalNumberOfIterations = iterations.size();
		List<ReportIteration> iterationDetails = new ArrayList<>();
		for (int i = 1; i <= totalNumberOfIterations; i++) {
			// This objects holds the details for the iteration, like iteration#, testcase
			// count, failed TCs, passed TCs etc...
			ReportIteration iteration = new ReportIteration();
			List<ExtentTest> testCases = iterations.get(i);
			iteration.setIterationNo(i);
			iteration.setNumberOfTestsExecuted(testCases.size());

			// Calculate the total time taken for execution the current iteration
			// The time is calculated as the difference of the start time of first TestCase
			// and End of the last test case of the iteration
			// The different is converted into minutes
			ExtentTest firstTestCase = testCases.get(0);
			ExtentTest lastTestCase = testCases.get(testCases.size() - 1);
			Date startTime = firstTestCase.getModel().getStartTime();
			Date endTime = lastTestCase.getModel().getEndTime();
			String timeTaken = "";
			long difference = (endTime.getTime() - startTime.getTime());
			long mins = difference / 60000;
			long seconds = (difference % 60000) / 1000;
			long ms = (difference % 60000) % 1000;
			long hrs = 0;
			if (mins > 60) {
				hrs = mins / 60;
				mins = mins % 60;
				timeTaken = hrs + "h" + mins + "m" + (seconds) + "s" + ms + "ms";
			} else
				timeTaken = mins + "m" + (seconds) + "s" + "ms";
			iteration.setTimeTaken(timeTaken);

			iteration.setSystemName(config.getComputerName());

			// Count the number of the tests passed and failed in the current iteration
			int numberOfTestsPassed = 0;
			int numberOfTestsFailed = 0;
			for (ExtentTest test : testCases) {
				if (test.getModel().getStatus().equals(Status.PASS))
					numberOfTestsPassed++;
				if (test.getModel().getStatus().equals(Status.FAIL))
					numberOfTestsFailed++;
			}
			iteration.setNumberOfTestsPassed(numberOfTestsPassed);
			iteration.setNumberOfTestsFailed(numberOfTestsFailed);
			iterationDetails.add(iteration);
		}
		return iterationDetails;
	}

	/**
	 * This method returns the list of details of TestCases executed in the given
	 * iteration
	 * 
	 * @param iterationNumber
	 * @return
	 */
	private List<IterationTestCase> getTestsForIteration(int iterationNumber) {
		List<ExtentTest> testCases = iterations.get(iterationNumber);
		List<IterationTestCase> testCaseDetails = new ArrayList<>();
		for (ExtentTest testCase : testCases) {
			IterationTestCase iterationTestCase = new IterationTestCase();
			iterationTestCase.setIterationNo(iterationNumber);
			iterationTestCase.setSystemName(config.getComputerName());
			iterationTestCase.setTestCaseTitle(testCase.getModel().getName());
			iterationTestCase.setTestStatus(testCase.getModel().getStatus().toString());
			long min = testCase.getModel().getRunDurationMillis() / 60000;
			long second = (testCase.getModel().getRunDurationMillis() % 60000) / 1000;
			long ms = (testCase.getModel().getRunDurationMillis() % 60000) % 1000;
			iterationTestCase.setTimeTaken(min + "m" + (second) + "s" + ms + "ms");
			iterationTestCase.setId(testCase.getModel().getID());
			testCaseDetails.add(iterationTestCase);
		}
		return testCaseDetails;
	}

	private void editDashboardTile(int tileNo, String tileHeader, String tileContent) {
		int tileno = 0;
		switch (tileNo) {
		case 1:
			tileno = 2;
			break;
		case 2:
			tileno = 5;
			break;
		case 3:
			tileno = 8;
			break;
		case 4:
			tileno = 11;
			break;
		case 5:
			tileno = 14;
			break;
		case 6:
			tileno = 17;
			break;
		default:
			log.info("Invalid tile number provided to edit");
		}

		Element divRow = doc.getElementsByClass("row").get(1);
		Element tile = divRow.getAllElements().get(tileno);
		tile.html(tileHeader + "\r\n" + "         <div class=\"panel-lead\">\r\n" + "          " + tileContent + "\r\n"
				+ "	       </div> ");
	}

	private void addNewTableOnDashboard(String[] cols, List<String[]> data, String tableId) {
		Element dashboard = doc.getElementById("dashboard-view");
		Element firstChildOfDashboard = dashboard.getAllElements().first();

		StringBuilder html = new StringBuilder("");
		html.append("<table id=\"" + tableId + "\" class=\"table table-hover text-centered\" "
				+ "style=\"border-radius: 5px;width: 70%;margin: 0px 50px;float: none;\">\r\n");
		//creating table column/headers
		html.append("<tr>\r\n");
		for(String col : cols)
			html.append("                     <th>" + col + "</th>\r\n");

		html.append("</tr>\r\n");
		//Creating table rows
		int rowCounter = 0;
		for(String[] row : data) {
			rowCounter++;
			html.append("<tr>\r\n");
			html.append("<td><div id=\"iteration" + rowCounter+ "\" style=\"cursor: pointer;\" ><a>"+row[0]+"</a></div></td>\r\n");
			String script = "\r\n$('#iteration"+rowCounter+"').click(function() {\r\n" +
					"                   $('#dashboard-view').toggleClass('hide');\r\n" +
					" 	                 $('#testCasesForIteration" + rowCounter+"').removeClass('hide');\r\n" +
					"                   $('#iterationTestCases').removeClass('hide');\r\n" +
					"                 });\r\n";
			addJavaScript(script);
			for(int i=1; i<row.length; i++)
				html.append("<td>" + row[i] + "</td>\r\n");
			html.append("</tr>\r\n");
		}

		html.append("</table>\r\n");
		firstChildOfDashboard.append(html.toString());
	}

	private String addIterationTestDetails(List<IterationTestCase> testCases, int id) {
		String[] cols = { "Test CaseTitle", "Test Class Name", "Status", "Time taken to Execute", "VM" };
		String html = "<div id=\"testCasesForIteration" + id + "\" class=\"view hide\">";

		StringBuilder myTable = new StringBuilder("");
		myTable.append(
				"<table class=\"table table-hover text-centered\" style=\"border-radius:5px;width: 70%;margin: 0px 50px;float: none;\"\r\n");
		myTable.append("<caption><h5><u>Iteration" + id + " Details</u></h5></caption>");
		// creating table column/headers
		myTable.append("<tr style=\"font-size:16px;\">\r\n");
		for (String col : cols)
			myTable.append("                      <th>" + col + "</th>\r\n");
		myTable.append("</tr>\r\n");

		// Creating table rows
		for (IterationTestCase row : testCases) {
			myTable.append("<tr>\r\n");
			myTable.append("<td>" + row.getTestCaseTitle() + "</td>\r\n");
			// For non-API tests add Class Name for the test case
			if (!config.getExecutionType().toUpperCase().contains("API"))
				myTable.append("<td>" + row.getTestClassName() + "</td>\r\n");

			if (row.getTestStatus().equalsIgnoreCase("fail")) {
				if (id == iterations.size()) {
					myTable.append("<td style=\"color:red;\"><div id=\"failed_" + row.getId() + "\" style=\"cursor: pointer;\"><b>Fail</b></td>\r\n");					
					String script = "$('#failed_" + row.getId() + "').click(function() {\r\n" +
							"      $('a[view=\"test-view\"]').click();\r\n" +
							"      var $scrollTo = $('#test-collection').find('li[test-id=\"" + row.getId()  +
							"\"]';\r\n" + "      $scrollTo.click();\r\n" + "      \r\n"  +
							"      var $container = $('div[class=\"subview-left left\"]');\r\n" +
							"      $container.animate({\r\n" +
							"      scrollTop: $scrollTo.offset().top - $container.offset().top + $container.scrollTop()\r\n" +
							"      });\r\n" + "      \r\n" +
							"      var $container1 = $('div[class=\"subview-righht left\"]',\r\n" +
							"          $scrollTo1 = $('div[class=\"subview-right left\"]').find('tr[status=\"fail\"]');\r\n" +
							"      $container1.animate({\r\n" +
							"       scrollTop: $scrollTo1.offset().top - $container1.offset().top + $container1.scrollTop()\r\n" +
							"      }); \r\n" + 
							"});";
					addJavaScript(script);
				} else
					myTable.append("<td style=\"color:red;\"><b>Fail</b></td>\r\n");
			} else if (row.getTestStatus().equalsIgnoreCase("pass"))
				myTable.append("<td style=\"color:green;\"><b>Pass</b></td>\r\n");
			else
				myTable.append("<td>" + row.getTestStatus() + "</td>\r\n");

			myTable.append("<td>" + row.getTimeTaken() + "</td>\r\n");
			myTable.append("<td>" + row.getSystemName() + "</td>\r\n");

			myTable.append("</tr>\r\n");
		}
		myTable.append("</table>\r\n");

		html += myTable.toString();
		html += "</div>";
		return html;
	}

	/**
	 * Write changes on the file
	 * 
	 * @throws IOException
	 */
	private void flush() {
		String fileName = reportFile.getAbsolutePath();

		try (Writer writer = new PrintWriter(fileName, "UTF-8");) {
			writer.write(doc.html());
			writer.close();
		} catch (IOException e) {
			log.info(e.getMessage());
		}

	}

	private void addIterationCountInReport() {
		editDashboardTile(2, "Iteration Count", Integer.toString(config.getIterations()));
	}

	private void editTimeTakenValueInReport() {
		String totalTimeTaken = "";
		// Get the start time of first test case of first iteration
		Date startTime = iterations.get(1).get(0).getModel().getStartTime();

		// Get the end time of last test case pf last iteration
		List<com.aventstack.extentreports.ExtentTest> lastIterationTests = iterations.get(iterations.size());
		Date endTime = lastIterationTests.get(lastIterationTests.size() - 1).getModel().getEndTime();

		// Total Execution time is the difference of End time and Start time. Convert
		// the difference in minute
		long difference = endTime.getTime() - startTime.getTime();
		long mins = difference / 60000;
		long seconds = (difference % 60000) / 1000;
		long hrs = 0;

		if (mins > 60) {
			hrs = mins / 60;
			mins = mins % 60;
			totalTimeTaken = hrs + "h" + mins + "m" + seconds + "s";
		} else
			totalTimeTaken = mins + " min " + seconds + " sec ";
		editDashboardTile(5, "Time Taken", totalTimeTaken);
	}

	private void addPassFailPercentageInReport() {
		double passPercentage = getPassPercentage();
		double failPercentage = 100 - passPercentage;

		Element divChartRow = doc.getElementById("charts-row");
		Element divChildSection = divChartRow.getElementsByAttributeValue("class", "card-panel nm-v").last();
		String htmlCode = "<div class=\"left panel-name\"> Pass % <div class=\"text\">"
				+ String.format("%.2f", passPercentage) + "</div></div>\r\n" + "        \r\n";
		if ((int) failPercentage < 10)
			htmlCode += "       <div class=\"center panel-name\"> Fail % <div class=\"text\" style=\"margin-right: 10px;\">"
					+ String.format("%.2f", failPercentage) + "</div></div>";
		else if ((int) failPercentage < 100)
			htmlCode += "         <div class=\"center panel-name\"> Fail % <div class=\"text\"style=\"margin-right: 5px;\">"
					+ String.format("%.2f", failPercentage) + "</div></div>";
		else
			htmlCode += "         <div class=\"center panel-name\"> Fail % <div class=\"text\">"
					+ String.format("%.2f", failPercentage) + "</div></div>";
		divChildSection.html(htmlCode);
	}

	private double getPassPercentage() {
		int totalTestCount = iterations.get(1).size();
		int passedTestCounter = 0;
		for(int i=1; i<=iterations.size(); i++) {
			List<ExtentTest> tests = iterations.get(i);
			for (ExtentTest test : tests) {
				if (test.getModel().getStatus().equals(Status.PASS))
					passedTestCounter++;
			}
		}

		double passedTCs = passedTestCounter;
		return(passedTCs*100)/totalTestCount;
	}

	private void setFontSize(int sizeInPx) {
		doc.getElementsByTag("body").first().attr("style", "font-size: " + sizeInPx + "px:");
	}

	private void addIterationDetailsTableInReport() {
		String[] cols = {"Iteration No.", "Time Taken", "No. of Tests Executed", "No. of Tests Passed", "No. of Tests Failed", "VM"};
		List<ReportIteration> iterationList = getAllIterationDetails();
		List<String[]> rows = new ArrayList<>();
		for(ReportIteration iteration : iterationList) {
			String[] row = iteration.toStringArray();
			rows.add(row);
		}
		addNewTableOnDashboard(cols, rows, "IterationDetails");
	}

	private void addTestCaseForEachIterationInReport() {
		int iterationCount = iterations.size();
		Element containerDiv = doc.getElementsByClass("container").first();

		StringBuilder html = new StringBuilder("<div id=\"iterationTestCases\" class=\"view hide\">\n");
		html.append("<div id=\"iterationList\" class=\"card-panel nm-v\">");

		StringBuilder iterationTable = new StringBuilder(
				"<table class=\"table table-hover\" style=\"border-radius: 5px;width: auto;margin: 0px 50px;float: none:\">\n");
		for (int i = 1; i <= iterationCount; i++) {
			iterationTable.append("<tr><td><div id=\"iteration_" + i + "\" style=\"cursor: pointer;\"><a>Iteration " + i
					+ "</a></div></td></tr>\n");
			String script = "$('#iteration_" + i + "').click(function() {\r\n"
					+ "            $('#iterationTestCases').children().each(function()) {\r\n"
					+ "	             $(this).addClass('hide');\r\n" + "            ));\r\n"
					+ "            $('#testCasesForIteration" + i + "').removeClass('hide');\r\n" + "     });";
			addJavaScript(script);
		}
		iterationTable.append("</table></div>");
		html.append(iterationTable);

		for (int i = 1; i <= iterationCount; i++) {
			List<IterationTestCase> testCasesList = getTestsForIteration(i);
			html.append(addIterationTestDetails(testCasesList, i));
		}
		html.append("</div>");
		containerDiv.append(html.toString());
	}

	private void addJavaScript(String script) {
		Element scriptTag = doc.getElementsByTag("script").last();
		String existingScript = scriptTag.html();
		scriptTag.html(existingScript + script);
	}
	private void editNavigationPanel() {
		String logoBinary = "";
		String executionStartTime = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a").format(config.getExecutionStartedAt());
		String html = "<div class=\"nav-wrapper\"> \r\n "+
				"     <a href=\"http://aqrlive/Pages/Default.aspx\"class=\"brand-logo black\" target=\"_blank\">" +
				"      <img src=\""+logoBinary+"\" style=\"margin-left: -5px;height: 50px;width: 50px;\">" +
				"      </a>\r\n" +
				"<!--slideout menu --> \r\n" +
				"<ul id=\"slide-out\" class=\"side-nav fixed hide-on-med-and-down\"> \r\n" +
				" <li class=\"wave-effect active\"><a href=\"#!\"onclick=\"configureView(-1);chartsView('dashboard');\" view=\"dashboard-view\"><i class=\"material-icons\">track_changes</i></a></li> \r\n" +
				" <li class=\"waves-effect\"><a href=\"#!\" view=\"test-view\" onclick=\"configureView(0);chartsView('test');><i class=\"material-icons\">dashboard</i></a></li>\r\n" +
				"</ul> \r\n" +
				"<!-- report name --> \r\n" +
				"    <span class=\"report-name\" style=\"font-size: 20px;position: absolute;\"><b>" + config.getProduct() + " Automation Execution Report</b></span>\r\n" +
				"<ul id=\"nav-mobile\" class=\"right hide-on-med-and-down nav-right\"> \r\n" +
				"	<li> <span class=\"label\">Browser : " + config.getBrowserName().toUpperCase()+ "</span> </li> \r\n" +
				"	<li> <a href=\""+config.getAppUrl()+"\"<span class=\"label\">Env.:" + config.getEnvName() + "</span></a></li>\r\n" +
				"	<li> <a href=\"#!\"> <span class=\"label suite-time\">"+executionStartTime+"</span> </a> </li> \r\n"+
				"</ul>"+
				"</div>";				
		
		Element nav = doc.getElementsByTag("nav").first();nav.html(html);
	}

	private void addLinkToLastIterationOnTestsPage() {
		String html = "<!-- Iterations -->\r\n" +
				"     <div id=\"toggle-test-view-charts\" class=\"chip transparent\"> \r\n" +
				"        <a class=\"blue-text\" id=\"last_iteration\" alt=\"Last Iteration\" title=\"Last Iteration\"> <i class=\"material-icons\">list</i> Last Iteration </a> \r\n" +
				"      </div> \r\n" +
				"     <!--Iterations -->";
		String script = "$(\"#last_iteration\").click(function() {\r\n" +
				"    $('a[view=\"dashboard-view\"]').click();\r\n" +
				"    $('#iteration" + iterations.size() + "').click();\r\n" +
				" });";
		Element section = doc.getElementById("controls");
				section.getAllElements().get(1).append(html);
		addJavaScript(script);

		Element enableDashboard = doc.getElementById("enable-dashboard");
		enableDashboard.attr("class", "blue-text");
	}

	private void createCopyOfReport(int iterationNumber) throws IOException {
		if (iterationNumber < ConfigProvider.getInstance().getIterations())
			log.info("Creating copy of the report");
		File reportFile = TextFileUtils.getLatestFile(ConfigProvider.getInstance().getResultFolder(), "html");
		String fileName = reportFile.getName().split(Pattern.quote("."))[0] + "_" + iterationNumber + ".html";
		log.info("Report file Name :" + ConfigProvider.getInstance().getResultFolder() + "\\" + fileName);
		FileUtils.copyFile(reportFile, new File(ConfigProvider.getInstance().getResultFolder() + "\\" + fileName));
	}

	public static void customise() {
		try {
			ReportCustomiser rc = new ReportCustomiser();
			List<ReportIteration> list = rc.getAllIterationDetails();
			for (ReportIteration ri : list) {
				log.info(ri.toString());
			}
			rc.setFontSize(15);
			rc.addIterationCountInReport();
			rc.editTimeTakenValueInReport();
			rc.addPassFailPercentageInReport();
			rc.addIterationDetailsTableInReport();
			rc.addTestCaseForEachIterationInReport();
			rc.editNavigationPanel();
			rc.addLinkToLastIterationOnTestsPage();
			rc.flush();

			// Creating copy of the generated report after each iteration
			if (ConfigProvider.getInstance().getIterations() > 1)
				rc.createCopyOfReport(list.size());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}