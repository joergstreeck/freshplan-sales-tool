#!/bin/bash

# FreshPlan Master Update Script
# Aktualisiert ALLE Dokumentationen und hält sie konsistent

echo "🚀 FreshPlan Master Documentation Update"
echo "======================================="
echo ""

# Farben
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 1. Datum überall aktualisieren
echo -e "${BLUE}📅 Schritt 1: Datum aktualisieren${NC}"
node scripts/inject-current-date.js
node update-docs-simple.js
echo ""

# 2. Projekt-Status aus Git sammeln
echo -e "${BLUE}📊 Schritt 2: Projekt-Metriken sammeln${NC}"

# Anzahl Commits heute
COMMITS_TODAY=$(git log --since="midnight" --oneline | wc -l | xargs)
echo "Commits heute: $COMMITS_TODAY"

# Geänderte Dateien
CHANGED_FILES=$(git status --porcelain | wc -l | xargs)
echo "Geänderte Dateien: $CHANGED_FILES"

# 3. Developer Dashboard generieren
echo ""
echo -e "${BLUE}🌐 Schritt 3: Developer Dashboard generieren${NC}"
node scripts/generate-developer-dashboard.js

# 4. Änderungen dokumentieren
echo ""
echo -e "${BLUE}📝 Schritt 4: Änderungen dokumentieren${NC}"
if [ -f "./scripts/document-changes.sh" ]; then
    ./scripts/document-changes.sh
fi

# 5. Link-Konsistenz prüfen
echo ""
echo -e "${BLUE}🔗 Schritt 5: Links überprüfen${NC}"
if [ -f "./scripts/check-doc-links.sh" ]; then
    ./scripts/check-doc-links.sh
fi

# 6. Zusammenfassung
echo ""
echo -e "${GREEN}✅ Master Update abgeschlossen!${NC}"
echo ""
echo "📋 Aktualisierte Dokumente:"
echo "  - MASTER_BRIEFING.md (mit aktuellem Datum)"
echo "  - PROJECT_STATE.json"
echo "  - DEVELOPER_DASHBOARD.html"
echo "  - Alle .md Dateien mit AUTO_DATE"
echo ""
echo -e "${YELLOW}💡 Für Claude:${NC}"
echo "  1. Sage ihm: 'Lies MASTER_BRIEFING.md'"
echo "  2. Lasse das Datum bestätigen"
echo "  3. Bei Bedarf weitere Docs anfordern"
echo ""
echo -e "${YELLOW}💡 Für Entwickler:${NC}"
echo "  Öffne DEVELOPER_DASHBOARD.html im Browser"