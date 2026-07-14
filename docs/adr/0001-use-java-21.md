# ADR 0001: Use Java 21 LTS

## Status

Accepted

## Context

The framework requires a stable, modern, and enterprise-ready programming platform.

Java is one of the most widely adopted technologies in enterprise environments, with strong ecosystem support, long-term maintenance, and proven reliability.

The project needs a version of Java that provides modern language features while maintaining long-term support.

## Decision

We will use Java 21 LTS as the baseline version for ai-qa-framework.

Java 21 provides:

- Long-term support
- Modern language improvements
- Performance enhancements
- Improved JVM capabilities
- Strong enterprise adoption

All modules and examples will target Java 21.

## Consequences

Positive:

- Stable enterprise foundation
- Access to modern Java features
- Easier maintenance and upgrades
- Alignment with current enterprise standards

Trade-offs:

- Requires users to have Java 21 installed
- Older Java environments are not supported