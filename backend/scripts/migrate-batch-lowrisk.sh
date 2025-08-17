#!/bin/bash
# migrate-batch-lowrisk.sh - Batch-Migration für Low-Risk Tests
# Version: 1.0
# Autor: Team + Claude
# Datum: 17.08.2025

set -euo pipefail

echo "=== 🔄 BATCH MIGRATION - Low-Risk Tests ==="
echo "Migrating tests WITH @TestTransaction..."
echo ""

MIGRATED=0
FAILED=0
SKIPPED=0

# Find all tests with @TestTransaction that use new Customer()
for file in $(rg -l '@TestTransaction' src/test/java --type java); do
    if ! rg -q -P 'new\s+Customer\b' "$file"; then
        SKIPPED=$((SKIPPED + 1))
        continue
    fi
    
    TEST_NAME=$(basename "$file" .java)
    echo "📝 Migrating: $TEST_NAME"
    
    # Backup
    cp "$file" "$file.backup"
    
    # Migrate: new Customer() → testDataBuilder.customer().persist()
    sed -i.tmp -E 's/new\s+Customer\(\)/testDataBuilder.customer().persist()/g' "$file"
    
    # Add import if missing
    if ! grep -q "import.*TestDataBuilder" "$file"; then
        # Add after package declaration
        sed -i.tmp '/^package/a\
import de.freshplan.test.TestDataBuilder;\
import jakarta.inject.Inject;' "$file"
        
        # Add injection if not present
        if ! grep -q "@Inject.*TestDataBuilder" "$file"; then
            # Add before first @Test
            sed -i.tmp '/@Test/{
                i\
    @Inject TestDataBuilder testDataBuilder;\
                :a
                n
                ba
            }' "$file"
        fi
    fi
    
    # Clean up temp files
    rm -f "$file.tmp"
    
    # Test immediately
    echo "   Testing $TEST_NAME..."
    if ./mvnw test -Dtest="$TEST_NAME" -q; then
        echo "   ✅ Success!"
        MIGRATED=$((MIGRATED + 1))
        rm "$file.backup"
    else
        echo "   ❌ Test failed - reverting"
        mv "$file.backup" "$file"
        FAILED=$((FAILED + 1))
    fi
    echo ""
done

echo "=== 📊 BATCH MIGRATION RESULTS ==="
echo "✅ Migrated: $MIGRATED"
echo "❌ Failed: $FAILED"
echo "⏭️  Skipped: $SKIPPED (no new Customer())"

if [ $FAILED -gt 0 ]; then
    echo ""
    echo "⚠️  Some migrations failed. Review manually."
    exit 1
else
    echo ""
    echo "🎉 Batch migration successful!"
    exit 0
fi