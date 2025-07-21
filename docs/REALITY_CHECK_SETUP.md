# 🔍 Reality Check Setup Guide

## Was ist Reality Check?
Ein 3-Stufen Prozess der sicherstellt, dass Planungsdokumente mit Code-Realität übereinstimmen BEVOR Implementation beginnt.

## Installation (30 Sekunden)

### 1. Git Hook aktivieren (optional aber empfohlen)
```bash
cp .git/hooks/post-checkout.example .git/hooks/post-checkout
```
→ Erinnert automatisch bei Branch-Wechsel

### 2. Alias einrichten (optional)
```bash
echo "alias rc='./scripts/reality-check.sh'" >> ~/.bashrc
source ~/.bashrc
```
→ Dann nur noch: `rc FC-008`

## Verwendung

### Bei jedem neuen Feature:
```bash
./scripts/reality-check.sh FC-XXX
```

### Der Prozess:
1. **Plan lesen** - Script zeigt wo
2. **Code lesen** - Script listet relevante Dateien
3. **Abgleich** - Interaktive Checkliste

### Bei PASSED ✅:
→ Implementation kann starten

### Bei FAILED ❌:
→ STOPP! Erst Diskrepanzen klären

## Wo wird erinnert?

1. **NEXT_STEP.md** - Ganz oben als Pflicht-Hinweis
2. **session-start.sh** - Zeigt letzte Checks
3. **Git Hook** - Bei Branch-Wechsel (wenn installiert)
4. **Übergabe** - Reality Check Status Section

## Warum ist das wichtig?

- **Ohne Reality Check:** 2+ Stunden mit falschen Annahmen verschwendet
- **Mit Reality Check:** 5 Minuten Check = richtige Implementation

## FAQ

**F: Muss ich das wirklich jedes Mal machen?**
A: Ja! Die 5 Minuten sparen dir später Stunden.

**F: Was wenn keine Dateipfade im Plan stehen?**
A: Das Script fragt dich nach den relevanten Dateien.

**F: Kann ich das überspringen wenn ich "sicher" bin?**
A: Nein. Gerade dann ist es wichtig.

## Support

Bei Problemen mit Reality Check:
1. Check die Logs: `cat .reality-check-log`
2. Script debuggen: `bash -x ./scripts/reality-check.sh FC-XXX`
3. Issue erstellen in GitHub