#!/bin/bash
# validate-structure.sh - Validiert die Dokumentationsstruktur
# Autor: Claude
# Datum: 21.07.2025
# Zweck: Konsistenz sicherstellen VOR jeder Arbeit

set -e

echo "🔍 DOKUMENTATIONS-STRUKTUR VALIDIERUNG"
echo "======================================"
echo ""

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Zähler für Probleme
ERRORS=0
WARNINGS=0

# 1. Feature Registry Check
echo "1️⃣  Prüfe Feature Registry..."
if [ ! -f "docs/FEATURE_REGISTRY.md" ]; then
    echo -e "${RED}❌ FEHLER: FEATURE_REGISTRY.md fehlt!${NC}"
    echo "   Erstelle mit: cp docs/templates/FEATURE_REGISTRY_TEMPLATE.md docs/FEATURE_REGISTRY.md"
    ((ERRORS++))
else
    echo -e "${GREEN}✅ Feature Registry vorhanden${NC}"
fi

# 2. Duplikate Check
echo ""
echo "2️⃣  Prüfe Feature-Code Duplikate..."
DUPLICATES=$(find docs/features -name "FC-*.md" -o -name "M*.md" | \
    grep -oE "(FC-[0-9]{3}|M[0-9]+)" | \
    sort | uniq -c | \
    awk '$1 > 2 {print $2 " (" $1 " Dateien)"}')

if [ -n "$DUPLICATES" ]; then
    echo -e "${RED}❌ Feature-Code Duplikate gefunden:${NC}"
    echo "$DUPLICATES" | while read dup; do
        echo "   - $dup"
        ((ERRORS++))
    done
else
    echo -e "${GREEN}✅ Keine Feature-Code Duplikate${NC}"
fi

# 3. Ordner-Nummerierung Check
echo ""
echo "3️⃣  Prüfe Ordner-Nummerierung..."
DUPLICATE_DIRS=$(find docs/features/PLANNED -maxdepth 1 -type d -name "[0-9]*" | \
    grep -oE "[0-9]+" | \
    sort | uniq -c | \
    awk '$1 > 1 {print $2 " (" $1 " Ordner)"}')

if [ -n "$DUPLICATE_DIRS" ]; then
    echo -e "${YELLOW}⚠️  Doppelte Ordner-Nummern:${NC}"
    echo "$DUPLICATE_DIRS" | while read dup; do
        echo "   - Nummer $dup"
        ((WARNINGS++))
    done
else
    echo -e "${GREEN}✅ Ordner-Nummerierung eindeutig${NC}"
fi

# 4. Platzhalter Check
echo ""
echo "4️⃣  Prüfe Platzhalter-Dokumente..."
PLACEHOLDERS=$(grep -r "Status:.*Placeholder Document" docs/features 2>/dev/null | wc -l || echo "0")

if [ "$PLACEHOLDERS" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  $PLACEHOLDERS Platzhalter-Dokumente gefunden${NC}"
    echo "   Nutze: ./scripts/clean-placeholders.sh zum Entfernen"
    ((WARNINGS++))
else
    echo -e "${GREEN}✅ Keine Platzhalter-Dokumente${NC}"
fi

# 5. Konsistenz mit Registry
echo ""
echo "5️⃣  Prüfe Konsistenz mit Registry..."
if [ -f "docs/FEATURE_REGISTRY.md" ]; then
    # Extrahiere alle FC-Codes aus Registry
    REGISTRY_CODES=$(grep -E "^\| FC-[0-9]{3} " docs/FEATURE_REGISTRY.md | awk -F'|' '{print $2}' | tr -d ' ')
    
    # Finde alle FC-Codes in Dateisystem
    FS_CODES=$(find docs/features -name "FC-*.md" | grep -oE "FC-[0-9]{3}" | sort -u)
    
    # Vergleiche
    MISSING_IN_REGISTRY=$(comm -13 <(echo "$REGISTRY_CODES" | sort) <(echo "$FS_CODES" | sort))
    MISSING_IN_FS=$(comm -23 <(echo "$REGISTRY_CODES" | sort) <(echo "$FS_CODES" | sort))
    
    if [ -n "$MISSING_IN_REGISTRY" ]; then
        echo -e "${RED}❌ Features im Dateisystem aber nicht in Registry:${NC}"
        echo "$MISSING_IN_REGISTRY" | while read code; do
            echo "   - $code"
            ((ERRORS++))
        done
    fi
    
    if [ -n "$MISSING_IN_FS" ]; then
        echo -e "${YELLOW}⚠️  Features in Registry aber nicht im Dateisystem:${NC}"
        echo "$MISSING_IN_FS" | while read code; do
            echo "   - $code"
            ((WARNINGS++))
        done
    fi
    
    if [ -z "$MISSING_IN_REGISTRY" ] && [ -z "$MISSING_IN_FS" ]; then
        echo -e "${GREEN}✅ Registry und Dateisystem synchron${NC}"
    fi
fi

# 6. CLAUDE_TECH Check
echo ""
echo "6️⃣  Prüfe CLAUDE_TECH Vollständigkeit..."
TECH_WITHOUT_CLAUDE=$(find docs/features -name "*_TECH_CONCEPT.md" | while read tech; do
    claude_tech="${tech/_TECH_CONCEPT/_CLAUDE_TECH}"
    if [ ! -f "$claude_tech" ]; then
        basename "$tech"
    fi
done)

if [ -n "$TECH_WITHOUT_CLAUDE" ]; then
    echo -e "${YELLOW}⚠️  TECH_CONCEPT ohne CLAUDE_TECH Version:${NC}"
    echo "$TECH_WITHOUT_CLAUDE" | head -5 | while read file; do
        echo "   - $file"
    done
    MISSING_COUNT=$(echo "$TECH_WITHOUT_CLAUDE" | wc -l)
    if [ "$MISSING_COUNT" -gt 5 ]; then
        echo "   ... und $((MISSING_COUNT - 5)) weitere"
    fi
    ((WARNINGS++))
else
    echo -e "${GREEN}✅ Alle TECH_CONCEPT haben CLAUDE_TECH${NC}"
fi

# Zusammenfassung
echo ""
echo "======================================"
echo "📊 ZUSAMMENFASSUNG"
echo "======================================"

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✅ Dokumentationsstruktur ist konsistent!${NC}"
    exit 0
else
    if [ $ERRORS -gt 0 ]; then
        echo -e "${RED}❌ $ERRORS kritische Fehler gefunden${NC}"
    fi
    if [ $WARNINGS -gt 0 ]; then
        echo -e "${YELLOW}⚠️  $WARNINGS Warnungen${NC}"
    fi
    
    echo ""
    echo "🔧 Empfohlene Aktionen:"
    echo "1. ./scripts/clean-placeholders.sh    # Platzhalter entfernen"
    echo "2. ./scripts/fix-duplicates.sh        # Duplikate bereinigen"
    echo "3. ./scripts/sync-registry.sh         # Registry synchronisieren"
    
    # Exit mit Fehler wenn kritische Probleme
    if [ $ERRORS -gt 0 ]; then
        exit 1
    fi
fi