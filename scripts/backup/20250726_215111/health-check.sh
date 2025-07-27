#!/bin/bash
# health-check.sh - Prüft die Gesundheit aller Scripts und Konfigurationen

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "🏥 FreshPlan Scripts Health Check"
echo "================================="
echo ""

# Check 1: Configuration file
echo "1️⃣  Checking configuration..."
if [[ -f "scripts/config/paths.conf" ]]; then
    echo -e "${GREEN}✅ paths.conf found${NC}"
    source scripts/config/paths.conf
    
    # Verify key paths exist
    if [[ -f "$CLAUDE_MD" ]]; then
        echo -e "${GREEN}✅ CLAUDE.md path correct${NC}"
    else
        echo -e "${RED}❌ CLAUDE.md not found at: $CLAUDE_MD${NC}"
    fi
else
    echo -e "${RED}❌ paths.conf missing${NC}"
fi

echo ""

# Check 2: Script permissions
echo "2️⃣  Checking script permissions..."
SCRIPTS=(
    "session-start.sh"
    "handover-with-sync.sh"
    "get-active-module.sh"
    "find-feature-docs.sh"
    "sync-master-plan.sh"
)

for script in "${SCRIPTS[@]}"; do
    if [[ -x "scripts/$script" ]]; then
        echo -e "${GREEN}✅ $script is executable${NC}"
    else
        echo -e "${RED}❌ $script is not executable${NC}"
    fi
done

echo ""

# Check 3: Dynamic feature finding
echo "3️⃣  Testing dynamic feature finding..."
if [[ -x "scripts/find-feature-docs.sh" ]]; then
    # Test with FC-005
    if ./scripts/find-feature-docs.sh FC-005 > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Feature finder works for FC-005${NC}"
    else
        echo -e "${YELLOW}⚠️  Feature finder failed for FC-005${NC}"
    fi
else
    echo -e "${RED}❌ find-feature-docs.sh not found${NC}"
fi

echo ""

# Check 4: Critical paths in scripts
echo "4️⃣  Checking hardcoded paths..."
HARDCODED_ISSUES=0

# Check for old CLAUDE.md paths
if grep -r "docs/CLAUDE.md" scripts/ 2>/dev/null | grep -v "paths.conf"; then
    echo -e "${RED}❌ Found hardcoded 'docs/CLAUDE.md' paths${NC}"
    ((HARDCODED_ISSUES++))
fi

# Check for absolute paths
if grep -r "/Users/joergstreeck" scripts/ 2>/dev/null | grep -v "paths.conf"; then
    echo -e "${YELLOW}⚠️  Found hardcoded absolute paths (should use \$PROJECT_ROOT)${NC}"
    ((HARDCODED_ISSUES++))
fi

if [[ $HARDCODED_ISSUES -eq 0 ]]; then
    echo -e "${GREEN}✅ No hardcoded path issues found${NC}"
fi

echo ""

# Summary
echo "📊 Health Check Summary"
echo "======================"
if [[ $HARDCODED_ISSUES -eq 0 ]]; then
    echo -e "${GREEN}✅ All systems healthy!${NC}"
else
    echo -e "${YELLOW}⚠️  Some issues found - review above${NC}"
fi

echo ""
echo "💡 Recommendations:"
echo "1. Update scripts to use paths.conf"
echo "2. Use find-feature-docs.sh for dynamic searches"
echo "3. Run this health check after major changes"