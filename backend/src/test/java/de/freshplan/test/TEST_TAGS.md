# Test-Tags Dokumentation

**Status:** ✅ Vollständig implementiert (Phase 2.4, 2025-10-01)
**Version:** 1.0

---

## Übersicht

Alle Tests im FreshPlan Backend verwenden semantische JUnit 5 `@Tag` Annotationen zur Kategorisierung. Dies ermöglicht selektive Test-Ausführung für schnelles lokales Feedback und optimierte CI-Pipelines.

---

## Tag-Schema

### `@Tag("unit")` - Unit Tests (44 Tests, ~5-10s)

**Charakteristik:**
- ✅ Keine `@QuarkusTest` Annotation
- ✅ Keine Datenbank (kein `@TestTransaction`, `@Transactional`)
- ✅ Oft mit `@ExtendWith(MockitoExtension.class)`
- ✅ Pure Logic Tests ohne externe Dependencies

**Beispiele:**
```java
@Tag("unit")
class OpportunityMapperTest {
    // DTO-Mapping, keine DB
}

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class LeadNormalizationServiceTest {
    @Mock private LeadRepository leadRepository;
    @InjectMocks private LeadNormalizationService service;
    // Mockito-basierte Unit Tests
}
```

**Verwendung:**
```bash
./mvnw test -P unit
```

**Laufzeit:** ~5-10 Sekunden (sehr schnell)

---

### `@Tag("integration")` - Integration Tests (54 Tests, ~5-8 min)

**Charakteristik:**
- ✅ `@QuarkusTest` vorhanden
- ✅ `@TestTransaction` oder `@Transactional`
- ✅ Service/Repository-Layer Tests
- ❌ KEINE REST-API Tests (kein RestAssured)

**Beispiele:**
```java
@QuarkusTest
@Tag("integration")
class CustomerServiceIntegrationTest {
    @Inject CustomerService customerService;
    @Inject CustomerRepository customerRepository;

    @Test
    @TestTransaction
    void testCreateCustomer() {
        // Service + Repository Integration
    }
}
```

**Verwendung:**
```bash
./mvnw test -P integration
```

**Laufzeit:** ~5-8 Minuten (mittlere Geschwindigkeit)

---

### `@Tag("e2e")` - End-to-End Tests (17 Tests, ~2-3 min)

**Charakteristik:**
- ✅ `@QuarkusTest` vorhanden
- ✅ `@TestHTTPEndpoint` oder RestAssured `given().when().then()`
- ✅ Voller Request-Response-Lifecycle
- ✅ Security + Validation + Persistence

**Beispiele:**
```java
@QuarkusTest
@Tag("e2e")
@TestHTTPEndpoint(UserResource.class)
class UserResourceIT {
    @Test
    @TestSecurity(user = "admin", roles = "admin")
    void testCreateUser() {
        given()
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post()
        .then()
            .statusCode(201);
    }
}
```

**Verwendung:**
```bash
./mvnw test -P e2e
```

**Laufzeit:** ~2-3 Minuten (schnell, wenige Tests)

---

### `@Tag("performance")` - Performance Tests (3 Tests)

**Charakteristik:**
- ✅ Last-/Performance-Messungen
- ✅ Große Datenmengen
- ✅ Benchmarks

**Beispiele:**
```java
@QuarkusTest
@Tag("performance")
class CustomerSearchPerformanceTest {
    @Test
    void testSearchWith10000Customers() {
        // Performance-Messung
    }
}
```

**Verwendung:**
```bash
./mvnw test -P performance
```

**Wann ausführen:** Wöchentlich/Monatlich, NICHT in jedem CI-Run

---

### `@Tag("quarantine")` - Flaky Tests (2 Tests)

**Charakteristik:**
- ⚠️ Instabile Tests
- ⚠️ Nur zur Analyse

**Verwendung:**
```bash
# Alle AUSSER quarantine (Standard für Release)
./mvnw test -P stable-tests
```

---

## Maven Profile - Verwendung

### Lokale Entwicklung

```bash
# 1. Schnelles Feedback (nur Unit-Tests, ~5-10s)
./mvnw test -P unit

# 2. Service-Layer validieren (~5-8 min)
./mvnw test -P integration

# 3. API-Tests (~2-3 min)
./mvnw test -P e2e

# 4. Alles außer flaky Tests (Standard für Release)
./mvnw test -P stable-tests
```

### CI-Pipeline (Staged Execution)

```yaml
# .github/workflows/tests.yml
jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - run: ./mvnw test -P unit

  integration-tests:
    needs: unit-tests
    runs-on: ubuntu-latest
    steps:
      - run: ./mvnw test -P integration

  e2e-tests:
    needs: integration-tests
    runs-on: ubuntu-latest
    steps:
      - run: ./mvnw test -P e2e
```

