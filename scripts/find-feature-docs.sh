#!/bin/bash
# find-feature-docs.sh - Findet Feature-Dokumentation in beliebiger Struktur
# Unterstützt alte (ACTIVE/PLANNED) und neue (FC-XXX-*) Strukturen

set -e

# Source configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/config/paths.conf"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Function to find feature documentation
find_feature_doc() {
    local feature_code="$1"
    local found_docs=()
    
    # Strategy 1: Direct FC-XXX-* folder
    local fc_pattern="${FEATURES_DIR}/${feature_code}-*"
    for dir in $fc_pattern; do
        if [[ -d "$dir" && -f "$dir/README.md" ]]; then
            found_docs+=("$dir/README.md")
        fi
    done
    
    # Strategy 2: ACTIVE/PLANNED structure
    for base in ACTIVE PLANNED ARCHIVE; do
        if [[ -d "${FEATURES_DIR}/${base}" ]]; then
            # Look for feature code in folder names
            while IFS= read -r -d '' file; do
                if grep -q "$feature_code" "$file" 2>/dev/null; then
                    found_docs+=("$file")
                fi
            done < <(find "${FEATURES_DIR}/${base}" -name "README.md" -print0 2>/dev/null)
        fi
    done
    
    # Strategy 3: Any markdown file with feature code
    while IFS= read -r -d '' file; do
        if grep -q "^# .*${feature_code}" "$file" 2>/dev/null; then
            found_docs+=("$file")
        fi
    done < <(find "${FEATURES_DIR}" -name "*.md" -print0 2>/dev/null)
    
    # Remove duplicates and return results
    printf '%s\n' "${found_docs[@]}" | sort -u
}

# Main execution
if [[ $# -eq 0 ]]; then
    # No argument - try to read from .current-focus
    if [[ -f "${PROJECT_ROOT}/.current-focus" ]]; then
        FEATURE=$(grep '"feature"' "${PROJECT_ROOT}/.current-focus" | cut -d'"' -f4 2>/dev/null || echo "")
    fi
else
    FEATURE="$1"
fi

if [[ -z "$FEATURE" ]]; then
    echo -e "${RED}❌ No feature code provided and none found in .current-focus${NC}"
    echo "Usage: $0 [FC-XXX]"
    exit 1
fi

echo -e "${YELLOW}🔍 Searching for ${FEATURE} documentation...${NC}"
echo "=================================="

DOCS=($(find_feature_doc "$FEATURE"))

if [[ ${#DOCS[@]} -eq 0 ]]; then
    echo -e "${RED}❌ No documentation found for ${FEATURE}${NC}"
    exit 1
else
    echo -e "${GREEN}✅ Found ${#DOCS[@]} document(s):${NC}"
    for doc in "${DOCS[@]}"; do
        echo "   📄 $doc"
    done
fi

# Export for other scripts
export FEATURE_DOCS="${DOCS[0]}"  # Primary doc
export ALL_FEATURE_DOCS="${DOCS[@]}"  # All docs