package com.automation.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
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
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();

            if (isHeadless) {
                options.addArguments("--headless=new");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--window-size=1920,1080"); // Force standard resolution paint
                options.addArguments("--remote-allow-origins=*"); // Prevents socket block exceptions
            }
            driver = new ChromeDriver(options);
        } else if (browser.equalsIgnoreCase("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();

            if (isHeadless) {
                options.addArguments("-headless");
                options.addArguments("--window-size=1920,1080");
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
        // Creates directory if missing before saving screen captures
        File screenshotFolder = new File(System.getProperty("user.dir") + "/screenshots");
        if (!screenshotFolder.exists()) {
            screenshotFolder.mkdirs();
        }

        File srcFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
        String screenshotPath = screenshotFolder.getAbsolutePath() + "/" + testName + "_" + System.currentTimeMillis() + ".png";
        try {
            FileHandler.copy(srcFile, new File(screenshotPath));
            System.out.println("[INFO] Intercepted test failure. Screenshot archived to: " + screenshotPath);
        } catch (IOException e) {
            System.err.println("[ERROR] Unable to save execution screenshot track: " + e.getMessage());
        }
    }
}
