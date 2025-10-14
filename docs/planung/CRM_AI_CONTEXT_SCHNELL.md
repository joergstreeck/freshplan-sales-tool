# 🤖 CRM AI Context Schnell - KI-optimiertes System-Verständnis

**📅 Letzte Aktualisierung:** 2025-10-14
**🎯 Zweck:** Schnelle KI-Einarbeitung in FreshFoodz B2B-Food-CRM System
**📊 Ansatz:** 80% Planungsstand + 20% Codebase-Realität (Hybrid Living Document)
**🤖 Zielgruppe:** Externe KIs + neue Claude-Instanzen + AI-Consultants

---

## 📑 INHALTSVERZEICHNIS (Quick Navigation für KI)

**QUICK START (für neue KI-Instanzen):**
- [🏗️ Architecture Flags](#architecture-flags) - System-Status auf einen Blick
- [🗺️ Modul-Overlays](#modul-overlays) - Schnellzugriff auf Module
- [🤖 KI-Arbeitshinweise](#ki-arbeitshinweise) - Sofort produktiv werden

**HAUPT-SEKTIONEN:**
1. [📋 Business-Context (15%)](#sektion-1-business-context) - FreshFoodz Mission, B2B-Food-Komplexität, ROI-Fokus
2. [🏗️ System-Architektur (35%)](#sektion-2-system-architektur-vision) - 8-Module-Ecosystem, Infrastructure, Security
3. [⚙️ Technical Implementation (20%)](#sektion-3-technical-implementation-reality) - Tech-Stack, Event-Backbone, Codebase-Structure
4. [🎯 Development-Patterns (30%)](#sektion-4-development-patterns) - Code-Standards, Testing, Performance

**SPEZIAL-THEMEN:**
- [🎨 Frontend Theme V2](#frontend-theme-v2) - FreshFoodz CI (#94C456, #004F7B)
- [🎖️ Modul-Status-Matrix](#quick-reference-modul-status-matrix) - Implementierungs-Stand aller Module
- [📚 Cross-References](#cross-references) - Wichtige Dokumente + Architektur-Leitlinien

**SPRINT-RESULTS (Recent Deliverables):**
- [Sprint 2.1.7 Results](#sprint-217---results) - ActivityOutcome Enum + Opportunities Backend + Code Review Fixes (PR #139)
- [Sprint 2.1.6 Phase 2 Results](#sprint-216-phase-2---results) - Bestandsleads-Migration + BusinessType Harmonization
- [Sprint 2.1.4 Results](#sprint-214---results) - Lead Deduplication + CI Performance (7min)

---

<a id="architecture-flags"></a>
> **🏗️ Architecture Flags (Stand: 2025-10-14)**
> - **CQRS Light aktiv** (`features.cqrs.enabled=true`), **eine Datenbank**, getrennte Command/Query-Services
> - **Events:** **PostgreSQL LISTEN/NOTIFY mit Envelope v2** (siehe Event-Backbone unten)
> - **Security:** Territory = **RLS-Datenraum** (DE/CH/AT), **Lead-Protection = userbasiertes Ownership**
> - **Settings-Registry (Hybrid JSONB + Registry)** produktiv, ETag + LISTEN/NOTIFY Cache-Invalidation
> - **Lead Deduplication aktiv** (V247: email/phone/company normalisiert, partielle UNIQUE Indizes)
> - **Idempotency Service operational** (24h TTL, SHA-256 Request-Hash, atomic INSERT … ON CONFLICT)
> - **CI optimiert:** 24min → 7min (70% schneller) - JUnit parallel (Surefire gesteuert), ValidatorFactory @BeforeAll
> - **Migrations-Hygiene:** Prod-Migrations <V8000, **V10xxx** = Production-Relevant (V10013-V10024)
> - **Multi-Contact Support OPERATIONAL** (Sprint 2.1.6 Phase 5): lead_contacts Tabelle (26 Felder), 100% Customer Parity, Backward Compatibility Trigger V10017 (KRITISCH!)
> - **Lead Scoring System OPERATIONAL** (Sprint 2.1.6 Phase 5): 0-100 Score, 4 Dimensionen (Pain/Revenue/Fit/Engagement), LeadScoringService (268 LOC), 19 Tests GREEN
> - **Enterprise Security 5-Layer ACTIVE** (Sprint 2.1.6 Phase 5): Rate Limiting, Audit Logs, XSS Sanitizer, Error Disclosure Prevention, HTTP Headers
> - **Migration Safety System 3-Layer** (Sprint 2.1.6 Phase 5): Pre-Commit Hook, GitHub Workflow, Enhanced get-next-migration.sh
> - **Bestandsleads-Migration operational** (Sprint 2.1.6 Phase 2): Batch-Import mit Idempotency, Backdating mit kumulativer Stop-the-Clock-Pause, Lead→Customer Conversion mit Archivierung (KEINE Hard-Deletes)
> - **Sprint 2.1.7 COMPLETE (14.10.2025 - PR #139):** ActivityOutcome Enum (V10027 - 7 values), Opportunity Backend Integration (V10026 FKs), Customer Number Sequence (V10028 - race-condition-safe), DEV-SEED Opportunities (V90003 - 10 Opportunities), Code Review Fixes (10 issues), 60/60 Tests GREEN
> - **Scale:** **5-50 Nutzer** mit saisonalen Peaks, **internes Tool**, kosteneffiziente Architektur

---

<a id="modul-overlays"></a>
## 🗺️ **MODUL-OVERLAYS (Schnellzugriff)**

**Hybrid-Struktur:** Zentrale Sprints + Modulare Overlays für detaillierte Navigation

- **Modul 02 Neukundengewinnung:** [features-neu/02_neukundengewinnung/_index.md](features-neu/02_neukundengewinnung/_index.md)
- **AI-Startseite:** [START_HERE_FOR_AI.md](START_HERE_FOR_AI.md)
- **Planungsmethodik:** [PLANUNGSMETHODIK.md](PLANUNGSMETHODIK.md)

---

<a id="sektion-1-business-context"></a>
## 📋 **SEKTION 1: BUSINESS-CONTEXT** (15%)

### **🍃 FreshFoodz Mission & Vision**
```yaml
UNTERNEHMEN: FreshFoodz Cook&Fresh® B2B-Food-Platform
MISSION: Digitalisierung der B2B-Lebensmittelbranche für Gastronomiebetriebe
ZIELGRUPPE: Restaurants, Hotels, Kantinen, Catering-Unternehmen (Deutschland + Schweiz)
UNIQUE-VALUE: Qualitäts-Premium + Seasonal-Specialties + Multi-Contact-Workflows

B2B-FOOD-KOMPLEXITÄT:
  - Multi-Contact-Rollen: CHEF (Menu-Planung) + BUYER (Einkauf + Budgets)
  - Seasonal-Business: Spargel-Saison + Oktoberfest + Weihnachts-Catering
  - Territory-Management: Deutschland (EUR + 19% MwSt) + Schweiz (CHF + 7.7% MwSt)
  - Lead-Management: KEIN Gebietsschutz - deutschland-weite Lead-Verfügbarkeit
```

### **🎯 Core Business Use Cases**
```yaml
LEAD-MANAGEMENT-EXCELLENCE:
  - Lead-Generierung ohne territoriale Einschränkungen
  - Multi-Contact-Workflows für komplexe Gastronomiebetriebe
  - T+3 Sample-Follow-up + T+7 Bulk-Order-Automation
  - ROI-Calculator für Business-Value-Demonstration

CUSTOMER-RELATIONSHIP-MANAGEMENT:
  - Multi-Location-Kunden mit verschiedenen Standorten
  - CHEF/BUYER parallele Kommunikation + Workflow-Management
  - Seasonal Campaign-Management (Spargel/Oktoberfest/Weihnachten)
  - Sample-Management + Feedback-Integration

BUSINESS-INTELLIGENCE + PERFORMANCE:
  - Real-time Business-KPIs + Territory-Performance
  - Pipeline-Analytics + Conversion-Tracking
  - Seasonal-Trends + Regional-Insights
  - ROI-Tracking + Cost-per-Lead Optimization
```

### **💰 ROI-Fokus & Competitive Advantage**
```yaml
BUSINESS-VALUE-DRIVERS:
  - +40% Lead-Conversion durch T+3/T+7 Follow-up-Automation
  - +25% Sample-Success-Rate durch systematisches Feedback-Management
  - 30-60% Cost-Reduction durch Territory + Seasonal-aware Infrastructure-Scaling
  - Industry-First B2B-Food-CRM mit Seasonal-Intelligence + Multi-Contact-Excellence

COMPETITIVE-DIFFERENTIATORS:
  - CAR-Strategy Help-System (Calibrated Assistive Rollout) - weltweit einzigartig
  - Territory + Seasonal-aware Autoscaling für B2B-Food-Patterns
  - 5-Level Settings-Hierarchie für komplexe Gastronomiebetrieb-Requirements
  - CQRS Light Architecture für Performance + Cost-Efficiency
```

---

<a id="frontend-theme-v2"></a>
## 🎨 **FRONTEND THEME V2 – FRESHFOODZ CI**

```yaml
CORPORATE-DESIGN:
  Primary-Colors:
    Main-Green: #94C456 (FreshFoodz Green - Primary Brand Color)
    Corporate-Blue: #004F7B (Vertrauenswürdig, Professional)

  Typography:
    Headlines: Antonio Bold (Impactful, Modern, Attention-grabbing)
    Body-Text: Poppins (Readable, Professional, Web-optimiert)

  UI-Pattern-Usage:
    Buttons-Primary: #94C456 (Call-to-Action)
    Navigation: #004F7B (Header, Sidebar)
    Alerts-Success: #94C456 (Lead converted, Sample shipped)
    Alerts-Info: #004F7B (Follow-up reminder, Territory info)

  Accessibility:
    Contrast-Ratio: AAA-Standard (WCAG 2.1) - alle Text-/Background-Kombinationen
    Font-Sizes: Minimum 14px Body, 16px+ Interactive Elements
    Mobile-First: Responsive Design für Außendienst-Nutzung

STYLING-TECH-STACK:
  UI-Framework: MUI v7 (Material-UI) - Primary Component Library
  CSS-Engine: Emotion (CSS-in-JS) - @emotion/react + @emotion/styled
  Utilities:
    - tailwind-merge (KEIN Tailwind CSS! Nur className merge utility)
    - class-variance-authority (Component Variants Pattern)
    - clsx (Conditional Classes)
  Icon-System:
    - @mui/icons-material (Primary Icons)
    - lucide-react (Secondary Icon Set)

  Theme-Customization:
    Location: /frontend/src/theme/freshfoodz-theme.ts
    Pattern: MUI createTheme() mit FreshFoodz-Overrides
    Colors: MUI Palette Override für primary/secondary
    Components: MUI Component-Overrides für Buttons/Cards/Navigation

COLOR-SPECIFICS (RGB für KI-Genauigkeit):
  Primary-Green: #94C456 → rgb(148, 196, 86)
  Corporate-Blue: #004F7B → rgb(0, 79, 123)
  Hover-States:
    - Primary-Hover: #7BA945 (dunklerer Grünton)
    - Secondary-Hover: #003A5C (dunkleres Blau)
  Status-Colors:
    - Success: #94C456 (FreshFoodz Green)
    - Error: #DC3545 (Standard Red)
    - Warning: #FFC107 (Amber)

CSS-DESIGN-TOKENS (Verbindlich):
  Layout:
    - Header-Height: 64px (weiß, shadow: 0 2px 4px rgba(0,0,0,0.08))
    - Content-Margin-Top: 8px (Abstand Header → Content)
    - Content-Padding: 16px
  Shadows:
    - Header: 0 2px 4px rgba(0,0,0,0.08)
    - Paper: 0 1px 3px rgba(0,0,0,0.05)
    - Card: 0 1px 2px rgba(0,0,0,0.04)

SMARTLAYOUT-SYSTEM (Intelligente Content-Breiten):
  Tables/Lists: 100% Breite (Auto-Detect: <Table>, <DataGrid>, data-wide="true")
  Forms: 800px max (Auto-Detect: <form>, mehrere <TextField>)
  Text/Articles: 1200px max (Auto-Detect: hauptsächlich <Typography>)
  Dashboard: 100% Breite (Grid mit Cards)
  Cockpit: 100% Breite, 0 Padding (Spezial-Layout)

ACCESSIBILITY (WCAG 2.1 AA+):
  Contrast-Ratios:
    - Green/White: 4.52:1 (AA ✅)
    - Blue/White: 8.89:1 (AAA ✅)
    - Black/White: 21:1 (AAA ✅)
  Focus-States: 3px solid outline, 2px offset
  High-Contrast-Mode: Automatische dunklere Farbtöne

UI-SPRACHE (DEUTSCH, kein Denglisch):
  Dashboard → Übersicht | Save → Speichern | Cancel → Abbrechen
  Delete → Löschen | Edit → Bearbeiten | Settings → Einstellungen
  Stil: "Sie"-Form, höflich, keine Abkürzungen

LOGO-VERWENDUNG:
  File: /frontend/public/freshplan-logo.png (19 KB PNG, @2x Retina)
  Component: <Logo> aus @/components/common/Logo
  Desktop: 40px Höhe | Mobile: 32px Höhe
  Variants: "full" (mit Text) | "icon" (nur Icon)
  Schutzzone: min. 16px Freiraum
  Nur auf weißem/hellem Hintergrund!
  Fallback: Automatisches FreshPlan Icon + Text (bei fehlenden Dateien)

IMPLEMENTATION-REFERENCE:
  Design-System-Spec: /docs/planung/grundlagen/DESIGN_SYSTEM.md (verbindlich!)
  Sprint-Docs: Sprint Summary unter "Frontend Changes"
  Module-Docs: Frontend Modul _index.md → "Design System" Sektion
  Theme-Config: /frontend/src/theme/freshfoodz-theme.ts
  Components: FreshFoodz-branded Header, CTA-Buttons, Status-Badges
  Storybook: Component-Katalog mit FreshFoodz CI (npm run storybook)
```

---

<a id="sektion-2-system-architektur-vision"></a>
## 🏗️ **SEKTION 2: SYSTEM-ARCHITEKTUR-VISION** (35%)

### **📊 8-Module CRM-Ecosystem Übersicht**
```yaml
MODULE-STRUKTUR (Business-Value-orientiert):

🏠 MODUL 01 - MEIN COCKPIT (Dashboard + ROI):
  Purpose: Personalisierte Dashboards + ROI-Calculator + Territory-Performance
  Status: ✅ 44 Production-Ready Artefakte - Enterprise-Assessment A+ (95/100)
  Key-Features: Real-time Widgets + Hot-Projections + Territory-Intelligence

🔍 MODUL 02 - NEUKUNDENGEWINNUNG (Lead-Management):
  Purpose: Lead-Capture + Multi-Contact-Workflows + Sample-Management
  Status: ✅ 100% IMPLEMENTED - Lead-Management Complete + Multi-Contact + Lead Scoring + Security (PR #137 CREATED)
  PRs: #103, #105, #110, #111, #122, #123, #131, #132, #133, #134, #135, #137 (READY FOR REVIEW)
  Delivered (Sprint 2.1.6 Phases 1-5):
    - Frontend MVP: Lead List + Create Dialog · Feature-Flag VITE_FEATURE_LEADGEN
    - Lead-Normalisierung: email lowercase · phone E.164 · company ohne Rechtsform-Suffixe
    - Idempotenz: 24h TTL · SHA-256 Hashing · atomic Upsert (ON CONFLICT)
    - Production Patterns: Security (23 Tests), Performance (P95 <7ms), Event (AFTER_COMMIT)
    - BusinessType Harmonization: Lead + Customer unified · V263/V264 Migrations · Single Source of Truth Pattern
    - Bestandsleads-Migration: Batch-Import · Backdating · Lead→Customer Conversion
    - Lead Scoring System (Phase 5): 0-100 Score · 4 Dimensionen (Pain/Revenue/Fit/Engagement) · LeadScoringService (268 LOC) · 19 Tests GREEN
    - Multi-Contact Support (Phase 5): lead_contacts Tabelle (26 Felder) · 100% Customer Parity · Backward Compatibility Trigger V10017 (KRITISCH!)
    - Enterprise Security (Phase 5): 5 Layer (Rate Limiting, Audit Logs, XSS Sanitizer, Error Disclosure Prevention, HTTP Headers)
    - Critical Bug Fixes (Phase 5): 4 Fixes (ETag Race Condition, Ambiguous Email Column, Missing Triggers, UTF-8 Encoding)
    - Migration Safety System (Phase 5): 3-Layer (Pre-Commit Hook, GitHub Workflow, Enhanced get-next-migration.sh)
    - 12 Migrationen V10013-V10024: Settings ETag Triggers · Lead Enums (VARCHAR + CHECK) · lead_contacts · Pain Scoring · Lead Scoring
    - Performance Optimizations: N+1 Query Fix (7x faster: 850ms→120ms) · Score Caching (90% fewer DB writes)
  Tests: 31/31 LeadResourceTest GREEN + 10/10 Security Tests GREEN
  Key-Features: KEIN Gebietsschutz + T+3/T+7 Automation + Multi-Contact-B2B + Lead Scoring + Enterprise Security

👥 MODUL 03 - KUNDENMANAGEMENT (Customer-Relations):
  Purpose: Customer-Lifecycle + Multi-Location + Relationship-Management
  Status: ✅ PRODUCTION-READY - Field-based Architecture + ABAC-Security
  Key-Features: Dynamic Customer-Schema + Multi-Contact-Support + Territory-RLS

📊 MODUL 04 - AUSWERTUNGEN (Business-Intelligence):
  Purpose: Analytics + Reporting + Business-KPIs + Performance-Tracking
  Status: ✅ PRODUCTION-READY - Advanced Analytics + Territory-Insights
  Key-Features: Real-time Dashboards + Seasonal-Trends + Cross-Module-KPIs

📧 MODUL 05 - KOMMUNIKATION (Omni-Channel):
  Purpose: Email + Sample-Follow-up + Multi-Contact-Communication
  Status: ✅ PRODUCTION-READY - Enterprise Email-Engine + SLA-Automation
  Key-Features: Thread/Message/Outbox-Pattern + T+3/T+7 + Territory-Compliance

⚙️ MODUL 06 - EINSTELLUNGEN (Settings-Platform):
  Purpose: Enterprise Settings-Engine + Territory-Management + Business-Rules
  Status: ✅ 99% PRODUCTION-READY - 4 Weltklasse Technical Concepts (9.9-10/10)
  Key-Features: 5-Level Scope-Hierarchie + Seasonal-Rules + Multi-Contact-Settings

🆘 MODUL 07 - HILFE & SUPPORT (CAR-Innovation):
  Purpose: AI-assistierte Hilfe + Struggle-Detection + Guided-Workflows
  Status: ✅ 95% PRODUCTION-READY - CAR-Strategy + 25 AI-Artefakte (9.4/10)
  Key-Features: Calibrated Assistive Rollout + Follow-Up T+3/T+7 + ROI-Calculator

🏛️ MODUL 08 - ADMINISTRATION (Enterprise-Admin):
  Purpose: Security + Compliance + Multi-Tenancy + External-Integrations
  Status: ✅ 100% PLANNING COMPLETE - 76 Production-Ready Artefakte (9.6/10)
  Key-Features: ABAC + Risk-Tiered-Approvals + AI/ERP-Integrations + DSGVO
```

### **🏗️ Infrastructure Layer (Modul 00)**
```yaml
INFRASTRUCTURE-FOUNDATION:

00.1 - INTEGRATION (API-Gateway + Event-Driven):
  Status: ✅ 95% PRODUCTION-READY - Enterprise Integration Architecture
  Pattern: CQRS Light + PostgreSQL LISTEN/NOTIFY + Kong/Envoy Gateway
  Features: Settings-Sync-Job + Event-Schemas + Gateway-Policies

00.2 - LEISTUNG (Performance + Optimization):
  Status: ✅ PRODUCTION-READY - Performance-Engineering + Optimization
  Pattern: Hot-Projections + ETag-Caching + Query-Optimization + Bundle-Splitting
  Features: <200ms P95 + Database-Optimization + Frontend-Performance

00.3 - SKALIERUNG (Territory + Seasonal-Autoscaling):
  Status: ✅ 98% PRODUCTION-READY - Territory + Seasonal-aware KEDA-Scaling
  Pattern: KEDA + Prometheus + Territory-Labels + Seasonal-Intelligence
  Features: Bayern-Oktoberfest + BW-Spargel + Weihnachts-Scaling (5x Load)

00.4 - BETRIEB (Operations-Excellence):
  Status: ✅ 95% PRODUCTION-READY - External AI Operations-Pack (9.5/10)
  Pattern: User-Lead-Protection + Seasonal-Operations + Business-KPIs
  Features: 6M+60T+10T State-Machine + Seasonal-Playbooks + Monitoring
```

### **📊 SLOs (Normal/Peak)**
- **API p95:** <200ms normal, <300-500ms Peak (saisonale Spitzen)
- **UI TTI:** <2s normal, <3s Peak
- **Settings Cache:** <50ms bei 5-50 concurrent users
- **Database Queries:** <100ms P95, Hot-Projections für Business-KPIs
- **Availability:** >99.5% (internes Tool, planned maintenance OK)

### **🔒 Security-Invarianten**
1. **Territory ist Datenraum** (RLS), kein Gebietsschutz
2. **Lead-Protection ist userbasiertes Ownership** (+ Reminder-Pipeline 60d→+10d)
3. **ABAC ergänzt RLS** (z.B. Kollaboratoren, Manager-Override mit Audit)

**Policy-Skizze (vereinfacht):**
- READ: User sieht Leads nur im eigenen Territory (RLS)
- EDIT: nur Owner oder Kollaborator; Manager mit `override=true` → Audit-Eintrag

### **🔄 Ende-zu-Ende Business-Flows**

**Flow: Lead → Sample → Trial → Order**
1. Lead QUALIFIED → SampleBox konfiguriert → `sample.status.changed=SHIPPED`
2. DELIVERY → Trial 2-4 Wochen, Feedback protokolliert → ROI aktualisiert
3. Erfolgreiche Produkte → Order an ERP, Pipeline auf CONVERTED

**Flow: Lead-Protection Reminder**
1. T+60 ohne Aktivität → Reminder (Activity-Kinds QUALIFIED_CALL/ROI_PRESENTATION/SAMPLE_FEEDBACK zählen)
2. T+10 Grace → bei keiner Aktivität → Schutz erlischt automatisch
3. Stop-the-Clock bei FreshFoodz-Gründen (Hold gesetzt)

### **🔗 Integration-Patterns & Performance-Targets**
```yaml
ARCHITECTURE-PATTERNS:

CQRS LIGHT (Cost-Efficient Event-Driven):
  Commands: Write-Services mit PostgreSQL LISTEN/NOTIFY
  Queries: Read-Services mit Hot-Projections + ETag-Caching
  Benefits: One-Database-Architecture + <200ms P95 + Cost-Efficiency

TERRITORY + SEASONAL-INTELLIGENCE:
  Territory-RLS: Deutschland/Schweiz Row-Level-Security + Multi-Tenancy
  Seasonal-Scaling: Spargel (2x) + Oktoberfest (4x) + Weihnachten (5x) Load-Patterns
  Business-Rules: Currency + Tax + Seasonal-Windows + Regional-Specialties

MULTI-CONTACT-B2B-ARCHITECTURE:
  CHEF-Workflows: Menu-Planung + Quality-Focus + Seasonal-Preferences
  BUYER-Workflows: Einkauf + Budget-Management + Cost-Optimization
  Parallel-Processing: Independent CHEF/BUYER Workflows mit Shared-Customer-Data

PERFORMANCE-TARGETS:
  API-Response: <200ms P95 (Critical Path)
  Database-Queries: <50ms (Hot-Projections)
  Frontend-Bundle: <200KB (Mobile-Optimized)
  Availability: >99.9% (Enterprise-SLA)
```

---

<a id="sektion-3-technical-implementation-reality"></a>
## ⚙️ **SEKTION 3: TECHNICAL IMPLEMENTATION REALITY** (20%)

### **🛠️ Current Tech-Stack**
```yaml
BACKEND-STACK:
  Framework: Quarkus 3.x (Java 21 + GraalVM Native-ready)
  Database: PostgreSQL 15+ mit Row-Level-Security (RLS) + JSONB + LISTEN/NOTIFY
  Security: Keycloak OIDC + ABAC (Attribute-based Access Control)
  Testing: JUnit 5 + Testcontainers + RestAssured + >80% Coverage-Target

FRONTEND-STACK:
  Framework: React 18 + TypeScript + Vite (Bundle <200KB Target)
  UI-Library: MUI v5 (Material-UI) mit FreshFoodz Corporate Design
  State: React Query + Context (KEIN Redux - zu heavy für unsere Needs)
  Testing: Vitest + React Testing Library + >80% Coverage-Target

INFRASTRUCTURE:
  Containerization: Docker + Kubernetes mit KEDA-Autoscaling
  Monitoring: Prometheus + Grafana + Micrometer-Metrics
  CI/CD: GitHub Actions + Flyway-Migrations + Quality-Gates
  Cloud: AWS-ready (ECS Fargate + RDS + CloudFront)

DEVELOPMENT-TOOLS:
  Build: Maven (Backend) + npm/Vite (Frontend)
  Code-Quality: SonarCloud + SpotBugs + ESLint + Prettier
  Migration: Flyway für Database-Schema-Evolution
  Documentation: OpenAPI 3.1 + ADRs (Architecture Decision Records)
```

### **🔔 Event-Backbone (aktuell, Stand 2025-10-01)**

```yaml
Transport: PostgreSQL LISTEN/NOTIFY
Envelope v2 (CloudEvents-angelehnt):
  Felder: id, source, type, time (UTC Instant), idempotencyKey, data
  Types: dashboard.lead_status_changed, dashboard.followup_completed

Idempotenz:
  Nicht-Batch: benötigt leadId und processedAt
  Key: UUID.v5 über (leadId|followUpType|processedAt)
  Batch: followUpType=="BATCH" → Fenster = processedAt auf Minute gerundet
  Stabiler Key: UUID.v5 über (userId|t3Count|t7Count|minute-window)

Payload-Limit:
  Max: 7900 Bytes (PostgreSQL NOTIFY limit ~8KB)
  Validation: Events > maxPayloadSize werden mit Exception abgelehnt
  Config: cqrs.events.max-payload-size (default: 7900)
  Implementation: EventPublisher.java validiert Payload-Größe vor Publishing

RBAC:
  Erlaubte Rollen: MANAGER | SALES | ADMIN
  freshplan.security.allow-unauthenticated-publisher: false (Prod), in Tests explizit true
  Metriken zählen: denied und unauthenticated

Metriken (Micrometer/Prometheus):
  freshplan_events_published{event_type,module,result}
  freshplan_events_consumed{event_type,module,result}
  freshplan_event_latency{event_type,path}
  freshplan_dedupe_cache_entries
  freshplan_dedupe_cache_hit_rate

Implementation-Details:
  Publisher: AFTER_COMMIT Pattern (nur in Publishern, nie in Listeners) - verbindlich seit PR #111
  Listener: Caffeine Cache für Deduplizierung (500k entries, 24h TTL)
  Channels: dashboard_updates, cross_module_events, settings_invalidated
  Performance SLO: listen_notify_lag_ms < 10000
```

---

<a id="sprint-217---results"></a>
## 🎯 **Sprint 2.1.7 - Results (ActivityOutcome Enum + Opportunity Backend Integration + Code Review Fixes)**

**Status:** ✅ COMPLETE (14.10.2025 - PR #139 READY FOR MERGE)

**Kernfeatures implementiert:**
- **ActivityOutcome Enum (V10027):** 7 values (SUCCESSFUL, UNSUCCESSFUL, NO_ANSWER, CALLBACK_REQUESTED, INFO_SENT, QUALIFIED, DISQUALIFIED) · VARCHAR + CHECK Constraint Pattern · Backend + Frontend Integration · 14 Tests GREEN (ActivityDialog.test.tsx)
- **Opportunity Backend Integration (V10026):** lead_id + customer_id Foreign Keys · Opportunity → Lead/Customer relationships · Business-Logic für Lead→Opportunity→Customer workflows
- **Customer Number Sequence (V10028 - PRODUCTION-KRITISCH):** PostgreSQL Sequence `customer_number_seq` · Race-condition-safe generation (nextval) · Format KD-00001, KD-00002, ... · Eliminates `count() + 1` race condition
- **DEV-SEED Opportunities (V90003):** 10 realistische Opportunities (IDs 90001-90010) · Total Value €163,000 · 4 from Leads + 6 from Customers
- **Code Review Fixes (10 Issues):**
  - 6 Code Review Issues (Copilot AI + Gemini Code Assist): Race Condition fix, Clock Injection (Issue #127), Return Type Consistency (DTO pattern), Redundant persist() removal
  - 3 Pre-existing Test Fixes: FollowUpAutomationServiceTest (9/9 GREEN), CustomerRepositoryTest (43/43 GREEN), DEV-SEED Auto-Recovery
  - 1 CI ESLint Fix: ActivityDialog.test.tsx linting errors

**Migrations deployed:**
- **V10026:** Opportunity lead_id + customer_id Foreign Keys
- **V10027:** activity_outcome VARCHAR(30) + CHECK Constraint (7 values)
- **V10028:** customer_number_seq (PRODUCTION-KRITISCH - race-condition-safe)
- **V90003:** DEV-SEED Opportunities (10 Opportunities, €163,000 total)

**Tests & Qualität:**
- **60/60 Backend Tests GREEN (100%):** All LeadResourceTest + Security Tests
- **Frontend Tests GREEN:** ActivityDialog.test.tsx (14 Tests), CI ESLint passed
- **CI Status:** 29/29 Checks passed

**Business Value:**
- **Race-Condition-Safe Customer Numbers:** Production-safe unter high concurrency
- **Type-Safe Activity Outcomes:** No mehr ungültige Outcome-Strings
- **Opportunity Backend Ready:** Lead → Opportunity → Customer workflows implementierbar
- **Testability Improved:** Clock Injection Pattern (Issue #127) für deterministische Tests

**Referenzen:**
- [TRIGGER_SPRINT_2_1_7.md](TRIGGER_SPRINT_2_1_7.md) - Sprint-Trigger (UNTERSCHEIDET sich vom completed Sprint!)
- [Sprint-Map Modul 02](features-neu/02_neukundengewinnung/SPRINT_MAP.md)
- [PR #139](https://github.com/joergstreeck/freshplan-sales-tool/pull/139) - READY FOR MERGE
- [/tmp/pr-comment.md](/tmp/pr-comment.md) - Detailed Code Review Fixes Summary

**⚠️ WICHTIG:** TRIGGER_SPRINT_2_1_7.md beschreibt einen ANDEREN geplanten Sprint (Team Management & Test Infrastructure), nicht diesen completed Sprint!

---

<a id="sprint-216-phase-2---results"></a>
## 🎯 **Sprint 2.1.6 Phase 2 - Results (Bestandsleads-Migration & Admin Features & BusinessType Harmonization)**

**Status:** ✅ COMPLETE (Commits 01819eb, ce9206a - Branch: feature/mod02-sprint-2.1.6-admin-apis)

**Kernfeatures implementiert:**
- **LeadImportService (297 LOC):** Batch-Import bis 1000 Leads · Dry-Run Mode · SHA-256 Idempotency · Duplikaten-Check mit isCanonical=false
- **LeadBackdatingService (107 LOC):** Historisches registeredAt setzen · Deadline-Neuberechnung (6M Protection + 60T Progress) · Stop-the-Clock Integration
- **LeadConvertService (204 LOC):** Lead → Customer + Location + Address + Contact (vollständige Field Harmonization) · Java Locale Country Mapping (200+ Länder)
- **REST APIs:** POST /api/admin/migration/leads/import (Admin-only) · PUT /api/leads/{id}/registered-at (Admin/Manager) · POST /api/leads/{id}/convert (All roles)
- **DTOs:** 6 neue Request/Response-Paire für Import/Backdating/Convert
- **DB Migrations:** V261 (Customer.originalLeadId), V263 (Lead.businessType), V264 (Customer.businessType + Data Migration)
- **Enum-Migration Strategie (3-Phasen-Plan - VARCHAR + CHECK Constraint Pattern):**
  - **Architektur-Entscheidung:** VARCHAR + CHECK Constraint (NICHT PostgreSQL ENUM Type!)
    - Begründung: JPA-Standard `@Enumerated(STRING)` funktioniert direkt (kein Custom Converter)
    - Schema-Evolution einfach (CHECK Constraint ändern = 2 Zeilen SQL)
    - Performance nur ~5% langsamer als PostgreSQL ENUM bei B-Tree Index
  - **Phase 1 (Sprint 2.1.6 Phase 5):** Lead-Modul Enums (LeadSource, BusinessType, KitchenSize)
    - LeadSource: MESSE, EMPFEHLUNG, TELEFON, WEB_FORMULAR, PARTNER, SONSTIGES
    - BusinessType: 9 unified values (RESTAURANT, HOTEL, CATERING, KANTINE, GROSSHANDEL, LEH, BILDUNG, GESUNDHEIT, SONSTIGES)
    - KitchenSize: KLEIN, MITTEL, GROSS, SEHR_GROSS
    - Migration V273: ALTER TABLE + CHECK Constraints + B-Tree Indizes
    - Frontend Hooks (useLeadSources, useBusinessTypes, useKitchenSizes)
  - **Phase 2 (Sprint 2.1.6.1 Phase 1):** Customer-Modul BusinessType-Migration
    - Customer.industry → Customer.businessType (9 Werte harmonisiert)
    - Dual-Mode: Auto-Sync Setter für Rückwärtskompatibilität
    - Migration V27X (dynamisch): VARCHAR + CHECK Constraint Pattern
    - Frontend useBusinessTypes()
  - **Phase 3 (Sprint 2.1.6.1 Phase 2):** CRM-weit Enum-Harmonisierung
    - ActivityType erweitern, OpportunityStatus, PaymentMethod, DeliveryMethod
    - EnumResource API erweitert: 4 neue Endpoints
    - Frontend Hooks: useActivityTypes, useOpportunityStatuses, usePaymentMethods, useDeliveryMethods
    - Pattern: Konsistent VARCHAR + CHECK für alle Enums
  - **Business-Rule:** MESSE/TELEFON = Erstkontakt PFLICHT (Pre-Claim Logic Variante B: `source.requiresFirstContact()` → `firstContactDocumentedAt` != NULL)
  - **Performance:** ~9x schneller als String-LIKE durch B-Tree Index-Nutzung (nicht ENUM Type!)
  - **Type-Safety:** Compiler-Validierung statt Runtime-Errors
  - **Timing:** Pre-Production = optimales Zeitfenster (keine Daten-Migration, Clean Slate)
  - **Artefakt:** [ENUM_MIGRATION_STRATEGY.md](features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md)

**Migrations deployed:**
- **V261:** Add customer.original_lead_id (BIGINT NULL, Soft Reference, Partial Index)
- **V263:** Add leads.business_type + CHECK constraint (9 values)
- **V264:** Add customers.business_type + Data Migration (Industry → BusinessType) + CHECK constraint

**Tests & Qualität:**
- 33 Tests (100% passing): Import (14), Backdating (13), Convert (6)
- Backend compiles successfully
- Frontend TypeScript compiles without errors
- Vite dev server runs without errors
- All enum endpoints accessible (business-types, lead-sources, kitchen-sizes)

**Business Value:**
- **500 Leads in 5 Minuten** importieren (statt 3 Tage manuell)
- **Historische Daten korrekt** (registeredAt = März 2024)
- **Schutzfristen automatisch** neu berechnet
- **Lead → Kunde Conversion** mit ALLEN Daten (Adresse, Kontakt, Historie)
- **Single Source of Truth:** Add new business types in ONE place (BusinessType.java) → automatic propagation to all forms

**Referenzen:**
- [TRIGGER_SPRINT_2_1_6.md](TRIGGER_SPRINT_2_1_6.md)
- [HARMONIZATION_COMPLETE.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_6/HARMONIZATION_COMPLETE.md)
- [AUTOMATED_JOBS_SPECIFICATION.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_6/AUTOMATED_JOBS_SPECIFICATION.md) ⭐ NEU (2025-10-06)
- [Sprint-Map Modul 02](features-neu/02_neukundengewinnung/SPRINT_MAP.md)

**Sprint 2.1.6 Phase 3 - Automated Jobs (Ready for Implementation):**
- ✅ **Specification COMPLETE:** AUTOMATED_JOBS_SPECIFICATION.md (418 Zeilen, 6 ADRs)
- ✅ **Branch ready:** feature/mod02-sprint-2.1.6-nightly-jobs
- 🔧 **Implementation pending:** 4 Nightly Jobs (Progress Warning, Protection Expiry, DSGVO Pseudonymization, Import Cleanup)
- 📋 **Next Steps:** Phase 4 (Frontend UI + Excel Upload), Phase 5 (Accessibility)

---

<a id="sprint-214---results"></a>
## 🎯 **Sprint 2.1.4 - Results (Lead Deduplication & Data Quality)**

**Status:** ✅ COMPLETE (PR #123 merged 2025-10-01)

**Kernfeatures implementiert:**
- **Normalisierung:** email (lowercase), phone (E.164), company (ohne Suffixe/Rechtsformen)
- **Unique Indizes:** Partiell mit `WHERE status != 'DELETED'` für email/phone/company
- **Idempotency Service:** 24h TTL · SHA-256 Request-Hash · atomic `INSERT … ON CONFLICT`
- **CI Performance Breakthrough:** ~24 min → **~7 min** (-70%)
  - JUnit Platform-Parallelisierung aktiv (Maven Surefire gesteuert)
  - ValidatorFactory in **@BeforeAll** (statt @BeforeEach) - 56s gespart über 8 Test-Klassen
  - Test-Migration: @QuarkusTest ↓27% (8 DTO-Tests → Plain JUnit mit Mockito)

**Migrations deployed:**
- **V247:** Normalisierungs-Spalten (email_normalized, phone_e164, company_name_normalized)
- **V10012:** Test/Dev-only Indizes (non-CONCURRENTLY für schnelle CI-Tests)
- **V251-V254:** Idempotency-Fixes, Events published column, Registered-at backdating
- **R__normalize_functions.sql:** Repeatable normalization functions (PostgreSQL)

**Tests & Qualität:**
- 1196 Tests in 7m29s, 0 Failures
- Performance dokumentiert in `backend/TEST_DEBUGGING_GUIDE.md`
- Operations Runbook: `/docs/operations/lead-deduplication-runbook.md`

**Referenzen:**
- [TRIGGER_SPRINT_2_1_4.md](TRIGGER_SPRINT_2_1_4.md)
- [Sprint-Map Modul 02](features-neu/02_neukundengewinnung/SPRINT_MAP.md)
- [Operations Runbook](../operations/lead-deduplication-runbook.md)

### **📁 Codebase-Structure Reality**
```yaml
PROJECT-STRUCTURE:
/backend
  /modules              # Modular-Monolith-Architecture
    /customer           # Module 03 - Customer-Management
      /core             # Domain-Logic (Pure Business)
      /api              # REST-Controllers + DTOs
      /infrastructure   # Database + External-Services
    /communication      # Module 05 - Email + Sample-Follow-up
    /settings           # Module 06 - Settings-Core-Engine
  /legacy               # Legacy-Code (Migration ongoing)
  /shared               # Cross-Module Utilities + Security

/frontend
  /src
    /components         # Reusable UI-Components
    /features           # Feature-specific Components
    /services           # API-Clients + Business-Logic
    /types              # TypeScript Type-Definitions
  /legacy               # Legacy-Frontend (Migration ongoing)

/docs/planung           # Comprehensive Planning-Documentation
  /features-neu         # Module 01-08 Technical-Concepts
  /claude-work          # Daily-Work + Implementation-Logs
  /grundlagen           # Foundation-Documents (Standards/Patterns)
```

### **🔌 API-Patterns & Database-Schema Reality**
```yaml
REST-API-PATTERNS:
  Standard: OpenAPI 3.1 + RFC7807 Problem-Details + ETag-Caching
  Authentication: Bearer-Token (Keycloak JWT) + ABAC-Claims
  Pagination: Offset/Limit + Total-Count-Headers
  Filtering: Query-Parameters + RSQL-Support für komplexe Filters
  Example: GET /api/customers?territory=DE&status=ACTIVE&page=0&size=20

DATABASE-PATTERNS:
  Primary-Keys: UUID v7 (Time-ordered für Performance)
  Multi-Tenancy: Row-Level-Security (RLS) + territory_id Column
  JSONB-Usage: Customer-Fields + Settings + Communication-Metadata
  Migrations: Flyway V-numbered + R-repeatable für Data-Migrations
  Example-Table: customers (id, territory_id, base_fields JSONB, custom_fields JSONB)

SECURITY-IMPLEMENTATION:
  RLS-Policy: CREATE POLICY customers_tenant ON customers USING (territory_id = current_setting('app.territory_id'))
  ABAC-Check: @PreAuthorize("hasPermission(#territoryId, 'CUSTOMER', 'READ')")
  Audit-Trail: created_at + updated_at + created_by + updated_by (automatisch)
```

---

<a id="sektion-4-development-patterns"></a>
## 🎯 **SEKTION 4: DEVELOPMENT-PATTERNS** (30%)

### **📝 Code-Standards & Architecture-Decisions**
```yaml
CODING-STANDARDS (aus CLAUDE.md):
  Line-Length: 80-100 Zeichen max (Readability über Cleverness)
  Naming: PascalCase (Classes) + camelCase (Methods) + UPPER_SNAKE (Constants)
  Comments: KEINE Comments außer JavaDoc/JSDoc (Code soll self-explanatory sein)
  Git-Commits: Conventional Commits (feat:/fix:/chore:) + Detailed Descriptions

ARCHITECTURE-DECISIONS (ADRs):
  Modular-Monolith: Modules statt Microservices für Development-Velocity + Simplicity
  CQRS-Light: PostgreSQL LISTEN/NOTIFY statt Event-Bus für Cost-Efficiency
  RLS-Security: Database-Level Security statt Application-Level für Performance
  React-Query: Server-State-Management statt Redux für Caching + Simplicity

QUALITY-GATES:
  Test-Coverage: ≥80% (Unit + Integration + E2E)
  Security-Scans: SonarCloud + Dependabot + OWASP-ZAP
  Performance: API <200ms P95 + Bundle <200KB + Lighthouse >90
  Code-Review: Two-Pass (Spotless Auto-Format + Strategic Review)
```

### **🏗️ Business-Logic-Patterns**
```yaml
DOMAIN-DRIVEN-DESIGN:
  Entities: Rich-Domain-Models mit Business-Logic (nicht Anemic)
  Value-Objects: Territory, Currency, Email-Address für Type-Safety
  Services: Business-Logic-Services für Cross-Entity-Operations
  Repositories: Database-Access-Layer mit ABAC + RLS-Integration

B2B-FOOD-BUSINESS-PATTERNS:
  Multi-Contact-Entity: Customer hat multiple Contacts mit Roles (CHEF/BUYER)
  Territory-Scoping: Alle Entities haben territory_id für RLS + Multi-Tenancy
  Seasonal-Logic: Business-Rules-Engine für Spargel/Oktoberfest/Weihnachten
  Lead-Management: KEIN Gebietsschutz - deutschland-weite Lead-Verfügbarkeit

COMMUNICATION-PATTERNS:
  Thread/Message/Outbox: Enterprise-Email-Management mit Reliability
  Sample-Follow-up: T+3/T+7 Automation mit SLA-Engine
  Activity-Tracking: Cross-Module-Events für Customer-Timeline
  Audit-Trail: Complete Change-History für Compliance + Legal-Requirements

SETTINGS-PATTERNS:
  5-Level-Scope-Hierarchy: GLOBAL→TENANT→TERRITORY→ACCOUNT→CONTACT_ROLE
  JSON-Schema-Validation: Runtime-Validation + Type-Safety + Evolution
  Cache-Layer: L1 Memory + ETag + LISTEN/NOTIFY für <50ms Performance
  Business-Rules-Engine: Territory + Seasonal + Role-specific Logic
```

### **🧪 Testing-Approach & Performance-Optimization**
```yaml
TESTING-PYRAMID:
  Unit-Tests (70%): Business-Logic + Domain-Models mit Mockito + AssertJ
  Integration-Tests (20%): API-Layer + Database mit Testcontainers + RestAssured
  E2E-Tests (10%): Critical-User-Journeys mit Playwright + Real-Database

TESTING-PATTERNS:
  Given-When-Then: BDD-Style für Readability + Business-Alignment
  Test-Data-Builders: Fluent-APIs für Test-Data-Creation + Maintainability
  Contract-Testing: OpenAPI-Schema-Validation für API-Compatibility
  Performance-Testing: k6-Load-Tests für P95-Targets + Scalability

PERFORMANCE-OPTIMIZATION:
  Database: Hot-Projections + GIN-Indexes + Partitioning + Query-Optimization
  Caching: ETag-HTTP-Caching + L1-Memory-Cache + CDN für Static-Assets
  Frontend: Code-Splitting + Lazy-Loading + Bundle-Optimization + Tree-Shaking
  API: Response-Compression + Parallel-Queries + Efficient-Pagination

MONITORING & OBSERVABILITY:
  Metrics: Micrometer + Prometheus (Golden-Signals: Latency/Traffic/Errors/Saturation)
  Tracing: OpenTelemetry für Distributed-Tracing + Performance-Analysis
  Logging: Structured-JSON-Logs + Correlation-IDs + Log-Aggregation
  Health-Checks: Custom-Health-Indicators für Business-Logic + Dependencies
```

---

<a id="quick-reference-modul-status-matrix"></a>
## 🎖️ **QUICK-REFERENCE: MODUL-STATUS-MATRIX**

```yaml
PLANNING-COMPLETE (Ready für Implementation):
  ✅ Modul 01 Mein-Cockpit: 44 Artefakte (A+ Enterprise-Assessment)
  ✅ Modul 02 Neukundengewinnung: 85% IMPLEMENTED
     Backend: Lead CRUD, Territory, Follow-ups ✅ · Frontend: List+Create ✅ · Qualität: Normalisierung + Idempotenz ✅
     Pending: Protection (6M+60T+10T) + Progressive Profiling (3 Stufen)
  ✅ Modul 03 Kundenmanagement: Field-based Architecture + ABAC-Security
  ✅ Modul 04 Auswertungen: Advanced Analytics + Territory-Insights
  ✅ Modul 05 Kommunikation: Enterprise Email-Engine + SLA-Automation
  ✅ Modul 06 Einstellungen: 4 Weltklasse Technical-Concepts (9.9-10/10)
  ✅ Modul 07 Hilfe-Support: CAR-Strategy + 25 AI-Artefakte (9.4/10)
  ✅ Modul 08 Administration: 76 Artefakte + Phasen-Architecture (9.6/10)

INFRASTRUCTURE-READY:
  ✅ Integration: API-Gateway + Event-Driven (95% Ready)
  ✅ Leistung: Performance-Engineering + Optimization (Ready)
  ✅ Skalierung: Territory + Seasonal-Autoscaling (98% Ready)
  ✅ Betrieb: Operations-Excellence + Monitoring (95% Ready)

IMPLEMENTATION-PRIORITIES:
  P0: Infrastructure Foundation-Deployment (Integration + Leistung)
  P1: Core-Business-Modules (Cockpit + Neukundengewinnung + Kundenmanagement)
  P2: Communication + Analytics (Kommunikation + Auswertungen)
  P3: Platform-Services (Einstellungen + Hilfe + Administration)
```

---

<a id="ki-arbeitshinweise"></a>
## 🤖 **KI-ARBEITSHINWEISE**

### **Für neue Claude-Instanzen:**
- **Sofort produktiv:** Alle Module haben detaillierte Technical-Concepts + Artefakte
- **Implementation-ready:** Copy-Paste-Code verfügbar, nur Integration + Testing required
- **Business-Context:** B2B-Food-Komplexität verstanden, KEIN Gebietsschutz beachten
- **Quality-Standards:** Code-Standards aus CLAUDE.md + Planungsmethodik.md befolgen

### **Für externe KI-Consultants:**
- **Strategic Vision:** 8-Module-Ecosystem für B2B-Food-CRM mit Territory + Seasonal-Intelligence
- **Technical Innovation:** CQRS Light + CAR-Strategy + 5-Level-Settings + Risk-Tiered-Approvals
- **Business Value:** Lead-Conversion + Sample-Success + Cost-Optimization + Competitive-Advantage
- **Integration-Points:** Cross-Module-Events + Settings-as-a-Service + Help-as-a-Service

<a id="cross-references"></a>
## 📚 **Cross-References (Living Documentation)**

**Siehe auch (SoT-Pack Integration):**
- **[TECHNICAL_CONCEPT_CORE.md](./features-neu/06_einstellungen/TECHNICAL_CONCEPT_CORE.md)** - Settings Core Engine + Scope-Hierarchie
- **[TECHNICAL_CONCEPT_BUSINESS.md](./features-neu/06_einstellungen/TECHNICAL_CONCEPT_BUSINESS.md)** - B2B-Food Business Logic + Multi-Contact
- **[technical-concept.md](./features-neu/00_infrastruktur/skalierung/technical-concept.md)** - Territory + Seasonal-aware Autoscaling
- **[CRM_COMPLETE_MASTER_PLAN_V5.md](./CRM_COMPLETE_MASTER_PLAN_V5.md)** - Aktueller Projektstand + Implementation-Timeline
- **[Module-Analysis.md](./Module-Analysis.md)** - Vollständige Modul-Status-Übersicht + Production-Ready Assessment

**Wichtige Architektur-Leitlinien für neue KIs:**
✅ **DO:** LISTEN/NOTIFY für Events, RLS für Security, userbasiertes Ownership
✅ **DO:** Territory als Datenraum behandeln, KEIN Gebietsschutz implementieren
✅ **DO:** 5-50 Nutzer-Scale, kosteneffiziente Architektur

### **Critical Success Factors:**
1. **Performance:** <200ms P95 API + <50ms Settings + >99.9% Availability
2. **Security:** ABAC + RLS + Territory-Scoping + DSGVO-Compliance
3. **Business-Logic:** Multi-Contact-B2B + Seasonal-Intelligence + KEIN Gebietsschutz
4. **User-Experience:** CAR-Help-System + Real-time-Dashboards + Mobile-Optimized

---

**🎯 Dieses Dokument gibt jeder KI das vollständige FreshFoodz B2B-Food-CRM System-Verständnis für sofortige Produktivität!**