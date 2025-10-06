# 🔄 Migration Guide: Legacy Builder → TestDataFactory Pattern

**Sprint:** 2.1.6 | **Issue:** #130 | **Autor:** Claude | **Datum:** 2025-10-05

## 📋 Übersicht

Dieser Guide dokumentiert die Migration von Legacy Buildern (`src/main/java/de/freshplan/test/builders/`) zu TestDataFactory-Pattern (`src/test/java/de/freshplan/test/builders/`).

**Problem:** CDI-Konflikte durch doppelte Builder-Definitionen (src/main vs src/test) verursachten:
- `EntityExistsException: detached entity passed to persist`
- `NoSuchFieldError` bei Repository-Injections
- 12 broken tests in `ContactInteractionServiceIT`

**Lösung:** Legacy Builder gelöscht, alle Tests auf TestDataFactory migriert.

---

## 🎯 Migration Patterns

### 1. CustomerTestDataFactory

#### ✅ KORREKT:
```java
@QuarkusTest
class MyTest {
    @Inject CustomerRepository customerRepository;

    @Test
    void testCustomer() {
        // Option A: Build & Persist
        Customer customer = CustomerTestDataFactory.builder()
            .withCompanyName("Test GmbH")
            .withCustomerType(CustomerType.NEUKUNDE)
            .buildAndPersist(customerRepository);

        // Option B: Build only (für Unit Tests)
        Customer customer = CustomerTestDataFactory.builder()
            .withCompanyName("Test GmbH")
            .build();
        customer.setId(UUID.randomUUID()); // Manuell setzen wenn nötig
    }
}
```

#### ❌ FALSCH:
```java
// FALSCH: Legacy Builder Pattern
@Inject CustomerBuilder customerBuilder;
Customer customer = customerBuilder.withCompanyName("Test").build();

// FALSCH: .reset() existiert nicht
Customer c = CustomerTestDataFactory.builder()
    .reset()  // ❌ Kein .reset()
    .withCompanyName("Test")
    .build();
```

---

### 2. ContactTestDataFactory

⚠️ **WICHTIG**: Benötigt **2 Parameter** für `buildAndPersist()`!

#### ✅ KORREKT:
```java
@QuarkusTest
class MyTest {
    @Inject CustomerRepository customerRepository;
    @Inject EntityManager entityManager;  // ⚠️ PFLICHT!

    @Test
    void testContact() {
        Customer customer = CustomerTestDataFactory.builder()
            .withCompanyName("Test GmbH")
            .buildAndPersist(customerRepository);

        CustomerContact contact = ContactTestDataFactory.builder()
            .forCustomer(customer)
            .withFirstName("Max")
            .withLastName("Mustermann")
            .withEmail("max@test.de")
            .asPrimary()
            .buildAndPersist(customerRepository, entityManager);  // ⚠️ 2 Parameter!
    }
}
```

#### ❌ FALSCH:
```java
// FALSCH: Nur 1 Parameter
contact = ContactTestDataFactory.builder()
    .buildAndPersist(contactRepository);  // ❌ Falsche Repo, fehlender EntityManager

// FALSCH: Falsches Repository
contact = ContactTestDataFactory.builder()
    .buildAndPersist(contactRepository, entityManager);  // ❌ CustomerRepository benötigt!
```

**📌 Merke:**
- 1. Parameter: `CustomerRepository` (nicht ContactRepository!)
- 2. Parameter: `EntityManager`

---

### 3. OpportunityTestDataFactory

⚠️ **WICHTIG**: Spezielle Methodennamen!

