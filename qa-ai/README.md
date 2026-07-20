# qa-ai

Failure Analyzer module implementing [ADR-0008: Failure Analyzer Contract](../docs/adr/0008-failure-analyzer-contract.md).

Isolated per [ADR-0004](../docs/adr/0004-ai-architecture.md) and [ADR-0005](../docs/adr/0005-ai-capability-sequencing.md): no dependency on `qa-core`, `qa-web`, `qa-api`, `qa-reporting`, `qa-test`, or MCP (`/mcp/qa-framework-mcp`) in either direction. See also the [qa-ai v0.1 implementation plan](../docs/architecture/qa-ai-v0.1-implementation-plan.md).

## Current capability (v0.2)

`qa-ai` reads `qa-reporting`'s execution artifact and classifies each failed test using `RuleBasedFailureAnalyzer` — a deterministic, keyword/regex-based classifier. No AI/LLM provider is involved; classification is fixed rules only, and the same input always produces the same output.

`FailureCategory` (the closed vocabulary from ADR-0008's Output Contract):

- `UI_SYNCHRONIZATION`
- `ASSERTION_FAILURE`
- `API_FAILURE`
- `AUTHENTICATION_FAILURE`
- `ENVIRONMENT_FAILURE`
- `UNKNOWN`

`RuleBasedFailureAnalyzer` checks a fixed, ordered set of rules against the failure message only. **The first matching rule wins** — this precedence order is intentional and tested (`RuleBasedFailureAnalyzerTest`):

1. `UI_SYNCHRONIZATION` — `ElementClickInterceptedException`, `cdk overlay`, `snackbar`
2. `ASSERTION_FAILURE` — `AssertionError`, or `expected` and `actual` both present
3. `API_FAILURE` — an HTTP 4xx/5xx status code, or `api request` / `api call` / `api failure` / `http error`
4. `AUTHENTICATION_FAILURE` — `login`, `auth`, `token`
5. `ENVIRONMENT_FAILURE` — `connection refused`, `timeout` / `timed out`, `connect exception`, `socket timeout`
6. `UNKNOWN` — none of the above matched

Confidence is deterministic, not probabilistic: `1.0` when a rule matches, `0.0` for `UNKNOWN`.

`UnclassifiedFailureAnalyzer` (from v0.1) remains available as a separate fallback implementation — it always returns `UNKNOWN`/`0.0` without attempting classification, distinct from `RuleBasedFailureAnalyzer`'s "analyzed, no rule matched" `UNKNOWN`.

## Input artifact

The only input is `qa-reporting`'s execution artifact — `target/qa-reports/execution-summary.txt`, written by `ExecutionReportWriter` during a `qa-test` run. `ExecutionSummaryReader` reads this file by path and returns only its `FAIL` records as `ExecutionRecord`s (`testName`, `status`, `timestamp`, `screenshot`, `message`), applying the same escaping rule `ExecutionReportWriter` uses. It does not depend on `qa-reporting` as a Maven module, does not hook into test execution, and returns an empty result (not an exception) if the file is missing.

## Output model

Each classified failure is a `FailureAnalysis`: `testName`, `sourceTimestamp` (both echoed verbatim from the source record), `category` (`FailureCategory`), `explanation`, `evidence` (the source message/screenshot the classification is grounded in), and `confidence`. Output is advisory only — nothing in the execution path consumes it automatically.

## Current limitations

- **No LLM / AI provider.** Classification is fixed, deterministic rules; anything the rules don't recognize is returned `UNKNOWN`. No hosted or local model is called.
- **No automatic fixes.** `qa-ai` never modifies locators, page objects, selectors, or configuration.
- **No test modification.** `qa-ai` never edits, generates, retries, or re-triggers tests — it only reads a completed execution artifact after the run has finished.
- **No MCP exposure.** Failure Analyzer output is not available through `qa-framework-mcp`'s `get_test_summary` or `get_test_inventory` tools, and `qa-ai` has no dependency on MCP in either direction. Exposing this output via MCP would require its own future ADR, per ADR-0006/ADR-0008.

## Package structure

```
com.aiqaframework.ai.failure
├── model     — ExecutionRecord, FailureAnalysis, FailureCategory
├── io        — ExecutionSummaryReader
└── analysis  — FailureAnalyzer (interface), RuleBasedFailureAnalyzer,
                UnclassifiedFailureAnalyzer, FailureEvidence (internal helper)
```

## Running tests

```bash
mvn -pl qa-ai clean verify
```

## Related documents

- [ADR-0004: AI Architecture Strategy](../docs/adr/0004-ai-architecture.md)
- [ADR-0005: AI Capability Sequencing](../docs/adr/0005-ai-capability-sequencing.md)
- [ADR-0006: MCP Integration Strategy](../docs/adr/0006-mcp-integration-strategy.md)
- [ADR-0008: Failure Analyzer Contract](../docs/adr/0008-failure-analyzer-contract.md)
- [qa-ai v0.1 implementation plan](../docs/architecture/qa-ai-v0.1-implementation-plan.md)
- [Roadmap](../ROADMAP.md)
