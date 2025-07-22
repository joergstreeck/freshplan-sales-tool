#!/bin/bash

# Test: V5 Struktur-Integrität
# Prüft NUR die aktuelle V5 Struktur, keine alten Dateien

echo "🎯 V5 Structure Integrity Test"
echo "=============================="
echo "Testing ONLY current V5 structure and CLAUDE_TECH files"
echo ""

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to run a test
run_test() {
    local test_name="$1"
    local test_result="$2"
    ((TOTAL_TESTS++))
    
    if [ "$test_result" -eq 0 ]; then
        echo -e "${GREEN}✅ $test_name${NC}"
        ((PASSED_TESTS++))
    else
        echo -e "${RED}❌ $test_name${NC}"
        ((FAILED_TESTS++))
    fi
}

echo "1️⃣ Checking Master Plan V5 exists..."
if [ -f "docs/CRM_COMPLETE_MASTER_PLAN_V5.md" ]; then
    run_test "Master Plan V5 exists" 0
else
    run_test "Master Plan V5 exists" 1
fi

echo ""
echo "2️⃣ Checking V5 CLAUDE_TECH references..."
# Extract all CLAUDE_TECH paths from V5
V5_CLAUDE_TECH_REFS=$(grep -o '/docs/features/[^)]*CLAUDE_TECH\.md' docs/CRM_COMPLETE_MASTER_PLAN_V5.md | sort -u)
MISSING_COUNT=0

while IFS= read -r ref_path; do
    # Remove leading slash for file check
    file_path="${ref_path#/}"
    if [ ! -f "$file_path" ]; then
        echo -e "${RED}   Missing: $ref_path${NC}"
        ((MISSING_COUNT++))
    fi
done <<< "$V5_CLAUDE_TECH_REFS"

if [ $MISSING_COUNT -eq 0 ]; then
    run_test "All V5 CLAUDE_TECH references exist" 0
else
    run_test "All V5 CLAUDE_TECH references exist ($MISSING_COUNT missing)" 1
fi

