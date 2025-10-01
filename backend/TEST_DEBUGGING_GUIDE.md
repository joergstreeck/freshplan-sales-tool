# Backend Testing Guide

**Version:** 3.0 (Sprint 2.1.4) | **Ziel:** Schnell (<20 Min), stabil, vorhersehbar

---

## 🎯 Test-Strategie: Wann welcher Test-Typ?

```
Braucht mein Test echte...
├─ REST API Endpoints? → @QuarkusTest (Integration)
├─ Datenbank + Transaktionen? → @QuarkusTest + @TestTransaction
├─ Security/RBAC/Events? → @QuarkusTest + @TestProfile
└─ Nur Service-Logik? → Mockito Unit Test (KEIN @QuarkusTest!)
```

**Performance-Impact:**
- **@QuarkusTest:** 15-20s Boot-Zeit → sparsam nutzen!
- **Mock-basiert:** <1s → Standard für Service-Logik

**Aktuell:** 153 @QuarkusTest = ~20-28 Min CI-Zeit
**Ziel:** 70% Mock / 30% @QuarkusTest für neue Tests

---

## 📁 Ablage-Strategie

```
backend/src/test/java/de/freshplan/
├─ api/              # REST Resource-Tests (@QuarkusTest, leichtgewichtig)
├─ domain/           # Service Unit-Tests (Mockito, KEINE DB)
├─ integration/      # CQRS/Stack-Tests (@QuarkusTest, selektiv)
├─ infrastructure/   # Security/RBAC/Caching
├─ modules/          # Modul-Tests (z.B. Leads)
├─ greenpath/        # Schnelle Smoke-Tests (Happy Path)
├─ test/             # A00_/ZZZ_-Tests, Profiles, Builders
└─ testsupport/      # TestIds, TestTx, Fixtures
```

**Namenskonventionen:**
- `A00_EnvDiagTest` → Gatekeeper (zuerst laufen)
- `ZZZ_FinalVerificationTest` → Abschluss-Validierung

---

## 📋 Templates: Quick Copy & Paste

### Unit Test (Mock-basiert) - Standard für Services

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Validation Service")
class CustomerValidationServiceTest {
    @Mock CustomerRepository repository;
    @InjectMocks CustomerValidationService service;

    @Test
    void shouldRejectDuplicateCustomerNumber() {
        // Given
        String number = TestIds.uniqueCustomerNumber(); // ✅ Eindeutige ID!
        when(repository.findByCustomerNumber(number))
            .thenReturn(Optional.of(new Customer()));

        // When
        var result = service.validateCustomerNumber(number);

        // Then
        assertThat(result.isValid()).isFalse();
        verify(repository).findByCustomerNumber(number);
    }
}
```

### Integration Test (@QuarkusTest) - Nur wenn DB/Events nötig

```java
@QuarkusTest
@TestTransaction // ✅ Auto-Rollback nach jedem Test
@DisplayName("Customer Service Integration")
class CustomerServiceIntegrationTest {
    @Inject CustomerService service;
    @Inject CustomerRepository repository;

    @Test
    void shouldPersistCustomerWithAuditTrail() {
        // Given - Eindeutige Test-Daten!
        var request = CustomerCreateRequest.builder()
            .customerNumber(TestIds.uniqueCustomerNumber())
            .companyName("Test AG " + UUID.randomUUID())
            .build();

        // When
        var response = service.createCustomer(request);

        // Then - DB + Events + Audit
        assertThat(repository.findById(response.id())).isNotNull();
    }
}
```

---

## 🔧 Kritische Patterns

### 1. Eindeutige Test-IDs (PFLICHT!)

```java
// ❌ NIEMALS statische IDs
createCustomer("TEST-001"); // Race Conditions, Duplikate!

// ✅ IMMER eindeutige IDs
createCustomer(TestIds.uniqueCustomerNumber());
createCustomer("CUST-" + UUID.randomUUID());
```

**TestIds Utility** (`testsupport/TestIds.java`):
```java
public class TestIds {
    private static final AtomicInteger counter = new AtomicInteger(1000);

