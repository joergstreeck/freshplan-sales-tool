# 🚨 CI Integration Test Failures - Debug Analysis

**Datum:** 2025-07-26 00:45
**Problem:** CI Integration Tests schlagen kontinuierlich fehl mit UserNotFoundException
**Status:** DEBUGGING IN PROGRESS

## 🔍 Problemanalyse

### Symptome:
- CI Integration Tests: **FAILURE** (seit mehreren Stunden)
- Backend CI: **SUCCESS** 
- Smoke Tests: **SUCCESS**
- Lint & Format: **SUCCESS**

### Fehler-Pattern:
```
UserNotFoundException: User not found with ID: [verschiedene UUIDs]
- dfb9d0db-bf02-43c3-a0ee-9da55ee26f5a
- fe08da02-4df6-4b18-93ed-129bf6593a8e  
- 8f1cac6b-037f-469c-a071-d06b41c4d7e8
- 7bab4bd0-7c42-4223-abbc-9c3eb8a1ba37
```

## 🧪 Durchgeführte Debug-Maßnahmen

### 1. UserResourceITDebug.java erstellt
- **Zweck:** Extensive Debug-Logs für Test-Isolation
- **Problem:** Läuft nicht in CI (wurde nicht ausgeführt)
- **Datei:** `backend/src/test/java/de/freshplan/api/UserResourceITDebug.java`

### 2. Race-Condition-Fixes versucht
- **Maßnahme:** Unique Timestamps + Thread-IDs für Benutzernamen
- **Dateien geändert:** `UserResourceIT.java` Methoden:
  - `createValidCreateRequest()`
  - `createValidUpdateRequest()` 
  - `createAndPersistUser()`
- **Ergebnis:** IMMER NOCH ROT

### 3. Patch-Datei dokumentiert
- **Datei:** `backend/UserResourceIT_Race_Condition_Fix.patch`
- **Inhalt:** Systematische Lösung für Test-Isolation

## 🎯 Erkenntnisse für den nächsten Claude

### Das Problem ist NICHT gelöst durch:
- ❌ Unique Timestamps in Benutzernamen
- ❌ Thread-ID Suffix  
- ❌ setUp() Cleanup-Logik

### Das Problem liegt wahrscheinlich bei:
- 🔍 **Test-Reihenfolge:** Tests interferen trotz unique Namen
- 🔍 **Transaktions-Isolation:** setUp/tearDown Race Conditions
- 🔍 **Database State:** Tests teilen sich DB-Zustand
- 🔍 **@TestMethodOrder:** Möglicherweise nicht korrekt implementiert

## 📋 NÄCHSTE SCHRITTE FÜR NACHFOLGER-CLAUDE

### 1. SOFORT: CI-Logs analysieren
```bash
gh run list --branch feature/m4-renewal-stage-implementation --limit 2
gh run view [LATEST_FAILED_RUN_ID] --log-failed | grep -A 20 -B 5 "UserNotFoundException"
```

### 2. DEBUG-STRATEGIE ERWEITERN
- UserResourceITDebug mit @Order Annotations erweitern
- Database-State zwischen Tests protokollieren  
- Transaction-Boundaries debuggen

### 3. ALTERNATIVE LÖSUNGSANSÄTZE
- **@TestInstance(Lifecycle.PER_CLASS)** - Klassen-Level-Isolation
- **@DirtiesContext** für Spring Boot Tests (falls verfügbar)
- **Separate Test-Database pro Thread** 
- **Sequential Test Execution:** `@Execution(ExecutionMode.SAME_THREAD)`

## 🗂️ DOKUMENTATION LINKS
- Debug Code: `backend/src/test/java/de/freshplan/api/UserResourceITDebug.java`
- Race Fix Patch: `backend/UserResourceIT_Race_Condition_Fix.patch`
- CI Pipeline: https://github.com/joergstreeck/freshplan-sales-tool/actions

## ⚠️ WICHTIGE WARNUNG
**Das Problem ist KOMPLEX und braucht systematisches Debugging!**
Nicht wieder "blind" Fixes versuchen - erst Debug-Logs analysieren, dann gezielt eingreifen!

---
**Für den nächsten Claude:** 
Starte mit CI-Log-Analyse und erweitere die Debug-Strategie basierend auf den konkreten Fehlern!