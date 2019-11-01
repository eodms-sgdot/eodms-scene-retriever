
/**
 * Main control for EODMS Scene Retriever.
 * 
 * Takes user input for start of run and controls WebBrowsing functions.
 * 
 * @author Kieran Moynihan, Khang Nguyen
 */

import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.checkpoint.CheckpointFactory as CheckpointFactory
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as MobileBuiltInKeywords
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testcase.TestCaseFactory as TestCaseFactory
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testdata.TestDataFactory as TestDataFactory
import com.kms.katalon.core.testobject.ObjectRepository as ObjectRepository
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WSBuiltInKeywords
import com.kms.katalon.core.webui.driver.DriverFactory as DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUiBuiltInKeywords
import internal.GlobalVariable as GlobalVariable
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.JavascriptExecutor
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium
import static org.junit.Assert.*
import java.util.regex.Pattern
import static org.apache.commons.lang3.StringUtils.join
import java.text.SimpleDateFormat

// launch startGUI
// StartGUI provides two input fields for user to enter a username and password for EODMS
StartGUI gui = new StartGUI();
// wait for user to finish entering login
while(gui.done() == false){
	Thread.sleep(250)
}
gui.dispose()

// open browser and go to URL
WebUI.openBrowser('https://www.eodms-sgdot.nrcan-rncan.gc.ca/index_en.jsp')
def driver = DriverFactory.getWebDriver()
String baseUrl = "https://www.eodms-sgdot.nrcan-rncan.gc.ca/index_en.jsp"
selenium = new WebDriverBackedSelenium(driver, baseUrl)
selenium.open("https://www.eodms-sgdot.nrcan-rncan.gc.ca/index_en.jsp")
WebBrowsing web = new WebBrowsing(selenium, gui);
// select the frame containing JavaScript for EODMS
selenium.selectFrame("index=0")
// wait for login button to load with the page
for (int quartersecond = 0;; quartersecond++) {
	if (quartersecond >= 240) fail("timeout");
	try { if (selenium.isVisible("link=Login")) break; } catch (Exception e) {}
	Thread.sleep(250);
}

//num will be the number of scans returned by the search
int num;

SearchParameters SearchDetails;

try{
	//log the user into EODMS using the provided login information
	web.login();
	
	//wait for the user to press the start button on the WaitGUI
	//the user is to enter the search parameters into the EODMS interface and then press start
	//This tool will read the webelements after user presses start in order to determine which options were selected
	WaitGUI gui2 = new WaitGUI();
	while (gui2.done() == false){
		Thread.sleep(250)
	}
	gui2.dispose()
	
	//get search parameters from EODMS interface
	SearchDetails = web.getSearch();
	dateType = SearchDetails.getDateType()
	startDate = SearchDetails.getStartDate()
	endDate = SearchDetails.getEndDate()
	
	//gets the number of products returned by the search
	println 'Getting number of scans...'
	num = Integer.parseInt(web.searchNumber());
	// List of years, used for separated SeasonalDates queries
	int[] yearList;
	// Maximum number of scenes to place in cart
	// Once this number of scenes is reached, the cart will be ordered 
	// and a new cart will be filled.
	int MAXSCENES = 50;
	// maxScenes is reset to MAXSCENES for each loop of cart filling
	int maxScenes = MAXSCENES;
	// Separated DateRange and SeasonalDates operations as SeasonalDates splits the dates entered into years and runs separately
	// This is necessary as the endDate must be decreased incrementally as carts are filled, and doing so with seasonal dates will affect 
	// all other years to be searched.
	// ex. 2014-04-01 -> 2016-09-30 vs. 2014-04-01 -> 2016-05-15 (in years 2014 and 2015, the search will only run to YYYY-05-15)
	if (dateType == 'SeasonalDates') {
		// get start and end years from date strings
		int startYear = Integer.parseInt(startDate.substring(0, 4))
		int endYear = Integer.parseInt(endDate.substring(0, 4))
		// determine the year gap and make a list of years
		int yearGap = endYear-startYear
		yearList = new int[(yearGap)+1]
		yearList[0] = startYear
		for (int i = 1; i < yearGap+1; i++) {
			yearList[i] = startYear+i;
		}
		// for each year, run the search over the specified range
		for (int year : yearList) {
			// create year string for the current year
			sDate = year.toString()+startDate.substring(4)
			eDate = year.toString()+endDate.substring(4)
			// go to the search tab
			for (int second = 0;second <= 60; second++) {
				try {
					selenium.click("id=Search")
					break;
				} catch (Exception e) {}
				Thread.sleep(1000);
			}
			Thread.sleep(250);
			// update the search dates to the current year
			web.enterDates(sDate, eDate, dateType);
			SearchDetails.setStartDate(sDate);
			SearchDetails.setEndDate(eDate);
			// get the number of products in the current year
			yearNum = Integer.parseInt(web.searchNumber());
			// if there are products for the year, get them (this is the same as dateRange)
			if (yearNum > 0) {
				while (true) {
					println 'Adding scans to cart...'
					web.logout()
					web.login()
					web.search(SearchDetails)
					// return from web.order:
					// [Last date that was seen from order (end next search here), 
					// number of products carted,
					// whether the cart was full,
					// number of scenes carted (can be multiple scenes per product)]
					String[] orderOut = web.order(sDate, eDate, maxScenes)
					// new end date
					eDate = orderOut[0]
					SearchDetails.setEndDate(eDate)
					// number of products remaining in year
					yearNum -= Integer.parseInt(orderOut[1])
					// number of scenes remaining before cart full
					maxScenes -= Integer.parseInt(orderOut[3])
					// if cart was full, assume there are more products
					if (Boolean.valueOf(orderOut[2]) == true) {
						println 'There are more products'
						// submit order
						web.finalOrder()
						// reset maxScenes
						maxScenes = MAXSCENES
					// if there are no more products in the year, finished searching year
					} else if (yearNum <= 0) {
						break;
					}
				}
			}
		}
	} else {
		while (true) {
			println 'Adding scans to cart...'
			web.logout()
			web.login()
			web.search(SearchDetails)
			// return from web.order:
			// [Last date that was seen from order (end next search here), 
			// number of products carted,
			// whether the cart was full,
			// number of scenes carted (can be multiple scenes per product)]
			String[] orderOut = web.order(startDate, endDate, maxScenes)
			// new end date
			endDate = orderOut[0]
			SearchDetails.setEndDate(endDate)
			// number of products remaining
			num -= Integer.parseInt(orderOut[1])
			// number of scenes remaining before cart full
			maxScenes -= Integer.parseInt(orderOut[3])
			// if cart was full, assume there are more products
			if (Boolean.valueOf(orderOut[2]) == true) {
				println 'There are more products'
				// submit order
				web.finalOrder()
				// reset maxScenes
				maxScenes = MAXSCENES
			// if there are no more products, finished searching
			} else if (num <= 0) {
				break;
			}
		}
	}
	// last submission
	println 'Got all products'
	web.finalOrder();
} catch (Exception e) {
	println e
} finally {
	for (int tries = 0; tries < 3; tries++){
		try{
			selenium.refresh()
			selenium.selectFrame("index=0")
			web.clearAOI(SearchDetails.getRegion())
			break;
		} catch (Exception e) {
			if (tries == 2){
				println e
			}
		}
	}
	driver.quit()
}