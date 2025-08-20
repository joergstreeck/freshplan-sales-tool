# 🚨 KRITISCHES PROBLEM: Test-Daten-Explosion

## Problem
**1090 Kunden in der Datenbank, davon 786 Test-Kunden!**

Stand: 15.08.2025, 17:40 Uhr

## Root Cause
1. **Jeder Test-Run fügt neue Daten hinzu**
   - SearchCQRSIntegrationTest: +2 Kunden pro Run
   - HtmlExportCQRSIntegrationTest: +2 Kunden pro Run
   - ContactEventCaptureCQRSIntegrationTest: +1 Kunde pro Run
   - etc.

2. **Keine Bereinigung möglich**
   - Foreign Key Constraints verhindern Löschung
   - Contacts, Opportunities, Interactions referenzieren Customers
   - Kaskadierendes Löschen nicht implementiert

3. **Exponentielles Wachstum**
   - Von 74 auf 1090 Kunden
   - 122 KD-S* (Search Tests)
   - 170 KD-E* (Export Tests)
   - 56 KD-EVT* (Event Tests)

## Auswirkungen
- **Tests schlagen fehl** wegen zu vieler Daten
- **Performance-Probleme** bei Queries
- **Falsche Test-Ergebnisse** durch Daten-Interferenz
- **Speicherplatz-Verschwendung**

## Sofort-Maßnahmen

### 1. Manuelle Bereinigung (VORSICHT!)
```sql
-- Erst abhängige Daten löschen
DELETE FROM contact_interactions WHERE contact_id IN 
  (SELECT id FROM customer_contacts WHERE customer_id IN 
    (SELECT id FROM customers WHERE customer_number LIKE 'KD-S%'));

DELETE FROM customer_contacts WHERE customer_id IN 
  (SELECT id FROM customers WHERE customer_number LIKE 'KD-S%');

DELETE FROM opportunities WHERE customer_id IN 
  (SELECT id FROM customers WHERE customer_number LIKE 'KD-S%');

-- Dann Customers
DELETE FROM customers WHERE customer_number LIKE 'KD-S%';
DELETE FROM customers WHERE customer_number LIKE 'KD-E%';
DELETE FROM customers WHERE customer_number LIKE 'KD-EVT%';
```

### 2. Test-Isolation implementieren

#### Option A: @TestTransaction mit Rollback
```java
@Test
@TestTransaction
void myTest() {
    // Test code
    // Automatic rollback after test
}
```

#### Option B: Eigene Test-Datenbank
```properties
# application-test.properties
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan_test
```

#### Option C: Cleanup in @AfterEach
```java
@AfterEach
@Transactional
void cleanup() {
    // Delete in correct order
    interactionRepository.deleteAll();
    contactRepository.deleteAll();
    opportunityRepository.deleteAll();
    customerRepository.delete("createdBy", "testuser");
}
```

## Langfristige Lösung

### 1. Test Data Builder Pattern
```java
@TestComponent
public class TestDataBuilder {
    private Set<UUID> createdCustomerIds = new HashSet<>();
    
    public Customer createTestCustomer() {
        Customer c = new Customer();
        // ... setup
        customerRepository.persist(c);
        createdCustomerIds.add(c.getId());
        return c;
    }
    
    @AfterEach
    public void cleanup() {
        // Delete in reverse order
        for (UUID id : createdCustomerIds) {
            deleteCustomerCascade(id);
        }
    }
}
```

### 2. Testcontainers für isolierte DB
```java
@QuarkusTest
@TestProfile(TestcontainersProfile.class)
class MyTest {
    // Fresh database for each test class
}
```

### 3. Feature Flag für Test-Modus
```java
if (testMode.isEnabled()) {
    // Use in-memory H2
} else {
    // Use PostgreSQL
}
```

## Lessons Learned

1. **NIEMALS Test-Daten ohne Cleanup-Strategie**
2. **Foreign Keys erfordern kaskadierendes Löschen**
3. **Test-Isolation ist KRITISCH**
4. **Monitoring der Test-DB ist wichtig**

## Sofort-TODO

1. [ ] Datenbank bereinigen (manuell via SQL)
2. [ ] @TestTransaction zu allen Tests hinzufügen
3. [ ] Cleanup-Strategie implementieren
4. [ ] CI/CD Check für Test-Daten-Wachstum

## Metriken

| Metrik | Wert | Ziel |
|--------|------|------|
| Totale Kunden | 1090 | < 100 |
| Test-Kunden | 786 | 0 nach Tests |
| Wachstum pro Test-Run | +5-10 | 0 |
| Performance-Impact | Hoch | Minimal |

## KRITIKALITÄT: 🔴 SEHR HOCH

Dieses Problem erklärt:
- Warum SearchCQRSIntegrationTest fehlschlägt
- Warum HtmlExportCQRSIntegrationTest falsche Daten findet
- Warum Tests immer langsamer werden
- Warum die CI irgendwann kollabieren wird

**MUSS SOFORT BEHOBEN WERDEN!**