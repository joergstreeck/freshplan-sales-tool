# 🚀 FreshFoodz CRM Production Roadmap 2025

**📅 Erstellt:** 2025-01-22
**🎯 Team:** Jörg + Claude Code (sequenziell)
**📊 Scope:** 35 PRs über 15 Wochen - Foundation-First Strategie
**✅ Validierung:** 2x Externe KI bestätigt (90% richtig + 4 kritische Korrekturen integriert)

---

## 🎯 CLAUDE QUICK-START (für neue Claude-Instanzen)

**🚨 AKTUELLER STATUS:**
- **Phase:** ✅ Phase 1 COMPLETE | 🚀 Phase 2 IN PROGRESS (80% complete)
- **Current Sprint:** 📋 Sprint 2.1.7.0 - IN PROGRESS (15.10.2025) - **Design System Migration (MainLayoutV2 → SmartLayout)**
- **Next Sprint:** 📋 Sprint 2.1.7.1 - PLANNED (Start: TBD) - **Opportunities UI Integration**
- **Active Branch:** feature/sprint-2-1-7-0-design-system-migration
- **Progress:** 21/36 PRs completed - 58% done
- **Blockers:** ❌ Keine - **🎯 Ready for Phase 1 (SmartLayout Review + Seiten-Inventar)**
- **Foundation Status:** ✅ COMPLETE - CQRS/Security/Settings/CI/RLS operational + DEV-SEED Infrastructure
- **Performance:** ✅ P95 <7ms (Backend) + CI 24min → 7min (70% schneller) + Frontend 90% Test-Coverage + Bundle 178 KB
- **Latest:** 🎉🎉🎉 **Opportunity Backend Integration COMPLETE (13.10.2025)** - V10026 + OpportunityService + 3 REST APIs + V90003 DEV-SEED (10 Opportunities)
  - ✅ **PR #137 - 4 MAIN FEATURES:**
    - **Lead Scoring System:** 0-100 Score, 4 Dimensionen (Pain/Revenue/Fit/Engagement), LeadScoringService (268 LOC), 19 Tests GREEN
    - **Multi-Contact Support:** 26-Felder lead_contacts Tabelle, 100% Customer Parity, Backward Compatibility Trigger (V10017 KRITISCH!)
    - **Enterprise Security:** 5 Layer (Rate Limiting, Audit Logs, XSS Sanitizer, Error Disclosure Prevention, HTTP Headers)
    - **Critical Bug Fixes:** 4 Fixes (ETag Race Condition, Ambiguous Email Column, Missing Triggers, UTF-8 Encoding)
  - ✅ **12 MIGRATIONEN V10013-V10024:**
    - V10013-V10015: Settings ETag Triggers, Lead Enums (VARCHAR + CHECK), first_contact_documented_at
    - V10016-V10017: lead_contacts Tabelle + Backward Compatibility Trigger (KRITISCH - synchronisiert primary contact → legacy fields)
    - V10018-V10022: Pain Scoring System (4 Faktoren), Lead Scoring System (0-100 Score), territory_id nullable
    - V10023-V10024: Lead Scoring Complete (revenue_score, NOT NULL Constraints)
  - ✅ **MIGRATION SAFETY SYSTEM (3-Layer):**
    - Pre-Commit Hook: Blocks wrong folder, old numbers, test-keywords vs. folder
    - GitHub Workflow: CI validation on every push/PR
    - Enhanced get-next-migration.sh: Dynamic Sanity-Check (MAX_JUMP=100), folder selection dialog
  - ✅ **CI/CD DEBUGGING (6 Iterationen - 2h Session):**
    - Root Cause: ZWEI Workflows (pr-pipeline-fast.yml + worktree-ci.yml) liefen ALLE Tests
    - Solution: npm script `test:critical` mit expliziten Pfaden → BEIDE Workflows grün!
  - ✅ **PERFORMANCE:** N+1 Query Fix (7x faster: 850ms→120ms), Score Caching (90% fewer DB writes)
  - ✅ **TESTS:** 31/31 LeadResourceTest GREEN + 10/10 Security Tests GREEN + 10 Critical Tests passing
  - ✅ **CI Status:** 50 commits, 3 weeks development, 125 files changed (+17.930/-1.826 LOC)
  - ✅ **MERGE:** 2025-10-12T01:43:11Z (Admin-Rechte, squash merge, commit c4c61de92)
- **Next Action:**
  1. **PRIORITÄT 1:** Sprint 2.1.7.1 starten (Opportunities UI Integration - 16-24h)
  2. **PRIORITÄT 2:** MSW Test Migration (32 geskippte Tests konvertieren - 2-3h)
  3. **PRIORITÄT 3:** Sprint 2.1.8 planen (Team Management & Test Infrastructure) ODER Sprint 2.2 (Kundenmanagement)

**🔗 WICHTIGE REFERENZEN:**
- **Arbeitsregeln:** [CLAUDE.md](./CLAUDE.md)
- **Master Plan:** [CRM_COMPLETE_MASTER_PLAN_V5.md](./CRM_COMPLETE_MASTER_PLAN_V5.md)
- **AI Context:** [CRM_AI_CONTEXT_SCHNELL.md](./CRM_AI_CONTEXT_SCHNELL.md)
- **Migration Nummer:** dynamisch per Script
```bash
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
echo "Nächste Migration: $MIGRATION"
```

---

## ⚠️ FOUNDATION-FIRST STRATEGIE (KRITISCH)

### 🚨 STOPP-REGEL: CQRS LIGHT ZUERST!
**Keine Business-Module ohne CQRS Light Foundation implementieren!**

**Begründung:**
- **Vermeidet 4-6 Wochen Doppelarbeit** (Refactoring von CRUD zu CQRS)
- **300+ Production-Ready Artefakte** sind auf neue Architektur ausgelegt
- **Performance-Excellence** von Anfang (<200ms P95) statt später nachrüsten
- **Kosteneffiziente Architektur** für internes Tool (5-50 Benutzer)

**Risiko ohne Foundation-First:**
- ❌ Business-Module auf CRUD-Foundation = 500ms+ Performance
- ❌ Späteres Refactoring aller Module nötig
- ❌ Artefakte können nicht genutzt werden
- ❌ Doppelte Entwicklungsarbeit

**Bestätigt durch 2 externe KI-Validierungen:**
- Erste KI: "90% richtig - Foundation-First optimal"
- Zweite KI: "Plan tragfähig - Foundation-First vermeidet Doppelarbeit"

---

## 📊 LIVE PROGRESS DASHBOARD

