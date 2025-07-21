#!/bin/bash

# Link-Integritäts-Test für CLAUDE_TECH Struktur
# Version: Final

set -uo pipefail

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
echo -e "${BLUE}║     CLAUDE_TECH Link-Integritäts-Test            ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════╝${NC}"
echo ""
echo "Projekt: $PROJECT_ROOT"
echo "Start: $(date)"
echo ""

# Temporary files for results
TEMP_VALID=$(mktemp)
TEMP_BROKEN=$(mktemp)
RESULTS_FILE="tests/link-test-results.log"
SUMMARY_FILE="tests/link-test-summary.md"

# Cleanup function
cleanup() {
    rm -f "$TEMP_VALID" "$TEMP_BROKEN"
}
trap cleanup EXIT

# Initialize result files
> "$RESULTS_FILE"
echo "# Link-Integritäts-Test Ergebnisse" > "$SUMMARY_FILE"
echo "Ausgeführt: $(date)" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Function to check a single link
check_link() {
    local source_file="$1"
    local link_path="$2"
    local line_num="$3"
    
    # Clean the path
    link_path=$(echo "$link_path" | sed 's|^/||' | sed 's|^./||')
    
    # Check various path resolutions
    if [ -f "$PROJECT_ROOT/$link_path" ]; then
        echo "VALID|$source_file|$line_num|$link_path" >> "$TEMP_VALID"
        echo -e "  ${GREEN}✓${NC} $link_path"
    elif [ -f "$PROJECT_ROOT/docs/$link_path" ]; then
        echo "VALID|$source_file|$line_num|$link_path" >> "$TEMP_VALID"
        echo -e "  ${GREEN}✓${NC} $link_path"
    else
        echo "BROKEN|$source_file|$line_num|$link_path" >> "$TEMP_BROKEN"
        echo -e "  ${RED}✗${NC} $link_path (Zeile $line_num)"
        echo "❌ $source_file:$line_num → $link_path" >> "$RESULTS_FILE"
    fi
}

# Function to process a markdown file
process_file() {
    local file="$1"
    
    if [ ! -f "$file" ]; then
        echo -e "  ${YELLOW}⚠ Datei nicht gefunden: $file${NC}"
        return
    fi
    
    echo -e "\n${BLUE}Prüfe:${NC} $file"
    
    # Extract markdown links using a more robust approach
    local temp_links=$(mktemp)
    
    # Find all markdown links [text](path)
    grep -n '\[[^]]*\]([^)]*)' "$file" 2>/dev/null | while IFS=':' read -r line_num rest; do
        # Extract all links from this line
        echo "$rest" | grep -o '\[[^]]*\]([^)]*)' | while read -r link_match; do
            # Extract the path
            local path=$(echo "$link_match" | sed 's/.*](\([^)]*\)).*/\1/')
            
            # Skip external URLs, anchors, and mailto
            if [[ "$path" =~ ^http ]] || [[ "$path" =~ ^# ]] || [[ "$path" =~ ^mailto: ]]; then
                continue
            fi
            
            # Check for KOMPAKT links (obsolete)
            if [[ "$path" =~ KOMPAKT\.md ]]; then
                echo "BROKEN|$file|$line_num|$path (KOMPAKT OBSOLETE)" >> "$TEMP_BROKEN"
                echo -e "  ${YELLOW}⚠️${NC} $path (KOMPAKT - veraltet) (Zeile $line_num)"
                continue
            fi
            
            # Only process if it looks like a file path
            if [[ "$path" =~ \.(md|txt|java|ts|tsx|js|jsx|sh|sql|json|xml|yaml|yml)$ ]] || [[ "$path" =~ / ]]; then
                echo "$line_num|$path" >> "$temp_links"
            fi
        done
    done
    
    # Process collected links
    if [ -s "$temp_links" ]; then
        while IFS='|' read -r line_num path; do
            check_link "$file" "$path" "$line_num"
        done < "$temp_links"
    fi
    
    rm -f "$temp_links"
}

# Main test execution
echo -e "${BLUE}═══ Phase 1: Master Plan V5 ═══${NC}"
process_file "docs/CRM_COMPLETE_MASTER_PLAN_V5.md"

echo -e "\n${BLUE}═══ Phase 2: ACTIVE Features ═══${NC}"
find docs/features/ACTIVE -name "*_CLAUDE_TECH.md" -type f | sort | while read -r file; do
    process_file "$file"
done

echo -e "\n${BLUE}═══ Phase 3: PLANNED Features ═══${NC}"
find docs/features/PLANNED -name "*_CLAUDE_TECH.md" -type f | sort | while read -r file; do
    process_file "$file"
done

echo -e "\n${BLUE}═══ Phase 4: ALL TECH_CONCEPT Files ═══${NC}"
find docs/features -name "*_TECH_CONCEPT.md" -type f | sort | while read -r file; do
    process_file "$file"
done

echo -e "\n${BLUE}═══ Phase 5: Other Documentation ═══${NC}"
find docs/features -name "*.md" -type f ! -name "*_CLAUDE_TECH.md" ! -name "*_TECH_CONCEPT.md" | sort | head -20 | while read -r file; do
    process_file "$file"
done

# Count results
VALID_COUNT=$(wc -l < "$TEMP_VALID" 2>/dev/null || echo "0")
BROKEN_COUNT=$(wc -l < "$TEMP_BROKEN" 2>/dev/null || echo "0")
TOTAL_COUNT=$((VALID_COUNT + BROKEN_COUNT))

# Generate detailed summary
{
    echo "## Statistiken"
    echo ""
    echo "| Metrik | Anzahl |"
    echo "|--------|--------|"
    echo "| Geprüfte Links | $TOTAL_COUNT |"
    echo "| ✅ Valide Links | $VALID_COUNT |"
    echo "| ❌ Defekte Links | $BROKEN_COUNT |"
    echo ""
    
    if [ "$BROKEN_COUNT" -gt 0 ]; then
        echo "## ❌ Defekte Links"
        echo ""
        echo "| Datei | Zeile | Link |"
        echo "|-------|-------|------|"
        while IFS='|' read -r status file line link; do
            echo "| $(basename "$file") | $line | $link |"
        done < "$TEMP_BROKEN" | head -20
        
        if [ "$BROKEN_COUNT" -gt 20 ]; then
            echo ""
            echo "... und $((BROKEN_COUNT - 20)) weitere defekte Links"
        fi
    else
        echo "## ✅ Ergebnis"
        echo ""
        echo "**Alle $VALID_COUNT Links sind valide!**"
    fi
    
    echo ""
    echo "---"
    echo "_Test abgeschlossen: $(date)_"
} >> "$SUMMARY_FILE"

# Display summary
echo -e "\n${BLUE}╔══════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                 ZUSAMMENFASSUNG                   ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════╝${NC}"
echo ""
echo "Geprüfte Links:  $TOTAL_COUNT"
echo -e "Valide Links:    ${GREEN}$VALID_COUNT${NC}"
echo -e "Defekte Links:   ${RED}$BROKEN_COUNT${NC}"

if [ "$BROKEN_COUNT" -gt 0 ]; then
    echo -e "\n${YELLOW}Details siehe:${NC}"
    echo "- Zusammenfassung: $SUMMARY_FILE"
    echo "- Defekte Links:   $RESULTS_FILE"
    echo -e "\n${RED}❌ Test fehlgeschlagen - $BROKEN_COUNT defekte Links gefunden${NC}"
    exit 1
else
    echo -e "\n${GREEN}✅ Alle Links sind valide!${NC}"
    echo -e "\nDetails: $SUMMARY_FILE"
    exit 0
fi