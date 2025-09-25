# Architecture Decision Records (ADR) Index

This directory contains all Architecture Decision Records for the FreshPlan Sales Tool.

## What is an ADR?

An Architecture Decision Record captures an important architectural decision made along with its context and consequences.

## ADR List

| ADR | Title | Status | Date |
|-----|-------|--------|------|
| [ADR-0001](ADR-0001-cqrs-light.md) | CQRS Light statt Full-CQRS | Accepted | 2025-09-21 |
| [ADR-0002](ADR-0002-listen-notify-over-eventbus.md) | PostgreSQL LISTEN/NOTIFY statt Event-Bus | Accepted | 2025-09-18 |
| [ADR-0003](ADR-0003-settings-registry-hybrid.md) | Settings-Registry Hybrid JSONB + Registry | Accepted | 2025-09-15 |
| [ADR-0004](ADR-0004-territory-rls-vs-lead-ownership.md) | Territory = RLS-Datenraum, Lead-Protection = User-based | Accepted | 2025-09-12 |
| [ADR-0005](ADR-0005-nginx-oidc-gateway.md) | Nginx+OIDC Gateway statt Kong/Envoy | Accepted | 2025-09-10 |
| [ADR-0006](ADR-0006-mock-governance.md) | Mock-Governance (Business-Logic mock-frei) | Accepted | 2025-09-23 |
| [ADR-0007](ADR-0007-rls-connection-affinity.md) | RLS Connection Affinity via CDI Interceptor | Accepted | 2025-09-25 |

## Categories

### Architecture
- ADR-0001: CQRS Light Architecture
- ADR-0002: Event System Design
- ADR-0005: API Gateway Pattern

### Security
- ADR-0004: Territory and RLS Design
- ADR-0007: RLS Connection Affinity

### Infrastructure
- ADR-0003: Settings Storage Pattern
- ADR-0006: Testing Strategy

## Status Definitions

- **Draft**: Decision under discussion
- **Proposed**: Decision proposed for review
- **Accepted**: Decision approved and implemented
- **Deprecated**: Decision no longer valid
- **Superseded**: Replaced by another ADR

## Template

When creating a new ADR, use this template:

```markdown
# ADR-XXXX: [Title]

**Status:** [Draft|Proposed|Accepted|Deprecated|Superseded]
**Date:** YYYY-MM-DD

## Context
[What is the issue we're facing?]

## Decision
[What have we decided to do?]

## Consequences
### Positive
[Good outcomes]

### Negative
[Trade-offs and risks]

## Alternatives Considered
[Other options we evaluated]

## References
[Links to relevant documentation]
```