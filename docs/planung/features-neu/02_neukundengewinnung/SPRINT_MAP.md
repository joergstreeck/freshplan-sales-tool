---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-10-02"
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

**Nächster Schritt:** Thin Vertical Slice (Thin Vertical Slice implemented in Sprint 2.1.3)

---

### **Sprint 2.1.3 – Frontend Implementation (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1_3.md](../../TRIGGER_SPRINT_2_1_3.md)
**Status:** ✅ COMPLETE
**Ergebnisse:**
- Thin Vertical Slice: `/leads` Route + `LeadList` + `LeadCreateDialog`
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

### **Sprint 2.1.5 – Lead Protection & Progressive Profiling (COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1_5.md](../../TRIGGER_SPRINT_2_1_5.md)
**Status:** ✅ Backend Phase 1 COMPLETE (01.10.2025), Frontend Phase 2 COMPLETE (04.10.2025)
**Scope:** Vertragliche Lead-Schutz-Mechaniken + B2B Progressive Profiling + DSGVO Consent

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
- **Dokumentation:** ADR-004, DELTA_LOG, CONTRACT_MAPPING, TEST_PLAN, SUMMARY, TRIGGER_2_1_6, BUSINESS_LOGIC_LEAD_ERFASSUNG.md

**✅ Frontend Phase 2 (PR #125 - COMPLETE):**
**Branch:** `feature/mod02-sprint-2.1.5-frontend-progressive-profiling`
**Status:** ✅ COMPLETE (04.10.2025)

**✅ Implementiert:**
- ✅ LeadWizard.tsx (3-Stufen Progressive Profiling UI, Full-Page Component)
- ✅ **Context-Prop Architecture:** CustomersPageV2 mit `context='leads'` Prop
  - ✅ Context-basierte API-Aufrufe (useLeads Hook für /api/leads)
  - ✅ Context-basierte Spalten (LEADS_TABLE_COLUMNS: Lead, Status, Branche, Erwarteter Umsatz, Erstellt am)
  - ✅ Context-basierte Sortierung (LEADS_SORT_OPTIONS: Name, Status, Erwarteter Umsatz, Erstellt am)
  - ✅ Context-basierte Filter (Status: LEAD/PROSPECT für Leads, AKTIV/INAKTIV für Customers)
  - ✅ Context-basierte Navigation (Leads: keine Detail-Seite, Customers: /customers/:id)
- ✅ **Migration V259:** Removed ui_leads_company_city unique constraint (Company+City = SOFT collision)
- ✅ API-Integration: Enhanced POST /api/leads mit Stage-Validierung
- ✅ Lead-Erstellung funktioniert mit Vormerkung (Stage 0) + Sonstige-Source
- ✅ Lead-Liste zeigt erstellte Leads nach refetch()
- ✅ LeadWizard ist Standard (keine Alternative UI)
- ✅ **Zwei-Felder-Lösung:** Separates Notizen-Feld + Erstkontakt-Block mit Checkbox (LeadWizard.tsx:365-505)
- ✅ **Pre-Claim Badge:** "⏳ Pre-Claim (X Tage)" Badge in CustomerTable (CustomerTable.tsx:95-108, 162-177)
- ✅ **Backend DTO:** registeredAt, protectedUntil, progressDeadline Felder (LeadDTO.java:58, 125)
- ✅ **Frontend Types:** registeredAt nullable für Pre-Claim Detection (types.ts:68)

**📋 Verschoben auf Sprint 2.1.6 (OPTIONAL Features):**
- ⏭️ **Quick-Action:** "Erstkontakt nachtragen" Button für Pre-Claim Leads
- ⏭️ **Pre-Claim Filter:** Quick-Filter "Pre-Claim Leads" in IntelligentFilterBar

**📋 Dokumentation COMPLETE (04.10.2025):**
- ✅ BUSINESS_LOGIC_LEAD_ERFASSUNG.md (Zwei-Felder-Lösung dokumentiert)
- ✅ PRE_CLAIM_LOGIC.md (UI-Beispiele mit Checkbox-Logik)
- ✅ FRONTEND_DELTA.md (Validierungsregeln-Tabelle + Implementierungs-Beispiele)
- ✅ SUMMARY.md (Zwei-Felder-Lösung als Feature aufgeführt)

**Activity-Types Progress-Mapping (definiert 02.10.2025):**
- ✅ countsAsProgress=true: QUALIFIED_CALL, MEETING, DEMO, ROI_PRESENTATION, SAMPLE_SENT
- ❌ countsAsProgress=false: NOTE, FOLLOW_UP, EMAIL, CALL, SAMPLE_FEEDBACK

**Stop-the-Clock Rules (Backend-only in 2.1.5):**
- RBAC: Nur MANAGER + ADMIN Role
- UI-Button: NICHT in Phase 2 (verschoben auf 2.1.6)
- Erlaubte Gründe: "FreshFoodz Verzögerung", "Kunde im Urlaub", "Andere"
- Audit-Log PFLICHT für jeden Stop/Resume Event

**📋 Verschoben auf Sprint 2.1.6:**
- V258 lead_transfers Tabelle (Lead-Transfer zwischen Partnern)
- POST /api/admin/migration/leads/import (Bestandsleads-Migrations-API, Modul 08)
- PUT /api/leads/{id}/registered-at (Backdating Endpoint)
- Lead → Kunde Convert Flow (automatische Übernahme bei QUALIFIED → CONVERTED)
- StopTheClockDialog UI (Manager-only, mit Approval-Workflow)
- ExtensionRequestDialog UI (Schutzfrist-Verlängerung)
- Nightly Jobs (Warning/Expiry/Pseudonymisierung - Scheduled Tasks)
- Vollständiger Fuzzy-Matching Algorithmus (Levenshtein-Distance, pg_trgm)
- DuplicateReviewModal (Merge/Unmerge UI mit Identitätsgraph)
- **Lead Status-Labels Frontend (REGISTERED → "Vormerkung", ACTIVE → "Aktiv", etc.)**
- **Lead Action-Buttons (Löschen/Bearbeiten) in CustomerTable**
- **Lead Detail-Seite für Navigation bei Lead-Klick**

**Artefakte:** [`artefakte/SPRINT_2_1_5/`](./artefakte/SPRINT_2_1_5/)

**Backend:**
- ✅ [ADR-004-lead-protection-inline-first.md](./shared/adr/ADR-004-lead-protection-inline-first.md)
- ✅ [DELTA_LOG_2_1_5.md](./artefakte/SPRINT_2_1_5/DELTA_LOG_2_1_5.md) (Implementierungs-Entscheidungen + PR-Strategie)
- ✅ [CONTRACT_MAPPING.md](./artefakte/SPRINT_2_1_5/CONTRACT_MAPPING.md) (§3.2, §3.3)
- ✅ [TEST_PLAN.md](./artefakte/SPRINT_2_1_5/TEST_PLAN.md) (Mock-First Strategie)
- ✅ [PRE_CLAIM_LOGIC.md](./artefakte/SPRINT_2_1_5/PRE_CLAIM_LOGIC.md)
- ✅ [DEDUPE_POLICY.md](./artefakte/SPRINT_2_1_5/DEDUPE_POLICY.md)
- ✅ [ACTIVITY_TYPES_PROGRESS_MAPPING.md](./artefakte/SPRINT_2_1_5/ACTIVITY_TYPES_PROGRESS_MAPPING.md)
- ✅ [BUSINESS_LOGIC_LEAD_ERFASSUNG.md](./artefakte/SPRINT_2_1_5/BUSINESS_LOGIC_LEAD_ERFASSUNG.md) (Zentrale Business-Logik Referenz)

**Frontend:**
- ✅ [FRONTEND_ACCESSIBILITY.md](../../../../frontend/FRONTEND_ACCESSIBILITY.md)
- ✅ [FRONTEND_DELTA.md](./artefakte/SPRINT_2_1_5/FRONTEND_DELTA.md)
- ✅ [SUMMARY.md](./artefakte/SPRINT_2_1_5/SUMMARY.md)

**DSGVO & Compliance:**
- ✅ [DSGVO_CONSENT_SPECIFICATION.md](./artefakte/SPRINT_2_1_5/DSGVO_CONSENT_SPECIFICATION.md)

**Delta:** Scope geändert von "Matching & Review" zu "Protection & Progressive", PLAN B (Inline-First) statt V249-Artefakt, Backend/Frontend Split (PR #124/#125)

---

### **Sprint 2.1.6 – Lead Completion & Admin Features (PHASE 4 COMPLETE)**
**Zentral:** [TRIGGER_SPRINT_2_1_6.md](../../TRIGGER_SPRINT_2_1_6.md)
**Status:** ✅ Phase 4 COMPLETE (2025-10-08) - Lead Intelligence Tests + LeadScoringService ✅
**Scope:** Bestandsleads-Migration, Convert-Flow, BusinessType Harmonization, Stop-the-Clock UI, Automated Jobs, Lead Intelligence

**⚠️ PRIORITY #0 - BLOCKER FIRST:**
- **Issue #130:** TestDataBuilder Refactoring (12 Tests broken, CI disabled)
- **Root Cause:** CDI-Konflikt zwischen Legacy Builder (src/main) und neuen Builder (src/test)
- **Fix:** Legacy Builder löschen, Tests auf neue Builder migrieren (1-2h)
- **Impact:** Worktree CI reaktivieren, 12 ContactInteractionServiceIT Tests grün

**Kern-Deliverables (UPDATED 06.10.2025):**
- ✅ **BusinessType Harmonization (Phase 2 COMPLETE):**
  - Shared BusinessType Enum (9 unified values)
  - V263: Lead.businessType + CHECK constraint
  - V264: Customer.businessType + Data Migration (Industry → BusinessType)
  - Single Source of Truth: GET /api/enums/business-types
  - Frontend: useBusinessTypes/useLeadSources/useKitchenSizes hooks
  - EnumField Component + DynamicFieldRenderer extension
  - Backward Compatibility: Auto-sync setters (industry ↔ businessType)
  - **Artefakt:** [HARMONIZATION_COMPLETE.md](./artefakte/SPRINT_2_1_6/HARMONIZATION_COMPLETE.md) ⭐ NEU
- ✅ **Bestandsleads-Migrations-API** (Modul 08):
  - POST /api/admin/migration/leads/import (Admin-only, Dry-Run Mode PFLICHT)
  - Batch-Import mit Validierung (max. 1000 Leads/Batch)
  - Historische Datumsfelder korrekt übernehmen (registeredAt, activities)
  - countsAsProgress explizit setzen (NICHT automatisch berechnen!)
  - Duplikaten-Check + Warning-Report
  - Audit-Log für alle Import-Vorgänge
  - Re-Import-Fähigkeit bei Fehlern
- ✅ **Lead → Kunde Convert Flow:**
  - Automatische Übernahme bei Status QUALIFIED → CONVERTED
  - Alle Lead-Daten übernehmen (ZERO Doppeleingabe)
  - Lead-ID Verknüpfung in customer.original_lead_id
  - Historie vollständig erhalten
- ⏸️ **Stop-the-Clock UI:**
  - StopTheClockDialog Component (Manager + Admin only)
  - Pause/Resume Buttons in LeadProtectionBadge
  - Grund-Auswahl mit Audit-Log
  - Maximale Pausendauer konfigurierbar
- ✅ **Backdating Endpoint:**
  - PUT /api/leads/{id}/registered-at (Admin/Manager)
  - Validierung: nicht in Zukunft; Reason Pflicht
  - Recalc Protection-/Activity-Fristen
- ⏸️ **Automated Jobs:**
  - Nightly Job: Progress Warning Check (Tag 53)
  - Nightly Job: Protection Expiry (Tag 70)
  - Nightly Job: Pseudonymisierung (60 Tage ohne Progress)

**✅ Phase 4 COMPLETE - Lead Intelligence Tests + LeadScoringService (PR #135):**
- ✅ **LeadScoringService** (264 LOC): 4-Faktoren-Berechnung (Umsatzpotenzial, Engagement, Fit, Dringlichkeit)
- ✅ **LeadScoringServiceTest** (19 Tests GREEN): Business-Logic-Tests + Integration + Edge-Cases
- ✅ **StopTheClockDialog.test.tsx** (12 Tests GREEN): RBAC-Permission-Tests (USER/ADMIN/MANAGER/SALES)
- ✅ **LeadResourceTest** (+3 DTO-Completeness-Tests): leadScore, progressPauseTotalSeconds, nullable fields
- ✅ **Testing Guide** (/docs/grundlagen/testing_guide.md): "Tests sind kein Selbstzweck - Fehler finden!"
- ✅ **Bug Fixes:** 3 Produktionsbugs gefunden & gefixt (leadScore DTO-Mapping, RBAC UI-Check, businessType UPPERCASE)
- ✅ **Test-Statistik:** 103 Tests GREEN (43 Backend + 60 Frontend)

**Frontend UI Components (ADR-006 Phase 2 - bereits implementiert in früherem Commit):**
- ✅ Lead-Scoring-System (LeadScoreIndicator.tsx)
- ✅ Lead-Status-Workflows (LeadStatusWorkflow.tsx)
- ✅ Lead-Activity-Timeline (LeadActivityTimeline.tsx)
- ✅ Stop-the-Clock Dialog (StopTheClockDialog.tsx)
- ✅ Lead Protection Manager (LeadProtectionManager.tsx)
- ✅ Lead Quality Dashboard (LeadQualityDashboard.tsx)

**❌ VERSCHOBEN AUF SPRINT 2.1.7 (Scope-Overflow):**
- ~~Lead-Transfer zwischen Partnern~~ (User Story 1 - zu komplex!)
- ~~Fuzzy-Matching & Review~~ (User Story 4 - verdient eigenen Sprint)
- ~~Row-Level-Security (RLS)~~ (User Story 5 - komplex, gehört zu Transfer)
- ~~Team Management~~ (User Story 6 - komplex, gehört zu RLS)

**Scope-Änderung:** Fokus auf Admin-Features (Migration, Convert, Jobs) statt Team-Features (Transfer, RLS)

---

### **Sprint 2.1.7 – Team Management & Test Infrastructure Overhaul (PLANNED)**
**Zentral:** [TRIGGER_SPRINT_2_1_7.md](../../TRIGGER_SPRINT_2_1_7.md)
**Status:** 📅 PLANNED (2025-10-19 - 2025-10-25)
**Scope:** Lead-Transfer + RLS + Team Management (verschoben aus 2.1.6) + Test Infrastructure (NEU!)
**Aufwand:** ~48-68h (~1-1.5 Wochen)

**🎯 ZWEI PARALLELE TRACKS:**

**Track 1 - Business Features (verschoben aus 2.1.6):**
1. **Lead-Transfer Workflow**
   - V260: lead_transfers Tabelle
   - POST /api/leads/{id}/transfer - Transfer-Request initiieren
   - PUT /api/leads/transfers/{id} - Genehmigen/Ablehnen (Manager/Admin)
   - 48h SLA für Entscheidung, Auto-Escalation an Admin
   - Frontend: TransferRequestDialog.tsx + TransferApprovalList.tsx

2. **Fuzzy-Matching & Duplicate Review**
   - Scoring-Algorithmus (Email 40%, Phone 30%, Company 20%, Address 10%)
   - Schwellwerte: Hard Duplicate ≥90%, Soft Duplicate 70-89%
   - 202 Response bei Soft Duplicate mit Kandidaten-Liste
   - Frontend: DuplicateReviewModal.tsx + MergePreviewDialog.tsx
   - Merge-Logik mit Undo-Möglichkeit (24h)

3. **Row-Level-Security (RLS) Implementation**
   - V261: RLS Policies (owner_policy, team_policy, admin_policy, transfer_recipient_policy)
   - Session-Context: SET LOCAL app.user_id, app.user_role, app.user_team_id
   - Performance: P95 <50ms RLS-Overhead (Index-Optimierung)

4. **Team Management**
   - V262: teams + team_members Tabellen
   - Team CRUD API (POST/GET/PUT/DELETE /api/teams)
   - Team-Member Assignment + Role-Management
   - Quotenregelung (max. Leads pro Team)
   - Frontend: TeamManagementPage.tsx + TeamDashboard.tsx

**Track 2 - Test Infrastructure (NEU - STRATEGISCH!):**
5. **CRM Szenario-Builder**
   - ScenarioBuilder für komplexe CRM-Workflows
   - Lead-Journey: PreClaimLead, RegisteredLead, QualifiedLead, ProtectedLead
   - Customer-Journey: NewCustomer, ActiveCustomer, RiskCustomer
   - Opportunity-Pipeline: QualifiedOpportunity → ProposalOpportunity → WonOpportunity

6. **Faker-Integration für realistische Testdaten**
   - Java Faker Dependency (net.datafaker)
   - RealisticDataGenerator (germanCompanyName, germanAddress, germanPhoneNumber)
   - Edge-Case-Daten (Umlaute, Sonderzeichen, lange Namen)

7. **Lead-spezifische TestDataFactories**
   - LeadTestDataFactory (asPreClaimLead, asRegisteredLead, asQualifiedLead)
   - LeadActivityTestDataFactory (asProgressActivity, asNonProgressActivity)

8. **Lead Activity Capture UI** (NEU - VERVOLLSTÄNDIGUNG, 4-6h)
   - AddLeadActivityDialog.tsx (Activity-Type, Zusammenfassung, Details, Outcome)
   - Action-Button in CustomerTable.tsx (nur für context='leads')
   - API-Integration: POST /api/leads/{id}/activities
   - German labels (E-Mail, Anruf, Termin, Notiz, etc.)
   - Validation: min. 10 Zeichen Zusammenfassung, Pflicht-Activity-Type
   - Success-Feedback: Snackbar "Aktivität erfasst"

9. **Test-Pattern Library & Documentation**
   - TESTING_PATTERNS.md (5+ Patterns: Lead-Journey, RBAC+RLS, Activity-Progress)
   - TEST_DATA_CHEATSHEET.md (Quick Reference)
   - Migration Guide für alte Tests

**Begründung Track 2:**
- ✅ Quality Investment: Hochwertige Tests = weniger Bugs = schnellere Entwicklung
- ✅ Sprint 2.2 Readiness: Kundenmanagement braucht komplexe Customer-Test-Szenarien
- ✅ Onboarding: Neue Entwickler verstehen CRM-Workflows durch Testdaten
- ✅ Regression Prevention: Alle Edge-Cases als Test-Szenarien dokumentiert

**Deliverables:**
- Lead-Transfer API + Frontend (Track 1)
- Fuzzy-Matching Service + Review-UI (Track 1)
- RLS Policies + Team Management (Track 1)
- CRMScenarioBuilder + Faker + TestDataFactories (Track 2)
- Lead Activity Capture UI (AddLeadActivityDialog.tsx) (Track 2)
- TESTING_PATTERNS.md + TEST_DATA_CHEATSHEET.md (Track 2)

---

## 🔗 **Cross-Module Dependencies**

### **Infrastruktur-Sprints (relevant für Modul 02):**
- **TRIGGER_SPRINT_1_2.md:** Security Foundation (RBAC + Territory RLS)
- **TRIGGER_SPRINT_1_3.md:** CQRS Foundation (Event-System Basis)
- **TRIGGER_SPRINT_1_5.md:** Performance Foundation (Bundle <200KB)

### **ADRs (Architecture Decisions):**
- **ADR-0002:** PostgreSQL LISTEN/NOTIFY (Event-Architektur)
- **ADR-0004:** Territory RLS vs Lead-Ownership (Security-Modell)

### **Sprint 2.1.8 – DSGVO Compliance & Lead-Import (PLANNED) - 🔴 GESETZLICH PFLICHT**
**Zentral:** [TRIGGER_SPRINT_2_1_8.md](../../TRIGGER_SPRINT_2_1_8.md)
**Status:** 📅 PLANNED (2025-10-26 - 2025-11-01)
**Scope:** Gesetzliche DSGVO-Features + B2B-Standard Lead-Import
**Aufwand:** ~40-56h (~1 Woche)

**🎯 KERN-DELIVERABLES:**
1. **Lead-Import via CSV/Excel** (Self-Service Bulk-Import, 8-12h)
2. **DSGVO-Auskunfts-Recht (Art. 15)** (PDF-Export, 6-8h) - 🔴 GESETZLICH PFLICHT
3. **DSGVO-Lösch-Recht (Art. 17)** (Sofort-Löschung, 6-8h) - 🔴 GESETZLICH PFLICHT
4. **DSGVO-Einwilligungs-Widerruf (Art. 7 Abs. 3)** (Consent-Revoke, 4-6h)
5. **Advanced Search** (Full-Text-Search, 8-12h)
6. **BANT-Qualifizierungs-Wizard** (Budget/Authority/Need/Timeline, 8-10h)

**Migrations:** 5 DB-Änderungen (Nummern: siehe `get-next-migration.sh` - DSGVO, Import, Search, BANT)

---

### **Sprint 2.1.9 – Lead-Kollaboration & Tracking (PLANNED)**
**Zentral:** [TRIGGER_SPRINT_2_1_9.md](../../TRIGGER_SPRINT_2_1_9.md)
**Status:** 📅 PLANNED (2025-11-02 - 2025-11-04)
**Scope:** Team-Workflows & Nachvollziehbarkeit
**Aufwand:** ~12-17h (~2-3 Tage)

**🎯 KERN-DELIVERABLES:**
1. **Lead-Notizen & Kommentare** (Team-Kollaboration, 6-8h)
2. **Lead-Status-Änderungs-Historie** (Audit-Trail, 4-6h)
3. **Lead-Temperatur (Hot/Warm/Cold)** (Visuelle Priorisierung, 2-3h)

**Migrations:** 3 DB-Änderungen (Nummern: siehe `get-next-migration.sh` - Notizen, Historie, Temperatur)

---

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

Phase 4 (Progressive Profiling): Sprint 2.1.4 + 2.1.5
  Status: ✅ COMPLETE
  Result: Lead Protection + Progressive Profiling + Pre-Claim
  PRs: #123, #124, #129, #131 (alle merged)

Phase 5 (Admin-Features): Sprint 2.1.6 (IN PROGRESS)
  Status: 🔧 IN PROGRESS
  Result: Migration-API, Convert-Flow, Stop-the-Clock UI, Automated Jobs

Phase 6 (Team Management): Sprint 2.1.7 (PLANNED)
  Status: 📅 PLANNED
  Result: Lead-Transfer, RLS, Team Management, Test Infrastructure

Phase 7 (DSGVO & Import): Sprint 2.1.8 (PLANNED)
  Status: 📅 PLANNED
  Result: DSGVO-Compliance + Lead-Import + BANT

Phase 8 (Kollaboration): Sprint 2.1.9 (PLANNED)
  Status: 📅 PLANNED
  Result: Lead-Notizen, Status-Historie, Temperatur
```

## 🎯 **Für neue Claude-Instanzen**

**Start immer mit den zentralen Trigger-Dokumenten** – sie enthalten die vollständigen Sprint-Kontexte, Cross-Module-Dependencies und Entscheidungshistorie. Diese Sprint-Map ist nur ein **Navigationshelfer**, nicht die Quelle der Wahrheit.