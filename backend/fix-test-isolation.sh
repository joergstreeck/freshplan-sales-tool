#!/bin/bash

# Portable script for fixing test isolation issues
# Works on both macOS and Linux

echo "🔧 Fixing test isolation issues..."
echo "=================================="

# Detect OS for sed compatibility
if [[ "$OSTYPE" == "darwin"* ]]; then
    SED_INPLACE="sed -i ''"
else
    SED_INPLACE="sed -i"
fi

# Find all test files that need fixing
TEST_FILES=$(find . -name "*Test.java" -o -name "*IT.java" | grep -E "(CustomerCommandServiceIntegrationTest|CustomerQueryServiceIntegrationTest|CustomerCQRSIntegrationTest|OpportunityCQRSIntegrationTest|SalesCockpitCQRSIntegrationTest|ContactServiceCQRSIntegrationTest|ProfileCQRSIntegrationTest|UserServiceCQRSIntegrationTest|SearchCQRSIntegrationTest|HelpSystemCompleteIntegrationTest)")

if [ -z "$TEST_FILES" ]; then
    echo "No test files found that need fixing."
    exit 0
fi

echo "Found $(echo "$TEST_FILES" | wc -l) test files to process"
echo ""

for test in $TEST_FILES; do
    if [ -f "$test" ]; then
        echo "Processing: $(basename $test)"
        
        # Add import if not present
        if ! grep -q "import io.quarkus.test.TestTransaction;" "$test"; then
            $SED_INPLACE '/import io.quarkus.test/a\
import io.quarkus.test.TestTransaction;' "$test"
        fi
        
        # Remove import jakarta.transaction.Transactional if present
        $SED_INPLACE '/import jakarta.transaction.Transactional;/d' "$test"
        
        # Replace @Transactional with @TestTransaction
        $SED_INPLACE 's/@Transactional/@TestTransaction/g' "$test"
        
        echo "  ✅ Fixed: $(basename $test)"
    fi
done

echo ""
echo "✅ Test isolation fixes complete!"
echo ""
echo "Next steps:"
echo "1. Run tests to verify: ./mvnw test"
echo "2. Commit changes if tests pass"