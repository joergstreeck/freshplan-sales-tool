# 📋 Trigger-Texte Update-Analyse

**Datum:** 12.07.2025  
**Zweck:** Prüfung ob Trigger-Texte an neue Struktur angepasst werden müssen

## 🔍 Analyse der Trigger-Texte

### Triggertext 1 (Übergabe erstellen)

**Aktueller Text ist GUT, aber könnte optimiert werden:**

#### Was passt noch:
- ✅ Verweis auf `./scripts/create-handover.sh`
- ✅ Code-Inspektion Requirement
- ✅ Konkrete Fragen (Was gemacht, was funktioniert, etc.)

#### Was sollte ergänzt werden:
1. **Verweis auf Master Plan V5** statt V4
2. **Quality Standards** erwähnen
3. **ACTIVE/ Ordner** Struktur nutzen

### Triggertext 2 (Session-Start)

**Benötigt Updates für neue Struktur:**

#### Probleme:
1. Verweist auf alte `.current-focus` und `get-active-module.sh`
2. Spoke-Dokument Konzept existiert nicht mehr
3. Keine Erwähnung von Quality Standards

#### Sollte verweisen auf:
1. **Master Plan V5** → Claude Working Section
2. **ACTIVE/ Ordner** für aktuelles Modul
3. **OPEN_QUESTIONS_TRACKER** für offene Fragen

## 📝 Empfohlene Updates

### Neuer Triggertext 1:
```
Erstelle eine vollständige Übergabe für die nächste Session.

NUTZE ZUERST das automatische Script:
./scripts/create-handover.sh

Dann ergänze die [MANUELL AUSFÜLLEN] Bereiche basierend auf:
1. Master Plan V5 - Claude Working Section
2. ACTIVE/ Ordner - Aktueller Fortschritt
3. Quality Standards - Wurden alle Checks durchgeführt?
4. Git-Status - Commits gemacht wie geplant?

WICHTIG:
- Verifiziere den Stand durch Code-Inspektion
- Referenziere das aktive Modul aus ACTIVE/
- Dokumentiere offene Fragen aus OPEN_QUESTIONS_TRACKER
- Halte die Übergabe KURZ und FOKUSSIERT
```

### Neuer Triggertext 2:
```
Lese alles gründlich durch und befolge strikt die Standardübergabe.
WICHTIG: Dies ist NUR die Orientierungsphase - noch NICHT arbeiten!

SCHRITTE:
1. Führe ./scripts/session-start.sh aus
2. Lies in dieser Reihenfolge:
   - CLAUDE.md (Arbeitsrichtlinien)
   - STANDARDUBERGABE_NEU.md (Prozess)
   - CRM_COMPLETE_MASTER_PLAN_V5.md → Claude Working Section
3. Check aktives Modul im ACTIVE/ Ordner
4. Lies OPEN_QUESTIONS_TRACKER.md für offene Punkte
5. Validiere Code-Stand durch Inspektion

DANN STOPPE und melde dich mit:
- Bestätigung des aktiven Moduls (aus ACTIVE/)
- Bestätigung des nächsten Schritts (aus README.md)
- Eventuelle Diskrepanzen zwischen Doku und Code
- Status: BEREIT FÜR ARBEITSPHASE
```

## 🚨 Weitere notwendige Anpassungen

### STANDARDUBERGABE_NEU.md sollte erwähnen:
1. **Master Plan V5** als zentrale Navigation
2. **ACTIVE/ Ordner** Konzept
3. **Quality Standards** Integration
4. **Daily Workflow** Guide

### Scripts die angepasst werden müssen:
1. `get-active-module.sh` → Sollte ACTIVE/ Ordner nutzen
2. `create-handover.sh` → Template mit neuer Struktur
3. `orientation-check.sh` → Quality Standards prüfen

## ✅ Fazit

Die Trigger-Texte sind **grundsätzlich gut**, benötigen aber **Updates** für:
- Neue Ordnerstruktur (ACTIVE/, PLANNED/, etc.)
- Master Plan V5 statt V4
- Quality Standards Integration
- Wegfall von Spoke-Dokumenten

Die vorgeschlagenen Updates machen die Trigger-Texte kompatibel mit der neuen, bereinigten Struktur.