# Postmortem: Backend-Start & CORS-Probleme - Sprint 2.1

**Datum:** 2025-09-25
**Severity:** SEV-3 (Dev-Environment blockiert)
**Dauer:** ~2 Stunden
**Betroffene Systeme:** Backend (Quarkus), Frontend (Vite)

## Timeline

- **21:30** - Frontend meldet CORS-Fehler, Backend nicht erreichbar auf Port 8080
- **21:45** - Backend-Start schlägt fehl mit Hibernate Validation Error
- **22:00** - Missing ElementCollection Tables identifiziert
- **22:30** - V231 Migration erstellt und angewendet
- **22:45** - CORS-Konfiguration repariert
- **23:00** - System läuft wieder stabil

## Root Cause Analysis

### Problem 1: Fehlende Join-Tabellen
**Ursache:** JPA `@ElementCollection` benötigt Join-Tabellen für `List<String>` Felder
- `Lead.collaboratorUserIds` → `lead_collaborators` Tabelle
- `UserLeadSettings.preferredTerritories` → `user_preferred_territories` Tabelle

**Warum nicht erkannt:** Hibernate DDL-Auto war auf `validate`, nicht `create`

### Problem 2: Dev-Migration Policy-Konflikte
**Ursache:** V8002 versuchte Policies zu erstellen, die bereits existierten
**Fix:** `DROP POLICY IF EXISTS` vor `CREATE POLICY`

### Problem 3: CORS-Blockierung
**Ursache:** Backend lief ohne Dev-Profil, CORS-Headers fehlten
**Fix:** Start mit `-Dquarkus.profile=dev` und expliziten CORS-Settings

### Problem 4: Mehrfach-Instanzen
**Ursache:** Mehrere Backend-Prozesse auf verschiedenen Ports (8080, 8082)
**Fix:** Prozess-Hygiene, alle alten Instanzen beenden

## Was gut funktioniert hat

- Schnelle Identifikation durch klare Fehlermeldungen
- Migration V231 löste das Problem sauber
- Dev/Prod-Trennung verhinderte Production-Impact

## Was verbessert werden muss

### Sofortmaßnahmen (umgesetzt)
1. ✅ V231 Migration für ElementCollection Tables
2. ✅ V8002 mit DROP IF EXISTS gefixed
3. ✅ CORS nur im Dev-Profil aktiv

### Präventionsmaßnahmen
1. **Integration-Test für Migrations**
   ```java
   @Test
   void testAllMigrationsWithValidate() {
       // Start mit hibernate.hbm2ddl.auto=validate
       // Muss ohne Fehler durchlaufen
   }
   ```

2. **Dev-Startup-Script**
   ```bash
   #!/bin/bash
   # kill-and-start.sh
   lsof -ti:8080 | xargs kill -9 2>/dev/null
   ./mvnw quarkus:dev -Dquarkus.profile=dev
   ```

3. **Flyway Locations strikt trennen**
   - Production: nur `db/migration`
   - Development: `db/migration,db/dev-migration`

## Lessons Learned

1. **"Eine Quelle der Wahrheit"**: JPA `@PreUpdate` statt DB-Trigger für `updated_at`
2. **Klare Dev/Prod-Trennung**: CORS, Migrations, Profiles müssen strikt getrennt sein
3. **Prozess-Hygiene**: Nur eine Backend-Instanz, klare Port-Zuordnung
4. **Test vor Merge**: Migrations immer mit `validate` testen

## Action Items

- [ ] Integration-Test für Migration+Validate schreiben
- [ ] Dev-Startup-Script erstellen
- [ ] Flyway-Config in application.properties dokumentieren
- [x] V231 Migration committed und deployed

## Referenzen

- PR #103: Territory Management
- Migration V231: `backend/src/main/resources/db/migration/V231__add_missing_elementcollection_tables.sql`
- Fix in `UserLeadSettingsService`: Thread-safe Implementation