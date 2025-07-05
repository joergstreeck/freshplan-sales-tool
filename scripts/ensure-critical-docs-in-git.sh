#!/bin/bash

echo "🔒 Ensuring critical docs are in Git"
echo "===================================="
echo ""

# List of critical files that MUST be in Git
CRITICAL_FILES=(
    "CLAUDE.md"
    "docs/STANDARDUBERGABE_NEU.md"
    "docs/STANDARDUBERGABE_KOMPAKT.md"
    "docs/STANDARDUBERGABE.md"
    "docs/TRIGGER_TEXTS.md"
    "docs/CRM_COMPLETE_MASTER_PLAN.md"
)

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Check and add each file
echo "📋 Checking critical files..."
for file in "${CRITICAL_FILES[@]}"; do
    if [ -f "$file" ]; then
        # Check if file is tracked by git
        if git ls-files --error-unmatch "$file" >/dev/null 2>&1; then
            echo -e "${GREEN}✅ Tracked: $file${NC}"
        else
            echo -e "${YELLOW}⚠️  Not tracked: $file - Adding now...${NC}"
            git add -f "$file"
            echo -e "${GREEN}✅ Added: $file${NC}"
        fi
    else
        echo -e "${RED}❌ File missing: $file${NC}"
    fi
done

echo ""
echo "📊 Git status for critical docs:"
git status --porcelain | grep -E "(CLAUDE.md|STANDARDUBERGABE|TRIGGER_TEXTS|CRM_COMPLETE_MASTER_PLAN)" || echo "All critical docs are committed ✅"

echo ""
echo "💡 This script is automatically called by:"
echo "   - quick-cleanup.sh (before commits)"
echo "   - Every handover creation"