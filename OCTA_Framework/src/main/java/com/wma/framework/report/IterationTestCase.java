package com.wma.framework.report;

import com.wma.framework.common.ConfigProvider;
import com.wma.framework.util.ExcelUtilities;

public class IterationTestCase {
	private int iterationNo;
	private String timeTaken;
	private String testCaseTitle;
	private String testStatus;
	private String systemName;
	private int id;
	private String testClassName;
	
	public int getIterationNo() {
		return iterationNo;
	}
	public void setIterationNo(int iterationNo) {
		this.iterationNo = iterationNo;
	}
	public String getTimeTaken() {
		return timeTaken;
	}
	public void setTimeTaken(String timeTaken) {
		this.timeTaken = timeTaken;
	}
	public String getTestCaseTitle() {
		return testCaseTitle;
	}
	public void setTestCaseTitle(String testCaseTitle) {
		this.testCaseTitle = testCaseTitle;
		this.testClassName = getTestClassName(testCaseTitle);
	}
	public String getTestStatus() {
		return testStatus;
	}
	public void setTestStatus(String testStatus) {
		this.testStatus = testStatus;
	}
	public String getSystemName() {
		return systemName;
	}
	
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}
	
	public void setId(int i) {
		this.id = i;
	}
	public int getId() {
		return this.id;
	}
	
	public String[] toStringArray() {
		String[] array = { testCaseTitle, testStatus, timeTaken, systemName };
		return array;
	}
	public String toString() {
		return iterationNo + "," + testCaseTitle + "," + testClassName + "," + timeTaken + "," + systemName;
	}
	
	public String getTestClassName() {
		return testClassName;
	}
	
	private String getTestClassName(String strTcTitle) {
		ExcelUtilities excel = new ExcelUtilities(ConfigProvider.getInstance().getTestDataFilePath());
		return excel.getRowWhere(ConfigProvider.getInstance().getProduct(), "TestCaseTitle", strTcTitle).get("TestCaseFunction");
	}
}