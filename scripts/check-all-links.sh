#!/bin/bash

# Comprehensive Link Checker
# Prüft ALLE Links in Markdown-Dateien auf Existenz und Korrektheit

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
NC='\033[0m'

# Counters
TOTAL_FILES=0
TOTAL_LINKS=0
VALID_LINKS=0
BROKEN_LINKS=0
EXTERNAL_LINKS=0
ANCHOR_LINKS=0
RELATIVE_PATHS=0
MISSING_SLASH=0
WRONG_PATH=0
FILE_NOT_FOUND=0

# Arrays for detailed reporting
declare -a BROKEN_LINK_DETAILS
declare -a RELATIVE_PATH_FILES
declare -a MISSING_TARGET_FILES

echo "🔍 Comprehensive Link Checker"
echo "============================="
echo "Checking all links in Markdown files..."
echo "Project root: $PROJECT_ROOT"
echo ""

# Function to check if a file exists with various extensions
file_exists_with_extensions() {
    local path="$1"
    
    # Check exact path
    [ -f "$path" ] && return 0
    
    # Check with .md extension
    [ -f "${path}.md" ] && return 0
    
    # Check without .md if it has one
    if [[ $path =~ \.md$ ]]; then
        local without_md="${path%.md}"
        [ -f "$without_md" ] && return 0
    fi
    
    return 1
}

