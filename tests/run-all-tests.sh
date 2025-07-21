#!/bin/bash

# CLAUDE_TECH Test Suite - Main Runner
# Führt alle Dokumentations-Tests aus

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Project root
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$PROJECT_ROOT"

# Test results
PASSED=0
FAILED=0
RESULTS_DIR="tests/results"
mkdir -p "$RESULTS_DIR"

echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║        CLAUDE_TECH Documentation Test Suite              ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "Start: $(date)"
echo ""

# Function to run a test
run_test() {
    local test_name="$1"
    local test_script="$2"
    local description="$3"
    
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}Running: $test_name${NC}"
    echo "Description: $description"
    echo ""
    
    if [ -f "$test_script" ]; then
        if bash "$test_script" > "$RESULTS_DIR/${test_name}.log" 2>&1; then
            ((PASSED++))
            echo -e "${GREEN}✅ PASSED${NC}"
            echo ""
            # Show summary from test
            tail -5 "$RESULTS_DIR/${test_name}.log" | grep -E "(✅|Coverage|valide)" || true
        else
            ((FAILED++))
            echo -e "${RED}❌ FAILED${NC}"
            echo ""
            # Show error
            tail -10 "$RESULTS_DIR/${test_name}.log" | grep -E "(❌|Error|failed)" || true
        fi
    else
        ((FAILED++))
        echo -e "${RED}❌ FAILED - Test script not found: $test_script${NC}"
    fi
    
    echo ""
}

# Run all tests
echo -e "${BLUE}═══ Running Documentation Tests ═══${NC}"
echo ""

# Test 1: Link Integrity
run_test "link-integrity" \
         "tests/link-integrity-test.sh" \
         "Prüft alle internen Links in CLAUDE_TECH Dokumenten"

# Test 2: Coverage
run_test "coverage" \
         "tests/test-coverage.sh" \
         "Prüft ob alle Features CLAUDE_TECH Versionen haben"

# Test 3: Structure Integrity
run_test "structure-integrity" \
         "tests/test-structure-integrity.sh" \
         "Prüft auf Duplikate und strukturelle Probleme"

# Test 4: Format Validation (wenn vorhanden)
if [ -f "tests/format-validation-test.sh" ]; then
    run_test "format-validation" \
             "tests/format-validation-test.sh" \
             "Validiert das CLAUDE_TECH Format"
else
    echo -e "${YELLOW}⚠️  Format Validation Test noch nicht implementiert${NC}"
    echo ""
fi

# Generate summary report
SUMMARY_FILE="$RESULTS_DIR/test-suite-summary.md"
{
    echo "# CLAUDE_TECH Test Suite Summary"
    echo ""
    echo "Executed: $(date)"
    echo ""
    echo "## Test Results"
    echo ""
    echo "| Test | Status | Details |"
    echo "|------|--------|---------|"
    
    # Check each test result
    if [ -f "$RESULTS_DIR/link-integrity.log" ]; then
        if grep -q "✅.*valide" "$RESULTS_DIR/link-integrity.log"; then
            echo "| Link Integrity | ✅ PASSED | All links valid |"
        else
            echo "| Link Integrity | ❌ FAILED | Broken links found |"
        fi
    fi
    
    if [ -f "$RESULTS_DIR/coverage.log" ]; then
        if grep -q "✅.*Coverage" "$RESULTS_DIR/coverage.log"; then
            coverage=$(grep -oE "[0-9]+%" "$RESULTS_DIR/coverage.log" | tail -1)
            echo "| Coverage | ✅ PASSED | $coverage coverage |"
        else
            echo "| Coverage | ❌ FAILED | Insufficient coverage |"
        fi
    fi
    
    echo ""
    echo "## Summary"
    echo "- Tests Passed: $PASSED"
    echo "- Tests Failed: $FAILED"
    echo "- Total Tests: $((PASSED + FAILED))"
    echo ""
    
    if [ $FAILED -eq 0 ]; then
        echo "✅ **All tests passed!**"
    else
        echo "❌ **$FAILED tests failed!**"
    fi
    
    echo ""
    echo "## Test Logs"
    echo ""
    for log in "$RESULTS_DIR"/*.log; do
        if [ -f "$log" ]; then
            echo "- \`$(basename "$log")\`"
        fi
    done
} > "$SUMMARY_FILE"

# Display final results
echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    TEST SUITE SUMMARY                     ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "Tests Passed: ${GREEN}$PASSED${NC}"
echo -e "Tests Failed: ${RED}$FAILED${NC}"
echo -e "Total Tests:  $((PASSED + FAILED))"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ All documentation tests passed!${NC}"
    echo ""
    echo "The CLAUDE_TECH documentation is:"
    echo "- Complete (100%+ coverage)"
    echo "- Valid (all links working)"
    echo "- Ready for implementation"
else
    echo -e "${RED}❌ Some tests failed!${NC}"
    echo ""
    echo "Please check the test logs in: $RESULTS_DIR/"
fi

echo ""
echo "Summary report: $SUMMARY_FILE"
echo "Individual logs: $RESULTS_DIR/*.log"

# Exit with appropriate code
exit $FAILED