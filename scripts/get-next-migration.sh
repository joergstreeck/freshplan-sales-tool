#!/bin/bash

# Script: get-next-migration.sh
# Zweck: Ermittelt die nächste freie Flyway Migration-Nummer
# Autor: FreshPlan Team
# Datum: 2025-08-07
# Update: 2025-08-08 - Robuster gemacht, funktioniert aus jedem Verzeichnis

set -e

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Finde das Projekt-Root (wo .git liegt)
find_project_root() {
    local dir="$PWD"
    while [ "$dir" != "/" ]; do
        if [ -d "$dir/.git" ]; then
            echo "$dir"
            return 0
        fi
        dir=$(dirname "$dir")
    done
    echo ""
    return 1
}

PROJECT_ROOT=$(find_project_root)
if [ -z "$PROJECT_ROOT" ]; then
    echo -e "${RED}❌ Fehler: Kein Git-Repository gefunden${NC}"
    exit 1
fi

# Bestimme Migration-Verzeichnis relativ zum Projekt-Root
MIGRATION_DIR="$PROJECT_ROOT/backend/src/main/resources/db/migration"

# Alternative Pfade falls wir bereits im backend-Ordner sind
if [ ! -d "$MIGRATION_DIR" ] && [ -d "src/main/resources/db/migration" ]; then
    MIGRATION_DIR="src/main/resources/db/migration"
elif [ ! -d "$MIGRATION_DIR" ] && [ -d "../backend/src/main/resources/db/migration" ]; then
    MIGRATION_DIR="../backend/src/main/resources/db/migration"
fi

# Prüfe ob Verzeichnis existiert
if [ ! -d "$MIGRATION_DIR" ]; then
    echo -e "${RED}❌ Migration-Verzeichnis nicht gefunden${NC}"
    echo -e "${YELLOW}   Gesucht in: $MIGRATION_DIR${NC}"
    echo -e "${YELLOW}   Aktuelles Verzeichnis: $PWD${NC}"
    exit 1
fi

echo -e "${BLUE}📁 Migration-Verzeichnis: ${NC}$MIGRATION_DIR"
echo ""

# Zeige die wirklich letzten 5 Migrationen (sortiert nach Versionsnummer)
echo -e "${YELLOW}📋 Letzte 5 Migrationen (nach Version sortiert):${NC}"
ls "$MIGRATION_DIR"/*.sql 2>/dev/null | \
    sed 's/.*\///' | \
    sort -V | \
    tail -5 | \
    while read -r file; do
        echo "   $file"
    done

echo ""

# Ermittle höchste Nummer (verbesserte Version)
HIGHEST=$(ls "$MIGRATION_DIR"/*.sql 2>/dev/null | \
          sed 's/.*\/V//' | \
          sed 's/[_].*$//' | \
          grep -E '^[0-9]+$' | \
          sort -n | \
          tail -1)

if [ -z "$HIGHEST" ]; then
    echo -e "${YELLOW}⚠️  Keine Migrationen gefunden. Starte mit V1${NC}"
    NEXT=1
else
    NEXT=$((HIGHEST + 1))
    echo -e "${GREEN}✅ Höchste Migration: V${HIGHEST}${NC}"
    
    # Zeige auch die aktuelle höchste Migration-Datei
    HIGHEST_FILE=$(ls "$MIGRATION_DIR"/V${HIGHEST}*.sql 2>/dev/null | head -1 | sed 's/.*\///')
    if [ -n "$HIGHEST_FILE" ]; then
        echo -e "${BLUE}   Datei: $HIGHEST_FILE${NC}"
    fi
fi

# Ausgabe der nächsten Nummer
echo ""
echo -e "${RED}🚨 NÄCHSTE FREIE MIGRATION: V${NEXT}${NC}"
echo ""
echo "Verwende diese Nummer für deine neue Migration:"
echo -e "${GREEN}  V${NEXT}__deine_migration_beschreibung.sql${NC}"
echo ""

# Prüfe auf Lücken in den letzten 20 Nummern (nicht alle, das wäre zu viel Output)
if [ "$HIGHEST" -gt 1 ]; then
    START=$((HIGHEST - 20))
    if [ $START -lt 1 ]; then
        START=1
    fi
    
    MISSING=""
    MISSING_COUNT=0
    EXPECTED=$START
    
    while [ $EXPECTED -lt $HIGHEST ]; do
        if ! ls "$MIGRATION_DIR"/V${EXPECTED}[_]*.sql >/dev/null 2>&1; then
            if [ $MISSING_COUNT -lt 10 ]; then
                MISSING="$MISSING V$EXPECTED"
            fi
            MISSING_COUNT=$((MISSING_COUNT + 1))
        fi
        EXPECTED=$((EXPECTED + 1))
    done
    
    if [ -n "$MISSING" ]; then
        echo -e "${YELLOW}⚠️  Fehlende Migrations-Nummern (letzte 20):${MISSING}${NC}"
        if [ $MISSING_COUNT -gt 10 ]; then
            echo -e "${YELLOW}   ... und $((MISSING_COUNT - 10)) weitere${NC}"
        fi
        echo "   (Das ist OK, wenn diese absichtlich übersprungen wurden)"
    fi
fi

# Prüfe ob die nächste Nummer bereits existiert (Sicherheitscheck)
if ls "$MIGRATION_DIR"/V${NEXT}[_]*.sql >/dev/null 2>&1; then
    echo ""
    echo -e "${RED}⚠️  WARNUNG: V${NEXT} existiert bereits!${NC}"
    echo -e "${YELLOW}   Bitte prüfe die Migrations manuell.${NC}"
    exit 1
fi

# Return nur die Nummer für Scripting
echo "---"
echo "Für Scripting: V${NEXT}"
exit 0