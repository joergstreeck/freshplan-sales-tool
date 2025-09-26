# ⚡ Performance Test Pattern - P95 Validation

**Status:** ✅ IMPLEMENTED (PR #110)
**Erstellt:** 26.09.2025
**Referenz:** `/backend/src/test/java/de/freshplan/modules/leads/performance/`

## 🎯 Pattern Overview

Systematisches Performance-Testing mit P95-Metriken für < 200ms Requirement.

## 📊 Performance Test Implementation

### Basis-Pattern mit P95-Messung
```java
@QuarkusTest
@DisplayName("Lead Performance Validation - FP-236")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LeadPerformanceValidationTest {

    private static final long P95_THRESHOLD_MS = 200;
    private static final int WARM_UP_ITERATIONS = 10;
    private static final int MEASURE_ITERATIONS = 100;

    @BeforeEach
    @Transactional
    void setUp() {
        // Test-Daten erstellen
        createTestLeads(100);
    }

    @Test
    @Order(1)
    @DisplayName("Find by territory should meet P95 < 200ms")
    @Transactional
    void testFindByTerritoryPerformance() {
        // Warm-up
        for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
            Lead.find("territory", territoryDE).list();
        }

        // Measure
        List<Long> measurements = new ArrayList<>(MEASURE_ITERATIONS);
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            long start = System.nanoTime();
            List<Lead> leads = Lead.find("territory", territoryDE).list();
            long duration = (System.nanoTime() - start) / 1_000_000; // to ms
            measurements.add(duration);
        }

        // Calculate P95
        Collections.sort(measurements);
        int p95Index = (int) Math.ceil(0.95 * measurements.size()) - 1;
        long p95 = measurements.get(p95Index);

        // Assert
        System.out.println("Find by territory P95: " + p95 + "ms");
        assertTrue(p95 < P95_THRESHOLD_MS,
            String.format("P95 should be < %dms, was: %dms",
                P95_THRESHOLD_MS, p95));
    }
}
```

## 🔍 Test-Szenarien

### 1. Query-Performance Tests
```java
@Test
void testFindByStatusPerformance() {
    long p95 = measureP95(() ->
        Lead.find("status", LeadStatus.ACTIVE).list()
    );
    assertP95(p95, "Find by status");
}

@Test
void testFindByOwnerPerformance() {
    long p95 = measureP95(() ->
        Lead.find("ownerUserId", ownerId).list()
    );
    assertP95(p95, "Find by owner");
}
```

### 2. Complex Query Tests
```java
@Test
void testComplexQueryPerformance() {
    long p95 = measureP95(() ->
        Lead.find("territory = ?1 and status = ?2",
            territoryDE, LeadStatus.ACTIVE).list()
    );
    assertP95(p95, "Complex query");
}
```

### 3. Pagination Tests
```java
@Test
void testPaginationPerformance() {
    long p95 = measureP95(() ->
        Lead.findAll()
            .page(Page.of(0, 20))
            .list()
    );
    assertP95(p95, "Pagination");
}
```

## 📈 Helper-Methoden

### P95 Measurement Helper
```java
private long measureP95(Runnable operation) {
    // Warm-up
    for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
        operation.run();
    }

    // Measure
    List<Long> measurements = new ArrayList<>();
    for (int i = 0; i < MEASURE_ITERATIONS; i++) {
        long start = System.nanoTime();
        operation.run();
        long duration = (System.nanoTime() - start) / 1_000_000;
        measurements.add(duration);
    }

    // Calculate P95
    Collections.sort(measurements);
    int p95Index = (int) Math.ceil(0.95 * measurements.size()) - 1;
    return measurements.get(p95Index);
}

private void assertP95(long p95, String operation) {
    System.out.println(operation + " P95: " + p95 + "ms");
    assertTrue(p95 < P95_THRESHOLD_MS,
        String.format("%s P95 should be < %dms, was: %dms",
            operation, P95_THRESHOLD_MS, p95));
}
```

## 📊 Erreichte Metriken (PR #110)

| Operation | P95 | Threshold | Status |
|-----------|-----|-----------|--------|
| Find by territory | 7ms | 200ms | ✅ |
| Find by status | 2ms | 200ms | ✅ |
| Find by owner | 1ms | 200ms | ✅ |
| Complex query | 2ms | 200ms | ✅ |
| Pagination | 2ms | 200ms | ✅ |

## 🚀 Best Practices

### 1. Warm-up Phase
- Immer 10+ Warm-up-Iterationen vor Messung
- JIT-Compiler Optimierungen abwarten
- Cache aufwärmen

### 2. Statistische Signifikanz
- Mindestens 100 Messungen für P95
- Outliers durch P95 statt Average filtern
- Konsistente Test-Datengröße

### 3. Test-Isolation
- @Transactional für Clean-State
- Eigene Test-Daten pro Test
- @Order für deterministische Reihenfolge

## 🔗 Integration mit CI

### GitHub Actions Konfiguration
```yaml
- name: Run Performance Tests
  run: |
    ./mvnw test -Dtest="*PerformanceValidationTest"
    if [ -f target/performance-results.json ]; then
      echo "::notice::P95 Performance Results:"
      cat target/performance-results.json
    fi
```

### Monitoring Dashboard
- Prometheus Metriken exportieren
- Grafana Dashboard für P95 Trends
- Alerts bei Performance-Degradation

## 📚 Referenzen
- [PR #110 - FP-236 Implementation](https://github.com/joergstreeck/freshplan-sales-tool/pull/110)
- [Performance Requirements](../technical-concept.md#technical-excellence)
- [Foundation Benchmark](../../../performance/phase-1-foundation-benchmark-2025-09-24.md)