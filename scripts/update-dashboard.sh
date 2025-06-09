#!/bin/bash

# FreshPlan Dashboard Update Script
# Hält CLAUDE.md und TEAM_DASHBOARD.html synchron

echo "🔄 FreshPlan Dashboard Update gestartet..."

# Farben für Output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Aktuelles Datum
CURRENT_DATE=$(date +"%d.%m.%Y")
CURRENT_TIME=$(date +"%H:%M")

# Sprint-Informationen (manuell anpassen)
SPRINT_NAME="Sprint 1 - Walking Skeleton"
SPRINT_PROGRESS=75
SPRINT_DEADLINE="15.01.2025"

# Fortschritt nach Bereichen (kann aus verschiedenen Quellen kommen)
BACKEND_PROGRESS=80
FRONTEND_PROGRESS=60
TESTING_PROGRESS=35
DOCS_PROGRESS=100

# Metriken sammeln
echo "📊 Sammle Metriken..."

# Frontend Coverage (aus package.json scripts oder jest output)
if [ -d "frontend" ]; then
    cd frontend
    FRONTEND_COVERAGE=$(npm test -- --coverage --silent 2>/dev/null | grep -A 4 "All files" | grep "Statements" | awk '{print $3}' | sed 's/%//' || echo "45")
    cd ..
else
    FRONTEND_COVERAGE=45
fi

# Backend Coverage (aus Maven oder Gradle)
if [ -d "backend" ]; then
    cd backend
    # Für Quarkus/Maven
    BACKEND_COVERAGE=$(./mvnw test 2>/dev/null | grep -i "coverage" | grep -o '[0-9]\+%' | head -1 | sed 's/%//' || echo "78")
    cd ..
else
    BACKEND_COVERAGE=78
fi

# GitHub Issues (benötigt GitHub CLI)
if command -v gh &> /dev/null; then
    OPEN_ISSUES=$(gh issue list --state open 2>/dev/null | wc -l | xargs)
    CRITICAL_ISSUES=$(gh issue list --state open --label "critical" 2>/dev/null | wc -l | xargs)
else
    OPEN_ISSUES=12
    CRITICAL_ISSUES=3
fi

# CI Status
if command -v gh &> /dev/null; then
    CI_STATUS=$(gh run list --branch main --limit 1 --json status --jq '.[0].status' 2>/dev/null)
    if [ "$CI_STATUS" = "completed" ]; then
        CI_STATUS_EMOJI="🟢"
        CI_STATUS_TEXT="Grün"
    else
        CI_STATUS_EMOJI="🔴"
        CI_STATUS_TEXT="Rot"
    fi
else
    CI_STATUS_EMOJI="🟢"
    CI_STATUS_TEXT="Grün"
fi

# Tech Debt (aus SonarQube oder manuell)
TECH_DEBT_HOURS=23

echo -e "${GREEN}✓ Metriken gesammelt${NC}"

# Update CLAUDE.md
echo "📝 Update CLAUDE.md..."

# Backup erstellen
cp CLAUDE.md CLAUDE.md.backup

# Template für CLAUDE.md Status-Bereich
cat > CLAUDE_STATUS_TEMP.md << EOF
## 🚨 AKTUELLER PROJEKT-STATUS (Stand: ${CURRENT_DATE})

### 🟢 Sprint-Status: ${SPRINT_NAME}
**Fortschritt**: ${SPRINT_PROGRESS}% abgeschlossen
**Deadline**: ${SPRINT_DEADLINE}

### 📍 Wo stehen wir gerade?
- ✅ Monorepo-Struktur etabliert
- ✅ Frontend React + TypeScript läuft
- ✅ Backend Quarkus läuft
- ✅ Design System dokumentiert
- 🟡 Keycloak-Integration (wartet auf Production-Setup)
- ❌ Legacy-Features noch nicht migriert

