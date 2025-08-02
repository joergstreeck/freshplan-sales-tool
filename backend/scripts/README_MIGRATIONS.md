# 🔧 Migration Management Scripts

## Übersicht

Diese Scripts helfen bei der Verwaltung von Flyway-Migrationen und verhindern häufige Fehler wie doppelte Versionsnummern oder falsche Reihenfolgen.

## Scripts

### 1. `create-migration.sh`
**Zweck:** Erstellt neue Migrationen mit automatisch vergebener Versionsnummer

**Verwendung:**
```bash
./scripts/create-migration.sh
```

**Features:**
- Findet automatisch die nächste freie Versionsnummer
- Erstellt Migration mit korrektem Template
- Verhindert Versionskonflikte
- Öffnet Datei optional im Editor

### 2. `check-migrations.sh`
**Zweck:** Überprüft alle Migrationen auf Probleme

**Verwendung:**
```bash
./scripts/check-migrations.sh
```

**Prüft auf:**
- Doppelte Versionsnummern
- Lücken in der Nummerierung
- TODO-Kommentare
- DROP Statements
- Zeigt Statistiken und Übersicht

### 3. `fix-migration-conflicts.sh`
**Zweck:** Behebt automatisch Versionskonflikte

**Verwendung:**
```bash
./scripts/fix-migration-conflicts.sh
```

**Features:**
- Erstellt Backup vor Änderungen
- Nummeriert konflikthafte Migrationen automatisch um
- Aktualisiert Versionsnummern in den Dateien

## Best Practices

1. **IMMER** `check-migrations.sh` vor einem Commit ausführen
2. **NIEMALS** manuell Versionsnummern vergeben - nutze `create-migration.sh`
3. Bei Merge-Konflikten: `fix-migration-conflicts.sh` ausführen
4. Migrations sollten immer rückwärtskompatibel sein (kein DROP in Production)

## Häufige Probleme

### "relation does not exist"
**Ursache:** Migration referenziert Tabelle, die erst in späterer Version erstellt wird
**Lösung:** 
1. `check-migrations.sh` ausführen
2. Prüfe Reihenfolge der CREATE TABLE Statements
3. Nutze `fix-migration-conflicts.sh` wenn nötig

### Testcontainer behält alte Migrationen
**Lösung:**
```bash
# Container neustarten
docker ps | grep postgres | awk '{print $1}' | xargs docker stop
# Oder in Test: @TestProfile("clean") verwenden
```

## Integration in CI/CD

Diese Scripts sollten in der CI-Pipeline laufen:
```yaml
- name: Check Migrations
  run: ./backend/scripts/check-migrations.sh
```