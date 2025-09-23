# 🔒 GESICHERTE TRIGGER-TEXTE FÜR STANDARDÜBERGABE

**WICHTIG: Diese Datei enthält die offiziellen Trigger-Texte. NIEMALS löschen oder überschreiben!**

**Version:** 3.2
**Letzte Aktualisierung:** 23.09.2025
**Major Update:** Auto-Compact-Prävention + MP5-Härtung + Migration-Fallback-Filter + COMPACT_CONTRACT Integration

---

## 📝 TEIL 1: Vollständige Übergabe erstellen (vor Komprimierung)

```
Erstelle eine vollständige Übergabe für die nächste Session.

  # 🛑 ÜBERGABE ERSTELLEN - NUR DIESE SCHRITTE AUSFÜHREN!

  ## ⚠️ KRITISCH: Führe NUR diese Schritte aus, dann STOPPE!

  ### SCHRITT 0: Context-Frühwarnung (Auto-Compact-Prävention)
  - Wenn diese Antwort > ~500 Wörter geworden wäre ODER ein großer Meilenstein (z.B. Datei/Test/Feature) gerade abgeschlossen wurde:
    → Sofort **MP5 QUICK UPDATE** erzeugen (Format siehe CLAUDE.md)
    → Danach mit SCHRITT 1 fortfahren

  ### SCHRITT 1: TODO-Status sichern
  ```bash
  TodoRead
  # Optional für bessere Zählung:
  TodoRead > /tmp/current-todos.txt

  SCHRITT 2: MIGRATION-CHECK (🚨 PFLICHT bei DB-Arbeit!)

  Primäres Script (immer verwenden):
  /Users/joergstreeck/freshplan-sales-tool/scripts/get-next-migration.sh
  # Ausgabe MUSS dem Muster V[0-9]+ entsprechen (z.B. V226)
  # Diese Nummer IMMER in Übergabe notieren!
  # NIEMALS alte Nummern wiederverwenden!

  # Fallback bei Script-Fehler (Templates ausblenden):
  ls -la backend/src/main/resources/db/migration/ | grep -v 'VXXX__' | tail -3
  # Nächste freie Produktions-Nummer manuell bestimmen

  SCHRITT 3: Universelles Handover-Script
  /Users/joergstreeck/freshplan-sales-tool/scripts/create-handover.sh
  # ABSOLUTER PFAD - funktioniert IMMER aus JEDEM Verzeichnis!
  # Wählt automatisch das beste verfügbare Script:
  # - create-handover-universal.sh (NEU: universelle Version)
  # - handover-with-sync-stable.sh (mit V5 Sync)
  # - create-handover-improved.sh (verbesserte Version)
  # - Minimales Fallback-Template wenn nötig

  SCHRITT 3b: MP5-Update JETZT erzwingen (Compact-Contract)

  Erzeuge SOFORT ein MP5 QUICK UPDATE (Format siehe CLAUDE.md) und füge es in
  /docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md ein über die Anker:
  - <!-- MP5:SESSION_LOG:START --> ... <!-- MP5:SESSION_LOG:END -->
  - <!-- MP5:NEXT_STEPS:START --> ... <!-- MP5:NEXT_STEPS:END -->
  - <!-- MP5:RISKS:START --> ... <!-- MP5:RISKS:END -->
  - <!-- MP5:DECISIONS:START --> ... <!-- MP5:DECISIONS:END -->

  Falls Dateischreiben nicht möglich: Quick Update in die Übergabe unter "MP5 UPDATE PENDING"

  SCHRITT 4: Template VOLLSTÄNDIG ausfüllen

  Die Übergabe wurde erstellt in:
  docs/planung/claude-work/daily-work/YYYY-MM-DD/

  PFLICHT-FELDER ausfüllen:
  1. TODO-Status (aus TodoRead) ✅ BEREITS GEMACHT
  2. MIGRATION-NUMMER (aus Schritt 2) ⚠️ KRITISCH - in Handover korrigieren!
  3. "Was wurde gemacht?" - mit konkreten git diff Statistiken
  4. "Bekannte Probleme" - alle aufgetretenen Issues dokumentieren
  5. "Nächster Schritt" - konkret formulieren

  TEMPLATE-STRUKTUR prüfen:
  - Ist "Was wurde gemacht" Section vorhanden?
  - Sind "Bekannte Probleme" dokumentiert?
  - Ist der "Nächste Schritt" konkret formuliert?

  SCHRITT 5: Master Plan V5 Status-Update (🆕 WICHTIG!)

  Prüfe und aktualisiere: /docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md

  - Feature-Status aktualisieren: 📋 Geplant → 🔄 In Progress → ✅ Abgeschlossen
  - Timeline-Änderungen dokumentieren
  - Neue Blocker/Dependencies eintragen
  - Fortschritts-Notizen hinzufügen
  - Aktuelle Sprint-Woche anpassen

  SCHRITT 6: Strukturelle Dokumentation (🆕 NEUE ARCHITEKTUR!)

  Prüfe relevante Updates:
  - Technical Concepts in /docs/planung/features-neu/ aktualisiert?
  - Infrastructure-Pläne in /docs/planung/infrastruktur/ betroffen?
  - Neue Architektur-Entscheidungen dokumentieren?

  SCHRITT 7: Git-Commit KONKRET ANFRAGEN (🆕 SMART!)

  1. Git-Status prüfen: `git status`
  2. Commit-Message formulieren (vollständig mit Beschreibung)
  3. DIREKTE FRAGE stellen:
     "Soll ich die geänderten Dateien committen?

     git add [dateien]
     git commit -m "[message]"

     ⚠️ WARTE AUF DEINE BESTÄTIGUNG:
     - JA → Ich führe den Commit aus
     - NEIN → Git-Status bleibt unverändert
     - ÄNDERN → Du sagst mir was geändert werden soll"

  4. NUR bei "JA" committen - NIEMALS ohne explizite Erlaubnis!

  SCHRITT 8: Finale Validierung

  - Alle TODOs dokumentiert?
  - MIGRATION-NUMMER in Übergabe? ⚠️ KRITISCH
  - Master Plan V5 aktuell?
  - TRIGGER_INDEX.md Workflow befolgt?
  - Git-Status sauber oder Commit vorbereitet?
  - Strukturelle Updates vollständig?

  FERTIG! Vollständige Übergabe komplett.

  ## 🚨 EMERGENCY HANDOVER (Auto-Compact)
  Falls Auto-Compact ohne Trigger passierte, dokumentiere mindestens:
  - Code Changes (kurz): Dateien/Module & Ergebnis
  - Tests: OK/Broken
  - Migration: VXXX (falls relevant)
  - Next Steps: 1-2 konkrete Punkte
  - Hinweis: "MP5 QUICK UPDATE bitte jetzt anwenden"
  - Optional: Pfad zur Detail-Doku in docs/planung/claude-work/daily-work/YYYY-MM-DD/
