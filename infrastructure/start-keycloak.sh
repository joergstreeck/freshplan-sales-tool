#!/bin/bash

echo "🔑 Starting Keycloak with FreshPlan Dev Realm..."

# Stop any existing containers
docker-compose down

# Start fresh
docker-compose up -d db keycloak

echo "⏳ Waiting for Keycloak to be ready..."

# Wait for Keycloak to be healthy
until docker-compose exec keycloak curl -f http://localhost:8080/health/ready &>/dev/null; do
    echo -n "."
    sleep 2
done

echo ""
echo "✅ Keycloak is ready!"
echo ""
echo "🌐 Keycloak Admin Console: http://localhost:8180"
echo "👤 Admin credentials: admin/admin"
echo ""
echo "📋 Test users:"
echo "  - admin@freshplan.de / admin123 (role: admin)"
echo "  - manager@freshplan.de / manager123 (roles: manager, viewer)"
echo "  - sales@freshplan.de / sales123 (role: sales)"
echo ""
echo "🔧 Backend client: freshplan-backend (secret: secret)"
echo "🎨 Frontend client: freshplan-frontend (public client)"