# ⚙️ Development Workflow - FreshPlan Entwicklungsprozess

**Erstellt:** 2025-09-17
**Status:** ✅ Aktueller Stand dokumentiert
**Basis:** Analyse realer CI/CD-Pipelines + Scripts + Tools
**Scope:** Kompletter Entwicklungszyklus vom Setup bis Deployment

## 📋 Overview: Enterprise Development Process

### **KERN-TECHNOLOGIEN (Aktueller Stack):**
```yaml
Frontend:
  Framework: React 18 + TypeScript + Vite
  UI: MUI v7.2.0 (Material Design System)
  State: React Query + Zustand
  Testing: Vitest + Playwright
  Build: Vite (ES Modules, HMR)

Backend:
  Framework: Quarkus 3.17.4 + Java 17
  Database: PostgreSQL 15 + Flyway Migrations
  Security: Keycloak OIDC (optional in dev)
  Testing: JUnit 5 + TestContainers
  Build: Maven 3.9+

Infrastructure:
  Containerization: Docker + docker-compose
  CI/CD: GitHub Actions (4 parallel workflows)
  Monitoring: Health checks + Logs
  Scripts: 60+ Bash automation scripts
```

## 🔄 **DEVELOPMENT LIFECYCLE**

### **Phase 1: Setup & Start (5-10 Minuten)**

#### **Automatisierter Dev-Setup:**
```bash
# ✅ One-Command Development Start
./scripts/start-dev.sh

# Was passiert automatisch:
# 1. ✅ Prerequisites Check (Docker, Java 17, Node 20)
# 2. 🗄️  PostgreSQL Container (Port 5432)
# 3. ☕ Quarkus Backend (Port 8080, Hot-Reload)
# 4. ⚛️  React Frontend (Port 5173, HMR)
# 5. 📊 Service Health Checks
```

#### **Manual Setup (falls nötig):**
```bash
# Backend Setup
cd backend/
./mvnw quarkus:dev                    # Hot-reload auf :8080

# Frontend Setup
cd frontend/
npm install && npm run dev            # HMR auf :5173

# Optional: Keycloak für Auth
./scripts/start-keycloak.sh           # Port :8180
```

#### **Service Verification:**
```bash
./scripts/check-services.sh          # Status aller Services
./scripts/health-check.sh             # Deep Health Check
./scripts/test-app.sh                 # Lockerer Funktionstest
```

### **Phase 2: Development (Kontinuierlich)**

#### **Core Development Commands:**
```bash
# Backend Development
cd backend/
./mvnw quarkus:dev                    # Live-Reload Development
./mvnw clean test                     # Unit Tests
./mvnw verify                         # Integration Tests

# Frontend Development
cd frontend/
npm run dev                           # Vite Dev Server (HMR)
npm run test                          # Vitest Unit Tests
npm run test:e2e                      # Playwright E2E Tests
npm run lint                          # ESLint + TypeScript Check
```

#### **Database Workflow:**
```bash
# Migration erstellen
./scripts/create-migration.sh "add_customer_table"

# Migration prüfen
./backend/scripts/check-migrations.sh

# Schema Reset (Development)
./scripts/clean-test-data.sh
```

#### **Code Quality Workflow:**
```bash
# Vollständige Quality Checks
./scripts/quality-check.sh           # Lint + Test + Security

# Schnelle Cleanup (vor commits)
./scripts/quick-cleanup.sh           # Entfernt .DS_Store, temp files

# Code Review Vorbereitung
./scripts/code-review.sh             # Comprehensive analysis
```

### **Phase 3: Testing & QA (Parallel)**

#### **Test-Pyramid Execution:**
```bash
# Unit Tests (schnell, <2s)
npm run test:unit                     # Frontend Units
./mvnw test -Dgroups="unit"          # Backend Units

# Integration Tests (mittel, <30s)
npm run test:integration              # API + Store Tests
./mvnw test -Dgroups="integration"   # DB + Service Tests

# E2E Tests (langsam, <5min)
npm run test:e2e                     # User Journey Tests
./scripts/run-e2e-tests.sh           # Full Browser Tests

# Performance Tests
npm run test:performance             # Bundle + Rendering
./mvnw verify -Pperformance          # Load Testing
```

