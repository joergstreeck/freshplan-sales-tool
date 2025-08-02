# 📚 DATABASE_MIGRATION_GUIDE - Pflichtlektüre für alle Entwickler

**Status:** VERBINDLICH ab 02.08.2025
**Zielgruppe:** Alle Entwickler (Menschen & KI)

## 🚨 GOLDENE REGELN - NIEMALS BRECHEN!

### Regel 1: Unveränderlichkeit
**Eine einmal gemergte Migration wird NIEMALS geändert oder gelöscht.**
- ❌ NIEMALS: Bestehende Migrationsdateien editieren
- ❌ NIEMALS: Migrationsdateien löschen oder umbenennen
- ✅ IMMER: Neue Migration für Korrekturen erstellen

### Regel 2: Idempotenz
**Jede Migration muss mehrfach ausführbar sein ohne Fehler.**
```sql
-- ✅ RICHTIG
ALTER TABLE customers ADD COLUMN IF NOT EXISTS new_field VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_customer_new_field ON customers(new_field);

-- ❌ FALSCH
ALTER TABLE customers ADD COLUMN new_field VARCHAR(50);
CREATE INDEX idx_customer_new_field ON customers(new_field);
```

### Regel 3: Dokumentation
**Jede Migration MUSS einen aussagekräftigen Header haben:**
```sql
-- =========================================
-- Migration: V[NR]__[aussagekräftiger_name].sql
-- Autor: [Name/System]
-- Datum: [YYYY-MM-DD]
-- Ticket: [FRESH-XXX]
-- Sprint: [Sprint-Name/Nummer]
-- Zweck: [Klare Beschreibung was und warum]
-- =========================================
```

## 📋 Migration Erstellen - Schritt für Schritt

### 1. Nächste Versionsnummer ermitteln
```bash
ls -la backend/src/main/resources/db/migration/ | grep "^-" | tail -1
# Beispiel: V35__last_migration.sql → Nächste ist V36
```

### 2. Migration-Template verwenden
```bash
# Script nutzen (falls vorhanden)
./scripts/create-migration.sh "add_customer_loyalty_fields"

# Oder manuell
cat > backend/src/main/resources/db/migration/V36__add_customer_loyalty_fields.sql << 'EOF'
-- =========================================
-- Migration: V36__add_customer_loyalty_fields.sql
-- Autor: [Dein Name]
-- Datum: $(date +%Y-%m-%d)
-- Ticket: FRESH-XXX
-- Sprint: Sprint X
-- Zweck: Fügt Loyalty-Felder zu customers hinzu
-- =========================================

-- Neue Spalten mit IF NOT EXISTS
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS loyalty_points INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS loyalty_tier VARCHAR(20);

-- Kommentare für Dokumentation
COMMENT ON COLUMN customers.loyalty_points IS 'Accumulated loyalty points';
COMMENT ON COLUMN customers.loyalty_tier IS 'Current loyalty tier (BRONZE, SILVER, GOLD, PLATINUM)';

-- Index wenn nötig (auch idempotent)
CREATE INDEX IF NOT EXISTS idx_customers_loyalty_tier 
ON customers(loyalty_tier) 
WHERE loyalty_tier IS NOT NULL;
EOF
```

### 3. Lokal testen
```bash
# Backend starten und Migration ausführen lassen
cd backend && ./mvnw quarkus:dev

# Verifizieren
psql -h localhost -U freshplan -d freshplan -c "\d customers" | grep loyalty
```

### 4. Review-Checkliste
- [ ] Header vollständig ausgefüllt?
- [ ] Alle Statements idempotent (IF NOT EXISTS)?
- [ ] Kommentare bei neuen Spalten?
- [ ] Rückwärtskompatibel?
- [ ] Performance-Auswirkungen bedacht?

## 🔍 Häufige Muster

### Spalte hinzufügen
```sql
ALTER TABLE table_name
ADD COLUMN IF NOT EXISTS column_name data_type DEFAULT default_value;

COMMENT ON COLUMN table_name.column_name IS 'Beschreibung';
```

