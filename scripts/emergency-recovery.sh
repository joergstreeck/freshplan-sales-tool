#!/bin/bash
# Emergency Recovery Script für Git-Katastrophen

echo "🚨 Git Emergency Recovery Tool"
echo "=============================="

# Aktuellen Status anzeigen
echo -e "\n📊 Aktueller Status:"
echo "Branch: $(git branch --show-current)"
echo "Änderungen:"
git status --short

# Recovery-Optionen
echo -e "\n🔧 Recovery-Optionen:"
echo "1) Alle Änderungen verwerfen (reset --hard)"
echo "2) Änderungen stashen und Branch wechseln"
echo "3) Einzelne Datei aus main wiederherstellen"
echo "4) Letzten Commit rückgängig machen"
echo "5) Reflog anzeigen (alle Aktionen)"
echo "6) Diagnose-Script ausführen"
echo "0) Abbrechen"

read -p "Wähle Option (0-6): " choice

case $choice in
    1)
        echo "⚠️  WARNUNG: Alle lokalen Änderungen werden verworfen!"
        read -p "Sicher? (y/n): " confirm
        if [ "$confirm" = "y" ]; then
            git reset --hard HEAD
            echo "✅ Änderungen verworfen"
        fi
        ;;
    2)
        backup_name="emergency-backup-$(date +%Y%m%d_%H%M%S)"
        git stash push -m "$backup_name"
        echo "✅ Änderungen gesichert als: $backup_name"
        git checkout main
        git pull origin main
        echo "✅ Auf main gewechselt und aktualisiert"
        ;;
    3)
        read -p "Dateipfad eingeben: " filepath
        if [ -n "$filepath" ]; then
            git checkout main -- "$filepath"
            echo "✅ $filepath aus main wiederhergestellt"
        fi
        ;;
    4)
        echo "Letzter Commit:"
        git log -1 --oneline
        read -p "Rückgängig machen? (y/n): " confirm
        if [ "$confirm" = "y" ]; then
            git reset --soft HEAD~1
            echo "✅ Commit rückgängig gemacht (Änderungen behalten)"
        fi
        ;;
    5)
        echo -e "\n📜 Git Reflog (letzte 20 Aktionen):"
        git reflog -20
        ;;
    6)
        if [ -f "./scripts/diagnose-problems.sh" ]; then
            ./scripts/diagnose-problems.sh
        else
            echo "❌ Diagnose-Script nicht gefunden"
        fi
        ;;
    0)
        echo "❌ Abgebrochen"
        exit 0
        ;;
    *)
        echo "❌ Ungültige Option"
        ;;
esac

echo -e "\n📊 Neuer Status:"
git status --short