#!/bin/bash

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

echo "🔍 FRESHPLAN SYSTEM DIAGNOSIS"
echo "=============================="
echo ""

# Test 1: Backend Health
echo "🔧 Backend Status:"
if curl -s http://localhost:8080/q/health > /dev/null 2>&1; then
    echo -e "   ${GREEN}✅ Backend: RUNNING${NC}"
    
    # Check API response
    RESPONSE=$(curl -s http://localhost:8080/api/customers 2>/dev/null)
    if echo "$RESPONSE" | grep -q "totalElements"; then
        CUSTOMERS=$(echo "$RESPONSE" | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
        if [ "$CUSTOMERS" = "0" ]; then
            echo -e "   ${YELLOW}⚠️  Database: EMPTY (no test data)${NC}"
            echo "   💡 FIX: Backend needs restart to load CustomerDataInitializer"
            echo "      Command: pkill -f quarkus && cd backend && nohup mvn quarkus:dev > ../logs/backend.log 2>&1 &"
        else
            echo -e "   ${GREEN}✅ Database: $CUSTOMERS customers found${NC}"
        fi
    else
        echo -e "   ${RED}❌ API: Not responding correctly${NC}"
        echo "   💡 FIX: Check backend logs with 'tail -f logs/backend.log'"
    fi
else
    echo -e "   ${RED}❌ Backend: DOWN${NC}"
    echo "   💡 FIX: Run the following commands:"
    echo "      export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
    echo "      cd backend && nohup mvn quarkus:dev > ../logs/backend.log 2>&1 &"
fi
echo ""

# Test 2: Frontend Status
echo "🔧 Frontend Status:"
if curl -s http://localhost:5173 > /dev/null 2>&1; then
    echo -e "   ${GREEN}✅ Frontend: RUNNING${NC}"
else
    echo -e "   ${RED}❌ Frontend: DOWN${NC}"
    echo "   💡 FIX: cd frontend && npm run dev"
fi
echo ""

# Test 3: Java Version
echo "🔧 Java Environment:"
JAVA_VERSION=$(java -version 2>&1 | head -1 | grep -o '"[^"]*"' | tr -d '"' | cut -d'.' -f1)
if [ "$JAVA_VERSION" = "17" ]; then
    echo -e "   ${GREEN}✅ Java: Version 17 (correct)${NC}"
else
    echo -e "   ${RED}❌ Java: Version $JAVA_VERSION (need 17)${NC}"
    echo "   💡 FIX: export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
fi
echo ""

# Test 4: PostgreSQL
echo "🔧 Database Status:"
if lsof -Pi :5432 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "   ${GREEN}✅ PostgreSQL: RUNNING${NC}"
else
    echo -e "   ${RED}❌ PostgreSQL: DOWN${NC}"
    echo "   💡 FIX: docker-compose up -d postgres"
fi
echo ""

echo "💡 TIP: Run this script anytime something seems broken"
echo "💡 USAGE: ./scripts/diagnose-problems.sh"