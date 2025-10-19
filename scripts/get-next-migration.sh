#!/bin/bash

# Script: get-next-migration.sh
# Zweck: Ermittelt die nächste freie Flyway Migration-Nummer
# Autor: FreshPlan Team
# Datum: 2025-08-07
# Update: 2025-08-08 - Robuster gemacht, funktioniert aus jedem Verzeichnis
# Update: 2025-08-09 - KRITISCH: Konsistente Berechnung sichergestellt
# Update: 2025-10-10 - NEUE STRATEGIE: Fortlaufende Nummerierung, Trennung durch Ordner
# Update: 2025-10-12 - DEV-SEED Support (3-Ordner-Struktur)

set -e

# Debug-Modus (setze DEBUG=1 für detaillierte Ausgabe)
DEBUG=${DEBUG:-0}

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo -e "${GREEN}┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓${NC}"
echo -e "${GREEN}┃  📋 MIGRATIONS-STRATEGIE (2-Sequenzen-Modell)                 ┃${NC}"
echo -e "${GREEN}┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫${NC}"
echo -e "${GREEN}┃  SEQUENZ 1 (Prod+Test GEMEINSAM): V1-V89999 fortlaufend      ┃${NC}"
echo -e "${GREEN}┃    - Production:  db/migration/      (Schema, alle Envs)      ┃${NC}"
echo -e "${GREEN}┃    - Test-Migs:   db/dev-migration/  (Test-Only, %test)       ┃${NC}"
echo -e "${GREEN}┃    → GLEICHE Nummern-Sequenz! Test kann später zu Prod.      ┃${NC}"
echo -e "${GREEN}┃                                                               ┃${NC}"
echo -e "${GREEN}┃  SEQUENZ 2 (SEED EIGENE): V90001+ für Dev-Daten              ┃${NC}"
echo -e "${GREEN}┃    - DEV-SEED:    db/dev-seed/       (Dev-Only, %dev)        ┃${NC}"
echo -e "${GREEN}┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛${NC}"
echo ""

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

# Bestimme Migration-Verzeichnisse relativ zum Projekt-Root
MIGRATION_DIR="$PROJECT_ROOT/backend/src/main/resources/db/migration"
DEV_MIGRATION_DIR="$PROJECT_ROOT/backend/src/main/resources/db/dev-migration"
DEV_SEED_DIR="$PROJECT_ROOT/backend/src/main/resources/db/dev-seed"

# Alternative Pfade falls wir bereits im backend-Ordner sind
if [ ! -d "$MIGRATION_DIR" ] && [ -d "src/main/resources/db/migration" ]; then
    MIGRATION_DIR="src/main/resources/db/migration"
    DEV_MIGRATION_DIR="src/main/resources/db/dev-migration"
    DEV_SEED_DIR="src/main/resources/db/dev-seed"
fi

# Prüfe ob Verzeichnisse existieren
if [ ! -d "$MIGRATION_DIR" ]; then
    echo -e "${RED}❌ Migration-Verzeichnis nicht gefunden${NC}"
    echo -e "${YELLOW}   Gesucht in: $MIGRATION_DIR${NC}"
    exit 1
fi

# Erstelle dev-seed/ falls nicht vorhanden
if [ ! -d "$DEV_SEED_DIR" ]; then
    echo -e "${YELLOW}⚠️  dev-seed/ existiert nicht, wird erstellt...${NC}"
    mkdir -p "$DEV_SEED_DIR"
    echo -e "${GREEN}✅ Verzeichnis erstellt: $DEV_SEED_DIR${NC}"
fi

echo -e "${BLUE}📁 Migration-Verzeichnisse:${NC}"
echo -e "${BLUE}   Production:  $MIGRATION_DIR${NC}"
echo -e "${BLUE}   Test-Migs:   $DEV_MIGRATION_DIR${NC}"
echo -e "${BLUE}   DEV-SEED:    $DEV_SEED_DIR${NC}"
echo ""

# Zeige die letzten 5 Migrationen (aus ALLEN Ordnern)
echo -e "${YELLOW}📋 Letzte 5 Migrationen (alle Ordner):${NC}"

