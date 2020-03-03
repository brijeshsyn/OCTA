package com.wma.framework.common;

public enum Frame {
	DEFAULT(null, null),
	//TEST_FRAME1("testFrame1", DEFAULT),
	//TEST_FRAME2("testFrame2", TEST_FRAME1),
    //TEST_FRAME3("testFrame3", TEST_FRAME2),
	;
	
	private String NAME;
	private Frame PARENT;
	private Frame(String name, Frame parent) {
		this.NAME = name;
		this.PARENT = parent;
	}
	
	public String getValue() {
		return NAME;
	}
	
	public Frame getParent() {
		return PARENT;
	}
	
}