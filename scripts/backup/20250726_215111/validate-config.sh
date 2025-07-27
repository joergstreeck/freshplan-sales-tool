#!/bin/bash

echo "🔍 FreshPlan Configuration Validator"
echo "===================================="
echo ""

# Check if we're in the right directory
if [ ! -f "pom.xml" ] && [ ! -f "backend/pom.xml" ]; then
    echo "❌ ERROR: Not in FreshPlan project root!"
    echo "   Please run from project root directory"
    exit 1
fi

# Function to check port
check_port() {
    local port=$1
    local service=$2
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        echo "✅ $service is running on port $port"
    else
        echo "❌ $service is NOT running on port $port"
        return 1
    fi
}

# Function to check Java version
check_java() {
    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        if [[ "$java_version" == "17" ]] || [[ "$java_version" == "21" ]]; then
            echo "✅ Java $java_version detected"
        else
            echo "⚠️  Java $java_version detected (Java 17+ recommended)"
        fi
    else
        echo "❌ Java not found!"
        return 1
    fi
}

# Function to check Maven
check_maven() {
    if command -v mvn &> /dev/null || [ -f "./mvnw" ]; then
        echo "✅ Maven wrapper found"
    else
        echo "❌ Maven not found!"
        return 1
    fi
}

# Function to check Node.js
check_node() {
    if command -v node &> /dev/null; then
        node_version=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
        if [[ "$node_version" -ge 18 ]]; then
            echo "✅ Node.js v$(node -v) detected"
        else
            echo "⚠️  Node.js v$(node -v) detected (v18+ recommended)"
        fi
    else
        echo "❌ Node.js not found!"
        return 1
    fi
}

# Function to check npm
check_npm() {
    if command -v npm &> /dev/null; then
        echo "✅ npm $(npm -v) detected"
    else
        echo "❌ npm not found!"
        return 1
    fi
}

# Function to check PostgreSQL
check_postgres() {
    # Use same method as check-services.sh for consistency
    if lsof -Pi :5432 -sTCP:LISTEN -t >/dev/null ; then
        echo "✅ PostgreSQL is running on port 5432"
    else
        echo "❌ PostgreSQL is NOT running on port 5432"
        return 1
    fi
}

# Run all checks
errors=0

echo "📋 Checking development tools..."
echo "--------------------------------"
check_java || ((errors++))
check_maven || ((errors++))
check_node || ((errors++))
check_npm || ((errors++))

echo ""
echo "📋 Checking services..."
echo "----------------------"
check_port 8080 "Backend (Quarkus)" || ((errors++))
check_port 5173 "Frontend (Vite)" || ((errors++))
check_postgres || ((errors++))
check_port 8180 "Keycloak" || echo "⚠️  Keycloak not running (optional in dev mode)"

echo ""
echo "📋 Configuration Summary"
echo "-----------------------"
if [ $errors -eq 0 ]; then
    echo "✅ All required services are configured correctly!"
else
    echo "❌ Found $errors configuration issues!"
    echo ""
    echo "💡 Quick fixes:"
    echo "   - Start Backend:  cd backend && ./mvnw quarkus:dev"
    echo "   - Start Frontend: cd frontend && npm run dev"
    echo "   - Start Database: docker-compose up -d postgres"
    echo "   - Start All:      ./scripts/start-dev.sh"
fi

exit $errors