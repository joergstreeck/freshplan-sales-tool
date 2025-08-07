#!/bin/bash

# Script zur Analyse von Migration-Abhängigkeiten

MIGRATION_DIR="src/main/resources/db/migration"

# Farben
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔍 Migration Dependency Analyzer${NC}"
echo "=================================="

# Finde alle CREATE TABLE Statements
echo -e "\n${YELLOW}📊 CREATE TABLE Statements:${NC}"
grep -n "CREATE TABLE" $MIGRATION_DIR/V*.sql | while read line; do
    FILE=$(echo "$line" | cut -d: -f1)
    VERSION=$(basename "$FILE" | sed 's/V\([0-9]*\)__.*/\1/')
    TABLE=$(echo "$line" | grep -oE "CREATE TABLE (IF NOT EXISTS )?(\w+)" | awk '{print $NF}')
    printf "V%-3s: %-30s in %s\n" "$VERSION" "$TABLE" "$(basename $FILE)"
done | sort -k1,1n

# Finde problematische Referenzen
echo -e "\n${RED}❌ Problematische Referenzen (Tabelle wird später erstellt):${NC}"

# Speichere CREATE TABLE info
declare -A table_versions
while IFS= read -r line; do
    FILE=$(echo "$line" | cut -d: -f1)
    VERSION=$(basename "$FILE" | sed 's/V\([0-9]*\)__.*/\1/')
    TABLE=$(echo "$line" | grep -oE "CREATE TABLE (IF NOT EXISTS )?(\w+)" | awk '{print $NF}')
    table_versions[$TABLE]=$VERSION
done < <(grep -n "CREATE TABLE" $MIGRATION_DIR/V*.sql)

# Prüfe ALTER TABLE und REFERENCES
for file in $MIGRATION_DIR/V*.sql; do
    VERSION=$(basename "$file" | sed 's/V\([0-9]*\)__.*/\1/')
    
    # Suche nach ALTER TABLE
    grep -E "ALTER TABLE|REFERENCES|JOIN|FROM" "$file" | grep -v "CREATE TABLE" | while read line; do
        # Extrahiere Tabellennamen
        TABLES=$(echo "$line" | grep -oE "(ALTER TABLE|REFERENCES|JOIN|FROM)\s+(\w+)" | awk '{print $2}' | sort -u)
        
        for TABLE in $TABLES; do
            if [ -n "${table_versions[$TABLE]}" ]; then
                CREATE_VERSION=${table_versions[$TABLE]}
                if [ "$VERSION" -lt "$CREATE_VERSION" ]; then
                    echo -e "${RED}V$VERSION references '$TABLE' which is created in V$CREATE_VERSION${NC}"
                    echo "  File: $(basename $file)"
                    echo "  Line: $line"
                    echo ""
                fi
            fi
        done
    done
done

# Spezielle Prüfung für contacts Tabelle
echo -e "\n${YELLOW}🔍 Spezielle Analyse für 'contacts' Tabelle:${NC}"
echo -e "Erstellt in: V${table_versions[contacts]} (V113__create_contacts_table.sql)"
echo -e "\nReferenziert in folgenden früheren Versionen:"
grep -l "contacts" $MIGRATION_DIR/V*.sql | while read file; do
    VERSION=$(basename "$file" | sed 's/V\([0-9]*\)__.*/\1/')
    if [ "$VERSION" -lt "113" ]; then
        echo -e "${RED}  V$VERSION: $(basename $file)${NC}"
    fi
done | sort -k1,1n

echo -e "\n${YELLOW}💡 Empfehlung:${NC}"
echo "1. Nutze './scripts/fix-migration-order.sh' um die Reihenfolge zu korrigieren"
echo "2. Oder verschiebe problematische Migrationen manuell nach V113+"
echo "3. Führe dann 'mvn clean compile' aus"