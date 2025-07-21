#!/bin/bash
# new-feature.sh - Erstellt neues Feature mit korrekter Struktur
# Autor: Claude
# Datum: 21.07.2025
# Verwendung: ./scripts/new-feature.sh "Feature Name" [FC-Code]

set -e

# Farben
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Parameter
FEATURE_NAME="$1"
CUSTOM_CODE="$2"

# Validierung
if [ -z "$FEATURE_NAME" ]; then
    echo -e "${RED}❌ Fehler: Feature-Name erforderlich${NC}"
    echo "Verwendung: $0 \"Feature Name\" [FC-Code]"
    echo "Beispiel: $0 \"Voice Assistant Integration\" FC-100"
    exit 1
fi

echo "🚀 NEUES FEATURE ERSTELLEN"
echo "========================="
echo ""

# 1. Feature Registry prüfen
if [ ! -f "docs/FEATURE_REGISTRY.md" ]; then
    echo -e "${RED}❌ FEATURE_REGISTRY.md fehlt!${NC}"
    echo "Erstelle zuerst die Registry mit: ./scripts/init-registry.sh"
    exit 1
fi

# 2. Feature-Code bestimmen
if [ -n "$CUSTOM_CODE" ]; then
    # Custom Code validieren
    if ! [[ "$CUSTOM_CODE" =~ ^FC-[0-9]{3}$ ]]; then
        echo -e "${RED}❌ Ungültiges Format: $CUSTOM_CODE${NC}"
        echo "Format muss sein: FC-XXX (z.B. FC-100)"
        exit 1
    fi
    
    # Prüfen ob Code schon existiert
    if grep -q "^| $CUSTOM_CODE " docs/FEATURE_REGISTRY.md; then
        echo -e "${RED}❌ Feature-Code $CUSTOM_CODE existiert bereits!${NC}"
        exit 1
    fi
    
    FEATURE_CODE="$CUSTOM_CODE"
else
    # Nächste freie Nummer finden
    echo "🔍 Suche nächste freie Feature-Nummer..."
    
    # Höchste FC-Nummer in Registry finden
    HIGHEST=$(grep -E "^\| FC-[0-9]{3} " docs/FEATURE_REGISTRY.md | \
              awk -F'|' '{print $2}' | \
              grep -oE "[0-9]{3}" | \
              sort -n | tail -1)
    
    if [ -z "$HIGHEST" ]; then
        NEXT_NUM=100
    else
        # Entferne führende Nullen und erhöhe
        NEXT_NUM=$((10#$HIGHEST + 1))
    fi
    
    # Prüfe ob FC-100+ frei ist (reservierter Bereich)
    if [ "$NEXT_NUM" -lt 100 ]; then
        NEXT_NUM=100
    fi
    
    # Formatiere mit führenden Nullen
    FEATURE_CODE=$(printf "FC-%03d" "$NEXT_NUM")
fi

echo -e "${GREEN}✅ Feature-Code: $FEATURE_CODE${NC}"

# 3. Status abfragen
echo ""
echo "📊 Feature-Status wählen:"
echo "1) ACTIVE   - Wird aktiv entwickelt"
echo "2) PLANNED  - Geplant für später"
echo "3) VISION   - Langfristige Vision"
read -p "Wähle (1-3): " -n 1 -r STATUS_CHOICE
echo ""

case $STATUS_CHOICE in
    1) STATUS="ACTIVE" ;;
    2) STATUS="PLANNED" ;;
    3) STATUS="VISION" ;;
    *) echo -e "${RED}❌ Ungültige Wahl${NC}"; exit 1 ;;
esac

# 4. Ordner-Namen generieren
FOLDER_NAME=$(echo "$FEATURE_NAME" | \
              tr '[:upper:]' '[:lower:]' | \
              sed 's/ /_/g' | \
              sed 's/[^a-z0-9_]//g')

# 5. Ordner-Nummer bestimmen
if [ "$STATUS" = "PLANNED" ]; then
    # Höchste Nummer in PLANNED finden
    HIGHEST_DIR=$(find docs/features/PLANNED -maxdepth 1 -type d -name "[0-9]*" | \
                  grep -oE "[0-9]+" | \
                  sort -n | tail -1)
    
    if [ -z "$HIGHEST_DIR" ]; then
        DIR_NUM=50  # Start bei 50 für neue PLANNED
    else
        DIR_NUM=$((HIGHEST_DIR + 1))
    fi
    
    FOLDER_PATH="docs/features/$STATUS/${DIR_NUM}_${FOLDER_NAME}"
else
    # Für ACTIVE keine Nummer vergeben (verwende bestehende Struktur)
    FOLDER_PATH="docs/features/$STATUS/${FOLDER_NAME}"
fi

# 6. Zusammenfassung
echo ""
echo "📋 FEATURE-ZUSAMMENFASSUNG"
echo "========================="
echo -e "Name:       ${BLUE}$FEATURE_NAME${NC}"
echo -e "Code:       ${BLUE}$FEATURE_CODE${NC}"
echo -e "Status:     ${BLUE}$STATUS${NC}"
echo -e "Ordner:     ${BLUE}$FOLDER_PATH${NC}"
echo ""

read -p "Feature erstellen? (j/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Jj]$ ]]; then
    echo "❌ Abgebrochen"
    exit 1
fi

# 7. Feature erstellen
echo ""
echo "🏗️  Erstelle Feature-Struktur..."

# Ordner erstellen
mkdir -p "$FOLDER_PATH"
echo -e "${GREEN}✅ Ordner erstellt${NC}"

# TECH_CONCEPT erstellen
cat > "$FOLDER_PATH/${FEATURE_CODE}_TECH_CONCEPT.md" << EOF
# ${FEATURE_CODE}: $FEATURE_NAME - Technisches Konzept

