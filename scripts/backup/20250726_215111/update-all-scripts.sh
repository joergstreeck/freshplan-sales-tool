#!/bin/bash
# update-all-scripts.sh - Aktualisiert alle Scripts auf dynamische Pfade

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "🔧 Updating all scripts to use dynamic paths"
echo "==========================================="
echo ""

# Create backup
echo "Creating backup..."
BACKUP_DIR="scripts/backup/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"
cp -r scripts/*.sh "$BACKUP_DIR/" 2>/dev/null || true
echo -e "${GREEN}✅ Backup created in $BACKUP_DIR${NC}"
echo ""

# Update patterns
declare -A REPLACEMENTS=(
    ["docs/CLAUDE.md"]='${CLAUDE_MD}'
    ["/Users/joergstreeck/freshplan-sales-tool"]='${PROJECT_ROOT}'
    ["docs/STANDARDUBERGABE_NEU.md"]='${STANDARDUBERGABE_NEU}'
    ["docs/CRM_COMPLETE_MASTER_PLAN_V5.md"]='${MASTER_PLAN_V5}'
    ["docs/NEXT_STEP.md"]='${NEXT_STEP}'
)

# Scripts to update
SCRIPTS=(
    "session-start.sh"
    "handover-with-sync.sh"
    "get-active-module.sh"
    "create-handover.sh"
    "sync-master-plan.sh"
)

echo "Updating scripts..."
for script in "${SCRIPTS[@]}"; do
    if [[ -f "scripts/$script" ]]; then
        echo -n "  Updating $script... "
        
        # Add source line if not present
        if ! grep -q "source.*paths.conf" "scripts/$script"; then
            # Find line after set -e
            sed -i '' '/^set -e/a\
\
# Source configuration\
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"\
source "${SCRIPT_DIR}/config/paths.conf"\
' "scripts/$script"
        fi
        
        # Apply replacements
        for old in "${!REPLACEMENTS[@]}"; do
            new="${REPLACEMENTS[$old]}"
            sed -i '' "s|$old|$new|g" "scripts/$script"
        done
        
        echo -e "${GREEN}done${NC}"
    else
        echo -e "  ${YELLOW}$script not found${NC}"
    fi
done

echo ""
echo -e "${GREEN}✅ Scripts updated!${NC}"
echo ""
echo "Next steps:"
echo "1. Run ./scripts/health-check.sh to verify"
echo "2. Test a few scripts manually"
echo "3. If issues, restore from $BACKUP_DIR"