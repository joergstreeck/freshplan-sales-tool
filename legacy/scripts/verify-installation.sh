#!/bin/bash

# Verifizierungs-Script für FreshPlan Installation
# Prüft ob alle wichtigen Dateien vorhanden und die Umgebung korrekt ist

echo "🔍 FreshPlan Installation Verification"
echo "====================================="
echo ""

ERRORS=0
WARNINGS=0

# Funktion für Erfolg/Fehler-Ausgabe
check_pass() {
    echo "✅ $1"
}

check_fail() {
    echo "❌ $1"
    ((ERRORS++))
}

check_warn() {
    echo "⚠️  $1"
    ((WARNINGS++))
}

# 1. Node.js Version prüfen
echo "1. Checking Node.js..."
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v)
    check_pass "Node.js installed: $NODE_VERSION"
else
    check_fail "Node.js not installed"
fi

# 2. NPM Packages prüfen
echo -e "\n2. Checking npm packages..."
if [ -f "package.json" ]; then
    check_pass "package.json found"
    
    if [ -d "node_modules" ]; then
        check_pass "node_modules exists"
    else
        check_fail "node_modules missing - run 'npm install'"
    fi
else
    check_fail "package.json not found"
fi

# 3. Wichtige Konfigurationsdateien
echo -e "\n3. Checking configuration files..."
configs=(
    "config/ui.config.json"
    "config/business-rules.config.json"
    "config/text-content.config.json"
    "vite.config.ts"
    "tsconfig.json"
)

for config in "${configs[@]}"; do
    if [ -f "$config" ]; then
        check_pass "$config exists"
    else
        check_fail "$config missing"
    fi
done

# 4. Standalone HTML prüfen
echo -e "\n4. Checking standalone version..."
if [ -f "freshplan-complete.html" ]; then
    check_pass "freshplan-complete.html exists"
    FILE_SIZE=$(wc -c < freshplan-complete.html)
    if [ $FILE_SIZE -gt 10000 ]; then
        check_pass "Standalone file size OK ($FILE_SIZE bytes)"
    else
        check_warn "Standalone file seems too small"
    fi
else
    check_fail "freshplan-complete.html missing"
fi

# 5. E2E Test-Dateien
echo -e "\n5. Checking E2E test setup..."
if [ -f "playwright.config.ts" ]; then
    check_pass "Playwright config exists"
else
    check_fail "Playwright config missing"
fi

if [ -d "e2e" ]; then
    TEST_COUNT=$(ls -1 e2e/*.spec.ts 2>/dev/null | wc -l)
    if [ $TEST_COUNT -gt 0 ]; then
        check_pass "$TEST_COUNT E2E test files found"
    else
        check_warn "No E2E test files in e2e/ directory"
    fi
else
    check_fail "e2e/ directory missing"
fi

# 6. Build-Verzeichnis
echo -e "\n6. Checking build output..."
if [ -d "dist" ]; then
    check_pass "dist/ directory exists"
    if [ -f "dist/index.html" ]; then
        check_pass "dist/index.html exists"
    else
        check_warn "dist/index.html missing - run 'npm run build'"
    fi
else
    check_warn "dist/ directory missing - run 'npm run build'"
fi

# 7. Assets prüfen
echo -e "\n7. Checking assets..."
if [ -f "assets/images/logo.png" ]; then
    check_pass "Logo file exists"
else
    check_warn "Logo file missing at assets/images/logo.png"
fi

# 8. Git Status
echo -e "\n8. Checking Git status..."
if [ -d ".git" ]; then
    check_pass "Git repository initialized"
    UNCOMMITTED=$(git status --porcelain | wc -l)
    if [ $UNCOMMITTED -eq 0 ]; then
        check_pass "All changes committed"
    else
        check_warn "$UNCOMMITTED uncommitted changes"
    fi
else
    check_warn "Not a git repository"
fi

# Zusammenfassung
echo -e "\n====================================="
echo "Summary:"
echo "-------------------------------------"
echo "✅ Passed: $((8 - ERRORS - WARNINGS))"
echo "⚠️  Warnings: $WARNINGS"
echo "❌ Errors: $ERRORS"

if [ $ERRORS -eq 0 ]; then
    echo -e "\n🎉 Installation looks good!"
    
    echo -e "\nNext steps:"
    echo "1. Run 'npm run dev' to start development server"
    echo "2. Run 'npm run test:e2e' to verify functionality"
    echo "3. Open 'freshplan-complete.html' for standalone demo"
else
    echo -e "\n⚠️  Please fix the errors above before continuing"
    exit 1
fi