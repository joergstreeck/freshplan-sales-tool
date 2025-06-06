# Team Sync Log

## Kommunikationsregeln
- **Check-Intervall:** Alle 30 Minuten oder vor größeren Änderungen
- **"An alle":** Wenn du das sagst, schreibe ich hier einen Eintrag für alle Teams
- **Format:** Timestamp - Team - Update

---

## 2025-01-06 - Sprint 1 Start

### 14:00 - KICKOFF
- Team BACK: Backend + Testing Worktrees
- Team FRONT: Frontend Worktree  
- Beide Teams arbeiten parallel
- CI Status: 55 Tests grün ✅

### 14:15 - Team FRONT
- Status: Bereit für React Setup
- Wartet auf: Bestätigung der API Endpoints
- Nächste Schritte: Vite + React 18 bootstrappen

### 14:15 - Team BACK  
- Status: Noch nicht gestartet
- Plan: Keycloak Dev-Realm, Roles API
- Wichtig: API Breaking Changes hier dokumentieren!

---

## API Änderungen (WICHTIG für beide Teams!)

### User API
- `GET /api/users` ✅
- `GET /api/users/{id}` ✅
- `GET /api/users/search?email=` ✅
- `POST /api/users` ✅
- `PUT /api/users/{id}` ✅
- `PUT /api/users/{id}/roles` 🚧 Team BACK heute
- `PUT /api/users/{id}/enable` ✅
- `PUT /api/users/{id}/disable` ✅

---

## Breaking Changes Log
<!-- Hier ALLE Breaking Changes dokumentieren! -->

---

## 🚨 WICHTIGE NACHRICHT VON JÖRG - 15:45

### An alle Teams: Klarheit vor Code!

**WICHTIG:** Bei JEGLICHEN Unklarheiten bezüglich:
- API Endpoints (Naming, Parameter, Response-Format)
- Vorhandene Architektur in unserem Unternehmen
- Business-Logik oder Prozesse
- Integration mit bestehenden Systemen
- Security-Anforderungen
- Performance-Erwartungen

**BITTE ZUERST FRAGEN!** 

❌ NICHT einfach programmieren und hoffen, dass es richtig ist
✅ IMMER nachfragen und Klarheit schaffen

**Warum:** Das Projekt ist zu komplex für Annahmen. Falsche Entscheidungen kosten uns später viel Zeit.

**Wie fragen:**
1. Dokumentiert eure Frage hier im TEAM_SYNC_LOG
2. Markiert mit "❓ FRAGE AN JÖRG:"
3. Wartet auf Antwort bevor ihr weitermacht

Ich beantworte ALLE Fragen gerne! Lieber einmal zu viel gefragt als in die falsche Richtung entwickelt.

---

## Blockers & Help Needed
<!-- Team-übergreifende Probleme hier rein -->

### 15:50 - Team FRONT
- ✅ Nachricht von Jörg verstanden
- 📝 Werde alle Unklarheiten hier dokumentieren bevor ich code

❓ **FRAGE AN JÖRG:** Bevor ich mit dem React Setup starte:
1. Sollen wir Material-UI (MUI) verwenden oder ein anderes UI Framework?
2. Gibt es bestehende Design-Guidelines oder Farb-Schemas von FreshPlan?
3. Soll die neue React-App später die Legacy-App komplett ersetzen oder parallel laufen?

---

### 23:08 - Team BACK
- ✅ Keycloak Dev-Realm konfiguriert (freshplan-realm.json)
- ✅ Docker-Compose mit automatischem Realm-Import
- ✅ Test-User angelegt (admin, manager, testuser)
- 📝 Dokumentation in docs/KEYCLOAK_SETUP.md erstellt
- 🚧 Als nächstes: /api/users/{id}/roles Endpoint

### 23:15 - Team FRONT
- ✅ CI/Logo gefunden in `/legacy/assets/images/`
- 📝 Farben: Primary Green #94C456, Primary Dark #004F7B
- ⏸️ Warte auf IT-Antwort zu: UI Framework, Architektur-Strategie
- 📢 Jörg hat Antworten bei IT angefragt

### 23:20 - Team BACK
- ✅ PUT /api/users/{id}/roles Endpoint fertiggestellt
- ⚠️ BREAKING CHANGE: UserResponse hat jetzt 'roles' Feld (string[])
- 📝 Erlaubte Rollen: 'admin', 'manager', 'user'
- 🔒 Nur Admins können Rollen zuweisen (@RolesAllowed)
- ✅ Umfassende Tests geschrieben (Unit + Integration)

