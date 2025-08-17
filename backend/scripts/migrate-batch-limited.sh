#!/bin/bash
# Limited version - only migrates first 3 tests
set -euo pipefail

echo "=== 🔄 LIMITED BATCH MIGRATION - First 3 Low-Risk Tests ==="
echo ""

MIGRATED=0
FAILED=0
SKIPPED=0
ALREADY_MIGRATED=0
MAX_TESTS=3

for file in $(rg -l '@TestTransaction' src/test/java --type java); do
    # Stop after MAX_TESTS
    if [ $MIGRATED -ge $MAX_TESTS ]; then
        echo "Stopping after $MAX_TESTS successful migrations (limited run)"
        break
    fi
    
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
            awk '/@Inject.*Repository/ {print; print "\n  @Inject CustomerBuilder customerBuilder;"; next} {print}' "$file" > "$file.new"
            mv "$file.new" "$file"
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

echo "=== 📊 LIMITED BATCH MIGRATION RESULTS ==="
echo "✅ Migrated: $MIGRATED"
echo "❌ Failed: $FAILED"
echo "⏭️  Skipped: $SKIPPED (no new Customer())"
echo "📌 Already migrated: $ALREADY_MIGRATED"
echo ""
echo "This was a limited run (max $MAX_TESTS tests)"
