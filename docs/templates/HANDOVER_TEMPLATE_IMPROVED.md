# 🔄 STANDARDÜBERGABE - [DATUM] [UHRZEIT]

**WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge:**
1. `/docs/CLAUDE.md` (Arbeitsrichtlinien und Standards)
2. Diese Übergabe
3. `/docs/STANDARDUBERGABE_NEU.md` als Hauptanleitung

## 🚨 KRITISCHE TECHNISCHE INFORMATIONEN

### 🖥️ Service-Konfiguration
| Service | Port | Technologie | Befehl zum Starten |
|---------|------|-------------|-------------------|
| **Backend** | `8080` | Quarkus mit Java 17 | `cd backend && ./mvnw quarkus:dev` |
| **Frontend** | `5173` | React/Vite | `cd frontend && npm run dev` |
| **PostgreSQL** | `5432` | PostgreSQL 15+ | Läuft als System-Service |
| **Keycloak** | `8180` | Auth Service | Docker oder Standalone |

### ⚠️ WICHTIGE HINWEISE
- **Java Version:** MUSS Java 17 sein! Check mit: `java -version`
- **Node Version:** Sollte v22.16.0+ sein. Check mit: `node -v`
- **Working Directory:** IMMER `/Users/joergstreeck/freshplan-sales-tool`
- **Branch-Regel:** NIEMALS direkt in `main` pushen!

### 🔧 Quick-Check Befehle
```bash
# Sind alle Services aktiv?
./scripts/check-services.sh

# Falls nicht, alle starten:
./scripts/start-services.sh

# Validierung der Entwicklungsumgebung:
./scripts/validate-config.sh
```

## 🎯 AKTUELLER STAND

### Git Status
```
Branch: [AKTUELLER BRANCH]
Modified: [LISTE DER GEÄNDERTEN DATEIEN]
Untracked: [LISTE DER NEUEN DATEIEN]

Letzte Commits:
[COMMIT HASH UND MESSAGES]
```

### Aktives Modul
**Feature:** [FC-XXX Name]
**Modul:** [FC-XXX-MX Name]
**Dokument:** [Pfad zum Modul-Dokument] ⭐
**Status:** [🔄 In Arbeit (XX%) / ✅ Abgeschlossen / 📋 In Planung]

## 📋 WAS WURDE HEUTE GEMACHT?
[Detaillierte Liste der Aktivitäten mit Code-Validierung]

## ✅ WAS FUNKTIONIERT?
[Verifizierte, funktionierende Features mit Bestätigung durch Tests/Logs]

## 🚨 WELCHE FEHLER GIBT ES?
[Aktuelle Probleme mit genauen Fehlermeldungen und betroffenen Dateien]

## 🔧 NÄCHSTE SCHRITTE
[Priorisierte Liste der nächsten Aufgaben mit konkreten Dateinamen]

## 📝 CHANGE LOGS DIESER SESSION
- [ ] Change Log erstellt für: [Feature-Name]
  - Link: `/docs/claude-work/daily-work/YYYY-MM-DD/YYYY-MM-DD_CHANGE_LOG_feature.md`
- [ ] Weitere Change Logs: [Liste weitere wenn vorhanden]

## 🚀 QUICK START FÜR NÄCHSTE SESSION
```bash
# 1. Zum Projekt wechseln
cd /Users/joergstreeck/freshplan-sales-tool

# 2. System-Check und Services starten
./scripts/validate-config.sh
./scripts/check-services.sh

# Falls Services nicht laufen:
./scripts/start-services.sh

# 3. Git-Status
git status
git log --oneline -5

# 4. Aktives Modul anzeigen
./scripts/get-active-module.sh

# 5. TODO-Status
TodoRead

# 6. [Spezifische Befehle für aktuelle Aufgabe]
[KONKRETE BEFEHLE DIE ZUM NÄCHSTEN SCHRITT FÜHREN]
```

## 📊 TEST-STATUS
```bash
# Backend Tests
cd backend && ./mvnw test

# Frontend Tests  
cd frontend && npm test

# Spezifische Tests für aktuelles Modul
[KONKRETE TEST-BEFEHLE]
```

## 🏛️ ARCHITEKTUR-KONTEXT
- **Backend:** Domain-Driven Design mit domain/api/infrastructure
- **Frontend:** Feature-based mit features/components/pages
- **State Management:** Zustand für Frontend, Quarkus für Backend
- **Auth:** Keycloak OIDC Integration

## 📚 RELEVANTE DOKUMENTATION
- Master Plan: `/docs/CRM_COMPLETE_MASTER_PLAN_V5.md`
- Aktives Feature-Konzept: [Link zum FC-XXX Dokument]
- API Contract: `/docs/technical/API_CONTRACT.md`
- Known Issues: `/docs/KNOWN_ISSUES.md`

---
**Session-Ende:** [UHRZEIT]  
**Hauptaufgabe:** [KURZE BESCHREIBUNG]  
**Status:** [✅ Abgeschlossen / 🔄 In Arbeit / ⏸️ Pausiert]