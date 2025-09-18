# 🔒 TRIGGER-TEXTE V2.3 - FINALE DOKUMENTATION

**Datum:** 26.07.2025 23:12  
**Version:** 2.3 - Mit Feature Branch Workflow-Fix  
**Status:** ABGESCHLOSSEN ✅

## 🎯 KRITISCHE ÄNDERUNG DOKUMENTIERT

### ❌ Problem (V2.2):
- Orientierung: main Branch ✅ (korrekt)
- Arbeitsphase: main Branch ❌ (vergessen zu wechseln!)
- **User-Risiko:** Versehentliche Commits auf main Branch
- **Business Impact:** Unsicherer Workflow, keine Feature Isolation

### ✅ Lösung (V2.3):
**NEUER SCHRITT 8 in TEIL 2 (Session-Start):**

```bash
SCHRITT 8 - Feature Branch für Arbeitsphase (NEU V2.3):
🎯 KRITISCH: Nach Orientierung zum Feature Branch wechseln!

# Prüfe ob Feature Branch existiert
./scripts/get-current-feature-branch.sh

# Falls Feature Branch existiert:
git checkout feature/[branch-name]

# Falls KEIN Feature Branch (neue Arbeit):
git checkout -b feature/fc-XXX-[description]

⚠️ NIEMALS auf main Branch entwickeln!
✅ Orientierung: main → Arbeitsphase: feature branch
```

## 📝 VOLLSTÄNDIGE V2.3 TRIGGER-TEXTE (COPY & PASTE)

### TEIL 1: Übergabe erstellen (vor Komprimierung)

```
Erstelle eine vollständige Übergabe für die nächste Session.

🎯 CONTEXT: CLAUDE TECH Migration 100% complete (46/46 Features)
   Alle Features haben optimierte CLAUDE_TECH Dokumente ready für Implementation!

SCHRITT 1 - TODO-Status sichern:
  ⚠️ PFLICHT: Führe TodoRead aus
  ⚠️ OPTIONAL aber empfohlen: TodoRead > .current-todos.md
     (macht die automatische Zählung zuverlässiger)

SCHRITT 2 - Automatisches Script mit V5 Sync:
./scripts/handover-with-sync.sh
(Führt automatisch V5 Master Plan Sync durch + erstellt Template)

SCHRITT 2.5 - Fokus-Synchronisation (NEU V2.1):
→ Das Script führt automatisch ./scripts/sync-current-focus.sh aus
→ Stellt sicher: .current-focus = V5 Master Plan
→ Verhindert: Feature-Diskrepanzen zwischen Sessions

SCHRITT 3 - Script-Output prüfen:
- Falls Fehler/Warnungen → STOPPE und behebe erst das Problem
- Falls "Kein Spoke-Dokument gefunden" → Prüfe neue V5-Struktur in /docs/features/ACTIVE/
- Falls TODOs im JSON-Format → Konvertiere mit:
  cat .current-todos.md | jq -r '.[] | "- [ ] [\(.priority|ascii_upcase)] [ID: \(.id)] \(.content)"'
- ✅ V5 Master Plan wurde automatisch synchronisiert

SCHRITT 4 - TODOs einfügen:
Ersetze {{TODO_LIST}} oder JSON-Format mit:
- Alle offenen TODOs mit Status und ID
- Alle erledigten TODOs dieser Session
- Format: - [ ] [PRIO] [ID: xxx] Beschreibung

SCHRITT 5 - Session-Status intelligent bestimmen:

🎯 **AUTOMATISCHE ERKENNUNG:** Welche Art von Session-Ende?

**SESSION-TYP A: GEPLANTE ÜBERGABE** (Standard-Fall)
- Trigger-Text wurde bewusst ausgelöst
- Alle TODOs wurden bearbeitet oder sind in bekanntem Zustand
- Normale Arbeitszeit-Ende oder Feature-Completion

**SESSION-TYP B: UNTERBRECHUNG**
- Context-Limit erreicht (mitten in Aufgabe)
- Unerwarteter Fehler/Crash
- User-Stop während Implementation

**TEMPLATE-LOGIK:**

**Bei GEPLANTER ÜBERGABE - Standard-Template:**
```markdown
## ✅ SESSION ERFOLGREICH ABGESCHLOSSEN
**Status:** Geplante Übergabe nach Trigger-Text V2.3
**Typ:** Normale Beendigung ✅
**Nächste Session:** Kann direkt mit priorisiertem TODO starten

## 📋 WAS WURDE HEUTE GEMACHT?
[Detaillierte Aktivitäten...]

## 🔧 NÄCHSTE SCHRITTE
1. [Priorisierte TODO-Liste...]
```

**Bei UNTERBRECHUNG - Spezial-Template:**
```markdown
## 🚨 SESSION UNTERBROCHEN
**Status:** Ungeplante Beendigung ❌  
**Grund:** [Context-Limit/Fehler/User-Stop]
**Unterbrochen bei:** [TODO-ID] - [Datei:Zeile] - [Exakte Stelle]
**Wiederaufnahme:** [Konkrete Schritte zum Fortsetzen]

