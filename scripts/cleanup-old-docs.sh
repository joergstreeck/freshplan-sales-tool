#!/bin/bash

# FreshPlan Dokumentations-Bereinigung
# Hält die Dokumentation sauber und aktuell

echo "🧹 FreshPlan Dokumentations-Bereinigung"
echo "======================================"
echo ""

# Konfiguration
CLAUDE_DAILY_DIR="docs/claude-daily"
ARCHIVE_DIR="docs/archive"
DAYS_UNTIL_REVIEW=7
DAYS_UNTIL_ARCHIVE=30

# Farben
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m'

# Statistiken
REVIEWED=0
ARCHIVED=0
DELETED=0

# 1. Finde alte Claude-Dokumente
echo "📅 Prüfe Dokumente älter als $DAYS_UNTIL_REVIEW Tage..."
echo ""

# Docs für Review (7-30 Tage alt)
find $CLAUDE_DAILY_DIR -name "*.md" -mtime +$DAYS_UNTIL_REVIEW -mtime -$DAYS_UNTIL_ARCHIVE 2>/dev/null | while read file; do
    echo -e "${YELLOW}⚠️  Review benötigt: $file${NC}"
    
    # Extrahiere wichtige Patterns
    if grep -q "WICHTIG\|TODO\|BREAKING\|Security" "$file"; then
        echo "   → Enthält wichtige Informationen!"
        
        # Erstelle Review-Task
        basename=$(basename "$file")
        echo "- [ ] Review: $basename" >> docs/REVIEW_QUEUE.md
    fi
    
    ((REVIEWED++))
done

# 2. Archiviere sehr alte Docs (>30 Tage)
echo ""
echo "📦 Archiviere Dokumente älter als $DAYS_UNTIL_ARCHIVE Tage..."

mkdir -p $ARCHIVE_DIR

find $CLAUDE_DAILY_DIR -name "*.md" -mtime +$DAYS_UNTIL_ARCHIVE 2>/dev/null | while read file; do
    echo -e "${YELLOW}📦 Archiviere: $file${NC}"
    
    # Erstelle Archiv-Struktur
    year=$(date -r "$file" +%Y)
    month=$(date -r "$file" +%m)
    mkdir -p "$ARCHIVE_DIR/$year/$month"
    
    # Verschiebe ins Archiv
    mv "$file" "$ARCHIVE_DIR/$year/$month/"
    ((ARCHIVED++))
done

# 3. Lösche leere Dokumentationen
echo ""
echo "🗑️  Lösche leere Dokumente..."

find docs -name "*.md" -type f -empty 2>/dev/null | while read file; do
    # Schütze wichtige Docs
    if [[ ! "$file" =~ (MASTER_BRIEFING|CLAUDE\.md|README|API_CONTRACT) ]]; then
        echo -e "${RED}🗑️  Lösche leer: $file${NC}"
        rm "$file"
        ((DELETED++))
    fi
done

# 4. Finde und melde Duplikate
echo ""
echo "🔍 Suche nach möglichen Duplikaten..."

# Finde Dateien mit ähnlichen Namen
find docs -name "*.md" -type f 2>/dev/null | sort | awk -F/ '{print $NF}' | uniq -d | while read dup; do
    echo -e "${YELLOW}⚠️  Mögliches Duplikat: $dup${NC}"
    find docs -name "$dup" -type f
    echo ""
done

# 5. Komprimiere alte Logs
echo ""
echo "🗜️  Komprimiere alte Log-Dateien..."

find . -name "*.log" -mtime +7 -type f ! -name "*.gz" 2>/dev/null | while read log; do
    echo "   Komprimiere: $log"
    gzip "$log"
done

# 6. Erstelle Cleanup-Report
REPORT="docs/claude-daily/$(date +%Y-%m-%d)_CLEANUP_REPORT.md"
cat > "$REPORT" << EOF
# Dokumentations-Bereinigung Report

**Datum:** $(date +"%Y-%m-%d %H:%M")

## 📊 Statistiken

- **Dokumente zur Review:** $REVIEWED
- **Archivierte Dokumente:** $ARCHIVED  
- **Gelöschte leere Dateien:** $DELETED

## 📋 Nächste Schritte

1. Prüfe \`docs/REVIEW_QUEUE.md\` für wichtige Inhalte
2. Integriere relevante Infos in Master-Docs
3. Lösche veraltete Informationen

## 🎯 Empfehlungen

- Nutze "Write Less, Update More" Prinzip
- Aktualisiere bestehende Docs statt neue zu erstellen
- Führe dieses Script wöchentlich aus

---
*Automatisch generiert von cleanup-old-docs.sh*
EOF

# 7. Zusammenfassung
echo ""
echo -e "${GREEN}✅ Bereinigung abgeschlossen!${NC}"
echo ""
echo "📊 Zusammenfassung:"
echo "   - $REVIEWED Dokumente zur Review markiert"
echo "   - $ARCHIVED Dokumente archiviert"
echo "   - $DELETED leere Dateien gelöscht"
echo ""
echo "📋 Report erstellt: $REPORT"
echo ""

# 8. Optional: Zeige Review-Queue
if [ -f "docs/REVIEW_QUEUE.md" ]; then
    echo "📌 Review-Queue:"
    tail -5 docs/REVIEW_QUEUE.md
fi