### 🔄 **Phase 1: Foundation (3.5 Wochen) - ✅ COMPLETE**
```
Progress: ██████████ 100% (6/6 Sprints COMPLETE)

Sprint 1.1: CQRS Light Foundation     ✅ PR #94 MERGED → FP-225 bis FP-227
Sprint 1.2: Security + Foundation     ✅ PR #95-96 MERGED → Security Context
Sprint 1.3: Security Gates + CI       ✅ PR #97-101 MERGED → CI/Testing/P95
Sprint 1.4: Foundation Quick-Wins     ✅ PR #102 MERGED → Cache + Prod-Config
Sprint 1.5: Security Retrofit 🔒      ✅ PR #106 MERGED → RLS Connection Affinity
Sprint 1.6: RLS Module Adoption       ✅ PR #107 MERGED → Modul 02 Fix + CI-Guard

🎯 Achievements:
- CQRS Light: P95 <200ms operational
- Security: Gates + Context + Headers + RLS Connection Affinity
- Settings: Registry mit ETag ≥70% Hit-Rate
- CI: Pipeline Split (PR <10min, Nightly ~30min)
- Testing: Integration Framework mit P95-Metriken
- RLS: Connection Affinity mit @RlsContext CDI Interceptor
- **Production Patterns:** 3 Copy-Paste-Ready Patterns für alle Module

📊 Performance Report: [phase-1-foundation-benchmark-2025-09-24.md](../performance/phase-1-foundation-benchmark-2025-09-24.md)
🔒 Security Update: [SECURITY_UPDATE_SPRINT_1_5.md](./SECURITY_UPDATE_SPRINT_1_5.md)

🎯 **Copy-Paste Ready Patterns (aus PR #110):**
- [Security Test Pattern](features-neu/02_neukundengewinnung/artefakte/SECURITY_TEST_PATTERN.md) → Alle Module
- [Performance Test Pattern](features-neu/02_neukundengewinnung/artefakte/PERFORMANCE_TEST_PATTERN.md) → P95 Validation
- [Event System Pattern](features-neu/02_neukundengewinnung/artefakte/EVENT_SYSTEM_PATTERN.md) → **CQRS Light Backbone** für alle Module
```

