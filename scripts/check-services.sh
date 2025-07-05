#!/bin/bash

echo "🔍 Checking FreshPlan Services..."
echo "================================"

# Function to check if a port is in use
check_port() {
    local port=$1
    local service=$2
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        echo "✅ $service läuft auf Port $port"
        return 0
    else
        echo "❌ $service läuft NICHT auf Port $port"
        return 1
    fi
}

# Check all services
errors=0

check_port 8080 "Backend" || ((errors++))
check_port 5173 "Frontend" || ((errors++))
check_port 5432 "PostgreSQL" || ((errors++))
check_port 8180 "Keycloak" || echo "   ℹ️  (Optional in Dev Mode)"

echo ""
if [ $errors -eq 0 ]; then
    echo "✅ Alle Services laufen!"
else
    echo "⚠️  $errors Service(s) sind nicht erreichbar!"
    echo ""
    echo "Starte Services mit:"
    echo "  ./scripts/start-dev.sh"
fi

exit $errors