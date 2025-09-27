---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "analyse"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# 📁 Verzeichnisstruktur-Analyse: Modul 02 Neukundengewinnung

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Verzeichnisstruktur-Analyse

**📅 Erstellt:** 2025-09-27
**🎯 Zweck:** Vollständige Bestandsaufnahme der aktuellen Modul-Struktur nach Legacy-Split
**📊 Status:** 51 Verzeichnisse, 122 Dateien analysiert

**⚠️ Stub‑Verzeichnisse:** Temporär (2 Sprints), nur Redirects → bitte nicht browsen; Inhalte liegen in `legacy-planning/`.

---

## 🗂️ **VOLLSTÄNDIGE VERZEICHNISSTRUKTUR**

```
02_neukundengewinnung/
├── .github/
│   └── workflows/
│       └── ci-module02.yml
├── analyse/
│   ├── _index.md
│   ├── analysis-2025-09-18-obsoleted.md
│   ├── API_CONTRACT.md
│   ├── INVENTORY.md
│   ├── RESEARCH_ANSWERS.md
│   └── VALIDATED_FOUNDATION_PATTERNS.md
├── artefakte/
│   ├── EVENT_SYSTEM_PATTERN.md
│   ├── PERFORMANCE_TEST_PATTERN.md
│   └── SECURITY_TEST_PATTERN.md
├── backend/
│   ├── analyse/                             # (leer)
│   ├── konzepte/                            # (leer)
│   ├── _index.md
│   └── pom-jacoco-config.xml
├── diskussionen/
│   ├── .DS_Store
│   ├── 2025-09-18_all-inkl-integration-spezifikation.md
│   ├── 2025-09-18_ausbau-strategie-diskussion.md
│   ├── 2025-09-18_complete-module-development-request.md
│   ├── 2025-09-18_finale-entwicklungsroadmap.md
│   ├── 2025-09-18_finale-ki-specs-bewertung.md
│   ├── 2025-09-18_handelsvertretervertrag-lead-requirements.md
│   ├── 2025-09-18_ki-complete-module-wuerdigung.md
│   ├── 2025-09-18_ki-production-feedback-wuerdigung.md
│   ├── 2025-09-18_ki-request-openapi-sql.md
│   ├── 2025-09-18_system-entscheidungen-ki-empfehlungen.md
│   ├── 2025-09-19_FOUNDATION_STANDARDS_COMPLIANCE_REQUEST.md
│   ├── 2025-09-19_FOUNDATION_STANDARDS_REFERENCE_BUNDLE.md
│   ├── bounce-handler.kt
│   ├── crm-email-deliverables_2025-09-18.zip
│   ├── email-bounce.yaml
│   ├── email-health.yaml
│   ├── email-outbox.yaml
│   └── outbox-dispatcher.kt
├── email-posteingang/
│   ├── api/
│   │   └── email-management.api.json
│   ├── api-specs/
│   │   ├── bounce-api.yaml
│   │   ├── email-health-outbox-bounce.yaml
│   │   ├── health-api.yaml
│   │   └── outbox-api.yaml
│   ├── config/
│   │   ├── production-ready-config.yaml
│   │   └── prometheus-alerts.yaml
│   ├── database/
│   │   ├── V20250918_01_core_defaults_constraints.sql
│   │   └── V20250918_02_email_outbox_bounce_events.sql
│   ├── events/
│   │   └── cockpit-event-schema.json
│   ├── tests/
│   │   └── EmailServiceTest.java
│   ├── workers/
│   │   ├── bounce-handler.kt
│   │   └── outbox-dispatcher.kt
│   └── technical-concept.md
├── frontend/
│   └── _index.md
├── implementation-plans/
│   ├── 01_LEAD_MANAGEMENT_PLAN.md
│   ├── 02_EMAIL_INTEGRATION_PLAN.md
│   ├── 03_CAMPAIGN_MANAGEMENT_PLAN.md
│   ├── 04_FOUNDATION_STANDARDS_PLAN.md
│   └── 05_CROSS_MODULE_INTEGRATION_PLAN.md
├── kampagnen/
│   ├── api/
│   │   └── campaign-management.api.json
│   ├── database/
│   │   └── seasonal_campaigns.sql
│   ├── frontend/
│   │   ├── CampaignDashboard.test.tsx
│   │   └── smartlayout.campaign-dashboard.json
│   └── technical-concept.md
├── lead-erfassung/
│   ├── api/
│   │   └── lead-management.api.json
│   ├── api-specs/
│   │   ├── leads_foundation_v1.1.yaml
│   │   └── leads_foundation.yaml
│   ├── backend/
│   │   ├── LeadDTO.java
│   │   ├── LeadEntity.java
│   │   ├── LeadExportAdapter.java
│   │   ├── LeadRepository.java
│   │   ├── LeadRepositoryImpl.java
│   │   ├── LeadRepositoryIntegrationTest.java
│   │   ├── LeadResource.java
│   │   ├── LeadScoringService.java
│   │   ├── LeadService.java
│   │   └── LeadStatus.java
│   ├── database/
│   │   └── VXXX__create_lead_table.sql
│   ├── frontend/
│   │   └── smartlayout.lead-form.json
│   ├── tests/
│   │   ├── lead-campaign-e2e.test.ts
│   │   ├── LeadResourceABACIT.java
│   │   └── LeadResourceTest.java
│   ├── .DS_Store
│   ├── Handelsvertretervertrag.pdf
│   └── technical-concept.md
├── postmortem/
│   └── 2025-09-25_backend-start-cors.md
├── shared/
│   ├── ci/
│   │   └── github-actions.yml
│   ├── common/
│   │   └── ProblemExceptionMapper.java
│   ├── config/
│   │   └── application.properties
│   ├── contracts/                           # (leer)
│   ├── docs/
│   │   ├── compliance_matrix.md
│   │   ├── compliance_update.md
│   │   ├── MIGRATION_HINWEISE.md
│   │   ├── performance_budget.md
│   │   ├── README_PATCH.md
│   │   └── README.md
│   ├── frontend/
│   │   ├── theme-v2.mui.ts
│   │   ├── theme-v2.tokens.css
│   │   └── ThemeV2Compliance.test.ts
│   ├── performance/
│   │   ├── leads_performance_test.js
│   │   └── leads_scenario.js
│   ├── security/
│   │   ├── ScopeContext.java
│   │   ├── SecurityScopeFilter.java
│   │   └── SecurityScopeFilterImpl.java
│   ├── _index.md
│   └── topics.md
├── test-coverage/
│   └── sprint-2.1-coverage.md
├── testing/
│   └── k6_lead_api_performance.js
├── _index.md
├── .DS_Store
├── ENTERPRISE_ASSESSMENT_FINAL.md
├── GAP_CLOSURE_REPORT.md
├── README.md
├── SPRINT_MAP.md
├── technical-concept.md
└── VERZEICHNISSTRUKTUR_ANALYSE.md

42 directories, 107 files
```

