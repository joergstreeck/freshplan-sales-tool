---
sprint_id: "2.1.6"
title: "Lead Completion & Admin Features"
doc_type: "konzept"
status: "in_progress"
owner: "team/leads-backend"
date_start: "2025-10-12"
date_end: "2025-10-18"
modules: ["02_neukundengewinnung"]
phases:
  - phase: "Phase 1"
    branch: "feature/issue-130-testdatabuilder-refactoring"
    scope: "Issue #130 BLOCKER Fix"
    status: "complete"
    pr: "#132"
  - phase: "Phase 2"
    branch: "feature/mod02-sprint-2.1.6-admin-apis"
    scope: "Core Backend APIs (Bestandsleads-Migration, Backdating, Convert Flow)"
    status: "harmonization_complete"
    commits: ["01819eb51", "ce9206ab6", "cbf5bd95e", "f93356a0e"]
    fixes_applied: ["V262 Migration", "Duplikate-Policy", "Stop-the-Clock Fix", "RBAC Standardisierung", "Lead-Archivierung", "V263 BusinessType Harmonisierung"]
  - phase: "Phase 3"
    branch: "feature/mod02-sprint-2.1.6-nightly-jobs"
    scope: "Automated Jobs (Progress Warning, Expiry, Pseudonymisierung) + Issue #134 (Batch-Import Idempotency)"
    status: "complete"
    issues: ["#134"]
    pr: "#134"
  - phase: "Phase 4"
    branch: "feature/mod02-sprint-2.1.6-phase-4-complete"
    scope: "Lead Quality Metrics & UI Components (LeadScoringService, 4 UI-Komponenten, 48 Tests)"
    status: "complete"
    pr: "#135"
    merged: "2025-10-08"
  - phase: "Phase 5"
    branch: "feature/mod02-sprint-2.1.6-enum-migration-phase-1"
    scope: "Lead-Enums Migration (LeadSource, BusinessType, KitchenSize) - Enum-Migration Phase 1"
    status: "pending"
    effort: "~8h"
    migrations: ["V273"]
  - phase: "Phase 5+"
    branch: "feature/mod02-sprint-2.1.6-lead-contacts-refactoring"
    scope: "Lead Contacts Refactoring - Multi-Contact Support (Harmonisierung mit Customer-Modul)"
    status: "in_progress"
    effort: "~3-5h"
    migrations: ["V276", "V277"]
    architecture_decision: "ADR-007"
entry_points:
  - "features-neu/02_neukundengewinnung/_index.md"
  - "features-neu/02_neukundengewinnung/backend/_index.md"
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
  - "features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_6/SUMMARY.md"
  - "claude-work/daily-work/2025-10-05/ISSUE_130_ANALYSIS.md"
  - "claude-work/daily-work/2025-10-05/BACKEND_STATUS_SPRINT_2_1_6.md"
  - "claude-work/daily-work/2025-10-05/MUI_ACCESSIBILITY_DECISION.md"
  - "claude-work/daily-work/2025-10-05/CRITICAL_FIXES_SUMMARY.md"
  - "claude-work/daily-work/2025-10-05/2025-10-05_HANDOVER_FINAL.md"
pr_refs: ["#132", "#133", "#134", "#135"]
updated: "2025-10-08"
---

# Sprint 2.1.6 – Lead Completion & Admin Features

**📍 Navigation:** Home → Planung → Sprint 2.1.6

## 🚀 SPRINT-PHASEN ÜBERSICHT

| Phase | Branch | Scope | Status | PR |
|-------|--------|-------|--------|-----|
| **Phase 1** | `feature/issue-130-testdatabuilder-refactoring` | Issue #130 BLOCKER Fix | ✅ COMPLETE | #132 |
| **Phase 2** | `feature/mod02-sprint-2.1.6-admin-apis` | Core Backend APIs (Bestandsleads-Migration, Backdating, Convert Flow) | ✅ COMPLETE | #133 |
| **Phase 3** | `feature/mod02-sprint-2.1.6-nightly-jobs` | Automated Jobs + **Issue #134** (Idempotency) + Outbox-Pattern | ✅ COMPLETE | #134 |
| **Phase 4** | `feature/mod02-sprint-2.1.6-phase-4-complete` | Lead Quality Metrics & UI Components (LeadScoringService, 4 UI-Komponenten, 48 Tests) | ✅ COMPLETE | #135 |
| **Phase 5** | `feature/mod02-sprint-2.1.6-enum-migration-phase-1` | Lead-Enums Migration (VARCHAR + CHECK Constraint Pattern - LeadSource, BusinessType, KitchenSize) | 📋 PENDING | ~8h |
| **Phase 5+** | `feature/mod02-sprint-2.1.6-lead-contacts-refactoring` | Lead Contacts Refactoring - Multi-Contact Support (Harmonisierung mit Customer-Modul - ADR-007) | 🚧 IN PROGRESS | ~3-5h |

> **📚 WICHTIGE DOKUMENTE (entry_points - siehe YAML Header oben):**
> - **Issue #130 Analyse:** [`ISSUE_130_ANALYSIS.md`](claude-work/daily-work/2025-10-05/ISSUE_130_ANALYSIS.md) - Detaillierte Analyse + Migration Guide
> - **Backend-Status:** [`BACKEND_STATUS_SPRINT_2_1_6.md`](claude-work/daily-work/2025-10-05/BACKEND_STATUS_SPRINT_2_1_6.md) - Existierende Felder, keine neuen Migrations!
> - **MUI Accessibility:** [`MUI_ACCESSIBILITY_DECISION.md`](claude-work/daily-work/2025-10-05/MUI_ACCESSIBILITY_DECISION.md) - Warum KERN-DELIVERABLE (EU Accessibility Act)
> - **Critical Fixes:** [`CRITICAL_FIXES_SUMMARY.md`](claude-work/daily-work/2025-10-05/CRITICAL_FIXES_SUMMARY.md) - 3 Fixes (Migration-Nummern, Scope, ADR-006)
> - **Handover:** [`2025-10-05_HANDOVER_FINAL.md`](claude-work/daily-work/2025-10-05/2025-10-05_HANDOVER_FINAL.md) - Vollständiger Kontext für neuen Claude
> - **Phase 5+ Architecture:** [`LEAD_CONTACTS_ARCHITECTURE.md`](features-neu/02_neukundengewinnung/artefakte/LEAD_CONTACTS_ARCHITECTURE.md) - Multi-Contact Support Design (ADR-007)

> **⚠️ TEST-STRATEGIE BEACHTEN!**
> Tests MÜSSEN Mocks verwenden, NICHT @QuarkusTest mit echter DB!
> Siehe: [`features-neu/02_neukundengewinnung/backend/TEST_MIGRATION_PLAN.md`](features-neu/02_neukundengewinnung/backend/TEST_MIGRATION_PLAN.md)
>
> **🎯 Arbeitsanweisung – Reihenfolge**
> 1. **SPRINT_MAP des Moduls öffnen** → `features-neu/02_neukundengewinnung/SPRINT_MAP.md`
> 2. **ADR-003 RLS Design prüfen** → `shared/adr/ADR-003-rls-leads-row-level-security.md`
> 3. **Backend:** Row-Level-Security Policies implementieren
> 4. **Backend:** Transfer-Flow mit Genehmigung
> 5. **Backend:** Fuzzy-Matching & Scoring (verschoben aus 2.1.5)
> 6. **Frontend:** Transfer-UI & Review-Flow

## 🔧 GIT WORKFLOW (KRITISCH!)

**PFLICHT-REGELN für alle Sprint-Arbeiten:**

### ✅ ERLAUBT (ohne User-Freigabe):
- `git commit` - Commits erstellen wenn User darum bittet
- `git add` - Dateien stagen
- `git status` / `git diff` - Status prüfen
- Feature-Branches anlegen (`git checkout -b feature/...`)

### 🚫 VERBOTEN (ohne explizite User-Freigabe):
- **`git push`** - NIEMALS ohne User-Erlaubnis pushen!
- **PR-Erstellung** - Nur auf explizite Anforderung
- **PR-Merge** - Nur wenn User explizit zustimmt
- **Branch-Deletion** - Remote-Branches nur mit User-OK löschen

### 📋 Standard-Workflow:
1. **Feature-Branch anlegen:** `git checkout -b feature/mod02-sprint-2.1.6-description`
2. **Arbeiten & Committen:** Code schreiben, Tests validieren, `git commit`
3. **User fragen:** "Branch ist bereit. Soll ich pushen und PR erstellen?"
4. **Erst nach Freigabe:** `git push` + PR-Erstellung

**Referenz:** `/CLAUDE.md` → Sektion "🚫 GIT PUSH POLICY (KRITISCH!)"

