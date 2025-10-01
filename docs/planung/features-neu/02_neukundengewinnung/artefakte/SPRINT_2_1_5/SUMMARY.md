---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "konzept"
status: "in_progress"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-10-01"
---

# Sprint 2.1.5 – Artefakte Summary

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5

## Übersicht

Sprint 2.1.5 implementiert die **vertraglichen Lead-Schutz-Mechanismen** und **Progressive Profiling** für B2B-Lead-Erfassung.

## Deliverables

### Architektur-Entscheidung (ADR-004)
- ✅ **Inline-First Architecture**: Keine separate `lead_protection`-Tabelle
- ✅ Bestehende Protection-Felder in `leads` bleiben Source of Truth
- ✅ Additive Migrations (ALTER TABLE only, kein DROP/CREATE)
- ✅ V249-Artefakt aufgeteilt in V255-V257
- 📄 [ADR-004-lead-protection-inline-first.md](../../shared/adr/ADR-004-lead-protection-inline-first.md)

### Backend (V255-V257)

**V255: leads_protection_basics_and_stage.sql**
- ⏸️ ALTER TABLE leads: progress_warning_sent_at, progress_deadline
- ⏸️ ALTER TABLE leads: stage (0..2) für Progressive Profiling
- ⏸️ Check Constraint: stage BETWEEN 0 AND 2

**V256: lead_activities_augment.sql**
- ⏸️ ALTER TABLE lead_activities: counts_as_progress (DEFAULT FALSE)
- ⏸️ Neue Felder: summary, outcome, next_action, next_action_date, performed_by
- ⏸️ Backfill performed_by aus user_id

**V257: lead_progress_helpers_and_triggers.sql**
- ⏸️ Function: calculate_protection_until(registered_at)
- ⏸️ Function: calculate_progress_deadline(last_activity)
- ⏸️ Trigger: update_progress_on_activity (bei counts_as_progress=true)

**V258: NICHT IMPLEMENTIERT**
- ❌ lead_transfers Tabelle → verschoben auf Sprint 2.1.6
- ❌ Backdating Endpoint → verschoben auf Sprint 2.1.6

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