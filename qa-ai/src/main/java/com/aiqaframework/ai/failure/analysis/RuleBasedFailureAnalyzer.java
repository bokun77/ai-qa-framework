package com.aiqaframework.ai.failure.analysis;

import com.aiqaframework.ai.failure.model.ExecutionRecord;
import com.aiqaframework.ai.failure.model.FailureAnalysis;
import com.aiqaframework.ai.failure.model.FailureCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Deterministic, keyword-based failure classifier. No AI provider is used: classification is a
 * fixed, ordered set of substring/regex checks against the failure message only, per ADR-0008's
 * Output Contract (closed category vocabulary, explanation grounded only in the source record).
 * Categories are checked in a fixed precedence order; the first match wins.
 */
public final class RuleBasedFailureAnalyzer implements FailureAnalyzer {

    private static final Pattern HTTP_STATUS_PATTERN = Pattern.compile("\\b[45]\\d{2}\\b");

    private static final String[] UI_SYNCHRONIZATION_KEYWORDS = {
            "elementclickinterceptedexception", "cdk overlay", "cdk-overlay", "snackbar"
    };

    private static final String[] API_KEYWORDS = {
            "api request", "api call", "api failure", "http error"
    };

    private static final String[] AUTHENTICATION_KEYWORDS = {
            "login", "auth", "token"
    };

    private static final String[] ENVIRONMENT_KEYWORDS = {
            "connection refused", "timed out", "timeout", "connect exception", "socket timeout"
    };

    @Override
    public List<FailureAnalysis> analyze(List<ExecutionRecord> failures) {
        List<FailureAnalysis> analyses = new ArrayList<>();
        for (ExecutionRecord failure : failures) {
            analyses.add(classify(failure));
        }
        return analyses;
    }

    private FailureAnalysis classify(ExecutionRecord failure) {
        String message = failure.message() == null ? "" : failure.message().toLowerCase();

        FailureCategory category;
        String explanation;

        if (containsAny(message, UI_SYNCHRONIZATION_KEYWORDS)) {
            category = FailureCategory.UI_SYNCHRONIZATION;
            explanation = "Message indicates an intercepted click or an overlay/snackbar timing issue.";
        } else if (message.contains("assertionerror") || (message.contains("expected") && message.contains("actual"))) {
            category = FailureCategory.ASSERTION_FAILURE;
            explanation = "Message indicates an assertion mismatch between expected and actual values.";
        } else if (HTTP_STATUS_PATTERN.matcher(message).find() || containsAny(message, API_KEYWORDS)) {
            category = FailureCategory.API_FAILURE;
            explanation = "Message indicates an HTTP 4xx/5xx response or an API request failure.";
        } else if (containsAny(message, AUTHENTICATION_KEYWORDS)) {
            category = FailureCategory.AUTHENTICATION_FAILURE;
            explanation = "Message indicates a login, authentication, or token failure.";
        } else if (containsAny(message, ENVIRONMENT_KEYWORDS)) {
            category = FailureCategory.ENVIRONMENT_FAILURE;
            explanation = "Message indicates a connection refusal or timeout, suggesting an environment/infrastructure issue.";
        } else {
            category = FailureCategory.UNKNOWN;
            explanation = "No rule matched the recorded failure message.";
        }

        return new FailureAnalysis(
                failure.testName(),
                failure.timestamp(),
                category,
                explanation,
                FailureEvidence.format(failure),
                category == FailureCategory.UNKNOWN ? 0.0 : 1.0);
    }

    private boolean containsAny(String haystack, String[] needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
