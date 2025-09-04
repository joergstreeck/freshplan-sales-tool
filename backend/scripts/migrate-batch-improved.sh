#!/bin/bash
# Improved migration script based on manual migration learnings
set -euo pipefail

echo "=== 🔄 IMPROVED BATCH MIGRATION - Low-Risk Tests ==="
echo "Based on learnings from manual migrations"
echo ""

MIGRATED=0
FAILED=0
SKIPPED=0
ALREADY_MIGRATED=0

# Function to check if file has helper methods
has_helper_methods() {
    local file=$1
    grep -q "createTest.*Customer\|createCustomer" "$file" 2>/dev/null
}

# Function to migrate helper methods
migrate_helper_methods() {
    local file=$1
    local temp_file="${file}.migrate"
    
    # Use awk to rewrite helper methods
    awk '
    BEGIN { in_helper = 0; }
    
    # Detect start of helper method
    /private Customer (createTest.*Customer|createCustomer.*)\(/ {
        in_helper = 1
        print $0
        next
    }
    
    # In helper method, replace new Customer()
    in_helper && /new Customer\(\)/ {
        print "    // Use CustomerBuilder for creating test customers"
        print "    // Note: We use build() here, not persist(), because the tests"
        print "    // handle persistence themselves with repository.persist()"
        print "    Customer customer = customerBuilder"
        print "        .withCompanyName(companyName)"
        print "        .withStatus(CustomerStatus.LEAD)"
        print "        .withPartnerStatus(PartnerStatus.KEIN_PARTNER)"
        print "        .withPaymentTerms(PaymentTerms.NETTO_30)"
        print "        .withFinancingType(FinancingType.PRIVATE)"
        print "        .build();  // build() not persist() - tests handle persistence"
        print "    "
        print "    // Override the auto-generated values for test compatibility"
        next
    }
    
    # Skip old setter lines in helper method
    in_helper && /customer\.set(CompanyName|CustomerNumber|IsDeleted|CreatedBy|UpdatedBy|CreatedAt|UpdatedAt|Status|PartnerStatus|PaymentTerms|PrimaryFinancing)\(/ {
        # Skip these lines - handled by builder or overridden
        next
    }
    
    # Keep customer number override
    in_helper && /customer\.setCustomerNumber\(.*KD-TEST/ {
        print $0
        print "    // Override company name to remove the [TEST-xxx] prefix that builder adds"
        print "    customer.setCompanyName(companyName);"
        next
    }
    
    # End of helper method
    in_helper && /^  \}$/ {
        in_helper = 0
    }
    
    # Default: print line as-is
    { print }
    ' "$file" > "$temp_file"
    
    mv "$temp_file" "$file"
}

# Process each test file
for file in $(rg -l '@TestTransaction' src/test/java --type java); do
    # Skip if already migrated
    if grep -q "CustomerBuilder customerBuilder" "$file"; then
        ALREADY_MIGRATED=$((ALREADY_MIGRATED + 1))
        echo "⏭️  Already migrated: $(basename "$file" .java)"
        continue
    fi
    
    # Skip if no Customer instantiation
    if ! rg -q 'new\s+Customer\(' "$file"; then
        SKIPPED=$((SKIPPED + 1))
        continue
    fi
    
    TEST_NAME=$(basename "$file" .java)
    echo "📝 Migrating: $TEST_NAME"
    
    # Backup
    cp "$file" "$file.backup"
    
    # Add import if missing
    if ! grep -q "import.*CustomerBuilder" "$file"; then
        sed -i.tmp '/^import.*Test;/a\
import de.freshplan.test.builders.CustomerBuilder;' "$file"
    fi
    
    # Add injection if not present
    if ! grep -q "@Inject.*CustomerBuilder" "$file"; then
        # Find where to add the injection
        if grep -q "@Inject.*EntityManager" "$file"; then
            # Add after EntityManager
            sed -i.tmp '/@Inject.*EntityManager/a\
  \
  @Inject CustomerBuilder customerBuilder;' "$file"
        elif grep -q "@Inject.*Repository" "$file"; then
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
    
    # Check if file has helper methods
    if has_helper_methods "$file"; then
        echo "   Detected helper methods - using intelligent migration"
        migrate_helper_methods "$file"
    else
        echo "   Simple migration - direct replacement"
        # Simple case: direct new Customer() replacement
        sed -i.tmp -E 's/Customer\s+(\w+)\s*=\s*new\s+Customer\(\)/Customer \1 = customerBuilder.persist()/g' "$file"
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
        echo "   Reason: Test execution failed"
    fi
    echo ""
done

echo "=== 📊 BATCH MIGRATION RESULTS ==="
echo "✅ Migrated: $MIGRATED"
echo "❌ Failed: $FAILED"
echo "⏭️  Skipped: $SKIPPED (no new Customer())"
echo "📌 Already migrated: $ALREADY_MIGRATED"
echo ""

if [ $FAILED -gt 0 ]; then
    echo "⚠️  Some migrations failed. Review the failed tests manually."
fi