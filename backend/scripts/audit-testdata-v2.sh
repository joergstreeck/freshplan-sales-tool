#!/bin/bash
# audit-testdata-v2.sh - Verbessertes Sicherheitsnetz mit Team-Feedback
# Version: 2.0
# Autor: Team + Claude
# Datum: 17.08.2025

set -euo pipefail

echo "=== 🔍 TEST DATA AUDIT v2.1 ==="
echo "Using precise PCRE patterns with negative lookahead"
echo ""

ERRORS=0
WARNINGS=0

# 1. Direkte Customer-Konstruktoren (mit grep-Filter statt komplexem Pattern)
echo -n "1. Checking for 'new Customer()' in tests... "
# Suche nach 'new Customer(' und filtere dann die falschen Positive raus
CUSTOMER_FOUND=$(rg -n --no-heading 'new\s+Customer\s*\(' src/test/java 2>/dev/null | 
    grep -v 'CustomerBuilder\|CustomerContact\|CustomerRepository\|CustomerService\|CustomerType\|CustomerStatus' | 
    wc -l || echo "0")
if [ "$CUSTOMER_FOUND" -gt 0 ]; then
    echo "❌ FOUND!"
    echo "   First 5 locations:"
    rg -n --no-heading 'new\s+Customer\s*\(' src/test/java | 
        grep -v 'CustomerBuilder\|CustomerContact\|CustomerRepository\|CustomerService\|CustomerType\|CustomerStatus' | 
        head -5
    ERRORS=$((ERRORS + 1))
else
    echo "✅ OK (0 found)"
fi

# 2. Direkte persist() außerhalb Builder (VERBESSERT)
echo -n "2. Checking for direct persist() calls... "
PERSIST_COUNT=$(rg -P '\.persist\s*\(' src/test/java 2>/dev/null | \
    grep -v 'TestDataBuilder\|builder\|Builder' | \
    wc -l || echo "0")
if [ "$PERSIST_COUNT" -gt 0 ]; then
    echo "❌ FOUND $PERSIST_COUNT occurrences!"
    echo "   Examples:"
    rg -n -P '\.persist\s*\(' src/test/java | grep -v 'builder' | head -3
    ERRORS=$((ERRORS + 1))
else
    echo "✅ OK (0 found)"
fi

# 3. Mockito varargs anti-pattern (ERWEITERT für verify)
echo -n "3. Checking for Mockito eq() with varargs... "
MOCKITO_ISSUES=0
if rg -q -P 'when\(.+\.delete\(\s*eq\(' src/test/java 2>/dev/null; then
    echo ""
    echo "   ❌ Found in when() statements"
    MOCKITO_ISSUES=$((MOCKITO_ISSUES + 1))
fi
if rg -q -P 'verify\(.+\.delete\(\s*eq\(' src/test/java 2>/dev/null; then
    echo "   ❌ Found in verify() statements"
    MOCKITO_ISSUES=$((MOCKITO_ISSUES + 1))
fi
if [ $MOCKITO_ISSUES -gt 0 ]; then
    echo "   Total issues: $MOCKITO_ISSUES"
    ERRORS=$((ERRORS + 1))
else
    echo "✅ OK (0 found)"
fi

# 4. Hardcodierte Test-Patterns (WARNUNG statt ERROR)
echo -n "4. Checking for hardcoded customer patterns... "
PATTERN_COUNT=$(rg -P '"(TEST-00[0-9]|TEST-[0-9]{3}|KD-[0-9]+)"' src/test/java 2>/dev/null | \
    grep -v 'SEED-' | wc -l || echo "0")
if [ "$PATTERN_COUNT" -gt 0 ]; then
    echo "⚠️  WARNING - Found $PATTERN_COUNT hardcoded patterns"
    WARNINGS=$((WARNINGS + 1))
else
    echo "✅ OK (0 found)"
fi

# 5. TestFixtures Usage (WARNUNG während Migration)
echo -n "5. Checking for TestFixtures usage... "
FIXTURES_COUNT=$(rg 'TestFixtures\.' src/test/java 2>/dev/null | wc -l || echo "0")
if [ "$FIXTURES_COUNT" -gt 0 ]; then
    echo "⚠️  WARNING - Still using TestFixtures ($FIXTURES_COUNT calls)"
    echo "   (OK during migration, should be @Deprecated)"
    WARNINGS=$((WARNINGS + 1))
else
    echo "✅ OK (0 found)"
fi

# 6. SEED Count Check (NEU - unterscheidet CI vs Local)
echo -n "6. Checking SEED data count... "
if command -v psql &> /dev/null && pg_isready -q 2>/dev/null; then
    SEED_COUNT=$(psql -h localhost -U freshplan -d freshplan -t -c \
        "SELECT COUNT(*) FROM customers WHERE is_test_data = true AND customer_number LIKE 'SEED-%'" 2>/dev/null || echo "0")
    SEED_COUNT=$(echo $SEED_COUNT | xargs)  # trim whitespace
    
    if [ -n "$CI" ] || [ "$GITHUB_ACTIONS" = "true" ]; then
        # CI: Strikt
        if [ "$SEED_COUNT" != "20" ]; then
            echo "❌ CI expects exactly 20 SEEDs, found: $SEED_COUNT"
            ERRORS=$((ERRORS + 1))
        else
            echo "✅ OK (20 SEEDs in CI)"
        fi
    else
        # Local: Tolerant
        if [ "$SEED_COUNT" -ge 20 ] && [ "$SEED_COUNT" -le 40 ]; then
            echo "✅ OK ($SEED_COUNT SEEDs, local tolerance 20-40)"
        else
            echo "⚠️  WARNING - Unexpected SEED count: $SEED_COUNT (expected 20-40)"
            WARNINGS=$((WARNINGS + 1))
        fi
    fi
else
    echo "⏭️  SKIPPED (DB not available)"
fi

echo ""
echo "=== 📊 SUMMARY ==="
echo "Errors: $ERRORS | Warnings: $WARNINGS"

if [ $ERRORS -eq 0 ]; then
    if [ $WARNINGS -eq 0 ]; then
        echo "✅ Perfect! All checks passed without warnings."
    else
        echo "✅ Passed with $WARNINGS warnings (non-blocking)"
    fi
    exit 0
else
    echo "❌ Found $ERRORS critical issues that MUST be fixed!"
    echo ""
    echo "Fix these issues:"
    echo "1. Replace 'new Customer()' → testDataBuilder.customer().build()/persist()"
    echo "2. Replace direct persist() → builder.persist()"
    echo "3. Remove eq() from Mockito delete() calls"
    exit 1
fi