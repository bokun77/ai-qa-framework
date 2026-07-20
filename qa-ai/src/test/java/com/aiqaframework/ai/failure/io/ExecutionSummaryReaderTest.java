package com.aiqaframework.ai.failure.io;

import com.aiqaframework.ai.failure.model.ExecutionRecord;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ExecutionSummaryReaderTest {

    @Test
    public void ignoresPassRecords() throws IOException {
        Path file = writeLines(
                "2026-07-20T10:00:00 | PASS | com.example.SampleTest.shouldPass");

        List<ExecutionRecord> failures = ExecutionSummaryReader.readFailures(file);

        assertTrue(failures.isEmpty());
    }

    @Test
    public void parsesFailedRecordWithScreenshotAndMessage() throws IOException {
        Path file = writeLines(
                "2026-07-20T10:00:01 | FAIL | com.example.SampleTest.shouldFail | screenshot=target/screenshots/shot.png | message=expected true but was false");

        List<ExecutionRecord> failures = ExecutionSummaryReader.readFailures(file);

        assertEquals(failures.size(), 1);
        ExecutionRecord record = failures.get(0);
        assertEquals(record.testName(), "com.example.SampleTest.shouldFail");
        assertEquals(record.status(), "FAIL");
        assertEquals(record.timestamp(), "2026-07-20T10:00:01");
        assertEquals(record.screenshot(), "target/screenshots/shot.png");
        assertEquals(record.message(), "expected true but was false");
    }

    @Test
    public void unescapesPipeAndBackslashInMessage() throws IOException {
        Path file = writeLines(
                "2026-07-20T10:00:02 | FAIL | com.example.SampleTest.shouldEscape | message=expected 5 \\| actual 3, path C:\\\\Temp\\\\file.png");

        List<ExecutionRecord> failures = ExecutionSummaryReader.readFailures(file);

        assertEquals(failures.size(), 1);
        assertEquals(failures.get(0).message(), "expected 5 | actual 3, path C:\\Temp\\file.png");
        assertNull(failures.get(0).screenshot());
    }

    @Test
    public void returnsEmptyListWhenFileIsMissing() {
        Path missing = Path.of("target", "does-not-exist-" + System.nanoTime() + ".txt");

        List<ExecutionRecord> failures = ExecutionSummaryReader.readFailures(missing);

        assertTrue(failures.isEmpty());
    }

    @Test
    public void onlyFailRecordsSurviveAMixedFile() throws IOException {
        Path file = writeLines(
                "2026-07-20T10:00:03 | PASS | com.example.SampleTest.first",
                "2026-07-20T10:00:04 | FAIL | com.example.SampleTest.second | message=boom",
                "2026-07-20T10:00:05 | SKIP | com.example.SampleTest.third");

        List<ExecutionRecord> failures = ExecutionSummaryReader.readFailures(file);

        assertEquals(failures.size(), 1);
        assertEquals(failures.get(0).testName(), "com.example.SampleTest.second");
    }

    private Path writeLines(String... lines) throws IOException {
        Path dir = Path.of("target", "test-summaries");
        Files.createDirectories(dir);
        Path file = dir.resolve("execution-summary-" + System.nanoTime() + ".txt");
        Files.write(file, List.of(lines));
        return file;
    }
}
