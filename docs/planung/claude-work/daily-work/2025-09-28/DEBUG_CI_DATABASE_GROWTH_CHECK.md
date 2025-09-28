# Debug-Protokoll: Database Growth Check CI-Problem

## 📅 Datum: 28.09.2025
## 🟢 Status: ERFOLGREICH GELÖST!

## Problem-Zusammenfassung
Der `database-growth-check.yml` Workflow schlägt fehl mit:
- ❌ `FATAL: database "freshplan" does not exist`
- ❌ `ERROR: flyway_schema_history table not found - migrations did not run!`
- ❌ `ERROR: customers table not found!`
- ❌ Test `A00_EnvDiagTest.verifyEnvironment` schlägt fehl

## Debug-Versuche Chronologie

### 1. Initial-Problem: CI Timeout (6+ Stunden)
**Problem:** CI hing bei Migration V248 mit CONCURRENTLY
**Lösung:**
- CONCURRENTLY aus Migrations entfernt
- V248 von Java zu SQL konvertiert
- Filesystem statt Classpath für Flyway

### 2. Test-Seeds Problem
**Problem:** Falsche Enum-Werte (ACTIVE statt AKTIV), fehlende Spalten
**Lösungen:**
- R__test_seeds.sql entfernt (Repeatable migrations liefen zu oft)
- V10005__seed_sample_customers.sql korrigiert
- CustomerStatus auf deutsche Werte (AKTIV) geändert

### 3. Steps werden nicht ausgeführt
**Symptom:** Keine Debug-Echo-Ausgaben im Log sichtbar
**Debugging:**
- Umfangreiche Debug-Ausgaben hinzugefügt
- GitHub Actions Debug-Mode aktiviert (ACTIONS_RUNNER_DEBUG: true)
- Test-Workflows erstellt:
  - `test-db-minimal.yml` ✅ (funktionierte!)
  - `database-growth-check-debug.yml` ✅ (funktionierte!)

### 4. Root Cause #1: Branch-Pattern
**Vermutung:** Workflow triggert nicht auf `feat/*` Branches
**Test:** Branch-Pattern erweitert auf `[main, develop, feature/*, feat/*]`
**Ergebnis:** Nicht das Problem - Workflow lief schon

### 5. Root Cause #2: Cache vor Checkout
**GEFUNDEN:** Cache-Step verwendete `hashFiles('backend/pom.xml')` VOR Checkout!
**Lösung:**
```yaml
# Vorher (FALSCH):
- name: Cache Maven dependencies  # Ohne Checkout!
  key: ${{ hashFiles('backend/pom.xml') }}  # Datei existiert nicht!

# Nachher (RICHTIG):
- name: Checkout code
- name: Cache Maven dependencies  # Nach Checkout!
```
**Ergebnis:** Steps laufen jetzt, ABER Database wird trotzdem nicht erstellt

### 6. Database Creation schlägt fehl
**Problem:** Database Creation Step wird ausgeführt aber schlägt stumm fehl
**Debug-Versuche:**
1. Erweiterte Wait-Loops (60 Versuche)
2. Force-Drop mit pg_terminate_backend
3. DROP DATABASE WITH (FORCE) für PostgreSQL 13+
4. Explizite Port/Host Parameter überall

### 7. Finale Lösung (AKTUELL)
**Ansatz:** Database direkt im Service-Container erstellen
```yaml
services:
  postgres:
    env:
      POSTGRES_DB: freshplan  # Statt postgres
```
**Status:** Gepusht, warten auf Ergebnis

## Erkenntnisse

### Was funktioniert:
- ✅ Minimale Test-Workflows ohne komplexe Steps
- ✅ Debug-Ausgaben erscheinen wenn Workflow läuft
- ✅ PostgreSQL-Service startet korrekt

### Was NICHT funktioniert:
- ❌ Komplexer Database-Drop/Create in separatem Step
- ❌ Cache-Step vor Checkout (Silent Failure!)
- ❌ CONCURRENTLY in Migrations (6h Timeout)

### Offene Fragen:
1. Warum werden manche Steps stumm übersprungen?
2. Warum erscheinen keine Fehler bei `hashFiles()` Failures?
3. Warum funktionieren einfache Workflows aber komplexe nicht?

## Nächste Schritte wenn aktueller Fix fehlschlägt:

