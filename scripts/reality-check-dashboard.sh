#!/bin/bash

# Reality Check Dashboard - Zeigt Status aller Features

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔══════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║            Reality Check Dashboard                   ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════╝${NC}"
echo ""

# Find all features
echo "Scanning all features..."
echo ""

# Active features
echo -e "${GREEN}ACTIVE Features:${NC}"
for feature in $(find docs/features/ACTIVE -name "FC-*_CLAUDE_TECH.md" -o -name "M*_CLAUDE_TECH.md" | sort); do
    FEATURE_CODE=$(basename "$feature" | sed 's/_CLAUDE_TECH.md//')
    
    # Run reality check silently
    if ./scripts/reality-check.sh "$FEATURE_CODE" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ $FEATURE_CODE${NC}"
    else
        # Count missing files
        MISSING=$(./scripts/reality-check.sh "$FEATURE_CODE" 2>&1 | grep -c "NICHT GEFUNDEN" || true)
        echo -e "  ${RED}❌ $FEATURE_CODE - $MISSING Dateien fehlen${NC}"
    fi
done

echo ""
echo -e "${YELLOW}PLANNED Features:${NC}"
# Similar for PLANNED...

echo ""
echo "══════════════════════════════════════════════════════"
echo -e "${BLUE}Summary:${NC}"
echo "  Last full scan: $(date)"
echo "  For details run: ./scripts/reality-check.sh FC-XXX"