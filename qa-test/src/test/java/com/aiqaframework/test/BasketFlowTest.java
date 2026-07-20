package com.aiqaframework.test;

import com.aiqaframework.core.TestConfig;
import com.aiqaframework.web.pages.BasketPage;
import com.aiqaframework.web.pages.ProductSearchPage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/** Verifies a product added to the cart from search results appears in the basket. */
public class BasketFlowTest extends BaseUiTest {

    @Test(groups = {"ui", "regression"})
    public void addedProductAppearsInBasket() {
        ProductSearchPage searchPage = new ProductSearchPage(driver);

        searchPage.open(TestConfig.baseUri());
        searchPage.search("Apple");
        String addedProductName = searchPage.getResultNames().get(0);

        searchPage.addFirstResultToCart();

        BasketPage basketPage = new BasketPage(driver);
        basketPage.openViaCartIcon();

        assertTrue(basketPage.getItemNames().stream()
                .anyMatch(name -> name.contains(addedProductName)));
    }
}
