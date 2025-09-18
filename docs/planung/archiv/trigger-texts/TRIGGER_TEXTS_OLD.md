# 🔒 GESICHERTE TRIGGER-TEXTE FÜR STANDARDÜBERGABE

**WICHTIG: Diese Datei enthält die offiziellen Trigger-Texte. NIEMALS löschen oder überschreiben!**

**Version:** 2.6  
**Letzte Aktualisierung:** 02.08.2025  
**Neues Feature:** MIGRATION-CHECK als verpflichtender Schritt 2.5 integriert

---

## 📝 TEIL 1: Übergabe erstellen (vor Komprimierung)

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

SCHRITT 5 - Ergänze diese Bereiche:
1. Was wurde gemacht? (git diff zeigt die Änderungen)
2. Was funktioniert? (nur verifizierte Features)
3. Welche Fehler? (exakte Fehlermeldungen)
4. Nächste Schritte (aus TODOs + V5 Arbeits-Dokument)
5. 🆕 STRATEGISCHE PLÄNE: Verweise auf aktive Planungsdokumente
   - Format: **Plan:** [Pfad] - [Kurzbeschreibung] - Status: [BEREIT/IN ARBEIT/BLOCKIERT]
   - Beispiel: **Plan:** /docs/features/ACTIVE/.../FC-XXX_CLAUDE_TECH.md - Feature X Implementation - Status: IN ARBEIT
6. 🚨 UNTERBRECHUNGEN: Falls interrupted, wo genau warst du?
   - Format: **Unterbrochen bei:** [TODO-XXX] - [Datei:Zeile] - [Was war der nächste Schritt]

SCHRITT 6 - NEXT_STEP.md aktualisieren:
- Prüfe: cat docs/NEXT_STEP.md
- Update mit aktuellem Stand nach diesem Format:
🎯 JETZT GERADE:

  [HAUPTAKTIVITÄT IN GROSSBUCHSTABEN]

  Stand [Datum Zeit]:
- ✅ Was ist fertig
- 🔄 Was läuft gerade
- 🚨 Wo unterbrochen

  🚀 NÄCHSTER SCHRITT:

  [KONKRETER NÄCHSTER SCHRITT (TODO-XX)]
# Konkrete Befehle

  UNTERBROCHEN BEI:
- Exakte Stelle
- Nächster geplanter Schritt

SCHRITT 7 - Validierungs-Checkliste:
## ✅ VALIDIERUNG:
- [ ] TodoRead ausgeführt? (Anzahl: ___)
- [ ] Alle TODOs in Übergabe? (Anzahl: ___)
- [ ] Zahlen stimmen überein? ⚠️ KRITISCH
- [ ] Git-Status korrekt?
- [ ] Service-Status geprüft?
- [ ] V5 Fokus dokumentiert? (✅ Auto-Sync durchgeführt)
- [ ] NEXT_STEP.md aktuell?
- [ ] Nächste Schritte klar?
- [ ] Strategische Pläne verlinkt?

