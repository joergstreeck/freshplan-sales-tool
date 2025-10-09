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

cat > .git/hooks/pre-commit << 'HOOKEOF'
#!/bin/bash
# Auto-installed Pre-Commit Hook
# Führt Migration Safety Check aus

# Rufe das eigentliche Script auf
exec ./scripts/pre-commit-migration-check.sh
HOOKEOF

# Mache Hook ausführbar
chmod +x .git/hooks/pre-commit

echo "✅ Pre-commit Hook installiert!"
echo ""
echo "Test:"
echo "  1. Erstelle eine Test-Migration im falschen Ordner"
echo "  2. git add + git commit"
echo "  3. Hook sollte committen blocken"
echo ""
echo "Deinstallieren:"
echo "  rm .git/hooks/pre-commit"
