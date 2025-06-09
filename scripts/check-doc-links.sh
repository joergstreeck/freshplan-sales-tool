#!/bin/bash
# FreshPlan Documentation Link Checker
# Prüft alle Markdown-Links auf Gültigkeit

echo "🔍 Checking documentation links..."

# Farben
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Counters
total_links=0
broken_links=0
broken_files=""

# Finde alle Markdown-Dateien (außer node_modules und Backups)
while IFS= read -r file; do
    # Extrahiere alle internen Links aus der Datei
    while IFS= read -r link; do
        ((total_links++))
        
        # Überspringe externe Links
        if [[ $link =~ ^https?:// ]]; then
            continue
        fi
        
        # Überspringe Anker-Links
        if [[ $link =~ ^# ]]; then
            continue
        fi
        
        # Konstruiere absoluten Pfad
        if [[ $link =~ ^/ ]]; then
            # Absoluter Pfad vom Projekt-Root
            target_path="${link#/}"
        else
            # Relativer Pfad
            dir=$(dirname "$file")
            target_path="$dir/$link"
        fi
        
        # Normalisiere Pfad (entferne ./ und ../)
        target_path=$(cd "$(dirname "$target_path")" 2>/dev/null && pwd)/$(basename "$target_path")
        target_path=${target_path#$PWD/}
        
        # Prüfe ob Datei existiert
        if [ ! -f "$target_path" ]; then
            echo -e "${RED}❌ Broken link in ${file}:${NC}"
            echo "   Link: $link"
            echo "   Expected: $target_path"
            ((broken_links++))
            broken_files="$broken_files\n$file"
        fi
    done < <(grep -oE '\[([^]]+)\]\(([^)]+\.md[^)]*)\)' "$file" | sed -E 's/\[[^]]+\]\(([^)]+)\)/\1/')
done < <(find . -name "*.md" -not -path "*/node_modules/*" -not -path "*/freshplan-backup-*/*" -not -path "*/.archive/*")

# Summary
echo ""
echo "📊 Link Check Summary:"
echo "   Total links checked: $total_links"
echo "   Broken links found: $broken_links"

if [ $broken_links -eq 0 ]; then
    echo -e "${GREEN}✅ All documentation links are valid!${NC}"
    exit 0
else
    echo -e "${RED}❌ Found $broken_links broken links!${NC}"
    echo -e "${YELLOW}Affected files:${NC}"
    echo -e "$broken_files" | sort | uniq
    exit 1
fi