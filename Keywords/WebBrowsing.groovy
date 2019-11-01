
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
import com.thoughtworks.selenium.Selenium
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.WebDriver
import org.openqa.selenium.Keys
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.JavascriptExecutor
import org.junit.After
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium
import static org.junit.Assert.*
import java.util.regex.Pattern
import static org.apache.commons.lang3.StringUtils.join
import java.text.SimpleDateFormat
import internal.GlobalVariable
import MobileBuiltInKeywords as Mobile
import WSBuiltInKeywords as WS
import WebUiBuiltInKeywords as WebUI

import java.util.HashMap
import java.util.UUID

/**
 * Holds the various search parameters which were entered into the EODMS interface by the user.
 * Is passed to the search() function to provide parameters for searches.
 * 
 * @author Kieran Moynihan
 */
public class SearchParameters {
	String region;
	String dateType;
	String[] dates;
	String[] selectedSatellites
	HashMap<String, String> textfieldValues;
	HashMap<String, Integer> radioSelected;
	HashMap<String, Integer> checkboxSelected;
	HashMap<String, String[]> selectboxSelectedOptionsText;

	public String getStartDate(){
		return dates[0]
	}

	public String getEndDate(){
		return dates[1]
	}

	public void setStartDate(String newStartDate){
		dates[0] = newStartDate
	}

	public void setEndDate(String newEndDate){
		dates[1] = newEndDate
	}

	/**
	 * 
	 * @param r		String						Name of Saved AOI region generated with UUID.
	 * @param d		String						Date type. Search option specifying Past 24 Hours, Any Time, Date Range, Seasonal Dates.
	 * @param dl	String[]					Dates. List of dates (0 or 2 dates) specified as boundaries of Date Range or Seasonal Dates.
	 * @param ss	String[]					Selected Satellites. List of satellites/data sources to get products from.
	 * @param tv	HashMap<String, String>		Text Field Values. List of all values which user had entered into text fields.
	 * @param rs	HashMap<String, Integer>	Radio buttons selected. Specifications for which radio buttons had been selected.
	 * @param cs	HashMap<String, Integer>	Check boxes selected. Specifications for which check boxes had been selected.
	 * @param sb	HashMap<String, String[]>	Select box options. List of each option selected from list of options in each select box.
	 */
	public SearchParameters(String r, String d, String[] dl, String[] ss,
	HashMap<String, String> tv, HashMap<String, Integer> rs,
	HashMap<String, Integer> cs, HashMap<String, String[]> sb) {
		this.region = r;
		this.dateType = d;
		this.dates = dl;
		this.selectedSatellites = ss;
		this.textfieldValues = tv;
		this.radioSelected = rs;
		this.checkboxSelected = cs;
		this.selectboxSelectedOptionsText = sb;
	}
}

/**
 * Provides functions used for manipulating and navigating the EODMS web page.
 *
 * @author Kieran Moynihan, Khang Nguyen
 */
public class WebBrowsing {
	// use these if selenium functions are not enough
	def driver = DriverFactory.getWebDriver()
	// Actions can help with focusing on and moving to element that might be hidden (need to scroll down a table to find them)
	Actions actions = new Actions(driver)
	// JavascriptExecutor can click on elements that are behind other elements by simply executing a click on the element, rather than simulating a user click
	JavascriptExecutor js = (JavascriptExecutor) driver

	private selenium
	private gui

	/**
	 * selClick with default duration 60 seconds
	 */
	public void selClick(String key){
		selClick(key, 60);
	}

	/**
	 * Attempts to click on an element multiple times until timeout occurs
	 */
	public void selClick(String key, int duration){
		for (int second = 0; second < duration; second++) {
			try {
				selenium.click(key)
				break;
			} catch (Exception except) {
				// prints the exception and fails on timeout
				if (second == duration-1) {
					println except
					fail('Timeout on click: '+key)
				}
			}
			Thread.sleep(1000);
		}
	}

