package com.automation.tests;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.io.FileHandler;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class BaseTest {
    // ThreadLocal ensures thread-safety when running tests in parallel
    private static final ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    /**
     * Retrieves the driver instance unique to the executing thread.
     */
    public WebDriver getDriver() {
        return driverThread.get();
    }

    @BeforeMethod
    @Parameters("browser")
    public void setUp(String browser) {
        WebDriver driver;

        // Dynamic browser initialization based on testng.xml execution configuration
        if (browser.equalsIgnoreCase("chrome")) {
            driver = new ChromeDriver();
        } else if (browser.equalsIgnoreCase("firefox")) {
            driver = new FirefoxDriver();
        } else {
            throw new IllegalArgumentException("Unsupported browser layout environment profile: " + browser);
        }

        driverThread.set(driver);

        // Core operational driver properties
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        // Automatically intercept failures to record screen evidence
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot(result.getName());
        }

        // Clean closure routines
        if (getDriver() != null) {
            getDriver().quit();
        }
        driverThread.remove(); // Clean thread workspace to avoid memory bloating
    }

    /**
     * Utility method to capture the exact browser viewport state during runtime errors.
     */
    private void captureScreenshot(String testName) {
        File srcFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
        String screenshotPath = System.getProperty("user.dir") + "/screenshots/" + testName + "_" + System.currentTimeMillis() + ".png";

        try {
            File targetFile = new File(screenshotPath);
            FileHandler.copy(srcFile, targetFile);
            System.out.println("[INFO] Intercepted test failure. Screenshot archived to: " + screenshotPath);
        } catch (IOException e) {
            System.err.println("[ERROR] Unable to archive failure screenshot track: " + e.getMessage());
        }
    }
}
