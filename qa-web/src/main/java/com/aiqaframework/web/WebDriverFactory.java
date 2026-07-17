package com.aiqaframework.web;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public final class WebDriverFactory {

    private WebDriverFactory() {
    }

    public static WebDriver createChromeDriver() {
        return new ChromeDriver();
    }
}
