package edu.ncsu.dlf.servlet.listeners;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 * Deals with the error described in the Stack Overflow post below
 * https://stackoverflow.com/questions/18069042/spring-mvc-webapp-schedule-java-sdk-http-connection-reaper-failed-to-stop
 * Specified in the web.xml file
 */

public class AmazonConnectionListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {}
    
    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        try {
            com.amazonaws.http.IdleConnectionReaper.shutdown();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    }