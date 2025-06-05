#!/bin/bash

# Backup-Script für FreshPlan Konfigurationen
# Erstellt automatische Backups der wichtigen Konfigurationsdateien

BACKUP_DIR="backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "🔒 Erstelle Backup der Konfigurationen..."

# UI-Konfiguration
cp config/ui.config.json "$BACKUP_DIR/"
echo "✓ UI-Konfiguration gesichert"

# Business Rules
cp config/business-rules.config.json "$BACKUP_DIR/"
echo "✓ Geschäftsregeln gesichert"

# Text Content
cp config/text-content.config.json "$BACKUP_DIR/"
echo "✓ Texte und Übersetzungen gesichert"

# Standalone HTML (stabile Version)
cp freshplan-complete.html "$BACKUP_DIR/"
echo "✓ Standalone-Version gesichert"

# Erstelle Info-Datei
cat > "$BACKUP_DIR/backup-info.txt" << EOF
FreshPlan Configuration Backup
==============================
Datum: $(date)
Version: $(git describe --tags --always 2>/dev/null || echo "untagged")
Branch: $(git branch --show-current 2>/dev/null || echo "unknown")

Gesicherte Dateien:
- ui.config.json: UI/Design-Einstellungen
- business-rules.config.json: Geschäftslogik und Rabatte
- text-content.config.json: Alle Texte und Übersetzungen
- freshplan-complete.html: Funktionierende Standalone-Version

Wiederherstellung:
cp backups/[BACKUP_FOLDER]/* config/
EOF

echo "✅ Backup erfolgreich erstellt in: $BACKUP_DIR"
echo ""
echo "📋 Tipp: Führen Sie dieses Script vor größeren Änderungen aus!"