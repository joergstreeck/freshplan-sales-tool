#!/bin/bash

echo "☕ Starting FreshPlan Backend with Java 17"
echo "========================================"
echo ""

# Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# Verify Java version
echo "📋 Java Version Check:"
java -version 2>&1 | head -1
echo ""

# Check if backend directory exists
if [ ! -d "backend" ]; then
    echo "❌ ERROR: backend directory not found!"
    echo "   Please run from project root directory"
    exit 1
fi

# Kill any existing process on port 8080
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null; then
    echo "⚠️  Port 8080 is already in use. Stopping existing process..."
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    sleep 2
fi

# Start backend
cd backend

echo "🚀 Starting Quarkus Backend on port 8080..."
echo "   Dev UI: http://localhost:8080/q/dev"
echo "   Swagger: http://localhost:8080/q/swagger-ui"
echo ""

# Set Maven options
export MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD"

# Start in dev mode with hot reload
./mvnw quarkus:dev \
    -Dquarkus.http.port=8080 \
    -Dquarkus.http.host=0.0.0.0 \
    -Djava.util.logging.manager=org.jboss.logmanager.LogManager