```

---

## 🚀 TEIL 2: Session-Start (nach Komprimierung)

```
Lese alles gründlich durch und befolge strict die Standardübergabe.

  # 🛑 STANDARDÜBERGABE - STOPPE NACH DIESEN SCHRITTEN!

  ## ⛔ KRITISCHER HALT
  **NUR die folgenden Schritte ausführen.**
  **DANN WARTEN auf "ARBEITSSTART"!**
  **KEINE eigenmächtige Arbeit!**

  ### SCHNELL-CHECK (3 Min):

  #### 1. Branch Check
  ```bash
  git branch --show-current
  /Users/joergstreeck/freshplan-sales-tool/scripts/get-current-feature-branch.sh

  # Falls kein Feature-Branch:
  git checkout -b feature/fc-XXX-[description]

  2. MIGRATION-CHECK (🚨 PFLICHT bei DB-Arbeit!)

  Primär:
  /Users/joergstreeck/freshplan-sales-tool/scripts/get-next-migration.sh
  # Ausgabe MUSS V[0-9]+ sein (z.B. V226)
  # Diese Nummer für heutige DB-Arbeit verwenden!

  # Fallback (Templates ausblenden):
  ls -la backend/src/main/resources/db/migration/ | grep -v 'VXXX__' | tail -3

  3. System-Start

  /Users/joergstreeck/freshplan-sales-tool/scripts/robust-session-start.sh
  # ABSOLUTER PFAD - funktioniert aus JEDEM Verzeichnis!
  # Prüft Services, startet PostgreSQL, zeigt Status
  # Bei Fehler: TRIGGER_INDEX.md Workflow befolgen

  4. Pflichtlektüre (🆕 NEUE STRUKTUR!)

  1. /CLAUDE.md (Arbeitsrichtlinien)
  2. Letzte Übergabe in /docs/planung/claude-work/daily-work/
  3. /docs/planung/TRIGGER_INDEX.md (Sprint-Workflow & 7-Dokumente-Reihenfolge)
  4. /docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md (Standard-Context)
  5. /docs/STANDARDUBERGABE_NEU.md (falls Details fehlen)

  ### MP5-Priorität (Pflicht)
  - Öffne die letzte Übergabe (heute)
  - Falls ein Block "MP5 UPDATE PENDING" vorhanden ist: ZUERST MP5 aktualisieren
  - Danach KEINE neuen Dokumente erzeugen - nur MP5 pflegen

  5. TODOs laden

  Aus letzter Übergabe TODO-Status → TodoWrite

  STATUS MELDEN:

  📋 STATUS:
  - Branch: [name]
  - TODOs: [X offen]
  - Migration: [VXXX] ⚠️ BESTÄTIGT
  - Nächster Schritt: [aus TRIGGER_INDEX.md]
  - ⛔ WARTE AUF ARBEITSSTART

  ## 📌 Compact-Kontrolle während der Arbeit
  - Bei ~70% Context FÜHRE /checkpoint aus (MP5 QUICK UPDATE)
  - Nach jedem großen Meilenstein (Feature/Tests/Datei): /checkpoint
  - Bei Session-Abschluss: /finalize → MP5 Update + keine neuen Files

  ⛔ STOPP! WARTE AUF "ARBEITSSTART"!
```

