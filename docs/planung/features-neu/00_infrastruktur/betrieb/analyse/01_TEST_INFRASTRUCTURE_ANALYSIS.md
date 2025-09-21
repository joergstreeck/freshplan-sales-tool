# Test Debt Recovery - Infrastructure Plan

**📊 Plan Status:** 🔴 Critical - Test Debt blockiert Entwicklung
**🎯 Owner:** QA Team + Development Team
**⏱️ Timeline:** Q4 2025 → Q1 2026
**🔧 Effort:** L

## 🎯 Executive Summary (für Claude)

**Mission:** Systematische Sanierung der Test-Infrastruktur von 32% auf 80%+ funktionsfähige Tests
**Problem:** 97 Tests in "migrate" Status, 32 Tests "quarantined", nur 40 "core" Tests stabil - blockiert CI/CD
**Solution:** 3-Phasen Test-Recovery mit TestDataBuilder-Migration und SEED-Elimination
**Timeline:** 12 Wochen strukturierte Test-Sanierung
**Impact:** Von 23% auf 80%+ Test-Coverage, CI/CD wieder funktionsfähig

**Quick Context:** Aktuell sind 129 von 169 Tests nicht funktionsfähig aufgrund SEED-Dependencies und Test-Isolation-Problemen. TestDataBuilder-Pattern ist bereits implementiert, muss aber systematisch ausgerollt werden.

## 📋 Context & Dependencies

### Current State:
- **40 Tests "core"** - Stabil, in CI aktiv (23% der Test-Suite)
- **97 Tests "migrate"** - In Migration, CI deaktiviert (57% broken)
- **32 Tests "quarantine"** - Gefährlich, komplett deaktiviert (19% toxic)
- TestDataBuilder-Pattern vorhanden aber nicht vollständig migriert
- SEED-Dependencies verursachen Test-Isolation-Probleme

### Target State:
- **135+ Tests "core"** - Stabil und in CI aktiv (80%+ Coverage)
- **<20 Tests "migrate"** - Nur komplexe Edge-Cases
- **0 Tests "quarantine"** - Alle Tests entweder funktional oder entfernt
- 100% TestDataBuilder-Pattern (SEED-frei)
- Robuste Test-Isolation ohne Side-Effects

### Dependencies:
→ [Testing Guide Standards](../grundlagen/TESTING_GUIDE.md) - TestDataBuilder-Pattern
→ [Development Workflow](../grundlagen/DEVELOPMENT_WORKFLOW.md) - CI Integration
→ [Performance Standards](../grundlagen/PERFORMANCE_STANDARDS.md) - Test Performance Benchmarks

## 🛠️ Implementation Phases

### Phase 1: Stabilize Core Tests (Woche 1-2)
**Goal:** 40 "core" Tests auf 100% Stabilität bringen

**Actions:**
- [ ] Audit aller 40 "core" Tests auf Flakiness und Performance
- [ ] Fix Test-Isolation-Issues in bestehenden Core-Tests
- [ ] Implement Parallel Test Execution für Core-Tests
- [ ] Setup Test-Performance-Monitoring (Target: <30s Gesamt-Laufzeit)
- [ ] Create Test-Kategorisierung-Automation (Tag-Validation)

**Code Changes:**
```java
// Test Performance Optimization
@TestMethodOrder(OrderAnnotation.class)
@Execution(ExecutionMode.CONCURRENT)
class OptimizedCoreTest {

    @TestDataBuilder
    private CustomerTestDataBuilder customerBuilder;

    @Test
    @Order(1)
    @Tag("core")
    void fastIsolatedTest() {
        // SEED-freier Test mit Builder-Pattern
        Customer customer = customerBuilder
            .withUniqueEmail()
            .withValidData()
            .build();
        // Test logic...
    }
}
```

**Success Criteria:**
- Alle 40 Core-Tests laufen parallel in <30s
- Zero Flakiness (100 consecutive runs pass)
- Test-Isolation garantiert (keine Side-Effects zwischen Tests)
- CI Pipeline läuft stabil mit Core-Tests

