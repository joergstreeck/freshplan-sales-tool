#!/bin/bash
# FreshPlan Documentation Link Updater
# Hilft beim Aktualisieren von Links nach Umstrukturierungen

echo "📝 Documentation Link Updater"
echo "============================"

# Mapping von alten zu neuen Pfaden
declare -A path_mappings=(
    ["docs/adr/"]="docs/architecture/"
    ["docs/team/"]="docs/setup/"
    ["docs/guides/"]="docs/development/"
    ["docs/technical/"]="docs/reference/"
    ["docs/maintenance/"]="docs/development/"
)

# Zeige geplante Änderungen
echo "Geplante Pfad-Änderungen:"
for old in "${!path_mappings[@]}"; do
    echo "  $old → ${path_mappings[$old]}"
done

echo ""
read -p "Änderungen durchführen? (y/n) " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Abgebrochen."
    exit 0
fi

# Führe Updates durch
for old_path in "${!path_mappings[@]}"; do
    new_path="${path_mappings[$old_path]}"
    
    echo "Updating: $old_path → $new_path"
    
    # Update in allen Markdown-Dateien
    find . -name "*.md" -not -path "*/node_modules/*" -not -path "*/freshplan-backup-*/*" \
        -exec sed -i.bak "s|${old_path}|${new_path}|g" {} \;
done

# Aufräumen der Backup-Dateien
find . -name "*.md.bak" -delete

echo "✅ Link-Updates abgeschlossen!"
echo ""
echo "Führe Link-Check aus..."
./scripts/check-doc-links.sh