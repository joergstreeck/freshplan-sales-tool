#!/bin/bash

# Reality Check Script - Prüft Plan vs. Code Reality
# Usage: ./scripts/reality-check.sh FC-008

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

FEATURE=$1

if [ -z "$FEATURE" ]; then
    echo "Usage: $0 <FEATURE-CODE>"
    echo "Example: $0 FC-008"
    exit 1
fi

echo -e "${BLUE}🔍 Reality Check für $FEATURE${NC}"
echo "===================================="
echo ""
echo "${YELLOW}📌 Der Reality Check Prozess:${NC}"
echo "1️⃣  Plan lesen und verstehen"
echo "2️⃣  Code lesen (VERPFLICHTEND)"
echo "3️⃣  Abgleich und Bestätigung"
echo ""
echo "===================================="

# Find plan file - prefer CLAUDE_TECH over TECH_CONCEPT
CLAUDE_TECH=$(find docs/features -name "${FEATURE}_CLAUDE_TECH.md" 2>/dev/null | head -1)
TECH_CONCEPT=$(find docs/features -name "${FEATURE}_TECH_CONCEPT.md" 2>/dev/null | head -1)

# Prefer CLAUDE_TECH if it exists
if [ -n "$CLAUDE_TECH" ]; then
    PLAN_FILE="$CLAUDE_TECH"
elif [ -n "$TECH_CONCEPT" ]; then
    PLAN_FILE="$TECH_CONCEPT"
else
    PLAN_FILE=""
fi

if [ -z "$PLAN_FILE" ]; then
    echo -e "${RED}❌ Kein Plan gefunden für $FEATURE${NC}"
    exit 1
fi

echo -e "\n${GREEN}SCHRITT 1: Plan lesen${NC}"
echo -e "📋 Plan gefunden: $PLAN_FILE"
echo -e "${YELLOW}→ Bitte lies jetzt den Plan durch bevor du fortfährst!${NC}"
echo ""
read -p "Hast du den Plan gelesen? (j/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Jj]$ ]]; then
    echo -e "${RED}❌ Abgebrochen - bitte lies erst den Plan${NC}"
    exit 1
fi

# Extract code file references
echo -e "\n${GREEN}SCHRITT 2: Code lesen (VERPFLICHTEND)${NC}"
echo "🔎 Extrahiere Code-Referenzen aus Plan..."
grep -Eo "[a-zA-Z0-9_/.-]+\.(tsx?|jsx?|java|py)" "$PLAN_FILE" 2>/dev/null | sort -u > /tmp/planned_files.txt || true

# Also check for common patterns
grep -Eo "(frontend|backend)/[a-zA-Z0-9_/.-]+\.(tsx?|jsx?|java|py)" "$PLAN_FILE" 2>/dev/null | sort -u >> /tmp/planned_files.txt || true

# Remove duplicates
sort -u /tmp/planned_files.txt -o /tmp/planned_files.txt

if [ ! -s /tmp/planned_files.txt ]; then
    echo -e "${YELLOW}⚠️  Keine Code-Dateien im Plan gefunden${NC}"
    echo "   Tipp: Füge konkrete Dateipfade zum Plan hinzu"
    echo ""
    echo "${YELLOW}Bitte gib die wichtigsten Code-Dateien manuell an:${NC}"
    echo "(eine pro Zeile, leere Zeile zum Beenden)"
    > /tmp/planned_files.txt
    while IFS= read -r line; do
        [ -z "$line" ] && break
        echo "$line" >> /tmp/planned_files.txt
    done
fi

if [ -s /tmp/planned_files.txt ]; then
    echo ""
    echo "📁 Zu lesende Code-Dateien:"
    cat /tmp/planned_files.txt
    echo ""
    echo -e "${YELLOW}→ Bitte lies JETZT diese Code-Dateien durch!${NC}"
    echo "   Achte besonders auf:"
    echo "   - Stimmt die Struktur mit dem Plan überein?"
    echo "   - Gibt es unerwartete Dependencies?"
    echo "   - Sind die geplanten Schnittstellen vorhanden?"
    echo ""
    read -p "Hast du alle relevanten Code-Dateien gelesen? (j/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Jj]$ ]]; then
        echo -e "${RED}❌ Abgebrochen - bitte lies erst den Code${NC}"
        exit 1
    fi
fi

# Check each file
echo -e "\n${GREEN}SCHRITT 3: Abgleich und Bestätigung${NC}"
echo "✓ Prüfe Existenz..."
FAIL_COUNT=0
PASS_COUNT=0

