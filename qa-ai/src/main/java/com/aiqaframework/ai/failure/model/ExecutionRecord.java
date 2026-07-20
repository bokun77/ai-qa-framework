package com.aiqaframework.ai.failure.model;

/**
 * One parsed line from qa-reporting's execution-summary.txt.
 *
 * @param testName   fully-qualified {@code <TestClass>.<methodName>}
 * @param status     {@code PASS}, {@code FAIL}, or {@code SKIP} as recorded by ExecutionReportWriter
 * @param timestamp  the raw timestamp field, as recorded (not parsed)
 * @param screenshot screenshot path, present only for some UI test failures; may be {@code null}
 * @param message    failure message, present only for some failures; may be {@code null}
 */
public record ExecutionRecord(String testName, String status, String timestamp, String screenshot, String message) {
}
