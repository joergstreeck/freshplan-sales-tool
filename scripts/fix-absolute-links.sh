#!/bin/bash

# Script zur Korrektur aller Links zu absoluten Pfaden
set -euo pipefail

echo "🔧 Converting all links to absolute paths..."
echo "==========================================="

PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
cd "$PROJECT_ROOT"

# Backup erstellen
BACKUP_DIR="docs/features.backup.absolute.$(date +%Y%m%d_%H%M%S)"
echo "📦 Creating backup: $BACKUP_DIR"
cp -r docs/features "$BACKUP_DIR"

echo ""
echo "🔄 Converting to absolute paths..."

# 1. Relative Pfade zu absoluten konvertieren
echo "- Converting relative paths to absolute..."

# Für ../ Pfade
find docs/features -name "*.md" -type f | while read file; do
    # Bestimme die Verzeichnistiefe relativ zu docs/features
    rel_path="${file#docs/features/}"
    depth=$(echo "$rel_path" | tr '/' '\n' | wc -l | tr -d ' ')
    depth=$((depth - 1))  # Eine Ebene weniger für die Datei selbst
    
    # Ersetze relative Pfade basierend auf der Tiefe
    if [ $depth -eq 2 ]; then
        # z.B. docs/features/ACTIVE/01_security/file.md
        sed -i '' 's|](../\.\./|](/docs/features/|g' "$file"
        sed -i '' 's|](\.\./|](/docs/features/ACTIVE/|g' "$file"
        sed -i '' 's|](\./|](/docs/features/ACTIVE/01_security/|g' "$file"
    elif [ $depth -eq 3 ]; then
        # z.B. docs/features/ACTIVE/01_security/subfolder/file.md
        sed -i '' 's|](../\.\./\.\./|](/docs/features/|g' "$file"
        sed -i '' 's|](../\.\./|](/docs/features/ACTIVE/|g' "$file"
        sed -i '' 's|](\.\./|](/docs/features/ACTIVE/01_security/|g' "$file"
    fi
done

# 2. Bekannte Pfad-Mappings korrigieren
echo "- Fixing known path mappings..."

# FC-018 ist in 22_mobile_light
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|/docs/features/PLANNED/18_mobile_pwa/FC-018_|/docs/features/PLANNED/22_mobile_light/FC-018_|g' {} \;

# FC-038 multi_tenant vs multitenant
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|/docs/features/PLANNED/38_multitenant/FC-038_|/docs/features/PLANNED/38_multi_tenant/FC-038_|g' {} \;

# M7_SETTINGS_TECH_CONCEPT zu M7_TECH_CONCEPT
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|M7_SETTINGS_TECH_CONCEPT\.md|M7_TECH_CONCEPT.md|g' {} \;

# 3. Master Plan und andere Docs außerhalb features/
echo "- Fixing paths to docs outside features/..."
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](/CRM_COMPLETE_MASTER_PLAN_V5\.md|](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](../\.\./CRM_COMPLETE_MASTER_PLAN_V5\.md|](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md|g' {} \;
find docs/features -name "*.md" -type f -exec \
    sed -i '' 's|](../\.\./\.\./CRM_COMPLETE_MASTER_PLAN_V5\.md|](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md|g' {} \;

# 4. Überprüfe auf fehlende Dateien und erstelle Platzhalter
echo ""
echo "🔍 Checking for missing target files..."

# Liste aller Links sammeln
grep -r "](/docs/features/" docs/features/ --include="*.md" | \
    sed 's/.*](\(\/docs\/features\/[^)]*\)).*/\1/' | \
    sort | uniq | while read link_path; do
    
    # Konvertiere absoluten Pfad zu relativem Pfad
    rel_path="${link_path#/}"
    
    # Prüfe ob Datei existiert
    if [ ! -f "$rel_path" ]; then
        echo "  ⚠️  Missing: $link_path"
        
        # Erstelle Verzeichnis falls nötig
        dir=$(dirname "$rel_path")
        if [ ! -d "$dir" ]; then
            mkdir -p "$dir"
        fi
        
        # Erstelle Platzhalter-Datei
        filename=$(basename "$link_path")
        feature_name=${filename%_*}
        
        cat > "$rel_path" << EOF
# $feature_name - Placeholder

**Status:** 🚧 Dokument noch nicht erstellt

Dieses Dokument ist ein Platzhalter für einen defekten Link.

## TODO
- [ ] Inhalt aus vorhandenen Dokumenten übertragen
- [ ] Struktur gemäß Template erstellen
- [ ] Links validieren

---
*Automatisch erstellt am $(date)*
EOF
        echo "    ✅ Created placeholder: $rel_path"
    fi
done

# 5. Statistiken
echo ""
echo "📊 Link Statistics:"
ABSOLUTE_COUNT=$(grep -r "](/docs/features/" docs/features/ --include="*.md" | wc -l | tr -d ' ')
echo "  - Absolute links: $ABSOLUTE_COUNT"

# Rollback-Anleitung
echo ""
echo "💡 To rollback if needed:"
echo "   rm -rf docs/features"
echo "   mv $BACKUP_DIR docs/features"

echo ""
echo "🎯 Next step: Run comprehensive link test"
echo "   ./tests/comprehensive-link-test.sh"