#### ✅ KORREKT:
```java
@QuarkusTest
class MyTest {
    @Inject CustomerRepository customerRepository;

    @Test
    void testOpportunity() {
        Customer customer = CustomerTestDataFactory.builder()
            .buildAndPersist(customerRepository);

        User user = UserTestDataFactory.builder()
            .withUsername("testuser")
            .build();

        Opportunity opp = OpportunityTestDataFactory.builder()
            .forCustomer(customer)
            .withName("Test Deal")
            .inStage(OpportunityStage.PROPOSAL)        // ⚠️ .inStage() nicht .withStage()!
            .assignedTo(user)                          // ⚠️ .assignedTo() nicht .withAssignedTo()!
            .withExpectedValue(BigDecimal.valueOf(50000))
            .withProbability(75)
            .build();  // Meist nur .build(), nicht .buildAndPersist()
    }
}
```

#### ❌ FALSCH:
```java
// FALSCH: Falsche Methodennamen
Opportunity opp = OpportunityTestDataFactory.builder()
    .withStage(OpportunityStage.PROPOSAL)    // ❌ Heißt .inStage()
    .withAssignedTo(user)                    // ❌ Heißt .assignedTo()
    .build();
```

---

### 4. CreateCustomerRequest (DTO Builder)

⚠️ **WICHTIG**: Lombok Builder - **KEIN 'with' Präfix**!

#### ✅ KORREKT:
```java
@Test
void testDTO() {
    CreateCustomerRequest request = CreateCustomerRequest.builder()
        .companyName("Test GmbH")              // ✅ KEIN 'with' Präfix
        .customerType(CustomerType.NEUKUNDE)
        .street("Teststr. 1")
        .postalCode("12345")
        .city("Berlin")
        .country("DE")
        .build();
}
```

#### ❌ FALSCH:
```java
// FALSCH: 'with' Präfix (nur bei Entity Factories!)
CreateCustomerRequest req = CreateCustomerRequest.builder()
    .withCompanyName("Test")     // ❌ Lombok nutzt kein 'with' Präfix
    .withCustomerType(...)       // ❌ Falsch
    .build();

// FALSCH: Nicht-existierender Builder
CreateCustomerRequest req = CustomerTestDataFactory.builder()
    .buildCreateRequest();       // ❌ Methode existiert nicht
```

**📌 Merke:**
- **Entity Factories** (CustomerTestDataFactory): `.withX()` Präfix
- **DTO Builders** (CreateCustomerRequest): **KEIN** `.withX()` Präfix (Lombok)

---

### 5. TestFixtures.CustomerBuilder (Mock Tests)

Für **Mock-Tests ohne @QuarkusTest**:

#### ✅ KORREKT:
```java
@ExtendWith(MockitoExtension.class)
class MyMockTest {
    private final CustomerBuilder customerBuilder = TestFixtures.customer();

    @Test
    void testMock() {
        Customer mockCustomer = customerBuilder
            .withName("Test Company GmbH")     // ⚠️ .withName() nicht .withCompanyName()!
            .build();
        mockCustomer.setId(UUID.randomUUID());
    }
}
```

#### ❌ FALSCH:
```java
// FALSCH: Falsche Methodennamen
Customer mock = TestFixtures.customer()
    .withCompanyName("Test")    // ❌ Heißt .withName() bei TestFixtures!
    .build();
```

**📌 Merke:** `TestFixtures.CustomerBuilder` nutzt `.withName()`, nicht `.withCompanyName()`!

---

## 🔧 Häufige Migration-Schritte

### Schritt 1: Imports aktualisieren
```java
// ❌ ENTFERNEN:
import de.freshplan.test.builders.CustomerBuilder;
import de.freshplan.test.builders.ContactBuilder;

// ✅ HINZUFÜGEN:
import de.freshplan.test.builders.CustomerTestDataFactory;
import de.freshplan.test.builders.ContactTestDataFactory;
```

### Schritt 2: @Inject entfernen
```java
// ❌ ENTFERNEN:
@Inject CustomerBuilder customerBuilder;
@Inject ContactBuilder contactBuilder;

// ✅ HINZUFÜGEN (falls buildAndPersist benötigt):
@Inject EntityManager entityManager;
```