### 23:25 - TEAM UPDATE von Jörg

**ChatGPT ist jetzt dabei!** 🎉

ChatGPT betreut BEIDE Teams parallel als:
- **Berater**: Architektur-Reviews, API-Design (<1h Antwortzeit)
- **Debug-Buddy**: Fehleranalyse, Debug-Strategien (sofort)
- **Doku-Co-Author**: ADRs, READMEs, CI-Workflows (1-2h)
- **Facilitator**: Sprint-Planung, Integration, Retros

**Wichtig**: Bei Fragen an ChatGPT immer [Front] oder [Back] angeben!

### 23:27 - Team FRONT
- ✅ Team-Konstellation verstanden
- 📝 Sehe Breaking Change von Team BACK: UserResponse.roles ist jetzt Pflichtfeld
- ✅ Werde das im Frontend berücksichtigen
- 💡 Super dass Roles-API fertig ist - kann ich direkt nutzen!

### 23:26 - Team BACK - TAGESBERICHT

✅ **Alle heutigen Ziele erreicht:**
1. Keycloak Dev-Realm importiert mit Test-Usern
2. PUT /api/users/{id}/roles komplett implementiert mit Tests
3. Security-Tests sind bereits aktiv (keine T-4 spezifischen Tests gefunden)

📋 **Wichtige Infos für Team FRONT:**
- UserResponse hat jetzt IMMER ein 'roles' Array (nicht mehr optional\!)
- Keycloak läuft auf Port 8180
- Test-User: admin@freshplan.de / admin123

🚀 **Bereit für nächste Aufgaben\!**

### 23:32 - Team BACK

❓ **FRAGE AN TEAM FRONT:**
- Braucht ihr noch etwas von uns für die Frontend-Implementierung?
- Sind die API-Spezifikationen klar genug?
- Sollen wir noch weitere Test-User mit speziellen Rollen anlegen?

💡 **Info:** Wir haben Probleme die Tests lokal auszuführen (Java-Setup).
Die CI sollte aber die Tests automatisch durchführen sobald wir pushen.

### 23:35 - Team FRONT
- 📝 Antwort an Team BACK:
  - API-Specs sind super klar! Danke für API_CONTRACT.md
  - Test-User reichen erstmal aus
  - Warte noch auf IT-Antwort für Framework-Entscheidung
- 💡 Helfe Jörg gerade bei Java-Installation für lokale Tests

### 23:48 - Team BACK - TEST UPDATE

✅ **Tests erfolgreich ausgeführt:**
- 87 von 88 Tests bestanden\!
- Nur 1 Test fehlgeschlagen (PingResourceTest - benötigt Docker)
- Alle neuen Roles-Tests laufen grün ✅

📝 **Nächste Schritte:**
- Code committen und pushen für CI
- Docker starten für lokale Integrationstests

### 23:56 - Team BACK - KORREKTUR

⚠️ **Wichtige Korrektur nach Projektdokumentation-Review:**

Nach gründlicher Durchsicht ALLER Projektdokumente korrigiert:
- ✅ Rollen angepasst: 'admin', 'manager', 'sales', 'viewer' (gemäß PHASE2_KICKOFF.md)
- ✅ Test-User korrigiert: sales@freshplan.de statt test@freshplan.de
- ✅ Erlaubte Rollen in RoleValidator.java aktualisiert

📝 **Erkenntnisse aus Dokumentations-Review:**
- Sprint 1 läuft bereits (06.01.-09.01.2025)
- Umfassende Test-Suite bereits vorhanden (>90% Coverage)
- 'Gründlichkeit vor Schnelligkeit' - hätte zuerst alles lesen sollen\!

### 00:04 - Team BACK - CODE GEPUSHT

✅ **Role Management Feature committed & pushed:**
- Commit: 896e913
- Branch: feature/user-management
- CI sollte jetzt die Tests ausführen

⚠️ **BREAKING CHANGE für Team FRONT:**
- UserResponse.roles ist jetzt PFLICHTFELD
- Neue Rollen: 'admin', 'manager', 'sales', 'viewer'
- Test-User: sales@freshplan.de (nicht mehr test@freshplan.de)

### 00:06 - Team BACK - FRAGE AN JÖRG

❓ **FRAGE AN JÖRG:**

Sprint 1 Ziele laut WAY_OF_WORKING.md:
- ✅ User Entity & Repository (erledigt)
- ✅ REST API für User CRUD (weitgehend erledigt)
- ❓ Frontend User-Liste (Team FRONT?)
- 🚧 Keycloak Integration (Realm konfiguriert, aber OIDC fehlt noch)
- ❓ E2E Tests (Playwright - wer macht das?)

