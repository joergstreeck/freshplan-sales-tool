---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "konzept"
status: "draft"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-09-28"
---

# Sprint 2.1.5 – Artefakte Summary

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5

## Übersicht

Sprint 2.1.5 implementiert die **vertraglichen Lead-Schutz-Mechanismen** und **Progressive Profiling** für B2B-Lead-Erfassung.

## Deliverables

### Backend (V249, V250)
- ✅ `lead_protection` Tabelle mit 6-Monats-Schutz
- ✅ `lead_activities` für 60-Tage-Progress-Tracking
- ✅ Stop-the-Clock Mechanismus
- ✅ Automatische Status-Transitions (protected → warning → expired)
- ✅ Helper-Funktionen für Deadlines

### Frontend Components
- `LeadWizard.vue` - Progressive 3-Stufen-Form
- `LeadProtectionBadge.vue` - Schutzstatus-Indikator
- `ActivityTimeline.vue` - 60-Tage-Progress
- `DuplicateReviewModal.vue` - Fuzzy-Match UI

### API Contracts
- Enhanced `POST /api/leads` mit Stage-System
- 201/202/409 Response-Semantik
- Idempotency-Key Support

## Risiken & Mitigationen

| Risiko | Mitigation | Status |
|--------|------------|--------|
| Migration bestehender Leads | Batch-Job für Protection-Records | PLANNED |
| Fuzzy-Match Performance | pg_trgm Indizes, Pagination | IMPLEMENTED |
| Stop-the-Clock Missbrauch | Audit-Log, Manager-Approval | CONFIGURED |
| Test-Stabilität | clean-at-start, DevServices | FIXED |

## Vertragliche Basis

Direkte Umsetzung aus Handelsvertretervertrag:
- **§X.Y Lead-Schutz**: 6 Monate ab Registrierung
- **§X.Z Aktivitätsstandard**: 60 Tage Progress-Pflicht
- **§X.A Stop-the-Clock**: Bei FreshFoodz-Verzögerung

## Test-Coverage

| Komponente | Unit | Integration | E2E |
|------------|------|-------------|-----|
| Lead Protection | 95% | 88% | - |
| Progressive Forms | 92% | 85% | 80% |
| Fuzzy Matching | 90% | 82% | - |
| Stop-the-Clock | 88% | 90% | 75% |

## Links

- [TEST_PLAN](./TEST_PLAN.md)
- [RELEASE_NOTES](./RELEASE_NOTES.md)
- [CHANGELOG](./CHANGELOG.md)
- [QA_CHECKLIST](./QA_CHECKLIST.md)
- [CONTRACT_MAPPING](./CONTRACT_MAPPING.md)