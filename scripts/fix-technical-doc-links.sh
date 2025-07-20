#!/bin/bash

# Script to mark all technical documentation links as "planned"
# This fixes the broken links identified by validate-navigation-links.sh

echo "🔧 Technical Documentation Links Fixer"
echo "===================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
TOTAL_FILES=0
TOTAL_FIXES=0

# Base directory
BASE_DIR="/Users/joergstreeck/freshplan-sales-tool"
DOCS_DIR="$BASE_DIR/docs/features"

echo "📝 Processing feature documents..."
echo ""

# Function to fix technical links in a file
fix_technical_links() {
    local file=$1
    local changes=0
    
    # Create backup
    cp "$file" "$file.bak"
    
    # Fix patterns for relative technical doc links
    # Pattern 1: - **IMPLEMENTATION_GUIDE.md** - Description
    sed -i '' 's/- \*\*\(\.\/[A-Z0-9_-]*IMPLEMENTATION_GUIDE\.md\)\*\* -/- \*\*\1\*\* *(geplant)* -/g' "$file"
    
    # Pattern 2: - **DECISION_LOG.md** - Description  
    sed -i '' 's/- \*\*\(\.\/[A-Z0-9_-]*DECISION_LOG\.md\)\*\* -/- \*\*\1\*\* *(geplant)* -/g' "$file"
    
    # Pattern 3: - **[ANYTHING]_CATALOG.md** - Description
    sed -i '' 's/- \*\*\(\.\/[A-Z0-9_-]*_CATALOG\.md\)\*\* -/- \*\*\1\*\* *(geplant)* -/g' "$file"
    
    # Pattern 4: - **[ANYTHING]_PATTERNS.md** - Description
    sed -i '' 's/- \*\*\(\.\/[A-Z0-9_-]*_PATTERNS\.md\)\*\* -/- \*\*\1\*\* *(geplant)* -/g' "$file"
    
    # Pattern 5: - **[ANYTHING]_PLAYBOOK.md** - Description
    sed -i '' 's/- \*\*\(\.\/[A-Z0-9_-]*_PLAYBOOK\.md\)\*\* -/- \*\*\1\*\* *(geplant)* -/g' "$file"
    
    # Pattern 6: - **[ANYTHING]_REFERENCE.md** - Description
    sed -i '' 's/- \*\*\(\.\/[A-Z0-9_-]*_REFERENCE\.md\)\*\* -/- \*\*\1\*\* *(geplant)* -/g' "$file"
    
    # Pattern 7: Other technical docs
    sed -i '' 's/- \*\*\(\.\/[A-Z0-9_-]*\.md\)\*\* -/- \*\*\1\*\* *(geplant)* -/g' "$file"
    
    # Check if file was modified
    if ! cmp -s "$file" "$file.bak"; then
        changes=$(diff "$file.bak" "$file" | grep "^>" | wc -l)
        TOTAL_FIXES=$((TOTAL_FIXES + changes))
        echo -e "${GREEN}✓${NC} Fixed $changes links in: ${file#$BASE_DIR/}"
        rm "$file.bak"
    else
        echo -e "${YELLOW}○${NC} No changes needed in: ${file#$BASE_DIR/}"
        rm "$file.bak"
    fi
    
    return $changes
}

# Process all KOMPAKT.md files
for feature_dir in "$DOCS_DIR/ACTIVE"/* "$DOCS_DIR/PLANNED"/*; do
    if [[ -d "$feature_dir" ]]; then
        for kompakt_file in "$feature_dir"/*KOMPAKT.md; do
            if [[ -f "$kompakt_file" ]]; then
                TOTAL_FILES=$((TOTAL_FILES + 1))
                fix_technical_links "$kompakt_file"
            fi
        done
    fi
done

echo ""
echo "📊 Summary"
echo "========="
echo "Total files processed: $TOTAL_FILES"
echo "Total links fixed: $TOTAL_FIXES"
echo ""

if [[ $TOTAL_FIXES -gt 0 ]]; then
    echo -e "${GREEN}✅ Successfully marked $TOTAL_FIXES technical doc links as planned!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Run validation script to verify: ./scripts/validate-navigation-links.sh"
    echo "2. Commit the changes"
else
    echo -e "${YELLOW}⚠️  No changes were needed - links may already be fixed${NC}"
fi