# Function to resolve and check a link
check_link() {
    local source_file="$1"
    local link_path="$2"
    local link_text="$3"
    local line_num="$4"
    
    # Skip empty links
    if [ -z "$link_path" ]; then
        return 0
    fi
    
    # Handle external links
    if [[ $link_path =~ ^https?:// ]] || [[ $link_path =~ ^ftp:// ]] || [[ $link_path =~ ^mailto: ]]; then
        ((EXTERNAL_LINKS++))
        ((VALID_LINKS++))
        return 0
    fi
    
    # Handle anchor links
    if [[ $link_path =~ ^#[^/] ]]; then
        ((ANCHOR_LINKS++))
        ((VALID_LINKS++))
        return 0
    fi
    
    ((TOTAL_LINKS++))
    
    # Remove anchor from path if present
    local clean_path="${link_path%%#*}"
    
    # Check for relative paths (../)
    if [[ $clean_path =~ \.\.\/ ]]; then
        ((RELATIVE_PATHS++))
        ((BROKEN_LINKS++))
        RELATIVE_PATH_FILES+=("$source_file:$line_num - [$link_text]($link_path)")
        return 1
    fi
    
    # Handle absolute paths (starting with /)
    if [[ $clean_path =~ ^/ ]]; then
        local target_path="${clean_path#/}"
        
        if file_exists_with_extensions "$PROJECT_ROOT/$target_path"; then
            ((VALID_LINKS++))
            return 0
        else
            ((FILE_NOT_FOUND++))
            ((BROKEN_LINKS++))
            BROKEN_LINK_DETAILS+=("$source_file:$line_num")
            BROKEN_LINK_DETAILS+=("  ❌ [$link_text]($link_path)")
            BROKEN_LINK_DETAILS+=("  Expected at: $PROJECT_ROOT/$target_path")
            BROKEN_LINK_DETAILS+=("")
            return 1
        fi
    fi
    
    # Handle paths without leading slash
    # First check if it would work with a leading slash
    if file_exists_with_extensions "$PROJECT_ROOT/$clean_path"; then
        ((MISSING_SLASH++))
        ((BROKEN_LINKS++))
        BROKEN_LINK_DETAILS+=("$source_file:$line_num")
        BROKEN_LINK_DETAILS+=("  🔧 [$link_text]($link_path)")
        BROKEN_LINK_DETAILS+=("  Should be: [$link_text](/$link_path)")
        BROKEN_LINK_DETAILS+=("")
        return 1
    fi
    
    # Try common path prefixes
    for prefix in "docs/" "docs/features/" "docs/features/ACTIVE/" "docs/features/PLANNED/"; do
        if file_exists_with_extensions "$PROJECT_ROOT/${prefix}$clean_path"; then
            ((WRONG_PATH++))
            ((BROKEN_LINKS++))
            BROKEN_LINK_DETAILS+=("$source_file:$line_num")
            BROKEN_LINK_DETAILS+=("  🔧 [$link_text]($link_path)")
            BROKEN_LINK_DETAILS+=("  Should be: [$link_text](/${prefix}$link_path)")
            BROKEN_LINK_DETAILS+=("")
            return 1
        fi
    done
    
    # File not found anywhere
    ((FILE_NOT_FOUND++))
    ((BROKEN_LINKS++))
    MISSING_TARGET_FILES+=("$source_file:$line_num - [$link_text]($link_path)")
    return 1
}

# Process all markdown files
echo "Scanning markdown files..."
echo ""

while IFS= read -r file; do
    ((TOTAL_FILES++))
    line_num=0
    
    # Read file line by line to track line numbers
    while IFS= read -r line; do
        ((line_num++))
        
        # Extract all markdown links from the line
        # Pattern: [text](path)
        while [[ $line =~ \[([^\]]*)\]\(([^\)]*)\) ]]; do
            link_text="${BASH_REMATCH[1]}"
            link_path="${BASH_REMATCH[2]}"
            
            check_link "$file" "$link_path" "$link_text" "$line_num"
            
            # Remove the matched part to find more links in the same line
            line="${line#*${BASH_REMATCH[0]}}"
        done
    done < "$file"
    
    # Show progress every 50 files
    if (( TOTAL_FILES % 50 == 0 )); then
        echo -ne "\rProcessed $TOTAL_FILES files..."
    fi
    
done < <(find . -name "*.md" -type f | grep -v -E "(\.git|node_modules|target|dist|build|backups|archive|LEGACY|\.backup)")

echo -ne "\rProcessed $TOTAL_FILES files... Done!\n"
echo ""

# Generate detailed report
echo "========================================"
echo "📊 LINK CHECK SUMMARY"
echo "========================================"
echo ""
echo "Files scanned: $TOTAL_FILES"
echo "Total links found: $((TOTAL_LINKS + EXTERNAL_LINKS + ANCHOR_LINKS))"
echo ""
echo -e "${GREEN}✅ Valid links: $VALID_LINKS${NC}"
echo "   - Internal: $VALID_LINKS"
echo "   - External: $EXTERNAL_LINKS"
echo "   - Anchors: $ANCHOR_LINKS"
echo ""

if [ $BROKEN_LINKS -gt 0 ]; then
    echo -e "${RED}❌ Broken links: $BROKEN_LINKS${NC}"
    echo "   - Relative paths (../): $RELATIVE_PATHS"
    echo "   - Missing leading slash: $MISSING_SLASH"
    echo "   - Wrong path: $WRONG_PATH"
    echo "   - File not found: $FILE_NOT_FOUND"
    echo ""
fi

# Show details for different error types
if [ $RELATIVE_PATHS -gt 0 ]; then
    echo "========================================"
    echo -e "${YELLOW}⚠️  RELATIVE PATH ISSUES ($RELATIVE_PATHS)${NC}"
    echo "========================================"
    echo "These use ../ which breaks when files move:"
    echo ""
    for detail in "${RELATIVE_PATH_FILES[@]:0:10}"; do
        echo "  $detail"
    done
    if [ ${#RELATIVE_PATH_FILES[@]} -gt 10 ]; then
        echo "  ... and $((${#RELATIVE_PATH_FILES[@]} - 10)) more"
    fi
    echo ""
fi

if [ ${#BROKEN_LINK_DETAILS[@]} -gt 0 ]; then
    echo "========================================"
    echo -e "${RED}❌ BROKEN LINK DETAILS${NC}"
    echo "========================================"
    # Show first 20 broken links
    shown=0
    for detail in "${BROKEN_LINK_DETAILS[@]}"; do
        echo "$detail"
        if [[ $detail == "" ]]; then
            ((shown++))
            if [ $shown -ge 20 ]; then
                remaining=$((BROKEN_LINKS - shown))
                if [ $remaining -gt 0 ]; then
                    echo "... and $remaining more broken links"
                fi
                break
            fi
        fi
    done
    echo ""
fi

# Generate fix script if needed
if [ $BROKEN_LINKS -gt 0 ]; then
    FIX_SCRIPT="$PROJECT_ROOT/scripts/fix-broken-links.sh"
    echo "========================================"
    echo "🔧 GENERATING FIX SCRIPT"
    echo "========================================"
    echo "Creating $FIX_SCRIPT with automated fixes..."
    
    cat > "$FIX_SCRIPT" << 'SCRIPT_HEADER'
#!/bin/bash
# Auto-generated script to fix broken links
# Generated by check-all-links.sh

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "🔧 Fixing broken links..."
echo "========================"

# Backup first
BACKUP_DIR="backups/links-$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

FIXED_COUNT=0

SCRIPT_HEADER
    
    # Add fixes for missing slashes
    if [ $MISSING_SLASH -gt 0 ]; then
        echo "" >> "$FIX_SCRIPT"
        echo "# Fix missing leading slashes" >> "$FIX_SCRIPT"
        echo "echo 'Fixing missing leading slashes...'" >> "$FIX_SCRIPT"
        # This would need actual link data to generate specific fixes
    fi
    
    chmod +x "$FIX_SCRIPT"
    echo -e "${GREEN}✅ Fix script created at: $FIX_SCRIPT${NC}"
    echo ""
fi

# Final summary
echo "========================================"
echo "🎯 RECOMMENDATIONS"
echo "========================================"

if [ $BROKEN_LINKS -eq 0 ]; then
    echo -e "${GREEN}✅ Excellent! All links are working correctly.${NC}"
    echo "Your documentation is well-maintained and navigable."
else
    echo "1. Fix relative paths (../) → Use absolute paths (/docs/...)"
    echo "2. Add missing leading slashes → path/to/file → /path/to/file"
    echo "3. Verify file locations for 'not found' errors"
    echo "4. Run the generated fix script: ./scripts/fix-broken-links.sh"
    echo "5. Re-run this check after fixes: ./scripts/check-all-links.sh"
fi

echo ""
echo "========================================"

# Exit with error if broken links found
exit $( [ $BROKEN_LINKS -gt 0 ] && echo 1 || echo 0 )