---

## 📋 **VERZEICHNIS-ERKLÄRUNGEN**

### **🏗️ HYBRID-STRUKTUR (Sprint 2.1.2 - Neue Navigation)**

#### **`_index.md`** (Root-Navigation)
- **Zweck:** Zentrale Einstiegsseite für Modul 02
- **Inhalt:** Executive Summary, Quick-Start-Links zu Backend/Frontend/Shared
- **Status:** ✅ Vollständig implementiert

#### **`SPRINT_MAP.md`** (Sprint-Verweise)
- **Zweck:** Links zu zentralen TRIGGER_SPRINT-Dokumenten
- **Inhalt:** Sprint 2.1, 2.1.1, 2.1.2 mit Status und PR-Verweisen
- **Status:** ✅ Vollständig implementiert (keine Kopien, nur Links)

#### **`backend/`** (Backend-Overlay)
- **`_index.md`:** Backend-Kontext + ADR-Links + Production-Status
- **`analyse/`:** (leer) Für Backend-spezifische Analysen geplant
- **`konzepte/`:** (leer) Für Backend-Architektur-Konzepte geplant
- **`pom-jacoco-config.xml`:** Test-Coverage-Konfiguration für Backend

#### **`frontend/`** (Frontend-Overlay)
- **`_index.md`:** Frontend-Kontext + Research-Links + Tech-Stack-Status
- **Status:** ✅ Verweist auf analyse/ für Research-Dokumente

#### **`shared/`** (Cross-Cutting Overlay)
- **`_index.md`:** Kanonische Contracts + Event-Patterns + Corporate Identity
- **`contracts/`:** (leer) Für Event-Schemas und API-Contracts geplant
- **Unterverzeichnisse:** ci/, common/, config/, docs/, frontend/, performance/, security/

---

### **📚 RESEARCH & ANALYSE (Sprint 2.1.2)**

