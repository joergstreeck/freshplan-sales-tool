# CRM AI Context - FreshPlan Sales Tool

```
KONTEXT-TYP: System-Dokumentation für KI-Agenten
PROJEKT: FreshFoodz B2B-Food-CRM (internes Vertriebstool)
STAND: 2025-11-28
TOKENS: ~12.000 (komplett) | ~3.000 (Quick Start bis COMMON PITFALLS)
```

## KI-ANWEISUNGEN

**Lesepriorität:**
- PFLICHT: Abschnitte "QUICK FACTS" bis "COMMON PITFALLS" (~200 Zeilen)
- OPTIONAL: Rest des Dokuments nur bei spezifischen Deep-Dive-Fragen

**Validierungsregel:**
- Konkrete Zahlen (Test-Counts, Migration-Nummern, LOC) immer gegen Codebase prüfen
- Codebase ist Single Source of Truth, nicht dieses Dokument

**Status-Konvention:**
- ✅ = Implementiert und produktiv
- ⏳ = In Entwicklung
- ❌ = Geplant/Fehlt

**Referenz-Dateien bei Unklarheiten:**
- Migrations: `/docs/planung/MIGRATIONS.md`
- Master Plan: `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`
- Arbeitsregeln: `/CLAUDE.md`

---

## QUICK FACTS

### Projekt-Identität
- **FreshFoodz** = Markenname (B2B-Food-Großhandel)
- **FreshPlan** = Technischer Projektname (CRM-System)
- **freshplan-sales-tool** = Repository-Name

### Was ist dieses Projekt?
**B2B-Food-CRM für Gastronomiebetriebe** (Restaurants, Hotels, Catering).
**Fokus:** Multi-Contact-Workflows (CHEF/BUYER), Seasonal-Intelligence, Territory-Management.
**Team-Größe:** 5-50 Nutzer (internes Tool, keine Microservices!)

### Tech-Stack (Kern)
- **Backend:** Quarkus 3.x (Java 17), PostgreSQL 15+
- **Frontend:** React 18, TypeScript, MUI v7
- **Events:** PostgreSQL LISTEN/NOTIFY (kein Kafka!)
- **Security:** Keycloak OIDC + RLS + ABAC

### Architektur-Constraints (bei Code-Generierung beachten!)
- ❌ **KEIN Gebietsschutz** - Territory = RLS-Datenraum, nicht Verkaufsgebiet
- ✅ **Multi-Contact-B2B** - CHEF + BUYER parallel pro Lead/Customer
- ✅ **Modular-Monolith** - KEINE Microservices (5-50 Nutzer)
- ✅ **Cost-Efficiency** - Keine Over-Engineering-Patterns

### Migrations-Hygiene (KRITISCH!)
- **V10xxx** = Production-Relevant
- **V90xxx** = DEV-SEED Data (liegt in `/db/dev-seed/`)
- **NIEMALS Nummern hardcoden!** `./scripts/get-next-migration.sh` nutzen!
- **📋 Vollständige Liste:** `/docs/planung/MIGRATIONS.md` (Single Source of Truth!)

### Next Steps
- **COMPLETE:** Sprint 2.1.7.2 (Customer-Management + Xentral-Integration) ✅
- **COMPLETE:** Sprint 2.1.7.4 (Customer Status Architecture - PROSPECT→AKTIV Lifecycle) ✅
- **COMPLETE:** Sprint 2.1.7.7 (Multi-Location Management + Server-Driven Architecture) ✅ PR #145 MERGED
- **NÄCHSTER:** Sprint 2.1.7.5 (Advanced Filters & Analytics) oder Sprint 2.1.8 (Team Management)

---

## QUICK START

### Code-Struktur (wichtigste Pfade)

**Backend:** `backend/src/main/java/de/freshplan/`
```
domain/           # Core-Entities (Customer, Opportunity) - STABIL, selten geändert
  ├── customer/   # Customer, Contact, Location Entities + Services
  ├── opportunity/# Opportunity Entity + Services
  └── shared/     # Shared Enums (BusinessType, LeadSource, etc.)
modules/          # Feature-Module (Leads, Xentral) - AKTIV entwickelt
  ├── leads/      # Lead-Management (CRUD, Scoring, Protection)
  └── xentral/    # ERP-Integration (Umsatz, Zahlungsverhalten)
api/resources/    # REST-Endpoints (@Path Annotationen)
infrastructure/   # Querschnittsfunktionen (Security, Events, Settings)
  ├── security/   # RLS, ABAC, Keycloak
  ├── cqrs/       # Event-Publisher, DomainEvent
  └── settings/   # 5-Level Settings-Hierarchie
```
**Faustregel:** `domain/` = etablierte Core-Entitäten, `modules/` = aktiv entwickelte Features

