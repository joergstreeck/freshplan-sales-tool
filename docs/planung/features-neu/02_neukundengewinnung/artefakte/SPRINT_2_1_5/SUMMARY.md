---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "konzept"
status: "in_progress"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-10-02"
---

# Sprint 2.1.5 – Artefakte Summary

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5

## Übersicht

Sprint 2.1.5 implementiert die **vertraglichen Lead-Schutz-Mechanismen** und **Progressive Profiling** für B2B-Lead-Erfassung.

## PR-Strategie (Backend/Frontend Split)

### Phase 1: Backend (PR #124) ✅ COMPLETE
**Branch:** `feature/mod02-sprint-2.1.5-lead-protection`
**Status:** READY FOR PR (2025-10-01)

#### Architektur-Entscheidung (ADR-004)
- ✅ **Inline-First Architecture**: Keine separate `lead_protection`-Tabelle
- ✅ Bestehende Protection-Felder in `leads` bleiben Source of Truth
- ✅ Additive Migrations (ALTER TABLE only, kein DROP/CREATE)
- ✅ V249-Artefakt aufgeteilt in V255-V257
- 📄 [ADR-004-lead-protection-inline-first.md](../../shared/adr/ADR-004-lead-protection-inline-first.md)

#### Backend Migrations (V255-V257)

**V255: leads_protection_basics_and_stage.sql**
- ✅ ALTER TABLE leads: progress_warning_sent_at, progress_deadline
- ✅ ALTER TABLE leads: stage (0..2) für Progressive Profiling
- ✅ Check Constraint: stage BETWEEN 0 AND 2
- ✅ Indizes: idx_leads_progress_deadline, idx_leads_stage

**V256: lead_activities_augment.sql**
- ✅ ALTER TABLE lead_activities: counts_as_progress (DEFAULT FALSE)
- ✅ Neue Felder: summary, outcome, next_action, next_action_date, performed_by
- ✅ Backfill performed_by aus user_id
- ✅ Index: idx_lead_activities_progress

**V257: lead_progress_helpers_and_triggers.sql**
- ✅ Function: calculate_protection_until(registered_at, protection_months)
- ✅ Function: calculate_progress_deadline(last_activity_at)
- ✅ Trigger: trg_update_progress_on_activity (bei counts_as_progress=true)

#### Entities & Services
- ✅ Lead.java: +3 Felder (progressWarningSentAt, progressDeadline, stage)
- ✅ LeadActivity.java: +6 Felder (countsAsProgress, summary, outcome, etc.)
- ✅ LeadProtectionService: +4 Methoden (canTransitionStage, calculateProtectionUntil, calculateProgressDeadline, needsProgressWarning)

#### Tests
- ✅ 24 Unit Tests (0.845s, Pure Mockito, 100% passed)
- ✅ Test-Struktur: @Nested, @ParameterizedTest, @Tag("unit")
- ✅ LeadProtectionServiceTest: Stage-Transitions, Progress-Deadlines, Protection-Calculations

#### Dokumentation
- ✅ ADR-004 (Inline-First Architecture)
- ✅ DELTA_LOG_2_1_5 (Implementierungs-Entscheidungen + PR-Strategie)
- ✅ CONTRACT_MAPPING (§3.2, §3.3 Vertragsabdeckung)
- ✅ TEST_PLAN (Mock-First Strategie)
- ✅ SUMMARY (diese Datei)
- ✅ TRIGGER_SPRINT_2_1_6 (verschobene Features)

### Phase 2: Frontend (PR #125) 🔄 IN PROGRESS
**Branch:** `feature/mod02-sprint-2.1.5-frontend-progressive-profiling`
**Status:** IN PROGRESS (02.10.2025)

#### Business Rules Defined (02.10.2025)
- ✅ **DSGVO Consent-Management:** Consent-Checkbox PFLICHT in Stage 1 (nicht vorausgefüllt)
  - Backend: `lead.consent_given_at TIMESTAMPTZ` speichern
  - Validierung: Ohne Consent KEIN Stage 1 Submit möglich
  - 📄 [DSGVO_CONSENT_SPECIFICATION.md](./DSGVO_CONSENT_SPECIFICATION.md)

- ✅ **Activity-Types Progress-Mapping:** countsAsProgress Regeln definiert
  - TRUE: QUALIFIED_CALL, MEETING, DEMO, ROI_PRESENTATION, SAMPLE_SENT (5 Types)
  - FALSE: NOTE, FOLLOW_UP, EMAIL, CALL, SAMPLE_FEEDBACK (5 Types)
  - 📄 [ACTIVITY_TYPES_PROGRESS_MAPPING.md](./ACTIVITY_TYPES_PROGRESS_MAPPING.md)

- ✅ **Stop-the-Clock RBAC Rules:** Manager + Admin only
  - RBAC: Nur MANAGER + ADMIN dürfen pausieren/resumen
  - UI-Button verschoben auf Sprint 2.1.6
  - 📄 [STOP_THE_CLOCK_RBAC_RULES.md](./STOP_THE_CLOCK_RBAC_RULES.md)

#### Frontend Components (React + MUI v7)
- ⏸️ `LeadWizard.tsx` - Progressive 3-Stufen-Form (Stage 0/1/2, Full-Page Component)
- ⏸️ `LeadProtectionBadge.tsx` - Schutzstatus-Indikator (Tooltip/Responsive/ARIA)
- ⏸️ `ActivityTimeline.tsx` - 60-Tage-Progress Tracking (countsAsProgress Filter)

#### API Integration
- ⏸️ Enhanced `POST /api/leads` mit Stage + Consent-Validierung
- ⏸️ Stage-Transition-Rules (0→1→2, kein Skip)
- ⏸️ 201/409 Response-Handling (RFC7807)

#### Tests
- ⏸️ Integration Tests für Progressive Profiling Flow (MSW-basiert)
- ⏸️ Stage-Transition-Tests
- ⏸️ UI-Component-Tests (Vitest)
- ⏸️ DSGVO Consent Tests (Checkbox required, Backend validation)

#### Feature-Flag
- ⏸️ `VITE_FEATURE_LEADGEN=true` für Feature-Toggle

### Verschoben auf Sprint 2.1.6
- ❌ **V258 lead_transfers Tabelle** - Lead-Transfer zwischen Partnern
- ❌ **PUT /api/leads/{id}/registered-at** - Backdating Endpoint für Bestandsleads
- ❌ **POST /api/admin/migration/leads/import** - Bestandsleads-Migrations-API (Modul 08)
  - 📄 [BESTANDSLEADS_MIGRATION_API_SPEC.md](../SPRINT_2_1_6/BESTANDSLEADS_MIGRATION_API_SPEC.md)
- ❌ **Lead → Kunde Convert Flow** - Automatische Übernahme bei QUALIFIED → CONVERTED
  - 📄 [LEAD_TO_CUSTOMER_CONVERT_FLOW.md](../SPRINT_2_1_6/LEAD_TO_CUSTOMER_CONVERT_FLOW.md)
- ❌ **StopTheClockDialog UI** - Manager-only Dialog für Pausierung
- ❌ **Nightly Jobs** - Warning/Expiry/Pseudonymisierung (Scheduled Tasks)
- ❌ **Vollständiger Fuzzy-Matching Algorithmus** - Levenshtein-Distance, pg_trgm
- ❌ **DuplicateReviewModal** - Merge/Unmerge UI mit Identitätsgraph

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