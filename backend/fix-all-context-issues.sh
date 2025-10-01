#!/bin/bash

# Sprint 2.1.4: Comprehensive fix for ALL ContextNotActiveException in tests
# This script adds @TestTransaction to ALL @QuarkusTest classes that don't have it yet

echo "🔍 Finding ALL @QuarkusTest classes..."

# Find all test files with @QuarkusTest
TEST_FILES=$(grep -r "@QuarkusTest" src/test/java --include="*.java" -l)

FIXED_COUNT=0
SKIPPED_COUNT=0

for TEST_FILE in $TEST_FILES; do
    echo ""
    echo "📄 Checking: $TEST_FILE"

    # Check if it already has @TestTransaction or @ActivateRequestContext at class level
    if grep -q "^@TestTransaction\|^@ActivateRequestContext" "$TEST_FILE"; then
        echo "   ✅ Already has context annotation"
        SKIPPED_COUNT=$((SKIPPED_COUNT + 1))
    else
        # Check if it has nested classes (might need special handling)
        if grep -q "@Nested" "$TEST_FILE"; then
            echo "   ⚠️  Has nested classes - adding @TestTransaction to main class"
        fi

        echo "   🔧 Adding @TestTransaction..."

        # Add import if not present
        if ! grep -q "import io.quarkus.test.TestTransaction;" "$TEST_FILE"; then
            sed -i '' '/^import io.quarkus.test.junit.QuarkusTest;/a\
import io.quarkus.test.TestTransaction;' "$TEST_FILE"
        fi

        # Add @TestTransaction before class declaration
        # Handle both cases: with and without other annotations
        sed -i '' '/@QuarkusTest/{
            n
            s/^\(class \|public class \|abstract class \)/@TestTransaction  \/\/ Sprint 2.1.4 Fix: Add transaction context\
\1/
        }' "$TEST_FILE"

        echo "   ✅ Fixed!"
        FIXED_COUNT=$((FIXED_COUNT + 1))
    fi
done

echo ""
echo "==============================================="
echo "✅ SUMMARY:"
echo "   Fixed: $FIXED_COUNT test classes"
echo "   Skipped: $SKIPPED_COUNT test classes (already had annotation)"
echo "==============================================="
echo ""
echo "📝 Next steps:"
echo "   1. Run 'mvn test' locally to validate"
echo "   2. Commit and push changes"
echo "   3. Monitor CI results"