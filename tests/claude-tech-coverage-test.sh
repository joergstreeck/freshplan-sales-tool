#!/bin/bash

# CLAUDE_TECH Coverage Test
# Prüft, ob alle Features eine CLAUDE_TECH Version haben

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
NC='\033[0m'

# Project root
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$PROJECT_ROOT"

echo -e "${BLUE}╔══════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║      CLAUDE_TECH Coverage Test                   ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════╝${NC}"
echo ""
echo "Projekt: $PROJECT_ROOT"
echo "Start: $(date)"
echo ""

# Results
RESULTS_FILE="tests/coverage-test-results.log"
SUMMARY_FILE="tests/coverage-test-summary.md"
> "$RESULTS_FILE"

# Initialize counters
declare -A FEATURE_COUNTS
declare -A MISSING_CLAUDE_TECH
declare -A HAS_CLAUDE_TECH
TOTAL_FEATURES=0
CLAUDE_TECH_COUNT=0

# Function to check a feature directory
check_feature_dir() {
    local dir="$1"
    local feature_name=$(basename "$dir")
    
    # Find all TECH_CONCEPT files
    local tech_concepts=$(find "$dir" -name "*_TECH_CONCEPT.md" -type f | wc -l)
    local claude_techs=$(find "$dir" -name "*_CLAUDE_TECH.md" -type f | wc -l)
    
    if [ $tech_concepts -gt 0 ] || [ $claude_techs -gt 0 ]; then
        ((TOTAL_FEATURES++))
        
        # List all features found
        find "$dir" -name "*.md" -type f | grep -E "(FC-|M[0-9]+)" | while read -r file; do
            local basename=$(basename "$file")
            local feature_id=$(echo "$basename" | grep -oE "(FC-[0-9]+|M[0-9]+)" | head -1)
            
            if [ ! -z "$feature_id" ]; then
                if [[ "$basename" =~ CLAUDE_TECH ]]; then
                    HAS_CLAUDE_TECH["$feature_id"]=1
                    echo "✅ $feature_id → $file" >> "$RESULTS_FILE"
                elif [[ "$basename" =~ TECH_CONCEPT ]] && [ -z "${HAS_CLAUDE_TECH[$feature_id]:-}" ]; then
                    MISSING_CLAUDE_TECH["$feature_id"]="$file"
                    echo "❌ $feature_id → Nur TECH_CONCEPT: $file" >> "$RESULTS_FILE"
                fi
            fi
        done
    fi
}

# Phase 1: Check ACTIVE features
echo -e "${BLUE}═══ Phase 1: ACTIVE Features ═══${NC}"
echo ""

find docs/features/ACTIVE -type d -mindepth 1 -maxdepth 1 | sort | while read -r dir; do
    echo -e "${BLUE}Prüfe:${NC} $(basename "$dir")"
    check_feature_dir "$dir"
    
    # Check subdirectories
    find "$dir" -type d -mindepth 1 -maxdepth 1 | while read -r subdir; do
        echo -e "  ${BLUE}└─${NC} $(basename "$subdir")"
        check_feature_dir "$subdir"
    done
done

# Phase 2: Check PLANNED features
echo -e "\n${BLUE}═══ Phase 2: PLANNED Features ═══${NC}"
echo ""

find docs/features/PLANNED -type d -mindepth 1 -maxdepth 1 | sort | while read -r dir; do
    echo -e "${BLUE}Prüfe:${NC} $(basename "$dir")"
    check_feature_dir "$dir"
done

# Count all CLAUDE_TECH files
CLAUDE_TECH_FILES=$(find docs/features -name "*_CLAUDE_TECH.md" -type f | sort)
CLAUDE_TECH_COUNT=$(echo "$CLAUDE_TECH_FILES" | wc -l)

# Count all TECH_CONCEPT files
TECH_CONCEPT_FILES=$(find docs/features -name "*_TECH_CONCEPT.md" -type f | sort)
TECH_CONCEPT_COUNT=$(echo "$TECH_CONCEPT_FILES" | wc -l)