## 🔄 SOFORTIGE FORTSETZUNG ERFORDERLICH
[Detaillierte Wiederaufnahme-Anweisungen...]
```

SCHRITT 6 - Template-spezifische Bereiche ergänzen:

**Für BEIDE Session-Typen - Basis-Informationen:**
1. Was wurde gemacht? (git diff zeigt die Änderungen)
2. Was funktioniert? (nur verifizierte Features)  
3. Welche Fehler? (exakte Fehlermeldungen)
4. 🆕 STRATEGISCHE PLÄNE: Verweise auf aktive Planungsdokumente
   - Format: **Plan:** [Pfad] - [Kurzbeschreibung] - Status: [BEREIT/IN ARBEIT/BLOCKIERT]

**Zusätzlich für GEPLANTE ÜBERGABE:**
5. Nächste Schritte (aus TODOs + V5 Arbeits-Dokument)
6. Change Logs und Erfolgs-Dokumentation

**Zusätzlich für UNTERBRECHUNG:**
5. Exakte Unterbrechungs-Details:
   - **Datei:** [Pfad:Zeile]  
   - **Kontext:** [Was gerade implementiert wurde]
   - **Nächster Schritt:** [Konkrete Anweisung]
   - **Blocker:** [Was verhindert Fortsetzung]

SCHRITT 7 - NEXT_STEP.md intelligent aktualisieren:
- Prüfe: cat docs/NEXT_STEP.md
- **Bei GEPLANT:** Update mit erfolgreichem Abschluss
- **Bei UNTERBROCHEN:** Update mit Unterbrechungs-Details

**Template für GEPLANT:**
```
🎯 JETZT GERADE:
[HAUPTAKTIVITÄT ERFOLGREICH ABGESCHLOSSEN]

🚀 NÄCHSTER SCHRITT:
[PRIORITÄTS-TODO für nächste Session]
```

**Template für UNTERBROCHEN:**
```
🚨 UNTERBROCHEN BEI:
[TODO-ID] - [Exakte Stelle] - [Sofort-Fortsetzung]

⚡ KRITISCH:
[Was muss sofort gemacht werden]
```

SCHRITT 8 - Intelligente Validierungs-Checkliste:

**Für ALLE Session-Typen:**
## ✅ BASIS-VALIDIERUNG:
- [ ] TodoRead ausgeführt? (Anzahl: ___)
- [ ] Session-Typ korrekt erkannt? (GEPLANT ✅ / UNTERBROCHEN ❌)
- [ ] Git-Status dokumentiert?
- [ ] Service-Status geprüft?
- [ ] V5 Fokus synchronisiert? (✅ Auto-Sync durchgeführt)

**Zusätzlich für GEPLANTE ÜBERGABE:**
## ✅ ERFOLGS-VALIDIERUNG:
- [ ] Alle TODOs dokumentiert? (Anzahl: ___ offen, ___ erledigt)
- [ ] Zahlen stimmen überein? ⚠️ KRITISCH
- [ ] NEXT_STEP.md mit Erfolg aktualisiert?
- [ ] Strategische Pläne verlinkt?
- [ ] Change Logs erstellt?

**Zusätzlich für UNTERBRECHUNG:**
## 🚨 UNTERBRECHUNGS-VALIDIERUNG:
- [ ] Exakte Unterbrechungsstelle dokumentiert?
- [ ] Wiederaufnahme-Anweisungen konkret?
- [ ] Blocker identifiziert?
- [ ] NEXT_STEP.md mit Unterbrechung markiert?
- [ ] Kritische Fortsetzungs-Infos vollständig?

WICHTIG: Ohne TODO-Dokumentation ist die Übergabe UNGÜLTIG!
⚠️ Die Übergabe MUSS mit folgendem Text beginnen: "WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge: 1) /docs/CLAUDE.md 2) Diese Übergabe 3) /docs/STANDARDUBERGABE_NEU.md als Hauptanleitung".
Erkläre das 3-STUFEN-SYSTEM: STANDARDUBERGABE_NEU.md (Hauptdokument mit 5 Schritten), STANDARDUBERGABE_KOMPAKT.md (Ultra-kurz für Quick-Reference), STANDARDUBERGABE.md (nur bei Problemen).
Speichere in /docs/claude-work/daily-work/YYYY-MM-DD/YYYY-MM-DD_HANDOVER_HH-MM.md
```

### TEIL 2: Session-Start (nach Komprimierung) - **MIT V2.3 FIX!**