# Sammle aus allen Ordnern
ALL_FILES=""
for file in "$MIGRATION_DIR"/V*.sql "$DEV_MIGRATION_DIR"/V*.sql "$DEV_SEED_DIR"/V*.sql; do
    if [ -f "$file" ]; then
        basename "$file"
    fi
done | while read -r filename; do
    num=$(echo "$filename" | sed 's/^V//' | sed 's/__.*$//' | sed 's/_.*$//')
    if [[ "$num" =~ ^[0-9]+$ ]]; then
        echo "$num $filename"
    fi
done | sort -n | tail -5 | while read -r num filename; do
    echo "   $filename"
done

echo ""

# Ermittle höchste Nummer nach 2-Sequenzen-Modell!
# Sequenz 1: Production + Test GEMEINSAM (V1-V89999)
# Sequenz 2: SEED EIGENE Sequenz (V90001+)

HIGHEST_PROD=$(find "$MIGRATION_DIR" -name "V*.sql" 2>/dev/null | \
  sed 's/.*V\([0-9]*\)__.*/\1/' | grep -E '^[0-9]+$' | awk '$1 < 90000' | sort -n | tail -1)

HIGHEST_TEST=$(find "$DEV_MIGRATION_DIR" -name "V*.sql" 2>/dev/null | \
  sed 's/.*V\([0-9]*\)__.*/\1/' | grep -E '^[0-9]+$' | awk '$1 < 90000' | sort -n | tail -1)

HIGHEST_SEED=$(find "$DEV_SEED_DIR" -name "V*.sql" 2>/dev/null | \
  sed 's/.*V\([0-9]*\)__.*/\1/' | grep -E '^[0-9]+$' | sort -n | tail -1)

# Defaults für leere Ordner
if [ -z "$HIGHEST_PROD" ]; then HIGHEST_PROD=0; fi
if [ -z "$HIGHEST_TEST" ]; then HIGHEST_TEST=0; fi
if [ -z "$HIGHEST_SEED" ]; then HIGHEST_SEED=90000; fi

# KRITISCH: Sequenz 1 = MAX aus Production UND Test!
HIGHEST_SEQUENTIAL=$HIGHEST_PROD
if [ "$HIGHEST_TEST" -gt "$HIGHEST_SEQUENTIAL" ]; then
    HIGHEST_SEQUENTIAL=$HIGHEST_TEST
fi

# Zeige höchste Nummern (2-Sequenzen-Modell)
echo -e "${GREEN}📊 Höchste Nummern (2-Sequenzen-Modell):${NC}"
echo -e "${BLUE}   Sequential (Prod+Test GEMEINSAM): V$HIGHEST_SEQUENTIAL${NC}"
echo -e "${BLUE}     → Production:  V$HIGHEST_PROD (migration/)${NC}"
echo -e "${BLUE}     → Test-Migs:   V$HIGHEST_TEST (dev-migration/)${NC}"
echo -e "${BLUE}   SEED (EIGENE Sequenz):            V$HIGHEST_SEED (dev-seed/)${NC}"
echo ""

# Frage nach Ordner-Auswahl (JETZT 3 OPTIONEN!)
echo -e "${YELLOW}In welchem Ordner soll die Migration erstellt werden?${NC}"
echo ""
echo -e "  ${GREEN}1)${NC} 🏭 Production (db/migration/)"
echo -e "     → Läuft in ALLEN Umgebungen (Dev, Test, CI, Production)"
echo -e "     → Für: Schema-Änderungen, Production-Features"
echo -e "     → Nummernbereich: V1-V89999"
echo ""
echo -e "  ${GREEN}2)${NC} 🧪 Test-Migrations (db/dev-migration/)"
echo -e "     → Läuft NUR in %test (CI-Tests)"
echo -e "     → Für: Test-spezifische Schemas, Debug-Views"
echo -e "     → Nummernbereich: V1-V89999"
echo -e "     ${CYAN}💡 Selten nötig! Meist ist Option 1 richtig.${NC}"
echo ""
echo -e "  ${GREEN}3)${NC} 🌱 DEV-SEED (db/dev-seed/)"
echo -e "     → Läuft NUR in %dev (manuelle Entwicklung)"
echo -e "     → Für: Realistische SEED-Daten für UI-Testing"
echo -e "     → Nummernbereich: V90001+"
echo -e "     ${CYAN}⚠️  NIEMALS in automatisierten Tests verwenden!${NC}"
echo ""
echo -e "  ${BLUE}💡 Im Zweifel → Option 1 (Production)${NC}"
echo ""

