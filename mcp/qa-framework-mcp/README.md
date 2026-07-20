# qa-framework-mcp

Node.js MCP server implementing [ADR-0006: MCP Integration Strategy](../../docs/adr/0006-mcp-integration-strategy.md)
and [ADR-0007: Test Inventory MCP](../../docs/adr/0007-test-inventory-mcp.md).

It exposes two tools:

- `get_test_inventory` — statically scans `qa-test/src/test/java` for TestNG `@Test`
  methods and returns their class name, method name, and TestNG groups.
- `get_test_summary` — reads `qa-test/target/qa-reports/execution-summary.txt` (written by
  `qa-reporting`'s `ExecutionReportWriter` during a test run) and returns pass/fail/skip
  counts plus each test's name, status, screenshot path, and failure message.

## Understanding the tools

`get_test_inventory` and `get_test_summary` answer different questions and read different
data sources. It is expected for their numbers to diverge — that is not a bug in either tool.

**`get_test_inventory`**

- Reads the `qa-test` source tree (`qa-test/src/test/java`).
- Shows what tests *exist*, regardless of whether or when they were last run.
- Example: 10 classes / 20 methods declared in source.

**`get_test_summary`**

- Reads `qa-test/target/qa-reports/execution-summary.txt`.
- Shows what ran in the *latest execution*, not the full inventory.
- A full run (e.g. `mvn verify`) may execute the whole suite, so its summary may contain
  both UI and API tests.
- A filtered run (e.g. `mvn test -Dgroups=api`) only executes tests in the `api` TestNG
  group, so its summary will contain only API tests — fewer than the 20 methods
  `get_test_inventory` reports.

This is expected behavior, not a discrepancy to reconcile: `qa-test`'s TestNG group/suite
filtering means the two tools are not supposed to always report the same counts. Neither
tool merges, cross-references, or infers anything from the other's output — see
[ADR-0007](../../docs/adr/0007-test-inventory-mcp.md) for the rationale.

## What this is not

Per [ADR-0006](../../docs/adr/0006-mcp-integration-strategy.md) and
[ADR-0007](../../docs/adr/0007-test-inventory-mcp.md):

- **Not part of the Maven reactor.** This directory is not declared in the root `pom.xml`
  `<modules>` list and is not built, tested, or packaged by `mvn verify` / CI.
- **Read-only.** `get_test_inventory` only reads `.java` files under `qa-test/src/test/java`;
  `get_test_summary` only reads `qa-test/target/qa-reports/execution-summary.txt`. Neither
  ever writes to, modifies, or deletes any file.
- **Does not execute tests.** Both tools parse existing text (source files or the summary
  file); neither invokes TestNG, Maven, or any test runner.
- **No dependency on `qa-ai`.** This server does not import from, call, or otherwise depend
  on the `qa-ai` module.
- **No AI interpretation.** Both tools return exactly what is already declared in the source
  or already recorded in the execution summary. Neither summarizes, ranks, recommends, or
  performs failure analysis — that remains the Failure Analyzer's job under ADR-0005, once
  its own follow-up ADR authorizes it.

No existing Java module (`qa-core`, `qa-web`, `qa-api`, `qa-reporting`, `qa-ai`, `qa-test`)
depends on this server, and this server does not modify any of them.

## Requirements

- Node.js 18+

## Install

```bash
cd mcp/qa-framework-mcp
npm install
```

## Run

The server communicates over stdio, which is how MCP clients (e.g. Claude Code, Claude
Desktop) normally launch it — you don't run it standalone in a terminal and expect visible
output.

```bash
npm start
```

By default it scans `<repo-root>/qa-test/src/test/java`, resolved relative to this
directory. To point it at a different Java source root (e.g. for testing), set
`QA_TEST_JAVA_ROOT`:

```bash
QA_TEST_JAVA_ROOT=/path/to/other/src/test/java npm start
```

## Configure in an MCP client

Example client configuration (adjust the path to your local checkout):

```json
{
  "mcpServers": {
    "qa-framework-mcp": {
      "command": "node",
      "args": ["/absolute/path/to/ai-qa-framework/mcp/qa-framework-mcp/src/index.js"]
    }
  }
}
```

## Tool: `get_test_inventory`

Takes no input arguments. Returns JSON shaped like:

```json
{
  "scannedRoot": "/absolute/path/to/qa-test/src/test/java",
  "classCount": 10,
  "methodCount": 20,
  "classes": [
    {
      "className": "com.aiqaframework.test.LoginTest",
      "sourceFile": "/absolute/path/to/qa-test/src/test/java/com/aiqaframework/test/LoginTest.java",
      "methods": [
        {
          "methodName": "validCredentialsLogInSuccessfully",
          "groups": ["ui", "smoke"]
        },
        {
          "methodName": "invalidCredentialsShowError",
          "groups": ["ui", "regression"]
        }
      ]
    }
  ]
}
```

Classes with no `@Test` methods (e.g. base classes, listeners) are not included.

## Tool: `get_test_summary`

Takes no input arguments. Reads
`<repo-root>/qa-test/target/qa-reports/execution-summary.txt` by default; override with the
`EXECUTION_SUMMARY_PATH` environment variable (e.g. for testing against a different file):

```bash
EXECUTION_SUMMARY_PATH=/path/to/execution-summary.txt npm start
```

That file is produced by running the `qa-test` suite (e.g. `mvn -pl qa-test test`), which
invokes `qa-reporting`'s `ExecutionReportWriter` via `ExecutionListener`. Returns JSON shaped
like:

```json
{
  "sourcePath": "/absolute/path/to/qa-test/target/qa-reports/execution-summary.txt",
  "exists": true,
  "totalTests": 10,
  "passed": 9,
  "failed": 1,
  "skipped": 0,
  "tests": [
    {
      "name": "com.aiqaframework.test.LoginTest.validCredentialsLogInSuccessfully",
      "status": "PASS",
      "screenshot": null,
      "message": null
    },
    {
      "name": "com.aiqaframework.test.ProductSearchTest.searchReturnsMatchingProducts",
      "status": "FAIL",
      "screenshot": "target/screenshots/ProductSearchTest.searchReturnsMatchingProducts.png",
      "message": "expected true but was false"
    }
  ]
}
```

If the summary file does not exist yet (the suite hasn't been run), the tool returns a clear
empty response instead of an error:

```json
{
  "sourcePath": "/absolute/path/to/qa-test/target/qa-reports/execution-summary.txt",
  "exists": false,
  "note": "execution-summary.txt not found. Run the qa-test suite to generate it.",
  "totalTests": 0,
  "passed": 0,
  "failed": 0,
  "skipped": 0,
  "tests": []
}
```

## Manual smoke test

You can exercise the server directly with the MCP SDK's stdio client without configuring a
full MCP client:

```bash
node -e '
import("@modelcontextprotocol/sdk/client/index.js").then(async ({ Client }) => {
  const { StdioClientTransport } = await import("@modelcontextprotocol/sdk/client/stdio.js");
  const transport = new StdioClientTransport({ command: "node", args: ["src/index.js"] });
  const client = new Client({ name: "smoke-test", version: "0.0.1" });
  await client.connect(transport);

  const inventory = await client.callTool({ name: "get_test_inventory", arguments: {} });
  console.log(inventory.content[0].text);

  const summary = await client.callTool({ name: "get_test_summary", arguments: {} });
  console.log(summary.content[0].text);

  await client.close();
});
'
```

To generate a real `execution-summary.txt` first, run the suite from the repo root, e.g.
`mvn -pl qa-test -Dgroups=api test` (API tests only, no browser required).

## Known limitations

- `get_test_inventory`'s scanner uses lightweight regex-based parsing, not a full Java
  parser. It assumes the codebase's existing style: one top-level class per file, `@Test`
  annotations with simple `groups = {"a", "b"}` or `groups = "a"` arguments, and a method
  declaration on the lines immediately following the annotation. Unusual formatting (e.g.
  `@Test` arguments containing nested parentheses) is not guaranteed to parse correctly.
- `get_test_summary`'s parser assumes `ExecutionReportWriter`'s current line format
  (`timestamp | STATUS | testName [| screenshot=...] [| message=...]`, with `\` and `|`
  escaped in field values). A change to that format in `qa-reporting` would require updating
  `src/summaryReader.js` to match.