#### **Quality Gates (Automatisch):**
```yaml
Pre-Commit:
  - ✅ ESLint (max 15 warnings)
  - ✅ TypeScript Type Check
  - ✅ Prettier Format Check
  - ✅ Unit Tests
  - ✅ Quick Cleanup

Pull Request:
  - ✅ All Unit + Integration Tests
  - ✅ Security Scans
  - ✅ Code Coverage Report
  - ✅ Performance Budget Check

Main Branch:
  - ✅ Full Test Suite
  - ✅ E2E Tests
  - ✅ Container Build
  - ✅ Deployment Smoke Tests
```

### **Phase 4: CI/CD Pipeline (Automatisiert)**

#### **GitHub Actions Workflows (4 Parallel):**

**1. CI - Lint & Format Check**
```yaml
Trigger: PRs + Main Branch Push
Jobs:
  - lint-frontend:     ESLint + TypeScript + Prettier
  - lint-backend:      Maven Verify + SpotBugs
  - lint-legacy:       Skipped (Legacy namespace issues)
  - quality-gate:      Combined Success Check

Performance: ~3-5 Minuten parallel
```

**2. Backend CI**
```yaml
Trigger: backend/** file changes
Setup:
  - PostgreSQL 14 Service
  - Java 17 (Temurin)
  - Maven Cache
Tests:
  - Core Tests (@Tag("core"))      # 40 aktive Tests
  - Environment Diagnostics       # DB + Config Validation
  - Hard Schema Reset             # Garantiert sauberer Start

Performance: ~5-10 Minuten
```

**3. CI - Integration Tests**
```yaml
Trigger: PRs + Main Branch Push
Jobs:
  - backend-integration:  Full Maven verify -Pgreen
  - e2e-smoke:           Frontend + Backend + Playwright

Services:
  - PostgreSQL 15
  - Background Quarkus Server
  - Frontend Preview Server

Performance: ~15-25 Minuten
```

**4. Database Growth Check**
```yaml
Trigger: Schedule + Manual
Purpose: Monitor DB size + Migration health
Checks:
  - Migration dependencies
  - Schema size growth
  - Performance degradation
```

### **Phase 5: Deployment & Release**

#### **Release Preparation:**
```bash
# Pre-Release Checks
./scripts/prepare-pr.sh              # Quality Gates
./scripts/reality-check.sh           # Comprehensive Validation

# Release Build
cd frontend/ && npm run build        # Production Bundle
cd backend/ && ./mvnw package        # JAR Build

# Container Build (wenn aktiviert)
docker build -t freshplan-frontend:latest frontend/
docker build -t freshplan-backend:latest backend/
```

#### **Deployment Workflow:**
```bash
# Development Environment
./scripts/start-dev.sh               # Local Development

# CI Environment
QUARKUS_PROFILE=ci ./mvnw test       # CI-spezifische Konfiguration

# Production Environment
QUARKUS_PROFILE=prod ./mvnw package  # Production JAR
```

## 🛠️ **DEVELOPMENT TOOLS & AUTOMATION**

### **Script-Ecosystem (60+ Scripts):**

#### **Core Automation:**
```bash
# Essential Daily Scripts
./scripts/start-dev.sh               # Development Environment
./scripts/stop-dev.sh                # Clean Shutdown
./scripts/quick-cleanup.sh           # Repository Hygiene
./scripts/health-check.sh            # Service Diagnostics

# Development Support
./scripts/create-migration.sh        # Database Migrations
./scripts/diagnose-problems.sh       # Problem Analysis
./scripts/test-app.sh                # Quick Function Test
./scripts/backup-critical-docs.sh    # Documentation Backup
```

