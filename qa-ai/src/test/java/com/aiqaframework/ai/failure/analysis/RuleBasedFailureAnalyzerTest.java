package com.aiqaframework.ai.failure.analysis;

import com.aiqaframework.ai.failure.model.ExecutionRecord;
import com.aiqaframework.ai.failure.model.FailureAnalysis;
import com.aiqaframework.ai.failure.model.FailureCategory;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class RuleBasedFailureAnalyzerTest {

    private final RuleBasedFailureAnalyzer analyzer = new RuleBasedFailureAnalyzer();

    @Test
    public void classifiesElementClickInterceptedAsUiSynchronization() {
        FailureAnalysis analysis = classify(
                "org.openqa.selenium.ElementClickInterceptedException: element click intercepted");

        assertEquals(analysis.category(), FailureCategory.UI_SYNCHRONIZATION);
        assertEquals(analysis.confidence(), 1.0);
    }

    @Test
    public void classifiesCdkOverlayAsUiSynchronization() {
        FailureAnalysis analysis = classify("Element is covered by a cdk overlay container");

        assertEquals(analysis.category(), FailureCategory.UI_SYNCHRONIZATION);
    }

    @Test
    public void classifiesSnackbarAsUiSynchronization() {
        FailureAnalysis analysis = classify("Click blocked by a snackbar still visible on screen");

        assertEquals(analysis.category(), FailureCategory.UI_SYNCHRONIZATION);
    }

    @Test
    public void classifiesAssertionErrorAsAssertionFailure() {
        FailureAnalysis analysis = classify("java.lang.AssertionError: expected [true] but found [false]");

        assertEquals(analysis.category(), FailureCategory.ASSERTION_FAILURE);
        assertEquals(analysis.confidence(), 1.0);
    }

    @Test
    public void classifiesExpectedActualWordingAsAssertionFailure() {
        FailureAnalysis analysis = classify("expected 5 but actual was 3");

        assertEquals(analysis.category(), FailureCategory.ASSERTION_FAILURE);
    }

    @Test
    public void classifiesHttpStatusCodeAsApiFailure() {
        FailureAnalysis analysis = classify("API call failed: received 503 Service Unavailable");

        assertEquals(analysis.category(), FailureCategory.API_FAILURE);
        assertEquals(analysis.confidence(), 1.0);
    }

    @Test
    public void classifiesApiRequestWordingAsApiFailure() {
        FailureAnalysis analysis = classify("api request failed with no response body");

        assertEquals(analysis.category(), FailureCategory.API_FAILURE);
    }

    @Test
    public void classifiesLoginFailureAsAuthenticationFailure() {
        FailureAnalysis analysis = classify("login failed: invalid credentials");

        assertEquals(analysis.category(), FailureCategory.AUTHENTICATION_FAILURE);
        assertEquals(analysis.confidence(), 1.0);
    }

    @Test
    public void classifiesTokenFailureAsAuthenticationFailure() {
        FailureAnalysis analysis = classify("auth token expired before request completed");

        assertEquals(analysis.category(), FailureCategory.AUTHENTICATION_FAILURE);
    }

    @Test
    public void classifiesConnectionRefusedAsEnvironmentFailure() {
        FailureAnalysis analysis = classify("java.net.ConnectException: Connection refused");

        assertEquals(analysis.category(), FailureCategory.ENVIRONMENT_FAILURE);
        assertEquals(analysis.confidence(), 1.0);
    }

    @Test
    public void classifiesTimeoutAsEnvironmentFailure() {
        FailureAnalysis analysis = classify("Request timed out after 30000ms");

        assertEquals(analysis.category(), FailureCategory.ENVIRONMENT_FAILURE);
    }

    @Test
    public void classifiesUnmatchedMessageAsUnknown() {
        FailureAnalysis analysis = classify("NullPointerException at line 42");

        assertEquals(analysis.category(), FailureCategory.UNKNOWN);
        assertEquals(analysis.confidence(), 0.0);
    }

    @Test
    public void classifiesNullMessageAsUnknown() {
        ExecutionRecord failure = new ExecutionRecord(
                "com.example.SampleTest.shouldFail", "FAIL", "2026-07-20T10:00:00", null, null);

        FailureAnalysis analysis = analyzer.analyze(List.of(failure)).get(0);

        assertEquals(analysis.category(), FailureCategory.UNKNOWN);
    }

    @Test
    public void echoesTestNameAndTimestampFromSourceRecord() {
        ExecutionRecord failure = new ExecutionRecord(
                "com.example.SampleTest.shouldFail", "FAIL", "2026-07-20T10:00:00", null, "login failed");

        FailureAnalysis analysis = analyzer.analyze(List.of(failure)).get(0);

        assertEquals(analysis.testName(), "com.example.SampleTest.shouldFail");
        assertEquals(analysis.sourceTimestamp(), "2026-07-20T10:00:00");
    }

    @Test
    public void uiSynchronizationTakesPrecedenceOverApiFailure() {
        FailureAnalysis analysis = classify(
                "org.openqa.selenium.ElementClickInterceptedException: element click intercepted, "
                        + "server also returned HTTP 500");

        assertEquals(analysis.category(), FailureCategory.UI_SYNCHRONIZATION);
    }

    @Test
    public void apiFailureTakesPrecedenceOverEnvironmentFailure() {
        FailureAnalysis analysis = classify(
                "API call failed: HTTP 500 error, connection refused during retry");

        assertEquals(analysis.category(), FailureCategory.API_FAILURE);
    }

    @Test
    public void authenticationFailureTakesPrecedenceOverEnvironmentFailure() {
        FailureAnalysis analysis = classify("login failed after request timeout");

        assertEquals(analysis.category(), FailureCategory.AUTHENTICATION_FAILURE);
    }

    private FailureAnalysis classify(String message) {
        ExecutionRecord failure = new ExecutionRecord(
                "com.example.SampleTest.shouldFail", "FAIL", "2026-07-20T10:00:00", null, message);
        return analyzer.analyze(List.of(failure)).get(0);
    }
}
