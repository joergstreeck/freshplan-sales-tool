#!/bin/bash

# Script: get-active-module.sh
# Zweck: Liest das aktive Modul aus .current-focus und findet zugehörige Dokumente
# Autor: Claude
# Datum: 09.07.2025

set -e

# Farben für Output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "🔍 Suche aktives Modul..."
echo "========================="

# Prüfe ob .current-focus existiert
if [ ! -f .current-focus ]; then
    echo -e "${RED}❌ Datei .current-focus nicht gefunden${NC}"
    echo "Kein aktives Modul definiert."
    exit 0
fi

# Lese aktives Feature und Modul aus JSON
FEATURE=$(grep '"feature"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "")
MODULE=$(grep '"module"' .current-focus | cut -d'"' -f4 2>/dev/null || echo "")

if [ -z "$FEATURE" ]; then
    echo -e "${YELLOW}⚠️  Kein Feature-Code in .current-focus gefunden${NC}"
    exit 0
fi

echo -e "${GREEN}✅ Aktives Feature:${NC} $FEATURE"

if [ -n "$MODULE" ] && [ "$MODULE" != "null" ]; then
    FULL_MODULE="$FEATURE-$MODULE"
    echo -e "${GREEN}✅ Aktives Modul:${NC} $FULL_MODULE"
    
    # Suche Hub-Dokument
    HUB_DOC=$(find docs/features -name "${FEATURE}-hub.md" 2>/dev/null | head -1)
    if [ -n "$HUB_DOC" ]; then
        echo -e "${GREEN}📋 Hub-Dokument:${NC} $HUB_DOC"
    fi
    
    # Suche Spoke-Dokument
    SPOKE_DOC=$(find docs/features -name "${FULL_MODULE}*.md" 2>/dev/null | head -1)
    if [ -n "$SPOKE_DOC" ]; then
        echo -e "${GREEN}⭐ Spoke-Dokument:${NC} $SPOKE_DOC"
        
        # Extrahiere Status aus Spoke
        if [ -f "$SPOKE_DOC" ]; then
            STATUS=$(grep -E "^\*\*Status:\*\*" "$SPOKE_DOC" | head -1 | sed 's/.*Status:\*\* //')
            if [ -n "$STATUS" ]; then
                echo -e "${GREEN}📊 Modul-Status:${NC} $STATUS"
            fi
            
            # Suche nach "NÄCHSTER SCHRITT"
            if grep -q "NÄCHSTER SCHRITT" "$SPOKE_DOC"; then
                echo ""
                echo -e "${GREEN}🎯 Nächster Schritt gefunden im Dokument${NC}"
                echo "Nutze 'grep -A 10 \"NÄCHSTER SCHRITT\" $SPOKE_DOC' zum Anzeigen"
            fi
        fi
    else
        echo -e "${YELLOW}⚠️  Kein Spoke-Dokument für $MODULE gefunden${NC}"
    fi
    
    # Exportiere Variablen für andere Scripts
    export ACTIVE_FEATURE="$FEATURE"
    export ACTIVE_MODULE="$MODULE"
    export ACTIVE_HUB="$HUB_DOC"
    export ACTIVE_SPOKE="$SPOKE_DOC"
    
else
    echo -e "${YELLOW}⚠️  Kein spezifisches Modul in .current-focus definiert${NC}"
    echo "Nur Feature $FEATURE ist aktiv"
fi

echo ""
echo "💡 Tipp: Aktualisiere .current-focus mit:"
echo "   echo 'Feature: FC-XXX-MX ModulName' > .current-focus"