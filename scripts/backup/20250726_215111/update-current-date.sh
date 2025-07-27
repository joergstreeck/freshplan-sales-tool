#!/bin/bash
# Aktualisiert NUR die AUTO_DATE Marker (nicht historische Daten!)

# Farben für Output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}📅 FreshPlan Current Date Update${NC}"
echo -e "${BLUE}================================${NC}"
echo -e "${YELLOW}ℹ️  Aktualisiert NUR Marker wie <!-- AUTO_DATE -->${NC}"
echo -e "${YELLOW}   Historische Daten bleiben unverändert!${NC}\n"

# Prüfe ob Node.js installiert ist
if ! command -v node &> /dev/null; then
    echo "❌ Node.js ist nicht installiert. Bitte installieren Sie Node.js."
    exit 1
fi

# Führe das Script aus
if [ "$1" == "--help" ] || [ "$1" == "-h" ]; then
    node scripts/update-current-date.js --help
else
    node scripts/update-current-date.js "$@"
fi