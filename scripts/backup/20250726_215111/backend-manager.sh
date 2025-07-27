#!/bin/bash

BACKEND_URL="http://localhost:8080"
JAVA_HOME_PATH="/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
PROJECT_ROOT="/Users/joergstreeck/freshplan-sales-tool"
LOG_FILE="$PROJECT_ROOT/logs/backend.log"
PID_FILE="$PROJECT_ROOT/logs/backend.pid"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Create logs directory if not exists
mkdir -p "$PROJECT_ROOT/logs"

check_backend() {
    curl -s "$BACKEND_URL/q/health" > /dev/null 2>&1
    return $?
}

start_backend() {
    echo "🚀 Starting Backend with Java 17..."
    
    # Set Java 17
    export JAVA_HOME="$JAVA_HOME_PATH"
    export PATH="$JAVA_HOME/bin:$PATH"
    
    # Kill any existing backend
    pkill -f "quarkus:dev" 2>/dev/null
    
    # Start backend
    cd "$PROJECT_ROOT/backend"
    nohup mvn quarkus:dev > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    cd "$PROJECT_ROOT"
    
    # Wait for startup
    echo "⏳ Waiting for backend to start..."
    for i in {1..60}; do
        sleep 1
        if check_backend; then
            echo -e "${GREEN}✅ Backend started successfully after ${i}s${NC}"
            return 0
        fi
    done
    
    echo -e "${RED}❌ Backend failed to start after 60s${NC}"
    echo "💡 Check logs: tail -f $LOG_FILE"
    return 1
}

stop_backend() {
    echo "🛑 Stopping Backend..."
    
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        kill $PID 2>/dev/null
        rm -f "$PID_FILE"
    fi
    
    # Fallback: Kill by name
    pkill -f "quarkus:dev" 2>/dev/null
    
    echo -e "${GREEN}✅ Backend stopped${NC}"
}

restart_backend() {
    stop_backend
    sleep 3
    start_backend
}

status_backend() {
    if check_backend; then
        echo -e "${GREEN}✅ Backend is RUNNING${NC}"
        echo "🌐 Health: http://localhost:8080/q/health"
        echo "📊 Swagger: http://localhost:8080/q/swagger-ui"
        echo "🛠️  Dev UI: http://localhost:8080/q/dev"
    else
        echo -e "${RED}❌ Backend is DOWN${NC}"
        echo "💡 Start with: $0 start"
    fi
}

show_logs() {
    if [ -f "$LOG_FILE" ]; then
        echo "📄 Backend logs (last 50 lines):"
        echo "================================="
        tail -n 50 "$LOG_FILE"
    else
        echo -e "${YELLOW}⚠️  No log file found${NC}"
    fi
}

# Main logic
case "$1" in
    start)   start_backend ;;
    stop)    stop_backend ;;
    restart) restart_backend ;;
    status)  status_backend ;;
    logs)    show_logs ;;
    *)       
        echo "FreshPlan Backend Manager"
        echo "========================"
        echo "Usage: $0 {start|stop|restart|status|logs}"
        echo ""
        echo "Commands:"
        echo "  start    - Start backend with Java 17"
        echo "  stop     - Stop backend"
        echo "  restart  - Stop and start backend"
        echo "  status   - Check if backend is running"
        echo "  logs     - Show recent backend logs"
        echo ""
        echo "Examples:"
        echo "  $0 start    # Start backend"
        echo "  $0 status   # Check if running"
        echo "  $0 logs     # Show recent logs"
        ;;
esac