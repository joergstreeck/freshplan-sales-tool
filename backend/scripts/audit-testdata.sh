#!/bin/bash
# audit-testdata.sh - Sicherheitsnetz für TestData-Migration
# Version: 1.0
# Autor: Team + Claude
# Datum: 17.08.2025

set -euo pipefail

echo "=== 🔍 TEST DATA AUDIT ==="
echo "Checking for test data anti-patterns..."
echo ""

ERRORS=0

# 1. Direkte Customer-Konstruktoren (präziser Pattern)
echo -n "1. Checking for 'new Customer()' in tests... "
if rg -q 'new\s+Customer\s*\(\s*\)' src/test/java 2>/dev/null; then
    echo "❌ FOUND!"
    echo "   Locations:"
    rg -n --no-heading 'new\s+Customer\s*\(\s*\)' src/test/java | head -5
    ERRORS=$((ERRORS + 1))
else
    echo "✅ OK (0 found)"
fi

# 2. Direkte persist() außerhalb Builder
echo -n "2. Checking for direct persist() calls... "
PERSIST_COUNT=$(rg '\.persist\s*\(' src/test/java 2>/dev/null | \
    grep -v 'TestDataBuilder\|CustomerBuilder\|ContactBuilder\|OpportunityBuilder' | \
    wc -l || echo "0")
if [ "$PERSIST_COUNT" -gt 0 ]; then
    echo "❌ FOUND $PERSIST_COUNT occurrences!"
    ERRORS=$((ERRORS + 1))
else
    echo "✅ OK (0 found)"
fi

# 3. Harte Kundennummern
echo -n "3. Checking for hardcoded customer numbers... "
if rg -q '"(CUST-|KD-|TEST-00|TEST-[0-9]{3})"' src/test/java 2>/dev/null; then
    echo "⚠️  WARNING - Found hardcoded patterns"
    echo "   (May be OK if using SEED-xxx)"
else
    echo "✅ OK (0 found)"
fi

# 4. Legacy TestFixtures
echo -n "4. Checking for TestFixtures usage... "
FIXTURES_COUNT=$(rg 'TestFixtures\.' src/test/java 2>/dev/null | wc -l || echo "0")
if [ "$FIXTURES_COUNT" -gt 0 ]; then
    echo "⚠️  WARNING - Still using TestFixtures ($FIXTURES_COUNT calls)"
    echo "   (Should migrate to TestDataBuilder)"
else
    echo "✅ OK (0 found)"
fi

# 5. Mockito varargs anti-pattern
echo -n "5. Checking for Mockito eq() with varargs... "
if rg -q 'when\(.+\.delete\s*\(\s*eq\(' src/test/java 2>/dev/null; then
    echo "❌ FOUND dangerous pattern!"
    ERRORS=$((ERRORS + 1))
else
    echo "✅ OK (0 found)"
fi

echo ""
echo "=== 📊 SUMMARY ==="
if [ $ERRORS -eq 0 ]; then
    echo "✅ All checks passed! Test data migration complete."
    exit 0
else
    echo "❌ Found $ERRORS critical issues that need fixing!"
    echo ""
    echo "Next steps:"
    echo "1. Fix all 'new Customer()' → use TestDataBuilder"
    echo "2. Replace direct persist() → use builder.persist()"
    echo "3. Check hardcoded values → use dynamic generation"
    exit 1
fi