#### **`analyse/`** (Frontend-Research Complete)
- **`_index.md`:** Research-Navigation + Links zu allen Analyse-Dokumenten
- **`INVENTORY.md`:** Stack-Analyse (React 18.3.1, Vite 6.3.5, TanStack Query)
- **`API_CONTRACT.md`:** Event-System + REST-Endpoints + RBAC-Matrix
- **`RESEARCH_ANSWERS.md`:** 11 offene Implementation-Fragen beantwortet
- **`VALIDATED_FOUNDATION_PATTERNS.md`:** Corporate Identity + UI-Patterns
- **`analysis-2025-09-18-obsoleted.md`:** Veraltete Analyse (vor Hybrid-Struktur)

---

### **🎯 PRODUCTION-READY PATTERNS (Sprint 2.1 + 2.1.1)**

#### **`artefakte/`** (Copy-Paste-Ready für alle Module)
- **`SECURITY_TEST_PATTERN.md`:** @TestSecurity + RBAC + fail-closed Tests
- **`PERFORMANCE_TEST_PATTERN.md`:** P95 <200ms Validation + k6-Integration
- **`EVENT_SYSTEM_PATTERN.md`:** PostgreSQL LISTEN/NOTIFY + AFTER_COMMIT Pattern

---

### **📋 ATOMIC IMPLEMENTATION PLANS**

#### **`implementation-plans/`** (Claude-optimierte Pläne)
- **`01_LEAD_MANAGEMENT_PLAN.md`:** Lead-CRUD + Territory-Scoping + RBAC
- **`02_EMAIL_INTEGRATION_PLAN.md`:** Email-Outbox + Bounce-Handling + Workers
- **`03_CAMPAIGN_MANAGEMENT_PLAN.md`:** Seasonal-Campaigns + Dashboard
- **`04_FOUNDATION_STANDARDS_PLAN.md`:** Security + Performance + Event-Patterns
- **`05_CROSS_MODULE_INTEGRATION_PLAN.md`:** Integration mit Modulen 03-08

---

### **🔧 DETAILLIERTE FEATURE-IMPLEMENTIERUNGEN**

#### **`lead-erfassung/`** (Lead-Management Feature)
- **`api/`:** OpenAPI-Spezifikationen für Lead-Management
- **`api-specs/`:** YAML-Schemas (v1.0 + v1.1)
- **`backend/`:** Vollständige Java-Implementierung (10 Klassen)
  - Entities: LeadEntity, LeadDTO, LeadStatus
  - Services: LeadService, LeadScoringService
  - Repository: LeadRepository + Implementation + Tests
  - Resources: LeadResource + LeadExportAdapter
- **`database/`:** SQL-Migration (Placeholder VXXX)
- **`frontend/`:** SmartLayout-Konfiguration für Lead-Formulare
- **`tests/`:** E2E-Tests + ABAC-Integration-Tests
- **`technical-concept.md`:** Feature-spezifisches Konzept
- **`Handelsvertretervertrag.pdf`:** Business-Requirements

#### **`email-posteingang/`** (Email-Integration Feature)
- **`api/`:** Email-Management-APIs
- **`api-specs/`:** Bounce + Health + Outbox API-Schemas (4 YAML-Dateien)
- **`config/`:** Production-Ready Config + Prometheus-Alerts
- **`database/`:** SQL-Migrationen für Outbox + Bounce-Events
- **`events/`:** Cockpit-Event-Schema (JSON)
- **`tests/`:** EmailServiceTest (Java)
- **`workers/`:** Kotlin-Worker für Bounce-Handler + Outbox-Dispatcher
- **`technical-concept.md`:** Email-Integration-Konzept

#### **`kampagnen/`** (Campaign-Management Feature)
- **`api/`:** Campaign-Management-API (JSON)
- **`database/`:** Seasonal-Campaigns SQL-Schema
- **`frontend/`:** Campaign-Dashboard (Test + SmartLayout-Config)
- **`technical-concept.md`:** Campaign-Management-Konzept

---

### **🗂️ STRATEGISCHE HISTORIE & DISKUSSIONEN**

#### **`diskussionen/`** (16 Dateien)
- **12x MD-Dateien:** Strategische KI-Diskussionen vom 2025-09-18/19
- **4x YAML-Dateien:** API-Schema-Drafts (email-bounce, email-health, email-outbox)
- **2x Kotlin-Dateien:** Code-Snippets (bounce-handler.kt, outbox-dispatcher.kt)
- **1x ZIP-Datei:** CRM-Email-Deliverables Bundle

