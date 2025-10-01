---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# 🗺️ Sprint-Map – Modul 02 Neukundengewinnung

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Sprint-Map

**Start here:** Dieser Index verweist auf die *zentralen* Sprint-Dokumente (keine Kopien!).

## 🎯 Relevante Sprints

### **Sprint 2.1 – Modul 02 Backend (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1.md](../../TRIGGER_SPRINT_2_1.md)
**Status:** ✅ 100% COMPLETE
**Ergebnisse:**
- Lead-Management System ohne Gebietsschutz
- Territory-Scoping (Deutschland/Schweiz)
- CQRS Light Integration
- Security Test Pattern, Performance Test Pattern, Event System Pattern

**PRs:** #103, #105, #110 (alle merged)

---

### **Sprint 2.1.1 – P0 Hotfix Integration Gaps (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1_1.md](../../TRIGGER_SPRINT_2_1_1.md)
**Spezifisches Log:** [SPRINT_2_1_1_DELTA_LOG.md](../../SPRINT_2_1_1_DELTA_LOG.md)
**Status:** ✅ COMPLETE
**Ergebnisse:**
- Event Distribution Pipeline (PostgreSQL LISTEN/NOTIFY)
- Dashboard Widget Integration
- Metrics Implementation (freshplan_* ohne _total suffix)
- FP-235: T+3/T+7 Follow-up Automation
- AFTER_COMMIT Pattern + Idempotenz + RBAC

**PRs:** #111 (merged 2025-09-26)

---