#### **Advanced Workflows:**
```bash
# Session Management
./scripts/robust-session-start.sh    # Robust Session Init
./scripts/create-handover.sh         # Session Handover Docs
./scripts/track-session.sh           # Development Tracking

# Migration Management
./scripts/get-next-migration.sh      # Migration Numbering
./scripts/check-migrations.sh        # Migration Validation
./scripts/fix-migration-conflicts.sh # Conflict Resolution

# Quality & Maintenance
./scripts/code-review.sh             # Code Review Preparation
./scripts/update-docs.sh             # Documentation Sync
./scripts/emergency-recovery.sh      # Disaster Recovery
```

### **IDE & Editor Setup:**

#### **Recommended Tools:**
```yaml
Backend Development:
  - IntelliJ IDEA Ultimate (Quarkus Plugin)
  - VS Code (Extension Pack for Java)
  - Eclipse with MicroProfile

Frontend Development:
  - VS Code (React + TypeScript Extensions)
  - WebStorm
  - Chrome DevTools

Database:
  - DBeaver (PostgreSQL Client)
  - pgAdmin 4
  - DataGrip
```

#### **VS Code Extensions (Empfohlen):**
```json
{
  "recommendations": [
    "vscjava.vscode-java-pack",
    "redhat.vscode-quarkus",
    "bradlc.vscode-tailwindcss",
    "ms-vscode.vscode-typescript-next",
    "esbenp.prettier-vscode",
    "dbaeumer.vscode-eslint",
    "ms-playwright.playwright"
  ]
}
```

### **Environment Configuration:**

#### **Development Environment:**
```bash
# Frontend Environment
VITE_API_URL=http://localhost:8080
VITE_KEYCLOAK_URL=http://localhost:8180
NODE_ENV=development

# Backend Environment
QUARKUS_PROFILE=dev
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/freshplan
QUARKUS_OIDC_ENABLED=false          # Auth disabled in dev
```

#### **CI Environment:**
```bash
# Frontend CI
VITE_API_URL=http://localhost:8080
CI=true
NODE_ENV=test

# Backend CI
QUARKUS_PROFILE=ci
QUARKUS_DATASOURCE_DEVSERVICES_ENABLED=false
QUARKUS_FLYWAY_MIGRATE_AT_START=true
```

## 📊 **PERFORMANCE & METRICS**

### **Development Performance:**
```yaml
Hot Reload Speed:
  - Frontend (Vite HMR):     <100ms
  - Backend (Quarkus):       <2s
  - Full Restart:            <10s

Test Execution Speed:
  - Frontend Unit Tests:     ~5s (1.024 tests)
  - Backend Core Tests:      ~15s (40 active tests)
  - Integration Tests:       ~2-5min
  - Full E2E Suite:          ~10-15min

Build Performance:
  - Frontend Production:     ~30s
  - Backend JAR:            ~60s
  - Docker Images:          ~2-3min
```

### **CI/CD Performance:**
```yaml
Pipeline Speed:
  - Lint & Format:          ~3-5min
  - Backend Tests:          ~5-10min
  - Integration Tests:      ~15-25min
  - Full Pipeline:          ~20-30min parallel

Resource Usage:
  - GitHub Actions Minutes: ~100-150/month
  - Docker Registry:        ~2GB images
  - Artifact Storage:       ~500MB/month
```

## 🔒 **SECURITY & BEST PRACTICES**

### **Security Workflow:**
```yaml
Development Security:
  - ✅ OIDC disabled in dev (faster iteration)
  - ✅ Secrets via environment variables
  - ✅ No hardcoded credentials
  - ✅ CORS configured per environment

CI/CD Security:
  - ✅ GitHub Secrets for sensitive data
  - ✅ Dependabot security updates
  - ✅ Container vulnerability scanning
  - ✅ No secrets in logs/artifacts
```

