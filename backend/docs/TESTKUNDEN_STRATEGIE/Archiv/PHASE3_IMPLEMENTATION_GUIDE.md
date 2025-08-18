# 🚀 PHASE 3 IMPLEMENTATION GUIDE
## Komplette Umsetzungsanleitung nach Team-Feedback

**Version:** 1.0  
**Status:** READY FOR IMPLEMENTATION  
**Datum:** 17.08.2025  
**Autor:** Team + Claude  

---

## 📋 Quick Navigation

- [1. Start-Checkliste](#1-start-checkliste)
- [2. Pilot-Test Anleitung](#2-pilot-test-anleitung)
- [3. Batch-Migration Guide](#3-batch-migration-guide)
- [4. Manual Migration Guide](#4-manual-migration-guide)
- [5. Validierung & Abschluss](#5-validierung--abschluss)

---

## 1. START-CHECKLISTE

### ✅ Vor dem Start sicherstellen:

```bash
# 1. Aktueller Branch
git status
# Sollte sein: feature/refactor-large-services

# 2. Scripts vorhanden und executable
ls -la scripts/audit-testdata-v2.sh
ls -la scripts/migrate-batch-lowrisk.sh

# 3. TestDataBuilder verfügbar
ls -la src/main/java/de/freshplan/test/TestDataBuilder.java

# 4. Baseline-Audit durchführen
./scripts/audit-testdata-v2.sh
# Dokumentiere aktuelle Zahlen!

# 5. Backup erstellen
git add -A && git commit -m "chore: Backup before Phase 3 migration"
```

---

## 2. PILOT-TEST ANLEITUNG

### 🎯 Ziel: Prozess validieren mit 1 Test

### Schritt 1: Test auswählen und prüfen

```bash
# ContactCommandServiceTest hat 1 Vorkommen
grep -n "new Customer()" \
  src/test/java/de/freshplan/domain/customer/service/command/ContactCommandServiceTest.java
```

### Schritt 2: Datei öffnen und analysieren

```java
// VORHER:
@Test
void someTest() {
    Customer customer = new Customer();
    customer.setCompanyName("Test");
    // ...
}
```

### Schritt 3: Migration durchführen

```java
// NACHHER (Unit-Test - KEIN @TestTransaction):
@Inject TestDataBuilder testDataBuilder;

@Test
void someTest() {
    Customer customer = testDataBuilder.customer()
        .withCompanyName("Test")
        .build();  // ← build() für Unit-Test!
    // ...
}
```

### Schritt 4: Test ausführen

```bash
./mvnw test -Dtest=ContactCommandServiceTest
```

### Schritt 5: Commit bei Erfolg

```bash
git add -p  # Review changes
git commit -m "test: Migrate ContactCommandServiceTest to TestDataBuilder (Pilot)"
```

### ✅ Pilot-Learnings dokumentieren:

```markdown
## Pilot Test Learnings:
- [ ] Import von TestDataBuilder nötig?
- [ ] @Inject funktioniert?
- [ ] build() vs persist() klar?
- [ ] Test grün?
- [ ] Unerwartete Probleme?
```

---

## 3. BATCH-MIGRATION GUIDE

### 🎯 Ziel: 25 Low-Risk Tests automatisiert migrieren

### Option A: Automatisiertes Script

```bash
# Script verwenden
./scripts/migrate-batch-lowrisk.sh

# Erwartete Ausgabe:
# ✅ Migrated: X
# ❌ Failed: Y
# ⏭️ Skipped: Z
```

### Option B: Semi-Automatisch (empfohlen für Kontrolle)

```bash
# 1. Liste erstellen
rg -l '@TestTransaction' src/test/java | \
  xargs -I {} sh -c 'grep -l "new Customer()" {} || true' > low-risk-tests.txt

# 2. Pro Datei migrieren (5er Batches)
head -5 low-risk-tests.txt | while read file; do
    echo "=== Migrating: $file ==="
    
    # Backup
    cp "$file" "$file.backup"
    
    # Replace (Integration-Test → persist())
    sed -i 's/new Customer()/testDataBuilder.customer().persist()/g' "$file"
    
    # Add imports if needed
    # ... (siehe migrate-batch-lowrisk.sh)
    
    # Test
    TEST=$(basename "$file" .java)
    if ./mvnw test -Dtest="$TEST" -q; then
        echo "✅ $TEST passed"
        rm "$file.backup"
    else
        echo "❌ $TEST failed - reverting"
        mv "$file.backup" "$file"
    fi
done
```

### Nach jedem Batch:

```bash
# 1. Audit durchführen
./scripts/audit-testdata-v2.sh

# 2. Commit
git add -A
git commit -m "test: Migrate batch of Low-Risk tests to TestDataBuilder"

# 3. Fortschritt dokumentieren
echo "Batch 1: 5/25 done" >> migration-progress.log
```

---

## 4. MANUAL MIGRATION GUIDE

### 🎯 Ziel: 5 High-Risk Dateien manuell migrieren

### 4.1 CustomerMapperTest (9 Vorkommen)

```java
// Besonderheit: Unit-Test, braucht KEIN @TestTransaction
// Alle 9 Stellen: new Customer() → builder.customer().build()

@QuarkusTest
class CustomerMapperTest {
    @Inject TestDataBuilder testDataBuilder;  // NEU
    
    @Test
    void testMapping() {
        // ALT: Customer customer = new Customer();
        // NEU:
        Customer customer = testDataBuilder.customer()
            .withCompanyName("Test GmbH")
            .withStatus(CustomerStatus.AKTIV)
            .build();  // ← build() nicht persist()!
    }
}
```

### 4.2 TestFixtures (Facade-Pattern)

```java
@Deprecated(since = "2.0", forRemoval = true)
@ApplicationScoped
public class TestFixtures {
    
    @Inject TestDataBuilder builder;  // NEU
    
    private Customer customer;  // ALT: = new Customer()
    
    @PostConstruct
    void init() {
        // NEU: Lazy initialization via Builder
        this.customer = builder.customer().build();
    }
    
    @Deprecated
    public Customer createCustomer() {
        return builder.customer().build();
    }
}
```

### 4.3 Performance-Tests

```java
// Besonderheit: Brauchen viele Testdaten
@Test
void performanceTest() {
    // Batch-Erstellung
    List<Customer> customers = IntStream.range(0, 1000)
        .mapToObj(i -> testDataBuilder.customer()
            .withCompanyName("Perf-Test-" + i)
            .build())  // Nur im Memory für Performance
        .collect(Collectors.toList());
}
```

### Nach jeder manuellen Migration:

```bash
# 1. Einzeltest
./mvnw test -Dtest=CustomerMapperTest

# 2. Review mit Team-Mitglied
git diff src/test/java/.../CustomerMapperTest.java

# 3. Commit
git commit -m "test: Manually migrate CustomerMapperTest (9 occurrences)"
```

---

## 5. VALIDIERUNG & ABSCHLUSS

### 🎯 Final Checks

### 5.1 Alle Audits grün

```bash
# Finaler Audit
./scripts/audit-testdata-v2.sh

# Erwartetes Ergebnis:
# ✅ new Customer() → 0 (außer TestDataBuilder selbst)
# ✅ persist() außerhalb Builder → 0
# ✅ Mockito eq() → 0
# ⚠️ TestFixtures → OK während Übergang
```

### 5.2 CI-Simulation

```bash
# CI-Umgebung simulieren
export CI=true
./scripts/audit-testdata-v2.sh
# SEED-Count muss exakt 20 sein!

# Alle Tests laufen lassen
./mvnw clean test
```

### 5.3 Performance-Vergleich

```bash
# Vorher (aus Logs oder Backup)
# Total time: 12:34 min

# Nachher
time ./mvnw test
# Sollte >30% schneller sein
```

### 5.4 ArchUnit-Regel aktivieren

```java
// src/test/java/de/freshplan/arch/TestDataBuilderEnforcementTest.java
// Implementieren und testen
./mvnw test -Dtest=TestDataBuilderEnforcementTest
```

### 5.5 Finaler Commit

```bash
# Alle Änderungen reviewen
git diff --stat

# Finaler Commit
git add -A
git commit -m "feat: Complete Phase 3 - Migrate all tests to TestDataBuilder

- Migrated 69 occurrences in 37 files
- Implemented 4-tier safety net
- TestFixtures now uses Facade pattern
- All audits passing
- Performance improved by X%

Closes #TICKET"
```

---

## 📊 TRACKING TEMPLATE

### Fortschritts-Tracking:

```markdown
## Phase 3 Migration Progress

### Pilot ✅ ABGESCHLOSSEN (17.08.2025 20:27)
- [x] ContactCommandServiceTest - ERFOLGREICH
  - Commit: 27e19e6e7
  - Strategie: CustomerBuilder direkt instanziiert
  - Methode: .build() für Unit-Test

### Batch Low-Risk (27)
- [ ] Batch 1 (5 Tests)
- [ ] Batch 2 (5 Tests)
- [ ] Batch 3 (5 Tests)
- [ ] Batch 4 (5 Tests)
- [ ] Batch 5 (7 Tests)

### Manual High-Risk (5)
- [ ] CustomerMapperTest (9)
- [ ] TestFixtures (1)
- [ ] OpportunityTestHelper (1)
- [ ] Performance Tests
- [ ] E2E Base Classes

### Validation
- [ ] audit-testdata-v2.sh → 0 errors
- [ ] CI simulation → green
- [ ] Performance → >30% faster
- [ ] ArchUnit → active
```

---

## ⚠️ TROUBLESHOOTING

### Problem: Test fails after migration

```bash
# 1. Check @TestTransaction
grep "@TestTransaction" failing-test.java

# 2. Check build() vs persist()
# Unit-Test? → build()
# Integration-Test? → persist() + @TestTransaction

# 3. Check imports
grep "import.*TestDataBuilder" failing-test.java
```

### Problem: "Cannot inject TestDataBuilder"

```java
// Lösung 1: @QuarkusTest hinzufügen
@QuarkusTest  // ← WICHTIG für CDI
class MyTest {
    @Inject TestDataBuilder testDataBuilder;
}

// Lösung 2: Manuell instanziieren für reine Unit-Tests
TestDataBuilder builder = new TestDataBuilder();
```

### Problem: SEED-Count nicht 20

```sql
-- Check what's there
SELECT customer_number, company_name 
FROM customers 
WHERE is_test_data = true 
  AND customer_number LIKE 'SEED-%'
ORDER BY customer_number;

-- Clean if needed (NUR in Test-DB!)
DELETE FROM customers 
WHERE customer_number LIKE 'SEED-%' 
  AND customer_number NOT IN ('SEED-001',...,'SEED-020');
```

---

**END OF IMPLEMENTATION GUIDE** 🎉

Bei Fragen: Team-Channel oder Code-Review anfordern!