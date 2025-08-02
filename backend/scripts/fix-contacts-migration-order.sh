#!/bin/bash

# Script zum Beheben der contacts-Tabellen-Abhängigkeiten

MIGRATION_DIR="src/main/resources/db/migration"

# Farben
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${RED}🔧 Contacts Migration Order Fixer${NC}"
echo "==================================="
echo -e "${YELLOW}⚠️  Dieses Script verschiebt alle Migrationen, die 'contacts' referenzieren,${NC}"
echo -e "${YELLOW}   nach V113 (wo contacts erstellt wird).${NC}"
echo ""

# Backup erstellen
BACKUP_DIR="migration_backup_contacts_$(date +%Y%m%d_%H%M%S)"
echo -e "${BLUE}📦 Erstelle Backup in $BACKUP_DIR...${NC}"
mkdir -p "$BACKUP_DIR"
cp -r "$MIGRATION_DIR" "$BACKUP_DIR/"
echo -e "${GREEN}✅ Backup erstellt${NC}"

# Finde höchste Version
HIGHEST=$(ls $MIGRATION_DIR/V*.sql | sed 's/.*V\([0-9]*\)__.*/\1/' | sort -n | tail -1)
NEXT_VERSION=$((HIGHEST + 1))

echo -e "\n${BLUE}📊 Aktuelle höchste Version: V${HIGHEST}${NC}"

# Finde Migrationen die contacts referenzieren aber vor V113 sind
echo -e "\n${YELLOW}🔍 Suche problematische Migrationen...${NC}"

MIGRATIONS_TO_MOVE=()
for file in $MIGRATION_DIR/V*.sql; do
    VERSION=$(basename "$file" | sed 's/V\([0-9]*\)__.*/\1/')
    
    # Skip V113 selbst und alles danach
    if [ "$VERSION" -ge "113" ]; then
        continue
    fi
    
    # Prüfe ob die Datei "contacts" referenziert
    if grep -q "contacts" "$file"; then
        # Aber ignoriere customer_contacts (das ist eine andere Tabelle)
        if grep -v "customer_contacts" "$file" | grep -q "contacts"; then
            MIGRATIONS_TO_MOVE+=("$file")
            echo -e "${RED}  V$VERSION: $(basename $file)${NC}"
        fi
    fi
done

if [ ${#MIGRATIONS_TO_MOVE[@]} -eq 0 ]; then
    echo -e "${GREEN}✅ Keine problematischen Migrationen gefunden!${NC}"
    exit 0
fi

echo -e "\n${YELLOW}📝 Verschiebe ${#MIGRATIONS_TO_MOVE[@]} Migrationen...${NC}"

# Verschiebe die Migrationen
for file in "${MIGRATIONS_TO_MOVE[@]}"; do
    BASENAME=$(basename "$file")
    SUFFIX=$(echo "$BASENAME" | sed 's/V[0-9]*__//')
    OLD_VERSION=$(echo "$BASENAME" | sed 's/V\([0-9]*\)__.*/\1/')
    NEW_NAME="V${NEXT_VERSION}__${SUFFIX}"
    NEW_PATH="$MIGRATION_DIR/$NEW_NAME"
    
    echo -e "\n${YELLOW}📝 Verschiebe:${NC}"
    echo -e "   Von: $BASENAME (V$OLD_VERSION)"
    echo -e "   Nach: $NEW_NAME (V$NEXT_VERSION)"
    
    # Ändere auch den Versions-Kommentar in der Datei
    sed -i.bak "s/-- V${OLD_VERSION}:/-- V${NEXT_VERSION}:/" "$file"
    rm "${file}.bak"
    
    # Benenne Datei um
    mv "$file" "$NEW_PATH"
    
    NEXT_VERSION=$((NEXT_VERSION + 1))
done

echo -e "\n${GREEN}✅ Migrationen erfolgreich verschoben!${NC}"

# Zeige neue Reihenfolge
echo -e "\n${BLUE}📋 Neue Migration-Reihenfolge für contacts-bezogene Dateien:${NC}"
ls $MIGRATION_DIR/V*.sql | grep -E "(V113|contacts)" | sort -V | while read file; do
    VERSION=$(basename "$file" | sed 's/V\([0-9]*\)__.*/\1/')
    printf "V%-3s: %s\n" "$VERSION" "$(basename $file)"
done

echo -e "\n${YELLOW}💡 Nächste Schritte:${NC}"
echo "1. Führe 'mvn clean compile' aus"
echo "2. Teste mit 'mvn test'"
echo "3. Committe die Änderungen"

echo -e "\n${GREEN}✨ Fertig!${NC}"