    public static String uniqueCustomerNumber() {
        return "TEST-CUST-" + counter.incrementAndGet() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
```

### 2. Commit-Grenzen bei Transaktionen

```java
// ❌ Problem: Service liest in neuer TX, sieht Daten nicht
@Test
void testRead() {
    em.persist(customer); // Noch nicht committed!
    var result = service.findById(customer.getId()); // Leere DB!
}

// ✅ Lösung: TestTx.committed() wrapper
@Test
void testRead() {
    var id = TestTx.committed(() -> {
        em.persist(customer);
        em.flush();
        return customer.getId();
    });
    var result = service.findById(id); // Findet Daten!
}
```

**TestTx Utility** (`testsupport/TestTx.java`):
```java
@ApplicationScoped
public class TestTx {
    @Inject UserTransaction tx;

    public <T> T committed(Callable<T> action) throws Exception {
        tx.begin();
        try {
            T result = action.call();
            tx.commit();
            return result;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
}
```

### 3. Relative Assertions (Phase 5C Standard)

```java
// ❌ Absolut - schlägt fehl bei Pollution
@Test
void testFindActive() {
    createCustomer();
    assertThat(repository.findAllActive()).hasSize(1);
}

// ✅ Relativ - robust
@Test
void testFindActive() {
    long before = repository.findAllActive().size();
    createCustomer();
    assertThat(repository.findAllActive()).hasSize((int)(before + 1));
}
```

### 4. Event-Listener deaktivieren (CI)

**In `application.properties` (test/ci):**
```properties
# Listener/NOTIFY blockieren Test-Suite → deaktivieren
quarkus.arc.selected-alternatives=de.freshplan.infrastructure.pg.TestPgNotifySender
freshplan.event.listener.enabled=false
```

---

## ⚙️ CI-Profile & DB-Config

**`application-ci.properties`:**
```properties
# DevServices AUS in CI
quarkus.datasource.devservices.enabled=false
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan
quarkus.datasource.username=freshplan
quarkus.datasource.password=freshplan

# Flyway: Clean für Determinismus
quarkus.flyway.clean-at-start=true
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=classpath:db/migration

# Connection Pool (CI-optimiert)
quarkus.datasource.jdbc.min-size=2
quarkus.datasource.jdbc.max-size=5

# Hang-Detection
quarkus.test.hang-detection-timeout=60s
```

**Warum `clean-at-start=true`?**
- Deterministisch (jeder Test startet bei 0)
- Versuch mit `false`: Tests hängen (Transaction-Deadlocks)
- Kosten: +2-3s pro @QuarkusTest Boot → akzeptabel

---

## 🏷️ Test-Tags

```java
@Tag("unit")        // Mock-Tests (schnell)
@Tag("integration") // @QuarkusTest (langsam, selektiv)
@Tag("slow")        // Performance-Tests >5s
@Tag("migrate")     // Legacy-Tests (V2XX Migrations)
```

**Nutzung:**
```bash
# Nur schnelle Unit-Tests
./mvnw test -DexcludedGroups="integration,slow"

# Nur Integration-Tests
./mvnw test -Dgroups="integration"
```

---

## ✅ PR-Checkliste (Test-Aspekte)

- [ ] Unit-Tests für neue Logik (Mockito bevorzugen)
- [ ] @QuarkusTest nur wo wirklich nötig
- [ ] Keine hardcoded IDs (nutze `TestIds.unique…()`)
- [ ] Relative Assertions bei DB-Queries
- [ ] Tests laufen lokal im CI-Profil
- [ ] Event-Listener deaktiviert in Tests
- [ ] Suite-Laufzeit plausibel (<20 Min)

---

## 🚀 Quick Commands

```bash
# Einzelner Test
./mvnw test -Dtest=CustomerServiceTest#shouldValidate

# Alle Tests einer Klasse
./mvnw test -Dtest=CustomerServiceTest

# CI-Modus (wie Pipeline)
./mvnw -q -Dquarkus.profile=ci test

# Nur schnelle Tests
./mvnw test -DexcludedGroups="integration,slow"

# Langsamste Tests finden
./mvnw test | grep "Time elapsed" | sort -k4 -n -r | head -10
```

---

## 🔥 Häufige Fehler → Quick-Fix

| Fehler | Ursache | Lösung |
|--------|---------|--------|
| `duplicate key TEST-001` | Statische IDs | `TestIds.uniqueCustomerNumber()` |
| `Failed to start quarkus` | DevServices/Listener | CI-Profile prüfen, Events aus |
| Test sieht keine Daten | Commit-Grenze | `TestTx.committed(…)` wrapper |
| Mock nicht aufgerufen | Feature-Flag/Pfad | Preconditions checken |
| Tests hängen | Event-Listener aktiv | `freshplan.event.listener.enabled=false` |

---

## 📚 Weiterführend

- **Backend Debug Plan:** `BACKEND_TEST_DEBUG_PLAN.md`
- **Master Plan V5:** `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`
- **Quarkus Testing:** https://quarkus.io/guides/getting-started-testing

---

**💡 Faustregel:** Mock first, @QuarkusTest only when necessary!
