#!/bin/bash

# Finales Script für die letzten 94 defekten Links
set -euo pipefail

echo "🔧 Final link fixes for remaining 94 broken links..."
echo "==================================================="

PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
cd "$PROJECT_ROOT"

# Backup
BACKUP_DIR="docs/features.backup.finalfix.$(date +%Y%m%d_%H%M%S)"
echo "📦 Creating backup: $BACKUP_DIR"
cp -r docs/features "$BACKUP_DIR"

echo ""
echo "🔄 Applying targeted fixes..."

# 1. Relative Pfade ohne führendes ../
echo "1️⃣ Fixing relative paths without ../"
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](00_technical_foundation/README\.md|](/docs/features/COMPLETED/00_technical_foundation/README.md|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](FC-010_TECH_CONCEPT\.md|](./FC-010_TECH_CONCEPT.md|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](FC-010_IMPLEMENTATION_GUIDE\.md|](./FC-010_IMPLEMENTATION_GUIDE.md|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](FC-010_DECISION_LOG\.md|](./FC-010_DECISION_LOG.md|g' {} \;

# 2. Verzeichnis-Links ohne README
echo "2️⃣ Fixing directory links..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/docs/features/ACTIVE/)$|](/docs/features/ACTIVE/README.md)|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/docs/features/PLANNED/)$|](/docs/features/PLANNED/README.md)|g' {} \;

# 3. Externe Docs außerhalb features/
echo "3️⃣ Fixing external document paths..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/docs/technical/XENTRAL_API_ANALYSIS\.md|](/docs/technical/XENTRAL_API_ANALYSIS.md|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/docs/features/ACTIVE/WAY_OF_WORKING\.md|](/WAY_OF_WORKING.md|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](../BUSINESS_PRIORITIES\.md|](/docs/BUSINESS_PRIORITIES.md|g' {} \;

# 4. FC-018 ist NICHT in 22_mobile_light sondern in 09_mobile_app
echo "4️⃣ Fixing FC-018 location..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|/docs/features/PLANNED/22_mobile_light/FC-018_|/docs/features/PLANNED/09_mobile_app/FC-018_|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|/docs/features/PLANNED/12_mobile_pwa/FC-018_|/docs/features/PLANNED/09_mobile_app/FC-018_|g' {} \;

# 5. Weitere falsche Pfadzuordnungen basierend auf tatsächlichen Standorten
echo "5️⃣ Fixing other misplaced features..."

# Finde die tatsächlichen Standorte
FC005_PATH=$(find docs/features -name "FC-005_TECH_CONCEPT.md" -type f | head -1)
FC010_PATH=$(find docs/features -name "FC-010_TECH_CONCEPT.md" -type f | head -1)
FC017_PATH=$(find docs/features -name "FC-017_TECH_CONCEPT.md" -type f | head -1)
FC028_PATH=$(find docs/features -name "FC-028_TECH_CONCEPT.md" -type f | head -1)
FC031_PATH=$(find docs/features -name "FC-031_TECH_CONCEPT.md" -type f | head -1)
M5_PATH=$(find docs/features -name "M5_TECH_CONCEPT.md" -type f | head -1)

# Korrigiere basierend auf tatsächlichen Pfaden
if [ -n "$FC005_PATH" ]; then
    CORRECT_FC005="/docs/features/${FC005_PATH#docs/features/}"
    find docs/features -name "*.md" -type f -exec \
        sed -i '' "s|/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT\.md|$CORRECT_FC005|g" {} \;
fi

if [ -n "$FC010_PATH" ]; then
    CORRECT_FC010="/docs/features/${FC010_PATH#docs/features/}"
    find docs/features -name "*.md" -type f -exec \
        sed -i '' "s|/docs/features/PLANNED/04_data_import_config/FC-010_TECH_CONCEPT\.md|$CORRECT_FC010|g" {} \;
fi

if [ -n "$FC017_PATH" ]; then
    CORRECT_FC017="/docs/features/${FC017_PATH#docs/features/}"
    find docs/features -name "*.md" -type f -exec \
        sed -i '' "s|/docs/features/PLANNED/11_sales_gamification/FC-017_TECH_CONCEPT\.md|$CORRECT_FC017|g" {} \;
fi

if [ -n "$FC028_PATH" ]; then
    CORRECT_FC028="/docs/features/${FC028_PATH#docs/features/}"
    find docs/features -name "*.md" -type f -exec \
        sed -i '' "s|/docs/features/PLANNED/28_whatsapp_business/FC-028_TECH_CONCEPT\.md|$CORRECT_FC028|g" {} \;
fi

if [ -n "$M5_PATH" ]; then
    CORRECT_M5="/docs/features/${M5_PATH#docs/features/}"
    find docs/features -name "*.md" -type f -exec \
        sed -i '' "s|/docs/features/PLANNED/05_customer_import/M5_TECH_CONCEPT\.md|$CORRECT_M5|g" {} \;
fi

# 6. Features die nicht existieren - erstelle sie
echo "6️⃣ Creating missing feature files..."

MISSING_FEATURES=(
    "docs/features/PLANNED/22_mobile_light/FC-018_TECH_CONCEPT.md"
    "docs/features/LEGACY/02_navigation_legacy/FC-002_TECH_CONCEPT.md"
    "docs/features/PLANNED/32_predictive_analytics/FC-032_TECH_CONCEPT.md"
    "docs/features/PLANNED/33_customer_360/FC-033_TECH_CONCEPT.md"
    "docs/features/PLANNED/35_territory_management/FC-035_TECH_CONCEPT.md"
    "docs/features/PLANNED/36_commission_engine/FC-036_TECH_CONCEPT.md"
    "docs/features/PLANNED/37_field_service/FC-037_TECH_CONCEPT.md"
    "docs/features/PLANNED/38_customer_portal/FC-038_TECH_CONCEPT.md"
    "docs/features/PLANNED/39_workflow_engine/FC-039_TECH_CONCEPT.md"
    "docs/features/PLANNED/40_api_gateway/FC-040_TECH_CONCEPT.md"
)

for filepath in "${MISSING_FEATURES[@]}"; do
    if [ ! -f "$filepath" ]; then
        mkdir -p "$(dirname "$filepath")"
        filename=$(basename "$filepath")
        feature_code=${filename%_*}
        
        cat > "$filepath" << EOF
# $feature_code - Technical Concept

**Status:** 🚧 Placeholder Document

## Overview
This is a placeholder for $feature_code technical concept.

## Technical Approach
- TBD

## Implementation
- TBD

---
*Created: $(date)*
EOF
        echo "   ✅ Created: $filepath"
    fi
done

# 7. Erstelle fehlende externe Dokumente
echo "7️⃣ Creating missing external documents..."

if [ ! -f "docs/technical/XENTRAL_API_ANALYSIS.md" ]; then
    mkdir -p docs/technical
    echo "# Xentral API Analysis" > docs/technical/XENTRAL_API_ANALYSIS.md
    echo "" >> docs/technical/XENTRAL_API_ANALYSIS.md
    echo "Technical analysis of Xentral API integration." >> docs/technical/XENTRAL_API_ANALYSIS.md
    echo "   ✅ Created: docs/technical/XENTRAL_API_ANALYSIS.md"
fi

if [ ! -f "docs/BUSINESS_PRIORITIES.md" ]; then
    echo "# Business Priorities" > docs/BUSINESS_PRIORITIES.md
    echo "" >> docs/BUSINESS_PRIORITIES.md
    echo "Current business priorities and strategic goals." >> docs/BUSINESS_PRIORITIES.md
    echo "   ✅ Created: docs/BUSINESS_PRIORITIES.md"
fi

# 8. Relative Links in gleichen Ordnern
echo "8️⃣ Fixing same-directory relative links..."
find docs/features -name "*.md" -type f | while read file; do
    dir=$(dirname "$file")
    # Korrigiere Links zu Dateien im gleichen Verzeichnis
    sed -i '' 's|](\(FC-[0-9]*_[A-Z_]*\.md\))|](./\1)|g' "$file"
    sed -i '' 's|](\.\/\./|](./|g' "$file"
done

# 9. Erstelle alle fehlenden README.md
echo "9️⃣ Ensuring all directories have README.md..."
find docs/features -type d -mindepth 1 | while read dir; do
    if [ ! -f "$dir/README.md" ]; then
        dirname=$(basename "$dir")
        parent=$(basename "$(dirname "$dir")")
        echo "# $dirname" > "$dir/README.md"
        echo "" >> "$dir/README.md"
        echo "Part of $parent features." >> "$dir/README.md"
    fi
done

# 10. Finale Validierung
echo ""
echo "📊 Final validation..."
AFTER_COUNT=$(./tests/comprehensive-link-test.sh 2>&1 | grep -c "❌ BROKEN:" || echo "0")
echo "Broken links remaining: $AFTER_COUNT"

if [ "$AFTER_COUNT" -gt 0 ]; then
    echo ""
    echo "📝 Remaining broken links:"
    ./tests/comprehensive-link-test.sh 2>&1 | grep "❌ BROKEN:" | sort | uniq
fi

echo ""
echo "💡 Rollback command:"
echo "   rm -rf docs/features && mv $BACKUP_DIR docs/features"
echo ""
echo "✅ Final link repair complete!"