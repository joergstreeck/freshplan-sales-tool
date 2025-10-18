#!/bin/bash

# =====================================================
# UNIVERSELLES HANDOVER-SCRIPT V2.0
# =====================================================
# Funktioniert aus JEDEM Verzeichnis
# Findet automatisch das Projekt-Root
# Erstellt vollständige Übergaben mit ALLEN Required Sections
#
# Update: 2025-10-18
# - Migration-Nummer via get-next-migration.sh (robust!)
# - 3-Ordner-Struktur dokumentiert
# - Session-Kontext Template
# - CI-validiert (handover-validation.yml)
# =====================================================

set -euo pipefail

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# =====================================================
# HILFSFUNKTIONEN
# =====================================================

# Finde Projekt-Root (suche nach .git + CLAUDE.md)
find_project_root() {
    local dir="$PWD"
    while [[ "$dir" != "/" ]]; do
        if [[ -d "$dir/.git" ]] && [[ -f "$dir/CLAUDE.md" ]]; then
            echo "$dir"
            return 0
        fi
        dir="$(dirname "$dir")"
    done

    # Fallback: Hardcoded path für CI
    if [[ -d "/Users/joergstreeck/freshplan-sales-tool/.git" ]]; then
        echo "/Users/joergstreeck/freshplan-sales-tool"
        return 0
    fi

    echo ""
    return 1
}

# Ermittle nächste Migration-Nummer (robust - 2-Sequenzen-Strategie)
get_next_migration_number() {
    local project_root="$1"

    # Prüfe ob get-next-migration.sh existiert
    if [[ ! -f "$project_root/scripts/get-next-migration.sh" ]]; then
        echo "UNKNOWN (Script fehlt)"
        return
    fi

    # Ermittle höchste Nummer aus migration/ UND dev-migration/ (GLEICHE Sequenz!)
    local migration_dir="$project_root/backend/src/main/resources/db/migration"
    local dev_migration_dir="$project_root/backend/src/main/resources/db/dev-migration"
    local dev_seed_dir="$project_root/backend/src/main/resources/db/dev-seed"

    local highest_sequential=0
    local highest_seed=0

    # Production + Test Migrations (GEMEINSAME Sequenz V1-V89999)
    if [[ -d "$migration_dir" ]]; then
        local prod_highest=$(ls -1 "$migration_dir" 2>/dev/null | \
            grep -oE 'V[0-9]+' | sed 's/V//' | sort -n | tail -1 || echo "0")
        if [[ $prod_highest -gt $highest_sequential ]]; then
            highest_sequential=$prod_highest
        fi
    fi

    if [[ -d "$dev_migration_dir" ]]; then
        local test_highest=$(ls -1 "$dev_migration_dir" 2>/dev/null | \
            grep -oE 'V[0-9]+' | sed 's/V//' | sort -n | tail -1 || echo "0")
        if [[ $test_highest -gt $highest_sequential ]]; then
            highest_sequential=$test_highest
        fi
    fi

    # DEV-SEED (EIGENE Sequenz V90001+)
    if [[ -d "$dev_seed_dir" ]]; then
        highest_seed=$(ls -1 "$dev_seed_dir" 2>/dev/null | \
            grep -oE 'V[0-9]+' | sed 's/V//' | sort -n | tail -1 || echo "90000")
    fi

    # Gib strukturierte Info zurück
    echo "Sequential (Prod+Test): V${highest_sequential}"
    echo "SEED: V${highest_seed}"
    echo ""
    echo "NEXT Sequential: V$((highest_sequential + 1)) (für db/migration/ ODER db/dev-migration/)"
    echo "NEXT SEED: V$((highest_seed + 1)) (für db/dev-seed/)"
}

# =====================================================
# MAIN EXECUTION
# =====================================================

