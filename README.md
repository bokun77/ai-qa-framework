# AI QA Framework

An enterprise-style QA automation framework combining Selenium-based UI testing, REST API testing, and structured execution reporting, with a dedicated (currently unimplemented) layer for AI-assisted testing capabilities.

The framework is exercised end-to-end against [OWASP Juice Shop](https://owasp.org/www-project-juice-shop/) as its reference application under test.

## Modules

The project is a multi-module Maven build:

| Module | Purpose |
|---|---|
| `qa-core` | Shared abstractions and configuration (`TestConfig`) used by other modules. |
| `qa-web` | Selenium WebDriver setup, screenshot capture, and Page Objects for the app under test. |
| `qa-api` | Minimal REST Assured-based HTTP client for API test scenarios. |
| `qa-reporting` | Writes a plain-text execution summary (pass/fail/skip) for each test run. |
| `qa-ai` | Internal AI analysis module.<br>**Current capabilities:** reads execution-summary artifacts; deterministic failure classification; `FailureAnalysis` contract.<br>**Current scope:** no LLM integration yet; no automatic test modification; no MCP exposure. See [qa-ai/README.md](qa-ai/README.md) and [Roadmap](ROADMAP.md). |
| `qa-test` | Test execution layer: TestNG suite, UI and API test classes, and the execution listener that ties reporting into the test run. |

## Prerequisites

- Java 21
- Maven
- Google Chrome (for local, non-headless UI test runs)
- A running instance of [OWASP Juice Shop](https://github.com/juice-shop/juice-shop) (default: `http://localhost:3000`)

Run Juice Shop locally with Docker:

```bash
docker run -d -p 3000:3000 bkimminich/juice-shop
```

## Running the tests

```bash
mvn clean verify
```

This runs the suite defined in `qa-test/src/test/resources/testng.xml`, which includes both the API and UI test groups.

### Configuration

Target application and credentials can be overridden via system properties (see `TestConfig`):

```bash
mvn clean verify -DbaseUri=http://localhost:3000 -Dusername=admin@juice-sh.op -Dpassword=admin123
```

### Headless / CI execution

`WebDriverFactory` runs Chrome headless automatically when the `CI` environment variable is set to `true`, which is how the GitHub Actions workflow (`.github/workflows/build.yml`) runs it against a Juice Shop service container on every push and pull request to `main`.

## Current test coverage

**UI (`qa-web` Page Objects, exercised via `qa-test`):**
- Home page loads with the expected title
- Login with valid and invalid credentials
- Logout
- Product search (matching results, no results, empty query, special characters)
- Add to cart

**API (`qa-api` client, exercised via `qa-test`):**
- Products endpoint returns data
- Login endpoint accepts valid credentials and rejects invalid ones
- Product search endpoint (matching results, no results, empty query, special characters, and a regression check for a known Juice Shop SQL-injection response)

## Reporting

`ExecutionListener` (a TestNG listener) records a pass/fail/skip line per test via `ExecutionReportWriter` to `target/qa-reports/execution-summary.txt`, and captures a screenshot on UI test failures to `target/screenshots/`. Both are uploaded as CI build artifacts.

## Architecture and decisions

Key technology and architecture choices are recorded as ADRs in [`docs/adr`](docs/adr):

- [ADR-0001: Use Java 21 LTS](docs/adr/0001-use-java-21.md)
- [ADR-0002: Use Selenium for Browser Automation](docs/adr/0002-use-selenium.md)
- [ADR-0003: Use TestNG as Test Execution Framework](docs/adr/0003-use-testng.md)
- [ADR-0004: AI Architecture Strategy](docs/adr/0004-ai-architecture.md)
- [ADR-0005: AI Capability Sequencing](docs/adr/0005-ai-capability-sequencing.md)

See also [`docs/AI_STRATEGY.md`](docs/AI_STRATEGY.md) for how AI is used in developing this framework, and [`ROADMAP.md`](ROADMAP.md) for what's planned next.

## Contributing

See [`.github/CONTRIBUTING.md`](.github/CONTRIBUTING.md), [`.github/CODE_OF_CONDUCT.md`](.github/CODE_OF_CONDUCT.md), and [`.github/SECURITY.md`](.github/SECURITY.md).

## License

See [`LICENSE`](LICENSE).
