#!/bin/bash

# FreshPlan Komplettes Dokumentations-Update
# Aktualisiert alle Dokumentationen, Dashboards und Links

echo "🚀 FreshPlan Dokumentations-Update gestartet..."

# Farben
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# 1. AUTO_DATE Marker aktualisieren
echo ""
echo "📅 Aktualisiere Datums-Marker..."
./scripts/update-current-date.sh
echo -e "${GREEN}✓ Datums-Marker aktualisiert${NC}"

# 2. Dashboard Update
echo ""
echo "📊 Aktualisiere Team Dashboard..."
if [ -f "./update-dashboard.sh" ]; then
    ./update-dashboard.sh
else
    echo -e "${YELLOW}⚠ Dashboard-Script nicht gefunden${NC}"
fi

# 3. Dokumentations-Links überprüfen
echo ""
echo "🔗 Überprüfe Dokumentations-Links..."
./scripts/check-doc-links.sh

# 4. Git-Status für Änderungen zeigen
echo ""
echo "📝 Geänderte Dateien:"
git status --short | grep -E "\.(md|html)$"

# 5. Optional: Changelog generieren für wichtige Änderungen
echo ""
echo "📋 Wichtige Änderungen seit gestern:"
git log --since="1 day ago" --pretty=format:"- %s" --grep="feat\|fix\|breaking" | head -10

echo ""
echo -e "${GREEN}✅ Dokumentations-Update abgeschlossen!${NC}"
echo ""
echo "💡 Tipp: Führe dieses Script täglich aus oder integriere es in den CI/CD Workflow"