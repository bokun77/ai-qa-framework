# qa-ai v0.1 Implementation Plan

Implements the input/output contract defined in [ADR-0008: Failure Analyzer Contract](../adr/0008-failure-analyzer-contract.md), now Accepted.

This is a planning document, not an authorization to write AI-provider integration code. v0.1 deliberately stops short of "analysis" — it wires up the contract end-to-end with a stub that performs no interpretation, so the plumbing (read artifact → build output shape) can be built, tested, and reviewed independently of picking a model or provider.

## Requirements this plan satisfies

- `qa-ai` remains isolated: no dependency on `qa-core`, `qa-web`, `qa-api`, `qa-reporting`, or `qa-test`; no existing module gains a dependency on `qa-ai`.
- Reads `execution-summary.txt` only — no other input source.
- No dependency on MCP (`/mcp/qa-framework-mcp`), in either direction.
- No modification to `qa-reporting` — its code, its artifact format, or its output directory.
- No AI provider integration — local or hosted. v0.1 ships a stub analyzer that always returns `UNKNOWN`/unclassified.

## Proposed package structure

Under the existing `qa-ai` module (`qa-ai/src/main/java/com/aiqaframework/ai/`), which today contains only a `package-info.java`:

```
com.aiqaframework.ai
├── package-info.java                  (existing)
└── failure
    ├── package-info.java
    ├── model
    │   ├── ExecutionRecord.java        — one parsed FAIL line: testName, timestamp, screenshot, message
    │   ├── FailureAnalysis.java        — ADR-0008 Output Contract shape: testName, sourceTimestamp,
    │   │                                  category, explanation, evidence, confidence
    │   └── FailureCategory.java        — closed enum from ADR-0008 (LOCATOR_ISSUE, ASSERTION_MISMATCH,
    │                                      ENVIRONMENT_ISSUE, TIMEOUT, APPLICATION_DEFECT, UNKNOWN).
    │                                      v0.1 only ever produces UNKNOWN; the other constants exist so
    │                                      the type is stable when a real analyzer is added later.
    ├── io
    │   └── ExecutionSummaryReader.java — reads target/qa-reports/execution-summary.txt, parses it per
    │                                      ADR-0008's Input Contract, filters to status == FAIL, returns
    │                                      List<ExecutionRecord>. Missing-file case returns an empty list,
    │                                      not an exception (mirrors summaryReader.js's exists:false handling).
    └── analysis
        ├── FailureAnalyzer.java        — interface: List<ExecutionRecord> -> List<FailureAnalysis>
        └── UnclassifiedFailureAnalyzer.java
                                         — v0.1's only implementation. For every input record, returns
                                           category=UNKNOWN, confidence=NONE, explanation="Not analyzed —
                                           no AI provider integrated (qa-ai v0.1)", evidence=the record's
                                           own message/screenshot echoed back unchanged. No interpretation,
                                           no heuristics — a structural passthrough, same spirit as MCP's
                                           get_test_summary/get_test_inventory.
```

`failure` is a sub-package, not the whole module, so `com.aiqaframework.ai` can hold sibling packages for the other five ADR-0004 capabilities later without restructuring this one.

`ExecutionSummaryReader` implements its own copy of `ExecutionReportWriter`'s escape/unescape rule (backslash- and pipe-escaping). It cannot reuse `qa-reporting`'s Java code without creating the dependency ADR-0008 forbids, and it cannot reuse `qa-framework-mcp`'s `summaryReader.js` since that's JavaScript in a separate runtime. The parsing *rule* is shared by contract (all three implementations — writer, MCP reader, this reader — must agree on the same format); the *code* is independently implemented in each, which is already the existing pattern between `ExecutionReportWriter` and `summaryReader.js`.

## Dependency decision

`qa-ai/pom.xml` gains no dependency on any other module in the reactor. Concretely:

