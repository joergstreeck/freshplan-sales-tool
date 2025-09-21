# 🔧 CI SYSTEMATIC DEBUGGING PLAN - 26.07.2025 18:55

**Ziel:** OpportunityRenewalResourceTest HTTP 500 Error systematisch lösen  
**Methodik:** 4-Stufen-Ansatz mit diagnostischem Wert  
**Zeitrahmen:** 30-45 Min (timeboxed)

## 🎯 PROBLEM-DEFINITION

### Kern-Issue:
- **OpportunityRenewalResourceTest:** 8 Tests schlagen fehl mit HTTP 500 statt 201
- **CI-spezifisch:** Lokale Tests funktionieren perfekt  
- **3 vorherige Fixes unwirksam:** dbfbbce, 8860dc6, db4893c
- **Blockiert:** Alle Backend-Entwicklung

### Hypothese:
**Race Conditions durch parallele Test-Execution in CI** + **Shared Database State**

## 📋 4-STUFEN-PLAN

### ✅ STUFE 1: Sequential Test Execution (5 Min)
**Ziel:** Race Conditions eliminieren  
**Methode:** JUnit 5 @Execution(ExecutionMode.SAME_THREAD)

```java
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
class OpportunityRenewalResourceTest {
    // Existing test code
}
```

**Erwartung:** Wenn Race Conditions die Ursache sind → CI wird grün

### ⏳ STUFE 2: GitHub Actions Cache Reset (2 Min)
**Ziel:** Environment-Issues eliminieren  
**Methode:** Kompletter Cache-Reset

```bash
# Cache komplett löschen
gh api repos/joergstreeck/freshplan-sales-tool/actions/caches --method DELETE
```

**Erwartung:** Eliminiert "vergiftete" Container/Dependencies

### ⏳ STUFE 3: HTTP 500 Stack-Trace Extraktion (10 Min)
**Ziel:** Root Cause identifizieren  
**Methode:** Systematische Log-Analyse

```bash
# Aktuelle fehlgeschlagene Runs
gh run list --branch feature/fc-005-documentation-restructure --limit 3

# Stack-Trace extrahieren
gh run view [FAILED_RUN_ID] --log-failed | grep -A 50 "HTTP 500"
gh run view [FAILED_RUN_ID] --log-failed | grep -A 30 "OpportunityRenewalResourceTest"
gh run view [FAILED_RUN_ID] --log-failed | grep -A 20 "Exception\|Error\|Caused by"
```

**Erwartung:** Exakte Backend-Exception identifizieren

### ⏳ STUFE 4: Transaction Isolation (15 Min)
**Ziel:** Database-State-Isolation  
**Methode:** TestContainers oder separate Test-Schemas

```java
// Option A: TestContainers per Test Class
@Testcontainers
class OpportunityRenewalResourceTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
}

// Option B: @Transactional + @Rollback
@Transactional
@Rollback
class OpportunityRenewalResourceTest {
    // Tests automatically rolled back
}
```

## 📊 ERFOLGSKRITERIEN

### ✅ ERFOLG:
- CI Integration Tests werden grün
- OpportunityRenewalResourceTest: 8/8 Tests bestehen  
- HTTP 201 statt HTTP 500
- Normale Backend-Entwicklung entsperrt

### 📋 TEIL-ERFOLG:
- Root Cause identifiziert (auch wenn nicht sofort lösbar)
- Systematisches Verständnis des Problems
- Dokumentierte Lösung für Team

### ❌ FALLBACK:
- Nach 45 Min → FC-005 Frontend Implementation
- CI-Problem dokumentiert für separaten Sprint
- Entwicklung mit CI-Bypass für Dokumentation

## 🔍 DIAGNOSE-MATRIX

| Stufe | Ergebnis GRÜN | Ergebnis ROT | Interpretation |
|-------|---------------|--------------|----------------|
| 1: Sequential | ✅ Race Conditions waren Ursache | ❌ Weitermachen mit Stufe 2 | Performance vs. Stabilität |
| 2: Cache Reset | ✅ Environment-Problem gelöst | ❌ Weitermachen mit Stufe 3 | Infra vs. Code-Problem |
| 3: Stack-Trace | ✅ Backend-Bug identifiziert | ❌ Weitermachen mit Stufe 4 | Business Logic Problem |
| 4: DB Isolation | ✅ Database-State-Problem | ❌ Tiefere Architektur-Issues | Test-Design-Problem |

## 📝 STUFEN-ERGEBNISSE

### ✅ STUFE 1 ABGESCHLOSSEN: Sequential Test Execution
**Ergebnis:** ❌ ROT - Tests immer noch fehlschlagend  
**Erkenntnis:** Race Conditions waren NICHT die Ursache  
**Commit:** 6ef9c5e "fix(ci): add sequential test execution to OpportunityRenewalResourceTest"  

### ✅ STUFE 3 ABGESCHLOSSEN: Stack-Trace Extraktion  
**Ergebnis:** ✅ ROOT CAUSE IDENTIFIZIERT!  
**Stack-Trace:** `java.lang.RuntimeException: Test user 'testuser' not found`  
**Lokalisierung:** OpportunityService.java:437 in getCurrentUser()  
**Problem:** TestDataInitializer läuft nicht in CI oder User wird nicht gefunden  

### 🎯 ECHTES PROBLEM IDENTIFIZIERT:
- @TestSecurity(user = "testuser") funktioniert nicht in CI
- User 'testuser' existiert nicht zum Testzeitpunkt  
- Lokal funktioniert es → CI-Environment-Unterschied

## 📝 DOKUMENTATIONS-PFLICHT

### Nach jeder Stufe:
1. **Ergebnis dokumentieren** (GRÜN/ROT/ERKENNTNISSE)
2. **TODO-Status aktualisieren**  
3. **Nächste Schritte definieren**
4. **Bei Erfolg:** Vollständigen Fix dokumentieren

### Change Logs:
- Jede Code-Änderung mit Commit-Message
- Vor/Nach-Vergleich bei CI-Runs
- Lessons Learned für Team

## 🚀 START-KOMMANDO

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Dokumentation erstellt ✅
# 2. TODOs aktualisiert ✅
# 3. STUFE 1 beginnen:
cd backend/src/test/java/de/freshplan/api/
# OpportunityRenewalResourceTest.java editieren
```

## 🎯 ERFOLGSMESSUNG

**Vor Debugging:**
- CI Integration Tests: 🔴 FAILED
- OpportunityRenewalResourceTest: 0/8 Tests bestehen

**Nach Debugging:**
- CI Integration Tests: ✅ PASSED  
- OpportunityRenewalResourceTest: 8/8 Tests bestehen
- Backend-Entwicklung: ✅ ENTSPERRT

---

**Status:** BEREIT FÜR STUFE 1 - Sequential Test Execution  
**Dokumentation:** VOLLSTÄNDIG  
**TODOs:** AKTUALISIERT