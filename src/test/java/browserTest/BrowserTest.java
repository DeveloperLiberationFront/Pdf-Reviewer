package browserTest;

import java.io.File;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class BrowserTest {
	private WebDriver driver;
	private String baseUrl;
	private boolean acceptNextAlert = true;
	private StringBuffer verificationErrors = new StringBuffer();
	private String testUserName;
	private String testUserPass;

	@Before
	public void setUp() throws Exception {
		testUserName = System.getProperty("testUserName");
		testUserPass = System.getProperty("testUserPass");
		driver = new FirefoxDriver();
		baseUrl = "http://pdfreviewcanary-ncsudlf.rhcloud.com/";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testUploadReview() throws Exception {
		driver.get(baseUrl + "/");
	    driver.findElement(By.id("login")).click();
	    driver.findElement(By.id("login_field")).clear();
	    driver.findElement(By.id("login_field")).sendKeys(testUserName);
	    driver.findElement(By.id("password")).clear();
	    driver.findElement(By.id("password")).sendKeys(testUserPass);
	    driver.findElement(By.name("commit")).click();
	    driver.findElement(By.id("showWriterBtn")).click();
	    driver.findElement(By.linkText("pdf-reviewer")).click();
	    driver.findElement(By.linkText("testing-review-pdf")).click();
	    driver.findElement(By.linkText("test.pdf")).click();
	    driver.findElement(By.linkText("(Myself) - pdf-reviewer")).click();
	    driver.findElement(By.id("customTags")).clear();
	    driver.findElement(By.id("customTags")).sendKeys("[Selenium]");
	    driver.findElement(By.id("submitReview")).click();
	    driver.findElement(By.cssSelector("button.close")).click();
	    driver.findElement(By.id("showStatusBtn")).click();
	    driver.findElement(By.linkText("Review Now")).click();
	    File file = new File("strangeAnnotations.pdf");	    
	    driver.findElement(By.id("pdf-file")).sendKeys(file.getAbsolutePath());
	    driver.findElement(By.id("upload")).click();
	    driver.findElement(By.linkText("here")).click();
	    driver.findElement(By.cssSelector("a.js-selected-navigation-item.sunken-menu-item > span.octicon.octicon-issue-opened")).click();
	    driver.findElement(By.linkText("Selenium")).click();
	    driver.findElement(By.cssSelector("input.js-check-all")).click();
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}

	private boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	private boolean isAlertPresent() {
		try {
			driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}

	private String closeAlertAndGetItsText() {
		try {
			Alert alert = driver.switchTo().alert();
			String alertText = alert.getText();
			if (acceptNextAlert) {
				alert.accept();
			} else {
				alert.dismiss();
			}
			return alertText;
		} finally {
			acceptNextAlert = true;
		}
	}
}
