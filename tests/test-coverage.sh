#!/bin/bash

# CLAUDE_TECH Coverage Test - Final Version
# Prüft die Dokumentations-Coverage

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

# Count documents
ALL_TECH_CONCEPTS=$(find docs/features -name "*_TECH_CONCEPT.md" -type f | wc -l | tr -d ' ')
CLAUDE_TECHS=$(find docs/features -name "*_CLAUDE_TECH.md" -type f | wc -l | tr -d ' ')

# Count placeholder documents
PLACEHOLDER_DOCS=$(find docs/features -name "*_TECH_CONCEPT.md" -type f -exec grep -l "Status:.*Placeholder Document" {} \; | wc -l | tr -d ' ')

# Calculate real TECH_CONCEPTS (excluding placeholders)
TECH_CONCEPTS=$((ALL_TECH_CONCEPTS - PLACEHOLDER_DOCS))

echo "Alle TECH_CONCEPT Dokumente:  $ALL_TECH_CONCEPTS"
echo "  davon Platzhalter:          $PLACEHOLDER_DOCS"
echo "  davon echte Dokumente:      $TECH_CONCEPTS"
echo ""
echo "CLAUDE_TECH Dokumente:        $CLAUDE_TECHS"

# Calculate coverage
if [ "$TECH_CONCEPTS" -gt 0 ]; then
    COVERAGE=$(( CLAUDE_TECHS * 100 / TECH_CONCEPTS ))
else
    COVERAGE=0
fi

echo -e "Coverage:               ${GREEN}$COVERAGE%${NC}"
echo ""

# Save detailed list
SUMMARY="tests/coverage-summary.md"
{
    echo "# CLAUDE_TECH Coverage Report"
    echo ""
    echo "Generated: $(date)"
    echo ""
    echo "## Summary"
    echo "- Alle TECH_CONCEPT: $ALL_TECH_CONCEPTS documents"
    echo "- Platzhalter: $PLACEHOLDER_DOCS documents"
    echo "- Echte TECH_CONCEPT: $TECH_CONCEPTS documents"
    echo "- CLAUDE_TECH: $CLAUDE_TECHS documents"
    echo "- **Coverage: $COVERAGE%** (ohne Platzhalter)"
    echo ""
    
    echo "## CLAUDE_TECH Documents"
    echo ""
    echo "### ACTIVE Features"
    find docs/features/ACTIVE -name "*_CLAUDE_TECH.md" -type f | sort | while read -r file; do
        echo "- \`${file#$PROJECT_ROOT/}\`"
    done
    
    echo ""
    echo "### PLANNED Features"
    find docs/features/PLANNED -name "*_CLAUDE_TECH.md" -type f | sort | head -20 | while read -r file; do
        echo "- \`${file#$PROJECT_ROOT/}\`"
    done
    
    PLANNED_COUNT=$(find docs/features/PLANNED -name "*_CLAUDE_TECH.md" -type f | wc -l | tr -d ' ')
    if [ "$PLANNED_COUNT" -gt 20 ]; then
        echo ""
        echo "_... and $((PLANNED_COUNT - 20)) more PLANNED features_"
    fi
    
    echo ""
    
    # List placeholder documents if any exist
    if [ "$PLACEHOLDER_DOCS" -gt 0 ]; then
        echo "## Platzhalter-Dokumente (nicht gezählt)"
        echo ""
        find docs/features -name "*_TECH_CONCEPT.md" -type f -exec grep -l "Status:.*Placeholder Document" {} \; | head -10 | while read -r file; do
            echo "- \`${file#$PROJECT_ROOT/}\`"
        done
        if [ "$PLACEHOLDER_DOCS" -gt 10 ]; then
            echo ""
            echo "_... und $((PLACEHOLDER_DOCS - 10)) weitere Platzhalter_"
        fi
        echo ""
    fi
    
    echo "## Result"
    if [ "$COVERAGE" -ge 100 ]; then
        echo "✅ **Perfect Coverage achieved!**"
        echo ""
        echo "Alle echten TECH_CONCEPT Dokumente haben eine CLAUDE_TECH Version!"
    elif [ "$COVERAGE" -ge 90 ]; then
        echo "✅ Excellent Coverage at $COVERAGE%"
    elif [ "$COVERAGE" -ge 80 ]; then
        echo "⚠️ Good Coverage at $COVERAGE%"
    else
        echo "❌ Low Coverage at $COVERAGE%"
    fi
} > "$SUMMARY"

# Show result
if [ "$COVERAGE" -ge 100 ]; then
    echo -e "${GREEN}✅ Perfect Coverage! Alle echten Features haben CLAUDE_TECH Dokumentation!${NC}"
    echo ""
    if [ "$PLACEHOLDER_DOCS" -gt 0 ]; then
        echo -e "${YELLOW}Info: $PLACEHOLDER_DOCS Platzhalter-Dokumente wurden nicht gezählt${NC}"
    fi
    echo ""
    echo "Details saved to: $SUMMARY"
    exit 0
elif [ "$COVERAGE" -ge 90 ]; then
    echo -e "${GREEN}✅ Excellent Coverage at $COVERAGE%${NC}"
    echo ""
    echo "Details saved to: $SUMMARY"
    exit 0
elif [ "$COVERAGE" -ge 80 ]; then
    echo -e "${YELLOW}⚠️ Good Coverage at $COVERAGE%${NC}"
    echo ""
    echo "Details saved to: $SUMMARY"
    exit 0
else
    echo -e "${RED}❌ Low Coverage at $COVERAGE%${NC}"
    echo ""
    echo "Details saved to: $SUMMARY"
    exit 1
fi