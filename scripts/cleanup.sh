#!/bin/bash

# FreshPlan Sales Tool - Automatic Cleanup Script
# Entfernt Build-Artefakte und temporäre Dateien sicher
# Erstellt: 08.06.2025

echo "🧹 FreshPlan Sales Tool - Cleanup gestartet..."
echo "📅 $(date)"
echo ""

# Farben für bessere Ausgabe
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funktion für sichere Löschung mit Bestätigung
safe_remove() {
    local path="$1"
    local description="$2"
    local criticality="${3:-medium}"  # low, medium, high
    
    if [ -e "$path" ]; then
        local size=$(du -sh "$path" 2>/dev/null | cut -f1)
        echo -e "${YELLOW}Gefunden:${NC} $description ($size)"
        echo -e "${BLUE}Pfad:${NC} $path"
        
        # Kritische Dateien IMMER nachfragen, auch ohne INTERACTIVE
        local should_ask=false
        if [ "$INTERACTIVE" = "true" ] || [ "$criticality" = "high" ]; then
            should_ask=true
        fi
        
        if [ "$should_ask" = "true" ]; then
            if [ "$criticality" = "high" ]; then
                echo -e "${RED}⚠️  KRITISCH: Diese Datei könnte wichtig sein!${NC}"
            fi
            read -p "Wirklich löschen? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                rm -rf "$path"
                echo -e "${GREEN}✅ Gelöscht${NC}"
            else
                echo -e "${YELLOW}⏭️  Sicher übersprungen${NC}"
            fi
        else
            # Nur bei "low" criticality automatisch löschen
            if [ "$criticality" = "low" ]; then
                rm -rf "$path"
                echo -e "${GREEN}✅ Automatisch gelöscht (unkritisch)${NC}"
            else
                echo -e "${YELLOW}⏭️  Übersprungen (verwende INTERACTIVE=true für Entscheidung)${NC}"
            fi
        fi
        echo ""
    fi
}

# Basis-Cleanup (sicher)
echo -e "${BLUE}🗂️  SCHRITT 1: Basis-Cleanup (sichere Dateien)${NC}"
echo "======================================================"

# macOS System-Dateien
echo "🍎 macOS System-Dateien..."
find . -name ".DS_Store" -type f -not -path "*/node_modules/*" -delete 2>/dev/null
find . -name ".Spotlight-V100" -type d -not -path "*/node_modules/*" -exec rm -rf {} + 2>/dev/null
find . -name ".Trashes" -type d -not -path "*/node_modules/*" -exec rm -rf {} + 2>/dev/null
echo -e "${GREEN}✅ macOS Dateien bereinigt${NC}"

# Editor-Backups
echo "📝 Editor-Backup-Dateien..."
find . -name "*~" -type f -not -path "*/node_modules/*" -delete 2>/dev/null
find . -name "*.swp" -type f -not -path "*/node_modules/*" -delete 2>/dev/null
find . -name "*.swo" -type f -not -path "*/node_modules/*" -delete 2>/dev/null
echo -e "${GREEN}✅ Editor-Backups bereinigt${NC}"

# Temporäre Dateien
echo "🗃️  Temporäre Dateien..."
find . -name "*.tmp" -type f -not -path "*/node_modules/*" -delete 2>/dev/null
find . -name "*.temp" -type f -not -path "*/node_modules/*" -delete 2>/dev/null
echo -e "${GREEN}✅ Temporäre Dateien bereinigt${NC}"

echo ""

# Build-Artefakte (vorsichtiger)
echo -e "${BLUE}🏗️  SCHRITT 2: Build-Artefakte${NC}"
echo "======================================"

# Frontend dist (kann neu gebaut werden)
safe_remove "./frontend/dist" "Frontend Build-Output" "medium"

# Coverage Reports (können neu generiert werden)
safe_remove "./frontend/coverage" "Frontend Test Coverage" "low"
safe_remove "./backend/target/site" "Backend Test Reports" "low"

# Logs
echo "📋 Log-Dateien..."
find . -name "*.log" -type f -not -path "*/node_modules/*" -mtime +7 -delete 2>/dev/null
echo -e "${GREEN}✅ Alte Logs (>7 Tage) bereinigt${NC}"

echo ""

# Entwicklungsumgebungen (nur wenn ungenutzt)
echo -e "${BLUE}🐍 SCHRITT 3: Entwicklungsumgebungen${NC}"
echo "==========================================="

# Python venv (nur wenn nicht aktiv)
if [ -d "./venv" ] && [ -z "$VIRTUAL_ENV" ]; then
    safe_remove "./venv" "Python Virtual Environment (inaktiv)" "high"
fi

if [ -d "./.venv" ] && [ -z "$VIRTUAL_ENV" ]; then
    safe_remove "./.venv" "Python Virtual Environment (inaktiv)" "high"
fi

echo ""

# Backup-Ordner (älter als 30 Tage)
echo -e "${BLUE}🗄️  SCHRITT 4: Alte Backup-Ordner${NC}"
echo "======================================="

find . -maxdepth 1 -name "*-backup-*" -type d -mtime +30 -exec basename {} \; | while read backup_dir; do
    if [ -d "./$backup_dir" ]; then
        safe_remove "./$backup_dir" "Alter Backup-Ordner (>30 Tage)" "medium"
    fi
done

echo ""

# Zusammenfassung
echo -e "${GREEN}🎉 CLEANUP ABGESCHLOSSEN!${NC}"
echo "=========================="
echo "📊 Aktueller Speicherverbrauch:"
du -sh . 2>/dev/null | head -1

echo ""
echo -e "${BLUE}💡 TIPP:${NC} Für interaktiven Modus: INTERACTIVE=true ./scripts/cleanup.sh"
echo -e "${BLUE}💡 TIPP:${NC} Für mehr Details: ./scripts/cleanup.sh --verbose"
echo ""
echo "✅ Alle temporären Dateien wurden bereinigt."
echo "⚠️  Build-Artefakte können mit 'npm run build' neu erstellt werden."