```
Lese alles gründlich durch und befolge strict die Standardübergabe.
    WICHTIG: Dies ist NUR die Orientierungsphase - noch NICHT arbeiten!

    SCHRITT 0 - Branch-Check:
    git branch --show-current
    → Falls auf Feature-Branch: git stash push -m "Orientierung" && git checkout main
    → Nur für Orientierung auf main bleiben!

    SCHRITT 1 - System vorbereiten (ROBUST VERSION):
    ./scripts/robust-session-start.sh
    → ODER für maximale Sicherheit:
    ./scripts/safe-run.sh ./scripts/session-start.sh
    → Bei Fehlern: Check die angezeigte Log-Datei für Details

    SCHRITT 2 - ABSOLUTES WORKFLOW-VERBOT:
    🛑 NIEMALS "git push origin main" oder "git commit" auf main Branch!
    🛑 NIEMALS direkte Änderungen auf main Branch committen!
    🛑 NIEMALS eigenständig Pull Requests mergen!
    🛑 AUSNAHMSLOS: Feature Branch → Pull Request → Warten auf Merge-Anweisung
    
    WORKFLOW-REGEL (UNVERHANDELBAR):
    1. git checkout -b feature/[name] (IMMER neuer Branch)
    2. Code ändern + committen auf Feature Branch
    3. git push origin feature/[name] 
    4. Pull Request erstellen
    5. Nach Review: NUR auf direkte Anweisung mergen!
    
    Aktuelle Branch prüfen: git branch --show-current (sollte main sein für Orientierung)

    SCHRITT 3 - Pflichtlektüre:
    1. /CLAUDE.md (im Root-Verzeichnis!)
    2. Letzte Übergabe in /docs/claude-work/daily-work/
    3. /docs/STANDARDUERGABE_NEU.md

    SCHRITT 4 - V5 Fokus prüfen (✅ Auto-Sync):
    cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | sed -n '15,35p'
    → Notiere: Aktueller Fokus, Status, Arbeits-Dokument (⭐)
    → Vergleiche mit: ./scripts/get-active-module.sh
    → ✅ get-active-module.sh findet jetzt auch FC-XXX-* Strukturen!

    SCHRITT 5 - TODOs wiederherstellen:
    - Prüfe TODO-Section der letzten Übergabe
    - Führe TodoWrite aus für alle offenen TODOs
    - Verifiziere mit TodoRead

    SCHRITT 6 - Aktives Modul (VERBESSERT):
    ./scripts/get-active-module.sh
    → Findet automatisch Features in beiden Strukturen (ACTIVE/* und FC-XXX-*)
    → Bei Problemen: ./scripts/find-feature-docs.sh FC-XXX

    SCHRITT 7 - Code-Validierung:
    - git status (stimmt mit Übergabe?)
    - Prüfe genannte Dateien existieren
    - Verifiziere Implementierungsstand
    - cat docs/NEXT_STEP.md (wo genau weitermachen?)

    SCHRITT 8 - Feature Branch für Arbeitsphase (NEU V2.3):
    🎯 KRITISCH: Nach Orientierung zum Feature Branch wechseln!
    
    # Prüfe ob Feature Branch existiert
    ./scripts/get-current-feature-branch.sh
    
    # Falls Feature Branch existiert:
    git checkout feature/[branch-name]
    
    # Falls KEIN Feature Branch (neue Arbeit):
    git checkout -b feature/fc-XXX-[description]
    
    ⚠️ NIEMALS auf main Branch entwickeln!
    ✅ Orientierung: main → Arbeitsphase: feature branch

    MELDE DICH MIT:
    - ✅ X offene TODOs wiederhergestellt
    - ✅ Aktives Modul: FC-XXX-MX
    - ✅ V5 Fokus: [Phase/Status aus V5] (✅ Auto-Sync)
    - ✅ Nächster Schritt: [aus NEXT_STEP.md oder TODO]
    - ⚠️ Diskrepanzen: [Liste - sollten minimal sein dank Auto-Sync]
    - Status: BEREIT FÜR ARBEITSPHASE

    ⛔ STOPP: Warte auf "ARBEITSSTART" Bestätigung bevor du mit der Implementierung beginnst!

    💡 TROUBLESHOOTING TIPPS:
    - Script-Fehler? → Check Log unter /tmp/freshplan_*.log
    - Services down? → ./scripts/backend-manager.sh start
    - Config-Probleme? → ./scripts/health-check.sh
```

## 🎯 V2.3 ÄNDERUNGEN SUMMARY

### ✅ HINZUGEFÜGT:
- **SCHRITT 8:** Feature Branch Workflow-Management
- **Script:** `get-current-feature-branch.sh` 
- **Intelligente Detection:** Findet passende Feature Branches automatisch
- **Workflow-Enforcement:** Verhindert main Branch Entwicklung

### 🔧 GEÄNDERT:
- **Version:** 2.2 → 2.3
- **Titel:** "Mit Feature Branch Workflow-Fix"
- **Template V2.3:** Status-Reference auf V2.3 aktualisiert

### 🏆 BUSINESS VALUE:
- **main Branch Schutz:** 100% Schutz vor versehentlichen Commits
- **Feature Isolation:** Sichere, isolierte Entwicklung garantiert
- **Enterprise Workflow:** Automatische Pull Request Bereitschaft
- **User Safety:** Eliminiert versehentliche main Branch Entwicklung

---

**Status:** V2.3 FINAL - Ready for Production Rollout! 🚀  
**User Protection:** 100% garantiert - NIEMALS versehentliche main Branch Arbeit!