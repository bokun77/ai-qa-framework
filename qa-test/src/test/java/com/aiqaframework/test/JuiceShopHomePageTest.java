package com.aiqaframework.test;

import com.aiqaframework.web.WebDriverFactory;
import com.aiqaframework.web.pages.JuiceShopHomePage;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/** Verifies the Juice Shop homepage loads successfully in a browser. */
public class JuiceShopHomePageTest {

    private static final String BASE_URI = "http://localhost:3000";

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = WebDriverFactory.createChromeDriver();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void homePageLoadsWithExpectedTitle() {
        JuiceShopHomePage homePage = new JuiceShopHomePage(driver);

        homePage.open(BASE_URI);

        assertEquals(homePage.getTitle(), "OWASP Juice Shop");
    }
}
