package com.aiqaframework.ai.failure.analysis;

import com.aiqaframework.ai.failure.model.ExecutionRecord;

/** Formats an {@link ExecutionRecord}'s message/screenshot into a FailureAnalysis evidence string. */
final class FailureEvidence {

    private static final String NO_EVIDENCE = "No message or screenshot recorded for this failure.";

    private FailureEvidence() {
    }

    static String format(ExecutionRecord failure) {
        StringBuilder evidence = new StringBuilder();
        if (failure.message() != null) {
            evidence.append("message=").append(failure.message());
        }
        if (failure.screenshot() != null) {
            if (evidence.length() > 0) {
                evidence.append("; ");
            }
            evidence.append("screenshot=").append(failure.screenshot());
        }
        return evidence.length() > 0 ? evidence.toString() : NO_EVIDENCE;
    }
}
