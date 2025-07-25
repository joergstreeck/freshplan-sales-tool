# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**CI INTEGRATION TEST ASSERTIONS FIXEN**

**Stand 26.07.2025 01:00:**
- ✅ **FC-012 Audit Trail System:** Vollständig repariert und funktionsfähig
- ✅ **Problem identifiziert:** Nicht UserNotFoundException, sondern Test-Assertion-Failures!
- ✅ **Root Cause:** Test-Assertions erwarten statische Namen, bekommen unique Timestamps
- 🔄 **Lösung dokumentiert:** Pattern-Matching statt exakte String-Vergleiche

**🚀 NÄCHSTER SCHRITT:**

**TODO-2: UserResourceIT Test-Assertions an unique Namen anpassen**

```bash
cd /Users/joergstreeck/freshplan-sales-tool/backend

# 1. Betroffene Assertions identifizieren
grep -n "equalTo.*user" src/test/java/de/freshplan/api/UserResourceIT.java

# 2. testUpdateUser_Success Test fixen
# Ersetze: .body("username", equalTo("updated.user"))
# Mit: .body("username", startsWith("updated.user."))

# 3. Pattern für andere Tests:
# .body("username", matchesPattern("updated\\.user\\.\\d+_\\d+"))
# .body("email", matchesPattern("updated\\.\\d+_\\d+@freshplan\\.de"))

# 4. Lokaler Test
./mvnw test -Dtest=UserResourceIT#testUpdateUser_Success

# 5. Vollständige Integration Tests
./mvnw test -Dtest=UserResourceIT

# 6. CI Push wenn lokal grün
git add . && git commit -m "fix: adapt UserResourceIT assertions to unique usernames"
git push origin feature/m4-renewal-stage-implementation
```

**Fehler-Details:**
```
Expected: updated.user  
Actual: updated.user.1753484020772_1
```

**UNTERBROCHEN BEI:**
- CI Integration Tests: 2 Failures identifiziert
- Lösung vollständig dokumentiert in `/docs/claude-work/daily-work/2025-07-26/2025-07-26_SOLUTION_test-assertion-fix.md`
- Nächster Schritt: 30-Minuten Test-Assertion-Fix

**STRATEGISCH WICHTIG:**
Das ist der letzte Blocker für 100% grüne CI-Pipeline! Race-Condition-Fix funktioniert, nur Assertions müssen angepasst werden.

---

## ⚠️ VOR JEDER IMPLEMENTATION - REALITY CHECK PFLICHT:
```bash
./scripts/reality-check.sh FC-002  # M4 Opportunity Pipeline Check
```

---

## 📊 OFFENE TODOS:
```
🔴 HIGH Priority: 2 TODOs (TODO-2: CI Assertions, TODO-3: UserResourceIT)
🟡 MEDIUM Priority: 1 TODO (TODO-4: RENEWAL-Spalte)
🟢 LOW Priority: 1 TODO (TODO-5: Übergabe)
```

**Status:**
- FC-012 Audit Trail System: ✅ PRODUCTION-READY
- CI Integration Tests: 🟡 2 Assertion-Failures (lösbar in 30 Min)
- RENEWAL Backend: ✅ 100% implementiert
- RENEWAL Frontend UI: 🔄 Bereit für Implementation nach CI-Fix
- Debug-System: ✅ DEPLOYED (umfassende Dokumentation)