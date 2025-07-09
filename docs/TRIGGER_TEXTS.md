# 🔒 GESICHERTE TRIGGER-TEXTE FÜR STANDARDÜBERGABE

**WICHTIG: Diese Datei enthält die offiziellen Trigger-Texte. NIEMALS löschen oder überschreiben!**

Letzte Aktualisierung: 05.07.2025

---

## 📝 TEIL 1: Übergabe erstellen (vor Komprimierung)

```
Erstelle eine vollständige Übergabe für die nächste Session. Die Übergabe MUSS mit folgendem Text beginnen: "WICHTIG: Lies ZUERST diese Dokumente in dieser Reihenfolge: 1) /docs/CLAUDE.md 2) Diese Übergabe 3) /docs/STANDARDUBERGABE_NEU.md als Hauptanleitung". 
Erkläre das 3-STUFEN-SYSTEM: STANDARDUBERGABE_NEU.md (Hauptdokument mit 5 Schritten), STANDARDUBERGABE_KOMPAKT.md (Ultra-kurz für Quick-Reference), STANDARDUBERGABE.md (nur bei Problemen). 
WICHTIG: Verifiziere ERST den echten Implementierungsstand durch Code-Inspektion (Glob/Grep/Read), dann dokumentiere was WIRKLICH fertig ist vs. was noch fehlt. Keine Annahmen - nur Fakten aus dem Code!
Führe jetzt ./scripts/validate-config.sh und ./scripts/check-services.sh aus und dokumentiere das Ergebnis. Nutze TodoRead für den aktuellen Stand. 
Dokumentiere klar: 
1) Was wurde heute gemacht? 
2) Was funktioniert?
3) Welche Fehler gibt es? 
4) Wie wurden sie gelöst oder was ist zu tun? 
5) Was sind die nächsten konkreten Schritte? 
6) Welcher Plan/Dokument ist gerade maßgeblich? 
Füge am Ende einen Abschnitt "NACH KOMPRIMIERUNG SOFORT AUSFÜHREN" mit den wichtigsten Befehlen ein. Speichere in /docs/claude-work/daily-work/YYYY-MM-DD/YYYY-MM-DD_HANDOVER_HH-MM.md
Denke dabei immer daran, was du benötigst, um effektiv weiter zu arbeiten.
TIPP: Du kannst ./scripts/create-handover.sh als Basis-Template nutzen.
```

---

## 🚀 TEIL 2: Session-Start (nach Komprimierung)

```
Lese alles gründlich durch und befolge strict die Standardübergabe. 
Wichtig: Fange noch nicht an zu arbeiten.
Führe ./scripts/session-start.sh aus für einen optimalen Start.
Lies dann zuerst CLAUDE.md, dann lese STANDARDUBERGABE_NEU.md und dann CRM_COMPLETE_MASTER_PLAN.md. Lese alle Dateien komplett. Wichtig: du pushst nie direkt in den main-branch.
DANN FOKUSSIERE DICH:
  1. Führe aus: ./scripts/get-active-module.sh
  2. Öffne das mit ⭐ markierte Spoke-Dokument
  3. Gehe direkt zum Abschnitt "NÄCHSTER SCHRITT FÜR IMPLEMENTIERUNG"
  4. Validiere den dokumentierten Stand durch Code-Inspektion
  5. Beginne mit der Arbeit am dokumentierten nächsten Schritt
Wenn du das getan hast, melde dich bei mir mit:
  - Bestätigung des aktiven Moduls (FC-XXX-MX)
  - Bestätigung des nächsten Schritts
  - Eventuelle Diskrepanzen zwischen Doku und Code
TIPP: Nutze während der Arbeit:
  - ./scripts/update-focus.sh - um deinen Fortschritt zu tracken
  - ./scripts/get-active-module.sh - um Dokumente schnell zu finden
```

---

## 🎯 KURZVERSION (für erfahrene Sessions)

**Teil 1:** 
```
Übergabe mit ./scripts/create-handover.sh, alle Punkte aus TRIGGER_TEXTS.md Teil 1 beachten.
```

**Teil 2:**
```
./scripts/session-start.sh → Docs lesen → get-active-module.sh → ⛔ STOPP: Status melden und auf "ARBEITSSTART" warten!
```

---

## 📋 CHECKLISTE für Übergabe

- [ ] Einleitungstext korrekt
- [ ] 3-STUFEN-SYSTEM erklärt
- [ ] Code-Inspektion durchgeführt
- [ ] Scripts ausgeführt
- [ ] TodoRead genutzt
- [ ] Alle 6 Fragen beantwortet
- [ ] "NACH KOMPRIMIERUNG" Abschnitt
- [ ] Korrekt gespeichert

---

**⚠️ BACKUP-HINWEIS:** Diese Datei wird auch in .git eingecheckt und ist Teil des Repositories!