---

## 🎯 KURZVERSION (für erfahrene Sessions)

**Teil 1:** 
```
Übergabe mit ./scripts/create-handover.sh (Universal-Script mit Auto-Sync!), alle Punkte aus TRIGGER_TEXTS.md Teil 1 beachten.
```

**Teil 2:**
```
Feature-Branch checkout → ./scripts/robust-session-start.sh → MIGRATION-CHECK (Templates filtern!) → MP5-Priorität prüfen → Docs lesen → /checkpoint-Pattern verstehen → ⛔ STOPP: Status melden und auf "ARBEITSSTART" warten!
```

---

## 📋 ERWEITERTE CHECKLISTE für Vollständige Übergabe

### ✅ Standard-Schritte (bewährt):
- [ ] TodoRead ausgeführt und dokumentiert?
- [ ] Migration-Nummer geprüft und notiert? ⚠️ KRITISCH
- [ ] Handover-Script erfolgreich ausgeführt?
- [ ] Template vollständig ausgefüllt?
- [ ] Git-Status sauber?

### 🆕 Neue Struktur-Updates:
- [ ] Master Plan V5 Feature-Status aktualisiert?
- [ ] Sprint-Woche und Timeline angepasst?
- [ ] TRIGGER_INDEX.md Workflow befolgt?
- [ ] Technical Concepts (falls bearbeitet) aktualisiert?
- [ ] Infrastructure-Pläne (falls relevant) gepflegt?

### 🔄 Smart-Features (V3.2):
- [ ] Template vollständig: "Was gemacht" + "Bekannte Probleme" ausgefüllt?
- [ ] Git-Commit konkret gefragt (nicht nur vorgeschlagen)?
- [ ] User-Antwort abgewartet: JA/NEIN/ÄNDERN?
- [ ] Strukturelle Dokumentation geprüft?
- [ ] TODO-Dokumentation in Übergabe vollständig?
- [ ] MP5 QUICK UPDATE erzwungen (Schritt 3b)?
- [ ] Context-Frühwarnung beachtet (Schritt 0)?
- [ ] Emergency Handover Pattern verfügbar?

---

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

### Standard-Regeln:
1. **NIEMALS** alte Migration-Nummern wiederverwenden
2. **IMMER** auf Feature-Branch arbeiten
3. **NIE** direkt auf main committen
4. **WARTEN** auf "ARBEITSSTART" vor Implementierung

### V3.2 Spezifische Fehler:
5. **Template-Unvollständigkeit:** "Was gemacht" und "Bekannte Probleme" vergessen
6. **Commit-Vorschlag statt Frage:** Nicht fragen sondern nur vorschlagen
7. **Migration-Nummer-Inkonsistenz:** Script-Output vs. tatsächliche Nummer
8. **Handover-Pfad falsch:** docs/claude-work/ statt docs/planung/claude-work/
9. **Master Plan V5 nicht aktualisiert:** Status-Updates vergessen
10. **Context-Frühwarnung ignoriert:** Kein /checkpoint bei 70% Context
11. **MP5-Update vergessen:** Schritt 3b übersprungen
12. **VXXX-Filter vergessen:** Templates im Migration-Fallback angezeigt
13. **Auto-Compact unvorbereitet:** Emergency Handover Pattern nicht angewandt

---

## 📜 ARCHIV

Vorherige Versionen:
- v3.1: NEXT_STEP.md entfernt + TRIGGER_INDEX.md Workflow
- v2.7: `/docs/TRIGGER_TEXTS_v2.7_archived.md` (handover-with-sync.sh Version)
- v2.6: Erste Version mit MIGRATION-CHECK als Pflichtschritt

---

**Ende der Trigger-Texte - Diese Datei ist die offizielle Referenz!**