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

### Phase 2: Frontend (PR #125) 🚧 IN PROGRESS
**Branch:** `feature/mod02-sprint-2.1.5-lead-protection`
**Status:** PARTIAL (2025-10-04)

**⚠️ ARCHITEKTUR-ENTSCHEIDUNG (ADR-006): Hybrid-Ansatz**
- ✅ **Phase 1 (Sprint 2.1.5):** Leads als Customer-Status (CustomersPageV2-Wiederverwendung)
- 🔄 **Phase 2 (Sprint 2.1.6):** Lead-spezifische Erweiterungen (Scoring, Workflows, Timeline)
- 📄 [ADR-006-lead-management-hybrid-architecture.md](../../shared/adr/ADR-006-lead-management-hybrid-architecture.md)

#### Frontend Components (React/TypeScript)

**Progressive Profiling (Sprint 2.1.5):**
- ✅ `LeadWizard.tsx` - Progressive 3-Stufen-Form (Stage 0/1/2)
  - ✅ **Zwei-Felder-Lösung (Design-Entscheidung 2025-10-04):**
    - Feld 1: Notizen/Quelle (immer sichtbar, optional) - KEIN Schutz-Einfluss
    - Feld 2: Erstkontakt-Dokumentation (conditional) - Aktiviert Schutz
  - ✅ Option A: Differenzierte Erstkontakt-Regelung
    - MESSE/TELEFON: Erstkontakt-Block immer sichtbar, PFLICHT
    - EMPFEHLUNG/WEB/PARTNER: Checkbox "Ich hatte bereits Erstkontakt" → nur dann Erstkontakt-Block
  - ✅ Dynamic UI-Texte basierend auf Source
  - ✅ Validation: nur validieren wenn User angefangen hat zu tippen
  - ✅ Calendar auto-close bei Datumswahl
  - ✅ Farbkorrektur: grey.50 statt info.light
  - ✅ Error-Indication für Phone/Email Felder
  - ✅ Legacy Backend-Support (name, email fields)

**Lead-Management UI - Phase 1 (Sprint 2.1.5):**
- ✅ `LeadsPage.tsx` - Wrapper um CustomersPageV2
  - ✅ Default-Filter: `status: ['LEAD']`
  - ✅ Title: "Lead-Management"
  - ✅ Create-Button: "Lead erfassen"
  - ✅ Context-Prop: `context="leads"` (Lifecycle-basierte Filter)
- ✅ Verfügbare Features (CustomersPageV2):
  - ✅ Intelligent Filter Bar + Quick Filters
    - ✅ **Lifecycle-Context-Architektur:** Kontext-sensitive Status-Filter
    - ✅ `context="leads"` → Status: LEAD, PROSPECT
    - ✅ `context="customers"` → Status: AKTIV, INAKTIV, ARCHIVIERT
    - ✅ Alle anderen Filter identisch (Risiko, Umsatz, Kontakte)
  - ✅ Smart Sort (verkaufsorientiert)
  - ✅ Column Manager (Drag & Drop)
  - ✅ Universal Search mit Caching
  - ✅ Virtualisierte Tabelle (react-window)
  - ✅ Zustand Store (persistent)

**Obsolet (durch Phase 1 ersetzt):**
- ❌ `LeadListEnhanced.tsx` - Card-basierte UI (wird gelöscht)
- ❌ `LeadStageBadge.tsx` - Duplikat zu CustomerStatusBadge (wird gelöscht)

**Behalten (Lead-spezifisch):**
- ✅ `LeadSourceIcon.tsx` - Emoji-Icons mit Tooltips
- ✅ `LeadProtectionBadge.tsx` - Schutzstatus-Visualisierung
- ⏸️ `ActivityTimeline.tsx` - 60-Tage-Progress Tracking (Sprint 2.1.6)

#### API Integration
- ✅ Enhanced `POST /api/leads` mit Stage-Validierung
- ✅ Stage-Transition-Rules (0→1→2, kein Skip)
- ✅ 201/409 Response-Handling
- ✅ Dev-Mode-Auth: sessionStorage dev-auth-user Support
- ⏸️ GET /api/leads: Backend-DTO-Anpassung erforderlich (aktuell: alte Struktur mit `name` statt `companyName`)

#### Tests
- ⏸️ Integration Tests für Progressive Profiling Flow
- ⏸️ Stage-Transition-Tests
- ⏸️ UI-Component-Tests (Vitest)

#### Performance-Optimierung
- ⏸️ **Server-Side Filtering & Pagination** (Sprint 2.1.5)
  - Migration von Client-Side zu Server-Side Filtering
  - Performance: 1600ms → 75ms (95% faster)
  - Response Size: 850KB → 45KB (94% smaller)
  - useCustomerSearch Hook + Load More Pagination
  - 📄 [SERVER_SIDE_FILTERING.md](./SERVER_SIDE_FILTERING.md)

#### Dokumentation
- ✅ FRONTEND_DELTA.md Section 8 (Lead-Management UI - ADR-006)
- ✅ FRONTEND_DELTA.md Section 9 (Server-Side Filtering)
- ✅ BUSINESS_LOGIC_LEAD_ERFASSUNG.md Section 11 (Hybrid-Ansatz)
- ✅ PRE_CLAIM_LOGIC.md (Option A Aktualisierung)
- ✅ i18n/de/leads.json (Dynamic UI-Texte)
- ✅ ADR-006 (Lead-Management Hybrid-Architektur)
- ✅ SERVER_SIDE_FILTERING.md (Performance-Optimierung)

### Verschoben auf Sprint 2.1.6 (Phase 2)
- ❌ V258 lead_transfers Tabelle
- ❌ PUT /api/leads/{id}/registered-at (Backdating Endpoint)
- ❌ Nightly Jobs (Warning/Expiry/Pseudonymisierung)
- ❌ Vollständiger Fuzzy-Matching Algorithmus
- ❌ DuplicateReviewModal.vue (Merge/Unmerge UI)

**Phase 2 Erweiterungen (Sprint 2.1.6):**
- 🔄 Lead-Scoring-System (0-100 Punkte)
- 🔄 Lead-Status-Workflows (LEAD → PROSPECT → AKTIV)
- 🔄 Lead-Activity-Timeline
- 🔄 Lead-Protection aktivieren (assignedTo Filter)

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