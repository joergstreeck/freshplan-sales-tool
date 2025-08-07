#!/bin/bash

# Script: get-next-migration.sh
# Zweck: Ermittelt die nächste freie Flyway Migration-Nummer
# Autor: FreshPlan Team
# Datum: 2025-08-07

set -e

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Pfad zu Migrations
MIGRATION_DIR="backend/src/main/resources/db/migration"

# Prüfe ob Verzeichnis existiert
if [ ! -d "$MIGRATION_DIR" ]; then
    echo -e "${RED}❌ Migration-Verzeichnis nicht gefunden: $MIGRATION_DIR${NC}"
    exit 1
fi

# Zeige die letzten 5 Migrationen
echo -e "${YELLOW}📋 Letzte 5 Migrationen:${NC}"
ls -la "$MIGRATION_DIR"/*.sql 2>/dev/null | tail -5 | awk '{print "   " $9}'

# Ermittle höchste Nummer
HIGHEST=$(ls "$MIGRATION_DIR"/*.sql 2>/dev/null | \
          sed 's/.*\/V//' | \
          sed 's/__.*//' | \
          sort -n | \
          tail -1)

if [ -z "$HIGHEST" ]; then
    echo -e "${YELLOW}⚠️  Keine Migrationen gefunden. Starte mit V1${NC}"
    NEXT=1
else
    NEXT=$((HIGHEST + 1))
    echo -e "${GREEN}✅ Höchste Migration: V${HIGHEST}${NC}"
fi

# Ausgabe der nächsten Nummer
echo -e "${RED}🚨 NÄCHSTE FREIE MIGRATION: V${NEXT}${NC}"
echo ""
echo "Verwende diese Nummer für deine neue Migration:"
echo -e "${GREEN}  $MIGRATION_DIR/V${NEXT}__deine_migration_beschreibung.sql${NC}"
echo ""

# Prüfe auf Lücken (optional aber hilfreich)
if [ "$HIGHEST" -gt 1 ]; then
    EXPECTED=1
    MISSING=""
    while [ $EXPECTED -lt $HIGHEST ]; do
        if [ ! -f "$MIGRATION_DIR/V${EXPECTED}__"*.sql ] && [ ! -f "$MIGRATION_DIR/V${EXPECTED}_"*.sql ]; then
            MISSING="$MISSING V$EXPECTED"
        fi
        EXPECTED=$((EXPECTED + 1))
    done
    
    if [ -n "$MISSING" ]; then
        echo -e "${YELLOW}⚠️  Fehlende Migrations-Nummern:${MISSING}${NC}"
        echo "   (Das ist OK, wenn diese absichtlich übersprungen wurden)"
    fi
fi

# Return nur die Nummer für Scripting
echo "---"
echo "Für Scripting: V${NEXT}"
exit 0