# STANDARDÜBERGABE NEU - Das 5-Schritte-System

## Zweck
Dieses Dokument ist deine **HAUPTANLEITUNG** für effiziente und nachvollziehbare Arbeitssitzungen. Es ist Teil des 3-STUFEN-SYSTEMS.

## 📚 Das 3-STUFEN-SYSTEM der Übergabe-Dokumente

1. **STANDARDUBERGABE_NEU.md** (DIESES DOKUMENT)
   - Hauptdokument mit dem vollständigen 5-Schritte-Prozess
   - IMMER als primäre Anleitung verwenden
   - Enthält alle wichtigen Scripts und Befehle

2. **STANDARDUBERGABE_KOMPAKT.md** (Ultra-Kurzversion)
   - Nur für Quick-Reference wenn du den Prozess schon kennst
   - Komprimierte Version für erfahrene Sessions
   - Keine Details, nur Stichpunkte

3. **STANDARDUBERGABE.md** (Erweiterte Version)
   - Nur bei ernsten Problemen verwenden
   - Detaillierte Troubleshooting-Anleitungen
   - Historische Kontexte und Hintergründe

## 🚀 Quick-Start für neue Session
```bash
# Ein Befehl für optimalen Session-Start:
./scripts/session-start.sh

# Während der Arbeit - Quick Status:
./scripts/quick-status.sh

# Code Review vor Commit:
./scripts/code-review.sh
```

## 🚨 KRITISCHE INFORMATIONEN (IMMER BEACHTEN!)

### Service-Ports und Technologien
- **Backend:** Port `8080` - Quarkus mit Java 17 (NICHT 11 oder 21!)
- **Frontend:** Port `5173` - React/Vite mit TypeScript
- **PostgreSQL:** Port `5432` - Datenbank
- **Keycloak:** Port `8180` - Authentication Service

### Working Directory
**IMMER:** `/Users/joergstreeck/freshplan-sales-tool`

### Branch-Regel
**NIEMALS** direkt in `main` pushen! Immer Feature-Branches nutzen.

## Das 5-Schritte-System

### 1. System-Check (Vor der Arbeit)
```bash
# IMMER diese Scripts ausführen:
./scripts/validate-config.sh  # Prüft Tools und Konfiguration
./scripts/check-services.sh   # Prüft laufende Services

# Falls Services nicht laufen, starte sie:
./scripts/start-services.sh   # Startet ALLE Services (PostgreSQL, Backend, Frontend)

# Oder einzeln starten:
./scripts/start-backend.sh   # Backend mit Java 17 auf Port 8080
./scripts/start-frontend.sh  # Frontend mit Vite auf Port 5173
```

### 1.5 Master Plan V5 Check (NEU! Ab 17.07.2025)
**🚨 KRITISCH: V5 ist der neue Standard für alle Arbeiten!**

Nach dem System-Check IMMER V5 prüfen:
```bash
# V5 Master Plan lesen (Claude-optimiert, nur 92 Zeilen):
cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md

# Fokus auf diese Sektionen:
# 1. "📍 Aktueller Fokus" - Was ist gerade dran?
# 2. "🧭 Navigation" - Nächste Schritte
# 3. "📊 Status Dashboard" - Welche Module sind ready?
```

**Quick-Checks:**
```bash
# Aktueller Fokus (Zeilen 15-35):
cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | sed -n '15,35p'

# Status Dashboard (Zeilen 77-85):
cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | sed -n '77,85p'

# 🧭 NEXT STEP Navigation (NEU!):
cat docs/NEXT_STEP.md
# → Zeigt dir GENAU wo du weitermachen sollst!
```

**MELDE DICH MIT V5-STATUS:**
- ✅ V5 gelesen, aktueller Fokus: [Phase X - Name]
- ✅ Arbeits-Dokument: [Pfad aus ⭐ Markierung]
- ✅ Status: [Ready/In Progress/Blocked]
- ⚠️ Offene kritische Fragen: [Anzahl aus OPEN_QUESTIONS_TRACKER.md]

### 2. Orientierung (Beginn der Arbeit)
**Pflicht-Lektüre in DIESER Reihenfolge:**
1. `/docs/CLAUDE.md` - Arbeitsrichtlinien und Standards
2. Letzte Übergabe in `/docs/claude-work/daily-work/YYYY-MM-DD/`
3. ~~`/docs/CRM_COMPLETE_MASTER_PLAN.md`~~ → **V5 bereits in Schritt 1.5 gelesen!**

