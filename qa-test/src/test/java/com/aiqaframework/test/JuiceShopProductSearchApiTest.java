package com.aiqaframework.test;

import com.aiqaframework.api.ApiClient;
import com.aiqaframework.core.TestConfig;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** Verifies the Juice Shop product search API returns products matching the query. */
public class JuiceShopProductSearchApiTest {

    @Test(groups = "api")
    public void searchReturnsMatchingProducts() {
        ApiClient apiClient = new ApiClient(TestConfig.baseUri());

        Response response = apiClient.get("/rest/products/search?q=apple");

        assertEquals(response.statusCode(), 200);
        assertTrue(response.jsonPath().getList("data.name", String.class)
                .stream().anyMatch(name -> name.toLowerCase().contains("apple")));
    }

    @Test(groups = "api")
    public void searchWithNoMatchReturnsEmptyList() {
        ApiClient apiClient = new ApiClient(TestConfig.baseUri());

        Response response = apiClient.get("/rest/products/search?q=zzzznotarealproduct123");

        assertEquals(response.statusCode(), 200);
        assertTrue(response.jsonPath().getList("data").isEmpty());
    }

    @Test(groups = "api")
    public void searchWithEmptyQueryReturnsAllProducts() {
        ApiClient apiClient = new ApiClient(TestConfig.baseUri());

        Response response = apiClient.get("/rest/products/search?q=");

        assertEquals(response.statusCode(), 200);
        assertTrue(response.jsonPath().getList("data").size() > 0);
    }

    @Test(groups = "api")
    public void searchWithSpecialCharactersReturnsEmptyList() {
        ApiClient apiClient = new ApiClient(TestConfig.baseUri());

        Response response = apiClient.get("/rest/products/search?q=!@$*()~");

        assertEquals(response.statusCode(), 200);
        assertTrue(response.jsonPath().getList("data").isEmpty());
    }

    /**
     * Documents a known Juice Shop SQL-injection vulnerability in the search endpoint:
     * an unescaped query breaks the backing query and surfaces a raw SQLite error (500)
     * instead of a handled 4xx response. Regression check, not exploitation.
     */
    @Test(groups = "api")
    public void searchWithSqlInjectionPayloadReturnsServerError() {
        ApiClient apiClient = new ApiClient(TestConfig.baseUri());

        Response response = apiClient.get("/rest/products/search?q='\";--");

        assertEquals(response.statusCode(), 500);
    }
}
