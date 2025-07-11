#!/bin/bash

# orientation-check.sh
# Hilft Claude bei der Orientierungsphase und signalisiert Bereitschaft für Arbeitsfreigabe

echo "🔍 ORIENTIERUNGS-CHECK"
echo "====================="
echo ""

# Farbdefinitionen
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${YELLOW}📋 Checkliste für Orientierungsphase:${NC}"
echo "====================================="
echo ""

echo -e "${GREEN}✅${NC} CLAUDE.md gelesen?"
echo -e "${GREEN}✅${NC} STANDARDUBERGABE_NEU.md gelesen?"
echo -e "${GREEN}✅${NC} CRM_COMPLETE_MASTER_PLAN.md gelesen?"
echo -e "${GREEN}✅${NC} Aktives Modul mit ./scripts/get-active-module.sh identifiziert?"
echo -e "${GREEN}✅${NC} ⭐-markiertes Modul-Dokument gelesen?"
echo -e "${GREEN}✅${NC} Code-Stand durch Inspektion validiert?"
echo -e "${GREEN}✅${NC} Nächster Schritt aus Dokumentation klar?"
echo -e "${GREEN}✅${NC} Diskrepanzen zwischen Doku und Code geprüft?"
echo ""

echo -e "${BLUE}🎯 Erwartete Meldung an User:${NC}"
echo "=============================="
echo "- Bestätigung des aktiven Moduls (FC-XXX-MX)"
echo "- Bestätigung des nächsten Schritts aus der Dokumentation"
echo "- Eventuelle Diskrepanzen zwischen Doku und Code"
echo "- Status: BEREIT FÜR ARBEITSPHASE"
echo ""

echo -e "${YELLOW}🚦 STATUS: BEREIT FÜR ARBEITSFREIGABE${NC}"
echo -e "${YELLOW}⏳ Warte auf Freigabe vom User...${NC}"
echo ""
echo "💡 Hinweis: Erst nach User-Freigabe mit Implementierung beginnen!"