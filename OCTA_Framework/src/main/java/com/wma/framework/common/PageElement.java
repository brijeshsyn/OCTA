package com.wma.framework.common;

import org.openqa.selenium.By;

public interface PageElement {
	
	public By getBy(String... placeholders);
	public Frame getFrame();
	public ByType getType();
	public String getExpression();
	
}