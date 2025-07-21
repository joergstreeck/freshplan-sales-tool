#!/bin/bash

# Script zur Reparatur aller KOMPAKT-Links
set -euo pipefail

echo "🔧 Starting KOMPAKT link repair..."
echo "=================================="

PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
cd "$PROJECT_ROOT"

# Backup erstellen
BACKUP_DIR="docs/features.backup.$(date +%Y%m%d_%H%M%S)"
echo "📦 Creating backup: $BACKUP_DIR"
cp -r docs/features "$BACKUP_DIR"

# Zähle vorher
BEFORE_COUNT=$(grep -r "KOMPAKT\.md" docs/features/ --include="*.md" | wc -l | tr -d ' ')
echo "📊 KOMPAKT links before: $BEFORE_COUNT"

# Mapping-Regeln definieren
declare -A mappings

# Feature-spezifische Mappings
mappings["FC-001_KOMPAKT.md"]="FC-001_TECH_CONCEPT.md"
mappings["FC-002_KOMPAKT.md"]="FC-002_TECH_CONCEPT.md"
mappings["FC-003_KOMPAKT.md"]="FC-003_TECH_CONCEPT.md"
mappings["FC-004_KOMPAKT.md"]="FC-004_TECH_CONCEPT.md"
mappings["FC-005_KOMPAKT.md"]="FC-005_TECH_CONCEPT.md"
mappings["FC-006_KOMPAKT.md"]="FC-006_TECH_CONCEPT.md"
mappings["FC-007_KOMPAKT.md"]="FC-007_TECH_CONCEPT.md"
mappings["FC-008_KOMPAKT.md"]="FC-008_TECH_CONCEPT.md"
mappings["FC-009_KOMPAKT.md"]="FC-009_TECH_CONCEPT.md"
mappings["FC-010_KOMPAKT.md"]="FC-010_TECH_CONCEPT.md"
mappings["FC-011_KOMPAKT.md"]="FC-011_TECH_CONCEPT.md"
mappings["FC-012_KOMPAKT.md"]="FC-012_TECH_CONCEPT.md"
mappings["FC-013_KOMPAKT.md"]="FC-013_TECH_CONCEPT.md"
mappings["FC-014_KOMPAKT.md"]="FC-014_TECH_CONCEPT.md"
mappings["FC-015_KOMPAKT.md"]="FC-015_TECH_CONCEPT.md"
mappings["FC-016_KOMPAKT.md"]="FC-016_TECH_CONCEPT.md"
mappings["FC-017_KOMPAKT.md"]="FC-017_TECH_CONCEPT.md"
mappings["FC-018_KOMPAKT.md"]="FC-018_TECH_CONCEPT.md"
mappings["FC-019_KOMPAKT.md"]="FC-019_TECH_CONCEPT.md"
mappings["FC-020_KOMPAKT.md"]="FC-020_TECH_CONCEPT.md"
mappings["FC-021_KOMPAKT.md"]="FC-021_TECH_CONCEPT.md"
mappings["FC-022_KOMPAKT.md"]="FC-022_TECH_CONCEPT.md"
mappings["FC-023_KOMPAKT.md"]="FC-023_TECH_CONCEPT.md"
mappings["FC-024_KOMPAKT.md"]="FC-024_TECH_CONCEPT.md"
mappings["FC-025_KOMPAKT.md"]="FC-025_TECH_CONCEPT.md"
mappings["FC-026_KOMPAKT.md"]="FC-026_TECH_CONCEPT.md"
mappings["FC-027_KOMPAKT.md"]="FC-027_TECH_CONCEPT.md"
mappings["FC-028_KOMPAKT.md"]="FC-028_TECH_CONCEPT.md"
mappings["FC-029_KOMPAKT.md"]="FC-029_TECH_CONCEPT.md"
mappings["FC-030_KOMPAKT.md"]="FC-030_TECH_CONCEPT.md"
mappings["FC-031_KOMPAKT.md"]="FC-031_TECH_CONCEPT.md"
mappings["FC-032_KOMPAKT.md"]="FC-032_TECH_CONCEPT.md"
mappings["FC-033_KOMPAKT.md"]="FC-033_TECH_CONCEPT.md"
mappings["FC-034_KOMPAKT.md"]="FC-034_TECH_CONCEPT.md"
mappings["FC-035_KOMPAKT.md"]="FC-035_TECH_CONCEPT.md"
mappings["FC-036_KOMPAKT.md"]="FC-036_TECH_CONCEPT.md"
mappings["FC-037_KOMPAKT.md"]="FC-037_TECH_CONCEPT.md"
mappings["FC-038_KOMPAKT.md"]="FC-038_TECH_CONCEPT.md"
mappings["FC-039_KOMPAKT.md"]="FC-039_TECH_CONCEPT.md"
mappings["FC-040_KOMPAKT.md"]="FC-040_TECH_CONCEPT.md"
mappings["FC-041_KOMPAKT.md"]="FC-041_TECH_CONCEPT.md"

