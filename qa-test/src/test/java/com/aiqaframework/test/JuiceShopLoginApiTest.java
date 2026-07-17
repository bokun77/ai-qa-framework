package com.aiqaframework.test;

import com.aiqaframework.api.ApiClient;
import com.aiqaframework.core.TestConfig;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/** Verifies the Juice Shop login API authenticates valid credentials and rejects invalid ones. */
public class JuiceShopLoginApiTest {

    @Test(groups = "api")
    public void loginWithValidCredentialsReturnsToken() {
        ApiClient apiClient = new ApiClient(TestConfig.baseUri());

        Response response = apiClient.post("/rest/user/login",
                Map.of("email", TestConfig.username(), "password", TestConfig.password()));

        assertEquals(response.statusCode(), 200);
        assertNotNull(response.jsonPath().getString("authentication.token"));
    }

    @Test(groups = "api")
    public void loginWithInvalidCredentialsReturnsUnauthorized() {
        ApiClient apiClient = new ApiClient(TestConfig.baseUri());

        Response response = apiClient.post("/rest/user/login",
                Map.of("email", "wrong@juice-sh.op", "password", "wrongpass"));

        assertEquals(response.statusCode(), 401);
    }
}
