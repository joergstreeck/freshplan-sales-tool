#!/bin/bash

# Script: get-next-migration.sh
# Zweck: Ermittelt die nächste freie Flyway Migration-Nummer
# Autor: FreshPlan Team
# Datum: 2025-08-07
# Update: 2025-08-08 - Robuster gemacht, funktioniert aus jedem Verzeichnis
# Update: 2025-08-09 - KRITISCH: Konsistente Berechnung sichergestellt
# Update: 2025-10-10 - NEUE STRATEGIE: Fortlaufende Nummerierung, Trennung durch Ordner

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
echo -e "${GREEN}┃  📋 NEUE MIGRATIONS-STRATEGIE (ab Oktober 2025)              ┃${NC}"
echo -e "${GREEN}┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫${NC}"
echo -e "${GREEN}┃  • Alle Migrationen fortlaufend: V10022, V10023, V10024...   ┃${NC}"
echo -e "${GREEN}┃  • Trennung durch ORDNER (nicht durch Nummern):              ┃${NC}"
echo -e "${GREEN}┃    - Production: db/migration/                               ┃${NC}"
echo -e "${GREEN}┃    - Test/Dev: db/dev-migration/                             ┃${NC}"
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

# Alternative Pfade falls wir bereits im backend-Ordner sind
if [ ! -d "$MIGRATION_DIR" ] && [ -d "src/main/resources/db/migration" ]; then
    MIGRATION_DIR="src/main/resources/db/migration"
    DEV_MIGRATION_DIR="src/main/resources/db/dev-migration"
elif [ ! -d "$MIGRATION_DIR" ] && [ -d "../backend/src/main/resources/db/migration" ]; then
    MIGRATION_DIR="../backend/src/main/resources/db/migration"
    DEV_MIGRATION_DIR="../backend/src/main/resources/db/dev-migration"
fi

# Prüfe ob Verzeichnisse existieren
if [ ! -d "$MIGRATION_DIR" ]; then
    echo -e "${RED}❌ Migration-Verzeichnis nicht gefunden${NC}"
    echo -e "${YELLOW}   Gesucht in: $MIGRATION_DIR${NC}"
    echo -e "${YELLOW}   Aktuelles Verzeichnis: $PWD${NC}"
    exit 1
fi

echo -e "${BLUE}📁 Migration-Verzeichnisse:${NC}"
echo -e "${BLUE}   Production: $MIGRATION_DIR${NC}"
echo -e "${BLUE}   Test/Dev:   $DEV_MIGRATION_DIR${NC}"
echo ""

# Zeige die letzten 5 Migrationen (aus BEIDEN Ordnern)
echo -e "${YELLOW}📋 Letzte 5 Migrationen (alle Ordner):${NC}"

# Sammle aus beiden Ordnern
ALL_FILES=""
for file in "$MIGRATION_DIR"/V*.sql "$DEV_MIGRATION_DIR"/V*.sql; do
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

# Ermittle höchste Nummer (aus BEIDEN Ordnern)
HIGHEST=$(find "$MIGRATION_DIR" "$DEV_MIGRATION_DIR" -name "V*.sql" 2>/dev/null | \
  sed 's/.*V\([0-9]*\)__.*/\1/' | grep -E '^[0-9]+$' | sort -n | tail -1)

if [ -z "$HIGHEST" ]; then
    echo -e "${YELLOW}⚠️  Keine Migrationen gefunden. Starte mit V1${NC}"
    NEXT=1
else
    NEXT=$((HIGHEST + 1))
    echo -e "${GREEN}✅ Höchste Migration: V${HIGHEST}${NC}"
    
    # Zeige auch die aktuelle höchste Migration-Datei
    HIGHEST_FILE=$(find "$MIGRATION_DIR" "$DEV_MIGRATION_DIR" -name "V${HIGHEST}*.sql" 2>/dev/null | head -1 | xargs basename 2>/dev/null)
    if [ -n "$HIGHEST_FILE" ]; then
        echo -e "${BLUE}   Datei: $HIGHEST_FILE${NC}"
    fi
fi

# Sanity-Check: Verhindert Tippfehler (z.B. V100023 statt V10023)
MAX_JUMP=100
if [ "$NEXT" -gt $((HIGHEST + MAX_JUMP)) ]; then
    echo ""
    echo -e "${RED}❌ FEHLER: Berechnete Nummer V$NEXT erscheint unrealistisch!${NC}"
    echo -e "${RED}   Maximal erwarteter Sprung: $MAX_JUMP${NC}"
    echo -e "${RED}   Aktuell höchste Nummer: V$HIGHEST${NC}"
    echo ""
    echo -e "${YELLOW}Mögliche Ursachen:${NC}"
    echo -e "${YELLOW}  1. Falsche Migration-Dateien im Verzeichnis${NC}"
    echo -e "${YELLOW}  2. Tippfehler in Dateinamen (z.B. V100023 statt V10023)${NC}"
    echo ""
    echo -e "${YELLOW}Bitte prüfen:${NC}"
    echo -e "${YELLOW}  ls backend/src/main/resources/db/migration/V*.sql | tail -5${NC}"
    echo -e "${YELLOW}  ls backend/src/main/resources/db/dev-migration/V*.sql | tail -5${NC}"
    exit 1
fi

# Ausgabe der nächsten Nummer
echo ""
echo -e "${RED}🚨 NÄCHSTE FREIE MIGRATION: V${NEXT}${NC}"
echo ""

# Frage nach Ordner-Auswahl
echo -e "${YELLOW}In welchem Ordner soll die Migration erstellt werden?${NC}"
echo ""
echo -e "  ${GREEN}1)${NC} 🏭 Production (db/migration/)"
echo -e "     → Läuft in ALLEN Umgebungen (Dev, Test, Production)"
echo -e "     → Für: Schema-Änderungen, Production-Features"
echo ""
echo -e "  ${GREEN}2)${NC} 🧪 Test/Dev (db/dev-migration/)"
echo -e "     → Läuft NUR in Dev/Test (NICHT in Production)"
echo -e "     → Für: Test-Daten, Demo-Features, Debug-Views"
echo ""
echo -e "  ${BLUE}💡 Tipp: Im Zweifel → Option 1 (Production)${NC}"
echo -e "  ${BLUE}   Test-Migrationen sind selten nötig!${NC}"
echo ""

read -p "Deine Wahl (1 oder 2): " CHOICE

if [ "$CHOICE" = "1" ]; then
    TARGET_DIR="$MIGRATION_DIR"
    TARGET_TYPE="Production"
    echo -e "${GREEN}✅ Production-Migration in db/migration/${NC}"
elif [ "$CHOICE" = "2" ]; then
    TARGET_DIR="$DEV_MIGRATION_DIR"
    TARGET_TYPE="Test/Dev"
    echo -e "${YELLOW}✅ Test/Dev-Migration in db/dev-migration/${NC}"
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
echo "  'Schreibe SQL-Code für $DESC."
echo "   WICHTIG: KEINE Datei erstellen, nur SQL-Code!'"
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
echo "  Falls Hook blockt:"
echo "  → Datei im falschen Ordner oder Nummer falsch"
echo "  → Siehe Fehler-Message für Details"
echo ""
echo "========================================="
echo -e "${GREEN}📁 ZIEL-DATEI:${NC}"
echo "========================================="
echo ""
echo "  $FILEPATH"
echo ""

# Return nur die Nummer für Scripting
echo "---"
echo "Für Scripting: V${NEXT} ($TARGET_TYPE)"
exit 0
