# ADR 0007: Test Inventory MCP

## Status

Accepted

## Context

ADR-0006 established MCP as a separate development/tooling layer at `/mcp/qa-framework-mcp`, outside the Maven reactor, read-only, and independent of `qa-ai`. Its first capability, `get_test_summary`, exposes `qa-reporting`'s execution artifacts (test name, status, timestamp, screenshot path, failure message) to external AI clients without any AI interpretation.

Execution results are not the only framework data an external AI client can usefully read. `qa-test` now defines TestNG groups and suite filtering (most recently in the commit adding groups and suite filtering support), and the module contains a growing set of test classes and methods across UI and API suites. There is currently no way for an external AI client to discover what tests exist, what they are named, or which TestNG groups they belong to, without reading source files directly. `get_test_summary` cannot answer this: it reports on past execution results, not on the static inventory of tests defined in the codebase.

A second MCP capability is needed to expose this static test inventory. As with `get_test_summary`, this must not become a channel for AI reasoning about the tests, and must not compromise any of the boundaries ADR-0006 already established for MCP.

This ADR is a planning decision only. It does not authorize implementation. No code is written, no module is created, and no existing module changes as a result of this ADR.

## Decision

### Second capability: `get_test_inventory`

A future MCP capability, `get_test_inventory`, will expose the static inventory of tests defined in `qa-test`:

- Reads test classes and methods from `qa-test`, along with the TestNG groups each test belongs to (as established by the groups/suite-filtering support already in `qa-test`).
- Returns raw, structured information reflecting what is already declared in the codebase: test class name, test method name, and TestNG group membership.
- Performs **no AI interpretation**: no suggestions on what tests to add, no gap analysis, no prioritization, no summarization beyond structuring the existing data. It is a passthrough, not an analysis capability.

This is the first capability exposing static test structure, as distinct from `get_test_summary`'s execution-result data. The two capabilities read different data sources and remain independent of one another.

### Reaffirmed constraints (from ADR-0006)

All rules established in ADR-0006 for MCP apply unchanged to this capability:

- **MCP remains outside the Maven reactor.** `get_test_inventory` does not change MCP's location (`/mcp/qa-framework-mcp`) or add it to the root aggregator's `<modules>` list.
- **MCP is read-only.** `get_test_inventory` only reads test class, method, and group declarations from `qa-test`. It never writes to or mutates test code, page objects, locators, configuration, or any other framework artifact.
- **MCP must not depend on `qa-ai`.** In-framework AI (`qa-ai`) and external AI tooling (MCP) remain independent tracks.
- **MCP must not execute or modify tests.** Reading test inventory is a static, source-level read. `get_test_inventory` must not invoke TestNG, run any suite, or otherwise trigger test execution to derive its output.
- **No existing QA module depends on MCP.** `qa-core`, `qa-web`, `qa-api`, `qa-reporting`, `qa-ai`, and `qa-test` must not reference, import, or require anything from `/mcp/qa-framework-mcp`.

### Scope of the first capability

`get_test_inventory`'s first version exposes exactly three things, and nothing else:

1. Test classes (fully qualified names).
2. Test methods within those classes.
3. TestNG groups each test method belongs to.

No AI interpretation or recommendation logic is included. It does not infer intent, suggest missing coverage, rank tests, or generate documentation about them. Any such capability would be a distinct, future decision — not an extension of `get_test_inventory`.

## Consequences

Positive:

- Gives external AI clients visibility into what tests exist and how they are grouped, without granting execution or write access.
- Reuses `qa-test`'s existing TestNG group/suite-filtering support instead of introducing a new tagging or metadata system.
- Keeps MCP's two capabilities cleanly separated by data source: `get_test_summary` reads execution results from `qa-reporting`, `get_test_inventory` reads static structure from `qa-test`. Neither depends on the other.
- Extends MCP without touching the Maven reactor, `qa-ai`'s isolation boundary, or any existing module's dependencies.

Trade-offs:

- `get_test_inventory` depends on how test classes, methods, and groups are declared in `qa-test`; changes to that structure (e.g. a different grouping mechanism) may require revisiting this ADR.
- Two independent MCP capabilities reading two different data sources (`qa-reporting`, `qa-test`) increases the surface area MCP needs to keep read-only and non-interpretive as it grows; this requires the same ongoing discipline ADR-0006 already flagged for the `qa-ai`/MCP boundary.
- Like `get_test_summary`, `get_test_inventory` is deliberately limited to raw passthrough data; it provides no analysis of coverage, redundancy, or test quality.

## Notes: `get_test_inventory` vs. `get_test_summary`

Now that both capabilities are implemented, it is worth restating explicitly how they relate, since their outputs can otherwise look like they disagree:

- `get_test_inventory` describes available test definitions — every `@Test` method declared in `qa-test`'s source tree, regardless of whether or when it was last run.
- `get_test_summary` describes the latest execution artifact (`execution-summary.txt`) — only the tests that were actually run in the most recent `qa-test` execution.
- These two capabilities are intentionally independent. They read different data sources (`qa-test` source vs. `qa-reporting`'s output) and neither is derived from or validated against the other.
- A summary can legitimately contain fewer tests than the inventory. `qa-test`'s TestNG group/suite-filtering support means a run can be scoped to a subset of tests (e.g. `-Dgroups=api`), and the summary will only reflect that subset.
- MCP does not merge, cross-reference, or infer anything from the difference between the two. It does not flag tests present in inventory but absent from the summary as "not run," "missing," or "failing" — that would be interpretation, which both tools are explicitly scoped to avoid.
- MCP remains read-only across both capabilities: this note describes how their outputs relate, not a new merging or diffing feature.

## Future Triggers

Implementation of `get_test_inventory` may begin once:

1. A follow-up implementation plan defines the concrete shape of its output (how test classes, methods, and groups are enumerated and serialized) and how the MCP server reads this from `qa-test` without adding MCP to the Maven reactor or introducing a dependency from `qa-test` on MCP.

Any capability that goes beyond raw inventory (e.g. coverage analysis, gap detection, or any AI interpretation of the test set) is out of scope for this ADR and would require its own follow-up ADR.

Until implementation begins, this ADR records intent and constraints only. No changes are made to `/mcp/qa-framework-mcp` or to any existing module as a result of this ADR.
