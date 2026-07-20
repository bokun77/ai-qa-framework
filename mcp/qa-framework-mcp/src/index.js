#!/usr/bin/env node
import { fileURLToPath } from "node:url";
import { dirname, resolve } from "node:path";
import { existsSync } from "node:fs";

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";

import { scanTestInventory } from "./scanner.js";
import { readExecutionSummary } from "./summaryReader.js";

const __dirname = dirname(fileURLToPath(import.meta.url));

// mcp/qa-framework-mcp/src -> mcp/qa-framework-mcp -> mcp -> repo root
const REPO_ROOT = resolve(__dirname, "..", "..", "..");
const QA_TEST_JAVA_ROOT =
  process.env.QA_TEST_JAVA_ROOT ?? resolve(REPO_ROOT, "qa-test", "src", "test", "java");
const EXECUTION_SUMMARY_PATH =
  process.env.EXECUTION_SUMMARY_PATH ??
  resolve(REPO_ROOT, "qa-test", "target", "qa-reports", "execution-summary.txt");

const server = new McpServer({
  name: "qa-framework-mcp",
  version: "0.1.0",
});

server.registerTool(
  "get_test_inventory",
  {
    title: "Get Test Inventory",
    description:
      "Read-only scan of qa-test/src/test/java for TestNG @Test methods. Returns each test " +
      "class's fully-qualified name and its test methods with their TestNG groups. " +
      "Static source inspection only: does not execute tests, does not modify files, and " +
      "performs no AI interpretation of the results.",
    annotations: {
      readOnlyHint: true,
      destructiveHint: false,
      idempotentHint: true,
      openWorldHint: false,
    },
  },
  async () => {
    if (!existsSync(QA_TEST_JAVA_ROOT)) {
      throw new Error(`qa-test Java source directory not found: ${QA_TEST_JAVA_ROOT}`);
    }

    const classes = scanTestInventory(QA_TEST_JAVA_ROOT);
    const totalMethods = classes.reduce((sum, cls) => sum + cls.methods.length, 0);

    const result = {
      scannedRoot: QA_TEST_JAVA_ROOT,
      classCount: classes.length,
      methodCount: totalMethods,
      classes,
    };

    return {
      content: [
        {
          type: "text",
          text: JSON.stringify(result, null, 2),
        },
      ],
      structuredContent: result,
    };
  }
);

server.registerTool(
  "get_test_summary",
  {
    title: "Get Test Summary",
    description:
      "Read-only read of qa-test/target/qa-reports/execution-summary.txt, the plain-text " +
      "record ExecutionReportWriter appends during a qa-test run. Returns pass/fail/skip " +
      "counts and each test's name, status, screenshot path, and failure message exactly " +
      "as recorded. Does not run tests, does not modify the file, and performs no AI " +
      "interpretation or failure analysis - it is a structured passthrough of what the " +
      "file already contains. If the file does not exist (e.g. the suite has not been run " +
      "yet), returns an empty result with exists: false rather than an error.",
    annotations: {
      readOnlyHint: true,
      destructiveHint: false,
      idempotentHint: true,
      openWorldHint: false,
    },
  },
  async () => {
    const result = readExecutionSummary(EXECUTION_SUMMARY_PATH);

    return {
      content: [
        {
          type: "text",
          text: JSON.stringify(result, null, 2),
        },
      ],
      structuredContent: result,
    };
  }
);

const transport = new StdioServerTransport();
await server.connect(transport);
