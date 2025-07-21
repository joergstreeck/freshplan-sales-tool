#!/bin/bash

# Script zur Korrektur absoluter Pfade in Markdown-Links
set -euo pipefail

echo "🔧 Fixing absolute paths in documentation..."
echo "=========================================="

PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
cd "$PROJECT_ROOT"

# Backup falls noch nicht vorhanden
if [ ! -d "docs/features.backup.paths" ]; then
    echo "📦 Creating backup: docs/features.backup.paths"
    cp -r docs/features docs/features.backup.paths
fi

# Zähle vorher
echo "📊 Counting absolute paths before fix..."
BEFORE_COUNT=$(grep -r "](/docs/features/" docs/features/ --include="*.md" | wc -l | tr -d ' ')
BEFORE_USER_PATHS=$(grep -r "](/Users/joergstreeck/" docs/features/ --include="*.md" | wc -l | tr -d ' ')
echo "  - /docs/features/ paths: $BEFORE_COUNT"
echo "  - /Users/joergstreeck/ paths: $BEFORE_USER_PATHS"

echo ""
echo "🔄 Fixing paths..."

# 1. Entferne /docs/features/ Präfix für relative Pfade
echo "- Converting /docs/features/ to relative paths..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/docs/features/|](../../|g' {} \;

# 2. Entferne vollständige absolute Pfade
echo "- Converting /Users/joergstreeck/freshplan-sales-tool/docs/features/ to relative..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/Users/joergstreeck/freshplan-sales-tool/docs/features/|](../../|g' {} \;

# 3. Master Plan Pfade korrigieren
echo "- Fixing CRM_COMPLETE_MASTER_PLAN_V5.md paths..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|]/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md|]../../../CRM_COMPLETE_MASTER_PLAN_V5.md|g' {} \;

# 4. Docs root Pfade
echo "- Fixing /Users/joergstreeck/freshplan-sales-tool/docs/ paths..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/Users/joergstreeck/freshplan-sales-tool/docs/|](../../../|g' {} \;

# 5. Pfadnummern-Mapping korrigieren (bekannte Fälle)
echo "- Fixing known path number mismatches..."

# FC-018 ist in 22_mobile_light, nicht 18_mobile_pwa
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|/18_mobile_pwa/FC-018_|/22_mobile_light/FC-018_|g' {} \;

# FC-038 ist in 38_multitenant
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|/38_multitenant/FC-038_|/38_multi_tenant/FC-038_|g' {} \;

# M7 Settings - verschiedene Schreibweisen vereinheitlichen
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|M7_SETTINGS_TECH_CONCEPT.md|M7_TECH_CONCEPT.md|g' {} \;

# Zähle nachher
echo ""
echo "📊 Counting absolute paths after fix..."
AFTER_COUNT=$(grep -r "](/docs/features/" docs/features/ --include="*.md" 2>/dev/null | wc -l | tr -d ' ' || echo "0")
AFTER_USER_PATHS=$(grep -r "](/Users/joergstreeck/" docs/features/ --include="*.md" 2>/dev/null | wc -l | tr -d ' ' || echo "0")
echo "  - /docs/features/ paths: $AFTER_COUNT"
echo "  - /Users/joergstreeck/ paths: $AFTER_USER_PATHS"

echo ""
echo "✅ Fixed: $((BEFORE_COUNT - AFTER_COUNT)) /docs/features/ paths"
echo "✅ Fixed: $((BEFORE_USER_PATHS - AFTER_USER_PATHS)) /Users/joergstreeck/ paths"

# Rollback-Anleitung
echo ""
echo "💡 To rollback if needed:"
echo "   rm -rf docs/features"
echo "   mv docs/features.backup.paths docs/features"

echo ""
echo "🎯 Next step: Run comprehensive link test again"
echo "   ./tests/comprehensive-link-test.sh"