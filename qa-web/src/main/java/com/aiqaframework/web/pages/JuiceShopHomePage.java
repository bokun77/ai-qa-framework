package com.aiqaframework.web.pages;

import org.openqa.selenium.WebDriver;

public class JuiceShopHomePage extends BasePage {

    public JuiceShopHomePage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl);
    }

    public String getTitle() {
        return driver.getTitle();
    }
}
