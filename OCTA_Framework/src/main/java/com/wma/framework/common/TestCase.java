package com.wma.framework.common;

public class TestCase {
	private String sTcId;
	private String sTcTitle;
	private String sTcFunction;
	private String sEnabled;
	private String sTestStatus;
	private String sExecuted;
	public String getsTcId() {
		return sTcId;
	}
	public TestCase setTcId(String sTcId) {
		this.sTcId = sTcId;
		return this;
	}
	public String getTcTitle() {
		return sTcTitle;
	}
	public TestCase setTcTitle(String sTcTitle) {
		this.sTcTitle = sTcTitle;
		return this;
	}
	public String getTcFunction() {
		return sTcFunction;
	}
	public TestCase setTcFunction(String sTcFunction) {
		this.sTcFunction = sTcFunction;
		return this;
	}
	public String getEnabled() {
		return sEnabled;
	}
	public TestCase setEnabled(String sEnabled) {
		this.sEnabled = sEnabled;
		return this;
	}
	public String getTestStatus() {
		return sTestStatus;
	}
	public TestCase setTestStatus(String sTestStatus) {
		this.sTestStatus = sTestStatus;
		return this;
	}
	public String getExecuted() {
		return sExecuted;
	}
	public TestCase setExecuted(String sExecuted) {
		this.sExecuted = sExecuted;
		return this;
	}
	
	
}