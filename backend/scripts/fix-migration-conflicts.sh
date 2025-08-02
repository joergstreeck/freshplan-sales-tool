#!/bin/bash

# Script zum automatischen Beheben von Migration-Konflikten

MIGRATION_DIR="src/main/resources/db/migration"

# Farben
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${RED}🔧 FreshPlan Migration Conflict Fixer${NC}"
echo "====================================="
echo -e "${YELLOW}⚠️  WARNUNG: Dieses Script ändert Dateinamen!${NC}"
echo -e "${YELLOW}   Stelle sicher, dass alle Änderungen committed sind.${NC}"
echo ""
read -p "Fortfahren? (y/n): " CONFIRM

if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
    echo "Abgebrochen."
    exit 0
fi

# Backup erstellen
BACKUP_DIR="migration_backup_$(date +%Y%m%d_%H%M%S)"
echo -e "\n${BLUE}📦 Erstelle Backup in $BACKUP_DIR...${NC}"
mkdir -p "$BACKUP_DIR"
cp -r "$MIGRATION_DIR" "$BACKUP_DIR/"
echo -e "${GREEN}✅ Backup erstellt${NC}"

# Finde doppelte Versionen
echo -e "\n${YELLOW}🔍 Suche nach Konflikten...${NC}"
DUPLICATES=$(ls $MIGRATION_DIR/V*.sql | sed 's/.*V\([0-9]*\)__.*/\1/' | sort | uniq -d)

if [ -z "$DUPLICATES" ]; then
    echo -e "${GREEN}✅ Keine Konflikte gefunden!${NC}"
    exit 0
fi

# Zeige Konflikte
echo -e "${RED}❌ Konflikte gefunden:${NC}"
for DUP in $DUPLICATES; do
    echo -e "\n${YELLOW}Version V${DUP}:${NC}"
    ls -la $MIGRATION_DIR/V${DUP}__*.sql | awk '{print "  ", $9}'
done

# Finde höchste Version
HIGHEST=$(ls $MIGRATION_DIR/V*.sql | sed 's/.*V\([0-9]*\)__.*/\1/' | sort -n | tail -1)
NEXT_VERSION=$((HIGHEST + 1))

echo -e "\n${BLUE}📊 Höchste Version: V${HIGHEST}${NC}"
echo -e "${BLUE}📊 Nächste freie Version startet bei: V${NEXT_VERSION}${NC}"

# Behebe Konflikte
echo -e "\n${YELLOW}🔧 Behebe Konflikte...${NC}"
for DUP in $DUPLICATES; do
    FILES=$(ls $MIGRATION_DIR/V${DUP}__*.sql | sort)
    FILE_COUNT=$(echo "$FILES" | wc -l)
    
    echo -e "\n${YELLOW}Bearbeite V${DUP} ($FILE_COUNT Dateien)...${NC}"
    
    # Erste Datei behält die Nummer
    FIRST_FILE=$(echo "$FILES" | head -1)
    echo -e "${GREEN}✅ Behalte: $(basename $FIRST_FILE)${NC}"
    
    # Andere Dateien bekommen neue Nummern
    echo "$FILES" | tail -n +2 | while read FILE; do
        BASENAME=$(basename "$FILE")
        SUFFIX=$(echo "$BASENAME" | sed 's/V[0-9]*__//')
        NEW_NAME="V${NEXT_VERSION}__${SUFFIX}"
        NEW_PATH="$MIGRATION_DIR/$NEW_NAME"
        
        echo -e "${YELLOW}📝 Benenne um:${NC}"
        echo -e "   Alt: $BASENAME"
        echo -e "   Neu: $NEW_NAME"
        
        # Ändere auch den Kommentar in der Datei
        sed -i.bak "s/-- V${DUP}:/-- V${NEXT_VERSION}:/" "$FILE"
        rm "${FILE}.bak"
        
        # Benenne Datei um
        mv "$FILE" "$NEW_PATH"
        
        NEXT_VERSION=$((NEXT_VERSION + 1))
    done
done

# Zeige Ergebnis
echo -e "\n${GREEN}✅ Konflikte behoben!${NC}"
echo -e "\n${BLUE}📋 Neue Migration-Liste:${NC}"
./scripts/check-migrations.sh | grep -A100 "Version | Dateiname" | head -20

echo -e "\n${YELLOW}💡 Nächste Schritte:${NC}"
echo "1. Prüfe die Änderungen mit: git status"
echo "2. Teste die Migrationen mit: mvn clean compile"
echo "3. Committe die Änderungen"
echo ""
echo -e "${GREEN}✨ Fertig!${NC}"