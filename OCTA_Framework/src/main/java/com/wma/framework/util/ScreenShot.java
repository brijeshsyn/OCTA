package com.wma.framework.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.Point;
import org.openqa.selenium.OutputType;
import com.wma.framework.common.ConfigProvider;
import com.wma.framework.common.WebDriverFactory;

public class ScreenShot {
	private static Logger log = Logger.getLogger(ScreenShot.class);

	private ScreenShot() {
	}

	/**
	 * This function is used to get screen shot of web aplication's displayed
	 * items This will capture screen shot and will store it at 
	 * resource/Result/Screenshots folder
	 * 
	 * @param strAction      Provide your testcase name as strAction
	 * @param screenshotPath
	 */
	public static String getScreenShot(String strAction) {
		WebDriver driver = WebDriverFactory.getWebDriver();
		File myFile = new File(ConfigProvider.getInstance().getScreenshotFolder());
		if(!myFile.exists())
			myFile.mkdirs();
		String currentDateTime = getCurrentDate();
		String location = ConfigProvider.getInstance().getScreenshotFolder() + System.getProperty("file.separator") 
		+ strAction.replaceAll("[^a-zA-ZQ-9]", "") + "_" + currentDateTime + ".png";
		log.info("Screenshot : " + location);
		try {
			File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(scrFile, new File(location));
		}  catch (IOException e) {
			e.printStackTrace();
		}
		return location;
	}

	/**
	 * This function is used to get current date time of the system This timestamp
	 * will be used by getScreenShot function 
	 * 
	 *  @return String 
	 */
	public static String getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return sdf.format(cal.getTime());
	}

	/**
	 * @throws IOException This function is used to get screen shot of provided web 
	 *                     element and will store it at provided location in 
	 *                     screenshotPath
	 * @param strAction Provide your testcase name as strAction
	 * @param element   Provide webelement name whose screenshot needs to be 
	 *                  captured 
	 * @return void 
	 */
	public static String getwebElemenstShot(String strAction, String screenshotPath, WebElement element)
			throws IOException {
		WebDriver driver = WebDriverFactory.getWebDriver();
		String currentDateTime = getCurrentDate();
		String location = screenshotPath + strAction + element + "_" + currentDateTime + ".png";

		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

		Point p = element.getLocation();

		int width = element.getSize().getWidth();
		int height = element.getSize().getHeight();

		BufferedImage img = ImageIO.read(scrFile);
		BufferedImage dest = img.getSubimage(p.getX(), p.getY(), width, height);

		ImageIO.write(dest, "png", scrFile);
		FileUtils.copyFile(scrFile, new File(location));

		return location;
	}
}
