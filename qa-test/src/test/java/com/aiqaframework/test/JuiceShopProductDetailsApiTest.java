package com.aiqaframework.test;

import com.aiqaframework.api.ApiClient;
import com.aiqaframework.core.TestConfig;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/** Verifies the Juice Shop single product API returns product data for a valid id and 404 for an unknown id. */
public class JuiceShopProductDetailsApiTest {

    private static final int VALID_PRODUCT_ID = 1;
    private static final int UNKNOWN_PRODUCT_ID = 999999;

    @Test(groups = {"api", "regression"})
    public void getProductByIdReturnsOk() {
        ApiClient apiClient = new ApiClient(TestConfig.baseUri());

        Response response = apiClient.get("/api/Products/" + VALID_PRODUCT_ID);

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getInt("data.id"), VALID_PRODUCT_ID);
    }

    @Test(groups = {"api", "regression"})
    public void getProductByUnknownIdReturnsNotFound() {
        ApiClient apiClient = new ApiClient(TestConfig.baseUri());

        Response response = apiClient.get("/api/Products/" + UNKNOWN_PRODUCT_ID);

        assertEquals(response.statusCode(), 404);
    }
}
