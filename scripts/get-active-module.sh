#!/bin/bash

# Script: get-active-module.sh
# Zweck: Liest das aktive Modul aus .current-focus und findet zugehörige Dokumente
# Autor: Claude
# Datum: 13.07.2025 - Angepasst an neue Dokumentationsstruktur

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
    echo -e "${GREEN}✅ Aktives Modul:${NC} $MODULE"
    
    # Neue Struktur: Suche in ACTIVE/PLANNED/ARCHIVE Ordnern
    # Konvertiere Module-Namen zu snake_case für Ordnersuche
    MODULE_FOLDER=$(echo "$MODULE" | tr '[:upper:]' '[:lower:]' | tr ' ' '_')
    
    # Suche nach Modul-Ordner in der neuen Struktur
    FOUND_DOCS=""
    
    # Prüfe ACTIVE Ordner
    if [ -d "docs/features/ACTIVE" ]; then
        ACTIVE_DOC=$(find docs/features/ACTIVE -type d -name "*${MODULE_FOLDER}*" 2>/dev/null | head -1)
        if [ -n "$ACTIVE_DOC" ] && [ -f "$ACTIVE_DOC/README.md" ]; then
            FOUND_DOCS="$ACTIVE_DOC/README.md"
            echo -e "${GREEN}⭐ Aktives Modul-Dokument:${NC} $FOUND_DOCS"
        fi
    fi
    
    # Falls nicht in ACTIVE, suche in PLANNED
    if [ -z "$FOUND_DOCS" ] && [ -d "docs/features/PLANNED" ]; then
        PLANNED_DOC=$(find docs/features/PLANNED -type d -name "*${MODULE_FOLDER}*" 2>/dev/null | head -1)
        if [ -n "$PLANNED_DOC" ] && [ -f "$PLANNED_DOC/README.md" ]; then
            FOUND_DOCS="$PLANNED_DOC/README.md"
            echo -e "${YELLOW}📋 Geplantes Modul-Dokument:${NC} $FOUND_DOCS"
        fi
    fi
    
    # Falls immer noch nicht gefunden, suche nach Feature-Code
    if [ -z "$FOUND_DOCS" ]; then
        # Suche nach Ordnern die mit Nummer beginnen (z.B. 01_security_foundation)
        NUMBERED_DOC=$(find docs/features/ACTIVE -type d -name "[0-9]*" 2>/dev/null | while read dir; do
            if [ -f "$dir/README.md" ] && grep -q "$FEATURE" "$dir/README.md" 2>/dev/null; then
                echo "$dir/README.md"
                break
            fi
        done)
        
        if [ -n "$NUMBERED_DOC" ]; then
            FOUND_DOCS="$NUMBERED_DOC"
            echo -e "${GREEN}⭐ Modul-Dokument gefunden:${NC} $FOUND_DOCS"
        fi
    fi
    
    # Extrahiere Status und weitere Infos wenn Dokument gefunden
    if [ -n "$FOUND_DOCS" ] && [ -f "$FOUND_DOCS" ]; then
        # Status extrahieren
        STATUS=$(grep -E "^\*\*Status:\*\*" "$FOUND_DOCS" | head -1 | sed 's/.*Status:\*\* //')
        if [ -n "$STATUS" ]; then
            echo -e "${GREEN}📊 Modul-Status:${NC} $STATUS"
        fi
        
        # Suche nach nächsten Schritten
        if grep -q -E "(NÄCHSTER SCHRITT|Next Step|TODO|Checklist)" "$FOUND_DOCS"; then
            echo ""
            echo -e "${GREEN}🎯 Aufgaben im Dokument gefunden${NC}"
            echo "Öffne das Dokument für Details: cat $FOUND_DOCS"
        fi
        
        # Exportiere für andere Scripts
        export ACTIVE_MODULE_DOC="$FOUND_DOCS"
    else
        echo -e "${YELLOW}⚠️  Kein Modul-Dokument gefunden${NC}"
        echo ""
        echo "Mögliche Gründe:"
        echo "1. Modul-Ordner existiert nicht in docs/features/ACTIVE/"
        echo "2. README.md fehlt im Modul-Ordner"
        echo "3. Modul-Name in .current-focus stimmt nicht mit Ordnername überein"
    fi
    
    # Exportiere Variablen für andere Scripts
    export ACTIVE_FEATURE="$FEATURE"
    export ACTIVE_MODULE="$MODULE"
    
else
    echo -e "${YELLOW}⚠️  Kein spezifisches Modul in .current-focus definiert${NC}"
    echo "Nur Feature $FEATURE ist aktiv"
fi

echo ""
echo "💡 Tipp: Aktualisiere .current-focus mit update-focus.sh"
echo "   Beispiel: ./scripts/update-focus.sh FC-008 'Security Foundation'"