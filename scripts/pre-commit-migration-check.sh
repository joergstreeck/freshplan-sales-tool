#!/bin/bash
# Pre-Commit Hook: Migration Safety Check  
# Prüft Migrations-Dateien vor jedem Commit

echo "🔍 Migration Safety Check..."

# Farben
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Finde alle neuen/geänderten Migrations-Dateien
MIGRATION_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep -E "db/(migration|dev-migration)/V[0-9]+__.*\.sql")

if [ -z "$MIGRATION_FILES" ]; then
    echo -e "${GREEN}✅ Keine Migrations-Änderungen${NC}"
    exit 0
fi

echo -e "${YELLOW}📄 Gefundene Migrationen:${NC}"
echo "$MIGRATION_FILES"
echo ""

# Finde höchste existierende Nummer (aus BEIDEN Ordnern, NUR committed files!)
# Wichtig: git ls-tree HEAD verwenden (zeigt NUR committed files)
HIGHEST=$(git ls-tree -r --name-only HEAD backend/src/main/resources/db/migration/ backend/src/main/resources/db/dev-migration/ 2>/dev/null | \
  grep "V[0-9]*__.*\.sql" | sed 's/.*V\([0-9]*\)__.*/\1/' | sort -n | tail -1)

if [ -z "$HIGHEST" ]; then
    HIGHEST=0
fi

echo -e "${YELLOW}📊 Höchste existierende Nummer: V$HIGHEST${NC}"
echo ""

# Prüfe jede neue Migration
ERROR=0
for FILE in $MIGRATION_FILES; do
    # Extrahiere Nummer
    VERSION=$(echo "$FILE" | sed 's/.*V\([0-9]*\)__.*/\1/')
    FILENAME=$(basename "$FILE")

    echo -e "${YELLOW}🔍 Prüfe: $FILENAME (V$VERSION)${NC}"

    # CHECK 1: Nummer muss höher sein als bisherige
    if [ "$VERSION" -le "$HIGHEST" ]; then
        echo -e "${RED}❌ FEHLER: V$VERSION ist nicht höher als V$HIGHEST!${NC}"
        echo -e "${RED}   Verwende: ./scripts/get-next-migration.sh${NC}"
        ERROR=1
        continue
    fi

    # CHECK 2: Bestimme Ordner
    if echo "$FILE" | grep -q "/dev-migration/"; then
        ORDNER="dev-migration"
        ORDNER_TYPE="Test/Dev"
    else
        ORDNER="migration"
        ORDNER_TYPE="Production"
    fi

    echo -e "   📁 Ordner: $ORDNER ($ORDNER_TYPE)"

    # CHECK 3: Dateiname-Keywords vs. Ordner
    if echo "$FILENAME" | grep -qiE "(test|demo|seed|sample|debug)"; then
        # Hat Test-Keywords
        if [ "$ORDNER" = "migration" ]; then
            echo -e "${RED}❌ FEHLER: Dateiname enthält Test-Keywords aber liegt in migration/!${NC}"
            echo -e "${RED}   Keywords gefunden: test|demo|seed|sample|debug${NC}"
            echo -e "${RED}   Test-Migrationen gehören in dev-migration/!${NC}"
            echo ""
            echo -e "${YELLOW}   Korrektur:${NC}"
            echo -e "${YELLOW}   mv backend/src/main/resources/db/migration/$FILENAME \\${NC}"
            echo -e "${YELLOW}      backend/src/main/resources/db/dev-migration/${NC}"
            ERROR=1
            continue
        else
            echo -e "   ✅ Test-Keywords + dev-migration/ = korrekt"
        fi
    else
        # Keine Test-Keywords
        if [ "$ORDNER" = "dev-migration" ]; then
            echo -e "${YELLOW}   ⚠️  WARNUNG: Keine Test-Keywords aber in dev-migration/!${NC}"
            echo -e "${YELLOW}   Ist das wirklich eine Test-Migration?${NC}"
            # Kein Error, nur Warnung
        else
            echo -e "   ✅ Production-Migration in migration/ = korrekt"
        fi
    fi

    echo ""
done

if [ $ERROR -eq 1 ]; then
    echo -e "${RED}❌ Migration Safety Check FEHLGESCHLAGEN!${NC}"
    echo -e "${RED}   Commit wurde BLOCKIERT.${NC}"
    echo ""
    echo -e "${YELLOW}Hilfe:${NC}"
    echo -e "${YELLOW}  1. Verwende: ./scripts/get-next-migration.sh${NC}"
    echo -e "${YELLOW}  2. Migration im richtigen Ordner erstellen${NC}"
    echo -e "${YELLOW}  3. Erneut committen${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Migration Safety Check BESTANDEN!${NC}"
exit 0
