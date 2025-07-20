#!/bin/bash

# Navigation Link Validator für FreshPlan Sales Tool - Version 2
# Prüft alle Navigation-Links in den Feature-Dokumenten
# Ignoriert *(geplant)* Markierungen

echo "🔍 Navigation Link Validator v2"
echo "==============================="
echo ""

# Farben für Output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
TOTAL_FILES=0
TOTAL_LINKS=0
BROKEN_LINKS=0
MISSING_NAV=0
PLANNED_LINKS=0

# Arrays für Reporting
declare -a BROKEN_LINK_REPORT
declare -a MISSING_NAV_FILES

# Base directory
BASE_DIR="/Users/joergstreeck/freshplan-sales-tool"
DOCS_DIR="$BASE_DIR/docs"

echo "📁 Scanning Feature Documents..."
echo ""

# Function to check if file exists
check_link() {
    local link=$1
    local source_file=$2
    
    # Extract path from markdown link
    if [[ $link =~ \[.*\]\((.*)\) ]]; then
        local path="${BASH_REMATCH[1]}"
        
        # Remove *(geplant)* and everything after it
        path=$(echo "$path" | sed 's/ \*([^)]*)\*.*//')
        
        # Check if this is a planned link
        if [[ $link =~ \*\(geplant\)\* ]]; then
            PLANNED_LINKS=$((PLANNED_LINKS + 1))
            return 0  # Planned links are always OK
        fi
        
        # Skip external links and anchors
        if [[ $path =~ ^http || $path =~ ^# ]]; then
            return 0
        fi
        
        # Resolve relative paths
        local full_path="$path"
        if [[ $path =~ ^\./ ]]; then
            local dir=$(dirname "$BASE_DIR/$source_file")
            full_path="$dir/${path#./}"
        elif [[ $path =~ ^/ ]]; then
            full_path="$BASE_DIR$path"
        fi
        
        # Check if path exists (file or directory)
        if [[ ! -f "$full_path" && ! -d "$full_path" ]]; then
            BROKEN_LINKS=$((BROKEN_LINKS + 1))
            BROKEN_LINK_REPORT+=("❌ $source_file -> $path")
            return 1
        fi
    fi
    return 0
}

# Find all KOMPAKT.md files
for feature_dir in "$DOCS_DIR/features/ACTIVE"/* "$DOCS_DIR/features/PLANNED"/*; do
    if [[ -d "$feature_dir" ]]; then
        for kompakt_file in "$feature_dir"/*KOMPAKT.md; do
            if [[ -f "$kompakt_file" ]]; then
                TOTAL_FILES=$((TOTAL_FILES + 1))
                
                # Get relative path for display
                rel_path=${kompakt_file#$BASE_DIR/}
                
                # Check if file has navigation section
                if grep -q "## 🧭 NAVIGATION & VERWEISE" "$kompakt_file"; then
                    echo -n "✓ Checking $rel_path... "
                    
                    # Extract all markdown links from navigation section
                    nav_section=$(sed -n '/## 🧭 NAVIGATION & VERWEISE/,/^##[^#]/p' "$kompakt_file" | grep -E '\[.*\]\(.*\)')
                    
                    local_broken=0
                    while IFS= read -r line; do
                        if [[ -n "$line" ]]; then
                            TOTAL_LINKS=$((TOTAL_LINKS + 1))
                            if ! check_link "$line" "$rel_path"; then
                                local_broken=$((local_broken + 1))
                            fi
                        fi
                    done <<< "$nav_section"
                    
                    if [[ $local_broken -eq 0 ]]; then
                        echo -e "${GREEN}OK${NC}"
                    else
                        echo -e "${RED}$local_broken broken links${NC}"
                    fi
                else
                    MISSING_NAV=$((MISSING_NAV + 1))
                    MISSING_NAV_FILES+=("$rel_path")
                    echo -e "${YELLOW}⚠️  $rel_path - MISSING NAVIGATION SECTION${NC}"
                fi
            fi
        done
    fi
done

echo ""
echo "📊 Validation Summary"
echo "===================="
echo "Total Features Checked: $TOTAL_FILES"
echo "Total Links Checked: $TOTAL_LINKS"
echo "Planned Links (ignored): ${GREEN}$PLANNED_LINKS${NC}"
echo "Broken Links Found: ${RED}$BROKEN_LINKS${NC}"
echo "Files Missing Navigation: ${YELLOW}$MISSING_NAV${NC}"

if [[ $BROKEN_LINKS -gt 0 || $MISSING_NAV -gt 0 ]]; then
    echo ""
    
    if [[ $BROKEN_LINKS -gt 0 ]]; then
        echo "❌ Broken Links Report:"
        echo "====================="
        for link in "${BROKEN_LINK_REPORT[@]}"; do
            echo "$link"
        done
    fi
    
    if [[ $MISSING_NAV -gt 0 ]]; then
        echo ""
        echo "⚠️  Files Missing Navigation:"
        echo "=========================="
        for file in "${MISSING_NAV_FILES[@]}"; do
            echo "- $file"
        done
    fi
    
    echo ""
    echo -e "${RED}❌ Validation failed - please fix the issues above${NC}"
    exit 1
else
    echo ""
    echo -e "${GREEN}✅ All navigation links are valid!${NC}"
    echo "📝 Note: $PLANNED_LINKS links marked as *(geplant)* were found and ignored."
    exit 0
fi