### Schritt 3: Builder-Aufrufe migrieren
```java
// VORHER (Legacy):
Customer customer = customerBuilder
    .withCompanyName("Test")
    .build();
customerRepository.persist(customer);

// NACHHER (TestDataFactory):
Customer customer = CustomerTestDataFactory.builder()
    .withCompanyName("Test GmbH")
    .buildAndPersist(customerRepository);
```

---

## 📊 Migration-Statistik (Issue #130)

- **Dateien migriert:** 45/45 (100%)
- **Compilation Errors:** 409 → 0 (100% eliminiert)
- **Tests Fixed:** 12/12 ContactInteractionServiceIT (100%)
- **Legacy Builder gelöscht:** 7 Dateien
- **Migration-Phasen:** 4 (3 Agenten + 1 manuelle Cleanup)
- **Dauer:** ~4h (mit Agenten-Unterstützung)

---

## ⚠️ Kritische Unterschiede

| Aspekt | Legacy Builder | TestDataFactory |
|--------|----------------|-----------------|
| **CDI** | @ApplicationScoped, @Inject | Kein CDI, statische .builder() |
| **Persist** | Manuell via Repository | `.buildAndPersist(repo)` |
| **Contact Persist** | `.persist()` | `.buildAndPersist(customerRepo, em)` ⚠️ |
| **Opportunity Stage** | `.withStage()` | `.inStage()` ⚠️ |
| **Opportunity User** | `.withAssignedTo()` | `.assignedTo()` ⚠️ |
| **DTO Builder** | - | **KEIN 'with' Präfix** ⚠️ |
| **TestFixtures** | - | `.withName()` nicht `.withCompanyName()` ⚠️ |

---

## 🚨 Häufige Fehler

### 1. EntityManager fehlt bei ContactTestDataFactory
```
Error: required: CustomerRepository, EntityManager; found: ContactRepository
```
**Fix:** `@Inject EntityManager entityManager;` hinzufügen

### 2. Falsche Methodennamen bei OpportunityTestDataFactory
```
Error: cannot find symbol: method withStage(OpportunityStage)
```
**Fix:** `.withStage()` → `.inStage()`

### 3. Lombok Builder Confusion
```
Error: cannot find symbol: method withCompanyName(String)
```
**Fix:** Bei DTOs kein 'with' Präfix: `.companyName()` statt `.withCompanyName()`

### 4. TestFixtures API verwechselt
```
Error: cannot find symbol: method withCompanyName(String)
```
**Fix:** `TestFixtures.customer().withName()` statt `.withCompanyName()`

---

## ✅ Checkliste für Test-Migration

- [ ] Legacy Builder Imports entfernt
- [ ] TestDataFactory Imports hinzugefügt
- [ ] @Inject Builder-Felder entfernt
- [ ] EntityManager injection hinzugefügt (falls ContactTestDataFactory)
- [ ] `.buildAndPersist()` Aufrufe korrekt (CustomerRepository + EntityManager)
- [ ] OpportunityTestDataFactory: `.inStage()` und `.assignedTo()` verwendet
- [ ] DTO Builder: **KEIN** 'with' Präfix
- [ ] TestFixtures: `.withName()` statt `.withCompanyName()`
- [ ] Test kompiliert ohne Fehler
- [ ] Test läuft erfolgreich durch

---

## 🔗 Weitere Ressourcen

- **Issue #130:** https://github.com/joergstreeck/freshplan-sales-tool/issues/130
- **Factory Implementierungen:** `/backend/src/test/java/de/freshplan/test/builders/`
- **Beispiel-Migrationen:** `ContactInteractionServiceIT.java`, `OpportunityMapperTest.java`
- **Sprint Planning:** `/docs/planung/TRIGGER_SPRINT_2_1_6.md`

---

**📅 Stand:** 2025-10-05 | **Status:** ✅ Vollständig | **Autor:** Claude (Sprint 2.1.6 Phase 1)
