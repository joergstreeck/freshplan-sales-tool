#!/bin/bash

# Master Test Runner - Führt alle "Truth Tests" aus
# Diese Tests sagen die WAHRHEIT über unseren Code

echo "🔍 FRESHPLAN TRUTH TEST SUITE"
echo "============================="
echo "Running tests that tell the TRUTH about our codebase..."
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Track results
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Function to run a test
run_test() {
    local test_name="$1"
    local test_script="$2"
    
    ((TOTAL_TESTS++))
    
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Running: $test_name"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    if [ -f "$test_script" ]; then
        if bash "$test_script"; then
            ((PASSED_TESTS++))
            echo -e "\n${GREEN}✅ PASSED: $test_name${NC}\n"
        else
            ((FAILED_TESTS++))
            echo -e "\n${RED}❌ FAILED: $test_name${NC}\n"
        fi
    else
        echo -e "${YELLOW}⚠️  SKIPPED: $test_script not found${NC}\n"
        ((FAILED_TESTS++))
    fi
}

# Run all truth tests
echo "Starting test execution..."
echo ""

# 1. Absolute Path Test
run_test "Absolute Path Validator" "$SCRIPT_DIR/test-absolute-paths.sh"

# 2. Link Navigability Test  
run_test "Link Navigability Test" "$SCRIPT_DIR/test-link-navigability.sh"

# 3. Documentation Consistency Test
run_test "Documentation Consistency Test" "$SCRIPT_DIR/test-documentation-consistency.sh"

# 4. Existing tests (if they work)
if [ -f "$SCRIPT_DIR/test-structure-integrity.sh" ]; then
    run_test "Structure Integrity Test" "$SCRIPT_DIR/test-structure-integrity.sh"
fi

# Final Summary
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "FINAL TRUTH REPORT"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Total tests run: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✅ ALL TESTS PASSED!${NC}"
    echo "The codebase is in a reliable state for planning and development."
    echo ""
    echo "You can trust:"
    echo "- All paths are absolute and navigable"
    echo "- Documentation matches the code"
    echo "- Structure is consistent"
    echo ""
    echo "A new Claude can start working immediately without debugging!"
    exit 0
else
    echo -e "${RED}❌ TESTS FAILED!${NC}"
    echo ""
    echo "IMPACT ON PLANNING:"
    echo "- ⏱️  Each new Claude wastes 2-3 hours debugging"
    echo "- 🐛 Hidden problems cause unexpected failures"  
    echo "- 📉 Productivity drops by 50% or more"
    echo "- 😤 Frustration increases with each session"
    echo ""
    echo "RECOMMENDED ACTIONS:"
    echo "1. Fix all failing tests BEFORE continuing development"
    echo "2. Run './scripts/fix-relative-paths.sh' for path issues"
    echo "3. Update documentation to match code reality"
    echo "4. Add these tests to CI/CD pipeline"
    echo ""
    echo "Remember: Reliable tests = Reliable planning = Happy developers!"
    exit 1
fi