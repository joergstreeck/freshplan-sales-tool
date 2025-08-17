#!/bin/bash
# migrate-simple-only.sh - Nur die einfachsten Fälle migrieren
set -euo pipefail

echo "=== 🔄 SIMPLE MIGRATION - Only easiest cases ==="
echo ""

MIGRATED=0
FAILED=0

# Liste von Tests die wir manuell als "einfach" identifiziert haben
SIMPLE_TESTS=(
    "CustomerContactTest"
    "CustomerTimelineServiceTest"
    "ContactQueryServiceTest"
    "TimelineCommandServiceTest"
)

for test_name in "${SIMPLE_TESTS[@]}"; do
    file=$(find src/test/java -name "${test_name}.java" -type f | head -1)
    
    if [ -z "$file" ]; then
        echo "⏭️  Test not found: $test_name"
        continue
    fi
    
    if grep -q "CustomerBuilder customerBuilder" "$file"; then
        echo "✅ Already migrated: $test_name"
        continue
    fi
    
    echo "📝 Attempting: $test_name"
    
    # Backup
    cp "$file" "$file.backup"
    
    # Add import
    if ! grep -q "import.*CustomerBuilder" "$file"; then
        sed -i.tmp '/^package/a\
\
import de.freshplan.test.builders.CustomerBuilder;' "$file"
    fi
    
    # Add injection after other @Inject
    if ! grep -q "@Inject.*CustomerBuilder" "$file"; then
        # Find a good spot
        if grep -q "@Inject.*Repository" "$file"; then
            sed -i.tmp '/@Inject.*Repository/{
                a\
  @Inject CustomerBuilder customerBuilder;
            }' "$file"
        elif grep -q "@Inject.*EntityManager" "$file"; then
            sed -i.tmp '/@Inject.*EntityManager/{
                a\
  @Inject CustomerBuilder customerBuilder;
            }' "$file"
        fi
    fi
    
    # Simple replacement - just the most basic case
    sed -i.tmp 's/= new Customer()/= customerBuilder.build()/g' "$file"
    
    # Clean up
    rm -f "$file.tmp"
    
    # Test
    echo "   Testing..."
    if timeout 30 ./mvnw test -Dtest="$test_name" -q 2>/dev/null; then
        echo "   ✅ SUCCESS: $test_name"
        MIGRATED=$((MIGRATED + 1))
        rm "$file.backup"
    else
        echo "   ❌ FAILED: $test_name"
        mv "$file.backup" "$file"
        FAILED=$((FAILED + 1))
    fi
    echo ""
done

echo "=== 📊 RESULTS ==="
echo "✅ Migrated: $MIGRATED"
echo "❌ Failed: $FAILED"