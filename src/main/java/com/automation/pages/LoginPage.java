package com.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // 1. Private Locators (Encapsulation: Tests cannot see or change raw elements)
    private final By usernameField = By.id("user-name");
    private final By passwordField = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By errorMessage = By.cssSelector("h3[data-test='error']");

    // 2. Constructor (Passes the thread-safe driver and initializes explicit waits)
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    // 3. Page Actions (Reusable methods for your test scripts)

    /**
     * Enters username credentials into the login input layout.
     */
    public void enterUsername(String username) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField)).clear();
        driver.findElement(usernameField).sendKeys(username);
    }

    /**
     * Enters password credentials into the login input layout.
     */
    public void enterPassword(String password) {
        driver.findElement(passwordField).sendKeys(password);
    }

    /**
     * Clicks the login submission trigger.
     */
    public void clickLogin() {
        driver.findElement(loginButton).click();
    }

    /**
     * High-level workflow wrapper for quick, seamless test setups.
     */
    public void loginWithCredentials(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    /**
     * Retrieves error message text if a login combination fails validation steps.
     */
    public String getErrorMessageText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }
}
