#!/bin/bash

# Portable script for fixing test isolation issues
# Works on both macOS and Linux

echo "🔧 Fixing test isolation issues..."
echo "=================================="

# Detect OS for sed compatibility
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS requires backup extension
    SED_CMD="sed -i.bak"
else
    # Linux doesn't need backup
    SED_CMD="sed -i"
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
            $SED_CMD '/import io.quarkus.test/a\
import io.quarkus.test.TestTransaction;' "$test"
        fi
        
        # Remove import jakarta.transaction.Transactional if present
        $SED_CMD '/import jakarta.transaction.Transactional;/d' "$test"
        
        # Replace @Transactional with @TestTransaction
        $SED_CMD 's/@Transactional/@TestTransaction/g' "$test"
        
        # Clean up backup files on macOS
        if [[ "$OSTYPE" == "darwin"* ]]; then
            rm -f "${test}.bak"
        fi
        
        echo "  ✅ Fixed: $(basename $test)"
    fi
done

echo ""
echo "✅ Test isolation fixes complete!"
echo ""
echo "Next steps:"
echo "1. Run tests to verify: ./mvnw test"
echo "2. Commit changes if tests pass"