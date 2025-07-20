#!/bin/bash

# Fix für Bold-Markierungen bei technischen Dokumenten mit *(geplant)*
# Von: **[Link](path)** *(geplant)* 
# Zu:  [Link](path) *(geplant)*

echo "🔧 Fixing bold markers in technical documentation links..."
echo "======================================================="
echo ""

# Counter
FIXED_FILES=0
TOTAL_FIXES=0

# Finde alle Dateien mit dem Pattern in der Technische Details Sektion
for file in $(find docs/features -name "*KOMPAKT.md" -type f); do
    # Prüfe ob die Datei das Pattern enthält
    if grep -q '^\- \*\*\[.*\](.*)\*\* \*(geplant)\*' "$file"; then
        echo "📝 Processing: ${file#docs/features/}"
        
        # Zähle Vorkommen vor dem Fix
        BEFORE=$(grep -c '^\- \*\*\[.*\](.*)\*\* \*(geplant)\*' "$file")
        
        # Entferne die Bold-Markierung nur bei Links mit *(geplant)*
        # Pattern: - **[text](path)** *(geplant)* → - [text](path) *(geplant)*
        sed -i '' 's/^\- \*\*\(\[[^]]*\]([^)]*)\)\*\* \*(geplant)\*/- \1 *(geplant)*/g' "$file"
        
        # Zähle Vorkommen nach dem Fix
        AFTER=$(grep -c '^\- \*\*\[.*\](.*)\*\* \*(geplant)\*' "$file" 2>/dev/null || echo 0)
        
        FIXES=$((BEFORE - AFTER))
        if [ $FIXES -gt 0 ]; then
            echo "  ✅ Fixed $FIXES links"
            FIXED_FILES=$((FIXED_FILES + 1))
            TOTAL_FIXES=$((TOTAL_FIXES + FIXES))
        fi
    fi
done

echo ""
echo "📊 Summary:"
echo "==========="
echo "✅ Fixed $TOTAL_FIXES links in $FIXED_FILES files"
echo ""

# Validierung
echo "🔍 Running validation..."
echo ""
./scripts/validate-navigation-links.sh 2>&1 | grep -E "(Validation Summary|Total|Broken|Files Missing)" -A 10