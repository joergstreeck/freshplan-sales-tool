#!/bin/bash
# Start FreshPlan Development Environment

echo "🚀 Starting FreshPlan Development Environment..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

# Start infrastructure
echo -e "${YELLOW}Starting infrastructure services...${NC}"
cd infrastructure
docker-compose up -d
cd ..

# Wait for services to be healthy
echo "⏳ Waiting for services to be ready..."
sleep 10

# Start backend
echo -e "${YELLOW}Starting backend...${NC}"
cd backend
export PATH="$PWD/../apache-maven-3.9.6/bin:$PATH"
mvn quarkus:dev &
BACKEND_PID=$!
cd ..

# Start frontend
echo -e "${YELLOW}Starting frontend...${NC}"
cd frontend
npm run dev &
FRONTEND_PID=$!
cd ..

echo -e "${GREEN}✅ Development environment started!${NC}"
echo ""
echo "📍 Services:"
echo "   Frontend:    http://localhost:5173"
echo "   Backend API: http://localhost:8080"
echo "   Swagger UI:  http://localhost:8080/q/swagger-ui"
echo "   Keycloak:    http://localhost:8180 (admin/admin)"
echo "   PostgreSQL:  localhost:5432 (freshplan/freshplan)"
echo ""
echo "🔐 For development login:"
echo "   1. Go to http://localhost:5173/login-bypass"
echo "   2. Click 'Login as Admin'"
echo ""
echo "Press Ctrl+C to stop all services..."

# Wait for Ctrl+C
trap 'echo "Stopping services..."; kill $BACKEND_PID $FRONTEND_PID; cd infrastructure && docker-compose down; exit' INT
wait