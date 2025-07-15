#!/bin/bash

# Script to start Keycloak for local development and E2E testing
# Usage: ./scripts/start-keycloak.sh [dev|prod]

set -e

MODE="${1:-dev}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🚀 Starting Keycloak in $MODE mode..."

cd "$PROJECT_ROOT"

# Check if docker-compose.keycloak.yml exists
if [ ! -f "docker-compose.keycloak.yml" ]; then
    echo "❌ Error: docker-compose.keycloak.yml not found!"
    exit 1
fi

# Stop any running Keycloak containers
echo "🛑 Stopping any existing Keycloak containers..."
docker-compose -f docker-compose.keycloak.yml down

# Start Keycloak based on mode
if [ "$MODE" = "prod" ]; then
    echo "🏭 Starting Keycloak in production mode with PostgreSQL..."
    docker-compose -f docker-compose.keycloak.yml --profile production up -d
else
    echo "🔧 Starting Keycloak in development mode..."
    docker-compose -f docker-compose.keycloak.yml up -d keycloak
fi

# Wait for Keycloak to be ready
echo "⏳ Waiting for Keycloak to be ready..."
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -s http://localhost:8180/health/ready > /dev/null 2>&1; then
        echo "✅ Keycloak is ready!"
        break
    fi
    
    ATTEMPT=$((ATTEMPT + 1))
    echo "   Attempt $ATTEMPT/$MAX_ATTEMPTS..."
    sleep 2
done

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    echo "❌ Keycloak failed to start within 60 seconds"
    docker-compose -f docker-compose.keycloak.yml logs keycloak
    exit 1
fi

# Display useful information
echo ""
echo "🎉 Keycloak is running!"
echo "📋 Access Information:"
echo "   - Admin Console: http://localhost:8180/admin"
echo "   - Admin User: admin"
echo "   - Admin Password: admin"
echo ""
echo "🧪 Test Users:"
echo "   - admin@freshplan.de / admin123 (admin role)"
echo "   - manager@freshplan.de / manager123 (manager role)"
echo "   - sales@freshplan.de / sales123 (sales role)"
echo ""
echo "🔑 Client Credentials:"
echo "   - Client ID: freshplan-backend"
echo "   - Client Secret: secret"
echo ""
echo "📦 To stop Keycloak:"
echo "   docker-compose -f docker-compose.keycloak.yml down"
echo ""