### Index erstellen
```sql
CREATE INDEX IF NOT EXISTS idx_table_column 
ON table_name(column_name);
```

### Constraint hinzufügen
```sql
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'constraint_name'
    ) THEN
        ALTER TABLE table_name 
        ADD CONSTRAINT constraint_name CHECK (condition);
    END IF;
END $$;
```

### Enum-Typ erweitern
```sql
-- Enums sind kompliziert! Neue Werte nur am Ende
ALTER TYPE enum_name ADD VALUE IF NOT EXISTS 'NEW_VALUE';
```

## ⚠️ Anti-Patterns - NIEMALS MACHEN!

### 1. Daten in Migrationen ändern
```sql
-- ❌ FALSCH - Daten-Updates gehören nicht in Schema-Migrationen
UPDATE customers SET status = 'ACTIVE' WHERE status IS NULL;

-- ✅ Richtig - Nur Schema-Änderungen, Defaults für NEUE Daten
ALTER TABLE customers ALTER COLUMN status SET DEFAULT 'ACTIVE';
```

### 2. Abhängigkeiten zwischen Migrationen
```sql
-- ❌ FALSCH - Annahme über andere Migration
-- In V38:
INSERT INTO lookup_table VALUES (...); -- Könnte V37 noch nicht ausgeführt sein!

-- ✅ Richtig - Jede Migration ist unabhängig
-- Nutze IF EXISTS checks
```

### 3. Destruktive Operationen ohne Sicherung
```sql
-- ❌ FALSCH
DROP TABLE old_table;

-- ✅ Richtig - Erst deprecaten, später löschen
ALTER TABLE old_table RENAME TO old_table_deprecated_v36;
-- In späteren Sprint: DROP TABLE IF EXISTS old_table_deprecated_v36;
```

### 4. Entity-Felder ohne Migration hinzufügen
```java
// ❌ FALSCH - Neue Felder in Entity ohne Migration
@Entity
public class Customer {
    private String newField; // BOOM! Schema-Validation Error
}

// ✅ Richtig - ERST Migration, DANN Entity
// 1. Migration V36__add_new_field.sql erstellen
// 2. Backend testen (startet es?)
// 3. DANN erst Entity anpassen
```

### 5. JSONB Felder ohne Default
```sql
-- ❌ FALSCH - JSONB ohne Default
ALTER TABLE customers ADD COLUMN pain_points jsonb;

-- ✅ Richtig - JSONB mit leerem Array als Default
ALTER TABLE customers 
ADD COLUMN IF NOT EXISTS pain_points jsonb DEFAULT '[]'::jsonb;
```

## 🚀 Notfall-Prozeduren

### Problem: Migration fehlgeschlagen
```bash
# 1. Status prüfen
cd backend && ./mvnw flyway:info

# 2. Bei Bedarf reparieren
./mvnw flyway:repair

# 3. Neu versuchen
./mvnw flyway:migrate
```

### Problem: Lokale DB ist kaputt
```bash
# Nuclear Option - Alles neu
./scripts/reset-database.sh

# Oder manuell
docker-compose down -v
docker-compose up -d postgres
cd backend && ./mvnw flyway:migrate
```

### Problem: Schema-Validation Error beim Backend-Start
```bash
# Symptom: Backend startet nicht mit Fehler wie:
# "Schema-validation: missing column [field_name]"

# 1. Fehlende Felder identifizieren
cd backend && ./mvnw quarkus:dev 2>&1 | grep "missing column"

# 2. Entity prüfen
grep -n "field_name" src/main/java/de/freshplan/domain/*/entity/*.java

# 3. Migration erstellen
cat > src/main/resources/db/migration/VXX__add_missing_fields.sql << 'EOF'
-- Header nicht vergessen!
ALTER TABLE table_name
ADD COLUMN IF NOT EXISTS field_name VARCHAR(50);
EOF

# 4. Backend neu starten und testen
./mvnw quarkus:dev
```