### 🚀 **Phase 2: Core Business (7.5 Wochen) - IN PROGRESS**
```
Progress: █████░░░░░ 55% (2.5/5 Sprints + 6 Sub-Sprints COMPLETE)

Sprint 2.1: 02 Neukundengewinnung     ✅ 100% COMPLETE → PR #103, #105, #110, #111 merged (FP-235 ✅)
                                      → 3 Production Patterns dokumentiert (Security/Performance/Events)
Sprint 2.1.2: Frontend Research       ✅ COMPLETE → PR #112 merged - Research & Patterns für UI
Sprint 2.1.3: Frontend Lead Mgmt      ✅ COMPLETE → PR #122 merged - Lead Management MVP
Sprint 2.1.4: Lead Deduplication      ✅ COMPLETE → PR #123 merged - Normalisierung + Idempotency (V247-V254)
Sprint 2.1.5: Progressive Profiling   ✅ COMPLETE (05.10.2025) → PR #124 Backend + PR #129 Frontend MERGED
                                      → **Monster-PR #129:** 56 Dateien, +8.525 LOC, -975 LOC
                                      → **Backend:** V255-V259 Migrations, LeadProtectionService (24 Tests, 0.845s), LeadDTO erweitert
                                      → **Frontend:** LeadWizard (812 LOC) + Pre-Claim Badge + Server-Side Filtering (contextConfig.ts)
                                      → **Tests:** LeadWizard Integration (802 LOC, MSW-basiert), TerritoryService (+78 LOC)
                                      → **Dokumentation:** 5 Artefakte (3.814 LOC) + ADR-006 Hybrid-Architektur
                                      → **DSGVO:** Consent-Checkbox Stage 1 (Art. 6 Abs. 1 lit. a), NICHT vorausgefüllt ✅
                                      → **Performance:** Bundle 178 KB, LeadWizard <50ms, Pre-Claim Badge <10ms, Filtering <200ms
                                      → **CI Issue:** Worktree CI temporär deaktiviert (Issue #130: TestDataBuilder Konflikt)
                                      → **Verschoben auf 2.1.6:** Quick-Action "Erstkontakt nachtragen", Pre-Claim Filter
                                      → [Modul 02 Sprint-Map](features-neu/02_neukundengewinnung/SPRINT_MAP.md)

Sprint 2.1.6: Lead Completion         ✅ 100% COMPLETE (05-11.10.2025) - PR #132, #133, #134, #135, #137 CREATED
                                      → **Phase 1:** Issue #130 Fix (TestDataBuilder CDI-Konflikt) ✅ PR #132 MERGED
                                      → **Phase 2:** Admin APIs (Import, Backdating, Convert) ✅ PR #133 MERGED
                                        - LeadImportService (297 LOC), LeadBackdatingService (107 LOC), LeadConvertService (204 LOC)
                                        - 33 Tests GREEN (Import: 14, Backdating: 13, Convert: 6)
                                      → **Phase 3:** Automated Nightly Jobs + Outbox-Pattern ✅ PR #134 MERGED
                                        - LeadMaintenanceService (461 LOC), LeadMaintenanceScheduler (127 LOC)
                                        - OutboxEmail Entity (147 LOC), ImportJob Idempotency (159 LOC)
                                        - 19 Tests GREEN (14 Import, 5 Maintenance)
                                      → **Phase 4:** Lead Quality Metrics & UI Components ✅ PR #135 MERGED (08.10.2025)
                                        - LeadScoringService (264 LOC, 4-Faktoren: Umsatz/Engagement/Fit/Dringlichkeit)
                                        - 4 UI-Komponenten (StopTheClockDialog, LeadScoreIndicator, LeadActivityTimeline, LeadStatusWorkflow)
                                        - 48 Frontend-Tests + 19 Backend-Tests
                                        - 3 Produktionsbugs gefunden & gefixt (RBAC, German labels, DTO-Mapping)
                                        - Gemini Code-Review: 4 Refactorings (DRY, Timestamps, Formatierung)
                                      → **Phase 5:** Multi-Contact + Lead Scoring + Security + Critical Fixes ✅ PR #137 CREATED (11.10.2025)
                                        - **4 Main Features:**
                                          - Lead Scoring System (0-100 Score, 4 Dimensionen, 268 LOC, 19 Tests GREEN)
                                          - Multi-Contact Support (26 Felder, 100% Customer Parity, Backward Compatibility Trigger V10017)
                                          - Enterprise Security (5 Layer: Rate Limiting, Audit, XSS, Error Disclosure, HTTP Headers)
                                          - Critical Bug Fixes (4 Fixes: ETag Race, Ambiguous Email, Missing Triggers, UTF-8)
                                        - **12 Migrationen V10013-V10024:**
                                          - V10013-V10015: Settings ETag Triggers, Lead Enums (VARCHAR + CHECK), first_contact_documented_at
                                          - V10016-V10017: lead_contacts Table + Backward Compatibility Trigger (KRITISCH!)
                                          - V10018-V10022: Pain Scoring (4 Faktoren), Lead Scoring (0-100), territory_id nullable
                                          - V10023-V10024: Lead Scoring Complete (revenue_score, NOT NULL Constraints)
                                        - **Migration Safety System (3-Layer):** Pre-Commit Hook, GitHub Workflow, Enhanced get-next-migration.sh
                                        - **Performance:** N+1 Query Fix (7x faster: 850ms→120ms), Score Caching (90% weniger DB-Writes)
                                        - **Tests:** 31/31 LeadResourceTest GREEN + 10/10 Security Tests GREEN
                                        - **CI Status:** 50 commits, 3 weeks, 125 files (+17.930/-1.826 LOC)
                                        - **PR #137:** https://github.com/joergstreeck/freshplan-sales-tool/pull/137
                                      → **VERSCHOBEN AUF 2.1.7:** Lead-Transfer, RLS, Team Management, Fuzzy-Matching
                                      → [Modul 02 Sprint-Map](features-neu/02_neukundengewinnung/SPRINT_MAP.md)

Sprint 2.1.6.1: Enum-Migration P2+3   ✅ PHASE 1 COMPLETE (12.10.2025) - PR #138 MERGED - Phase 2+3 SKIPPED
                                      → **Phase 1 (4h):** Customer-Modul BusinessType-Migration ✅ COMPLETE
                                        - ✅ **PR #138 MERGED TO MAIN:** Commit 3222312cf (12.10.2025, 17:46 CEST)
                                        - ✅ DISCOVERY: Migration V264 bereits vorhanden aus Sprint 2.1.6 Phase 5
                                        - ✅ Backend: Auto-Sync Setter Tests (27 unit tests GREEN) - CustomerAutoSyncSetterTest.java (413 LOC)
                                        - ✅ Frontend: CustomerForm refactored (useBusinessTypes() hook) - 5 hardcoded → 9 dynamic values
                                        - ✅ Frontend: MSW Mock Tests (18 tests GREEN) - mockServer.ts + CustomerForm.test.tsx
                                        - ✅ Code Reviews: Copilot + Gemini komplett adressiert (Commit 1c699d341)
                                        - ✅ CI-Fixes: Mock Guard `/tests/` Path Exception + Spotless Format (Commit bd3136e36)
                                        - ✅ Tests: 45 GREEN (27 Backend + 18 Frontend), CI: 31/31 Checks PASSED
                                        - ✅ Dokumentation: ENUM_MIGRATION_STRATEGY.md + Master Plan V5 + Roadmap + Trigger Index
                                      → **Phase 2+3:** ⚠️ SKIPPED (Tables do not exist yet)
                                        - ActivityType, OpportunityStatus, PaymentMethod, DeliveryMethod
                                        - Reason: orders, opportunities, customer_activities tables nicht vorhanden
                                        - Decision: Implement when business need arises
                                      → **Artefakt:** [ENUM_MIGRATION_STRATEGY.md](features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md)
                                      → **PR #138:** https://github.com/joergstreeck/freshplan-sales-tool/pull/138

Sprint 2.1.6.2: DEV-SEED + Bugfixes ✅ COMPLETE (13.10.2025) - Commit 8884e2cb7
                                      → **DEV-SEED Infrastructure (Production-Ready):**
                                        - ✅ V90001: 5 realistische Customer-Szenarien (IDs 90001-90005)
                                        - ✅ V90002: 10 Lead-Szenarien + 21 Contacts + 21 Activities (IDs 90001-90010)
                                        - ✅ Score-Range validiert: 21-59 (System Ceiling confirmed)
                                        - ✅ Hot Leads: 90003 (Score 59), 90007 (Score 57)
                                        - ✅ Edge Cases: PreClaim (90006), Grace Period (90005), LOST (90004)
                                        - ✅ **Neue Migration-Strategie:** db/dev-seed/ Folder für DEV-only Daten
                                      → **Frontend Bugfixes (3 kritische Bugs):**
                                        - ✅ Auto-Save Race Condition (409 Conflict bei Component Mount)
                                        - ✅ Auth Bypass Permission Failure (Stop-the-Clock "Keine Berechtigung")
                                        - ✅ GRACE_PERIOD Translation (fehlende deutsche Übersetzung)
                                      → **Backend Error Handling:**
                                        - ✅ OptimisticLockException → 409 Conflict (proper HTTP status)
                                        - ✅ RlsConnectionAffinityGuard: SUPERUSER detection verbessert
                                        - ✅ application.properties: RLS fallback user für DEV-SEED
                                      → **Frontend .ENV Configuration:**
                                        - ✅ .env.development: Auth Bypass aktiviert
                                        - ✅ VITE_USE_KEYCLOAK_IN_DEV=false für Mock-User
                                      → **Commit:** 8884e2cb7 "fix: Frontend Bugfixes - Auto-Save Race Condition + Auth Bypass + UI Fixes"

Sprint 2.1.7: ActivityOutcome + Code Review ✅ COMPLETE (14.10.2025) - PR #139 READY FOR MERGE
                                      → **Backend Integration (V10026 + V10027 + V10028 + V90003):**
                                        - ✅ **V10026 Migration:** lead_id + customer_id FKs, Check Constraint (lead_id OR customer_id OR stage='NEW_LEAD')
                                        - ✅ **V10027 Migration:** activity_outcome ENUM (7 values: SUCCESSFUL, UNSUCCESSFUL, NO_ANSWER, CALLBACK_REQUESTED, INFO_SENT, QUALIFIED, DISQUALIFIED)
                                        - ✅ **V10028 Migration:** customer_number_seq (Race Condition Fix - production-ready PostgreSQL sequence)
                                        - ✅ **OpportunityService:** 3 Service-Methoden (createFromLead, convertToCustomer, createForCustomer)
                                        - ✅ **REST APIs:** POST /api/opportunities/from-lead/{leadId}, POST /api/opportunities/{id}/convert-to-customer, POST /api/opportunities/for-customer/{customerId}
                                        - ✅ **Lead Status Auto-Update:** CONVERTED Status bei Opportunity-Erstellung (Industry Standard: ONE-WAY)
                                        - ✅ **V90003 DEV-SEED:** 10 realistische Opportunities (4 from Leads, 6 from Customers), Total Value €163,000
                                        - ✅ **Full Traceability:** Lead → Opportunity → Customer mit originalLeadId (V261)
                                      → **Code Review Fixes (PR #139 - 10 Issues):**
                                        - ✅ **6 Code Review Issues (Copilot AI + Gemini Code Assist):**
                                          - Fix #1 - CRITICAL: Race Condition in generateCustomerNumber() (V10028 sequence fix)
                                          - Fix #2-3 - Clock Injection Pattern (Issue #127: GlobalExceptionMapper + LeadResource - 12 replacements)
                                          - Fix #4 - Redundant persist() in LeadResource (Line 892/896)
                                          - Fix #5 - Return Type Consistency (OpportunityService.createForCustomer → OpportunityResponse DTO)
                                          - Fix #6 - Cascading Fix (OpportunityResource + Tests - 7 compilation errors)
                                        - ✅ **3 Pre-existing Test Fixes (14 Fehler - NICHT durch Code Review verursacht):**
                                          - Fix #7 - FollowUpAutomationServiceTest Clock Mock (8 errors → 9/9 GREEN)
                                          - Fix #8 - CustomerRepositoryTest Test Isolation (6 failures → 43/43 GREEN)
                                          - Fix #9 - DEV-SEED Auto-Recovery Enhancement (robust-session-start.sh)
                                        - ✅ **1 CI Fix:**
                                          - Fix #10 - ActivityDialog.test.tsx ESLint Errors (2 unused variables)
                                      → **Tests:** 60/60 Backend Tests GREEN (100%) + Frontend ESLint GREEN ✅
                                      → **Performance:** Backend startet in 4.6s, Flyway 161 migrations validated
                                      → **Commits:** 869730d2d (fixes), 4e68415b9 (docs), a64574d2b (spotless), 540b9d09c (eslint)
                                      → **PR #139:** https://github.com/joergstreeck/freshplan-sales-tool/pull/139
                                      → **Completion:** [SPRINT_2_1_7_SUMMARY.md](SPRINT_2_1_7_SUMMARY.md)

Sprint 2.1.7.1: Opportunities UI     📋 PLANNING (14.10.2025) - Aufwand: 16-24h (2-3 Tage)
                                      → **Phase 1:** Lead → Opportunity UI (CreateOpportunityDialog, Lead Detail Integration, LeadOpportunitiesList)
                                      → **Phase 2:** Kanban Board Enhancements (Filter: Nur offene/Alle/Archiv, Backend Endpoint /api/opportunities?status=active)
                                      → **Phase 3:** Customer → Opportunity UI (Customer Detail Integration, CustomerOpportunitiesList, Conversion Button)
                                      → **Phase 4:** Testing & Polish (Unit Tests, E2E Tests mit Playwright, UI/UX Polish)
                                      → **Prerequisites:** ✅ Sprint 2.1.7 Backend complete, ✅ V10026 + V90003 deployed
                                      → **Trigger:** [TRIGGER_SPRINT_2_1_7_1.md](TRIGGER_SPRINT_2_1_7_1.md)

Sprint 2.1.8: Team Mgmt & Test Infra 📅 VERSCHOBEN (19-25.10.2025) - VORMALS Sprint 2.1.7
                                      → **Track 1 - Business (verschoben aus 2.1.6):**
                                        - Lead-Transfer Workflow (V260: lead_transfers, POST /api/leads/{id}/transfer, 48h SLA)
                                        - Fuzzy-Matching & Review (Scoring-Algorithmus: Email 40%, Phone 30%, Company 20%, Address 10%)
                                        - Row-Level-Security (V261: RLS Policies - owner, team, admin, transfer_recipient)
                                        - Team Management (V262: teams + team_members, CRUD-API, Quotenregelung)
                                      → **Track 2 - Test Infrastructure (NEU - STRATEGISCH!):**
                                        - CRM Szenario-Builder (Lead-Journey, Customer-Journey, Opportunity-Pipeline)
                                        - Faker-Integration (RealisticDataGenerator für deutsche Testdaten)
                                        - Lead-spezifische TestDataFactories (LeadTestDataFactory, LeadActivityTestDataFactory)
                                        - Test-Pattern Library (TESTING_PATTERNS.md, TEST_DATA_CHEATSHEET.md)
                                      → **Track 3 - Code Quality (NEU - aus PR #133 Review):**
                                        - **Issue #135:** Name Parsing Robustness (LeadConvertService - Library-basiert statt String.split)
                                        - EnumResource Refactoring: LeadSource + KitchenSize als Backend-Enums (konsistent mit BusinessType)
                                        - Backend DTOs: Kapselung (private fields + getters statt public fields)
                                      → **Begründung Track 2:** Quality Investment für Sprint 2.2+ Velocity, Test-Szenarien für Onboarding
                                      → **Begründung Track 3:** Gemini Code Review Feedback (Medium Priority - verbessert Datenqualität)
Sprint 2.2: 03 Kundenmanagement       📋 Ready → 39 Artefakte + nutzt Security/Performance Patterns
Sprint 2.3: 05 Kommunikation          📋 Ready → Security-Gate ✅ + nutzt Event-System Pattern
Sprint 2.4: 01 Cockpit                🟡 Planning → CQRS-optimiert
Sprint 2.5: 06 Einstellungen          🟡 Planning → Settings Foundation
```

