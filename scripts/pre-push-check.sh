#!/bin/bash

# FreshPlan Pre-Push Security Check
# NIEMALS ohne diesen Check pushen!

echo "🚦 FreshPlan Pre-Push Security Check"
echo "===================================="
echo ""

# Exit on any error
set -e

# Farben
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m'

ERRORS=0
WARNINGS=0

# 1. Repository Sauberkeit
echo "🧹 Prüfe Repository-Sauberkeit..."
if [ -f "./scripts/quick-cleanup.sh" ]; then
    ./scripts/quick-cleanup.sh
    echo -e "${GREEN}✓ Repository gesäubert${NC}"
else
    echo -e "${YELLOW}⚠ Cleanup-Script nicht gefunden${NC}"
    ((WARNINGS++))
fi
echo ""

# 2. Secrets-Check
echo "🔒 Prüfe auf mögliche Secrets..."
SECRETS_FOUND=0

# Patterns die NICHT gepusht werden dürfen
PATTERNS=(
    "password.*=.*['\"].*['\"]"
    "api[_-]?key.*=.*['\"].*['\"]"
    "secret.*=.*['\"].*['\"]"
    "token.*=.*['\"].*['\"]"
    "private[_-]?key"
    "AWS_SECRET"
    "postgresql://.*:.*@"
)

for pattern in "${PATTERNS[@]}"; do
    if git diff --cached --name-only | xargs grep -l "$pattern" 2>/dev/null; then
        echo -e "${RED}❌ Mögliches Secret gefunden: $pattern${NC}"
        git diff --cached --name-only | xargs grep -n "$pattern" || true
        ((SECRETS_FOUND++))
    fi
done

if [ $SECRETS_FOUND -eq 0 ]; then
    echo -e "${GREEN}✓ Keine Secrets gefunden${NC}"
else
    echo -e "${RED}❌ $SECRETS_FOUND mögliche Secrets gefunden!${NC}"
    ((ERRORS++))
fi
echo ""

# 3. Test-Status
echo "🧪 Prüfe Test-Status..."

# Frontend Tests
if [ -d "frontend" ]; then
    echo "   Frontend Tests..."
    cd frontend
    if npm test -- --run 2>&1 | grep -q "failed"; then
        echo -e "${RED}❌ Frontend Tests fehlgeschlagen${NC}"
        ((ERRORS++))
    else
        echo -e "${GREEN}✓ Frontend Tests bestanden${NC}"
    fi
    cd ..
fi

# Backend Tests
if [ -d "backend" ]; then
    echo "   Backend Tests..."
    cd backend
    if ./mvnw test -q 2>&1 | grep -q "FAILURE"; then
        echo -e "${RED}❌ Backend Tests fehlgeschlagen${NC}"
        ((ERRORS++))
    else
        echo -e "${GREEN}✓ Backend Tests bestanden${NC}"
    fi
    cd ..
fi
echo ""

# 4. Linter Check
echo "🎨 Prüfe Code-Qualität..."

# Frontend Linter
if [ -f "frontend/package.json" ]; then
    cd frontend
    if npm run lint 2>&1 | grep -q "error"; then
        echo -e "${YELLOW}⚠ Frontend Linter Warnungen${NC}"
        ((WARNINGS++))
    else
        echo -e "${GREEN}✓ Frontend Linter zufrieden${NC}"
    fi
    cd ..
fi
echo ""

# 5. Security Audit
echo "🛡️  Security Audit..."
if [ -f "frontend/package.json" ]; then
    cd frontend
    VULNS=$(npm audit --json 2>/dev/null | jq '.metadata.vulnerabilities.high + .metadata.vulnerabilities.critical' 2>/dev/null || echo "0")
    if [ "$VULNS" -gt 0 ]; then
        echo -e "${RED}❌ $VULNS kritische Sicherheitslücken gefunden!${NC}"
        npm audit
        ((ERRORS++))
    else
        echo -e "${GREEN}✓ Keine kritischen Sicherheitslücken${NC}"
    fi
    cd ..
fi
echo ""

# 6. Dokumentations-Check
echo "📚 Prüfe Dokumentation..."
if git diff --cached --name-only | grep -E "\.(java|ts|tsx|js)$" > /dev/null; then
    # Code wurde geändert - prüfe ob auch Docs updated wurden
    if ! git diff --cached --name-only | grep -E "\.(md)$" > /dev/null; then
        echo -e "${YELLOW}⚠ Code geändert aber keine Dokumentation aktualisiert${NC}"
        ((WARNINGS++))
    else
        echo -e "${GREEN}✓ Dokumentation wurde aktualisiert${NC}"
    fi
else
    echo -e "${GREEN}✓ Keine Code-Änderungen${NC}"
fi
echo ""

# 7. Commit Message Check
echo "💬 Prüfe Commit-Nachrichten..."
LAST_COMMIT=$(git log -1 --pretty=%B)
if [[ ! "$LAST_COMMIT" =~ ^(feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert): ]]; then
    echo -e "${YELLOW}⚠ Commit-Message folgt nicht Conventional Commits${NC}"
    echo "   Sollte beginnen mit: feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert"
    ((WARNINGS++))
else
    echo -e "${GREEN}✓ Commit-Message Format korrekt${NC}"
fi
echo ""

# 8. File Size Check
echo "📦 Prüfe Dateigrößen..."
LARGE_FILES=$(git diff --cached --name-only | xargs -I {} find {} -size +1M 2>/dev/null | wc -l || echo 0)
if [ "$LARGE_FILES" -gt 0 ]; then
    echo -e "${YELLOW}⚠ $LARGE_FILES große Dateien (>1MB) gefunden${NC}"
    git diff --cached --name-only | xargs -I {} find {} -size +1M -exec ls -lh {} \; 2>/dev/null || true
    ((WARNINGS++))
else
    echo -e "${GREEN}✓ Keine großen Dateien${NC}"
fi

# Zusammenfassung
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ $ERRORS -gt 0 ]; then
    echo -e "${RED}❌ PUSH BLOCKIERT: $ERRORS kritische Fehler gefunden!${NC}"
    echo ""
    echo "Bitte behebe die Fehler und führe den Check erneut aus."
    exit 1
elif [ $WARNINGS -gt 0 ]; then
    echo -e "${YELLOW}⚠️  PUSH MÖGLICH: $WARNINGS Warnungen gefunden${NC}"
    echo ""
    echo "Überlege, ob du die Warnungen beheben möchtest."
    echo ""
    read -p "Trotzdem pushen? (j/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Jj]$ ]]; then
        echo "Push abgebrochen."
        exit 1
    fi
else
    echo -e "${GREEN}✅ ALLES GRÜN: Bereit zum Push!${NC}"
    echo ""
    echo "Du kannst jetzt sicher pushen."
fi

echo ""
echo "💡 Tipp: Führe diesen Check IMMER vor 'git push' aus!"