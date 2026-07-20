package com.aiqaframework.test;

import com.aiqaframework.core.TestConfig;
import com.aiqaframework.web.pages.ProductDetailsPage;
import com.aiqaframework.web.pages.ProductSearchPage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** Verifies opening product details from search results shows the matching product. */
public class ProductDetailsTest extends BaseUiTest {

    @Test(groups = {"ui", "regression"})
    public void openingProductDetailsShowsMatchingProduct() {
        ProductSearchPage searchPage = new ProductSearchPage(driver);

        searchPage.open(TestConfig.baseUri());
        searchPage.search("Apple");
        String firstResultName = searchPage.getResultNames().get(0);

        searchPage.openFirstResultDetails();

        ProductDetailsPage detailsPage = new ProductDetailsPage(driver);
        assertTrue(detailsPage.isDisplayed());
        assertEquals(detailsPage.getProductName(), firstResultName);
        assertTrue(detailsPage.isPriceVisible());

        detailsPage.close();

        assertTrue(detailsPage.isDismissed());
    }
}
