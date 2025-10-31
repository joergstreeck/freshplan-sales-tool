# ISSUE: XentralOrderEventHandler CDI Injection Problem

**Sprint:** 2.1.7.2 - Xentral Integration
**Erstellt:** 2025-10-22
**Status:** 🟡 OPEN (Nicht kritisch für Sprint 2.1.7.4)
**Priorität:** MEDIUM
**Owner:** Sprint 2.1.7.2 Team

---

**📍 Navigation:**
- [🏠 Sprint 2.1.7.2 Hauptdokument](../../TRIGGER_SPRINT_2_1_7_2.md)
- [🔧 Technische Spec](./SPEC_SPRINT_2_1_7_2_TECHNICAL.md) - D7 Webhook Integration
- [📋 Commit-Zusammenfassung](./sprint-2.1.7.2-COMMIT-SUMMARY.md) - Was funktioniert

**💡 Issue-Kontext:**
- Dieses Issue betrifft **D7 - Xentral Webhook Integration**
- Mock-Implementation existiert, aber CDI-Injection schlägt fehl
- **Nicht kritisch** - Sprint 2.1.7.4 definiert nur Interface, echte Implementation kommt später

---

## 📋 PROBLEM-BESCHREIBUNG

**Tests vorhanden:** 6 Tests in `MockXentralOrderEventHandlerTest.java`  
**Tests Status:** 1 Error, 5 Skipped  
**Fehler:** CDI Injection Problem beim Test-Start

### Test-Output:
```
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0
[INFO] BUILD FAILURE
```

### Root Cause:
Die `XentralOrderEventHandler`-Interface und `MockXentralOrderEventHandler`-Implementation sind korrekt implementiert, aber die CDI-Injection schlägt im Test-Kontext fehl.

---

## 🔍 ANALYSE

### Betroffene Dateien:
1. **Interface:**  
   `backend/src/main/java/de/freshplan/modules/xentral/service/XentralOrderEventHandler.java`

2. **Mock Implementation:**  
   `backend/src/main/java/de/freshplan/modules/xentral/service/MockXentralOrderEventHandler.java`
   - Korrekt annotiert mit `@ApplicationScoped`
   - Implementiert Interface korrekt

3. **Tests:**  
   `backend/src/test/java/de/freshplan/modules/xentral/service/MockXentralOrderEventHandlerTest.java`
   - 6 Tests implementiert (vollständige Coverage)
   - CDI-Injection schlägt fehl

### Warum nicht kritisch für Sprint 2.1.7.4?
- Sprint 2.1.7.4 definiert nur das **Interface** (Vorbereitung)
- Die **echte Implementation** kommt erst in Sprint 2.1.7.2
- Mock-Implementation ist nur ein Platzhalter (loggt nur Events)
- Keine Business-Logik betroffen

---

## 🎯 LÖSUNGSANSÄTZE für Sprint 2.1.7.2

### Option 1: QuarkusTest-Konfiguration prüfen
```java
@QuarkusTest
class MockXentralOrderEventHandlerTest {
  
  @Inject
  XentralOrderEventHandler handler; // ← Hier schlägt Injection fehl
  
  // Tests...
}
```

**Mögliche Ursachen:**
- Fehlende CDI-Konfiguration für `modules.xentral` Package
- Interface ohne `@Default` Qualifier bei mehreren Implementations
- Test-Isolation-Problem

### Option 2: Alternative Injection
```java
@Inject
MockXentralOrderEventHandler mockHandler; // Direkte Injection der Klasse

// Oder via Producer:
@Produces
@ApplicationScoped
XentralOrderEventHandler produceHandler() {
  return new MockXentralOrderEventHandler();
}
```

### Option 3: Integration Test statt Unit Test
```java
// Echte Xentral-Webhook-Integration testen
@QuarkusIntegrationTest
class XentralWebhookIntegrationTest {
  // Test mit echtem HTTP-Endpoint
}
```

---

## ✅ VERIFIZIERTE FUNKTIONALITÄT

**Was funktioniert:**
1. ✅ Interface-Definition korrekt
2. ✅ Mock-Implementation kompiliert
3. ✅ Quarkus startet ohne Fehler (dev mode)
4. ✅ Business-Logik in anderen Services (CustomerService, OpportunityService) funktioniert

**Was NICHT funktioniert:**
1. ❌ CDI-Injection im @QuarkusTest

---

## 🔧 FIX-STRATEGIE für Sprint 2.1.7.2

1. **Phase 1: Test-Fix (Quick)**
   - CDI-Konfiguration prüfen
   - Ggf. `@Default` Qualifier hinzufügen
   - Alternative Injection-Methode testen

2. **Phase 2: Echte Implementation**
   - Xentral-Webhook-Endpoint implementieren
   - CustomerService.activateCustomer() Integration
   - Echte Tests mit Webhook-Mocking

3. **Phase 3: E2E-Test**
   - Integration Test mit echtem Xentral-Webhook
   - Verify PROSPECT → AKTIV Flow

---

## 📝 TECHNISCHE NOTIZEN

### Xentral-Webhook Spec (Sprint 2.1.7.2):
```java
POST /api/webhooks/xentral/order-delivered
{
  "xentralCustomerId": "XEN-12345",
  "orderNumber": "ORD-2025-001",
  "deliveryDate": "2025-10-22"
}
```

### Erwartete Business-Logik:
1. Find Customer by `xentralCustomerId`
2. If Status = PROSPECT:
   - Call `customerService.activateCustomer(customerId, orderNumber)`
   - Log activation
3. If Status = AKTIV:
   - Update `lastOrderDate`
   - Log order event

---

## 📊 TEST-STATUS

| Test | Status | Bemerkung |
|------|--------|-----------|
| Interface Injection | ❌ ERROR | CDI-Problem |
| Mock logs event | ⏭️ SKIPPED | Abhängig von Injection |
| Null values handling | ⏭️ SKIPPED | Abhängig von Injection |
| Empty strings handling | ⏭️ SKIPPED | Abhängig von Injection |
| Past delivery date | ⏭️ SKIPPED | Abhängig von Injection |
| Future delivery date | ⏭️ SKIPPED | Abhängig von Injection |

**Gesamt:** 1/6 executed, 5 skipped

---

## 🚀 NEXT STEPS

**Sprint 2.1.7.2:**
1. Fix CDI-Injection in Tests
2. Implement echten Xentral-Webhook-Endpoint
3. Replace Mock mit echter Implementation
4. Integration Tests mit Webhook-Mocking
5. E2E-Test mit Testdaten

**Dependencies:**
- Xentral API Documentation
- Webhook Authentication (API Key?)
- Error Handling Strategy

---

**Letzte Aktualisierung:** 2025-10-22 (Sprint 2.1.7.4 completed)  
**Verantwortlich für Fix:** Sprint 2.1.7.2 Team