---

## Sprint-Ziel

**PRIORITY #0:** Issue #130 Fix (TestDataBuilder CDI-Konflikt - BLOCKER) - **MUSS ZUERST!**

**Kern-Deliverables:**
1. **Bestandsleads-Migrations-API** (Modul 08) - Admin-Import für Altdaten
2. **Lead → Kunde Convert Flow** - Automatische Übernahme bei QUALIFIED → CONVERTED
3. **Stop-the-Clock UI** - Manager/Admin Dialog für Schutzfrist-Pausierung
4. **Backdating Endpoint** - Historische Datumsfelder nachträglich setzen
5. **Automated Jobs** - Nightly Tasks (Progress Warning, Expiry, Pseudonymisierung)

**Optional (ADR-006 Phase 2):**
6. **Lead-Scoring-System** - Backend + Frontend (0-100 Punkte)
7. **Lead-Status-Workflows** - UI für LEAD → PROSPECT → AKTIV Transitions
8. **Lead-Activity-Timeline** - Interaktions-Historie mit Icons
9. **MUI Dialog Accessibility Fix** - aria-hidden Warning (WCAG 2.1 Level A)

**Scope-Änderung (05.10.2025):**
- ✅ **Issue #130 hinzugefügt** (BLOCKER - 12 Tests broken, CI disabled)
- ❌ **Lead-Transfer verschoben** auf Sprint 2.1.7 (User Story 1 entfernt - zu komplex!)
- ❌ **RLS + Team Management verschoben** auf Sprint 2.1.7 (User Story 5 & 6 entfernt)
- ❌ **Fuzzy-Matching verschoben** auf Sprint 2.1.7 (User Story 4 entfernt - eigene User Story verdient)

**Scope-Erweiterung (08.10.2025) - Phase 5:**
- ✅ **Enum-Migration Phase 1** (Lead-Modul: LeadSource, BusinessType, KitchenSize)
- **Begründung:** MESSE/TELEFON-Check funktioniert NICHT ohne Enum (Pre-Claim Logic)
- **Timing:** Pre-Production = goldene Zeit (keine Daten-Migration, verhindert technische Schulden)
- **Verweis:** [ENUM_MIGRATION_STRATEGY.md](features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md)

## User Stories

### 0. Lead Stage Enum Refactoring (Issue #125) - ✅ **COMPLETE (PR #131)**
**Begründung:** Type Safety für Lead Stage - Verhindert Magic Numbers, verbessert Code-Qualität

**Akzeptanzkriterien:**
- ✅ Enum LeadStage erstellt mit 3 Values (VORMERKUNG=0, REGISTRIERUNG=1, QUALIFIZIERT=2)
- ✅ Lead.stage Typ geändert: `Short` → `LeadStage` mit `@Enumerated(EnumType.ORDINAL)`
- ✅ LeadProtectionService.canTransitionStage() nutzt Enum-Methoden (LeadStage.canTransitionTo())
- ✅ Alle Tests grün (24 Unit Tests + Integration Tests)
- ✅ JSON Serialization funktioniert (0/1/2 in API, VORMERKUNG/REGISTRIERUNG/QUALIFIZIERT in UI)
- ✅ KEINE DB-Migration erforderlich (ORDINAL nutzt 0,1,2)

