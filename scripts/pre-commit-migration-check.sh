#!/bin/bash
# Pre-Commit Hook: Migration Safety Check
# Prüft Migrations-Dateien vor jedem Commit
# Update: 2025-10-21 - Idempotenz-Prüfung hinzugefügt (CHECK 4)

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

# Finde höchste Nummern (2-Sequenzen-Modell!)
# Sequenz 1: Production + Test (V1-V89999 GEMEINSAM)
# Sequenz 2: SEED (V90001+ EIGENE Sequenz)

HIGHEST_SEQUENTIAL=$(git ls-tree -r --name-only HEAD backend/src/main/resources/db/migration/ backend/src/main/resources/db/dev-migration/ 2>/dev/null | \
  grep "V[0-9]*__.*\.sql" | sed 's/.*V\([0-9]*\)__.*/\1/' | awk '$1 < 90000' | sort -n | tail -1)

HIGHEST_SEED=$(git ls-tree -r --name-only HEAD backend/src/main/resources/db/dev-seed/ 2>/dev/null | \
  grep "V[0-9]*__.*\.sql" | sed 's/.*V\([0-9]*\)__.*/\1/' | sort -n | tail -1)

if [ -z "$HIGHEST_SEQUENTIAL" ]; then
    HIGHEST_SEQUENTIAL=0
fi

if [ -z "$HIGHEST_SEED" ]; then
    HIGHEST_SEED=90000
fi

echo -e "${YELLOW}📊 Höchste Nummern (2-Sequenzen-Modell):${NC}"
echo -e "   Sequential (Prod+Test): V$HIGHEST_SEQUENTIAL"
echo -e "   SEED: V$HIGHEST_SEED"
echo ""