| Dependency | Add? | Reasoning |
|---|---|---|
| `qa-core` | No | `ExecutionSummaryReader` is self-contained file I/O; nothing in `qa-core` is needed, and staying dependency-free keeps `qa-ai` maximally isolated (`architecture.md`: "qa-ai remains independent unless integration is explicitly required" — no requirement exists yet). |
| `qa-reporting` | No | Forbidden by ADR-0008's Input Contract; the artifact is read by path, not by API. |
| `org.testng:testng` (test scope) | Yes | Already declared in the root `dependencyManagement`; `qa-reporting` already uses it the same way (test-scope only). Reusing an already-managed version is not a new dependency to the project, just a new module using an existing one. |
| `org.slf4j:slf4j-api` / `logback-classic` | Yes | Already root-managed and already used by `qa-reporting`'s `ExecutionReportWriter` for warning on I/O failure. `ExecutionSummaryReader` needs the same class of warning (e.g. malformed line, unreadable file) — reusing the existing logging stack is more consistent than inventing a different one. |
| AI/LLM provider client (any) | No | Explicitly out of scope for v0.1 per the requirements above and ADR-0008's Security Considerations, which require the provider/data-handling decision to be made deliberately, not defaulted into by an early dependency choice. |
| JSON library (e.g. Jackson) | No | See output format decision below — v0.1's stub output uses the same plain-text, pipe-delimited convention `ExecutionReportWriter` already uses, so no serialization library is needed yet. |

Net effect: `qa-ai`'s `pom.xml` goes from zero dependencies to two test/runtime dependencies, both already present in the root `dependencyManagement` and already used elsewhere in the reactor. No new artifact is introduced to the project as a whole.

**Output location and format (resolving the item ADR-0008 left open, for v0.1 only):** `UnclassifiedFailureAnalyzer`'s output is written to `qa-ai/target/qa-ai-reports/failure-analysis.txt`, under `qa-ai`'s own `target/`, never under `qa-reporting`'s or `qa-test`'s. Format mirrors `execution-summary.txt`'s pipe-delimited convention rather than introducing JSON, so v0.1 needs no new library. This is a v0.1-scoped decision, not a final one — moving to structured JSON is expected once a real provider is integrated and/or an MCP-exposure ADR needs machine-readable output, and can be revisited without touching the Output Contract fields themselves.

## First milestone scope (v0.1)

**In scope:**

1. `qa-ai` package skeleton above, with `package-info.java` files following the existing style.
2. `ExecutionSummaryReader` — parses `execution-summary.txt` per ADR-0008's Input Contract, filtered to `FAIL` records only. Handles the missing-file case (empty list, not an exception) and the escaping rule.
3. `ExecutionRecord`, `FailureAnalysis`, `FailureCategory` — the data shapes fixed by ADR-0008's Input/Output Contracts.
4. `UnclassifiedFailureAnalyzer` — the only `FailureAnalyzer` implementation. Always returns `UNKNOWN`/`NONE`-confidence, with an explanation stating plainly that no analysis was performed. Proves the Output Contract's shape and the "advisory only, human review required" framing end-to-end without any AI reasoning.
5. A minimal way to run the above manually against a real `execution-summary.txt` (e.g. a small runner class with a `main` method, or a TestNG test that exercises the read → analyze → write path) — enough to verify the wiring, not a CLI or automation hook.
6. Unit tests (TestNG, matching `ExecutionReportWriterTest`'s style) covering: valid FAIL/PASS/SKIP line parsing, the escaping edge cases already covered on the writer side, missing-file handling, and the stub analyzer's fixed output shape.

**Explicitly out of scope for v0.1:**

- Any AI/LLM provider integration, local or hosted — deferred to a later milestone once ADR-0008's Security Considerations (data handling, retention, provider choice) are resolved.
- Real failure classification or heuristics of any kind — every output record is `UNKNOWN` in v0.1, by design.
- Reading or interpreting screenshot *contents* — the screenshot path is passed through as evidence, unopened.
- Any MCP exposure of Failure Analyzer output — out of scope per ADR-0006/ADR-0008 and gated on its own future ADR.
- Automatic invocation as part of `mvn verify`'s test phase or any TestNG listener — `UnclassifiedFailureAnalyzer` is invoked manually/on demand only, never wired into test execution.
- Finalizing the output format/location beyond v0.1 — the plain-text choice above is scoped to this milestone.

**Definition of done for v0.1:** given a real `execution-summary.txt` with at least one `FAIL` record, running the v0.1 wiring produces a `failure-analysis.txt` under `qa-ai/target/qa-ai-reports/` containing one unclassified record per failure, with `qa-ai`'s `pom.xml` unchanged in dependency direction (no new inter-module dependency) and no file outside `qa-ai/` touched.