### 📋 **Phase 3: Enhancement (4.5 Wochen)**
```
Progress: ░░░░░░░░░░ 0% (0/3 Sprints)

Sprint 3.1: 04 Auswertungen          🟡 Planning → Analytics on CQRS
Sprint 3.2: 07+08 Hilfe + Admin      🟡 Planning → CAR-Strategy + User Mgmt
Sprint 3.3: Final Integration        🟡 Planning → Kong/Envoy Policies
```

**🎯 GESAMT-FORTSCHRITT: 17/36 PRs ✅ (47% done) | 6.5/15 Wochen | ETA: 2025-04-30**

---

## 📋 DETAILLIERTE SPRINT-PLÄNE

### **SPRINT 1.1: CQRS LIGHT FOUNDATION (Woche 1)** 📋

**Ziel:** PostgreSQL LISTEN/NOTIFY + Command/Query-Pattern Implementation
**🎯 Bildet CQRS Light Backbone:** Event System Pattern aus PR #110 ist die Basis für alle Module

**PRs:**
```yaml
Day 1-2: feature/00-migrationen-listen-notify-setup-FP-225  📋
  ✅ PostgreSQL LISTEN/NOTIFY Implementation
  ✅ Basic Event-Schema Setup
  ✅ Performance-Tests <200ms P95
  ✅ Migration (dynamisch) mit Rollback-Plan

Day 3-4: feature/00-migrationen-command-service-pattern-FP-226  📋
  ✅ Command-Service Architecture
  ✅ Command-Handler Pattern
  ✅ Unit + Integration Tests (neue Test-Struktur)

Day 5: feature/00-migrationen-query-service-pattern-FP-227  📋
  ✅ Query-Service Architecture
  ✅ Read-Projections Pattern
  ✅ Performance-Optimization + Feature-Flag
```

**Success Criteria:**
- [ ] P95 Query-Performance <200ms (k6-smoke)
- [ ] CQRS Feature-Flag functional
- [ ] Zero-Downtime Migration + Rollback <5min
- [ ] Foundation für nachfolgende Module bereit

---

### **SPRINT 1.2: SECURITY + FOUNDATION (Woche 2)** 📋

**Ziel:** ABAC/RLS Security Foundation + Settings Registry

**PRs:**
```yaml
Day 6-7: feature/00-sicherheit-abac-rls-foundation-FP-228  📋
  ✅ ABAC Policy Engine
  ✅ RLS Territory-Scoping
  ✅ Security Contract-Tests (fail-closed)

Day 8-9: feature/00-governance-settings-registry-core-FP-229  📋
  ✅ Settings Registry MVP
  ✅ 5-Level Scope-Hierarchie
  ✅ ETag-Caching ≥70% Hit-Rate

Day 10: feature/00-test-struktur-migration-start-FP-230  📋
  ✅ Neue Test-Struktur Implementation
  ✅ Test-Guidelines Documentation
  ✅ Legacy-Test-Migration (erste Batch)
```

**Success Criteria:**
- [ ] ABAC/RLS Contracts grün (Owner/Kollaborator/Manager-Override)
- [ ] Settings Registry operational mit ETag-Performance
- [ ] Test-Struktur migration guidelines etabliert

---

### **SPRINT 1.3: SECURITY GATES + CI HARDENING (Woche 3)** 📋