**Frontend:** `frontend/src/`
```
features/         # Feature-Module (Domain-driven)
  ├── customers/  # Kunden-UI + Wizard + Multi-Location
  ├── leads/      # Lead-UI + Scoring + Protection
  └── opportunity/# Kanban-Board + Pipeline
components/       # Shared UI-Components
theme/            # FreshFoodz Design System (freshfoodz.ts)
hooks/            # Shared React Hooks
api/generated/    # OpenAPI-generierte Types
```

### Befehle

```bash
# Backend starten (Port 8080)
cd backend && ./mvnw quarkus:dev

# Frontend starten (Port 5173)
cd frontend && npm install && npm run dev

# Tests
cd backend && ./mvnw test           # Backend
cd frontend && npm run test         # Frontend
cd frontend && npm run test:e2e     # E2E
```

### Haupt-API-Endpoints

| Endpoint | Beschreibung |
|----------|-------------|
| `GET/POST /api/leads` | Lead-Management |
| `GET/POST /api/customers` | Kunden-Management |
| `GET/POST /api/opportunities` | Verkaufschancen |
| `GET /api/enums/{type}` | Backend-Enums für UI-Dropdowns |
| `GET /api/location-service-schema` | Server-Driven Field Definitions |

### Domänen-Begriffe

**CHEF** = Küchenchef (entscheidet Qualität/Menü)
**BUYER** = Einkäufer (entscheidet Budget/Preise)
**Multi-Contact** = Ein Lead/Customer hat CHEF + BUYER parallel (unabhängige Workflows)

### Wichtige Enums (Backend definiert, Frontend konsumiert)

| Enum | Werte | Verwendung |
|------|-------|------------|
| `BusinessType` | RESTAURANT, HOTEL, CATERING, KANTINE, KRANKENHAUS, ALTENHEIM, BETRIEBSRESTAURANT | Kundensegment |
| `LeadStatus` | NEW, CONTACTED, QUALIFIED, CONVERTED, LOST | Lead-Lifecycle |
| `CustomerStatus` | LEAD, PROSPECT, AKTIV, RISIKO, INAKTIV, ARCHIVIERT | Kunden-Lifecycle |
| `OpportunityStage` | NEW_LEAD, QUALIFICATION, NEEDS_ANALYSIS, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST | Sales-Pipeline |
| `HierarchyType` | STANDALONE, HEADQUARTER, FILIALE, ABTEILUNG, FRANCHISE | Multi-Location |

### Xentral-Integration (ERP-Verknüpfung)

**Zwei Kundennummern-Konzepte:**
- `customerNumber` = CRM-interne Nummer (FP-00001, auto-generiert bei Customer-Erstellung)
- `xentralCustomerId` = Xentral ERP-Kundennummer (externe ID, manuell verknüpft)

**Verknüpfung:** Customer kann 0 oder 1 Xentral-Verknüpfung haben. Bei Filialen wird `xentralCustomerId` vom HEADQUARTER geerbt.

### RLS-Pattern (Row-Level-Security)

```java
// Service-Methode mit RLS-Schutz
@RlsContext  // Setzt app.tenant_id + app.user_id in PostgreSQL Session
public List<Lead> findByTerritory() {
    return leadRepository.listAll();  // RLS-Policy filtert automatisch!
}
```

RLS-Policy in PostgreSQL filtert automatisch nach `territory_id = current_setting('app.territory_id')`.

### Troubleshooting

| Problem | Befehl/Lösung |
|---------|---------------|
| Backend startet nicht | `docker ps` (PostgreSQL?) oder `brew services start postgresql@15` |
| Port 8080 belegt | `lsof -i :8080` dann `kill -9 <PID>` |
| CORS-Error Frontend | Backend auf Port 8080 starten, `.env` prüfen |
| Tests rot | `./mvnw clean && ./mvnw test` |
| Migration-Konflikt | `./scripts/get-next-migration.sh` |

---

## COMMON PITFALLS

