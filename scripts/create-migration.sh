#!/bin/bash
# Script to create new migrations with automatic number validation

# Check if we're in the right directory
if [ ! -f "pom.xml" ] && [ ! -f "backend/pom.xml" ]; then
    echo "❌ FEHLER: Muss im Projekt-Root ausgeführt werden!"
    exit 1
fi

# Parse arguments
if [ "$#" -eq 1 ]; then
    # Only description provided, auto-detect number
    DESCRIPTION=$1
    echo "🔍 Ermittle nächste freie Migrationsnummer..."
    
    # Check MIGRATION_REGISTRY.md
    if [ -f "docs/MIGRATION_REGISTRY.md" ]; then
        NEXT_NUMBER=$(grep "NÄCHSTE VERFÜGBARE" docs/MIGRATION_REGISTRY.md | grep -o 'V[0-9]*' | sed 's/V//')
        if [ -z "$NEXT_NUMBER" ]; then
            echo "❌ FEHLER: Konnte nächste Nummer nicht aus Registry ermitteln!"
            exit 1
        fi
        echo "✅ Nächste freie Nummer aus Registry: V$NEXT_NUMBER"
    else
        echo "❌ FEHLER: docs/MIGRATION_REGISTRY.md nicht gefunden!"
        exit 1
    fi
elif [ "$#" -eq 2 ]; then
    # Both number and description provided
    NEXT_NUMBER=$1
    DESCRIPTION=$2
else
    echo "Usage: $0 [number] <description>"
    echo "Examples:"
    echo "  $0 'add_customer_fields'              # Auto-detect number"
    echo "  $0 121 'add_customer_fields'          # Explicit number"
    exit 1
fi

# Validate description
if [[ ! "$DESCRIPTION" =~ ^[a-z0-9_]+$ ]]; then
    echo "❌ FEHLER: Beschreibung darf nur Kleinbuchstaben, Zahlen und Unterstriche enthalten!"
    echo "   Beispiel: add_customer_fields"
    exit 1
fi

# Verify expected number from registry
EXPECTED_NEXT=$(grep "NÄCHSTE VERFÜGBARE" docs/MIGRATION_REGISTRY.md | grep -o 'V[0-9]*' | sed 's/V//')
if [ "$NEXT_NUMBER" != "$EXPECTED_NEXT" ]; then
    echo "🚨 WARNUNG: Nummer stimmt nicht mit Registry überein!"
    echo "   Du willst: V$NEXT_NUMBER"
    echo "   Registry:  V$EXPECTED_NEXT"
    echo ""
    echo "Trotzdem fortfahren? (y/N)"
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Check if file already exists
MIGRATION_DIR="backend/src/main/resources/db/migration"
FILENAME="$MIGRATION_DIR/V${NEXT_NUMBER}__${DESCRIPTION}.sql"

if [ -f "$FILENAME" ]; then
    echo "🚨 FEHLER: Migration V$NEXT_NUMBER existiert bereits!"
    ls -la "$FILENAME"
    exit 1
fi

# Create migration directory if it doesn't exist
mkdir -p "$MIGRATION_DIR"

# Get current date and author
AUTHOR=$(git config user.name || echo "Unknown")
DATE=$(date +%Y-%m-%d)

# Create migration file
cat > "$FILENAME" << EOF
-- =========================================
-- Migration: V${NEXT_NUMBER}__${DESCRIPTION}.sql
-- Autor: $AUTHOR
-- Datum: $DATE
-- Ticket: FRESH-XXX
-- Sprint: Sprint X
-- Zweck: [BESCHREIBUNG HIER EINFÜGEN]
-- =========================================

-- TODO: Deine SQL-Statements hier
-- WICHTIG: Alle Statements müssen idempotent sein!

-- Beispiele:
-- ALTER TABLE table_name ADD COLUMN IF NOT EXISTS column_name data_type;
-- CREATE INDEX IF NOT EXISTS idx_name ON table_name(column_name);
-- CREATE TABLE IF NOT EXISTS table_name (...);

EOF

echo "✅ Migration erstellt: $FILENAME"
echo ""
echo "📝 Nächste Schritte:"
echo "1. Öffne die Datei und füge deine SQL-Statements ein"
echo "2. Ersetze FRESH-XXX mit der richtigen Ticket-Nummer"
echo "3. Ersetze Sprint X mit dem aktuellen Sprint"
echo "4. Beschreibe den Zweck der Migration"
echo "5. Teste die Migration lokal: cd backend && ./mvnw quarkus:dev"
echo "6. Aktualisiere MIGRATION_REGISTRY.md:"
echo "   ./scripts/update-migration-registry.sh"
echo ""
echo "💡 Tipp: Nutze 'code $FILENAME' um die Datei zu öffnen"