### Problem: JSONB Spalte macht Probleme
```sql
-- Häufiger Fehler: JSONB ohne cast
-- ❌ FALSCH
ALTER TABLE customers ADD COLUMN data jsonb DEFAULT '{}';

-- ✅ RICHTIG - mit explizitem Cast
ALTER TABLE customers 
ADD COLUMN IF NOT EXISTS data jsonb DEFAULT '{}'::jsonb;

-- Für Arrays
ALTER TABLE customers 
ADD COLUMN IF NOT EXISTS tags jsonb DEFAULT '[]'::jsonb;
```

## 📚 Weiterführende Dokumentation

- [Flyway Best Practices](https://flywaydb.org/documentation/bestpractices)
- [PostgreSQL ALTER TABLE Docs](https://www.postgresql.org/docs/current/sql-altertable.html)
- `/backend/FLYWAY_BEST_PRACTICES.md` (projektspezifisch)

## 🤝 Für neue Teammitglieder (inkl. Claude)

Wenn du neu im Team bist:
1. **LIES DIESES DOKUMENT KOMPLETT**
2. Schaue dir die letzten 5 Migrationen an als Beispiele
3. Bei Unsicherheit: Frage lieber einmal zu viel
4. Teste IMMER lokal bevor du committest

## 🔄 Dieses Dokument lebt!

Letzte Aktualisierung: 02.08.2025 (16:45)
Nächste Review: Sprint-Ende
Von: Claude (TODO-022 abgeschlossen)

### 📝 Für Claude: Automatische Dokumentations-Updates

**WICHTIG für Claude-Sessions:** Wenn du an Migrationen arbeitest:

1. **TODO erstellen**: Füge IMMER einen TODO hinzu:
   ```
   "DATABASE_MIGRATION_GUIDE.md mit neuen Erkenntnissen aktualisieren"
   ```

2. **Während der Arbeit**: Notiere dir:
   - Neue Patterns die du entdeckst
   - Probleme und deren Lösungen
   - Nützliche SQL-Snippets

3. **Vor Übergabe**: Aktualisiere diese Guide mit:
   - Neuen Beispielen aus deiner Session
   - Gelernten Lektionen
   - Verbesserten Erklärungen

### 📊 Update-Log (von Claude für Claude)

#### 02.08.2025 - Schema-Validation Problem
- **Problem**: Sprint 2 Felder ohne Migration
- **Lösung**: V35 Migration mit location-Feldern
- **Lektion**: Entity-Änderungen IMMER mit Migration!

#### 02.08.2025 - Sprint 2 Migration Komplett-Fix
- **Problem**: Backend startete nicht - Schema-Validation Fehler für mehrere Sprint 2 Felder
  - locations_austria, locations_switzerland, locations_germany, locations_rest_eu
  - total_locations_eu
  - pain_points (JSONB)
  - primary_financing
- **Lösung**: Drei separate Migrationen erstellt:
  - V35: Alle location-Felder (INTEGER)
  - V36: pain_points als JSONB mit Default '[]'
  - V37: primary_financing (VARCHAR)
- **Lektion**: 
  - Bei Schema-Validierungsfehlern IMMER alle Felder in Entity prüfen
  - JSONB Felder brauchen Default-Wert und ::jsonb Cast
  - Schrittweise vorgehen - eine Migration pro logischer Einheit
- **Code-Beispiel**:
```sql
-- JSONB mit korrektem Default
ALTER TABLE customers 
ADD COLUMN IF NOT EXISTS pain_points jsonb DEFAULT '[]'::jsonb;

-- Mehrere Spalten in einer Migration (wenn zusammengehörig)
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS locations_germany INTEGER,
ADD COLUMN IF NOT EXISTS locations_austria INTEGER,
ADD COLUMN IF NOT EXISTS locations_switzerland INTEGER;
```

#### 02.08.2025 - Flyway Migration Chaos Aufräumen
- **Problem**: Viele alte Migrationen (V6-V119) wurden gelöscht/verschoben
- **Lösung**: Backup-Ordner erstellt, pragmatischer Fix für akutes Problem
- **Lektion**: Migration-History ist heilig - niemals alte Migrationen löschen!
- **TODO für Team**: Komplette Migration-Strategie überarbeiten

[Neue Einträge hier hinzufügen]

Verbesserungsvorschläge? → PR erstellen!