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

