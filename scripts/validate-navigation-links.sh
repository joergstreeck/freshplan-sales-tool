#!/bin/bash

# Navigation Link Validator für FreshPlan Sales Tool
# Prüft alle Navigation-Links in den Feature-Dokumenten

echo "🔍 Navigation Link Validator"
echo "=========================="
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
        
        # Skip external links and anchors
        if [[ $path =~ ^http || $path =~ ^# ]]; then
            return 0
        fi
        
        # Convert relative to absolute path
        local full_path="$BASE_DIR$path"
        
        if [[ ! -f "$full_path" ]]; then
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
echo -e "Broken Links Found: ${RED}$BROKEN_LINKS${NC}"
echo -e "Files Missing Navigation: ${YELLOW}$MISSING_NAV${NC}"
echo ""

# Report broken links
if [[ $BROKEN_LINKS -gt 0 ]]; then
    echo "❌ Broken Links Report:"
    echo "====================="
    for broken in "${BROKEN_LINK_REPORT[@]}"; do
        echo "$broken"
    done
    echo ""
fi

# Report missing navigation
if [[ $MISSING_NAV -gt 0 ]]; then
    echo "⚠️  Files Missing Navigation:"
    echo "=========================="
    for missing in "${MISSING_NAV_FILES[@]}"; do
        echo "$missing"
    done
    echo ""
fi

# Final status
if [[ $BROKEN_LINKS -eq 0 && $MISSING_NAV -eq 0 ]]; then
    echo -e "${GREEN}✅ All navigation links are valid!${NC}"
    exit 0
else
    echo -e "${RED}❌ Validation failed - please fix the issues above${NC}"
    exit 1
fi