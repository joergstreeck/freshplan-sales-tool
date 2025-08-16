#!/bin/bash

# Fix Test Isolation Problems
# Devise: Sicherheit geht vor Schnelligkeit

echo "==================================="
echo "Test Isolation Fix Script"
echo "==================================="
echo ""

# Backup first!
echo "Creating backup..."
cp -r src/test src/test.backup.$(date +%Y%m%d_%H%M%S)

# List of critical test files to fix
CRITICAL_TESTS=(
    "src/test/java/de/freshplan/domain/customer/service/query/CustomerQueryServiceIntegrationTest.java"
    "src/test/java/de/freshplan/domain/customer/performance/ContactPerformanceTest.java"
    "src/test/java/de/freshplan/domain/export/service/HtmlExportCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/opportunity/api/OpportunityResourceIntegrationTest.java"
    "src/test/java/de/freshplan/domain/opportunity/repository/OpportunityRepositoryTest.java"
    "src/test/java/de/freshplan/domain/help/api/HelpSystemResourceIntegrationTest.java"
    "src/test/java/de/freshplan/domain/customer/repository/CustomerTimelineRepositoryPerformanceTest.java"
    "src/test/java/de/freshplan/domain/customer/service/ContactServiceCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/customer/service/ContactEventCaptureCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/customer/service/timeline/TimelineCQRSIntegrationTest.java"
)

# All CQRS Integration Tests
CQRS_TESTS=(
    "src/test/java/de/freshplan/domain/audit/service/AuditCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/profile/service/ProfileCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/customer/service/CustomerCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/customer/service/ContactInteractionServiceCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/help/service/HelpContentCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/help/service/UserStruggleDetectionCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/cockpit/service/SalesCockpitCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/testdata/service/TestDataServiceCQRSIntegrationTest.java"
    "src/test/java/de/freshplan/domain/user/service/UserServiceCQRSIntegrationTest.java"
)

echo "Fixing critical tests with @BeforeEach..."
for test in "${CRITICAL_TESTS[@]}"; do
    if [ -f "$test" ]; then
        echo "Processing: $(basename $test)"
        
        # Add import if not present
        if ! grep -q "import io.quarkus.test.TestTransaction;" "$test"; then
            sed -i '' '/import io.quarkus.test/a\
import io.quarkus.test.TestTransaction;' "$test"
        fi
        
        # Remove import jakarta.transaction.Transactional if present
        sed -i '' '/import jakarta.transaction.Transactional;/d' "$test"
        
        # Replace @Transactional with @TestTransaction
        sed -i '' 's/@Transactional/@TestTransaction/g' "$test"
        
        echo "  ✅ Fixed"
    else
        echo "  ⚠️ File not found: $test"
    fi
done

echo ""
echo "Fixing CQRS Integration Tests..."
for test in "${CQRS_TESTS[@]}"; do
    if [ -f "$test" ]; then
        echo "Processing: $(basename $test)"
        
        # Add import if not present
        if ! grep -q "import io.quarkus.test.TestTransaction;" "$test"; then
            sed -i '' '/import io.quarkus.test/a\
import io.quarkus.test.TestTransaction;' "$test"
        fi
        
        # Remove import jakarta.transaction.Transactional if present  
        sed -i '' '/import jakarta.transaction.Transactional;/d' "$test"
        
        # Replace @Transactional with @TestTransaction
        sed -i '' 's/@Transactional/@TestTransaction/g' "$test"
        
        echo "  ✅ Fixed"
    else
        echo "  ⚠️ File not found: $test"
    fi
done

echo ""
echo "==================================="
echo "Fix Complete!"
echo "==================================="
echo ""
echo "Next steps:"
echo "1. Run: mvn test -Dtest=*CQRSIntegrationTest"
echo "2. Check database: should stay at 74 customers"
echo "3. If tests fail, restore from backup: src/test.backup.*"
echo ""
echo "Devise: Sicherheit geht vor Schnelligkeit ✅"