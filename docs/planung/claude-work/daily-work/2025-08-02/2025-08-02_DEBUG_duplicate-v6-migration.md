# 🔧 DEBUG: Duplicate V6 Migration Problem

**Datum:** 02.08.2025, 18:00
**Problem:** Backend startet nicht - Duplicate V6 Migration
**Status:** 🔄 IN ARBEIT

## 🚨 Fehlermeldung:
```
Caused by: org.flywaydb.core.api.FlywayException: Found more than one migration with version 6
Offenders:
-> V6__create_permission_system_core.sql (SQL)
-> V6__add_expansion_planned_field.sql (SQL)
```

## 📋 Analyse:

### Was ist passiert?
1. Zwei verschiedene Migrationen haben dieselbe Versionsnummer V6
2. Flyway kann nicht entscheiden, welche zuerst ausgeführt werden soll
3. Backend startet nicht, weil Flyway abbricht

### Warum ist das passiert?
- Wahrscheinlich parallele Entwicklung in verschiedenen Branches
- Beide Entwickler haben V6 als nächste freie Nummer verwendet
- Beim Merge wurden beide Dateien behalten

## 🚀 Lösungsschritte:

### Schritt 1: Aktuelle Migrationen analysieren
```bash
# Alle Migrationen anzeigen
ls -la backend/src/main/resources/db/migration/ | grep -E "V[0-9]+__"

# Doppelte Versionen finden
ls backend/src/main/resources/db/migration/V*.sql | sed 's/.*V\([0-9]*\)__.*/\1/' | sort | uniq -d
```

### Schritt 2: Höchste Migrationsnummer finden
```bash
# Höchste verwendete Nummer
ls backend/src/main/resources/db/migration/V*.sql | sed 's/.*V\([0-9]*\)__.*/\1/' | sort -n | tail -1
```

### Schritt 3: Eine der V6 Migrationen umbenennen
```bash
# Entscheidung basierend auf Inhalt:
# - V6__create_permission_system_core.sql -> behalten (wichtiger/größer)
# - V6__add_expansion_planned_field.sql -> umbenennen zu nächster freier Nummer
```

### Schritt 4: Clean Build
```bash
cd backend
./mvnw clean
```

### Schritt 5: Backend neu starten
```bash
./scripts/backend-manager.sh restart
```

## 🎯 Erwartetes Ergebnis:
- Keine doppelten Migrationsnummern
- Backend startet erfolgreich
- Frontend kann sich verbinden

## 📝 Lessons Learned:
1. **Immer** höchste Migrationsnummer prüfen vor neuer Migration
2. **Niemals** Migrationsnummern raten
3. Bei Merge-Konflikten Migrationsnummern anpassen

## 🔗 Siehe auch:
- /docs/DATABASE_MIGRATION_GUIDE.md
- /docs/guides/DEBUG_COOKBOOK.md#migration-conflicts