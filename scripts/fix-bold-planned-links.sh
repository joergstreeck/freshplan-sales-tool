#!/bin/bash

# Fix für die fehlerhaften Bold-Markierungen bei planned links
# Problem: **[Link](path)** *(geplant)* sollte [Link](path) *(geplant)* sein

echo "🔧 Fixing bold planned link markers..."
echo "====================================="
echo ""

# Counter
FIXED_FILES=0
FIXED_LINKS=0

# Find all files with the pattern
for file in $(find docs/features -name "*KOMPAKT.md" -type f); do
    if grep -q '\*\*\[.*\](.*)\*\* \*(geplant)\*' "$file"; then
        echo "📝 Processing: ${file#docs/features/}"
        
        # Count links before fix
        LINKS_BEFORE=$(grep -c '\*\*\[.*\](.*)\*\* \*(geplant)\*' "$file")
        
        # Fix the pattern - remove bold markers around links with *(geplant)*
        sed -i '' 's/\*\*\(\[[^]]*\]([^)]*)\)\*\* \*(geplant)\*/\1 *(geplant)*/g' "$file"
        
        # Count links after fix
        LINKS_AFTER=$(grep -c '\*\*\[.*\](.*)\*\* \*(geplant)\*' "$file" || echo 0)
        
        LINKS_FIXED=$((LINKS_BEFORE - LINKS_AFTER))
        if [ $LINKS_FIXED -gt 0 ]; then
            echo "  ✅ Fixed $LINKS_FIXED links"
            FIXED_FILES=$((FIXED_FILES + 1))
            FIXED_LINKS=$((FIXED_LINKS + LINKS_FIXED))
        fi
    fi
done

echo ""
echo "📊 Summary:"
echo "==========="
echo "✅ Fixed $FIXED_LINKS links in $FIXED_FILES files"
echo ""
echo "🔍 Running validation to verify..."
echo ""

# Run validation and show summary
./scripts/validate-navigation-links.sh 2>&1 | grep -A 20 "Validation Summary" || echo "Validation script failed"