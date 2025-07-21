#!/bin/bash

# Umfassendes Script zur Reparatur ALLER verbleibenden defekten Links
set -euo pipefail

echo "🔧 Fixing ALL remaining broken links..."
echo "======================================"

PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
cd "$PROJECT_ROOT"

# Backup erstellen
BACKUP_DIR="docs/features.backup.final.$(date +%Y%m%d_%H%M%S)"
echo "📦 Creating backup: $BACKUP_DIR"
cp -r docs/features "$BACKUP_DIR"

# Zähle vorher
echo "📊 Counting broken links before fix..."
BEFORE_COUNT=$(./tests/comprehensive-link-test.sh 2>&1 | grep -c "❌ BROKEN:" || true)
echo "Broken links before: $BEFORE_COUNT"

echo ""
echo "🔄 Applying comprehensive fixes..."
echo "=================================="

# 1. Vollständige Pfade mit /Users/joergstreeck/ entfernen
echo "1️⃣ Removing /Users/joergstreeck/freshplan-sales-tool prefix..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|/Users/joergstreeck/freshplan-sales-tool||g' {} \;

# 2. Relative Pfade konvertieren
echo "2️⃣ Converting relative paths to absolute..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](../ACTIVE/|](/docs/features/ACTIVE/|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](../PLANNED/|](/docs/features/PLANNED/|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](../COMPLETED/|](/docs/features/COMPLETED/|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](../LEGACY/|](/docs/features/LEGACY/|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](../MASTER/|](/docs/features/MASTER/|g' {} \;

# 3. Doppelte Schrägstriche entfernen
echo "3️⃣ Cleaning up double slashes..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](//|](/|g' {} \;

# 4. Relative Pfade mit ./ konvertieren
echo "4️⃣ Converting ./ paths..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](\./|](|g' {} \;

# 5. Externe Dokumente (außerhalb features/)
echo "5️⃣ Fixing paths to external documents..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](../../CLAUDE\.md|](/CLAUDE.md|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](../../WAY_OF_WORKING\.md|](/WAY_OF_WORKING.md|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](../2025-|](/docs/features/2025-|g' {} \;

# 6. Bekannte falsche Pfadzuordnungen korrigieren
echo "6️⃣ Fixing known path mismatches..."

# FC-018 kann in verschiedenen Ordnern sein
DIRS_WITH_FC018=$(find docs/features -name "FC-018_*" -type f | cut -d'/' -f4 | sort | uniq)
echo "   FC-018 found in: $DIRS_WITH_FC018"

# FC-030 hat verschiedene Varianten
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|/30_aussendienst_optimierung/FC-030_|/30_one_tap_actions/FC-030_|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|/30_sales_automation/FC-030_|/30_one_tap_actions/FC-030_|g' {} \;

# FC-031 Varianten
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|/31_route_planning/FC-031_|/31_dynamic_documents/FC-031_|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|/31_ai_sales_assistant/FC-031_|/31_dynamic_documents/FC-031_|g' {} \;

# 7. Verzeichnis-Links mit README.md ergänzen
echo "7️⃣ Adding README.md to directory links..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/docs/features/ACTIVE/$|](/docs/features/ACTIVE/README.md|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/docs/features/PLANNED/$|](/docs/features/PLANNED/README.md|g' {} \;

# 8. Fehlende Features in falschen Ordnern
echo "8️⃣ Creating mappings for misplaced features..."

# Erstelle eine Mapping-Datei für alle Features
echo "   Building feature location map..."
find docs/features -name "FC-*_TECH_CONCEPT.md" -o -name "M*_TECH_CONCEPT.md" | while read file; do
    basename=$(basename "$file")
    echo "$basename|$file"
done > /tmp/feature-map.txt

# Korrigiere alle falschen Pfade basierend auf tatsächlichen Standorten
echo "   Applying location corrections..."
while IFS='|' read -r filename filepath; do
    correct_path="${filepath#docs/features/}"
    correct_path="/docs/features/$correct_path"
    
    # Ersetze alle Vorkommen mit dem korrekten Pfad
    find docs/features -name "*.md" -type f -exec \
        grep -l "$filename" {} \; | while read mdfile; do
        # Ersetze nur wenn der Pfad nicht schon korrekt ist
        sed -i '' "s|]/docs/features/[^/]*/[^/]*/$filename|]$correct_path|g" "$mdfile"
    done
done < /tmp/feature-map.txt

# 9. CLAUDE_TECH Links korrigieren
echo "9️⃣ Fixing CLAUDE_TECH naming variations..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|M1_NAVIGATION_CLAUDE_TECH\.md|M1_CLAUDE_TECH.md|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|M2_QUICK_CREATE_CLAUDE_TECH\.md|M2_CLAUDE_TECH.md|g' {} \;

# 10. Erstelle fehlende README.md Dateien
echo "🔟 Creating missing README.md files..."
for dir in ACTIVE PLANNED COMPLETED LEGACY MASTER; do
    if [ -d "docs/features/$dir" ] && [ ! -f "docs/features/$dir/README.md" ]; then
        echo "# $dir Features" > "docs/features/$dir/README.md"
        echo "" >> "docs/features/$dir/README.md"
        echo "Overview of $dir features." >> "docs/features/$dir/README.md"
    fi
done

# 11. Letzte Durchgänge für spezielle Fälle
echo "1️⃣1️⃣ Final cleanup passes..."

# Entferne trailing slashes
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|\.md/|.md|g' {} \;

# Korrigiere #-Links (Anker)
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|\.md#|.md#|g' {} \;

# 12. Validierung
echo ""
echo "📊 Validating fixes..."
AFTER_COUNT=$(./tests/comprehensive-link-test.sh 2>&1 | grep -c "❌ BROKEN:" || echo "0")
echo "Broken links after: $AFTER_COUNT"
echo "✅ Fixed: $((BEFORE_COUNT - AFTER_COUNT)) links"

# 13. Report verbleibende Probleme
if [ "$AFTER_COUNT" -gt 0 ]; then
    echo ""
    echo "⚠️  Still $AFTER_COUNT broken links remaining:"
    ./tests/comprehensive-link-test.sh 2>&1 | grep "❌ BROKEN:" | head -20
    
    echo ""
    echo "📝 Creating detailed report..."
    ./tests/comprehensive-link-test.sh 2>&1 | grep "❌ BROKEN:" > /tmp/remaining-broken-links.txt
    echo "Full list saved to: /tmp/remaining-broken-links.txt"
fi

# Cleanup
rm -f /tmp/feature-map.txt

# Rollback info
echo ""
echo "💡 To rollback if needed:"
echo "   rm -rf docs/features"
echo "   mv $BACKUP_DIR docs/features"

echo ""
echo "✅ Link repair complete!"