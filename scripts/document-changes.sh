#!/bin/bash

# FreshPlan Automatische Änderungs-Dokumentation
# Dokumentiert wichtige Änderungen automatisch

echo "📝 Dokumentiere wichtige Änderungen..."

CHANGELOG_DIR="docs/development/changelog"
TODAY=$(date +"%Y-%m-%d")
CHANGELOG_FILE="$CHANGELOG_DIR/$TODAY-changes.md"

# Erstelle Verzeichnis falls nicht vorhanden
mkdir -p $CHANGELOG_DIR

# Header für heutiges Changelog
cat > $CHANGELOG_FILE << EOF
# Änderungen vom $TODAY

**Automatisch generiert um $(date +"%H:%M Uhr")**

## 🚀 Features
EOF

# Features aus Commits
git log --since="midnight" --pretty=format:"- %s" --grep="^feat" >> $CHANGELOG_FILE || echo "Keine neuen Features heute." >> $CHANGELOG_FILE

cat >> $CHANGELOG_FILE << EOF

## 🐛 Fixes
EOF

# Fixes aus Commits
git log --since="midnight" --pretty=format:"- %s" --grep="^fix" >> $CHANGELOG_FILE || echo "Keine Fixes heute." >> $CHANGELOG_FILE

cat >> $CHANGELOG_FILE << EOF

## 🔧 Refactoring
EOF

# Refactoring aus Commits
git log --since="midnight" --pretty=format:"- %s" --grep="^refactor" >> $CHANGELOG_FILE || echo "Kein Refactoring heute." >> $CHANGELOG_FILE

cat >> $CHANGELOG_FILE << EOF

## 📊 Geänderte Dateien

### Backend
EOF

# Backend-Änderungen
git diff --name-only HEAD~10..HEAD | grep "^backend/" | sed 's/^/- /' >> $CHANGELOG_FILE || echo "Keine Backend-Änderungen." >> $CHANGELOG_FILE

cat >> $CHANGELOG_FILE << EOF

### Frontend
EOF

# Frontend-Änderungen
git diff --name-only HEAD~10..HEAD | grep "^frontend/" | sed 's/^/- /' >> $CHANGELOG_FILE || echo "Keine Frontend-Änderungen." >> $CHANGELOG_FILE

cat >> $CHANGELOG_FILE << EOF

### Dokumentation
EOF

# Dokumentations-Änderungen
git diff --name-only HEAD~10..HEAD | grep "\.md$" | sed 's/^/- /' >> $CHANGELOG_FILE || echo "Keine Dokumentations-Änderungen." >> $CHANGELOG_FILE

# Kritische Änderungen erkennen
echo "" >> $CHANGELOG_FILE
echo "## ⚠️ Kritische Änderungen" >> $CHANGELOG_FILE

# Suche nach Breaking Changes
git log --since="midnight" --pretty=format:"- %s" --grep="BREAKING" >> $CHANGELOG_FILE || echo "Keine Breaking Changes." >> $CHANGELOG_FILE

# Schema-Änderungen
if git diff --name-only HEAD~10..HEAD | grep -q "migration"; then
    echo "- ⚠️ Datenbank-Schema geändert!" >> $CHANGELOG_FILE
fi

# API-Änderungen
if git diff --name-only HEAD~10..HEAD | grep -q "API_CONTRACT\|api/"; then
    echo "- ⚠️ API-Änderungen erkannt!" >> $CHANGELOG_FILE
fi

echo "" >> $CHANGELOG_FILE
echo "---" >> $CHANGELOG_FILE
echo "*Generiert von scripts/document-changes.sh*" >> $CHANGELOG_FILE

echo "✅ Änderungen dokumentiert in: $CHANGELOG_FILE"

# Optional: Wichtige Änderungen in KNOWN_ISSUES.md eintragen
if grep -q "BREAKING\|kritisch" $CHANGELOG_FILE; then
    echo ""
    echo "⚠️  Kritische Änderungen erkannt! Bitte KNOWN_ISSUES.md manuell aktualisieren."
fi