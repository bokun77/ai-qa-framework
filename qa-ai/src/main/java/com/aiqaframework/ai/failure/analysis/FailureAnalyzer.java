package com.aiqaframework.ai.failure.analysis;

import com.aiqaframework.ai.failure.model.ExecutionRecord;
import com.aiqaframework.ai.failure.model.FailureAnalysis;

import java.util.List;

/** Produces one {@link FailureAnalysis} per failed {@link ExecutionRecord}, per ADR-0008. */
public interface FailureAnalyzer {

    List<FailureAnalysis> analyze(List<ExecutionRecord> failures);
}