	/**
	 * Enters dates into the dates tab.
	 */
	public void enterDates(String sDate, String eDate, String dateType) {
		// ensure in the date options tab
		selClick("id=Search")
		selClick("id=tab2")

		// row dependent on date search type
		String tRow = "";
		// rows correspond to table rows in the list of date options
		// row 5 contains text fields for DateRange option, not visible unless DateRange selected
		if (dateType == 'AnyTime') {
			tRow = '2'
		} else if (dateType == 'Past24Hours') {
			tRow = '3'
		} else if (dateType == 'DateRange') {
			tRow = "4"
		} else if (dateType == 'SeasonalDates') {
			tRow = "6"
		}
		// if AnyTime or Past24Hours selected, no further action required after selecting option
		if (tRow < 4) {
			selenium.click("//div[@id='panel2']/div/table/tbody/tr["+tRow+"]/td[2]/table/tbody/tr/td/span/label")
			return;
		}
		// get previous end date from date field
		// convert date YYYY-MM-DD -> YYYYMMDD
		String pEDate = String.join("", driver.findElementByXPath("//input[@id='"+dateType+"EndDate']").getAttribute("value").split("-"))
		// if there was a previous end date and the new start date is greater (later) than the previous end date, change the end date first (otherwise start date first)
		// this is because if you enter a start date that is later than the value in the end date field, an error will pop up and your previously entered start date will be cleared
		if (!(pEDate == "") && Integer.parseInt(String.join("", sDate.split("-"))) > Integer.parseInt(pEDate)) {
			// click end date field and enter end date
			selenium.click("id="+dateType+"EndDate")
			selenium.type("id="+dateType+"EndDate", eDate)
			// click date options tab again to remove popup calendar
			selenium.click("id=tab2")
			// click start date field and enter start date
			selenium.click("id="+dateType+"StartDate")
			selenium.type("id="+dateType+"StartDate", sDate)
			// click date options tab again to remove popup calendar
			selenium.click("id=tab2")
		} else {
			selenium.click("//div[@id='panel2']/div/table/tbody/tr["+tRow+"]/td[2]/table/tbody/tr/td/span/label")
			selenium.click("id="+dateType+"StartDate")
			selenium.type("id="+dateType+"StartDate", sDate)
			selenium.click("id=tab2")
			selenium.click("id="+dateType+"EndDate")
			selenium.type("id="+dateType+"EndDate", eDate)
			selenium.click("id=tab2")
		}
	}

	/**
	 * Logs user into EODMS from the main page.
	 */
	public void login(){
		// start login
		for (int quartersecond = 0; quartersecond <= 120; quartersecond++){
			try{
				selenium.click("link=Login")
				break;
			} catch (Exception e) {
				if (quartersecond == 120) throw e;
				WebElement[] tables = driver.findElementsByXPath('//table[@class="resizableContentPanel"]')
				for (WebElement table : tables) {
					if (table.getAttribute('aria-label') == 'The current projection is not supported and no alternate projections are available. In some cases zooming in can solve this problem.') {
						WebElement okBtn = table.findElementByXPath('./tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr/td[2]/table/tbody/tr/td/div/div/div/div/div/table/tbody/tr[2]/td[2]/table/tbody/tr/td[2]/div')
						js.executeScript("arguments[0].click()", okBtn)
						break;
					}
				}
			}
		}
		// enter username
		selenium.click("id=usernameTextBox")
		selenium.type("id=usernameTextBox", gui.getUsername())
		// enter  password
		selenium.click("id=passwordTextBox")
		selenium.type("id=passwordTextBox", gui.getPassword())
		// press login button
		selenium.click("//*[@id='RootDockPanel']/div/div/div[1]/table/tbody/tr/td/div/table/tbody/tr[2]/td/div/div/div/div/table/tbody/tr/td/table/tbody/tr[4]/td/table/tbody/tr[2]/td/div/table/tbody/tr/td[2]/table/tbody/tr[3]/td/table/tbody/tr[2]/td/table/tbody/tr/td[2]/table/tbody/tr/td/table/tbody/tr[1]/td/div/table/tbody/tr/td[2]/table/tbody/tr/td/table/tbody/tr[2]/td/div/div/div/div/div/table/tbody/tr[2]/td[2]/table/tbody/tr/td[2]/div")
		Thread.sleep(1000);
		for(int halfsecond = 0; halfsecond <=60; halfsecond++){
			try {
				if (selenium.isVisible("link=Login")) {
					fail('Incorrect Login Information')
				}
			} catch (Exception e) {}
			try {
				if (selenium.isVisible("link=My Account")) break;
			} catch (Exception e) {
				if (halfsecond == 60) {
					fail('Timeout on login')
				}
				Thread.sleep(500)
			}
		}
		selClick("link=Search")
	}

