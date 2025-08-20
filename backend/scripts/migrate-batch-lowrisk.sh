#!/bin/bash
# migrate-batch-lowrisk.sh - Batch-Migration für Low-Risk Tests
# Version: 2.0 - Angepasst für CustomerBuilder
# Autor: Team + Claude
# Datum: 17.08.2025

set -euo pipefail

echo "=== 🔄 BATCH MIGRATION - Low-Risk Tests ==="
echo "Migrating tests WITH @TestTransaction to CustomerBuilder..."
echo ""

MIGRATED=0
FAILED=0
SKIPPED=0
ALREADY_MIGRATED=0

# Find all tests with @TestTransaction that use new Customer()
for file in $(rg -l '@TestTransaction' src/test/java --type java); do
    # Skip if already migrated to CustomerBuilder
    if grep -q "CustomerBuilder" "$file"; then
        ALREADY_MIGRATED=$((ALREADY_MIGRATED + 1))
        echo "⏭️  Already migrated: $(basename "$file" .java)"
        continue
    fi
    
    if ! rg -q 'new\s+Customer\(' "$file"; then
        SKIPPED=$((SKIPPED + 1))
        continue
    fi
    
    TEST_NAME=$(basename "$file" .java)
    echo "📝 Migrating: $TEST_NAME"
    
    # Backup
    cp "$file" "$file.backup"
    
    # Migrate: new Customer() → customerBuilder...persist()
    # First replace simple new Customer() calls
    sed -i.tmp -E 's/new\s+Customer\(\)/customerBuilder.persist()/g' "$file"
    
    # Add import if missing
    if ! grep -q "import.*CustomerBuilder" "$file"; then
        # Add CustomerBuilder import after other imports
        sed -i.tmp '/^import.*Test;/a\
import de.freshplan.test.builders.CustomerBuilder;' "$file"
    fi
    
    # Add injection if not present
    if ! grep -q "@Inject.*CustomerBuilder" "$file"; then
        # Find where to add the injection (after other @Inject or before first @Test)
        if grep -q "@Inject.*Repository" "$file"; then
            # Add after last @Inject
            sed -i.tmp '/@Inject.*Repository/{
                a\
\
  @Inject CustomerBuilder customerBuilder;
            }' "$file"
        else
            # Add before first @Test
            sed -i.tmp '/@Test/{
                i\
  @Inject CustomerBuilder customerBuilder;\
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
    if timeout 60 ./mvnw test -Dtest="$TEST_NAME" -q 2>/dev/null; then
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
echo "📌 Already migrated: $ALREADY_MIGRATED"

if [ $FAILED -gt 0 ]; then
    echo ""
    echo "⚠️  Some migrations failed. Review manually."
    echo "You can check the .backup files for failed tests."
    exit 1
else
    echo ""
    echo "🎉 Batch migration successful!"
    echo "Total progress: $((MIGRATED + ALREADY_MIGRATED)) tests using CustomerBuilder"
    exit 0
fi