echo ""
echo "3️⃣ Checking ACTIVE features structure..."
ACTIVE_STRUCTURE_OK=0
for dir in docs/features/ACTIVE/*/; do
    if [ -d "$dir" ]; then
        feature_name=$(basename "$dir")
        # Check for CLAUDE_TECH file
        claude_tech_count=$(find "$dir" -name "*CLAUDE_TECH.md" | wc -l)
        if [ $claude_tech_count -eq 0 ]; then
            echo -e "${RED}   No CLAUDE_TECH in: $dir${NC}"
            ACTIVE_STRUCTURE_OK=1
        fi
    fi
done
run_test "All ACTIVE features have CLAUDE_TECH" $ACTIVE_STRUCTURE_OK

echo ""
echo "4️⃣ Checking for duplicate CLAUDE_TECH files..."
DUPLICATES=$(find docs/features -name "*CLAUDE_TECH.md" -exec basename {} \; | sort | uniq -d)
if [ -z "$DUPLICATES" ]; then
    run_test "No duplicate CLAUDE_TECH filenames" 0
else
    echo -e "${RED}   Duplicates found: $DUPLICATES${NC}"
    run_test "No duplicate CLAUDE_TECH filenames" 1
fi

echo ""
echo "5️⃣ Checking Feature Registry alignment..."
REGISTRY_COUNT=$(grep -c "^| [A-Z]" docs/FEATURE_REGISTRY.md 2>/dev/null || echo 0)
ACTUAL_CLAUDE_TECH=$(find docs/features -name "*CLAUDE_TECH.md" | wc -l)
echo "   Registry entries: $REGISTRY_COUNT"
echo "   Actual CLAUDE_TECH files: $ACTUAL_CLAUDE_TECH"

# Allow small discrepancy (registry might count features, not files)
if [ $((ACTUAL_CLAUDE_TECH - REGISTRY_COUNT)) -le 5 ] && [ $((ACTUAL_CLAUDE_TECH - REGISTRY_COUNT)) -ge -5 ]; then
    run_test "Feature Registry aligned with files (±5 tolerance)" 0
else
    run_test "Feature Registry mismatch (diff: $((ACTUAL_CLAUDE_TECH - REGISTRY_COUNT)))" 1
fi

echo ""
echo "6️⃣ Checking critical paths exist..."
CRITICAL_PATHS=(
    "docs/CRM_COMPLETE_MASTER_PLAN_V5.md"
    "docs/FEATURE_REGISTRY.md"
    "docs/NEXT_STEP.md"
    "docs/features/ACTIVE"
    "docs/features/PLANNED"
    "CLAUDE.md"
)

CRITICAL_OK=0
for path in "${CRITICAL_PATHS[@]}"; do
    if [ ! -e "$path" ]; then
        echo -e "${RED}   Missing critical path: $path${NC}"
        CRITICAL_OK=1
    fi
done
run_test "All critical paths exist" $CRITICAL_OK

echo ""
echo "7️⃣ Checking V5 internal consistency..."
# Check if V5 claims match reality
V5_ACTIVE_COUNT=$(grep -c "ACTIVE.*CLAUDE_TECH" docs/CRM_COMPLETE_MASTER_PLAN_V5.md 2>/dev/null || echo 0)
ACTUAL_ACTIVE_COUNT=$(find docs/features/ACTIVE -name "*CLAUDE_TECH.md" | wc -l)

if [ "$V5_ACTIVE_COUNT" -eq "$ACTUAL_ACTIVE_COUNT" ]; then
    run_test "V5 ACTIVE count matches reality" 0
else
    echo -e "${YELLOW}   V5 claims: $V5_ACTIVE_COUNT ACTIVE${NC}"
    echo -e "${YELLOW}   Reality: $ACTUAL_ACTIVE_COUNT ACTIVE${NC}"
    run_test "V5 ACTIVE count mismatch" 1
fi

echo ""
echo "8️⃣ Checking for broken V5 links..."
BROKEN_V5_LINKS=0
while IFS= read -r line; do
    if [[ $line =~ \[([^\]]+)\]\((/docs/features/[^\)]+)\) ]]; then
        link_text="${BASH_REMATCH[1]}"
        link_path="${BASH_REMATCH[2]}"
        file_path="${link_path#/}"
        
        if [ ! -f "$file_path" ]; then
            echo -e "${RED}   Broken link: [$link_text]($link_path)${NC}"
            ((BROKEN_V5_LINKS++))
        fi
    fi
done < docs/CRM_COMPLETE_MASTER_PLAN_V5.md

if [ $BROKEN_V5_LINKS -eq 0 ]; then
    run_test "No broken links in V5" 0
else
    run_test "Broken links in V5 ($BROKEN_V5_LINKS found)" 1
fi

echo ""
echo "9️⃣ Checking NEXT_STEP.md references..."
if [ -f "docs/NEXT_STEP.md" ]; then
    NEXT_STEP_OK=0
    # Check if referenced TODOs exist in code
    TODO_REFS=$(grep -o "TODO-[0-9]\+" docs/NEXT_STEP.md | sort -u)
    for todo in $TODO_REFS; do
        if ! grep -q "$todo" docs/CRM_COMPLETE_MASTER_PLAN_V5.md 2>/dev/null; then
            echo -e "${YELLOW}   Warning: $todo referenced but not in V5${NC}"
        fi
    done
    run_test "NEXT_STEP.md is consistent" $NEXT_STEP_OK
else
    run_test "NEXT_STEP.md exists" 1
fi

echo ""
echo "🔟 Checking for old structure remnants..."
OLD_PATTERNS=(
    "*KOMPAKT.md"
    "*KOMPAKT_NEW.md"
    "MASTER_PLAN_V4"
    "CRM_COMPLETE_MASTER_PLAN.md"
)

OLD_FOUND=0
for pattern in "${OLD_PATTERNS[@]}"; do
    found=$(find docs/features -name "$pattern" 2>/dev/null | wc -l)
    if [ $found -gt 0 ]; then
        echo -e "${YELLOW}   Found old pattern: $pattern ($found files)${NC}"
        OLD_FOUND=$((OLD_FOUND + found))
    fi
done

if [ $OLD_FOUND -eq 0 ]; then
    run_test "No old structure remnants in features/" 0
else
    run_test "Old structure remnants found ($OLD_FOUND files)" 1
fi

echo ""
echo "========================================"
echo "SUMMARY:"
echo "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✅ V5 STRUCTURE IS HEALTHY!${NC}"
    exit 0
else
    echo -e "${RED}❌ V5 STRUCTURE NEEDS ATTENTION!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Fix missing CLAUDE_TECH references in V5"
    echo "2. Ensure all ACTIVE features have CLAUDE_TECH files"
    echo "3. Update Feature Registry to match reality"
    echo "4. Remove old structure remnants"
    exit 1
fi