	/**
	 * Gets the search parameters from the EODMS interface.
	 */
	public SearchParameters getSearch(){
		// ensure in search tab
		selClick("id=Search")
		// go to location panel
		selClick("id=tab1")
		// Open Save Your Area of Interest
		if (driver.findElementByXPath("//div[@id='panel1']/div/table/tbody/tr[16]/td/table").getAttribute("aria-hidden") == "true") {
			selClick("link=Save Your Area of Interest")
		}
		// Save the Area of Interest as ESR_[UUID]
		String tempAOI = 'ESR_'+UUID.randomUUID().toString()
		selClick("//div[@id='panel1']/div/table/tbody/tr[16]/td/table/tbody/tr/td/table/tbody/tr/td[2]/table/tbody/tr/td/input", 5)
		selenium.type("//div[@id='panel1']/div/table/tbody/tr[16]/td/table/tbody/tr/td/table/tbody/tr/td[2]/table/tbody/tr/td/input", tempAOI)
		selClick("//div[@id='panel1']/div/table/tbody/tr[16]/td/table/tbody/tr/td/table/tbody/tr/td[3]/div/div")
		Thread.sleep(500)
		for (int quartersecond = 0; quartersecond <= 120; quartersecond++){
			WebElement[] tables = driver.findElementsByXPath('//table[@class="resizableContentPanel"]')
			boolean exit = false
			for (WebElement table : tables) {
				if (table.getAttribute('aria-describedby') == 'saveAOIBoxDescription') {
					WebElement okBtn = table.findElementByXPath('./tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr/td[2]/table/tbody/tr/td/div/div/div/div/div/table/tbody/tr[2]/td[2]/table/tbody/tr/td[2]/div')
					js.executeScript("arguments[0].click()", okBtn)
					exit = true
					break;
				}
			}
			if (exit) break;
		}
		// go to date options
		selClick("id=tab2")
		// list of Date Type buttons (Any Time, Past 24 Hours, Date Range, Seasonal Dates)
		WebElement[] datetypes = driver.findElementsByXPath("//input[@name='dates']")
		// Actual dateType selected
		String dateType = ""
		// for each dateType button
		for (int i = 0; i < datetypes.length; i++){
			WebElement radBut = datetypes[i]
			// if dateType button is selected
			if (radBut.isSelected()){
				// get the id of the button
				String id = radBut.getAttribute("id")
				// dateType is the value of the Label associated with this radio button
				dateType = selenium.getText("//label[@for='"+id+"']")
				break;
			}
		}
		// start and end dates
		String[] dates;
		// if is a dateType that uses date values
		if (dateType == 'Date Range' || dateType == 'Seasonal Dates') {
			// Remove spaces from dateType name
			if (dateType == 'Date Range') {
				dateType = 'DateRange'
			} else {
				dateType = 'SeasonalDates'
			}
			// retrieve start and end dates
			String startDate = driver.findElementByXPath("//input[@id='"+dateType+"StartDate']").getAttribute("value")
			String endDate = driver.findElementByXPath("//input[@id='"+dateType+"EndDate']").getAttribute("value")
			// set dates
			dates = [startDate, endDate];
			// if is a dateType that doesn't use date values
		} else {
			if (dateType == 'Any Time') {
				dateType = 'AnyTime'
			} else {
				dateType = 'Past24Hours'
			}
			// set default dates
			dates = ["", ""];
		}
		// Go to Data tab (sensors)
		selenium.click("id=tab3")

		// array of satellite options
		WebElement[] satOptions = driver.findElementsByXPath("//table[@id='panel3']/tbody/tr/td/div/div/div/div/table/tbody/tr[2]/td/div/div[2]/div/div/div/div/div")

		// holds satellite option names of satellites that are checked
		String[] satList = new String[satOptions.length];

		// for each satellite option in satOptions, if the option is checked, add the name of the option to satList
		int satListCount = 0;
		for (int i = 0; i < satOptions.length; i++) {
			if (!(satOptions[i].getAttribute("aria-label").endsWith("Not Checked")) && !(satOptions[i].getAttribute("aria-label").endsWith("Partially Checked"))) {
				satList[i] = driver.findElementByXPath("//div[@id='"+satOptions[i].getAttribute("id")+"']/table/tbody/tr/td[4]/table/tbody/tr[1]/td/table/tbody/tr/td[1]/div").getAttribute("innerText");
				satListCount++;
			} else {
				satList[i] = ""
			}
		}

		// satList with "" values removed
		String[] selectedSatellites = new String[satListCount];
		satListCount = 0;
		for (int i = 0; i < satList.length; i++) {
			if (satList[i] != "") {
				selectedSatellites[satListCount] = satList[i];
				satListCount++;
			}
		}
		// Go to Data options tab
		selenium.click("id=tab4")
		// retrieve all parameter web elements from the options tab
		WebElement optionsPanel = driver.findElementByXPath("//div[@id='panel4']")
		WebElement[] textfields = optionsPanel.findElementsByXPath(".//input[@type='text']")
		WebElement[] radiobuttons = optionsPanel.findElementsByXPath(".//input[@type='radio']")
		WebElement[] checkboxes = optionsPanel.findElementsByXPath(".//input[@type='checkbox']")
		WebElement[] selectboxes = optionsPanel.findElementsByXPath(".//select")

		// hash maps with keys of identifiers and values of values for each (set of) element(s)
		HashMap<String, String> textfieldValues = new HashMap<String, String>();
		HashMap<String, Integer> radioSelected = new HashMap<String, Integer>();
		HashMap<String, Integer> checkboxSelected = new HashMap<String, Integer>();
		HashMap<String, String[]> selectboxSelectedOptionsText = new HashMap<String, String[]>();

		// for each text field
		for (WebElement textfield : textfields) {
			// if the user entered text in the field
			if (textfield.getAttribute("title") != "") {
				// add the string to hash map with title of text field as key
				textfieldValues.put(textfield.getAttribute("title"), textfield.getAttribute("value"))
			}
		}
		// for each radio button
		for (WebElement radiobutton : radiobuttons) {
			// if the radio button was selected by the user
			if (radiobutton.isSelected()){
				String category = radiobutton.findElementByXPath("../../../../../../table").getAttribute("title")
				WebElement[] allButtons = radiobutton.findElementsByXPath("../../../td")
				int position = 0
				for (int i = 0; i < allButtons.length; i++){
					if (allButtons[i].findElementByXPath("./span/input").getAttribute("id") == radiobutton.getAttribute("id")) {
						position = i+1;
						break;
					}
				}
				// add the index in the group of the selected radio button to the hash map with the name of the group of radio buttons as the key
				radioSelected.put(category, position)
			}
		}
		// for each check box
		for (WebElement checkbox : checkboxes) {
			// if the check box was selected by the user
			if (checkbox.isSelected()){
				String category = checkbox.findElementByXPath("../../../../../../table").getAttribute("title")
				WebElement[] allButtons = checkbox.findElementsByXPath("../../../td")
				int position = 0
				for (int i = 0; i < allButtons.length; i++){
					if (allButtons[i].findElementByXPath("./span/input").getAttribute("id") == checkbox.getAttribute("id")) {
						position = i+1;
						break;
					}
				}
				// add the index in the group of the selected check box to the hash map with the name of the group of check boxes as the key
				checkboxSelected.put(category, position)
			}
		}
		// for each select box
		for (WebElement selectbox : selectboxes) {
			WebElement[] selOpt = new Select(selectbox).getAllSelectedOptions();
			String[] options = new String[selOpt.length]
			for (int i = 0; i < selOpt.length; i++) {
				options[i] = selOpt[i].getText()
			}
			// add the list of options selected within the select box to the hash map with the name of the hash map as the  key
			selectboxSelectedOptionsText.put(selectbox.getAttribute("title"), options)
		}
		// return the SearchParameters
		return new SearchParameters(tempAOI, dateType, dates, selectedSatellites, textfieldValues, radioSelected, checkboxSelected, selectboxSelectedOptionsText)
	}

