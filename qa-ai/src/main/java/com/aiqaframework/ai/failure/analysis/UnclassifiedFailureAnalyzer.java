package com.aiqaframework.ai.failure.analysis;

import com.aiqaframework.ai.failure.model.ExecutionRecord;
import com.aiqaframework.ai.failure.model.FailureAnalysis;
import com.aiqaframework.ai.failure.model.FailureCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Fallback analyzer: makes no attempt at classification, so every failure is returned
 * {@code UNKNOWN} with zero confidence. Useful where "not analyzed" must be distinguishable
 * from {@link RuleBasedFailureAnalyzer}'s "analyzed, but no rule matched" {@code UNKNOWN}.
 */
public final class UnclassifiedFailureAnalyzer implements FailureAnalyzer {

    private static final String EXPLANATION = "Not analyzed: qa-ai has no AI provider integrated.";

    @Override
    public List<FailureAnalysis> analyze(List<ExecutionRecord> failures) {
        List<FailureAnalysis> analyses = new ArrayList<>();
        for (ExecutionRecord failure : failures) {
            analyses.add(new FailureAnalysis(
                    failure.testName(),
                    failure.timestamp(),
                    FailureCategory.UNKNOWN,
                    EXPLANATION,
                    FailureEvidence.format(failure),
                    0.0));
        }
        return analyses;
    }
}
