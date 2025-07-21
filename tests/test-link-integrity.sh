#!/bin/bash

# Link-Integritäts-Test für CLAUDE_TECH Struktur
# Prüft alle Links in Master Plan V5 und CLAUDE_TECH Dokumenten

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TOTAL_LINKS=0
BROKEN_LINKS=0
VALID_LINKS=0
WARNINGS=0

# Results file
RESULTS_FILE="link-test-results.log"
SUMMARY_FILE="link-test-summary.log"

# Project root
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$PROJECT_ROOT"

echo -e "${BLUE}=== CLAUDE_TECH Link-Integritäts-Test ===${NC}"
echo "Projekt-Root: $PROJECT_ROOT"
echo "Start: $(date)"
echo ""

# Initialize results file
{
    echo "# Link-Integritäts-Test Ergebnisse"
    echo "Ausgeführt: $(date)"
    echo "Projekt: $PROJECT_ROOT"
    echo ""
} > "$RESULTS_FILE"

# Function to check if file exists
check_file_exists() {
    local file="$1"
    local source="$2"
    local line_num="$3"
    
    ((TOTAL_LINKS++))
    
    # Clean path (remove leading slash if checking relative path)
    local clean_path="${file#/}"
    
    # Try different path resolutions
    if [ -f "$file" ]; then
        ((VALID_LINKS++))
        echo -e "  ${GREEN}✓${NC} $file"
        return 0
    elif [ -f "$PROJECT_ROOT/$file" ]; then
        ((VALID_LINKS++))
        echo -e "  ${GREEN}✓${NC} $file (resolved to project root)"
        return 0
    elif [ -f "$PROJECT_ROOT/$clean_path" ]; then
        ((VALID_LINKS++))
        echo -e "  ${GREEN}✓${NC} $file (cleaned path)"
        return 0
    else
        ((BROKEN_LINKS++))
        echo -e "  ${RED}✗${NC} $file"
        echo "    ${YELLOW}→ In: $source:$line_num${NC}"
        {
            echo "❌ BROKEN: $file"
            echo "   Source: $source:$line_num"
            echo ""
        } >> "$RESULTS_FILE"
        return 1
    fi
}

# Function to extract and check links from a file
check_links_in_file() {
    local file="$1"
    echo -e "\n${BLUE}Prüfe: ${NC}$file"
    
    if [ ! -f "$file" ]; then
        echo -e "  ${YELLOW}⚠ Datei nicht gefunden${NC}"
        ((WARNINGS++))
        return
    fi
    
    # Extract various link patterns
    # Pattern 1: Markdown links [text](path)
    local link_count=0
    while IFS=':' read -r line_num line; do
        link_count=$((link_count + 1))
        # Extract path from markdown link
        echo "$line" | grep -oE '\[.*\]\([^)]+\)' | sed 's/.*](\([^)]*\)).*/\1/' | while read -r link; do
            # Skip URLs and anchors
            if [[ ! "$link" =~ ^https?:// ]] && [[ ! "$link" =~ ^# ]] && [[ ! "$link" =~ ^mailto: ]]; then
                check_file_exists "$link" "$file" "$line_num"
            fi
        done
    done
    
    # Pattern 2: Direct file paths in backticks `path/to/file`
    grep -n -E '`[^`]+\.(md|txt|sh|java|ts|tsx|js|jsx)`' "$file" 2>/dev/null | while IFS=':' read -r line_num line; do
        echo "$line" | grep -oE '`[^`]+\.(md|txt|sh|java|ts|tsx|js|jsx)`' | tr -d '`' | while read -r path; do
            # Only check if it looks like a file path
            if [[ "$path" =~ / ]]; then
                check_file_exists "$path" "$file" "$line_num"
            fi
        done
    done
    
    # Pattern 3: Reference links [text]: path
    grep -n -E '^\[.*\]:\s+[^[:space:]]+' "$file" 2>/dev/null | while IFS=':' read -r line_num line; do
        # Extract path after ]: 
        echo "$line" | sed 's/.*]:[[:space:]]*//' | while read -r link; do
            # Skip URLs
            if [[ ! "$link" =~ ^https?:// ]]; then
                check_file_exists "$link" "$file" "$line_num"
            fi
        done
    done
}

# Main test execution
echo -e "${BLUE}=== Phase 1: Master Plan V5 ===${NC}"
check_links_in_file "docs/CRM_COMPLETE_MASTER_PLAN_V5.md"

echo -e "\n${BLUE}=== Phase 2: ACTIVE Features ===${NC}"
find docs/features/ACTIVE -name "*_CLAUDE_TECH.md" -type f | sort | while read -r file; do
    check_links_in_file "$file"
done

echo -e "\n${BLUE}=== Phase 3: PLANNED Features ===${NC}"
find docs/features/PLANNED -name "*_CLAUDE_TECH.md" -type f | sort | while read -r file; do
    check_links_in_file "$file"
done

echo -e "\n${BLUE}=== Phase 4: Implementation Guides ===${NC}"
find docs/features -name "IMPLEMENTATION_GUIDE.md" -type f | sort | while read -r file; do
    check_links_in_file "$file"
done

# Generate summary
{
    echo "# Link-Integritäts-Test Zusammenfassung"
    echo ""
    echo "## Statistiken"
    echo "- Total Links geprüft: $TOTAL_LINKS"
    echo "- ✅ Valide Links: $VALID_LINKS"
    echo "- ❌ Defekte Links: $BROKEN_LINKS"
    echo "- ⚠️  Warnungen: $WARNINGS"
    echo ""
    echo "## Ergebnis"
    if [ $BROKEN_LINKS -eq 0 ]; then
        echo "✅ **ALLE LINKS SIND VALIDE!**"
    else
        echo "❌ **$BROKEN_LINKS DEFEKTE LINKS GEFUNDEN!**"
        echo ""
        echo "Details siehe: $RESULTS_FILE"
    fi
    echo ""
    echo "Test abgeschlossen: $(date)"
} > "$SUMMARY_FILE"

# Display summary
echo -e "\n${BLUE}=== TEST ZUSAMMENFASSUNG ===${NC}"
cat "$SUMMARY_FILE"

# Create detailed report if broken links found
if [ $BROKEN_LINKS -gt 0 ]; then
    echo -e "\n${RED}=== DEFEKTE LINKS ===${NC}"
    grep "❌ BROKEN:" "$RESULTS_FILE" | head -10
    if [ $BROKEN_LINKS -gt 10 ]; then
        echo -e "\n... und $((BROKEN_LINKS - 10)) weitere"
        echo -e "Siehe ${YELLOW}$RESULTS_FILE${NC} für vollständige Liste"
    fi
fi

# Exit with error if broken links found
if [ $BROKEN_LINKS -gt 0 ]; then
    exit 1
else
    echo -e "\n${GREEN}✅ Link-Integritäts-Test erfolgreich!${NC}"
    exit 0
fi