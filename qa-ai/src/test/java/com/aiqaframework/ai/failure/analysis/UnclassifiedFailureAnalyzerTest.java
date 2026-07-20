package com.aiqaframework.ai.failure.analysis;

import com.aiqaframework.ai.failure.model.ExecutionRecord;
import com.aiqaframework.ai.failure.model.FailureAnalysis;
import com.aiqaframework.ai.failure.model.FailureCategory;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class UnclassifiedFailureAnalyzerTest {

    private final UnclassifiedFailureAnalyzer analyzer = new UnclassifiedFailureAnalyzer();

    @Test
    public void alwaysReturnsUnknownCategoryWithZeroConfidence() {
        ExecutionRecord failure = new ExecutionRecord(
                "com.example.SampleTest.shouldFail", "FAIL", "2026-07-20T10:00:00",
                "target/screenshots/shot.png", "expected true but was false");

        List<FailureAnalysis> analyses = analyzer.analyze(List.of(failure));

        assertEquals(analyses.size(), 1);
        FailureAnalysis analysis = analyses.get(0);
        assertEquals(analysis.testName(), "com.example.SampleTest.shouldFail");
        assertEquals(analysis.sourceTimestamp(), "2026-07-20T10:00:00");
        assertEquals(analysis.category(), FailureCategory.UNKNOWN);
        assertEquals(analysis.confidence(), 0.0);
        assertTrue(analysis.explanation().toLowerCase().contains("not analyzed"));
        assertTrue(analysis.evidence().contains("expected true but was false"));
        assertTrue(analysis.evidence().contains("target/screenshots/shot.png"));
    }

    @Test
    public void reportsNoEvidenceWhenNeitherMessageNorScreenshotIsPresent() {
        ExecutionRecord failure = new ExecutionRecord(
                "com.example.SampleTest.shouldFail", "FAIL", "2026-07-20T10:00:00", null, null);

        FailureAnalysis analysis = analyzer.analyze(List.of(failure)).get(0);

        assertEquals(analysis.evidence(), "No message or screenshot recorded for this failure.");
    }

    @Test
    public void producesOneAnalysisPerInputFailure() {
        ExecutionRecord first = new ExecutionRecord(
                "com.example.SampleTest.first", "FAIL", "2026-07-20T10:00:00", null, "boom");
        ExecutionRecord second = new ExecutionRecord(
                "com.example.SampleTest.second", "FAIL", "2026-07-20T10:00:01", null, "bang");

        List<FailureAnalysis> analyses = analyzer.analyze(List.of(first, second));

        assertEquals(analyses.size(), 2);
        assertEquals(analyses.get(0).testName(), "com.example.SampleTest.first");
        assertEquals(analyses.get(1).testName(), "com.example.SampleTest.second");
    }
}