**Dann Status abgleichen:**
```bash
git status                    # Git-Status prüfen
git log --oneline -10        # Letzte Commits
TodoRead                     # TODO-Liste lesen
```

## 🎯 FEATURE-TYP BESTIMMEN (NEU!)
**WICHTIG: Bei JEDER Aufgabe den Feature-Typ prüfen!**

1. **Schaue ins Feature-Dokument** (FC-XXX_KOMPAKT.md)
2. **Finde "Feature-Typ"** Zeile
3. **Handle entsprechend:**
   - 🎨 **FRONTEND** → `./scripts/ui-development-start.sh --module=[name]`
   - 🔧 **BACKEND** → Normal weiterarbeiten
   - 🔀 **FULLSTACK** → Beides beachten

**Beispiel:**
```bash
# Feature-Typ prüfen:
cat docs/features/ACTIVE/*/M3_*_KOMPAKT.md | grep "Feature-Typ"
# Output: **Feature-Typ:** 🎨 FRONTEND

# Bei Frontend-Arbeit IMMER:
./scripts/ui-development-start.sh --module=sales-cockpit
```

### 3. Arbeiten (Der Hauptteil)
- **Fokussiert** an der in der Übergabe definierten Hauptaufgabe arbeiten
- **Code-Validierung**: NIEMALS Annahmen treffen!
  ```bash
  # Beispiele für Code-Inspektion:
  find backend -name "*.java" | grep -i customer | head -20
  grep -r "TODO" backend/src --include="*.java" | wc -l
  ls -la backend/src/main/resources/db/migration/
  ```
- **Dokumentiere** wichtige Entscheidungen in `/docs/claude-work/daily-work/YYYY-MM-DD/`
- **Feature-Konzepte** für größere Features erstellen:
  - Feature Code vergeben (FC-XXX)
  - Template nutzen: `/docs/templates/TECH_CONCEPT_TEMPLATE.md`
  - In `/docs/features/` ablegen
  - Master Plan nur mit Verweis aktualisieren
  - **WICHTIG**: Bei jeder Übergabe den Implementierungs-Status in der FC-Datei aktualisieren!
- **Two-Pass-Review** nach jedem signifikanten Schritt (siehe CLAUDE.md Abschnitt 0.10)

#### 📝 Change Logs (NEU! Ab 07.07.2025)
**PFLICHT bei jeder signifikanten Änderung:**
- Erstelle IMMER ein Change Log VOR der Implementierung
- Format: `YYYY-MM-DD_CHANGE_LOG_<feature-name>.md`
- Nutze das Template: `cp docs/templates/CHANGE_LOG_TEMPLATE.md docs/claude-work/daily-work/$(date +%Y-%m-%d)/`
- Dokumentiere Vorher/Nachher-Zustand mit Screenshots/Logs
- Verlinke das Change Log in der Übergabe

**Was zählt als "signifikante Änderung"?**
- Neue Features oder Funktionalitäten
- Änderungen an bestehenden Features
- Datenbank-Migrationen oder Schema-Änderungen
- API-Änderungen
- UI/UX-Änderungen
- Performance-Optimierungen
- Bug-Fixes die Verhalten ändern

### 4. Problemlösung
**Bei Problemen - Systematisches Vorgehen:**
1. **Analysiere** das Problem selbstständig
2. **Dokumentiere** strukturiert:
   ```bash
   # Kopiere Template für Problem-Analyse:
   cp docs/templates/PROBLEM_ANALYSIS_TEMPLATE.md \
      docs/claude-work/daily-work/$(date +%Y-%m-%d)/PROBLEM_$(date +%H%M).md
   ```
3. **Schlage** 2-3 konkrete Lösungswege vor
4. **Halte dich** an die "Wenn du nicht weiterkommst"-Regeln im CLAUDE.md

### 5. Übergabe (Abschluss der Arbeit)
**Erstelle IMMER eine vollständige Übergabe mit folgendem Format:**

```bash
# Automatisches Template erstellen:
./scripts/create-handover.sh
# Dann die [MANUELL AUSFÜLLEN] Bereiche ergänzen
```

