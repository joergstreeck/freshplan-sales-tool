# Testing Team Workspace

## Quick Start
```bash
# Backend Tests
cd backend
./mvnw -Pgreen clean verify

# Frontend Tests
cd frontend
npm test

# E2E Tests
npx playwright test
```

## Aktueller Sprint – T-4 Security & Coverage
- [ ] Security/OIDC Tests aktivieren
- [ ] E2E Test Suite erweitern
- [ ] Performance Baseline etablieren
- [ ] CI Pipeline optimieren

## Test Coverage Status

| Component | Unit | Integration | E2E | Target |
|-----------|------|-------------|-----|--------|
| Backend | 85% | 70% | 🚧 | 90% |
| Frontend | 🚧 | 📋 | 📋 | 80% |
| API | ✅ | ✅ | 🚧 | 100% |

## CI/CD Pipeline
```yaml
Current: 55 tests ✅
Target: 100+ tests
Build Time: ~3 min
Target: < 2 min
```

## Test Execution
```bash
# Isolierter Test
./mvnw test -Dtest=UserServiceTest#testGetAllUsers

# Mit Debug Output
./mvnw test -Dtest.debug=true

# Parallel Tests
./mvnw test -Dparallel=methods -DthreadCount=4

# Coverage Report
./mvnw verify jacoco:report
```

## Performance Testing
```bash
# k6 Load Tests
k6 run scripts/load-test.js

# JMeter
jmeter -n -t test-plan.jmx
```

## Team-Rituale
- Daily Sync: 09:00 CET (10 min)
- Test Review: Fr 11:00 CET
- CI Status: Slack #ci-alerts

## Debug-Strategie
Bei Problemen: "Strategie der kleinen Schritte"
- Siehe `docs/CI_DEBUGGING_STRATEGY.md`
- Test isolieren → minimieren → debuggen

## Kontakt
- Lead: Testing Team
- Slack: #freshplan-testing
- Dashboard: [CI/CD Status](link)