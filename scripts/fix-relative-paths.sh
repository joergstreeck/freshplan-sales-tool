#!/bin/bash

# Fix Relative Paths - Konvertiert alle relativen Pfade zu absoluten
# WICHTIG: Macht ein Backup bevor es Änderungen vornimmt!

echo "🔧 FIX RELATIVE PATHS"
echo "===================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Create backup
BACKUP_DIR="backups/relative-paths-$(date +%Y%m%d_%H%M%S)"
echo "Creating backup in $BACKUP_DIR..."
mkdir -p "$BACKUP_DIR"

# Statistics
TOTAL_FILES=0
FIXED_FILES=0
TOTAL_PATHS_FIXED=0

echo ""
echo "Scanning for files with relative paths..."
echo ""

# Find all files with relative paths and back them up
while IFS= read -r file; do
    if grep -q "\.\./" "$file" 2>/dev/null; then
        ((TOTAL_FILES++))
        
        # Create backup
        backup_path="$BACKUP_DIR/$file"
        mkdir -p "$(dirname "$backup_path")"
        cp "$file" "$backup_path"
        
        echo -e "${BLUE}Processing:${NC} $file"
        
        # Count relative paths before
        before_count=$(grep -c "\.\./" "$file")
        
        # Fix different types of relative paths
        # Type 1: ../features/ → /docs/features/
        sed -i.bak 's|\.\./features/|/docs/features/|g' "$file"
        
        # Type 2: ../../docs/ → /docs/
        sed -i.bak 's|\.\./\.\./docs/|/docs/|g' "$file"
        
        # Type 3: ../../../backend/ → /backend/
        sed -i.bak 's|\.\./\.\./\.\./backend/|/backend/|g' "$file"
        
        # Type 4: ../ACTIVE/ → /docs/features/ACTIVE/
        sed -i.bak 's|\.\./ACTIVE/|/docs/features/ACTIVE/|g' "$file"
        
        # Type 5: ../PLANNED/ → /docs/features/PLANNED/
        sed -i.bak 's|\.\./PLANNED/|/docs/features/PLANNED/|g' "$file"
        
        # Type 6: Generic ../ at start of path in markdown links
        sed -i.bak 's|](\.\./|](/docs/features/|g' "$file"
        
        # Remove .bak files created by sed
        rm -f "$file.bak"
        
        # Count relative paths after
        after_count=$(grep -c "\.\./" "$file" 2>/dev/null || echo 0)
        
        if [ $after_count -lt $before_count ]; then
            ((FIXED_FILES++))
            paths_fixed=$((before_count - after_count))
            ((TOTAL_PATHS_FIXED+=paths_fixed))
            echo -e "  ${GREEN}✓ Fixed $paths_fixed relative paths${NC}"
        else
            echo -e "  ${YELLOW}⚠ Still has $after_count relative paths (manual fix needed)${NC}"
        fi
        
        # Show remaining relative paths if any
        if [ $after_count -gt 0 ]; then
            echo "  Remaining relative paths:"
            grep -n "\.\./" "$file" | head -3 | sed 's/^/    /'
            if [ $after_count -gt 3 ]; then
                echo "    ... and $((after_count - 3)) more"
            fi
        fi
        echo ""
    fi
done < <(find . -name "*.md" -type f | grep -v -E "(\.git|node_modules|target|dist|build|backup)")

# Also fix common incorrect absolute paths
echo "Fixing incorrect absolute paths..."
echo ""

# Fix paths that should start with /docs/features/
find . -name "*.md" -type f | xargs grep -l "](/features/" | while read -r file; do
    echo -e "${BLUE}Fixing absolute paths in:${NC} $file"
    sed -i 's|](/features/|](/docs/features/|g' "$file"
    ((TOTAL_PATHS_FIXED++))
done

# Summary
echo ""
echo "========================================"
echo "SUMMARY:"
echo "========================================"
echo "Files with relative paths found: $TOTAL_FILES"
echo -e "${GREEN}Files successfully processed: $FIXED_FILES${NC}"
echo -e "${GREEN}Total paths fixed: $TOTAL_PATHS_FIXED${NC}"
echo ""
echo "Backup created at: $BACKUP_DIR"
echo ""

# Run validation
echo "Running validation test..."
echo ""

if [ -f "tests/test-absolute-paths.sh" ]; then
    if bash tests/test-absolute-paths.sh; then
        echo -e "\n${GREEN}✅ SUCCESS: All relative paths have been fixed!${NC}"
        echo ""
        echo "Next steps:"
        echo "1. Review the changes with: git diff"
        echo "2. Test navigation manually on a few links"
        echo "3. Commit the fixes"
        echo "4. Add path validation to your CI/CD"
    else
        echo -e "\n${YELLOW}⚠️  WARNING: Some relative paths still remain${NC}"
        echo ""
        echo "Manual intervention needed for complex cases."
        echo "Check the test output above for details."
    fi
else
    echo -e "${YELLOW}⚠️  Validation test not found${NC}"
    echo "Run manually: ./tests/test-absolute-paths.sh"
fi

echo ""
echo "To restore from backup if needed:"
echo "cp -r $BACKUP_DIR/* ."