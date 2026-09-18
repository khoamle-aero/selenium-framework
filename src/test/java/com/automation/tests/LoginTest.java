package com.automation.tests;

import com.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(description = "Verify successful login with valid credentials")
    public void testSuccessfulLogin() {
        // 1. Navigate to the sandbox web application
        getDriver().get("https://saucedemo.com");

        // 2. Initialize the Page Object with our thread-safe driver
        LoginPage loginPage = new LoginPage(getDriver());

        // 3. Perform actions using the page object workflow wrapper
        loginPage.loginWithCredentials("standard_user", "secret_sauce");

        // 4. Assert that the login was successful by validating the current URL changing to inventory page
        String currentUrl = getDriver().getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("inventory.html"),
                "Login validation failed! Current URL does not contain inventory path: " + currentUrl);
    }

    @Test(description = "Verify error message is displayed when logging in with locked out user")
    public void testLockedOutUserLoginError() {
        getDriver().get("https://saucedemo.com");

        LoginPage loginPage = new LoginPage(getDriver());

        // Use credentials for a locked out user account
        loginPage.loginWithCredentials("locked_out_user", "secret_sauce");

        // Assert that the page object returns the expected validation error text
        String actualErrorMessage = loginPage.getErrorMessageText();
        String expectedErrorMessage = "Epic sadface: Sorry, this user has been locked out.";

        Assert.assertEquals(actualErrorMessage, expectedErrorMessage,
                "Error validation failed! The error message text did not match.");
    }
}
