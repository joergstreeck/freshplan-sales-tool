# 🚀 CRM Master Plan V5 - Sales Command Center (Kompakt)

**🚀 START HIER (Pflicht-Lesefolge für neue Claude-Instanzen):**
1. **`/CLAUDE.md`** - Meta-Arbeitsregeln + Quick-Start
2. **`/docs/planung/TRIGGER_INDEX.md`** - Sprint-Trigger + 7-Dokumente-Reihenfolge
3. **`/docs/planung/CRM_AI_CONTEXT_SCHNELL.md`** - Business-Kontext
4. **`/docs/planung/PRODUCTION_ROADMAP_2025.md`** - Roadmap + Task-Klassen

**📊 Plan Status:** ✅ PLANNING COMPLETE – Implementation pending (CQRS-First)
**🔎 Status-Legende:** ✅ Planning COMPLETE · 🟡 In Progress · 🔄 Review · 🚫 Replaced
**📐 Definition of Done (DoD):** siehe Abschnitt „Foundation DoD & SLOs"
**📋 Architecture Decision Records (ADR):** siehe Abschnitt „ADR-Log"
**🎯 Owner:** Development Team + Product Team
**⏱️ Timeline:** Q4 2025 → Q2 2026
**🔧 Effort:** L (Large - Multi-Sprint Project)
**✅ PLANNING PHASE COMPLETE - ALLE MODULE:**

**🎯 BUSINESS MODULES (01-08):**
- **Module 01 Cockpit:** ✅ Planning COMPLETE – Implementation pending (100% Foundation Standards, 44 Production-Ready Artefakte)
- **Module 02 Neukundengewinnung:** ✅ 75% IMPLEMENTED – PR #103, #105, #110 merged
  - [Security Test Pattern](./features-neu/02_neukundengewinnung/artefakte/SECURITY_TEST_PATTERN.md) ✅
  - [Performance Test Pattern](./features-neu/02_neukundengewinnung/artefakte/PERFORMANCE_TEST_PATTERN.md) ✅
  - [Event System Pattern](./features-neu/02_neukundengewinnung/artefakte/EVENT_SYSTEM_PATTERN.md) ✅
- **Module 03 Kundenmanagement:** ✅ Planning COMPLETE – Implementation pending (100% Foundation Standards, 39 Production-Ready Artefakte)
- **Module 04 Auswertungen:** ✅ Planning COMPLETE – Implementation pending (97% Production-Ready, 12 Implementation-Files)
- **Module 05 Kommunikation:** ✅ Planning COMPLETE – Implementation pending (Enterprise-Ready, 41 Production-Ready Artefakte)
- **Module 06 Einstellungen:** ✅ Planning COMPLETE – Implementation pending (Enterprise Assessment A-, Settings-Engine + ABAC Security)
- **Module 07 Hilfe & Support:** ✅ Planning COMPLETE – Implementation pending (CAR-Strategy Help-System + Operations-Integration)
- **Module 08 Verwaltung:** ✅ Planning COMPLETE – Implementation pending (User Management + Permissions + System Administration)

**🏗️ INFRASTRUCTURE MODULES (00):**
- **Module 00 Sicherheit:** ✅ Planning COMPLETE – Implementation pending (ABAC + RLS Security Model + Multi-Territory Support, 13 Artefakte)
- **Module 00 Integration:** ✅ Planning COMPLETE – Implementation pending (CQRS Light Architecture + Gateway minimal)
- **Module 00 Betrieb:** ✅ Planning COMPLETE – Implementation pending (CQRS Light Operations für 5-50 Benutzer + Simple Monitoring)
- **Module 00 Skalierung:** ✅ Planning COMPLETE – Implementation pending (Territory + Seasonal-aware Autoscaling, 5 Copy-Paste-Ready Artefakte)

**🚨 NEXT:** Production Implementation Phase - Vollständige Planungsphase abgeschlossen mit 310+ Production-Ready Artefakten

