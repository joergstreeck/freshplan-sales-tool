#!/bin/bash

# Script to clean test data from the development database
# Only works in development mode

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🧹 FreshPlan Test Data Cleaner"
echo "=============================="
echo

# Check if backend is running
if ! curl -s http://localhost:8080/q/health > /dev/null; then
    echo "❌ Backend is not running on port 8080"
    echo "   Please start the backend first with: ./scripts/start-backend.sh"
    exit 1
fi

# Show current test data before cleaning
echo "📊 Current test data:"
curl -s http://localhost:8080/api/dev/test-data/stats | jq '.'
echo

# Confirm deletion
read -p "⚠️  Are you sure you want to delete all test data? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Operation cancelled"
    exit 1
fi

# Clean test data via API endpoint
echo "🗑️  Cleaning test data..."
response=$(curl -s -X DELETE http://localhost:8080/api/dev/test-data/clean \
    -H "Content-Type: application/json" \
    -H "Accept: application/json")

if [ $? -eq 0 ]; then
    echo "✅ Test data cleaned successfully!"
    echo
    echo "Response: $response"
    echo
    echo "📊 Remaining test data:"
    curl -s http://localhost:8080/api/dev/test-data/stats | jq '.'
else
    echo "❌ Failed to clean test data"
    echo "Response: $response"
    exit 1
fi