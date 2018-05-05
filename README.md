
PDF Reviewer Tool

Aids the paper writing cycle by processing pdfs containing highlights and comments and turning them into GitHub issues that can be used to track progress in resolving them.

## Building

1. Install [Maven](http://maven.apache.org/download.cgi).  
2. Download the repository
- Run `mvn clean install` to get dependencies
- (Optional) Generate an Eclipse Project setup with  
`mvn eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true`  
Then, import this project into Eclipse.
- Run the app using `mvn tomcat7:run`.  This will run an embedded version of Apache Tomcat to run the server.  
- Congratulations, the app is now running on localhost:9090

