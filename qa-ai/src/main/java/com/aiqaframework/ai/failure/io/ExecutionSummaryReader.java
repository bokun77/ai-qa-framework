package com.aiqaframework.ai.failure.io;

import com.aiqaframework.ai.failure.model.ExecutionRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reads qa-reporting's {@code ExecutionReportWriter} output (execution-summary.txt) and
 * returns its FAIL records as {@link ExecutionRecord}s.
 *
 * Read-only: only reads the file, never writes, executes, or modifies anything. Consumes the
 * artifact by path only, per ADR-0008 - no dependency on qa-reporting is introduced.
 */
public final class ExecutionSummaryReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionSummaryReader.class);

    private static final String FIELD_DELIMITER = " | ";
    private static final Pattern FIELD_DELIMITER_PATTERN = Pattern.compile(Pattern.quote(FIELD_DELIMITER));
    private static final String SCREENSHOT_PREFIX = "screenshot=";
    private static final String MESSAGE_PREFIX = "message=";
    private static final String FAIL_STATUS = "FAIL";

    private ExecutionSummaryReader() {
    }

    /**
     * Reads {@code sourcePath} and returns only its FAIL records, in file order. Returns an
     * empty list, never an exception, if the file is missing or unreadable.
     */
    public static List<ExecutionRecord> readFailures(Path sourcePath) {
        if (!Files.exists(sourcePath)) {
            return List.of();
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(sourcePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Failed to read execution summary at {}", sourcePath, e);
            return List.of();
        }

        List<ExecutionRecord> failures = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            ExecutionRecord record = parseLine(trimmed);
            if (record != null && FAIL_STATUS.equals(record.status())) {
                failures.add(record);
            }
        }
        return failures;
    }

    private static ExecutionRecord parseLine(String line) {
        String[] fields = FIELD_DELIMITER_PATTERN.split(line, -1);
        if (fields.length < 3) {
            return null;
        }

        String timestamp = fields[0];
        String status = unescape(fields[1]);
        String testName = unescape(fields[2]);

        String screenshot = null;
        String message = null;
        for (int i = 3; i < fields.length; i++) {
            String field = fields[i];
            if (field.startsWith(SCREENSHOT_PREFIX)) {
                screenshot = unescape(field.substring(SCREENSHOT_PREFIX.length()));
            } else if (field.startsWith(MESSAGE_PREFIX)) {
                message = unescape(field.substring(MESSAGE_PREFIX.length()));
            }
        }

        return new ExecutionRecord(testName, status, timestamp, screenshot, message);
    }

    /**
     * Reverses ExecutionReportWriter#escape. escape() doubles backslashes first, then escapes
     * "|", so unescaping must undo pipe-escaping before backslash-unescaping.
     */
    private static String unescape(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\|", "|").replace("\\\\", "\\");
    }
}
