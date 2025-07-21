#!/bin/bash

# Vereinfachter Link-Integritäts-Test für CLAUDE_TECH Struktur

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Counters
TOTAL=0
BROKEN=0
VALID=0

# Project root
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$PROJECT_ROOT"

echo -e "${BLUE}=== CLAUDE_TECH Link-Integritäts-Test (Vereinfacht) ===${NC}"
echo "Start: $(date)"
echo ""

# Results file
RESULTS="tests/link-test-results.log"
> "$RESULTS"

# Function to check a single file
check_file() {
    local source_file="$1"
    local target_file="$2"
    local line_num="$3"
    
    ((TOTAL++))
    
    # Clean the path
    target_file="${target_file#./}"
    
    # Check existence
    if [ -f "$target_file" ] || [ -f "$PROJECT_ROOT/$target_file" ]; then
        ((VALID++))
        echo -e "  ${GREEN}✓${NC} $target_file"
    else
        ((BROKEN++))
        echo -e "  ${RED}✗${NC} $target_file (Zeile $line_num)"
        echo "BROKEN: $target_file in $source_file:$line_num" >> "$RESULTS"
    fi
}

echo -e "${BLUE}Phase 1: Master Plan V5${NC}"
echo "Prüfe: docs/CRM_COMPLETE_MASTER_PLAN_V5.md"

# Extract markdown links manually
grep -n '\[.*\](.*)' docs/CRM_COMPLETE_MASTER_PLAN_V5.md | while IFS=':' read -r line_num content; do
    # Extract paths from markdown links, excluding URLs
    echo "$content" | grep -o '\[[^]]*\]([^)]*)' | grep -v 'http' | sed 's/.*](//' | sed 's/)$//' | while read -r path; do
        if [ ! -z "$path" ] && [[ "$path" != "#"* ]]; then
            check_file "docs/CRM_COMPLETE_MASTER_PLAN_V5.md" "$path" "$line_num"
        fi
    done
done

echo ""
echo -e "${BLUE}Phase 2: CLAUDE_TECH Dokumente${NC}"

# Check all CLAUDE_TECH files
find docs/features -name "*_CLAUDE_TECH.md" -type f | head -10 | while read -r file; do
    echo "Prüfe: $file"
    
    # Extract markdown links
    grep -n '\[.*\](.*)' "$file" 2>/dev/null | while IFS=':' read -r line_num content; do
        echo "$content" | grep -o '\[[^]]*\]([^)]*)' | grep -v 'http' | sed 's/.*](//' | sed 's/)$//' | while read -r path; do
            if [ ! -z "$path" ] && [[ "$path" != "#"* ]]; then
                check_file "$file" "$path" "$line_num"
            fi
        done
    done || true
done

echo ""
echo -e "${BLUE}=== ZUSAMMENFASSUNG ===${NC}"
echo "Geprüfte Links: $TOTAL"
echo -e "Valide Links:   ${GREEN}$VALID${NC}"
echo -e "Defekte Links:  ${RED}$BROKEN${NC}"

if [ $BROKEN -gt 0 ]; then
    echo ""
    echo -e "${YELLOW}Defekte Links gespeichert in: $RESULTS${NC}"
    exit 1
else
    echo ""
    echo -e "${GREEN}✅ Alle Links sind valide!${NC}"
    exit 0
fi