# 🔄 STANDARDÜBERGABE - [DATUM] [UHRZEIT]

**WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge:**
1. `/docs/CLAUDE.md` (Arbeitsrichtlinien und Standards)
2. Diese Übergabe
3. `/docs/STANDARDUBERGABE_NEU.md` als Hauptanleitung

## 🚨 KRITISCHE TECHNISCHE INFORMATIONEN

### 🖥️ Service-Konfiguration
| Service | Port | Technologie | Status |
|---------|------|-------------|--------|
| **Backend** | `8080` | Quarkus mit Java 17 | [STATUS] |
| **Frontend** | `5173` | React/Vite | [STATUS] |
| **PostgreSQL** | `5432` | PostgreSQL 15+ | [STATUS] |
| **Keycloak** | `8180` | Auth Service | [STATUS] |

### ⚠️ WICHTIGE HINWEISE
- **Java Version:** MUSS Java 17 sein! (aktuell: [VERSION])
- **Node Version:** v22.16.0+ erforderlich (aktuell: [VERSION])
- **Working Directory:** `/Users/joergstreeck/freshplan-sales-tool`
- **Branch-Regel:** NIEMALS direkt in `main` pushen!

## 🎯 AKTUELLER STAND

### Git Status
```
Branch: [BRANCH]
Letzter Commit: [COMMIT]
Ungetrackte Dateien: [COUNT]
```

### Aktives Modul
**Feature:** [FC-XXX]
**Modul:** [MODUL-NAME]
**Dokument:** [PFAD] ⭐
**Status:** [STATUS]

## 📋 TODO-LISTE (AUTOMATISCH SYNCHRONISIERT)

{{TODO_LIST}}

## 📋 WAS WURDE HEUTE GEMACHT?

[LISTE DER DURCHGEFÜHRTEN ARBEITEN]

## ✅ WAS FUNKTIONIERT?

[VERIFIZIERTE FUNKTIONEN]

## 🚨 WELCHE FEHLER GIBT ES?

[BEKANNTE PROBLEME]

## 🔧 NÄCHSTE SCHRITTE

### Offene TODOs (Priorität: high)
{{HIGH_PRIORITY_TODOS}}

### Offene TODOs (Priorität: medium/low)
{{OTHER_TODOS}}

### Nach Abschluss der TODOs
[NÄCHSTES MODUL/FEATURE]

## 📝 CHANGE LOGS DIESER SESSION
[LISTE DER CHANGE LOGS]

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

# 6. [SPEZIFISCHE NÄCHSTE AKTION]
```

---
**Session-Ende:** [UHRZEIT]  
**Hauptaufgabe:** [HAUPTAUFGABE]  
**Status:** [GESAMTSTATUS]