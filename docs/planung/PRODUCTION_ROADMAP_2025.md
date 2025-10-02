# 🚀 FreshFoodz CRM Production Roadmap 2025

**📅 Erstellt:** 2025-01-22
**🎯 Team:** Jörg + Claude Code (sequenziell)
**📊 Scope:** 35 PRs über 15 Wochen - Foundation-First Strategie
**✅ Validierung:** 2x Externe KI bestätigt (90% richtig + 4 kritische Korrekturen integriert)

---

## 🎯 CLAUDE QUICK-START (für neue Claude-Instanzen)

**🚨 AKTUELLER STATUS:**
- **Phase:** ✅ Phase 1 COMPLETE | 🚀 Phase 2 IN PROGRESS
- **Current Sprint:** Sprint 2.1.5 Frontend Phase 2 IN PROGRESS (02.10.2025)
- **Progress:** 10/36 PRs - 28% done (PR #123 merged, Sprint 2.1.5 Backend COMPLETE)
- **Blockers:** Keine
- **Foundation Status:** ✅ COMPLETE - CQRS/Security/Settings/CI/RLS operational
- **Performance:** ✅ P95 <7ms (Lead-Module) + CI 24min → 7min (70% schneller) + Frontend 90% Test-Coverage
- **Latest:** Sprint 2.1.5 Backend COMPLETE (V255-V257, ADR-004 Inline-First, 24 Unit Tests) + Frontend Phase 2 IN PROGRESS
- **Sprint 2.1.6/2.1.7:** PLANNED (12-18.10.2025 / 19-25.10.2025) - Migration-API, Lead-Scoring, Mobile-Optimierung

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
Progress: ███░░░░░░░ 40% (2.5/5 Sprints + 4 Sub-Sprints)

Sprint 2.1: 02 Neukundengewinnung     ✅ 100% COMPLETE → PR #103, #105, #110, #111 merged (FP-235 ✅)
                                      → 3 Production Patterns dokumentiert (Security/Performance/Events)
Sprint 2.1.2: Frontend Research       ✅ COMPLETE → PR #112 merged - Research & Patterns für UI
Sprint 2.1.3: Frontend Lead Mgmt      ✅ COMPLETE → PR #122 merged - Lead Management MVP
                                      → [Modul 02 Sprint-Map](features-neu/02_neukundengewinnung/SPRINT_MAP.md)
Sprint 2.1.4: Lead Dedup & Quality    ✅ COMPLETE → PR #123 merged - Normalisierung, Idempotenz, CI 24min→7min
                                      → [Operations Runbook](../operations/lead-deduplication-runbook.md)
Sprint 2.1.5: Protection & Profiling  🔄 Backend Phase 1 COMPLETE (01.10.2025) → Frontend Phase 2 IN PROGRESS (02.10.2025)
                                      → V255-V257 (Progress Tracking + Stage), ADR-004 (Inline-First)
                                      → 24 Unit Tests (0.845s, Pure Mockito, 100% passed)
                                      → DSGVO Consent-Checkbox (Stage 1, consent_given_at), Activity-Types Progress-Mapping definiert
                                      → LeadWizard.tsx + LeadProtectionBadge.tsx + ActivityTimeline.tsx (Full-Page Components)
                                      → Feature-Flag: VITE_FEATURE_LEADGEN=true
                                      → Features verschoben: V258, Backdating, Nightly Jobs, Fuzzy-Matching → Sprint 2.1.6

Sprint 2.1.6: Transfer & Migration    📅 PLANNED (12-18.10.2025)
                                      → Bestandsleads-Migrations-API (Modul 08, POST /api/admin/migration/leads/import)
                                      → Lead → Kunde Convert Flow (automatische Übernahme bei Status QUALIFIED → CONVERTED)
                                      → Stop-the-Clock UI (Manager-only, StopTheClockDialog)
                                      → Extended Lead-Transfer Workflow (mit Genehmigung)
                                      → V258 lead_transfers, Nightly Jobs (Warning/Expiry/Pseudonymisierung)
                                      → Fuzzy-Matching & Scoring (Levenshtein-Distance, pg_trgm)

Sprint 2.1.7: Scoring & Mobile        📅 PLANNED (19-25.10.2025)
                                      → Lead-Scoring Algorithmus (0-100 Punkte, V259 Migration)
                                      → Activity-Templates System (V260 Migration, CRUD-API)
                                      → Mobile-First UI Optimierung (Touch, Breakpoints <768px, Bundle <200KB)
                                      → Offline-Fähigkeit (Service Worker + IndexedDB + Background Sync)
                                      → QR-Code-Scanner für schnelle Kontakterfassung (vCard/meCard Import)
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

**🎯 GESAMT-FORTSCHRITT: 10/36 PRs ✅ (28% done) | 4.0/15 Wochen | ETA: 2025-05-15**

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

**Nächster Schritt:** Thin Vertical Slice mit Feature-Flag `VITE_FEATURE_LEADGEN`

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