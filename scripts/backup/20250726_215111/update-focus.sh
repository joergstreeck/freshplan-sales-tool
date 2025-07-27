#!/bin/bash

# Script: update-focus.sh
# Zweck: Einfaches Update der .current-focus Datei während der Arbeit
# Autor: Claude
# Datum: 09.07.2025
# Update: 10.07.2025 - Respektiert jetzt Übergaben und ist flexibler
#
# Verwendung:
#   ./scripts/update-focus.sh                              # Interaktiver Modus
#   ./scripts/update-focus.sh --override                   # Override-Modus (ignoriert current-focus)
#   ./scripts/update-focus.sh M3-cockpit                   # Setze Modul direkt
#   ./scripts/update-focus.sh M3-cockpit "Task"            # Setze Modul und Task
#   ./scripts/update-focus.sh FC-002 "M4 Pipeline"         # Setze Feature und Modul
#   ./scripts/update-focus.sh FC-002 "M4 Pipeline" "Task"  # Setze Feature, Modul und Task

set -e

# Hilfe anzeigen
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    echo "update-focus.sh - Verwaltet den aktuellen Arbeitsfokus"
    echo ""
    echo "Verwendung:"
    echo "  ./scripts/update-focus.sh                              # Interaktiver Modus"
    echo "  ./scripts/update-focus.sh --override                   # Override-Modus"
    echo "  ./scripts/update-focus.sh M3-cockpit                   # Setze Modul direkt"
    echo "  ./scripts/update-focus.sh M3-cockpit 'Task'            # Setze Modul und Task"
    echo "  ./scripts/update-focus.sh FC-002 'M4 Pipeline'         # Setze Feature und Modul"
    echo "  ./scripts/update-focus.sh FC-002 'M4 Pipeline' 'Task'  # Setze Feature, Modul und Task"
    echo ""
    echo "Flags:"
    echo "  --override, -o    Ignoriere aktuelle .current-focus Werte"
    echo "  --help, -h        Diese Hilfe anzeigen"
    echo ""
    echo "Das Script warnt, wenn eine Übergabe-Datei existiert und das"
    echo "aktuelle Modul nicht mit der Übergabe übereinstimmt."
    exit 0
fi

# Farben für Output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "📍 Update Current Focus"
echo "====================="

# Prüfe auf spezielle Flags
if [ "$1" = "--override" ] || [ "$1" = "-o" ]; then
    echo -e "${YELLOW}Override-Modus aktiviert - ignoriere .current-focus${NC}"
    OVERRIDE_MODE=true
    shift # Entferne das Flag aus den Argumenten
else
    OVERRIDE_MODE=false
fi

# Prüfe ob eine Übergabe-Datei von heute existiert
TODAY=$(date +%Y-%m-%d)
HANDOVER_DIR="docs/claude-work/daily-work/$TODAY"
LATEST_HANDOVER=""

