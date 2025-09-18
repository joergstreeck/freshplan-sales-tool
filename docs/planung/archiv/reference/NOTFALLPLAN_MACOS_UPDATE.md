# 🚨 NOTFALLPLAN - macOS Update

## 🔴 SOFORT-MASSNAHMEN bei Problemen

### 1. System startet nicht mehr
- **Option A:** Recovery Mode (CMD+R beim Start)
- **Option B:** Time Machine Backup (falls vorhanden)
- **Option C:** Internet Recovery (CMD+Option+R)

### 2. Git/Code nicht mehr erreichbar
- **GitHub:** https://github.com/joergstreeck/freshplan-sales-tool
- **Letzter Commit:** a815212 (security: Remove .env from tracking)
- **Clone-Befehl:** 
  ```bash
  git clone https://github.com/joergstreeck/freshplan-sales-tool.git
  ```

### 3. Entwicklungsumgebung kaputt
```bash
# Node.js neu installieren (Option 1: Homebrew)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
brew install node

# Node.js neu installieren (Option 2: Official)
# Download von https://nodejs.org/

# Frontend wiederherstellen
cd freshplan-sales-tool/frontend
npm install
npm run dev
```

## 📱 KRITISCHE KONTAKTE

### Team BACK
- Arbeitet parallel am Backend
- Hat eigene Sicherungen durchgeführt
- Kann bei Backend-Fragen helfen

### Ressourcen
- **Frontend läuft auf:** http://localhost:5173
- **Backend soll auf:** http://localhost:8080
- **Keycloak auf:** http://localhost:8180
- **PostgreSQL auf:** localhost:5432

## 🔧 QUICK FIXES

### Problem: "command not found: node"
```bash
# Prüfe ob node in anderem Pfad
which node
echo $PATH

# Füge zu ~/.zshrc oder ~/.bash_profile hinzu
export PATH="/usr/local/bin:$PATH"
source ~/.zshrc
```

### Problem: "EACCES permission denied"
```bash
sudo chown -R $(whoami) ~/.npm
sudo chown -R $(whoami) /usr/local/lib/node_modules
```

### Problem: Port bereits belegt
```bash
# Finde blockierenden Prozess
lsof -i :5173
kill -9 <PID>

# Alternative Ports nutzen
VITE_PORT=3000 npm run dev
```

### Problem: Maven nicht gefunden
```bash
# Maven ist lokal installiert in
~/apache-maven-3.9.6/bin/mvn

# Füge zu PATH hinzu
export PATH="$HOME/apache-maven-3.9.6/bin:$PATH"
```

## 💾 BACKUP-LOCATIONS

### Lokal gesichert:
- Node modules Liste: `frontend/node_modules_backup.txt`
- Maven Installation: `~/apache-maven-3.9.6/`

### Remote (GitHub):
- Vollständiger Code
- Alle Konfigurationen
- MSW Mocks für Offline-Entwicklung

## 🔄 ROLLBACK-STRATEGIE

### Level 1: Nur Frontend
```bash
cd frontend
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

### Level 2: Git Reset
```bash
git fetch origin
git reset --hard origin/main
```

### Level 3: Kompletter Neustart
```bash
cd ~
rm -rf freshplan-sales-tool
git clone https://github.com/joergstreeck/freshplan-sales-tool.git
cd freshplan-sales-tool/frontend
npm install
```

## ✅ FUNKTIONIERT GARANTIERT (ohne Backend)

Dank MSW Mock Service Worker:
- Calculator mit allen Features
- User Management
- Integration Tests
- Alle UI-Komponenten

**Start-Befehl:**
```bash
cd frontend && npm run dev
```

## 🎯 PRIORITÄTEN nach Update

1. **Node.js funktionsfähig** → Frontend läuft
2. **Git funktionsfähig** → Code-Zugriff
3. **Docker** → Nur für Backend nötig
4. **PostgreSQL** → Nur für Backend nötig

## 📞 NOTFALL-CHECKLISTE

- [ ] System bootet?
- [ ] Terminal öffnet?
- [ ] `node --version` funktioniert?
- [ ] `git --version` funktioniert?
- [ ] Internet-Verbindung da?
- [ ] GitHub erreichbar?
- [ ] localhost:5173 erreichbar?

---

**WICHTIG:** Frontend ist komplett unabhängig vom Backend!
MSW Mocks simulieren alle Backend-Funktionen perfekt.

**Erstellt:** 2025-01-07 19:25 Uhr
**Von:** Team FRONT
**Status:** BEREIT FÜR UPDATE! 🚀