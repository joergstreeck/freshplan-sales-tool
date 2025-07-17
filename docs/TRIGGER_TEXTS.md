# 🔒 GESICHERTE TRIGGER-TEXTE FÜR STANDARDÜBERGABE

**WICHTIG: Diese Datei enthält die offiziellen Trigger-Texte. NIEMALS löschen oder überschreiben!**

Letzte Aktualisierung: 17.07.2025

---

## 📝 TEIL 1: Übergabe erstellen (vor Komprimierung)

```
Erstelle eine vollständige Übergabe für die nächste Session.

SCHRITT 1 - TODO-Status sichern:
  ⚠️ PFLICHT: Führe TodoRead aus
  ⚠️ OPTIONAL aber empfohlen: TodoRead > .current-todos.md
     (macht die automatische Zählung zuverlässiger)

SCHRITT 2 - Automatisches Script:
./scripts/create-handover.sh

SCHRITT 3 - Script-Output prüfen:
- Falls Fehler/Warnungen → STOPPE und behebe erst das Problem
- Falls "Kein Spoke-Dokument gefunden" → Prüfe neue V5-Struktur in /docs/features/ACTIVE/

SCHRITT 4 - TODOs einfügen:
Ersetze {{TODO_LIST}} mit:
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
   - Beispiel: **Plan:** /docs/claude-work/.../feature-plan.md - Feature X Implementation - Status: IN ARBEIT
6. 🚨 UNTERBRECHUNGEN: Falls interrupted, wo genau warst du?
   - Format: **Unterbrochen bei:** [TODO-XXX] - [Datei:Zeile] - [Was war der nächste Schritt]

SCHRITT 6 - NEXT_STEP.md aktualisieren:
- Prüfe: cat docs/NEXT_STEP.md
- Update mit aktuellem Stand:
- Bei TODO-Wechsel: Neues TODO + konkreter Schritt
- Bei Unterbrechung: Datei:Zeile wo du warst
- Format siehe NEXT_STEP.md Template 

SCHRITT 7 - Validierung:
- [ ] TodoRead ausgeführt? (Anzahl: X)
- [ ] Alle TODOs in Übergabe? (Anzahl: X)
- [ ] Zahlen stimmen überein? ⚠️ KRITISCH
- [ ] Git-Status korrekt?
- [ ] Service-Status geprüft?
- [ ] V5 Fokus dokumentiert? (aus CRM_COMPLETE_MASTER_PLAN_V5.md)
- [ ] NEXT_STEP.md aktuell?
- [ ] Nächste Schritte klar?
- [ ] Strategische Pläne verlinkt?

WICHTIG: Ohne TODO-Dokumentation ist die Übergabe UNGÜLTIG!
```

---

## 🚀 TEIL 2: Session-Start (nach Komprimierung)

```
Lese alles gründlich durch und befolge strict die Standardübergabe.
WICHTIG: Dies ist NUR die Orientierungsphase - noch NICHT arbeiten!

SCHRITT 1 - System vorbereiten:
./scripts/session-start.sh
→ Bei Fehlern/Warnungen STOPPE und analysiere

SCHRITT 2 - Pflichtlektüre:
1. CLAUDE.md (besonders Session-Ende-Routine)
2. Letzte Übergabe (besonders TODO-Status)
3. STANDARDUBERGABE_NEU.md

SCHRITT 3 - V5 Fokus prüfen:
cat docs/CRM_COMPLETE_MASTER_PLAN_V5.md | sed -n '15,35p'
→ Notiere: Aktueller Fokus, Status, Arbeits-Dokument (⭐)
→ Vergleiche mit get-active-module.sh Output
→ Bei Diskrepanz: Welcher Stand ist aktueller?

SCHRITT 4 - TODOs wiederherstellen:
- Prüfe TODO-Section der letzten Übergabe
- Führe TodoWrite aus für alle offenen TODOs
- Verifiziere mit TodoRead

SCHRITT 5 - Aktives Modul:
./scripts/get-active-module.sh
→ Bei "Kein Spoke-Dokument": Prüfe neue Struktur in /docs/features/ACTIVE/

SCHRITT 6 - Code-Validierung:
- git status (stimmt mit Übergabe?)
- Prüfe genannte Dateien existieren
- Verifiziere Implementierungsstand
- cat docs/NEXT_STEP.md (wo genau weitermachen?)

MELDE DICH MIT:
- ✅ X offene TODOs wiederhergestellt
- ✅ Aktives Modul: FC-XXX-MX 
- ✅ V5 Fokus: [Phase/Status aus V5]
- ✅ Nächster Schritt: [aus NEXT_STEP.md oder TODO]
- ⚠️ Diskrepanzen: [Liste]
- Status: BEREIT FÜR ARBEITSPHASE
```

---

## 🎯 KURZVERSION (für erfahrene Sessions)

**Teil 1:** 
```
TodoRead → TodoRead > .current-todos.md → ./scripts/create-handover.sh → TODOs einfügen → NEXT_STEP.md update → Validierung
```

**Teil 2:**
```
./scripts/session-start.sh → Docs lesen → V5 Check → TODOs wiederherstellen → Status melden → BEREIT FÜR ARBEITSPHASE
```

---

## 📋 CHECKLISTE für Übergabe

- [ ] TodoRead ausgeführt
- [ ] .current-todos.md erstellt (optional)
- [ ] create-handover.sh ausgeführt
- [ ] TODOs in Übergabe eingefügt
- [ ] Git-Status dokumentiert
- [ ] V5 Fokus dokumentiert
- [ ] NEXT_STEP.md aktualisiert
- [ ] Strategische Pläne verlinkt
- [ ] Validierung durchgeführt
- [ ] Korrekt gespeichert

---

**⚠️ BACKUP-HINWEIS:** Diese Datei wird auch in .git eingecheckt und ist Teil des Repositories!