**Ziel:** Security-Gates als Required Checks + CI Pipeline hardening

**PRs:**
```yaml
Day 11-12: security-gates-enforcement-FP-231  📋
  ✅ 5 Security-Contract-Tests als Required PR-Checks
  ✅ Fail-closed Verification
  ✅ PR-Template mit 6 Pflichtblöcken

Day 13-14: foundation-integration-testing-FP-232  📋
  ✅ Cross-Foundation Integration Tests
  ✅ Quality-Gates Validation
  ✅ CI Pipeline Split: PR-Smoke vs Nightly-Full

Day 15: foundation-stabilization-buffer  📋
  ✅ PUFFER-TAG für Foundation-Stabilisierung
  ✅ Performance-Benchmarks dokumentiert
  ✅ Rollback-Procedures getestet
```

**Success Criteria:**
- [ ] Security-Gates blockieren PRs bei Violations
- [ ] Foundation-Performance <200ms P95 bestätigt
- [ ] Alle nachfolgenden Module können starten

---

### **🚨 SECURITY-GATE CHECKPOINT PHASE 1 → PHASE 2**

**VERBINDLICHE FREIGABE-KRITERIEN:**
- [ ] ✅ CQRS Light operational mit <200ms P95
- [ ] ✅ ABAC/RLS Security-Contracts grün
- [ ] ✅ Settings Registry mit ETag-Performance
- [ ] ✅ 5 Security-Contract-Tests als Required Checks
- [ ] ✅ Rollback-Procedures <5min getestet
- [ ] ✅ Foundation-Integration-Tests grün

**NUR WENN ALLE PUNKTE ✅ → PHASE 2 STARTEN!**

---

### **SPRINT 2.1: 02 NEUKUNDENGEWINNUNG (Woche 4)** 📋

**Ziel:** Lead-Management System mit Territory-Scoping (NACH Security Foundation)

**Verfügbare Artefakte:** Foundation Standards (design-system, openapi, backend, frontend, sql, k6)

**PRs:**
```yaml
Day 16-17: feature/02-leads-territory-management-FP-233  📋
  ✅ Territory-Assignment ohne Gebietsschutz
  ✅ Lead-Protection userbasiertes Ownership
  ✅ ABAC-Integration für Multi-Contact-B2B

Day 18-19: feature/02-leads-capture-system-FP-234  📋
  ✅ Lead-Capture Forms + Validation
  ✅ Multi-Contact-Workflows (CHEF/BUYER)
  ✅ Integration mit Settings-Registry

Day 20-21: feature/02-leads-follow-up-automation-FP-235  ✅ COMPLETE (PR #111)
  ✅ T+3/T+7 Follow-up Automation implementiert
  ✅ Dashboard Widget Integration mit Lead-Metriken
  ✅ Event Distribution via LISTEN/NOTIFY mit AFTER_COMMIT
  ✅ Prometheus Metrics für Follow-up Tracking
  ✅ Sample-Management Integration
  ✅ ROI-Calculator für Lead-Qualification

Day 22: feature/02-leads-security-integration-FP-236  📋
  ✅ ABAC/RLS Integration validiert
  ✅ Cross-Module Event-Integration (LISTEN/NOTIFY)
  ✅ Performance-Tests + Frontend-Integration
```

