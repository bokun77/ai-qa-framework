package com.aiqaframework.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.id("loginButton");
    private static final By ERROR_MESSAGE = By.className("error");
    private static final By LANGUAGE_SNACKBAR = By.cssSelector(".mat-mdc-snack-bar-label");
    private static final By WELCOME_BANNER_CLOSE = By.cssSelector("button[aria-label='Close Welcome Banner']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUri) {
        driver.get(baseUri + "/#/login");
        dismissWelcomeBanner();
        waitForInvisible(LANGUAGE_SNACKBAR);
    }

    private void dismissWelcomeBanner() {
        try {
            waitForClickable(WELCOME_BANNER_CLOSE).click();
        } catch (org.openqa.selenium.TimeoutException ignored) {
        }
    }

    public void login(String email, String password) {
        waitForVisible(EMAIL_INPUT).sendKeys(email);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        waitForClickable(LOGIN_BUTTON).click();
    }

    public String getErrorMessage() {
        return waitForVisible(ERROR_MESSAGE).getText();
    }

    public void waitForRedirectAwayFromLogin() {
        waitForUrlNotContaining("/login");
    }
}
