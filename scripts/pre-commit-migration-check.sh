#!/bin/bash
# Pre-Commit Hook: Migration Safety Check
# Prüft Migrations-Dateien vor jedem Commit
# Update: 2025-10-12 - DEV-SEED Support (3-Ordner-Struktur)

echo "🔍 Migration Safety Check..."

# Farben
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Finde alle neuen/geänderten Migrations-Dateien (ALLE 3 Ordner!)
MIGRATION_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep -E "db/(migration|dev-migration|dev-seed)/V[0-9]+__.*\.sql")

if [ -z "$MIGRATION_FILES" ]; then
    echo -e "${GREEN}✅ Keine Migrations-Änderungen${NC}"
    exit 0
fi

echo -e "${YELLOW}📄 Gefundene Migrationen:${NC}"
echo "$MIGRATION_FILES"
echo ""

# Finde höchste existierende Nummer (aus ALLEN 3 Ordnern, NUR committed files!)
# Wichtig: git ls-tree HEAD verwenden (zeigt NUR committed files)
HIGHEST=$(git ls-tree -r --name-only HEAD backend/src/main/resources/db/migration/ backend/src/main/resources/db/dev-migration/ backend/src/main/resources/db/dev-seed/ 2>/dev/null | \
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

    # CHECK 2: Bestimme Ordner (JETZT 3 OPTIONEN!)
    if echo "$FILE" | grep -q "/dev-seed/"; then
        ORDNER="dev-seed"
        ORDNER_TYPE="DEV-SEED"
    elif echo "$FILE" | grep -q "/dev-migration/"; then
        ORDNER="dev-migration"
        ORDNER_TYPE="Test-Migration"
    else
        ORDNER="migration"
        ORDNER_TYPE="Production"
    fi

    echo -e "   📁 Ordner: $ORDNER ($ORDNER_TYPE)"

    # CHECK 2a: Range-Validierung für DEV-SEED
    if [ "$ORDNER" = "dev-seed" ]; then
        if [ "$VERSION" -lt 90001 ]; then
            echo -e "${RED}❌ FEHLER: DEV-SEED muss >= V90001 sein!${NC}"
            echo -e "${RED}   Gefunden: V$VERSION (zu niedrig)${NC}"
            echo -e "${RED}   DEV-SEED Range: V90001+${NC}"
            ERROR=1
            continue
        else
            echo -e "   ✅ V$VERSION >= V90001 (DEV-SEED Range korrekt)"
        fi
    fi

    # CHECK 3: Dateiname-Keywords vs. Ordner
    # Prüfe auf Prefix (nicht irgendwo im Namen, sondern am Anfang der Beschreibung)
    # Beschreibung ist alles nach V<nummer>__
    DESCRIPTION=$(echo "$FILENAME" | sed 's/^V[0-9]*__//' | sed 's/\.sql$//')

    # CHECK 3a: DEV-SEED sollte 'seed_' Prefix haben
    if [ "$ORDNER" = "dev-seed" ]; then
        if ! echo "$DESCRIPTION" | grep -q "^seed_"; then
            echo -e "${YELLOW}   ⚠️  WARNUNG: DEV-SEED sollte mit 'seed_' beginnen!${NC}"
            echo -e "${YELLOW}   Gefunden: $DESCRIPTION${NC}"
            echo -e "${YELLOW}   Empfohlen: seed_$DESCRIPTION${NC}"
            # Kein Error, nur Warnung
        else
            echo -e "   ✅ seed_ Prefix vorhanden"
        fi
    fi

    # CHECK 3b: Test-Keywords vs. Ordner
    if echo "$DESCRIPTION" | grep -qiE "^(test_|demo_|sample_|debug_)"; then
        # Hat Test-Prefix
        if [ "$ORDNER" = "migration" ]; then
            echo -e "${RED}❌ FEHLER: Migration startet mit Test-Prefix aber liegt in migration/!${NC}"
            echo -e "${RED}   Gefundener Prefix: $(echo "$DESCRIPTION" | grep -oiE "^(test_|demo_|sample_|debug_)")${NC}"
            echo -e "${RED}   Test-Migrationen gehören in dev-migration/!${NC}"
            echo ""
            echo -e "${YELLOW}   Korrektur:${NC}"
            echo -e "${YELLOW}   mv backend/src/main/resources/db/migration/$FILENAME \\${NC}"
            echo -e "${YELLOW}      backend/src/main/resources/db/dev-migration/${NC}"
            ERROR=1
            continue
        elif [ "$ORDNER" = "dev-migration" ]; then
            echo -e "   ✅ Test-Prefix + dev-migration/ = korrekt"
        fi
    fi

    # CHECK 3c: seed_ Prefix sollte NUR in dev-seed/ sein
    if echo "$DESCRIPTION" | grep -q "^seed_"; then
        if [ "$ORDNER" != "dev-seed" ]; then
            echo -e "${RED}❌ FEHLER: 'seed_' Prefix aber NICHT in dev-seed/!${NC}"
            echo -e "${RED}   Gefunden in: $ORDNER/${NC}"
            echo -e "${RED}   SEED-Daten gehören in dev-seed/!${NC}"
            echo ""
            echo -e "${YELLOW}   Korrektur:${NC}"
            echo -e "${YELLOW}   mv backend/src/main/resources/db/$ORDNER/$FILENAME \\${NC}"
            echo -e "${YELLOW}      backend/src/main/resources/db/dev-seed/${NC}"
            ERROR=1
            continue
        fi
    fi

    # CHECK 3d: Production sollte keine Test-Keywords haben
    if [ "$ORDNER" = "migration" ]; then
        if echo "$DESCRIPTION" | grep -qiE "^(test_|demo_|seed_|sample_|debug_)"; then
            # Bereits in CHECK 3b abgefangen
            :
        else
            echo -e "   ✅ Production-Migration ohne Test-Keywords"
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
