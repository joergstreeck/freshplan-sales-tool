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

### 2. Orientierung (Beginn der Arbeit)
**Pflicht-Lektüre in DIESER Reihenfolge:**
1. `/docs/CLAUDE.md` - Arbeitsrichtlinien und Standards
2. Letzte Übergabe in `/docs/claude-work/daily-work/YYYY-MM-DD/`
3. `/docs/CRM_COMPLETE_MASTER_PLAN.md` - Aktueller Masterplan

**Dann Status abgleichen:**
```bash
git status                    # Git-Status prüfen
git log --oneline -10        # Letzte Commits
TodoRead                     # TODO-Liste lesen
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
- **Two-Pass-Review** nach jedem signifikanten Schritt (siehe CLAUDE.md Abschnitt 0.10)

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