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
| 2.1.4  | IN_PROGRESS | Lead Deduplication & Data Quality (Phase 1)                   | V247 Migration, Normalization Service, Idempotency     |
| 2.1.5  | NEXT   | Matching & Review-Flow (Phase 2)                                  | `POST /api/leads:match`, Kandidaten-Modal, Audit       |
| 2.1.6  | PLAN   | Merge/Unmerge + Historie                                          | Zusammenführen, Rückgängig, Protokollierung            |

**Verweise:**
- Sprint-Trigger 2.1.3: `../../TRIGGER_SPRINT_2_1_3.md`
- Sprint-Map: `./SPRINT_MAP.md`
- Frontend-Doku 2.1.3: `./frontend/_index.md`
- Artefakte 2.1.3: `./artefakte/SPRINT_2_1_3/`