**Vorteil:** Schnelles Fail-Fast (Unit-Tests scheitern in 10s statt nach 10 min)

---

## Entscheidungsbaum: Welches Tag verwenden?

```
Hat der Test @QuarkusTest?
│
├─ NEIN → @Tag("unit")
│   └─ Beispiele: Mapper, DTO-Validation, Mockito-Tests
│
└─ JA
    │
    ├─ Verwendet RestAssured / @TestHTTPEndpoint?
    │   └─ JA → @Tag("e2e")
    │       └─ Beispiele: UserResourceIT, LeadResourceTest
    │
    └─ NEIN
        │
        ├─ Performance/Load-Test?
        │   └─ JA → @Tag("performance")
        │       └─ Beispiele: CustomerSearchPerformanceTest
        │
        └─ NEIN → @Tag("integration")
            └─ Beispiele: CustomerServiceIntegrationTest, AuditRepositoryTest
```

---

## Migration veralteter Tags

### Alte Tags (NICHT mehr verwenden!)

- ❌ `@Tag("migrate")` → Veraltet, wurde in semantic tags migriert
- ❌ `@Tag("core")` → Veraltet, wurde in semantic tags migriert

### Automatische Migration

Für neue Tests immer das korrekte semantische Tag verwenden!

**Scripts:**
```bash
# Veraltete Tags migrieren
./scripts/migrate-test-tags.sh --from migrate --execute

# Untagged Tests automatisch taggen
./scripts/tag-untagged-tests.sh --execute
```

---

## Statistik (Stand 2025-10-01)

| Tag | Anzahl | Prozent | Laufzeit |
|-----|--------|---------|----------|
| `@Tag("unit")` | 48 | 41.4% | ~5-10s |
| `@Tag("integration")` | 61 | 52.6% | ~5-8 min |
| `@Tag("e2e")` | 4 | 3.4% | ~2-3 min |
| `@Tag("performance")` | 3 | 2.6% | variabel |
| `@Tag("quarantine")` | 0 | 0% | - |
| **GESAMT** | **116** | **100%** | ~10-11 min |

**Anmerkung:** Einige Test-Dateien enthalten mehrere Test-Klassen (z.B. `OpportunityMapperTest` mit `ToResponseTests`, `FromEntityTests`, etc.), daher kann die Anzahl der Tags höher sein als die Anzahl der Dateien.

---

## Best Practices

### 1. **Immer das richtige Tag verwenden**
```java
// ✅ RICHTIG
@Tag("unit")
class CustomerMapperTest { }

// ❌ FALSCH (fehlendes Tag)
class CustomerMapperTest { }
```

### 2. **Ein Tag pro Test-Klasse**
```java
// ✅ RICHTIG
@Tag("integration")
class CustomerServiceTest { }

// ❌ FALSCH (mehrere Tags verwirren)
@Tag("integration")
@Tag("unit")
class CustomerServiceTest { }
```

### 3. **Tag vor der Klassendeklaration**
```java
// ✅ RICHTIG
@QuarkusTest
@Tag("integration")
class MyTest { }

// ❌ FALSCH (Tag nach @QuarkusTest ist unübersichtlich)
@Tag("integration")
@QuarkusTest
class MyTest { }
```

### 4. **Performance-Tests sparsam verwenden**
```java
// ✅ Nur wenn wirklich Performance gemessen wird
@Tag("performance")
void testWith10000Records() { }

// ❌ Normale Tests nicht als performance taggen
```

---

## Troubleshooting

### Problem: Test wird nicht ausgeführt mit `-P unit`

**Lösung:** Prüfe ob Test `@Tag("unit")` hat:
```bash
grep -n "@Tag" src/test/java/path/to/MyTest.java
```

### Problem: Test-Suite zu langsam

**Lösung:** Prüfe ob Tests richtig getaggt sind:
```bash
# Finde @QuarkusTest mit @Tag("unit") (sollte integration sein!)
grep -l "@QuarkusTest" src/test/java/**/*Test.java | xargs grep -l '@Tag("unit")'
```

### Problem: CI-Pipeline schlägt fehl bei Tag-basierten Tests

**Lösung:** Stelle sicher dass ALLE Tests ein Tag haben:
```bash
# Finde Tests ohne @Tag
find src/test/java -name "*Test.java" | xargs grep -L "@Tag"
```

---

## Referenzen

- **JUnit 5 Tags:** https://junit.org/junit5/docs/current/user-guide/#writing-tests-tagging-and-filtering
- **Maven Surefire Groups:** https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html
- **FreshPlan Test Strategy:** `docs/planung/TEST_STRATEGY_IMPROVEMENT.md`

---

**Autor:** FreshPlan Team
**Letzte Aktualisierung:** 2025-10-01 (Phase 2.4)
**Review:** Erfolgreich implementiert und getestet
