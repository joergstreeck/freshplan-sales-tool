# 📋 OPTIMIERTE TRIGGER-TEXTE V2.0

**Datum:** 08.08.2025
**Autor:** Claude
**Status:** GETESTET & VERBESSERT

---

## 🎯 TEXT 1: SESSION-ENDE (Übergabe erstellen)

```markdown
# 🛑 ÜBERGABE ERSTELLEN - NUR DIESE SCHRITTE AUSFÜHREN!

## ⚠️ KRITISCH: Führe NUR diese Schritte aus, dann STOPPE!

### SCHRITT 1: TODO-Status sichern
```bash
TodoRead
# Optional für bessere Zählung:
TodoRead > /tmp/current-todos.txt
```

### SCHRITT 2: Stabilisiertes Handover-Script
```bash
./scripts/handover-with-sync-stable.sh

# Bei Fehler - Manueller Fallback:
ls -la backend/src/main/resources/db/migration/ | tail -3
# Notiere nächste V-Nummer für Übergabe
```

### SCHRITT 3: Template ausfüllen
Die Übergabe wurde erstellt in:
`docs/claude-work/daily-work/YYYY-MM-DD/`

Fülle aus:
1. TODO-Status (aus TodoRead)
2. Was wurde gemacht? (git diff --stat)
3. Bekannte Probleme
4. NEXT_STEP.md Update

### SCHRITT 4: Validierung
- [ ] Alle TODOs dokumentiert?
- [ ] Migration-Nummer notiert?
- [ ] NEXT_STEP.md aktuell?
- [ ] Git-Status sauber?

**FERTIG! Übergabe komplett.**
```

---

## 🚀 TEXT 2: SESSION-START (Neue Session)

```markdown
# 🛑 STANDARDÜBERGABE - STOPPE NACH DIESEN SCHRITTEN!

## ⛔ KRITISCHER HALT
**NUR die folgenden Schritte ausführen.**
**DANN WARTEN auf "ARBEITSSTART"!**
**KEINE eigenmächtige Arbeit!**

### SCHNELL-CHECK (2 Min):

#### 1. Branch & Migration
```bash
git branch --show-current
./scripts/get-current-feature-branch.sh

# Bei DB-Arbeit:
ls -la backend/src/main/resources/db/migration/ | tail -3
```

#### 2. System-Start
```bash
./scripts/robust-session-start.sh
# Bei Fehler: cat docs/NEXT_STEP.md
```

#### 3. TODOs laden
Aus letzter Übergabe TODO-Status → TodoWrite

### STATUS MELDEN:
```
📋 STATUS:
- Branch: [name]
- TODOs: [X offen]
- Migration: [VXXX]
- Nächster Schritt: [aus NEXT_STEP.md]
- ⛔ WARTE AUF ARBEITSSTART
```

## ⛔ STOPP! WARTE AUF "ARBEITSSTART"!
```

---

## 🔄 VEREINFACHTER WORKFLOW

### Am Session-Ende:
1. User: "Erstelle Übergabe"
2. Claude: Verwendet **TEXT 1** → erstellt Übergabe → FERTIG
3. User: `/compact`

### Bei neuer Session:
1. User: Verwendet **TEXT 2**
2. Claude: Führt Schnell-Check aus → meldet Status → WARTET
3. User: "ARBEITSSTART"
4. Claude: Beginnt mit Arbeit

---

## ✅ VORTEILE DER V2 TEXTE:

1. **KÜRZER:** Nur essenzielle Schritte
2. **KLARER STOPP:** Prominent platziert, mehrfach wiederholt
3. **FALLBACKS:** Bei Script-Fehlern gibt es Alternativen
4. **MIGRATION:** Explizit geprüft mit ls-Befehl
5. **KEINE REDUNDANZ:** Keine langen Erklärungen

---

## 📝 WICHTIGE ÄNDERUNGEN:

### Entfernt:
- Lange Branch-Erklärungen
- Redundante Workflow-Regeln
- Zu viele optionale Schritte
- Komplexe Guide-Updates

### Hinzugefügt:
- Prominenter STOPP-Befehl
- Schnell-Check Format
- Direkte ls-Befehle für Migration
- Klare Zeitvorgaben

### Verbessert:
- Script mit Fehlerbehandlung
- Manuelle Fallbacks
- Strukturierte Status-Meldung
- Eindeutige Warteaufforderung