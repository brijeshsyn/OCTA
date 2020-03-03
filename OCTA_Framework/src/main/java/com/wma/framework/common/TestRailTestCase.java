package com.wma.framework.common;

public class TestRailTestCase {
	private String sRunId;
	private String sCaseId;
	private String sTcId;
	private String sTcTitle;
	private String sTcFunction;
	public String getsRunId() {
		return sRunId;
	}
	public TestRailTestCase setRunId(String sRunId) {
		this.sRunId = sRunId;
		return this;
	}
	public String getCaseId() {
		return sCaseId;
	}
	public TestRailTestCase setCaseId(String sCaseId) {
		this.sCaseId = sCaseId;
		return this;
	}
	public String getTcId() {
		return sTcId;
	}
	public TestRailTestCase setTcId(String sTcId) {
		this.sTcId = sTcId;
		return this;
	}
	public String getTcTitle() {
		return sTcTitle;
	}
	public TestRailTestCase setTcTitle(String sTcTitle) {
		this.sTcTitle = sTcTitle;
		return this;
	}
	public String getTcFunction() {
		return sTcFunction;
	}
	public TestRailTestCase setTcFunction(String sTcFuction) {
		this.sTcFunction = sTcFuction;
		return this;
	}
	
}