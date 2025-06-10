# FreshPlan Team Setup - Sprint 1

**📅 Aktuelles Datum: <!-- AUTO_DATE --> (System: 08.06.2025)**

## 🎯 Team-Zuteilung

### Team BACK
**Worktrees:**
- `freshplan-backend` (branch: `feature/user-management`)
- `freshplan-testing` (branch: `feature/expand-test-coverage`)

**Schwerpunkte:** User-Management, OIDC, Security-Tests

**Kick-off Tasks (Tagesziel):**
1. ✅ Keycloak-Dev-Realm importieren
2. ✅ `/api/users/{id}/roles` fertigstellen
3. ✅ Security-ITs (T-4) reaktivieren

### Team FRONT
**Worktree:**
- `freshplan-frontend` (branch: `feature/react-migration`)

**Schwerpunkte:** React-Migration, UI, API-Client

**Kick-off Tasks (Tagesziel):**
1. ✅ Projekt auf Vite + React 18 bootstrappen
2. ✅ Auth-Context skizzieren (Token Flow)
3. ✅ Erste Story: "User-Liste rendern"

## 📋 Organisatorische Schritte

### 1. Team-Leads
- **Team BACK:** @Max
- **Team FRONT:** @Sophie

### 2. GitHub Projects
- [ ] Team BACK Board anlegen
- [ ] Team FRONT Board anlegen
- Template: Kanban (Automated)
- Spalten: `Backlog | In-Progress | Review/QA | Done`

### 3. PR-Richtlinien
- Reviewer-Pairing: mind. 1 Cross-Team-Review
- Label-Convention: 
  - `area/backend`
  - `area/frontend`
  - `area/testing`

### 4. Daily Sync
- **Zeit:** 09:00 CET (15 min)
- **Format:** Zoom Stand-up mit Show & Tell
- **Async:** Slack #fp-daily

### 5. Weekly Integration
- **Zeit:** Freitags 14:00 CET (30 min)
- **Agenda:** Demo + Merge-Plan

## 🚀 Technischer Kick-off Check-Liste

### Team BACK
- [ ] Worktree klonen: `cd ../freshplan-backend`
- [ ] Dependencies: `./mvnw clean install`
- [ ] Keycloak DevServices: `./mvnw quarkus:dev -Dquarkus.keycloak.devservices.enabled=true`
- [ ] Security-Tests aktivieren: `-Pgreen-security`

### Team FRONT
- [ ] Worktree klonen: `cd ../freshplan-frontend`
- [ ] Dependencies: `npm ci`
- [ ] Dev Server: `npm run dev`
- [ ] Mock-API: `swagger-codegen` für erste Mocks

### Gemeinsam
- [ ] VS Code Workspace Settings pro Team
- [ ] GitHub Secrets: `KEYCLOAK_URL`, `REALM`
- [ ] CI beobachten: `worktree-ci.yml`

## 💚 Debug-Strategie
Bei Problemen: **Strategie der kleinen Schritte**
- Test isolieren
- Debug-Output einbauen
- Schrittweise erweitern
- Siehe: `docs/CI_DEBUGGING_STRATEGY.md`

---

**Ready, Set, Code!** 🚀

Sobald Team-Leads bestätigt → Tickets in "In-Progress"

Auf eine weiterhin saftig-grüne CI-Pipeline! 💚