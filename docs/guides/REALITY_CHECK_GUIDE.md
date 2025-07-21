# 🔍 Reality Check Guide

## Was ist der Reality Check?

Ein verpflichtender Schritt VOR jeder Feature-Implementierung, der sicherstellt, dass Planungsdokumente mit der Code-Realität übereinstimmen.

## Warum ist das wichtig?

- ❌ **Ohne Reality Check:** 2+ Stunden Arbeit mit veralteten Plänen verschwendet
- ✅ **Mit Reality Check:** Diskrepanzen in 5 Minuten erkannt und geklärt

## Der Prozess

### 1. Vor jedem neuen Feature:
```bash
./scripts/reality-check.sh FC-XXX
```

### 2. Bei PASSED ✅:
- Plan stimmt mit Code überein
- Implementation kann starten

### 3. Bei FAILED ❌:
- Diskrepanzen gefunden
- STOPP! Erst klären:
  - Sind Dateien umbenannt/verschoben?
  - Ist der Plan veraltet?
  - Fehlen Dateien die erst erstellt werden müssen?

## Integration in den Workflow

### In CLAUDE.md:
- Regel #18: Reality Check ist PFLICHT

### In der Übergabe:
```markdown
## 🔍 REALITY CHECK STATUS
- [✅] FC-008: Check passed (21.07. 16:45)
- [❌] FC-011: Check failed - AuthService.java missing
- [ ] M4: Check ausstehend
```

### In NEXT_STEP.md:
```bash
# Immer zuerst:
./scripts/reality-check.sh FC-XXX
```

## Automatisches Logging

Das Script loggt automatisch in `.reality-check-log`:
- Erfolgreiche Checks mit Timestamp
- Fehlgeschlagene Checks mit Grund

## Best Practices

1. **Füge konkrete Dateipfade zu Plänen hinzu**
   - Schlecht: "Wir nutzen einen Auth Service"
   - Gut: "AuthService in `backend/src/main/java/de/freshplan/security/AuthService.java`"

2. **Update Pläne bei Änderungen**
   - Wenn Dateien verschoben werden
   - Wenn Architektur sich ändert
   - Wenn neue Dependencies dazukommen

3. **Reality Check auch bei kleinen Features**
   - Auch "offensichtliche" Dinge prüfen
   - Verhindert Annahmen

## Beispiel-Output

```bash
🔍 Reality Check für FC-008
====================================
📋 Plan gefunden: docs/features/ACTIVE/01_security_foundation/FC-008_CLAUDE_TECH.md

🔎 Extrahiere Code-Referenzen aus Plan...
📁 Gefundene Datei-Referenzen:
frontend/src/contexts/AuthContext.tsx
backend/src/main/java/de/freshplan/security/SecurityConfig.java

✓ Prüfe Existenz...
  ✅ frontend/src/contexts/AuthContext.tsx
  ❌ backend/src/main/java/de/freshplan/security/SecurityConfig.java - NICHT GEFUNDEN

====================================
Zusammenfassung: 1 OK, 1 FEHLEN

❌ REALITY CHECK FAILED
   → Bitte erst Plan aktualisieren oder fehlende Dateien klären!
```

## Troubleshooting

**Problem:** "Keine Code-Dateien im Plan gefunden"
**Lösung:** Füge konkrete Dateipfade zum Plan hinzu

**Problem:** Datei existiert aber wird als fehlend gezeigt
**Lösung:** Prüfe ob der Pfad im Plan korrekt ist

**Problem:** Reality Check passed aber Code passt nicht zum Plan
**Lösung:** Der Check prüft nur Existenz - manuelle Prüfung des Inhalts nötig