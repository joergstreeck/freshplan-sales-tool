# V9000 Migration Analysis Report

**Datum:** 2025-10-06
**Status:** OBSOLETE - Migration gelöscht, Tests schlagen fehl
**Impact:** CI-only, kein Production-Impact

---

## 🎯 Executive Summary

Die **V9000 CI-Migration wurde bewusst gelöscht** im Commit `1478ab4af` (20. Aug 2025) als Teil der SEED-Strategie-Entfernung. Die dazugehörigen Sanity-Tests (`CiFkSanityIT`) wurden **nicht aktualisiert** und schlagen daher im CI fehl.

**Empfehlung:** Tests entfernen oder an neue Architektur anpassen.

---

## 📋 Was war V9000?

### Zweck
CI-spezifische Migration die alle Foreign Keys auf `customers` table mit **CASCADE deletes** konfigurierte, um automatisches Test-Cleanup zu ermöglichen.

### Datei
```
backend/src/test/resources/db/ci-migrations/V9000__fk_cascade_for_tests.sql
```

### Funktionsweise
```sql
-- Self-FK (customers->customers): ON DELETE SET NULL
-- Andere FKs (z.B. contacts->customers): ON DELETE CASCADE
```

**Ziel:** Wenn ein Test-Customer gelöscht wird, werden automatisch alle abhängigen Records (Contacts, Opportunities, etc.) mit gelöscht.

---

## 🔍 Warum wurde V9000 gelöscht?

### Commit: 1478ab4af (20. Aug 2025)
**Titel:** "feat: TestDataBuilder Migration & CI-Optimierung"

**Kontext:**
```
BREAKING CHANGE: SEED-Strategie vollständig entfernt,
TestDataBuilder ist jetzt Pflicht

Hauptziele:
- SEED-frei: Alle SEED-Abhängigkeiten eliminiert
- Builder-only: TestDataBuilder-Pattern durchgängig
- CI optimiert: Von 10 auf 3 Minuten (-70%)
```

