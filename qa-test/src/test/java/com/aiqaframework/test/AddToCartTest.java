package com.aiqaframework.test;

import com.aiqaframework.core.TestConfig;
import com.aiqaframework.web.pages.ProductSearchPage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/** Verifies adding a product to the cart increments the cart item count. */
public class AddToCartTest extends BaseUiTest {

    @Test(groups = {"ui", "regression"})
    public void addingProductToCartIncrementsCartCount() {
        ProductSearchPage searchPage = new ProductSearchPage(driver);

        searchPage.open(TestConfig.baseUri());
        searchPage.search("Apple");
        int initialCount = Integer.parseInt(searchPage.getCartItemCount());

        searchPage.addFirstResultToCart();

        assertEquals(Integer.parseInt(searchPage.getCartItemCount()), initialCount + 1);
    }
}
