#!/bin/bash

# Letztes Script für die finalen 41 defekten Links
set -euo pipefail

echo "🔧 Fixing the last 41 broken links..."
echo "====================================="

PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
cd "$PROJECT_ROOT"

# Backup
BACKUP_DIR="docs/features.backup.last41.$(date +%Y%m%d_%H%M%S)"
echo "📦 Creating backup: $BACKUP_DIR"
cp -r docs/features "$BACKUP_DIR"

echo ""
echo "🔄 Creating missing files and fixing paths..."

# 1. Erstelle fehlende Dateien im gleichen Verzeichnis (./xxx.md)
echo "1️⃣ Creating missing same-directory files..."

# Finde alle Dateien die ./FC-010_*.md referenzieren
find docs/features -name "*.md" -type f | while read file; do
    dir=$(dirname "$file")
    
    # Prüfe ob FC-010 Dateien im gleichen Verzeichnis benötigt werden
    if grep -q "\./FC-010_DECISION_LOG\.md" "$file" && [ ! -f "$dir/FC-010_DECISION_LOG.md" ]; then
        echo "# FC-010 Decision Log" > "$dir/FC-010_DECISION_LOG.md"
        echo "" >> "$dir/FC-010_DECISION_LOG.md"
        echo "Decision log for FC-010 implementation." >> "$dir/FC-010_DECISION_LOG.md"
        echo "   ✅ Created: $dir/FC-010_DECISION_LOG.md"
    fi
    
    if grep -q "\./FC-010_IMPLEMENTATION_GUIDE\.md" "$file" && [ ! -f "$dir/FC-010_IMPLEMENTATION_GUIDE.md" ]; then
        echo "# FC-010 Implementation Guide" > "$dir/FC-010_IMPLEMENTATION_GUIDE.md"
        echo "" >> "$dir/FC-010_IMPLEMENTATION_GUIDE.md"
        echo "Implementation guide for FC-010." >> "$dir/FC-010_IMPLEMENTATION_GUIDE.md"
        echo "   ✅ Created: $dir/FC-010_IMPLEMENTATION_GUIDE.md"
    fi
    
    if grep -q "\./FC-010_TECH_CONCEPT\.md" "$file" && [ ! -f "$dir/FC-010_TECH_CONCEPT.md" ]; then
        # Prüfe ob es bereits ein FC-010_TECH_CONCEPT.md woanders gibt
        EXISTING=$(find docs/features -name "FC-010_TECH_CONCEPT.md" -type f | head -1)
        if [ -n "$EXISTING" ]; then
            # Ändere den Link zum existierenden Pfad
            CORRECT_PATH="/docs/features/${EXISTING#docs/features/}"
            sed -i '' "s|\./FC-010_TECH_CONCEPT\.md|$CORRECT_PATH|g" "$file"
            echo "   📝 Updated link in $file to existing FC-010"
        else
            echo "# FC-010 Technical Concept" > "$dir/FC-010_TECH_CONCEPT.md"
            echo "" >> "$dir/FC-010_TECH_CONCEPT.md"
            echo "Technical concept for FC-010." >> "$dir/FC-010_TECH_CONCEPT.md"
            echo "   ✅ Created: $dir/FC-010_TECH_CONCEPT.md"
        fi
    fi
    
    # Gleiche Logik für IMPLEMENTATION_BACKEND/FRONTEND
    if grep -q "IMPLEMENTATION_BACKEND\.md" "$file" && [ ! -f "$dir/IMPLEMENTATION_BACKEND.md" ]; then
        echo "# Implementation Backend" > "$dir/IMPLEMENTATION_BACKEND.md"
        echo "" >> "$dir/IMPLEMENTATION_BACKEND.md"
        echo "Backend implementation details." >> "$dir/IMPLEMENTATION_BACKEND.md"
        echo "   ✅ Created: $dir/IMPLEMENTATION_BACKEND.md"
    fi
    
    if grep -q "IMPLEMENTATION_FRONTEND\.md" "$file" && [ ! -f "$dir/IMPLEMENTATION_FRONTEND.md" ]; then
        echo "# Implementation Frontend" > "$dir/IMPLEMENTATION_FRONTEND.md"
        echo "" >> "$dir/IMPLEMENTATION_FRONTEND.md"
        echo "Frontend implementation details." >> "$dir/IMPLEMENTATION_FRONTEND.md"
        echo "   ✅ Created: $dir/IMPLEMENTATION_FRONTEND.md"
    fi
done

# 2. Erstelle fehlende Features mit korrekten Pfaden
echo "2️⃣ Creating missing feature files..."

