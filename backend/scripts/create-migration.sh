#!/bin/bash

# Script zur automatischen Erstellung von Flyway-Migrationen mit korrekter Versionsnummer
# Verhindert Versionskonflikte und doppelte Nummern

MIGRATION_DIR="src/main/resources/db/migration"

# Farben für Output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}🔍 FreshPlan Migration Creator${NC}"
echo "================================"

# Prüfe ob Migration-Verzeichnis existiert
if [ ! -d "$MIGRATION_DIR" ]; then
    echo -e "${RED}❌ Migration directory not found: $MIGRATION_DIR${NC}"
    exit 1
fi

# Finde die höchste Versionsnummer
echo -e "${YELLOW}📊 Analysiere existierende Migrationen...${NC}"
HIGHEST_VERSION=$(ls $MIGRATION_DIR/V*.sql 2>/dev/null | sed 's/.*V\([0-9]*\)__.*/\1/' | sort -n | tail -1)

if [ -z "$HIGHEST_VERSION" ]; then
    HIGHEST_VERSION=0
fi

NEXT_VERSION=$((HIGHEST_VERSION + 1))

echo -e "Höchste gefundene Version: ${GREEN}V${HIGHEST_VERSION}${NC}"
echo -e "Nächste verfügbare Version: ${GREEN}V${NEXT_VERSION}${NC}"

# Zeige die letzten 5 Migrationen
echo -e "\n${YELLOW}📝 Letzte 5 Migrationen:${NC}"
ls -la $MIGRATION_DIR/V*.sql | tail -5 | awk '{print $9}' | xargs -I {} basename {}

# Frage nach dem Migrations-Namen
echo -e "\n${YELLOW}📝 Bitte gib einen Namen für die Migration ein:${NC}"
echo "(z.B. 'create_contact_table' oder 'add_user_permissions')"
read -p "Name: " MIGRATION_NAME

# Validiere den Namen
if [ -z "$MIGRATION_NAME" ]; then
    echo -e "${RED}❌ Migration name cannot be empty!${NC}"
    exit 1
fi

# Ersetze Leerzeichen durch Unterstriche
MIGRATION_NAME=$(echo "$MIGRATION_NAME" | tr ' ' '_' | tr '-' '_')

# Erstelle den Dateinamen
FILENAME="V${NEXT_VERSION}__${MIGRATION_NAME}.sql"
FILEPATH="$MIGRATION_DIR/$FILENAME"

# Prüfe ob Datei bereits existiert (sollte nicht passieren, aber sicher ist sicher)
if [ -f "$FILEPATH" ]; then
    echo -e "${RED}❌ File already exists: $FILEPATH${NC}"
    exit 1
fi

# Erstelle die Migration-Datei mit Template
cat > "$FILEPATH" << EOF
-- V${NEXT_VERSION}: ${MIGRATION_NAME}
-- Created: $(date +"%Y-%m-%d %H:%M:%S")
-- Author: FreshPlan Team
-- Description: TODO: Add description here

-- TODO: Add your SQL statements here

EOF

echo -e "${GREEN}✅ Migration erfolgreich erstellt:${NC}"
echo -e "   ${FILEPATH}"
echo -e "\n${YELLOW}💡 Nächste Schritte:${NC}"
echo "1. Öffne die Datei und füge deine SQL-Statements hinzu"
echo "2. Teste die Migration mit: mvn clean compile"
echo "3. Committe die Datei mit: git add $FILEPATH"

# Optional: Öffne die Datei im Editor
echo -e "\n${YELLOW}📝 Möchtest du die Datei jetzt öffnen? (y/n)${NC}"
read -p "Antwort: " OPEN_FILE

if [ "$OPEN_FILE" = "y" ] || [ "$OPEN_FILE" = "Y" ]; then
    # Versuche verschiedene Editoren
    if command -v code &> /dev/null; then
        code "$FILEPATH"
    elif command -v vim &> /dev/null; then
        vim "$FILEPATH"
    elif command -v nano &> /dev/null; then
        nano "$FILEPATH"
    else
        echo -e "${YELLOW}Kein Editor gefunden. Bitte öffne die Datei manuell.${NC}"
    fi
fi

echo -e "\n${GREEN}✨ Fertig!${NC}"