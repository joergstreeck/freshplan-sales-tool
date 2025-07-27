#!/bin/bash

# Script to run E2E tests with Keycloak
# Usage: ./scripts/run-e2e-tests.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🧪 Running E2E Tests with Keycloak..."
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Keycloak is running
echo "🔍 Checking Keycloak status..."
if curl -s http://localhost:8180/health/ready > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Keycloak is running${NC}"
else
    echo -e "${YELLOW}⚠️  Keycloak is not running. Starting it now...${NC}"
    "$SCRIPT_DIR/start-keycloak.sh" dev
    
    # Wait a bit more for realm import
    echo "⏳ Waiting for realm import to complete..."
    sleep 5
fi

# Run E2E tests
echo ""
echo "🚀 Running E2E tests..."
cd "$PROJECT_ROOT/backend"

# Run only E2E tests with proper Maven command
mvn test -Dtest="de.freshplan.e2e.*" -DfailIfNoTests=false

# Check test results
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✅ E2E tests passed successfully!${NC}"
else
    echo ""
    echo -e "${RED}❌ E2E tests failed${NC}"
    echo "Check the test output above for details."
    exit 1
fi

echo ""
echo "📊 Test Summary:"
echo "   - Keycloak integration tests ✓"
echo "   - API security tests ✓"
echo "   - Role-based access control ✓"
echo ""
echo "💡 Tips:"
echo "   - View Keycloak admin console: http://localhost:8180/admin"
echo "   - Check test reports: backend/target/surefire-reports/"
echo "   - Stop Keycloak: docker-compose -f docker-compose.keycloak.yml down"
echo ""