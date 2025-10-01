---
module: "02_neukundengewinnung"
domain: "shared"
sprint: "2.1.3"
doc_type: "deltalog"
status: "approved"
owner: "team/leads"
updated: "2025-09-28"
---

# Changelog – Sprint 2.1.3

- feat(frontend/leads): `LeadList` + `LeadCreateDialog`
- feat(i18n): Namespace `leads.*` (de/en)
- feat(msw): POST `/api/leads` inkl. 409-Pfad
- fix(dev): Backend-Ping entfernt, MSW-Start vereinfacht
- test: Unit/Integration (Validierung, 409, i18n)
- chore: Copilot-Findings adressiert (i18n-Konsistenz)
- style: Prettier angewandt; Lint-Issues behoben