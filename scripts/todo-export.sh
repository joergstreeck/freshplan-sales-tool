#!/bin/bash

# Script: todo-export.sh
# Zweck: Placeholder für TODO-Export - muss manuell gefüllt werden
# Autor: Claude
# Datum: 13.07.2025

# Ausgabedatei
TODO_FILE=".current-todos.md"

cat > "$TODO_FILE" << 'EOF'
## 📋 AKTUELLE TODO-LISTE

### ⚠️ MANUELLE EINGABE ERFORDERLICH
Claude muss hier die TODOs einfügen nach diesem Format:

#### 🔴 Offene TODOs (High Priority):
- [ ] [ID: xxx] Beschreibung (status: pending/in_progress)

#### 🟡 Offene TODOs (Medium Priority):
- [ ] [ID: yyy] Beschreibung (status: pending)

#### 🟢 Offene TODOs (Low Priority):
- [ ] [ID: zzz] Beschreibung (status: pending)

#### ✅ Erledigte TODOs (diese Session):
- [x] [ID: aaa] Beschreibung (status: completed)

**Zuletzt aktualisiert:** $(date +"%Y-%m-%d %H:%M")
EOF

echo "📋 TODO-Export Template erstellt: $TODO_FILE"
echo ""
echo "WICHTIG: Claude muss jetzt TodoRead ausführen und die TODOs hier eintragen!"
echo "Dann kann create-handover.sh diese Datei einlesen."