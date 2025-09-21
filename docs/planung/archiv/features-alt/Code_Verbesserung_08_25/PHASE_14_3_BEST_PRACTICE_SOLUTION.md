# Phase 14.3 - Best Practice Lösung für Async Testing in Quarkus

## ✅ Die richtige Lösung: QuarkusTransaction

### Problem
Bei Async-Tests mit `Awaitility` geht der CDI Request Context in Lambda-Expressions verloren:
```java
// ❌ FALSCH - Führt zu "Cannot use EntityManager" Fehler
await()
    .untilAsserted(() -> {
        AuditEntry entry = auditRepository.findById(id); // FEHLER!
    });
```

### Lösung: QuarkusTransaction.call()
```java
// ✅ RICHTIG - Best Practice für Async Database Access
await()
    .until(() -> {
        return QuarkusTransaction.call(() -> {
            AuditEntry entry = auditRepository.findById(id);
            return entry != null;
        });
    });
```

## Implementierte Fixes

### 1. AuditCQRSIntegrationTest
```java
// Import hinzugefügt
import io.quarkus.narayana.jta.QuarkusTransaction;

// Test angepasst
await()
    .atMost(Duration.ofSeconds(2))
    .until(() -> {
        return QuarkusTransaction.call(() -> {
            AuditEntry entry = auditRepository.findById(auditId);
            return entry != null && 
                   entry.getEntityId().equals(testEntityId) &&
                   entry.getEventType().equals(AuditEventType.CUSTOMER_CREATED);
        });
    });
```

**Ergebnis:** Von 3 Fehlern auf 1 reduziert! ✅

### 2. ContactEventCaptureCQRSIntegrationTest
```java
// Alle untilAsserted() zu until() mit QuarkusTransaction.call() geändert
await()
    .until(() -> {
        return QuarkusTransaction.call(() -> {
            List<ContactInteraction> interactions = 
                interactionRepository.find("contactId", testContact.getId()).list();
            return interactions.stream()
                .anyMatch(i -> "EMAIL".equals(i.getInteractionType()));
        });
    });
```

## Warum ist das Best Practice?

### ✅ Vorteile:
1. **Transaction Management:** Jeder DB-Zugriff läuft in eigener Transaktion
2. **CDI Context:** Kein Context-Verlust in Lambda-Expressions
3. **Clean Code:** Explizit und verständlich
4. **Testbar:** Funktioniert zuverlässig in CI/CD
5. **Performance:** Keine unnötigen Thread.sleep()

### ❌ Anti-Patterns vermieden:
- `Thread.sleep()` - macht Tests langsam und unzuverlässig
- `@ActivateRequestContext` auf Lambda - funktioniert nicht
- Polling ohne Transaction - führt zu Context-Fehlern

## Alternative Best Practices

### 1. TransactionManager (Explizit)
```java
@Inject
TransactionManager transactionManager;

transactionManager.begin();
try {
    AuditEntry entry = auditRepository.findById(id);
    assertThat(entry).isNotNull();
    transactionManager.commit();
} catch (Exception e) {
    transactionManager.rollback();
    throw e;
}
```

### 2. Event-Driven Testing
```java
CountDownLatch latch = new CountDownLatch(1);
auditCompleteEvent.observeAsync(event -> {
    capturedId.set(event.getAuditId());
    latch.countDown();
});
assertTrue(latch.await(5, TimeUnit.SECONDS));
```

### 3. Separate Test-Transaktionen
```java
@Test
@TestTransaction
void step1_create() { /* create */ }

@Test
@TestTransaction
@Order(2)
void step2_verify() { /* verify */ }
```

## Lessons Learned

1. **Quarkus CDI Context** ist nicht thread-safe
2. **Awaitility Lambda** läuft in separatem Thread
3. **QuarkusTransaction.call()** ist die sauberste Lösung
4. **Best Practice** > Quick Fix immer!

## Status

- **AuditCQRSIntegrationTest:** 9/10 Tests grün ✅
- **ContactEventCaptureCQRSIntegrationTest:** In Arbeit
- **Lösung:** Funktioniert und ist Best Practice!

## Referenzen
- [Quarkus Transaction Guide](https://quarkus.io/guides/transaction)
- [Awaitility Documentation](https://github.com/awaitility/awaitility)
- [CDI Context Management](https://quarkus.io/guides/cdi-reference#contexts)

---
**Erstellt von:** Claude
**Datum:** 15.08.2025
**Status:** Best Practice implementiert ✅