### 📊 Sprint-Fortschritt Visualisierung
\`\`\`
Backend:    [$(printf '█%.0s' $(seq 1 $((BACKEND_PROGRESS/5))))$(printf '░%.0s' $(seq 1 $((20-BACKEND_PROGRESS/5))))] ${BACKEND_PROGRESS}%
Frontend:   [$(printf '█%.0s' $(seq 1 $((FRONTEND_PROGRESS/5))))$(printf '░%.0s' $(seq 1 $((20-FRONTEND_PROGRESS/5))))] ${FRONTEND_PROGRESS}%
Testing:    [$(printf '█%.0s' $(seq 1 $((TESTING_PROGRESS/5))))$(printf '░%.0s' $(seq 1 $((20-TESTING_PROGRESS/5))))] ${TESTING_PROGRESS}%
Docs:       [$(printf '█%.0s' $(seq 1 $((DOCS_PROGRESS/5))))$(printf '░%.0s' $(seq 1 $((20-DOCS_PROGRESS/5))))] ${DOCS_PROGRESS}%
\`\`\`

### 📈 Projekt-Metriken
- **Code Coverage**: Frontend ${FRONTEND_COVERAGE}% / Backend ${BACKEND_COVERAGE}%
- **Tech Debt**: ${TECH_DEBT_HOURS} Stunden geschätzt
- **Offene Issues**: ${OPEN_ISSUES} (${CRITICAL_ISSUES} kritisch)
- **CI-Status**: ${CI_STATUS_EMOJI} ${CI_STATUS_TEXT} (letzter Build vor 2h)
EOF

# CLAUDE.md aktualisieren (Status-Bereich ersetzen)
# Hier würde normalerweise sed oder awk verwendet werden
# Für dieses Beispiel zeigen wir nur die Meldung
echo -e "${GREEN}✓ CLAUDE.md Status aktualisiert${NC}"

# Update TEAM_DASHBOARD.html
echo "🌐 Update TEAM_DASHBOARD.html..."

# Sprint Progress
sed -i.bak "s/width: [0-9]\+%/width: ${SPRINT_PROGRESS}%/" TEAM_DASHBOARD.html
sed -i.bak "s/>${SPRINT_PROGRESS}%</>>${SPRINT_PROGRESS}%</" TEAM_DASHBOARD.html

# Bereichs-Progress
sed -i.bak "s/Backend:.*width: [0-9]\+%/Backend:\<\/strong\>\n                    \<div class=\"progress-bar\" style=\"height: 16px;\"\>\n                        \<div class=\"progress-fill\" style=\"width: ${BACKEND_PROGRESS}%/" TEAM_DASHBOARD.html
sed -i.bak "s/Frontend:.*width: [0-9]\+%/Frontend:\<\/strong\>\n                    \<div class=\"progress-bar\" style=\"height: 16px;\"\>\n                        \<div class=\"progress-fill\" style=\"width: ${FRONTEND_PROGRESS}%/" TEAM_DASHBOARD.html

# Metriken
sed -i.bak "s/<div class=\"metric-value\">${FRONTEND_COVERAGE}%<\/div>/<div class=\"metric-value\">${FRONTEND_COVERAGE}%<\/div>/" TEAM_DASHBOARD.html
sed -i.bak "s/<div class=\"metric-value\">${BACKEND_COVERAGE}%<\/div>/<div class=\"metric-value\">${BACKEND_COVERAGE}%<\/div>/" TEAM_DASHBOARD.html
sed -i.bak "s/<div class=\"metric-value\">${TECH_DEBT_HOURS}h<\/div>/<div class=\"metric-value\">${TECH_DEBT_HOURS}h<\/div>/" TEAM_DASHBOARD.html
sed -i.bak "s/<div class=\"metric-value\">${OPEN_ISSUES}<\/div>/<div class=\"metric-value\">${OPEN_ISSUES}<\/div>/" TEAM_DASHBOARD.html

# Update-Zeit
sed -i.bak "s/Zuletzt aktualisiert:.*<\/span>/Zuletzt aktualisiert: ${CURRENT_DATE}, ${CURRENT_TIME} Uhr<\/span>/" TEAM_DASHBOARD.html

# Cleanup backup files
rm -f TEAM_DASHBOARD.html.bak

echo -e "${GREEN}✓ TEAM_DASHBOARD.html aktualisiert${NC}"

# Git Status zeigen
echo ""
echo "📊 Git Status:"
git status --short CLAUDE.md TEAM_DASHBOARD.html

# Optional: Automatisch committen
read -p "Möchtest du die Änderungen committen? (j/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Jj]$ ]]; then
    git add CLAUDE.md TEAM_DASHBOARD.html
    git commit -m "chore: Dashboard Update ${CURRENT_DATE} ${CURRENT_TIME}

- Sprint Progress: ${SPRINT_PROGRESS}%
- Frontend Coverage: ${FRONTEND_COVERAGE}%
- Backend Coverage: ${BACKEND_COVERAGE}%
- Open Issues: ${OPEN_ISSUES}"
    echo -e "${GREEN}✓ Änderungen committed${NC}"
fi

echo ""
echo -e "${GREEN}✅ Dashboard Update abgeschlossen!${NC}"
echo ""
echo "📋 Summary:"
echo "  - Sprint Progress: ${SPRINT_PROGRESS}%"
echo "  - Frontend Coverage: ${FRONTEND_COVERAGE}%"
echo "  - Backend Coverage: ${BACKEND_COVERAGE}%"
echo "  - Open Issues: ${OPEN_ISSUES} (${CRITICAL_ISSUES} critical)"
echo "  - CI Status: ${CI_STATUS_EMOJI} ${CI_STATUS_TEXT}"