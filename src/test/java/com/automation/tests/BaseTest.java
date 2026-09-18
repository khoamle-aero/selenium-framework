package com.automation.tests;

import io.github.bonigarcia.wdm.WebDriverManager; // <-- Add this new import line
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
    private static final ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    public WebDriver getDriver() {
        return driverThread.get();
    }

    @BeforeMethod
    @Parameters("browser")
    public void setUp(String browser) {
        WebDriver driver;
        boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        if (browser.equalsIgnoreCase("chrome")) {
            // Automatically sets up matching chromedriver files cleanly inside cloud instances
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            if (isHeadless) {
                options.addArguments("--headless=new");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
            }
            driver = new ChromeDriver(options);
        } else if (browser.equalsIgnoreCase("firefox")) {
            // Automatically sets up matching geckodriver files cleanly inside cloud instances
            WebDriverManager.firefoxdriver().setup();

            FirefoxOptions options = new FirefoxOptions();
            if (isHeadless) {
                options.addArguments("-headless");
            }
            driver = new FirefoxDriver(options);
        } else {
            throw new IllegalArgumentException("Unsupported browser layout environment: " + browser);
        }

        driverThread.set(driver);
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot(result.getName());
        }
        if (getDriver() != null) {
            getDriver().quit();
        }
        driverThread.remove();
    }

    private void captureScreenshot(String testName) {
        File srcFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
        String screenshotPath = System.getProperty("user.dir") + "/screenshots/" + testName + "_" + System.currentTimeMillis() + ".png";
        try {
            FileHandler.copy(srcFile, new File(screenshotPath));
        } catch (IOException e) {
            System.err.println("[ERROR] Unable to save execution screenshot track: " + e.getMessage());
        }
    }
}
