package com.aiqaframework.test;

import com.aiqaframework.api.ApiClient;
import com.aiqaframework.core.TestConfig;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** Verifies the Juice Shop products API responds successfully with product data. */
public class JuiceShopProductsApiTest {

    @Test(groups = {"api", "regression"})
    public void getProductsReturnsOk() {
        ApiClient apiClient = new ApiClient(TestConfig.baseUri());

        Response response = apiClient.get("/api/Products");

        assertEquals(response.statusCode(), 200);
        assertTrue(response.jsonPath().getList("data").size() > 0);
    }
}
