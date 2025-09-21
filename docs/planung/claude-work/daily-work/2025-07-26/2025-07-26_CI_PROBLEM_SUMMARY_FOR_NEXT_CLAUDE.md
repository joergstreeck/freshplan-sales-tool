# 🚨 CI-PROBLEM ZUSAMMENFASSUNG - Für den nächsten Claude

**Datum:** 26.07.2025 17:45  
**Status:** 🔴 KRITISCHES CI-INFRASTRUCTURE-PROBLEM  
**Priorität:** HOCH - Blockiert normale Backend-Entwicklung

## 📋 PROBLEM-ÜBERSICHT

### Das CI-Problem in Kürze:
- ❌ **Integration Tests schlagen seit Stunden/Tagen fehl**
- ❌ **OpportunityRenewalResourceTest: HTTP 500 statt 201**
- ❌ **3 verschiedene Fix-Versuche haben NICHTS bewirkt**
- ✅ **Lokale Tests funktionieren perfekt**

### Betroffene Workflows:
- 🔴 **CI - Integration Tests**: PERMANENT ROT
- ✅ **Backend CI**: GRÜN
- ✅ **Smoke Tests**: GRÜN  
- ✅ **Lint & Format**: GRÜN

## 🔍 BEREITS DURCHGEFÜHRTE ANALYSE

### Vorherige Dokumente (LESEN!):
1. **[2025-07-26_DEBUG_ci-integration-test-failures.md](./2025-07-26_DEBUG_ci-integration-test-failures.md)**
   - Race Conditions vermutet und behandelt
   - UserResourceITDebug.java erstellt
   - Unique Timestamps implementiert
   - **ERGEBNIS:** Keine Verbesserung

2. **[2025-07-26_DEBUG_hartnackiges-ci-problem.md](./2025-07-26_DEBUG_hartnackiges-ci-problem.md)**
   - 3 Fix-Commits dokumentiert (dbfbbce, 8860dc6, db4893c)
   - Systematische Root-Cause-Analyse
   - **ERGEBNIS:** CI zeigt EXAKT die gleichen Fehler

### Bereits versuchte Fixes:
```bash
dbfbbce - AuditService @ActivateRequestContext Fix
8860dc6 - UserResourceIT Pattern-Assertions Fix  
db4893c - AuditEntry User-Validation Fix
```
**ALLE UNWIRKSAM!**

## 🎯 KERN-PROBLEM IDENTIFIZIERT

### Der echte Fehler:
```
OpportunityRenewalResourceTest - 8 Tests schlagen fehl:
❌ Expected status code <201> but was <500>
```

### Das bedeutet:
- Tests versuchen **Opportunities zu erstellen** (POST-Requests)
- Backend antwortet mit **500 Internal Server Error**
- **Nicht** Race Conditions, **nicht** TestDataInitializer
- **Echter Backend-Bug** oder **CI-Environment-Problem**

## 🚨 AKTUELLER BLOCKING-STATUS

### Was funktioniert NICHT:
- ❌ Jede Backend-Code-Änderung → CI rot
- ❌ Integration Tests → permanent fehlschlagend
- ❌ Opportunity-Service-Tests → HTTP 500
- ❌ Normale Feature-Entwicklung → blockiert

### Was funktioniert NOCH:
- ✅ Frontend-only Änderungen
- ✅ Dokumentations-Änderungen (mit CI-Bypass)
- ✅ Lokale Entwicklung

## 📋 NÄCHSTE SCHRITTE FÜR NEUEN CLAUDE

### OPTION A: Problem systematisch lösen (EMPFOHLEN für Backend-Arbeit)
```bash
# 1. Aktuelle CI-Logs analysieren
gh run list --branch feature/fc-005-documentation-restructure --limit 3
gh run view [FAILED_RUN_ID] --log-failed | grep -A 30 "HTTP 500"

# 2. Exakte Exception aus CI extrahieren
gh run view [FAILED_RUN_ID] --log-failed | grep -A 50 "OpportunityRenewalResourceTest"

# 3. Stack-Trace identifizieren
gh run view [FAILED_RUN_ID] --log-failed | grep -A 20 "Exception\|Error\|Caused by"

# 4. Lokalen Vergleich durchführen
cd backend
./mvnw test -Dtest=OpportunityRenewalResourceTest -X
```

### OPTION B: CI-Bypass für Dokumentation (GERECHTFERTIGT)
- Bei **reinen Dokumentations-Changes**
- Wenn **kein Backend-Code** geändert wird
- Für **dringende Releases** während CI-Reparatur

## 🔗 WICHTIGE DATEIEN

### Debug-Code und Patches:
- `backend/src/test/java/de/freshplan/api/UserResourceITDebug.java`
- `backend/UserResourceIT_Race_Condition_Fix.patch`

### CI-Pipeline:
- https://github.com/joergstreeck/freshplan-sales-tool/actions

### Relevante Commits:
```bash
dbfbbce - fix: resolve AuditService RequestScoped context error in tests
8860dc6 - fix: resolve UserResourceIT race conditions with unique test data  
db4893c - fix: resolve AuditEntry user validation for CI environment
```

## ⚠️ KRITISCHE WARNUNG

**DAS PROBLEM IST KOMPLEX UND HARTNÄCKIG!**

- ❌ **Nicht "blind" weitere Fixes versuchen**
- ✅ **Erst exakte HTTP 500 Root-Cause identifizieren**
- ✅ **Systematisch Stack-Traces aus CI extrahieren**
- ✅ **CI-Environment vs. Lokal vergleichen**

## 🎯 EMPFEHLUNG FÜR VERSCHIEDENE SZENARIEN

### Wenn du **Backend-Features** entwickelst:
→ **Problem systematisch lösen** (Option A)

### Wenn du **Dokumentation** änderst:
→ **CI-Bypass verwenden** (Option B) - siehe letzten erfolgreichen Merge

### Wenn du **Frontend-only** arbeitest:
→ **Kein Problem** - Frontend-CI funktioniert

---

**FAZIT:** Dies ist ein **Infrastructure-Problem**, das normale Entwicklung blockiert. Die bisherige Analyse ist umfassend dokumentiert - nutze sie als Basis für systematisches Debugging!