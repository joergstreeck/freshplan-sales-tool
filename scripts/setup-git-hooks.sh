#!/bin/bash
# Setup Git Hooks für Migration Safety

echo "🔧 Git Hooks Setup..."

# Prüfe ob wir in einem Git Repo sind
if [ ! -d ".git" ]; then
    echo "❌ FEHLER: Nicht in einem Git Repository!"
    exit 1
fi

# Erstelle .git/hooks falls nicht existiert
mkdir -p .git/hooks

# Installiere pre-commit Hook
echo "📝 Installiere pre-commit Hook..."

# Prüfe ob Hook bereits existiert
if [ -f .git/hooks/pre-commit ]; then
    echo "⚠️  Pre-commit Hook existiert bereits!"

    # Prüfe ob Migration-Check schon drin ist
    if grep -q "pre-commit-migration-check.sh" .git/hooks/pre-commit; then
        echo "ℹ️  Migration-Check ist bereits installiert."
        echo "✅ Nichts zu tun!"
        exit 0
    fi

    echo ""
    echo "Möchtest du den Migration-Check zum bestehenden Hook hinzufügen?"
    echo "(Der bestehende Hook wird erweitert, nicht ersetzt)"
    read -p "Hinzufügen? (y/n): " answer

    if [ "$answer" != "y" ] && [ "$answer" != "Y" ]; then
        echo "❌ Abgebrochen."
        exit 1
    fi

    # Backup erstellen
    cp .git/hooks/pre-commit .git/hooks/pre-commit.backup
    echo "📦 Backup erstellt: .git/hooks/pre-commit.backup"

    # Füge Migration-Check hinzu (vor exit 0 oder am Ende)
    # Entferne trailing exit 0 falls vorhanden
    sed -i '' '/^exit 0$/d' .git/hooks/pre-commit

    # Füge Migration-Check hinzu
    cat >> .git/hooks/pre-commit << 'HOOKEOF'

# Migration Safety Check
if [ -f "./scripts/pre-commit-migration-check.sh" ]; then
    ./scripts/pre-commit-migration-check.sh
    if [ $? -ne 0 ]; then
        exit 1
    fi
fi

exit 0
HOOKEOF

    echo "✅ Migration-Check zum bestehenden Hook hinzugefügt!"
else
    # Erstelle neuen Hook
    cat > .git/hooks/pre-commit << 'HOOKEOF'
#!/bin/bash
# Auto-installed Pre-Commit Hook
# Führt Migration Safety Check aus

./scripts/pre-commit-migration-check.sh || exit 1

exit 0
HOOKEOF

    chmod +x .git/hooks/pre-commit
    echo "✅ Pre-commit Hook neu erstellt!"
fi

echo ""
echo "Test:"
echo "  1. Erstelle eine Test-Migration im falschen Ordner"
echo "  2. git add + git commit"
echo "  3. Hook sollte committen blocken"
echo ""
echo "Deinstallieren:"
echo "  rm .git/hooks/pre-commit"
echo "  # Oder Backup wiederherstellen:"
echo "  mv .git/hooks/pre-commit.backup .git/hooks/pre-commit"
