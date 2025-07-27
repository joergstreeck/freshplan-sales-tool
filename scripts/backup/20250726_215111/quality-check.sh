#!/bin/bash

# 🏆 Quality Check Script
# Führt alle Quality Gates vor Commit aus

echo "🏆 FreshPlan Quality Check"
echo "========================="

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Trackers
FAILED=0

# 1. Frontend Lint & Format
echo -e "\n📋 Frontend Quality Checks..."
if [ -d "frontend" ]; then
    cd frontend
    
    # Lint
    echo -n "  Linting... "
    if npm run lint --silent; then
        echo -e "${GREEN}✅${NC}"
    else
        echo -e "${RED}❌${NC}"
        FAILED=$((FAILED + 1))
    fi
    
    # Format Check
    echo -n "  Formatting... "
    if npm run format:check --silent; then
        echo -e "${GREEN}✅${NC}"
    else
        echo -e "${RED}❌${NC}"
        echo -e "${YELLOW}    Run 'npm run format:fix' to fix${NC}"
        FAILED=$((FAILED + 1))
    fi
    
    # Tests
    echo -n "  Unit Tests... "
    if npm run test:unit -- --run --silent; then
        echo -e "${GREEN}✅${NC}"
    else
        echo -e "${RED}❌${NC}"
        FAILED=$((FAILED + 1))
    fi
    
    # Security
    echo -n "  Security Audit... "
    AUDIT_RESULT=$(npm audit --json 2>/dev/null | jq '.metadata.vulnerabilities.high + .metadata.vulnerabilities.critical' 2>/dev/null || echo "0")
    if [ "$AUDIT_RESULT" = "0" ]; then
        echo -e "${GREEN}✅${NC}"
    else
        echo -e "${RED}❌ ($AUDIT_RESULT critical/high vulnerabilities)${NC}"
        FAILED=$((FAILED + 1))
    fi
    
    cd ..
fi

# 2. Backend Checks
echo -e "\n📋 Backend Quality Checks..."
if [ -d "backend" ]; then
    cd backend
    
    # Spotless
    echo -n "  Code Formatting... "
    if ./mvnw spotless:check -q; then
        echo -e "${GREEN}✅${NC}"
    else
        echo -e "${RED}❌${NC}"
        echo -e "${YELLOW}    Run './mvnw spotless:apply' to fix${NC}"
        FAILED=$((FAILED + 1))
    fi
    
    # Compile
    echo -n "  Compilation... "
    if ./mvnw compile -q; then
        echo -e "${GREEN}✅${NC}"
    else
        echo -e "${RED}❌${NC}"
        FAILED=$((FAILED + 1))
    fi
    
    # Tests
    echo -n "  Unit Tests... "
    if ./mvnw test -q; then
        echo -e "${GREEN}✅${NC}"
    else
        echo -e "${RED}❌${NC}"
        FAILED=$((FAILED + 1))
    fi
    
    cd ..
fi

# 3. Git Checks
echo -e "\n📋 Git Checks..."
echo -n "  No TODOs in staged files... "
TODO_COUNT=$(git diff --cached --name-only | xargs grep -l "TODO" 2>/dev/null | wc -l)
if [ "$TODO_COUNT" -eq 0 ]; then
    echo -e "${GREEN}✅${NC}"
else
    echo -e "${YELLOW}⚠️  ($TODO_COUNT files with TODOs)${NC}"
fi

# 4. Documentation Check
echo -n "  README.md updated... "
if git diff --cached --name-only | grep -q "README.md"; then
    echo -e "${GREEN}✅${NC}"
else
    echo -e "${YELLOW}⚠️  (Don't forget to update docs)${NC}"
fi

# Summary
echo -e "\n📊 Quality Check Summary"
echo "========================"
if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ All quality checks passed!${NC}"
    echo -e "\nReady to commit! 🚀"
    exit 0
else
    echo -e "${RED}❌ $FAILED quality checks failed${NC}"
    echo -e "\nPlease fix the issues before committing."
    exit 1
fi