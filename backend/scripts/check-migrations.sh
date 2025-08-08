#!/bin/bash

# Script zur Überprüfung von Flyway-Migrationen auf Konflikte und Probleme

MIGRATION_DIR="src/main/resources/db/migration"

# Farben
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔍 FreshPlan Migration Checker${NC}"
echo "================================"

# Prüfe ob Migration-Verzeichnis existiert
if [ ! -d "$MIGRATION_DIR" ]; then
    echo -e "${RED}❌ Migration directory not found: $MIGRATION_DIR${NC}"
    exit 1
fi

echo -e "\n${YELLOW}📊 Analysiere Migrationen...${NC}"

# 1. Prüfe auf doppelte Versionsnummern
echo -e "\n${YELLOW}1️⃣ Prüfe auf doppelte Versionsnummern...${NC}"
DUPLICATES=$(ls $MIGRATION_DIR/V*.sql | sed 's/.*V\([0-9]*\)__.*/\1/' | sort | uniq -d)

if [ -z "$DUPLICATES" ]; then
    echo -e "${GREEN}✅ Keine doppelten Versionsnummern gefunden${NC}"
else
    echo -e "${RED}❌ FEHLER: Doppelte Versionsnummern gefunden:${NC}"
    for DUP in $DUPLICATES; do
        echo -e "${RED}   Version V${DUP}:${NC}"
        ls $MIGRATION_DIR/V${DUP}__*.sql | xargs -I {} basename {}
    done
    ERRORS_FOUND=1
fi

# 2. Prüfe auf Lücken in der Nummerierung
echo -e "\n${YELLOW}2️⃣ Prüfe auf Lücken in der Nummerierung...${NC}"
VERSIONS=$(ls $MIGRATION_DIR/V*.sql | sed 's/.*V\([0-9]*\)__.*/\1/' | sort -n)
PREV=0
GAPS=""

for V in $VERSIONS; do
    if [ $((PREV + 1)) -lt $V ]; then
        GAPS="$GAPS $((PREV + 1))-$((V - 1))"
    fi
    PREV=$V
done

if [ -z "$GAPS" ]; then
    echo -e "${GREEN}✅ Keine Lücken in der Nummerierung${NC}"
else
    echo -e "${YELLOW}⚠️  Warnung: Lücken in der Nummerierung gefunden:${NC}"
    echo -e "${YELLOW}   Fehlende Versionen: $GAPS${NC}"
fi

# 3. Zeige Übersicht aller Migrationen
echo -e "\n${YELLOW}3️⃣ Übersicht aller Migrationen:${NC}"
echo -e "${BLUE}Version | Dateiname${NC}"
echo "---------|----------"

ls $MIGRATION_DIR/V*.sql | sort -V | while read file; do
    VERSION=$(basename "$file" | sed 's/V\([0-9]*\)__.*/\1/')
    FILENAME=$(basename "$file")
    printf "V%-7s | %s\n" "$VERSION" "$FILENAME"
done

# 4. Statistiken
echo -e "\n${YELLOW}4️⃣ Statistiken:${NC}"
TOTAL=$(ls $MIGRATION_DIR/V*.sql 2>/dev/null | wc -l)
HIGHEST=$(ls $MIGRATION_DIR/V*.sql 2>/dev/null | sed 's/.*V\([0-9]*\)__.*/\1/' | sort -n | tail -1)
echo -e "Anzahl Migrationen: ${GREEN}$TOTAL${NC}"
echo -e "Höchste Version: ${GREEN}V$HIGHEST${NC}"
echo -e "Nächste freie Version: ${GREEN}V$((HIGHEST + 1))${NC}"

# 5. Prüfe auf verdächtige Patterns
echo -e "\n${YELLOW}5️⃣ Prüfe auf verdächtige Patterns...${NC}"

# Suche nach TODO-Kommentaren
TODO_COUNT=$(grep -l "TODO" $MIGRATION_DIR/V*.sql 2>/dev/null | wc -l)
if [ $TODO_COUNT -gt 0 ]; then
    echo -e "${YELLOW}⚠️  $TODO_COUNT Migrationen mit TODO-Kommentaren:${NC}"
    grep -l "TODO" $MIGRATION_DIR/V*.sql | xargs -I {} basename {}
fi

# Suche nach DROP Statements
DROP_COUNT=$(grep -il "DROP" $MIGRATION_DIR/V*.sql 2>/dev/null | wc -l)
if [ $DROP_COUNT -gt 0 ]; then
    echo -e "${YELLOW}⚠️  $DROP_COUNT Migrationen mit DROP Statements (destruktiv!):${NC}"
    grep -il "DROP" $MIGRATION_DIR/V*.sql | xargs -I {} basename {}
fi

# 6. Letzte Änderungen
echo -e "\n${YELLOW}6️⃣ Zuletzt geänderte Migrationen:${NC}"
ls -lt $MIGRATION_DIR/V*.sql | head -5 | awk '{print $6, $7, $8, $9}'

# Zusammenfassung
echo -e "\n${BLUE}📋 Zusammenfassung:${NC}"
if [ -z "$ERRORS_FOUND" ]; then
    echo -e "${GREEN}✅ Keine kritischen Probleme gefunden!${NC}"
    echo -e "${YELLOW}💡 Tipp: Nutze './scripts/create-migration.sh' für neue Migrationen${NC}"
else
    echo -e "${RED}❌ Es wurden Probleme gefunden! Bitte behebe diese vor dem nächsten Deploy.${NC}"
    exit 1
fi