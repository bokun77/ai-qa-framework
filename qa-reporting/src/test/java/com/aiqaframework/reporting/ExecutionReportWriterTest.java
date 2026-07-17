package com.aiqaframework.reporting;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ExecutionReportWriterTest {

    private static final Path SUMMARY_FILE = Path.of("target", "qa-reports", "execution-summary.txt");

    @BeforeMethod
    public void resetSummaryFile() throws IOException {
        Files.createDirectories(SUMMARY_FILE.getParent());
        Files.deleteIfExists(SUMMARY_FILE);
    }

    @Test
    public void recordsPassWithoutOptionalFields() throws IOException {
        ExecutionReportWriter.record("com.example.SampleTest.shouldPass", "PASS", null, null);

        String line = onlyLine();
        assertTrue(line.contains(" | PASS | com.example.SampleTest.shouldPass"));
        assertFalse(line.contains("screenshot="));
        assertFalse(line.contains("message="));
    }

    @Test
    public void recordsFailureWithScreenshotAndMessage() throws IOException {
        Path screenshot = Path.of("target", "screenshots", "shot.png");

        ExecutionReportWriter.record("com.example.SampleTest.shouldFail", "FAIL", screenshot, "expected true but was false");

        String line = onlyLine();
        assertTrue(line.contains("screenshot=" + screenshot));
        assertTrue(line.contains("message=expected true but was false"));
    }

    @Test
    public void escapesPipeInFailureMessageSoFieldCountIsPreserved() throws IOException {
        ExecutionReportWriter.record("com.example.SampleTest.shouldEscape", "FAIL", null, "expected 5 | actual 3");

        String line = onlyLine();
        // Delimiter is literally " | "; an escaped pipe must never reproduce that exact sequence,
        // otherwise a future consumer splitting on it would see a corrupted field count.
        String[] fields = line.split(" \\| ");

        assertEquals(fields.length, 4);
        assertEquals(fields[3], "message=expected 5 \\| actual 3");
    }

    @Test
    public void escapesBackslashesInFailureMessage() throws IOException {
        ExecutionReportWriter.record("com.example.SampleTest.shouldEscapeBackslash", "FAIL", null, "C:\\Temp\\file.png");

        String line = onlyLine();
        assertTrue(line.contains("message=C:\\\\Temp\\\\file.png"));
    }

    @Test
    public void appendsMultipleRecordsAsSeparateLines() throws IOException {
        ExecutionReportWriter.record("com.example.SampleTest.first", "PASS", null, null);
        ExecutionReportWriter.record("com.example.SampleTest.second", "SKIP", null, null);

        List<String> lines = Files.readAllLines(SUMMARY_FILE);
        assertEquals(lines.size(), 2);
        assertTrue(lines.get(0).contains("first"));
        assertTrue(lines.get(1).contains("second"));
    }

    private String onlyLine() throws IOException {
        List<String> lines = Files.readAllLines(SUMMARY_FILE);
        assertEquals(lines.size(), 1);
        return lines.get(0);
    }
}
