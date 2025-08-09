# STANDARDÜBERGABE KOMPAKT - Quick Reference

**⚡ Ultra-Kurzversion für erfahrene Sessions**

## 5-Schritte Speed-Run:

### 1️⃣ System-Check
```bash
/Users/joergstreeck/freshplan-sales-tool/scripts/robust-session-start.sh
# Absoluter Pfad - funktioniert aus JEDEM Verzeichnis!
```

### 2️⃣ Orientierung
- CLAUDE.md → Letzte Übergabe → CRM_MASTER_PLAN.md
- `git status && TodoRead`

### 3️⃣ Arbeiten
- Code validieren (grep/find/ls)
- Two-Pass Review bei Milestones
- Dokumentieren in `/docs/claude-work/daily-work/`
- FC-Status in `/docs/features/` aktualisieren!

### 4️⃣ Problemlösung
- Analysieren → Dokumentieren → Lösungen vorschlagen

### 5️⃣ Übergabe
```bash
/Users/joergstreeck/freshplan-sales-tool/scripts/create-handover.sh
# Absoluter Pfad - erstellt automatisch vollständige Übergabe!
```
- FC-Status dokumentieren
- "NACH KOMPRIMIERUNG" Abschnitt
- Speichern: `YYYY-MM-DD_HANDOVER_HH-MM.md`

## 🔥 Wichtigste Befehle:
```bash
# Backend
cd backend && ./mvnw quarkus:dev

# Frontend  
cd frontend && npm run dev

# Tests
./mvnw test -Dtest=CustomerResourceIntegrationTest

# Clean
/Users/joergstreeck/freshplan-sales-tool/scripts/quick-cleanup.sh

# Migration Check
/Users/joergstreeck/freshplan-sales-tool/scripts/get-next-migration.sh
```

## 📍 Ports:
- Backend: 8080
- Frontend: 5173
- DB: 5432
- Keycloak: 8180

**Bei Problemen → STANDARDUBERGABE.md**