**NICHT TUN → STATTDESSEN:**

| Falsch | Richtig | Grund |
|--------|---------|-------|
| Territory = Gebietsschutz | Territory = RLS-Datenraum | Leads sind deutschland-weit, keine regionale Beschränkung |
| Microservices-Architektur | Modular-Monolith | 5-50 Nutzer, kein Overhead nötig |
| Migration-Nummer selbst vergeben | `./scripts/get-next-migration.sh` | Race-Conditions vermeiden |
| `CREATE TYPE ... AS ENUM` | `VARCHAR(30) + CHECK` | JPA-kompatibel, einfache Evolution |
| `localStorage` in Artifacts | `useState()` | localStorage in Claude.ai Artifacts nicht verfügbar |
| `useFieldDefinitions()` | `useLocationServiceSchema()` | fieldCatalog.json entfernt, Backend ist SoT |
| Filiale als Location | Filiale als Customer mit `parent_customer_id` | CRM Best Practice (Salesforce-Pattern) |

**Migration erstellen:**
```bash
NEXT=$(./scripts/get-next-migration.sh | tail -1)
touch backend/src/main/resources/db/migration/${NEXT}__beschreibung.sql
```

---

## KNOWN GAPS

| Feature | Status | Hinweis |
|---------|--------|---------|
| Progressive Profiling UI | ❌ | Lead-Anreicherung über Zeit - geplant |
| Team Management | ⏳ | Kollaboratoren + Lead-Transfer - Sprint 2.1.8 |
| KEDA Autoscaling | ⏳ | Territory + Seasonal-aware - Deployment pending |
| Production Monitoring | ⏳ | Prometheus + Grafana - Setup pending |

**Entwicklungsstrategie:** Backend-First (Frontend folgt wenn Backend stabil)

---

## INHALTSVERZEICHNIS (Deep-Dive Sektionen)

**Nur bei Bedarf lesen - oben ist für 90% der Tasks ausreichend.**

| Sektion | Inhalt |
|---------|--------|
| SYSTEM-STATUS | Architecture Flags, Module-Status |
| STRATEGISCHER KONTEXT | Business-Mission, ROI |
| SYSTEM-ARCHITEKTUR | Module 01-08, Infrastructure Layer |
| TECHNICAL IMPLEMENTATION | Tech-Stack Details, Event-Backbone, Patterns |
| DEVELOPMENT-STANDARDS | Code-Standards, Testing, CI/CD |
| CODEBASE-REALITY | Aktuelle LOC, Test-Coverage |
| DATABASE MIGRATIONS | Thematisch gruppierte Migrations |
| SECURITY ARCHITECTURE | RLS, ABAC Details |

---

<a id="system-status"></a>
## ⚡ SYSTEM-STATUS AUF EINEN BLICK (Stand: 2025-11-28)

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
- ✅ **Multi-Location/Filialen Management** (Sprint 2.1.7.7) - Parent-Child Customer Hierarchie → Details in MODUL 03

**SERVER-DRIVEN UI ARCHITECTURE (Sprint 2.1.7.7):**
- ✅ **fieldCatalog.json ENTFERNT** - Backend ist Single Source of Truth → Details in MODUL 03

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
- 📊 **Tests:** Backend 2400+ Tests GREEN, Frontend Tests GREEN
- 📦 **Migrations:** Production V10047 (Latest: contact_multi_location_assignment), ~47 Migrations V10xxx → **Details:** `/docs/planung/MIGRATIONS.md`
- 🚀 **Backend:** Multi-Location BranchService + HierarchyMetrics operational ✅, Server-Driven UI ✅
- 🚀 **Frontend:** HierarchyDashboard + CreateBranchDialog operational ✅
- 📋 **Latest:** Sprint 2.1.7.7 MERGED (28.11.2025) - PR #145 - Multi-Location Management + Server-Driven Architecture

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
  - ⚠️ **HINWEIS:** CustomerStatus enthält aktuell: LEAD, PROSPECT, AKTIV, RISIKO, INAKTIV, ARCHIVIERT
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
- Multi-Location-Kunden mit Parent-Child Hierarchie (Sprint 2.1.7.7)
- CHEF/BUYER parallele Kommunikation + Workflow-Management
- Seasonal Campaign-Management (Spargel/Oktoberfest/Weihnachten)
- Sample-Management + Feedback-Integration
- **Xentral-ERP-Integration** (Sprint 2.1.7.2 COMPLETE):
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
**Purpose:** Customer-Lifecycle + Multi-Location/Filialen + Relationship-Management
**Status:** ✅ PRODUCTION-READY - Server-Driven UI + Multi-Location Hierarchie + Xentral-Integration

