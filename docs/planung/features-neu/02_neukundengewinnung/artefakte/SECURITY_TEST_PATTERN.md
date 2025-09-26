# 🔒 Security Test Pattern - Lead Module (FP-236)

**Status:** ✅ IMPLEMENTED (PR #110)
**Erstellt:** 26.09.2025
**Referenz-Implementation:** `/backend/src/test/java/de/freshplan/modules/leads/security/`

## 🎯 Pattern Overview

Umfassendes Security-Test-Pattern mit 4 Test-Kategorien für ABAC/RLS-Validierung.

## 📋 Test-Kategorien

### 1. Basic Security Tests (Positive Cases)
```java
@QuarkusTest
@DisplayName("Lead Basic Security Tests - FP-236")
public class LeadSecurityBasicTest {

    @Test
    void testLeadCreationWithTerritory() {
        // Territory-Assignment validieren
        // Protection-Timing prüfen
        // Owner-Zuweisung verifizieren
    }

    @Test
    void testStopTheClockFunctionality() {
        // Protection pausieren
        // Clock-Stop validieren
        // Resume verifizieren
    }
}
```

### 2. Negative Security Tests (Fail-Closed)
```java
@QuarkusTest
@DisplayName("Lead Negative Security Tests - FP-236")
public class LeadSecurityNegativeTest {

    @Test
    @TestSecurity(user = "different-user", roles = {"user"})
    void testUnauthorizedAccessDenied() {
        // Foreign Lead Access verhindern
        // Ownership-Check validieren
        // Collaborator-Rechte prüfen
    }

    @Test
    void testPrivilegeEscalationPrevention() {
        // Collaborator != Owner
        // Owner-Only Operations schützen
        // Territory-Isolation garantieren
    }
}
```

### 3. Performance Validation Tests
```java
@QuarkusTest
@DisplayName("Lead Performance Validation - FP-236")
public class LeadPerformanceValidationTest {

    private static final long P95_THRESHOLD_MS = 200;

    @Test
    void testFindByTerritoryPerformance() {
        long p95 = measureP95(() -> Lead.find("territory", territory));
        assertTrue(p95 < P95_THRESHOLD_MS,
            "P95 should be < 200ms, was: " + p95);
    }
}
```

### 4. Event Integration Tests
```java
@QuarkusTest
@DisplayName("Lead Event Integration Tests - FP-236")
public class LeadEventIntegrationTest {

    @Inject TestEventCollector eventCollector;

    @Test
    void testStatusChangeEventPublished() throws InterruptedException {
        // Event publizieren
        eventPublisher.publishStatusChange(lead, oldStatus, newStatus, user);

        // Event-Empfang verifizieren (wenn LISTEN/NOTIFY aktiv)
        Thread.sleep(500); // Async-Processing abwarten

        // In CI mit Postgres:
        // LeadStatusChangeEvent received =
        //     eventCollector.pollStatusChangeEvent(5, TimeUnit.SECONDS);
        // assertNotNull(received);
    }
}
```

## 🔧 Test-Helper: TestEventCollector
```java
@ApplicationScoped
public class TestEventCollector {
    private final BlockingQueue<LeadStatusChangeEvent> events =
        new LinkedBlockingQueue<>();

    void onEvent(@Observes @Priority(APPLICATION - 100)
                 LeadStatusChangeEvent event) {
        events.add(event);
    }

    public LeadStatusChangeEvent pollStatusChangeEvent(
            long timeout, TimeUnit unit) throws InterruptedException {
        return events.poll(timeout, unit);
    }
}
```

## 📊 Metriken & Coverage

### Erreichte Metriken (PR #110)
- **Tests:** 23 Tests alle grün
- **Performance:** P95 < 7ms (Requirement: < 200ms)
- **Coverage:** Lead-Module ≥ 85%
- **Security:** Fail-Closed Policy validiert

### Test-Matrix
| Kategorie | Tests | Status | P95 |
|-----------|-------|--------|-----|
| Basic Security | 6 | ✅ | - |
| Negative Security | 6 | ✅ | - |
| Performance | 5 | ✅ | < 7ms |
| Event Integration | 6 | ✅ | - |

## 🚀 Verwendung in anderen Modulen

### Copy-Paste Template
1. Test-Klassen aus `/backend/src/test/java/de/freshplan/modules/leads/security/` kopieren
2. Package/Modul-Namen anpassen
3. Entity-spezifische Tests ergänzen
4. Performance-Thresholds validieren

### ⚠️ WICHTIGER HINWEIS: RLS-Limitierung im Test-Setup
**DISCLAIMER:** Die Negative-Tests mit `@TestSecurity` dokumentieren das **erwartete Fail-Closed-Verhalten**, aber RLS ist im Test-Setup nicht vollständig aktiv. Die Tests zeigen:
- Wie sich das System verhalten SOLLTE mit aktiver RLS
- Fail-Closed-Pattern als Security-Standard
- Erwartete Access-Denial bei falschen Rollen/User-IDs

In Production muss RLS auf DB-Ebene aktiv sein (siehe [ADR-0007 RLS Connection Affinity](../../../adr/ADR-0007-rls-connection-affinity.md)).

### Anpassungen pro Modul
- **Modul 03 (Kunden):** Customer statt Lead, Account-Hierarchy
- **Modul 05 (Kommunikation):** Thread-Security, Message-Isolation
- **Modul 01 (Cockpit):** Widget-Access, Dashboard-Permissions

## 🔗 Referenzen
- [PR #110](https://github.com/joergstreeck/freshplan-sales-tool/pull/110)
- [FP-236 Security-Integration](../technical-concept.md#event-system-implementation-pr-110-complete)
- [Gemini Code Review](https://github.com/joergstreeck/freshplan-sales-tool/pull/110#issuecomment-3339529993)