	/**
	 * Enters the search parameters into the EODMS interface.
	 */
	public void search(SearchParameters SearchDetails){
		// wait for new page to load
		for (int quartersecond = 0; quartersecond < 60 ; quartersecond++) {
			try {
				if (selenium.isVisible("link=Use a Saved Area of Interest")) break;
			} catch (Exception e) { if (quartersecond == 60) fail("Can't see Saved Area of Interest link")}
			Thread.sleep(250);
		}
		// go to saved AOIs
		if (driver.findElementByXPath("//div[@id='panel1']/div/table/tbody/tr[12]/td/table").getAttribute("aria-hidden") == "true") {
			selClick("link=Use a Saved Area of Interest")
		}
		WebElement[] aois = driver.findElementsByXPath("//div[@id='panel1']/div/table/tbody/tr[12]/td/table/tbody/tr/td/table/tbody/tr/td[1]/a")
		for (WebElement aoi : aois){
			if (aoi.getAttribute('title') == SearchDetails.getRegion()){
				js.executeScript("arguments[0].click()", aoi)
				break;
			}
		}
		// go to date options
		selenium.click("id=tab2")
		// get dateType, start and end dates
		String dateType = SearchDetails.getDateType();
		String startDate = SearchDetails.getStartDate();
		String endDate = SearchDetails.getEndDate();
		if (dateType == 'AnyTime') {
			// click Any Time option
			selenium.click("//div[@id='panel2']/div/table/tbody/tr[2]/td[2]/table/tbody/tr/td/span/label")
		} else {
			// If SeasonalDates or DateRange, enter bounding dates
			// if SeasonalDates or DateRange but no dates were entered, fail
			if(startDate != "" && endDate != ""){
				enterDates(startDate, endDate, dateType)
				// catch is AnyTime
			}else{
				fail('Date Option is set to '+dateType+' but no dates were set.')
			}
		}
		// Go to Data tab (sensors)
		selenium.click("id=tab3")
		// select the specified satellites
		// get satellites to select from search details
		String[] selectedSatellites = SearchDetails.getSelectedSatellites()

		// list of satellite/data source options
		WebElement[] satOptions = driver.findElementsByXPath("//table[@id='panel3']/tbody/tr/td/div/div/div/div/table/tbody/tr[2]/td/div/div[2]/div/div/div/div/div")

		// holds xpath values for satellites that are selected
		String[] satelliteButtons = new String[selectedSatellites.length];

		// get and save xpath values into satelliteButtons
		for (int i = 0; i < satOptions.length; i++) {
			String satName = driver.findElementByXPath("//div[@id='"+satOptions[i].getAttribute("id")+"']/table/tbody/tr/td[4]/table/tbody/tr[1]/td/table/tbody/tr/td[1]/div").getAttribute("innerText");
			for (int j = 0; j  < selectedSatellites.length; j++) {
				if (selectedSatellites[j] == satName){
					satelliteButtons[j] = "//div[@id='"+satOptions[i].getAttribute("id")+"']/table/tbody/tr/td[2]/img"
				}
			}
		}
		// for each satellite to select
		for (String sat : satelliteButtons) {
			for (int decisecond = 0; true; decisecond++){
				// try to click the satellite until it works
				try {
					// unless it is already selected
					if (!(driver.findElementByXPath(sat).getAttribute("title") == "This node and all children nodes are selected.")){
						selenium.click(sat)
					}
					break;
				} catch (Exception e) {
					if (decisecond >= 100) {
						println e
						fail('Timeout looking for satellite buttons.')
					}
				}
				Thread.sleep(100)
			}

		}
		// Go to Data options tab
		selenium.click("id=tab4")
		// Get search parameters from SearchDetails
		HashMap<String, String> tfv = SearchDetails.getTextfieldValues()
		HashMap<String, Integer> rbs = SearchDetails.getRadioSelected()
		HashMap<String, Integer> cbs = SearchDetails.getCheckboxSelected()
		HashMap<String, String[]> sbo = SearchDetails.getSelectboxSelectedOptionsText()

		// wait for Date Options to load
		for (int decisecond = 0; true; decisecond++){
			try {
				if (selenium.isVisible("//div[@id='panel4']/table/tbody/tr/td/table/tbody/tr/td/div/div/table/tbody/tr[1]/td/div")) break;
			} catch (Exception e){
				if (decisecond >= 100) {
					println e
					fail('Timeout waiting for Data Options.')
				}
			}
			Thread.sleep(100);
		}

		for (int quartersecond = 0; true; quartersecond++) {
			try {
				// get the list of text fields on page
				WebElement[] textFields = driver.findElementsByXPath("//input[@type='text']")
				// for each text field in search parameters
				for (String textFieldTitle : tfv.keySet()) {
					// for each text field on page
					for (WebElement textField : textFields) {
						// if the text field on the page has the same title as from search parameters
						if (textField.getAttribute("title") == textFieldTitle) {
							// type the gathered value into the text field
							actions.moveToElement(textField)
							actions.click()
							actions.sendKeys(tfv.get(textFieldTitle))
						}
					}
				}
				actions.build().perform()
				break;
			} catch (Exception e) {
				if (quartersecond >= 12) {
					println e;
					fail('Timeout waiting to enter text field parameters.');
				}
				Thread.sleep(250)
			}
		}

		for (int quartersecond = 0; true; quartersecond++) {
			try {
				// for each selected radio button from search parameters
				for (String radioButtonCategory : rbs.keySet()) {
					// click on it
					actions.moveToElement(driver.findElementByXPath("//table[@title='"+radioButtonCategory+"']/tbody/tr/td["+rbs.get(radioButtonCategory).toString()+"]/span/input"))
					actions.click()
				}
				actions.build().perform()
				break;
			} catch (Exception e) {
				if (quartersecond >= 10) {
					println e;
					fail('Timeout waiting to enter radio button parameters.');
				}
				Thread.sleep(250)
			}
		}

		for (int quartersecond = 0; true; quartersecond++) {
			try {
				// for each selected check box from search parameters
				for (String checkboxCategory : cbs.keySet()) {
					// click on it
					actions.moveToElement(driver.findElementByXPath("//table[@title='"+checkboxCategory+"']/tbody/tr/td["+cbs.get(checkboxCategory).toString()+"]/span/input"))
					actions.click()
				}
				actions.build().perform()
				break;
			} catch (Exception e) {
				if (quartersecond >= 10) {
					println e;
					fail('Timeout waiting to enter checkbox parameters.');
				}
				Thread.sleep(250)
			}
		}

		for (int quartersecond = 0; true; quartersecond++) {
			try {
				// get the list of selectboxes current on the page
				WebElement[] selectBoxes = driver.findElementsByXPath("//select")
				// for each select box title that was gathered from search parameters
				for (String selectboxTitle : sbo.keySet()) {
					// for each select box on page
					for (WebElement selectBox : selectBoxes) {
						// if the select box on the page has the same title as we have, it should paramaterized
						if (selectBox.getAttribute("title") == selectboxTitle) {
							// get options selected from search parameters
							String[] selectedOptions = sbo.get(selectboxTitle)
							// if at least one option was selected
							if (selectedOptions.length > 0) {
								Select sel = new Select(selectBox)
								// deselect all previously selected options ("Any"/default option normally)
								sel.deselectAll()
								// select each option that was gathered from searhc parameters
								for (String option : selectedOptions){
									sel.selectByVisibleText(option)
								}
							}
						}
					}
				}
				break;
			} catch (Exception e) {
				if (quartersecond >= 10) {
					println e;
					fail('Timeout waiting to enter selectbox parameters.');
				}
				Thread.sleep(250)
			}
		}

		// go to submit search tab
		selenium.click("id=tab5")
		// specify the number of products per collection to the max (500)
		selenium.click("id=numSearchResults")
		selenium.click("//option[@value='500']")
	}

