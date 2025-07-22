#!/bin/bash

# Test: Dokumentation muss mit Code übereinstimmen
# Prüft ob Konfiguration, Imports und Beispiele korrekt sind

echo "📋 Documentation Consistency Test"
echo "================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Counters
TOTAL_CHECKS=0
FAILED_CHECKS=0

echo "Checking documentation consistency with actual code..."
echo ""

# Function to check a specific consistency rule
check_consistency() {
    local check_name="$1"
    local expected="$2"
    local actual="$3"
    local file="$4"
    
    ((TOTAL_CHECKS++))
    
    if [ "$expected" != "$actual" ]; then
        ((FAILED_CHECKS++))
        echo -e "${RED}❌ INCONSISTENCY: $check_name${NC}"
        echo "   File: $file"
        echo "   Expected: $expected"
        echo "   Actual: $actual"
        echo ""
        return 1
    fi
    return 0
}

# 1. Check Keycloak Configuration
echo "1. Checking Keycloak Configuration..."
echo "-------------------------------------"

# Extract realm from application.properties
if [ -f "backend/src/main/resources/application.properties" ]; then
    ACTUAL_REALM=$(grep "quarkus.oidc.auth-server-url" backend/src/main/resources/application.properties | grep -o "realms/[^/]*" | cut -d'/' -f2)
    
    # Check if documentation mentions the correct realm
    while IFS= read -r doc_file; do
        if grep -q "realm" "$doc_file" 2>/dev/null; then
            DOC_REALM=$(grep -i "realm" "$doc_file" | grep -o "freshplan\|FreshFoodz\|freshfoodz" | head -1)
            if [ -n "$DOC_REALM" ] && [ -n "$ACTUAL_REALM" ]; then
                check_consistency "Keycloak Realm" "$ACTUAL_REALM" "$DOC_REALM" "$doc_file"
            fi
        fi
    done < <(find docs/features -name "*CLAUDE_TECH*.md" -o -name "*TECH_CONCEPT*.md" | grep -i security)
fi

# 2. Check Port Configuration
echo -e "\n2. Checking Port Configuration..."
echo "-------------------------------------"

# Standard ports
EXPECTED_BACKEND_PORT="8080"
EXPECTED_FRONTEND_PORT="5173"
EXPECTED_KEYCLOAK_PORT="8180"

# Check if docs mention correct ports
grep -r "localhost:80" docs/features --include="*.md" | while read -r line; do
    file=$(echo "$line" | cut -d':' -f1)
    port=$(echo "$line" | grep -o "localhost:[0-9]*" | cut -d':' -f2)
    
    case $port in
        "8080"|"8081") 
            # Backend port OK
            ;;
        "5173")
            # Frontend port OK
            ;;
        "8180"|"8181")
            # Keycloak port OK
            ;;
        *)
            ((TOTAL_CHECKS++))
            ((FAILED_CHECKS++))
            echo -e "${RED}❌ Unknown port in documentation${NC}"
            echo "   File: $file"
            echo "   Found port: $port"
            echo "   Expected: 8080 (backend), 5173 (frontend), or 8180 (keycloak)"
            echo ""
            ;;
    esac
done

# 3. Check Java Package Structure
echo -e "\n3. Checking Java Package References..."
echo "-------------------------------------"

# Check if documented packages exist
while IFS= read -r doc_file; do
    # Extract Java package references
    grep -o "de\.freshplan\.[a-zA-Z.]*" "$doc_file" 2>/dev/null | sort -u | while read -r package; do
        ((TOTAL_CHECKS++))
        
        # Convert package to path
        package_path="backend/src/main/java/$(echo $package | tr '.' '/')"
        
        # Check if at least one file exists in this package
        if ! find "$package_path" -name "*.java" -maxdepth 1 2>/dev/null | grep -q .; then
            ((FAILED_CHECKS++))
            echo -e "${RED}❌ Package not found${NC}"
            echo "   Documentation: $doc_file"
            echo "   References package: $package"
            echo "   Expected at: $package_path"
            echo ""
        fi
    done
done < <(find docs/features -name "*.md" -type f)

# 4. Check for javax vs jakarta
echo -e "\n4. Checking javax vs jakarta imports..."
echo "-------------------------------------"

# Count javax usage (should be 0 in new code)
JAVAX_IN_DOCS=$(grep -r "import javax\." docs/features --include="*.md" | wc -l)
JAKARTA_IN_DOCS=$(grep -r "import jakarta\." docs/features --include="*.md" | wc -l)

if [ $JAVAX_IN_DOCS -gt 0 ]; then
    ((TOTAL_CHECKS++))
    ((FAILED_CHECKS++))
    echo -e "${RED}❌ Outdated javax imports in documentation${NC}"
    echo "   Found $JAVAX_IN_DOCS javax imports (should use jakarta)"
    echo "   Files with javax:"
    grep -l "import javax\." docs/features/**/*.md | head -5
    echo ""
fi

# 5. Check Security Annotations
echo -e "\n5. Checking Security Annotations..."
echo "-------------------------------------"

# Check if @RolesAllowed matches documented roles
DOCUMENTED_ROLES=$(grep -r "@RolesAllowed" docs/features --include="*.md" | grep -o '"[^"]*"' | sort -u)
ACTUAL_ROLES=$(grep -r "@RolesAllowed" backend/src --include="*.java" | grep -o '"[^"]*"' | sort -u)

echo "Documented roles:"
echo "$DOCUMENTED_ROLES" | sed 's/^/  /'
echo ""
echo "Actual roles in code:"
echo "$ACTUAL_ROLES" | sed 's/^/  /'

# Summary
echo ""
echo "========================================"
echo "FINAL REPORT:"
echo "========================================"
echo "Total consistency checks: $TOTAL_CHECKS"
echo -e "${RED}Failed checks: $FAILED_CHECKS${NC}"

if [ $FAILED_CHECKS -eq 0 ]; then
    echo -e "\n${GREEN}✅ SUCCESS: Documentation is consistent with code!${NC}"
    exit 0
else
    echo -e "\n${RED}❌ FAILED: Found $FAILED_CHECKS inconsistencies!${NC}"
    echo ""
    echo "IMPACT:"
    echo "- Developers waste time debugging mismatches"
    echo "- Code examples don't work as documented"
    echo "- Configuration errors in production"
    echo ""
    echo "HOW TO FIX:"
    echo "1. Update documentation to match actual code"
    echo "2. Or update code to match documentation"
    echo "3. Keep docs and code in sync going forward"
    exit 1
fi