**📋 LATEST UPDATE (26.09.2025):**
- ✅ **PR #110 MERGED:** FP-236 Security-Integration complete (23 Tests, P95 < 7ms, Gemini Review adressiert)
- ✅ **Sprint 2.1 COMPLETE:** Lead-Management mit ABAC/RLS, Events (LISTEN/NOTIFY), Performance validiert
- 🚀 **Next:** Sprint 2.1.1 P0 HOTFIX (PR #111) → Sprint 2.2 Kundenmanagement

**🚀 STRATEGIC DECISION (21.09.2025):** CQRS Light Migration-First Strategy confirmed - CQRS Light Foundation (1-2 Wochen Q4 2025) → Business-Module (Q1 2026) für kosteneffiziente interne Performance + Zero Doppelarbeit

## 🎯 Production-Ready Patterns (aus PR #110)

### **Copy-Paste Ready für alle Module:**
- **[Security Test Pattern](./features-neu/02_neukundengewinnung/artefakte/SECURITY_TEST_PATTERN.md)** - 23 Tests, @TestSecurity, Fail-Closed
  - ✅ Direkt nutzbar für **Modul 03** (Kundenmanagement) - Customer-Security-Tests
  - ✅ Direkt nutzbar für **Modul 05** (Kommunikation) - Thread-Access-Control
  - ✅ Template für **alle Module** - ABAC/RLS Testing-Standard
- **[Performance Test Pattern](./features-neu/02_neukundengewinnung/artefakte/PERFORMANCE_TEST_PATTERN.md)** - P95 < 200ms Validation
  - ✅ Direkt nutzbar für **Modul 04** (Auswertungen) - Analytics-Query-Performance
  - ✅ Helper-Methoden für **alle Module** - measureP95(), assertP95()
- **[Event System Pattern](./features-neu/02_neukundengewinnung/artefakte/EVENT_SYSTEM_PATTERN.md)** - LISTEN/NOTIFY mit AFTER_COMMIT
  - ✅ Direkt nutzbar für **Modul 05** (Kommunikation) - Email-Send-Events
  - ✅ Direkt nutzbar für **Modul 01** (Cockpit) - Dashboard-Widget-Updates
  - ✅ Cross-Module Event-Routing für **alle 8 Module**

## 📐 Foundation DoD & SLOs

### **CQRS Light Foundation (messbare DoD):**
- **Performance:** `api_request_p95_ms < 200` (PromQL: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))`)
- **Event System:** `listen_notify_lag_ms < 10000`, Event-Payload JSON-Schema validiert
- **Testing:** k6 Performance-Baseline grün (`k6_scenario_success_rate >= 0.95`), Unit-Tests ≥85%
- **Monitoring:** Distributed Tracing aktiv, Query-Performance-Dashboards operational

### **Security (ABAC + RLS v2) (messbare DoD):**
- **Access Control:** `security_contract_tests_passed = 1` (Owner/Non-Owner/Kollaborator/Manager-Override)
- **Territory Management:** `rls_policies_active = 1` für DE/CH/AT Datenräume
- **Audit Trail:** `audit_events_logged_count > 0` für kritische Operations
- **Lead Protection:** `lead_ownership_violations = 0` (6M + 60T + 10T Stop-Clock)

### **Settings-Registry Hybrid (messbare DoD):**
- **Performance:** `etag_hit_rate_pct >= 70`, `settings_fetch_p95_ms < 50`
- **Validation:** `json_schema_validation_active = 1` für alle Setting-Types
- **Cache:** `listen_notify_invalidation_success_rate >= 0.95`, Cache-Consistency-Tests grün
- **Scope:** `settings_hierarchy_functional = 1` (User/Tenant/Global), Merge-Engine operational

### **SLO-Kernwerte (CI-verdrahtbar mit PromQL):**
- **API Performance:** `api_request_p95_ms < 200` (normal), `< 500` (Peak/Seasonal)
  ```promql
  # Normal: histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{route!~".*health.*"}[5m])) by (le))
  # Peak: histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{route!~".*health.*"}[5m])) by (le))
  ```
- **Frontend Performance:** `bundle_size_bytes < 200000`, `web_vitals_lcp_p75_ms < 2500` (normal), `< 3000` (Peak)
- **Business Logic:** `lead_access_check_p95_ms < 50` (normal), `< 100` (Peak), `roi_calculator_p95_ms < 100` (normal), `< 200` (Peak)
  ```promql
  # Lead-Access: histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{operation="leadAccess"}[5m])) by (le))
  # ROI-Calculator: histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{operation="roiCalculator"}[5m])) by (le))
  ```
- **Infrastructure:** `etag_hit_rate_pct >= 70` (normal), `>= 60` (Peak), `listen_notify_lag_ms < 10000`

## 📋 ADR-Index (Architecture Decision Records)

**Zentrale Architektur-Entscheidungen mit verlinkten Dokumenten:**

- **ADR-0001:** CQRS Light statt Full-CQRS → `/docs/planung/adr/ADR-0001-cqrs-light.md`
- **ADR-0002:** PostgreSQL LISTEN/NOTIFY statt Event-Bus → `/docs/planung/adr/ADR-0002-listen-notify-over-eventbus.md`
- **ADR-0003:** Settings-Registry Hybrid JSONB + Registry → `/docs/planung/adr/ADR-0003-settings-registry-hybrid.md`
- **ADR-0004:** Territory = RLS-Datenraum, Lead-Protection = User-based → `/docs/planung/adr/ADR-0004-territory-rls-vs-lead-ownership.md`
- **ADR-0005:** Nginx+OIDC Gateway statt Kong/Envoy → `/docs/planung/adr/ADR-0005-nginx-oidc-gateway.md`
- **ADR-0006:** Mock-Governance (Business-Logic mock-frei) → `/docs/planung/adr/ADR-0006-mock-governance.md`
- **ADR-0007:** RLS Connection Affinity Pattern → `/docs/planung/adr/ADR-0007-rls-connection-affinity.md`

### **ADR-0001: CQRS Light statt Full-CQRS (21.09.2025)**
**Kontext:** Performance für 5-50 interne Nutzer mit Budget-Constraints
**Entscheidung:** CQRS Light mit einer DB + LISTEN/NOTIFY statt Event-Bus
**Begründung:** Kosteneffizient, <200ms Performance ausreichend, einfache Wartung
**Konsequenzen:** Eventuelle Skalierung >100 Nutzer erfordert Event-Bus-Migration
**Details:** → `/docs/planung/adr/ADR-0001-cqrs-light.md`

## Session Log
<!-- MP5:SESSION_LOG:START -->
- 2025-09-23 17:45 — System Infrastructure: V3.2 Auto-Compact-System vollständig implementiert (COMPACT_CONTRACT v2 + MP5-Anker + Trigger-Updates), Migration: V225, Tests: OK
- 2025-09-23 18:20 — System Infrastructure: V3.3 Branch-Gate Implementation abgeschlossen (Workflow-Lücke geschlossen, aktives Angebot statt passives Warten), Migration: V225, Tests: OK
- 2025-09-23 18:45 — Emergency Handover: 3 kritische V3.3 Verbesserungen identifiziert (main-commit-warning, Branch-Gate Prominenz, Feature-Branch Schutz), Migration: V225, Tests: OK
- 2025-09-23 19:00 — System Optimization: Trigger-Reihenfolge optimiert (Handover-First Strategy) + Context-Management definiert, Migration: V225, Tests: OK
- 2025-09-23 20:15 — Governance Implementation: Mock-Governance ADR-0006 + Standards/Snippets implementiert + TRIGGER_SPRINT_1_1 erweitert, Migration: V225, Tests: OK
- 2025-09-23 20:30 — SAFE MODE Handover: Mock-Governance Implementation COMPLETE + Handover 17:51 erstellt, Migration: V225, Status: Ready für Sprint 1.1
- 2025-09-25 20:30 — Security Retrofit: Sprint 1.5 + 1.6 RLS Connection Affinity Fix + Module Adoption (PR #106, #107 MERGED), Migration: V242/V243, Tests: OK
- 2025-09-23 18:45 — Sprint 1.1 Complete: CQRS Light Foundation operational + PR #94 mit Review-Fixes, Migration: V225, Tests: OK
- 2025-09-25 11:45 — Infrastruktur-Doku Update: Sprint 1.1-1.4 Status in alle /infrastruktur Dokumente eingepflegt, Phase 1 zu 100% COMPLETE (PR #102), Tests: OK
- 2025-09-25 02:00 — Territory-Klarstellung: PR #103 Impact dokumentiert - Territory ≠ Gebietsschutz! 8 Module korrigiert (Territory nur für Currency/Tax), Migration: n/a, Tests: OK
- 2025-09-25 00:55 — Sprint 2.1 PR #1: Territory Management ERFOLGREICH GEMERGED (PR #103) + Race Condition Fix via UserLeadSettingsService + ElementCollection Tables (V231), Migration: V231, Tests: ✅ All GREEN
- 2025-09-24 22:15 — Sprint 1.4 Complete: Foundation Quick-Wins (Cache + Prod-Config) + PR #102 merged, Phase 1: 100% COMPLETE
- 2025-09-23 19:40 — Sprint 1.1 MERGED: PR #94 erfolgreich in main + alle KI-Reviews umgesetzt + Sprint 3 Backlog erstellt, Migration: V226, Tests: OK
- 2025-09-23 21:00 — Sprint 1.2 PR #1 MERGED: Security Context Foundation (V227) + Code Review Fixes + Follow-Up dokumentiert, Migration: V227, Tests: OK
- 2025-09-23 22:30 — Sprint 1.3 PR #97 READY: Security Gates Enforcement (FP-231) + CORS-Trennung + Security Headers + CI-Hardening, alle Checks grün, wartet auf Review
- 2025-09-23 22:45 — Sprint 1.3 MERGED: PR #97 erfolgreich in main + bash arithmetic fixes + Follow-up Issue #98 erstellt, Migration: V227, Tests: OK
- 2025-09-24 00:39 — Sprint 1.3 PR #3 COMPLETE: Frontend Settings Integration mit ETag/304 (PR #100) + Settings Registry gemerged (PR #99), Migration: n/a, Tests: OK
- 2025-09-24 01:29 — Sprint 1.3 PR #2 SUBMITTED: Integration Testing Framework (PR #101) + Code Review Fixes umgesetzt, Migration: n/a, Tests: OK
- 2025-09-24 03:10 — Sprint 1.3 PR #2 MERGED: Foundation Integration Testing Framework abgeschlossen (PR #101)
  - CI Pipeline Split (PR-Fast <10min vs Nightly-Full ~30min)
  - Performance-Benchmarks mit P95-Metriken implementiert
  - ETag Hit-Rate Tracking ≥70% etabliert
  - Foundation Validation Script finalisiert
  - **Phase 1 Foundation 🔄 FINAL OPTIMIZATION** - Sprint 1.4 Quick-Wins läuft
  - **📊 Performance Report:** [phase-1-foundation-benchmark-2025-09-24.md](../performance/phase-1-foundation-benchmark-2025-09-24.md)
- 2025-09-25 03:10 — Sprint 2.1 PR #2 Implementation: Lead Endpoints & Queries mit Code Review Fixes
  - Lead REST API vollständig implementiert (CRUD + Activities + Collaborators)
  - Protection System mit 6/60/10 Regel + Stop-the-Clock Feature
  - Advanced Features aus Artefakten integriert: Email Activity Detection, Campaign Templates, FreshFoodz CI
  - Code Review Fixes: Exception Handling, Test Isolation, Performance-Optimierung
  - Migrations: V232 (campaign_templates), V233 (territories.active)
  - Tests: LeadResourceSimpleTest ✅ GREEN, Integration Tests pending JWT-Fix
- 2025-09-25 12:08 — Sprint 2.1 PR #105: Lead Endpoints Finalisierung mit HTTP-Semantics & Security Fixes
  - Strong ETags implementiert statt Weak ETags (HTTP Spec Compliance)
  - LeadDTO eingeführt gegen LazyInitializationException
  - RlsGucFilter für fail-closed Security mit PostgreSQL GUCs implementiert
  - PanacheQuery Fix für 400 Bad Request Fehler
  - Migrations: V240 (email_unique_index), V241 (activity_type_constraint mit CLOCK_*)
  - Tests: LeadResourceTest 13/13 ✅ GREEN (100% Pass Rate)
- 2025-09-25 18:42 — **Sprint 1.5 MERGED:** Security Retrofit - RLS Connection Affinity (PR #106) **KRITISCHER SECURITY FIX**
  - CDI Interceptor Pattern mit @RlsContext für garantierte Connection Affinity
  - Fail-closed RLS Policies + RlsBootstrapGuard für Production Safety
  - GUC-Keys umbenannt (keine PostgreSQL reserved words): app.user_context, app.role_context
  - EventSubscriber mit session-scoped GUCs + konfigurierbare Reconnect-Logic
  - Migrations: V242 (fail_closed_policies), V243 (update_guc_keys)
  - Tests: RlsConnectionAffinityTest ✅, RlsRoleConsistencyTest ✅
  - Gemini Review: "Exzellent und äußerst wichtig"
  - **Security Impact: Verhindert Datenleaks zwischen Territories/Tenants**
- 2025-09-25 20:45 — **Sprint 1.6 MERGED:** RLS Module Adoption - 5 kritische Services gesichert (PR #107)
  - GAP-Analyse: 33+ Services ohne @RlsContext identifiziert
  - Modul 02: 5 kritische Services mit @RlsContext annotiert (Sprint 2.1 kann fortgesetzt werden)
  - CI-Guard implementiert: tools/rls-guard.sh mit FP-armer Heuristik
  - RLS-Badge in allen 8 Modulen dokumentiert
  - ADR-0007 RLS Connection Affinity Pattern dokumentiert
  - Tests: Alle kritischen Checks grün, PR Template fix applied
- 2025-09-25 21:00 — **Hotfix FP-277:** Flyway V243 Checksum Fix (PR #108 MERGED)
  - Kritischer Production Blocker behoben (V243 Original wiederhergestellt)
  - V244 als idempotente Dokumentation hinzugefügt
  - Schema-Qualifizierung für Robustheit implementiert
- 2025-09-25 21:30 — **Sprint 2.1 PARTIAL:** Lead-Management 50% (PR #103, #105 MERGED)
  - FP-233: Territory Management ohne Gebietsschutz ✅ (PR #103)
  - FP-234: Lead-Capture-System ✅ (PR #105 - Lead REST API mit User-Protection)
  - FP-235: Follow-up Automation (T+3/T+7) ❌ OFFEN
  - FP-236: Security-Integration (ABAC/RLS Tests) ❌ OFFEN
  - 13/13 Tests grün, Migrations V232-V241 deployed
- 2025-09-26 18:45 — **Sprint 2.1 COMPLETE:** PR #110 FP-236 Security-Integration MERGED
  - 23 Security/Performance/Event-Tests implementiert (alle grün)
  - Performance P95 < 7ms validiert (Requirement: < 200ms)
  - PostgreSQL LISTEN/NOTIFY mit AFTER_COMMIT und Idempotenz
  - Gemini Code Review vollständig adressiert
  - ManagedExecutor, @TestSecurity, TestEventCollector implementiert
- 2025-09-26 19:45 — **Nachdokumentation:** PR #110 Pattern-Artefakte erstellt und überall referenziert
  - 3 Copy-Paste Ready Patterns dokumentiert (Security, Performance, Event)
  - Alle Hauptdokumente mit Pattern-Links erweitert
  - Alle Sprint-Dokumente mit Pattern-Nutzung ergänzt
  - KI-Review Punkte umgesetzt (RLS-Disclaimer, CQRS Backbone)
  - Migration: n/a, Tests: OK
<!-- MP5:SESSION_LOG:END -->

## Next Steps
<!-- MP5:NEXT_STEPS:START -->
- **🚀 Sprint 2.1.1 P0 HOTFIX:** PR #111 implementieren
  - Integration Gaps beheben (Event Distribution, Dashboard Widget, Metrics)
  - Siehe [INTEGRATION_STATUS.md](./infrastruktur/INTEGRATION_STATUS.md)
- **Sprint 2.1 Finalisierung:** FP-235 Follow-up Automation
  - T+3/T+7 Sample-Management implementieren
- **Dann Sprint 2.2:** Kundenmanagement
  - Field-based Customer Architecture mit 39 Artefakten
  - RLS Migration für Customer Services (FP-273)
  - 10-12h, 5 PRs geplant (FP-237 bis FP-241)
- **Follow-up RLS Migration:** Domain Services
  - FP-274: 15+ Domain Services mit @RlsContext
  - CI-Guard implementieren und aktivieren
  - RLS-Badge in alle Module 01-08 einfügen
- Sprint 2.1: Kann erst nach Sprint 1.6 fortfahren
- Follow-up Tickets:
  - FP-272: Modul 02 Services annotieren (P0)
  - FP-273: Modul 03 Migration planen (P1)
  - FP-274: CI-Rule für @RlsContext (P1)
  - FP-275: Import/Batch RLS-sicher machen (P1)
  - FP-276: Monitoring via Agroal/Micrometer (P2)
<!-- MP5:NEXT_STEPS:END -->

## Risks
<!-- MP5:RISKS:START -->
- ✅ ~~GUC-Context auf falscher Connection~~ - BEHOBEN durch Sprint 1.5 Connection Affinity
- **Sprint 2.1 blockiert:** Modul 02 Services ohne @RlsContext - Mitigation: Sprint 1.6 mit P0
- 33+ Services ohne RLS-Schutz - Mitigation: Systematische Migration via Sprint 1.6
- Zwei Dev-Server laufen parallel (Bash IDs: 64b7dc, 1db55e) - Mitigation: Beim nächsten Start bereinigen
<!-- MP5:RISKS:END -->

## Decisions
<!-- MP5:DECISIONS:START -->
- 2025-09-25 — ADR-0007: RLS Connection Affinity Pattern für alle Module verbindlich
- 2025-09-25 — Sprint 1.6 eingefügt: Module-Migration zu @RlsContext vor Phase 2
- 2025-09-25 — CI-Guard Pattern: Heuristik statt Regex für FP-arme Prüfung
- 2025-09-25 — Territory ohne Gebietsschutz bestätigt: Lead-Protection rein user-basiert (ADR-0004)
- 2025-09-25 — Service-Pattern für Thread-Safety: Statische DB-Methoden durch CDI-Services ersetzt
- 2025-09-24 — Settings Registry Production-Ready: Race Condition Prevention + 304 Support
- 2025-09-23 — ADR-0006 angenommen: Mock-Governance (Business-Logic mock-frei)
<!-- MP5:DECISIONS:END -->

### **ADR-0002: PostgreSQL LISTEN/NOTIFY statt Event-Bus (18.09.2025)**
**Kontext:** Event-System für Settings-Invalidation + Cross-Module-Communication
**Entscheidung:** Postgres LISTEN/NOTIFY mit JSON-Payload, kein CloudEvents/Kafka
**Begründung:** Minimale Infrastruktur, bereits vorhandene DB-Expertise
**Konsequenzen:** Event-System auf Single-DB limitiert, aber ausreichend für Scope
**Details:** → `/docs/planung/adr/ADR-0002-listen-notify-over-eventbus.md`

### **ADR-0003: Settings-Registry Hybrid JSONB + Registry (15.09.2025)**
**Kontext:** User/Tenant/Global Settings mit Performance + Typisierung
**Entscheidung:** JSONB Storage + Type-Registry + ETag-Caching
**Begründung:** Flexibilität + Performance + Entwicklerfreundlichkeit
**Konsequenzen:** Leichte Komplexität, aber optimale Balance für unseren Use-Case
**Details:** → `/docs/planung/adr/ADR-0003-settings-registry-hybrid.md`

### **ADR-0004: Territory = RLS-Datenraum, Lead-Protection = User-based (12.09.2025)**
**Kontext:** Gebietsschutz-Missverständnis vs. tatsächliche Anforderungen
**Entscheidung:** Territory als Datenraum (DE/CH/AT), Lead-Protection user-basiert
**Begründung:** Klarere Trennung, flexiblere Lead-Workflows ohne Gebiets-Constraints
**Konsequenzen:** Dokumentation muss Territory-Begriff präzise definieren
**Details:** → `/docs/planung/adr/ADR-0004-territory-rls-vs-lead-ownership.md`

### **ADR-0005: Nginx+OIDC Gateway statt Kong/Envoy (10.09.2025)**
**Kontext:** API Gateway für Authentication + Rate-Limiting
**Entscheidung:** Minimales Nginx+OIDC Setup, Kong/Envoy als optional/später
**Begründung:** YAGNI für internes Tool, Budget-effizient, schnelle Implementation
**Konsequenzen:** Advanced Gateway-Features (Tracing, Analytics) später nachrüstbar
**Details:** → `/docs/planung/adr/ADR-0005-nginx-oidc-gateway.md`

## 🎯 Executive Summary (für Claude)

**Mission:** Entwicklung eines intelligenten Sales Command Centers für B2B-Convenience-Food-Vertrieb an Gastronomiebetriebe
**Problem:** Fragmentierte Vertriebsprozesse, manuelle Workflows, fehlende Insights für komplexe B2B-Beratungsverkäufe von Convenience-Food-Produkten
**Solution:** Integrierte CRM-Plattform mit Field-Based Architecture und Event-Driven Communication speziell für Cook&Fresh® B2B-Food-Vertrieb
**Impact:** 3x schnellere Lead-Qualifizierung, 2x höhere Conversion durch ROI-basierte Beratung, vollständige Sales-Process-Automation

**🏆 MILESTONE ERREICHT:** VOLLSTÄNDIGE PLANUNGSPHASE ABGESCHLOSSEN - Alle Business-Module (01-08) + Infrastructure-Module (00: Sicherheit, Integration, Betrieb) sind COMPLETE und production-ready. Enterprise-Grade Quality mit 300+ Production-Ready Artefakten verfügbar.

**🆕 AKTUELL (21.09.2025) - PLANUNGSPHASE COMPLETE + STRATEGIC IMPLEMENTATION-REIHENFOLGE:**
- **Governance Infrastructure:** 10/10 Claude-Ready, Settings-MVP Pack (9.7/10) integriert, atomare Planung Standards etabliert
- **Planungsmethodik:** Von 801 auf 252 Zeilen optimiert, garantiert 9+/10 Claude-Readiness für neue Module
- **Module 01-08:** ✅ **ALLE BUSINESS-MODULE COMPLETE** - Foundation Standards + Enterprise-Grade Artefakte
- **Module 00 Infrastructure:** ✅ **ALLE INFRASTRUCTURE-MODULE COMPLETE** - Sicherheit + Integration (CQRS Light) + Betrieb (Cost-Efficient) + Skalierung (Territory + Seasonal-aware) mit External AI Excellence
- **🚀 CQRS Light Migration-First Strategy:** Strategic Analysis complete - CQRS Light Foundation (1-2 Wochen) vor Business-Module spart 4-6 Wochen Doppelarbeit + kosteneffiziente Performance für interne Nutzung

## 🍽️ FreshFoodz Business-Kontext (B2B-Convenience-Food-Hersteller)

**Unser Geschäftsmodell:**
- **Produkt:** Cook&Fresh® Convenience-Food mit patentierter Konservierungstechnologie
- **Haltbarkeit:** Bis 40 Tage ohne künstliche Konservierungsstoffe
- **Zielgruppen:** Multi-Channel B2B-Vertrieb
  - **Direktkunden:** Restaurants, Hotels, Betriebsgastronomie, Vending-Betreiber
  - **Partner-Channel:** Lieferanten, Händler, Wiederverkäufer (B2B2B)
- **Verkaufsansatz:** "Genussberater" - kanal-spezifische ROI-basierte Beratung

**Sales-Prozess-Besonderheiten (Multi-Channel):**
```yaml
DIREKTKUNDEN (Restaurants, Hotels, Betriebsgastronomie, Vending):
1. Lead-Qualifizierung → Betriebstyp, Größe, Konzept, Entscheidungsstruktur
2. Bedarf-Analyse → Setup, Personal, aktuelle Herausforderungen, Volumen
3. ROI-Demonstration → Kosteneinsparung vs. Investition (kanal-spezifisch)
4. Produkt-Sampling → Gratis Produktkatalog + individualisierte Sample-Boxes
5. Test-Phase → Kunde testet im echten Betrieb (2-4 Wochen)
6. Individuelles Angebot → Basierend auf Produktmix und Volumen
7. Verhandlung → Mengenrabatte, Lieferkonditionen, Service-Level
8. Abschluss → Langfristige Lieferverträge + Account-Management

