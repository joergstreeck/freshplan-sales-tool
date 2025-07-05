#!/bin/bash

echo "🔍 FreshPlan Code Review Checker"
echo "================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Initialize counters
ISSUES=0
WARNINGS=0

echo "📋 Running automated checks..."
echo ""

# 1. Check line length
echo "1️⃣  Checking line length (max 100 chars)..."
LONG_LINES=$(find backend/src/main/java -name "*.java" -type f -exec grep -l '.\{101,\}' {} \; 2>/dev/null | wc -l)
if [ "$LONG_LINES" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Found $LONG_LINES files with lines > 100 characters${NC}"
    ((WARNINGS++))
else
    echo -e "${GREEN}✅ All lines within limit${NC}"
fi

# 2. Check for TODOs
echo ""
echo "2️⃣  Checking for TODO comments..."
TODO_COUNT=$(grep -r "TODO\|FIXME\|XXX" backend/src/main/java --include="*.java" 2>/dev/null | wc -l)
if [ "$TODO_COUNT" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Found $TODO_COUNT TODO/FIXME comments${NC}"
    grep -r "TODO\|FIXME" backend/src/main/java --include="*.java" 2>/dev/null | head -5
    ((WARNINGS++))
else
    echo -e "${GREEN}✅ No TODO comments found${NC}"
fi

# 3. Check for System.out.println
echo ""
echo "3️⃣  Checking for System.out.println..."
SYSOUT_COUNT=$(grep -r "System\.out\.println" backend/src/main/java --include="*.java" 2>/dev/null | wc -l)
if [ "$SYSOUT_COUNT" -gt 0 ]; then
    echo -e "${RED}❌ Found $SYSOUT_COUNT System.out.println statements${NC}"
    ((ISSUES++))
else
    echo -e "${GREEN}✅ No System.out.println found${NC}"
fi

# 4. Check for proper exception handling
echo ""
echo "4️⃣  Checking exception handling..."
EMPTY_CATCH=$(grep -r "catch.*{[\s]*}" backend/src/main/java --include="*.java" 2>/dev/null | wc -l)
if [ "$EMPTY_CATCH" -gt 0 ]; then
    echo -e "${RED}❌ Found $EMPTY_CATCH empty catch blocks${NC}"
    ((ISSUES++))
else
    echo -e "${GREEN}✅ No empty catch blocks${NC}"
fi

# 5. Check JavaDoc coverage
echo ""
echo "5️⃣  Checking JavaDoc coverage..."
PUBLIC_METHODS=$(grep -r "public.*(" backend/src/main/java --include="*.java" | grep -v "public static void main" | wc -l)
JAVADOC_COMMENTS=$(grep -r "/\*\*" backend/src/main/java --include="*.java" | wc -l)
if [ "$JAVADOC_COMMENTS" -lt "$PUBLIC_METHODS" ]; then
    echo -e "${YELLOW}⚠️  JavaDoc coverage might be incomplete${NC}"
    echo "   Public methods: $PUBLIC_METHODS, JavaDoc blocks: $JAVADOC_COMMENTS"
    ((WARNINGS++))
else
    echo -e "${GREEN}✅ Good JavaDoc coverage${NC}"
fi

# 6. Check test coverage
echo ""
echo "6️⃣  Checking test files..."
JAVA_FILES=$(find backend/src/main/java -name "*.java" -type f | wc -l)
TEST_FILES=$(find backend/src/test/java -name "*Test.java" -type f | wc -l)
if [ "$TEST_FILES" -lt "$JAVA_FILES" ]; then
    echo -e "${YELLOW}⚠️  Test coverage might be incomplete${NC}"
    echo "   Java files: $JAVA_FILES, Test files: $TEST_FILES"
    ((WARNINGS++))
else
    echo -e "${GREEN}✅ Good test file coverage${NC}"
fi

# 7. Check for hardcoded values
echo ""
echo "7️⃣  Checking for hardcoded values..."
HARDCODED=$(grep -r "localhost\|8080\|5432\|password\|secret" backend/src/main/java --include="*.java" | grep -v "application.properties" | wc -l)
if [ "$HARDCODED" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Found $HARDCODED potential hardcoded values${NC}"
    ((WARNINGS++))
else
    echo -e "${GREEN}✅ No obvious hardcoded values${NC}"
fi

# Summary
echo ""
echo "📊 Code Review Summary"
echo "====================="
echo -e "Critical Issues: ${RED}$ISSUES${NC}"
echo -e "Warnings: ${YELLOW}$WARNINGS${NC}"
echo ""

if [ "$ISSUES" -gt 0 ]; then
    echo -e "${RED}❌ Code review FAILED - Please fix critical issues${NC}"
    exit 1
elif [ "$WARNINGS" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Code review passed with warnings${NC}"
    echo "   Consider addressing the warnings before merging"
else
    echo -e "${GREEN}✅ Code review PASSED - Great job!${NC}"
fi

echo ""
echo "💡 For detailed manual review, use the checklist in CLAUDE.md Section 0.10"