WICHTIG: Ohne TODO-Dokumentation ist die Übergabe UNGÜLTIG!
⚠️ Die Übergabe MUSS mit folgendem Text beginnen: "WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge: 1) /docs/CLAUDE.md 2) Diese Übergabe 3) /docs/STANDARDUBERGABE_NEU.md als Hauptanleitung".
Erkläre das 3-STUFEN-SYSTEM: STANDARDUBERGABE_NEU.md (Hauptdokument mit 5 Schritten), STANDARDUBERGABE_KOMPAKT.md (Ultra-kurz für Quick-Reference), STANDARDUBERGABE.md (nur bei Problemen).
Speichere in /docs/claude-work/daily-work/YYYY-MM-DD/YYYY-MM-DD_HANDOVER_HH-MM.md
```

---

## 🚀 TEIL 2: Session-Start (nach Komprimierung)

```
Lese alles gründlich durch und befolge strict die Standardübergabe.

    SCHRITT 0 - SOFORT zum Feature-Branch wechseln:
    git branch --show-current
    ./scripts/get-current-feature-branch.sh
    
    → Falls Feature-Branch gefunden: git checkout feature/[branch-name]
    → Falls KEIN Feature-Branch: git checkout -b feature/fc-XXX-[description]
    
    ⚠️ DU BLEIBST AUF DEM FEATURE-BRANCH FÜR DIE GESAMTE SESSION!
    ✅ Orientierung und Arbeit erfolgen auf dem gleichen Branch!

    SCHRITT 1 - System vorbereiten:
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

    SCHRITT 2.5 - MIGRATION-CHECK (🚨 PFLICHT bei DB-Arbeit!):
    🚨🚨🚨 MIGRATION ALERT - V121 FREI 🚨🚨🚨
    cat docs/FLYWAY_MIGRATION_HISTORY.md | head -20
    echo -e "\033[1;31m🚨 NÄCHSTE FREIE MIGRATION: V121\033[0m"
    → Bei Migration-Arbeit ohne klare V121+ Nummer: SESSION UNTERBRECHEN!
    → Nach neuer Migration: ./scripts/update-flyway-history.sh V121 "name" "beschreibung"

    SCHRITT 3 - Pflichtlektüre:
    1. /CLAUDE.md (besonders Session-Ende-Routine)
    2. Letzte Übergabe (besonders TODO-Status)
    3. /docs/STANDARDUERGABE_NEU.md
    4. 🆕 Prüfe Guide-Updates aus Übergabe:
       - Wurden Guides aktualisiert? → Lies die Änderungen
       - Arbeitet diese Session mit Migrationen? → Lies DATABASE_MIGRATION_GUIDE.md
       - Gab es Debug-Sessions? → Lies DEBUG_COOKBOOK.md Updates

    SCHRITT 4 - V5 Fokus prüfen (✅ Auto-Sync):
    cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | sed -n '15,35p'
    → Notiere: Aktueller Fokus, Status, Arbeits-Dokument (⭐)
    → Vergleiche mit ./scripts/get-active-module.sh Output
    → ✅ KEINE DISKREPANZ MEHR: V5 wurde automatisch synchronisiert!

    SCHRITT 5 - TODOs wiederherstellen:
    - Prüfe TODO-Section der letzten Übergabe
    - Führe TodoWrite aus für alle offenen TODOs
    - 🆕 WICHTIG: Prüfe ob ein TODO "Guide aktualisieren" existiert:
      → Falls ja: Plane Zeit für Guide-Update ein
      → Falls Migration-Arbeit: Erstelle TODO "DATABASE_MIGRATION_GUIDE.md aktualisieren"
    - Verifiziere mit TodoRead

    SCHRITT 6 - Aktives Modul:
    ./scripts/get-active-module.sh
    → Bei "Kein Spoke-Dokument": Prüfe neue Struktur in docs/features/ACTIVE/

    SCHRITT 7 - Code-Validierung:
    - git status (stimmt mit Übergabe?)
    - Prüfe genannte Dateien existieren
    - Verifiziere Implementierungsstand
    - cat docs/NEXT_STEP.md (wo genau weitermachen?)
    - 🆕 Bei Migration-Arbeit: Prüfe letzte Migration-Nummer
    - 🆕 Bei Debug-Arbeit: Prüfe bekannte Probleme in DEBUG_COOKBOOK.md

    MELDE DICH MIT:
    - ✅ Arbeits-Branch: feature/[branch-name]
    - ✅ X offene TODOs wiederhergestellt
    - ✅ Aktives Modul: FC-XXX-MX
    - ✅ V5 Fokus: [Phase/Status aus V5] (✅ Auto-Sync)
    - ✅ Nächster Schritt: [aus NEXT_STEP.md oder TODO]
    - 🆕 Guide-Status: [Welche Guides sind relevant für diese Session?]
    - ✅ Migration-Check: [V121 bestätigt/N/A]
    - ⚠️ Diskrepanzen: [Liste - sollten minimal sein dank Auto-Sync]
    - Status: BEREIT FÜR ARBEITSPHASE

    ⛔ STOPP: Warte auf "ARBEITSSTART" Bestätigung bevor du mit der Implementierung beginnst!
```

---

## 🎯 KURZVERSION (für erfahrene Sessions)

**Teil 1:** 
```
Übergabe mit ./scripts/handover-with-sync.sh (Auto-Sync!), alle Punkte aus TRIGGER_TEXTS.md Teil 1 beachten.
```

**Teil 2:**
```
Feature-Branch checkout → ./scripts/robust-session-start.sh → WORKFLOW-VERBOT verstehen → MIGRATION-CHECK (bei DB-Arbeit!) → Docs lesen → ./scripts/get-active-module.sh → ⛔ STOPP: Status melden und auf "ARBEITSSTART" warten!
```

---

## 📋 CHECKLISTE für Übergabe

- [ ] Einleitungstext korrekt
- [ ] 3-STUFEN-SYSTEM erklärt
- [ ] Code-Inspektion durchgeführt
- [ ] ./scripts/handover-with-sync.sh ausgeführt (Auto-Sync V5!)
- [ ] Scripts ausgeführt
- [ ] TodoRead genutzt
- [ ] Alle 6 Fragen beantwortet
- [ ] "NACH KOMPRIMIERUNG" Abschnitt
- [ ] Korrekt gespeichert

---

**⚠️ BACKUP-HINWEIS:** Diese Datei wird auch in .git eingecheckt und ist Teil des Repositories!