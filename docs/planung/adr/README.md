# Architecture Decision Records (ADRs)

## Index

| ADR | Titel | Status | Datum |
|-----|-------|--------|-------|
| [ADR-0001](./ADR-0001-cqrs-light.md) | CQRS Light statt Full-CQRS | ✅ ACCEPTED | 2025-09-21 |
| [ADR-0002](./ADR-0002-listen-notify-over-eventbus.md) | PostgreSQL LISTEN/NOTIFY statt Event-Bus | ✅ ACCEPTED | 2025-09-18 |
| [ADR-0003](./ADR-0003-settings-registry-hybrid.md) | Settings-Registry Hybrid JSONB + Registry | ✅ ACCEPTED | 2025-09-15 |
| [ADR-0004](./ADR-0004-territory-rls-vs-lead-ownership.md) | Territory = RLS-Datenraum, Lead-Protection = User-based | ✅ ACCEPTED | 2025-09-12 |
| [ADR-0005](./ADR-0005-nginx-oidc-gateway.md) | Nginx+OIDC Gateway statt Kong/Envoy | ✅ ACCEPTED | 2025-09-10 |
| [ADR-0006](./ADR-0006-mock-governance.md) | Mock-Governance (Business-Logic mock-frei) | ✅ ACCEPTED | 2025-09-23 |
| [ADR-0007](./ADR-0007-rls-connection-affinity.md) | RLS Connection Affinity Pattern | ✅ IMPLEMENTED | 2025-09-25 |

## Kontext

Diese ADRs dokumentieren wichtige Architekturentscheidungen für das FreshPlan Sales Tool.

> **🔎 Modul-Overlays:** Detaillierte Modul-Einstiege verweisen auf relevante ADRs.
> Beispiel Modul 02 Neukundengewinnung → Backend: [features-neu/02_neukundengewinnung/backend/_index.md](../features-neu/02_neukundengewinnung/backend/_index.md)

## ADR-Format Sie folgen dem Format:
- **Status:** PROPOSED / ACCEPTED / DEPRECATED / SUPERSEDED / IMPLEMENTED
- **Context:** Problemstellung
- **Decision:** Gewählte Lösung
- **Consequences:** Auswirkungen

## Navigation

- [Zurück zur Hauptdokumentation](../CRM_COMPLETE_MASTER_PLAN_V5.md)
- [Zur Roadmap](../PRODUCTION_ROADMAP_2025.md)