read -p "Deine Wahl (1, 2 oder 3): " CHOICE

if [ "$CHOICE" = "1" ]; then
    TARGET_DIR="$MIGRATION_DIR"
    TARGET_TYPE="Production"
    RECOMMENDED_RANGE="V1-V89999"

    # Berechne NEXT aus SEQUENTIAL (Prod+Test gemeinsam!)
    NEXT=$((HIGHEST_SEQUENTIAL + 1))

    echo -e "${GREEN}✅ Production-Migration in db/migration/${NC}"
    echo -e "${GREEN}   Nächste Nummer: V${NEXT} (Sequential: V${HIGHEST_SEQUENTIAL})${NC}"
    if [ "$HIGHEST_TEST" -gt "$HIGHEST_PROD" ]; then
        echo -e "${YELLOW}   ⚠️  Hinweis: Test-Migration V${HIGHEST_TEST} ist höher als Production V${HIGHEST_PROD}${NC}"
        echo -e "${YELLOW}       → 2-Sequenzen-Modell: Nächste ist V${NEXT} (beide Ordner gemeinsam)${NC}"
    fi

    # Warnung wenn Nummer > 89999
    if [ "$NEXT" -ge 90000 ]; then
        echo -e "${YELLOW}⚠️  WARNUNG: V${NEXT} ist >= V90000 (SEED-Range!)${NC}"
        echo -e "${YELLOW}   Production sollte < V90000 sein.${NC}"
        read -p "Trotzdem fortfahren? (y/n): " CONFIRM
        if [ "$CONFIRM" != "y" ]; then
            echo -e "${RED}❌ Abgebrochen${NC}"
            exit 1
        fi
    fi

elif [ "$CHOICE" = "2" ]; then
    TARGET_DIR="$DEV_MIGRATION_DIR"
    TARGET_TYPE="Test-Migration"
    RECOMMENDED_RANGE="V1-V89999"

    # Berechne NEXT aus SEQUENTIAL (Prod+Test gemeinsam!)
    NEXT=$((HIGHEST_SEQUENTIAL + 1))

    echo -e "${YELLOW}✅ Test-Migration in db/dev-migration/${NC}"
    echo -e "${YELLOW}   Nächste Nummer: V${NEXT} (Sequential: V${HIGHEST_SEQUENTIAL})${NC}"
    if [ "$HIGHEST_PROD" -gt "$HIGHEST_TEST" ]; then
        echo -e "${CYAN}   💡 Hinweis: Production V${HIGHEST_PROD} ist höher als Test V${HIGHEST_TEST}${NC}"
        echo -e "${CYAN}       → 2-Sequenzen-Modell: Nächste ist V${NEXT} (beide Ordner gemeinsam)${NC}"
    fi

    # Warnung wenn Nummer > 89999
    if [ "$NEXT" -ge 90000 ]; then
        echo -e "${YELLOW}⚠️  WARNUNG: V${NEXT} ist >= V90000 (SEED-Range!)${NC}"
        echo -e "${YELLOW}   Test-Migrations sollten < V90000 sein.${NC}"
        read -p "Trotzdem fortfahren? (y/n): " CONFIRM
        if [ "$CONFIRM" != "y" ]; then
            echo -e "${RED}❌ Abgebrochen${NC}"
            exit 1
        fi
    fi

