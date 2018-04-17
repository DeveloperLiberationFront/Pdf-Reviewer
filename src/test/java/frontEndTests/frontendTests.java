package frontEndTests;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * Automated tests that check the front end functionality.
 * Uses a chrome web driver application
 * @author Nicholas Anthony
 */
public class frontendTests {

	public static final String ADDRESS = "http://localhost:9090/";
	
	public static final int DEFAULT_TIMEOUT = 20;
	
	public static final String DRIVER = "../chromedriver.exe";
	
	public String files;
	
	@Before
	public void setUp() {
		files = System.getProperty("user.dir");
		files = files.replace('\\', '/');
		files += "/src/test/resources/test-files";
	}
	
	/**
	 * Tests the login functionality
	 */
	@Test
	public void testLogin() {
		
		System.setProperty("webdriver.chrome.driver", DRIVER);
		
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

		driver.get(ADDRESS);
		assertEquals(driver.getCurrentUrl(), "http://localhost:9090/");
		WebElement loginButton = driver.findElement(By.id("login"));
		
		assertFalse(loginButton == null);
		
		assertTrue(loginButton.isEnabled());
		
		loginButton.click();		
		
		//Wait until redirected
		WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
//		wait.until(ExpectedConditions.titleIs("Sign in to GitHub · GitHub"));
		
		assertTrue(driver.getTitle().contains("Sign in to GitHub"));
		
		driver.findElement(By.id("login_field")).sendKeys("smirhos+pdf-reviewer-bot@ncsu.edu");
		driver.findElement(By.id("password")).sendKeys("smirhos+pdf-reviewer-bot@ncsu.edu");
		driver.findElement(By.name("commit")).click();
		
//		wait.until(ExpectedConditions.titleIs("PDF Reviewer"));

		assertTrue(driver.getTitle().contains("PDF Reviewer"));
		
		driver.close();
	}
	
	/**
	 * Tests uploading a PDF
	 */
	@Test
	public void testUpload() {
		System.setProperty("webdriver.chrome.driver", DRIVER);

		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		
		String username = "smirhos+pdf-reviewer-bot@ncsu.edu";
		String password = "smirhos+pdf-reviewer-bot@ncsu.edu";
		
		login(driver, username, password);
		
		driver.findElement(By.id("repo0")).click();
		assertEquals(driver.findElement(By.id("repo0")).getText(), "FOOBAR");
				
		Select branchDropDown = new Select(driver.findElement(By.id("branchList")));
		branchDropDown.selectByIndex(1);
		assertEquals(branchDropDown.getOptions().get(1).getText(), "dev");
		
		driver.findElement(By.className("file-upload-input")).sendKeys(files + "/Test_Document_A1.pdf");
		
		WebElement removeButton = driver.findElement(By.className("remove-pdf"));
		
		assertFalse(removeButton == null);
		
		driver.findElement(By.className("mdl-button__ripple-container")).click();
		
		driver.close();
	}
	
	/**
	 * Tests the remove upload button
	 */
	@Test
	public void testRemove() {
		System.setProperty("webdriver.chrome.driver", DRIVER);
		
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		
		String username = "smirhos+pdf-reviewer-bot@ncsu.edu";
		String password = "smirhos+pdf-reviewer-bot@ncsu.edu";
		
		login(driver, username, password);
		
		driver.findElement(By.id("repo0")).click();
		assertEquals(driver.findElement(By.id("repo0")).getText(), "FOOBAR");
				
		Select branchDropDown = new Select(driver.findElement(By.id("branchList")));
		branchDropDown.selectByIndex(1);
		assertEquals(branchDropDown.getOptions().get(1).getText(), "dev");
		
		driver.findElement(By.className("file-upload-input")).sendKeys(files + "/Test_Document_A1.pdf");
		
		WebElement removeButton = driver.findElement(By.className("remove-pdf"));
		assertFalse(removeButton == null);
		
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		removeButton.click();
				
		assertEquals(driver.findElement(By.className("drag-text")).getText(), "DRAG AND DROP A FILE OR CLICK INSIDE THE BOX");
		
		driver.close();
	}
	
	/**
	 * Tests uploading an invalid file type
	 */
	@Test
	public void testInvalidFile() {
		System.setProperty("webdriver.chrome.driver", DRIVER);
		
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		
		String username = "smirhos+pdf-reviewer-bot@ncsu.edu";
		String password = "smirhos+pdf-reviewer-bot@ncsu.edu";
		
		login(driver, username, password);
		
		driver.findElement(By.id("repo0")).click();
		assertEquals(driver.findElement(By.id("repo0")).getText(), "FOOBAR");
				
		Select branchDropDown = new Select(driver.findElement(By.id("branchList")));
		branchDropDown.selectByIndex(1);
		assertEquals(branchDropDown.getOptions().get(1).getText(), "dev");
		
		driver.findElement(By.className("file-upload-input")).sendKeys(files + "/Test_Document_I1.docx");
		
		Alert alert = driver.switchTo().alert();
		
		System.out.println(alert.getText());
		
		assertTrue(alert.getText().equals("Please upload pdf files only."));
		
		alert.accept();
		
		WebElement removeButton = driver.findElement(By.className("remove-pdf"));
		assertFalse(removeButton == null);
		
		//assertFalse(driver.findElement(By.className("mdl-button__ripple-container")).isEnabled());
		
		driver.close();
	}
	
	/**
	 * Tests what is displayed when the user doesn't have access to any repositories
	 */
	@Test
	public void noRepos() {
		System.setProperty("webdriver.chrome.driver", DRIVER);
		
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		
		String username = "nranthon+noRepo@ncsu.edu";
		String password = "nranthon+noRepo@ncsu.edu";
		
		login(driver, username, password);
		
		try {
			driver.findElement(By.id("repo0"));
			fail("It shouldn't exist");
		} catch (NoSuchElementException e) {

		} finally {
			driver.close();
		}
		
		
	}
	
	/**
	 * Logs into the system using the testing credentials
	 * @param driver The chrome web driver to use
	 */
	private void login(WebDriver driver, String username, String password) {
		driver.get(ADDRESS);
		WebElement loginButton = driver.findElement(By.id("login"));
		
		loginButton.click();		
				
		//Wait until redirected
		WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
//		wait.until(ExpectedConditions.titleIs("Sign in to GitHub · GitHub"));
		
		driver.findElement(By.id("login_field")).sendKeys(username);
		driver.findElement(By.id("password")).sendKeys(password);
		driver.findElement(By.name("commit")).click();
		
		try {
//			wait.until(ExpectedConditions.titleIs("Authorize application"));
			TimeUnit.SECONDS.sleep(1);
			driver.findElement(By.id("js-oauth-authorize-btn")).click();
		} catch (TimeoutException | InterruptedException e) {
			
		} finally {
//			wait.until(ExpectedConditions.titleIs("PDF Reviewer"));
		}
		
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
