# 📋 TRIGGER-TEXT UPDATE - 21.07.2025

**Zweck:** Aktualisierung der Übergabe-Trigger nach 100% CLAUDE TECH Migration

---

## ✅ WAS WURDE AKTUALISIERT

### 1. TRIGGER_TEXTS.md
- **Datum:** Von 17.07.2025 auf 21.07.2025 aktualisiert
- **Kontext:** Neuer Context-Header hinzugefügt, der die 100% Migration reflektiert
- **Strukturbereinigung:** Veraltete Referenzen entfernt (bereits abgeschlossen)
- **Fokus:** Auf Implementation mit CLAUDE_TECH Dokumenten verschoben

### 2. ENTFERNTE ELEMENTE
- Strukturbereinigung-Plan Referenzen (veraltet)
- Sales Command Center Hinweise (bereits dokumentiert)
- Hybrid-Ansatz Erklärungen (nicht mehr relevant)

### 3. NEUE ELEMENTE
```
🎯 CONTEXT: CLAUDE TECH Migration 100% complete (46/46 Features)
   Alle Features haben optimierte CLAUDE_TECH Dokumente ready für Implementation!
```

### 4. SCRIPT VALIDIERUNG
- `create-handover.sh`: ✅ Funktioniert weiterhin korrekt
- Template enthält bereits flexible Platzhalter
- Keine Änderungen am Script erforderlich

---

## 🚀 FINALER TRIGGER-TEXT FÜR COPY & PASTE

```
Erstelle eine vollständige Übergabe für die nächste Session.

🎯 CONTEXT: CLAUDE TECH Migration 100% complete (46/46 Features)
   Alle Features haben optimierte CLAUDE_TECH Dokumente ready für Implementation!

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
   - Beispiel: **Plan:** /docs/features/ACTIVE/.../FC-XXX_CLAUDE_TECH.md - Feature X Implementation - Status: IN ARBEIT
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

## 📌 VORTEILE DER NEUEN VERSION

1. **Klarer Kontext:** Sofort ersichtlich, dass CLAUDE TECH Migration abgeschlossen ist
2. **Fokus auf Implementation:** Keine veralteten Migrations-Referenzen mehr
3. **Saubere Struktur:** Entfernung aller veralteten Strukturbereinigung-Hinweise
4. **Ready-to-use:** Direkt kopierbar ohne Anpassungen

---

## ✅ NÄCHSTE SCHRITTE

1. Verwende den neuen Trigger-Text für alle zukünftigen Übergaben
2. Die offizielle Version ist jetzt in `/docs/TRIGGER_TEXTS.md` gespeichert
3. Teil 2 (Session-Start) war bereits korrekt - keine Änderungen erforderlich!

**Die Übergabe-Organisation ist jetzt auf dem neuesten Stand und reflektiert den 100% CLAUDE TECH Status!**

---

## 🚀 FINALER SESSION-START TRIGGER-TEXT (TEIL 2)

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