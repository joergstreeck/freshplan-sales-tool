#!/bin/bash
# Quarantine Tests Script
# Markiert problematische Tests als "quarantine"
# Phase 3B - TestDataBuilder Migration

echo "=== Quarantining problematic tests ==="

# Liste der bekannt problematischen Tests
QUARANTINE_TESTS=(
    "BaseIntegrationTestWithCleanup"
    "TestDataCleanup" 
    "DatabaseCleanupTest"
    "EmergencyTestDataCleanupTest"
    "TestDataPollutionFix"
    "ContactsCountDebugTest"
    "TestDataPollutionAnalysisTest"
    "CI_DatabaseStateDebugTest"
    "DatabaseGrowthTrackerTest"
    "TestDataIntegrityCheckTest"
    "DatabaseDeepCleanupTest"
    "DirectDatabaseCleanupTest"
    "TestCustomerCleanupTest"
    "MarkRealCustomersAsTestDataTest"
    "DatabaseAnalysisTest"
    "DetailedDatabaseAnalysisTest"
    "TestIsolationAnalysisTest"
    "AAA_EarlyBirdDebugTest"
    "ZZZ_FinalVerificationTest"
    "CIDebugTest"
    "SimpleSeedTest"
    "SeedDataVerificationTest"
    "MID_SeedDataCheckTest"
    "TestCustomerVerificationTest"
)

QUARANTINED=0
ALREADY_QUARANTINED=0
NOT_FOUND=0
FAILED=0

for test in "${QUARANTINE_TESTS[@]}"; do
    file=$(find src/test -name "${test}.java" 2>/dev/null | head -1)
    
    if [ -f "$file" ]; then
        echo "Processing $test..."
        
        # Check if already has @Tag("quarantine")
        if grep -q '@Tag("quarantine")' "$file"; then
            echo "  ⚠️  $test already quarantined"
            ALREADY_QUARANTINED=$((ALREADY_QUARANTINED + 1))
            continue
        fi
        
        # Add import if not exists
        if ! grep -q "import org.junit.jupiter.api.Tag;" "$file"; then
            if grep -q "import org.junit.jupiter.api" "$file"; then
                sed -i '' '/import org.junit.jupiter.api/a\
import org.junit.jupiter.api.Tag;' "$file"
            else
                sed -i '' '/^package /a\
\
import org.junit.jupiter.api.Tag;' "$file"
            fi
        fi
        
        # Replace @Tag("migrate") with @Tag("quarantine") if exists
        if grep -q '@Tag("migrate")' "$file"; then
            sed -i '' 's/@Tag("migrate")/@Tag("quarantine")/' "$file"
            echo "  🚨 Quarantined $test (was migrate)"
            QUARANTINED=$((QUARANTINED + 1))
        elif grep -q '@Tag("core")' "$file"; then
            # Should not happen but check anyway
            echo "  ⚠️  WARNING: $test is marked as core but should be quarantine!"
            FAILED=$((FAILED + 1))
        elif ! grep -q '@Tag(' "$file"; then
            # Add quarantine tag if no tag exists
            if grep -q "@QuarkusTest" "$file"; then
                sed -i '' '/@QuarkusTest/a\
@Tag("quarantine")' "$file"
                echo "  🚨 Quarantined $test (was untagged)"
            elif grep -q "class ${test}" "$file"; then
                sed -i '' "/class ${test}/i\\
@Tag(\"quarantine\")" "$file"
                echo "  🚨 Quarantined $test (was untagged)"
            else
                echo "  ❌ Could not add quarantine tag to $test"
                FAILED=$((FAILED + 1))
                continue
            fi
            QUARANTINED=$((QUARANTINED + 1))
        else
            echo "  ⚠️  $test has unknown tag - manual check needed"
            FAILED=$((FAILED + 1))
        fi
    else
        echo "  ❓ $test not found"
        NOT_FOUND=$((NOT_FOUND + 1))
    fi
done

echo ""
echo "=== Quarantine Summary ==="
echo "Newly quarantined: $QUARANTINED"
echo "Already quarantined: $ALREADY_QUARANTINED"
echo "Not found: $NOT_FOUND"
echo "Failed: $FAILED"
echo "Total processed: ${#QUARANTINE_TESTS[@]}"

echo ""
echo "✅ Quarantine process completed! Verify with:"
echo "   grep -r '@Tag(\"quarantine\")' src/test/ | wc -l"