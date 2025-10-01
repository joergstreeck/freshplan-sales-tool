# Mock-Migration Pattern - @QuarkusTest → Plain JUnit/Mock

**Ziel:** @QuarkusTest-Count von 79 → 50 reduzieren für schnellere CI-Zeiten

**Etabliert:** 2025-10-01
**Erste Migration:** OpportunityRenewalServiceTest (15s → 0.115s = **130x schneller**)

---

## 🎯 Wann migrieren?

### ✅ PERFEKTE Kandidaten (Plain JUnit)
- **Enum/Logic Tests** - Keine Dependencies
- **DTO/Mapper Tests** - Reine Transformationen
- **Validation Tests** - Nur Input-Checks
- **Utility/Helper Tests** - Stateless Logic

**Beispiel:** OpportunityRenewalServiceTest
- Testet nur `OpportunityStage` Enum-Properties
- Kein `persist()`, `flush()`, Repository-Calls
- **0 persist-Calls** = Perfekter Kandidat

### ✅ GUTE Kandidaten (@QuarkusTest + @InjectMock)
- **Service-Logic Tests** - Mit gemockten Dependencies
- **Calculator/Business-Logic** - Ohne DB
- **Workflow Tests** - Gemockte Repository-Calls

**Kriterien:**
- Wenige `persist()` Calls (1-5)
- Dependencies können gemockt werden
- Keine komplexe Transaction-Logik

### ❌ KEINE Migration
- **Repository Tests** - Echte DB-Queries nötig
- **Integration Tests** - Mehrere Services zusammen
- **Transaction Tests** - `@TestTransaction` erforderlich
- **Security Tests** - `@TestSecurity` erforderlich

---

## 📋 Migration-Workflow

### Schritt 1: Kandidaten identifizieren
```bash
# Finde Tests ohne persist() Calls
find src/test/java -name "*Test.java" | while read f; do
  if grep -q "@QuarkusTest" "$f" && ! grep -q "persist\|flush" "$f"; then
    echo "Kandidat: $(basename $f)"
  fi
done
```

### Schritt 2: Migration durchführen

#### Option A: Plain JUnit (kein Quarkus)
```java
// VORHER
@QuarkusTest
@Tag("migrate")
@TestSecurity(user = "testuser", roles = {"admin"})
class MyServiceTest {
  @Inject MyService myService;

  @Test
  void testEnumLogic() {
    assertThat(MyEnum.VALUE.isActive()).isTrue();
  }
}

// NACHHER
@Tag("unit")
class MyServiceTest {
  // Keine Annotations - plain JUnit!

  @Test
  void testEnumLogic() {
    assertThat(MyEnum.VALUE.isActive()).isTrue();
  }
}
```

#### Option B: @QuarkusTest + @InjectMock
```java
// VORHER
@QuarkusTest
@Tag("migrate")
class MyServiceTest {
  @Inject MyService myService;
  @Inject MyRepository myRepository;

  @Test
  @TestTransaction
  void testServiceLogic() {
    myRepository.persist(entity);
    var result = myService.process();
    assertThat(result).isNotNull();
  }
}

// NACHHER
@QuarkusTest
@Tag("unit")
class MyServiceTest {
  @Inject MyService myService;
  @InjectMock MyRepository myRepository;

  @Test  // Kein @TestTransaction!
  void testServiceLogic() {
    when(myRepository.findById(1L)).thenReturn(mockEntity);
    var result = myService.process();
    assertThat(result).isNotNull();
  }
}
```

### Schritt 3: Wichtige Änderungen

**Imports anpassen:**
```java
// Plain JUnit: Entfernen
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;

// @InjectMock: Hinzufügen
import io.quarkus.test.InjectMock;
import static org.mockito.Mockito.when;
```

**@Nested entfernen:**
- @Nested funktioniert nicht mit @TestTransaction in Inner Classes
- Flache Struktur bevorzugen
- Sections mit Kommentaren trennen

**Tags anpassen:**
```java
// Von "migrate" zu "unit"
@Tag("unit")  // statt @Tag("migrate")
```

### Schritt 4: Validieren
```bash
# Kompilieren
./mvnw test-compile -q

# Testen
./mvnw test -Dtest=MyServiceTest -q

# Zeit messen (vorher/nachher)
time ./mvnw test -Dtest=MyServiceTest
```

### Schritt 5: Commit
```bash
git add src/test/java/.../MyServiceTest.java
git commit -m "test: migrate MyServiceTest to plain JUnit

Performance: 15s → 0.1s (150x faster)
Tests: X passed
"
```

---

## 📊 Erfolgskennzahlen

### Aktueller Stand (2025-10-01)
| Metrik | Vorher | Nach 2 Migrationen | Ziel |
|--------|--------|-------------------|------|
| @QuarkusTest | 79 | 77 | 50 |
| CI-Zeit | 14-16 min | ~13:30 min | 6-8 min |
| Migriert | 0 | 2 | 29 |
| Einsparung | 0s | ~30s | ~7.5 min |

### Migrierte Tests

**1. OpportunityRenewalServiceTest**
- **Vorher:** @QuarkusTest, 10 Tests, ~15s
- **Nachher:** Plain JUnit, 9 Tests, 0.115s
- **Speedup:** 130x schneller ⚡
- **LOC:** 196 → 185 (-11 LOC)

**2. SmartSortServiceTest**
- **Vorher:** @QuarkusTest, 13 Tests, ~15s
- **Nachher:** Plain JUnit, 13 Tests, 0.127s
- **Speedup:** 118x schneller ⚡
- **LOC:** 241 → 267 (+26 LOC, better documentation)

---

## 🎓 Lessons Learned

### ✅ Do's
1. **Start mit einfachsten Kandidaten** (0 persist-Calls)
2. **Plain JUnit bevorzugen** über @InjectMock
3. **Template etablieren** bevor Batch-Migration
4. **Zeit messen** für Impact-Nachweis
5. **Tests ausführen** nach jeder Migration

### ❌ Don'ts
1. **Nicht** Repository-Tests migrieren
2. **Nicht** @TestTransaction entfernen wenn DB nötig
3. **Nicht** Batch ohne Template
4. **Nicht** Migration ohne Test-Ausführung

---

## 🚀 Nächste Schritte

### Batch 1: Enum/Logic Tests (geschätzt 5-8 Files)
- SmartSortServiceTest (13 Tests, 0 persist)
- Weitere Enum-Tests finden
- **Impact:** ~90s CI-Zeit

### Batch 2: Service mit Mock (geschätzt 10-15 Files)
- Services ohne persist() aber mit Dependencies
- @InjectMock verwenden
- **Impact:** ~180s CI-Zeit

### Batch 3: Optimierung (geschätzt 5-10 Files)
- Grenzfälle prüfen
- Komplexere Migrations-Patterns
- **Impact:** ~100s CI-Zeit

**Gesamt-Ziel:** 79 → 50 @QuarkusTest = **~7,5 min CI-Einsparung**

---

## 📖 Referenzen

- **Template-Beispiel:** `OpportunityRenewalServiceTest.java`
- **Test Strategy:** `TEST_STRATEGY_IMPROVEMENT.md` Phase 2.2
- **Quarkus Docs:** https://quarkus.io/guides/getting-started-testing#mock-support
