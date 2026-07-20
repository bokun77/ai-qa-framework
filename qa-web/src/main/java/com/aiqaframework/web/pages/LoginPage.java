package com.aiqaframework.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.id("loginButton");
    private static final By ERROR_MESSAGE = By.className("error");
    private static final By LANGUAGE_SNACKBAR = By.cssSelector(".mat-mdc-snack-bar-label");
    private static final By ACCOUNT_MENU_BUTTON = By.id("navbarAccount");
    private static final By LOGOUT_BUTTON = By.id("navbarLogoutButton");
    private static final By LOGIN_MENU_ITEM = By.id("navbarLoginButton");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUri) {
        driver.get(baseUri + "/#/login");
        dismissWelcomeBanner();
        waitForInvisible(LANGUAGE_SNACKBAR);
    }

    public void login(String email, String password) {
        waitForVisible(EMAIL_INPUT).sendKeys(email);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        clickWhenReady(LOGIN_BUTTON);
    }

    public String getErrorMessage() {
        return waitForVisible(ERROR_MESSAGE).getText();
    }

    public void waitForRedirectAwayFromLogin() {
        waitForUrlNotContaining("/login");
    }

    public void logout() {
        clickWhenReady(ACCOUNT_MENU_BUTTON);
        clickWhenReady(LOGOUT_BUTTON);
    }

    public boolean isLoggedOut() {
        clickWhenReady(ACCOUNT_MENU_BUTTON);
        return waitForVisible(LOGIN_MENU_ITEM).isDisplayed();
    }
}
