package com.aiqaframework.ai.failure.model;

/**
 * Analysis of a single failed {@link ExecutionRecord}, per ADR-0008's Output Contract.
 * Advisory only: never consumed automatically by test execution.
 *
 * @param testName       echoed verbatim from the source {@link ExecutionRecord}
 * @param sourceTimestamp echoed verbatim from the source {@link ExecutionRecord}
 * @param category       classification from the closed {@link FailureCategory} vocabulary
 * @param explanation    reasoning grounded only in the source record's message/screenshot
 * @param evidence       reference back to the exact source data used
 * @param confidence     0.0 (no confidence) to 1.0 (full confidence); v0.1 always reports 0.0
 */
public record FailureAnalysis(
        String testName,
        String sourceTimestamp,
        FailureCategory category,
        String explanation,
        String evidence,
        double confidence) {
}
