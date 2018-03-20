package team19Tests;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openqa.selenium.By;
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
	
	/**
	 * Tests the login functionality
	 */
	//**
	@Test
	public void testLogin() {
		
		System.setProperty("webdriver.chrome.driver", "C:/Users/Dikolai/Documents/College/CSC 492/chromedriver.exe");
		
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
	//*/
	
	/**
	 * Tests uploading a PDF
	 */
	//**
	@Test
	public void testUpload() {
		System.setProperty("webdriver.chrome.driver", "C:/Users/Dikolai/Documents/College/CSC 492/chromedriver.exe");
		
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		
		String username = "smirhos+pdf-reviewer-bot@ncsu.edu";
		String password = "smirhos+pdf-reviewer-bot@ncsu.edu";
		
		login(driver, username, password);
		
		driver.findElement(By.id("repo0")).click();
		assertEquals(driver.findElement(By.id("repo0")).getText(), "FOOBAR");
				
		Select branchDropDown = new Select(driver.findElement(By.id("branch_id")));
		branchDropDown.selectByIndex(1);
		assertEquals(branchDropDown.getOptions().get(1).getText(), "Dev1");
		
		driver.findElement(By.className("file-upload-input")).sendKeys("C:/Users/Dikolai/Documents/College/CSC 492/test-files/Test_Document_A1.pdf");
		
		WebElement removeButton = driver.findElement(By.className("remove-pdf"));
		
		assertFalse(removeButton == null);
		
		driver.findElement(By.className("mdl-button__ripple-container")).click();
		
		driver.close();
	}
	//*/
	
	/**
	 * Tests the remove upload button
	 */
	//**
	@Test
	public void testRemove() {
		System.setProperty("webdriver.chrome.driver", "C:/Users/Dikolai/Documents/College/CSC 492/chromedriver.exe");
		
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		
		String username = "smirhos+pdf-reviewer-bot@ncsu.edu";
		String password = "smirhos+pdf-reviewer-bot@ncsu.edu";
		
		login(driver, username, password);
		
		driver.findElement(By.id("repo0")).click();
		assertEquals(driver.findElement(By.id("repo0")).getText(), "FOOBAR");
				
		Select branchDropDown = new Select(driver.findElement(By.id("branch_id")));
		branchDropDown.selectByIndex(1);
		assertEquals(branchDropDown.getOptions().get(1).getText(), "Dev1");
		
		driver.findElement(By.className("file-upload-input")).sendKeys("C:/Users/Dikolai/Documents/College/CSC 492/test-files/Test_Document_A1.pdf");
		
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
	//*/
	
	/**
	 * Tests uploading an invalid file type
	 */
	//**
	@Test
	public void testInvalidFile() {
		System.setProperty("webdriver.chrome.driver", "C:/Users/Dikolai/Documents/College/CSC 492/chromedriver.exe");
		
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		
		String username = "smirhos+pdf-reviewer-bot@ncsu.edu";
		String password = "smirhos+pdf-reviewer-bot@ncsu.edu";
		
		login(driver, username, password);
		
		driver.findElement(By.id("repo0")).click();
		assertEquals(driver.findElement(By.id("repo0")).getText(), "FOOBAR");
				
		Select branchDropDown = new Select(driver.findElement(By.id("branch_id")));
		branchDropDown.selectByIndex(1);
		assertEquals(branchDropDown.getOptions().get(1).getText(), "Dev1");
		
		driver.findElement(By.className("file-upload-input")).sendKeys("C:/Users/Dikolai/Documents/College/CSC 492/test-files/Test_Document_I1.docx");
		
		WebElement removeButton = driver.findElement(By.className("remove-pdf"));
		assertFalse(removeButton == null);
		
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		driver.findElement(By.className("mdl-button__ripple-container")).click();
		
		//TODO add checks for error message
		
		driver.close();
	}
	//*/
	
	/**
	 * Logs into the system using the testing credentials
	 * @param driver The chrome web driver to use
	 */
	//**
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
	//*/
}
