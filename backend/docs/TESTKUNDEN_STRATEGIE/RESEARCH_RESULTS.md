# 🔍 Recherche-Ergebnisse: Vollständige Inventarisierung

**Datum:** 2025-08-19  
**Zweck:** Komplette Listen für Phasen-Dokumente (keine Recherche für Claude nötig)

---

## 📁 SEED-Infrastruktur (zu entfernen in Phase 1)

### SQL-Migrationen:
```
backend/src/test/resources/db/callbacks/afterMigrate__ensure_seed_data.sql
backend/src/test/resources/db/ci-migrations/V9000__fk_cascade_for_tests.sql  
backend/src/test/resources/db/testdata/V10004__cleanup_test_seed.sql
backend/src/test/resources/db/testdata/V10005__test_seed_data.sql
backend/src/test/resources/db/testdata/V10006__deprecated_keep_history.sql
backend/src/test/resources/db/testdata/V10007__seed_protection_trigger.sql
backend/src/test/resources/db/testdata/TEST_V900__stable_seed_data.sql.backup
```

### Java-Code mit SEED-Logik:
```
backend/src/main/java/de/freshplan/domain/customer/repository/CustomerRepository.java
- Zeilen 414-463: deleteAllTestDataExceptSeeds(), deleteAllExceptSeeds()

backend/src/test/java/de/freshplan/test/BaseIntegrationTestWithCleanup.java
- SEED-aware cleanup logic

backend/src/test/java/de/freshplan/test/utils/TestDataCleanup.java  
- SEED-protection in cleanup methods

backend/src/test/java/de/freshplan/test/migrations/SimpleSeedTest.java
- Tests die SEED-Daten erwarten

backend/src/test/java/de/freshplan/test/config/TestDatabaseStrategy.java
- SEED-001 bis SEED-050 Definitionen
```

### CI-Workflow:
```
.github/workflows/backend-ci.yml
- Zeilen 131-134: SEED count check vor Tests
- Zeilen 242-243: SEED count nach Tests  
- Zeilen 252-253: SEED integrity verification
```

---

## 🏗️ Builder/Factory-Infrastruktur (zu verbessern in Phase 2)

### Existierende Builder/Factories:
```
✅ backend/src/test/java/de/freshplan/test/builders/ContactBuilder.java
✅ backend/src/test/java/de/freshplan/test/TestDataBuilder.java (generisch)
✅ backend/src/test/java/de/freshplan/test/builders/ContactTestDataFactory.java
✅ backend/src/test/java/de/freshplan/test/builders/CustomerTestDataFactory.java
✅ backend/src/test/java/de/freshplan/test/builders/OpportunityTestDataFactory.java  
✅ backend/src/test/java/de/freshplan/test/builders/UserTestDataFactory.java
```

### Analyse-Status:
- **6 Builder/Factory-Klassen** vorhanden
- **Mix aus Builder + Factory Pattern** (Konsolidierung nötig)
- **Qualitäts-Check nötig:** Pflichtfelder, isTestData=true, eindeutige IDs

---

## 🧪 Tests mit Migration-Bedarf (zu migrieren in Phase 3)

### GROSSE MIGRATION NÖTIG! **146 Tests total**

### Tests mit Builder/Factory-Injection (bereits gut): **38 Tests ✅**
```
✅ Diese Tests nutzen bereits Builder/Factories und sind größtenteils OK:
- CustomerResourceIntegrationTest.java
- OpportunityResourceIntegrationTest.java  
- CustomerServiceIntegrationTest.java
- CustomerCQRSIntegrationTest.java
- OpportunityServiceIntegrationTest.java
- ContactRepositoryTest.java
- CustomerRepositoryTest.java
- + 31 weitere Tests

(Vollständige Liste: 38 Tests mit @Inject Builder/Factory)
```

### Tests OHNE @TestTransaction: **98 Tests ❌**
```
❌ Diese Tests brauchen @TestTransaction Migration:
- Großteil der 146 Test-Klassen
- Nutzen manuelle Cleanup-Strategien
- Potential für Rollback-Migration
```

### Tests mit problematischen Cleanup-Patterns: **82 Tests ❌**
```
❌ Diese Tests nutzen gefährliche Cleanup-Methoden:
- deleteAll() Aufrufe
- DELETE FROM SQL
- TRUNCATE Statements  
- @BeforeEach/@AfterEach Cleanup
- SEED-Protection-Logik
```

### Base-Classes mit SEED-Dependencies:
```
❌ backend/src/test/java/de/freshplan/test/BaseIntegrationTestWithCleanup.java
❌ backend/src/test/java/de/freshplan/test/utils/BaseIntegrationTest.java  
❌ backend/src/test/java/de/freshplan/test/migrations/SimpleSeedTest.java
❌ backend/src/test/java/de/freshplan/test/config/TestDatabaseStrategy.java
```

---

## ⚙️ CI-Konfiguration (zu vereinfachen in Phase 4)

### Problematische CI-Steps:
```
❌ .github/workflows/backend-ci.yml:
   - Step "Run smoke tests for test data setup" (Zeilen 92-148)
   - Step "Verify test data cleanup" (Zeilen 231-321)  
   - Erwartet exakt 20 SEED-Kunden
   - Komplexe SEED-Validierung
```

### Zu entfernende CI-Parameter:
```
❌ Aktuelle flyway.locations Overrides:
   -Dquarkus.flyway.locations=classpath:db/migration,classpath:db/testdata,classpath:db/ci-migrations,classpath:db/callbacks

✅ Neue vereinfachte Konfiguration:  
   -Dquarkus.flyway.locations=classpath:db/migration
```

---

## 📋 Migrations-Aufwand Zusammenfassung

| Phase | Dateien zu ändern | Aufwand |
|-------|------------------|---------|
| **Phase 1 (Rollback)** | 7 SQL + 4 Java + 1 YAML | 1-2 Stunden |
| **Phase 2 (Builder)** | 6 Builder/Factory Klassen | 2-3 Stunden |  
| **Phase 3 (Tests)** | **146 Tests** (98 ohne @TestTransaction, 82 mit problematischen Cleanup) | **2-3 TAGE!** |
| **Phase 4 (CI)** | 1 YAML Workflow | 1 Stunde |

**Gesamt-Aufwand:** ~3-4 TAGE für vollständigen Rollback (hauptsächlich Test-Migration!)

---

## 🎯 Ready für Phasen-Dokumente

Mit diesen **konkreten Listen** können wir jetzt Phasen-Dokumente erstellen die:
- ✅ **Keine Recherche** für Claude erfordern
- ✅ **Exakte Dateilisten** enthalten  
- ✅ **Copy-Paste Commands** bereitstellen
- ✅ **Klare Validierung** haben

**➡️ Bereit für Erstellung der 4 Phasen-Dokumente**