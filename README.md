PDF-Reviewer
============

# Building

  - Install [Appengine](https://developers.google.com/appengine/docs/java/gettingstarted/setup)
  - Install [Maven](http://maven.apache.org/download.cgi)
  - Download the repository
  - If you have forked it, [register](https://github.com/settings/applications/new) the app on GitHub
  - Otherwise, ask me for the secret key
  - Add a file called `src/main/java/src/main/SecretKeys.java`
    - See below for what to put in it
  - Run `mvn clean install` to get dependencies
  - Run the app using `mvn appengine:devserver`


# `SecretKeys.java`

```java
package src.main;

public class SecretKeys {
	public static final String GitHub = "CLIENT_SECRET";
}
```
