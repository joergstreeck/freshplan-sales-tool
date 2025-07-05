#!/bin/bash

echo "🚀 Starting FreshPlan Development Environment"
echo "==========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to wait for service
wait_for_service() {
    local port=$1
    local service=$2
    local max_attempts=30
    local attempt=1
    
    echo -n "⏳ Waiting for $service on port $port..."
    
    while ! lsof -Pi :$port -sTCP:LISTEN -t >/dev/null && [ $attempt -le $max_attempts ]; do
        sleep 1
        echo -n "."
        ((attempt++))
    done
    
    if [ $attempt -gt $max_attempts ]; then
        echo -e " ${RED}timeout!${NC}"
        return 1
    else
        echo -e " ${GREEN}ready!${NC}"
        return 0
    fi
}

# Check prerequisites
echo "📋 Checking prerequisites..."
if ! command_exists docker; then
    echo -e "${RED}❌ Docker is not installed!${NC}"
    exit 1
fi

if ! command_exists java; then
    echo -e "${RED}❌ Java is not installed!${NC}"
    exit 1
fi

if ! command_exists node; then
    echo -e "${RED}❌ Node.js is not installed!${NC}"
    exit 1
fi

# Start PostgreSQL
echo ""
echo "🗄️  Starting PostgreSQL..."
if ! docker ps | grep -q freshplan-postgres; then
    docker-compose up -d postgres
    wait_for_service 5432 "PostgreSQL"
else
    echo -e "${GREEN}✅ PostgreSQL already running${NC}"
fi

# Start Keycloak (optional)
echo ""
echo "🔐 Starting Keycloak (optional)..."
if ! docker ps | grep -q freshplan-keycloak; then
    echo "Skip Keycloak in dev mode (OIDC disabled)"
else
    echo -e "${GREEN}✅ Keycloak already running${NC}"
fi

# Start Backend
echo ""
echo "☕ Starting Backend..."
if ! lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null; then
    cd backend
    ./mvnw quarkus:dev -Dquarkus.http.port=8080 > ../logs/backend.log 2>&1 &
    cd ..
    wait_for_service 8080 "Backend"
else
    echo -e "${GREEN}✅ Backend already running${NC}"
fi

# Start Frontend
echo ""
echo "⚛️  Starting Frontend..."
if ! lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null; then
    cd frontend
    npm run dev > ../logs/frontend.log 2>&1 &
    cd ..
    wait_for_service 5173 "Frontend"
else
    echo -e "${GREEN}✅ Frontend already running${NC}"
fi

# Summary
echo ""
echo "📊 Service Status:"
echo "=================="
./scripts/check-services.sh

echo ""
echo -e "${GREEN}✅ Development environment is ready!${NC}"
echo ""
echo "🌐 Access points:"
echo "  - Frontend:  http://localhost:5173"
echo "  - Backend:   http://localhost:8080"
echo "  - Swagger:   http://localhost:8080/q/swagger-ui"
echo "  - Database:  postgresql://localhost:5432/freshplan"
echo ""
echo "📝 Logs:"
echo "  - Backend:   tail -f logs/backend.log"
echo "  - Frontend:  tail -f logs/frontend.log"
echo ""
echo "🛑 To stop all services: ./scripts/stop-dev.sh"