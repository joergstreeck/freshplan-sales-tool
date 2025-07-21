#!/bin/bash

# Comprehensive Link Test - Finds ALL broken links
echo "🔍 Comprehensive Link Test"
echo "=========================="

PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
cd "$PROJECT_ROOT"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Counters
TOTAL_FILES=0
TOTAL_LINKS=0
BROKEN_LINKS=0
KOMPAKT_LINKS=0

# Find all broken links
echo "Scanning ALL markdown files in docs/features/..."
echo ""

# Function to check a single file
check_file() {
    local file="$1"
    local file_links=0
    local file_broken=0
    
    # Extract all markdown links
    while IFS= read -r line; do
        if [[ $line =~ \[([^\]]+)\]\(([^\)]+)\) ]]; then
            link_text="${BASH_REMATCH[1]}"
            link_path="${BASH_REMATCH[2]}"
            ((TOTAL_LINKS++))
            ((file_links++))
            
            # Skip external links and anchors
            if [[ $link_path =~ ^http ]] || [[ $link_path =~ ^# ]] || [[ $link_path =~ ^mailto: ]]; then
                continue
            fi
            
            # Check for KOMPAKT links
            if [[ $link_path =~ KOMPAKT\.md ]]; then
                echo -e "${YELLOW}  ⚠️  KOMPAKT: $link_path${NC}"
                ((KOMPAKT_LINKS++))
                ((BROKEN_LINKS++))
                ((file_broken++))
                continue
            fi
            
            # Clean path
            clean_path="${link_path#/}"
            
            # Check if file exists
            if [ ! -f "$PROJECT_ROOT/$clean_path" ] && [ ! -f "$PROJECT_ROOT/docs/$clean_path" ]; then
                echo -e "${RED}  ❌ BROKEN: $link_path${NC}"
                ((BROKEN_LINKS++))
                ((file_broken++))
            fi
        fi
    done < "$file"
    
    if [ $file_broken -gt 0 ]; then
        echo -e "${RED}$file: $file_broken broken links${NC}"
    elif [ $file_links -gt 0 ]; then
        echo -e "${GREEN}$file: ✓ ($file_links links OK)${NC}"
    fi
}

# Find all markdown files
while IFS= read -r file; do
    ((TOTAL_FILES++))
    check_file "$file"
done < <(find docs/features -name "*.md" -type f)

echo ""
echo "========================================"
echo "SUMMARY:"
echo "========================================"
echo "Total files scanned: $TOTAL_FILES"
echo "Total links found: $TOTAL_LINKS"
echo -e "${YELLOW}KOMPAKT links (outdated): $KOMPAKT_LINKS${NC}"
echo -e "${RED}Other broken links: $((BROKEN_LINKS - KOMPAKT_LINKS))${NC}"
echo -e "${RED}TOTAL BROKEN LINKS: $BROKEN_LINKS${NC}"

if [ $BROKEN_LINKS -eq 0 ]; then
    echo -e "\n${GREEN}✅ All links are valid!${NC}"
    exit 0
else
    echo -e "\n${RED}❌ Found $BROKEN_LINKS broken links!${NC}"
    
    # List files with KOMPAKT links
    echo -e "\n${YELLOW}Files with KOMPAKT links:${NC}"
    grep -l "KOMPAKT\.md" docs/features/**/*.md | sort | uniq
    
    exit 1
fi