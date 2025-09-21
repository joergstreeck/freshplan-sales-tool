# 📋 OPTIMIERTE TRIGGER-TEXTE V3.0

**Datum:** 08.08.2025
**Autor:** Claude
**Status:** VERBESSERT MIT MIGRATION-CHECK

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

### SCHRITT 2: MIGRATION-CHECK (🚨 PFLICHT bei DB-Arbeit!)
```bash
./scripts/get-next-migration.sh
# Diese Nummer IMMER in Übergabe notieren!
# NIEMALS alte Nummern wiederverwenden!

# Fallback bei Script-Fehler:
ls -la backend/src/main/resources/db/migration/ | tail -3
# Manuell nächste V-Nummer berechnen
```

### SCHRITT 3: Stabilisiertes Handover-Script
```bash
./scripts/handover-with-sync-stable.sh

# Das Script zeigt automatisch die Migration-Nummer
# Bei Fehler wurde bereits in Schritt 2 ermittelt
```

### SCHRITT 4: Template ausfüllen
Die Übergabe wurde erstellt in:
`docs/claude-work/daily-work/YYYY-MM-DD/`

Fülle aus:
1. TODO-Status (aus TodoRead)
2. **MIGRATION-NUMMER** (aus Schritt 2) ⚠️ KRITISCH
3. Was wurde gemacht? (git diff --stat)
4. Bekannte Probleme
5. NEXT_STEP.md Update

### SCHRITT 5: Validierung
- [ ] Alle TODOs dokumentiert?
- [ ] **MIGRATION-NUMMER in Übergabe?** ⚠️ KRITISCH
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

### SCHNELL-CHECK (3 Min):

#### 1. Branch Check
```bash
git branch --show-current
./scripts/get-current-feature-branch.sh

# Falls kein Feature-Branch:
git checkout -b feature/fc-XXX-[description]
```

#### 2. MIGRATION-CHECK (🚨 PFLICHT bei DB-Arbeit!)
```bash
./scripts/get-next-migration.sh
# Diese Nummer IMMER verwenden!

# Fallback bei Script-Fehler:
ls -la backend/src/main/resources/db/migration/ | tail -3
```

#### 3. System-Start
```bash
./scripts/robust-session-start.sh
# Bei Fehler: cat docs/NEXT_STEP.md
```

#### 4. TODOs laden
Aus letzter Übergabe TODO-Status → TodoWrite

### STATUS MELDEN:
```
📋 STATUS:
- Branch: [name]
- TODOs: [X offen]
- Migration: [VXXX] ⚠️ BESTÄTIGT
- Nächster Schritt: [aus NEXT_STEP.md]
- ⛔ WARTE AUF ARBEITSSTART
```

## ⛔ STOPP! WARTE AUF "ARBEITSSTART"!
```

---

## 🔄 VEREINFACHTER WORKFLOW

### Am Session-Ende:
1. User: "Erstelle Übergabe"
2. Claude: Verwendet **TEXT 1** 
   - TODO-Status
   - **MIGRATION-CHECK** ⚠️ NEU
   - Handover-Script
   - Template ausfüllen
3. User: `/compact`

### Bei neuer Session:
1. User: Verwendet **TEXT 2**
2. Claude: 
   - Branch-Check
   - **MIGRATION-CHECK** ⚠️ 
   - System-Start
   - Status melden → WARTET
3. User: "ARBEITSSTART"
4. Claude: Beginnt mit Arbeit

---

## ✅ VERBESSERUNGEN IN V3:

### Text 1 (Session-Ende):
- ✅ **MIGRATION-CHECK als Schritt 2** - VOR dem Handover-Script
- ✅ Explizite Anweisung die Nummer zu notieren
- ✅ Fallback mit ls-Befehl
- ✅ Validierung prüft Migration-Nummer

### Text 2 (Session-Start):
- ✅ Migration-Check prominent als Schritt 2
- ✅ Bestätigung in Status-Meldung
- ✅ Fallback-Option

### Warum das besser ist:
1. **Migration wird IMMER geprüft** - nicht optional
2. **Nummer wird EXPLIZIT notiert** - nicht vergessen
3. **Fallback vorhanden** - robuster
4. **In beiden Texten konsistent** - keine Verwirrung

---

## 📝 WICHTIGSTE ÄNDERUNG:

**MIGRATION-CHECK ist jetzt PFLICHT-SCHRITT** in beiden Texten:
- Text 1: Als Schritt 2 VOR dem Handover-Script
- Text 2: Als Schritt 2 NACH Branch-Check

Dies stellt sicher, dass:
- Die korrekte Nummer IMMER ermittelt wird
- Die Nummer IMMER in die Übergabe kommt
- Keine Konflikte durch doppelte Nummern entstehen