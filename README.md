# FreshPlan 2.0 - Enterprise Sales Platform

## 🏗️ Monorepo Structure

```
freshplan-sales-tool/
├── /legacy              # Stable monolith (tagged as legacy-1.0.0)
├── /frontend            # React SPA (Sprint 0+)
├── /backend             # Quarkus API (Sprint 0+)
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

### Legacy Application

The stable legacy application is preserved in `/legacy` and tagged as `legacy-1.0.0`.

```bash
cd legacy
npm install
npm run dev
```

## 📚 Documentation

- [Technical Roadmap](./FRESHPLAN_2.0_TECHNICAL_ROADMAP.md)
- [Migration Plan](./WEB_APP_MIGRATION_PLAN.md)
- [Architecture Decisions](./docs/adr/)
- [Vision & Roadmap](./VISION_AND_ROADMAP.md)

## 🤝 Team & AI Collaboration

- **Frontend Team** + ChatGPT
- **Backend Team** + Claude
- **DevOps Team** + Both AIs

## 📋 Current Sprint

**Sprint 0 - Walking Skeleton**
- [ ] Monorepo setup
- [ ] Keycloak integration
- [ ] Basic React app with auth
- [ ] Secured API endpoint
- [ ] E2E test pipeline

## 🎯 Code Quality Commitment

We commit to:
- **Sauberkeit**: Clear module boundaries, meaningful names, no dead code
- **Robustheit**: Unit/integration/E2E tests, defensive error handling
- **Wartbarkeit**: SOLID, DRY, CI checks, complete docs & ADRs
- **Transparenz**: Immediate escalation of uncertainties

See [CLAUDE.md](./CLAUDE.md) for detailed working guidelines.