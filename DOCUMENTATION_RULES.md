# 📚 Dokumentations-Regeln für FreshPlan

**⚠️ WICHTIG: Diese Regeln gelten für ALLE (Claude, Entwickler, Team)**

## 🎯 Grundprinzip: "Write Less, Update More"

### 1. Dokumentations-Hierarchie (Was darf wo?)

#### Stufe 1: Master-Dokumente (NUR Updates, KEINE neuen Docs!)
- `MASTER_BRIEFING.md` - Projekt-Übersicht
- `PROJECT_STATE.json` - Maschinenlesbare Daten
- `CLAUDE.md` - Arbeitsregeln

#### Stufe 2: Lebende Dokumente (Updates bevorzugt)
- `API_CONTRACT.md` - API-Spezifikation
- `KNOWN_ISSUES.md` - Aktuelle Probleme
- `CHANGELOG.md` - Wichtige Änderungen

#### Stufe 3: Arbeits-Dokumente (Temporär, werden archiviert)
- `docs/claude-daily/YYYY-MM-DD/*.md` - Tägliche Arbeit
- Nach 7 Tagen → Archivierung
- Nach 30 Tagen → Löschung (außer wichtige Erkenntnisse)

### 2. Was NICHT dokumentiert werden soll

❌ **KEINE separaten Docs für:**
- Kleine Bugfixes (→ Git Commit Message)
- Routine-Updates (→ CHANGELOG.md)
- Temporäre Probleme (→ KNOWN_ISSUES.md)
- Code-Erklärungen (→ Code-Kommentare)
- Meeting-Notes (→ Team-Tools)

✅ **Stattdessen:**
- Bestehende Dokumente aktualisieren
- Strukturierte Commit Messages
- Code selbst dokumentieren

### 3. Dokumentations-Lifecycle

```
NEU → 7 Tage aktiv → Review → Archiv/Update/Löschen
```

#### Täglicher Cleanup (automatisch):
1. Docs älter als 7 Tage → Review-Queue
2. Wichtige Infos → in Master-Docs integrieren
3. Veraltete Infos → löschen
4. Arbeits-Docs → archivieren

### 4. Claude's Dokumentations-Regeln

#### Claude DARF:
✅ Master-Dokumente AKTUALISIEREN
✅ In `docs/claude-daily/` TEMPORÄR ablegen
✅ CHANGELOG.md ergänzen
✅ KNOWN_ISSUES.md pflegen

#### Claude DARF NICHT:
❌ Neue Dokumente im Root erstellen
❌ Neue Kategorien erfinden
❌ Redundante Informationen duplizieren
❌ Ohne Datum-Prefix speichern

### 5. Qualitäts-Checks

Vor jeder Dokumentation fragen:
1. Existiert bereits ein Dokument dafür? → UPDATE
2. Ist es temporär? → `claude-daily/`
3. Ist es permanent wichtig? → Master-Docs
4. Ist es Code-spezifisch? → In den Code

### 6. Automatische Bereinigung

```bash
# Täglich ausführen
./scripts/cleanup-old-docs.sh

# Was passiert:
- Docs > 7 Tage → Review
- Docs > 30 Tage → Archiv
- Leere Docs → Löschen
- Duplikate → Zusammenführen
```

### 7. Commit Message statt Dokumentation

Für kleine Änderungen:
```bash
git commit -m "fix(user): Korrigiere Email-Validierung

- Problem: Email mit Umlauten wurden abgelehnt
- Lösung: Regex angepasst für internationale Zeichen
- Test: Unit-Test hinzugefügt
- Closes #123"
```

### 8. Die 3-R-Regel

Bevor du dokumentierst:
1. **Reduce** - Brauchen wir das wirklich?
2. **Reuse** - Können wir ein bestehendes Doc updaten?
3. **Recycle** - Können wir alte Infos ersetzen?

---
*Weniger ist mehr - aber das Wichtige muss aktuell bleiben!*