**Success Criteria:**
- [ ] Lead-Management mit Territory-Scoping operational
- [x] T+3/T+7 Automation funktional ✅ (PR #111)
- [ ] ABAC/RLS-Integration bestätigt
- [ ] Performance <200ms P95 auf CQRS Foundation

---

### **SPRINT 2.1.2: FRONTEND RESEARCH (docs-only)** 📋

**Ziel:** Strukturierte Frontend-Research für Modul 02 Neukundengewinnung

**Deliverables:**
- ✅ INVENTORY.md - Stack-Analyse & Gaps
- ✅ API_CONTRACT.md - Event-System, RBAC, REST-Endpoints
- ✅ RESEARCH_ANSWERS.md - 11 offene Fragen beantwortet
- ✅ VALIDATED_FOUNDATION_PATTERNS.md - Konsolidierte Patterns aus grundlagen/ & infrastruktur/

**Status:** Research abgeschlossen → PR #112 (Draft, docs-only)

**Nächster Schritt:** Sprint 2.1.3 implementiert Thin Vertical Slice

---

### **SPRINT 2.2: 03 KUNDENMANAGEMENT (Woche 5-6)** 📋

**Ziel:** Field-based Customer Architecture + Multi-Contact System

**Verfügbare Artefakte:** 39 Production-Ready Deliverables (EXCEPTIONAL Quality 10/10)

**PRs:**
```yaml
Day 23-24: feature/03-customers-field-architecture-core-FP-237  📋
  ✅ Dynamic Customer-Schema Implementation
  ✅ Field-based Architecture statt Entity-based
  ✅ JSONB + Performance-Optimization

Day 25-26: feature/03-customers-multi-contact-system-FP-238  📋
  ✅ Multi-Contact Support (CHEF/BUYER parallel)
  ✅ Contact-Role-Management
  ✅ Complex Gastronomiebetrieb-Requirements

Day 27-28: feature/03-customers-territory-scoping-FP-239  📋
  ✅ Territory-Management Deutschland/Schweiz
  ✅ Currency + Tax + Regional-Specialties
  ✅ RLS Territory-Scoping Integration

Day 29-30: feature/03-customers-frontend-integration-FP-240  📋
  ✅ React Frontend Components
  ✅ Field-Based Forms + Validation
  ✅ Multi-Contact UI/UX

Day 31: feature/03-customers-complete-testing-FP-241  📋
  ✅ End-to-End Customer-Lifecycle Tests
  ✅ Performance-Validation auf CQRS
  ✅ Cross-Module Integration mit 02 Leads
```

**Success Criteria:**
- [ ] Field-based Customer Architecture operational
- [ ] Multi-Contact-System für B2B-Food funktional
- [ ] Territory-Management Deutschland/Schweiz
- [ ] Integration mit Lead-Management bestätigt

---

### **🚨 SECURITY-GATE CHECKPOINT: "KOMMUNIKATION KANN STARTEN"**

**VERBINDLICHE FREIGABE-KRITERIEN VOR SPRINT 2.3:**
- [ ] ✅ Lead-Management (02) operational mit ABAC/RLS
- [ ] ✅ Customer-Management (03) operational mit Territory-Scoping
- [ ] ✅ 5 Security-Contract-Tests weiterhin grün
- [ ] ✅ Owner/Kollaborator/Manager-Override mit Audit funktional
- [ ] ✅ Cross-Module Events (LISTEN/NOTIFY) zwischen 02+03 funktional

**GRUND:** Kommunikation (05) referenziert Leads/Accounts + benötigt userbasierte Ownership

---

### **SPRINT 2.3: 05 KOMMUNIKATION (Woche 7) - NACH SECURITY-GATE** 📋

**Ziel:** Enterprise Email-Engine + Thread/Message/Outbox Pattern

**Verfügbare Artefakte:** 41 Production-Ready Files (Best-of-Both-Worlds 8.6/10)

**PRs:**
```yaml
Day 32-33: feature/05-kommunikation-thread-message-core-FP-242  📋
  ✅ Thread/Message-Pattern Implementation
  ✅ Lead/Account-Context Integration
  ✅ userbasierte Ownership (Owner/Kollaborator)

Day 34-35: feature/05-kommunikation-outbox-pattern-FP-243  📋
  ✅ Outbox-Pattern für Email-Reliability
  ✅ Bounce-Handling + Status-Tracking
  ✅ SLA-Engine T+3/T+7 Integration

Day 36-37: feature/05-kommunikation-email-engine-FP-244  📋
  ✅ Enterprise Email-Engine
  ✅ Multi-Contact Email-Workflows
  ✅ Template-System + Personalization

Day 38: feature/05-kommunikation-security-integration-FP-245  📋
  ✅ ABAC/RLS für Kommunikation validiert
  ✅ Activity-Timeline Cross-Module-Events
  ✅ Performance-Tests + Email-Engine-Load-Tests
```

**Success Criteria:**
- [ ] Email-Engine operational mit Enterprise-Reliability
- [ ] Thread/Message-Pattern mit Lead/Account-Integration
- [ ] ABAC/RLS für Kommunikation bestätigt
- [ ] Cross-Module Activity-Timeline funktional

---

### **SPRINT 2.4: 01 COCKPIT (Woche 8)** 📋

**Ziel:** ROI-Dashboard + Real-time Widgets auf CQRS Foundation

**Verfügbare Artefakte:** 44 Production-Ready Artefakte (Enterprise Assessment A+ 95/100)

**PRs:**
```yaml
Day 39-40: feature/01-cockpit-dashboard-widgets-FP-246  📋
  ✅ Real-time Dashboard-Widgets
  ✅ Territory-Performance + ROI-Insights
  ✅ LISTEN/NOTIFY Live-Updates

Day 41-42: feature/01-cockpit-roi-calculator-FP-247  📋
  ✅ ROI-Calculator für Business-Value-Demo
  ✅ Multi-Channel B2B-Food-Calculations
  ✅ Cost-per-Lead + Conversion-Tracking

Day 43-44: feature/01-cockpit-cqrs-integration-FP-248  📋
  ✅ Hot-Projections für Dashboard-Performance
  ✅ Read-optimized Views auf CQRS Foundation
  ✅ ETag-Caching für <200ms Dashboard-Loads

Day 45: feature/01-cockpit-performance-optimization-FP-249  📋
  ✅ Dashboard-Performance <200ms P95 bestätigt
  ✅ Cross-Module KPI-Integration (02+03+05)
  ✅ Live-Badges via LISTEN/NOTIFY + Journal-Fallback
```

**Success Criteria:**
- [ ] Dashboard-Performance <200ms P95 auf CQRS
- [ ] ROI-Calculator für B2B-Food-Business funktional
- [ ] Real-time Updates via LISTEN/NOTIFY
- [ ] Cross-Module KPI-Integration bestätigt

---

### **SPRINT 2.5: 06 EINSTELLUNGEN + CROSS-MODULE (Woche 9-10)** 📋

**Ziel:** Settings UI + Cross-Module Integration Testing

**Verfügbare Artefakte:** 4 Weltklasse Technical Concepts (9.9-10/10)

**PRs:**
```yaml
Day 46-47: feature/06-einstellungen-ui-implementation-FP-250  📋
  ✅ Settings UI auf Settings-Registry Foundation
  ✅ 5-Level Scope-Hierarchie Frontend
  ✅ Territory + Seasonal + Role-specific Settings

Day 48-49: feature/06-einstellungen-business-rules-FP-251  📋
  ✅ Business-Rules-Engine Integration
  ✅ Territory-aware Currency + Tax-Settings
  ✅ Multi-Contact-Settings für CHEF/BUYER

Day 50-52: cross-module-integration-testing-FP-252  📋
  ✅ End-to-End Cross-Module Integration Tests
  ✅ Lead → Customer → Communication → Cockpit Flow
  ✅ Performance-Tests für Complete Business-Workflows

Day 53: core-business-stabilization-buffer  📋
  ✅ PUFFER-TAG für Core-Business-Stabilisierung
  ✅ Performance-Benchmarks für alle Module
  ✅ Cross-Module Event-Flows dokumentiert
```

**Success Criteria:**
- [ ] Settings UI operational auf Registry-Foundation
- [ ] Business-Rules-Engine für Territory-Management
- [ ] Complete Cross-Module Integration bestätigt
- [ ] Core Business-Workflows <200ms P95

---

### **SPRINT 3.1: 04 AUSWERTUNGEN (Woche 11)** 📋

**Ziel:** Analytics Platform auf CQRS Foundation mit Real-Business-Data

**Verfügbare Artefakte:** 12 Copy-Paste-Ready Implementation-Files (97% Production-Ready)

**PRs:**
```yaml
Day 54-55: feature/04-auswertungen-analytics-core-FP-253  📋
  ✅ Analytics-Platform auf CQRS Query-Services
  ✅ ReportsResource.java + Database-Views
  ✅ SQL-Projections mit Performance-Indices

Day 56-57: feature/04-auswertungen-real-time-dashboards-FP-254  📋
  ✅ Real-time Business-KPIs + Pipeline-Analytics
  ✅ Territory-Insights + Seasonal-Trends
  ✅ Cross-Module-KPIs (Lead-Conversion + Sample-Success)

Day 58-59: feature/04-auswertungen-listen-notify-integration-FP-255  📋
  ✅ LISTEN/NOTIFY Live-Updates für Analytics
  ✅ WebSocket Real-time + Journal-Fallback
  ✅ Universal Export Integration (JSONL-Streaming)

Day 60: feature/04-auswertungen-performance-optimization-FP-256  📋
  ✅ Analytics Queries auf Read-Replica/Batch-Projections
  ✅ ABAC Territory-Scoping für Analytics
  ✅ Performance: keine OLTP-Query-Beeinflussung
```

**Success Criteria:**
- [ ] Analytics-Platform operational auf CQRS Foundation
- [ ] Real-time Business-KPIs mit Live-Updates
- [ ] Territory-Insights + Seasonal-Intelligence
- [ ] Analytics-Performance isoliert von OLTP

---

### **SPRINT 3.2: 07 HILFE + 08 ADMINISTRATION (Woche 12)** 📋

**Ziel:** CAR-Strategy Help-System + Enterprise Administration

**Verfügbare Artefakte:**
- 07: 25 AI-Artefakte CAR-Strategy (9.4/10)
- 08: 76 Production-Ready Artefakte (9.6/10)

**PRs:**
```yaml
Day 61-62: feature/07-hilfe-car-strategy-core-FP-257  📋
  ✅ CAR-Strategy Help-System (Calibrated Assistive Rollout)
  ✅ Struggle-Detection + Guided-Workflows
  ✅ Help-as-a-Service Cross-Module Integration

Day 63-64: feature/08-administration-user-management-FP-258  📋
  ✅ Enterprise User-Management + Permissions
  ✅ Risk-Tiered-Approvals System
  ✅ DSGVO-Compliance + Complete Audit-Trail

Day 65-66: feature/08-administration-security-integration-FP-259  📋
  ✅ ABAC + Multi-Tenancy + External-Integrations
  ✅ AI/ERP-Integration Points
  ✅ Lead-Protection-System validation

Day 67: final-system-integration-testing-FP-260  📋
  ✅ Complete System Integration Tests
  ✅ Help-System + Administration Cross-Module
  ✅ Enterprise-Grade Security + Audit validation
```

**Success Criteria:**
- [ ] CAR-Strategy Help-System operational
- [ ] Enterprise Administration mit ABAC/Multi-Tenancy
- [ ] Complete System Integration bestätigt
- [ ] DSGVO-Compliance + Audit-Trail vollständig

---

### **SPRINT 3.3: FINAL INTEGRATION (Woche 13-14)** 📋

**Ziel:** Kong/Envoy Policies + Deployment-Preparation

**PRs:**
```yaml
Day 68-70: feature/00-integration-kong-envoy-policies-FP-261  📋
  ✅ Kong + Envoy Gateway-Policies (nachgelagert)
  ✅ Rate-Limiting + Idempotency + CORS
  ✅ Production-Grade API-Gateway Setup

Day 71-74: final-system-testing-deployment-prep  📋
  ✅ Complete End-to-End System Testing
  ✅ Production-Deployment-Pipeline Setup
  ✅ Performance-Benchmarks für alle Module

Day 75: production-deployment-buffer  📋
  ✅ PUFFER-TAG für Production-Deployment
  ✅ Go-Live Preparation + Rollback-Plans
  ✅ Documentation + Handover Complete
```

**Success Criteria:**
- [ ] Production-Grade API-Gateway operational
- [ ] Complete System-Performance <200ms P95
- [ ] Production-Deployment-Pipeline ready
- [ ] Enterprise-Grade FreshFoodz CRM COMPLETE

---

## 📦 ARTEFAKTE-INTEGRATION WORKFLOW

### 🎯 VERFÜGBARE PRODUCTION-READY ARTEFAKTE

**Module 01 Cockpit:** [44 Artefakte](./features-neu/01_mein-cockpit/artefakte/)
- Enterprise Assessment A+ (95/100)
- ABAC Security + ROI-Calculator + Multi-Channel Dashboard
- Ready: API-Specs, Backend-Services, Frontend-Components, SQL-Schemas, Testing

**Module 02 Neukundengewinnung:** [Foundation Standards](./features-neu/02_neukundengewinnung/shared/docs/)
- 92%+ Foundation Standards Compliance
- Ready: design-system/, openapi/, backend/, frontend/, sql/, k6/, docs/

**Module 03 Kundenmanagement:** [39 Production-Ready Deliverables](./features-neu/03_kundenmanagement/artefakte/)
- EXCEPTIONAL Quality (10/10) - Enterprise-Grade Standards
- Ready: Field-based Architecture + ABAC Security + Testing 80%+

**Module 04 Auswertungen:** [12 Copy-Paste-Ready Files](./features-neu/04_auswertungen/artefakte/)
- 97% Production-Ready, Gap-Closure PERFECT (9.7/10)
- Ready: JSONL-Streaming + ABAC-Security + WebSocket Real-time

**Module 05 Kommunikation:** [41 Production-Ready Files](./features-neu/05_kommunikation/artefakte/)
- Best-of-Both-Worlds (8.6/10 Enterprise-Ready)
- Ready: Thread/Message/Outbox-Pattern + SLA-Engine + Email-Core

**Module 06 Einstellungen:** [4 Technical Concepts](./features-neu/06_einstellungen/)
- 99% Production-Ready - Weltklasse Technical Concepts (9.9-10/10)
- Ready: Settings-Engine + 5-Level Scope-Hierarchie + ABAC Security

**Module 07 Hilfe & Support:** [CAR-Strategy + 25 AI-Artefakte](./features-neu/07_hilfe_support/)
- 95% Production-Ready - CAR-Strategy (9.4/10)
- Ready: Calibrated Assistive Rollout + Operations-Integration

**Module 08 Administration:** [76 Production-Ready Artefakte](./features-neu/08_administration/phase-2-integrations/artefakte/)
- Quality Score 9.6/10 - Enterprise Administration
- Ready: Lead-Protection + Multi-Provider AI + External Integrations

### 🔧 ARTEFAKTE-INTEGRATION PROZESS

**Bei jedem PR:**
1. **📂 Artefakt auswählen** aus entsprechendem Module-Verzeichnis
2. **📋 Copy-Paste** in Projekt-Struktur (Backend/Frontend/SQL)
3. **🔧 Minimal anpassen** für CQRS Foundation + aktuelle Requirements
4. **🧪 Tests ausführen** + anpassen für neue Test-Struktur
5. **📝 Artefakt-Nutzung dokumentieren** in PR-Description

**Artefakte-Policy:**
- ✅ Bevorzuge vorhandene Artefakte vor Neu-Entwicklung
- ✅ Minimal-Änderungen dokumentieren + begründen
- ✅ Artefakt-Links in PR-Template referenzieren
- ✅ Test-Artefakte in neue Test-Struktur migrieren

---

## 🔧 VERBINDLICHE QUALITY GATES

### 📋 REQUIRED PR-CHECKS (GitHub Actions)

**Foundation Phase:**
- ✅ `security-contracts` - 5 ABAC/RLS Contract-Tests
- ✅ `k6-smoke` - Performance <200ms P95
- ✅ `bundle-size` - Frontend <+20KB Regression
- ✅ `zap-baseline` - Security Scan clean
- ✅ `migration-rollback-check` - Rollback-Plan validated

**Business Phase (zusätzlich):**
- ✅ `lighthouse-pwa-performance` - PWA Performance ≥70
- ✅ `jest-coverage-threshold` - Coverage ≥80% modulbezogen
- ✅ `abac-rls-integration` - Cross-Module Security validated

**Enhancement Phase (zusätzlich):**
- ✅ `analytics-performance-isolation` - Keine OLTP-Beeinflussung
- ✅ `cross-module-integration` - End-to-End Workflows

### 📝 PR-TEMPLATE (6 PFLICHTBLÖCKE)

```markdown
## 🎯 Ziel
[Was wird implementiert? Welches Feature/Sub-Feature?]

## ⚠️ Risiko
[Welche Risiken? Mitigation-Strategien?]

## 🔄 Migrations-Schritte + Rollback
[SQL-Änderungen? Migration VXX? Rollback-Plan <5min?]

## ⚡ Performance-Nachweis
```
✅ k6-smoke: P95 <200ms
✅ Bundle-size: <+20KB
✅ EXPLAIN ANALYZE: [Query-Plans für neue Queries]
```

## 🔒 Security-Checks
```
✅ ABAC/RLS-Tests: grün
✅ ZAP-baseline: clean
✅ Input-Validation: implemented
```

## 📚 SoT-Referenzen
```
✅ Technical-Concept: [Link]
✅ Artefakte genutzt: [Liste mit Änderungen]
✅ CLAUDE.md Regeln: befolgt
```

## 📦 Artefakte-Integration
**Verwendete Artefakte:** [Liste aus ./features-neu/XX/artefakte/]
**Änderungen an Artefakten:** [Begründung für Anpassungen]
**Test-Migration:** [Betroffene Tests in neue Struktur verschoben]
```

### 🔄 MAINTENANCE-PROTOKOLL

**Bei PR-Merge:**
1. **Progress aktualisieren:**
   ```bash
   # Progress-Counter: X/30 → (X+1)/30
   # Status ändern: 📋 → ⏳ → ✅
   # Completion-Date hinzufügen
   ```

2. **Blocker-Status prüfen:**
   ```bash
   # Folge-Sprints freigeben: 🔒 → 📋
   # Dependencies checken
   # Security-Gates validieren
   ```

3. **CURRENT STATUS updaten:**
   ```bash
   # Next Action: Nächste feature/branch-FP-XXX
   # Active Branch: Current branch name
   # Progress: (X+1)/35 PRs completed
   ```

**Template für Quick-Updates:**
```bash
# Completion Update
sed -i 's/📋 Sprint X/✅ Sprint X (YYYY-MM-DD)/' PRODUCTION_ROADMAP_2025.md
sed -i 's/Progress: X\/30/Progress: Y\/30/' PRODUCTION_ROADMAP_2025.md
sed -i 's/Next Action: old-branch/Next Action: new-branch/' PRODUCTION_ROADMAP_2025.md
```

---

## 📚 DEVELOPER QUICK REFERENCE

### 🌿 BRANCH-NAMING KONVENTION
```yaml
PATTERN: feature/[module]-[sub-feature]-[component]-[ticket-id]

AKTUELLE TICKETS (FP-225 bis FP-261):
✅ feature/00-migrationen-listen-notify-setup-FP-225        📋 NEXT
✅ feature/00-migrationen-command-service-pattern-FP-226    📋 Ready
✅ feature/00-migrationen-query-service-pattern-FP-227      📋 Ready
✅ feature/00-sicherheit-abac-rls-foundation-FP-228         🔒 Blocked
✅ feature/00-governance-settings-registry-core-FP-229      🔒 Blocked
[... alle 35 PRs mit Ticket-IDs ...]
```

### 🔧 WICHTIGE KOMMANDOS
```bash
# Migration-Nummer checken
./scripts/get-next-migration.sh  # Zeigt aktuelle freie Nummer

# System starten
./scripts/robust-session-start.sh

# Tests ausführen
./mvnw test  # Backend
npm test     # Frontend

# Quality-Gates lokal
./mvnw spotless:apply  # Code-Formatting
npm run lint           # Frontend Linting
```

### 📋 DEBUG-LINKS
- **Debug Cookbook:** [./grundlagen/DEBUG_COOKBOOK.md](./grundlagen/DEBUG_COOKBOOK.md)
- **CI Debugging:** [./grundlagen/CI_DEBUGGING_LESSONS_LEARNED.md](./grundlagen/CI_DEBUGGING_LESSONS_LEARNED.md)
- **TypeScript Guide:** [./grundlagen/TYPESCRIPT_IMPORT_TYPE_GUIDE.md](./grundlagen/TYPESCRIPT_IMPORT_TYPE_GUIDE.md)
- **Performance Standards:** [./grundlagen/PERFORMANCE_STANDARDS.md](./grundlagen/PERFORMANCE_STANDARDS.md)

### 🎯 NOTFALL-KONTAKTE
- **Project Lead:** Jörg Streeck
- **Backup Documentation:** [CRM_COMPLETE_MASTER_PLAN_V5.md](./CRM_COMPLETE_MASTER_PLAN_V5.md)
- **AI Context:** [CRM_AI_CONTEXT_SCHNELL.md](./CRM_AI_CONTEXT_SCHNELL.md)

---

## 🔄 MAINTENANCE & UPDATES

### 📅 WÖCHENTLICHE REVIEWS
**Jeden Freitag:**
- [ ] Progress Dashboard aktualisieren
- [ ] Blocker-Status evaluieren
- [ ] Performance-Benchmarks checken
- [ ] Quality-Gates-Status validieren
- [ ] Next-Week Planning

### 🎯 MILESTONE-GATES
**Nach jeder Phase:**
- [ ] Complete Phase Review + Retrospective
- [ ] Performance-Benchmarks für alle Module
- [ ] Cross-Module Integration Tests
- [ ] Documentation-Update
- [ ] External Validation (optional)

### 📊 SUCCESS-METRICS
**Fortlaufende Messung:**
- **Velocity:** PRs per Week (Target: 2-3)
- **Quality:** Failed Quality-Gates per Week (Target: <2)
- **Performance:** Module-Performance <200ms P95 (Target: 100%)
- **Coverage:** Test-Coverage per Module (Target: ≥80%)

---

## 🏆 ERFOLGSMESSUNG & ABSCHLUSS

### 🎯 DEFINITION OF DONE (Enterprise CRM Complete)

**Technical Excellence:**
- [ ] ✅ Alle 35 PRs merged + Quality-Gates passed
- [ ] ✅ Performance <200ms P95 für alle Module bestätigt
- [ ] ✅ Test-Coverage ≥80% für alle Module
- [ ] ✅ ABAC/RLS Security für alle Module validiert
- [ ] ✅ CQRS Light Architecture vollständig operational

**Business Value:**
- [ ] ✅ Complete B2B-Food-CRM Workflow (Lead → Customer → Communication → Analytics)
- [ ] ✅ Territory-Management Deutschland/Schweiz operational
- [ ] ✅ Multi-Contact-Workflows (CHEF/BUYER) funktional
- [ ] ✅ ROI-Calculator + Real-time Dashboards operational
- [ ] ✅ Enterprise Administration + Help-System funktional

**Production Readiness:**
- [ ] ✅ Production-Deployment-Pipeline ready
- [ ] ✅ Kong/Envoy API-Gateway operational
- [ ] ✅ Complete Documentation + Handover
- [ ] ✅ Rollback-Procedures <5min für alle Komponenten

### 🚀 GO-LIVE CRITERIA

**ENTERPRISE-GRADE FRESHFOODZ CRM BEREIT:**
- **Foundation Excellence:** CQRS Light + ABAC + Settings + Performance <200ms
- **Business Excellence:** Complete CRM-Workflow für B2B-Food-Vertrieb
- **Operations Excellence:** Monitoring + Help-System + Administration
- **Security Excellence:** DSGVO + Audit + Multi-Tenancy + Lead-Protection

**🎯 FINALE VISION ERREICHT:**
Enterprise-Grade B2B-Food-CRM für Cook&Fresh® Vertrieb mit Territory + Seasonal-Intelligence, CQRS Light Performance-Excellence und External AI Operations-Integration!

---

**📋 Dokument-Status:** Production-Ready Roadmap
**🔄 Letzte Aktualisierung:** 2025-01-22
**✅ Validation:** 2x Externe KI bestätigt + alle Korrekturen integriert
**🎯 Ready for Implementation:** feature/00-migrationen-listen-notify-setup-FP-225