elif [ "$CHOICE" = "3" ]; then
    TARGET_DIR="$DEV_SEED_DIR"
    TARGET_TYPE="DEV-SEED"
    RECOMMENDED_RANGE="V90001+"

    # Berechne NEXT aus SEED-Ordner
    NEXT=$((HIGHEST_SEED + 1))

    # Erzwinge V90001+ für SEED-Daten
    if [ "$NEXT" -lt 90001 ]; then
        echo -e "${CYAN}✅ DEV-SEED-Migration in db/dev-seed/${NC}"
        echo -e "${YELLOW}⚠️  SEED-Daten sollten >= V90001 sein (klare Trennung)${NC}"
        NEXT=90001
        echo -e "${CYAN}✅ Nummer angepasst auf V${NEXT}${NC}"
    else
        echo -e "${CYAN}✅ DEV-SEED-Migration in db/dev-seed/${NC}"
        echo -e "${CYAN}   Nächste Nummer: V${NEXT} (basierend auf V${HIGHEST_SEED})${NC}"
    fi

    echo ""
    echo -e "${CYAN}┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓${NC}"
    echo -e "${CYAN}┃  ⚠️  WICHTIG: DEV-SEED Regeln                     ┃${NC}"
    echo -e "${CYAN}┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫${NC}"
    echo -e "${CYAN}┃  • NUR für manuelle Entwicklung (UI-Testing)      ┃${NC}"
    echo -e "${CYAN}┃  • NIEMALS in automatisierten Tests verwenden!    ┃${NC}"
    echo -e "${CYAN}┃  • Naming: 'seed_dev_customers', 'seed_dev_leads' ┃${NC}"
    echo -e "${CYAN}┃  • Prefix: '[DEV-SEED]' in company_name           ┃${NC}"
    echo -e "${CYAN}┃  • IDs: 'KD-DEV-001', Lead-IDs 90001-90999        ┃${NC}"
    echo -e "${CYAN}┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛${NC}"
    echo ""

else
    echo -e "${RED}❌ Ungültige Auswahl. Abbruch.${NC}"
    exit 1
fi

echo ""

# Frage nach Beschreibung
read -p "Migration-Beschreibung (snake_case, z.B. 'add_user_role'): " DESC

if [ -z "$DESC" ]; then
    echo -e "${RED}❌ Beschreibung erforderlich. Abbruch.${NC}"
    exit 1
fi

# Für DEV-SEED: Empfehle 'seed_' Prefix
if [ "$CHOICE" = "3" ] && ! echo "$DESC" | grep -q "^seed_"; then
    echo -e "${YELLOW}⚠️  Empfehlung: DEV-SEED sollte mit 'seed_' beginnen${NC}"
    read -p "Mit 'seed_' prefixen? (y/n): " PREFIX
    if [ "$PREFIX" = "y" ]; then
        DESC="seed_${DESC}"
        echo -e "${GREEN}✅ Neue Beschreibung: $DESC${NC}"
    fi
fi

# Konstruiere Dateinamen
FILENAME="V${NEXT}__${DESC}.sql"
FILEPATH="$TARGET_DIR/$FILENAME"

# Prüfe ob Datei bereits existiert
if [ -f "$FILEPATH" ]; then
    echo ""
    echo -e "${RED}❌ FEHLER: $FILENAME existiert bereits!${NC}"
    exit 1
fi

echo ""
echo "========================================="
echo -e "${YELLOW}🤖 ANWEISUNG FÜR CLAUDE:${NC}"
echo "========================================="
echo ""
if [ "$CHOICE" = "3" ]; then
    echo "  'Schreibe DEV-SEED SQL für $DESC."
    echo "   WICHTIG: KEINE Datei erstellen, nur SQL-Code!"
    echo "   Format: INSERT INTO ... ON CONFLICT DO NOTHING"
    echo "   IDs: Fixed UUIDs/IDs (z.B. c0000000-dev1-...)"
    echo "   Naming: [DEV-SEED] Prefix, KD-DEV-001 etc.'"
else
    echo "  'Schreibe SQL-Code für $DESC."
    echo "   WICHTIG: KEINE Datei erstellen, nur SQL-Code!'"
fi
echo ""
echo "========================================="
echo -e "${YELLOW}⚠️  MIGRATION SAFETY:${NC}"
echo "========================================="
echo ""
echo "  Nach Erstellen der Datei:"
echo "  1. Datei speichern als: $FILEPATH"
echo "  2. git add $FILEPATH"
echo "  3. git commit"
echo "  4. → Pre-Commit Hook prüft automatisch!"
echo ""
echo "========================================="
echo -e "${GREEN}📁 ZIEL-DATEI:${NC}"
echo "========================================="
echo ""
echo "  $FILEPATH"
echo ""
echo "  Typ: $TARGET_TYPE"
echo "  Empfohlener Range: $RECOMMENDED_RANGE"
echo ""

# Return nur die Nummer für Scripting
echo "---"
echo "Für Scripting: V${NEXT} ($TARGET_TYPE)"
exit 0
