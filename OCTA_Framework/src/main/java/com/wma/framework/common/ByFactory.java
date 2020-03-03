package com.wma.framework.common;

import org.openqa.selenium.By;

public class ByFactory {

	private ByFactory() {
		// This private constructor is to restrict creation of any object pf this class
	}

	/**
	 * This method gives the object of type 'By', which is used by selenium to find 
	 * element, based on the value provided as ByType object 
	 *
	 * @param byType
	 * @param expression
	 * @param placeholders
	 * @return
	 */
	public static By getBy(ByType byType, String expression, String...placeholders) {
		String value = String.format(expression, (Object[]) placeholders);
		switch (byType) {
		case ID:
			return By.id(value);
		case NAME:
			return By.name(value);
		case LINK_TEXT:
			return By.linkText(value);
		case PARTIAL_LINK_TEXT:
			return By.partialLinkText(value);
		case CLASS_NAME:
			return By.className(value);
		case TAG_NAME:
			return By.tagName(value);
		case CSS_SELECTOR:
			return By.cssSelector(value);
		case XPATH:
			return By.xpath(value);
		}
		return null;
	}
}

