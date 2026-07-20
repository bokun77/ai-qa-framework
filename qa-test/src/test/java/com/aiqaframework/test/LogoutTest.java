package com.aiqaframework.test;

import com.aiqaframework.core.TestConfig;
import com.aiqaframework.web.pages.LoginPage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/** Verifies a logged-in user can log out and the navbar reflects the logged-out state. */
public class LogoutTest extends BaseUiTest {

    @Test(groups = {"ui", "smoke"})
    public void loggedInUserCanLogOut() {
        LoginPage loginPage = new LoginPage(driver);

        loginPage.open(TestConfig.baseUri());
        loginPage.login(TestConfig.username(), TestConfig.password());
        loginPage.waitForRedirectAwayFromLogin();

        assertTrue(loginPage.isLoggedIn());

        loginPage.logout();

        assertTrue(loginPage.isLoggedOut());
    }
}
