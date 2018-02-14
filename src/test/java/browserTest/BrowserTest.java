package browserTest;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

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
	    int successCounter = 0;
    	    try {
    		driver.get(baseUrl + "/");
    		successCounter++;
    	    driver.findElement(By.id("login")).click();
    	    driver.findElement(By.id("login_field")).clear();
    	    driver.findElement(By.id("login_field")).sendKeys(testUserName);
    	    driver.findElement(By.id("password")).clear();
    	    driver.findElement(By.id("password")).sendKeys(testUserPass);
    	    driver.findElement(By.name("commit")).click();
    	    successCounter++;
    	    driver.findElement(By.id("showWriterBtn")).click();
    	    driver.findElement(By.linkText("pdf-reviewer")).click();
    	    driver.findElement(By.linkText("testing-review-pdf")).click();
    	    driver.findElement(By.linkText("test.pdf")).click();
    	    driver.findElement(By.linkText("(Myself) - pdf-reviewer")).click();
    	    driver.findElement(By.id("customTags")).clear();
    	    driver.findElement(By.id("customTags")).sendKeys("[Selenium]");
    	    driver.findElement(By.id("submitReview")).click();
    	    successCounter++;
    	    driver.findElement(By.cssSelector("button.close")).click();
    	    driver.findElement(By.id("showStatusBtn")).click();
    	    successCounter++;
    	    driver.findElement(By.linkText("Review Now")).click();
    	    File file = new File("strangeAnnotations.pdf");	    
    	    driver.findElement(By.id("pdf-file")).sendKeys(file.getAbsolutePath());
    	    driver.findElement(By.id("upload")).click();
    	    driver.findElement(By.linkText("here")).click();
    	    successCounter++;
    	    driver.findElement(By.cssSelector("a.js-selected-navigation-item.sunken-menu-item > span.octicon.octicon-issue-opened")).click();
    	    driver.findElement(By.linkText("Selenium")).click();
    	    driver.findElement(By.cssSelector("input.js-check-all")).click();
    	    successCounter++;
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}

	    reportSuccesses(successCounter);
	}

	private void reportSuccesses(int successCounter) throws IOException {
	    System.out.println(successCounter +" things went well");
	    GitHubClient client = new GitHubClient();
	    String botUserName = System.getProperty("testUserName");
	    String botPassword = System.getProperty("testUserPass");
	    client.setCredentials(botUserName, botPassword);
	    
	    String sha = System.getenv("TRAVIS_COMMIT");
	    System.out.println("making a comment on commit "+sha);
	    
	    RepositoryService repoService = new RepositoryService(client);
	    Repository repo = repoService.getRepository("DeveloperLiberationFront", "Pdf-Reviewer");
	    CommitService service = new CommitService(client);
	    CommitComment comment = new CommitComment();
	    
        String message = "step | pass?\n" +
                "--- | --- \n" +
                "canary site online |  :fire: \n" +
                "authenticated with GitHub | :fire: \n" +
                "no carry over reviews |  ::fire: \n" +
                "created request |  :fire: \n" +
                "Uploaded pdf |  :fire: \n" +
                "Found issues in GitHub |  :fire: \n";
        
        for(int i = 0; i< successCounter; i++) {
            message = message.replaceFirst(":fire:", ":white_check_mark:");
        }

	    comment.setBody(message);
        service.addComment(repo, sha, comment);
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
