#!/bin/bash
# clean-placeholders.sh - Entfernt Platzhalter-Dokumente sicher
# Autor: Claude
# Datum: 21.07.2025

set -e

echo "🧹 PLATZHALTER-DOKUMENTE BEREINIGUNG"
echo "===================================="
echo ""

# Farben
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Backup-Verzeichnis
BACKUP_DIR="docs/features.backup.placeholders.$(date +%Y%m%d_%H%M%S)"

# 1. Finde alle Platzhalter
echo "🔍 Suche Platzhalter-Dokumente..."
PLACEHOLDERS=$(grep -r "Status:.*Placeholder Document" docs/features --include="*.md" -l 2>/dev/null || true)

if [ -z "$PLACEHOLDERS" ]; then
    echo -e "${GREEN}✅ Keine Platzhalter-Dokumente gefunden!${NC}"
    exit 0
fi

# Zähle gefundene Platzhalter
COUNT=$(echo "$PLACEHOLDERS" | wc -l | tr -d ' ')
echo -e "${YELLOW}📋 $COUNT Platzhalter-Dokumente gefunden${NC}"

# 2. Zeige Vorschau
echo ""
echo "📄 Gefundene Platzhalter:"
echo "$PLACEHOLDERS" | head -10 | while read file; do
    echo "   - $file"
done

if [ "$COUNT" -gt 10 ]; then
    echo "   ... und $((COUNT - 10)) weitere"
fi

# 3. Bestätigung
echo ""
echo -e "${YELLOW}⚠️  Diese Dateien werden gelöscht (mit Backup)${NC}"
read -p "Fortfahren? (j/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Jj]$ ]]; then
    echo "❌ Abgebrochen"
    exit 1
fi

# 4. Erstelle Backup
echo ""
echo "💾 Erstelle Backup in $BACKUP_DIR..."
mkdir -p "$BACKUP_DIR"

# Kopiere Struktur und Dateien
echo "$PLACEHOLDERS" | while read file; do
    # Erhalte Verzeichnisstruktur im Backup
    dir=$(dirname "$file")
    backup_dir="$BACKUP_DIR/${dir#docs/features/}"
    mkdir -p "$backup_dir"
    
    # Kopiere Datei
    cp "$file" "$backup_dir/"
    echo -e "   ${GREEN}✓${NC} Backup: $file"
done

# 5. Lösche Platzhalter
echo ""
echo "🗑️  Lösche Platzhalter-Dokumente..."
DELETED=0

echo "$PLACEHOLDERS" | while read file; do
    if rm "$file"; then
        echo -e "   ${GREEN}✓${NC} Gelöscht: $file"
        ((DELETED++))
    else
        echo -e "   ${RED}✗${NC} Fehler beim Löschen: $file"
    fi
done

# 6. Aufräumen leerer Verzeichnisse
echo ""
echo "🧹 Räume leere Verzeichnisse auf..."
find docs/features -type d -empty -delete 2>/dev/null || true

# 7. Zusammenfassung
echo ""
echo "===================================="
echo -e "${GREEN}✅ BEREINIGUNG ABGESCHLOSSEN${NC}"
echo "===================================="
echo "📊 Ergebnis:"
echo "   - $COUNT Platzhalter gefunden"
echo "   - $COUNT Dateien gesichert in: $BACKUP_DIR"
echo "   - $COUNT Platzhalter gelöscht"
echo ""
echo "💡 Backup wiederherstellen mit:"
echo "   cp -r $BACKUP_DIR/* docs/features/"
echo ""
echo "🔍 Struktur validieren mit:"
echo "   ./scripts/validate-structure.sh"