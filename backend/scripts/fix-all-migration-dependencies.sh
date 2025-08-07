#!/bin/bash

# Umfassendes Script zum Beheben ALLER Migration-Abhängigkeiten

MIGRATION_DIR="src/main/resources/db/migration"

# Farben
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${RED}🔧 Complete Migration Dependency Fixer${NC}"
echo "======================================="

# Backup
BACKUP_DIR="migration_backup_complete_$(date +%Y%m%d_%H%M%S)"
echo -e "${BLUE}📦 Erstelle Backup in $BACKUP_DIR...${NC}"
mkdir -p "$BACKUP_DIR"
cp -r "$MIGRATION_DIR" "$BACKUP_DIR/"
echo -e "${GREEN}✅ Backup erstellt${NC}"

# Phase 1: Sammle alle CREATE TABLE Informationen
echo -e "\n${YELLOW}📊 Phase 1: Analysiere CREATE TABLE Statements...${NC}"
declare -A table_create_versions

while IFS= read -r line; do
    FILE=$(echo "$line" | cut -d: -f1)
    VERSION=$(basename "$FILE" | sed 's/V\([0-9]*\)__.*/\1/')
    TABLE=$(echo "$line" | grep -oE "CREATE TABLE (IF NOT EXISTS )?([a-zA-Z_]+)" | awk '{print $NF}')
    
    if [ -n "$TABLE" ] && [ "$TABLE" != "TABLE" ] && [ "$TABLE" != "IF" ] && [ "$TABLE" != "NOT" ] && [ "$TABLE" != "EXISTS" ]; then
        table_create_versions[$TABLE]=$VERSION
        echo "  Tabelle '$TABLE' wird erstellt in V$VERSION"
    fi
done < <(grep -n "CREATE TABLE" $MIGRATION_DIR/V*.sql)

# Phase 2: Finde alle Abhängigkeiten
echo -e "\n${YELLOW}📊 Phase 2: Finde Abhängigkeiten...${NC}"
declare -A migration_dependencies

for file in $MIGRATION_DIR/V*.sql; do
    VERSION=$(basename "$file" | sed 's/V\([0-9]*\)__.*/\1/')
    DEPS=""
    
    # Suche nach ALTER TABLE, REFERENCES, etc.
    while IFS= read -r line; do
        # Suche nach Tabellennamen nach bestimmten Keywords
        TABLES=$(echo "$line" | grep -oE "(ALTER TABLE|REFERENCES|JOIN|FROM|UPDATE|INSERT INTO|DELETE FROM)\s+([a-zA-Z_]+)" | awk '{print $2}' | sort -u)
        
        for TABLE in $TABLES; do
            # Prüfe ob diese Tabelle in einer späteren Migration erstellt wird
            if [ -n "${table_create_versions[$TABLE]}" ]; then
                CREATE_VERSION=${table_create_versions[$TABLE]}
                if [ "$VERSION" -lt "$CREATE_VERSION" ]; then
                    DEPS="$DEPS $TABLE:V$CREATE_VERSION"
                fi
            fi
        done
    done < <(grep -E "ALTER TABLE|REFERENCES|JOIN|FROM|UPDATE|INSERT INTO|DELETE FROM" "$file" | grep -v "CREATE TABLE")
    
    if [ -n "$DEPS" ]; then
        migration_dependencies[$VERSION]="$DEPS"
        echo -e "${RED}  V$VERSION hat Abhängigkeiten zu: $DEPS${NC}"
    fi
done

# Phase 3: Bestimme neue Reihenfolge
echo -e "\n${YELLOW}📊 Phase 3: Berechne neue Reihenfolge...${NC}"

# Finde höchste aktuelle Version
HIGHEST=$(ls $MIGRATION_DIR/V*.sql | sed 's/.*V\([0-9]*\)__.*/\1/' | sort -n | tail -1)
NEXT_VERSION=$((HIGHEST + 1))

# Verschiebe problematische Migrationen
echo -e "\n${YELLOW}📝 Phase 4: Verschiebe Migrationen...${NC}"

for VERSION in "${!migration_dependencies[@]}"; do
    FILE=$(ls $MIGRATION_DIR/V${VERSION}__*.sql 2>/dev/null | head -1)
    
    if [ -n "$FILE" ]; then
        BASENAME=$(basename "$FILE")
        SUFFIX=$(echo "$BASENAME" | sed 's/V[0-9]*__//')
        NEW_NAME="V${NEXT_VERSION}__${SUFFIX}"
        NEW_PATH="$MIGRATION_DIR/$NEW_NAME"
        
        echo -e "\n${YELLOW}📝 Verschiebe:${NC}"
        echo -e "   Von: $BASENAME (V$VERSION)"
        echo -e "   Nach: $NEW_NAME (V$NEXT_VERSION)"
        echo -e "   Grund: ${migration_dependencies[$VERSION]}"
        
        # Update Version im Kommentar
        sed -i.bak "s/-- V${VERSION}:/-- V${NEXT_VERSION}:/" "$FILE"
        sed -i.bak "s/Migration V${VERSION}:/Migration V${NEXT_VERSION}:/" "$FILE"
        rm "${FILE}.bak"
        
        # Benenne Datei um
        mv "$FILE" "$NEW_PATH"
        
        NEXT_VERSION=$((NEXT_VERSION + 1))
    fi
done

# Phase 5: Finale Überprüfung
echo -e "\n${GREEN}✅ Migrationen verschoben!${NC}"
echo -e "\n${BLUE}📋 Führe finale Überprüfung durch...${NC}"

# Prüfe ob noch Probleme existieren
PROBLEMS=0
for file in $MIGRATION_DIR/V*.sql; do
    VERSION=$(basename "$file" | sed 's/V\([0-9]*\)__.*/\1/')
    
    while IFS= read -r line; do
        TABLES=$(echo "$line" | grep -oE "(ALTER TABLE|REFERENCES|JOIN|FROM)\s+([a-zA-Z_]+)" | awk '{print $2}' | sort -u)
        
        for TABLE in $TABLES; do
            if [ -n "${table_create_versions[$TABLE]}" ]; then
                CREATE_VERSION=${table_create_versions[$TABLE]}
                if [ "$VERSION" -lt "$CREATE_VERSION" ]; then
                    echo -e "${RED}⚠️  PROBLEM: V$VERSION referenziert '$TABLE' (erstellt in V$CREATE_VERSION)${NC}"
                    PROBLEMS=$((PROBLEMS + 1))
                fi
            fi
        done
    done < <(grep -E "ALTER TABLE|REFERENCES|JOIN|FROM" "$file" | grep -v "CREATE TABLE")
done

if [ $PROBLEMS -eq 0 ]; then
    echo -e "${GREEN}✅ Keine Abhängigkeitsprobleme mehr gefunden!${NC}"
else
    echo -e "${RED}❌ Es gibt noch $PROBLEMS Abhängigkeitsprobleme!${NC}"
fi

echo -e "\n${YELLOW}💡 Nächste Schritte:${NC}"
echo "1. Führe 'mvn clean compile' aus"
echo "2. Teste mit 'mvn test'"
echo "3. Bei Erfolg: Committe die Änderungen"

echo -e "\n${GREEN}✨ Fertig!${NC}"