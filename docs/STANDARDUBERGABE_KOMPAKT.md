# STANDARDÜBERGABE KOMPAKT - Quick Reference

**⚡ Ultra-Kurzversion für erfahrene Sessions**

## 5-Schritte Speed-Run:

### 1️⃣ System-Check
```bash
./scripts/validate-config.sh && ./scripts/check-services.sh
```

### 1.5️⃣ V5 Check (NEU!)
```bash
# V5 Quick-Befehle:
cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | head -35  # Fokus-Sektion
cat docs/NEXT_STEP.md                              # 🧭 Wo genau weitermachen!
./scripts/get-active-module.sh                      # Status aktives Modul
cat docs/features/OPEN_QUESTIONS_TRACKER.md | head -20  # Kritische Fragen
```

### 2️⃣ Orientierung
- CLAUDE.md → Letzte Übergabe → ~~CRM_MASTER_PLAN.md~~ V5 bereits gelesen!
- `git status && TodoRead`

### 3️⃣ Arbeiten
- Code validieren (grep/find/ls)
- Two-Pass Review bei Milestones
- Dokumentieren in `/docs/claude-work/daily-work/`
- FC-Status in `/docs/features/` aktualisieren!

### 4️⃣ Problemlösung
- Analysieren → Dokumentieren → Lösungen vorschlagen

### 5️⃣ Übergabe
- Template verwenden
- Code-Stand verifizieren
- FC-Status dokumentieren
- "NACH KOMPRIMIERUNG" Abschnitt
- Speichern: `YYYY-MM-DD_HANDOVER_HH-MM.md`

## 🔥 Wichtigste Befehle:
```bash
# V5 Navigation (NEU!)
cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | sed -n '15,35p'  # Aktueller Fokus
cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | sed -n '77,85p'  # Status Dashboard
./scripts/get-active-module.sh                             # Aktives Modul mit ⭐

# Backend
cd backend && ./mvnw quarkus:dev

# Frontend  
cd frontend && npm run dev

# Tests
./mvnw test -Dtest=CustomerResourceIntegrationTest

# Clean
./scripts/quick-cleanup.sh
```

## 📍 Ports:
- Backend: 8080
- Frontend: 5173
- DB: 5432
- Keycloak: 8180

**Bei Problemen → STANDARDUBERGABE.md**