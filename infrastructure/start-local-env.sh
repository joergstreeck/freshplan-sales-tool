#!/bin/bash
# Start local development environment for FreshPlan 2.0

echo "🚀 Starting FreshPlan local environment..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker Desktop first."
    exit 1
fi

# Start services
echo "📦 Starting PostgreSQL and Keycloak..."
docker compose up -d db keycloak

# Wait for services to be healthy
echo "⏳ Waiting for services to be ready..."
timeout 60s bash -c 'until docker compose ps | grep -E "(healthy|running).*freshplan-db" && docker compose ps | grep -E "(healthy|running).*freshplan-keycloak"; do sleep 2; done'

if [ $? -eq 0 ]; then
    echo "✅ Services are ready!"
    echo ""
    echo "📋 Service URLs:"
    echo "   - PostgreSQL: localhost:5432 (user: freshplan, pass: freshplan)"
    echo "   - Keycloak Admin: http://localhost:8180 (user: admin, pass: admin)"
    echo ""
    echo "💡 Next steps:"
    echo "   1. Create Keycloak realm 'freshplan' and client 'freshplan-backend'"
    echo "   2. cd ../backend && ./mvnw quarkus:dev"
    echo "   3. cd ../frontend && npm run dev"
else
    echo "❌ Services failed to start. Check logs with: docker compose logs"
    exit 1
fi