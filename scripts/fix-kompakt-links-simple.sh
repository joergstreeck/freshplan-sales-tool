#!/bin/bash

# Einfaches Script zur Reparatur aller KOMPAKT-Links
set -euo pipefail

echo "🔧 Starting KOMPAKT link repair (simplified version)..."
echo "====================================================="

PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
cd "$PROJECT_ROOT"

# Backup erstellen
BACKUP_DIR="docs/features.backup.$(date +%Y%m%d_%H%M%S)"
echo "📦 Creating backup: $BACKUP_DIR"
cp -r docs/features "$BACKUP_DIR"

# Zähle vorher
BEFORE_COUNT=$(grep -r "KOMPAKT\.md" docs/features/ --include="*.md" | wc -l | tr -d ' ')
echo "📊 KOMPAKT links before: $BEFORE_COUNT"

echo ""
echo "🔄 Replacing all KOMPAKT links with TECH_CONCEPT..."
echo "==================================================="

# Generische Ersetzung: KOMPAKT → TECH_CONCEPT
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/KOMPAKT\.md/TECH_CONCEPT.md/g" {} \;

# Spezielle Fälle für Module (falls anders benannt)
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M1_NAVIGATION_KOMPAKT/M1_NAVIGATION_TECH_CONCEPT/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M2_QUICK_CREATE_KOMPAKT/M2_QUICK_CREATE_TECH_CONCEPT/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M3_SALES_COCKPIT_KOMPAKT/M3_SALES_COCKPIT_TECH_CONCEPT/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M4_OPPORTUNITY_PIPELINE_KOMPAKT/M4_OPPORTUNITY_PIPELINE_TECH_CONCEPT/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M5_CUSTOMER_REFACTOR_KOMPAKT/M5_CUSTOMER_REFACTOR_TECH_CONCEPT/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M6_ANALYTICS_MODULE_KOMPAKT/M6_ANALYTICS_MODULE_TECH_CONCEPT/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M7_SETTINGS_ENHANCEMENT_KOMPAKT/M7_SETTINGS_ENHANCEMENT_TECH_CONCEPT/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M8_CALCULATOR_MODAL_KOMPAKT/M8_CALCULATOR_MODAL_TECH_CONCEPT/g" {} \;

# Module ohne _KOMPAKT suffix korrigieren
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M1_NAVIGATION_TECH_CONCEPT\.md/M1_TECH_CONCEPT.md/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M2_QUICK_CREATE_TECH_CONCEPT\.md/M2_TECH_CONCEPT.md/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M3_SALES_COCKPIT_TECH_CONCEPT\.md/M3_TECH_CONCEPT.md/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M4_OPPORTUNITY_PIPELINE_TECH_CONCEPT\.md/M4_TECH_CONCEPT.md/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M5_CUSTOMER_REFACTOR_TECH_CONCEPT\.md/M5_TECH_CONCEPT.md/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M6_ANALYTICS_MODULE_TECH_CONCEPT\.md/M6_TECH_CONCEPT.md/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M7_SETTINGS_ENHANCEMENT_TECH_CONCEPT\.md/M7_TECH_CONCEPT.md/g" {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/M8_CALCULATOR_MODAL_TECH_CONCEPT\.md/M8_TECH_CONCEPT.md/g" {} \;

# Zähle nachher
echo ""
echo "📊 Checking results..."
AFTER_COUNT=$(grep -r "KOMPAKT\.md" docs/features/ --include="*.md" 2>/dev/null | wc -l | tr -d ' ' || echo "0")
echo "📊 KOMPAKT links after: $AFTER_COUNT"
echo "✅ Fixed: $((BEFORE_COUNT - AFTER_COUNT)) links"

# Validierung
if [ "$AFTER_COUNT" -gt 0 ]; then
    echo ""
    echo "⚠️  WARNING: $AFTER_COUNT KOMPAKT links still remaining!"
    echo "Showing first 10 remaining KOMPAKT links:"
    grep -r "KOMPAKT\.md" docs/features/ --include="*.md" | head -10
else
    echo ""
    echo "✅ All KOMPAKT links successfully replaced!"
fi

# Check for broken _TECH_CONCEPT patterns
echo ""
echo "🔍 Checking for correct TECH_CONCEPT patterns..."
TECH_CONCEPT_COUNT=$(grep -r "_TECH_CONCEPT\.md" docs/features/ --include="*.md" | wc -l | tr -d ' ')
echo "📊 Total TECH_CONCEPT links: $TECH_CONCEPT_COUNT"

# Rollback-Anleitung
echo ""
echo "💡 To rollback if needed:"
echo "   rm -rf docs/features"
echo "   mv $BACKUP_DIR docs/features"

echo ""
echo "🎯 Next step: Run comprehensive link test"
echo "   ./tests/comprehensive-link-test.sh"