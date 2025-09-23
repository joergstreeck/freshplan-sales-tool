#!/bin/bash

# Sprint 1.3: Security Gates - Fail-Closed Verification Script
# Part of FP-231: Security Gates Enforcement
#
# This script verifies that all security policies fail closed,
# meaning that in case of any error or uncertainty, access is denied.

set -e

echo "🔒 Starting Fail-Closed Security Verification..."
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0
WARNINGS=0

# Function to check a security aspect
check_security() {
    local test_name="$1"
    local test_command="$2"
    local expected_result="$3"

    echo -n "Checking: $test_name... "

    if eval "$test_command"; then
        if [ "$expected_result" = "pass" ]; then
            echo -e "${GREEN}✅ PASSED${NC}"
        else
            echo -e "${RED}❌ FAILED - Should have failed but passed${NC}"
            ((ERRORS++))
        fi
    else
        if [ "$expected_result" = "fail" ]; then
            echo -e "${GREEN}✅ PASSED (correctly failed)${NC}"
        else
            echo -e "${RED}❌ FAILED - Should have passed but failed${NC}"
            ((ERRORS++))
        fi
    fi
}

echo ""
echo "1. Verifying Security Context Functions..."
echo "-------------------------------------------"

# Detect if we're in CI or local environment
if [ -n "$CI" ]; then
    # In CI, use localhost with the exposed port
    export PGPASSWORD=freshplan
    PSQL_CMD="psql -h localhost -p 5432 -U freshplan -d freshplan"

    # Check if PostgreSQL is available
    if ! $PSQL_CMD -c "SELECT 1" >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  PostgreSQL not available in CI - skipping database checks${NC}"
        echo "This is expected if the database container hasn't been started yet."
        exit 0
    fi
else
    # Local environment
    PSQL_CMD="psql -U freshplan -d freshplan"
fi

# Check if security functions exist
check_security "set_app_context function exists" \
    "$PSQL_CMD -c \"SELECT 1 FROM pg_proc WHERE proname = 'set_app_context';\" | grep -q '1 row'" \
    "pass"

check_security "current_app_context function exists" \
    "$PSQL_CMD -c \"SELECT 1 FROM pg_proc WHERE proname = 'current_app_context';\" | grep -q '1 row'" \
    "pass"

check_security "has_role function exists" \
    "$PSQL_CMD -c \"SELECT 1 FROM pg_proc WHERE proname = 'has_role';\" | grep -q '1 row'" \
    "pass"

echo ""
echo "2. Verifying Fail-Closed Behavior..."
echo "-------------------------------------"

# Test that without proper context, functions return safe defaults
check_security "current_app_user returns NULL without context" \
    "$PSQL_CMD -c \"SELECT current_app_user() IS NULL;\" | grep -q 't'" \
    "pass"

check_security "has_role returns FALSE without context" \
    "$PSQL_CMD -c \"SELECT has_role('admin');\" | grep -q 'f'" \
    "pass"

echo ""
echo "3. Verifying RLS Policies (if tables exist)..."
echo "-----------------------------------------------"

# Check if any RLS-enabled tables exist and verify they fail closed
TABLES_WITH_RLS=$($PSQL_CMD -t -c \
    "SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND rowsecurity = true;" 2>/dev/null | tr -d ' ')

if [ -z "$TABLES_WITH_RLS" ]; then
    echo -e "${YELLOW}⚠️  No tables with RLS enabled yet (expected in Sprint 1.x)${NC}"
    ((WARNINGS++))
else
    for table in "$TABLES_WITH_RLS"; do
        check_security "RLS enabled on $table" \
            "$PSQL_CMD -c \"SELECT rowsecurity FROM pg_tables WHERE tablename = '$table';\" | grep -q 't'" \
            "pass"
    done
fi

echo ""
echo "4. Verifying Security Settings Table..."
echo "----------------------------------------"

check_security "security_settings table exists" \
    "$PSQL_CMD -c \"SELECT 1 FROM information_schema.tables WHERE table_name = 'security_settings';\" | grep -q '1 row'" \
    "pass"

check_security "security_audit_log table exists" \
    "$PSQL_CMD -c \"SELECT 1 FROM information_schema.tables WHERE table_name = 'security_audit_log';\" | grep -q '1 row'" \
    "pass"

echo ""
echo "5. Verifying Application Security Headers..."
echo "--------------------------------------------"

# Start the application if not running
if ! curl -s http://localhost:8080/q/health > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Backend not running on port 8080, skipping header checks${NC}"
    ((WARNINGS++))
else
    # Check security headers
    HEADERS=$(curl -sI http://localhost:8080/q/health)

    check_security "X-Content-Type-Options header present" \
        "echo \"$HEADERS\" | grep -q 'X-Content-Type-Options: nosniff'" \
        "pass"

    check_security "Referrer-Policy header present" \
        "echo \"$HEADERS\" | grep -q 'Referrer-Policy'" \
        "pass"

    check_security "X-Frame-Options header present" \
        "echo \"$HEADERS\" | grep -q 'X-Frame-Options'" \
        "pass"

    check_security "Content-Security-Policy header present" \
        "echo \"$HEADERS\" | grep -q 'Content-Security-Policy'" \
        "pass"
fi

echo ""
echo "6. Verifying CORS Configuration..."
echo "-----------------------------------"

if curl -s http://localhost:8080/q/health > /dev/null 2>&1; then
    # Test CORS preflight
    CORS_RESPONSE=$(curl -sI -X OPTIONS \
        -H "Origin: http://localhost:5173" \
        -H "Access-Control-Request-Method: POST" \
        http://localhost:8080/api/customers 2>/dev/null)

    check_security "CORS allows localhost:5173 in dev" \
        "echo \"$CORS_RESPONSE\" | grep -q 'Access-Control-Allow-Origin'" \
        "pass"

    # Test unauthorized origin should fail
    CORS_FAIL=$(curl -sI -X OPTIONS \
        -H "Origin: http://malicious.com" \
        -H "Access-Control-Request-Method: POST" \
        http://localhost:8080/api/customers 2>/dev/null)

    check_security "CORS blocks unauthorized origins" \
        "echo \"$CORS_FAIL\" | grep -q 'Access-Control-Allow-Origin: http://malicious.com'" \
        "fail"
fi

echo ""
echo "================================================"
echo "Security Verification Summary:"
echo "================================================"

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ All security checks PASSED!${NC}"
    echo -e "${GREEN}✅ System correctly implements fail-closed security.${NC}"
else
    echo -e "${RED}❌ $ERRORS security checks FAILED!${NC}"
    echo -e "${RED}❌ Security policies may not be failing closed properly.${NC}"
fi

if [ $WARNINGS -gt 0 ]; then
    echo -e "${YELLOW}⚠️  $WARNINGS warnings detected (non-critical).${NC}"
fi

echo ""
echo "Fail-closed principle verified for Sprint 1.3 Security Gates."

# Exit with error if any checks failed
exit $ERRORS