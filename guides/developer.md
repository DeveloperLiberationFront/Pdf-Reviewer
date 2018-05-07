# PDF Reviewer Tool Developer&#39;s Guide (Last updated: May 6, 2018)

## Setting Up the Environment

1. Follow the steps detailed in the installation guide to install and build the application
2. To run the tomcat server run the command `mvn tomcat7:run` and the application will now be running on localhost:9090.
    * The port used can be changed in the xml file under the `<port>` tag, however all the documentation will be referencing port 9090 as the port to be used

We as a team use several different IDEs so we do not provided support for any singular one.

## Updating Maven Dependencies

Tip: When running `mvn clean install` the shell will display a list of dependencies, along with an indication of their latest version number (if they are not already at the latest version).

1. Go to the pom.xml, file and find the tag `<dependencies>` under which all the runtime dependencies are listed along with their version number.
2. Each dependency is listed under a dependency tag, which is formatted as follows:

```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.9</version>
</dependency> 
```

3. To update a specific dependency simply change the version number to the version to the latest version or the version you would like and re-run the `mvn clean install` command.



## Adding Features to the Application

### Adding a web page

1. Start by creating the HTML page you would like and adding it to `src/main/webapp` folder
    * If you would like to add any JavaScript to the page, simply create JavaScript file (.js) and add it to the src/main/webapp/js folder
2. Create a Java file in the `src/main/java/edu/ncsu/dlf/servlets` folder following the convention `[Desired Name]Servlet.java`.
    * Make sure the class you create extends HttpServlet, follow the examples provided by the existing servlets: <br> `public class [DesiredName]Servlet extends HttpServlet`
3. 
    * The class can contain override methods for each of the HTTP verbs. Follow the examples provided by the existing servlets.
    * To simply load an HTML page, in the doGet() method just forward the request to the desired HTML page that you created earlier.<br>`req.getRequestDispatcher("[desired].html").forward(req, resp);`

4. Go to the WEB-INF folder and create a mapping of your desired route to the servlet that was just created in the web.xml file. Follow the format of the existing mappings in the web.xml file.
5. Now run the application as described previously and hit `localhost:9090/[desired route]` and your HTML page should be loaded.

### Adding web page tests

1. Create your desired Java test class for your new web page and add it to the src/test/java folder.
2. Follow the naming convention `[Desired Web Page Name]Test.java`
3. Make sure to use the Chrome Driver as the Selenium Web Driver as the default driver uses an outdated version of Internet Explorer
    * The Chrome driver needs to know where the Chrome Driver application is in your file system so make sure you include the following line of code: <br> `System.setProperty("webdriver.chrome.driver", "../chromedriver.exe");`
4. When testing that a page is being loaded after clicking a button, use the following commands to ensure the page is loaded before running any additional code.

```java
WebDriverWait wait = new WebDriverWait([WebDriver Name], [Desired Timeout]);
wait.until(ExpectedConditions.titleIs([Desired Title]));
```

5. Make sure to use assert calls to check that desired outcomes occur after making specific actions.

Tip: Due to some issues with Maven, the Wait function must be commented out before build. The function can be uncommented after build so that the tests may run properly.

### Adding components to the model

1. Create your desired Java class in the `src/main/java/edu/ncsu/dlf/model` folder and create an object following standard OO principles.
2. To use the created object in a servlet to interact with the frontend simply import the object into the servlet: `import edu.ncsu.dlf.model.[DesiredObject];`

### Adding unit tests for new model components

1. Create your desired Unit test class in the `src/test/java/modelTests` folder with the naming convention `[New Model Component Name]Test.java`
2. Begin writing unit tests to make sure the new component&#39;s methods all work properly
Tip: If using Eclipse, run the test files by right clicking on the file in the Package Explorer and select Run As and JUnit Test. If you want to see the code coverage then right click the file in the Package Explorer and select Coverage As and JUnit Test. (Must have EclEmma installed in Eclipse)
Tip: All model tests are run upon build. In order to exclude certain tests from being run, add 
```xml
<exclude>**/[Desired Excluded Test Name].java</exclude>
```
In the <excludes> section of the pom.xml file.



## Updating Features of the Application

### Updating a web page

1. Begin by finding the relevant HTML file in the src/main/webapp folder, depending on the change you would like to make look at the relevant JS and CSS files attached and investigate those further from there.
2. Check the servlets to see what servlet is loading the relevant HTML page, and update the logic from that point onwards.

Tip: When modifying or updating front end files such as HTML, JS, or CSS there is no need to restart the tomcat server or rebuild the application.

### Updating a model component

1. Go to the relevant model component in src/main/java/edu/ncsu/dlf/model and update the logic as desired.
2. Go the matching JUnit test and update the tests according to your changes.

## Project Structure
````
| .
├── main
│   └── java
│       └── edu
│           └── ncsu
│               └── dlf
│                   ├── model
│                   │   ├── Pdf.java
│                   │   ├── PdfComment.java
│                   │   └── Repo.java
│                   ├── servlet
│                   │   ├── FileUploadServlet.java
│                   │   ├── LoginServlet.java
│                   │   ├── RepositoriesServlet.java
│                   │   ├── ToolServlet.java
│                   │   ├── UploadIssueRunnable.java
│                   │   └── listeners
│                   │       └── AmazonConnectionListener.java
│                   └── utils
│                       ├── HttpUtils.java
│                       ├── ImageUtils.java
│                       └── S3Utils.java
└── test
    └── java
        ├── frontEndTests
        │   └── frontendTests.java
        ├── modelTests
        │   ├── PdfCommentTest.java
        │   ├── PdfTest.java
        │   └── RepoTest.java
        ├── servletTests
        │   ├── LoginServletTest.java
        │   ├── RepositoriesServletTest.java
        │   └── ToolServletTest.java
        └── test
            └── TestUtils.java 
````

### Brief Notes on the main files

* **Model**
    - Pdf - An object used to represent a PDF in the system, modeled by the Apache PDFBox library.
        - getPDFComments() - The comments from an uploaded PDF can be retrieved by using this function, which returns an ArrayList of PdfComment objects. This function handles turning annotations into PdfComments as well.
        - updateCommentsWithColorsAndLinks() - Updates a list of PdfComment objects to have the appropriate colors and links to issues
  - PdfComment - An object used to represent a single comment on a PDF in the system. The constructor takes a string and turns it into a PdfComment object by processing the string and associating the appropriate tags with each comment so that each PdfComment object can easily be turned into GitHub issues. The object also contains a buffered image, which is included in the GitHub issues, however the image processing is mostly done in functions of the Pdf class.
    - Tag (Enum) - A enum to recognize the different types of tags that a comment could be associated with (ex: Must-Fix, Should-Fix, etc.). The tags are mapped to labels on GitHub.
* **Servlet**
  - LoginServlet** - Completes the GitHub OAuth 2 web application flow
  - RepositoriesServlet** - Gets a list of the all the repositories and their branches the user has access to and passes it to the front end sorted by time of last update.
  - FileUploadServlet - Takes the raw PDF file data from the front end it, converts it to the appropriate Pdf and PdfComment objects in our system and then passes those comment objects to the UploadIssueRunnable
  - UploadIssueRunnable - Takes a list of PdfComment objects and creates a GitHub issue for each them in a multi-threaded manner
- **Utils**
  - HttpUtils - Works with the Apache HTTPResponse library to convert HTTP requests and response to human readable strings.
  - ImageUtils - Houses the logic to grab screenshots of the area surrounding a specific PDF annotation
  - S3Utils - An uploader that is used to upload images to Amazon&#39;s S3 service, and returns an image URL
