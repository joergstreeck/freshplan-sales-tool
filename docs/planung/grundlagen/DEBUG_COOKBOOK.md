# 🚑 DEBUG COOKBOOK - FreshPlan Sales Tool

**Zweck:** Zentrale Sammlung aller Debug-Lösungen für wiederkehrende Probleme  
**Zielgruppe:** Claude-Sessions und Entwickler  
**Letzte Aktualisierung:** 2025-09-21

---

## 🎯 QUICK NAVIGATION - Symptom-basierte Suche

### Frontend Probleme
- [White Screen ohne Fehlermeldung](#white-screen) 🔴
- [Failed to fetch / Connection refused](#failed-to-fetch) 🟡
- [Vite Dev Server läuft nicht](#vite-issues) 🟡
- [Keycloak Auth hängt](#auth-hang) 🔵
- [TypeScript Import Type Fehler](#typescript-import-type) 🆕
- [MUI Grid v2 Migration Fehler](#mui-grid-v2) 🆕

### Backend Probleme  
- [Backend nicht erreichbar](#backend-down) 🔴
- [401 Unauthorized Fehler](#auth-401) 🔵
- [Keine Testdaten vorhanden](#no-test-data) 🟢
- [Java Version Probleme](#java-version) 🟡

### CI/CD Probleme
- [Integration Tests rot (HTTP 500)](#ci-http-500) 🔴
- [User not found in Tests](#ci-user-missing) 🟡
- [Race Conditions in Tests](#ci-race-conditions) 🟢

### Service Management
- [Port bereits belegt](#port-in-use) 🟢
- [Docker Container Probleme](#docker-issues) 🟡
- [Services starten nicht](#services-wont-start) 🔴

---

## 🏗️ BASICS - Service Grundlagen

➡️ **[Vollständige Service-Übersicht und Start-Befehle](/Users/joergstreeck/freshplan-sales-tool/docs/guides/DEBUG_COOKBOOK_BASICS.md)**

**Quick Info:**
- Frontend: Port `5173` (Vite/React)
- Backend: Port `8080` (Quarkus/Java 17)
- PostgreSQL: Port `5432`
- Keycloak: Port `8180`

**⚠️ Java 17 ist PFLICHT!** Details in [Service Basics](/Users/joergstreeck/freshplan-sales-tool/docs/guides/DEBUG_COOKBOOK_BASICS.md#java-version)

---

## 🔴 FRONTEND PROBLEME

### White Screen {#white-screen}

**Symptome:**
- Browser zeigt nur weiße Seite
- Keine Fehler in Browser-Konsole
- HTML wird geladen aber kein React Content

**Häufigkeit:** Nach Merges, Auth-Änderungen, Vite-Crashes

**Quick Fix:**
```bash
cd frontend && pkill -f vite && npm run dev
```

**Diagnose:**
1. Browser DevTools → Network Tab → main.tsx geladen?
2. Console Tab → JavaScript Fehler?
3. Service Status: `./scripts/check-services.sh`
4. Vite läuft? `lsof -i :5173`

**Lösung:**
```bash
# 1. Vite komplett neu starten
pkill -f node && pkill -f vite

# 2. Mit sauberem Cache
cd frontend
rm -rf node_modules/.vite
npm run dev

# 3. Oder über Script
./scripts/start-dev.sh
```

**Root Causes:**
- Vite Dev Server in "Zombie" Zustand
- Keycloak Auth Initialisierung hängt
- React StrictMode Probleme
- Environment Variables nicht geladen

**Prävention:**
- Immer `start-dev.sh` verwenden
- Nach Merges Services neu starten
- Auth Bypass in Dev aktivieren: `VITE_AUTH_BYPASS=true`

---

### Failed to Fetch {#failed-to-fetch}

**Symptome:**
- "Failed to fetch" Fehlermeldung
- "Connection refused" im Browser
- API Calls schlagen fehl

**Quick Fix:**
```bash
./scripts/backend-manager.sh restart
```

**Diagnose:**
1. Backend läuft? `curl http://localhost:8080/q/health`
2. CORS konfiguriert? Check Network Tab Headers
3. Auth Token vorhanden? localStorage prüfen

**Lösung:**
```bash
# Backend neu starten mit korrekter Java Version
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
cd backend && mvn quarkus:dev
```

---

### Vite Issues {#vite-issues}

**Symptome:**
- Port 5173 antwortet nicht
- "Port already in use" Fehler
- HMR (Hot Module Replacement) funktioniert nicht

**Quick Fix:**
```bash
lsof -ti:5173 | xargs kill -9 && cd frontend && npm run dev
```

**Diagnose:**
```bash
# Wer nutzt Port 5173?
lsof -i :5173

# Vite Prozesse
ps aux | grep vite
```

**Lösung:**
```bash
# Alle Node Prozesse beenden
pkill -f node

# Frontend mit Force neu starten
cd frontend && npm run dev -- --force
```

---

### Auth Hang {#auth-hang}

**Symptome:**
- App lädt endlos
- Weiße Seite nach Keycloak Redirect
- "Initializing..." hängt

**Quick Fix (Development):**
```bash
# Auth Bypass aktivieren
echo "VITE_AUTH_BYPASS=true" >> frontend/.env
cd frontend && npm run dev
```

**Diagnose:**
1. Keycloak erreichbar? `curl http://localhost:8180`
2. Browser Console → Keycloak Errors?
3. Network Tab → Auth Redirects?

**Lösung:**
```bash
# Option 1: Keycloak neu starten
docker restart freshplan-keycloak

# Option 2: Development Bypass
# In frontend/.env:
VITE_AUTH_BYPASS=true

# Frontend neu starten
cd frontend && npm run dev
```

---

### TypeScript Import Type {#typescript-import-type}

**Symptome:**
- "The requested module does not provide an export named 'FieldDefinition'"
- Vite Build Fehler bei Type-Exports
- White Screen nach TypeScript Änderungen

**Häufigkeit:** Bei `verbatimModuleSyntax: true` in tsconfig.json

**Quick Fix:**
```bash
# 1. Import korrigieren
# FALSCH: import { FieldDefinition } from './types';
# RICHTIG: import type { FieldDefinition } from './types';

# 2. Cache löschen und neu starten
cd frontend
rm -rf node_modules/.vite
npm run dev
```

**Diagnose:**
1. Fehler lesen: Welcher Export fehlt?
2. Export prüfen: `export interface FieldDefinition` vorhanden?
3. Import prüfen: `import type` verwendet?
4. Weitere Imports suchen: `grep -r "import {.*FieldDefinition" src/`

**Lösung:**
```typescript
// ✅ RICHTIG - Types direkt exportieren
export interface FieldDefinition { ... }
export type FieldCatalog = { ... }

// ✅ RICHTIG - Type Imports verwenden
import type { FieldDefinition, FieldCatalog } from './types/field.types';

// ✅ RICHTIG - Enums bleiben normale Imports
import { EntityType } from './types/field.types';

// ❌ FALSCH - Keine Re-Exports
type Foo = { ... }
export { Foo }; // NICHT SO!
```

**Root Causes:**
- TypeScript `verbatimModuleSyntax` erfordert explizite Type-Imports
- Vite/ESM Module Resolution ist strikt
- Re-Exports am Dateiende funktionieren nicht für Types

**Prävention:**
- IMMER `export interface` oder `export type` direkt
- IMMER `import type` für Types/Interfaces
- Nach Type-Änderungen Cache löschen
- ESLint Rule: `@typescript-eslint/consistent-type-imports`

**Siehe auch:**
- [TypeScript Import Type Guide](/Users/joergstreeck/freshplan-sales-tool/docs/guides/TYPESCRIPT_IMPORT_TYPE_GUIDE.md)
- [Debug Session vom 27.07.2025](/Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-07-27/2025-07-27_DEBUG_typescript-import-type-marathon.md)
- [Sprint 2 Dokumentation Updates](/Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-07-27/2025-07-27_IMPL_sprint2-docs-typescript-update.md)

---

### MUI Grid v2 Migration Fehler {#mui-grid-v2}

**Symptome:**
- Console Warnung: "MUI Grid: The `item` prop has been removed"
- Console Warnung: "MUI Grid: The `xs` prop has been removed"
- Console Warnung: "MUI Grid: The `sm` prop has been removed"
- Console Warnung: "MUI Grid: The `md` prop has been removed"

**Häufigkeit:** Nach MUI Update auf v7, bei Grid-Components

**Quick Fix:**
```bash
# Grid v1 Syntax zu Grid v2 konvertieren
# VORHER:  <Grid item xs={12} sm={6} md={3}>
# NACHHER: <Grid size={{ xs: 12, sm: 6, md: 3 }}>
```

**Diagnose:**
1. MUI Version prüfen: `npm list @mui/material`
2. Grid Import prüfen: Verwenden wir `Grid` aus `@mui/material`?
3. Alte Syntax suchen: `grep -r "Grid item" src/`
4. Funktionierende Beispiele finden: Andere Grid-Verwendungen im Projekt

**Lösung:**
```typescript
// ✅ RICHTIG - Grid v2 Syntax (MUI v7)
import { Grid } from '@mui/material';

<Grid container spacing={3}>
  <Grid size={{ xs: 12, sm: 6, md: 3 }}>
    <Component />
  </Grid>
  <Grid size="grow">  // Nimmt verfügbaren Platz
    <Component />
  </Grid>
  <Grid size="auto">  // Nimmt nur benötigten Platz
    <Component />
  </Grid>
</Grid>

// ❌ FALSCH - Alte Grid v1 Syntax
<Grid container spacing={3}>
  <Grid item xs={12} sm={6} md={3}>  // item prop entfernt!
    <Component />
  </Grid>
  <Grid item xs>  // xs als boolean nicht mehr erlaubt!
    <Component />
  </Grid>
</Grid>
```

**Migration Pattern:**
```typescript
// Systematische Migration:
// 1. Standard Breakpoints
<Grid item xs={12} sm={6} md={4}>      → <Grid size={{ xs: 12, sm: 6, md: 4 }}>

// 2. Nur xs (volle Breite)
<Grid item xs={12}>                    → <Grid size={{ xs: 12 }}>

// 3. Flex-grow (verfügbarer Platz)
<Grid item xs>                         → <Grid size="grow">

// 4. Auto-sizing
<Grid item>                            → <Grid size="auto">

// 5. Container bleibt gleich
<Grid container spacing={3}>           → <Grid container spacing={3}>
```

**Root Causes:**
- MUI v7 hat Grid v2 als Standard (Breaking Change)
- Grid v2 verwendet `size` prop statt `item` + breakpoints
- Grid2 Component existiert nicht mehr (war nur in v6)
- Import `Unstable_Grid2` funktioniert nicht in v7

**Prävention:**
- Bei MUI Updates Migration Guide beachten
- Konsistente Grid-Syntax im gesamten Projekt
- ESLint Rule für deprecated Props einrichten
- Nach Updates Console auf Warnungen prüfen

**Verwandte Dokumentation:**
- [MUI Grid v2 Migration Guide](https://mui.com/material-ui/migration/upgrade-to-grid-v2/)
- [Lösung vom 09.08.2025](/Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-08-09/SOLUTION_MUI_GRID_V2.md)
- [Debug Plan vom 09.08.2025](/Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-08-09/DEBUG_PLAN_MUI_GRID_V2.md)

---

## 🟡 BACKEND PROBLEME

### Backend Down {#backend-down}

**Symptome:**
- localhost:8080 nicht erreichbar
- "Connection refused" Fehler
- Health Check failed

**Quick Fix:**
```bash
./scripts/backend-manager.sh start
```

**Diagnose:**
```bash
# Java Version prüfen (MUSS 17 sein!)
java -version

# Backend Logs
tail -f logs/backend.log

# Port Check
lsof -i :8080
```

**Lösung:**
```bash
# 1. Java 17 sicherstellen
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 2. Backend persistent starten
cd backend
nohup mvn quarkus:dev > ../logs/backend.log 2>&1 &

# 3. Health Check
sleep 30 && curl http://localhost:8080/q/health
```

**Root Causes:**
- Falsche Java Version (nicht 17)
- Port 8080 bereits belegt
- Memory/CPU Limits erreicht
- PostgreSQL nicht verfügbar

---

### Auth 401 {#auth-401}

**Symptome:**
- 401 Unauthorized in API Calls
- Tests schlagen fehl mit 401
- "Token expired" Meldungen

**Quick Fix (Dev):**
```bash
# In application.properties
echo "%dev.quarkus.oidc.enabled=false" >> backend/src/main/resources/application.properties
```

**Diagnose:**
1. Token im Request? Check Authorization Header
2. Token gültig? JWT debuggen auf jwt.io
3. Keycloak Realm korrekt?

**Lösung für Tests:**
```java
// Test mit Security Annotation
@Test
@TestSecurity(user = "testuser", roles = {"admin", "manager", "sales"})
void testSecuredEndpoint() {
    // Test Code
}
```

---

### No Test Data {#no-test-data}

**Symptome:**
- "Keine Kunden gefunden"
- Leere Listen in UI
- API gibt leere Arrays zurück

**Quick Fix:**
```bash
# Backend neu starten triggert DataInitializer
./scripts/backend-manager.sh restart
```

**Diagnose:**
```bash
# Datenbank prüfen
psql -h localhost -p 5432 -U freshplan -d freshplan -c "SELECT COUNT(*) FROM customer;"
```

**Lösung:**
```bash
# Option 1: Backend Restart
cd backend && mvn quarkus:dev

# Option 2: Testdaten manuell laden
curl -X POST http://localhost:8080/api/test-data/init
```

---

### Java Version {#java-version}

**Symptome:**
- "Unsupported class file major version"
- Build Fehler mit Java Versionen
- Runtime Exceptions

**Quick Fix:**
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
```

**Diagnose:**
```bash
# Aktuelle Version
java -version
javac -version

# Alle Java Installationen
/usr/libexec/java_home -V
```

**Lösung:**
```bash
# 1. Java 17 installieren (falls nötig)
brew install --cask temurin17

# 2. In .zshrc oder .bashrc
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 3. Shell neu laden
source ~/.zshrc
```

---

## 🔴 CI/CD PROBLEME

### CI HTTP 500 {#ci-http-500}

**Symptome:**
- Integration Tests rot mit HTTP 500
- "Internal Server Error" in CI Logs
- Lokal funktioniert alles

**Quick Fix:**
```yaml
# In .github/workflows/ci.yml
- name: Run Integration Tests
  run: mvn test -Dtest.mode=sequential
```

**Diagnose:**
```bash
# CI Logs abrufen
gh run list --limit 5
gh run view [RUN_ID] --log-failed

# Stack Traces suchen
gh run view [RUN_ID] --log-failed | grep -A 30 "Exception"
```

**Lösung:** Siehe [CI Debugging Lessons Learned](/Users/joergstreeck/freshplan-sales-tool/docs/guides/CI_DEBUGGING_LESSONS_LEARNED.md)

[↑ Zurück zur Navigation](#quick-navigation---symptom-basierte-suche)

---

### CI User Missing {#ci-user-missing}

**Symptome:**
- "Test user 'testuser' not found"
- UserRepository returns empty
- Authentication Tests failing

**Quick Fix:**
```java
// Multi-Tier User Fallback
return userRepository.findByUsername("testuser")
    .or(() -> userRepository.findByUsername("ci-test-user"))
    .or(() -> createTemporaryTestUser())
    .orElseThrow();
```

**Lösung:** Robuster Test-User Mechanismus implementieren

---

## 🟢 SERVICE MANAGEMENT

### Port In Use {#port-in-use}

**Symptome:**
- "Port XXXX already in use"
- Service startet nicht
- Bind Address Fehler

**Quick Fix:**
```bash
# Port 5173 (Frontend)
lsof -ti:5173 | xargs kill -9

# Port 8080 (Backend)  
lsof -ti:8080 | xargs kill -9
```

**Diagnose:**
```bash
# Wer nutzt welchen Port?
lsof -i :5173 -P -n | grep LISTEN
lsof -i :8080 -P -n | grep LISTEN
```

---

### Docker Issues {#docker-issues}

**Symptome:**
- Container starten nicht
- "Cannot connect to Docker daemon"
- Volume Mount Fehler

**Quick Fix:**
```bash
# Docker Desktop starten (macOS)
open -a Docker

# Container neu starten
docker-compose down && docker-compose up -d
```

**Diagnose:**
```bash
# Docker Status
docker ps -a
docker-compose ps

# Logs
docker-compose logs -f [service-name]
```

---

### Services Won't Start {#services-wont-start}

**Symptome:**
- start-dev.sh schlägt fehl
- Services timeout beim Start
- Abhängigkeiten nicht erfüllt

**Quick Fix:**
```bash
# Clean Start aller Services
./scripts/stop-dev.sh
./scripts/cleanup.sh
./scripts/start-dev.sh
```

**Diagnose & Lösung:**
```bash
# 1. Einzeln debuggen
./scripts/check-services.sh

# 2. PostgreSQL zuerst
docker start freshplan-db || docker run -d -p 5432:5432 [...]

# 3. Dann Backend
cd backend && mvn quarkus:dev

# 4. Dann Frontend
cd frontend && npm run dev
```

---

## 📚 WEITERE RESSOURCEN

- [CI Debugging Strategy](/Users/joergstreeck/freshplan-sales-tool/docs/guides/CI_DEBUGGING_STRATEGY.md)
- [Keycloak Setup Guide](/Users/joergstreeck/freshplan-sales-tool/docs/KEYCLOAK_SETUP.md)
- [Backend Start Guide](/Users/joergstreeck/freshplan-sales-tool/docs/technical/BACKEND_START_GUIDE.md)
- [Known Issues](/Users/joergstreeck/freshplan-sales-tool/docs/KNOWN_ISSUES.md)

---

## 🔧 DIAGNOSE SCRIPT

Das `diagnose-problems.sh` Script prüft automatisch:
- Service Status (alle Ports)
- Java Version
- Node/NPM Version
- Docker Status
- Letzte Error Logs
- Common Issues

**Verwendung:**
```bash
./scripts/diagnose-problems.sh

# Mit Details
./scripts/diagnose-problems.sh --verbose
```

---

**Maintenance:** Dieses Dokument wird kontinuierlich erweitert. Neue Probleme und Lösungen bitte mit Datum und Kontext hinzufügen.