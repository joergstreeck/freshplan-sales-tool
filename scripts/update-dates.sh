#!/bin/bash
# Einfacher Wrapper für das Datum-Update Script

# Farben für Output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}📅 FreshPlan Dokumentations-Datum Update${NC}"
echo -e "${BLUE}=====================================\n${NC}"

# Prüfe ob Node.js installiert ist
if ! command -v node &> /dev/null; then
    echo "❌ Node.js ist nicht installiert. Bitte installieren Sie Node.js."
    exit 1
fi

# Führe das Script aus
if [ "$1" == "--help" ] || [ "$1" == "-h" ]; then
    node scripts/update-doc-dates.js --help
elif [ "$1" == "--dry-run" ]; then
    echo -e "${BLUE}🔍 Testlauf - keine Dateien werden geändert${NC}\n"
    node scripts/update-doc-dates.js --dry-run
else
    echo -e "${GREEN}✨ Aktualisiere Datums-Angaben in allen Markdown-Dokumenten...${NC}\n"
    node scripts/update-doc-dates.js "$@"
    echo -e "\n${GREEN}✅ Alle Dokumente wurden aktualisiert!${NC}"
fi