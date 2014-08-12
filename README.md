PDF-Reviewer
============

# Building

  1. Install [Maven](http://maven.apache.org/download.cgi).  You will not need to explicitly install AppEngine, Maven will do that for you.
  2. Download the repository
  3. If you have forked it, [register](https://github.com/settings/applications/new) the app on GitHub, to get your app key.  Otherwise, ask me for the current key.
  - Make a copy of [SecretKeys.java.example](blob/master/src/main/java/src/main/SecretKeys.java.example) and rename this copy `SecretKeys.java`.  This will hold your app key, and is **not** tracked by version control.  Replace the dummy `GitHub` String with the actual key.
  - Run `mvn clean install` to get dependencies
  - Run the app using `mvn appengine:devserver`.  This will involve downloading the AppEngine SDK, which is quite large. 


