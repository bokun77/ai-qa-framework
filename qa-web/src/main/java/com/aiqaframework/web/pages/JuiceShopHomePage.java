package com.aiqaframework.web.pages;

import org.openqa.selenium.WebDriver;

public class JuiceShopHomePage {

    private final WebDriver driver;

    public JuiceShopHomePage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl);
    }

    public String getTitle() {
        return driver.getTitle();
    }
}
