# 🚀 FreshPlan Sales Command Center

**📅 Aktuelles Datum: 25.09.2025**

> **"Wir bauen ein intelligentes Sales Command Center, das unsere Vertriebsmitarbeiter lieben, weil es ihnen proaktiv die Informationen, Insights und geführten Prozesse liefert, die sie brauchen, um erfolgreich zu sein."**

> **📖 Bitte zuerst lesen: [Way of Working](WAY_OF_WORKING.md) - Unsere verbindlichen Entwicklungsstandards**

## 🎯 Die Vision

Wir entwickeln nicht nur ein CRM, sondern ein **Sales Command Center** - eine revolutionäre 3-Spalten-Oberfläche, die Vertriebsmitarbeiter durch ihren Tag führt:

- **Spalte 1: Mein Tag** - Proaktive Prioritäten, KI-Empfehlungen, Triage-Inbox
- **Spalte 2: Fokus-Liste** - Dynamische Kundenlisten mit Enterprise-Features
- **Spalte 3: Aktions-Center** - Geführte Prozesse für maximale Effizienz

Mehr Details: [Vision & Roadmap](./VISION_AND_ROADMAP.md) | [Master Plan V5](./docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md) | [Production Roadmap](./docs/planung/PRODUCTION_ROADMAP_2025.md)

## 📊 Projekt-Status

### Phase 1: Foundation ✅ COMPLETE (100%)
- **Sprint 1.1-1.4:** CQRS Light, Security, Settings, CI/CD ✅
- **Sprint 1.5:** RLS Connection Affinity Security Fix (PR #106) ✅
- **Sprint 1.6:** RLS Module Adoption (PR #107) ✅
- **Performance:** P95 <200ms erreicht mit ETag ≥70% Hit-Rate

### Phase 2: Core Business 🔧 IN PROGRESS (10%)
- **Sprint 2.1:** Neukundengewinnung - Lead-Management läuft
- **Sprint 2.2-2.5:** Kundenmanagement, Kommunikation, Cockpit, Einstellungen

### Aktuelle PRs: 11/37 merged (30% gesamt)

Details: [Production Roadmap 2025](./docs/planung/PRODUCTION_ROADMAP_2025.md)

## 🏗️ Architektur & Technologie

### Tech Stack
- **Frontend**: React 18 + TypeScript + Vite + Zustand + React Query
- **Backend**: Quarkus + PostgreSQL + Testcontainers
- **Security**: Row-Level Security (RLS) + @RlsContext CDI Interceptor + Fail-closed Pattern
- **Auth**: Keycloak (OIDC)
- **Cloud**: AWS (ECS, RDS, CloudFront)

## Mock-Governance (Business-Logic mock-frei)
- Business-Pfade: `frontend/src/{app,features,lib,hooks,store}` dürfen keine Mocks importieren.
- Ausnahmen: Tests/Stories/Fixtures.
- CI „mock-guard" blockt PRs bei Verstößen.
- Dev-Seeds: lokale Daten via Flyway `db/dev-migration` (nur `dev`-Profil).

**Schnellstart**
```bash
# Frontend (ESLint + Husky)
cd frontend && npm i && npx husky install

# Backend (Dev-Seeds)
# quarkus:dev lädt db/migration + db/dev-migration im dev-Profil
```

### Monorepo Structure
```
freshplan-sales-tool/
├── /legacy              # Stable monolith (tagged as legacy-1.0.0)
├── /frontend            # React SPA - Das Sales Command Center
├── /backend             # Quarkus API - Enterprise-ready Services
├── /infrastructure      # Docker, AWS CDK, K8s
├── /docs               # Architecture decisions, guides
└── /.github/workflows  # CI/CD pipelines
```

## 🚀 Quick Start

### Prerequisites
- Node.js 20+
- Java 17+
- Docker & Docker Compose
- AWS CLI (for deployment)

### Development Setup

```bash
# Clone the repository
git clone https://github.com/joergstreeck/freshplan-sales-tool.git
cd freshplan-sales-tool

# Quick start with our session script:
./scripts/session-start.sh

# Or manually start services:
# Start infrastructure (PostgreSQL, Keycloak)
cd infrastructure
docker-compose up -d

# Start backend
cd ../backend
./mvnw quarkus:dev

# Start frontend
cd ../frontend
npm install
npm run dev
```

### 🚀 Access the Sales Command Center

Once all services are running:
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Dev UI**: http://localhost:8080/q/dev

### Legacy Application

The stable legacy application is preserved in `/legacy` and tagged as `legacy-1.0.0`.

```bash
cd legacy
npm install
npm run dev
```

## 📚 Documentation

### 🎯 Vision & Strategy
- [Vision & Roadmap](./VISION_AND_ROADMAP.md) - Sales Command Center Vision
- [Master Plan V4](./docs/CRM_COMPLETE_MASTER_PLAN.md) - Die finale Wahrheit
- [Way of Working](./WAY_OF_WORKING.md) - Unsere Entwicklungsstandards

### 🛠️ Technical Documentation
- [API Contract](./docs/technical/API_CONTRACT.md) - Frontend/Backend Schnittstelle
- [Architecture Decisions](./docs/adr/) - Wichtige Entscheidungen
- [Technical Roadmap](./docs/technical/FRESHPLAN_2.0_TECHNICAL_ROADMAP.md)
- [Migration Plan](./docs/technical/WEB_APP_MIGRATION_PLAN.md)
- [Team Setup](./docs/team/TEAM_SETUP.md)

## 🤝 Team & AI Collaboration

- **Frontend Team** + ChatGPT
- **Backend Team** + Claude
- **DevOps Team** + Both AIs

## 📋 Current Phase: Phase 1 - Das begeisternde Fundament

### ✅ Bereits erfolgreich implementiert:
- [x] Backend APIs: Customer Management, Timeline, User Management
- [x] Frontend: CustomerList, Calculator, Design System Grundlagen
- [x] Infrastructure: PostgreSQL, Flyway, Testcontainers
- [x] Integration Tests: Alle 18 Tests grün mit Testcontainers

### 🚧 Aktuell in Arbeit:
- [ ] **Sales Cockpit Frontend** - 3-Spalten-Layout mit Zustand
- [ ] **Backend-for-Frontend (BFF)** - Optimierte Datenabfragen
- [ ] **Activity Timeline Frontend** - Kundenhistorie visualisieren
- [ ] **Responsive Design** - Mobile-first Approach

## 🎯 Unsere Philosophie: Die 3 Kernprinzipien

### 1. 🎯 Geführte Freiheit (Guided Freedom)
Das System führt den Nutzer mit intelligenten Standard-Workflows, ohne zu bevormunden.

### 2. 🔗 Alles ist miteinander verbunden
Keine Information ist eine Sackgasse - jeder Datenpunkt führt zur nächsten relevanten Aktion.

### 3. 📈 Skalierbare Exzellenz
Enterprise-ready von Tag 1 mit Performance < 200ms und proaktiver Datenqualität.

## 🏆 Unser Qualitätsversprechen

Bei jedem Commit verpflichten wir uns zu:
- **Sauberkeit**: Klare Modul-Grenzen, sprechende Namen, keine toten Pfade
- **Robustheit**: Unit/Integration/E2E Tests, defensives Error-Handling
- **Wartbarkeit**: SOLID, DRY, CI-Checks, lückenlose Docs & ADRs
- **Transparenz**: Unklarheiten sofort ansprechen

Details: [CLAUDE.md](./CLAUDE.md) | [Way of Working](./WAY_OF_WORKING.md)