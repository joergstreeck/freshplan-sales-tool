#!/bin/bash

# FreshPlan Sales Tool - Einfacher App-Test
# Lockerer Test ob die wichtigsten Seiten noch funktionieren

echo "🧪 Teste FreshPlan Sales Tool..."

# Farben
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Einfache Tests
test_passed=0
test_total=0

# Test 1: Frontend erreichbar
echo "📱 Teste Frontend..."
if curl -s --max-time 5 http://localhost:5173 | grep -q -i "freshplan\|react\|vite"; then
    echo -e "${GREEN}✅ Frontend läuft${NC}"
    ((test_passed++))
else
    echo -e "${RED}❌ Frontend Problem${NC}"
fi
((test_total++))

# Test 2: Backend API erreichbar
echo "🔧 Teste Backend..."
if curl -s --max-time 5 http://localhost:8080/api/ping | grep -q "timestamp\|ping\|pong"; then
    echo -e "${GREEN}✅ Backend API läuft${NC}"
    ((test_passed++))
else
    echo -e "${RED}❌ Backend API Problem${NC}"
fi
((test_total++))

# Test 3: Users Route (lockerer Test)
echo "👥 Teste Users-Route..."
if curl -s --max-time 5 "http://localhost:5173/users" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Users-Route erreichbar${NC}"
    ((test_passed++))
else
    echo -e "${YELLOW}⚠️  Users-Route nicht erreichbar (möglicherweise OK)${NC}"
fi
((test_total++))

# Zusammenfassung
echo ""
echo "📊 Test-Ergebnis: $test_passed/$test_total Tests bestanden"

if [ $test_passed -eq $test_total ]; then
    echo -e "${GREEN}🎉 Alle Tests erfolgreich!${NC}"
    exit 0
elif [ $test_passed -ge 2 ]; then
    echo -e "${YELLOW}⚠️  Grundfunktionen OK, aber nicht alles perfekt${NC}"
    exit 0
else
    echo -e "${RED}❌ Kritische Probleme gefunden!${NC}"
    exit 1
fi