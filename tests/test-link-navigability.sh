#!/bin/bash

# Test: Alle Links müssen navigierbar sein
# Simuliert wie ein neuer Claude Links folgen würde

echo "🧭 Link Navigability Test"
echo "========================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Counters
TOTAL_LINKS=0
BROKEN_LINKS=0
ABSOLUTE_BUT_WRONG=0
MISSING_TARGETS=0

echo "Testing link navigability from project root..."
echo "Project root: $PROJECT_ROOT"
echo ""

# Function to resolve and check a link
check_link() {
    local source_file="$1"
    local link_path="$2"
    local link_text="$3"
    
    # Skip external links and anchors
    if [[ $link_path =~ ^http ]] || [[ $link_path =~ ^# ]] || [[ $link_path =~ ^mailto: ]]; then
        return 0
    fi
    
    ((TOTAL_LINKS++))
    
    # Check if link starts with / (absolute from project root)
    if [[ $link_path =~ ^/ ]]; then
        # Remove leading slash and check from project root
        target_path="${link_path#/}"
        
        if [ -f "$PROJECT_ROOT/$target_path" ]; then
            # Link is correct!
            return 0
        else
            # Absolute path but file doesn't exist
            ((ABSOLUTE_BUT_WRONG++))
            ((BROKEN_LINKS++))
            echo -e "${RED}❌ Broken absolute link in:${NC} $source_file"
            echo "   Link: [$link_text]($link_path)"
            echo "   Expected file at: $PROJECT_ROOT/$target_path"
            echo "   Status: FILE NOT FOUND"
            echo ""
            return 1
        fi
    else
        # Relative path - this is problematic!
        if [[ $link_path =~ \.\./ ]]; then
            ((BROKEN_LINKS++))
            echo -e "${YELLOW}⚠️  Relative path in:${NC} $source_file"
            echo "   Link: [$link_text]($link_path)"
            echo "   Problem: Relative paths break navigation"
            echo ""
            return 1
        fi
        
        # Check if it's a path without leading slash (common mistake)
        if [ -f "$PROJECT_ROOT/$link_path" ]; then
            ((BROKEN_LINKS++))
            echo -e "${BLUE}🔧 Missing leading slash in:${NC} $source_file"
            echo "   Link: [$link_text]($link_path)"
            echo "   Should be: [$link_text](/$link_path)"
            echo ""
            return 1
        fi
        
        # Try common locations
        for prefix in "docs/" "docs/features/" ""; do
            if [ -f "$PROJECT_ROOT/${prefix}$link_path" ]; then
                ((BROKEN_LINKS++))
                echo -e "${BLUE}🔧 Incorrect path in:${NC} $source_file"
                echo "   Link: [$link_text]($link_path)"
                echo "   Should be: [$link_text](/${prefix}$link_path)"
                echo ""
                return 1
            fi
        done
        
        # Can't find the file anywhere
        ((MISSING_TARGETS++))
        ((BROKEN_LINKS++))
        echo -e "${RED}❌ Target not found for:${NC} $source_file"
        echo "   Link: [$link_text]($link_path)"
        echo "   Searched in multiple locations - file doesn't exist"
        echo ""
        return 1
    fi
}

# Extract and check all markdown links
echo "Checking all markdown links..."
echo "=============================="
echo ""

while IFS= read -r file; do
    # Extract all markdown links from the file
    while IFS= read -r line; do
        if [[ $line =~ \[([^\]]+)\]\(([^\)]+)\) ]]; then
            link_text="${BASH_REMATCH[1]}"
            link_path="${BASH_REMATCH[2]}"
            check_link "$file" "$link_path" "$link_text"
        fi
    done < "$file"
done < <(find . -name "*.md" -type f | grep -v -E "(\.git|node_modules|target|dist|build|backup|archive|LEGACY)")

echo ""
echo "========================================"
echo "FINAL REPORT:"
echo "========================================"
echo "Total links checked: $TOTAL_LINKS"
echo -e "${RED}Broken links: $BROKEN_LINKS${NC}"
echo "  - Absolute but wrong: $ABSOLUTE_BUT_WRONG"
echo "  - Missing targets: $MISSING_TARGETS"
echo "  - Other issues: $((BROKEN_LINKS - ABSOLUTE_BUT_WRONG - MISSING_TARGETS))"

if [ $BROKEN_LINKS -eq 0 ]; then
    echo -e "\n${GREEN}✅ SUCCESS: All links are navigable!${NC}"
    echo "A new Claude can follow any link without issues."
    exit 0
else
    echo -e "\n${RED}❌ FAILED: Found $BROKEN_LINKS navigation problems!${NC}"
    echo ""
    echo "IMPACT:"
    echo "- New Claude sessions waste 30-60 minutes debugging"
    echo "- Documentation becomes unreliable"
    echo "- Productivity drops significantly"
    echo ""
    echo "NEXT STEPS:"
    echo "1. Fix paths to use absolute paths (starting with /)"
    echo "2. Ensure all targets exist"
    echo "3. Run this test again to verify"
    exit 1
fi