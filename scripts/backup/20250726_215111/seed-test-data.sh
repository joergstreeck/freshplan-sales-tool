#!/bin/bash

# Script to seed test data in the development database
# Only works in development mode

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🌱 FreshPlan Test Data Seeder"
echo "============================="
echo

# Check if backend is running
if ! curl -s http://localhost:8080/q/health > /dev/null; then
    echo "❌ Backend is not running on port 8080"
    echo "   Please start the backend first with: ./scripts/start-backend.sh"
    exit 1
fi

# Seed test data via API endpoint
echo "📊 Seeding test data..."
response=$(curl -s -X POST http://localhost:8080/api/dev/test-data/seed \
    -H "Content-Type: application/json" \
    -H "Accept: application/json")

if [ $? -eq 0 ]; then
    echo "✅ Test data seeded successfully!"
    echo
    echo "Response: $response"
    echo
    echo "🔍 Current test data statistics:"
    curl -s http://localhost:8080/api/dev/test-data/stats | jq '.'
else
    echo "❌ Failed to seed test data"
    echo "Response: $response"
    exit 1
fi

echo
echo "💡 To view the test data in the Sales Cockpit:"
echo "   Open http://localhost:5173/cockpit"
echo
echo "🧹 To clean up test data later:"
echo "   ./scripts/clean-test-data.sh"