# Lösung für Problem #6: Feature Flag Inkonsistenz (CQRS)

**Datum:** 2025-08-17
**Problem:** Inkonsistente CQRS Feature Flag Nutzung in Tests
**Severity:** MITTEL

## 🎯 Das Problem

- **Default-Wert:** `features.cqrs.enabled=true` in application.properties
- **Test-Profile:** `AuditCQRSTestProfile` setzt es explizit auf `true`
- **Risiko:** Tests könnten unterschiedliche Modi testen, ohne es zu wissen

## ✅ Empfohlene Lösung: Explizite Test-Profile für beide Modi

### 1. Test-Profile für CQRS=false (Legacy Mode)

**LegacyModeTestProfile.java:**
```java
package de.freshplan.test.profiles;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test Profile for Legacy Mode (CQRS disabled).
 * Use this to ensure backwards compatibility.
 */
public class LegacyModeTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "features.cqrs.enabled", "false",
            "test.mode", "legacy"
        );
    }
    
    @Override
    public String getConfigProfile() {
        return "test-legacy";
    }
}
```

### 2. Test-Profile für CQRS=true (CQRS Mode)

**CQRSModeTestProfile.java:**
```java
package de.freshplan.test.profiles;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test Profile for CQRS Mode (CQRS enabled).
 * This is the future architecture.
 */
public class CQRSModeTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "features.cqrs.enabled", "true",
            "test.mode", "cqrs"
        );
    }
    
    @Override
    public String getConfigProfile() {
        return "test-cqrs";
    }
}
```

### 3. Test-Matrix implementieren

**Abstrakte Basis-Klasse für Dual-Mode Tests:**
```java
package de.freshplan.test.base;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/**
 * Base class for tests that should run in both modes.
 */
public abstract class DualModeTestBase {
    
    @QuarkusTest
    @TestProfile(LegacyModeTestProfile.class)
    public static class LegacyModeTest extends DualModeTestBase {
        // Tests werden von Basis-Klasse geerbt
    }
    
    @QuarkusTest
    @TestProfile(CQRSModeTestProfile.class)
    public static class CQRSModeTest extends DualModeTestBase {
        // Tests werden von Basis-Klasse geerbt
    }
    
    // Abstrakte Test-Methoden, die in beiden Modi laufen müssen
    @Test
    abstract void testCriticalBusinessLogic();
}
```

### 4. Konkrete Implementierung für Services

**CustomerServiceDualModeTest.java:**
```java
package de.freshplan.domain.customer.service;

public class CustomerServiceDualModeTest extends DualModeTestBase {
    
    @Inject
    CustomerService customerService;
    
    @ConfigProperty(name = "features.cqrs.enabled")
    boolean cqrsEnabled;
    
    @Override
    @Test
    void testCriticalBusinessLogic() {
        // Arrange
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setCompanyName("Test GmbH");
        
        // Act
        CustomerResponse response = customerService.createCustomer(request);
        
        // Assert - funktioniert in beiden Modi
        assertThat(response).isNotNull();
        assertThat(response.getCompanyName()).isEqualTo("Test GmbH");
        
        // Mode-spezifische Assertions
        if (cqrsEnabled) {
            // CQRS-spezifische Prüfungen
            verify(eventBus).publish(any(CustomerCreatedEvent.class));
        } else {
            // Legacy-spezifische Prüfungen
            verify(repository).persist(any(Customer.class));
        }
    }
}
```

### 5. CI-Pipeline Matrix

**.github/workflows/backend-ci.yml:**
```yaml
test-matrix:
  strategy:
    matrix:
      test-profile: [legacy, cqrs]
  steps:
    - name: Run tests with ${{ matrix.test-profile }} mode
      run: |
        ./mvnw test \
          -Dquarkus.test.profile=${{ matrix.test-profile }} \
          -Dtest.mode=${{ matrix.test-profile }}
    
    - name: Validate test coverage
      run: |
        if [ "${{ matrix.test-profile }}" == "cqrs" ]; then
          # CQRS mode sollte mindestens gleiche Coverage haben
          ./mvnw jacoco:check -Djacoco.minimum.coverage=0.80
        fi
```

## 🎯 Migration-Strategie

### Phase 1: Bestandsaufnahme (1 Tag)
```bash
# Alle Tests finden, die CQRS-relevante Services nutzen
grep -r "@Inject.*Service" src/test --include="*.java" | \
  grep -E "(Customer|Opportunity|Contact|Audit)Service"
```

### Phase 2: Kategorisierung (2 Tage)
- **Kategorie A:** Tests die in beiden Modi laufen MÜSSEN
- **Kategorie B:** Tests die nur in CQRS laufen
- **Kategorie C:** Tests die nur in Legacy laufen

### Phase 3: Implementierung (1 Woche)
1. Test-Profile erstellen
2. Dual-Mode Base Classes implementieren
3. Kritische Tests migrieren
4. CI-Matrix aktivieren

## ✅ Vorteile dieser Lösung

1. **Explizit statt implizit:** Jeder Test deklariert seinen Modus
2. **Vollständige Coverage:** Beide Modi werden getestet
3. **Migration-Pfad:** Schrittweise von Legacy zu CQRS
4. **CI-Sicherheit:** Matrix testet alle Kombinationen
5. **Rückwärtskompatibilität:** Legacy-Mode bleibt testbar

## 📊 Metriken für Erfolg

```sql
-- Test-Verteilung prüfen
SELECT 
    test_profile,
    COUNT(*) as test_count,
    AVG(execution_time) as avg_time
FROM test_results
GROUP BY test_profile;
```

## ⚠️ Wichtige Hinweise

1. **Default sollte CQRS sein** (Zukunft)
2. **Legacy nur für Rückwärtskompatibilität**
3. **Neue Tests immer für CQRS Mode**
4. **Dual-Mode nur für kritische Business-Logik**

## 📝 Checkliste

- [ ] Test-Profile erstellen
- [ ] Basis-Klassen implementieren
- [ ] Kritische Tests identifizieren
- [ ] Migration durchführen
- [ ] CI-Matrix konfigurieren
- [ ] Dokumentation aktualisieren
- [ ] Team-Schulung