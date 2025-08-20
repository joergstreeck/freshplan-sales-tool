# Phase 1: SEED-Infrastruktur komplett entfernen

**Aufwand:** 30 Minuten  
**Ziel:** Komplette Vereinfachung durch Entfernung aller SEED-Dependencies  
**Ergebnis:** Sauberer Flyway-Setup nur mit Schema-Migrationen

---

## 🎯 Überblick

Diese Phase entfernt die gesamte SEED-Infrastruktur, die für Solo-Development over-engineered ist:
- 7 SQL-Dateien löschen
- SEED-Protection-Code aus 4 Java-Klassen entfernen  
- CI-Workflow von SEED-Validierung befreien
- Flyway-Konfiguration vereinfachen

**📋 Verweis auf Recherche:** Siehe [RESEARCH_RESULTS.md - SEED-Infrastruktur](./RESEARCH_RESULTS.md#📁-seed-infrastruktur-zu-entfernen-in-phase-1)

---

## 📁 Schritt 1: SQL-Dateien löschen

### Zu löschende Dateien (Copy-Paste Liste):
```bash
rm "backend/src/test/resources/db/callbacks/afterMigrate__ensure_seed_data.sql"
rm "backend/src/test/resources/db/ci-migrations/V9000__fk_cascade_for_tests.sql"
rm "backend/src/test/resources/db/testdata/V10004__cleanup_test_seed.sql"  
rm "backend/src/test/resources/db/testdata/V10005__test_seed_data.sql"
rm "backend/src/test/resources/db/testdata/V10006__deprecated_keep_history.sql"
rm "backend/src/test/resources/db/testdata/V10007__seed_protection_trigger.sql"
rm "backend/src/test/resources/db/testdata/TEST_V900__stable_seed_data.sql.backup"
```

### Validierung:
```bash
# Prüfen dass alle SEED-Dateien weg sind
find backend/src/test/resources/db -name "*seed*" -o -name "*SEED*" | wc -l
# Erwartetes Ergebnis: 0
```

---

## 🔧 Schritt 2: Java-Code bereinigen

### 2.1 CustomerRepository.java - SEED-Protection entfernen

**Datei:** `backend/src/main/java/de/freshplan/domain/customer/repository/CustomerRepository.java`

**Zu entfernende Methoden (Zeilen 414-463):**
```java
// KOMPLETT LÖSCHEN:
public long deleteAllTestDataExceptSeeds() {
    return delete("""
        (isTestData = true 
         OR customerNumber LIKE 'TEST-%')
        AND customerNumber NOT LIKE 'SEED-%'
        AND companyName NOT LIKE '[SEED]%'
        """);
}

public long deleteAllExceptSeeds() {
    return delete("""
        customerNumber NOT LIKE 'SEED-%'
        AND companyName NOT LIKE '[SEED]%'
        """);
}

// Alle anderen SEED-bezogenen Methoden auch löschen
```

**Neue einfache Methode (als Ersatz):**
```java
/**
 * Delete all test data. Only use in tests with proper isolation.
 * WARNING: Never call in production!
 */
public long deleteAllTestData() {
    if (!isTestProfile()) {
        throw new IllegalStateException("deleteAllTestData() only allowed in test profile");
    }
    return delete("isTestData = true OR customerNumber LIKE 'TEST-%'");
}

private boolean isTestProfile() {
    return ConfigProvider.getConfig()
        .getOptionalValue("quarkus.profile", String.class)
        .filter(profile -> "test".equals(profile))
        .isPresent();
}
```

### 2.2 BaseIntegrationTestWithCleanup.java - SEED-Awareness entfernen

**Datei:** `backend/src/test/java/de/freshplan/test/BaseIntegrationTestWithCleanup.java`

**Vorher (SEED-aware):**
```java
@AfterEach
void cleanup() {
    // Cleanup but preserve SEED data
    customerRepository.deleteAllTestDataExceptSeeds();
    contactRepository.deleteTestDataExceptSeeds();
    opportunityRepository.deleteTestDataExceptSeeds();
}
```

**Nachher (einfach):**
```java
// KOMPLETT ENTFERNEN - Tests sollen @TestTransaction nutzen
// Falls doch Cleanup nötig, dann nur:
@AfterEach 
void cleanup() {
    // Only for tests that cannot use @TestTransaction
    customerRepository.deleteAllTestData();
    contactRepository.deleteAllTestData();
    opportunityRepository.deleteAllTestData();
}
```

### 2.3 TestDataCleanup.java - SEED-Protection entfernen

**Datei:** `backend/src/test/java/de/freshplan/test/utils/TestDataCleanup.java`

**Zu entfernen:**
```java
// Alle Methoden mit "ExceptSeeds" löschen
public static void cleanupCustomersExceptSeeds() { ... }
public static void cleanupContactsExceptSeeds() { ... }
public static void cleanupOpportunitiesExceptSeeds() { ... }
```

**Vereinfachen zu:**
```java
/**
 * Emergency cleanup for tests that cannot use @TestTransaction.
 * Prefer @TestTransaction over manual cleanup!
 */
public static void cleanupTestData() {
    log.warn("Manual cleanup used - consider @TestTransaction instead");
    customerRepository.deleteAllTestData();
    contactRepository.deleteAllTestData(); 
    opportunityRepository.deleteAllTestData();
}
```

### 2.4 SimpleSeedTest.java - Komplett löschen

**Datei:** `backend/src/test/java/de/freshplan/test/migrations/SimpleSeedTest.java`

```bash
# Datei komplett löschen
rm "backend/src/test/java/de/freshplan/test/migrations/SimpleSeedTest.java"
```

---

## ⚙️ Schritt 3: CI-Workflow bereinigen

### 3.1 GitHub Actions Workflow

**Datei:** `.github/workflows/backend-ci.yml`

**Zu entfernende Abschnitte:**

**Abschnitt 1 - SEED count check (Zeilen ~131-134):**
```yaml
# LÖSCHEN:
- name: Verify SEED customer count before tests
  run: |
    SEED_COUNT=$(PGPASSWORD=postgres psql -h localhost -U postgres -d test_db -t -c "SELECT COUNT(*) FROM customers WHERE customer_number LIKE 'SEED-%'")
    echo "SEED customers found: $SEED_COUNT"
    if [ "$SEED_COUNT" -ne "20" ]; then
      echo "ERROR: Expected 20 SEED customers, found $SEED_COUNT"
      exit 1
    fi
```

**Abschnitt 2 - SEED count after tests (Zeilen ~242-243):**
```yaml
# LÖSCHEN:
- name: Verify SEED customer count after tests  
  run: |
    SEED_COUNT=$(PGPASSWORD=postgres psql -h localhost -U postgres -d test_db -t -c "SELECT COUNT(*) FROM customers WHERE customer_number LIKE 'SEED-%'")
    if [ "$SEED_COUNT" -ne "20" ]; then
      echo "ERROR: SEED customers corrupted by tests"
      exit 1
    fi
```

**Abschnitt 3 - SEED integrity (Zeilen ~252-253):**
```yaml
# LÖSCHEN:
- name: SEED integrity verification
  run: |
    # Complex SEED validation logic
    ...
```

### 3.2 Flyway-Konfiguration vereinfachen

**Vorher (komplex):**
```bash
-Dquarkus.flyway.locations=classpath:db/migration,classpath:db/testdata,classpath:db/ci-migrations,classpath:db/callbacks
```

**Nachher (einfach):**
```bash
-Dquarkus.flyway.locations=classpath:db/migration
```

**In CI-Workflow ersetzen:**
```yaml
# Alle Vorkommen ersetzen
- name: Run tests
  run: ./mvnw test -Dquarkus.flyway.locations=classpath:db/migration
```

---

## ✅ Schritt 4: Validierung

### 4.1 Keine SEED-Referenzen mehr
```bash
# Prüfen dass keine SEED-Referenzen im Code
grep -r "SEED" backend/src/ | grep -v ".git" | wc -l
# Erwartetes Ergebnis: 0 (oder nur Kommentare)
```

### 4.2 Flyway läuft sauber
```bash
cd backend
./mvnw flyway:info
# Erwartetes Ergebnis: Nur Migrationen aus db/migration, keine testdata/ci-migrations
```

### 4.3 Tests laufen ohne SEED-Dependencies
```bash
cd backend  
./mvnw test -Dtest=UserRepoSaveLoadIT
# Erwartetes Ergebnis: Test läuft grün ohne SEED-Daten
```

---

## 🎯 Erfolgskriterien - Definition of Done (messbar)

**Phase 1 ist NUR DANN abgeschlossen, wenn alle Validierungen grün sind:**

### ✅ **Validierung 1: Keine SEED-Dateien mehr**
```bash
find backend/src/test/resources/db -name '*seed*' -o -name '*SEED*' | wc -l
# MUSS ergeben: 0
```

### ✅ **Validierung 2: Flyway History sauber**
```bash
cd backend && ./mvnw flyway:info | grep -c "testdata\|ci-migrations\|callbacks"
# MUSS ergeben: 0 (nur db/migration Pfade)
```

### ✅ **Validierung 3: A00 Environment Check bestätigt 0 Kunden**
```bash
cd backend && ./mvnw test -Dtest=UserRepoSaveLoadIT
# Im Log MUSS stehen: "Schema contains 0 customers at start"
```

### ✅ **Validierung 4: Keine SEED-Code-Referenzen**
```bash
grep -r -i "seed" backend/src/ | grep -v ".git" | grep -v "target/" | wc -l
# SOLLTE ergeben: 0 (außer Kommentare/Dokumentation)
```

### ✅ **Validierung 5: CI ohne SEED-Validierung** 
```bash
grep -c -i "seed" .github/workflows/backend-ci.yml
# MUSS ergeben: 0
```

**💡 Erst wenn ALLE 5 Validierungen erfolgreich sind → Phase 2 starten!**

---

## ➡️ Nächste Phase

Nach erfolgreichem Abschluss von Phase 1:
→ **Phase 2A: Builder Core-Verbesserungen** (PHASE_2A_BUILDER_CORE.md)

**Zeitschätzung:** Diese Phase dauert etwa 30 Minuten und macht sofort alles einfacher!