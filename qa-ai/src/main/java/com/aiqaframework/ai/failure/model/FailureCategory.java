package com.aiqaframework.ai.failure.model;

/**
 * Closed vocabulary for Failure Analyzer output categories, per ADR-0008's Output Contract.
 * v0.2 classifies deterministically ({@code RuleBasedFailureAnalyzer}) with no AI provider;
 * {@link #UNKNOWN} covers both "not analyzed" and "analyzed, no rule matched".
 */
public enum FailureCategory {
    UI_SYNCHRONIZATION,
    ASSERTION_FAILURE,
    API_FAILURE,
    AUTHENTICATION_FAILURE,
    ENVIRONMENT_FAILURE,
    UNKNOWN
}