	/**
	 * Gets the number of products in the search from the search page.
	 */
	public String searchNumber(){
		// ensure in Search, Submit Search tab
		selClick("id=Search")
		selClick("id=tab5")
		// run the Get total result count link
		selClick("link=Get total result count")
		Thread.sleep(1000);
		// wait for count to finish
		for (int second = 0; true; second++) {
			try {
				if (selenium.isVisible("//img[@alt='Search Complete']")) break;
			} catch (Exception e) {
				if (second == 300) {
					fail('Timeout waiting for product count.')
				}
			}
			Thread.sleep(1000);
		}
		// get count from the popup
		String tmp = selenium.getText("//div/table/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td/div/div[2]").split("\\s+")[1]
		// close the popup
		selClick("xpath=(.//*[normalize-space(text()) and normalize-space(.)='Loading'])[1]/following::img[14]")
		return tmp
	}

	/**
	 * Default value override of order()
	 * - Default max scenes per cart is 50
	 */
	public String[] order (String sDate, String eDate) {
		order(sDate, eDate, 50)
	}

	/**
	 * Manages the process of adding products/scenes to the EODMS cart
	 */
	public String[] order (String sDate, String eDate, int maxScenes) {
		// whether cart is full/maxScenes is reached
		boolean full = false;
		// max capacity of Cart
		int maxCart = -1;
		// date of last product looked at
		String lastDate = "";
		// XPath to table holding product lists
		String frame = "//div[@id='InitialSearchPanel']/table/tbody";
		// current page that is being looked at
		int pagenum = 0;
		// lowest product number on page
		int lowCount = -1;
		// highest product number on page
		int highCount = -1;
		// total number of products on all pages
		int maxCount = -1;
		// number of products that have been added to the cart
		int productCount = 0;
		// number of scenes that have been added to the cart
		int currentCart = 0;

		// ensure in search tab
		selClick("id=Search")
		Thread.sleep(500);
		/**
		 // if a start and end date were provided
		 if (!(sDate == "" && eDate == "")) {
		 String dateType = ""
		 // go to date options tab
		 selenium.click("id=tab2")
		 // wait and see whether DateRange or SeasonalDates are being used
		 for (int second = 0;second <= 60; second++) {
		 try {
		 if (selenium.isVisible("id=DateRangeStartDate")) {
		 dateType = 'DateRange'
		 break;
		 } else if (selenium.isVisible("id=SeasonalDatesStartDate")) {
		 dateType = 'SeasonalDates'
		 break;
		 }
		 } catch (Exception e) {}
		 Thread.sleep(1000);
		 }
		 // enter the passed dates
		 enterDates(sDate, eDate, dateType)
		 }
		 */
		// go to Submit search tab
		selClick("id=tab5")
		Thread.sleep(500);
		// press search button
		selClick("//div[@id='panel5']/div/table/tbody/tr[6]/td[2]/div/div/div/div/div/table/tbody/tr[2]/td[2]/table/tbody/tr/td[2]/div")
		// wait for results to load
		for (int second = 0;second <= 60; second++) {
			try {
				if (selenium.isVisible(frame+"/tr[3]/td/table/tbody/tr/td[2]/table/tbody/tr/td[7]/div")) break;
			} catch (Exception e) {}
			Thread.sleep(1000);
		}
		// get number of pages
		int pages = Integer.parseInt(selenium.getText(frame+"/tr[3]/td/table/tbody/tr/td[2]/table/tbody/tr/td[7]/div"))
		// start adding products to cart until a limit is reached
		while (true) {
			try {
				// update page number
				pagenum++;
				// get the string containing the low, high, and max product counts
				String[] scenesInPage = selenium.getText(frame+"/tr[3]/td/table/tbody/tr/td[1]/div").split("\\s+")
				// get low and high product counts
				lowCount = Integer.parseInt(scenesInPage[1])
				highCount = Integer.parseInt(scenesInPage[3])
				// if no max count has been set yet
				if (maxCount == -1) {
					// if there is a max count value in string
					if (pages > 1) {
						maxCount = Integer.parseInt(scenesInPage[5])
						// if there is only one page, there won't be a max count value, only low to high (max count is same as high)
					} else {
						maxCount = highCount
					}
				}
				// for each product in the page
				for (int currentScene = 0; currentScene < ((highCount-lowCount)+1); currentScene++){
					// update product count
					productCount++;
					// row in the table that product is located in
					String productRow = Integer.toString(currentScene+3)
					// wait for product to be visible
					for (int second = 0;second <= 240; second++) {
						try {
							if (selenium.isVisible(frame+"/tr[2]/td/div/div/div/div[2]/table/tbody/tr["+productRow+"]/td[2]/div/span/input")) break;
						} catch (Exception e) {}
						Thread.sleep(250);
					}
					WebElement elem;
					// get the checkbox element and date of the product
					for (int i = 0; i < 100; i++){
						try {
							elem = driver.findElementByXPath(frame+"/tr[2]/td/div/div/div/div[2]/table/tbody/tr["+productRow+"]/td[2]/div/span/input")
							lastDate = driver.findElementByXPath(frame+"/tr[2]/td/div/div/div/div[2]/table/tbody/tr["+productRow+"]/td[4]/div").getText().split("\\s+")[0]
							break;
						} catch (StaleElementReferenceException e) {
							Thread.sleep(100)
							continue;
						}
					}
					// if the element has not already been added to the cart
					if (!driver.findElementByXPath(frame+"/tr[2]/td/div/div/div/div[2]/table/tbody/tr["+productRow+"]/td[2]/div/span/input").isSelected()) {
						// try to add the element to the cart
						selClick(frame+"/tr[2]/td/div/div/div/div[2]/table/tbody/tr["+productRow+"]/td[2]/div/span/input")
						boolean skip = false
						// wait for a popup to load
						for (int quartersecond = 0;quartersecond <= 300; quartersecond++) {
							try {
								// if the add to cart popup appears
								if (selenium.isVisible("xpath=(.//*[normalize-space(text()) and normalize-space(.)='Add to Cart'])[1]/preceding::input[1]")) break;
							} catch (Exception e) {}
							try{
								// if a popup indicating that the product has been ordered previously
								/**
								 // re-order the product
								 // Note: if this is re-enabled, then there will be duplicates requested. The current system starts a new search 
								 // from the end-date of the most recently looked at product. If there were multiple products on the same date, 
								 // those same objects will re-appear in the search for the next collection of products. This should be fixed
								 selenium.click("//div[(text() = 'Re-order' or . = 'Re-order')]")
								 */
								// do not re-order previously ordered products
								selenium.click("//div[(text() = 'Do not re-order' or . = 'Do not re-order')]")
								// skip (remove this if previously ordered products will be ordered again)
								skip = true
								break;
							} catch (Exception e){}
							try{
								// if a popup indicating that the product is not available in the archive
								// close the popup
								WebElement[] tables = driver.findElementsByXPath('//table[@class="resizableContentPanel"]')
								boolean exit = false
								for (WebElement table : tables) {
									if (table.getAttribute('aria-label') == 'Order Imagery Product') {
										WebElement closeBtn = table.findElementByXPath('./tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr/td[3]/table/tbody/tr/td/div/div/div/div/div/table/tbody/tr[2]/td[2]/table/tbody/tr/td[2]/div')
										js.executeScript("arguments[0].click()", closeBtn)
										// and skip to the next product
										skip = true
										exit = true
										break;
									}
								}
								if (exit) break;
							} catch (Exception e){println e}
							Thread.sleep(250);
						}
						// if the product reached the add to cart popup
						if (skip == false) {
							// get the cart limits (current number in cart and max number in cart)
							String[] cartLimits = selenium.getText("//div[@class='addToCartLimitPanel']/div").split("\\s+")
							currentCart = Integer.parseInt(cartLimits[0])
							// update maxCart if no value yet
							if (maxCart == -1) {
								maxCart = Integer.parseInt(cartLimits[4])
								// cart max is the lowest of maxCart (max allowed by EODMS) and maxScenes (max passed by program)
								maxCart = (maxCart < maxScenes) ? maxCart : maxScenes
							}
							// get all checkboxes for scenes in product
							WebElement[] scanProducts = driver.findElementsByXPath("//table[@class='basicTable']/tbody/tr/td[6]/span/input")
							if (currentCart+scanProducts.length > maxCart) {
								// if not all scenes will fit in the cart
								// mark as cart being full
								full = true
							} else {
								// if all scenes will fit
								// click each scene checkbox
								for (WebElement scanProduct : scanProducts) {
									actions.moveToElement(scanProduct)
									actions.click()
								}
								actions.build().perform()
							}
							// click button to update cart
							selenium.click("//td[3]/table/tbody/tr/td/div/div/div/div/div/table/tbody/tr[2]/td[2]/table/tbody/tr/td[2]/div")
						}
					}
					// if the cart is full, stop adding
					if (full == true) break;
				}
				// if the last page has been completed or cart is full, stop adding
				if (pagenum >= pages || full == true) break;
				// otherwise, go to the next page
				WebElement elem = driver.findElementByXPath(frame+"/tr[3]/td/table/tbody/tr/td[2]/table/tbody/tr/td[8]/div")
				// JavascriptExecutor is used here because sometimes next-page button is hidden behind an invisible element
				js.executeScript("arguments[0].click()", elem)
			} catch (Exception e) {
				println e
				fail('Error in ordering scenes')
			}
		}
		// return informative string array to main (used for determining future carts)
		String[] output = [lastDate, productCount.toString(), full.toString(), currentCart.toString()]
		return output;
	}

