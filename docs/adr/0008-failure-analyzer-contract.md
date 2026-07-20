# ADR 0008: Failure Analyzer Contract

## Status

Accepted

## Context

ADR-0005 sequenced the Failure Analyzer as the first AI capability `qa-ai` will implement, gated on two conditions: `qa-reporting`'s execution artifacts being stable, and a follow-up ADR defining the Failure Analyzer's scope, inputs, and how `qa-ai` will consume `qa-reporting` output without breaking module isolation. ADR-0006 independently points at the same follow-up ADR as the prerequisite for any MCP capability that touches failure artifacts (screenshots, failure messages), so that MCP would consume the same contract rather than defining a competing one.

Since those two ADRs were written, `qa-reporting`'s execution artifact — `target/qa-reports/execution-summary.txt`, written by `ExecutionReportWriter` — has gained two independent readers: `qa-framework-mcp`'s `get_test_summary` (ADR-0006) reads it directly, and `get_test_inventory` (ADR-0007) reads a different source but now coexists with it under the same MCP server. The artifact's line format (`timestamp | status | testName [| screenshot=...] [| message=...]`) has not changed since ADR-0006, and has a dedicated parser (`mcp/qa-framework-mcp/src/summaryReader.js`) and its own test coverage (`ExecutionReportWriterTest`). `qa-ai` itself remains an empty, isolated placeholder module — no code, no dependencies.

No ADR yet defines what the Failure Analyzer reads, what it produces, or how it stays decoupled from the moment of test execution. Without that contract, an eventual implementation risks either reaching into `qa-reporting` as a compile-time dependency (recoupling AI analysis to the execution path ADR-0004 requires it stay independent from), or hooking into `ExecutionListener` directly (making analysis part of the test run itself rather than something that happens after it). This ADR is the follow-up both ADR-0005 and ADR-0006 call for.

This ADR is a planning decision only. It does not authorize implementation. No code is written, no module is created, and no existing module changes as a result of this ADR.

## Decision

This ADR defines, but does not implement, the contract for `qa-ai`'s Failure Analyzer: what it reads (Input Contract), what it produces (Output Contract), and the isolation rules that keep it decoupled from test execution and independent of MCP (qa-ai boundaries, MCP boundaries). The central design choice is that the contract is **artifact-based, not code-based**: the Failure Analyzer consumes `execution-summary.txt` by reading the file, the same way `get_test_summary` already does, rather than by depending on `qa-reporting` as a Maven module or hooking into TestNG listeners. This is what allows AI analysis to remain decoupled from test execution — it runs after a run completes, out of process, against a finished artifact.

## Input Contract

The Failure Analyzer's only input is the artifact `qa-reporting`'s `ExecutionReportWriter` already produces: `target/qa-reports/execution-summary.txt`. This ADR does not introduce any new artifact, field, or data source.

Format, as already implemented and reaffirmed here as the contract:

```
<timestamp> | <status> | <testName> [| screenshot=<path>] [| message=<failureMessage>]
```

- `timestamp` — `LocalDateTime.now()` at record time. No timezone; local to the execution host.
- `status` — one of `PASS`, `FAIL`, `SKIP`. The Failure Analyzer's scope is limited to records where `status == FAIL`; `PASS`/`SKIP` records share the file but carry no failure signal.
- `testName` — fully-qualified `<TestClass>.<methodName>`.
- `screenshot` — present only for UI test failures (`ExecutionListener` captures one only when the failing test instance is a `BaseUiTest` with a live driver). Absent for API test failures and for UI failures without a driver. The Failure Analyzer must treat this field as optional, never assumed.
- `message` — `Throwable#getMessage()` from the TestNG failure. May be `null`. Free text, not a structured error code.
- Field values are escaped per `ExecutionReportWriter#escape` (backslashes and `|` escaped, line breaks collapsed). The Failure Analyzer must unescape using the same rule `summaryReader.js` already implements — not a new parser.

Guarantees this contract does **not** make:

- **No history.** `ExecutionReportWriter` deletes the file at the start of each JVM run. Only the most recent execution is available; there is no run-over-run trend data. (This is consistent with ADR-0005 scoping Reporting Intelligence, which needs trend data, out separately from Failure Analyzer.)
- **No run identity.** There is no run ID, build number, or correlation key tying a given `execution-summary.txt` to a specific CI run beyond the file's own location and mtime.
- **No screenshot durability guarantee.** The path is recorded at write time; nothing guarantees the file still exists when the Failure Analyzer later reads it (e.g. CI artifact cleanup between the run and the analysis).

The Failure Analyzer consumes this artifact by reading the file directly. It does not gain a Maven dependency on `qa-reporting`, and it does not register as an `ITestListener` or otherwise hook into execution. It runs against a completed artifact, after execution, out of process — never during a test run.

## Output Contract

The Failure Analyzer produces one analysis record per input record where `status == FAIL`. This ADR fixes the shape and constraints of that output; it does not fix the model or heuristic that produces it, which is an implementation detail for the follow-up plan ADR-0005 already requires.

Each output record must contain, at minimum:

- `testName` — echoed verbatim from the input record, so output traces back to a specific input line unambiguously.
- `sourceTimestamp` — the input record's `timestamp`, echoed verbatim.
- `category` — a value from a closed, explicitly enumerated vocabulary (e.g. locator issue, assertion mismatch, environment/infrastructure issue, timeout, application defect, unknown/unclassified). The follow-up implementation plan must enumerate the exact set; this ADR only requires that the set be closed — the Failure Analyzer must not emit free-form categories.
- `explanation` — free text grounded only in the input record's own `message` and/or referenced `screenshot`, if present. It must not introduce claims that cannot be traced to those two fields.
- `evidence` — a reference back to the exact input used (the `message` text, the `screenshot` path, or both), so a human reviewer can verify the analysis against the source line without re-running anything.
- `confidence` — an explicit indicator (e.g. low/medium/high). Omitting confidence is not acceptable; the contract requires the analyzer to signal when it is uncertain.

