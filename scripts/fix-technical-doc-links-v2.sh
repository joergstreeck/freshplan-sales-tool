#!/bin/bash

# Script to mark all technical documentation links as "planned"
# Version 2: Handles markdown link format [text](url)

echo "🔧 Technical Documentation Links Fixer V2"
echo "======================================"
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
    
    # Fix markdown links to relative technical docs
    # Pattern: [text](./filename.md) -> [text](./filename.md) *(geplant)*
    
    # Only fix links in the technical details section that don't already have *(geplant)*
    # and are relative links (start with ./)
    
    # First, let's fix links that are in list format: - **[text](./file.md)** - description
    sed -i '' '/^- \*\*\[.*\](\.\//s/\](\.\([^)]*\.md\))\*\* - \(.*\)$/](\.\1)\*\* *(geplant)* - \2/g' "$file"
    
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