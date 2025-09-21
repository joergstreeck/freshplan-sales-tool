# Debug Plan: MUI Grid v2 Migration Issues

## 🔴 PROBLEM
Console zeigt Grid v2 Warnungen, obwohl wir Grid v1 Syntax verwenden:
- "MUI Grid: The `item` prop has been removed"
- "MUI Grid: The `xs` prop has been removed"

## 📋 DEBUG PLAN

### 1. MUI Version Check
- [ ] Check package.json für MUI Version
- [ ] Check ob wir tatsächlich Grid v2 haben

### 2. Debug Cookbook Check
- [ ] Prüfe DEBUG_COOKBOOK.md für Grid-Issues
- [ ] Prüfe TYPESCRIPT_IMPORT_TYPE_GUIDE.md

### 3. Frontend Analyse
- [ ] Suche nach funktionierenden Grid-Verwendungen
- [ ] Vergleiche mit Audit-Components

### 4. Lösungsansätze
- [ ] Option A: Grid v2 Syntax verwenden (size prop)
- [ ] Option B: Downgrade zu Grid v1
- [ ] Option C: Mixed approach je nach Component

## 📝 DOKUMENTATION FÜR NÄCHSTEN CLAUDE

### Was wir versucht haben:
1. Grid2 Component importieren → FEHLER: Grid2 existiert nicht in MUI v5.16.20
2. Grid mit item prop verwenden → FEHLER: Grid v2 akzeptiert kein item prop
3. Grid mit size prop → Noch nicht getestet

### Aktuelle Situation:
- MUI Version: 5.16.20 (aus package.json)
- Grid Component ist Grid v2 (erkennbar an den Fehlermeldungen)
- Grid v2 braucht `size` prop statt `item xs={12}`

### Console Errors:
```
MUI Grid: The `item` prop has been removed
MUI Grid: The `xs` prop has been removed
MUI Grid: The `sm` prop has been removed
MUI Grid: The `md` prop has been removed
```

## 🎯 NÄCHSTE SCHRITTE
1. MUI Version prüfen
2. Funktionierende Grid-Beispiele finden
3. Konsistente Lösung implementieren