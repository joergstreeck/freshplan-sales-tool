---
Navigation: [⬅️ Previous](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/04-INTEGRATION/README.md) | [🏠 Home](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md) | [➡️ Next](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/README.md)
Parent: [📁 FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)
Related: [🔗 Master Plan V5](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md) | [🔗 CLAUDE.md](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md)
---

# 🧪 FC-005 TESTING - ÜBERSICHT

**Modul:** FC-005 Customer Management  
**Bereich:** Testing Strategy  
**Coverage-Ziel:** ≥ 80% für neue Features  

## 📋 Test-Dokumentation

### [01-unit-tests.md](./01-unit-tests.md)
**Unit Tests für Business Logic & Components**
- Backend Unit Tests (Services, Validation)
- Frontend Unit Tests (Store, Components)
- Test Patterns & Best Practices
- Mocking Strategies

### [02-integration-tests.md](./02-integration-tests.md)
**API & Database Integration**
- REST API Tests mit RestAssured
- Database Transaction Tests
- Event System Tests
- Security Integration Tests

### [03-e2e-tests.md](./03-e2e-tests.md)
**End-to-End User Journeys**
- Customer Onboarding Flow
- Field Management Scenarios
- Draft & Save Workflows
- Playwright Test Setup

### [04-performance-tests.md](./04-performance-tests.md)
**Performance & Load Testing**
- k6 Load Test Scenarios
- API Performance Benchmarks
- Database Query Performance
- Frontend Performance Metrics

## 🎯 Test-Pyramide

```
         /\
        /E2E\        10% - Critical User Journeys
       /------\
      /  Integ  \    30% - API & DB Integration
     /------------\
    /   Unit Tests  \ 60% - Business Logic & Components
   /------------------\
```

## 📊 Coverage-Ziele

| Layer | Minimum | Ziel | Fokus |
|-------|---------|------|-------|
| Unit | 80% | 90% | Business Logic, Validation |
| Integration | 70% | 80% | API Contracts, DB Operations |
| E2E | - | 100% | Critical Paths |
| Performance | - | 100% | Load Scenarios |

## 🚀 Quick Commands

```bash
# Backend Tests
cd backend
./mvnw test                    # Unit Tests
./mvnw verify                  # Unit + Integration
./mvnw test -Dtest=*IT        # Only Integration Tests

# Frontend Tests  
cd frontend
npm test                       # Unit Tests (Watch Mode)
npm run test:ci               # CI Mode
npm run test:coverage         # Coverage Report

# E2E Tests
npm run test:e2e              # Playwright Tests
npm run test:e2e:ui           # With UI

# Performance Tests
k6 run tests/performance/customer-api.js
```

## 🔧 Test-Umgebungen

| Umgebung | Zweck | Datenbank | Auth |
|----------|-------|-----------|------|
| Unit | Isolierte Tests | In-Memory | Mocked |
| Integration | API Tests | Test-DB | Test-Keycloak |
| E2E | User Flows | Test-DB | Test-Keycloak |
| Performance | Load Tests | Dedicated | Disabled |

## 📚 Weiterführende Dokumentation

- [Test Data Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/02-integration-tests.md#test-data-management)
- [CI/CD Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/04-performance-tests.md#cicd-integration)
- [Debugging Guide](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md#automatisches-debugging)