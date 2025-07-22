#!/bin/bash

# Test: Keine relativen Pfade erlaubt!
# Dieser Test stellt sicher, dass alle Pfade absolut vom Projekt-Root sind

echo "🔍 Absolute Path Validator"
echo "=========================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Counters
TOTAL_FILES=0
FILES_WITH_RELATIVE=0
TOTAL_RELATIVE_PATHS=0

echo "Scanning for relative paths (../) in all markdown files..."
echo ""

# Find all markdown files with relative paths
while IFS= read -r file; do
    ((TOTAL_FILES++))
    
    # Count relative paths in this file
    relative_count=$(grep -c "\.\./" "$file" 2>/dev/null || echo 0)
    
    if [ "$relative_count" -gt 0 ]; then
        ((FILES_WITH_RELATIVE++))
        ((TOTAL_RELATIVE_PATHS+=relative_count))
        
        echo -e "${RED}❌ $file${NC}"
        echo "   Found $relative_count relative paths:"
        
        # Show first 3 examples
        grep -n "\.\./" "$file" | head -3 | while read -r line; do
            echo "   $line"
        done
        
        if [ $relative_count -gt 3 ]; then
            echo "   ... and $((relative_count - 3)) more"
        fi
        echo ""
    fi
done < <(find . -name "*.md" -type f | grep -v -E "(\.git|node_modules|target|dist|build|backup)")

echo "========================================"
echo "SUMMARY:"
echo "========================================"
echo "Total files scanned: $TOTAL_FILES"
echo -e "${RED}Files with relative paths: $FILES_WITH_RELATIVE${NC}"
echo -e "${RED}Total relative paths found: $TOTAL_RELATIVE_PATHS${NC}"

if [ $FILES_WITH_RELATIVE -eq 0 ]; then
    echo -e "\n${GREEN}✅ SUCCESS: No relative paths found!${NC}"
    echo "All paths are absolute - safe for any Claude to navigate."
    exit 0
else
    echo -e "\n${RED}❌ FAILED: Found $TOTAL_RELATIVE_PATHS relative paths in $FILES_WITH_RELATIVE files!${NC}"
    echo ""
    echo "WHY THIS MATTERS:"
    echo "- Relative paths break when accessed from different directories"
    echo "- New Claude sessions waste time debugging navigation"
    echo "- Links become unreliable after refactoring"
    echo ""
    echo "HOW TO FIX:"
    echo "1. Run: ./scripts/fix-relative-paths.sh"
    echo "2. Or manually replace '../' with absolute paths from project root"
    echo "   Example: ../features/FC-001 → /docs/features/ACTIVE/FC-001"
    exit 1
fi