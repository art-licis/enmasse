package io.enmasse.systemtest.web;

import io.enmasse.systemtest.Logging;
import io.enmasse.systemtest.TestBase;
import org.junit.Test;
import org.openqa.grid.internal.utils.configuration.StandaloneConfiguration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.server.SeleniumServer;

import java.net.BindException;
import java.net.ServerSocket;

public class SeleniumTestBase extends TestBase {
    private SeleniumServer server;

    /**
     * Providing start selenium server
     * @param config configuration for server
     * @throws Exception
     */
    protected void startSeleniumServer(StandaloneConfiguration config) throws Exception {

        try {
            ServerSocket serverSocket = new ServerSocket(config.port);
            serverSocket.close();
            try {
                server = new SeleniumServer(config);
            } catch (Exception e) {
                Logging.log.info("Could not create Selenium Server " + e.getMessage());
            }
            try {
                server.boot();
                Logging.log.info("Server started");
            } catch (Exception e) {
                Logging.log.info("Could not start Selenium Server " + e.getMessage());
            }
        } catch (BindException e) {
            Logging.log.info("Selenium server already up");
        }
    }

    /**
     * Stop selenium server
     */
    protected void stopSeleniumServer() {
        if (server != null) {
            try {
                server.stop();
                server = null;
                System.out.println("Server stopped");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testSeleniumServerStartStop() throws Exception {
        StandaloneConfiguration conf = new StandaloneConfiguration();
        conf.port = 4443;
        startSeleniumServer(conf);

        WebDriver driver = new FirefoxDriver();
        System.out.println("Firefox is opened");
        driver.get("http://www.google.com");
        System.out.println("Google is selected");
        WebElement element = driver.findElement(By.name("q"));
        element.sendKeys("Cheese!");
        element.submit();
        System.out.println("Page title is: " + driver.getTitle());
        driver.close();
        stopSeleniumServer();
    }
}