---

### **🔧 SHARED INFRASTRUCTURE (Production-Integriert)**

#### **`shared/`** (Verzeichnisse im Detail)
- **`ci/github-actions.yml`:** CI-Pipeline-Konfiguration
- **`common/ProblemExceptionMapper.java`:** Globale Error-Handling
- **`config/application.properties`:** Quarkus-Konfiguration
- **`contracts/`:** (leer) Für Event-Schemas geplant
- **`docs/`:** Compliance-Matrix + Migration-Hinweise + Performance-Budget
- **`frontend/`:** Theme v2 (MUI + CSS + Tests) - CI-konform (#94C456, #004F7B)
- **`performance/`:** k6-Performance-Tests (JavaScript)
- **`security/`:** RBAC-Filter + Scope-Context (Java)

---

### **📊 TESTING & QA**

#### **`test-coverage/`**
- **`sprint-2.1-coverage.md`:** Test-Coverage-Report für Sprint 2.1

#### **`testing/`**
- **`k6_lead_api_performance.js`:** Performance-Baseline für Lead-APIs

#### **`postmortem/`**
- **`2025-09-25_backend-start-cors.md`:** CORS-Konfiguration Postmortem

#### **`.github/workflows/`**
- **`ci-module02.yml`:** Modul-spezifische CI-Pipeline

---

### **📄 ROOT-LEVEL DOKUMENTE**

- **`technical-concept.md`:** Strategic Overview für gesamtes Modul
- **`README.md`:** Legacy-Navigation (vor _index.md Implementation)
- **`ENTERPRISE_ASSESSMENT_FINAL.md`:** Business-Assessment
- **`GAP_CLOSURE_REPORT.md`:** Implementation-Gaps-Analyse
- **`VERZEICHNISSTRUKTUR_ANALYSE.md`:** Diese Analyse

---

## 🎯 **STRUKTUR-BEWERTUNG**

### **✅ STÄRKEN**
1. **Hybrid-Navigation vollständig:** _index.md, SPRINT_MAP.md, backend/frontend/shared Overlays
2. **Production-Patterns dokumentiert:** 3 Copy-Paste-ready Patterns für alle Module
3. **Detaillierte Feature-Planung:** Vollständige Implementierungsartefakte für 3 Features
4. **Research komplett:** Frontend-Analyse (Sprint 2.1.2) vollständig
5. **Koordinierte Planung:** implementation-plans/ entsprechen Feature-Verzeichnissen

### **🔧 OPTIMIERUNGSPOTENTIAL**
1. **Leere Verzeichnisse:** backend/analyse/, backend/konzepte/, shared/contracts/
2. **Dual Navigation:** README.md + _index.md (Redirect möglich)
3. **diskussionen/ Archivierung:** 16 historische Diskussionen (September 2025)

### **📊 STATISTIKEN (nach Legacy-Split)**
- **Verzeichnisse:** 51 (8 Kern-Items + legacy-planning + Stubs)
- **Dateien:** 122 (Hybrid-Docs + Legacy-Archive + Production-Patterns)

---

## 🧭 **WIE NAVIGIEREN? (für neue Claude-Instanzen)**

**✅ EMPFOHLENER EINSTIEG:**
1. **Sprint-Kontext:** Trigger → entry_points → `SPRINT_MAP.md`
2. **Modul-Übersicht:** `_index.md` → Status-Dashboard + Start-Reihenfolge
3. **Domain-Arbeit:** `backend/_index.md` ODER `frontend/_index.md` ODER `shared/_index.md`
4. **Research bei Bedarf:** `analyse/_index.md`
5. **Produktionsmuster:** `artefakte/`

**❌ NICHT BROWSEN:**
- Stub-Verzeichnisse (temporär, 2 Sprints): `lead-erfassung/`, `email-posteingang/`, `kampagnen/`, `diskussionen/`, `implementation-plans/`, `test-coverage/`, `testing/`, `postmortem/`
- Legacy-Details: siehe `legacy-planning/` nur bei explizitem Bedarf

**🎯 Einstieg immer Sprint → SPRINT_MAP → Overlays, NICHT über Stubs/Legacy.**

---

**🔄 Letzte Aktualisierung:** 2025-09-27 (Tree + Navigation nach Legacy-Split aktualisiert)