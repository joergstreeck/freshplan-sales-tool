# 🔒 GESICHERTE TRIGGER-TEXTE FÜR STANDARDÜBERGABE

**WICHTIG: Diese Datei enthält die offiziellen Trigger-Texte. NIEMALS löschen oder überschreiben!**

**Version:** 2.9  
**Letzte Aktualisierung:** 08.08.2025  
**Neues Feature:** Alle Scripts mit absoluten Pfaden - funktionieren IMMER aus JEDEM Verzeichnis

---

## 📝 TEIL 1: Übergabe erstellen (vor Komprimierung)

```
Erstelle eine vollständige Übergabe für die nächste Session.

  # 🛑 ÜBERGABE ERSTELLEN - NUR DIESE SCHRITTE AUSFÜHREN!

  ## ⚠️ KRITISCH: Führe NUR diese Schritte aus, dann STOPPE!

  ### SCHRITT 1: TODO-Status sichern
  ```bash
  TodoRead
  # Optional für bessere Zählung:
  TodoRead > /tmp/current-todos.txt

  SCHRITT 2: MIGRATION-CHECK (🚨 PFLICHT bei DB-Arbeit!)

  /Users/joergstreeck/freshplan-sales-tool/scripts/get-next-migration.sh
  # Diese Nummer IMMER in Übergabe notieren!
  # NIEMALS alte Nummern wiederverwenden!

  # Fallback bei Script-Fehler:
  ls -la backend/src/main/resources/db/migration/ | tail -3
  # Manuell nächste V-Nummer berechnen

  SCHRITT 3: Universelles Handover-Script

  /Users/joergstreeck/freshplan-sales-tool/scripts/create-handover.sh
  # ABSOLUTER PFAD - funktioniert IMMER aus JEDEM Verzeichnis!
  # Wählt automatisch das beste verfügbare Script:
  # - create-handover-universal.sh (NEU: universelle Version)
  # - handover-with-sync-stable.sh (mit V5 Sync)
  # - create-handover-improved.sh (verbesserte Version)
  # - Minimales Fallback-Template wenn nötig

  # Das Script zeigt automatisch die Migration-Nummer prominent an
  # Bei Fehler wurde Nummer bereits in Schritt 2 ermittelt

  SCHRITT 4: Template ausfüllen

  Die Übergabe wurde erstellt in:
  docs/claude-work/daily-work/YYYY-MM-DD/

  Fülle aus:
  1. TODO-Status (aus TodoRead)
  2. MIGRATION-NUMMER (aus Schritt 2) ⚠️ KRITISCH
  3. Was wurde gemacht? (git diff --stat)
  4. Bekannte Probleme
  5. NEXT_STEP.md Update

  SCHRITT 5: Validierung

  - Alle TODOs dokumentiert?
  - MIGRATION-NUMMER in Übergabe? ⚠️ KRITISCH
  - NEXT_STEP.md aktuell?
  - Git-Status sauber?

  FERTIG! Übergabe komplett.
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

  /Users/joergstreeck/freshplan-sales-tool/scripts/get-next-migration.sh
  # Diese Nummer IMMER verwenden!

  # Fallback bei Script-Fehler:
  ls -la backend/src/main/resources/db/migration/ | tail -3

  3. System-Start

  /Users/joergstreeck/freshplan-sales-tool/scripts/robust-session-start.sh
  # ABSOLUTER PFAD - funktioniert aus JEDEM Verzeichnis!
  # Prüft Services, startet PostgreSQL, zeigt Status
  # Bei Fehler: cat docs/NEXT_STEP.md

  4. Pflichtlektüre

  1. /docs/CLAUDE.md
  2. Letzte Übergabe in /docs/claude-work/daily-work/
  3. /docs/STANDARDUERGABE_NEU.md (falls Details fehlen)

  5. TODOs laden

  Aus letzter Übergabe TODO-Status → TodoWrite

  STATUS MELDEN:

  📋 STATUS:
  - Branch: [name]
  - TODOs: [X offen]
  - Migration: [VXXX] ⚠️ BESTÄTIGT
  - Nächster Schritt: [aus NEXT_STEP.md]
  - ⛔ WARTE AUF ARBEITSSTART

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
Feature-Branch checkout → ./scripts/robust-session-start.sh → WORKFLOW-VERBOT verstehen → MIGRATION-CHECK (bei DB-Arbeit!) → Docs lesen → ./scripts/get-active-module.sh → ⛔ STOPP: Status melden und auf "ARBEITSSTART" warten!
```

---

## 📋 CHECKLISTE für Übergabe

- [ ] TodoRead ausgeführt und dokumentiert?
- [ ] Migration-Nummer geprüft und notiert?
- [ ] Handover-Script erfolgreich ausgeführt?
- [ ] Template vollständig ausgefüllt?
- [ ] NEXT_STEP.md aktualisiert?
- [ ] Git-Status sauber?
- [ ] V5 Master Plan synchronisiert?

---

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **NIEMALS** alte Migration-Nummern wiederverwenden
2. **IMMER** auf Feature-Branch arbeiten
3. **NIE** direkt auf main committen
4. **WARTEN** auf "ARBEITSSTART" vor Implementierung
5. **DOKUMENTIEREN** aller TODOs in Übergabe

---

## 📜 ARCHIV

Vorherige Versionen:
- v2.7: `/docs/TRIGGER_TEXTS_v2.7_archived.md` (handover-with-sync.sh Version)
- v2.6: Erste Version mit MIGRATION-CHECK als Pflichtschritt

---

**Ende der Trigger-Texte - Diese Datei ist die offizielle Referenz!**