Explicit exclusions — the output must **not**:

- Modify or auto-heal test code, locators, page objects, or configuration. (Locator self-healing is Locator Intelligence's future scope per ADR-0004, not the Failure Analyzer's.)
- Re-trigger, retry, or gate any test execution. The Failure Analyzer has no execution authority.
- Be written into `qa-reporting`'s own artifact directory (`target/qa-reports/`). Output belongs to `qa-ai` alone, preserving the one-way dependency direction (`qa-reporting` → `qa-ai`) already established by ADR-0005/0006; `qa-reporting` must never need to know `qa-ai` exists.
- Be treated as authoritative. Per ADR-0004's "human decisions remain part of the testing process," this output is advisory input to a human, not an automated verdict.

The exact output format and storage location (JSON file, structured log, etc.) is left to the follow-up implementation plan; this ADR fixes only the fields and constraints above.

## qa-ai boundaries

Reaffirming and extending ADR-0004/0005:

- `qa-ai` gains no Maven dependency on `qa-core`, `qa-web`, `qa-api`, `qa-reporting`, or `qa-test`. It consumes `execution-summary.txt` (and referenced screenshots) by file path — the same pattern `qa-framework-mcp` already uses — not by importing `qa-reporting` code.
- No existing module gains a dependency on `qa-ai`. This was already true under ADR-0004/0005 and is unchanged here.
- The Failure Analyzer executes only after a run completes. It is not registered as a TestNG listener, is not invoked from `qa-test`, `qa-core`, `qa-web`, or `qa-api`, and does not participate in the Maven reactor's test phase.
- The Failure Analyzer has no execution authority: it cannot start, stop, retry, or gate a test run, and no part of the execution path consumes its output automatically.

## MCP boundaries

Reaffirming ADR-0006:

- MCP must never depend on `qa-ai`. This ADR does not change that. The Failure Analyzer's output is not exposed through `get_test_summary` or `get_test_inventory`, and this ADR does not introduce a new MCP tool.
- A future MCP capability exposing Failure Analyzer output (e.g. a hypothetical `get_failure_analysis`) is out of scope here and requires its own follow-up ADR, per ADR-0006's future triggers. This ADR is the "reporting-artifact consumption contract" ADR-0006 said any such capability must consume — it authorizes nothing on its own.
- The dependency graph stays one-way with three legs, not two: `qa-reporting` produces artifacts; `qa-ai` and MCP each independently read them; neither `qa-ai` nor MCP depends on the other, and `qa-reporting` depends on neither.

## Security considerations

- `message` (`Throwable#getMessage()`) and screenshots are free-form and can carry stack traces, URLs with query parameters or tokens, form field values, session identifiers, or other incidental data from the application under test. This exposure already exists in `execution-summary.txt` and is already read by MCP; the Failure Analyzer changes the risk calculus by being the first capability that sends this data to an AI process for interpretation rather than structuring it for passthrough, which is exactly the "sensitive enterprise data must be protected" concern ADR-0004 flagged.
- Whether the Failure Analyzer runs a local model or calls a hosted API determines whether failure messages and screenshots ever leave the local environment. This ADR does not select a provider and introduces no new dependency; the follow-up implementation plan must state where the data is processed and what retention/logging policy applies before implementation begins.
- Because `explanation`/`evidence` in the output contract are required to quote the source `message`/`screenshot`, the output artifact inherits the same sensitivity as the input. It must be handled with at least the same care as `execution-summary.txt` and screenshots today — not committed to source control, not published anywhere the source artifact wouldn't be.
- Screenshots can capture more than the specific failure (other open panels, visible session state). This is an existing `qa-reporting`/`ScreenshotCapture` concern, not new to this ADR, but the Failure Analyzer amplifies it by being the first consumer that reasons over screenshot content instead of just recording its path.

## Consequences

Positive:

- Gives ADR-0005 and ADR-0006 the follow-up contract both were waiting on, without pre-committing to an implementation.
- Keeps AI analysis decoupled from test execution: the artifact-based input contract means the Failure Analyzer can be built, run, and iterated on without touching `ExecutionListener`, `qa-reporting`, or the Maven reactor.
- Gives MCP a stable boundary to design against if a future `get_failure_analysis`-style capability is proposed, instead of MCP and `qa-ai` independently guessing at each other's shape.

Trade-offs:

- The output contract deliberately leaves the `category` vocabulary, storage format, and AI provider unresolved — real implementation work still requires a separate plan before any code is written.
- Because the input contract is tied to `execution-summary.txt`'s current line format, any future change to that format (already flagged as a risk in ADR-0006/0007) requires revisiting this ADR too, now with three dependents instead of two.

## Future Triggers

Implementation of the Failure Analyzer may begin once:

1. This ADR is accepted (status moves from Proposed to Accepted), satisfying the follow-up-ADR condition ADR-0005 requires.
2. `qa-reporting`'s `execution-summary.txt` format is reconfirmed stable at implementation time — if it has changed since this ADR, this ADR must be revised first, the same way `summaryReader.js` would need updating.
3. A follow-up implementation plan resolves what this ADR deliberately leaves open: the closed `category` vocabulary, the output file format/location, and — per Security Considerations — which AI process performs the analysis and what data-handling policy governs it.

Any MCP capability exposing Failure Analyzer output remains gated separately, per ADR-0006, and requires its own follow-up ADR consuming this one.

Until all conditions above are met, `qa-ai` remains an empty, isolated module, unchanged by this ADR. No code is written, no dependency is added, and no existing module changes as a result of this ADR.
