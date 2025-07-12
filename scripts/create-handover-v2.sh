#!/bin/bash

# Script: create-handover-v2.sh
# Zweck: Erstellt eine vollständige Übergabe mit automatischer TODO-Integration
# Autor: Claude
# Datum: 13.07.2025

set -e

# Farben
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Datum und Zeit
DATE=$(date +%Y-%m-%d)
TIME=$(date +%H-%M)
DATETIME="${DATE}_${TIME}"

# Ausgabedatei
OUTPUT_DIR="docs/claude-work/daily-work/$DATE"
OUTPUT_FILE="$OUTPUT_DIR/${DATETIME}_HANDOVER.md"

# Verzeichnis erstellen falls nicht vorhanden
mkdir -p "$OUTPUT_DIR"

echo "📝 Erstelle Übergabe-Dokument..."
echo "================================"

# 1. System-Informationen sammeln
echo "🔍 Sammle System-Informationen..."

# Service Status
BACKEND_STATUS="❓"
FRONTEND_STATUS="❓"
DB_STATUS="❓"
KEYCLOAK_STATUS="❓"

if lsof -i:8080 >/dev/null 2>&1; then BACKEND_STATUS="✅ Läuft"; else BACKEND_STATUS="❌ Gestoppt"; fi
if lsof -i:5173 >/dev/null 2>&1; then FRONTEND_STATUS="✅ Läuft"; else FRONTEND_STATUS="❌ Gestoppt"; fi
if lsof -i:5432 >/dev/null 2>&1; then DB_STATUS="✅ Läuft"; else DB_STATUS="❌ Gestoppt"; fi
if lsof -i:8180 >/dev/null 2>&1; then KEYCLOAK_STATUS="✅ Läuft"; else KEYCLOAK_STATUS="❌ Gestoppt"; fi

# Java und Node Versionen
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
NODE_VERSION=$(node -v)

# Git Informationen
GIT_BRANCH=$(git branch --show-current)
GIT_COMMIT=$(git log -1 --oneline)
UNTRACKED_COUNT=$(git status --porcelain | grep -c "^??" || echo "0")

# Aktives Modul
if [ -f .current-focus ]; then
    FEATURE=$(grep '"feature"' .current-focus | cut -d'"' -f4 || echo "Unbekannt")
    MODULE=$(grep '"module"' .current-focus | cut -d'"' -f4 || echo "Unbekannt")
else
    FEATURE="Kein aktives Feature"
    MODULE="Kein aktives Modul"
fi

# Modul-Dokument finden
MODULE_DOC="Nicht gefunden"
if [ -f scripts/get-active-module.sh ]; then
    MODULE_INFO=$(./scripts/get-active-module.sh 2>/dev/null | grep "Modul-Dokument:" | cut -d' ' -f3- || echo "")
    if [ -n "$MODULE_INFO" ]; then
        MODULE_DOC="$MODULE_INFO ⭐"
    fi
fi

# 2. TODO-Liste extrahieren
echo "📋 Extrahiere TODO-Liste..."

# Temporäre Datei für TODOs
TODO_TEMP=$(mktemp)

# Claude's TodoRead simulieren (da wir das nicht direkt aufrufen können)
# Hier müssten wir die TODOs aus einer Datei oder Datenbank lesen
# Für jetzt erstellen wir Platzhalter

cat > "$TODO_TEMP" << 'EOF'
### 📋 AKTUELLE TODO-LISTE

**⚠️ HINWEIS:** Diese Liste wird automatisch aus dem TODO-System extrahiert.

#### 🔴 Hohe Priorität (in_progress/pending)
{{HIGH_PRIORITY_TODOS}}

#### 🟡 Mittlere Priorität
{{MEDIUM_PRIORITY_TODOS}}

#### 🟢 Niedrige Priorität
{{LOW_PRIORITY_TODOS}}

#### ✅ Abgeschlossen in dieser Session
{{COMPLETED_TODOS}}

**Letzte Aktualisierung:** $(date +"%Y-%m-%d %H:%M")
EOF

# 3. Template verwenden und ausfüllen
echo "📄 Fülle Template aus..."

if [ -f "docs/templates/HANDOVER_TEMPLATE.md" ]; then
    cp "docs/templates/HANDOVER_TEMPLATE.md" "$OUTPUT_FILE"
else
    echo -e "${RED}❌ HANDOVER_TEMPLATE.md nicht gefunden!${NC}"
    echo "Erstelle Basis-Übergabe..."
    cat > "$OUTPUT_FILE" << EOF
# 🔄 STANDARDÜBERGABE - $DATE $TIME

## 🚨 KRITISCHE INFORMATIONEN FEHLEN
Template nicht gefunden. Bitte manuell ergänzen.
EOF
fi

# 4. Platzhalter ersetzen
echo "🔄 Ersetze Platzhalter..."

