---
module: "02_neukundengewinnung"
doc_type: "roadmap"
owner: "team/leads"
updated: "2025-09-28"
---

# Roadmap – Modul 02 Neukundengewinnung

| Sprint | Status | Kurzbeschreibung                                                   | Hauptdeliverables                                      |
|-------:|:------:|--------------------------------------------------------------------|--------------------------------------------------------|
| 2.1    | DONE   | Backend Lead Management ohne Gebietsschutz                        | Territory-Scoping, CQRS Light, PR #103, #105, #110     |
| 2.1.1  | DONE   | P0 HOTFIX Integration Gaps                                        | Event Pipeline, Dashboard Widget, PR #111              |
| 2.1.2  | DONE   | Frontend Research                                                 | API Contract, Inventory, PR #112                       |
| 2.1.3  | DONE   | Thin Slice FE: Create+List, Validierung, i18n, RFC7807, MSW       | LeadList, LeadCreateDialog, Tests, CI grün, PR #122    |
| 2.1.4  | DONE   | Lead Deduplication & Data Quality (Phase 1)                       | V247/V248 Migration, Normalization, Idempotency, PR #123 |
| 2.1.5  | IN_PROGRESS | Lead Protection & Progressive Profiling (B2B)                 | 6-Monats-Schutz, 60-Tage-Standard, Stop-Clock, 3 Stages |
| 2.1.6  | PLAN   | Lead Transfer & Team Management + Matching                        | Transfer-Flow, Merge/Unmerge, Team-RBAC, Audit-Historie |

**Verweise:**
- Sprint-Map (aktuell): `./SPRINT_MAP.md`
- Sprint-Trigger 2.1.5: `../../TRIGGER_SPRINT_2_1_5.md`
- Artefakte 2.1.5: `./artefakte/SPRINT_2_1_5/`
- Delta-Log Scope-Änderung: `./artefakte/SPRINT_2_1_5/DELTA_LOG_2_1_5.md`
- OpenAPI Contract: `./analyse/api/leads.openapi.md`
- RBAC ADR: `./shared/adr/ADR-002-rbac-lead-protection.md`