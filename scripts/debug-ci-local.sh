#!/bin/bash

# Local CI Debug Script
# Reproduces CI environment locally for debugging

set -e

echo "🔍 FreshPlan CI Debug - Local Reproduction"
echo "=========================================="

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

debug_log() {
    echo -e "${BLUE}[DEBUG]${NC} $1"
}

error_log() {
    echo -e "${RED}[ERROR]${NC} $1"
}

success_log() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning_log() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Function to check command existence
check_command() {
    if command -v "$1" &> /dev/null; then
        success_log "$1 is available"
        $1 --version 2>/dev/null || $1 -version 2>/dev/null || echo "Could not get version"
    else
        error_log "$1 is not available"
        return 1
    fi
}

# System Requirements Check
echo -e "\n${BLUE}1. SYSTEM REQUIREMENTS CHECK${NC}"
echo "=================================="

debug_log "Checking system requirements..."
check_command node
check_command npm
check_command java
check_command mvn
check_command psql

# Environment Info
echo -e "\n${BLUE}2. ENVIRONMENT INFORMATION${NC}"
echo "=================================="

debug_log "Current working directory: $(pwd)"
debug_log "Git branch: $(git branch --show-current)"
debug_log "Git status:"
git status --porcelain

# Java Environment Check
echo -e "\n${BLUE}3. JAVA ENVIRONMENT${NC}"
echo "=================================="

debug_log "JAVA_HOME: ${JAVA_HOME:-'Not set'}"
debug_log "Java classpath check..."
java -cp "." -version 2>/dev/null || warning_log "Java classpath issue detected"

# Database Check
echo -e "\n${BLUE}4. DATABASE CONNECTION${NC}"
echo "=================================="

debug_log "Checking PostgreSQL connection..."
if pg_isready -h localhost -p 5432 -U freshplan -d freshplan &>/dev/null; then
    success_log "PostgreSQL is available"
    psql -h localhost -p 5432 -U freshplan -d freshplan -c "SELECT version();" 2>/dev/null || warning_log "Could not query database"
else
    error_log "PostgreSQL not available on localhost:5432"
    warning_log "Make sure PostgreSQL is running: brew services start postgresql"
fi

# Frontend Debug
echo -e "\n${BLUE}5. FRONTEND DEBUG${NC}"
echo "=================================="

cd frontend

debug_log "Frontend directory contents:"
ls -la

debug_log "Package.json lint script:"
grep -A1 -B1 '"lint"' package.json || error_log "No lint script found"

debug_log "ESLint config:"
if [ -f "eslint.config.js" ]; then
    cat eslint.config.js
else
    error_log "No eslint.config.js found"
fi

debug_log "Node modules status:"
if [ -d "node_modules" ]; then
    success_log "node_modules exists"
    ls -la node_modules/.bin/ | head -5
else
    warning_log "No node_modules - running npm install"
    npm install
fi

debug_log "Running ESLint with debug output..."
echo "--- ESLint Output Start ---"
npm run lint -- --debug 2>&1 || warning_log "ESLint had warnings/errors"
echo "--- ESLint Output End ---"

debug_log "Testing frontend build..."
npm run build 2>&1 || error_log "Frontend build failed"

# Backend Debug
echo -e "\n${BLUE}6. BACKEND DEBUG${NC}"
echo "=================================="

cd ../backend

debug_log "Backend directory contents:"
ls -la

debug_log "Maven wrapper permissions:"
ls -la mvnw

debug_log "POM.xml check:"
if [ -f "pom.xml" ]; then
    success_log "pom.xml exists"
    debug_log "Project info:"
    ./mvnw help:evaluate -Dexpression=project.name -q -DforceStdout 2>/dev/null || warning_log "Could not get project name"
    ./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null || warning_log "Could not get project version"
else
    error_log "No pom.xml found"
fi

debug_log "Java compilation test..."
./mvnw clean compile -q || error_log "Compilation failed"

debug_log "Running backend tests with detailed output..."
echo "--- Maven Test Output Start ---"
export DATABASE_URL="jdbc:postgresql://localhost:5432/freshplan"
export CI_GREEN="true"
./mvnw test -X -e | tee ../debug-maven.log || warning_log "Some tests failed"
echo "--- Maven Test Output End ---"

debug_log "Test results summary:"
if [ -d "target/surefire-reports" ]; then
    find target/surefire-reports -name "*.xml" -exec grep -l "failure\|error" {} \; | head -5
else
    warning_log "No surefire reports found"
fi

# Summary
echo -e "\n${BLUE}7. DEBUG SUMMARY${NC}"
echo "=================================="

cd ..

success_log "Debug complete! Check the following files:"
echo "  - debug-maven.log (Maven test output)"
echo "  - backend/target/surefire-reports/ (Test reports)"

debug_log "Next steps for CI debugging:"
echo "  1. Review the detailed logs above"
echo "  2. Check specific test failures in surefire-reports"
echo "  3. Compare local vs CI environment differences"
echo "  4. Run: gh run list --branch \$(git branch --show-current) --limit 5"

warning_log "If issues persist, create a minimal reproduction case"

echo -e "\n${GREEN}Debug script completed!${NC}"