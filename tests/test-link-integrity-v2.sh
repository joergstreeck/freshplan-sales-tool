#!/bin/bash

# Link-Integritäts-Test für CLAUDE_TECH Struktur v2
# Prüft alle internen Datei-Links in Markdown-Dokumenten

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

# Results arrays
declare -a BROKEN_LINKS
declare -a VALID_LINKS

echo -e "${BLUE}=== CLAUDE_TECH Link-Integritäts-Test v2 ===${NC}"
echo "Projekt: $PROJECT_ROOT"
echo "Start: $(date)"
echo ""

# Results file
RESULTS_FILE="tests/link-test-results.log"
SUMMARY_FILE="tests/link-test-summary.log"
> "$RESULTS_FILE"

# Function to normalize path
normalize_path() {
    local path="$1"
    # Remove leading ./
    path="${path#./}"
    # Remove leading /
    path="${path#/}"
    echo "$path"
}

# Function to check if file exists
check_file_exists() {
    local source="$1"
    local target="$2"
    local line_num="$3"
    
    # Normalize the target path
    local normalized=$(normalize_path "$target")
    
    # Try different path resolutions
    if [ -f "$PROJECT_ROOT/$normalized" ]; then
        VALID_LINKS+=("$target|$source:$line_num")
        return 0
    elif [ -f "$PROJECT_ROOT/docs/$normalized" ]; then
        VALID_LINKS+=("$target|$source:$line_num")
        return 0
    else
        BROKEN_LINKS+=("$target|$source:$line_num")
        return 1
    fi
}

# Function to extract links from a file
extract_links() {
    local file="$1"
    local links=()
    
    # Pattern 1: [text](path) - Markdown links
    while IFS= read -r line; do
        local line_num=$(echo "$line" | cut -d: -f1)
        local content=$(echo "$line" | cut -d: -f2-)
        
        # Extract all markdown links from this line
        while [[ "$content" =~ \[([^\]]*)\]\(([^)]+)\) ]]; do
            local link="${BASH_REMATCH[2]}"
            # Skip external URLs and anchors
            if [[ ! "$link" =~ ^https?:// ]] && [[ ! "$link" =~ ^# ]] && [[ ! "$link" =~ ^mailto: ]]; then
                # Only check if it looks like a file path
                if [[ "$link" =~ \.(md|txt|java|ts|tsx|js|jsx|sh)$ ]] || [[ "$link" =~ / ]]; then
                    links+=("$line_num:$link")
                fi
            fi
            # Remove the processed link to find more
            content="${content#*${BASH_REMATCH[0]}}"
        done
    done < <(grep -n '\[.*\](.*' "$file" 2>/dev/null || true)
    
    # Return the links
    printf '%s\n' "${links[@]}"
}

# Main test function
test_file() {
    local file="$1"
    local file_broken=0
    local file_valid=0
    
    if [ ! -f "$file" ]; then
        echo -e "  ${YELLOW}⚠ Datei nicht gefunden: $file${NC}"
        return
    fi
    
    echo -e "\n${BLUE}Prüfe:${NC} $file"
    
    # Extract and check links
    while IFS=: read -r line_num link; do
        if [ -z "$link" ]; then continue; fi
        
        if check_file_exists "$file" "$link" "$line_num"; then
            ((file_valid++))
            echo -e "  ${GREEN}✓${NC} $link"
        else
            ((file_broken++))
            echo -e "  ${RED}✗${NC} $link (Zeile $line_num)"
            echo "❌ $file:$line_num → $link" >> "$RESULTS_FILE"
        fi
    done < <(extract_links "$file")
    
    if [ $file_broken -eq 0 ] && [ $file_valid -gt 0 ]; then
        echo -e "  ${GREEN}→ Alle $file_valid Links valide${NC}"
    elif [ $file_broken -gt 0 ]; then
        echo -e "  ${RED}→ $file_broken defekte Links gefunden${NC}"
    fi
}

# Test execution
echo -e "${BLUE}=== Phase 1: Master Plan V5 ===${NC}"
test_file "docs/CRM_COMPLETE_MASTER_PLAN_V5.md"

echo -e "\n${BLUE}=== Phase 2: ACTIVE Features ===${NC}"
find docs/features/ACTIVE -name "*_CLAUDE_TECH.md" -type f | sort | while read -r file; do
    test_file "$file"
done

echo -e "\n${BLUE}=== Phase 3: PLANNED Features (erste 5) ===${NC}"
find docs/features/PLANNED -name "*_CLAUDE_TECH.md" -type f | sort | head -5 | while read -r file; do
    test_file "$file"
done

# Generate summary
{
    echo "# Link-Integritäts-Test Zusammenfassung"
    echo ""
    echo "## Statistiken"
    echo "- Valide Links: ${#VALID_LINKS[@]}"
    echo "- Defekte Links: ${#BROKEN_LINKS[@]}"
    echo "- Gesamt: $((${#VALID_LINKS[@]} + ${#BROKEN_LINKS[@]}))"
    echo ""
    echo "## Ergebnis"
    if [ ${#BROKEN_LINKS[@]} -eq 0 ]; then
        echo "✅ **ALLE LINKS SIND VALIDE!**"
    else
        echo "❌ **${#BROKEN_LINKS[@]} DEFEKTE LINKS GEFUNDEN!**"
        echo ""
        echo "### Defekte Links:"
        printf '%s\n' "${BROKEN_LINKS[@]}" | while IFS='|' read -r link location; do
            echo "- $link"
            echo "  → In: $location"
        done
    fi
    echo ""
    echo "Test abgeschlossen: $(date)"
} > "$SUMMARY_FILE"

# Display summary
echo -e "\n${BLUE}=== ZUSAMMENFASSUNG ===${NC}"
cat "$SUMMARY_FILE"

# Save detailed results
if [ ${#BROKEN_LINKS[@]} -gt 0 ]; then
    echo -e "\n${YELLOW}Details gespeichert in:${NC}"
    echo "- $RESULTS_FILE (defekte Links)"
    echo "- $SUMMARY_FILE (Zusammenfassung)"
fi

# Exit status
if [ ${#BROKEN_LINKS[@]} -eq 0 ]; then
    echo -e "\n${GREEN}✅ Link-Integritäts-Test erfolgreich!${NC}"
    exit 0
else
    echo -e "\n${RED}❌ Test fehlgeschlagen - defekte Links gefunden${NC}"
    exit 1
fi