### **Sprint 2.1.2 – Frontend Research (COMPLETE)**
**Modul-Spezifisch:** [frontend/analyse/_index.md](./frontend/analyse/_index.md)
**Status:** ✅ Research Complete (PR #112, Draft)
**Ergebnisse:**
- **INVENTORY.md:** Stack-Analyse + Gaps + Foundation Patterns
- **API_CONTRACT.md:** Event-System + RBAC + REST-Endpoints + Polling Strategy
- **RESEARCH_ANSWERS.md:** 11 offene Fragen beantwortet
- **VALIDATED_FOUNDATION_PATTERNS.md:** Konsolidierte Patterns aus grundlagen/ & infrastruktur/

**Nächster Schritt:** Thin Vertical Slice mit Feature-Flag `VITE_FEATURE_LEADGEN`

---

### **Sprint 2.1.3 – Frontend Implementation (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1_3.md](../../TRIGGER_SPRINT_2_1_3.md)
**Status:** ✅ COMPLETE
**Ergebnisse:**
- Thin Vertical Slice: `/leads` Route + `LeadList` + `LeadCreateDialog`
- Feature-Flag: `VITE_FEATURE_LEADGEN=true` aktiviert
- Vollständige Business-Logik:
  - Client-seitige Validierung (Name ≥2, E-Mail-Format)
  - Duplikat-Erkennung (409 Response bei gleicher E-Mail)
  - Source-Tracking (`source='manual'`)
  - RFC7807 Error Handling mit Feld-Fehlern
- Vollständige i18n (de/en) ohne hardcoded Strings
- MUI Theme V2 Integration + MainLayoutV2 Wrapper
- Tests: 90% Coverage (Integration Tests für alle Business-Flows)
- MSW für realistische API-Simulation

**PRs:** #122 (merged 2025-09-28)

---

### **Sprint 2.1.4 – Lead Deduplication & Data Quality (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1_4.md](../../TRIGGER_SPRINT_2_1_4.md)
**Status:** ✅ COMPLETE
**Scope:** Lead Normalization & Deduplication – Phase 1

**Ergebnisse:**
- **Normalisierung:** email (lowercase), phone (E.164), company (ohne Suffixe/Rechtsformen)
- **Partielle UNIQUE Indizes:** WHERE status != 'DELETED' für email/phone/company
- **IdempotencyService:** 24h TTL, SHA-256 Request-Hash, atomic INSERT … ON CONFLICT
- **LeadNormalizationService:** 31 Tests, vollständige Normalisierungs-Logik
- **CI Performance Breakthrough:** 24min → 7min (70% schneller!)
  - Root Cause 1: junit-platform.properties override (blockierte Maven Surefire parallel)
  - Root Cause 2: ValidatorFactory in @BeforeEach (56s verschwendet)
  - Fix: JUnit parallel config entfernt, ValidatorFactory → @BeforeAll static
- **Test-Migration:** @QuarkusTest ↓27% (8 DTO-Tests → Plain JUnit mit Mockito)

**Migrations:**
- V247: Normalisierung (email_normalized, phone_e164, company_name_normalized)
- V10012: CI-only Indizes (non-CONCURRENTLY für Tests)
- V251-V254: Idempotency-Fixes, Events published column
- R__normalize_functions.sql: Repeatable normalization functions

**Deliverables:**
- LeadNormalizationService mit 31 Tests
- IdempotencyService mit 8 Tests + Integration Tests
- Partielle UNIQUE Constraints mit WHERE-Klauseln
- RFC7807 Problem Details für 409 Conflicts
- TEST_DEBUGGING_GUIDE.md mit Performance Patterns aktualisiert

**Artefakte:** [`artefakte/SPRINT_2_1_4/`](./artefakte/SPRINT_2_1_4/)
**PRs:** #123 (merged 2025-10-01)

---

### **Sprint 2.1.5 – Lead Protection & Progressive Profiling (IN_PROGRESS)**
**Zentral:** [TRIGGER_SPRINT_2_1_5.md](../../TRIGGER_SPRINT_2_1_5.md)
**Status:** 🔄 Backend Phase 1 COMPLETE (01.10.2025), Frontend Phase 2 ausstehend
**Scope:** Vertragliche Lead-Schutz-Mechanismen + B2B Progressive Profiling

**✅ Backend Phase 1 COMPLETE (PR #124):**
**Branch:** `feature/mod02-sprint-2.1.5-lead-protection`
**Status:** READY FOR PR (01.10.2025)

- **ADR-004:** Inline-First Architecture (PLAN B statt separate lead_protection Tabelle)
- **Migrations V255-V257:**
  - V255: Progress Tracking + Stage (0=Vormerkung, 1=Registrierung, 2=Qualifiziert)
  - V256: lead_activities augmentation (counts_as_progress, summary, outcome, next_action)
  - V257: Functions + Trigger (calculate_protection_until, trg_update_progress_on_activity)
- **Entities:** Lead.java (+3), LeadActivity.java (+6)
- **Service:** LeadProtectionService (canTransitionStage, calculateProgressDeadline, needsProgressWarning)
- **Tests:** 24 Unit Tests (0.845s, Pure Mockito, 100% passed)
- **Dokumentation:** ADR-004, DELTA_LOG, CONTRACT_MAPPING, TEST_PLAN, SUMMARY, TRIGGER_2_1_6

**⏸️ Frontend Phase 2 (PR #125 - ausstehend):**
**Branch:** `feature/mod02-sprint-2.1.5-frontend-progressive-profiling`
**Status:** NOT STARTED

- LeadWizard.vue (3-Stufen Progressive Profiling UI)
- LeadProtectionBadge.vue (Status-Indicator)
- ActivityTimeline.vue (Progress Tracking Display)
- API-Integration: Enhanced POST /api/leads mit Stage-Validierung
- Tests: Integration Tests für Progressive Profiling Flow

**📋 Verschoben auf Sprint 2.1.6:**
- V258 lead_transfers Tabelle
- PUT /api/leads/{id}/registered-at (Backdating Endpoint)
- Nightly Jobs (Warning/Expiry/Pseudonymisierung)
- Vollständiger Fuzzy-Matching Algorithmus + DuplicateReviewModal.vue

**Artefakte:** [`artefakte/SPRINT_2_1_5/`](./artefakte/SPRINT_2_1_5/)
- ✅ [ADR-004-lead-protection-inline-first.md](./shared/adr/ADR-004-lead-protection-inline-first.md)
- ✅ [DELTA_LOG_2_1_5.md](./artefakte/SPRINT_2_1_5/DELTA_LOG_2_1_5.md) (Implementierungs-Entscheidungen + PR-Strategie)
- ✅ [CONTRACT_MAPPING.md](./artefakte/SPRINT_2_1_5/CONTRACT_MAPPING.md) (§3.2, §3.3)
- ✅ [TEST_PLAN.md](./artefakte/SPRINT_2_1_5/TEST_PLAN.md) (Mock-First Strategie)
- ✅ [SUMMARY.md](./artefakte/SPRINT_2_1_5/SUMMARY.md) (PR-Strategie + Deliverables)

**Delta:** Scope geändert von "Matching & Review" zu "Protection & Progressive", PLAN B (Inline-First) statt V249-Artefakt, Backend/Frontend Split (PR #124/#125)

---

### **Sprint 2.1.6 – Lead Transfer & Team Management (PLANNED)**
**Status:** 📅 PLANNED (2025-10-12 - 2025-10-18)
**Scope:** Lead-Übergabe, Team-Management, Merge/Unmerge

> **⚠️ TEST-STRATEGIE BEACHTEN!**
> Tests MÜSSEN Mocks verwenden, NICHT @QuarkusTest mit echter DB!
> Siehe: [backend/TEST_MIGRATION_PLAN.md](./backend/TEST_MIGRATION_PLAN.md)

**Geplante Features:**
- Lead-Transfer zwischen Partnern mit Genehmigung
- Quotenregelung für Teams
- Fuzzy-Matching & Review-Flow (Scoring, Kandidatenliste, Merge/Reject/Create-New)
- Merge/Unmerge mit Identitätsgraph
- Audit-Historie für alle Transfers
- Team-basierte Sichtbarkeit (RLS Phase 1)

**Note:** Enthält Matching & Review Features (ursprünglich für 2.1.5 geplant)

---

## 🔗 **Cross-Module Dependencies**

### **Infrastruktur-Sprints (relevant für Modul 02):**
- **TRIGGER_SPRINT_1_2.md:** Security Foundation (RBAC + Territory RLS)
- **TRIGGER_SPRINT_1_3.md:** CQRS Foundation (Event-System Basis)
- **TRIGGER_SPRINT_1_5.md:** Performance Foundation (Bundle <200KB)

### **ADRs (Architecture Decisions):**
- **ADR-0002:** PostgreSQL LISTEN/NOTIFY (Event-Architektur)
- **ADR-0004:** Territory RLS vs Lead-Ownership (Security-Modell)

## 📋 **Implementation Timeline**

```yaml
Phase 1 (Backend): Sprint 2.1 + 2.1.1
  Status: ✅ COMPLETE
  Result: Production-ready Lead-Management Backend

Phase 2 (Frontend Research): Sprint 2.1.2
  Status: ✅ COMPLETE
  Result: Vollständige Frontend-Analyse + API-Contract

Phase 3 (Frontend Implementation): Sprint 2.1.3
  Status: ✅ COMPLETE
  Result: Lead Management MVP mit Business-Logik
  PR: #122 (merged)
```

## 🎯 **Für neue Claude-Instanzen**

**Start immer mit den zentralen Trigger-Dokumenten** – sie enthalten die vollständigen Sprint-Kontexte, Cross-Module-Dependencies und Entscheidungshistorie. Diese Sprint-Map ist nur ein **Navigationshelfer**, nicht die Quelle der Wahrheit.