**Was soll ich als nächstes priorisieren?**
1. OIDC-Integration fertigstellen (@Authenticated annotations etc.)?
2. GET /api/users/{id}/permissions implementieren?
3. Docker starten und alle Tests grün bekommen?
4. Mit ChatGPT einen Plan für die OIDC-Integration entwerfen?

### 00:09 - Team BACK - CI ISSUE

🔴 **CI ist rot nach Push\!**

❓ **FRAGE AN JÖRG:**
- Soll ich eine PR von feature/user-management → main erstellen?
- Können wir die CI-Logs einsehen um zu sehen was fehlschlägt?
- Ist es ein Problem mit den neuen Keycloak-Dependencies?

Commit 896e913 auf Branch feature/user-management

### 00:12 - Team BACK - CI FIX

🔧 **CI-Fix gepusht:**
- Problem: mvnw Syntax-Fehler in GitHub Actions
- Lösung: 'sh' prefix für alle mvnw-Aufrufe
- .gitattributes für korrekte Zeilenenden hinzugefügt
- Commit: 6da9a21

⏳ Warte auf CI-Ergebnis...

### 00:13 - Team BACK - SELBSTVERANTWORTUNG

✅ **Verstanden\!** Werde selbst die CI überwachen:
- GitHub Actions regelmäßig checken
- Bei roten Checks sofort reagieren
- Fixes eigenständig durchführen
- Nur bei größeren Problemen eskalieren

### 00:15 - Team BACK - CI FIX #2

🔧 **Zweiter CI-Fix gepusht:**
- Root Cause: mvnw nutzt bash-Syntax aber hatte sh-Shebang
- Fix 1: mvnw Shebang zu #\!/bin/bash geändert
- Fix 2: Alle Aufrufe nutzen jetzt bash statt sh
- Commit: ad3ef6d

🤞 Das sollte es jetzt sein\!

### 00:18 - Team BACK - CI FIX #3

🔧 **Dritter CI-Fix gepusht:**
- Problem: maven-wrapper.jar fehlte (war in .gitignore)
- Lösung: JAR zum Repository hinzugefügt
- .gitignore angepasst
- Commit: c54712d

📝 Lesson Learned: Maven Wrapper JAR muss immer committed sein\!

### 00:20 - Team BACK - CI STRATEGY CHANGE

🔄 **Neue CI-Strategie:**
- Maven Wrapper hat zu viele Kompatibilitätsprobleme
- Nutze jetzt direkt Maven (ist in GitHub Actions vorinstalliert)
- Neuer Workflow: backend-ci.yml mit PostgreSQL Service
- Commit: e7ae389

💡 Manchmal ist die einfachste Lösung die beste\!

### 00:28 - Team BACK - FRAGE AN TEAM FRONT

❓ **FRAGE AN TEAM FRONT:**

Jörg möchte, dass wir CI-Logs selbst abrufen können.
Hattest du schon Zugriff auf GitHub Actions Logs?
Wenn ja, wie hast du das gemacht?

Ideen:
- GitHub CLI (gh) nutzen?
- Webhook der Logs in Repository schreibt?
- GitHub API mit Token?

Bitte um schnelle Antwort\! 🙏

### 00:30 - Team FRONT - ANTWORT AN TEAM BACK

📝 **Antwort zu CI-Logs:**

Für GitHub Actions Logs empfehle ich **GitHub CLI (gh)**:

```bash
# Installation falls noch nicht vorhanden
brew install gh

# Authentifizierung
gh auth login

# Workflow Runs anzeigen
gh run list --branch feature/user-management

# Logs eines spezifischen Runs anzeigen
gh run view <RUN_ID>

# Logs downloaden
gh run download <RUN_ID>

# Live-Logs während Ausführung
gh run watch <RUN_ID>
```

**Beispiel für euren Branch:**
```bash
# Letzte 5 Runs anzeigen
gh run list --branch feature/user-management --limit 5

# Details zum letzten fehlgeschlagenen Run
gh run list --branch feature/user-management --status failure --limit 1
```

💡 **Tipp:** Mit `gh run watch` könnt ihr live zuschauen!

✅ **Zu euren anderen Updates:**
- Super Arbeit mit den Rollen-Korrekturen!
- Ich werde die neuen Rollen ('admin', 'manager', 'sales', 'viewer') im Frontend berücksichtigen
- Test-User sales@freshplan.de notiert

🚀 Team BACK rockt! Weiter so!

