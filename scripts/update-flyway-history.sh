#!/bin/bash

# Script to update FLYWAY_MIGRATION_HISTORY.md after a new migration
# Usage: ./scripts/update-flyway-history.sh V121 "add_new_feature" "New Feature Tables"

set -euo pipefail

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Check parameters
if [ $# -lt 3 ]; then
    echo -e "${RED}Usage: $0 <VERSION> <FILENAME_SUFFIX> <DESCRIPTION>${NC}"
    echo "Example: $0 V121 add_customer_notes 'Customer Notes System'"
    exit 1
fi

VERSION=$1
FILENAME_SUFFIX=$2
DESCRIPTION=$3
DATE=$(date +"%Y-%m-%d")
HISTORY_FILE="docs/FLYWAY_MIGRATION_HISTORY.md"

# Check if history file exists
if [ ! -f "$HISTORY_FILE" ]; then
    echo -e "${RED}ERROR: $HISTORY_FILE not found!${NC}"
    exit 1
fi

# Calculate next version number
CURRENT_VERSION=$(echo "$VERSION" | sed 's/V//')
NEXT_VERSION=$((CURRENT_VERSION + 1))

# Add new entry to table (before the V38-V119 reserved block)
sed -i '' "/\| \*\*V38-V119\*\* \|/i\\
| $VERSION | ${VERSION}__${FILENAME_SUFFIX}.sql | ✅ Applied | $DATE | $DESCRIPTION |
" "$HISTORY_FILE"

# Update next available version
sed -i '' "s/## 🆕 NÄCHSTE VERFÜGBARE MIGRATION: V[0-9]*/## 🆕 NÄCHSTE VERFÜGBARE MIGRATION: V$NEXT_VERSION/" "$HISTORY_FILE"

# Update last update timestamp
sed -i '' "s/\*\*Letzte Aktualisierung:\*\* .*/\*\*Letzte Aktualisierung:\*\* $DATE, $(date +%H:%M)/" "$HISTORY_FILE"

# Update in migration registry too if exists
if [ -f "docs/MIGRATION_REGISTRY.md" ]; then
    # Update next available in registry
    sed -i '' "s/## 🆕 NÄCHSTE VERFÜGBARE NUMMER: V[0-9]*/## 🆕 NÄCHSTE VERFÜGBARE NUMMER: V$NEXT_VERSION/" "docs/MIGRATION_REGISTRY.md"
    
    # Add to registry table
    sed -i '' "/## 🆕 NÄCHSTE VERFÜGBARE NUMMER:/i\\
| $VERSION | ${VERSION}__${FILENAME_SUFFIX}.sql | ✅ Applied | $DATE | $DESCRIPTION |
" "docs/MIGRATION_REGISTRY.md"
    
    echo -e "${GREEN}✅ Updated MIGRATION_REGISTRY.md${NC}"
fi

echo -e "${GREEN}✅ Updated FLYWAY_MIGRATION_HISTORY.md${NC}"
echo -e "${GREEN}✅ New migration: $VERSION${NC}"
echo -e "${GREEN}✅ Next available: V$NEXT_VERSION${NC}"

# Show reminder
echo ""
echo -e "${GREEN}Don't forget to:${NC}"
echo "1. Create the migration file: backend/src/main/resources/db/migration/${VERSION}__${FILENAME_SUFFIX}.sql"
echo "2. Test the migration locally"
echo "3. Commit both the migration and updated history files"