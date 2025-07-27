#!/bin/bash

echo "🛑 Stopping FreshPlan Development Environment"
echo "==========================================="
echo ""

# Function to stop service on port
stop_port() {
    local port=$1
    local service=$2
    
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null; then
        echo -n "Stopping $service on port $port..."
        lsof -ti:$port | xargs kill -9 2>/dev/null
        echo " ✅"
    else
        echo "$service on port $port is not running"
    fi
}

# Stop services
stop_port 8080 "Backend"
stop_port 5173 "Frontend"

# Stop Docker services
echo ""
echo "Stopping Docker services..."
docker-compose down

echo ""
echo "✅ All services stopped!"