main() {
    echo -e "${BLUE}${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}${BOLD}  🤝 HANDOVER CREATION V2.0${NC}"
    echo -e "${BLUE}${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""

    # 1. Finde Projekt-Root
    echo -e "${BLUE}🔍 Searching for FreshPlan project root...${NC}"
    PROJECT_ROOT=$(find_project_root)

    if [[ -z "$PROJECT_ROOT" ]]; then
        echo -e "${RED}❌ Fehler: Kein Git-Repository gefunden!${NC}"
        echo -e "${YELLOW}Bitte im FreshPlan-Projekt ausführen.${NC}"
        exit 1
    fi

    echo -e "${GREEN}✅ Found project root: $PROJECT_ROOT${NC}"
    cd "$PROJECT_ROOT"
    echo ""

    # 2. Timestamp
    TIMESTAMP=$(date +"%Y-%m-%d_%H-%M")
    DATE=$(date +"%Y-%m-%d")
    TIME=$(date +"%H:%M")

    # 3. Handover-Verzeichnis
    HANDOVER_DIR="$PROJECT_ROOT/docs/planung/claude-work/daily-work/$DATE"
    mkdir -p "$HANDOVER_DIR"

    HANDOVER_FILE="$HANDOVER_DIR/${DATE}_HANDOVER_${TIME//:/-}.md"

    echo -e "${BLUE}📝 Erstelle Handover: ${BOLD}$(basename $HANDOVER_FILE)${NC}"
    echo ""

    # =====================================================
    # 4. DATEN SAMMELN
    # =====================================================

    echo -e "${YELLOW}📊 Sammle Projekt-Daten...${NC}"

    # Git-Status
    CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")
    MODIFIED_FILES=$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')
    LAST_COMMIT=$(git log -1 --oneline 2>/dev/null || echo "No commits")
    GIT_STATUS=$(git status --short 2>/dev/null | head -20 || echo "Git status not available")

    # Migration-Nummer (robust via helper function)
    echo -e "${YELLOW}🔢 Ermittle Migration-Nummern (3-Ordner-Struktur)...${NC}"
    MIGRATION_INFO=$(get_next_migration_number "$PROJECT_ROOT")

    # Working Directory
    CURRENT_DIR=$(pwd)

    echo -e "${GREEN}✅ Daten gesammelt!${NC}"
    echo ""

    # =====================================================
    # 5. HANDOVER ERSTELLEN
    # =====================================================

    echo -e "${YELLOW}✍️  Erstelle Handover-Template...${NC}"

    cat > "$HANDOVER_FILE" << EOF
# 🤝 Übergabe: $DATE $TIME

**Actor:** Claude Code
**Branch:** $CURRENT_BRANCH
**Letzter Commit:** $LAST_COMMIT
**Migration:** [MANUAL: Ausfüllen - siehe Migration-Info unten]
**MP5-Status:** PENDING (wird in Compact Contract Schritt 3 erledigt)

---

## ✅ WAS WURDE GEMACHT?

**[MANUAL: Claude muss VOLLSTÄNDIG ausfüllen!]**

**Kontext:** [Warum wurde diese Session gestartet? Was war die Ausgangslage?]

**Erledigt:**
- [ ] **[Hauptaufgabe 1]:** [Details]
- [ ] **[Hauptaufgabe 2]:** [Details]
- [ ] **[Hauptaufgabe 3]:** [Details]

**Commits:** [Wenn Commits erstellt wurden - Liste mit Commit-Hashes]
- \`abc123\` - [Commit-Message]
- \`def456\` - [Commit-Message]

**Merge/PR:** [Falls PR erstellt/gemerged]
- PR #XXX: [Link] (Status: MERGED/READY FOR REVIEW)

**Migration:** [Falls DB-Arbeit]
- VXXXXX: [Beschreibung]
- VYYYY: [Beschreibung]

**Tests:** [Test-Status]
- Backend: XX/XX GREEN
- Frontend: XX/XX GREEN
- Total: XX/XX GREEN

**Status:** [Zusammenfassung - z.B. "✅ Sprint X.Y.Z COMPLETE", "🚧 In Progress", "❌ Blocked"]

---

## ⚠️ BEKANNTE PROBLEME

**[MANUAL: Claude muss ALLE Issues auflisten!]**

**Falls KEINE Probleme:**
\`\`\`
KEINE! 🎉
\`\`\`

**Falls Probleme vorhanden:**
- **[Problem 1]:** [Beschreibung] - [Mitigation/Workaround]
- **[Problem 2]:** [Beschreibung] - [Impact]

---

## 🎯 NÄCHSTER SCHRITT

**[MANUAL: Claude muss KONKRET formulieren!]**

**Sprint-Status:** [Z.B. "Sprint X.Y.Z ist COMPLETE", "Sprint X.Y.Z läuft", "Waiting on User"]

**Optionen für nächste Session:**
1. **[Option 1]:** [Konkrete nächste Aufgabe]
   - Branch: \`feature/...\`
   - Migration-Check: \`./scripts/get-next-migration.sh\`
   - Aufwand: ~Xh (Y Tage)

2. **[Option 2]:** [Alternative Aufgabe]
   - [Details]

3. **[Option 3]:** [Weitere Alternative]
   - [Details]

**Empfehlung:** [Mit Product Owner abstimmen / Direkt starten / Blocked wegen...]

---

## 📋 TODO-STATUS (Snapshot)

**[MANUAL: Claude muss TODO-Status aus TodoWrite eintragen!]**

**Alle Todos aus heutiger Session:**
- [ ] [Todo 1] - Status: [pending/in_progress/completed]
- [ ] [Todo 2] - Status: [pending/in_progress/completed]

**Falls keine Todos:**
\`\`\`
Keine aktiven Todos - Session abgeschlossen.
\`\`\`

---

## 🔧 TECHNISCHE DETAILS

### Git-Status
\`\`\`
Branch: $CURRENT_BRANCH
HEAD: $(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
Remote: $(git remote -v 2>/dev/null | head -1 || echo "No remote")
Modified Files: $MODIFIED_FILES
\`\`\`

\`\`\`
$GIT_STATUS
\`\`\`

### Migration-Info (3-Ordner-Struktur)

**Aktuelle Höchstwerte:**
\`\`\`
$MIGRATION_INFO
\`\`\`

**Migration-Strategie (seit Oktober 2025 - 2-Sequenzen-Modell):**

**SEQUENZ 1: Fortlaufende Nummern (V1-V89999) - Production + Test GEMEINSAM**
- **Production:** \`db/migration/\`
  - Für: Schema-Änderungen, Production-Features
  - Läuft: ALLE Umgebungen (Dev, Test, CI, Production)

- **Test-Migrations:** \`db/dev-migration/\`
  - Für: Test-spezifische Schemas, Debug-Views
  - Läuft: NUR %test Profile (CI-Tests)
  - SELTEN nötig! Meist ist Production richtig.

**WICHTIG:** Production + Test nutzen die GLEICHE Nummern-Sequenz!
- Wenn Production bei V10030 ist und Test bei V10008, ist die NÄCHSTE Nummer V10031
- So kann eine Test-Migration später zu Production verschoben werden (gleiche Nummer, nur Ordner wechselt)

**SEQUENZ 2: SEED Range (V90001+) - EIGENE Sequenz**
- **DEV-SEED:** \`db/dev-seed/\`
  - Für: Realistische SEED-Daten für UI-Testing
  - Läuft: NUR %dev Profile (manuelle Entwicklung)
  - ⚠️ NIEMALS in automatisierten Tests verwenden!
  - Eigener Nummernbereich V90001+ - unabhängig von Sequenz 1

**Neue Migration erstellen:**
\`\`\`bash
./scripts/get-next-migration.sh
# Interaktiver Dialog - wähle richtigen Ordner!
\`\`\`

**Migration Safety System (3-Layer):**
- ✅ Pre-Commit Hook: \`scripts/pre-commit-migration-check.sh\` (blockiert lokal)
- ✅ GitHub Workflow: \`.github/workflows/migration-safety-check.yml\` (blockiert CI)
- ✅ get-next-migration.sh: Dynamischer Sanity-Check (MAX_JUMP=100)

### Backend Status
\`\`\`
Quarkus Dev Mode: [MANUAL: RUNNING/STOPPED/NOT STARTED]
Health: [UP/DOWN]
Database: [Connected/Disconnected]
Latest Migration Deployed: VXXXXX
\`\`\`

### Test-Status
\`\`\`
[MANUAL: Falls Tests gelaufen]
Backend: XX/XX GREEN ✅
Frontend: XX/XX GREEN ✅
Total: XX/XX GREEN ✅
Coverage: >XX% ✅
\`\`\`

### Working Directory
\`\`\`
Aktuell: $CURRENT_DIR
Projekt: $PROJECT_ROOT
\`\`\`

---

## 📚 REFERENZEN

**Master Plan V5:** \`/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md\` (MP5 Update in Compact Contract Schritt 3)
**Roadmap:** \`/docs/planung/PRODUCTION_ROADMAP_2025.md\` (Roadmap Update in Compact Contract Schritt 4)
**Trigger Index:** \`/docs/planung/TRIGGER_INDEX.md\`
**AI Context:** \`/docs/planung/CRM_AI_CONTEXT_SCHNELL.md\`
**CLAUDE.md:** \`/CLAUDE.md\` (Meta-Arbeitsregeln)

**Falls PR erstellt:**
- PR #XXX: [GitHub URL]
- Merge Commit: [Hash]

**Falls Issue relevant:**
- Issue #XXX: [GitHub URL]

---

## 📝 NOTIZEN

**[MANUAL: Optionale Notizen eintragen]**

- **Arbeitsweise heute:** [Systematisch / Debugging / Refactoring / etc.]
- **Qualität:** [Code Quality Checks durchgeführt?]
- **Documentation:** [Docs aktualisiert?]
- **Git Hygiene:** [Clean commits? Branch cleanup?]

**Wichtige Erkenntnisse:**
- [Erkenntnis 1]
- [Erkenntnis 2]

---

**⚠️ WICHTIG für Claude:**

Dieser Handover wurde automatisch erstellt mit einem **TEMPLATE**.

**Du MUSST folgende Sections VOLLSTÄNDIG ausfüllen:**
1. ✅ WAS WURDE GEMACHT? (Pflicht!)
2. ⚠️ BEKANNTE PROBLEME (Pflicht!)
3. 🎯 NÄCHSTER SCHRITT (Pflicht!)
4. 📋 TODO-STATUS (Pflicht!)
5. 🔧 TECHNISCHE DETAILS - Backend/Test Status (Pflicht!)

**Ersetze alle [MANUAL: ...] Platzhalter!**

**Nach Ausfüllen:**
1. MP5 QUICK UPDATE durchführen (siehe CLAUDE.md - Compact Contract Schritt 3)
2. Roadmap Update durchführen (siehe CLAUDE.md - Compact Contract Schritt 4)
3. "MP5-Status: PENDING" entfernen
4. Commit anbieten (mit Branch-Sicherheit!)

_Automatisch erstellt von create-handover-universal.sh V2.0_
_Validiert durch CI: .github/workflows/handover-validation.yml_
EOF

    # =====================================================
    # 6. ABSCHLUSS
    # =====================================================

    echo -e "${GREEN}✅ Handover erstellt: ${BOLD}$HANDOVER_FILE${NC}"
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}📋 Quick-Info:${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo "  Branch: $CURRENT_BRANCH"
    echo "  Modified Files: $MODIFIED_FILES"
    echo "  Migration Info:"
    echo "$MIGRATION_INFO" | sed 's/^/    /'
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${RED}${BOLD}⚠️  WICHTIG:${NC}"
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}Das ist ein TEMPLATE!${NC}"
    echo -e "${YELLOW}Du MUSST alle [MANUAL: ...] Sections ausfüllen!${NC}"
    echo ""
    echo -e "${GREEN}Required Sections (CI-validiert):${NC}"
    echo "  1. ✅ WAS WURDE GEMACHT?"
    echo "  2. ⚠️ BEKANNTE PROBLEME"
    echo "  3. 🎯 NÄCHSTER SCHRITT"
    echo "  4. 📋 TODO-STATUS"
    echo "  5. 🔧 TECHNISCHE DETAILS"
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}✨ Fertig!${NC}"
    echo ""
}

# Run main function
main "$@"
