#!/bin/bash

# CLAUDE_TECH Coverage Test - Simplified Version
# Prüft die Coverage der CLAUDE_TECH Dokumentation

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
echo -e "${BLUE}║      CLAUDE_TECH Coverage Test                   ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════╝${NC}"
echo ""
echo "Start: $(date)"
echo ""

# Temporary files
TECH_CONCEPTS=$(mktemp)
CLAUDE_TECHS=$(mktemp)
SUMMARY_FILE="tests/coverage-summary.md"

# Cleanup
cleanup() {
    rm -f "$TECH_CONCEPTS" "$CLAUDE_TECHS"
}
trap cleanup EXIT

# Find all documents
echo -e "${BLUE}Suche Dokumente...${NC}"
find docs/features -name "*_TECH_CONCEPT.md" -type f | sort > "$TECH_CONCEPTS"
find docs/features -name "*_CLAUDE_TECH.md" -type f | sort > "$CLAUDE_TECHS"

TECH_COUNT=$(wc -l < "$TECH_CONCEPTS" | tr -d ' ')
CLAUDE_COUNT=$(wc -l < "$CLAUDE_TECHS" | tr -d ' ')

echo "TECH_CONCEPT Dokumente: $TECH_COUNT"
echo "CLAUDE_TECH Dokumente:  $CLAUDE_COUNT"
echo ""

# Generate detailed report
{
    echo "# CLAUDE_TECH Coverage Report"
    echo ""
    echo "Generated: $(date)"
    echo ""
    echo "## Summary"
    echo ""
    echo "| Document Type | Count |"
    echo "|---------------|-------|"
    echo "| TECH_CONCEPT | $TECH_COUNT |"
    echo "| CLAUDE_TECH | $CLAUDE_COUNT |"
    echo "| **Coverage** | **$(( CLAUDE_COUNT * 100 / TECH_COUNT ))%** |"
    echo ""
    
    echo "## CLAUDE_TECH Documents by Feature"
    echo ""
    echo "### ACTIVE Features"
    grep "/ACTIVE/" "$CLAUDE_TECHS" | while read -r file; do
        local feature=$(basename "$file" | grep -oE "(FC-[0-9]+|M[0-9]+)" | head -1)
        echo "- ✅ $feature: \`${file#$PROJECT_ROOT/}\`"
    done || echo "_(No ACTIVE features found)_"
    
    echo ""
    echo "### PLANNED Features" 
    grep "/PLANNED/" "$CLAUDE_TECHS" | while read -r file; do
        local feature=$(basename "$file" | grep -oE "(FC-[0-9]+|M[0-9]+)" | head -1)
        echo "- ✅ $feature: \`${file#$PROJECT_ROOT/}\`"
    done || echo "_(No PLANNED features found)_"
    
    # Check for missing CLAUDE_TECH files
    echo ""
    echo "## Coverage Analysis"
    echo ""
    
    # Create comparable lists
    cat "$TECH_CONCEPTS" | while read -r concept; do
        local expected_claude=$(echo "$concept" | sed 's/_TECH_CONCEPT.md/_CLAUDE_TECH.md/g')
        if ! grep -q "$(basename "$expected_claude")" "$CLAUDE_TECHS"; then
            echo "❌ Missing: $(basename "$expected_claude" .md)"
        fi
    done > missing.tmp
    
    local missing_count=$(wc -l < missing.tmp | tr -d ' ')
    
    if [ "$missing_count" -eq 0 ]; then
        echo "✅ **Perfect Coverage! All features have CLAUDE_TECH versions!**"
    else
        echo "### Missing CLAUDE_TECH Documents ($missing_count)"
        echo ""
        cat missing.tmp
    fi
    
    rm -f missing.tmp
    
    echo ""
    echo "---"
    echo "_Test completed: $(date)_"
} > "$SUMMARY_FILE"

# Display results
echo -e "${BLUE}═══ RESULTS ═══${NC}"
echo ""

if [ "$TECH_COUNT" -eq "$CLAUDE_COUNT" ]; then
    echo -e "${GREEN}✅ 100% Coverage!${NC}"
    echo "All $TECH_COUNT features have CLAUDE_TECH versions"
else
    local coverage=$(( CLAUDE_COUNT * 100 / TECH_COUNT ))
    echo -e "${YELLOW}⚠️  $coverage% Coverage${NC}"
    echo "$CLAUDE_COUNT of $TECH_COUNT features have CLAUDE_TECH versions"
fi

echo ""
echo "Detailed report: $SUMMARY_FILE"

# List all CLAUDE_TECH files by category
echo ""
echo -e "${BLUE}═══ CLAUDE_TECH Files by Category ═══${NC}"
echo ""
echo "ACTIVE:"
grep "/ACTIVE/" "$CLAUDE_TECHS" | while read -r file; do
    echo "  - $(basename "$file")"
done | sort | uniq

echo ""
echo "PLANNED:"
grep "/PLANNED/" "$CLAUDE_TECHS" | while read -r file; do
    echo "  - $(basename "$file")"
done | sort | uniq

# Exit with appropriate code
if [ "$TECH_COUNT" -eq "$CLAUDE_COUNT" ]; then
    exit 0
else
    exit 1
fi