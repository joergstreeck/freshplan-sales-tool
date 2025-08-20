#!/bin/bash
# Core Tests Tagging Script
# Automatisches Hinzufügen von @Tag("core") zu stabilen Tests
# Phase 3A - TestDataBuilder Migration

echo "=== Core Tests Tagging Started ==="
echo "Working directory: $(pwd)"

# Array der 38 Core-Tests
CORE_TESTS=(
    "ContactRepositoryTest"
    "CustomerRepositoryTest"  
    "CustomerResourceIntegrationTest"
    "CustomerServiceIntegrationTest"
    "CustomerCQRSIntegrationTest"
    "OpportunityResourceIntegrationTest"
    "OpportunityServiceIntegrationTest"
    "UserRepoSaveLoadIT"
    "CustomerCommandServiceIntegrationTest"
    "ContactCommandServiceTest"
    "ContactInteractionCommandServiceTest"
    "CustomerCommandServiceTest"
    "ContactInteractionQueryServiceTest"
    "ContactQueryServiceTest"
    "CustomerQueryServiceIntegrationTest"
    "TimelineCommandServiceTest"
    "TimelineQueryServiceTest"
    "HtmlExportQueryServiceTest"
    "HelpContentCQRSIntegrationTest"
    "OpportunityEntityStageTest"
    "OpportunityStageTest"
    "OpportunityDatabaseIntegrationTest"
    "OpportunityCQRSIntegrationTest"
    "OpportunityServiceMockTest"
    "OpportunityServiceStageTransitionTest"
    "OpportunityCommandServiceTest"
    "OpportunityMapperTest"
    "OpportunityQueryServiceTest"
    "SearchServiceTest"
    "SearchQueryServiceTest"
    "UserTest"
    "UserRepositoryTest"
    "UserServiceCQRSIntegrationTest"
    "UserServiceRolesTest"
    "UserServiceTest"
    "UserCommandServiceTest"
    "UserMapperTest"
    "UserQueryServiceTest"
)

TAGGED_COUNT=0
ALREADY_TAGGED=0
NOT_FOUND=0
FAILED=0

for test in "${CORE_TESTS[@]}"; do
    echo "Processing $test..."
    
    # Find test file
    file=$(find src/test -name "${test}.java" 2>/dev/null | head -1)
    
    if [ -f "$file" ]; then
        # Check if @Tag already exists
        if grep -q "@Tag(" "$file"; then
            echo "  ⚠️  $test already has @Tag - skipping"
            ALREADY_TAGGED=$((ALREADY_TAGGED + 1))
            continue
        fi
        
        # Add import if not exists
        if ! grep -q "import org.junit.jupiter.api.Tag;" "$file"; then
            # Find a good spot for import (after other junit imports)
            if grep -q "import org.junit.jupiter.api.Test;" "$file"; then
                # Add after Test import
                sed -i '' '/import org.junit.jupiter.api.Test;/a\
import org.junit.jupiter.api.Tag;' "$file"
                echo "  ✅ Added Tag import to $test"
            elif grep -q "import org.junit.jupiter.api" "$file"; then
                # Add after any junit import
                sed -i '' '/import org.junit.jupiter.api/a\
import org.junit.jupiter.api.Tag;' "$file"
                echo "  ✅ Added Tag import to $test"
            else
                # Fallback: add after package statement
                sed -i '' '/^package /a\
\
import org.junit.jupiter.api.Tag;' "$file"
                echo "  ✅ Added Tag import to $test (after package)"
            fi
        fi
        
        # Add @Tag("core") before class declaration
        # Check if @QuarkusTest exists
        if grep -q "@QuarkusTest" "$file"; then
            # Add after @QuarkusTest
            sed -i '' '/@QuarkusTest/a\
@Tag("core")' "$file"
            echo "  ✅ Tagged $test as core (after @QuarkusTest)"
        elif grep -q "^class ${test}" "$file"; then
            # Add before class declaration
            sed -i '' "/^class ${test}/i\\
@Tag(\"core\")" "$file"
            echo "  ✅ Tagged $test as core (before class)"
        elif grep -q "public class ${test}" "$file"; then
            # Add before public class declaration
            sed -i '' "/public class ${test}/i\\
@Tag(\"core\")" "$file"
            echo "  ✅ Tagged $test as core (before public class)"
        else
            echo "  ❌ Could not find class declaration for $test"
            FAILED=$((FAILED + 1))
            continue
        fi
        
        TAGGED_COUNT=$((TAGGED_COUNT + 1))
    else
        echo "  ❌ $test not found"
        NOT_FOUND=$((NOT_FOUND + 1))
    fi
done

echo ""
echo "=== Core Tests Tagging Summary ==="
echo "Successfully tagged: $TAGGED_COUNT"
echo "Already tagged: $ALREADY_TAGGED"  
echo "Not found: $NOT_FOUND"
echo "Failed: $FAILED"
echo "Expected total: ${#CORE_TESTS[@]}"

if [ $TAGGED_COUNT -gt 0 ]; then
    echo ""
    echo "✅ Core tagging completed! Run validation:"
    echo "   grep -r '@Tag(\"core\")' src/test/ | wc -l"
fi

if [ $FAILED -gt 0 ]; then
    echo ""
    echo "⚠️  Some tests could not be tagged automatically. Manual intervention needed."
fi