**Multi-Location/Filialen Management (Sprint 2.1.7.7 COMPLETE):**
- ✅ **Option A: CRM Best Practice** - Filialen sind separate Customers mit `parent_customer_id` FK
  - Wie Salesforce, Dynamics 365, HubSpot - NICHT als Locations unter einem Customer!
  - Jede Filiale hat eigene Opportunities, Contacts, Activities
- ✅ **HierarchyType Enum:** STANDALONE | HEADQUARTER | FILIALE | ABTEILUNG | FRANCHISE (5 Werte)
- ✅ **BranchService:** createBranch(), getBranchesByHeadquarter(), updateBranch(), deleteBranch(), validateHierarchy()
- ✅ **HierarchyMetricsService:** Roll-up Umsätze für HEADQUARTER (alle Filialen aggregiert)
- ✅ **XentralAddressMatcher:** Fuzzy Address-Matching mit Levenshtein Distance (80% Threshold)

**Server-Driven UI Architecture (Sprint 2.1.7.7 COMPLETE):**
- ✅ **fieldCatalog.json ENTFERNT** - Pre-Commit Hook blockiert Re-Einführung!
- ✅ **LocationServiceSchemaResource:** Backend `/api/location-service-schema` als Single Source of Truth
- ✅ **useLocationServiceSchema():** Frontend Hook für dynamische Field Definitions
- **Benefit:** Forms ändern sich ohne Frontend-Deployment!

**Frontend Components (Sprint 2.1.7.7):**
- ✅ **HierarchyDashboard:** Branch-Übersicht mit Metriken + Parent-Selection (mit Tests)
- ✅ **CreateBranchDialog:** Formular für neue Filialen (mit Tests)
- ✅ **FILIALE Option enabled:** UI zeigt Parent-Selection bei HierarchyType=FILIALE

**Weitere Key-Features:**
- ✅ **Xentral ERP Live-Daten** - Umsatz 30/90/365 Tage, Zahlungsverhalten-Ampel 🟢🟡🟠🔴
- ✅ **Churn-Alarm** - Pro Kunde konfigurierbar (14-365 Tage), Seasonal-Aware (Eisdiele ≠ Restaurant)
- ✅ **Unified Activity Timeline** - Alle Kontakte (Email/Phone/Meeting/Notes) in einer Timeline
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
- **Churn-Alarm:** Tage seit letzter Bestellung (variable Threshold pro Kunde, Default: 90 Tage)
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
- Framework: Quarkus 3.x (Java 17 + GraalVM Native-ready)
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
- **Multi-Location (Sprint 2.1.7.7):** hierarchy_type + parent_customer_id in V5 (Customer Tables)
  - `parent_customer_id` FK auf customers (Self-Referencing)
  - `hierarchy_type` VARCHAR(30): STANDALONE | HEADQUARTER | FILIALE | ABTEILUNG | FRANCHISE
  - Index auf parent_customer_id für effiziente Child-Abfragen
- **Supporting Migrations:** V10034 (fix seasonal_months to JSONB), V10035 (Xentral Integration Fields), V10036 (Customer Churn Threshold)

**Enum Pattern (Architektur-Entscheidung):**
- **Pattern:** `VARCHAR(30) + CHECK CONSTRAINT` (NIEMALS PostgreSQL ENUM Type!)
- **Begründung:** JPA-Standard, einfache Schema-Evolution, nur ~5% langsamer
- **Beispiel:** ActivityOutcome, BusinessType, OpportunityType, HierarchyType, CustomerStatus
- **Java:** `@Enumerated(EnumType.STRING)` direkt nutzbar (kein Custom Converter)
- **HierarchyType Values:** STANDALONE (default) | HEADQUARTER | FILIALE | ABTEILUNG | FRANCHISE

### 📁 Codebase-Structure (Modular-Monolith)

