package com.automation.tests;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.io.FileHandler;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class BaseTest {
    // ThreadLocal guarantees isolated memory pools when running browsers in parallel threads
    private static final ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    /**
     * Retrieves the isolated driver instance tied to the active executing test thread.
     */
    public WebDriver getDriver() {
        return driverThread.get();
    }

    @BeforeMethod
    @Parameters("browser")
    public void setUp(String browser) {
        WebDriver driver;

        // Dynamically flags headless mode if triggered via CLI / GitHub Actions flags (-Dheadless=true)
        boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions options = new ChromeOptions();
            if (isHeadless) {
                options.addArguments("--headless=new");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
            }
            driver = new ChromeDriver(options);
        } else if (browser.equalsIgnoreCase("firefox")) {
            FirefoxOptions options = new FirefoxOptions();
            if (isHeadless) {
                options.addArguments("-headless");
            }
            driver = new FirefoxDriver(options);
        } else {
            throw new IllegalArgumentException("Unsupported browser layout profile environment environment: " + browser);
        }

        driverThread.set(driver);

        // Core operational driver properties
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        // Intercept validation failures instantly to preserve UI evidence
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot(result.getName());
        }

        // Clean termination sequence
        if (getDriver() != null) {
            getDriver().quit();
        }
        driverThread.remove(); // Purges thread memory workspace completely
    }

    /**
     * Captures and archives a localized viewport screenshot file during execution failures.
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