	/**
	 * Submits an order once the cart is full (according to maxCart) or all products are collected
	 */
	public void finalOrder(){
		// go to the cart page
		selClick("//div[@id='Cart']/div/div/div/div/div/table/tbody/tr[2]/td[2]")
		Thread.sleep(1000);
		// click on the Submit Satellite Product Order button
		selClick("//tr[3]/td/table/tbody/tr/td/div/div/div/div/div/table/tbody/tr[2]/td[2]/table/tbody/tr/td[2]/div")
		// click on the Order button
		Thread.sleep(1000);
		for (int second = 0; second <= 60; second++) {
			try {
				WebElement elem = driver.findElementByXPath("//div[@title='Order']/div/div/div/div/table/tbody/tr[2]/td[2]")
				js.executeScript("arguments[0].click()", elem)
				break;
			} catch (Exception e){
				if (second == 60) {
					throw e
				}
			}
			Thread.sleep(250)
		}
		Thread.sleep(3000);
		WebElement[] tables = driver.findElementsByXPath('//table[@class="resizableContentPanel"]')
		for (WebElement table : tables) {
			if (table.getAttribute('aria-label') == "You currently do not have any FTP Locations configured. Please use the 'Edit FTP Locations' button on this window to configure one or select another download destination type.") {
				WebElement okBtn = table.findElementByXPath('./tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr/td[2]/table/tbody/tr/td/div/div/div/div/div/table/tbody/tr[2]/td[2]/table/tbody/tr/td[2]/div')
				js.executeScript("arguments[0].click()", okBtn)
				WebElement elem = driver.findElementByXPath("//div[@title='Order']/div/div/div/div/table/tbody/tr[2]/td[2]")
				js.executeScript("arguments[0].click()", elem)
				break;
			}
		}
		// click on the OK button after order appears
		for (int quartersecond = 0; quartersecond <= 240; quartersecond++) {
			try {
				WebElement elem = driver.findElementByXPath("//div[@title='OK']/div/div/div/div/table/tbody/tr[2]/td[2]/table")
				js.executeScript("arguments[0].click()", elem)
				break;
			} catch (Exception e) {
				if (quartersecond == 240) {
					throw e
				}
			}
			Thread.sleep(250);
		}
		Thread.sleep(3000);
	}

