#!/bin/bash

# Quick Link Test - Simplified version
echo "🔍 Quick Link Integrity Check"
echo "============================"

PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
cd "$PROJECT_ROOT"

# Count broken links
BROKEN_COUNT=0
TOTAL_COUNT=0

echo "Checking TECH_CONCEPT links..."

# Check the three new files specifically
for file in docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md \
            docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md \
            docs/features/PLANNED/99_sales_gamification/FC-017_TECH_CONCEPT.md; do
    
    echo "Checking: $file"
    
    if [ ! -f "$file" ]; then
        echo "❌ File not found: $file"
        ((BROKEN_COUNT++))
        continue
    fi
    
    # Extract all markdown links
    while IFS= read -r line; do
        if [[ $line =~ \[([^\]]+)\]\(([^\)]+)\) ]]; then
            link_text="${BASH_REMATCH[1]}"
            link_path="${BASH_REMATCH[2]}"
            ((TOTAL_COUNT++))
            
            # Skip external links and anchors
            if [[ $link_path =~ ^http ]] || [[ $link_path =~ ^# ]]; then
                continue
            fi
            
            # Clean path
            link_path="${link_path#/}"
            
            # Check if file exists
            if [ -f "$PROJECT_ROOT/$link_path" ]; then
                echo "  ✓ $link_path"
            else
                echo "  ❌ BROKEN: $link_path"
                ((BROKEN_COUNT++))
            fi
        fi
    done < "$file"
done

echo ""
echo "Summary:"
echo "Total links checked: $TOTAL_COUNT"
echo "Broken links: $BROKEN_COUNT"

if [ $BROKEN_COUNT -eq 0 ]; then
    echo "✅ All links are valid!"
    exit 0
else
    echo "❌ Found $BROKEN_COUNT broken links!"
    exit 1
fi