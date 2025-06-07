# CI Debugging-Strategie: "Die Strategie der kleinen Schritte"

## Übersicht

Diese Strategie wurde aus unserer Erfahrung mit dem User Management Feature entwickelt, wo wir von 52 Tests mit 16 Fehlschlägen zu 55 grünen Tests gekommen sind.

## Wann diese Strategie anwenden?

- ✅ CI schlägt nach mehreren Versuchen immer noch fehl
- ✅ Der Fehler ist nicht offensichtlich aus den Logs ersichtlich
- ✅ Lokale Tests laufen, aber CI schlägt fehl
- ✅ Komplexe Interaktionen zwischen Komponenten

## Die "Green Path" Strategie

### Phase 1: Minimaler grüner Zustand

```xml
<!-- pom.xml - Minimales Test-Set -->
<profile>
    <id>debug</id>
    <build>
        <plugins>
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <includes>
                        <!-- Nur die absolut notwendigen Tests -->
                        <include>**/*MinimalTest.java</include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

### Phase 2: Schrittweise Erweiterung

```bash
# Schritt 1: Basis-Tests
./mvnw test -Pgreen -Dincludes="**/AppStartsIT.java"

# Schritt 2: +Repository Tests
./mvnw test -Pgreen -Dincludes="**/*Repository*.java"

# Schritt 3: +Service Tests
./mvnw test -Pgreen -Dincludes="**/*Service*.java"

# Schritt 4: +REST Tests
./mvnw test -Pgreen
```

## Debug-Output Patterns

### 1. Strukturierter Debug-Output

```java
public class DebugHelper {
    private static final String DEBUG_PREFIX = "=== DEBUG";
    private static final boolean CI_ENVIRONMENT = 
        System.getenv("CI") != null;
    
    public static void log(String phase, String message, Object... args) {
        if (CI_ENVIRONMENT) {
            System.out.printf("%s [%s]: %s ===%n", 
                DEBUG_PREFIX, phase, String.format(message, args));
        }
    }
    
    public static void logEntity(String phase, Object entity) {
        if (CI_ENVIRONMENT && entity != null) {
            System.out.printf("%s [%s-ENTITY]: %s ===%n", 
                DEBUG_PREFIX, phase, entity.toString());
        }
    }
}
```

### 2. Test-spezifisches Debugging

```java
@Test
void debuggingFailingTest() {
    // Checkpoint 1
    DebugHelper.log("SETUP", "Starting test setup");
    var testData = createTestData();
    DebugHelper.logEntity("SETUP", testData);
    
    // Checkpoint 2
    DebugHelper.log("EXECUTION", "Calling service method");
    var result = service.process(testData);
    DebugHelper.logEntity("EXECUTION", result);
    
    // Checkpoint 3
    DebugHelper.log("VERIFICATION", "Checking assertions");
    assertThat(result).isNotNull();
    DebugHelper.log("VERIFICATION", "Test completed successfully");
}
```

### 3. Repository/Database Debugging

```java
@Test
@Transactional
void debugDatabaseInteraction() {
    // Log vor DB-Operation
    DebugHelper.log("DB", "Before persist - Entity: %s", entity);
    repository.persist(entity);
    
    // Expliziter Flush für CI
    repository.flush();
    DebugHelper.log("DB", "After flush - ID: %s", entity.getId());
    
    // Clear für sauberen State
    repository.getEntityManager().clear();
    DebugHelper.log("DB", "After clear - Loading entity");
    
    // Reload und verifizieren
    var loaded = repository.findById(entity.getId());
    DebugHelper.logEntity("DB", loaded);
}
```

## CI-spezifische Anpassungen

### GitHub Actions Workflow

```yaml
name: Debug CI

on:
  workflow_dispatch:
    inputs:
      debug_enabled:
        description: 'Enable debug mode'
        required: false
        default: 'false'

jobs:
  debug-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          
      - name: Run tests with debug
        env:
          DEBUG_MODE: ${{ github.event.inputs.debug_enabled }}
        run: |
          if [ "$DEBUG_MODE" = "true" ]; then
            echo "=== Running in DEBUG mode ==="
            ./mvnw test -X -Dtest=FailingTest
          else
            ./mvnw test
          fi
          
      - name: Upload debug logs
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: debug-logs
          path: |
            target/surefire-reports/
            **/*.log
```

## Praktisches Beispiel: UserServiceTest Fix

### Problem
```java
// Test schlug in CI fehl
@Test
void testGetAllUsers() {
    when(userRepository.listAll()).thenReturn(List.of(user1, user2));
    when(userMapper.toResponse(any())).thenReturn(response1); // FEHLER!
    
    var result = userService.getAllUsers();
    assertThat(result).hasSize(2); // Fehlschlag: alle Responses identisch
}
```

### Debug-Ansatz
```java
@Test
void testGetAllUsers_DEBUG() {
    // Schritt 1: Minimal
    System.out.println("=== DEBUG: Test setup ===");
    var users = List.of(user1, user2);
    when(userRepository.listAll()).thenReturn(users);
    
    // Schritt 2: Mock-Verhalten debuggen
    System.out.println("=== DEBUG: Setting up mapper mocks ===");
    when(userMapper.toResponse(any())).thenAnswer(invocation -> {
        User u = invocation.getArgument(0);
        System.out.println("=== DEBUG: Mapping user: " + u.getUsername());
        return u.getUsername().equals("user1") ? response1 : response2;
    });
    
    // Schritt 3: Ausführung
    System.out.println("=== DEBUG: Calling service ===");
    var result = userService.getAllUsers();
    System.out.println("=== DEBUG: Result size: " + result.size());
    result.forEach(r -> 
        System.out.println("=== DEBUG: Response: " + r.getUsername())
    );
    
    // Schritt 4: Assertion
    assertThat(result).hasSize(2);
}
```

### Lösung
```java
// Answer-Pattern für unterschiedliche Responses
when(userMapper.toResponse(any(User.class)))
    .thenAnswer(invocation -> {
        User user = invocation.getArgument(0);
        if (user.getUsername().equals("john.doe")) {
            return testUserResponse;
        } else {
            return anotherResponse;
        }
    });
```

## Checkliste für CI-Debugging

- [ ] Test isoliert ausführen (`-Dtest=SpecificTest`)
- [ ] Debug-Output an kritischen Stellen
- [ ] Entity-Lifecycle beachten (flush/clear)
- [ ] Mock-Verhalten explizit prüfen
- [ ] CI-Umgebung vs. Lokal vergleichen
- [ ] Timing-Issues berücksichtigen
- [ ] Transaction-Boundaries prüfen
- [ ] Dependencies-Versionen verifizieren

## Lessons Learned

1. **Immer mit minimalem Test beginnen** - Erst wenn dieser grün ist, erweitern
2. **Debug-Output strukturiert einsetzen** - Mit klaren Phasen-Markierungen
3. **CI-Umgebung simulieren** - Docker-Container lokal für CI-Parität
4. **Schrittweise vorgehen** - Nie mehr als eine Änderung gleichzeitig
5. **Dokumentieren** - Gefundene Probleme für das Team festhalten

## Weiterführende Ressourcen

- [Maven Surefire Debug Options](https://maven.apache.org/surefire/maven-surefire-plugin/examples/debugging.html)
- [JUnit 5 Conditional Test Execution](https://junit.org/junit5/docs/current/user-guide/#conditional-execution)
- [Testcontainers for CI/CD](https://www.testcontainers.org/features/configuration/)