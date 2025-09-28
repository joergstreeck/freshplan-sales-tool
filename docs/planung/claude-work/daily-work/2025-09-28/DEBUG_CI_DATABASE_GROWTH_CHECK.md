# Debug-Protokoll: Database Growth Check CI-Problem

## 📅 Datum: 28.09.2025
## 🔴 Status: NOCH IMMER ROT

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

---
Stand: 28.09.2025 18:00 - Schema-Reset Ansatz mit deaktivierten Seeds