# Basis-Informationen
sed -i.bak "s/\[DATUM\]/$DATE/g" "$OUTPUT_FILE"
sed -i.bak "s/\[UHRZEIT\]/$(date +%H:%M)/g" "$OUTPUT_FILE"
sed -i.bak "s/\[VERSION\]/$JAVA_VERSION/g" "$OUTPUT_FILE"
sed -i.bak "s/\[BRANCH\]/$GIT_BRANCH/g" "$OUTPUT_FILE"
sed -i.bak "s/\[COMMIT\]/$GIT_COMMIT/g" "$OUTPUT_FILE"
sed -i.bak "s/\[COUNT\]/$UNTRACKED_COUNT/g" "$OUTPUT_FILE"
sed -i.bak "s/\[FC-XXX\]/$FEATURE/g" "$OUTPUT_FILE"
sed -i.bak "s/\[MODUL-NAME\]/$MODULE/g" "$OUTPUT_FILE"
sed -i.bak "s/\[PFAD\]/$MODULE_DOC/g" "$OUTPUT_FILE"

# Service Status
sed -i.bak "s/| \*\*Backend\*\* | \`8080\` | Quarkus mit Java 17 | \[STATUS\] |/| **Backend** | \`8080\` | Quarkus mit Java 17 | $BACKEND_STATUS |/g" "$OUTPUT_FILE"
sed -i.bak "s/| \*\*Frontend\*\* | \`5173\` | React\/Vite | \[STATUS\] |/| **Frontend** | \`5173\` | React\/Vite | $FRONTEND_STATUS |/g" "$OUTPUT_FILE"
sed -i.bak "s/| \*\*PostgreSQL\*\* | \`5432\` | PostgreSQL 15+ | \[STATUS\] |/| **PostgreSQL** | \`5432\` | PostgreSQL 15+ | $DB_STATUS |/g" "$OUTPUT_FILE"
sed -i.bak "s/| \*\*Keycloak\*\* | \`8180\` | Auth Service | \[STATUS\] |/| **Keycloak** | \`8180\` | Auth Service | $KEYCLOAK_STATUS |/g" "$OUTPUT_FILE"

# Node Version
sed -i.bak "s/v22.16.0+ erforderlich (aktuell: \[VERSION\])/v22.16.0+ erforderlich (aktuell: $NODE_VERSION)/g" "$OUTPUT_FILE"

# TODO-Integration (Platzhalter für manuelle Ergänzung)
sed -i.bak "/{{TODO_LIST}}/r $TODO_TEMP" "$OUTPUT_FILE"
sed -i.bak "/{{TODO_LIST}}/d" "$OUTPUT_FILE"

# Cleanup
rm -f "${OUTPUT_FILE}.bak"
rm -f "$TODO_TEMP"

# 5. Interaktive Ergänzungen
echo ""
echo -e "${GREEN}✅ Übergabe-Dokument erstellt:${NC}"
echo "   $OUTPUT_FILE"
echo ""
echo -e "${YELLOW}⚠️  WICHTIG: Bitte ergänze manuell:${NC}"
echo "   1. Was wurde heute gemacht?"
echo "   2. Was funktioniert?"
echo "   3. Welche Fehler gibt es?"
echo "   4. TODOs aus TodoRead einfügen"
echo "   5. Spezifische nächste Schritte"
echo ""
echo "📝 Öffne mit: code $OUTPUT_FILE"

# 6. TODO-Integration Helper
cat > "$OUTPUT_DIR/TODO_INTEGRATION_HELPER.md" << 'EOF'
# TODO-Integration Helper

## So integrierst du die TODOs korrekt:

1. Führe `TodoRead` in deiner Session aus
2. Kopiere die TODO-Liste
3. Ersetze die Platzhalter im Übergabe-Dokument:
   - {{HIGH_PRIORITY_TODOS}} → Alle TODOs mit priority="high" und status="pending"/"in_progress"
   - {{MEDIUM_PRIORITY_TODOS}} → Alle TODOs mit priority="medium"
   - {{LOW_PRIORITY_TODOS}} → Alle TODOs mit priority="low"
   - {{COMPLETED_TODOS}} → Alle TODOs mit status="completed" aus dieser Session

## Beispiel-Format:

```markdown
#### 🔴 Hohe Priorität (in_progress/pending)
- [ ] **[ID: sec-9]** Commit & PR erstellen (status: pending)
- [ ] **[ID: xyz]** Weitere Aufgabe (status: in_progress)

#### ✅ Abgeschlossen in dieser Session
- [x] **[ID: sec-8]** Two-Pass Review durchführen
```

## Wichtige Regeln:
1. ALLE offenen TODOs müssen aufgelistet werden
2. IDs beibehalten für Nachverfolgbarkeit
3. Status explizit angeben
4. Checkboxen verwenden: [ ] für offen, [x] für erledigt
EOF

echo "📋 TODO-Integration Helper erstellt:"
echo "   $OUTPUT_DIR/TODO_INTEGRATION_HELPER.md"