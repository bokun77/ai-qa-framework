package com.aiqaframework.core;

public final class TestConfig {

    private static final String DEFAULT_BASE_URI = "http://localhost:3000";
    private static final String DEFAULT_USERNAME = "admin@juice-sh.op";
    private static final String DEFAULT_PASSWORD = "admin123";

    private TestConfig() {
    }

    public static String baseUri() {
        return System.getProperty("baseUri", DEFAULT_BASE_URI);
    }

    public static String username() {
        return System.getProperty("username", DEFAULT_USERNAME);
    }

    public static String password() {
        return System.getProperty("password", DEFAULT_PASSWORD);
    }
}