MISSING_FILES=(
    "docs/features/LEGACY/pdf_generator/PDF_GENERATOR_TECH_CONCEPT.md"
    "docs/features/PLANNED/01_customer_management/FC-019_TECH_CONCEPT.md"
    "docs/features/PLANNED/02_opportunity_pipeline/FC-016_TECH_CONCEPT.md"
    "docs/features/PLANNED/18_sales_gamification/FC-017_TECH_CONCEPT.md"
    "docs/features/PLANNED/19_mobile_pwa/FC-018_TECH_CONCEPT.md"
    "docs/features/PLANNED/31_dynamic_documents/FC-031_TECH_CONCEPT.md"
    "docs/features/PLANNED/32_field_service_time/FC-032_TECH_CONCEPT.md"
    "docs/features/PLANNED/33_sla_management/FC-033_TECH_CONCEPT.md"
    "docs/features/PLANNED/21_integration_hub/FC-021_IMPLEMENTATION_GUIDE.md"
    "docs/features/PLANNED/21_integration_hub/ADAPTER_DEVELOPMENT_GUIDE.md"
    "docs/features/PLANNED/23_event_sourcing/FC-023_IMPLEMENTATION_GUIDE.md"
    "docs/features/PLANNED/23_event_sourcing/EVENT_CATALOG.md"
    "docs/features/PLANNED/23_event_sourcing/EVENT_SOURCING_PATTERNS.md"
    "docs/features/PLANNED/26_analytics_platform/FC-026_IMPLEMENTATION_GUIDE.md"
    "docs/features/PLANNED/26_analytics_platform/EVENT_TRACKING_PLAN.md"
    "docs/features/PLANNED/29_voice_first/FC-029_IMPLEMENTATION_GUIDE.md"
    "docs/features/PLANNED/29_voice_first/VOICE_COMMANDS.md"
    "docs/features/PLANNED/29_voice_first/SPEECH_GRAMMAR.md"
)

for filepath in "${MISSING_FILES[@]}"; do
    if [ ! -f "$filepath" ]; then
        mkdir -p "$(dirname "$filepath")"
        filename=$(basename "$filepath")
        
        # Bestimme Titel basierend auf Dateiname
        if [[ $filename == FC-* ]]; then
            feature_code=${filename%_*}
            title="$feature_code - ${filename#*_}"
            title=${title%.md}
        else
            title=${filename%.md}
            title=${title//_/ }
        fi
        
        cat > "$filepath" << EOF
# $title

**Status:** 🚧 Placeholder Document

## Overview
Placeholder for $title documentation.

## Details
- TBD

---
*Created: $(date)*
EOF
        echo "   ✅ Created: $filepath"
    fi
done

# 3. Erstelle fehlende technische Dokumente
echo "3️⃣ Creating missing technical documents..."

TECH_DOCS=(
    "docs/technical/API_CONTRACT.md"
    "docs/technical/BACKEND_START_GUIDE.md"
    "docs/technical/FRONTEND_BACKEND_SPECIFICATION.md"
    "docs/team/DEVELOPMENT_SETUP.md"
)

for doc in "${TECH_DOCS[@]}"; do
    if [ ! -f "$doc" ]; then
        mkdir -p "$(dirname "$doc")"
        filename=$(basename "$doc")
        title=${filename%.md}
        title=${title//_/ }
        
        cat > "$doc" << EOF
# $title

## Overview
$title documentation.

## Content
- TBD

---
*Placeholder created: $(date)*
EOF
        echo "   ✅ Created: $doc"
    fi
done

# 4. Erstelle pdf-generator Verzeichnis
echo "4️⃣ Creating pdf-generator directory..."
if [ ! -d "docs/features/ACTIVE/pdf-generator" ]; then
    mkdir -p "docs/features/ACTIVE/pdf-generator"
    echo "# PDF Generator Module" > "docs/features/ACTIVE/pdf-generator/README.md"
    echo "" >> "docs/features/ACTIVE/pdf-generator/README.md"
    echo "PDF generation functionality." >> "docs/features/ACTIVE/pdf-generator/README.md"
    echo "   ✅ Created: docs/features/ACTIVE/pdf-generator/"
fi

# 5. Verzeichnis-Links korrigieren
echo "5️⃣ Fixing directory links to include README.md..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/docs/features/ACTIVE/)$|](/docs/features/ACTIVE/README.md)|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/docs/features/PLANNED/)$|](/docs/features/PLANNED/README.md)|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/docs/features/ACTIVE/pdf-generator/)$|](/docs/features/ACTIVE/pdf-generator/README.md)|g' {} \;

# 6. Finale Validierung
echo ""
echo "📊 Final validation..."
REMAINING=$(./tests/comprehensive-link-test.sh 2>&1 | grep -c "❌ BROKEN:" || echo "0")
echo "Broken links remaining: $REMAINING"

if [ "$REMAINING" -eq "0" ]; then
    echo ""
    echo "🎉 SUCCESS! All links are now working!"
else
    echo ""
    echo "📝 Still broken:"
    ./tests/comprehensive-link-test.sh 2>&1 | grep "❌ BROKEN:" | sort | uniq
fi

echo ""
echo "💡 Rollback: rm -rf docs/features && mv $BACKUP_DIR docs/features"
echo ""
echo "✅ Link repair process complete!"