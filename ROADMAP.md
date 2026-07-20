# Roadmap

This roadmap reflects the state of the framework as of v1.3.0 and the direction defined by the project's [ADRs](docs/adr).

## Done (v1.0.0)

- Multi-module Maven architecture (`qa-core`, `qa-web`, `qa-api`, `qa-reporting`, `qa-ai`, `qa-test`) — [ADR-0001](docs/adr/0001-use-java-21.md)
- Selenium WebDriver integration with a CI-compatible headless Chrome setup — [ADR-0002](docs/adr/0002-use-selenium.md)
- TestNG-based test execution with grouped `ui`/`api` suites and a shared execution listener — [ADR-0003](docs/adr/0003-use-testng.md)
- Page Object Model for the Juice Shop home, login, and product search pages
- REST Assured-based API client and coverage for the products, login, and product search endpoints, including edge cases and a documented SQL-injection regression check
- UI coverage for home page load, login, logout, product search, and add-to-cart
- Plain-text execution summary and failure screenshot capture (`qa-reporting`, `qa-web`)
- GitHub Actions CI running the full suite against a Juice Shop service container on every push/PR to `main`
- AI architecture strategy and capability sequencing defined — [ADR-0004](docs/adr/0004-ai-architecture.md), [ADR-0005](docs/adr/0005-ai-capability-sequencing.md)

## Done (v1.1.x)

- Product Details flow
- Product Details API coverage
- Stable Page Object expansion

## Done (v1.2.x)

- Registration flow
- Registration UI test
- Registration API flow
- `ApiClient` POST support

## Done (v1.3.0)

- Basket flow
- Basket UI verification
- Complete user/product journey coverage (registration through basket)

## MCP Integration

Per [ADR-0006](docs/adr/0006-mcp-integration-strategy.md), MCP (`mcp/qa-framework-mcp`) is a separate, read-only development/tooling layer for exposing framework data to external AI clients, living outside the Maven reactor and independent of `qa-ai`. Both planned capabilities are implemented:

- `get_test_summary` — reads `qa-reporting`'s execution artifacts (test name, status, timestamp, screenshot path, failure message).
- `get_test_inventory` — reads `qa-test`'s static test structure (test classes, methods, TestNG groups) — [ADR-0007](docs/adr/0007-test-inventory-mcp.md).

Neither capability performs AI interpretation; both are raw passthroughs of existing data.

## Next: AI Assisted Quality Engineering

With test coverage expansion complete through v1.3.0 and `qa-reporting`'s execution artifacts stable, the next phase turns to the `qa-ai` module, which per [ADR-0005](docs/adr/0005-ai-capability-sequencing.md) has remained an empty, isolated placeholder until this point.

Planned milestones:

- **ADR-0008: Failure Analyzer Contract** — defines the input/output contract for the Failure Analyzer and the boundaries between `qa-ai` and MCP. Planning only; no implementation.
- **Failure Analyzer v1** — consumes `execution-summary` artifacts, analyzes failed tests, classifies failures, and provides recommendations.

Future possibilities (not yet sequenced):

- Flaky test detection
- Trend analysis
- Intelligent test suggestions

Constraints carried forward from [ADR-0006](docs/adr/0006-mcp-integration-strategy.md):

- MCP must remain separate from `qa-ai`.
- `qa-ai` must not modify tests automatically.
- No speculative AI features are built before their contracts exist.

The Failure Analyzer remains the first of six planned AI capabilities ([ADR-0004](docs/adr/0004-ai-architecture.md)). The rest stay deferred, each needing its own prerequisite data source before it can be sequenced:

- Test Generator — needs requirements/user-story input not yet captured by the framework
- Locator Intelligence — needs locator usage and failure history not yet captured
- Test Data Generator — needs schema/data-model input not yet defined
- Documentation Assistant — needs a stable set of artifacts across multiple capabilities to summarize
- Reporting Intelligence — needs trend data across multiple executions, not just a single run

## Beyond AI capability work

Longer-term directions noted in [ADR-0004](docs/adr/0004-ai-architecture.md)'s Future Evolution section, not yet scheduled:

- Local AI models
- Enterprise AI provider integrations
- Retrieval-Augmented Generation (RAG)
- AI agents for testing workflows
- Autonomous testing capabilities
