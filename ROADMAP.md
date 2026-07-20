# Roadmap

This roadmap reflects the state of the framework as of v1.0.0 and the direction defined by the project's [ADRs](docs/adr).

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

## Next

Per [ADR-0005](docs/adr/0005-ai-capability-sequencing.md), the `qa-ai` module remains an empty, isolated placeholder until both of the following are true:

1. `qa-reporting`'s execution artifacts (execution summary, screenshots) are considered stable.
2. A follow-up ADR or implementation plan defines the **Failure Analyzer**'s scope, inputs, and how `qa-ai` will consume `qa-reporting` output without breaking module isolation.

The Failure Analyzer is the first of six planned AI capabilities ([ADR-0004](docs/adr/0004-ai-architecture.md)), chosen because it is the only one with an existing upstream data source. The rest are explicitly deferred and will each need their own prerequisite data source defined before they can be sequenced:

- Test Generator — needs requirements/user-story input not yet captured by the framework
- Locator Intelligence — needs locator usage and failure history not yet captured
- Test Data Generator — needs schema/data-model input not yet defined
- Documentation Assistant — needs a stable set of artifacts across multiple capabilities to summarize
- Reporting Intelligence — needs trend data across multiple executions, not just a single run

## MCP Integration (future)

Per [ADR-0006](docs/adr/0006-mcp-integration-strategy.md), MCP is planned as a separate development/tooling layer for exposing framework data to external AI clients, living outside the Maven reactor and independent of `qa-ai`. It is not yet implemented. The first planned capability is a read-only test summary reader, gated on a follow-up implementation plan.

## Beyond AI capability work

Longer-term directions noted in [ADR-0004](docs/adr/0004-ai-architecture.md)'s Future Evolution section, not yet scheduled:

- Local AI models
- Enterprise AI provider integrations
- Retrieval-Augmented Generation (RAG)
- AI agents for testing workflows
- Autonomous testing capabilities
