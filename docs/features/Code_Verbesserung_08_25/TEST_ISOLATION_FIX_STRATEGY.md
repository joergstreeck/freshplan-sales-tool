# Test Isolation Fix-Strategie

## 🚨 Kritisches Problem
60 Tests ohne Isolation führen zur Datenbank-Explosion (1090 statt 74 Kunden)

## 📊 Problem-Analyse
- **16 KRITISCHE Tests** (Severity ≥ 8)
- **29 HOHE Tests** (Severity ≥ 5)
- **Hauptursache**: `@Transactional` statt `@TestTransaction`

## 🎯 Fix-Strategie

### Option 1: @TestTransaction (Empfohlen)
```java
// VORHER - Daten bleiben permanent
@BeforeEach
@Transactional
void setUp() {
    customerRepository.persist(testCustomer);
}

// NACHHER - Automatischer Rollback
@BeforeEach
@TestTransaction  // ← Das ist der Fix!
void setUp() {
    customerRepository.persist(testCustomer);
}
```

**Vorteile:**
- ✅ Minimale Änderung (nur Annotation tauschen)
- ✅ Automatischer Rollback nach jedem Test
- ✅ Quarkus Best Practice
- ✅ Keine Performance-Einbußen

**Nachteile:**
- ❌ Funktioniert nicht mit async/await Tests

### Option 2: @AfterEach Cleanup
```java
@AfterEach
@Transactional
void cleanup() {
    // Lösche in umgekehrter Reihenfolge (FK Constraints!)
    interactionRepository.deleteAll();
    contactRepository.deleteAll();
    opportunityRepository.deleteAll();
    customerRepository.delete("createdBy", "testuser");
}
```

**Vorteile:**
- ✅ Funktioniert mit async Tests
- ✅ Explizite Kontrolle

**Nachteile:**
- ❌ Mehr Code
- ❌ FK Constraints müssen beachtet werden
- ❌ Langsamer

### Option 3: Test-spezifische Datenbank
```java
@QuarkusTestResource(TestDatabaseResource.class)
class MyTest {
    // Eigene H2 in-memory DB pro Test
}
```

**Vorteile:**
- ✅ Perfekte Isolation
- ✅ Schnell (in-memory)

**Nachteile:**
- ❌ Unterschied zu Production DB
- ❌ Mehr Setup

## 📝 Implementierungs-Plan

### Phase 1: Kritische Tests (SOFORT)
1. SearchCQRSIntegrationTest
2. CustomerQueryServiceIntegrationTest
3. ContactPerformanceTest
4. HtmlExportCQRSIntegrationTest
5. OpportunityResourceIntegrationTest
6. OpportunityRepositoryTest

### Phase 2: Hohe Priorität
- Alle CQRS Integration Tests
- Alle Tests mit @BeforeEach + persist()

### Phase 3: Mittlere Priorität
- Tests mit @Transactional ohne @TestTransaction

## 🔧 Umsetzung

### Schritt 1: Import hinzufügen
```java
import io.quarkus.test.TestTransaction;
```

### Schritt 2: Annotation ersetzen
```java
// Suche:
@Transactional

// Ersetze mit:
@TestTransaction
```

### Schritt 3: Spezialfälle behandeln

#### Async Tests (mit await())
Diese Tests brauchen speziellen Ansatz:
```java
@Test
void asyncTest() {
    // KEIN @TestTransaction hier!
    // Stattdessen QuarkusTransaction.call() in await()
    
    await().until(() -> {
        return QuarkusTransaction.call(() -> {
            // DB access hier
        });
    });
}
```

#### Tests mit mehreren Transaktionen
```java
@Test
@TestTransaction
void testWithMultipleTransactions() {
    // Haupt-Transaktion
    
    QuarkusTransaction.requiringNew().run(() -> {
        // Neue Transaktion
    });
}
```

## ⚠️ Wichtige Hinweise

1. **NIEMALS beide Annotations gleichzeitig**
   ```java
   // ❌ FALSCH
   @Transactional
   @TestTransaction
   
   // ✅ RICHTIG
   @TestTransaction
   ```

2. **Reihenfolge beachten bei Cleanup**
   - ContactInteractions → Contacts → Opportunities → Customers

3. **Test nach Fix verifizieren**
   ```bash
   mvn test -Dtest=TestName
   ```

## 🎯 Erfolgskriterien

1. **Keine neuen Test-Daten nach Test-Run**
2. **Alle Tests weiterhin grün**
3. **Performance nicht verschlechtert**
4. **Datenbank bleibt bei 74 Original-Kunden**

## 📊 Erwartete Ergebnisse

| Metrik | Vorher | Nachher |
|--------|--------|---------|
| Kunden nach Test | +10-20 | ±0 |
| Test-Performance | Baseline | ≤ Baseline |
| DB-Größe | Wächst | Konstant |

## 🚀 Nächste Schritte

1. Fix implementieren (Option 1: @TestTransaction)
2. Tests einzeln verifizieren
3. Kompletten Test-Suite-Run
4. DB-Stand prüfen
5. CI/CD Check implementieren