while IFS= read -r file; do
    # Try exact path first
    if [ -f "$file" ]; then
        echo -e "  ${GREEN}✅ $file${NC}"
        ((PASS_COUNT++))
    else
        # Try to find file anywhere
        FOUND=$(find . -name "$(basename "$file")" -type f 2>/dev/null | head -1)
        if [ -n "$FOUND" ]; then
            echo -e "  ${YELLOW}⚠️  $file -> gefunden als: $FOUND${NC}"
            ((PASS_COUNT++))
        else
            echo -e "  ${RED}❌ $file - NICHT GEFUNDEN${NC}"
            ((FAIL_COUNT++))
        fi
    fi
done < /tmp/planned_files.txt

echo ""
echo "===================================="
echo -e "Zusammenfassung: ${GREEN}$PASS_COUNT OK${NC}, ${RED}$FAIL_COUNT FEHLEN${NC}"

# Generate interactive checklist
echo ""
echo "## Reality Check Checkliste:"
echo ""
echo "${YELLOW}Bitte bestätige jeden Punkt:${NC}"
echo ""

CHECKLIST_FAILED=false

# Checklist items
declare -a checklist=(
    "Plan und Code-Struktur stimmen überein"
    "Alle APIs/Endpoints sind wie geplant"
    "Datenstrukturen sind kompatibel"
    "Dependencies sind installiert"
    "Keine unerwarteten Abhängigkeiten gefunden"
)

for item in "${checklist[@]}"; do
    read -p "✓ $item? (j/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Jj]$ ]]; then
        echo -e "  ${RED}→ Nicht bestätigt${NC}"
        CHECKLIST_FAILED=true
    else
        echo -e "  ${GREEN}→ OK${NC}"
    fi
done

if [ "$CHECKLIST_FAILED" = true ]; then
    echo ""
    echo -e "${RED}⚠️  Nicht alle Punkte bestätigt${NC}"
    echo "Bitte kläre die Diskrepanzen bevor du mit der Implementation beginnst!"
    FAIL_COUNT=$((FAIL_COUNT + 1))
fi

# Final summary
echo ""
echo "===================================="
echo "${BLUE}📊 ZUSAMMENFASSUNG${NC}"
echo ""
echo "✓ Plan gelesen und verstanden"
echo "✓ Code-Dateien durchgesehen"
echo "✓ Abgleich durchgeführt"
echo ""

# Final result
if [ "$FAIL_COUNT" -eq 0 ] && [ "$CHECKLIST_FAILED" != true ]; then
    echo -e "${GREEN}✅ REALITY CHECK PASSED${NC}"
    echo ""
    echo "   Plan und Code stimmen überein!"
    echo "   → Du kannst mit der Implementation beginnen!"
    echo ""
    echo "${YELLOW}Tipp:${NC} Halte Plan und Code während der Arbeit synchron!"
    
    # Log success for handover
    echo "- [✅] $FEATURE: Reality Check passed - Plan/Code synchron ($(date '+%d.%m. %H:%M'))" >> .reality-check-log
else
    echo -e "${RED}❌ REALITY CHECK FAILED${NC}"
    echo ""
    if [ "$FAIL_COUNT" -gt 0 ]; then
        echo "   → $FAIL_COUNT Dateien fehlen oder sind falsch benannt"
    fi
    if [ "$CHECKLIST_FAILED" = true ]; then
        echo "   → Diskrepanzen zwischen Plan und Code gefunden"
    fi
    echo ""
    echo "${YELLOW}STOPP!${NC} Bitte NICHT mit der Implementation beginnen!"
    echo ""
    echo "Nächste Schritte:"
    echo "1. Kläre alle Diskrepanzen"
    echo "2. Update den Plan ODER den Code"
    echo "3. Führe Reality Check erneut aus"
    echo ""
    echo "${BLUE}Warum ist das wichtig?${NC}"
    echo "→ Mit veralteten Plänen verschwendest du 2+ Stunden Arbeit"
    echo "→ Mit aktuellem Plan arbeitest du effizient und richtig"
    
    # Log failure for handover
    if [ "$FAIL_COUNT" -gt 0 ]; then
        echo "- [❌] $FEATURE: Reality Check failed - $FAIL_COUNT Dateien fehlen ($(date '+%d.%m. %H:%M'))" >> .reality-check-log
    else
        echo "- [❌] $FEATURE: Reality Check failed - Plan/Code Diskrepanz ($(date '+%d.%m. %H:%M'))" >> .reality-check-log
    fi
fi

# Cleanup
rm -f /tmp/planned_files.txt