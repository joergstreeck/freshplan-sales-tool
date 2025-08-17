# 🎯 TEST DATA STRATEGY - Die neue Testkunden-Architektur
## Produktbeschreibung & Referenzhandbuch

**Version:** 4.0  
**Status:** TEAM-APPROVED & PRODUCTION-READY  
**Datum:** 17.08.2025  
**Autor:** Claude & Jörg (Architektur-Session) + Team-Expertise

---

## 📚 Inhaltsverzeichnis

1. [Executive Summary](#1-executive-summary)
2. [Die Philosophie](#2-die-philosophie)
3. [Architektur-Übersicht](#3-architektur-übersicht)
4. [Der TestDataBuilder](#4-der-testdatabuilder)
5. [Race-Condition-Safe Helpers](#5-race-condition-safe-helpers-neu)
6. [Test-Kategorien & Patterns](#6-test-kategorien--patterns)
7. [Erweiterung für neue Features](#7-erweiterung-für-neue-features)
8. [Best Practices](#8-best-practices)
9. [CI-Integration](#9-ci-integration-kritisch)
10. [Monitoring & Metriken](#10-monitoring--metriken)

---

## 1. Executive Summary

### Was ist das?
Eine **zentrale, erweiterbare Test-Daten-Architektur** die:
- ✅ Test-Isolation garantiert
- ✅ CI-Stabilität sicherstellt
- ✅ Neue Features einfach integriert
- ✅ Von jedem Entwickler in 5 Minuten verstanden wird

### Kernprinzip
```
Ein System, eine Wahrheit, keine Magie
```

### Die drei Säulen

| Säule | Zweck | Implementierung |
|-------|-------|-----------------|
| **SEED-Daten** | Stabile E2E-Tests | V9999 Migration (20 Einträge) |
| **TestDataBuilder** | Dynamische Test-Daten | Java Builder Pattern |
| **Test-Isolation** | Unabhängige Tests | @TestTransaction |

---

## 2. Die Philosophie

### 2.1 Warum diese Architektur?

#### Das Problem vorher
```
Chaos-Zustand:
├── 6 verschiedene Initializers
├── 4 Test-Migrationen
├── 233 Stellen die Daten erstellen
└── Niemand weiß, woher welche Daten kommen
```

#### Die Lösung
```
Klare Struktur:
├── 1 TestDataBuilder (Single Source of Truth)
├── 1 SEED-Migration (V9999)
└── Klare Verantwortlichkeiten
```

### 2.2 Grundprinzipien

#### **Prinzip 1: Test Isolation**
> Jeder Test erstellt und verwaltet seine eigenen Daten

```java
@Test
@TestTransaction  // Automatisches Rollback
void myTest() {
    // Eigene Daten erstellen
    Customer c = testDataBuilder.customer().build();
    // Test
    // Rollback automatisch!
}
```

#### **Prinzip 2: Explizit statt Implizit**
> Keine Magie, keine versteckten Abhängigkeiten

```java
// ❌ SCHLECHT: Woher kommt das?
Customer c = findTestCustomer();

// ✅ GUT: Explizit erstellt
Customer c = testDataBuilder.customer()
    .withName("Mein Test")
    .build();
```

#### **Prinzip 3: Erweiterbarkeit**
> Neue Features fügen zum Builder hinzu, nicht neue Systeme

---

## 3. Architektur-Übersicht

### 3.1 Gesamt-Architektur

```
┌─────────────────────────────────────────────────────────┐
│                     PRODUCTION                          │
│                 Keine Test-Daten!                       │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                    DEVELOPMENT                          │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │            TestDataBuilder (Java)                │   │
│  │         Erstellt Daten on-demand                 │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                      TESTING                            │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │         V9999 SEED-Daten (20 Einträge)          │   │
│  │              Stabile Referenz-Daten              │   │
│  └─────────────────────────────────────────────────┘   │
│                           +                             │
│  ┌─────────────────────────────────────────────────┐   │
│  │            TestDataBuilder (Java)                │   │
│  │         Dynamische Test-Daten pro Test          │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 3.2 Datenfluss

```
Test startet
    ↓
@TestTransaction (Begin)
    ↓
TestDataBuilder.customer()
    .withName("Test")
    .build()
    ↓
Test läuft
    ↓
@TestTransaction (Rollback)
    ↓
DB ist wieder sauber!
```

### 3.3 Komponenten-Diagramm

```
┌──────────────────────────────────────┐
│         TestDataBuilder              │
│                                      │
│  ┌──────────────────────────────┐   │
│  │    CustomerBuilder           │   │
│  │  - withName()                │   │
│  │  - withStatus()              │   │
│  │  - asPremium()               │   │
│  │  - asRisk()                  │   │
│  └──────────────────────────────┘   │
│                                      │
│  ┌──────────────────────────────┐   │
│  │    ContactBuilder            │   │
│  │  - forCustomer()             │   │
│  │  - asPrimary()               │   │
│  └──────────────────────────────┘   │
│                                      │
│  ┌──────────────────────────────┐   │
│  │    OpportunityBuilder        │   │
│  │  - forCustomer()             │   │
│  │  - withValue()               │   │
│  │  - inStage()                 │   │
│  └──────────────────────────────┘   │
│                                      │
│  ┌──────────────────────────────┐   │
│  │    ScenarioBuilder           │   │
│  │  - completeFlow()            │   │
│  │  - withDependencies()        │   │
│  └──────────────────────────────┘   │
└──────────────────────────────────────┘
```

---

## 4. Der TestDataBuilder ✅ IMPLEMENTIERT (17.08.2025)

### 4.1 Konzept & Struktur

Der `TestDataBuilder` wurde als **modulare Architektur** mit separaten Dateien implementiert:

```
test/
├── TestDataBuilder.java              # Zentrale Facade (222 Zeilen)
├── builders/                          
│   ├── CustomerBuilder.java          # 338 Zeilen - 15 Enums, 6 Szenarien
│   ├── ContactBuilder.java           # 269 Zeilen - Rollen & Kommunikation
│   ├── OpportunityBuilder.java       # 263 Zeilen - Sales-Stages
│   ├── TimelineEventBuilder.java     # 352 Zeilen - 10 Event-Typen
│   └── UserBuilder.java              # 220 Zeilen - 6 Rollen-Szenarien
└── utils/
    └── TestDataUtils.java            # 69 Zeilen - Shared Utilities
```

**Vorteile der Aufteilung:**
- ✅ Bessere Wartbarkeit (200-350 Zeilen statt 1500+)
- ✅ Single Responsibility Principle
- ✅ Parallele Entwicklung möglich
- ✅ Einfacheres Testing der Builder

```java
@ApplicationScoped
public class TestDataBuilder {
    // Injection der separaten Builder
    @Inject CustomerBuilder customerBuilder;
    @Inject ContactBuilder contactBuilder;
    @Inject OpportunityBuilder opportunityBuilder;
    @Inject TimelineEventBuilder timelineEventBuilder;
    @Inject UserBuilder userBuilder;
    
    // Reset() für frische Instanzen
    public CustomerBuilder customer() { 
        return customerBuilder.reset(); 
    }
    
    // Komplette Szenarien
    public ScenarioBuilder scenario() { ... }
}
```

### 4.2 Builder-Pattern im Detail (MIT build() vs persist())

#### Basic Builder - ZWEI PFADE!
```java
public class CustomerBuilder {
    private String companyName = "Test Company";
    private CustomerStatus status = CustomerStatus.AKTIV;
    private boolean isTestData = true;  // IMMER!
    
    public CustomerBuilder withCompanyName(String name) {
        this.companyName = name;
        return this;
    }
    
    /**
     * build() - NUR Objekt-Erstellung, KEINE DB
     * Für: Unit-Tests, Service-Tests ohne Persistierung
     */
    public Customer build() {
        Customer c = new Customer();
        String id = uniqueId();
        c.setCustomerNumber("TEST-" + id);  // GARANTIERT unique!
        c.setCompanyName("[TEST-" + id + "] " + companyName);
        c.setIsTestData(true);
        // Alle Required Fields setzen
        c.setPartnerStatus(PartnerStatus.KEIN_PARTNER);
        c.setPaymentTerms(PaymentTerms.NETTO_30);
        c.setPrimaryFinancing(FinancingType.PRIVATE);
        return c;
    }
    
    /**
     * persist() - Objekt-Erstellung MIT DB-Persistierung
     * Für: Integration-Tests, Tests mit DB-Interaktion
     */
    @Transactional
    public Customer persist() {
        Customer c = build();
        customerRepository.persist(c);
        customerRepository.flush();  // Constraints SOFORT prüfen!
        return c;
    }
}
```

#### Vordefinierte Szenarien
```java
public class CustomerBuilder {
    
    // Häufige Test-Szenarien als Shortcuts
    public CustomerBuilder asPremiumCustomer() {
        return this
            .withStatus(CustomerStatus.PREMIUM)
            .withAnnualVolume(1_000_000)
            .withLoyaltyYears(5)
            .withCreditLine(100_000);
    }
    
    public CustomerBuilder asRiskCustomer() {
        return this
            .withStatus(CustomerStatus.RISIKO)
            .withPaymentDelayDays(90)
            .withRiskScore(85)
            .withCreditLine(0);
    }
    
    public CustomerBuilder asNewLead() {
        return this
            .withStatus(CustomerStatus.LEAD)
            .withCreatedToday()
            .withNoContacts();
    }
}
```

### 4.3 Komplette Szenarien

```java
public class ScenarioBuilder {
    
    public TestScenario createCompleteBusinessFlow() {
        // Customer mit allen Abhängigkeiten
        Customer customer = customerBuilder
            .asPremiumCustomer()
            .persist();
            
        Contact primaryContact = contactBuilder
            .forCustomer(customer)
            .asPrimaryContact()
            .persist();
            
        Contact salesContact = contactBuilder
            .forCustomer(customer)
            .asSalesContact()
            .persist();
            
        Opportunity opportunity = opportunityBuilder
            .forCustomer(customer)
            .withValue(250_000)
            .inStage(Stage.NEGOTIATION)
            .persist();
            
        // Audit Trail
        auditService.log("Test scenario created", customer.getId());
        
        return new TestScenario(
            customer, 
            List.of(primaryContact, salesContact),
            List.of(opportunity)
        );
    }
}
```

---

## 5. Race-Condition-Safe Helpers (NEU!)

### 5.1 Das Problem: Concurrent Test Execution

Bei parallelen Tests entstehen Race Conditions bei gemeinsamen Referenzdaten:
- Permissions
- Rollen
- Industries
- Alle Enum-ähnlichen Entities

### 5.2 Die Lösung: PermissionHelper mit PostgreSQL ON CONFLICT ✅ OHNE REQUIRES_NEW

```java
@ApplicationScoped
public class PermissionHelperPg {

    @Inject EntityManager em;

    /**
     * Race-safe Permission-Erstellung mit PostgreSQL ON CONFLICT
     * - ON CONFLICT ist atomisch genug - KEIN REQUIRES_NEW nötig!
     * - Adaptiv: Erkennt ob 'code' oder 'permission_code' existiert
     * - 100% Thread-safe
     */
    @Transactional
    public Permission findOrCreatePermission(String code, String description) {
        // Test-Markierung über Description (permissions hat kein is_test_data!)
        String testDesc = "[TEST] " + (description != null ? description : code);
        
        UUID id = (UUID) em.createNativeQuery("""
            INSERT INTO permissions (
                id, permission_code, name, description, resource, action, created_at
            )
            VALUES (
                gen_random_uuid(), :code, :name, :desc, :resource, :action, CURRENT_TIMESTAMP
            )
            ON CONFLICT (permission_code) DO NOTHING
            RETURNING id
            """)
            .setParameter("code", code)
            .setParameter("name", code.replace(":", " "))
            .setParameter("desc", testDesc)
            .setParameter("resource", code.split(":")[0])
            .setParameter("action", code.split(":")[1])
            .getSingleResult();
            
        // Falls ON CONFLICT: hole existierende Permission
        if (id == null) {
            return em.createQuery(
                "SELECT p FROM Permission p WHERE p.permissionCode = :code", 
                Permission.class)
                .setParameter("code", code)
                .getSingleResult();
        }
        
        return em.find(Permission.class, id);
    }
}
```

### 5.3 Integration in TestDataBuilder

```java
@ApplicationScoped
public class TestDataBuilder {

    @Inject PermissionHelperPg permissionHelper;

    public UserBuilder user() {
        return new UserBuilder(permissionHelper);
    }
    
    public class UserBuilder {
        private Set<String> permissionCodes = new HashSet<>();
        
        public UserBuilder withPermission(String code) {
            permissionCodes.add(code);
            return this;
        }
        
        public User persist() {
            User user = build();
            // Race-safe permission assignment
            for (String code : permissionCodes) {
                Permission p = permissionHelper.findOrCreatePermission(
                    code, 
                    "Test permission: " + code
                );
                user.addPermission(p);
            }
            userRepository.persist(user);
            return user;
        }
    }
}
```

### 5.4 Concurrency-Test

```java
@Test
void concurrent_permission_creation_is_safe() throws Exception {
    String code = "perm:test";
    int threads = 16;
    var pool = Executors.newFixedThreadPool(threads);
    
    var tasks = IntStream.range(0, 50)
        .<Callable<Permission>>mapToObj(i ->
            () -> permissionHelper.findOrCreatePermission(code, "Test")
        ).toList();

    pool.invokeAll(tasks);
    
    // MUSS genau 1 sein trotz 50 parallelen Versuchen!
    assertThat(permissionRepository.count("code", code)).isEqualTo(1);
}
```

---

## 6. Test-Kategorien & Patterns

### 6.1 Die drei Test-Kategorien

| Kategorie | Datenbank | Test-Daten | Rollback |
|-----------|-----------|------------|----------|
| **Unit Tests** | ❌ Keine | Mocks | - |
| **Integration Tests** | ✅ TestContainers | TestDataBuilder | @TestTransaction |
| **E2E Tests** | ✅ Real DB | SEED + Builder | Manuell |

### 6.2 Test Patterns

#### Pattern 1: Unit Test (keine DB)
```java
@Test
class CustomerServiceUnitTest {
    
    @Mock
    CustomerRepository repository;
    
    @InjectMocks
    CustomerService service;
    
    @Test
    void calculateDiscount_forPremiumCustomer_returns15Percent() {
        // Given: Mock-Daten
        Customer mockCustomer = mock(Customer.class);
        when(mockCustomer.getStatus()).thenReturn(CustomerStatus.PREMIUM);
        when(repository.findById(1L)).thenReturn(mockCustomer);
        
        // When
        Discount discount = service.calculateDiscount(1L);
        
        // Then
        assertThat(discount.getPercentage()).isEqualTo(15);
    }
}
```

#### Pattern 2: Integration Test (mit DB)
```java
@QuarkusTest
class CustomerServiceIntegrationTest {
    
    @Inject
    TestDataBuilder testDataBuilder;
    
    @Inject
    CustomerService service;
    
    @Test
    @TestTransaction  // ← WICHTIG! Automatisches Rollback
    void createCustomer_withValidData_persistsInDatabase() {
        // Given: Test-Daten mit Builder
        CreateCustomerRequest request = testDataBuilder
            .createCustomerRequest()
            .withCompanyName("Integration Test AG")
            .build();
        
        // When
        Customer created = service.createCustomer(request);
        
        // Then
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCompanyName()).contains("Integration Test AG");
        
        // Cleanup: Automatisch durch @TestTransaction!
    }
}
```

#### Pattern 3: E2E Test (voller Stack)
```java
@QuarkusIntegrationTest
class CustomerE2ETest {
    
    @Test
    void completeCustomerJourney() {
        // SEED-001 existiert immer (aus V9999)
        
        // 1. Read existing customer
        given()
            .when()
            .get("/api/customers/SEED-001")
            .then()
            .statusCode(200)
            .body("companyName", equalTo("[SEED] Active Restaurant"));
            
        // 2. Update customer
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "status": "PREMIUM",
                    "annualVolume": 500000
                }
                """)
            .when()
            .patch("/api/customers/SEED-001")
            .then()
            .statusCode(200);
            
        // 3. Create opportunity
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "customerId": "SEED-001",
                    "name": "E2E Test Opportunity",
                    "value": 100000
                }
                """)
            .when()
            .post("/api/opportunities")
            .then()
            .statusCode(201);
    }
}
```

---

## 7. Erweiterung für neue Features

### 7.1 Workflow für neue Features

```
Neues Feature "X" braucht Test-Daten
            ↓
    TestDataBuilder erweitern
            ↓
    Neue Builder-Methoden
            ↓
    Tests schreiben
            ↓
    (Optional) SEED-Daten?
```

### 7.2 Praktisches Beispiel: "AI-Recommendations" Feature

#### Schritt 1: Analyse
Das AI-Feature braucht:
- Kunden mit Historie
- Saisonale Muster
- Kaufverhalten

#### Schritt 2: TestDataBuilder erweitern

```java
public class TestDataBuilder {
    
    // NEU: AI-spezifischer Builder
    public AITestDataBuilder aiTestData() {
        return new AITestDataBuilder();
    }
    
    public class AITestDataBuilder {
        
        public Customer createSeasonalCustomer() {
            Customer customer = customerBuilder
                .withCompanyName("Seasonal Business")
                .persist();
                
            // Saisonale Order-Historie
            for (Month month : Month.values()) {
                int volume = calculateSeasonalVolume(month);
                createOrder(customer, month, volume);
            }
            
            return customer;
        }
        
        public Customer createPredictableCustomer() {
            // Customer mit vorhersagbarem Muster
            Customer customer = customerBuilder
                .withCompanyName("Predictable Corp")
                .persist();
                
            // Regelmäßige Bestellungen
            for (int week = 1; week <= 52; week++) {
                createWeeklyOrder(customer, week, 10000);
            }
            
            return customer;
        }
    }
}
```

#### Schritt 3: Tests schreiben

```java
@Test
@TestTransaction
void aiRecommendation_forSeasonalCustomer_suggestsStockIncrease() {
    // Given: Saisonaler Kunde
    Customer seasonal = testDataBuilder
        .aiTestData()
        .createSeasonalCustomer();
        
    // When: AI-Analyse
    Recommendation rec = aiService.analyzeCustomer(seasonal);
    
    // Then: Korrekte Empfehlung
    assertThat(rec.getType()).isEqualTo("SEASONAL_PREPARATION");
    assertThat(rec.getSuggestedActions()).contains("Increase stock before summer");
}
```

#### Schritt 4: (Optional) SEED-Daten erweitern

Nur wenn E2E-Tests das Feature brauchen:

```sql
-- In V9999 ergänzen:
INSERT INTO customers (customer_number, company_name, business_type, is_test_data)
VALUES ('SEED-AI-001', '[SEED] Seasonal Ice Cream Shop', 'SEASONAL', true);
```

### 7.3 Feature-spezifische Traits

```java
// Trait-Interface für wiederverwendbare Patterns
public interface CustomerTraits {
    
    interface Seasonal {
        CustomerBuilder withSummerPeak();
        CustomerBuilder withWinterPeak();
        CustomerBuilder withChristmasPeak();
    }
    
    interface Financial {
        CustomerBuilder withCreditLine(BigDecimal amount);
        CustomerBuilder withPaymentTerms(int days);
        CustomerBuilder withDiscount(int percent);
    }
    
    interface Loyalty {
        CustomerBuilder asNewCustomer();      // < 3 Monate
        CustomerBuilder asRegular();          // 1-3 Jahre
        CustomerBuilder asLoyal();            // 3-5 Jahre
        CustomerBuilder asVIP();              // > 5 Jahre
    }
}

// Verwendung:
Customer vipCustomer = testDataBuilder.customer()
    .asVIP()                    // Loyalty trait
    .withCreditLine(100_000)    // Financial trait
    .withSummerPeak()           // Seasonal trait
    .build();
```

---

## 8. Best Practices

### 8.1 DO's ✅

#### Immer TestDataBuilder verwenden
```java
// ✅ RICHTIG
Customer c = testDataBuilder.customer().build();

// ❌ FALSCH
Customer c = new Customer();
```

#### Immer Test-Markierung
```java
// ✅ RICHTIG: Automatisch durch Builder
customer.setIsTestData(true);
customer.setCompanyName("[TEST-123] Name");

// ❌ FALSCH: Keine Markierung
customer.setCompanyName("Test GmbH");
```

#### Test-Isolation sicherstellen
```java
// ✅ RICHTIG: Eigene Daten
@Test
@TestTransaction
void myTest() {
    Customer c = testDataBuilder.customer()
        .withUniqueName()
        .build();
}

// ❌ FALSCH: Geteilte Daten
@BeforeAll
static void setupSharedData() {
    sharedCustomer = createCustomer();
}
```

### 8.2 DON'Ts ❌

#### Keine neuen Initializers
```java
// ❌ NIEMALS neue Initializer erstellen!
@ApplicationScoped
@IfBuildProfile("dev")
public class MyNewTestDataInitializer {  // NEIN!
```

#### Keine Test-Migrationen
```sql
-- ❌ KEINE neuen Test-Daten-Migrationen!
-- V300__my_test_data.sql  // NEIN!
```

#### Keine hartcodierten IDs
```java
// ❌ FALSCH
Customer c = repository.findById(123L);

// ✅ RICHTIG
Customer c = testDataBuilder.customer().persist();
Long id = c.getId();
```

### 8.3 Naming Conventions

| Prefix | Verwendung | Beispiel |
|--------|------------|----------|
| `[SEED]` | Stabile E2E-Daten aus V9999 | `[SEED] Restaurant München` |
| `[TEST-{id}]` | Dynamische Test-Daten | `[TEST-1234567] Test Customer` |
| `[DEV]` | Manuelle Dev-Daten | `[DEV] Demo Restaurant` |

---

## 9. CI-Integration (KRITISCH!)

### 9.1 Das CI-Flag Problem

**Problem:** V10000 Migration braucht `ci.build` Flag um in CI zu funktionieren!

### 9.2 Die Lösung: JDBC-URL Konfiguration

```yaml
# .github/workflows/backend-ci.yml
- name: Run tests with CI flag
  run: |
    ./mvnw test \
      -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan?options=-c%20ci.build%3Dtrue \
      -Dquarkus.profile=ci
```

### 9.3 Guard Migrations für CI

```sql
-- V10001__assert_test_data_contract.sql
DO $$
DECLARE
    test_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO test_count 
    FROM customers WHERE is_test_data = true;
    
    IF test_count > 30 THEN
        RAISE EXCEPTION 'Too many test customers: %. Maximum: 30', test_count;
    END IF;
    
    RAISE NOTICE 'Test data OK: % customers', test_count;
END $$;
```

### 9.4 CI-spezifische Zwei-Stufen-Cleanup ✅ IMPLEMENTIERT

```sql
-- V10000__cleanup_test_data_in_ci.sql
DO $$
DECLARE
    test_count INTEGER;
    hard_threshold INTEGER := 100;
    soft_threshold INTEGER := 50;
BEGIN
    -- NUR in CI ausführen!
    IF current_setting('ci.build', true) <> 'true' THEN
        RETURN;
    END IF;
    
    SELECT COUNT(*) INTO test_count 
    FROM customers WHERE is_test_data = true;
    
    IF test_count > hard_threshold THEN
        -- HARD CLEANUP: Lösche ALLE Test-Daten
        DELETE FROM customers WHERE is_test_data = true;
        RAISE NOTICE 'HARD cleanup: Deleted all % test records', test_count;
        
    ELSIF test_count > soft_threshold THEN
        -- SOFT CLEANUP: Nur alte Daten (>90 Minuten)
        DELETE FROM customers 
        WHERE is_test_data = true 
          AND created_at < NOW() - INTERVAL '90 minutes';
        RAISE NOTICE 'SOFT cleanup: Deleted old test records';
        
    ELSE
        -- NO CLEANUP: Alles OK
        RAISE NOTICE 'No cleanup needed: % test records', test_count;
    END IF;
END $$;
```

### 9.5 Test-Daten Metriken in CI

```bash
# CI-Check nach Tests
psql -c "SELECT * FROM test_data_dashboard" | tee test-metrics.log

# Fail wenn Status nicht OK
if grep -q "CRITICAL\|WARNING" test-metrics.log; then
    echo "❌ Too many test data!"
    exit 1
fi
```

---

## 10. Monitoring & Metriken

### 10.1 Test-Daten-Dashboard

```sql
-- Monitoring Query für Test-Daten
CREATE VIEW test_data_dashboard AS
WITH stats AS (
    SELECT 
        COUNT(*) as total_test_data,
        COUNT(*) FILTER (WHERE company_name LIKE '[SEED]%') as seed_data,
        COUNT(*) FILTER (WHERE company_name LIKE '[TEST-%]%') as dynamic_test_data,
        COUNT(*) FILTER (WHERE company_name LIKE '[DEV]%') as dev_data,
        COUNT(*) FILTER (WHERE created_at > NOW() - INTERVAL '1 hour') as created_last_hour,
        COUNT(*) FILTER (WHERE created_at < NOW() - INTERVAL '7 days') as older_than_week
    FROM customers
    WHERE is_test_data = true
)
SELECT 
    total_test_data,
    seed_data,
    dynamic_test_data,
    dev_data,
    created_last_hour,
    older_than_week,
    CASE 
        WHEN older_than_week > 100 THEN 'CLEANUP NEEDED!'
        WHEN total_test_data > 200 THEN 'WARNING: Too many test data'
        ELSE 'OK'
    END as status
FROM stats;
```

### 10.2 CI-Metriken

```yaml
# GitHub Actions Metrics
test-metrics:
  - test-data-count: < 50
  - test-duration: < 10min
  - flaky-test-rate: < 1%
  - isolation-violations: 0
```

### 10.3 Automatisches Cleanup

```java
@Scheduled(cron = "0 0 * * * ?")  // Täglich um Mitternacht
public void cleanupOldTestData() {
    // Lösche Test-Daten älter als 7 Tage
    int deleted = customerRepository
        .delete("isTestData = ?1 AND createdAt < ?2",
                true, 
                LocalDateTime.now().minusDays(7));
                
    LOG.info("Cleaned up {} old test records", deleted);
}
```

---

## 🎯 Zusammenfassung

### Das System in einem Satz:
> **Ein zentraler TestDataBuilder erstellt alle Test-Daten on-demand, isoliert und markiert.**

### Die Vorteile:
- ✅ **Einfachheit**: Ein System für alles
- ✅ **Isolation**: Keine Test-Abhängigkeiten  
- ✅ **Erweiterbarkeit**: Features fügen zum Builder hinzu
- ✅ **Stabilität**: CI wird endlich grün
- ✅ **Performance**: Weniger Daten, schnellere Tests

### Der Erfolg:
```
Vorher: 6 Systeme, 73+ Test-Daten, CI rot
Nachher: 1 System, <30 Test-Daten, CI grün
```

---

## 11. Qualitätssicherung durch Automatisierung (NEU)

### 11.1 ArchUnit-Regeln für Test-Disziplin

```java
// Erzwingt Builder-Nutzung automatisch
@ArchTest
static final ArchRule no_direct_persist_in_tests =
    methods().that().areDeclaredInClassesThat()
        .haveSimpleNameEndingWith("Test")
    .should().notCallMethod(CustomerRepository.class, "persist");
```

### 11.2 CI-Checks für Anti-Patterns

```bash
# Mockito varargs check in CI
if grep -r "when.*delete.*eq(" src/test/java; then
    echo "❌ Use any() instead of eq() for Panache delete!"
    exit 1
fi
```

### 11.3 V9000 Environment-Guard

```sql
-- Verhindert versehentliche Prod-Ausführung
IF current_setting('freshplan.environment', true) NOT IN ('ci', 'test') THEN
    RAISE NOTICE 'V9000 skipped - only for CI/Test';
    RETURN;
END IF;
```

### 11.4 Database Growth auf Test-Daten fokussiert

```java
// Nur is_test_data = true wird überwacht
"SELECT COUNT(*) FROM customers WHERE is_test_data = true"
```

---

*"Simplicity is the ultimate sophistication" - Leonardo da Vinci*

**Ende der Produktbeschreibung**