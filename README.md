PDF-Reviewer
============

Aids the paper writing cycle by processing pdfs containing highlights and comments and turning them into GitHub issues that can be used to track progress in resolving them.

## Building

1. Install [Maven](http://maven.apache.org/download.cgi).  
2. Download the repository
3. (optional) Install mongodb (perhaps using [vagrant](https://www.vagrantup.com/)?).  If you don't have a GitHub API key, you should [make an application](https://developer.github.com/program/).  You'll need a Google account or something similar for the Picassa picture hosting. Finally, set up the secret environment variables as documented below.
- Run `mvn clean install` to get dependencies
- (Optional) Generate an Eclipse Project setup with  
`mvn eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true`  
Then, import this project into Eclipse.
- Run the app using `mvn tomcat7:run`.  This will run an embedded version of Apache Tomcat to run the server.  
- Congratulations, the app is now running on localhost:9090

## Deploying to OpenShift
1. Create an OpenShift account.
2. Start a new Tomcat 7/ JBoss 2.0 application.  Make note of its git repository, something like `ssh://551234567892ecdeadbeef9a@pdfapp-mycompany.rhcloud.com/~/git/pdfapp.git/`
3. Add a Mongodb Cartridge.  Version 2.4 is fine, newer is probably better.
4. Add an `openshift` remote to your local git repository:  
`git remote add openshift ssh://551234567892ecdeadbeef9a@pdfapp-mycompany.rhcloud.com/~/git/pdfapp.git/`
5. Push your code to this new remote:  
`git push openshift master:master`  
Or, if you want to push from a local test branch to master, do:  
`git push openshift my-local-test-branch:master`  
6. Boom!  The remote host will keep you informed about the build process and it should be live:  
![image](https://cloud.githubusercontent.com/assets/6819944/6632588/2e104860-c90f-11e4-8fb0-1933af10f6ef.png)



## Secrets

There are a few environment variables the application depends on:
- GITHUB_API=The secret Git key
- GITHUB_ID=The public Git key
- PICASSA_USER=The picassa username/password you will be hosting the images with
- PICASSA_PASSWORD
- PICASSA_PDF_ALBUM_ID=The album id you want to store images to.  set this to `default` if you don't care
- OPENSHIFT_MONGODB_DB_PORT=Used to connect with MongoDB
- OPENSHIFT_MONGODB_DB_HOST
- OPENSHIFT_MONGODB_DB_USERNAME
- OPENSHIFT_MONGODB_DB_PASSWORD


These should be set accordingly to run locally and, if you are running on your own server or something, they should be set there as well. 
Picassa is being used to host images (for now) because it's secure (i.e. with Google) and has unlimited hosting for small (less than 2048x2048) pictures, which is perfect for our purposes.