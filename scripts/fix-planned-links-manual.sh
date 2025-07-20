#!/bin/bash

# Fix für die fehlerhaften *(geplant)* Markierungen
# Problem: ")** *(geplant" sollte ") *(geplant)*" sein

echo "🔧 Fixing planned link markers..."
echo "================================"

# Counter
FIXED=0

# Find all files with the incorrect pattern
for file in $(find docs/features -name "*KOMPAKT.md" -type f); do
    if grep -q ')\*\* \*(geplant' "$file"; then
        echo "Fixing: $file"
        
        # Create backup
        cp "$file" "$file.bak"
        
        # Fix the pattern
        # Von: ](...md)** *(geplant)* - Text
        # Zu:  ](...md) *(geplant)* - Text
        sed -i '' 's/\]\(([^)]*\.md)\)\*\* \*(geplant)\* - /](\1 *(geplant)* - /g' "$file"
        
        # Count fixes
        FIXED=$((FIXED + 1))
        
        # Remove backup if successful
        rm "$file.bak"
    fi
done

echo ""
echo "✅ Fixed $FIXED files"
echo ""
echo "Running validation..."
./scripts/validate-navigation-links.sh 2>&1 | tail -20