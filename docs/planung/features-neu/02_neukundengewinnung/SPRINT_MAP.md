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
- **Dokumentation:** ADR-004, DELTA_LOG, CONTRACT_MAPPING, TEST_PLAN, SUMMARY, TRIGGER_2_1_6

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
- ✅ [PRE_CLAIM_LOGIC.md](./artefakte/SPRINT_2_1_5/PRE_CLAIM_LOGIC.md) ⭐ NEU
- ✅ [DEDUPE_POLICY.md](./artefakte/SPRINT_2_1_5/DEDUPE_POLICY.md) ⭐ NEU
- ✅ [ACTIVITY_TYPES_PROGRESS_MAPPING.md](./artefakte/SPRINT_2_1_5/ACTIVITY_TYPES_PROGRESS_MAPPING.md) ⭐ NEU

**Frontend:**
- ✅ [FRONTEND_ACCESSIBILITY.md](../../../../frontend/FRONTEND_ACCESSIBILITY.md)
- ✅ [FRONTEND_DELTA.md](./artefakte/SPRINT_2_1_5/FRONTEND_DELTA.md) ⭐ NEU
- ✅ [SUMMARY.md](./artefakte/SPRINT_2_1_5/SUMMARY.md)

**DSGVO & Compliance:**
- ✅ [DSGVO_CONSENT_SPECIFICATION.md](./artefakte/SPRINT_2_1_5/DSGVO_CONSENT_SPECIFICATION.md) ⭐ NEU

**Delta:** Scope geändert von "Matching & Review" zu "Protection & Progressive", PLAN B (Inline-First) statt V249-Artefakt, Backend/Frontend Split (PR #124/#125)

---

### **Sprint 2.1.6 – Lead Transfer & Bestandsleads-Migration (PLANNED)**
**Zentral:** [TRIGGER_SPRINT_2_1_6.md](../../TRIGGER_SPRINT_2_1_6.md)
**Status:** 📅 PLANNED (2025-10-12 - 2025-10-18)
**Scope:** Lead-Transfer, Bestandsleads-Migration (Modul 08), Lead → Kunde Convert, Stop-the-Clock UI

> **⚠️ TEST-STRATEGIE BEACHTEN!**
> Tests MÜSSEN Mocks verwenden, NICHT @QuarkusTest mit echter DB!
> Siehe: [backend/TEST_MIGRATION_PLAN.md](./backend/TEST_MIGRATION_PLAN.md)

**Geplante Features (aus 2.1.5 verschoben + NEU):**
- **Bestandsleads-Migrations-API** (Modul 08):
  - POST /api/admin/migration/leads/import (Admin-only, Dry-Run Mode PFLICHT)
  - Batch-Import mit Validierung (max. 1000 Leads/Batch)
  - Historische Datumsfelder korrekt übernehmen (registeredAt, activities)
  - countsAsProgress explizit setzen (NICHT automatisch berechnen!)
  - Duplikaten-Check + Warning-Report
  - Audit-Log für alle Import-Vorgänge
  - Re-Import-Fähigkeit bei Fehlern
- **Lead → Kunde Convert Flow:**
  - Automatische Übernahme bei Status QUALIFIED → CONVERTED
  - Alle Lead-Daten übernehmen (ZERO Doppeleingabe)
  - Lead-ID Verknüpfung in customer.original_lead_id
  - Historie vollständig erhalten
- **Stop-the-Clock UI:**
  - StopTheClockDialog Component (Manager + Admin only)
  - Pause/Resume Buttons in LeadProtectionBadge
  - Grund-Auswahl mit Audit-Log
  - Maximale Pausendauer konfigurierbar
- **Lead-Transfer zwischen Partnern:**
  - V258 lead_transfers Tabelle
  - Transfer-Request mit Begründung
  - Genehmigungsprozess (Manager/Admin)
  - 48h SLA für Entscheidung
- **Backdating Endpoint:**
  - PUT /api/leads/{id}/registered-at (Admin/Manager)
  - Validierung: nicht in Zukunft; Reason Pflicht
  - Recalc Protection-/Activity-Fristen
- **Automated Jobs:**
  - Nightly Job: Progress Warning Check (Tag 53)
  - Nightly Job: Protection Expiry (Tag 70)
  - Nightly Job: Pseudonymisierung (60 Tage ohne Progress)
- **Fuzzy-Matching & Review:**
  - Vollständiger Scoring-Algorithmus (Email, Phone, Company, Address)
  - Schwellwerte konfigurierbar (hard/soft duplicates)
  - DuplicateReviewModal (Merge/Reject/Create-New)
  - Merge-Historie mit Undo-Möglichkeit
- **Team Management (OPTIONAL):**
  - Team CRUD Operations
  - Team-Member Assignment
  - Quotenregelung für Teams
  - Team-Dashboard mit Metriken
- **Row-Level-Security (OPTIONAL):**
  - Owner kann eigene Leads sehen (lead_owner_policy)
  - Team-Mitglieder sehen Team-Leads (lead_team_policy)
  - Admin hat Vollzugriff (lead_admin_policy)

**Note:** Enthält Features aus Sprint 2.1.5 (verschoben) + Matching & Review

---

### **Sprint 2.1.7 – Lead Scoring & Mobile Optimization (PLANNED)**
**Zentral:** [TRIGGER_SPRINT_2_1_7.md](../../TRIGGER_SPRINT_2_1_7.md)
**Status:** 📅 PLANNED (2025-10-19 - 2025-10-25)
**Scope:** Lead-Scoring, Activity-Templates, Mobile-First UI, Offline-Fähigkeit, QR-Code-Scanner

**Geplante Features:**
- **Lead-Scoring Algorithmus:**
  - Lead-Score Berechnung (0-100 Punkte)
  - Faktoren: Stage, Estimated Volume, Business Type, Activity Frequency, Response Time
  - Backend: lead.score INT Feld (V259 Migration)
  - Scheduled Job: Score-Recalc täglich
  - UI: Score-Spalte mit Color-Coding, Filter, Score-Breakdown
- **Activity-Templates System:**
  - Backend: activity_templates Tabelle (V260 Migration)
  - Standard-Templates (Seeds): "Erstkontakt Küchenchef", "Sample-Box Versand", etc.
  - Frontend: ActivityTimeline → "Template verwenden" Dropdown
  - User-spezifische Templates erstellen/editieren/löschen
- **Mobile-First UI Optimierung:**
  - LeadWizard: Touch-optimiert (Button-Größen ≥ 44px)
  - LeadList: Card-View auf Mobile, Infinite Scroll, Swipe-Aktionen
  - Performance: Bundle <200KB, First Contentful Paint <1.5s (3G)
- **Offline-Fähigkeit (Progressive Web App):**
  - Service Worker für Offline-Support
  - IndexedDB für lokale Lead-Speicherung
  - Sync-Strategy: Online/Offline/Reconnect
  - UI-Indikator: "Offline-Modus aktiv" Badge
  - Conflict-Resolution: Server-Wins
- **QR-Code-Scanner Integration:**
  - QR-Code-Scanner Component (Camera-API)
  - Unterstützte Formate: vCard, meCard, Plain Text
  - Automatisches Parsing in LeadWizard Felder
  - Desktop-Fallback: File-Upload

**Deliverables:**
- LeadScoringService mit konfigurierbaren Gewichtungen
- QRCodeScanner.tsx Component
- ActivityTemplateSelector.tsx Component
- LeadScoreBreakdown.tsx Component
- OfflineIndicator.tsx Component
- Lighthouse Score: Performance >90, PWA >90

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