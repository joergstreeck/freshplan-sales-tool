#!/bin/bash

# Link-Integritäts-Test für CLAUDE_TECH Struktur
# Findet und prüft alle internen Datei-Links

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

# Counters
TOTAL_LINKS=0
BROKEN_LINKS=0
VALID_LINKS=0

echo -e "${BLUE}=== CLAUDE_TECH Link-Integritäts-Test ===${NC}"
echo "Projekt: $PROJECT_ROOT"
echo "Start: $(date)"
echo ""

# Results files
RESULTS_FILE="tests/link-test-results.txt"
BROKEN_FILE="tests/broken-links.txt"
> "$RESULTS_FILE"
> "$BROKEN_FILE"

# Function to check if a file exists
check_link() {
    local source_file="$1"
    local link_path="$2"
    local line_num="$3"
    
    ((TOTAL_LINKS++))
    
    # Clean the path
    link_path=$(echo "$link_path" | sed 's|^/||' | sed 's|^./||')
    
    # Check if file exists
    if [ -f "$PROJECT_ROOT/$link_path" ] || [ -f "$PROJECT_ROOT/docs/$link_path" ]; then
        ((VALID_LINKS++))
        echo -e "  ${GREEN}✓${NC} $link_path"
        echo "✓ $link_path (in $source_file:$line_num)" >> "$RESULTS_FILE"
        return 0
    else
        ((BROKEN_LINKS++))
        echo -e "  ${RED}✗${NC} $link_path (Zeile $line_num)"
        echo "$source_file:$line_num → $link_path" >> "$BROKEN_FILE"
        return 1
    fi
}

# Function to process a markdown file
process_file() {
    local file="$1"
    
    if [ ! -f "$file" ]; then
        return
    fi
    
    echo -e "\n${BLUE}Prüfe:${NC} $file"
    
    local file_links=0
    local file_broken=0
    
    # Find all markdown links [text](path)
    grep -n -o '\[[^]]*\]([^)]*)' "$file" 2>/dev/null | while IFS=':' read -r line_num link_match; do
        # Extract the path from the link
        local path=$(echo "$link_match" | sed 's/.*](\([^)]*\)).*/\1/')
        
        # Skip external URLs, anchors, and mailto links
        if [[ "$path" =~ ^http ]] || [[ "$path" =~ ^# ]] || [[ "$path" =~ ^mailto: ]]; then
            continue
        fi
        
        # Skip if not a file path
        if [[ ! "$path" =~ \. ]] && [[ ! "$path" =~ / ]]; then
            continue
        fi
        
        # Check the link
        if check_link "$file" "$path" "$line_num"; then
            ((file_links++))
        else
            ((file_broken++))
        fi
    done
    
    if [ $file_broken -eq 0 ] && [ $file_links -gt 0 ]; then
        echo -e "  ${GREEN}→ Alle Links valide ($file_links)${NC}"
    elif [ $file_broken -gt 0 ]; then
        echo -e "  ${RED}→ $file_broken defekte Links${NC}"
    fi
}

# Test Master Plan V5
echo -e "${BLUE}=== Phase 1: Master Plan V5 ===${NC}"
process_file "docs/CRM_COMPLETE_MASTER_PLAN_V5.md"

# Test ACTIVE features
echo -e "\n${BLUE}=== Phase 2: ACTIVE Features ===${NC}"
find docs/features/ACTIVE -name "*_CLAUDE_TECH.md" -type f | sort | while read -r file; do
    process_file "$file"
done

# Test PLANNED features (first 5)
echo -e "\n${BLUE}=== Phase 3: PLANNED Features (Sample) ===${NC}"
find docs/features/PLANNED -name "*_CLAUDE_TECH.md" -type f | sort | head -5 | while read -r file; do
    process_file "$file"
done

# Summary
echo -e "\n${BLUE}=== ZUSAMMENFASSUNG ===${NC}"
echo "Geprüfte Links:  $TOTAL_LINKS"
echo -e "Valide Links:    ${GREEN}$VALID_LINKS${NC}"
echo -e "Defekte Links:   ${RED}$BROKEN_LINKS${NC}"

if [ $BROKEN_LINKS -gt 0 ]; then
    echo -e "\n${YELLOW}Defekte Links in: $BROKEN_FILE${NC}"
    echo -e "\n${RED}Top 5 defekte Links:${NC}"
    head -5 "$BROKEN_FILE"
    exit 1
else
    echo -e "\n${GREEN}✅ Alle Links sind valide!${NC}"
    exit 0
fi