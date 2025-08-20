#!/bin/bash
# Migration Tests Tagging Script
# Markiert alle Tests die NICHT core sind als "migrate"
# Phase 3B - TestDataBuilder Migration

echo "=== Migration Tests Tagging Started ==="
echo "Working directory: $(pwd)"

MIGRATION_TAGGED=0
ALREADY_TAGGED=0
SKIPPED_CORE=0
FAILED=0

# Find all test files
find src/test -name "*Test.java" -o -name "*IT.java" | while read file; do
    testname=$(basename "$file" .java)
    
    # Skip if already tagged as core
    if grep -q '@Tag("core")' "$file"; then
        echo "  ⏭️  Skipping core test: $testname"
        SKIPPED_CORE=$((SKIPPED_CORE + 1))
        continue
    fi
    
    # Skip if already has any @Tag
    if grep -q '@Tag(' "$file"; then
        echo "  ⚠️  Already tagged: $testname"
        ALREADY_TAGGED=$((ALREADY_TAGGED + 1))
        continue
    fi
    
    echo "  🔄 Tagging $testname as migrate..."
    
    # Add import if not exists
    if ! grep -q "import org.junit.jupiter.api.Tag;" "$file"; then
        # Find good spot for import
        if grep -q "import org.junit.jupiter.api.Test;" "$file"; then
            sed -i '' '/import org.junit.jupiter.api.Test;/a\
import org.junit.jupiter.api.Tag;' "$file"
        elif grep -q "import org.junit.jupiter.api" "$file"; then
            # Add after any junit import
            sed -i '' '/import org.junit.jupiter.api/a\
import org.junit.jupiter.api.Tag;' "$file"
        else
            # Fallback: add after package statement
            sed -i '' '/^package /a\
\
import org.junit.jupiter.api.Tag;' "$file"
        fi
    fi
    
    # Add @Tag("migrate") before class declaration
    if grep -q "@QuarkusTest" "$file"; then
        sed -i '' '/@QuarkusTest/a\
@Tag("migrate")' "$file"
        echo "  ✅ Tagged $testname as migrate (after @QuarkusTest)"
    elif grep -q "^class ${testname}" "$file"; then
        # Add before class declaration
        sed -i '' "/^class ${testname}/i\\
@Tag(\"migrate\")" "$file"
        echo "  ✅ Tagged $testname as migrate (before class)"
    elif grep -q "public class ${testname}" "$file"; then
        # Add before public class declaration
        sed -i '' "/public class ${testname}/i\\
@Tag(\"migrate\")" "$file"
        echo "  ✅ Tagged $testname as migrate (before public class)"
    else
        echo "  ❌ Could not find class declaration for $testname"
        FAILED=$((FAILED + 1))
        continue
    fi
    
    MIGRATION_TAGGED=$((MIGRATION_TAGGED + 1))
done

echo ""
echo "=== Migration Tests Tagging Summary ==="
echo "Tagged as migrate: $MIGRATION_TAGGED"
echo "Already tagged: $ALREADY_TAGGED"  
echo "Skipped (core): $SKIPPED_CORE"
echo "Failed: $FAILED"

echo ""
echo "✅ Migration tagging completed! Verify with:"
echo "   grep -r '@Tag(\"migrate\")' src/test/ | wc -l"