if [ -d "$HANDOVER_DIR" ]; then
    LATEST_HANDOVER=$(ls -t "$HANDOVER_DIR"/*HANDOVER*.md 2>/dev/null | head -1)
    if [ -n "$LATEST_HANDOVER" ]; then
        echo -e "${YELLOW}⚠️  Warnung: Aktuelle Übergabe gefunden!${NC}"
        echo -e "${BLUE}📄 $LATEST_HANDOVER${NC}"
        echo ""
        
        # Versuche, das aktive Modul aus der Übergabe zu extrahieren
        if grep -q "Aktives Modul:" "$LATEST_HANDOVER"; then
            HANDOVER_MODULE=$(grep "Aktives Modul:" "$LATEST_HANDOVER" | head -1 | sed 's/.*Aktives Modul:[[:space:]]*//' | cut -d' ' -f1)
            echo -e "${GREEN}Aus Übergabe extrahiert: $HANDOVER_MODULE${NC}"
            echo ""
        fi
    fi
fi

# Read current values if file exists
if [ -f .current-focus ]; then
    CURRENT_FEATURE=$(grep '"feature"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "FC-002")
    CURRENT_MODULE=$(grep '"module"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "null")
    CURRENT_FILE=$(grep '"lastFile"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "null")
    CURRENT_LINE=$(grep '"lastLine"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "null")
    CURRENT_TASK=$(grep '"nextTask"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "null")
else
    CURRENT_FEATURE="FC-002"
    CURRENT_MODULE="null"
    CURRENT_FILE="null"
    CURRENT_LINE="null"
    CURRENT_TASK="null"
fi

# Wenn eine Übergabe existiert und das Modul nicht übereinstimmt, warne
if [ -n "$HANDOVER_MODULE" ] && [ "$HANDOVER_MODULE" != "$CURRENT_MODULE" ]; then
    echo -e "${RED}⚠️  ACHTUNG: Diskrepanz zwischen .current-focus und Übergabe!${NC}"
    echo -e "Current-Focus zeigt: ${YELLOW}$CURRENT_MODULE${NC}"
    echo -e "Übergabe zeigt: ${GREEN}$HANDOVER_MODULE${NC}"
    echo ""
    read -p "Möchtest du den Fokus auf das Übergabe-Modul setzen? (j/n): " USE_HANDOVER
    if [[ "$USE_HANDOVER" =~ ^[Jj]$ ]]; then
        CURRENT_MODULE="$HANDOVER_MODULE"
        echo -e "${GREEN}✅ Fokus wird auf Übergabe-Modul gesetzt${NC}"
    fi
    echo ""
fi

# If called with arguments, use them
if [ $# -eq 0 ]; then
    # Interactive mode
    echo -e "${GREEN}Aktuelle Werte:${NC}"
    echo "Feature: $CURRENT_FEATURE"
    echo "Module: $CURRENT_MODULE"
    echo "Last File: $CURRENT_FILE"
    echo "Last Line: $CURRENT_LINE"
    echo "Next Task: $CURRENT_TASK"
    echo ""
    
    read -p "Feature (Enter für $CURRENT_FEATURE): " NEW_FEATURE
    NEW_FEATURE=${NEW_FEATURE:-$CURRENT_FEATURE}
    
    read -p "Module (Enter für $CURRENT_MODULE): " NEW_MODULE
    NEW_MODULE=${NEW_MODULE:-$CURRENT_MODULE}
    
    read -p "Letzte Datei (Enter für keine Änderung): " NEW_FILE
    NEW_FILE=${NEW_FILE:-$CURRENT_FILE}
    
    if [ "$NEW_FILE" != "null" ] && [ "$NEW_FILE" != "$CURRENT_FILE" ]; then
        read -p "Zeile in der Datei: " NEW_LINE
        NEW_LINE=${NEW_LINE:-"null"}
    else
        NEW_LINE=$CURRENT_LINE
    fi
    
    read -p "Nächste Aufgabe: " NEW_TASK
    NEW_TASK=${NEW_TASK:-$CURRENT_TASK}
else
    # Command line mode: update-focus.sh <feature_or_module> ["task"]
    # Wenn das erste Argument mit FC- beginnt, ist es ein Feature Code
    if [[ "$1" =~ ^FC-[0-9]+ ]]; then
        NEW_FEATURE=$1
        NEW_MODULE=${2:-$CURRENT_MODULE}
        NEW_TASK=${3:-$CURRENT_TASK}
    else
        # Ansonsten ist es ein Module Name
        NEW_MODULE=$1
        NEW_TASK=${2:-$CURRENT_TASK}
        NEW_FEATURE=$CURRENT_FEATURE
    fi
    NEW_FILE=$CURRENT_FILE
    NEW_LINE=$CURRENT_LINE
fi

# Update the file
cat > .current-focus << EOF
{
  "feature": "$NEW_FEATURE",
  "module": "$NEW_MODULE",
  "lastFile": "$NEW_FILE",
  "lastLine": "$NEW_LINE",
  "nextTask": "$NEW_TASK",
  "lastUpdated": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

echo ""
echo -e "${GREEN}✅ Focus aktualisiert:${NC}"
echo "Feature: $NEW_FEATURE"
echo "Module: $NEW_MODULE"

if [ "$NEW_FILE" != "null" ]; then
    echo "Last File: $NEW_FILE"
    if [ "$NEW_LINE" != "null" ]; then
        echo "Line: $NEW_LINE"
    fi
fi

if [ "$NEW_TASK" != "null" ]; then
    echo "Next Task: $NEW_TASK"
fi

# Show the spoke document if it exists
if [ "$NEW_MODULE" != "null" ]; then
    SPOKE=$(find docs/features -name "${NEW_FEATURE}-${NEW_MODULE}*.md" 2>/dev/null | head -1)
    if [ -n "$SPOKE" ]; then
        echo ""
        echo -e "${GREEN}📋 Spoke-Dokument:${NC} $SPOKE"
    fi
fi