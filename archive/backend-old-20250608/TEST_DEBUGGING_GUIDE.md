# Test Debugging Guide für Backend

## Quick Reference

### Test isoliert ausführen
```bash
# Einzelner Test
./mvnw test -Dtest=UserServiceTest#testGetAllUsers

# Alle Tests einer Klasse
./mvnw test -Dtest=UserServiceTest

# Mit Pattern
./mvnw test -Dtest=*ServiceTest

# Mit Debug-Output
./mvnw test -Dtest=UserServiceTest -X
```

### Debug-Helper für Tests

Erstelle diese Utility-Klasse in `src/test/java/de/freshplan/test/util/`:

```java
package de.freshplan.test.util;

public class TestDebugHelper {
    private static final boolean DEBUG_ENABLED = 
        Boolean.parseBoolean(System.getProperty("test.debug", "false")) ||
        System.getenv("CI") != null;
    
    public static void debug(String format, Object... args) {
        if (DEBUG_ENABLED) {
            System.out.printf("[DEBUG] " + format + "%n", args);
        }
    }
    
    public static void debugEntity(String label, Object entity) {
        if (DEBUG_ENABLED) {
            System.out.printf("[DEBUG] %s: %s%n", label, 
                entity != null ? entity.toString() : "null");
        }
    }
    
    public static void debugException(String label, Exception e) {
        if (DEBUG_ENABLED) {
            System.out.printf("[DEBUG] %s: %s%n", label, e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
```

### Verwendung in Tests

```java
import static de.freshplan.test.util.TestDebugHelper.*;

@Test
void problematicTest() {
    // Setup
    debug("Setting up test data");
    var user = createTestUser();
    debugEntity("Created user", user);
    
    // Execution
    debug("Calling service method");
    try {
        var result = service.updateUser(user);
        debugEntity("Update result", result);
    } catch (Exception e) {
        debugException("Service call failed", e);
        throw e;
    }
    
    // Verification
    debug("Verifying results");
    assertThat(result).isNotNull();
}
```

### Aktivierung

```bash
# Lokal mit Debug
./mvnw test -Dtest.debug=true

# In CI wird automatisch aktiviert (CI env variable)
```

## Häufige Probleme und Lösungen

### 1. Mock gibt immer dasselbe zurück

**Problem:**
```java
when(mapper.toResponse(any())).thenReturn(response1);
// Alle Aufrufe geben response1 zurück
```

**Lösung:**
```java
when(mapper.toResponse(any())).thenAnswer(invocation -> {
    User user = invocation.getArgument(0);
    return createResponseFor(user);
});
```

### 2. Entity wird nicht aktualisiert

**Problem:**
```java
repository.persist(entity);
var loaded = repository.findById(entity.getId());
// loaded hat alte Werte
```

**Lösung:**
```java
repository.persist(entity);
repository.flush(); // Force DB write
em.clear();         // Clear cache
var loaded = repository.findById(entity.getId());
```

### 3. Transactional Test schlägt fehl

**Problem:**
```java
// Test ohne @Transactional
service.updateUser(user); // Fails
```

**Lösung:**
```java
@Test
@Transactional  // Wichtig!
void testUpdate() {
    service.updateUser(user);
}
```

## CI-spezifische Tipps

1. **Immer flush() nach persist()**
2. **@Transactional bei DB-Tests**
3. **Timeouts großzügiger setzen**
4. **Debug-Output nur in CI**

## Nützliche Maven-Befehle

```bash
# Nur fehlgeschlagene Tests wiederholen
./mvnw test -Dsurefire.rerunFailingTestsCount=2

# Tests parallel ausführen
./mvnw test -Dparallel=methods -DthreadCount=4

# Bestimmte Tests überspringen
./mvnw test -Dtest=!LongRunningTest

# Mit mehr Speicher
./mvnw test -DargLine="-Xmx1024m"
```