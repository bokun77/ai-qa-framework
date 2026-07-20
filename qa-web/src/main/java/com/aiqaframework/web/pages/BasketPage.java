package com.aiqaframework.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class BasketPage extends BasePage {

    private static final By CART_BUTTON = By.cssSelector("button[aria-label='Show the shopping cart']");
    private static final By ITEM_ROW = By.cssSelector("app-purchase-basket mat-row");
    private static final By ITEM_PRODUCT_NAME = By.cssSelector(".cdk-column-product");

    public BasketPage(WebDriver driver) {
        super(driver);
    }

    public void openViaCartIcon() {
        clickWhenReady(CART_BUTTON);
        waitForVisible(ITEM_ROW);
    }

    public List<String> getItemNames() {
        return driver.findElements(ITEM_ROW).stream()
                .map(row -> row.findElement(ITEM_PRODUCT_NAME).getText().trim())
                .toList();
    }
}
