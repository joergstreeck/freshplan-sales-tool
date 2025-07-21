#!/bin/bash

# Structure Integrity Test
# Verhindert Duplikate und strukturelle Probleme

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Project root
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$PROJECT_ROOT"

echo -e "${BLUE}╔══════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║      Structure Integrity Test                    ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════╝${NC}"
echo ""

ERRORS=0
WARNINGS=0

# Test 1: Feature-Code Duplikate
echo "🔍 Prüfe Feature-Code Duplikate..."
echo ""

# Erstelle temporäre Datei für Feature-Codes
TEMP_FILE=$(mktemp)
trap "rm -f $TEMP_FILE" EXIT

# Sammle alle Feature-Codes mit ihren Pfaden
find docs/features -name "*_TECH_CONCEPT.md" -o -name "*_CLAUDE_TECH.md" | grep -v "LEGACY" | while read -r file; do
    feature_code=$(basename "$file" | grep -oE "(FC-[0-9]+|M[0-9]+)" | head -1 || true)
    if [ -n "$feature_code" ]; then
        echo "$feature_code|$file" >> "$TEMP_FILE"
    fi
done

# Finde Duplikate (mehr als 2 Dateien = Problem)
sort "$TEMP_FILE" | awk -F'|' '{
    code=$1
    file=$2
    if (code in files) {
        files[code] = files[code] "|" file
        count[code]++
    } else {
        files[code] = file
        count[code] = 1
    }
}
END {
    for (code in files) {
        # Nur wenn mehr als 2 Dateien (TECH_CONCEPT + CLAUDE_TECH = normal)
        if (count[code] > 2) {
            print code "|" files[code] "|" count[code]
        }
    }
}' | while IFS='|' read -r code files_list count; do
    echo -e "${RED}❌ Duplikat gefunden: $code (${count} Dateien)${NC}"
    echo "$files_list" | tr '|' '\n' | while read -r loc; do
        echo "   - $loc"
    done
    ((ERRORS++))
    echo ""
done

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ Keine Feature-Code Duplikate gefunden${NC}"
fi
echo ""

# Test 2: Ordner-Struktur Konsistenz
echo "🔍 Prüfe Ordner-Struktur Konsistenz..."
echo ""

# Prüfe bekannte Probleme
find docs/features/PLANNED docs/features/ACTIVE -name "FC-*" -type f | while read -r file; do
    case "$file" in
        *"/01_customer_management/FC-019"*)
            echo -e "${YELLOW}⚠️  FC-019 sollte in 19_advanced_metrics sein${NC}"
            echo "   Aktuell: $file"
            ((WARNINGS++))
            ;;
        *"/02_opportunity_pipeline/FC-016"*)
            echo -e "${YELLOW}⚠️  FC-016 sollte in 18_opportunity_cloning sein${NC}"
            echo "   Aktuell: $file"
            ((WARNINGS++))
            ;;
        *"/18_sales_gamification/FC-017"*)
            echo -e "${YELLOW}⚠️  FC-017 sollte in 99_sales_gamification sein${NC}"
            echo "   Aktuell: $file"
            ((WARNINGS++))
            ;;
    esac
done

# Test 3: Mehrfache Feature-Nummern in verschiedenen Ordnern
echo ""
echo "🔍 Prüfe mehrfache Feature-Nummern..."
echo ""

# Temporäre Datei für Feature-Ordner-Zuordnung
TEMP_DIRS=$(mktemp)

# Sammle Feature-Codes pro Ordner
find docs/features/PLANNED docs/features/ACTIVE -type d -mindepth 1 -maxdepth 1 | while read -r dir; do
    for file in "$dir"/*_TECH_CONCEPT.md "$dir"/*_CLAUDE_TECH.md 2>/dev/null; do
        if [ -f "$file" ]; then
            feature_code=$(basename "$file" | grep -oE "FC-[0-9]+" | head -1 || true)
            if [ -n "$feature_code" ]; then
                echo "$feature_code|$(basename "$dir")" >> "$TEMP_DIRS"
            fi
        fi
    done
done

# Finde Feature-Codes in mehreren Ordnern
sort -u "$TEMP_DIRS" | awk -F'|' '{
    code=$1
    dir=$2
    if (code in dirs && dirs[code] !~ dir) {
        dirs[code] = dirs[code] "|" dir
        multi[code] = 1
    } else {
        dirs[code] = dir
    }
}
END {
    for (code in multi) {
        print code "|" dirs[code]
    }
}' | while IFS='|' read -r code dirs_list; do
    echo -e "${RED}❌ $code existiert in mehreren Ordnern:${NC}"
    echo "$dirs_list" | tr '|' '\n' | while read -r dir; do
        echo "   - $dir"
    done
    ((ERRORS++))
    echo ""
done

rm -f "$TEMP_DIRS"

# Test 4: Platzhalter-Dokumente
echo "🔍 Prüfe auf Platzhalter-Dokumente..."
echo ""

PLACEHOLDER_COUNT=$(find docs/features -name "*_TECH_CONCEPT.md" -type f -exec grep -l "Status:.*Placeholder Document" {} \; | wc -l | tr -d ' ')

if [ "$PLACEHOLDER_COUNT" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  $PLACEHOLDER_COUNT Platzhalter-Dokumente gefunden${NC}"
    echo ""
    echo "Top 5 Platzhalter:"
    find docs/features -name "*_TECH_CONCEPT.md" -type f -exec grep -l "Status:.*Placeholder Document" {} \; | head -5 | while read -r file; do
        echo "   - ${file#$PROJECT_ROOT/}"
    done
    WARNINGS=$((WARNINGS + PLACEHOLDER_COUNT))
else
    echo -e "${GREEN}✅ Keine Platzhalter-Dokumente${NC}"
fi
echo ""

# Test 5: Module (M1-M8) Konsistenz
echo "🔍 Prüfe Module (M1-M8) Konsistenz..."
echo ""

for module in M1 M2 M3 M4 M5 M6 M7 M8; do
    locations=$(find docs/features -name "${module}_*" -type f | grep -v "GUIDE\|IMPLEMENTATION" | wc -l | tr -d ' ')
    
    if [ "$locations" -gt 2 ]; then  # TECH_CONCEPT + CLAUDE_TECH = 2
        echo -e "${YELLOW}⚠️  $module existiert an $locations Stellen:${NC}"
        find docs/features -name "${module}_*" -type f | grep -v "GUIDE\|IMPLEMENTATION" | while read -r file; do
            echo "   - ${file#$PROJECT_ROOT/}"
        done
        ((WARNINGS++))
        echo ""
    fi
done

# Zusammenfassung
echo ""
echo -e "${BLUE}════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}ZUSAMMENFASSUNG${NC}"
echo ""

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✅ Struktur ist sauber!${NC}"
    echo ""
    echo "Keine strukturellen Probleme gefunden."
    exit 0
else
    if [ $ERRORS -gt 0 ]; then
        echo -e "${RED}❌ $ERRORS kritische Fehler gefunden${NC}"
    fi
    if [ $WARNINGS -gt 0 ]; then
        echo -e "${YELLOW}⚠️  $WARNINGS Warnungen${NC}"
    fi
    echo ""
    echo "Empfehlungen:"
    echo "1. Duplikate in einen Ordner konsolidieren"
    echo "2. Feature-Codes eindeutig vergeben"
    echo "3. Platzhalter durch echte Dokumente ersetzen"
    echo "4. Ordnerstruktur bereinigen"
    
    # Exit mit Fehler wenn kritische Fehler
    if [ $ERRORS -gt 0 ]; then
        exit 1
    else
        exit 0
    fi
fi