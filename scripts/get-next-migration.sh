#!/bin/bash

# Script: get-next-migration.sh
# Zweck: Ermittelt die nächste freie Flyway Migration-Nummer
# Autor: FreshPlan Team
# Datum: 2025-08-07
# Update: 2025-08-08 - Robuster gemacht, funktioniert aus jedem Verzeichnis
# Update: 2025-08-09 - KRITISCH: Konsistente Berechnung sichergestellt
# Update: 2025-08-17 - WICHTIG: Ignoriere V8000+ (nur V1-V7999 für Production)

set -e

# Debug-Modus (setze DEBUG=1 für detaillierte Ausgabe)
DEBUG=${DEBUG:-0}

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

# Zeige die wirklich letzten 5 Produktions-Migrationen (V1-V7999 + V10000-V19999)
echo -e "${YELLOW}📋 Letzte 5 Produktions-Migrationen (V1-V7999, V10000-V19999):${NC}"

# Erstelle temporäre Liste mit Nummer und Dateiname
for file in "$MIGRATION_DIR"/V*.sql; do
    if [ -f "$file" ]; then
        basename "$file"
    fi
done | while read -r filename; do
    # Extrahiere Nummer
    num=$(echo "$filename" | sed 's/^V//' | sed 's/__.*$//' | sed 's/_.*$//')
    # Production: V1-V7999 oder V10000-V19999 (V8000-V9999 = Test/CI reserved)
    if [[ "$num" =~ ^[0-9]+$ ]] && { [ "$num" -lt 8000 ] || { [ "$num" -ge 10000 ] && [ "$num" -lt 20000 ]; }; }; then
        echo "$num $filename"
    fi
done | sort -n | tail -5 | while read -r num filename; do
    echo "   $filename"
done

echo ""

# Ermittle höchste Nummer (KRITISCH: numerische Sortierung!)
# Extrahiere nur die Nummer aus V<nummer>__beschreibung.sql

if [ "$DEBUG" = "1" ]; then
    echo -e "${BLUE}🔍 DEBUG: Analysiere alle Migrations...${NC}"
    ls "$MIGRATION_DIR"/V*.sql 2>/dev/null | while read -r filepath; do
        filename=$(basename "$filepath")
        num=$(echo "$filename" | sed 's/^V//' | sed 's/__.*$//' | sed 's/_.*$//')
        echo "   $filename -> Nummer: $num"
    done | head -10
    echo ""
fi

# Drei verschiedene Methoden zur Sicherheit
# WICHTIG: Production Migrationen: V1-V7999 + V10000-V19999 (V8000-V9999 = Test/CI reserved)
METHOD1=$(ls "$MIGRATION_DIR"/V*.sql 2>/dev/null | while read -r filepath; do
    basename "$filepath" | sed 's/^V//' | sed 's/__.*$//' | sed 's/_.*$//'
done | grep -E '^[0-9]+$' | awk '($1 < 8000) || ($1 >= 10000 && $1 < 20000)' | sort -n | tail -1)

METHOD2=$(find "$MIGRATION_DIR" -name "V*.sql" -type f 2>/dev/null | \
          sed 's/.*\/V//' | sed 's/[_].*$//' | \
          grep -E '^[0-9]+$' | awk '($1 < 8000) || ($1 >= 10000 && $1 < 20000)' | sort -n | tail -1)

METHOD3=$(cd "$MIGRATION_DIR" 2>/dev/null && ls V*.sql 2>/dev/null | \
          sed 's/^V//' | cut -d'_' -f1 | \
          grep -E '^[0-9]+$' | awk '($1 < 8000) || ($1 >= 10000 && $1 < 20000)' | sort -n | tail -1)

# Verwende Method1 als Standard
HIGHEST="$METHOD1"

# Konsistenz-Check: Alle Methoden müssen dasselbe Ergebnis liefern
if [ "$DEBUG" = "1" ]; then
    echo -e "${BLUE}🔍 DEBUG: Konsistenz-Prüfung der Methoden${NC}"
    echo "   Method 1 (ls + basename): $METHOD1"
    echo "   Method 2 (find): $METHOD2"
    echo "   Method 3 (cd + ls): $METHOD3"
fi

# Warnung wenn Methoden unterschiedliche Ergebnisse liefern
if [ -n "$METHOD1" ] && [ -n "$METHOD2" ] && [ "$METHOD1" != "$METHOD2" ]; then
    echo -e "${RED}⚠️  WARNUNG: Inkonsistente Ergebnisse!${NC}"
    echo "   Method1: V$METHOD1, Method2: V$METHOD2"
    # Im Zweifel das höhere nehmen
    HIGHEST=$(echo -e "$METHOD1\n$METHOD2" | sort -n | tail -1)
    echo "   Verwende sicherheitshalber: V$HIGHEST"