1. **Nuclear Option:** Workflow komplett neu schreiben
2. **Alternative:** Backend-CI workflow verwenden (der funktioniert)
3. **Workaround:** Database-Tests in separaten Job auslagern
4. **Debug:** Step-für-Step einzeln testen mit Echo nach jedem

## Wichtige Links
- Failing Run: https://github.com/joergstreeck/freshplan-sales-tool/actions/runs/18075810320
- PR: feat/mod02-backend-sprint-2.1.4

## Lessons Learned
1. **IMMER Checkout vor Cache/Hash-Operations**
2. **NIEMALS CONCURRENTLY in CI-Migrations**
3. **Test-Seeds gehören NICHT in Produktions-Migrationen**
4. **Einfache Debug-Workflows helfen bei Diagnose**
5. **GitHub Actions Silent Failures sind tückisch**

### 8. Zurück zu funktionierendem Ansatz
**Problem:** "relation/column already exists" Fehler
**Analyse:**
- Flyway clean-at-start=true verursacht Doppel-Migrations
- Database Drop/Create funktioniert nicht (Active Connections)
**Lösung:**
- clean-at-start=false
- Schema-Reset mit DROP SCHEMA CASCADE
- V10005 seed migration deaktiviert (.disabled)

### 9. Weitere CI-Fehler nach Schema-Reset
**Symptome:**
- A00_EnvDiagTest erwartet leere DB, findet aber 20 customers
- "Startzustand nicht leer: customers=20"
**Ursache:** V10005__seed_sample_customers.sql erstellt 20 Test-Kunden
**Lösung:** V10005 zu .disabled umbenannt

### 10. YAML-Syntax-Fehler
**Problem:** Workflow failed mit "workflow file issue"
**Ursachen gefunden:**
1. Kommentar zwischen Steps ohne Step-Name (ungültig)
2. Heredoc-Syntax (`<<EOF`) problematisch in GitHub Actions
**Lösungen:**
- Kommentare entfernt
- Heredoc durch einzelne `psql -c` Befehle ersetzt

### 11. Aktueller Stand - Vereinfachter Ansatz
**Setup:**
```yaml
# 1. Service Container mit POSTGRES_DB: freshplan
services:
  postgres:
    env:
      POSTGRES_DB: freshplan

# 2. Schema-Reset (kein DB-Drop!)
psql -c "DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public;"
psql -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"

# 3. application-ci.properties
quarkus.flyway.clean-at-start=false  # WICHTIG!
quarkus.flyway.migrate-at-start=true
```

**Commits heute:**
- `d56c5bf3d` - Restore working database-growth-check from green run
- `150d98b4b` - Resolve database-growth-check failures properly
- `2606a7fe4` - Remove invalid YAML comment between steps
- `5fd8fd0f8` - Replace heredoc with individual psql commands
- `07b641105` - Disable V10005 seed migration
- `c31162f87` - Go back to working approach

### 12. 🎉 ERFOLG - Finale Lösung
**Erfolgreiche Runs:**
- `18076497278` - Nach V10005 Deaktivierung
- `18076536417` - Mit finalem Setup

**Funktionierende Konfiguration:**
1. **POSTGRES_DB: freshplan** im Service Container
2. **Schema-Reset** mit DROP SCHEMA CASCADE (kein DB-Drop!)
3. **V10005 deaktiviert** (.disabled Extension)
4. **clean-at-start=false** (verhindert Doppel-Migrations)
5. **Einfache psql -c Befehle** (kein Heredoc)

## Zusammenfassung der Lösung

Die Kombination folgender Faktoren führte zum Erfolg:
- ✅ Rückkehr zum simplen Ansatz (wie im grünen Run 753c9272)
- ✅ Deaktivierung problematischer Seed-Migration V10005
- ✅ Kein Flyway clean-at-start (verursacht Konflikte)
- ✅ Schema-Reset statt Database-Drop
- ✅ Korrektur aller YAML-Syntax-Fehler

**Total Debug-Zeit:** ~4 Stunden
**Commits zur Lösung:** 7
**Hauptproblem:** Überkomplizierung durch zu viel Debugging

---
Stand: 28.09.2025 18:10 - ✅ PROBLEM GELÖST - Database Growth Check läuft wieder!