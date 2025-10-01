#!/bin/bash

# Sprint 2.1.4: Fix ContextNotActiveException in tests
# This script adds @TestTransaction or @ActivateRequestContext to tests that need it

echo "🔍 Finding tests that need context fixes..."

# List of test classes from CI error log that need fixing
TEST_CLASSES=(
    "CustomerRepositoryTest"
    "ContactsCountConsistencyTest"
    "TimelineCommandServiceTest"
    "TimelineQueryServiceTest"
    "OpportunityServiceStageTransitionTest"
    "UserServiceRolesTest"
    "SalesCockpitQueryServiceTest"
)

for TEST_CLASS in "${TEST_CLASSES[@]}"; do
    echo "  Searching for $TEST_CLASS..."

    # Find the test file
    TEST_FILE=$(find src/test/java -name "${TEST_CLASS}.java" 2>/dev/null | head -1)

    if [ -n "$TEST_FILE" ]; then
        echo "  ✅ Found: $TEST_FILE"

        # Check if it already has @TestTransaction or @ActivateRequestContext
        if grep -q "@TestTransaction\|@ActivateRequestContext" "$TEST_FILE"; then
            echo "     Already has context annotation, skipping"
        else
            echo "     Adding @TestTransaction..."

            # Add import if not present
            if ! grep -q "import io.quarkus.test.TestTransaction;" "$TEST_FILE"; then
                sed -i '' '/^import io.quarkus.test.junit.QuarkusTest;/a\
import io.quarkus.test.TestTransaction;' "$TEST_FILE"
            fi

            # Add @TestTransaction before class declaration
            sed -i '' 's/^@QuarkusTest$/&\
@TestTransaction  \/\/ Sprint 2.1.4 Fix: Add transaction context/' "$TEST_FILE"

            echo "     ✅ Fixed!"
        fi
    else
        echo "  ⚠️  Not found: $TEST_CLASS"
    fi
done

echo ""
echo "✅ Context fixes applied!"
echo ""
echo "📊 Summary:"
echo "  - Added @TestTransaction to test classes that need transaction context"
echo "  - SecurityContextProviderTest already fixed with @ActivateRequestContext"
echo "  - TerritoryServiceTest already fixed with @TestTransaction"