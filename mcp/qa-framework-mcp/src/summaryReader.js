import { existsSync, readFileSync } from "node:fs";

const FIELD_DELIMITER = " | ";
const SCREENSHOT_PREFIX = "screenshot=";
const MESSAGE_PREFIX = "message=";

/**
 * Reverses ExecutionReportWriter#escape (qa-reporting). escape() doubles backslashes
 * first, then escapes "|", so unescaping must undo pipe-escaping before backslash-unescaping.
 */
function unescapeField(value) {
  if (value == null) return null;
  return value.split("\\|").join("|").split("\\\\").join("\\");
}

function parseLine(line) {
  const fields = line.split(FIELD_DELIMITER);
  if (fields.length < 3) {
    return null;
  }

  const [, rawStatus, rawName, ...rest] = fields;

  let screenshot = null;
  let message = null;

  for (const field of rest) {
    if (field.startsWith(SCREENSHOT_PREFIX)) {
      screenshot = unescapeField(field.slice(SCREENSHOT_PREFIX.length));
    } else if (field.startsWith(MESSAGE_PREFIX)) {
      message = unescapeField(field.slice(MESSAGE_PREFIX.length));
    }
  }

  return {
    name: unescapeField(rawName),
    status: unescapeField(rawStatus),
    screenshot,
    message,
  };
}

function emptyResult(sourcePath, note) {
  return {
    sourcePath,
    exists: false,
    note,
    totalTests: 0,
    passed: 0,
    failed: 0,
    skipped: 0,
    tests: [],
  };
}

/**
 * Reads and parses qa-reporting's ExecutionReportWriter output.
 * Read-only: only reads the file, never writes, executes, or modifies anything.
 * Returns a clear empty response (exists: false) if the file is missing.
 */
export function readExecutionSummary(sourcePath) {
  if (!existsSync(sourcePath)) {
    return emptyResult(
      sourcePath,
      "execution-summary.txt not found. Run the qa-test suite to generate it."
    );
  }

  const content = readFileSync(sourcePath, "utf8");
  const tests = content
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line.length > 0)
    .map(parseLine)
    .filter((record) => record !== null);

  let passed = 0;
  let failed = 0;
  let skipped = 0;

  for (const test of tests) {
    const status = test.status ? test.status.toUpperCase() : "";
    if (status === "PASS") passed += 1;
    else if (status === "FAIL") failed += 1;
    else if (status === "SKIP") skipped += 1;
  }

  return {
    sourcePath,
    exists: true,
    totalTests: tests.length,
    passed,
    failed,
    skipped,
    tests,
  };
}
