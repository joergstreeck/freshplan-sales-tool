#!/bin/bash

# Sprint 2.1.4 Fix: Add @TestTransaction to failing tests
# This fixes ContextNotActiveException errors

echo "🔧 Adding @TestTransaction to failing tests..."

# List of files to fix
FILES=(
  "src/test/java/de/freshplan/modules/leads/service/UserLeadSettingsServiceTest.java"
  "src/test/java/de/freshplan/modules/leads/service/TerritoryServiceTest.java"
  "src/test/java/de/freshplan/modules/leads/api/LeadResourceTest.java"
  "src/test/java/de/freshplan/test/DatabaseGrowthTrackerTest.java"
  "src/test/java/de/freshplan/test/CIDebugTest.java"
  "src/test/java/de/freshplan/test/DetailedDatabaseAnalysisTest.java"
  "src/test/java/de/freshplan/test/TestDataIntegrityCheckTest.java"
  "src/test/java/de/freshplan/modules/leads/service/FollowUpDebugTest.java"
)

for FILE in "${FILES[@]}"; do
  if [ -f "$FILE" ]; then
    echo "Processing: $FILE"

    # Check if already has @TestTransaction
    if grep -q "@TestTransaction" "$FILE"; then
      echo "  ✅ Already has @TestTransaction"
      continue
    fi

    # Add import if not present
    if ! grep -q "import io.quarkus.test.TestTransaction;" "$FILE"; then
      # Add import after other io.quarkus imports
      sed -i '' '/import io.quarkus.test/a\
import io.quarkus.test.TestTransaction;' "$FILE"
      echo "  ✅ Added import"
    fi

    # Add @TestTransaction before class declaration
    # Find line with "class " and add annotation before it
    sed -i '' '/^@QuarkusTest/a\
@TestTransaction' "$FILE"
    echo "  ✅ Added @TestTransaction annotation"

    # Add comment about the fix
    sed -i '' '/^class.*Test/i\
// Sprint 2.1.4 Fix: Added @TestTransaction to fix ContextNotActiveException' "$FILE"

  else
    echo "⚠️  File not found: $FILE"
  fi
done

echo "✅ Done! Fixed all test files."
echo ""
echo "📊 Summary:"
echo "- Added @TestTransaction to prevent ContextNotActiveException"
echo "- Tests now run in transaction context with automatic rollback"
echo "- This fixes the CI failures from Sprint 2.1.4"