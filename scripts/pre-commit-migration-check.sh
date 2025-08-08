#!/bin/bash
# Pre-commit hook to prevent duplicate migration numbers
# Install: cp scripts/pre-commit-migration-check.sh .git/hooks/pre-commit && chmod +x .git/hooks/pre-commit

echo "🔍 Checking for migration duplicates..."

# Change to backend migration directory
MIGRATION_DIR="backend/src/main/resources/db/migration"

# Check if we have any SQL files being committed
if ! git diff --cached --name-only | grep -q "$MIGRATION_DIR.*\.sql$"; then
    echo "✅ No migration files in commit"
    exit 0
fi

# Extract all migration numbers from existing files
existing_numbers=$(find $MIGRATION_DIR -name "V*.sql" 2>/dev/null | \
    sed 's/.*V\([0-9]*\)__.*/\1/' | sort -n)

# Extract migration numbers from staged files
staged_numbers=$(git diff --cached --name-only | \
    grep "$MIGRATION_DIR.*\.sql$" | \
    sed 's/.*V\([0-9]*\)__.*/\1/')

# Check for duplicates in all files (existing + staged)
all_numbers=$(echo -e "$existing_numbers\n$staged_numbers" | sort -n)
duplicates=$(echo "$all_numbers" | uniq -d)

if [ ! -z "$duplicates" ]; then
    echo "🚨 FEHLER: Doppelte Migrationsnummern gefunden!"
    echo ""
    echo "Folgende Nummern sind bereits vergeben:"
    for num in $duplicates; do
        echo "  - V$num"
        find $MIGRATION_DIR -name "V${num}__*.sql" | sed 's/^/    /'
    done
    echo ""
    echo "📋 Nächste freie Nummer ermitteln:"
    echo "   cat docs/MIGRATION_REGISTRY.md | grep 'NÄCHSTE VERFÜGBARE'"
    echo ""
    echo "💡 Tipp: Nutze das Migration Creation Script:"
    echo "   ./scripts/create-migration.sh [nummer] [beschreibung]"
    echo ""
    echo "⚠️  Um trotzdem zu committen (NICHT EMPFOHLEN):"
    echo "   git commit --no-verify"
    exit 1
fi

# Check if MIGRATION_REGISTRY.md is updated when adding new migrations
if git diff --cached --name-only | grep -q "$MIGRATION_DIR.*\.sql$"; then
    if ! git diff --cached --name-only | grep -q "docs/MIGRATION_REGISTRY.md"; then
        echo "⚠️  WARNUNG: Neue Migration aber MIGRATION_REGISTRY.md nicht aktualisiert!"
        echo ""
        echo "📝 Bitte aktualisiere die Registry:"
        echo "   ./scripts/update-migration-registry.sh"
        echo "   git add docs/MIGRATION_REGISTRY.md"
        echo ""
        echo "Trotzdem committen? (y/N)"
        read -r response
        if [[ ! "$response" =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
fi

echo "✅ Keine Migrations-Duplikate gefunden"
exit 0