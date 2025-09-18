# 🚨 WICHTIGE HINWEISE FÜR macOS UPDATE

## Kritische Punkte die ich gefunden habe:

### 1. ⚠️ .env Datei war im Repository!
- **Problem:** `.env` war versehentlich committed
- **Gelöst:** Aus Git entfernt mit `git rm --cached`
- **TODO nach Update:** Neuen Commit machen!

### 2. 📁 Lokale Installationen
- **Maven:** `~/apache-maven-3.9.6/` (manuell installiert)
- **Node/npm:** Über System, muss nach Update geprüft werden

### 3. 🔧 Laufende Services
- Alle Frontend-Prozesse gestoppt ✅
- Backend läuft NICHT (braucht Docker)
- Keycloak läuft NICHT (braucht Docker)

### 4. 💾 Wichtige Daten
- **Vite Cache:** In `frontend/node_modules/.vite/`
- **DS_Store Dateien:** Überall verteilt (unwichtig)
- **Logs:** Keine wichtigen gefunden

### 5. 🔑 Environment Variables
```bash
# Frontend .env (NICHT SENSITIV)
VITE_KEYCLOAK_URL=http://localhost:8180
VITE_API_URL=http://localhost:8080

# Test .env (NUR FÜR TESTS)
VITE_TEST_USER_EMAIL=e2e@test.de
VITE_TEST_USER_PASSWORD=test-password
```

## Nach dem Update SOFORT:

1. **Git commit für .env removal:**
   ```bash
   cd /Users/joergstreeck/freshplan-sales-tool
   git add .
   git commit -m "security: Remove .env from tracking and update .gitignore"
   git push
   ```

2. **Node.js prüfen:**
   ```bash
   node --version  # Sollte v18+ sein
   npm --version   # Sollte v9+ sein
   ```

3. **Frontend testen:**
   ```bash
   cd frontend
   rm -rf node_modules
   npm install
   npm run dev
   ```

## Bekannte Issues:

1. **Maven Wrapper:** JAR musste manuell heruntergeladen werden
2. **Docker:** Nicht installiert (für Backend nötig)
3. **PostgreSQL:** Nicht installiert (für Backend nötig)

## MSW Fallback:
Frontend funktioniert KOMPLETT ohne Backend dank Mock Service Worker!
- Calculator ✅
- Users ✅  
- Integration Tests ✅

---
Erstellt: 2025-01-07 19:15 Uhr
Von: Team FRONT