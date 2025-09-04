# CI Diagnostic Report - A00 Environment Issues

**Datum:** 2025-08-20
**Problem:** A00 Environment Diagnostics schlägt fehl

## 🚨 Gefundene Probleme

### Problem 1: Flyway Locations enthält alte SEED-Pfade
```
DIAG[CFG-001] flyway.locations = classpath:db/migration,classpath:db/testdata,classpath:db/ci-migrations,classpath:db/callbacks
```

**Root Cause:** Die Flyway-Locations werden zur Build-Zeit oder durch CLI-Parameter überschrieben.

**Gefundene problematische Stelle:**
- `backend/test-ci-exact.sh` Zeile 27: Enthält noch alte Flyway-Locations

### Problem 2: Datenbank startet nicht leer
```
DIAG[DB-001] Startzustand nicht leer: customers=6
```

**Root Cause:** Die lokale Datenbank wurde bereits mit Testdaten befüllt und wird nicht zurückgesetzt.

## ✅ Lösungen

### Fix 1: Entfernen des alten Test-Skripts
```bash
# Das Skript ist veraltet und sollte gelöscht werden
rm backend/test-ci-exact.sh
```

### Fix 2: CI-Profile korrekt verwenden
Die CI sollte IMMER mit dem core-tests Profile laufen:
```bash
./mvnw verify -Pcore-tests \
  -Dquarkus.profile=ci \
  -Dtest.run.id=${{ github.run_id }}
```

### Fix 3: Sicherstellen dass Flyway nur classpath:db/migration verwendet

**application-ci.properties** sollte explizit setzen:
```properties
quarkus.flyway.locations=classpath:db/migration
```

### Fix 4: Database Reset vor Tests

In der CI muss die Datenbank IMMER zurückgesetzt werden:
```sql
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
```

## 📋 Checklist für CI-Fix

- [ ] Altes test-ci-exact.sh entfernen
- [ ] application-ci.properties überprüfen
- [ ] CI Workflow verwendet -Pcore-tests
- [ ] Keine CLI-Overrides für flyway.locations
- [ ] Schema-Reset ist aktiviert

## 🔍 Verifikation

Nach den Fixes sollte A00_EnvDiagTest folgendes zeigen:
```
DIAG[CFG] flyway.locations=classpath:db/migration
DIAG[DB] customers at start: 0
✅ A00 Environment Check: Alle kritischen Validierungen bestanden
```