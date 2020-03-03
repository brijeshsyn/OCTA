package com.wma.framework.common;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CommonWebActions<T extends PageElement> {
	private static Logger log = Logger.getLogger(CommonWebActions.class);
	private WebDriver driver;
	private ConfigProvider config;
	private Frame CURRENT_FRAME;

	public CommonWebActions(WebDriver driver) {
		this.driver = driver;
		this.config = ConfigProvider.getInstance();
		CURRENT_FRAME = Frame.DEFAULT;
	}

	/**
	 * This method pauses the execution of mai thread, for the specified amount of time.
	 * 
	 * @param millis
	 */
	public void waitExplicity(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
			log.info(e);
		}
	}

	private void switchToFrame(Frame frame) {
		if(frame.getParent() != null)
			switchToFrame(frame.getParent());

		driver.switchTo().frame(frame.getValue());
	}
	private void switchToFrameIfNeeded(T element) {
		if(CURRENT_FRAME == element.getFrame())
			return;
		else {
			driver.switchTo().defaultContent();
			switchToFrame(element.getFrame());
			CURRENT_FRAME = element.getFrame();
		}
	}

	private WebElement getWebElement(T element, String... placeholders) {
		switchToFrameIfNeeded(element);
		try {
			return driver.findElement(element.getBy(placeholders));
		} catch(Exception e) {
			return new WebDriverWait(driver, config.getDefaultTimeOut()).until(ExpectedConditions.elementToBeClickable(element.getBy(placeholders)));
		}
	}

	/**
	 * This method converts the generic object t, to List of objects of WebElement
	 * 
	 * @param element
	 * @param placeholders
	 * @return
	 */
	public List<WebElement> getWebElements(T element, String... placeholders) {
		switchToFrameIfNeeded(element);
		try {
			return driver.findElements(element.getBy(placeholders));
		} catch(Exception e) {
			return new WebDriverWait(driver, config.getDefaultTimeOut()).until(ExpectedConditions.presenceOfAllElementsLocatedBy(element.getBy(placeholders)));
		}
	} 

	/*
	 * Scroll web page to bring the web element into visible area
	 */
	private void scrollToElement(WebElement element) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
		waitExplicity(500);
	}

	/*
	 * perform click action through java script
	 */
	private void clickUsingJS(WebElement element) {
		((JavascriptExecutor) driver).executeAsyncScript("arguments[0].scrollIntoView(true);", element);
	}

	/*
	 * perform type/sendkeys action through Java script
	 */
	private void sendkeysThroughJS(String cssSelector, String value) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("document.querySelectorAll(\""+cssSelector+"\"[0].click();");
		js.executeScript("document.querySelectorAll(\""+cssSelector+"\"[0].value=\""+ value + "\";");
	}

	/** This method can be used to type value into a text box. It tries to type using the selenium method sendkeys, in case the default method doesn't work, it tries typing using java script
	 * 
	 *   @param element
	 *   @param value
	 *   @param placeholders
	 */
	public void sendkeys(T element, String value, String...placeholders) {
		try {
			getWebElement(element, placeholders).sendKeys(value);
		} catch(Exception e) {
			log.error(e);
			if(element.getType().equals(ByType.ID))
				sendkeysThroughJS("#" + element.getExpression(), value);
			else if (element.getType().equals(ByType.CSS_SELECTOR))
				sendkeysThroughJS(element.getExpression(), value);
			else 
				log.error("TRy using'CSS Selector' to locate the element");
		}
	}

	/**
	 * <p>To click on the web element. This method tries to click using default click method of Selenium, in case something goes wrong,
	 * it scrolls the page till the web element , then perform click using java script.</p>
	 * @param element
	 * @param placeholders
	 */
	public void click(T element, String... placeholders) {
		try {
			getWebElement(element, placeholders).click();
		} catch (Exception e) {
			scrollToElement(getWebElement(element, placeholders));
			clickUsingJS(getWebElement(element, placeholders));
		}
	}

	private void adjustCheckboxState(T element, boolean state, String... placeholders) {
		WebElement checkbox = getWebElement(element, placeholders);
		if(checkbox.isSelected() != state)
			checkbox.click();
	}

	/**
	 * It makes the check box ticked 
	 * @param element
	 * @param placeholders
	 */
	public void tickCheckbox(T element, String... placeholders) {
		adjustCheckboxState(element, true, placeholders);
	}

	/**
	 * It makes the check box un-ticked
	 * 
	 * @param element
	 * @param placeholders
	 */
	public void untickCheckbox(T element, String... placeholders) {
		adjustCheckboxState(element, false, placeholders);
	}

	/**
	 *  To run windows commands through java
	 *  @param command 
	 */
	public void runwindowsCommand(String command) {
		try {
			ProcessBuilder builder = new ProcessBuilder(
					"cmd.exe", "/c", command);
			builder.redirectErrorStream(true);
			Process p = builder.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while (true) {
				line = r.readLine();
				if(line == null || line.isEmpty()) { break; }
				log.info(line);
			}
		} catch(Exception e) {
			System.err.println("Could not run the command" + command);
			log.info(e);
		}
	}

	/**
	 * To open a new window/tab in the browser
	 * Once a new window/ tab is opened, then it can be switched using windowHadles  
	 */
	public void openNewWindow( ) {
		log.info("Opening new Browser window");
		((JavascriptExecutor)driver).executeScript("window.open()");
	}

	/** To check whether the element 
	 * @param element 
	 * @param placeholders
	 * @return
	 */
	public boolean isElementDisplayed(T element, String... placeholders) {
		boolean flag = false;
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		try {
			flag = driver.findElement(element.getBy(placeholders)).isDisplayed();
		} catch(Exception e) {
			flag = false;
		}
		driver.manage().timeouts().implicitlyWait(ConfigProvider.getInstance().getDefaultTimeOut(),TimeUnit.SECONDS);
		return flag;
	}
	/**
	 * To check whether the element is enabled on the screen 
	 * @param element
	 * @param placeholders
	 * @return
	 */
	public boolean isElementEnabled(T element, String...placeholders) {
		boolean flag = false;
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		try {
			flag = driver.findElement(element.getBy(placeholders)).isEnabled();
		} catch(Exception e) {
			flag = false;
		}
		driver.manage().timeouts().implicitlyWait(ConfigProvider.getInstance().getDefaultTimeOut(), TimeUnit.SECONDS);
		return flag;
	}

	/**
	 *  This method finds the font color of the web element and return object of java.awt.Color class
	 *  @param element
	 *  @param placeholders
	 *  @return
	 */
	public Color getColorOfElement(T element, String... placeholders) {
		WebElement el = getWebElement(element, placeholders);
		String color = el.getCssValue("color");
		String[] rgba = color.replace("rgba(","").replace(")","").split(",");
		return new Color(Integer.parseInt(rgba[0].trim()),Integer.parseInt(rgba[1].trim()),Integer.parseInt(rgba[2].trim()),Integer.parseInt(rgba[3].trim()));
	}

	/**
	 * This method finds the font color of the web element and return the color in Hexadecimal format
	 * 
	 * @param element
	 * @param placeholders
	 * @return
	 */
	public String getColorOfElementInHexadecimalFormat(T element, String...placeholders) {
		Color c = getColorOfElement(element, placeholders);
		String hexColour = Integer.toHexString(c.getRGB() & 0xffffff);
		if (hexColour.length() < 6)
			hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;

		return "#" + hexColour;
	}

	public String getPageTitle() {
		return driver.getTitle();
	}

	public String getPageURL() {
		return driver.getCurrentUrl();
	}

	public String getText(T element, String...placeholders) {
		return getWebElement(element, placeholders).getText();
	}

	/**
	 * To launch the given URL in a new window instance of the browser 
	 * @param driver
	 * @param url
	 * @return the reference to the Parent Window, it is expected to be used to switch back to parent window 
	 */
	public String launchNewWindowSwitchToIt(String url) {
		log.info("Launching new window");
		String parentWindowHandle = "";
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.open(\"" + url +"\")");
		Set<String> tabs = driver.getWindowHandles();
		if(tabs.size() > 1) {
			Iterator<String> it = tabs.iterator();
			parentWindowHandle = it.next();
			String child = it.next();
			log.info("Switching to new ");
			driver.switchTo().window(child);
			log.info("Switched to new window");
		}
		return parentWindowHandle;
	}

	public void switchToWindow(String windowHandle) {
		driver.switchTo().window(windowHandle);
	}

	/**
	 * To handle alert
	 * 
	 * @param okCancel Pass 'OK' to click on 'OK'/'Accept'/'Yes' button and Pass 'Cancel' to click on 'Cancel'/'Reject'/'No' button
	 */
	private void clickOnAlert(String okCancel) {
		try {
			Alert alert = driver.switchTo().alert();
			if(okCancel.equalsIgnoreCase("Ok"))
				alert.accept();
			else if(okCancel.equalsIgnoreCase("Ceacel"))
				alert.dismiss();
			else
				log.info("Invalid value passes in the parameter");
		} catch(Exception e) {
			log.info("Alert is not present");
			e.printStackTrace();
		}
	}

	public void acceptAlert() {
		clickOnAlert("ok");
	}

	public void dismissAlert() {
		clickOnAlert("Cancel");
	}

	public String getTextFromAlert() {
		Alert alert = driver.switchTo().alert();
		return alert.getText();
	}

	/**
	 * To get the count of search result 
	 * @return
	 */
	public int getTableRowCount(T element, String...placeholders) {
		WebElement table = getWebElement(element, placeholders);
		return table.findElements(By.tagName("tr")).size()-1;
	}

	/**
	 * To get the list of column names in the search result table 
	 * @return
	 */
	public List<String> getTableHeaders(T element, String...placeholders) {
		WebElement table = getWebElement(element, placeholders);
		List<String> header = new ArrayList<>();
		List<WebElement> listOfHeaders = table.findElements(By.tagName("tr")).get(0).findElements(By.tagName("th"));
		for(WebElement th : listOfHeaders) {
			if(!th.getText().equals(""))
				header.add(th.getText());
			else if(!th.getAttribute("data-title").equals(""))
				header.add(th.getAttribute("data-title"));
		}
		return header;
	}

	/**
	 * To get the row content where row has given value for the given column 
	 * @param colName 
	 * @param value
	 * @return
	 */
	public Map<String, String> getTableRowContent(T element,String colName, String value, String...placeholders) {
		return getTableRowContent(element, getTableRowNumWhere(element, colName, value, placeholders), placeholders);
	}

	/**
	 * To click on the cell for the given colName and value 
	 * @param colName
	 * @param value
	 */
	public void clickOnTableCell(T element, String colName, String value, String...placeholders) {
		Map<String, WebElement> rowElements = getTableRowElements(element, getTableRowNumWhere(element, colName, value, placeholders), placeholders);
		rowElements.get(colName).click();
	}

	/**
	 * To get content of the given row. The content is in the form of key, Value pair,
	 * where key is the column name and value is the respective element
	 * @param rowNum
	 * @return
	 */
	private Map<String, WebElement> getTableRowElements(T element, int rowNum, String...placeholders) {
		WebElement table = getWebElement(element, placeholders);
		Map<String, WebElement> row = new HashMap<>();

		List<WebElement> cells = table.findElements(By.tagName("tr")).get(rowNum).findElements(By.xpath("*"));
		List<String> headers = getTableHeaders(element, placeholders);

		if(headers.size() > cells.size())
			headers.remove(0);

		for(int i=0; i<headers.size(); i++) {
			driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
			if(!cells.get(i).findElements(By.tagName("a")).isEmpty())
				row.put(headers.get(i), cells.get(i).findElement(By.tagName("*")));
			else if(!cells.get(i).getText().isEmpty())
				row.put(headers.get(i), cells.get(i));
			else if(!cells.get(i).findElements(By.tagName("*")).isEmpty())
				row.put(headers.get(i), cells.get(i).findElement(By.tagName("*")));
		}
		driver.manage().timeouts().implicitlyWait(config.getDefaultTimeOut(), TimeUnit.SECONDS);
		return row;
	}

	/** To get the content of the given row, in the form of key, value pair
	 *  where key is column name and value is the respective content
	 *  @param rowNum
	 *  @return
	 */
	public Map<String, String> getTableRowContent(T element, int rowNum, String...placeholders) {
		Map<String, WebElement> rowElements = getTableRowElements(element, rowNum, placeholders);
		Map<String, String> rowContent = new HashMap<>();

		Set<String> cols = rowElements.keySet();
		for(String col : cols) {
			if(rowElements.get(col) != null)
				rowContent.put(col, rowElements.get(col).getText());
			else
				rowContent.put(col, "");
		}

		return rowContent;
	}

	/**
	 * To get the row number for the given value in the given column
	 * @param colName
	 * @param value
	 */
	private int getTableRowNumWhere(T element, String colName, String value, String...placeholders) {
		int rowCount = getTableRowCount(element, placeholders);
		for(int i=1; i<=rowCount; i++) {
			Map<String, String> rowContents = getTableRowContent(element, i, placeholders);
			String actualValue = rowContents.get(colName);
			if(actualValue.contains(value))
				return i;
		}
		return -1;
	}

	public void navigateBack() {
		driver.navigate().back();
	}	

	public void navigateForward() {
		driver.navigate().forward();
	}

	public void executeJavascriptExplicityly(String script) {
		((JavascriptExecutor) driver).executeScript(script);
	}
}