**Backend:** `/backend/src/main/java/de/freshplan/`
```
/api                    # REST-Controllers + DTOs (12+ Resources)
/domain                 # Business-Logic by Feature
  /customer             # Customer Entity + Services + Branch/Hierarchy
  /opportunity          # Opportunity Pipeline + Services
  /communication        # Email + Activity Timeline
  /help                 # CAR-Strategy Help System
  /audit                # Audit Trail + Events
  /profile              # User Profiles
  /shared               # Cross-Domain Services (Events, CQRS, Security)
  /...                  # Weitere Domain-Module
/modules                # External Integrations
  /leads                # Lead-Management (expanded module)
  /xentral              # ERP-Integration Services
/infrastructure         # Database + External-Services + Events
/shared                 # Cross-Module Utilities + Security
```

**Frontend:** `/frontend/src/`
```
/components             # Reusable UI-Components
/features               # Feature-specific Components
  /customers            # Customer Management + Hierarchy + Wizard
  /leads                # Lead Management
  /opportunities        # Pipeline + Kanban
  /settings             # Settings Management
/pages                  # Page-Level Components
/hooks                  # Custom React Hooks (26+)
/services               # API-Clients + Business-Logic
/types                  # TypeScript Type-Definitions
/theme                  # FreshFoodz Theme V2 (freshfoodz-theme.ts)
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

**Current Test Status:** → Siehe SEKTION 6: CODEBASE-REALITY für aktuelle Zahlen (2400+ Backend Tests)

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

### 📊 Latest Implementation (Stand: 2025-11-28)

**Completed Sprints:**
- ✅ **Sprint 2.1.7.7** (28.11.2025): Multi-Location Management + Server-Driven Architecture (PR #145 MERGED)
  - Multi-Location/Filialen Management: Parent-Child Hierarchie mit HierarchyType Enum (5 Werte)
  - Server-Driven UI: fieldCatalog.json ENTFERNT, Backend als Single Source of Truth
  - BranchService, HierarchyMetricsService, XentralAddressMatcher implementiert
  - Frontend: HierarchyDashboard, CreateBranchDialog (mit Tests)
  - CI-Fixes: LeadConvertServiceTest, E2E Timeouts, JaCoCo Coverage
- ✅ **Sprint 2.1.7.4** (22.10.2025): Customer Status Architecture - PROSPECT→AKTIV Lifecycle + Seasonal Business Support (PR #143 MERGED)
- ✅ **Sprint 2.1.7.3** (19.10.2025): Customer → Opportunity UI - Business-Type-Matrix, OpportunitySettingsPage, Admin-UI
- ✅ **Sprint 2.1.7.2** (31.10.2025): Customer-Management + Xentral-Integration (PR #144 MERGED)
- ✅ **Sprint 2.1.7.1** (18.10.2025): Lead → Opportunity UI - Complete Workflow, Kanban Pipeline, Drag & Drop, Filter-UI
- ✅ **Sprint 2.1.7.0** (15.10.2025): Design System - FreshFoodz CI V2 Migration (97 Violations behoben)
- ✅ **Sprint 2.1.6.1** (14.10.2025): Opportunity Backend - Lead→Opportunity→Customer Workflows

**Next Planning:**
- 📋 **Sprint 2.1.7.5**: Advanced Filters & Analytics
- 📋 **Sprint 2.1.8**: Team Management + Kollaboratoren + Lead-Transfer

**Test Status:**
- Backend: 2400+ Tests GREEN ✅ - Multi-Location, Server-Driven UI, Xentral-Integration operational
- Frontend: Tests GREEN ✅ - HierarchyDashboard, CreateBranchDialog Tests, ESLint passed
- CI: Performance optimiert (JUnit parallel, ValidatorFactory optimization)

### 🎖️ Modul-Status-Matrix (Implementierungs-Stand)

**Planning-Complete (Ready für Implementation):**
- ✅ Modul 01 Mein-Cockpit: Production-Ready Artefakte (A+ Enterprise-Assessment)
- ✅ Modul 02 Neukundengewinnung: **100% IMPLEMENTED**
  - Backend: Lead CRUD, Territory, Follow-ups, Multi-Contact, Lead Scoring, Opportunity Backend ✅
  - Frontend: List+Create, ActivityDialog, Progressive Profiling ✅
  - Qualität: Normalisierung + Idempotenz + Enterprise Security ✅
  - Tests: Backend GREEN (100% Coverage), Frontend GREEN
- ✅ Modul 03 Kundenmanagement: Multi-Location Hierarchie + Server-Driven UI + ABAC-Security (Sprint 2.1.7.7 COMPLETE)
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