# Module-spezifische Mappings
mappings["M1_NAVIGATION_KOMPAKT.md"]="M1_TECH_CONCEPT.md"
mappings["M2_QUICK_CREATE_KOMPAKT.md"]="M2_TECH_CONCEPT.md"
mappings["M3_SALES_COCKPIT_KOMPAKT.md"]="M3_TECH_CONCEPT.md"
mappings["M4_OPPORTUNITY_PIPELINE_KOMPAKT.md"]="M4_TECH_CONCEPT.md"
mappings["M5_CUSTOMER_REFACTOR_KOMPAKT.md"]="M5_TECH_CONCEPT.md"
mappings["M6_ANALYTICS_MODULE_KOMPAKT.md"]="M6_TECH_CONCEPT.md"
mappings["M7_SETTINGS_ENHANCEMENT_KOMPAKT.md"]="M7_TECH_CONCEPT.md"
mappings["M8_CALCULATOR_MODAL_KOMPAKT.md"]="M8_TECH_CONCEPT.md"

# Durchführen der Ersetzungen
echo ""
echo "🔄 Replacing KOMPAKT links..."
echo "=============================="

# Zähler für Fortschritt
FIXED_COUNT=0

# Spezifische Ersetzungen durchführen
for old in "${!mappings[@]}"; do
    new="${mappings[$old]}"
    
    # Finde alle Dateien mit diesem Pattern
    files_with_pattern=$(grep -rl "$old" docs/features/ --include="*.md" 2>/dev/null || true)
    
    if [ -n "$files_with_pattern" ]; then
        echo "📝 Replacing: $old → $new"
        
        # Ersetze in allen gefundenen Dateien
        while IFS= read -r file; do
            if [ -f "$file" ]; then
                sed -i '' "s|$old|$new|g" "$file"
                ((FIXED_COUNT++))
            fi
        done <<< "$files_with_pattern"
    fi
done

# Generische KOMPAKT → TECH_CONCEPT Ersetzung für übrige Fälle
echo ""
echo "🔄 Generic KOMPAKT replacement..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' "s/_KOMPAKT\.md/_TECH_CONCEPT.md/g" {} \;

# Zähle nachher
AFTER_COUNT=$(grep -r "KOMPAKT\.md" docs/features/ --include="*.md" | wc -l | tr -d ' ')
echo ""
echo "📊 KOMPAKT links after: $AFTER_COUNT"
echo "✅ Fixed: $((BEFORE_COUNT - AFTER_COUNT)) links"

# Validierung
if [ "$AFTER_COUNT" -gt 0 ]; then
    echo ""
    echo "⚠️  WARNING: $AFTER_COUNT KOMPAKT links still remaining!"
    echo "Remaining KOMPAKT links:"
    grep -r "KOMPAKT\.md" docs/features/ --include="*.md" | head -10
else
    echo ""
    echo "✅ All KOMPAKT links successfully replaced!"
fi

# Rollback-Anleitung
echo ""
echo "💡 To rollback if needed:"
echo "   rm -rf docs/features"
echo "   mv $BACKUP_DIR docs/features"

echo ""
echo "🎯 Next step: Run comprehensive link test"
echo "   ./tests/comprehensive-link-test.sh"