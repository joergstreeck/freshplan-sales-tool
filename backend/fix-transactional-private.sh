#!/bin/bash

echo "🔍 Suche nach @Transactional private Methoden..."

# Finde alle Java Test-Dateien mit @Transactional private pattern
FILES=$(find src/test/java -name "*.java" -type f -exec grep -l "@Transactional" {} \; | xargs grep -l "private")

if [ -z "$FILES" ]; then
    echo "✅ Keine @Transactional private Methoden gefunden!"
    exit 0
fi

echo "📝 Gefundene Dateien mit @Transactional private Methoden:"
echo "$FILES"

# Für jede gefundene Datei
for file in $FILES; do
    echo ""
    echo "🔧 Bearbeite: $file"
    
    # Zeige die gefundenen Zeilen
    echo "Gefundene private Methoden mit @Transactional:"
    grep -B1 "private.*(" "$file" | grep -B1 "@Transactional" | grep -A1 "private"
    
    # Erstelle Backup
    cp "$file" "${file}.bak"
    
    # Ersetze @Transactional\n  private mit @Transactional\n  protected
    # Dies ist komplex in sed, daher nutzen wir perl
    perl -i -pe 's/(\@Transactional\s*\n\s*)private(\s+\w+\s+\w+\s*\()/${1}protected${2}/g' "$file"
    
    echo "✅ Datei aktualisiert: $file"
done

echo ""
echo "🎯 Fertig! Alle @Transactional private Methoden wurden auf protected geändert."
echo "💡 Tipp: Führe 'mvn test' aus um zu prüfen ob alle Tests funktionieren."