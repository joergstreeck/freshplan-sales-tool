#!/bin/bash
# =================================================================
# update-migration-registry.sh - Automatische Registry-Aktualisierung
# =================================================================
# Dieses Script analysiert alle Migrationen und aktualisiert die Registry
# Verwendung: ./scripts/update-migration-registry.sh

set -e

REGISTRY_FILE="docs/MIGRATION_REGISTRY.md"
MIGRATION_DIR="backend/src/main/resources/db/migration"

echo "🔍 Analysiere Migrationen..."

# Finde höchste Migrationsnummer
HIGHEST_VERSION=$(find "$MIGRATION_DIR" -name "V*.sql" | \
  sed 's/.*\/V\([0-9]*\)__.*/\1/' | \
  sort -n | tail -1)

echo "📊 Höchste gefundene Version: V$HIGHEST_VERSION"

# Nächste verfügbare Nummer
NEXT_VERSION=$((HIGHEST_VERSION + 1))

echo "🆕 Nächste verfügbare Version: V$NEXT_VERSION"

# Prüfe auf Duplikate
echo "🔍 Prüfe auf Duplikate..."
DUPLICATES=$(find "$MIGRATION_DIR" -name "V*.sql" | \
  xargs -I {} basename {} | \
  sed 's/V\([0-9]*\)__.*/\1/' | \
  sort -n | uniq -d)

if [ -n "$DUPLICATES" ]; then
    echo "⚠️  WARNUNG: Duplikate gefunden!"
    echo "$DUPLICATES" | while read dup; do
        echo "   - V$dup ist mehrfach vorhanden!"
        find "$MIGRATION_DIR" -name "V${dup}__*.sql" | xargs basename
    done
else
    echo "✅ Keine Duplikate gefunden"
fi

# Update Registry-Datei
if [ -f "$REGISTRY_FILE" ]; then
    # Update "Höchste vergebene Nummer"
    sed -i '' "s/\*\*Höchste vergebene Nummer:\*\* V[0-9]*/\*\*Höchste vergebene Nummer:\*\* V$HIGHEST_VERSION/" "$REGISTRY_FILE"
    
    # Update "NÄCHSTE VERFÜGBARE NUMMER"
    sed -i '' "s/## 🆕 NÄCHSTE VERFÜGBARE NUMMER: V[0-9]*/## 🆕 NÄCHSTE VERFÜGBARE NUMMER: V$NEXT_VERSION/" "$REGISTRY_FILE"
    
    # Update Datum
    CURRENT_DATE=$(date +"%d.%m.%Y, %H:%M")
    sed -i '' "s/\*\*Letzte Aktualisierung:\*\* .*/\*\*Letzte Aktualisierung:\*\* $CURRENT_DATE/" "$REGISTRY_FILE"
    
    echo "✅ Registry aktualisiert"
else
    echo "❌ Registry-Datei nicht gefunden: $REGISTRY_FILE"
    exit 1
fi

# Zeige alle Migrationen
echo ""
echo "📋 Aktuelle Migrationen:"
find "$MIGRATION_DIR" -name "V*.sql" | sort -V | while read file; do
    VERSION=$(basename "$file" | sed 's/V\([0-9]*\)__.*/\1/')
    NAME=$(basename "$file")
    echo "   V$VERSION: $NAME"
done

echo ""
echo "✅ Fertig! Nächste freie Nummer: V$NEXT_VERSION"
echo "📝 Vergiss nicht, die Registry nach neuen Migrationen zu committen!"