# Prüfe jede neue Migration
ERROR=0
for FILE in $MIGRATION_FILES; do
    # Extrahiere Nummer
    VERSION=$(echo "$FILE" | sed 's/.*V\([0-9]*\)__.*/\1/')
    FILENAME=$(basename "$FILE")

    echo -e "${YELLOW}🔍 Prüfe: $FILENAME (V$VERSION)${NC}"

    # CHECK 1: Nummer muss höher sein als bisherige (sequenz-spezifisch!)
    if [ "$VERSION" -ge 90001 ]; then
        # SEED Sequenz
        if [ "$VERSION" -le "$HIGHEST_SEED" ]; then
            echo -e "${RED}❌ FEHLER: V$VERSION (SEED) ist nicht höher als V$HIGHEST_SEED!${NC}"
            echo -e "${RED}   SEED-Sequenz: V90001+${NC}"
            echo -e "${RED}   Nächste SEED: V$((HIGHEST_SEED + 1))${NC}"
            ERROR=1
            continue
        fi
    else
        # Sequential Sequenz (Prod+Test)
        if [ "$VERSION" -le "$HIGHEST_SEQUENTIAL" ]; then
            echo -e "${RED}❌ FEHLER: V$VERSION (Sequential) ist nicht höher als V$HIGHEST_SEQUENTIAL!${NC}"
            echo -e "${RED}   Sequential-Sequenz: V1-V89999 (Prod+Test gemeinsam)${NC}"
            echo -e "${RED}   Nächste Sequential: V$((HIGHEST_SEQUENTIAL + 1))${NC}"
            echo -e "${RED}   Verwende: ./scripts/get-next-migration.sh${NC}"
            ERROR=1
            continue
        fi
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

    # CHECK 4: Idempotenz-Prüfung (KRITISCH!)
    echo -e "${YELLOW}   🔍 Idempotenz-Prüfung...${NC}"

    IDEMPOTENT=1
    NON_IDEMPOTENT_ISSUES=()

    # Prüfe CREATE TABLE ohne IF NOT EXISTS
    if grep -qE "^[[:space:]]*CREATE[[:space:]]+TABLE[[:space:]]+[^I]" "$FILE"; then
        if ! grep -qE "CREATE[[:space:]]+TABLE[[:space:]]+IF[[:space:]]+NOT[[:space:]]+EXISTS" "$FILE"; then
            NON_IDEMPOTENT_ISSUES+=("CREATE TABLE ohne 'IF NOT EXISTS' gefunden")
            IDEMPOTENT=0
        fi
    fi

    # Prüfe ALTER TABLE ADD COLUMN ohne IF NOT EXISTS
    if grep -qE "ALTER[[:space:]]+TABLE.*ADD[[:space:]]+COLUMN[[:space:]]+[^I]" "$FILE"; then
        if ! grep -qE "ADD[[:space:]]+COLUMN[[:space:]]+IF[[:space:]]+NOT[[:space:]]+EXISTS" "$FILE"; then
            NON_IDEMPOTENT_ISSUES+=("ALTER TABLE ADD COLUMN ohne 'IF NOT EXISTS' gefunden")
            IDEMPOTENT=0
        fi
    fi

    # Prüfe CREATE INDEX ohne IF NOT EXISTS
    if grep -qE "^[[:space:]]*CREATE[[:space:]]+INDEX[[:space:]]+[^I]" "$FILE"; then
        if ! grep -qE "CREATE[[:space:]]+INDEX[[:space:]]+IF[[:space:]]+NOT[[:space:]]+EXISTS" "$FILE"; then
            NON_IDEMPOTENT_ISSUES+=("CREATE INDEX ohne 'IF NOT EXISTS' gefunden")
            IDEMPOTENT=0
        fi
    fi

    # Prüfe INSERT ohne ON CONFLICT
    if grep -qE "^[[:space:]]*INSERT[[:space:]]+INTO" "$FILE"; then
        if ! grep -qE "ON[[:space:]]+CONFLICT.*DO[[:space:]]+(NOTHING|UPDATE)" "$FILE"; then
            NON_IDEMPOTENT_ISSUES+=("INSERT ohne 'ON CONFLICT DO NOTHING' gefunden")
            IDEMPOTENT=0
        fi
    fi

    # Prüfe ALTER TABLE ADD CONSTRAINT ohne DO-Block
    if grep -qE "ALTER[[:space:]]+TABLE.*ADD[[:space:]]+CONSTRAINT[[:space:]]+[^I]" "$FILE"; then
        if ! grep -qE "DO[[:space:]]*\$\$.*IF[[:space:]]+NOT[[:space:]]+EXISTS.*pg_constraint" "$FILE"; then
            NON_IDEMPOTENT_ISSUES+=("ALTER TABLE ADD CONSTRAINT ohne DO-Block (pg_constraint check)")
            IDEMPOTENT=0
        fi
    fi

    if [ $IDEMPOTENT -eq 0 ]; then
        echo -e "${RED}   ❌ NICHT IDEMPOTENT!${NC}"
        echo -e "${RED}   Probleme:${NC}"
        for issue in "${NON_IDEMPOTENT_ISSUES[@]}"; do
            echo -e "${RED}     - $issue${NC}"
        done
        echo ""
        echo -e "${YELLOW}   💡 Idempotente Migration-Patterns:${NC}"
        echo -e "${YELLOW}      CREATE TABLE IF NOT EXISTS ...${NC}"
        echo -e "${YELLOW}      ALTER TABLE ... ADD COLUMN IF NOT EXISTS ...${NC}"
        echo -e "${YELLOW}      CREATE INDEX IF NOT EXISTS ...${NC}"
        echo -e "${YELLOW}      INSERT INTO ... ON CONFLICT DO NOTHING${NC}"
        echo -e "${YELLOW}      Constraints: DO \$\$ BEGIN IF NOT EXISTS (...) THEN ... END IF; END \$\$;${NC}"
        echo ""
        ERROR=1
    else
        echo -e "   ${GREEN}✅ Migration ist idempotent${NC}"
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