### **Code Quality Standards:**
```yaml
Frontend Standards:
  - ✅ ESLint with React/TypeScript rules
  - ✅ Prettier for consistent formatting
  - ✅ TypeScript strict mode
  - ✅ Component-first architecture

Backend Standards:
  - ✅ SpotBugs static analysis
  - ✅ JaCoCo code coverage
  - ✅ Quarkus best practices
  - ✅ Domain-driven design patterns

Testing Standards:
  - ✅ Test Pyramid architecture
  - ✅ TestDataBuilder pattern (SEED-free)
  - ✅ Container-based integration tests
  - ✅ Visual regression testing
```

## 📚 **TROUBLESHOOTING & COMMON ISSUES**

### **Development Issues:**

#### **Backend Won't Start:**
```bash
# Diagnose
./scripts/diagnose-problems.sh

# Common fixes
./scripts/clean-test-data.sh         # DB conflicts
./backend/scripts/flyway-repair.sh  # Migration issues
pkill -f quarkus                     # Kill zombie processes
```

#### **Frontend Issues:**
```bash
# Clear caches
rm -rf frontend/node_modules/.vite
npm install

# Port conflicts
lsof -Pi :5173 -sTCP:LISTEN -t | xargs kill

# TypeScript errors
npm run type-check
```

#### **Test Failures:**
```bash
# Backend test debugging
./mvnw test -Dtest="A00_EnvDiagTest"    # Environment check
./mvnw test -X                          # Debug output

# Frontend test debugging
npm run test -- --reporter=verbose
npm run test:ui                         # Vitest UI
```

### **CI/CD Issues:**

#### **Pipeline Failures:**
```bash
# Local CI simulation
./scripts/simulate-ci-locally.sh

# Debug specific workflows
gh run list --limit 5
gh run view <RUN_ID> --log-failed

# Common CI fixes
./scripts/debug-ci-local.sh
```

## 🚀 **OPTIMIZATION & SCALING**

### **Performance Optimizations:**
```yaml
Development:
  - ✅ Vite HMR for instant feedback
  - ✅ Maven cache in CI
  - ✅ Incremental TypeScript compilation
  - ✅ Test parallelization

Production:
  - ✅ Vite bundle optimization
  - ✅ Quarkus native compilation (optional)
  - ✅ PostgreSQL connection pooling
  - ✅ CDN-ready asset structure
```

### **Future Improvements (Noted for Planning):**
```yaml
Identified Opportunities:
  - Test structure modernization (>2.000 tests need organization)
  - CQRS migration completion (hybrid → full CQRS)
  - Container orchestration (Docker Compose → Kubernetes)
  - Monitoring & Observability (Metrics + Tracing)
```

## 📋 **TEAM WORKFLOW PATTERNS**

### **Daily Development:**
```bash
# Morning Setup (1-2 Minuten)
./scripts/start-dev.sh
./scripts/health-check.sh

# Development Loop
# 1. Code changes → Hot reload
# 2. Unit tests → npm run test:unit
# 3. Integration check → Quick API test
# 4. Commit → ./scripts/quick-cleanup.sh

# End of Day
./scripts/stop-dev.sh
./scripts/create-handover.sh         # Wenn Session-Übergabe
```

### **Pull Request Workflow:**
```bash
# PR Preparation
./scripts/prepare-pr.sh              # Quality gates
./scripts/code-review.sh             # Self-review

# CI automatically runs:
# - Lint & Format checks
# - Unit + Integration tests
# - Security scans
# - Performance checks

# Post-merge
# - Automatic deployment to staging
# - Smoke tests
# - Documentation updates
```

---

**📋 Workflow basiert auf:** Analyse realer CI/CD-Pipelines + 60+ Scripts + Team-Patterns
**📅 Letzte Aktualisierung:** 2025-09-17
**👨‍💻 Maintainer:** Development Team

**🎯 Dieser Workflow unterstützt Enterprise-Grade Development mit >2.000 Tests und parallelen CI-Pipelines!**