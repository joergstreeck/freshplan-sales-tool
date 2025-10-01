# CustomerSearch Test-Konsolidierung - Implementierungs-Guide

**Status:** Bereit zur Implementierung
**Geschätzte Zeit:** 2-3 Stunden
**Impact:** -3 Files, -299 lines, -45s CI-Zeit
**Priorität:** Mittel (Quick Win für CI-Performance)

---

## 🎯 ÜBERSICHT

**Aktuell:** 5 CustomerSearch Test-Files mit Redundanzen
**Ziel:** 2 konsolidierte Test-Files mit @Nested Classes

### Aktuelle Files:

| File | Tests | Zeilen | Status |
|------|-------|--------|--------|
| CustomerSearchBasicTest | 10 | 188 | → Merge in Core |
| CustomerSearchFilterTest | 12 | 317 | → Merge in Core |
| CustomerSearchSortTest | 12 | 324 | → Merge in Core |
| CustomerSearchSmartSortTest | 16 | 388 | → Merge in Advanced |
| CustomerSearchPaginationTest | 22 | 282 | → Merge in Advanced |
| **TOTAL** | **72** | **1,499** | |

### Neue Struktur:

| File | Tests | Zeilen | Inhalt |
|------|-------|--------|--------|
| **CustomerSearchServiceTest** | 34 | ~600 | Basic + Filter + Sort |
| **CustomerSearchAdvancedTest** | 38 | ~600 | SmartSort + Pagination |
| **TOTAL** | **72** | **~1,200** | **-299 lines (-20%)** |

---

## ✅ VORTEILE

1. **CI-Zeit:** -45s pro Build (5× Quarkus Boot → 2×)
2. **Code-Reduktion:** -299 Zeilen (-20%)
3. **Wartbarkeit:** Bessere Organisation mit @Nested Classes
4. **Redundanz:** Setup-Methoden nur 1× statt 5×
5. **Navigation:** Alle Basic/Filter/Sort Tests in einem File

---

## 🚀 IMPLEMENTIERUNG

### Schritt 1: CustomerSearchServiceTest erstellen

**Location:** `src/test/java/de/freshplan/domain/customer/service/CustomerSearchServiceTest.java`

**Struktur:**
```java
@QuarkusTest
@Tag("migrate")
@DisplayName("CustomerSearchService - Core Functionality")
class CustomerSearchServiceTest {

  @Inject CustomerSearchService searchService;
  @Inject CustomerRepository customerRepository;
  @Inject CustomerBuilder customerBuilder;

  @Nested
  @DisplayName("Basic Search Operations")
  class BasicSearchTests {
    // 10 tests from CustomerSearchBasicTest
  }

  @Nested
  @DisplayName("Filter Operations")
  class FilterTests {
    // 12 tests from CustomerSearchFilterTest
  }

  @Nested
  @DisplayName("Sorting Operations")
  class SortTests {
    // 12 tests from CustomerSearchSortTest
  }

  // Shared helper methods
}
```

**Copy-Paste Workflow:**
1. Alle @Test Methoden aus BasicTest in BasicSearchTests @Nested Class kopieren
2. Alle @Test Methoden aus FilterTest in FilterTests @Nested Class kopieren
3. Alle @Test Methoden aus SortTest in SortTests @Nested Class kopieren
4. Helper methods (private void create...) am Ende des Files konsolidieren

### Schritt 2: CustomerSearchAdvancedTest erstellen

**Location:** `src/test/java/de/freshplan/domain/customer/service/CustomerSearchAdvancedTest.java`

**Struktur:**
```java
@QuarkusTest
@Tag("migrate")
@DisplayName("CustomerSearchService - Advanced Features")
class CustomerSearchAdvancedTest {

  @Inject CustomerSearchService searchService;
  @Inject CustomerRepository customerRepository;
  @Inject CustomerBuilder customerBuilder;
  @Inject SmartSortService smartSortService;

  @Nested
  @DisplayName("Smart Sorting Strategies")
  class SmartSortTests {
    // 16 tests from CustomerSearchSmartSortTest
  }

  @Nested
  @DisplayName("Pagination Operations")
  class PaginationTests {
    // 22 tests from CustomerSearchPaginationTest
  }

  // Shared helper methods
}
```

### Schritt 3: Tests validieren

```bash
# Teste neue Files
./mvnw test -Dtest=CustomerSearchServiceTest
./mvnw test -Dtest=CustomerSearchAdvancedTest

# Prüfe Anzahl Tests
grep -c "@Test" src/test/java/de/freshplan/domain/customer/service/CustomerSearchServiceTest.java
# Sollte: 34

grep -c "@Test" src/test/java/de/freshplan/domain/customer/service/CustomerSearchAdvancedTest.java
# Sollte: 38
```

### Schritt 4: Alte Files löschen

```bash
git rm src/test/java/de/freshplan/domain/customer/service/CustomerSearchBasicTest.java
git rm src/test/java/de/freshplan/domain/customer/service/CustomerSearchFilterTest.java
git rm src/test/java/de/freshplan/domain/customer/service/CustomerSearchSortTest.java
git rm src/test/java/de/freshplan/domain/customer/service/CustomerSearchSmartSortTest.java
git rm src/test/java/de/freshplan/domain/customer/service/CustomerSearchPaginationTest.java
```

### Schritt 5: Commit

```bash
git add src/test/java/de/freshplan/domain/customer/service/CustomerSearchServiceTest.java
git add src/test/java/de/freshplan/domain/customer/service/CustomerSearchAdvancedTest.java

git commit -m "refactor: consolidate CustomerSearch tests (5 → 2 files)

- Merge Basic + Filter + Sort → CustomerSearchServiceTest (34 tests)
- Merge SmartSort + Pagination → CustomerSearchAdvancedTest (38 tests)
- Remove duplicate setup methods
- Use @Nested classes for better organization

Impact:
- Files: 5 → 2 (-60%)
- Lines: 1,499 → 1,200 (-20%)
- CI time: -45s per build
- Tests: 72 (unchanged, all passing)

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## ⚠️ WICHTIGE HINWEISE

### @Nested Classes:
- **Erforderlich** für Gruppierung der Tests
- Jeder @Nested Block braucht eigenen @DisplayName
- @Test Methoden bleiben mit @TestTransaction

### Helper Methods:
- Duplikate konsolidieren (gleiche Namen → eine Version)
- Als private methods am Ende des Files
- Parameter übergeben statt Class-Variablen wenn möglich

### Häufige Fehler:
- ❌ @TestTransaction vergessen auf @Test Methoden
- ❌ Helper methods nicht mitkopiert
- ❌ @Nested Class @DisplayName vergessen
- ❌ Imports nicht angepasst

---

## 📊 ERFOLGS-KRITERIEN

Nach Konsolidierung:
- ✅ Alle 72 Tests laufen durch
- ✅ Keine Compile-Fehler
- ✅ Setup-Methoden konsolidiert
- ✅ 2 Files statt 5
- ✅ ~1,200 Zeilen statt 1,499
- ✅ CI-Zeit messbar reduziert

---

## 🔍 REFERENZEN

**Detaillierter Guide:** `/tmp/customer_search_consolidation_guide.md` (auf Build-Server)
**Analyse:** `/tmp/customer_search_consolidation_plan.md`

**JUnit @Nested Docs:**
https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested

---

**Erstellt:** 2025-10-01
**Status:** Bereit zur Implementierung
**Assignee:** Team entscheidet
**Geschätzte Zeit:** 2-3 Stunden (Copy-Paste Arbeit)
