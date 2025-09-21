# 📋 FIX: Migration Script Robustheit

**Datum:** 2025-08-08  
**Problem:** Script versagte bei Aufruf aus backend-Verzeichnis  
**Lösung:** Intelligente Pfaderkennung implementiert  

## 🐛 Problem

Das Script `get-next-migration.sh` hatte mehrere Probleme:

1. **Hardcodierter Pfad:** Erwartete immer Ausführung vom Projekt-Root
2. **Falsche Sortierung:** Zeigte erste 5 statt letzte 5 Migrationen
3. **Fehler aus backend/:** Script fand Migration-Verzeichnis nicht

## ✅ Lösung

### Verbesserungen implementiert:

1. **Intelligente Pfaderkennung:**
   - Findet automatisch Projekt-Root via `.git` Verzeichnis
   - Funktioniert aus jedem Unterverzeichnis
   - Fallback-Pfade für verschiedene Szenarien

2. **Korrekte Sortierung:**
   - Verwendet `sort -V` für natürliche Versionssortierung
   - Zeigt wirklich die letzten 5 Migrationen (V208-V212)
   - Zeigt Dateiname der höchsten Migration

3. **Robustere Nummer-Erkennung:**
   - Bessere Regex mit `grep -E '^[0-9]+$'`
   - Entfernt alle Sonderzeichen korrekt
   - Verhindert Fehler bei ungewöhnlichen Dateinamen

4. **Verbesserte Ausgabe:**
   - Farbcodierung für verschiedene Informationen
   - Zeigt Pfad zum Migration-Verzeichnis
   - Begrenzt Lücken-Output auf letzte 20 Nummern

5. **Sicherheits-Features:**
   - Prüft ob nächste Nummer bereits existiert
   - Warnung bei Konflikten
   - Hilfreiche Debug-Informationen

## 📝 Dokumentation aktualisiert

- ✅ `/docs/TRIGGER_TEXTS.md` - Verwendet jetzt das neue Script
- ✅ `/docs/STANDARDUBERGABE_NEU.md` - Migration-Check mit neuem Script
- ✅ Migration-Nummer aktualisiert: V213 ist die nächste freie

## 🧪 Test

```bash
# Aus Projekt-Root
./scripts/get-next-migration.sh
# ✅ Funktioniert

# Aus backend-Verzeichnis
cd backend && ../scripts/get-next-migration.sh
# ✅ Funktioniert

# Output zeigt korrekt:
# - Letzte 5: V208-V212
# - Nächste freie: V213
```

## 🎯 Ergebnis

Das Script ist jetzt **produktionsreif** und robust gegen verschiedene Ausführungsorte.