**Feature Code:** ${FEATURE_CODE}  
**Feature-Typ:** 🔀 FULLSTACK  
**Erstellt:** $(date +%Y-%m-%d)  
**Status:** $STATUS

## 🎯 Zusammenfassung

[Kurze Beschreibung des Features und dessen Nutzen]

## 🔧 Technische Übersicht

### Backend-Komponenten
- [ ] API Endpoints definieren
- [ ] Domain Model erstellen
- [ ] Service Layer implementieren
- [ ] Repository Pattern

### Frontend-Komponenten  
- [ ] UI Components
- [ ] State Management
- [ ] API Integration
- [ ] Routing

### Datenbank
- [ ] Schema Design
- [ ] Migrations
- [ ] Indexes

## 📋 Implementation Tasks

1. **Backend Setup**
   - [ ] Entity/Domain Model
   - [ ] REST Endpoints
   - [ ] Business Logic
   - [ ] Tests

2. **Frontend Implementation**
   - [ ] Component Structure
   - [ ] State Management
   - [ ] API Calls
   - [ ] UI/UX

3. **Integration**
   - [ ] End-to-End Tests
   - [ ] Performance Tests
   - [ ] Security Review

## 🚀 MVP Scope

[Was gehört zum MVP, was nicht?]

## 📊 Erfolgsmetriken

- [ ] Performance: < 200ms API Response
- [ ] Test Coverage: > 80%
- [ ] User Satisfaction: [Metrik definieren]

## 🔗 Abhängigkeiten

- [Liste von anderen Features/Systemen]

## 📝 Offene Fragen

1. [Frage 1]
2. [Frage 2]
EOF

echo -e "${GREEN}✅ TECH_CONCEPT erstellt${NC}"

# README erstellen
cat > "$FOLDER_PATH/README.md" << EOF
# $FEATURE_NAME

**Feature Code:** ${FEATURE_CODE}  
**Status:** $STATUS  
**Ordner:** \`$FOLDER_PATH\`

## 📁 Struktur

\`\`\`
$FOLDER_PATH/
├── README.md                          # Diese Datei
├── ${FEATURE_CODE}_TECH_CONCEPT.md   # Technisches Konzept
├── ${FEATURE_CODE}_CLAUDE_TECH.md    # Claude-optimierte Version (TODO)
├── IMPLEMENTATION_GUIDE.md           # Schritt-für-Schritt Anleitung (TODO)
└── tests/                            # Feature-spezifische Tests (TODO)
\`\`\`

## 🚀 Quick Links

- [Technisches Konzept](./${FEATURE_CODE}_TECH_CONCEPT.md)
- [Feature Registry](/docs/FEATURE_REGISTRY.md)
- [Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)

## 📊 Status

- [ ] Konzept erstellt
- [ ] CLAUDE_TECH Version
- [ ] Implementation begonnen
- [ ] Tests geschrieben
- [ ] Code Review
- [ ] Deployed

## 🔧 Entwicklung starten

\`\`\`bash
# Reality Check durchführen
./scripts/reality-check.sh ${FEATURE_CODE}

# Bei Frontend-Features
./scripts/ui-development-start.sh --module=${FOLDER_NAME}

# Tests ausführen
cd backend && ./mvnw test -Dtest=${FEATURE_CODE}*
\`\`\`
EOF

echo -e "${GREEN}✅ README erstellt${NC}"

# 8. Registry Update
echo ""
echo "📝 Update Feature Registry..."

# Datum
DATE=$(date +%Y-%m-%d)

# Neuen Eintrag vorbereiten
if [ "$STATUS" = "PLANNED" ]; then
    REGISTRY_ENTRY="| $FEATURE_CODE | $FEATURE_NAME | PLANNED | $FOLDER_PATH | $DATE | TBD |"
    # In PLANNED Sektion einfügen
    sed -i "/## 📅 GEPLANTE FEATURES/,/## 🚫 RESERVIERTE CODES/ {
        /^$/i $REGISTRY_ENTRY
    }" docs/FEATURE_REGISTRY.md
else
    REGISTRY_ENTRY="| $FEATURE_CODE | $FEATURE_NAME | $STATUS | $FOLDER_PATH | $DATE | $DATE |"
    # In ACTIVE Sektion einfügen
    sed -i "/## ✅ AKTIVE FEATURES/,/## 📅 GEPLANTE FEATURES/ {
        /^$/i $REGISTRY_ENTRY
    }" docs/FEATURE_REGISTRY.md
fi

echo -e "${GREEN}✅ Registry aktualisiert${NC}"

# 9. Git hinzufügen
echo ""
echo "📦 Git vorbereiten..."
git add "$FOLDER_PATH"
git add docs/FEATURE_REGISTRY.md

# 10. Zusammenfassung
echo ""
echo "===================================="
echo -e "${GREEN}✅ FEATURE ERFOLGREICH ERSTELLT${NC}"
echo "===================================="
echo ""
echo "📋 Nächste Schritte:"
echo "1. Technisches Konzept ausarbeiten:"
echo "   vim $FOLDER_PATH/${FEATURE_CODE}_TECH_CONCEPT.md"
echo ""
echo "2. CLAUDE_TECH Version erstellen:"
echo "   ./scripts/create-claude-tech.sh $FEATURE_CODE"
echo ""
echo "3. Reality Check einrichten:"
echo "   ./scripts/reality-check.sh $FEATURE_CODE"
echo ""
echo "4. Commit erstellen:"
echo "   git commit -m \"feat: add $FEATURE_CODE - $FEATURE_NAME\""
echo ""
echo -e "${BLUE}Feature-Code $FEATURE_CODE ist nun reserviert und dokumentiert!${NC}"