# 🤖 CRM AI Context Schnell - KI-optimiertes System-Verständnis

**📅 Letzte Aktualisierung:** 2025-10-01
**🎯 Zweck:** Schnelle KI-Einarbeitung in FreshFoodz B2B-Food-CRM System
**📊 Ansatz:** 80% Planungsstand + 20% Codebase-Realität (Hybrid Living Document)
**🤖 Zielgruppe:** Externe KIs + neue Claude-Instanzen + AI-Consultants

> **🏗️ Architecture Flags (Stand: 2025-10-01)**
> - **CQRS Light aktiv** (`features.cqrs.enabled=true`), **eine Datenbank**, getrennte Command/Query-Services
> - **Events:** **PostgreSQL LISTEN/NOTIFY mit Envelope v2** (siehe Event-Backbone unten)
> - **Security:** Territory = **RLS-Datenraum** (DE/CH/AT), **Lead-Protection = userbasiertes Ownership**
> - **Settings-Registry (Hybrid JSONB + Registry)** produktiv, ETag + LISTEN/NOTIFY Cache-Invalidation
> - **Lead Deduplication aktiv** (V247: email/phone/company normalisiert, partielle UNIQUE Indizes)
> - **Idempotency Service operational** (24h TTL, SHA-256 Request-Hash, atomic INSERT … ON CONFLICT)
> - **CI optimiert:** 24min → 7min (70% schneller) - JUnit parallel (Surefire gesteuert), ValidatorFactory @BeforeAll
> - **Migrations-Hygiene:** Prod-Migrations <V8000, **V10xxx** = Test/Dev-only (z.B. V10012 Indizes ohne CONCURRENTLY)
> - **Scale:** **5-50 Nutzer** mit saisonalen Peaks, **internes Tool**, kosteneffiziente Architektur

---

## 🗺️ **MODUL-OVERLAYS (Schnellzugriff)**

**Hybrid-Struktur:** Zentrale Sprints + Modulare Overlays für detaillierte Navigation

- **Modul 02 Neukundengewinnung:** [features-neu/02_neukundengewinnung/_index.md](features-neu/02_neukundengewinnung/_index.md)
- **AI-Startseite:** [START_HERE_FOR_AI.md](START_HERE_FOR_AI.md)
- **Planungsmethodik:** [PLANUNGSMETHODIK.md](PLANUNGSMETHODIK.md)

---

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
  Status: ✅ 85% IMPLEMENTED - Lead-Management MVP + Frontend + Deduplication
  PRs: #103, #105, #110, #111 (Integration), #122 (Frontend), #123 (Dedup/Quality)
  Delivered:
    - Frontend MVP: Lead List + Create Dialog · Feature-Flag VITE_FEATURE_LEADGEN
    - Lead-Normalisierung: email lowercase · phone E.164 · company ohne Rechtsform-Suffixe
    - Idempotenz: 24h TTL · SHA-256 Hashing · atomic Upsert (ON CONFLICT)
    - Production Patterns: Security (23 Tests), Performance (P95 <7ms), Event (AFTER_COMMIT)

  Business Logic (Sprint 2.1.5 Planungsstand):
    PRE-CLAIM MECHANIK (Stage 0):
      - Lead ohne Kontakt/Erstkontakt = Pre-Claim (registered_at = NULL)
      - 10 Tage Frist zur Vervollständigung (kein Schutz in Stage 0)
      - Schutz startet erst bei: Kontakt ODER dokumentiertem Erstkontakt
      - Ausnahme: Bestandsleads bei Migration → sofortiger Schutz

    DEDUPE POLICY (2.1.5 - Hard Collisions only):
      - HARD BLOCK: Email/Phone/Firma+PLZ exakt → Manager-Override erforderlich
      - SOFT WARN: Domain+Stadt ODER Firma+Stadt → Warnung + Fortfahren erlaubt
      - Normalisierung: Email lowercase, Phone E.164, Company ohne Suffixe/Rechtsformen
      - Kein Fuzzy-Matching (pg_trgm) → Sprint 2.1.6

    ACTIVITY TYPES PROGRESS-MAPPING (13 Types):
      - Progress (countsAsProgress=true, 5): QUALIFIED_CALL, MEETING, DEMO, ROI_PRESENTATION, SAMPLE_SENT
      - Non-Progress (countsAsProgress=false, 5): NOTE, FOLLOW_UP, EMAIL, CALL, SAMPLE_FEEDBACK
      - System (countsAsProgress=false, 3): FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, LEAD_ASSIGNED
      - V257 Trigger: update_progress_on_activity() fired NUR bei countsAsProgress=TRUE

    DSGVO CONSENT (Source-abhängig):
      - source = WEB_FORMULAR → Consent PFLICHT (Art. 6 Abs. 1 lit. a)
      - source != WEB_FORMULAR → Berechtigtes Interesse (Art. 6 Abs. 1 lit. f)
      - Rechtliche Grundlage: B2B-Geschäftsanbahnung (berechtigtes Interesse)

  Sprint 2.1.5 Status (04.10.2025 - UX Best Practice dokumentiert):
    - ✅ **Backend:** V255-V258 Migrations COMPLETE (31 Unit Tests, Protection + Progress + 13 Activity-Types)
    - ✅ **UX-Regeln (Best Practice):** Progressive Profiling - Jede Karte separat speicherbar
      - Button-Logik: [Vormerkung speichern], [Registrierung speichern], [Qualifizierung speichern]
      - Erstkontakt: Nur auf Karte 0 (optional, nachträglich möglich), startet Schutz
      - DSGVO: KEINE Checkbox bei Vertrieb (nur Hinweis Art. 6 Abs. 1 lit. f)
      - Pflichtfeld Karte 1: Mind. 1 Kontaktkanal (Email ODER Phone)
      - Pre-Claim: 10-Tage-Frist wenn kein Erstkontakt, Badge "⏳ läuft ab in X Tagen"
    - ✅ **Dokumentation:** FRONTEND_DELTA.md Section 0 (UX-Regeln) + TRIGGER korrigiert + Anti-Patterns
    - ⏸️ **Frontend Code:** LeadWizard.tsx Refactor (Button-Logik, Erstkontakt auf Karte 0, DSGVO-Hinweis)
    - ⏸️ **Sprint 2.1.6:** Backdating, consent_given_at (V259), Bestandsleads-Migration, Nightly Jobs

  Key-Features: KEIN Gebietsschutz + T+3/T+7 Automation + Multi-Contact-B2B

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