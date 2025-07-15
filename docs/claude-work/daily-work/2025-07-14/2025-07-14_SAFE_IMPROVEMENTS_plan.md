# Plan für sichere Verbesserungen

**Datum:** 14.07.2025 13:55
**Status:** 🟢 SICHER

## Identifizierte sichere Verbesserungen:

### 1. Maven Wrapper Fix (mvn-wrapper-fix)
**Problem:** Maven Wrapper funktioniert nicht (-DmultiModuleProjectDirectory)
**Lösung:** Wrapper-Dateien neu generieren
**Risiko:** ⚪ Null - nur Tool-Fix

### 2. Log-Rotation einrichten
**Problem:** backend.log wächst unbegrenzt (aktuell 1.9MB)
**Lösung:** Logback-Konfiguration für Rolling File Appender
**Risiko:** ⚪ Null - nur Logging

### 3. Ungetrackte Dokumentation aufräumen
**Problem:** 36+ ungetrackte Dateien
**Lösung:** Review und entweder committen oder .gitignore
**Risiko:** ⚪ Null - nur Dokumentation

### 4. Development Scripts verbessern
**Problem:** Scripts könnten robuster sein
**Lösung:** Fehlerbehandlung hinzufügen
**Risiko:** 🟡 Gering - nur Development-Tools

### 5. Git-Hygiene
**Problem:** Alter Stash vom 14.07.
**Lösung:** Stash analysieren und aufräumen
**Risiko:** ⚪ Null - nur Git

### 6. Test-Coverage Report
**Problem:** Wissen nicht genau wo wir stehen
**Lösung:** Coverage-Report generieren
**Risiko:** ⚪ Null - nur Analyse

## Reihenfolge:
1. Maven Wrapper (schnell)
2. Log-Rotation (nützlich)
3. Coverage-Report (informativ)
4. Git/Doku aufräumen (Hygiene)