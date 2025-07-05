#!/bin/bash

echo "🚀 Starting ALL FreshPlan Services"
echo "================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# Function to check if service is running
check_service() {
    local port=$1
    local service=$2
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null; then
        echo -e "${GREEN}✅ $service is already running on port $port${NC}"
        return 0
    else
        return 1
    fi
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

# 1. Start PostgreSQL
echo "1️⃣  PostgreSQL Database"
echo "----------------------"
if check_service 5432 "PostgreSQL"; then
    echo "   Database is ready"
else
    echo "   Starting PostgreSQL via Docker..."
    docker-compose up -d postgres
    wait_for_service 5432 "PostgreSQL"
fi
echo ""

# 2. Start Backend (in background)
echo "2️⃣  Backend Service (Java 17 + Quarkus)"
echo "--------------------------------------"
if check_service 8080 "Backend"; then
    echo "   Backend is ready"
else
    echo "   Starting Backend..."
    # Create logs directory if it doesn't exist
    mkdir -p logs
    # Start backend in background
    (cd backend && MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw quarkus:dev -Dquarkus.http.port=8080 > ../logs/backend.log 2>&1) &
    wait_for_service 8080 "Backend"
fi
echo ""

# 3. Start Frontend (in background)
echo "3️⃣  Frontend Service (Vite)"
echo "--------------------------"
if check_service 5173 "Frontend"; then
    echo "   Frontend is ready"
else
    echo "   Starting Frontend..."
    # Create logs directory if it doesn't exist
    mkdir -p logs
    # Start frontend in background
    (cd frontend && npm run dev > ../logs/frontend.log 2>&1) &
    wait_for_service 5173 "Frontend"
fi
echo ""

# 4. Summary
echo "📊 Service Summary"
echo "=================="
./scripts/check-services.sh

echo ""
echo -e "${GREEN}✅ All services are running!${NC}"
echo ""
echo "🌐 Access URLs:"
echo "  - Frontend:   http://localhost:5173"
echo "  - Backend:    http://localhost:8080"
echo "  - Swagger UI: http://localhost:8080/q/swagger-ui"
echo "  - Dev UI:     http://localhost:8080/q/dev"
echo ""
echo "📝 Log files:"
echo "  - Backend:  tail -f logs/backend.log"
echo "  - Frontend: tail -f logs/frontend.log"
echo ""
echo "🛑 To stop all services: ./scripts/stop-dev.sh"
echo ""
echo "💡 TIP: Services are running in background mode."
echo "   Use the log files to monitor their output."