fi

if [ -z "$HIGHEST" ]; then
    echo -e "${YELLOW}⚠️  Keine Produktions-Migrationen gefunden. Starte mit V1${NC}"
    NEXT=1
else
    NEXT=$((HIGHEST + 1))
    echo -e "${GREEN}✅ Höchste Produktions-Migration: V${HIGHEST}${NC}"
    echo -e "${BLUE}   (Nur V1-V7999 für Production, V8000+ sind Test/CI-only)${NC}"
    
    # Zeige auch die aktuelle höchste Migration-Datei
    HIGHEST_FILE=$(ls "$MIGRATION_DIR"/V${HIGHEST}*.sql 2>/dev/null | head -1 | sed 's/.*\///')
    if [ -n "$HIGHEST_FILE" ]; then
        echo -e "${BLUE}   Datei: $HIGHEST_FILE${NC}"
    fi
fi

# Warnung wenn wir uns der Produktions-Grenze nähern
if [ "$NEXT" -ge 7900 ] && [ "$NEXT" -lt 8000 ]; then
    echo -e "${YELLOW}⚠️  ACHTUNG: Näherung an Produktions-Limit (V8000)${NC}"
    echo -e "${YELLOW}   Nur noch $((8000 - NEXT)) Produktions-Migrationen möglich!${NC}"
    echo ""
fi

# Kritischer Check: Keine Migrationen in V8000-V9999 Range (Test/CI reserved)
if [ "$NEXT" -ge 8000 ] && [ "$NEXT" -lt 10000 ]; then
    echo -e "${RED}❌ FEHLER: V8000-V9999 ist für Test/CI-Migrationen reserviert!${NC}"
    echo -e "${YELLOW}   Production Migrationen: V1-V7999 oder V10000-V19999${NC}"
    echo -e "${YELLOW}   Bitte V10000+ verwenden oder mit dem Team besprechen.${NC}"
    exit 1
fi

# Interaktive Warnung für V10000-V10099 (oft CI-only Range)
if [ "$NEXT" -ge 10000 ] && [ "$NEXT" -lt 10100 ]; then
    echo -e "${YELLOW}⚠️  V10000-V10099 wird oft für CI-only Migrationen verwendet${NC}"
    echo -e "${YELLOW}   Ist das eine Test-Migration (nur CI/Dev)? (y/n):${NC}"
    read -r answer

    if [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
        echo ""
        echo -e "${RED}   ❌ WICHTIG: Füge die Migration zu application.properties hinzu:${NC}"
        echo -e "${YELLOW}      quarkus.flyway.ignoreMigrationPatterns=...,*:${NEXT}${NC}"
        echo ""
        echo -e "${YELLOW}   Siehe: docs/planung/MIGRATIONS.md Section 'Test-Migrationen erstellen'${NC}"
        echo ""
    else
        echo -e "${GREEN}   ✅ OK, dies ist eine Production-Migration${NC}"
        echo ""
    fi
fi

# Check: Keine Migrationen über V19999
if [ "$NEXT" -ge 20000 ]; then
    echo -e "${RED}❌ FEHLER: V20000+ ist aktuell nicht erlaubt!${NC}"
    echo -e "${YELLOW}   Bitte mit dem Team besprechen, wie weiter vorgegangen werden soll.${NC}"
    exit 1
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

# Konsistenz-Check: Verifiziere nochmal
if [ "$DEBUG" = "1" ]; then
    echo -e "${BLUE}🔍 DEBUG: Konsistenz-Check${NC}"
    echo "   Höchste gefundene Migration: V${HIGHEST:-0}"
    echo "   Nächste freie Migration: V${NEXT}"
    
    # Double-Check mit alternativer Methode (V1-V7999 + V10000-V19999)
    ALT_HIGHEST=$(find "$MIGRATION_DIR" -name "V*.sql" -type f 2>/dev/null | \
                   sed 's/.*\/V//' | sed 's/[_].*$//' | \
                   grep -E '^[0-9]+$' | awk '($1 < 8000) || ($1 >= 10000 && $1 < 20000)' | sort -n | tail -1)
    if [ -n "$ALT_HIGHEST" ] && [ "$ALT_HIGHEST" != "$HIGHEST" ]; then
        echo -e "${RED}   ⚠️  WARNUNG: Alternative Methode ergab V${ALT_HIGHEST}${NC}"
    fi
fi

# Return nur die Nummer für Scripting
echo "---"
echo "Für Scripting: V${NEXT}"
exit 0