**Status:** ✅ MERGED (PR #131, 05.10.2025)

**Referenzen:**
- Issue: https://github.com/joergstreeck/freshplan-sales-tool/issues/125
- PR: https://github.com/joergstreeck/freshplan-sales-tool/pull/131

### 0.5. TestDataBuilder Refactoring (Issue #130) - **🔴 BLOCKER - MUST DO FIRST**
**Begründung:** 12 Tests broken durch CDI-Konflikt zwischen Legacy und neuen Buildern - blockiert Worktree CI

**Problem-Analyse:**
- **Root Cause:** Doppelte TestDataBuilder in `src/main` und `src/test` mit CDI `@ApplicationScoped`
- **CDI-Konflikt:** Quarkus lädt beide Builder → Legacy nutzt `CustomerContactRepository` (deprecated), Neue nutzen `ContactRepository` (current)
- **Fehler:** `EntityExistsException: detached entity passed to persist: de.freshplan.domain.customer.entity.CustomerContact`
- **Impact:** 12 ContactInteractionServiceIT Tests schlagen fehl, Worktree CI Job "Test Suite Expansion" disabled

**Broken Tests (alle in ContactInteractionServiceIT):**
1. `shouldCalculateDataQualityMetricsAccurately`
2. `shouldCalculateEngagementTrends`
3. `shouldCalculateWarmthScoreWithMultipleFactors`
4. `shouldCategorizeInteractionOutcomes`
5. `shouldCreateInteractionAndUpdateContact`
6. `shouldGetInteractionsChronologically`
7. `shouldHandleCQRSModeWhenEnabled`
8. `shouldHandleConcurrentInteractionUpdates`
9. `shouldHandleInvalidContactIdGracefully`
10. `shouldHandleLowDataScenario`
11. `shouldRecordDifferentInteractionTypes`
12. `shouldTrackDataFreshnessCorrectly`

**Akzeptanzkriterien (Quick Fix):**
- [ ] Legacy Builder aus `src/main/java/de/freshplan/test/builders/` löschen
- [ ] Alle 12 Tests auf neue Builder (`src/test/java/de/freshplan/test/builders/`) umstellen
- [ ] ContactInteractionServiceIT: 12/12 Tests grün
- [ ] Worktree CI "Test Suite Expansion" Job reaktiviert (`.github/workflows/worktree-ci.yml`)
- [ ] Keine CDI `NoSuchFieldError` mehr
- [ ] Migration Guide für bestehende Tests dokumentiert

**Technische Details:**
```java
// LÖSCHEN (Legacy in src/main):
/src/main/java/de/freshplan/test/builders/ContactBuilder.java       → nutzt CustomerContactRepository
/src/main/java/de/freshplan/test/builders/CustomerBuilder.java
/src/main/java/de/freshplan/test/TestDataBuilder.java              → CDI Facade

// BEHALTEN (Neu in src/test):
/src/test/java/de/freshplan/test/builders/ContactTestDataFactory.java   → nutzt ContactRepository ✅
/src/test/java/de/freshplan/test/builders/CustomerTestDataFactory.java
/src/test/java/de/freshplan/test/builders/ContactBuilder.java           → CDI-enabled ✅

// MIGRATION-PATTERN:
// Alt (Legacy):
@Inject TestDataBuilder testData;
Customer customer = testData.customer().withCompanyName("Test GmbH").persist();

// Neu (TestDataFactory):
Customer customer = CustomerTestDataFactory.builder()
    .withCompanyName("Test GmbH")
    .buildAndPersist(customerRepository);
```

**Aufwand:** 1-2h (Low Complexity - nur Migration, keine neuen Features)

**Referenzen:**
- **Issue:** https://github.com/joergstreeck/freshplan-sales-tool/issues/130
- **Detaillierte Analyse:** `/docs/planung/claude-work/daily-work/2025-10-05/ISSUE_130_ANALYSIS.md`
  - Nicht-technische Erklärung des Problems
  - Root Cause Analyse (CDI-Konflikt)
  - Migration Guide mit 3 Pattern-Beispielen
  - Quick Fix Strategie (1-2h)
- **CI Workflow:** `.github/workflows/worktree-ci.yml` (disabled job)
- **Testing Guide:** `/docs/planung/grundlagen/TESTING_GUIDE.md` (Zeile 106-152 - Builder Pattern)

**⚠️ WICHTIG:** Dieser Fix MUSS vor allen anderen User Stories abgeschlossen werden, da sonst CI instabil bleibt!

---

## 📋 PHASE 2 REVIEW FIXES (2025-10-06)

**Kontext:** Nach Abschluss der Core Backend APIs (3 Services, 33 Tests) wurde ein externes Code-Review durchgeführt. Folgende 6 Verbesserungen wurden identifiziert und implementiert:

### Fix #1: Duplikate-Handling (Migration-Ausnahme dokumentiert)
**Problem:** Import importierte Duplikate mit `isCanonical=false` und umging DEDUPE_POLICY.
**Lösung:** Ausnahme als **MIGRATION-SPEZIFISCHE POLICY** dokumentiert:
- Duplikate werden mit **WARNING** importiert (nicht blockiert)
- `isCanonical=false` verhindert Unique Constraint Violation
- Admin muss nach Import manuell mergen
- Normale Lead-Erstellung folgt weiterhin RFC 7807 DEDUPE_POLICY

**Code:** LeadImportService.java:110-120 (Kommentar ergänzt)

### Fix #2: Idempotenz robuster (Migration V262 - Tabelle bereit)
**Problem:** SHA-256 Hash fragil bei Feld-Reihenfolge-Änderungen.
**Lösung:** `import_jobs` Tabelle für Idempotency-Key Tracking:
- `idempotency_key` (Client-provided, Header: `Idempotency-Key`)
- `request_fingerprint` (SHA-256 Fallback)
- `result_summary` (JSONB mit Import-Statistiken)
- TTL: 7 Tage (Cleanup in Phase 3)

**Migration:** V262__add_stop_the_clock_cumulative_pause_and_idempotency.sql
**Service-Implementierung:** Phase 3 (Nightly Jobs)

### Fix #4: Stop-the-Clock kumulative Pausenzeit (KRITISCHER FIX)
**Problem:** `Duration.between(clockStoppedAt, now)` zählte nur letzte Pause, nicht kumulative.
**Lösung:**
- Neues Feld: `progress_pause_total_seconds` (BIGINT, Default 0)
- Formel: `progressDeadline = registeredAt + 60d + (pause_total_seconds / 86400)d`
- Bei Resume: `pause_total_seconds += Duration.between(clockStoppedAt, now).toSeconds()`

**Migration:** V262 (Zeilen 10-23)
**Code:** Lead.java:156-158, LeadBackdatingService.java:72-81

### Fix #5: RBAC-Rollennamen standardisiert
**Problem:** Inkonsistente Rollennamen (`"admin"` vs. `"ROLE_ADMIN"`).
**Lösung:** Alle Endpoints auf `ROLE_*` Pattern umgestellt:
- `@RolesAllowed({"ROLE_ADMIN"})` (statt `{"admin"}`)
- `@RolesAllowed({"ROLE_ADMIN", "ROLE_SALES_MANAGER"})` (statt `{"ADMIN", "MANAGER"}`)

**Dateien:**
- LeadImportResource.java:25 (Import API)
- LeadResource.java:606 (Backdating)
- LeadResource.java:647 (Convert)

### Fix #6: Backdating Reason Mindestlänge (BEREITS IMPLEMENTIERT ✅)
**Status:** `@Size(min = 10)` bereits in BackdatingRequest.java:16 vorhanden.

### Fix #7: Lead-Löschung durch Archivierung (Audit-Trail)
**Problem:** `keepLeadRecord=false` löschte Lead hart → Audit-Trail verloren.
**Lösung:**
- Lead wird IMMER archiviert (`status=CONVERTED`), niemals gelöscht
- `keepLeadRecord` Parameter wird ignoriert (Log-Warnung)
- Hard-Delete nur für DSGVO-Compliance (Pseudonymisierung Job in Phase 3)

**Code:** LeadConvertService.java:152-164

**Zusammenfassung:**
- ✅ **Migration V262** erstellt (Stop-the-Clock + Idempotency)
- ✅ **Migration V263** erstellt (Lead BusinessType Harmonisierung)
- ✅ **Migration V264** erstellt (Customer BusinessType + Data Migration)
- ✅ **7 Fixes** implementiert (Code + Kommentare + Frontend SoT)
- ✅ **Tests** alle grün (21/21 in LeadImportServiceTest, LeadBackdatingServiceTest, LeadConvertServiceTest)
- ✅ **Dokumentation** aktualisiert (TRIGGER, BUSINESS_LOGIC, MP5, HARMONIZATION_COMPLETE)

**Artefakte (Phase 2):**
- ✅ [HARMONIZATION_COMPLETE.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_6/HARMONIZATION_COMPLETE.md) ⭐ NEU (BusinessType Harmonization - 264 Zeilen Complete Documentation)
- ✅ [BUSINESS_LOGIC_LEAD_ERFASSUNG.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/BUSINESS_LOGIC_LEAD_ERFASSUNG.md) (Updated: BusinessType, LeadSource, KitchenSize mit SoT Pattern)

### Fix #9: BusinessType Harmonisierung (V263 + Frontend Single Source of Truth)
**Problem:** Lead.businessType hatte 5 hardcodierte Werte im Frontend (restaurant, hotel, catering, canteen, other), Customer.industry hatte 9 Werte als Enum. Keine einheitliche Systematik, Frontend hardcodete Werte statt Backend-API zu nutzen.

**Lösung:**
- **Backend:**
  - Neues Enum: `BusinessType` (9 Werte: RESTAURANT, HOTEL, CATERING, KANTINE, GROSSHANDEL, LEH, BILDUNG, GESUNDHEIT, SONSTIGES)
  - Migration V263: Uppercase-Migration + CHECK constraint auf leads.business_type
  - Neue REST-API: `GET /api/enums/business-types` → `[{value: "RESTAURANT", label: "Restaurant"}, ...]`
  - EnumResource.java: Single Source of Truth für Dropdown-Werte
- **Frontend:**
  - Neuer Hook: `useBusinessTypes()` (React Query, 5min Cache)
  - LeadWizard.tsx: Dynamisches Laden statt Hardcoding
  - types.ts: Uppercase BusinessType values (harmonisiert mit Backend)

**Migrierte Werte:**
```sql
'restaurant' → 'RESTAURANT'
'hotel' → 'HOTEL'
'catering' → 'CATERING'
'canteen'/'kantine' → 'KANTINE'
'other' → 'SONSTIGES'
```

**Migration:** V263__add_business_type_constraint.sql
**Code:**
- Backend: `BusinessType.java`, `EnumResource.java`, `LeadImportServiceTest.java`
- Frontend: `useBusinessTypes.ts`, `LeadWizard.tsx`, `types.ts`

**Tests:** 21/21 grün (Phase 2 Services)

**Vorteile:**
- ✅ NO Hardcoding: Frontend lädt Werte von Backend
- ✅ Konsistenz: Lead + Customer nutzen gleiche Werte
- ✅ Wartbarkeit: Neue BusinessTypes nur im Backend hinzufügen
- ✅ Datenintegrität: CHECK constraint erzwingt gültige Werte

---

### 1. ~~Lead-Transfer Workflow~~ ❌ **VERSCHOBEN AUF SPRINT 2.1.7**
**Begründung:** Zu komplex für Sprint 2.1.6 - benötigt eigenen Sprint mit Team Management & RLS

**Verschoben nach:** `/docs/planung/TRIGGER_SPRINT_2_1_7.md` (User Story 1)

**Original-Scope:**
- Migration: lead_transfers Tabelle (siehe `get-next-migration.sh` in Sprint 2.1.7)
- Transfer-Request mit Begründung
- Genehmigungsprozess (Manager/Admin)
- 48h SLA für Entscheidung
- Automatische Eskalation an Admin
- Audit-Trail für alle Transfers
- Email-Benachrichtigungen

### 2. Backdating Endpoint (verschoben aus 2.1.5)
**Akzeptanzkriterien:**
- PUT /api/leads/{id}/registered-at (Admin/Manager)
- Validierung: nicht in Zukunft; Reason Pflicht
- Audit: `lead_registered_at_backdated`
- Felder bereits vorhanden: `registered_at_override_reason`, etc.
- Recalc Protection-/Activity-Fristen

### 3. Automated Jobs (verschoben aus 2.1.5)
**Akzeptanzkriterien:**
- Nightly Job: Progress Warning Check (Tag 53)
- Nightly Job: Protection Expiry (Tag 70)
- Nightly Job: Pseudonymisierung (60 Tage ohne Progress)
- Email-Benachrichtigungen
- Dashboard-Alerts

### 4. ~~Fuzzy-Matching & Review~~ ❌ **VERSCHOBEN AUF SPRINT 2.1.7**
**Begründung:** Komplexer Scoring-Algorithmus verdient eigenen Sprint mit ausreichend Zeit

**Verschoben nach:** `/docs/planung/TRIGGER_SPRINT_2_1_7.md` (User Story 2)

**Original-Scope:**
- Vollständiger Scoring-Algorithmus (Email, Phone, Company, Address)
- Schwellwerte konfigurierbar (hard/soft duplicates)
- 202 Response mit Kandidaten-Liste
- DuplicateReviewModal.vue
- Review-UI: Merge/Reject/Create-New
- Merge-Historie mit Undo-Möglichkeit

### 5. ~~Row-Level-Security (RLS) Implementation~~ ❌ **VERSCHOBEN AUF SPRINT 2.1.7**
**Begründung:** RLS + Team Management sind komplex und benötigen eigenen Sprint

**Verschoben nach:** `/docs/planung/TRIGGER_SPRINT_2_1_7.md` (User Story 3)

**Original-Scope:**
- Owner kann eigene Leads sehen (lead_owner_policy)
- Team-Mitglieder sehen Team-Leads (lead_team_policy)
- Admin hat Vollzugriff (lead_admin_policy)
- Transfer-Empfänger sieht pending Transfers
- Session-Context mit user_id und role

### 6. ~~Team Management~~ ❌ **VERSCHOBEN AUF SPRINT 2.1.7**
**Begründung:** Team-Features sind komplex und gehören thematisch zu RLS + Lead-Transfer

**Verschoben nach:** `/docs/planung/TRIGGER_SPRINT_2_1_7.md` (User Story 4)

**Original-Scope:**
- Team CRUD Operations
- Team-Member Assignment
- Quotenregelung für Teams
- Team-Dashboard mit Metriken
- Territory-Zuordnung (DE/CH)

### 7. Frontend UI Phase 2 (ADR-006 - OPTIONAL)
**Begründung:** Lead-spezifische UI-Features aufbauend auf Phase 1 (CustomersPageV2 Reuse)

**Akzeptanzkriterien:**
- [ ] **Lead-Scoring-System (0-100 Punkte):**
  - Backend: `ALTER TABLE leads ADD COLUMN lead_score INTEGER`
  - Scoring-Faktoren: Umsatzpotenzial (25%), Engagement (25%), Fit (25%), Dringlichkeit (25%)
  - Frontend: LeadScoreIndicator.tsx mit Progress Bar
  - Hook: useLeadScore.ts für Score-Berechnung
- [ ] **Lead-Status-Workflows:**
  - UI für Status-Übergänge: LEAD → PROSPECT → AKTIV
  - Frontend: LeadStatusWorkflow.tsx + LeadQualificationForm.tsx
  - Tracking: Lead-Konversions-Metriken
- [ ] **Lead-Activity-Timeline:**
  - Activity-Log: Chronologische Interaktions-Historie
  - Activity-Types: EMAIL_SENT, CALL_MADE, MEETING_SCHEDULED, QUOTE_SENT
  - Frontend: LeadActivityTimeline.tsx mit Icons
- [ ] **Lead-Protection aktivieren (Quick Win):**
  - Backend: `GET /api/leads?assignedTo={userId}`
  - Index: `CREATE INDEX idx_leads_assigned_to ON leads(assigned_to) WHERE status IN ('LEAD', 'PROSPECT')`
  - Frontend: Quick Filter "Meine Leads" (assignedTo = currentUser)

**Aufwand:** 12-16h (High Complexity - aber OPTIONAL für Sprint 2.1.6!)

**Referenzen:**
- ADR-006: Lead-Management Hybrid-Architektur (Phase 2 Features)
- Phase 1 (Sprint 2.1.5): CustomersPageV2 mit Context-Prop bereits COMPLETE

### 8. MUI Dialog Accessibility Fix (aria-hidden Focus Management) - **KERN-DELIVERABLE**
**Begründung:** WCAG 2.1 Level A Compliance ist gesetzliche Pflicht (EU Accessibility Act 2025) - Einmalig fixen, alle Dialoge profitieren

**Problem:**
```
Blocked aria-hidden on an element because its descendant retained focus.
The element is displayed on screen with 'display:block' or equivalent styles.
```

**Warum KERN-DELIVERABLE (nicht OPTIONAL)?**
- ✅ **Gesetzliche Pflicht:** EU Accessibility Act ab 2025 - WCAG 2.1 Level A ist MUSS
- ✅ **Minimaler Aufwand:** 1-2h für komplette Lösung (alle Dialoge)
- ✅ **Einmalige Investition:** Jeder neue Dialog profitiert automatisch
- ✅ **Professioneller Standard:** Keine Accessibility-Warnungen in Production
- ✅ **Sprint 2.1.6 hat Zeit:** Backend-Felder existieren bereits → Zeit für UI-Qualität

**Akzeptanzkriterien:**
- [ ] MUI Dialog Focus-Management korrekt implementiert (disableEnforceFocus=false beibehalten)
- [ ] aria-hidden Warning in Browser Console eliminiert (alle Dialoge)
- [ ] WCAG 2.1 Level A Compliance für Dialog-Focus-Management validiert
- [ ] Keine Regression bei Keyboard-Navigation (Tab, Escape, Enter)
- [ ] FocusTrap funktioniert weiterhin korrekt

**Betroffene Komponenten:**
- **LeadWizard.tsx** (MUI Dialog mit Multi-Step-Form) - PRIORITY #1
- **StopTheClockDialog.tsx** (NEU in Sprint 2.1.6) - direkt korrekt implementieren!
- **Alle anderen Dialogs** mit Focus-Management (CustomerEditDialog, etc.)

**Technische Lösung:**
- MUI `disableEnforceFocus` Option prüfen (nur bei Bedarf aktivieren)
- `disableRestoreFocus` für spezifische Dialogs konfigurieren
- `aria-hidden` korrekt auf Dialog-Overlay und Parent-Elementen setzen
- Focus-Management mit `useRef` + `useEffect` für Custom-Steuerung
- **Best Practice Pattern dokumentieren** für alle zukünftigen Dialogs

**Referenzen:**
- [MUI Dialog API](https://mui.com/material-ui/api/dialog/)
- [WCAG 2.1 Focus Management](https://www.w3.org/WAI/WCAG21/Understanding/focus-visible.html)
- [React Focus Management Best Practices](https://react-spectrum.adobe.com/react-aria/FocusScope.html)

**Aufwand:** 1-2h (Low Complexity - MUI Props-Konfiguration + Testing)

---

## 📋 PHASE 3 SCOPE (Nightly Jobs + Issue #134)

**Branch:** `feature/mod02-sprint-2.1.6-nightly-jobs`
**Geschätzter Aufwand:** 2.0 Tage (1.5 Tage Jobs + 0.3 Tage Issue #134 + 0.2 Tage Doku)
**Entscheidungsdatum:** 2025-10-06 (Session mit Claude)

### 🎯 Architektur-Entscheidungen (ADR-Style)

#### **Decision 1: Email-Integration über Outbox-Pattern**
**Context:** Jobs müssen Email-Benachrichtigungen versenden
**Decision:** Verwende `OutboxEmail` aus Modul 05 (Kommunikation)
**Rationale:**
- ✅ Modul 05 hat bereits Email-Outbox-Pattern geplant
- ✅ Jobs schreiben nur DB-Einträge (schnell, transaktionssicher)
- ✅ `EmailOutboxProcessor` sendet asynchron (Retry-Mechanismus)
**Consequences:** Jobs bleiben schlank, keine SMTP-Abhängigkeiten

#### **Decision 2: Import Jobs Archivierung statt Löschung**
**Context:** 7-Tage-TTL für `import_jobs` Tabelle
**Decision:** Status='ARCHIVED' statt DELETE
**Rationale:**
- ✅ Audit-Trail bleibt erhalten (Admin-Nachvollziehbarkeit)
- ✅ Compliance-Anforderungen (Import-Historie)
- ✅ Später: Hard-Delete nach 90 Tagen Archiv
**Consequences:** Zusätzlicher Status-Wert, separater Cleanup-Job später

#### **Decision 3: B2B-Pseudonymisierung (DSGVO Art. 4)**
**Context:** Was pseudonymisieren bei B2B-Leads?
**Decision:** Nur personenbezogene Daten (PII), Firmendaten behalten
**Pseudonymisierung:**
- ⚠️ `email` → SHA256-Hash (Duplikat-Check bleibt funktional)
- ⚠️ `phone` → NULL
- ⚠️ `contactPerson` → "ANONYMIZED"
- ⚠️ `notes` → NULL (kann PII enthalten)
**Behalten:**
- ✅ `companyName` (Analytics)
- ✅ `city` (Territory-Statistik)
- ✅ `businessType` (Branchenanalyse)
- ✅ `assignedTo` (Vertriebspartner-Stats)
- ✅ `sourceCampaign` (Campaign-ROI)
**Rationale:** DSGVO schützt natürliche Personen, nicht Firmen
**Consequences:** Analytics bleiben nutzbar, Duplikat-Check funktioniert

#### **Decision 4: Event-Publishing für Dashboard-Updates**
**Context:** Dashboard soll Echtzeit-Updates bei Job-Completion
**Decision:** CDI Events nach jedem Job
**Events:**
- `LeadProgressWarningIssuedEvent` (Dashboard-Counter)
- `LeadProtectionExpiredEvent` (Manager-Alert)
- `LeadsPseudonymizedEvent` (Compliance-Dashboard)
- `ImportJobsArchivedEvent` (Admin-Dashboard)
**Rationale:** Referenz `FollowUpAutomationService.java` (bereits implementiert)
**Consequences:** Dashboard reagiert ohne Polling, bessere UX

#### **Decision 5: Hybrid-Test-Strategie (Option C)**
**Context:** CI-Performance vs. Code-Coverage
**Decision:** 80% Mock-Tests (Service-Layer) + 20% @QuarkusTest (Integration)
**Rationale:**
- ✅ Service-Tests: Schnell (<1s), Business-Logik isoliert
- ✅ Integration-Tests: Realistisch (echte DB), Scheduler-Validierung
- ✅ CI bleibt performant, Coverage ≥85%
**Consequences:** Separate Service-Klasse (ohne @Scheduled) + Scheduler-Wrapper

#### **Decision 6: Keine Vertragsreferenzen im Code**
**Context:** Verträge können sich pro Partner unterscheiden
**Decision:** Generische Business-Begriffe, keine §-Referenzen
**Beispiel:**
- ❌ `// §2(8)c Erinnerung mit Nachfrist`
- ✅ `// 60-Day Activity Rule - Warning 7 days before expiry`
**Rationale:** Verträge sind nicht einheitlich, Paragraphen verschieben sich
**Consequences:** Contract-Mapping nur in separaten Docs (CONTRACT_MAPPING.md)

---

### Kern-Deliverables:

#### 1. Progress Warning Job (Nightly 1:00 Uhr)
**Business-Regel:** 60-Day Activity Rule - Warning 7 days before protection expires
**Vertragsbasis:** Aktivitätsstandard (60 Tage belegbarer Fortschritt)
```java
/**
 * Progress Warning Check (60-Day Activity Rule)
 *
 * Sends reminder 7 days before protection expires if no meaningful activity.
 * Contract Mapping: See docs/CONTRACT_MAPPING.md
 */
@Scheduled(cron = "0 0 1 * * ?") // 1 AM daily
@Transactional
public void checkProgressWarnings() {
  // Find: progress_deadline < NOW() + 7 days AND progress_warning_sent_at IS NULL
  // Update: progress_warning_sent_at = NOW()
  // Create: OutboxEmail entry for assigned partner
  // Publish: LeadProgressWarningIssuedEvent
}
```

#### 2. Protection Expiry Job (Nightly 2:00 Uhr)
**Business-Regel:** Automatic protection expiry after 60-day deadline
**Vertragsbasis:** Erlöschen des Lead-Schutzes nach Ablauf
```java
/**
 * Protection Expiry Check (60-Day Deadline)
 *
 * Automatically expires lead protection after deadline with 10-day grace period.
 * Contract Mapping: See docs/CONTRACT_MAPPING.md
 */
@Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
@Transactional
public void checkProtectionExpiry() {
  // Find: progress_deadline < NOW() AND stage != CONVERTED
  // Update: protection_expired = true
  // Create: OutboxEmail entry for manager notification
  // Publish: LeadProtectionExpiredEvent
}
```

#### 3. DSGVO Pseudonymisierung Job (Nightly 3:00 Uhr)
**Business-Regel:** B2B personal data pseudonymization (DSGVO Art. 4)
**Vertragsbasis:** Löschung/Pseudonymisierung nicht verfolgter Leads
```java
/**
 * DSGVO Pseudonymization (B2B Personal Data)
 *
 * Pseudonymizes personal contact data after 60 days of expired protection.
 * Keeps company data for analytics (DSGVO exemption for legal entities).
 * Contract Mapping: See docs/CONTRACT_MAPPING.md
 */
@Scheduled(cron = "0 0 3 * * ?") // 3 AM daily
@Transactional
public void pseudonymizeExpiredLeads() {
  // Find: protection_until < NOW() - 60 days AND stage != CONVERTED
  // Pseudonymize PII: email→hash, phone→null, contactPerson→"ANONYMIZED", notes→null
  // Keep: companyName, city, businessType, assignedTo, sourceCampaign
  // Update: pseudonymized_at = NOW()
  // Publish: LeadsPseudonymizedEvent
}
```

#### 4. Import Jobs Cleanup (Nightly 4:00 Uhr)
**Business-Regel:** Archive completed import jobs after 7-day TTL
```java
/**
 * Import Jobs Archival (7-Day TTL)
 *
 * Archives completed import jobs for audit trail retention.
 * Hard-delete scheduled for Sprint 2.1.7 (90-day archive retention).
 */
@Scheduled(cron = "0 0 4 * * ?") // 4 AM daily
@Transactional
public void archiveCompletedImportJobs() {
  // Find: ttl_expires_at < NOW() AND status = 'COMPLETED'
  // Update: status = 'ARCHIVED', archived_at = NOW()
  // NO DELETE (audit trail retention)
  // Publish: ImportJobsArchivedEvent
}
```

#### 4. **Issue #134: Batch-Import Idempotency** ⭐ NEU
**Zweck:** Verhindere Duplikate bei Import-Retry
**Quelle:** PR #133 Copilot/Gemini Review Feedback

**Implementierung:**
```java
// ImportJob.java (NEW Entity)
@Entity
@Table(name = "import_jobs")
public class ImportJob {
  @Id @GeneratedValue
  private Long id;

  @Column(name = "request_fingerprint", unique = true)
  private String requestFingerprint;

  @Enumerated(EnumType.STRING)
  private ImportStatus status; // PENDING, COMPLETED, FAILED

  @Column(columnDefinition = "TEXT")
  private String resultSummary; // JSON: {successCount, failureCount, errors}

  private String createdBy;
  private LocalDateTime createdAt;
}

// LeadImportService.java (UPDATED)
private boolean isDuplicateImport(String requestHash) {
  return importJobRepository
    .find("request_fingerprint = ?1 AND status = 'COMPLETED'", requestHash)
    .firstResultOptional()
    .isPresent();
}

private void recordImportAudit(LeadImportResponse response, String userId) {
  ImportJob job = new ImportJob();
  job.setRequestFingerprint(response.requestHash);
  job.setStatus(response.statistics.failureCount > 0 ? FAILED : COMPLETED);
  job.setResultSummary(toJson(response.statistics));
  job.setCreatedBy(userId);
  importJobRepository.persist(job);
}
```

**Benefits:**
- ✅ Nightly Jobs können idempotent sein (z.B. External CRM Import)
- ✅ Admin kann Import sicher wiederholen (bei Timeout/Crash)
- ✅ Audit Trail: Wer hat wann was importiert
- ✅ V262 Migration bereits deployed (Tabelle existiert)

**Aufwand:**
- ImportJob Entity: 30min
- Service Logic (isDuplicate + recordAudit): 1h
- Tests (95% Coverage): 1h
- **Total: 2.5h**

**Related:**
- Migration: V262 (bereits deployed)
- PR: #133 (Admin-APIs)
- Review: Copilot/Gemini Feedback

---

## Technische Details

### 🟢 Backend-Status-Übersicht (Existierende Features!)

**WICHTIG:** Viele Sprint 2.1.6 Features haben bereits Backend-Unterstützung in `Lead.java`!
Dieser Sprint fokussiert primär auf **API-Endpoints + Frontend UI**, NICHT auf Backend-Grundlagen.

**✅ Backend-Ready (nur API/UI fehlen):**

1. **Stop-the-Clock (User Story 3):**
```java
// Lead.java Zeile 145-154 (bereits vorhanden!)
@Column(name = "clock_stopped_at")
public LocalDateTime clockStoppedAt;

@Column(name = "stop_reason", columnDefinition = "TEXT")
public String stopReason;

@Size(max = 50)
@Column(name = "stop_approved_by")
public String stopApprovedBy;
```
→ **Nur API-Endpoint + StopTheClockDialog.tsx fehlen!**

2. **Backdating (Technische Details):**
```java
// Lead.java Zeile 119-132 (bereits vorhanden!)
@Size(max = 250)
@Column(name = "registered_at_override_reason")
public String registeredAtOverrideReason;

@Size(max = 100)
@Column(name = "registered_at_set_by")
public String registeredAtSetBy;
```
→ **Nur PUT /api/leads/{id}/registered-at Endpoint fehlt!**

3. **Automated Jobs (Technische Details):**
```java
// Lead.java Zeile 174-178 (bereits vorhanden!)
@Column(name = "progress_warning_sent_at")
public LocalDateTime progressWarningSentAt;

@Column(name = "progress_deadline")
public LocalDateTime progressDeadline;
```
→ **Nur @Scheduled Jobs fehlen!**

4. **Lead Stage (Sprint 2.1.5 - Issue #125):**
```java
// Lead.java Zeile 138-143 (bereits deployed - V255!)
@Enumerated(EnumType.ORDINAL)
@Column(name = "stage", nullable = false)
public LeadStage stage = LeadStage.VORMERKUNG;
```
→ **✅ COMPLETE (PR #131 merged)**

**❌ Backend-Felder fehlen (NEUE Migrations nötig in 2.1.7!):**

- `lead_transfers` Tabelle (User Story 1 - verschoben auf 2.1.7)
- `teams` + `team_members` Tabellen (User Story 6 - verschoben auf 2.1.7)
- RLS Policies (User Story 5 - verschoben auf 2.1.7)

**Migration-Check:**
```bash
# Nächste verfügbare Migration-Nummer:
./scripts/get-next-migration.sh
# Output: V260 (für Sprint 2.1.7!)
```

**Konsequenz für Sprint 2.1.6:**
- ✅ **KEINE** neuen DB-Migrations erforderlich (Backend-Felder existieren bereits!)
- ✅ Fokus auf API-Layer (Resource-Classes) + Frontend (React Components)
- ✅ Schnellere Umsetzung möglich (weniger DB-Arbeit)

---

### Backdating (aus 2.1.5):

**Backend-Status:** ✅ **Backend-Ready!**
- Felder `registeredAtOverrideReason`, `registeredAtSetBy` existieren bereits in `Lead.java`
- **Nur noch erforderlich:** PUT /api/leads/{id}/registered-at Endpoint

```java
// PUT /api/leads/{id}/registered-at
@RolesAllowed({"admin", "manager"})
public void updateRegisteredAt(Long id, BackdatingRequest request) {
  // Validate: not in future
  // Update: registered_at + override_reason + set_by = current user
  // Recalc: protection_until, progress_deadline (basierend auf neuem registered_at)
  // Audit: lead_registered_at_backdated
}
```

### 2. Lead → Kunde Convert Flow (NEU aus 2.1.5)
**Akzeptanzkriterien:**
- Automatische Übernahme bei Status QUALIFIED → CONVERTED
- Alle Lead-Daten übernehmen (ZERO Doppeleingabe)
- Lead-ID Verknüpfung in customer.original_lead_id
- Historie vollständig erhalten (Activities, Protection-Daten)
- Validation: nur QUALIFIED Leads können konvertiert werden

**API-Spec:**
```json
POST /api/leads/{id}/convert
Authorization: Bearer <token>

Response 200:
{
  "customerId": "uuid-customer-123",
  "leadId": "uuid-lead-456",
  "message": "Lead successfully converted to customer"
}
```

### 3. Stop-the-Clock UI (NEU aus 2.1.5)
**Akzeptanzkriterien:**
- StopTheClockDialog Component (Manager + Admin only)
- Pause/Resume Buttons in LeadProtectionBadge
- Grund-Auswahl: "FreshFoodz Verzögerung", "Kunde im Urlaub", "Andere"
- Audit-Log für jeden Stop/Resume Event
- Maximale Pausendauer konfigurierbar (Default: 30 Tage)

**Backend-Status:** ✅ **Backend-Ready!**
- Felder `clockStoppedAt`, `stopReason`, `stopApprovedBy` existieren bereits in `Lead.java`
- **Nur noch erforderlich:** PUT /api/leads/{id}/stop-clock Endpoint + Frontend UI

**Frontend Components:**
- `StopTheClockDialog.tsx` - Pause/Resume mit Grund
- `LeadProtectionBadge.tsx` - Pause/Resume Buttons ergänzen

**API-Spec:**
```java
// PUT /api/leads/{id}/stop-clock
@RolesAllowed({"admin", "manager"})
public void stopClock(Long id, StopClockRequest request) {
  // Validate: request.reason not empty
  // Update: clock_stopped_at = NOW(), stop_reason, stop_approved_by = current user
  // Recalc: protection_until += (NOW() - clock_stopped_at) when resume
  // Audit: lead_clock_stopped
}

// PUT /api/leads/{id}/resume-clock
@RolesAllowed({"admin", "manager"})
public void resumeClock(Long id) {
  // Calculate pause duration: NOW() - clock_stopped_at
  // Update: protection_until += pause_duration
  // Clear: clock_stopped_at = NULL, stop_reason = NULL
  // Audit: lead_clock_resumed
}
```

### 4. Pre-Claim UI-Erweiterungen (OPTIONAL aus 2.1.5)
**Akzeptanzkriterien:**
- Quick-Action "Erstkontakt nachtragen" Button für Pre-Claim Leads
- Pre-Claim Filter in IntelligentFilterBar
- Lead Status-Labels Frontend (REGISTERED → "Vormerkung", ACTIVE → "Aktiv")
- Lead Action-Buttons (Löschen/Bearbeiten) in CustomerTable

**Frontend Components:**
- `AddFirstContactDialog.tsx` - Quick-Action für Erstkontakt-Nacherfassung
- `IntelligentFilterBar.tsx` - Pre-Claim Filter ergänzen

### 5. Lead Detail-Seite (OPTIONAL aus 2.1.5)
**Akzeptanzkriterien:**
- Lead Detail-Route `/leads/:id`
- Lead-Informationen anzeigen (Company, Contact, Activities, Protection)
- Navigation von CustomerTable Lead-Klick
- Edit-Modus für Lead-Daten
- Activity-Timeline Integration

**Frontend Components:**
- `LeadDetailPage.tsx` - Detail-Ansicht für Leads
- `LeadEditDialog.tsx` - Edit-Modus

### Automated Jobs (Backend-only, UI in 2.1.7):

**Backend-Status:** ✅ **Backend teilweise Ready!**
- Feld `progressWarningSentAt` existiert bereits in `Lead.java`
- Feld `progressDeadline` existiert bereits in `Lead.java`
- **Nur noch erforderlich:** 3 @Scheduled Jobs implementieren

```java
// Job 1: Progress Warning (Tag 53)
@Scheduled(cron = "0 0 1 * * ?")  // 1 AM daily
void checkProgressWarnings() {
  // Find leads: progress_deadline < NOW() + 7 days AND progress_warning_sent_at IS NULL
  // Set: progress_warning_sent_at = NOW()
  // Send: Email notification an assigned_to Vertriebsmitarbeiter
}

// Job 2: Protection Expiry (Tag 60)
@Scheduled(cron = "0 0 2 * * ?")  // 2 AM daily
void checkProtectionExpiry() {
  // Find leads: protection_until < NOW() AND stage != CONVERTED
  // Update: protection_expired = true
  // Send: Email notification an Manager
}

// Job 3: DSGVO Pseudonymisierung (Tag 60)
@Scheduled(cron = "0 0 3 * * ?")  // 3 AM daily
void pseudonymizeExpiredLeads() {
  // Find leads: protection_until < NOW() - 60 days AND stage != CONVERTED
  // Pseudonymize: company_name = "DSGVO-gelöscht", notes = NULL, etc.
  // Update: pseudonymized_at = NOW()
}
```

### 6. Lead-Transfer & RLS (Verschoben auf Sprint 2.1.7)
**Begründung:** Team-Management und RLS sind komplex und benötigen eigenen Sprint.

**Verschobene Features:**
- Transfer API (POST /leads/{id}/transfer, Genehmigungsprozess)
- Row-Level-Security Policies (owner_policy, team_policy, admin_policy)
- Team Management CRUD
- Fuzzy-Matching & Duplicate Review
- lead_transfers Tabelle (Migration: siehe `get-next-migration.sh`)

**Cross-Module Dependency:**
- **Modul 00 Sicherheit:** ADR-003 RLS Design → Sprint 2.1.7
- **Modul 00 Betrieb:** RLS Performance Monitoring → Sprint 2.1.7

## Definition of Done (Sprint 2.1.6)

**PRIORITY #0 - BLOCKER (MUST DO FIRST!):**
- [x] **Issue #130 Fix - TestDataBuilder Refactoring** ✅ COMPLETE (PR #132 merged)
  - [x] Legacy Builder aus `src/main/java/de/freshplan/test/builders/` gelöscht
  - [x] 12 Tests in ContactInteractionServiceIT grün
  - [x] Worktree CI "Test Suite Expansion" Job reaktiviert
  - [x] Migration Guide dokumentiert

**Phase 2 - Core Backend APIs (Branch: feature/mod02-sprint-2.1.6-admin-apis) ✅ COMPLETE:**
- [x] **Bestandsleads-Migrations-API funktionsfähig** ✅ COMPLETE (Commits: 01819eb, ce9206a)
  - [x] LeadImportService (297 LOC) + LeadImportResource implementiert
  - [x] POST /api/admin/migration/leads/import (Admin-only, Batch bis 1000)
  - [x] 14 Tests (8 Service + 6 REST) ✅ PASSED
  - [x] Dry-Run Mode, Validation, Duplicate-Check (isCanonical=false), SHA-256 Idempotenz
- [x] **Lead → Kunde Convert Flow End-to-End** ✅ COMPLETE + Field Harmonization
  - [x] LeadConvertService (204 LOC) mit vollständiger Daten-Harmonisierung
  - [x] POST /api/leads/{id}/convert (All roles)
  - [x] Customer + Location + Address + Contact Mapping
  - [x] Java Locale Country Code Mapping (DE → DEU, 200+ Länder, 0 Wartung)
  - [x] Migration V261: customer.original_lead_id (Soft Reference, Partial Index)
  - [x] 6 Service Tests ✅ PASSED (inkl. Location/Address/Contact Validation)
- [x] **Backdating Endpoint** ✅ COMPLETE
  - [x] LeadBackdatingService (107 LOC) + PUT /api/leads/{id}/registered-at
  - [x] Admin/Manager RBAC
  - [x] Protection/Progress Deadline Recalculation + Stop-the-Clock Integration
  - [x] 13 Tests (7 Service + 6 REST) ✅ PASSED
- [x] **Backend Tests ≥80% Coverage** ✅ 33/33 Tests passing (100% success rate)
- [x] **Dokumentation aktualisiert:**
  - [x] BUSINESS_LOGIC_LEAD_ERFASSUNG.md Section 11 (Bestandsleads-Migration)
  - [x] CRM_COMPLETE_MASTER_PLAN_V5.md (Latest Update mit Commits)
  - [x] CRM_AI_CONTEXT_SCHNELL.md (Architecture Flags + Sprint 2.1.6 Section)

**Phase 3 - Automated Jobs (Branch: feature/mod02-sprint-2.1.6-nightly-jobs) - ✅ COMPLETE (PR #134):**
- [x] **@Scheduled Progress Warning Job** (Tag 53 - 7 Tage vor Frist) ✅
- [x] **@Scheduled Protection Expiry Job** (Tag 60 - Schutzfrist abgelaufen) ✅
- [x] **@Scheduled DSGVO Pseudonymisierung Job** (60 Tage ohne Progress) ✅
- [x] **@Scheduled Import Jobs Archival** (7-Day TTL) ✅
- [x] **Outbox-Pattern für Email-Benachrichtigungen** (ADR-001) ✅
- [x] **Issue #134: Batch Import Idempotency** (SHA-256 Fingerprinting) ✅
- [x] **Integration Tests** (7 Tests, 5 GREEN, 2 DISABLED) ✅
- [x] **4 Migrations** (V265-V268) ✅

**📋 Artefakte:**
- [AUTOMATED_JOBS_SPECIFICATION.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_6/AUTOMATED_JOBS_SPECIFICATION.md)
- [PHASE_3_GAP_ANALYSIS.md](../backend/src/test/java/de/freshplan/modules/leads/service/PHASE_3_GAP_ANALYSIS.md)
- [TECHNICAL_DEBT_ANALYSIS.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_6/TECHNICAL_DEBT_ANALYSIS.md)

**Phase 4 - Lead Quality Metrics & UI Components (Branch: feature/mod02-sprint-2.1.6-phase-4-complete) - ✅ COMPLETE (PR #135, merged 08.10.2025):**
- [x] **LeadScoringService** (264 LOC, 19 Tests) - 4-Faktoren-Berechnung: Umsatzpotenzial (25%), Engagement (25%), Fit (25%), Dringlichkeit (25%)
- [x] **Lead.getProtectionUntil() Helper** - Single Source of Truth (refactored 5 Dateien)
- [x] **4 neue UI-Komponenten** (~1100 LOC, 48 Tests):
  - StopTheClockDialog.tsx (217 LOC, 12 Tests) - Admin/Manager Stop-the-Clock mit RBAC
  - LeadScoreIndicator.tsx (121 LOC) - 0-100 Score mit Farbcodierung (#94C456)
  - LeadActivityTimeline.tsx (213 LOC, 20 Tests) - Chronologische Historie
  - LeadStatusWorkflow.tsx (123 LOC) - Status-Stepper (REGISTERED → INTERESSENT → ACTIVE)
- [x] **Bug Fixes** - 3 Produktionsbugs gefunden & gefixt (RBAC, German labels, DTO-Mapping)
- [x] **Code Quality** - Gemini Code-Review: 4 Refactorings (DRY, Timestamps, Formatierung)
- [x] **Migrations** - V269 (lead_score), V270 (outbox_emails.failed_at), V271 (lead_score NOT NULL DEFAULT 0)
- [x] **CI Status** - 29/29 Checks passed (Backend, Frontend, E2E, Security, Performance)

**📋 Artefakte:**
- Testing Guide: [docs/planung/grundlagen/testing_guide.md](grundlagen/testing_guide.md)
- PR #135: https://github.com/joergstreeck/freshplan-sales-tool/pull/135

**Phase 5 - Enum-Migration Phase 1 (Lead-Modul) - 📋 PENDING:**

**🎯 Kern-Deliverables (8h total):**

**1. LeadSource Enum (2h) - VARCHAR + CHECK Pattern**
- [ ] Backend: `LeadSource` Enum mit 6 Werten (MESSE, EMPFEHLUNG, TELEFON, WEB_FORMULAR, PARTNER, SONSTIGES)
- [ ] DB-Migration V273: **ALTER TABLE leads ADD CONSTRAINT chk_lead_source** (NICHT CREATE TYPE!)
- [ ] B-Tree Index: `CREATE INDEX idx_leads_source ON leads(source)` für Performance
- [ ] Business-Logic: `LeadSource.requiresFirstContact()` für Pre-Claim Logic
- [ ] EnumResource: GET /api/enums/lead-sources Endpoint
- [ ] Frontend: `useLeadSources()` Hook + LeadWizard Integration
- [ ] Tests: ≥85% Coverage (Backend + Frontend)

**2. BusinessType Enum (3h) - VARCHAR + CHECK Pattern**
- [ ] Backend: `BusinessType` Enum mit 9 Werten (RESTAURANT, HOTEL, CATERING, KANTINE, GROSSHANDEL, LEH, BILDUNG, GESUNDHEIT, SONSTIGES)
- [ ] DB-Migration V273: **ALTER TABLE leads ADD CONSTRAINT chk_lead_business_type** (NICHT CREATE TYPE!)
- [ ] B-Tree Index: `CREATE INDEX idx_leads_business_type ON leads(business_type)` für Performance
- [ ] Daten-Migration: lowercase → UPPERCASE (restaurant → RESTAURANT, etc.)
- [ ] EnumResource: GET /api/enums/business-types Endpoint
- [ ] Frontend: `useBusinessTypes()` Hook + LeadWizard Integration
- [ ] Tests: ≥85% Coverage (Backend + Frontend)

**3. KitchenSize Enum (2h) - VARCHAR + CHECK Pattern**
- [ ] Backend: `KitchenSize` Enum mit 4 Werten (KLEIN, MITTEL, GROSS, SEHR_GROSS)
- [ ] DB-Migration V273: **ALTER TABLE leads ADD CONSTRAINT chk_lead_kitchen_size** (NICHT CREATE TYPE!)
- [ ] B-Tree Index: `CREATE INDEX idx_leads_kitchen_size ON leads(kitchen_size)` für Performance
- [ ] EnumResource: GET /api/enums/kitchen-sizes Endpoint
- [ ] Frontend: `useKitchenSizes()` Hook + LeadWizard Integration
- [ ] Tests: ≥85% Coverage (Backend + Frontend)

**4. Frontend Single Source of Truth (1h)**
- [ ] React Query Hooks mit 5min Cache (useLeadSources, useBusinessTypes, useKitchenSizes)
- [ ] LeadWizard: Dynamische Dropdowns ohne Hardcoding
- [ ] Pre-Claim Logic: MESSE/TELEFON → Erstkontakt PFLICHT (Enum-basiert)
- [ ] Tests: MSW-basierte Integration Tests für alle 3 Enums

**Strategische Begründung:**
- ✅ **MESSE/TELEFON-Check funktioniert** (Pre-Claim Logic erfordert `source.requiresFirstContact()`)
- ✅ **Performance ~9x schneller als String-LIKE** (B-Tree Index-Nutzung, nicht ENUM Type!)
- ✅ **Type-Safety** (Compiler-Validierung statt Runtime-Errors)
- ✅ **Pre-Production Timing** (keine Daten-Migration, Clean Slate, verhindert technische Schulden)
- ✅ **JPA-Standard-Kompatibilität** (`@Enumerated(STRING)` funktioniert direkt, kein Custom Converter)
- ✅ **Schema-Evolution einfach** (CHECK Constraint ändern = 2 Zeilen SQL, nicht ALTER TYPE komplex)

**Verweis:** [ENUM_MIGRATION_STRATEGY.md](features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md) - Vollständiger 3-Phasen-Plan

---

**Phase 5+ - Lead Contacts Refactoring (Multi-Contact Support) - 🚧 IN PROGRESS:**

**🎯 Kern-Deliverables (3-5h total):**

**Architektur-Entscheidung (ADR-007 - OPTION C: 100% Parity):**
- ✅ **Vollständige Harmonisierung** - ALLE Felder von `customer_contacts` übernommen
- ✅ **Multi-Contact Support** - N:1 Beziehung (mehrere Kontakte pro Lead)
- ✅ **Strukturierte Daten** - `firstName` + `lastName` getrennt (statt flat `contact_person`)
- ✅ **CRM Intelligence Ready** - warmth_score, data_quality_score, relationship data (birthday, hobbies, etc.)
- ✅ **Backward Compatibility** - Trigger synchronisiert `lead_contacts.primary` → `leads.contact_person`
- ✅ **Pre-Production Timing** - Noch keine Produktionsdaten → Clean Migration ohne Legacy-Komplexität
- ✅ **Future-Proof** - Alle Customer-Features funktionieren sofort für Leads (Same Code, Same Tools)

**1. Database Migrations (1h) - V276 + V277 (OPTION C: Full Parity)**
- [ ] V276: `lead_contacts` Tabelle erstellen - **ALLE customer_contacts Felder:**
  - Basic Info: salutation, title, first_name, last_name, position, decision_level
  - Contact Info: email, phone, mobile
  - Relationship Data: birthday, hobbies, family_status, children_count, personal_notes
  - Intelligence Data: warmth_score, warmth_confidence, last_interaction_date, interaction_count
  - Data Quality: data_quality_score, data_quality_recommendations
  - Flags: is_primary, is_active, is_decision_maker, is_deleted
- [ ] V276: Indizes (idx_lead_contacts_lead_id, idx_lead_contacts_primary, idx_lead_contacts_active, idx_lead_contacts_email, idx_lead_contacts_deleted)
- [ ] V276: CHECK Constraints (email_or_phone_or_mobile, names_not_empty)
- [ ] V276: Trigger (updated_at auto-update)
- [ ] V276: Daten-Migration (`contact_person` → `first_name` + `last_name` splitten, email/phone migrieren)
- [ ] V276: Deprecate `leads.contact_person/email/phone` (COMMENT ON COLUMN - wird in V280 entfernt)
- [ ] V277: Backward Compatibility Trigger (`lead_contacts.primary` → `leads.contact_person/email/phone` sync)
- [ ] V277: UNIQUE Constraint (nur 1 primary contact per lead)

**2. Backend - LeadContact Entity + Repository (1h) (OPTION C: Full Parity)**
- [ ] Entity: `LeadContact` mit **ALLEN customer_contacts Feldern** (salutation, title, decision_level, mobile, birthday, hobbies, family_status, warmth_score, data_quality_score, etc.)
- [ ] Entity: Builder Pattern für einfache Konstruktion
- [ ] Entity: Business Methods (getFullName(), getDisplayName() wie Contact.java)
- [ ] Repository: `LeadContact extends PanacheEntityBase`
- [ ] Lead Entity: `@OneToMany List<LeadContact> contacts`
- [ ] Lead Entity: `getPrimaryContact()` Helper-Methode
- [ ] Lead Entity: `addContact()` / `removeContact()` Business Logic (mit Auto-Primary wenn erster Contact)

**3. Backend - API Refactoring (1h)**
- [ ] DTO: `LeadCreateRequest.ContactData` (nested DTO für strukturierte Contacts)
- [ ] DTO: `LeadCreateRequest.contacts: List<ContactData>` (ersetzt flat contactPerson)
- [ ] DTO: Backward Compatibility (deprecated contactPerson/email/phone fields mit @Deprecated)
- [ ] Resource: `LeadResource.createLead()` - contacts[] statt contactPerson verarbeiten
- [ ] DTO: `LeadDTO.contacts: List<ContactDTO>` (Response enthält alle Kontakte)

**4. Frontend - TypeScript Types + LeadWizard (0.5h)**
- [ ] Types: `LeadContact` Type (id, firstName, lastName, email, phone, isPrimary)
- [ ] Types: `Lead.contacts: LeadContact[]` (ersetzt flat contact fields)
- [ ] Types: `getPrimaryContact()` Helper-Funktion
- [ ] LeadWizard: `contacts[]` senden statt flat contactPerson
- [ ] LeadList: `getPrimaryContact()` für Anzeige nutzen

**5. Tests (0.5-1h)**
- [ ] Backend: `LeadContactTest` (CRUD, Primary Constraint, Builder Pattern)
- [ ] Backend: `LeadResourceTest` (createLead mit contacts[], getPrimaryContact)
- [ ] Backend: Migration Tests (V276 Daten-Migration korrekt?)
- [ ] Frontend: LeadWizard Integration Tests (contacts[] API-Call)
- [ ] Coverage: ≥85% für neue LeadContact Entity

**Strategische Begründung:**
- ✅ **Konsistenz** - Lead + Customer haben gleiche Contact-Architektur
- ✅ **Erweiterbarkeit** - Position, decisionLevel, isPrimary später einfach hinzufügbar
- ✅ **User-Feedback** - "weitere Kontakte können nicht nacherfasst werden" wird gelöst
- ✅ **Frontend-Reality** - LeadWizard sendet bereits `{ firstName, lastName }` → Backend muss das verstehen
- ✅ **Type-Safety** - Strukturierte Daten statt String-Konkatenation

**Verweis:** [LEAD_CONTACTS_ARCHITECTURE.md](features-neu/02_neukundengewinnung/artefakte/LEAD_CONTACTS_ARCHITECTURE.md) - Vollständiges Architektur-Design (ADR-007)

---

**Dokumentation:**
- [x] **Convert-Flow dokumentiert** ✅ (BUSINESS_LOGIC_LEAD_ERFASSUNG.md Section 11)
- [x] **Lead Contacts Architecture** ✅ (LEAD_CONTACTS_ARCHITECTURE.md - ADR-007)
- [ ] **Migration-API Runbook** (Modul 08, Betrieb) - Phase 4
- [ ] **Stop-the-Clock RBAC Policy** (Modul 00 Sicherheit) - Phase 4

## Risiken & Mitigation

**Sprint 2.1.6 spezifisch:**
- **Issue #130 Regression:** Wenn alte Tests nicht migriert werden → CI bleibt broken
  - **Mitigation:** Migration Guide + Code Review vor Merge
- **Migration-API Datenverlust:** Falsche Historische Datumsfelder → Protection falsch berechnet
  - **Mitigation:** Dry-Run PFLICHT, Audit-Log für alle Imports, Re-Import-Fähigkeit
- **Convert-Flow Race-Conditions:** Parallele Conversions → Dateninkonsistenz
  - **Mitigation:** Optimistic Locking + Validation (nur QUALIFIED Leads)

**Verschoben auf Sprint 2.1.7:**
- ~~RLS Performance~~ → Sprint 2.1.7 (Index-Optimierung)
- ~~Policy-Konflikte~~ → Sprint 2.1.7 (RLS Test-Suite)
- ~~Transfer-Deadlocks~~ → Sprint 2.1.7 (Pessimistic Locking)
- ~~False Positives~~ → Sprint 2.1.7 (Matching-Schwellen)

## Abhängigkeiten

- Sprint 2.1.4 (Normalisierung) muss abgeschlossen sein
- Sprint 2.1.5 (Protection) sollte parallel laufen können
- PostgreSQL 14+ für RLS Features
- Team-Tabellen müssen existieren

## Test Strategy

```java
@QuarkusTest
@TestTransaction
@WithRLS  // Custom Annotation
class LeadTransferRLSTest {

    @Test
    @AsUser("partner-a")
    void cannotSeeOtherPartnersLeads() {
        // Partner A creates lead
        // Partner B queries: 0 results
    }

    @Test
    @AsUser("team-member")
    void canSeeTeamLeads() {
        // Team lead with visibility='team'
        // Member sees it
    }

    @Test
    @AsAdmin
    void adminSeesEverything() {
        // 10 leads, different owners
        // Admin sees all 10
    }
}
```

## Monitoring & KPIs

- **Transfer Approval Time:** Ziel <24h average
- **RLS Query Performance:** P95 <50ms overhead
- **Matching Accuracy:** >95% precision, >90% recall
- **Team Quota Utilization:** Dashboard-Widget

## Next Sprint Preview

Sprint 2.2: Kundenmanagement - Field-based Customer Architecture mit Contact-Hierarchie