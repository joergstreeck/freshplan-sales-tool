#!/bin/bash

# Simple handover creation script - minimal output to avoid crashes
# This script creates a handover template and lets Claude fill it

DATE=$(date +%Y-%m-%d)
TIME=$(date +%H-%M)
DATETIME=$(date +"%d.%m.%Y %H:%M")

# Create directory
HANDOVER_DIR="docs/claude-work/daily-work/$DATE"
mkdir -p "$HANDOVER_DIR"

# Handover file
HANDOVER_FILE="$HANDOVER_DIR/${DATE}_HANDOVER_${TIME}.md"

# Check service status
SERVICE_STATUS=$($HOME/freshplan-sales-tool/scripts/check-services.sh 2>&1 | tail -4 | head -3)

# Get Java and Node versions
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
NODE_VERSION=$(node -v)

# Create template with critical info
cat > "$HANDOVER_FILE" << 'EOF'
# 🔄 STANDARDÜBERGABE - DATETIME_PLACEHOLDER

**WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge:**
1. `/docs/CLAUDE.md` (Arbeitsrichtlinien und Standards)
2. Diese Übergabe
3. `/docs/STANDARDUBERGABE_NEU.md` als Hauptanleitung

## 🚨 KRITISCHE TECHNISCHE INFORMATIONEN

### 🖥️ Service-Konfiguration
| Service | Port | Technologie | Status |
|---------|------|-------------|--------|
| **Backend** | `8080` | Quarkus mit Java 17 | SERVICE_CHECK_PLACEHOLDER |
| **Frontend** | `5173` | React/Vite | SERVICE_CHECK_PLACEHOLDER |
| **PostgreSQL** | `5432` | PostgreSQL 15+ | SERVICE_CHECK_PLACEHOLDER |
| **Keycloak** | `8180` | Auth Service | SERVICE_CHECK_PLACEHOLDER |

### ⚠️ WICHTIGE HINWEISE
- **Java Version:** MUSS Java 17 sein! (aktuell: JAVA_VERSION_PLACEHOLDER)
- **Node Version:** v22.16.0+ erforderlich (aktuell: NODE_VERSION_PLACEHOLDER)
- **Working Directory:** `/Users/joergstreeck/freshplan-sales-tool`
- **Branch-Regel:** NIEMALS direkt in `main` pushen!

## 🎯 AKTUELLER STAND

### Git Status
```
[Git status wird von Claude eingefügt]
```

### Aktives Modul
**Feature:** [Von Claude ausfüllen]
**Modul:** [Von Claude ausfüllen]
**Dokument:** [Von Claude ausfüllen] ⭐
**Status:** [Von Claude ausfüllen]

## 📋 WAS WURDE HEUTE GEMACHT?
[Von Claude ausfüllen - konkrete Code-Änderungen mit Dateinamen]

## ✅ WAS FUNKTIONIERT?
[Von Claude ausfüllen - verifiziert durch Tests/Logs]

## 🚨 WELCHE FEHLER GIBT ES?
[Von Claude ausfüllen - mit genauer Fehlermeldung und betroffenen Dateien]

## 📋 TODO-LISTE

### ⚠️ TODO-STATUS FEHLT!
**KRITISCH: Führe diese Schritte aus:**
```bash
1. ./scripts/todo-export.sh
2. TodoRead in deiner Session
3. Füge TODOs in .current-todos.md ein
4. Führe ./scripts/create-handover.sh erneut aus
```

## 🔧 NÄCHSTE SCHRITTE
[Von Claude ausfüllen - konkret mit Dateinamen und Befehlen]

## 📝 CHANGE LOGS DIESER SESSION
- [ ] Change Log erstellt für: [Feature-Name]
  - Link: `/docs/claude-work/daily-work/DATE_PLACEHOLDER/DATE_PLACEHOLDER_CHANGE_LOG_feature.md`

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

# 6. [Spezifische Befehle von Claude für aktuelle Aufgabe]
```

---
**Session-Ende:** TIME_PLACEHOLDER  
**Hauptaufgabe:** [Von Claude ausfüllen]  
**Status:** [Von Claude ausfüllen]
EOF

# Replace placeholders
sed -i.bak "s/DATETIME_PLACEHOLDER/$DATETIME/g" "$HANDOVER_FILE"
sed -i.bak "s/TIME_PLACEHOLDER/$TIME/g" "$HANDOVER_FILE"
sed -i.bak "s/DATE_PLACEHOLDER/$DATE/g" "$HANDOVER_FILE"
sed -i.bak "s/JAVA_VERSION_PLACEHOLDER/$JAVA_VERSION/g" "$HANDOVER_FILE"
sed -i.bak "s/NODE_VERSION_PLACEHOLDER/$NODE_VERSION/g" "$HANDOVER_FILE"
sed -i.bak "s/SERVICE_CHECK_PLACEHOLDER/[Von Script prüfen]/g" "$HANDOVER_FILE"
rm "$HANDOVER_FILE.bak"

# Check if TODOs exist and update template
if [ -f ".current-todos.md" ]; then
    # Create a temporary file with proper markers
    TODO_START="## 📋 TODO-LISTE"
    TODO_END="## 🔧 NÄCHSTE SCHRITTE"
    
    # Extract content before TODOs
    sed -n "1,/${TODO_START}/p" "$HANDOVER_FILE" | sed '$d' > "${HANDOVER_FILE}.tmp"
    
    # Add the actual TODOs
    echo "" >> "${HANDOVER_FILE}.tmp"
    cat ".current-todos.md" >> "${HANDOVER_FILE}.tmp"
    echo "" >> "${HANDOVER_FILE}.tmp"
    
    # Extract content after TODOs
    sed -n "/${TODO_END}/,\$p" "$HANDOVER_FILE" >> "${HANDOVER_FILE}.tmp"
    
    # Replace original file
    mv "${HANDOVER_FILE}.tmp" "$HANDOVER_FILE"
    
    echo "✅ TODOs wurden automatisch eingefügt!"
else
    echo "⚠️  WARNUNG: Keine .current-todos.md gefunden!"
    echo "   Führe erst ./scripts/todo-export.sh aus!"
fi

# Simple output
echo "✅ Handover template created:"
echo "   $HANDOVER_FILE"
echo ""
echo "📝 Claude, please fill in the template with:"
echo "   - Git status and recent commits"
echo "   - What was done today"
echo "   - Current state and any issues"
echo "   - Next steps for the next session"
echo ""
if [ ! -f ".current-todos.md" ]; then
    echo "⚠️  KRITISCH: TODOs fehlen! Führe aus:"
    echo "   1. ./scripts/todo-export.sh"
    echo "   2. TodoRead und TODOs in .current-todos.md eintragen"
    echo "   3. ./scripts/create-handover.sh erneut"
fi