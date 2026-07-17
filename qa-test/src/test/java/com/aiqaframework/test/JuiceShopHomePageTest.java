package com.aiqaframework.test;

import com.aiqaframework.core.TestConfig;
import com.aiqaframework.web.pages.JuiceShopHomePage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/** Verifies the Juice Shop homepage loads successfully in a browser. */
public class JuiceShopHomePageTest extends BaseUiTest {

    @Test(groups = "ui")
    public void homePageLoadsWithExpectedTitle() {
        JuiceShopHomePage homePage = new JuiceShopHomePage(driver);

        homePage.open(TestConfig.baseUri());

        assertEquals(homePage.getTitle(), "OWASP Juice Shop");
    }
}