PARTNER-CHANNEL (Lieferanten, Händler, Wiederverkäufer):
1. Partner-Qualifizierung → Kundenbasis, Vertriebskapazität, Markt-Coverage
2. Portfolio-Analyse → Wie passt Cook&Fresh® in deren Sortiment?
3. Margin-Struktur → Partnerkonditionen, Support-Level, Incentives
4. Pilot-Programm → Test mit ausgewählten End-Kunden
5. Rollout-Planung → Schrittweise Expansion, Marketing-Support
6. Partner-Enablement → Training, Sales-Tools, Produkt-Schulungen
7. Performance-Tracking → Umsatz-Ziele, Market-Penetration
8. Strategic Partnership → Langfristige Kooperation, exklusive Gebiete
```

**CRM-Anforderungen für Multi-Channel B2B-Food-Vertrieb:**
- **Channel-Management:** Direktkunden vs. Partner-Channel mit verschiedenen Prozessen
- **ROI-Kalkulation:** Kanal-spezifische Berechnungen (Restaurant vs. Hotel vs. Partner-Margin)
- **Produkt-Matching:** 200+ Cook&Fresh® Produkte für verschiedene Betriebstypen
- **Sample-Management:** Tracking für End-Kunden UND Partner-Demos
- **Partner-Enablement:** Tools und Materialien für Wiederverkäufer
- **Territory-Management:** Gebietsschutz und Konflikt-Vermeidung zwischen Kanälen

## ⚙️ Arbeitsmodus & PR-Hygiene

### **Team-Setup: Jörg + Claude (sequenziell)**
- **Branch-Schema:** `feature/[module]-[sub-feature]-[component]-FP-XXX`
- **PR-Größen:** <50 Files, <2000 Lines, Build <15min
- **Coverage-Gates:** ≥80% PR-weise + keine Verschlechterung; Modul-Ziel ≥85%
- **Sprint-Zyklus:** Ein Sub-Feature inkl. Backend+Frontend+Tests → Merge → Nächstes

### **Migration-Workflow (kritisch):**
```bash
# NIEMALS Migration-Nummern hardcoden!
MIGRATION=$(./scripts/get-next-migration.sh | tail -1)
# Fallback: ls -la backend/src/main/resources/db/migration/ | tail -3
```

### **CI-Auto-Fix-Grenzen:**
- **NUR Feature-Branches** (niemals main!)
- **Required Reviews bleiben** bestehen
- **Token-Scope minimal** (read:repo, write:packages falls nötig)

### **Trigger-Workflow (jede Session):**
1. **`/docs/planung/TRIGGER_INDEX.md` lesen** (7-Dokumente-Reihenfolge)
2. **Master Plan V5 checken** (aktueller Stand)
3. **Migration-Check** mit Script ausführen
4. **TodoWrite** für systematisches Task-Tracking

## 🗺️ Sidebar-Navigation & Feature-Struktur

**Navigation-Architektur:**
```
├── 🏠 Mein Cockpit                # Dashboard & Insights
├── 👤 Neukundengewinnung          # Lead Generation & Campaigns
├── 👥 Kundenmanagement            # CRM Core (M4 Pipeline)
├── 📊 Auswertungen               # Analytics & Reports
├── 💬 Kommunikation              # Team Communication
├── ⚙️ Einstellungen              # User Configuration
├── 🆘 Hilfe & Support            # Help System
└── 🔐 Administration             # Admin Functions
```

**Feature-Module-Mapping:** [Sidebar-basierte Module](./features-neu/)

## 🔗 Infrastruktur-Koordination

**Kritische Infrastructure Dependencies:**
| Infrastructure Plan | Status | Impact auf Features |
|---------------------|--------|-------------------|
| [Test Debt Recovery](./infrastruktur/TEST_DEBT_RECOVERY_PLAN.md) | 🔴 Critical | Blockiert Feature-Velocity |
| [SmartLayout Migration](./infrastruktur/SMARTLAYOUT_MIGRATION_PLAN.md) | 🔄 In Progress | UI Performance +50% |
| [CQRS Migration](./infrastruktur/CQRS_MIGRATION_PLAN.md) | 🟡 Review | Read-Performance +200% |
| [Performance Module](./features-neu/00_infrastruktur/leistung/README.md) | ✅ **COMPLETE** | <200KB Bundle + <100ms API Excellence |
| [Scaling Module](./features-neu/00_infrastruktur/skalierung/README.md) | ✅ **COMPLETE** | Territory + Seasonal-aware Autoscaling |

**Infrastructure-Koordination:** [Infrastructure Master Index](./infrastruktur/00_MASTER_INDEX.md)

## 🗺️ Feature Implementation Roadmap

### **Q4 2025: Foundation Standards COMPLETED → Implementation Ready**

**🎯 MAJOR MILESTONE:** Alle Kern-Module haben Enterprise-Grade Foundation Standards erreicht und sind bereit für Production Implementation.

**📦 ENTERPRISE-GRADE ARTEFAKTE VERFÜGBAR:**
- **Module 01 Cockpit:** 44 Production-Ready Artefakte (API, Backend, Frontend, SQL, Testing, CI/CD)
- **Module 02 Neukundengewinnung:** Foundation Standards Artefakte (design-system, openapi, backend, frontend, sql, k6, docs)
- **Module 03 Kundenmanagement:** 39 Production-Ready Deliverables (EXCEPTIONAL Quality Rating 10/10)
- **Module 04 Auswertungen:** 12 Copy-Paste-Ready Implementation-Files (97% Production-Ready, Gap-Closure PERFECT 9.7/10)
- **Module 05 Kommunikation:** 41 Production-Ready Artefakte (Best-of-Both-Worlds: DevOps Excellence + Business Logic Perfektion, 8.6/10 Enterprise-Ready)
- **Gesamt:** 175+ Enterprise-Grade Implementierungen ready for copy-paste Integration

#### **01_mein-cockpit** [Technical Concept](./features-neu/01_mein-cockpit/technical-concept.md)
- **Status:** ✅ **100% FOUNDATION STANDARDS COMPLIANCE ERREICHT** - Enterprise Assessment A+ (95/100)
- **Artefakte:** 44 Production-Ready Implementierungen verfügbar | [Enterprise Assessment](./features-neu/01_mein-cockpit/ENTERPRISE_ASSESSMENT_FINAL.md)
- **Timeline:** ✅ ALLE 4 PHASEN ABGESCHLOSSEN - Ready for Production Deployment
- **Code-Basis:** ✅ Vollständige Implementation mit ABAC Security, ROI-Calculator, Multi-Channel Dashboard

#### **02_neukundengewinnung** [Complete Module Planning](./features-neu/02_neukundengewinnung/)
- **Status:** ✅ **Foundation Standards COMPLETED** (92%+ Compliance erreicht)
- **Artefakte:** design-system/, openapi/, backend/, frontend/, sql/, k6/, docs/ | [Compliance Matrix](./features-neu/02_neukundengewinnung/shared/docs/compliance_matrix.md)
- **Timeline:** 20-24 Wochen Complete Module Development (Phase 1: 12w, Phase 2: 8w, Phase 3: 4w)
- **Dependencies:** all.inkl Mail-Provider, UserLeadSettings Entity - Ready for Integration
- **Implementation:** [Shared Docs](./features-neu/02_neukundengewinnung/shared/docs/) | [Finale Roadmap](./features-neu/02_neukundengewinnung/diskussionen/2025-09-18_finale-entwicklungsroadmap.md)

**email-posteingang/** [Technical Concept](./features-neu/02_neukundengewinnung/email-posteingang/technical-concept.md)
- **Status:** ✅ Technical Concept abgeschlossen, KI-Production-Specs integriert | **Timeline:** Phase 1 (Woche 1-12) | **Dependencies:** all.inkl Integration

**lead-erfassung/** [Technical Concept](./features-neu/02_neukundengewinnung/lead-erfassung/technical-concept.md)
- **Status:** ✅ Technical Concept abgeschlossen, Handelsvertretervertrag-konform | **Timeline:** Phase 1 (Woche 1-12) | **Dependencies:** UserLeadSettings Entity

**kampagnen/** [Technical Concept](./features-neu/02_neukundengewinnung/kampagnen/technical-concept.md)
- **Status:** ✅ Technical Concept abgeschlossen, Multi-Touch-Attribution | **Timeline:** Phase 2 (Woche 13-20) | **Dependencies:** Email+Lead Foundation

#### **03_kundenmanagement** [Technical Concept](./features-neu/03_kundenmanagement/technical-concept.md) | [README](./features-neu/03_kundenmanagement/README.md)
**🏛️ Enterprise CRM-Platform Status:** ✅ **100% Foundation Standards Compliance - Production-Ready**

**Platform-Optimierung** [Artefakte](./features-neu/03_kundenmanagement/artefakte/)
- **Status:** ✅ **Enterprise-Level Implementation** (100% Foundation Standards Compliance)
- **Qualität:** EXCEPTIONAL (10/10) - Enterprise-Grade Standards erreicht
- **Artefakte:** 39 Production-Ready Deliverables (API-Specs, Backend-Services, Frontend-Components, SQL-Schemas, Testing-Suite)
- **Timeline:** Ready for Implementation - Alle Foundation Standards erfüllt
- **Achievement:** Vollständige Enterprise CRM-Platform mit ABAC Security, Theme V2, API Standards, Testing 80%+

**customer-management/** Dashboard-Hub (Route: `/customer-management`)
- **Status:** ✅ Production-Ready (389 LOC) + "Neuer Kunde" Button | **Timeline:** Wartung | **Issues:** 🔴 Dashboard-Bug (falsche Route-Pfade)

**customers/** Enterprise Customer-Liste (Route: `/customers` → `/customer-management/customers`)
- **Status:** ✅ Production-Ready (400+276 LOC) + "Neuer Kunde" Button | **Timeline:** Route-Migration | **Dependencies:** Routen-Konsolidierung

**opportunities/** Kanban-Pipeline (Route: `/customer-management/opportunities`)
- **Status:** ✅ Production-Ready (799 LOC Drag&Drop) | **Timeline:** Integration-Tests | **Dependencies:** Dashboard-Bug-Fix

**activities/** Activity-Timeline (Route: `/customer-management/activities`)
- **Status:** 🔴 Navigation vorhanden, kein Code | **Timeline:** Woche 6-8 | **Dependencies:** Activity-Implementation

**🚨 Kritische Gaps:** Field-Backend-Mismatch (Frontend field-ready, Backend entity-based)

#### **08_administration** [README](./features-neu/08_administration/README.md)
**🏛️ Enterprise Administration Platform Status:** ✅ **PHASE 1 Complete, PHASE 2 Fully Planned**

**phase-1-core/** [Technical Concept](./features-neu/08_administration/phase-1-core/technical-concept.md)
- **Status:** ✅ ABAC Security + Audit System + Monitoring (95%+ Compliance) | **Timeline:** COMPLETE | **Dependencies:** None

**phase-2-integrations/** [Technical Concept](./features-neu/08_administration/phase-2-integrations/technical-concept.md) | [Implementation Roadmap](./features-neu/08_administration/phase-2-integrations/implementation-roadmap.md) | [Artefakte](./features-neu/08_administration/phase-2-integrations/artefakte/)
- **Status:** ✅ **VOLLSTÄNDIG GEPLANT** - Ready for Implementation (Quality Score 9.6/10) | **Timeline:** 2-3 Wochen (2025-10-07 → 2025-10-21) | **Dependencies:** Phase 1 deployed
- **Qualität:** OUTSTANDING - 26 Production-Ready Files (296 SQL + 394 Java + OpenAPI 3.1 + React)
- **Features:** Lead-Protection-System + Multi-Provider AI + Sample Management + External Integrations
- **Artefakte:** Strukturiert nach Planungsmethodik.md (sql-templates/, backend-java/, openapi-specs/, frontend-components/, configuration/)
- **Ready for:** Sofortige Implementation mit Copy-Paste Deployment (<1 Tag Setup)

**audit-dashboard/** [Technical Concept](./features-neu/08_administration/audit-dashboard/technical-concept.md)
- **Status:** ✅ FC-012 funktional | **Timeline:** Phase 1 COMPLETE | **Dependencies:** DONE

**benutzerverwaltung/** [Technical Concept](./features-neu/08_administration/benutzerverwaltung/technical-concept.md)
- **Status:** ✅ Keycloak-Integration funktional | **Timeline:** Phase 1 COMPLETE | **Dependencies:** DONE

**hilfe-konfiguration/hilfe-system-demo/** [Technical Concept](./features-neu/08_administration/hilfe-konfiguration/hilfe-system-demo/technical-concept.md)
- **Status:** ✅ Funktional | **Timeline:** Woche 1 (Integration-Test) | **Dependencies:** Help-System Framework

### **Q1 2026: Communication & Settings**

#### **05_kommunikation** [Technical Concept](./features-neu/05_kommunikation/technical-concept.md)
- **Status:** ✅ **COMPLETE + BEST-OF-BOTH-WORLDS INTEGRATION** (8.6/10 Enterprise-Ready)
- **Artefakte:** 41 Production-Ready Files (DevOps Excellence + Business Logic Perfektion) | [Vollständige Planungsdokumentation](./features-neu/05_kommunikation/README.md)
- **Timeline:** ✅ ALLE PHASEN ABGESCHLOSSEN - 10-12 Wochen Hybrid-Synthese Ready for Production Implementation
- **Implementation:** Best-of-Both-Worlds: KI DevOps-Excellence + Claude Business-Logic-Perfektion, SLA-Engine T+3/T+7, Shared Email-Core, Enterprise CI/CD
- **Strategic Achievement:** Paradebeispiel für strategische KI-Zusammenarbeit mit systematischer Analyse und Hybrid-Synthese

#### **06_einstellungen**
**mein-profil/** [Technical Concept](./features-neu/06_einstellungen/mein-profil/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 1-2 | **Dependencies:** User Profile API

**benachrichtigungen/** [Technical Concept](./features-neu/06_einstellungen/benachrichtigungen/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 3-4 | **Dependencies:** Notification Preferences

**darstellung/** [Technical Concept](./features-neu/06_einstellungen/darstellung/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 5-6 | **Dependencies:** Theme System

**sicherheit/** [Technical Concept](./features-neu/06_einstellungen/sicherheit/technical-concept.md)
- **Status:** 📋 Geplant (FC-015 Migration) | **Timeline:** Woche 7-8 | **Dependencies:** Rights & Roles System

#### **04_auswertungen** [Technical Concept](./features-neu/04_auswertungen/technical-concept.md)
- **Status:** ✅ **97% PRODUCTION-READY** - Gap-Closure PERFECT (9.7/10)
- **Artefakte:** 12 Copy-Paste-Ready Implementation-Files | [Artefakte](./features-neu/04_auswertungen/artefakte/)
- **Timeline:** 2-3 Wochen Implementation → Q4 2025 Woche 4-6
- **Implementation:** JSONL-Streaming, ABAC-Security, WebSocket Real-time, Universal Export Integration

**Analytics Platform Status:**
- **ReportsResource.java:** Thin Controller-Wrapper für Analytics-Services ✅ Ready
- **Database-Views:** SQL-Projections mit Performance-Indices ✅ Ready
- **Export-Framework:** Universal Export + JSONL-Streaming für Data Science ✅ Ready
- **Security:** ABAC Territory-Scoping + JWT-Integration ✅ Ready

### **Q1 2026: Extended Features & Help System**

#### **07_hilfe-support**
**erste-schritte/** [Technical Concept](./features-neu/07_hilfe-support/erste-schritte/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 1-2 | **Dependencies:** Onboarding Framework

**handbuecher/** [Technical Concept](./features-neu/07_hilfe-support/handbuecher/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 3-4 | **Dependencies:** Documentation System

**video-tutorials/** [Technical Concept](./features-neu/07_hilfe-support/video-tutorials/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 5-6 | **Dependencies:** Video Streaming

**haeufige-fragen/** [Technical Concept](./features-neu/07_hilfe-support/haeufige-fragen/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 7-8 | **Dependencies:** FAQ System

**support-kontaktieren/** [Technical Concept](./features-neu/07_hilfe-support/support-kontaktieren/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 9-10 | **Dependencies:** Ticketing System

## 🎯 Aktuelle Sprint-Woche: Q4 2025, Woche 1

### 🔥 Nächste 3 konkrete Aktionen:
1. **01_mein-cockpit:** ✅ Technical Concept abgeschlossen → Phase 1 Implementation starten
2. **Trigger-Texte V3.0:** ✅ **ABGESCHLOSSEN** - Vollständig implementiert
3. **Feature-Diskussion:** Mit anderen KIs über CRM_AI_CONTEXT_SCHNELL.md möglich

### ⚠️ Aktuelle Blocker:
- **Dokumentations-Strategie:** ✅ **GELÖST** - Duale Strategie implementiert
- **Compact-Problem:** ✅ **UMGANGEN** - Robust Handover System etabliert
- **M8 Calculator** Integration fehlt für ActionCenter (bleibt)

## Next Steps
<!-- MP5:NEXT_STEPS:START -->
- Sprint 2.1 PR #2: Lead Endpoints implementieren (User-basierte Protection)
- Sprint 2.1 PR #3: Lead UI Components (Territory nur für Currency/Tax Display)
- Frontend Bundle-Optimierung von 750KB auf <200KB angehen
- Legacy-Docs: Territory-Missverständnisse in älteren Dokumenten korrigieren
<!-- MP5:NEXT_STEPS:END -->

## 🎯 Critical Success Metrics

### Q4 2025 Targets:
- **Cockpit + Kundenmanagement + Administration:** 100% functional
- **Page Load:** <200ms P95 (via SmartLayout + CQRS)
- **Test Coverage:** 80%+ (via Test Debt Recovery)

### Business Impact Ziele:
- **Lead-Processing:** 3x schneller durch Automation
- **Conversion Rate:** 2x höher durch Guided Workflows
- **Sales Cycle:** -30% durch proaktive Workflows

## 🤖 Claude Handover Section

**Aktueller Master-Plan-Stand:**
CRM Master Plan V5 kompakt refactoriert nach PLANUNGSMETHODIK. Infrastructure-Koordination über Master Index etabliert, Feature-Development mit klarer Q4 2025 → Q2 2026 Timeline. Sidebar-basierte Feature-Struktur implementiert.

**Nächste strategische Aktionen:**
1. 01_mein-cockpit: ✅ Technical Concept abgeschlossen → ChannelType Entity erweitern für Phase 1
2. Test Debt Recovery starten (kritische Infrastruktur-Blockade)
3. FC-005 Customer Management: Field-Based Architecture finalisieren

**Kritische Koordinations-Punkte:**
- Infrastructure und Feature-Development parallel ausführen
- Test Debt Recovery blockiert Feature-Velocity → höchste Priorität
- SmartLayout Performance-Gains unterstützen CRM User-Experience
- Alle Technical Concepts nur Claude-optimiert verlinken (keine Diskussionen/Human-Guides)

**Master-Plan-Integration:**
Einziger strategischer Master Plan. Alle Infrastructure-Pläne über Master Index koordiniert. Feature-Development folgt Sidebar-Navigation mit Technical Concepts als einzige Detail-Referenz.

## 📚 Foundation Knowledge (für Claude)

### 🎯 **Core Standards - IMMER ZUERST LESEN:**
- **[Design System](./grundlagen/DESIGN_SYSTEM.md)** - FreshFoodz CI (#94C456, #004F7B, Antonio Bold, Material-UI v5+)
- **[API Standards](./grundlagen/API_STANDARDS.md)** - OpenAPI 3.1, RBAC-Patterns, Error-Handling
- **[Coding Standards](./grundlagen/CODING_STANDARDS.md)** - TypeScript import type, PascalCase, 80-100 chars
- **[Security Guidelines](./grundlagen/SECURITY_GUIDELINES.md)** - ABAC, Territory-Scoping, Audit-Trail
- **[Performance Standards](./grundlagen/PERFORMANCE_STANDARDS.md)** - P95 <200ms, Bundle <500KB, Coverage >90%
- **[Performance Module](./features-neu/00_infrastruktur/leistung/README.md)** - ✅ COMPLETE: <200KB Bundle + <100ms API Excellence (9.8/10)
- **[Scaling Module](./features-neu/00_infrastruktur/skalierung/README.md)** - ✅ COMPLETE: Territory + Seasonal-aware Autoscaling (9.8/10)
- **[Testing Guide](./grundlagen/TESTING_GUIDE.md)** - Given-When-Then, 80% Coverage, Integration-Tests

### 🛠️ **Development & Quality:**
- **[Component Library](./grundlagen/COMPONENT_LIBRARY.md)** - Reusable UI-Components
- **[Development Workflow](./grundlagen/DEVELOPMENT_WORKFLOW.md)** - Git-Flow, PR-Process, CI/CD
- **[Code Review Standard](./grundlagen/CODE_REVIEW_STANDARD.md)** - Qualitätssicherung
- **[Database Migration Guide](./grundlagen/DATABASE_MIGRATION_GUIDE.md)** - Migration-Regeln & Registry
- **[Business Logic Standards](./grundlagen/BUSINESS_LOGIC_STANDARDS.md)** - Domain-Logic-Patterns

### 🚑 **Debug & Troubleshooting:**
- **[Debug Cookbook](./grundlagen/DEBUG_COOKBOOK.md)** - Symptom-basierte Problemlösung
- **[TypeScript Guide](./grundlagen/TYPESCRIPT_IMPORT_TYPE_GUIDE.md)** - Import Type Probleme
- **[CI Debugging Lessons](./grundlagen/CI_DEBUGGING_LESSONS_LEARNED.md)** - Systematische Debug-Methodik
- **[Keycloak Setup](./grundlagen/KEYCLOAK_SETUP.md)** - Authentication Setup

### 🔄 Workflow-Dokumente:
- **[CI Debugging Strategy](./workflows/CI_DEBUGGING_STRATEGY.md)** - CI-Methodik
- **[ESLint Cleanup](./workflows/ESLINT_CLEANUP_SAFE_APPROACH.md)** - Maintenance

### ⚡ Quick-Troubleshooting:
- **Frontend Issues:** White Screen, Failed to fetch → Debug Cookbook
- **Backend Issues:** 401 Unauthorized, No Test Data → Debug Cookbook
- **CI Issues:** HTTP 500, Race Conditions → CI Debugging Lessons
- **TypeScript:** Import Errors → TypeScript Guide

---
**📋 Dokument-Zweck:** Kompakte Planungsübersicht für Claude
**🔗 Für KI-Instanzen:** → [CRM AI Context Schnell](/docs/planung/CRM_AI_CONTEXT_SCHNELL.md)
**🔄 Letzte Aktualisierung:** 2025-09-19 - Foundation Standards COMPLETED für alle Kern-Module (01, 02, 03, 04, 05) mit Best-of-Both-Worlds Integration