### Begründung
1. **SEED-Customers wurden entfernt**
   - Früher: CI nutzte fest-SEED-Kunden (Customer #90001-90005)
   - V9000 ermöglichte: `DELETE FROM customers WHERE customer_number = '90001'` → CASCADE cleanup

2. **Neue Test-Strategie: TestDataFactory**
   - Jeder Test erstellt eigene Testdaten via Factory
   - Tests nutzen `@Transactional` für automatisches Rollback
   - Keine manuellen DELETE-Statements mehr nötig
   - Keine CASCADE-FKs mehr erforderlich

3. **CI-Optimierung**
   - CI-Laufzeit von 10 auf 3 Minuten reduziert (-70%)
   - Ein Maven-Run statt mehrerer Steps
   - Schema-Reset garantiert sauberen Start

---

## ❌ Aktuelle Failing Tests

### 1. `CiFkSanityIT.v9000MigrationWasApplied()`
```
Expected at least one CASCADE FK to customers, found 0
==> expected: <true> but was: <false>
```

**Problem:** Test prüft ob V9000 Migration existiert - tut sie aber nicht mehr.

### 2. `CiFkSanityIT.fkActionsForCustomersAreAsExpected()`
```
V9000 migration not found in flyway_schema_history!
CI-specific migrations are not being loaded.
==> expected: <true> but was: <false>
```

**Problem:** Test erwartet CASCADE FKs die von V9000 gesetzt würden.

---

## 🔧 Lösungsoptionen

### Option 1: Tests löschen (Empfohlen) ✅
**Begründung:**
- V9000 ist obsolet → Tests testen nichts Relevantes mehr
- Neue Architektur nutzt @Transactional statt CASCADE deletes
- Kein Production-Code betroffen

**Vorgehen:**
```bash
git rm backend/src/test/java/de/freshplan/test/CiFkSanityIT.java
```

**Impact:**
- 2 failing Tests verschwinden
- CI wird grün
- Keine Funktionalität verloren

---

### Option 2: Tests an neue Architektur anpassen
**Begründung:**
- Falls FK-Sanity weiterhin geprüft werden soll
- Könnte helfen Datenbank-Inkonsistenzen zu finden

**Vorgehen:**
1. Rename: `CiFkSanityIT` → `DatabaseIntegrityIT`
2. Neue Tests:
   - Prüfe dass customers.parent_customer_id richtig referenziert
   - Prüfe dass alle FK-Constraints existieren
   - Keine Prüfung auf CASCADE/SET NULL (nicht mehr relevant)

**Impact:**
- Tests bleiben, aber mit neuem Zweck
- Mehr Arbeit, unsicherer Nutzen

---

### Option 3: V9000 wiederherstellen (NICHT empfohlen) ❌
**Begründung:**
- Widerspricht aktueller Architektur
- SEED-free Strategie wird untergraben
- Tests würden grün, aber ohne echten Mehrwert

**Vorgehen:**
```bash
git show ba1f6c857:backend/src/test/resources/db/ci-migrations/V9000__fk_cascade_for_tests.sql \
  > backend/src/test/resources/db/ci-migrations/V9000__fk_cascade_for_tests.sql
```

**Impact:**
- Tests werden grün
- ABER: Migration ist veraltet und passt nicht zu aktueller Architektur

---

## 📊 Impact-Analyse

### Production
- ✅ **Kein Impact** - V9000 war CI-only
- ✅ Production-Datenbank wird nicht von ci-migrations geladen

### Tests
- ⚠️ **2 von 296 Tests failing** (0.7% failure rate)
- ✅ **Alle anderen Tests grün** (294/296)
- ⚠️ Tests sind tagged `@Tag("migrate")` → laufen nur in Migration-Suite

### CI/CD
- ⚠️ **CI schlägt aktuell fehl** wegen 2 failing tests
- ✅ **Kann sofort gefixt werden** durch Test-Deletion
- ✅ Keine Auswirkung auf Production-Deployments

### Aktuelle PR #132
- ✅ **Nicht durch PR verursacht** - Pre-existing issue
- ✅ **PR ist merge-ready** - 294/296 Tests passing
- ⚠️ Sollte trotzdem gefixt werden für sauberes CI

---

## ✅ Empfehlung

**OPTION 1: Tests löschen**

**Begründung:**
1. V9000 Migration ist obsolet (SEED-Strategie entfernt)
2. Neue Architektur nutzt `@Transactional` + TestDataFactory
3. CASCADE FKs sind nicht mehr Teil der Test-Strategie
4. Tests testen nichts Relevantes mehr

**Vorgehen:**
```bash
# 1. Test-Datei löschen
git rm backend/src/test/java/de/freshplan/test/CiFkSanityIT.java

# 2. Commit
git commit -m "chore(tests): Remove obsolete CiFkSanityIT tests

CiFkSanityIT tested V9000 CI migration which was removed in commit 1478ab4af
as part of SEED-strategy removal.

Context:
- V9000 configured CASCADE deletes for SEED customer cleanup
- New architecture uses @Transactional + TestDataFactory
- CASCADE FKs no longer needed for test cleanup
- Tests have been failing since V9000 removal (Aug 20, 2025)

Impact:
- Removes 2 failing tests (v9000MigrationWasApplied, fkActionsForCustomersAreAsExpected)
- CI will be green again
- No functional impact (tests were CI-sanity checks only)

Related: #89 (SEED-strategy removal)
"

# 3. Push
git push
```

---

## 📚 Referenzen

- **V9000 Migration (deleted):** Commit `ba1f6c857`
- **Löschung:** Commit `1478ab4af` (20. Aug 2025)
- **Failing Tests:** `CiFkSanityIT.java`
- **Related Issue:** #89 (SEED-strategy removal)
- **TestDataFactory Migration:** PR #132 (aktuell)

---

## 🎓 Lessons Learned

1. **Tests sollten mit Code gelöscht werden**
   - V9000 wurde gelöscht, aber Tests blieben
   - Resultat: Failing tests seit 6 Wochen

2. **CI-Migrations brauchen klare Ownership**
   - `/db/ci-migrations/` Verzeichnis ist leer
   - Aber Tests erwarten noch Migrations
   - Dokumentation fehlt

3. **Test-Tags sind wichtig**
   - `@Tag("migrate")` verhindert dass Tests in allen Suites laufen
   - Aber CI läuft alle Tests → Failure

---

**Zusammenfassung:**
V9000 ist obsolet seit SEED-Entfernung. Tests sollten gelöscht werden. Kein Production-Impact, nur CI-Cleanup nötig.
