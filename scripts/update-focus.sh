#!/bin/bash

# Script: update-focus.sh
# Zweck: Einfaches Update der .current-focus Datei während der Arbeit
# Autor: Claude
# Datum: 09.07.2025

set -e

# Farben für Output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "📍 Update Current Focus"
echo "====================="

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
    # Command line mode: update-focus.sh <module> ["task"]
    NEW_MODULE=$1
    NEW_TASK=${2:-$CURRENT_TASK}
    NEW_FEATURE=$CURRENT_FEATURE
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