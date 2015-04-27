#Milestone Special:

##Motivation:
This Pdf-Review application joins several complex systems and APIs. For example, the application must connect to Github, a database, and an image hosting service, among other components. Accordingly, it is rather difficult to ensure that the application combines all these systems correctly. 

Before implementing the special milestone, our deployment pipeline supported building the application, running unit-tests, running static analysis, and deploying the application. However, each deployment would always end with one manual step: navigating to the deployed application and making sure the application was live.

We could test the system more rigorously by mocking each of these components individually, but this approach does not fully test the live interactions between the components, and fails when one of the components changes.

##What We Did:  
Instead, we decided to add a validation testing stage to the end of our deployment pipeline. Our validation tests run in a headless browser using [Selenium](http://www.seleniumhq.org/) after the application successfully deploys to the canary branch. 

The validation tests are run with the following command:

`mvn test -Dtest=BrowserTest -DtestUserName=$TEST_USER_NAME -DtestUserPass=$TEST_USER_PASS`
![image](https://cloud.githubusercontent.com/assets/5032534/7355987/854f3822-ecf3-11e4-8e35-95ad94244b7c.png)

After running the validation tests, we notify the user of their results by posting a comment on the commit message that is associated with the current deployment. 

![image](https://cloud.githubusercontent.com/assets/5032534/7356488/7547209a-ecf6-11e4-910a-64ee6226b1c2.png)


Here is a [Screencast](https://www.youtube.com/watch?v=p0gWFAut4C0&feature=youtu.be) that demonstrates our pipeline and demonstrates the validation tests being run (in the browser as opposed to headlessly).
