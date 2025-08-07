# 🔧 Flyway Best Practices für FreshPlan

## 🚨 Goldene Regeln

1. **NIEMALS** bereits ausgeführte Migrationen ändern oder löschen!
2. **IMMER** neue Migrationen mit fortlaufenden Nummern erstellen
3. **BACKUP** vor jedem Repair oder Clean

## 📋 Aktuelle Konfiguration

```properties
# application.properties
quarkus.flyway.out-of-order=true          # Erlaubt Migrationen außer der Reihe
quarkus.flyway.baseline-on-migrate=true    # Baseline bei erster Migration
quarkus.flyway.validate-on-migrate=false   # In Dev deaktiviert
```

## 🔍 Problem-Diagnose

### Symptom: "Migration checksum mismatch"
**Ursache:** Eine bereits ausgeführte Migration wurde geändert
**Lösung:** 
```bash
./scripts/flyway-repair.sh
# Wähle Option 3 (Repair)
```

### Symptom: "Missing migration"
**Ursache:** Eine Migration wurde gelöscht oder Branch gewechselt
**Lösung:** Out-of-order ist bereits aktiviert, Repair hilft

### Symptom: "relation does not exist"
**Ursache:** Migrationen in falscher Reihenfolge
**Lösung:** 
```bash
./scripts/check-migrations.sh     # Status prüfen
./scripts/create-migration.sh      # Neue Migration erstellen
```

## 🛠️ Verfügbare Scripts

### 1. Migration Management
```bash
# Neue Migration erstellen (automatische Versionsnummer)
./scripts/create-migration.sh

# Status prüfen
./scripts/check-migrations.sh

# Konflikte beheben
./scripts/fix-migration-conflicts.sh
```

### 2. Flyway Operations
```bash
# Interaktives Repair-Tool
./scripts/flyway-repair.sh

# Optionen:
# 1) Info - Status anzeigen
# 2) Validate - Probleme finden
# 3) Repair - Historie reparieren
# 4) Clean - WARNUNG: Löscht alles!
# 5) Baseline - Für existierende DBs
# 6) Migrate - Migrationen ausführen
```

## 🔄 Workflow bei Problemen

1. **Check Status**
   ```bash
   ./scripts/check-migrations.sh
   ./scripts/flyway-repair.sh  # Option 1 (Info)
   ```

2. **Bei Validierungsfehlern**
   ```bash
   ./scripts/flyway-repair.sh  # Option 2 (Validate)
   ./scripts/flyway-repair.sh  # Option 3 (Repair)
   ```

3. **Bei Abhängigkeitsproblemen**
   ```bash
   ./scripts/analyze-migration-dependencies.sh
   ./scripts/fix-contacts-migration-order.sh
   ```

4. **Nuclear Option (nur Dev!)**
   ```bash
   ./scripts/flyway-repair.sh  # Option 4 (Clean)
   # Löscht ALLES und startet neu
   ```

## 🚀 CI/CD Integration

```yaml
# In .github/workflows/ci.yml
- name: Validate Migrations
  run: |
    cd backend
    ./scripts/check-migrations.sh
    mvn flyway:validate
```

## 💡 Team-Koordination

1. **Vor jedem Commit:**
   - `./scripts/check-migrations.sh` ausführen
   - Keine doppelten Versionsnummern

2. **Bei Merge-Konflikten:**
   - `./scripts/fix-migration-conflicts.sh` nutzen
   - Niemals manuell Nummern vergeben

3. **Claude-Sessions:**
   - Immer Scripts nutzen, nie manuell
   - Bei Problemen: Repair statt Panik

## ⚠️ Warnung

**Production:** 
- KEIN `clean` 
- KEIN `baseline` ohne Backup
- `repair` nur nach genauer Analyse

**Development:**
- `clean` erlaubt für Fresh Start
- `out-of-order` aktiviert für Flexibilität