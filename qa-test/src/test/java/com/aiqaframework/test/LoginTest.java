package com.aiqaframework.test;

import com.aiqaframework.core.TestConfig;
import com.aiqaframework.web.pages.LoginPage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/** Verifies login succeeds with valid credentials and fails with a clear error otherwise. */
public class LoginTest extends BaseUiTest {

    @Test(groups = "ui")
    public void validCredentialsLogInSuccessfully() {
        LoginPage loginPage = new LoginPage(driver);

        loginPage.open(TestConfig.baseUri());
        loginPage.login(TestConfig.username(), TestConfig.password());
        loginPage.waitForRedirectAwayFromLogin();

        assertFalse(driver.getCurrentUrl().contains("/login"));
    }

    @Test(groups = "ui")
    public void invalidCredentialsShowError() {
        LoginPage loginPage = new LoginPage(driver);

        loginPage.open(TestConfig.baseUri());
        loginPage.login("wrong@juice-sh.op", "wrongpassword");

        assertEquals(loginPage.getErrorMessage(), "Invalid email or password.");
    }
}