#### Übergabe-Template:
```markdown
# 🔄 STANDARDÜBERGABE - [DATUM] [UHRZEIT]

**WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge:**
1. `/docs/CLAUDE.md` (Arbeitsrichtlinien und Standards)
2. Diese Übergabe
3. `/docs/STANDARDUBERGABE_NEU.md` als Hauptanleitung

## 📚 Das 3-STUFEN-SYSTEM verstehen
[Kurze Erklärung der drei Dokumente]

## 🎯 AKTUELLER STAND (Code-Inspektion-Validiert)
### ✅ SYSTEM-STATUS ([UHRZEIT])
[Ergebnis von validate-config.sh und check-services.sh]

### 🏗️ IMPLEMENTIERTE FEATURES (Code-validiert)
[Was ist WIRKLICH im Code vorhanden - mit Grep/Find-Befehlen verifiziert]

## 📋 WAS WURDE HEUTE GEMACHT?
[Detaillierte Liste der Aktivitäten]

## 📝 CHANGE LOGS DIESER SESSION
- [ ] Change Log erstellt für: [Feature-Name]
  - Link: `/docs/claude-work/daily-work/YYYY-MM-DD/YYYY-MM-DD_CHANGE_LOG_feature.md`
- [ ] Weitere Change Logs: [Liste weitere wenn vorhanden]

## 📑 FEATURE-KONZEPTE STATUS-UPDATE
- [ ] FC-001 (Dynamic Focus List) Status aktualisiert: ✅ Backend / 🔄 Frontend
  - Link: `/docs/features/2025-07-07_TECH_CONCEPT_dynamic-focus-list.md`
- [ ] Weitere FC-Updates: [Liste weitere aktive Feature-Konzepte]

## 🎯 HEUTIGER FOKUS
**Aktives Modul:** [FC-XXX-MX Name]
**Modul-Dokument:** [Pfad] ⭐
**Hub-Dokument:** [Pfad] (Referenz)
**Letzte Zeile bearbeitet:** [Component:Line]
**Nächster Schritt:** [Konkrete Aufgabe]

## 🏛️ ARCHITEKTUR & PLANUNG
- [ ] **Feature-Konzept [FC-XXX] geprüft:** Das Konzept ist auf dem neuesten Stand und enthält alle notwendigen, "kompressionssicheren" Implementierungsdetails.
  - Dateipfade und Komponenten-Namen definiert
  - Props und State vollständig spezifiziert
  - State Management Stores zugeordnet
  - API-Interaktionen dokumentiert
  - Kernlogik beschrieben

## 🛠️ WAS FUNKTIONIERT?
[Verifizierte, funktionierende Features]

## 🚨 WELCHE FEHLER GIBT ES?
[Aktuelle Probleme mit Fehlermeldungen]

## 🔧 WIE WURDEN SIE GELÖST / WAS IST ZU TUN?
[Lösungen oder konkrete nächste Schritte]

## 📈 NÄCHSTE KONKRETE SCHRITTE
[Priorisierte Liste mit Zeitschätzungen]

## 📚 MASSGEBLICHE DOKUMENTE
[Welcher Plan/Dokument ist aktuell gültig]

## 🚀 NACH KOMPRIMIERUNG SOFORT AUSFÜHREN
\```bash
# 1. Zum Projekt wechseln
cd /Users/joergstreeck/freshplan-sales-tool

# 2. System-Check und Services starten
./scripts/validate-config.sh
./scripts/check-services.sh

# Falls Services nicht laufen:
./scripts/start-services.sh

# 3. Git-Status
git status
git log --oneline -5

# 4. TODO-Status
TodoRead

# 5. [Spezifische Befehle für aktuelle Aufgabe]
\```
```

#### Speicherort für Übergaben:
`/docs/claude-work/daily-work/YYYY-MM-DD/YYYY-MM-DD_HANDOVER_HH-MM.md`

#### Wichtige Regeln für Übergaben:
- **VERIFIZIERE** erst den echten Code-Stand (keine Annahmen!)
- **DOKUMENTIERE** nur Fakten aus Code-Inspektion
- **TodoRead** IMMER für aktuellen TODO-Stand nutzen
- **DENKE** daran: Was brauchst DU, um effektiv weiterzuarbeiten?