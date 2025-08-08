# 🚀 Datenbank-Stabilisierungs-Plan

## Status: 06.08.2025

### ✅ Aktueller Zustand
- Backend: UP (Port 8080)
- Frontend: UP (Port 5173)  
- PostgreSQL: UP
- Flyway: 29 Migrationen erfolgreich
- Testdaten: 44 Customers vorhanden

### 🔧 Sofortmaßnahmen

#### 1. Flyway Clean für sauberen Neustart
```bash
# Option A: Soft Reset (behält Struktur)
cd backend
./mvnw quarkus:dev -Dquarkus.flyway.repair-at-start=true

# Option B: Hard Reset (kompletter Neuaufbau)
./mvnw quarkus:dev -Dquarkus.flyway.clean-at-start=true
```

#### 2. Dummy Migrations entfernen
```bash
# Lösche V6-V9 Dummy Files (nur lokal)
rm src/main/resources/db/migration/V6__dummy_migration.sql
rm src/main/resources/db/migration/V7__dummy_migration.sql
rm src/main/resources/db/migration/V8__dummy_migration.sql
rm src/main/resources/db/migration/V9__dummy_migration.sql

# Starte Backend neu mit Clean
./mvnw quarkus:dev -Dquarkus.flyway.clean-at-start=true
```

#### 3. CustomerSearch API Fix
Das Problem liegt in `CustomerSearchResource.java`:
- `CustomerSearchRequest` wird nicht korrekt erstellt
- POST Body wird erwartet, aber Query Parameter werden gesendet

**Quick Fix:**
```java
// In CustomerSearchResource.searchCustomers()
if (request == null) {
    request = new CustomerSearchRequest();
}
if (query != null) {
    request.setGlobalSearch(query);
}
```

### 📋 Migrations-Bereinigung

#### Aktuelle Situation:
- **main Branch:** V1-V5, V10-V17, V35-V37, V100-V109 (25 Files)
- **Lokal:** + V6-V9 Dummies (29 Files)

#### Empfohlene Struktur:
```
V1-V5:    Initial Setup ✅
V10-V17:  Schema Fixes ✅
V35-V37:  Sprint 2 ✅
V100-V109: Extended Features ✅
V200+:    Neue Features (ab jetzt)
```

### 🎯 Langfristige Stabilität

1. **Versionierungs-Strategie:**
   - Neue Migrations ab V200 beginnen
   - Keine Lücken mehr erstellen
   - Semantische Versionierung: V{Sprint}_{Feature}

2. **Test-Daten Management:**
   ```bash
   # Separate Test-Daten Migration
   V999__test_data.sql  # Nur in Dev/Test
   ```

3. **CI/CD Integration:**
   - Flyway validate in CI Pipeline
   - Automatische Migration Tests
   - Rollback-Strategie definieren

4. **Monitoring:**
   - Health-Check für DB-Schema
   - Migration-History tracking
   - Alerting bei fehlgeschlagenen Migrations

### 🔍 Verification Checklist

- [ ] Alle Migrations in main sind dokumentiert
- [ ] Keine Dummy-Files in Production
- [ ] CustomerSearch API funktioniert
- [ ] Testdaten vollständig geladen
- [ ] Frontend kann Daten abrufen
- [ ] Keine Flyway Validation Errors

### 💡 Best Practices

1. **Immer mit Clean starten bei Problemen:**
   ```bash
   ./mvnw quarkus:dev -Dquarkus.flyway.clean-at-start=true
   ```

2. **Migration testen vor Commit:**
   ```bash
   ./mvnw flyway:validate
   ./mvnw flyway:info
   ```

3. **Backup vor großen Änderungen:**
   ```bash
   pg_dump freshplan > backup_$(date +%Y%m%d).sql
   ```

### 📞 Support-Kontakte

Bei Problemen mit:
- **Flyway:** Check logs in `logs/backend.log`
- **PostgreSQL:** `pg_isready -h localhost -p 5432`
- **Diagnose:** `./scripts/diagnose-problems.sh`

---
Erstellt von Claude am 06.08.2025