#!/bin/bash

# Local Database Growth Check
# Run this before committing to ensure tests don't pollute the database

echo "========================================="
echo "Database Growth Check"
echo "========================================="
echo ""

# Count customers before
echo "Counting customers before tests..."
BEFORE=$(mvn test -Dtest="DatabaseAnalysisTest#countCustomers" -q | grep "Total Customers:" | awk '{print $3}')
echo "Customers before: $BEFORE"

# Run a subset of tests (you can modify this)
echo ""
echo "Running CQRS Integration Tests..."
mvn test -Dtest="*CQRSIntegrationTest" -q > /dev/null 2>&1

# Count customers after
echo ""
echo "Counting customers after tests..."
AFTER=$(mvn test -Dtest="DatabaseAnalysisTest#countCustomers" -q | grep "Total Customers:" | awk '{print $3}')
echo "Customers after: $AFTER"

# Calculate growth
GROWTH=$((AFTER - BEFORE))

echo ""
echo "========================================="
echo "RESULTS"
echo "========================================="
echo "Before: $BEFORE customers"
echo "After:  $AFTER customers"
echo "Growth: $GROWTH customers"
echo ""

if [ $GROWTH -gt 10 ]; then
    echo "❌ FAILED: Database grew by $GROWTH customers!"
    echo ""
    echo "This indicates missing test isolation."
    echo "Please check:"
    echo "1. Use @TestTransaction instead of @Transactional"
    echo "2. Don't persist data in @BeforeEach without rollback"
    echo "3. Implement cleanup in @AfterEach if needed"
    exit 1
else
    echo "✅ PASSED: Growth is within acceptable limits"
    echo ""
    echo "Safe to commit!"
fi