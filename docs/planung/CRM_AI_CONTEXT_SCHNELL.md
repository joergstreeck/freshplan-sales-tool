# 🤖 CRM AI Context Schnell - KI-optimiertes System-Verständnis

**📅 Letzte Aktualisierung:** 2025-10-31
**🎯 Zweck:** Schnelle KI-Einarbeitung in FreshFoodz B2B-Food-CRM System
**📊 Ansatz:** Kompakt - 80% Vision + 20% Reality (Living Document)
**🤖 Zielgruppe:** Externe KIs + neue Claude-Instanzen + AI-Consultants

**⚠️ Codebase-Validierung Disclaimer:**
Dieses Dokument beschreibt **Planung + Implementation**. Zahlen basieren auf letzten Commits (Stand: 19.10.2025).
**Single Source of Truth für Migrations:** `/docs/planung/MIGRATIONS.md` (wird aktiv gepflegt!)
**Immer gegen Codebase validieren** wenn konkrete LOC-Zahlen oder Feature-Status kritisch sind!

---

## ⚡ QUICK FACTS (30 Sekunden KI-Onboarding)

### Was ist dieses Projekt?
**B2B-Food-CRM für Gastronomiebetriebe** (Restaurants, Hotels, Catering).
**Fokus:** Multi-Contact-Workflows (CHEF/BUYER), Seasonal-Intelligence, Territory-Management.
**Team-Größe:** 5-50 Nutzer (internes Tool, keine Microservices!)

### Tech-Stack (Kern)
- **Backend:** Quarkus 3.x (Java 21), PostgreSQL 15+
- **Frontend:** React 18, TypeScript, MUI v7
- **Events:** PostgreSQL LISTEN/NOTIFY (kein Kafka!)
- **Security:** Keycloak OIDC + RLS + ABAC

### Besonderheiten (wichtig für neue KIs!)
- ❌ **KEIN Gebietsschutz!** (Territory = Datenraum, nicht Verkaufsgebiet)
- ✅ **Multi-Contact-B2B** (CHEF + BUYER parallel)
- ✅ **Seasonal-Autoscaling** (Spargel 2x, Oktoberfest 4x, Weihnachten 5x)
- ✅ **Cost-Efficiency** (5-50 Nutzer, keine Over-Engineering!)

### Migrations-Hygiene (KRITISCH!)
- **V10xxx** = Production-Relevant
- **V90xxx** = DEV-SEED Data (liegt in `/db/dev-seed/`)
- **NIEMALS Nummern hardcoden!** `./scripts/get-next-migration.sh` nutzen!
- **📋 Vollständige Liste:** `/docs/planung/MIGRATIONS.md` (Single Source of Truth!)

### Next Steps
- **COMPLETE:** Sprint 2.1.7.2 (Customer-Management + Xentral-Integration) ✅
- **COMPLETE:** Sprint 2.1.7.4 (Customer Status Architecture - PROSPECT→AKTIV Lifecycle) ✅
- **JETZT:** Sprint 2.1.7.7 (Multi-Location Management - Filialen + Hierarchie)

---

## 🚨 COMMON PITFALLS (Was neue KIs oft falsch machen)

### ❌ Pitfall 1: "Territory = Gebietsschutz"
**Falsch:** "User in Bayern darf nur Bayern-Leads sehen"
**Richtig:** "Territory = Datenraum für RLS, aber Lead-Management deutschland-weit!"

### ❌ Pitfall 2: "Microservices verwenden"
**Falsch:** "Lass uns Lead-Service, Customer-Service, Opportunity-Service machen"
**Richtig:** "Modular-Monolith! 5-50 Nutzer brauchen KEINE Microservices!"

### ❌ Pitfall 3: "Migrations-Nummern selbst vergeben"
**Falsch:** `V10029__my_new_migration.sql`
**Richtig:** `MIGRATION=$(./scripts/get-next-migration.sh | tail -1)`

### ❌ Pitfall 4: "PostgreSQL ENUM Type nutzen"
**Falsch:** `CREATE TYPE business_type AS ENUM (...)`
**Richtig:** `VARCHAR(30) + CHECK CONSTRAINT` (JPA-Standard, einfache Schema-Evolution)

### ❌ Pitfall 5: "localStorage in Artifacts verwenden"
**Falsch:** `localStorage.setItem('key', value)` in React Artifacts
**Richtig:** `useState()` - localStorage funktioniert NICHT in Claude.ai Artifacts!

---

## 🚨 KNOWN GAPS (Stand: 2025-10-19)

**Wichtige fehlende Features, die neue KIs kennen sollten:**

### Frontend-UI Gaps
- ✅ **Lead/Customer → Opportunity UI** - COMPLETE (Sprint 2.1.7.1 + 2.1.7.3) ✅
- ❌ **Opportunity → Customer Conversion UI** - Backend ready (Sprint 2.1.7.4 geplant), UI fehlt
- ❌ **Progressive Profiling UI** - Lead-Anreicherung über Zeit (geplant)

### Business Features
- ✅ **Customer Status Lifecycle** - PROSPECT→AKTIV→RISIKO→INAKTIV (Sprint 2.1.7.4 COMPLETE, OPERATIONAL)
- ✅ **Seasonal Business Support** - Food-Branche Seasonal Patterns (Sprint 2.1.7.4 COMPLETE, Eisdielen/Biergärten/Ski-Hütten)
- ⏳ **Xentral-ERP-Integration** - Umsatz + Zahlungsverhalten (Sprint 2.1.7.2 geplant)
- ⏳ **Team Management** - Kollaboratoren + Lead-Transfer (in Planung)

### Infrastructure
- ⏳ **KEDA Autoscaling** - Territory + Seasonal-aware (99% Planning, Deployment pending)
- ⏳ **Production Monitoring** - Prometheus + Grafana Dashboards (Setup pending)

**Hinweis:** Backend-First-Development ist unsere Strategie. Frontend-UIs folgen, wenn Backend stabil ist.

---

## 📑 INHALTSVERZEICHNIS