	public boolean clearAOI(String region){
		try{
			boolean login = false
			for (int quartersecond = 0; quartersecond <= 120; quartersecond++){
				try{
					if (selenium.isVisible("link=Login")) login = true
				}catch (Exception e){
					try {
						if (selenium.isVisible("link=My Account")) login = false
					}catch (Exception f){
						if (quartersecond == 120){
							throw f
						}
					}
				}
			}
			if (login == true){
				login()
			}
			selClick("link=Search")
			selClick("id=tab1")
			WebElement aoiTable = driver.findElementByXPath("//div[@id='panel1']/div/table/tbody/tr[12]/td/table")
			if (aoiTable.getAttribute("aria-hidden") == "true") {
				selClick("link=Use a Saved Area of Interest")
			}
			WebElement[] aois = aoiTable.findElementsByXPath("./tbody/tr/td/table/tbody/tr")
			for (WebElement aoi : aois) {
				if (aoi.findElementByXPath("./td[1]/a").getAttribute("title") == region) {
					js.executeScript("arguments[0].click()", aoi.findElementByXPath("./td[2]/a"))
					WebElement[] tables = driver.findElementsByXPath('//table[@class="resizableContentPanel"]')
					for (int quartersecond = 0; quartersecond <= 120; quartersecond++){
						boolean exit = false
						for (WebElement table : tables) {
							if (table.getAttribute('aria-label') == 'Are you sure you want to delete this Saved AOI?') {
								WebElement okBtn = table.findElementByXPath('./tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr/td[2]/table/tbody/tr/td/div/div/div/div/div/table/tbody/tr[2]/td[2]/table/tbody/tr/td[2]/div')
								js.executeScript("arguments[0].click()", okBtn)
								break;
							}
						}
						return true
					}
				}
			}

		} catch (Exception e) {
			throw e
		}
	}

	/**
	 * Logs user out of EODMS
	 */
	public void logout(){
		selClick("id=gwt-uid-2")
		selClick("id=gwt-uid-6")
		for (int second = 0;second <= 60; second++) {
			try {
				if (selenium.isVisible("link=Login")) break;
			} catch (Exception e) {}
			Thread.sleep(1000);
		}
		Thread.sleep(1000);
	}

	//reset selenium
	public void selen(WebDriverBackedSelenium selenium){
		this.selenium = selenium;
	}

	public WebBrowsing(WebDriverBackedSelenium selenium, StartGUI gui){
		this.selenium = selenium;
		this.gui = gui;
	}
}