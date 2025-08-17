#!/bin/bash
# Test-Version mit nur 3 Tests
set -euo pipefail

echo "=== 🔄 TEST RUN - First 3 Low-Risk Tests ==="
echo ""

COUNTER=0
MAX_TESTS=3

for file in $(rg -l '@TestTransaction' src/test/java --type java); do
    if grep -q "CustomerBuilder" "$file"; then
        echo "⏭️  Already migrated: $(basename "$file" .java)"
        continue
    fi
    
    if ! rg -q 'new\s+Customer\(' "$file"; then
        continue
    fi
    
    if [ $COUNTER -ge $MAX_TESTS ]; then
        echo "Stopping after $MAX_TESTS tests (test run)"
        break
    fi
    
    TEST_NAME=$(basename "$file" .java)
    echo "Would migrate: $TEST_NAME"
    COUNTER=$((COUNTER + 1))
done