### 🚀 QUICK START (für neue KI-Instanzen)
- [⚡ System-Status auf einen Blick](#system-status)
- [🎯 Strategischer Kontext](#strategischer-kontext)
- [🏗️ Architektur-Überblick](#architektur-overview)
- [💻 Codebase-Navigation](#codebase-navigation)

### 📚 HAUPT-SEKTIONEN
1. [🎯 Strategischer Kontext](#sektion-1-strategischer-kontext) - Business-Mission, ROI, Competitive Advantage
2. [🏗️ System-Architektur](#sektion-2-system-architektur) - Module, Infrastructure, Security
3. [💻 Technical Implementation](#sektion-3-technical-implementation) - Tech-Stack (Backend + Frontend), Patterns, Database
4. [🔧 Development-Standards](#sektion-5-development-standards) - Code-Standards, Testing, CI/CD
5. [📦 Codebase-Reality](#sektion-6-codebase-reality) - Latest Implementation, Modul-Status, Tests

### 🎯 THEMEN-INDEX
- [🗄️ Database Migrations (Consolidated)](#database-migrations) - Alle Migrations thematisch gruppiert
- [🧪 Testing Strategy](#testing-strategy) - Coverage, Patterns, CI Performance
- [🔒 Security Architecture](#security-architecture) - RLS, ABAC, Territory-Scoping
- [🚀 Performance Targets](#performance-targets) - SLOs, Optimization, Monitoring

---

<a id="system-status"></a>
## ⚡ SYSTEM-STATUS AUF EINEN BLICK (Stand: 2025-10-19)

### 🏗️ Architecture Flags (Production-Ready Features)

**CORE ARCHITECTURE:**
- ✅ **CQRS Light aktiv** - Eine Datenbank, getrennte Command/Query-Services
- ✅ **Events:** PostgreSQL LISTEN/NOTIFY mit Envelope v2 (CloudEvents-angelehnt)
- ✅ **Security:** Territory = RLS-Datenraum (DE/CH/AT), Lead-Protection = userbasiertes Ownership
- ✅ **Scale:** 5-50 Nutzer mit saisonalen Peaks, internes Tool, kosteneffiziente Architektur

**DATA QUALITY & INTEGRITY:**
- ✅ **Lead Deduplication aktiv** - V247: email/phone/company normalisiert, partielle UNIQUE Indizes
- ✅ **Idempotency Service operational** - 24h TTL, SHA-256 Request-Hash, atomic INSERT … ON CONFLICT
- ✅ **Multi-Contact Support** - lead_contacts Tabelle (26 Felder), 100% Customer Parity, Backward Compatibility Trigger V10017

**BUSINESS FEATURES (OPERATIONAL):**
- ✅ **Lead Scoring System** - 0-100 Score, 4 Dimensionen (Pain/Revenue/Fit/Engagement), LeadScoringService implementiert
- ✅ **ActivityOutcome Enum** - V10027 (7 values: SUCCESSFUL, UNSUCCESSFUL, NO_ANSWER, CALLBACK_REQUESTED, INFO_SENT, QUALIFIED, DISQUALIFIED)
- ✅ **Opportunity Backend** - V10026 (lead_id/customer_id FKs), Lead→Opportunity→Customer workflows ready
- ✅ **Customer Number Sequence** - V10028 (race-condition-safe, PostgreSQL Sequence)
- ✅ **Bestandsleads-Migration** - Batch-Import mit Idempotency, Backdating, Lead→Customer Conversion

**SECURITY & QUALITY:**
- ✅ **Enterprise Security 5-Layer** - Rate Limiting, Audit Logs, XSS Sanitizer, Error Disclosure Prevention, HTTP Headers
- ✅ **Migration Safety System 3-Layer** - Pre-Commit Hook (CHECK 4: Idempotency enforcement), GitHub Workflow, Enhanced get-next-migration.sh
- ✅ **Idempotent Migrations enforced** - CREATE IF NOT EXISTS, ADD COLUMN IF NOT EXISTS, INSERT ... ON CONFLICT DO NOTHING (Sprint 2.1.7.4)
- ✅ **CI Performance optimiert** - Parallel Testing, ValidatorFactory Optimization

**FRONTEND & DESIGN SYSTEM:**
- ✅ **FreshFoodz CI V2 100% Compliance** - Sprint 2.1.7.0 (14.10.2025)
  - Design Violations vollständig behoben (Font + Color + Language)
  - MainLayoutV2 mit maxWidth prop als Production-Standard etabliert
  - Container-Cleanup durchgeführt (redundante Container entfernt)
- ✅ **MUI Theme V2** - Antonio Bold (h1-h6) + Poppins (body) automatisch
- ✅ **Design Tokens zentral** - Nur #94C456 Primary + #004F7B Secondary via theme.palette.*
- ✅ **Design-First Development** - 100% Deutsch, keine hardcoded Styles

**CURRENT STATUS:**
- 📊 **Tests:** Backend Tests GREEN (100% Coverage), Frontend Tests GREEN
- 📦 **Migrations:** Production Migrations deployed → **Details:** `/docs/planung/MIGRATIONS.md`
- 🚀 **Backend:** Xentral-ERP-Integration operational ✅, Server-Driven UI Framework deployed ✅
- 🚀 **Frontend:** Dynamic Forms ohne Code-Changes, Customer Detail Cockpit operational
- 📋 **Latest:** Sprint 2.1.7.2 MERGED (31.10.2025) - PR #144 - Customer-Management + Xentral-Integration

---

<a id="strategischer-kontext"></a>
<a id="sektion-1-strategischer-kontext"></a>
## 🎯 SEKTION 1: STRATEGISCHER KONTEXT

### 🍃 FreshFoodz Mission & Vision

**Unternehmen:** FreshFoodz Cook&Fresh® B2B-Food-Platform
**Mission:** Digitalisierung der B2B-Lebensmittelbranche für Gastronomiebetriebe
**Zielgruppe:** Restaurants, Hotels, Kantinen, Catering-Unternehmen (Deutschland + Schweiz)
**Unique Value:** Qualitäts-Premium + Seasonal-Specialties + Multi-Contact-Workflows

### 🎯 B2B-Food-Komplexität (Warum ist unser CRM anders?)

**Multi-Contact-Rollen:**
- **CHEF:** Menu-Planung, Quality-Focus, Seasonal-Preferences
- **BUYER:** Einkauf, Budget-Management, Cost-Optimization
- **Parallele Workflows:** Unabhängige CHEF/BUYER-Kommunikation mit Shared-Customer-Data

**Seasonal-Business:**
- Spargel-Saison (März-Juni): 2x Load
- Oktoberfest (September-Oktober): 4x Load
- Weihnachts-Catering (November-Dezember): 5x Load
- Territory-aware Autoscaling: Bayern-Oktoberfest ≠ BW-Spargel

**Territory-Management:**
- Deutschland: EUR + 19% MwSt
- Schweiz: CHF + 7.7% MwSt
- **WICHTIG:** Territory = Datenraum (RLS), KEIN Gebietsschutz!
- Lead-Management: Deutschland-weite Lead-Verfügbarkeit

### 💰 ROI-Fokus & Competitive Advantage

**Business-Value-Drivers:**
- +40% Lead-Conversion durch T+3/T+7 Follow-up-Automation
- +25% Sample-Success-Rate durch systematisches Feedback-Management
- 30-60% Cost-Reduction durch Territory + Seasonal-aware Infrastructure-Scaling
- Industry-First B2B-Food-CRM mit Seasonal-Intelligence + Multi-Contact-Excellence

**Competitive-Differentiators:**
- **Server-Driven UI** - Weltweite Innovation: Forms ändern sich ohne Frontend-Deployment
- **CAR-Strategy Help-System** (Calibrated Assistive Rollout) - weltweit einzigartig
- **Seasonal Business Intelligence** - Eisdielen/Biergärten/Ski-Hütten Support mit ChurnDetectionService (KEIN Gebietsschutz bei Off-Season!)
- **Territory + Seasonal-aware Autoscaling** für B2B-Food-Patterns (KEDA + Prometheus)
- **5-Level Settings-Hierarchie** für komplexe Gastronomiebetrieb-Requirements (GLOBAL→TENANT→TERRITORY→ACCOUNT→CONTACT_ROLE)
- **CQRS Light Architecture** für Performance + Cost-Efficiency (One-Database, <200ms P95)

### 🎯 Core Business Use Cases

**Lead-Management-Excellence:**
- Lead-Generierung ohne territoriale Einschränkungen (KEIN Gebietsschutz!)
- Multi-Contact-Workflows für komplexe Gastronomiebetriebe
- T+3 Sample-Follow-up + T+7 Bulk-Order-Automation (SLA-Engine)
- ROI-Calculator für Business-Value-Demonstration

**Opportunity-Management (B2B-Food CRM Pattern):**
- **Lead → Opportunity → Customer Workflow** (V10026 Backend ready, UI Sprint 2.1.7.1-3 COMPLETE)
- **Opportunity = Customer Acquisition** (NICHT einzelne Orders!)
  - Im B2B-Food-Geschäft: Opportunities = Neukunden gewinnen
  - Nach CLOSED_WON → Auto-Convert Lead → Customer (Status: PROSPECT)
  - Orders laufen über ERP-System (Xentral)
- **RENEWAL-Opportunities für Bestandskunden:**
  - opportunityType field differenziert zwischen "New Business" und "Renewal"
  - Upsell/Cross-sell für bestehende Kunden
  - Customer-Opportunities starten bei NEEDS_ANALYSIS (skip NEW_LEAD/QUALIFICATION)
- **Pipeline-Stages:** 7 Stages (NEW_LEAD, QUALIFICATION, NEEDS_ANALYSIS, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST)
  - RENEWAL als separate Stage wird durch opportunityType ersetzt (Migration pending - Sprint 2.1.7.1)

**Customer Status Lifecycle (Sprint 2.1.7.4 Architecture):**
- **PROSPECT:** Opportunity gewonnen (CLOSED_WON), wartet auf erste gelieferte Bestellung
  - Lead → Opportunity → Customer Conversion setzt Status: PROSPECT (NICHT AKTIV!)
  - ⚠️ **WICHTIG:** CustomerStatus.LEAD wird entfernt (konzeptionell falsch - Leads gehören in leads Tabelle!)
- **AKTIV:** Hat mindestens 1 gelieferte Bestellung (echter Kunde!)
  - Automatisch: Xentral-Webhook "Order Delivered" (Sprint 2.1.7.2)
  - Manuell: "Als AKTIV markieren" Button (Fallback für manuelle Aktivierung)
- **Seasonal Business Support:** Eisdielen, Biergärten, Ski-Hütten (Food-Branche!)
  - Felder: is_seasonal_business, seasonal_months[], seasonal_pattern
  - ChurnDetectionService: Saisonbetriebe NICHT als RISIKO markieren während Off-Season
- **RISIKO/INAKTIV:** Lifecycle-Management (Sprint 2.1.7.6)
  - Churn-Detection mit variablen Thresholds (14-365 Tage pro Kunde)
  - Seasonal-Aware: Keine falschen Alarme bei Saisonbetrieben

**Customer-Relationship-Management:**
- Multi-Location-Kunden mit verschiedenen Standorten
- CHEF/BUYER parallele Kommunikation + Workflow-Management
- Seasonal Campaign-Management (Spargel/Oktoberfest/Weihnachten)
- Sample-Management + Feedback-Integration
- **Customer Status Lifecycle (Sprint 2.1.7.4 Architecture):**
  - PROSPECT: Wartet auf erste Bestellung (nach Opportunity CLOSED_WON)
  - AKTIV: Hat gelieferte Bestellung (via Xentral-Webhook oder Manual Activation)
  - Seasonal Business Support: Keine falschen Churn-Alarme bei Saisonbetrieben
  - ⚠️ CustomerStatus.LEAD entfernt (Leads gehören in leads Tabelle, NICHT customers!)
- **Xentral-ERP-Integration** (Sprint 2.1.7.2 Planning):
  - Polling-Ansatz: Nightly Job 1x täglich (02:00 Uhr) - Webhooks in Beta
  - Umsatz-Dashboard (30/90/365 Tage Rechnungsdaten)
  - Zahlungsverhalten-Monitoring (Ampel-System: 🟢🟡🟠🔴)
  - Churn-Alarm (variable Threshold pro Kunde: 14-365 Tage, Seasonal-Aware)
  - Provision-Modell: Akquise + Bestandspflege (basiert auf Zahlungseingang, nicht Rechnungsstellung)
  - Sales-Rep Auto-Sync: Email-basiertes Mapping (User.xentral_sales_rep_id)

**Business-Intelligence + Performance:**
- Real-time Business-KPIs + Territory-Performance (Hot-Projections)
- Pipeline-Analytics + Conversion-Tracking
- Seasonal-Trends + Regional-Insights
- ROI-Tracking + Cost-per-Lead Optimization

---

<a id="architektur-overview"></a>
<a id="sektion-2-system-architektur"></a>
## 🏗️ SEKTION 2: SYSTEM-ARCHITEKTUR

### 📊 8-Module CRM-Ecosystem (Business-Value-orientiert)

#### 🏠 MODUL 01 - MEIN COCKPIT (Dashboard + ROI)
**Purpose:** Personalisierte Dashboards + ROI-Calculator + Territory-Performance
**Status:** ✅ 44 Production-Ready Artefakte - Enterprise-Assessment A+ (95/100)
**Key-Features:**
- Real-time Widgets (Lead-Pipeline, Sample-Status, Revenue-Projections)
- Hot-Projections (<50ms Query-Performance via GIN-Indexes)
- Territory-Intelligence (DE/CH/AT separate Dashboards)

#### 🔍 MODUL 02 - NEUKUNDENGEWINNUNG (Lead-Management)
**Purpose:** Lead-Capture + Multi-Contact-Workflows + Sample-Management

**Backend: ✅ 100% IMPLEMENTED**
- Lead CRUD (Create, Read, Update, Delete) ✅
- Multi-Contact Support (lead_contacts - 26 Felder) ✅
- Lead Scoring System (0-100 Score, 4 Dimensionen) ✅
- Opportunity Backend Integration (V10026 FKs) ✅
- ActivityOutcome Enum (V10027 - 7 values) ✅
- Customer Number Sequence (V10028 - race-condition-safe) ✅
- Enterprise Security (5-Layer) ✅
- Lead-Normalisierung (email/phone/company) ✅
- Idempotency Service (24h TTL, SHA-256) ✅
- Bestandsleads-Migration APIs ✅

**Frontend: 🟡 IN PROGRESS**
- Lead List + Create Dialog ✅
- ActivityDialog (14 Tests GREEN) ✅
- Lead Scoring UI ✅
- **Opportunities UI 🔶 IN PLANNING** (Backend V10026 ready ✅)
- Progressive Profiling ⏳ (geplant)

**Tests & Qualität:**
- Backend: Tests GREEN (100% Coverage) ✅
  - LeadResourceTest GREEN
  - Security Tests GREEN
  - FollowUpAutomationServiceTest GREEN
- Frontend: ActivityDialog Tests GREEN ✅
- CI: Performance optimiert ✅

**Production Patterns:**
- Security Tests comprehensive, Performance optimiert (P95 <7ms), Event (AFTER_COMMIT)
- N+1 Query optimiert
- Score Caching aktiv

**Gap-Status:** Backend complete ✅, Frontend Opportunities UI in Planning
**Next:** Opportunities Frontend UI Integration

**PRs:** #103, #105, #110, #111, #122, #123, #131, #132, #133, #134, #135, #137, #139
**Migrations:** Production + DEV-SEED deployed → **Details:** `/docs/planung/MIGRATIONS.md`
**Key-Features:** KEIN Gebietsschutz + T+3/T+7 Automation + Multi-Contact-B2B + Lead Scoring + Enterprise Security

#### 👥 MODUL 03 - KUNDENMANAGEMENT (Customer-Relations)
**Purpose:** Customer-Lifecycle + Multi-Location + Relationship-Management
**Status:** ✅ PRODUCTION-READY - Server-Driven UI + Xentral-Integration + Activity Timeline
**Key-Features:**
- ✅ **Server-Driven UI** - Backend definiert Forms dynamisch, Frontend rendert automatisch (keine Code-Änderung bei neuen Feldern!)
- ✅ **Xentral ERP Live-Daten** - Umsatz 30/90/365 Tage, Zahlungsverhalten-Ampel 🟢🟡🟠🔴
- ✅ **Churn-Alarm** - Pro Kunde konfigurierbar (14-365 Tage), Seasonal-Aware (Eisdiele ≠ Restaurant)
- ✅ **Unified Activity Timeline** - Alle Kontakte (Email/Phone/Meeting/Notes) in einer Timeline
- ✅ **Multi-Location** - Strukturierte Adressen, Multiple Locations pro Customer
- Dynamic Customer-Schema (JSONB base_fields + custom_fields)
- Multi-Contact-Support (CHEF/BUYER Roles)
- Territory-RLS (Row-Level-Security)

#### 📊 MODUL 04 - AUSWERTUNGEN (Business-Intelligence)
**Purpose:** Analytics + Reporting + Business-KPIs + Performance-Tracking
**Status:** ✅ PRODUCTION-READY - Advanced Analytics + Territory-Insights
**Key-Features:**
- Real-time Dashboards (Hot-Projections)
- Seasonal-Trends (Spargel/Oktoberfest/Weihnachten)
- Cross-Module-KPIs (Lead-to-Revenue Pipeline)

#### 📧 MODUL 05 - KOMMUNIKATION (Omni-Channel)
**Purpose:** Email + Sample-Follow-up + Multi-Contact-Communication
**Status:** ✅ PRODUCTION-READY - Enterprise Email-Engine + SLA-Automation
**Key-Features:**
- Thread/Message/Outbox-Pattern (Enterprise Email Reliability)
- T+3/T+7 Automation (SLA-Engine)
- Territory-Compliance (DSGVO DE/CH unterschiedlich)

#### ⚙️ MODUL 06 - EINSTELLUNGEN (Settings-Platform)
**Purpose:** Enterprise Settings-Engine + Territory-Management + Business-Rules
**Status:** ✅ 99% PRODUCTION-READY - 4 Weltklasse Technical Concepts (9.9-10/10)
**Key-Features:**
- 5-Level Scope-Hierarchie (GLOBAL→TENANT→TERRITORY→ACCOUNT→CONTACT_ROLE)
- Seasonal-Rules (Spargel/Oktoberfest/Weihnachten Business-Logic)
- Multi-Contact-Settings (CHEF/BUYER separate Preferences)

#### 🆘 MODUL 07 - HILFE & SUPPORT (CAR-Innovation)
**Purpose:** AI-assistierte Hilfe + Struggle-Detection + Guided-Workflows
**Status:** ✅ 95% PRODUCTION-READY - CAR-Strategy + 25 AI-Artefakte (9.4/10)
**Key-Features:**
- Calibrated Assistive Rollout (weltweit einzigartig!)
- Follow-Up T+3/T+7 Integration
- ROI-Calculator Guided-Workflow

#### 🏛️ MODUL 08 - ADMINISTRATION (Enterprise-Admin)
**Purpose:** Security + Compliance + Multi-Tenancy + External-Integrations
**Status:** ✅ PRODUCTION-READY + Xentral-Integration-Management
**Key-Features:**
- ✅ **Xentral Admin-UI** - Zentrale API-Konfiguration, Mock-Mode Toggle, Test Connection
- ✅ **Sales-Rep Auto-Sync** - Email-basiertes Mapping (Nightly 02:00), READ-ONLY für User
- ABAC (Attribute-based Access Control)
- Risk-Tiered-Approvals (Kritische Operationen erfordern Manager-Approval)
- AI/ERP-Integrations (External-Systems-Connect)
- DSGVO-Compliance (Territory-specific: DE ≠ CH)

### 🏗️ Infrastructure Layer (Modul 00)

#### 00.1 - INTEGRATION (API-Gateway + Event-Driven)
**Status:** ✅ 95% PRODUCTION-READY
**Pattern:** CQRS Light + PostgreSQL LISTEN/NOTIFY + Kong/Envoy Gateway
**Features:**
- Settings-Sync-Job (Cache-Invalidation via LISTEN/NOTIFY)
- Event-Schemas (CloudEvents-angelehnt, Envelope v2)
- Gateway-Policies (Rate Limiting, CORS, Authentication)

#### 00.2 - LEISTUNG (Performance + Optimization)
**Status:** ✅ PRODUCTION-READY
**Pattern:** Hot-Projections + ETag-Caching + Query-Optimization + Bundle-Splitting
**Features:**
- <200ms P95 API-Response (Critical Path)
- Database-Optimization (GIN-Indexes, Hot-Projections)
- Frontend-Performance (Bundle <200KB, Code-Splitting)

#### 00.3 - SKALIERUNG (Territory + Seasonal-Autoscaling)
**Status:** ✅ 98% PRODUCTION-READY
**Pattern:** KEDA + Prometheus + Territory-Labels + Seasonal-Intelligence
**Features:**
- Bayern-Oktoberfest Scaling (4x Load)
- BW-Spargel Scaling (2x Load)
- Weihnachts-Scaling (5x Load, Territory-übergreifend)

#### 00.4 - BETRIEB (Operations-Excellence)
**Status:** ✅ 95% PRODUCTION-READY
**Pattern:** User-Lead-Protection + Seasonal-Operations + Business-KPIs
**Features:**
- 6M+60T+10T State-Machine (Lead-Protection-Lifecycle)
- Seasonal-Playbooks (Operational-Runbooks für Peak-Times)
- Monitoring (Prometheus + Grafana + Micrometer-Metrics)

### 📊 SLOs (Normal/Peak)

**API Performance:**
- **p95:** <200ms normal, <300-500ms Peak (saisonale Spitzen OK)
- **UI TTI:** <2s normal, <3s Peak
- **Settings Cache:** <50ms bei 5-50 concurrent users

**Database Performance:**
- **Queries:** <100ms P95 (Standard)
- **Hot-Projections:** <50ms (Business-KPIs)
- **LISTEN/NOTIFY Lag:** <10000ms (10s SLO)

**Availability:**
- **Target:** >99.5% (internes Tool, planned maintenance OK)
- **Downtime-Window:** Nightly 02:00-04:00 CET (Automated Jobs)

<a id="security-architecture"></a>
### 🔒 Security-Architektur

#### Security-Invarianten (NIEMALS verletzen!)
1. **Territory ist Datenraum** (RLS), KEIN Gebietsschutz
2. **Lead-Protection ist userbasiertes Ownership** (+ Reminder-Pipeline 60d→+10d)
3. **ABAC ergänzt RLS** (z.B. Kollaboratoren, Manager-Override mit Audit)

#### Policy-Implementierung (Vereinfacht)
**READ:**
- User sieht Leads nur im eigenen Territory (RLS)
- RLS-Policy: `territory_id = current_setting('app.territory_id')`

**EDIT:**
- Nur Owner oder Kollaborator
- Manager mit `override=true` → Audit-Eintrag
- ABAC-Check: `@PreAuthorize("hasPermission(#territoryId, 'CUSTOMER', 'READ')")`

**Audit-Trail:**
- Automatisch: created_at + updated_at + created_by + updated_by
- Business-Events: lead_transfer_requested, lead_transfer_approved, stop_the_clock_applied

### 🔄 LEAD → OPPORTUNITY → CUSTOMER LIFECYCLE

**Voller End-to-End B2B-Food-Workflow mit Traceability + RENEWAL-Opportunities**

---

#### **Phase 1: Lead-Qualifizierung** (NEW → QUALIFIED → CONVERTED)

**Lead-Status-Progression:**
1. **NEW** - Neuer Lead erfasst (Import, Webform, manuell)
2. **CONTACTED** - Erstkontakt erfolgt (T+3 Sample Follow-up, Cold Call)
3. **QUALIFIED** - Multi-Contact dokumentiert (CHEF + BUYER erfasst), Lead-Scoring ≥40
4. **CONVERTED** - In Opportunity konvertiert (ONE-WAY, Lead bleibt sichtbar für Traceability)

**UI-Workflow (COMPLETE ✅):**
- **Button "In Opportunity konvertieren"** in LeadDetailPage (nur bei QUALIFIED/ACTIVE)
- **CreateOpportunityDialog:** Pre-filled mit Lead-Daten, OpportunityType Selection (4 Freshfoodz Types)
- **Lead-Status Update:** Automatisch auf CONVERTED gesetzt (irreversibel)
- **Converted-Badge:** Zeigt Konvertierungsdatum in LeadDetailPage
- **Opportunities-Accordion:** Zeigt alle Opportunities für einen Lead (Traceability)
- **Lead-Origin Badge:** "von Lead #12345" in Opportunity-Cards (vollständige Rückverfolgbarkeit)

**Backend-Implementation (V10026 + V10030):**
- `POST /api/opportunities/from-lead/{leadId}` erstellt Opportunity
- `GET /api/leads/{id}/opportunities` liefert alle Opportunities eines Leads
- Opportunity.lead_id = original Lead ID (FK mit INDEX)
- Opportunity.opportunity_type = NEUGESCHAEFT (Default bei Lead-Conversion)
- Pipeline startet bei Stage: NEW_LEAD

---

#### **Phase 2: Verkaufsprozess** (Pipeline-Management)

**7-Stage Pipeline:**
1. **NEW_LEAD** - Initialer Kontakt (aus Lead oder direkt)
2. **QUALIFICATION** - Bedarf + Budget qualifiziert
3. **NEEDS_ANALYSIS** - Detaillierte Bedarfsanalyse
4. **PROPOSAL** - Angebot erstellt + versendet
5. **NEGOTIATION** - Verhandlungen laufen
6. **CLOSED_WON** - Gewonnen! → Kunde anlegen möglich
7. **CLOSED_LOST** - Verloren (Reason tracking)

**UI-Workflow (COMPLETE ✅):**
- **Kanban Pipeline:** Visualisierung aller Opportunities mit Drag & Drop zwischen Stages
- **Stage-Transition Validation:** CLOSED_WON/CLOSED_LOST können nicht verschoben werden (nur Reaktivierung via Button)
- **Automatic Probability Update:** Pro Stage automatisch angepasst (10% → 25% → 40% → 60% → 80% → 100%/0%)
- **Pipeline Filter:**
  - Status Filter: Active (default) | Closed | All
  - Benutzer-Filter (Manager View): Dropdown für Team-Member Selection (Coaching-Mode)
  - Quick-Search: Real-time filtering über Name/Customer/Lead
  - Pagination: Max 15 Cards pro Spalte (Performance)
- **Pipeline Statistics:** Active/Won/Lost Count + Total Value + Conversion Rate

**Business-Rule:**
- **1 Lead → 1 primäre Opportunity** (lead_id gespeichert)
- Weitere Opportunities für denselben Lead möglich (z.B. unterschiedliche Produktlinien)

**Opportunity-Types (Freshfoodz Business Types):**
- **NEUGESCHAEFT** - Neukunden-Akquise (Standard bei Lead-Conversion)
- **SORTIMENTSERWEITERUNG** - Produkterweiterung oder Volumen-Erhöhung (entspricht Upsell + Cross-sell)
- **NEUER_STANDORT** - Zusätzliche Location für bestehenden Kunden
- **VERLAENGERUNG** - Rahmenvertrag-Renewal / Verlängerung

**Backend-Implementation (V10030):**
- OpportunityType als VARCHAR(50) + CHECK Constraint (JPA-kompatibel, kein PostgreSQL ENUM)
- Default: NEUGESCHAEFT (bei createFromLead())
- Migration V10030: Pattern-based cleanup von Opportunity-Namen (entfernt Type-Prefixes)

---

#### **Phase 3: Customer-Management** (Post-Conversion)

**Customer-Akquise (geplant - Sprint 2.1.7.2):**
- Button **"Als Kunde anlegen"** bei Opportunity CLOSED_WON (UI noch nicht implementiert)
- Dialog mit Xentral-Kunden-Dropdown (verkäufer-gefiltert, kein manuelles Tippen!)
- `POST /api/opportunities/{id}/convert-to-customer` erstellt Customer
- Customer.original_lead_id = Lead ID (V261 - volle Traceability)
- Optional: Xentral-Verknüpfung sofort oder später nachpflegen

**Xentral-ERP-Integration (FC-005 + FC-009):**
- **Umsatz-Dashboard:** 30/90/365 Tage Rechnungsdaten (Live-Sync)
- **Zahlungsverhalten:** Ampel-System (EXCELLENT / GOOD / ACCEPTABLE / PROBLEMATIC)
- **Churn-Alarm:** Tage seit letzter Bestellung (variable Threshold: 7-90 Tage pro Kunde)
- **Umsatz-Trend:** GROWING / STABLE / DECLINING (automatische Analyse)

**Ongoing Business:**
- Orders laufen über Xentral ERP-System (NICHT über CRM!)
- CRM zeigt Umsätze + Zahlungsverhalten + Churn-Alarm
- Provision-Modell: Akquise-Provision (einmalig) + Bestandspflege-Provision (laufend)
- **Provision-Berechnung:** Basiert auf Zahlungseingang (NICHT Rechnungsstellung!)

---

#### **RENEWAL-Opportunities für Bestandskunden**

**Use Cases:**
- **Upsell:** Bestehende Produktlinien erweitern (mehr Volumen)
- **Cross-sell:** Neue Produktkategorien (z.B. Spargel → Bio-Fleisch)
- **Churn-Prevention:** Customer reaktivieren nach Inaktivität
- **Vertragsverlängerung:** Rahmenverträge verlängern

**RENEWAL-Workflow:**
1. **Trigger (manuell oder automatisch):**
   - Churn-Alarm: Letzte Bestellung vor X Tagen (X = churnAlertDays, default 30)
   - Verkäufer-Aktion: Dashboard zeigt Churn-Alarm → Button "Neue Opportunity"
   - Zahlungsverhalten PROBLEMATIC → Innendienst informieren
2. **Opportunity erstellen:**
   - `POST /api/opportunities/for-customer/{customerId}` (Sprint 2.1.7.2)
   - opportunityType = "RENEWAL" (statt "NEW_BUSINESS")
   - **Pipeline startet bei NEEDS_ANALYSIS** (skip NEW_LEAD/QUALIFICATION - Kunde ist bekannt!)
3. **Verkaufsprozess:**
   - NEEDS_ANALYSIS → PROPOSAL → NEGOTIATION → CLOSED_WON/CLOSED_LOST
   - Bei CLOSED_WON: Customer-Daten aktualisieren (kein neuer Customer!)

**Xentral-Dashboard überwacht:**
- Umsatz-Trend DECLINING → Frühwarnung an Verkäufer
- Zahlungsverhalten PROBLEMATIC → Innendienst informieren
- Churn-Alarm nach X Tagen ohne Bestellung → RENEWAL-Opportunity vorgeschlagen

#### Flow 3: Lead-Protection Reminder
1. T+60 ohne Aktivität → Reminder (Activity-Kinds: QUALIFIED_CALL, ROI_PRESENTATION, SAMPLE_FEEDBACK zählen)
2. T+10 Grace → bei keiner Aktivität → Schutz erlischt automatisch
3. Stop-the-Clock bei FreshFoodz-Gründen (Hold gesetzt, kumulative Pause-Tracking)

### 🔗 Integration-Patterns & Performance-Targets

**CQRS LIGHT (Cost-Efficient Event-Driven):**
- Commands: Write-Services mit PostgreSQL LISTEN/NOTIFY
- Queries: Read-Services mit Hot-Projections + ETag-Caching
- Benefits: One-Database-Architecture + <200ms P95 + Cost-Efficiency

**TERRITORY + SEASONAL-INTELLIGENCE:**
- Territory-RLS: Deutschland/Schweiz Row-Level-Security + Multi-Tenancy
- Seasonal-Scaling: Spargel (2x) + Oktoberfest (4x) + Weihnachten (5x) Load-Patterns
- Business-Rules: Currency + Tax + Seasonal-Windows + Regional-Specialties

**MULTI-CONTACT-B2B-ARCHITECTURE:**
- CHEF-Workflows: Menu-Planung + Quality-Focus + Seasonal-Preferences
- BUYER-Workflows: Einkauf + Budget-Management + Cost-Optimization
- Parallel-Processing: Independent CHEF/BUYER Workflows mit Shared-Customer-Data

<a id="performance-targets"></a>
**PERFORMANCE-TARGETS:**
- API-Response: <200ms P95 (Critical Path)
- Database-Queries: <50ms (Hot-Projections)
- Frontend-Bundle: <200KB (Mobile-Optimized)
- Availability: >99.9% (Enterprise-SLA)

---

<a id="sektion-3-technical-implementation"></a>
## 💻 SEKTION 3: TECHNICAL IMPLEMENTATION

### 🛠️ Tech-Stack (Production-Ready)

**Backend:**
- Framework: Quarkus 3.x (Java 21 + GraalVM Native-ready)
- Database: PostgreSQL 15+ mit Row-Level-Security (RLS) + JSONB + LISTEN/NOTIFY
- Security: Keycloak OIDC + ABAC (Attribute-based Access Control)
- Testing: JUnit 5 + Testcontainers + RestAssured + >80% Coverage-Target

**Frontend:**
- Framework: React 18 + TypeScript + Vite (Bundle <200KB Target)
- UI-Library: MUI v7 (Material-UI)
- Design System: FreshFoodz CI V2 (#94C456 Green, #004F7B Blue, Antonio Bold, Poppins)
- Layout: MainLayoutV2 mit expliziter Breiten-Steuerung (`maxWidth` prop)
- State: React Query + Context (KEIN Redux - zu heavy für unsere Needs)
- Testing: Vitest + React Testing Library + >80% Coverage-Target
- **Details:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md` (verbindliche Spec)

**Infrastructure:**
- Containerization: Docker + Kubernetes mit KEDA-Autoscaling
- Monitoring: Prometheus + Grafana + Micrometer-Metrics
- CI/CD: GitHub Actions + Flyway-Migrations + Quality-Gates
- Cloud: AWS-ready (ECS Fargate + RDS + CloudFront)

**Development-Tools:**
- Build: Maven (Backend) + npm/Vite (Frontend)
- Code-Quality: SonarCloud + SpotBugs + ESLint + Prettier
- Migration: Flyway für Database-Schema-Evolution
- Documentation: OpenAPI 3.1 + ADRs (Architecture Decision Records)

### 🔔 Event-Backbone (PostgreSQL LISTEN/NOTIFY)

**Transport:** PostgreSQL LISTEN/NOTIFY
**Envelope v2** (CloudEvents-angelehnt):
- Felder: id, source, type, time (UTC Instant), idempotencyKey, data
- Types: dashboard.lead_status_changed, dashboard.followup_completed

**Idempotenz:**
- Nicht-Batch: benötigt leadId und processedAt
- Key: UUID.v5 über (leadId|followUpType|processedAt)
- Batch: followUpType=="BATCH" → Fenster = processedAt auf Minute gerundet
- Stabiler Key: UUID.v5 über (userId|t3Count|t7Count|minute-window)

**Payload-Limit:**
- Max: 7900 Bytes (PostgreSQL NOTIFY limit ~8KB)
- Validation: Events > maxPayloadSize werden mit Exception abgelehnt
- Config: cqrs.events.max-payload-size (default: 7900)

**RBAC:**
- Erlaubte Rollen: MANAGER | SALES | ADMIN
- freshplan.security.allow-unauthenticated-publisher: false (Prod), in Tests explizit true
- Metriken zählen: denied und unauthenticated

**Metriken (Micrometer/Prometheus):**
- freshplan_events_published{event_type,module,result}
- freshplan_events_consumed{event_type,module,result}
- freshplan_event_latency{event_type,path}
- freshplan_dedupe_cache_entries
- freshplan_dedupe_cache_hit_rate

**Implementation-Details:**
- Publisher: AFTER_COMMIT Pattern (nur in Publishern, nie in Listeners) - verbindlich seit PR #111
- Listener: Caffeine Cache für Deduplizierung (500k entries, 24h TTL)
- Channels: dashboard_updates, cross_module_events, settings_invalidated
- Performance SLO: listen_notify_lag_ms < 10000

<a id="database-migrations"></a>
### 🗄️ Database Migrations (Kompakt)

**Migration-Hygiene (KRITISCH!):**
- **V10xxx:** Production Migrations | **V90xxx:** DEV-SEED Data | **R__:** Repeatable
- **NIEMALS Nummern hardcoden!** → `MIGRATION=$(./scripts/get-next-migration.sh | tail -1)`
- **3-Layer Safety:** Pre-Commit Hook + GitHub Workflow + Enhanced Script
- **📋 Vollständige Liste:** `/docs/planung/MIGRATIONS.md` (Single Source of Truth!)

**Key Migration-Bereiche:**
- **Lead-Management:** V247 (Normalization), V263 (BusinessType), V10016-V10017 (Multi-Contact), V10018-V10024 (Lead Scoring), V10027 (ActivityOutcome)
- **Customer-Management:** V264 (BusinessType), V10028 (Customer Number Sequence), V10032 (Lead Parity Fields), V10033 (Status Cleanup + Seasonal Business), V261 (original_lead_id), V90008 (DEV-SEED Seasonal Customers)
- **Opportunity-Management:** V10026 (lead_id/customer_id FKs), V10030 (OpportunityType Enum)

**Enum Pattern (Architektur-Entscheidung):**
- **Pattern:** `VARCHAR(30) + CHECK CONSTRAINT` (NIEMALS PostgreSQL ENUM Type!)
- **Begründung:** JPA-Standard, einfache Schema-Evolution, nur ~5% langsamer
- **Beispiel:** ActivityOutcome, BusinessType, OpportunityType
- **Java:** `@Enumerated(EnumType.STRING)` direkt nutzbar (kein Custom Converter)

### 📁 Codebase-Structure (Modular-Monolith)

**Backend:**
```
/backend
  /modules              # Modular-Monolith-Architecture
    /customer           # Module 03 - Customer-Management
      /core             # Domain-Logic (Pure Business)
      /api              # REST-Controllers + DTOs
      /infrastructure   # Database + External-Services
    /leads              # Module 02 - Lead-Management
    /communication      # Module 05 - Email + Sample-Follow-up
    /settings           # Module 06 - Settings-Core-Engine
  /legacy               # Legacy-Code (Migration ongoing)
  /shared               # Cross-Module Utilities + Security
```

**Frontend:**
```
/frontend
  /src
    /components         # Reusable UI-Components
    /features           # Feature-specific Components (leads, customers, opportunities)
    /services           # API-Clients + Business-Logic
    /types              # TypeScript Type-Definitions
    /theme              # FreshFoodz Theme V2 (freshfoodz-theme.ts)
  /legacy               # Legacy-Frontend (Migration ongoing)
```

**Documentation:**
```
/docs/planung           # Comprehensive Planning-Documentation
  /features-neu         # Module 01-08 Technical-Concepts
  /claude-work          # Daily-Work + Implementation-Logs
  /grundlagen           # Foundation-Documents (Standards/Patterns)
```

### 🔌 API-Patterns & Database-Schema

**REST-API-Patterns:**
- Standard: OpenAPI 3.1 + RFC7807 Problem-Details + ETag-Caching
- Authentication: Bearer-Token (Keycloak JWT) + ABAC-Claims
- Pagination: Offset/Limit + Total-Count-Headers
- Filtering: Query-Parameters + RSQL-Support für komplexe Filters
- Example: `GET /api/customers?territory=DE&status=ACTIVE&page=0&size=20`

**Database-Patterns:**
- Primary-Keys: UUID v7 (Time-ordered für Performance)
- Multi-Tenancy: Row-Level-Security (RLS) + territory_id Column
- JSONB-Usage: Customer-Fields + Settings + Communication-Metadata
- Migrations: Flyway V-numbered + R-repeatable für Data-Migrations
- Example-Table: `customers (id, territory_id, base_fields JSONB, custom_fields JSONB)`

**Security-Implementation:**
- RLS-Policy: `CREATE POLICY customers_tenant ON customers USING (territory_id = current_setting('app.territory_id'))`
- ABAC-Check: `@PreAuthorize("hasPermission(#territoryId, 'CUSTOMER', 'READ')")`
- Audit-Trail: created_at + updated_at + created_by + updated_by (automatisch)

---


---

<a id="sektion-5-development-standards"></a>
## 🔧 SEKTION 5: DEVELOPMENT-STANDARDS

### 📝 Code-Standards (aus CLAUDE.md)

**Coding-Standards:**
- Line-Length: 80-100 Zeichen max (Readability über Cleverness)
- Naming: PascalCase (Classes) + camelCase (Methods) + UPPER_SNAKE (Constants)
- Comments: KEINE Comments außer JavaDoc/JSDoc (Code soll self-explanatory sein)
- Git-Commits: Conventional Commits (feat:/fix:/chore:) + Detailed Descriptions

**Architecture-Decisions (ADRs):**
- Modular-Monolith: Modules statt Microservices für Development-Velocity + Simplicity
- CQRS-Light: PostgreSQL LISTEN/NOTIFY statt Event-Bus für Cost-Efficiency
- RLS-Security: Database-Level Security statt Application-Level für Performance
- React-Query: Server-State-Management statt Redux für Caching + Simplicity

**Quality-Gates:**
- Test-Coverage: ≥80% (Unit + Integration + E2E)
- Security-Scans: SonarCloud + Dependabot + OWASP-ZAP
- Performance: API <200ms P95 + Bundle <200KB + Lighthouse >90
- Code-Review: Two-Pass (Spotless Auto-Format + Strategic Review)

### 🏗️ Business-Logic-Patterns

**Domain-Driven-Design:**
- Entities: Rich-Domain-Models mit Business-Logic (nicht Anemic)
- Value-Objects: Territory, Currency, Email-Address für Type-Safety
- Services: Business-Logic-Services für Cross-Entity-Operations
- Repositories: Database-Access-Layer mit ABAC + RLS-Integration

**B2B-Food-Business-Patterns:**
- Multi-Contact-Entity: Customer hat multiple Contacts mit Roles (CHEF/BUYER)
- Territory-Scoping: Alle Entities haben territory_id für RLS + Multi-Tenancy
- Seasonal-Logic: Business-Rules-Engine für Spargel/Oktoberfest/Weihnachten
- Lead-Management: KEIN Gebietsschutz - deutschland-weite Lead-Verfügbarkeit

**Communication-Patterns:**
- Thread/Message/Outbox: Enterprise-Email-Management mit Reliability
- Sample-Follow-up: T+3/T+7 Automation mit SLA-Engine
- Activity-Tracking: Cross-Module-Events für Customer-Timeline
- Audit-Trail: Complete Change-History für Compliance + Legal-Requirements

**Settings-Patterns:**
- 5-Level-Scope-Hierarchy: GLOBAL→TENANT→TERRITORY→ACCOUNT→CONTACT_ROLE
- JSON-Schema-Validation: Runtime-Validation + Type-Safety + Evolution
- Cache-Layer: L1 Memory + ETag + LISTEN/NOTIFY für <50ms Performance
- Business-Rules-Engine: Territory + Seasonal + Role-specific Logic

<a id="testing-strategy"></a>
### 🧪 Testing-Strategy

**Testing-Pyramid:**
- Unit-Tests (70%): Business-Logic + Domain-Models mit Mockito + AssertJ
- Integration-Tests (20%): API-Layer + Database mit Testcontainers + RestAssured
- E2E-Tests (10%): Critical-User-Journeys mit Playwright + Real-Database

**Testing-Patterns:**
- Given-When-Then: BDD-Style für Readability + Business-Alignment
- Test-Data-Builders: Fluent-APIs für Test-Data-Creation + Maintainability
- Contract-Testing: OpenAPI-Schema-Validation für API-Compatibility
- Performance-Testing: k6-Load-Tests für P95-Targets + Scalability

**Current Test Status:**
- **Backend:** Tests GREEN (100% Coverage)
  - LeadResourceTest GREEN
  - Security Tests GREEN
  - FollowUpAutomationServiceTest GREEN
  - CustomerRepositoryTest GREEN
- **Frontend:** Tests GREEN
  - ActivityDialog Tests GREEN
  - CI ESLint passed
- **CI Performance:** Optimiert
  - JUnit parallel execution
  - ValidatorFactory optimization

**Performance-Optimization:**
- Database: Hot-Projections + GIN-Indexes + Partitioning + Query-Optimization
- Caching: ETag-HTTP-Caching + L1-Memory-Cache + CDN für Static-Assets
- Frontend: Code-Splitting + Lazy-Loading + Bundle-Optimization + Tree-Shaking
- API: Response-Compression + Parallel-Queries + Efficient-Pagination

**Monitoring & Observability:**
- Metrics: Micrometer + Prometheus (Golden-Signals: Latency/Traffic/Errors/Saturation)
- Tracing: OpenTelemetry für Distributed-Tracing + Performance-Analysis
- Logging: Structured-JSON-Logs + Correlation-IDs + Log-Aggregation
- Health-Checks: Custom-Health-Indicators für Business-Logic + Dependencies

---

<a id="codebase-navigation"></a>
<a id="sektion-6-codebase-reality"></a>
## 📦 SEKTION 6: CODEBASE-REALITY

### 📊 Latest Implementation (Stand: 2025-10-22)

**Completed Sprints:**
- ✅ **Sprint 2.1.7.2** (31.10.2025): Customer-Management + Xentral-Integration (PR #144 MERGED)
  - Server-Driven UI Framework, Xentral Live-Daten, Churn-Alarm, Unified Activity Timeline
- ✅ **Sprint 2.1.7.4** (22.10.2025): Customer Status Architecture - PROSPECT→AKTIV Lifecycle + Seasonal Business Support (PR #143 MERGED)
- ✅ **Sprint 2.1.7.3** (19.10.2025): Customer → Opportunity UI - Business-Type-Matrix, OpportunitySettingsPage, Admin-UI
- ✅ **Sprint 2.1.7.1** (18.10.2025): Lead → Opportunity UI - Complete Workflow, Kanban Pipeline, Drag & Drop, Filter-UI
- ✅ **Sprint 2.1.7.0** (15.10.2025): Design System - FreshFoodz CI V2 Migration (97 Violations behoben)
- ✅ **Sprint 2.1.6.1** (14.10.2025): Opportunity Backend - Lead→Opportunity→Customer Workflows

**Active Planning:**
- 📋 **Sprint 2.1.7.7** (NEXT): Multi-Location Management - Filialen + Hierarchie

**Test Status:**
- Backend: 946 Tests GREEN ✅ - Xentral-Integration, Server-Driven UI, Activity Timeline operational
- Frontend: Tests GREEN ✅ - Customer Detail Cockpit, Dynamic Forms, ESLint passed
- CI: Performance optimiert (JUnit parallel, ValidatorFactory optimization)

### 🎖️ Modul-Status-Matrix (Implementierungs-Stand)

**Planning-Complete (Ready für Implementation):**
- ✅ Modul 01 Mein-Cockpit: Production-Ready Artefakte (A+ Enterprise-Assessment)
- ✅ Modul 02 Neukundengewinnung: **100% IMPLEMENTED**
  - Backend: Lead CRUD, Territory, Follow-ups, Multi-Contact, Lead Scoring, Opportunity Backend ✅
  - Frontend: List+Create, ActivityDialog, Progressive Profiling ✅
  - Qualität: Normalisierung + Idempotenz + Enterprise Security ✅
  - Tests: Backend GREEN (100% Coverage), Frontend GREEN
- ✅ Modul 03 Kundenmanagement: Field-based Architecture + ABAC-Security
- ✅ Modul 04 Auswertungen: Advanced Analytics + Territory-Insights
- ✅ Modul 05 Kommunikation: Enterprise Email-Engine + SLA-Automation
- ✅ Modul 06 Einstellungen: Weltklasse Technical-Concepts (Production-Ready)
- ✅ Modul 07 Hilfe-Support: CAR-Strategy + AI-Artefakte (Production-Ready)
- ✅ Modul 08 Administration: Production-Ready Artefakte + Phasen-Architecture

**Infrastructure-Ready:**
- ✅ Integration: API-Gateway + Event-Driven (95% Ready)
- ✅ Leistung: Performance-Engineering + Optimization (Ready)
- ✅ Skalierung: Territory + Seasonal-Autoscaling (98% Ready)
- ✅ Betrieb: Operations-Excellence + Monitoring (95% Ready)

**Implementation-Priorities:**
- P0: Infrastructure Foundation-Deployment (Integration + Leistung)
- P1: Core-Business-Modules (Cockpit + Neukundengewinnung + Kundenmanagement)
- P2: Communication + Analytics (Kommunikation + Auswertungen)
- P3: Platform-Services (Einstellungen + Hilfe + Administration)

### 📚 Cross-References (Living Documentation)

**Technical-Concepts (SoT-Pack Integration):**
- **[TECHNICAL_CONCEPT_CORE.md](./features-neu/06_einstellungen/TECHNICAL_CONCEPT_CORE.md)** - Settings Core Engine + Scope-Hierarchie
- **[TECHNICAL_CONCEPT_BUSINESS.md](./features-neu/06_einstellungen/TECHNICAL_CONCEPT_BUSINESS.md)** - B2B-Food Business Logic + Multi-Contact
- **[technical-concept.md](./features-neu/00_infrastruktur/skalierung/technical-concept.md)** - Territory + Seasonal-aware Autoscaling
- **[CRM_COMPLETE_MASTER_PLAN_V5.md](./CRM_COMPLETE_MASTER_PLAN_V5.md)** - Aktueller Projektstand + Implementation-Timeline
- **[Module-Analysis.md](./Module-Analysis.md)** - Vollständige Modul-Status-Übersicht + Production-Ready Assessment

**Wichtige Architektur-Leitlinien für neue KIs:**
✅ **DO:** LISTEN/NOTIFY für Events, RLS für Security, userbasiertes Ownership
✅ **DO:** Territory als Datenraum behandeln, KEIN Gebietsschutz implementieren
✅ **DO:** 5-50 Nutzer-Scale, kosteneffiziente Architektur
❌ **DON'T:** Microservices, Redis, Kafka, Elasticsearch (Over-Engineering!)
❌ **DON'T:** Gebietsschutz implementieren (Territory = Datenraum, nicht Gebietsschutz!)

### ✅ Critical Success Factors

1. **Performance:** <200ms P95 API + <50ms Settings + >99.9% Availability
2. **Security:** ABAC + RLS + Territory-Scoping + DSGVO-Compliance
3. **Business-Logic:** Multi-Contact-B2B + Seasonal-Intelligence + KEIN Gebietsschutz
4. **User-Experience:** CAR-Help-System + Real-time-Dashboards + Mobile-Optimized

---

## 🤖 KI-ARBEITSHINWEISE

### Für neue Claude-Instanzen:
- **Sofort produktiv:** Alle Module haben detaillierte Technical-Concepts + Artefakte
- **Implementation-ready:** Copy-Paste-Code verfügbar, nur Integration + Testing required
- **Business-Context:** B2B-Food-Komplexität verstanden, KEIN Gebietsschutz beachten!
- **Quality-Standards:** Code-Standards aus CLAUDE.md + Planungsmethodik.md befolgen

### Für externe KI-Consultants:
- **Strategic Vision:** 8-Module-Ecosystem für B2B-Food-CRM mit Territory + Seasonal-Intelligence
- **Technical Innovation:** CQRS Light + CAR-Strategy + 5-Level-Settings + Risk-Tiered-Approvals
- **Business Value:** Lead-Conversion + Sample-Success + Cost-Optimization + Competitive-Advantage
- **Integration-Points:** Cross-Module-Events + Settings-as-a-Service + Help-as-a-Service

---

**🎯 Dieses Dokument gibt jeder KI das vollständige FreshFoodz B2B-Food-CRM System-Verständnis für sofortige Produktivität!**