# Generate summary
{
    echo "# CLAUDE_TECH Coverage Test Zusammenfassung"
    echo ""
    echo "Ausgeführt: $(date)"
    echo ""
    echo "## 📊 Statistiken"
    echo ""
    echo "| Metrik | Anzahl |"
    echo "|--------|--------|"
    echo "| TECH_CONCEPT Dokumente | $TECH_CONCEPT_COUNT |"
    echo "| CLAUDE_TECH Dokumente | $CLAUDE_TECH_COUNT |"
    echo "| Coverage | $(( CLAUDE_TECH_COUNT * 100 / TECH_CONCEPT_COUNT ))% |"
    echo ""
    
    echo "## 📋 CLAUDE_TECH Dokumente nach Feature"
    echo ""
    echo "### ACTIVE Features"
    echo "$CLAUDE_TECH_FILES" | grep "/ACTIVE/" | while read -r file; do
        local feature_id=$(basename "$file" | grep -oE "(FC-[0-9]+|M[0-9]+)" | head -1)
        echo "- ✅ $feature_id: \`$(echo "$file" | sed "s|$PROJECT_ROOT/||")\`"
    done
    
    echo ""
    echo "### PLANNED Features"
    echo "$CLAUDE_TECH_FILES" | grep "/PLANNED/" | while read -r file; do
        local feature_id=$(basename "$file" | grep -oE "(FC-[0-9]+|M[0-9]+)" | head -1)
        echo "- ✅ $feature_id: \`$(echo "$file" | sed "s|$PROJECT_ROOT/||")\`"
    done
    
    # Check for missing CLAUDE_TECH
    local missing=$(comm -23 <(echo "$TECH_CONCEPT_FILES" | sed 's/_TECH_CONCEPT/_CLAUDE_TECH/g' | sort) <(echo "$CLAUDE_TECH_FILES" | sort))
    
    if [ ! -z "$missing" ]; then
        echo ""
        echo "## ❌ Fehlende CLAUDE_TECH Dokumente"
        echo ""
        echo "$missing" | while read -r file; do
            local concept_file=$(echo "$file" | sed 's/_CLAUDE_TECH/_TECH_CONCEPT/g')
            if [ -f "$concept_file" ]; then
                local feature_id=$(basename "$file" | grep -oE "(FC-[0-9]+|M[0-9]+)" | head -1)
                echo "- ❌ $feature_id: Nur TECH_CONCEPT vorhanden"
            fi
        done
    fi
    
    echo ""
    echo "## 🎯 Ergebnis"
    echo ""
    if [ "$CLAUDE_TECH_COUNT" -eq "$TECH_CONCEPT_COUNT" ]; then
        echo "✅ **100% Coverage! Alle Features haben CLAUDE_TECH Versionen!**"
    else
        local missing_count=$((TECH_CONCEPT_COUNT - CLAUDE_TECH_COUNT))
        echo "⚠️ **$missing_count Features fehlen noch CLAUDE_TECH Versionen**"
    fi
    
    echo ""
    echo "---"
    echo "_Test abgeschlossen: $(date)_"
} > "$SUMMARY_FILE"

# Display results
echo -e "\n${BLUE}╔══════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                 ZUSAMMENFASSUNG                   ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════╝${NC}"
echo ""
echo "TECH_CONCEPT Dokumente:  $TECH_CONCEPT_COUNT"
echo -e "CLAUDE_TECH Dokumente:   ${GREEN}$CLAUDE_TECH_COUNT${NC}"

if [ "$CLAUDE_TECH_COUNT" -eq "$TECH_CONCEPT_COUNT" ]; then
    echo -e "Coverage:                ${GREEN}100%${NC}"
    echo ""
    echo -e "${GREEN}✅ Perfekte Coverage! Alle Features haben CLAUDE_TECH Versionen!${NC}"
else
    local coverage=$(( CLAUDE_TECH_COUNT * 100 / TECH_CONCEPT_COUNT ))
    echo -e "Coverage:                ${YELLOW}$coverage%${NC}"
    local missing=$((TECH_CONCEPT_COUNT - CLAUDE_TECH_COUNT))
    echo ""
    echo -e "${YELLOW}⚠️  $missing Features fehlen noch CLAUDE_TECH Versionen${NC}"
fi

echo ""
echo -e "${MAGENTA}Details siehe:${NC} $SUMMARY_FILE"

# Exit with appropriate code
if [ "$CLAUDE_TECH_COUNT" -eq "$TECH_CONCEPT_COUNT" ]; then
    exit 0
else
    exit 1
fi