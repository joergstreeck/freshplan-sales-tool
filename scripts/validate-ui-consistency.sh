#!/bin/bash

# UI Consistency Validation Script
# Zweck: UI-Konsistenz nach Frontend-Entwicklung prüfen
# Autor: Claude
# Datum: 17.07.2025

set -e

# Farben für Output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Parameter
FILE_PATH=${1:-""}

if [ -z "$FILE_PATH" ]; then
    echo -e "${RED}❌ Fehler: Kein Dateipfad angegeben!${NC}"
    echo "Usage: $0 <file-path>"
    exit 1
fi

if [ ! -f "$FILE_PATH" ]; then
    echo -e "${RED}❌ Fehler: Datei nicht gefunden: $FILE_PATH${NC}"
    exit 1
fi

echo -e "${YELLOW}🔍 Validiere UI-Konsistenz...${NC}"
echo "Datei: $FILE_PATH"
echo ""

# Variablen für Probleme
ISSUES=()

# Check 1: Base Components verwendet?
echo "Prüfe Base Components..."
if [[ "$FILE_PATH" == *".tsx" ]]; then
    # Für Pages: PageWrapper prüfen
    if [[ "$FILE_PATH" == *"Page.tsx" ]] || [[ "$FILE_PATH" == *"page.tsx" ]]; then
        if ! grep -q "PageWrapper" "$FILE_PATH"; then
            ISSUES+=("❌ PageWrapper nicht verwendet (für Pages erforderlich)")
        else
            echo -e "${GREEN}✅ PageWrapper verwendet${NC}"
        fi
    fi
    
    # Für Components: SectionCard prüfen
    if ! grep -q -E "(PageWrapper|SectionCard|StandardTable|FormDialog)" "$FILE_PATH"; then
        echo -e "${YELLOW}⚠️  Keine Base Components gefunden (könnte OK sein für kleine Components)${NC}"
    else
        echo -e "${GREEN}✅ Base Component(s) verwendet${NC}"
    fi
fi

# Check 2: Hardcoded Text?
echo ""
echo "Prüfe auf hardcoded Texte..."
# Suche nach Strings in JSX
HARDCODED=$(grep -E '>[^<]*[A-Za-z]{3,}[^<]*<' "$FILE_PATH" | grep -v -E '(t\(|{/\*|//|className=|style=|data-|aria-)' || true)
if [ ! -z "$HARDCODED" ]; then
    ISSUES+=("❌ Mögliche hardcoded Texte gefunden:")
    while IFS= read -r line; do
        ISSUES+=("   $line")
    done <<< "$HARDCODED"
else
    echo -e "${GREEN}✅ Keine hardcoded Texte gefunden${NC}"
fi

# Check 3: useTranslation Hook?
echo ""
echo "Prüfe Translation Hook..."
if [[ "$FILE_PATH" == *".tsx" ]]; then
    if ! grep -q "useTranslation" "$FILE_PATH"; then
        # Prüfe ob überhaupt Text-Content vorhanden
        if grep -q -E '>[^<]+<' "$FILE_PATH"; then
            ISSUES+=("⚠️  useTranslation Hook nicht verwendet (aber Text-Content gefunden)")
        fi
    else
        echo -e "${GREEN}✅ useTranslation Hook verwendet${NC}"
        
        # Prüfe ob t() auch verwendet wird
        if ! grep -q "t(" "$FILE_PATH"; then
            ISSUES+=("⚠️  useTranslation importiert aber t() nicht verwendet")
        fi
    fi
fi

# Check 4: Theme verwendet?
echo ""
echo "Prüfe Theme-Nutzung..."
if grep -q -E "(sx=|theme\.)" "$FILE_PATH"; then
    echo -e "${GREEN}✅ Theme-konform (sx oder theme verwendet)${NC}"
else
    if grep -q "style=" "$FILE_PATH"; then
        ISSUES+=("⚠️  Inline-Styles gefunden - besser Theme/sx verwenden")
    fi
fi

# Check 5: Deutsche Begriffe
echo ""
echo "Prüfe auf englische UI-Begriffe..."
ENGLISH_TERMS=("Dashboard" "Settings" "Save" "Cancel" "Delete" "Edit" "Create" "Update" "Search" "Filter" "Sort" "Export" "Import" "Upload" "Download" "Submit" "Reset" "Back" "Next" "Previous" "Close")
for term in "${ENGLISH_TERMS[@]}"; do
    if grep -q "$term" "$FILE_PATH" | grep -v -E "(//|/\*|import|from|interface|type|const|let|var|function|class)" || false; then
        ISSUES+=("⚠️  Englischer Begriff gefunden: '$term' (nutze deutsche Übersetzung)")
    fi
done

# Ergebnis
echo ""
echo "================================"
if [ ${#ISSUES[@]} -eq 0 ]; then
    echo -e "${GREEN}✨ Komponente ist UI-konform!${NC}"
    
    # Hilfreiche Erinnerungen
    echo ""
    echo -e "${YELLOW}⚠️  Vergiss nicht die Übersetzungen:${NC}"
    # Extrahiere mögliche Translation Keys
    TRANSLATION_KEYS=$(grep -o "t('[^']*')" "$FILE_PATH" | sed "s/t('//g" | sed "s/')//g" | sort | uniq || true)
    if [ ! -z "$TRANSLATION_KEYS" ]; then
        while IFS= read -r key; do
            echo "   - $key"
        done <<< "$TRANSLATION_KEYS"
    fi
else
    echo -e "${RED}❌ UI-Konsistenz-Probleme gefunden:${NC}"
    echo ""
    for issue in "${ISSUES[@]}"; do
        echo "$issue"
    done
    echo ""
    echo -e "${YELLOW}💡 Tipps:${NC}"
    echo "- Nutze Base Components aus frontend/src/components/base/"
    echo "- Verwende immer useTranslation Hook für Texte"
    echo "- Schaue in UI_SPRACHREGELN.md für deutsche Begriffe"
    echo "- Referenz: SettingsPage.tsx oder SalesCockpitV2.tsx"
    exit 1
fi

echo ""
echo "Happy Coding! 🚀"