**Next:** → [Phase 2](#phase-2)

### Phase 2: Migrate Test Recovery (Woche 3-8)
**Goal:** 50+ Tests von "migrate" zu "core" Status migrieren

**Actions:**
- [ ] Priorisierung der 97 "migrate" Tests nach Business-Value
- [ ] TestDataBuilder-Migration für 5-10 Tests pro Woche
- [ ] SEED-Dependency-Elimination systematisch durchführen
- [ ] Create Automated Migration-Scripts für Common-Patterns
- [ ] Implement Test-Data-Cleanup-Automation

**Migration Strategy:**
```java
// Von SEED-abhängig zu Builder-Pattern
// VORHER (migrate):
@Test
void oldSeedDependentTest() {
    // Abhängig von SEED-Daten, nicht isoliert
    Customer customer = customerRepository.findById(SEED_CUSTOMER_ID);
    // Test bricht wenn SEED-Daten fehlen...
}

// NACHHER (core):
@Test
@Tag("core")
void newBuilderBasedTest() {
    // Isoliert, SEED-frei, parallel-safe
    Customer customer = customerTestDataBuilder
        .withEmailDomain("testdomain.com")
        .withRandomName()
        .build();
    // Test ist vollständig isoliert
}
```

**Success Criteria:**
- 50+ Tests von "migrate" → "core" Status
- Test-Coverage steigt von 23% auf 60%+
- Alle migrierten Tests laufen SEED-frei
- Migration-Scripts reduzieren manuelle Arbeit um 70%

**Next:** → [Phase 3](#phase-3)

### Phase 3: Quarantine Resolution (Woche 9-12)
**Goal:** Alle 32 "quarantine" Tests analysieren und entscheiden

**Actions:**
- [ ] Analyse aller 32 Quarantine-Tests auf Rescue-Potential
- [ ] Rescue 15-20 Tests mit kritischer Business-Logic
- [ ] Delete 10-15 Tests die obsolet oder duplicate sind
- [ ] Create Replacement-Tests für deleted critical functionality
- [ ] Final Test-Suite-Optimization und Cleanup

**Decision Matrix:**
```yaml
Quarantine Test Triage:
  Rescue-Kriterien:
    - Testet kritische Business-Logic
    - Nicht duplicate zu anderen Tests
    - Realistisch migrierbar (<2h Aufwand)

  Delete-Kriterien:
    - Obsolete Funktionalität
    - Duplicate Coverage
    - Unrealistische Migration (>4h Aufwand)
    - Flaky beyond repair
```

**Success Criteria:**
- 15-20 Tests rescued und zu "core" migriert
- 10-15 obsolete Tests entfernt
- 0 Tests im "quarantine" Status
- Final Test-Suite-Coverage >80%

**Next:** → [Phase 4](#phase-4)

### Phase 4: Test Infrastructure Hardening (Woche 13-14)
**Goal:** Test-Infrastructure für langfristige Stabilität optimieren

**Actions:**
- [ ] Implement Automated Test-Health-Monitoring
- [ ] Create Test-Performance-Regression-Prevention
- [ ] Setup Test-Flakiness-Detection und Auto-Quarantine
- [ ] Document Test-Writing-Guidelines und Best-Practices
- [ ] Train Team auf TestDataBuilder-Pattern und Test-Isolation

**Success Criteria:**
- Test-Suite läuft in <60s mit 135+ Tests
- Automated Detection von Flaky-Tests
- Team kann neue Tests SEED-frei schreiben
- Test-Infrastructure ist self-healing bei Common-Issues

## ✅ Success Metrics

**Quantitative:**
- **Test-Coverage:** 80%+ (currently 23%)
- **Test-Execution-Time:** <60s für Full-Suite (currently >5min for core only)
- **Test-Success-Rate:** >98% in CI (currently 67% including migrate)
- **Flakiness-Rate:** <1% (currently ~15%)
- **Test-Migration-Rate:** 80+ Tests migrate→core (currently 0)

**Qualitative:**
- Entwickler können Tests parallel entwickeln ohne Conflicts
- CI/CD Pipeline ist zuverlässig und vertrauenswürdig
- Neue Features können mit stabilen Tests entwickelt werden
- QA-Prozess ist nicht mehr durch Test-Infrastructure blockiert

**Timeline:**
- Phase 1: Woche 1-2 (Core Stabilization)
- Phase 2: Woche 3-8 (Test Migration)
- Phase 3: Woche 9-12 (Quarantine Resolution)
- Phase 4: Woche 13-14 (Infrastructure Hardening)

## 🔗 Related Documentation

**Foundation Knowledge:**
- **Testing Standards:** → [TESTING_GUIDE.md](../grundlagen/TESTING_GUIDE.md)
- **Development Workflow:** → [DEVELOPMENT_WORKFLOW.md](../grundlagen/DEVELOPMENT_WORKFLOW.md)
- **Performance Guidelines:** → [PERFORMANCE_STANDARDS.md](../grundlagen/PERFORMANCE_STANDARDS.md)

**Implementation Details:**
- **Code Location:** `backend/src/test/java/com/freshplan/`
- **Test Builders:** `*TestDataBuilder.java` classes
- **CI Configuration:** `.github/workflows/backend-ci.yml`

**Related Plans:**
- **Dependencies:** → [Performance Optimization Plan](./PERFORMANCE_OPTIMIZATION_PLAN.md) (parallel)
- **Follow-ups:** → [E2E Test Automation](./E2E_TEST_AUTOMATION_PLAN.md) (planned)
- **Alternatives:** Evaluated: Complete Test Rewrite (too risky), External Test Service (too expensive)

## 🤖 Claude Handover Section

**Für nächsten Claude:**

**Aktueller Stand:**
Test Debt Recovery Plan erstellt nach PLANUNGSMETHODIK. TestDataBuilder-Pattern ist implementiert, aber nur 40 von 169 Tests sind stabil. 97 Tests in Migration, 32 in Quarantine.

**Nächster konkreter Schritt:**
Phase 1 starten - Audit der 40 "core" Tests auf Flakiness. Beginne mit Test-Performance-Monitoring und paralleler Execution der bestehenden Core-Tests.

**Wichtige Dateien für Context:**
- `backend/src/test/java/com/freshplan/testdata/` - TestDataBuilder-Implementierungen
- `backend/src/test/java/com/freshplan/` - Aktuelle Test-Suite mit Tag-Kategorisierung
- `docs/planung/grundlagen/TESTING_GUIDE.md` - Test-Standards und Builder-Pattern-Details

**Offene Entscheidungen:**
- Welche der 97 "migrate" Tests haben höchste Business-Value-Priorität?
- Soll Test-Parallelisierung auf Method- oder Class-Level implementiert werden?
- Wie aggressiv sollen wir bei Quarantine-Test-Deletion sein?

**Kontext-Links:**
- **Grundlagen:** → [Testing Guide mit aktueller Tag-Kategorisierung](../grundlagen/TESTING_GUIDE.md)
- **Dependencies:** → [Development Workflow für CI Integration](../grundlagen/DEVELOPMENT_WORKFLOW.md)