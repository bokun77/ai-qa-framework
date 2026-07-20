# ADR 0006: MCP Integration Strategy

## Status

Accepted

## Context

ADR-0004 established a dedicated, isolated AI layer (`qa-ai`) for in-framework AI capabilities, and ADR-0005 sequenced the first of those capabilities (Failure Analyzer), gating its implementation on `qa-reporting`'s execution artifacts being stable and on a follow-up ADR defining the consumption contract. `qa-ai` remains an empty, isolated placeholder module.

There is a separate need: a way for external AI clients (e.g. an AI coding assistant working against this repository) to read framework data — starting with test execution results — during development. This is not the same problem as `qa-ai`:

- `qa-ai` performs AI reasoning *inside* the framework and feeds results back into automation.
- MCP exposes framework data *outward* to external AI clients. It does not reason about the data, and nothing it does feeds back into test execution.

`qa-reporting` already produces execution artifacts (the plain-text execution summary and failure screenshots via `ExecutionReportWriter`) and is the natural data source for this. Placing MCP inside `qa-ai`, or letting it participate in the Maven reactor alongside the automation modules, would blur that boundary and risk breaking the isolation rules already established for `qa-ai` and for runtime automation. A decision is needed on where MCP lives, what it depends on, and what its first capability is, before any implementation begins.

This ADR is a planning decision only. It does not authorize implementation. No code is written, no module is created, and no existing module changes as a result of this ADR.

## Decision

### MCP is a separate development/tooling layer

MCP support is a development- and AI-client-facing tooling layer, not a test-execution component and not an in-framework AI capability. It exists to expose already-produced framework data to external AI clients, nothing more.

### Location

MCP will live at `/mcp/qa-framework-mcp`, outside the Maven module tree.

- It is **not part of the Maven reactor**. It is not declared in the root aggregator's `<modules>` list and is not built, tested, or packaged by `mvn verify`/CI as part of the framework build.
- It is not placed inside `qa-ai`: `qa-ai` is reserved for in-framework AI capabilities under ADR-0004/0005 and must remain empty until its own trigger condition is met. MCP is a different concern and must not be used to backfill that module.

### Dependency rules

- **No existing QA module depends on MCP.** `qa-core`, `qa-web`, `qa-api`, `qa-reporting`, `qa-ai`, and `qa-test` must not reference, import, or require anything from `/mcp/qa-framework-mcp`. This reaffirms the existing rule that runtime automation must not depend on AI tooling.
- **MCP never depends on `qa-ai`.** The two AI-adjacent tracks — in-framework AI (`qa-ai`) and external AI tooling (MCP) — remain independent, even once `qa-ai` has real implementation.
- **MCP is read-only.** It only reads `qa-reporting`'s execution artifacts. It never writes to or mutates test code, page objects, locators, configuration, or the artifacts themselves.

The dependency direction is one-way: MCP reads what `qa-reporting` produces; nothing in the framework reads or depends on MCP.

### Initial capability: `get_test_summary`

The first and only MCP capability is `get_test_summary`:

- Reads `qa-reporting`'s existing execution artifacts (the execution summary produced by `ExecutionReportWriter`).
- Returns raw, structured information reflecting what is already in that artifact — test name, status, timestamp, screenshot path, failure message where present.
- Performs **no AI interpretation**: no root-cause analysis, no summarization beyond structuring the existing data, no scoring or recommendation. It is a passthrough, not an analysis capability.

A failure-artifact-analysis capability is explicitly out of scope for this ADR. That is the Failure Analyzer's job under ADR-0005, gated on its own follow-up ADR defining the reporting-artifact consumption contract. `get_test_summary` must not become a substitute for that contract.

### Relationship between the pieces

- `qa-reporting` produces execution artifacts.
- MCP (`qa-framework-mcp`) exposes those artifacts, read-only and uninterpreted, to external AI clients.
- `qa-ai` remains reserved, empty, and isolated for future in-framework AI capabilities, starting with the Failure Analyzer per ADR-0005.

## Consequences

Positive:

- Gives external AI clients a defined, low-risk entry point into the framework's execution data without touching the runtime automation path or the `qa-ai` isolation boundary.
- Keeps `qa-ai` and MCP on clearly separate tracks, each governed by its own ADR and trigger conditions.
- Reuses an already-stable artifact (the execution summary) instead of introducing new data-generation work.
- Keeping MCP outside the Maven reactor means the framework's build, test, and CI behavior is entirely unaffected by whether MCP exists, is under active development, or is broken.

Trade-offs:

- `get_test_summary` is deliberately limited in value — a raw passthrough — until an eventual Failure Analyzer contract exists; MCP's usefulness will grow only as fast as `qa-reporting`'s artifacts do.
- Living outside the Maven reactor means MCP needs its own build/dependency setup, separate from the framework's existing tooling.
- Maintaining two independent AI-adjacent tracks (`qa-ai`, MCP) requires ongoing discipline to keep the boundary from eroding — e.g. resisting the urge to have MCP call into a future Failure Analyzer directly instead of through a defined contract.

## Future Triggers

Implementation of `qa-framework-mcp` (starting with `get_test_summary`) may begin once:

1. A follow-up implementation plan defines the concrete shape of `get_test_summary`'s output and how the MCP server reads `qa-reporting`'s execution summary, without adding MCP to the Maven reactor.

Any additional MCP capability that touches failure artifacts (screenshots, failure messages) may be considered only after:

1. ADR-0005's Failure Analyzer trigger conditions are met, and
2. The follow-up ADR/plan required by ADR-0005 defines the reporting-artifact consumption contract — so MCP consumes the same contract rather than defining a competing one.

Until implementation begins, this ADR records intent and constraints only. No `/mcp/qa-framework-mcp` code exists, and no existing module is changed.
