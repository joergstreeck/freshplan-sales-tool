#!/bin/bash

# Simple handover creation script - minimal output to avoid crashes
# This script creates a handover template and lets Claude fill it

# Ensure we're in the right directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

DATE=$(date +%Y-%m-%d)
TIME=$(date +%H-%M)
DATETIME=$(date +"%d.%m.%Y %H:%M")

# Create directory
HANDOVER_DIR="docs/claude-work/daily-work/$DATE"
mkdir -p "$HANDOVER_DIR"

# Handover file
HANDOVER_FILE="$HANDOVER_DIR/${DATE}_HANDOVER_${TIME}.md"

# Check service status (relative path)
SERVICE_STATUS=$(./scripts/check-services.sh 2>&1 | tail -4 | head -3 || echo "Service check failed")

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

### ⚠️ TODO-STATUS WIRD VON CLAUDE EINGEFÜGT!
**Claude:** Bitte führe `TodoRead` aus und füge die aktuelle TODO-Liste hier ein.

### 📊 TODO-ANZAHL ZUM VERGLEICH:
**Automatisch gezählt:** TODO_COUNT_PLACEHOLDER TODOs gefunden
**Nach TodoRead:** [Von Claude einzutragen]
⚠️ **Bei Diskrepanz:** ALLE TODOs aus dieser Übergabe mit TodoWrite wiederherstellen!

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

# Count TODOs in recent handovers
TODO_COUNT=0
if [ -f ".current-todos.md" ]; then
    # Count from current todos file (JSON format)
    # Count occurrences of "id": "todo-" in the JSON
    TODO_COUNT=$(grep -o '"id": "todo-' ".current-todos.md" 2>/dev/null | wc -l | tr -d ' ')
    if [ "$TODO_COUNT" -gt 0 ]; then
        echo "✅ TODOs gezählt: $TODO_COUNT (aus .current-todos.md)"
    fi
elif [ -f "backend/.current-todos.md" ]; then
    # Try backend directory
    TODO_COUNT=$(grep -o '"id": "todo-' "backend/.current-todos.md" 2>/dev/null | wc -l | tr -d ' ')
    if [ "$TODO_COUNT" -gt 0 ]; then
        echo "✅ TODOs gezählt: $TODO_COUNT (aus backend/.current-todos.md)"
    fi
else
    # Count from most recent handover
    LATEST_HANDOVER=$(find docs/claude-work/daily-work -name "*HANDOVER*.md" -type f 2>/dev/null | sort -r | head -1)
    if [ -f "$LATEST_HANDOVER" ]; then
        # Try strict pattern first
        TODO_COUNT=$(grep -E "^\- \[ \].*\[ID: todo-" "$LATEST_HANDOVER" 2>/dev/null | wc -l | tr -d ' ')
        
        # If no results, try more flexible pattern
        if [ "$TODO_COUNT" -eq 0 ]; then
            TODO_COUNT=$(grep -E "^\- \[ \].*todo-[0-9]+" "$LATEST_HANDOVER" 2>/dev/null | wc -l | tr -d ' ')
        fi
        
        if [ "$TODO_COUNT" -gt 0 ]; then
            echo "✅ TODOs aus letzter Übergabe: $TODO_COUNT"
            echo "   Quelle: $(basename $LATEST_HANDOVER)"
        fi
    fi
fi

# Warning if no TODOs found
if [ "$TODO_COUNT" -eq 0 ]; then
    echo "⚠️  WARNUNG: TODO-Zählung fehlgeschlagen!"
    echo "   Tipp: 'TodoRead > .current-todos.md' ausführen für zuverlässigere Zählung"
    TODO_COUNT="[MANUELL_PRÜFEN]"
fi

# Replace TODO count placeholder
sed -i.bak "s/TODO_COUNT_PLACEHOLDER/$TODO_COUNT/g" "$HANDOVER_FILE"
rm "$HANDOVER_FILE.bak"

# Check if TODOs exist and update template
TODO_FILE=""
if [ -f ".current-todos.md" ]; then
    TODO_FILE=".current-todos.md"
elif [ -f "backend/.current-todos.md" ]; then
    TODO_FILE="backend/.current-todos.md"
fi

if [ -n "$TODO_FILE" ]; then
    # Don't automatically insert JSON TODOs - Claude should format them properly
    echo "✅ TODOs gefunden in: $TODO_FILE"
    echo "⚠️  Claude muss die TODOs aus $TODO_FILE formatiert einfügen!"
else
    # Keep the template as is - don't modify the TODO section
    echo "⚠️  TODOs müssen von Claude eingefügt werden!"
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
    echo "⚠️  HINWEIS: TODOs müssen von Claude eingefügt werden!"
    echo "   Claude sollte 'TodoRead' ausführen und die Liste in die Handover eintragen"
fi