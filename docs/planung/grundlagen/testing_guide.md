# Testing Guide - FreshPlan Sales Tool

**Stand:** 2025-10-08
**Sprint:** 2.1.6 Phase 4
**Speicherort:** `docs/planung/grundlagen/testing_guide.md`

> **💡 Hinweis:** Für Coverage-Tools, CI/CD Setup & Monitoring siehe: `TESTING_INFRASTRUCTURE.md`

---

## 🎯 **WICHTIGSTER GRUNDSATZ: Tests sind kein Selbstzweck!**

> **Tests sollen Fehler im Code und Logikfehler finden - BEVOR sie in Produktion gehen.**
> Das erspart späteres aufwendiges Debugging, Hotfixes und Produktionsausfälle.

### Warum Tests schreiben?

1. **Fehler früh finden** → Billiger zu fixen (Dev statt Prod)
2. **Logikfehler aufdecken** → Business-Rules korrekt implementiert?
3. **Regression verhindern** → Neue Features brechen alte nicht
4. **Dokumentation** → Tests zeigen, wie Code funktionieren SOLL
5. **Refactoring-Sicherheit** → Mit Tests kann man Code umbauen ohne Angst

### ❌ **Schlechte Gründe für Tests:**
- "Weil wir 80% Coverage brauchen" (Coverage ist Mittel, kein Zweck!)
- "Weil der CI grün sein muss" (CI zeigt nur, ob Tests laufen - nicht ob sie gut sind!)
- "Weil das im Review gefordert wird" (Tests sollen HELFEN, nicht nerven!)

### ✅ **Gute Gründe für Tests:**
- "Ich will sicherstellen, dass leadScore IMMER im DTO gemapped wird"
- "Ich will testen, ob ADMIN Stop-Clock nutzen kann, USER aber nicht"
- "Ich will wissen, ob meine Scoring-Formel korrekt rechnet"
- "Ich will verhindern, dass NULL-Werte Abstürze verursachen"

---

## 📊 **Test-Strategie: Was soll getestet werden?**

### 1. **Business-Logic-Tests (PFLICHT)**
- **Warum:** Kernlogik der Anwendung muss korrekt sein
- **Beispiel:** Lead-Scoring-Berechnung (4 Faktoren → 0-100 Score)
- **Test:** "Lead mit €50k Volume → Score ≥70"

### 2. **Integration-Tests (PFLICHT)**
- **Warum:** API-Endpoints müssen korrekt arbeiten
- **Beispiel:** PATCH /leads/{id} mit Stop-Clock
- **Test:** "ADMIN kann Clock stoppen, USER bekommt 403"

### 3. **DTO-Completeness-Tests (EMPFOHLEN)**
- **Warum:** Fehlende Felder im API-Response verhindern
- **Beispiel:** `leadScore` fehlte im DTO (Produktionsbug!)
- **Test:** "GET /leads/{id} enthält leadScore-Feld"

### 4. **UI-Component-Tests (EMPFOHLEN)**
- **Warum:** UI-Bugs und UX-Probleme früh finden
- **Beispiel:** StopTheClockDialog zeigt Fehler für USER
- **Test:** "USER sieht Permission-Error-Dialog, nicht Pause-Form"

### 5. **Edge-Case-Tests (WICHTIG)**
- **Warum:** Unerwartete Eingaben crashen oft
- **Beispiel:** Negative Werte, NULL-Felder, leere Strings
- **Test:** "Score bleibt ≥0 auch bei negativem Volume"

---

## 🔍 **Test-Gap-Analyse: Warum fanden Tests Bugs nicht?**

### **Beispiel aus Sprint 2.1.6 Phase 4:**

#### **Bug 1: `leadScore` fehlte im LeadDTO**
- **Symptom:** DB-Spalte existiert, Domain-Field existiert, aber DTO-Mapping fehlt
- **Warum nicht gefunden?** Tests prüften nur spezifische Felder, nicht ALLE
- **Lösung:** DTO-Completeness-Test schreiben:
  ```java
  @Test
  void testLeadDtoIncludesLeadScore() {
    Long leadId = createTestLeadWithScore("user1", 75);

    given()
      .when().get("/" + leadId)
      .then()
      .statusCode(200)
      .body("leadScore", equalTo(75)); // Explizit prüfen!
  }
  ```

#### **Bug 2: Stop-the-Clock RBAC UI-Check fehlte**
- **Symptom:** Backend hat RBAC, Frontend zeigt Button allen Usern → 403 Error
- **Warum nicht gefunden?** Keine Frontend-Tests mit verschiedenen Rollen
- **Lösung:** Component-Test mit Rollen schreiben:
  ```typescript
  it('should show permission error for USER role', () => {
    mockUseAuth.mockReturnValue({
      hasRole: (role: string) => role === 'USER'
    });

    render(<StopTheClockDialog open={true} lead={mockLead} />);

    expect(screen.getByText('Keine Berechtigung')).toBeInTheDocument();
  });
  ```

#### **Bug 3: Lead-Score-Berechnung fehlte komplett**
- **Symptom:** DB-Spalte da, UI da, aber keine Berechnung
- **Warum nicht gefunden?** Kein Business-Logic-Test für Scoring
- **Lösung:** Service-Test schreiben:
  ```java
  @Test
  void testLeadScoringCalculation() {
    Lead lead = new Lead();
    lead.estimatedVolume = new BigDecimal("50000");
    lead.employeeCount = 25;
    lead.lastActivityAt = LocalDateTime.now().minusDays(2);
    lead.businessType = "RESTAURANT";

    leadScoringService.calculateScore(lead);

    assertTrue(lead.leadScore >= 70,
      "High-value lead should have score ≥70");
  }
  ```

---

## 🛠️ **Test-Typen im Detail**

### **Backend (Quarkus + JUnit 5 + RestAssured)**

#### **1. Integration Tests (REST API)**
```java
@QuarkusTest
@TestHTTPEndpoint(LeadResource.class)
class LeadResourceTest {

  @Test
  @TestSecurity(user = "user1", roles = {"ADMIN"})
  @DisplayName("ADMIN can stop the clock")
  void testStopClockWithAdminRole() {
    Long leadId = createTestLead("user1");

    Map<String, Object> request = new HashMap<>();
    request.put("stopClock", true);
    request.put("stopReason", "Kunde im Urlaub");

    String etag = getEtagForLead(leadId);

    given()
      .contentType(ContentType.JSON)
      .header("If-Match", etag)
      .body(request)
      .when().patch("/" + leadId)
      .then()
      .statusCode(200)
      .body("clockStoppedAt", notNullValue());
  }
}
```

#### **2. Service Tests (Business Logic)**
```java
@QuarkusTest
class LeadScoringServiceTest {

  @Inject LeadScoringService scoringService;

  @Test
  @Transactional
  @DisplayName("High volume lead gets high score")
  void testHighValueLead() {
    Lead lead = createBaseLead();
    lead.estimatedVolume = new BigDecimal("50000");
    lead.employeeCount = 25;
    lead.lastActivityAt = LocalDateTime.now().minusDays(2);
    lead.businessType = "RESTAURANT";

    int score = scoringService.calculateScore(lead);

    assertTrue(score >= 70,
      "Lead with €50k + 25 employees should score ≥70");
  }
}
```

---

### **Frontend (Vitest + React Testing Library)**

#### **1. Component Tests (UI + Interactions)**
```typescript
describe('StopTheClockDialog - RBAC', () => {
  it('should show permission error for USER role', () => {
    mockUseAuth.mockReturnValue({
      hasRole: (role: string) => role === 'USER',
    });

    render(
      <StopTheClockDialog
        open={true}
        lead={mockLead}
        onClose={mockOnClose}
        onSuccess={mockOnSuccess}
      />
    );

    expect(screen.getByText('Keine Berechtigung')).toBeInTheDocument();
    expect(screen.getByText(/Nur Administratoren und Manager/)).toBeInTheDocument();
    expect(screen.queryByLabelText('Grund für Pause')).not.toBeInTheDocument();
  });

  it('should show pause form for ADMIN role', () => {
    mockUseAuth.mockReturnValue({
      hasRole: (role: string) => role === 'ADMIN',
    });

    render(
      <StopTheClockDialog
        open={true}
        lead={mockLead}
        onClose={mockOnClose}
        onSuccess={mockOnSuccess}
      />
    );

    expect(screen.getByText('Schutzfrist pausieren')).toBeInTheDocument();
    expect(screen.getByLabelText('Grund für Pause')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Pausieren' })).toBeEnabled();
  });
});
```

---

## 📋 **Test-Checklist: Neue Features**

Wenn du ein neues Feature implementierst, stelle dir diese Fragen:

### ✅ **Backend**
- [ ] Habe ich Business-Logic-Tests für die Kernfunktion?
- [ ] Habe ich Integration-Tests für alle API-Endpoints?
- [ ] Habe ich RBAC-Tests für alle Rollen (ADMIN, MANAGER, USER)?
- [ ] Habe ich Edge-Case-Tests (NULL, negative Werte, leere Strings)?
- [ ] Habe ich DTO-Completeness-Tests für neue Felder?
- [ ] Habe ich Optimistic-Locking-Tests (ETag, If-Match)?

### ✅ **Frontend**
- [ ] Habe ich Component-Tests für alle UI-Elemente?
- [ ] Habe ich RBAC-Tests für verschiedene User-Rollen?
- [ ] Habe ich Interaction-Tests (Click, Input, Submit)?
- [ ] Habe ich Validation-Tests (Required Fields, Formats)?
- [ ] Habe ich Error-Handling-Tests (API-Fehler, Network-Fehler)?

---

## 🚀 **Commands**

### **Backend Tests ausführen**
```bash
# Alle Tests
./mvnw test

# Einzelne Testklasse
./mvnw test -Dtest=LeadScoringServiceTest

# Einzelner Test
./mvnw test -Dtest=LeadScoringServiceTest#testHighValueLead

# Mit Coverage
./mvnw verify
# Report: backend/target/site/jacoco/index.html
```

### **Frontend Tests ausführen**
```bash
# Alle Tests
npm test

# Watch-Mode (interaktiv)
npm test

# Einzelne Datei
npm test -- StopTheClockDialog.test.tsx

# Mit Coverage
npm run test:coverage
# Report: frontend/coverage/index.html
```

---

## 📈 **Coverage-Ziele**

### **Minimum Requirements:**
- **Backend:** ≥80% Line Coverage, ≥75% Branch Coverage
- **Frontend:** ≥80% Statement Coverage, ≥70% Branch Coverage
- **Kritische Module:** ≥90% Coverage (z.B. LeadProtectionService, LeadScoringService)

### **ABER:** Coverage ist kein Selbstzweck!
- 100% Coverage ≠ Bug-freier Code
- Schlechte Tests mit 100% Coverage sind nutzlos
- Lieber 70% Coverage mit guten Tests als 95% mit sinnlosen Tests

---

## 🔬 **Test-Debugging**

### **Backend-Test failed?**
```bash
# 1. Logs ansehen
./mvnw test -Dtest=LeadScoringServiceTest -X

# 2. Einzelnen Test debuggen
./mvnw test -Dtest=LeadScoringServiceTest#testHighValueLead -Dmaven.surefire.debug

# 3. Surefire-Report ansehen
cat backend/target/surefire-reports/LeadScoringServiceTest.txt
```

### **Frontend-Test failed?**
```bash
# 1. Watch-Mode mit UI
npm test

# 2. Debug-Output
npm test -- --reporter=verbose

# 3. Einzelner Test mit Debug
npm test -- StopTheClockDialog.test.tsx --reporter=verbose
```

---

## 🌱 **DEV-SEED: Testdaten für lokale Entwicklung**

### **Was ist DEV-SEED?**

DEV-SEED ist eine **separate Migration-Strategie** für realistische Testdaten in der lokalen Entwicklungsumgebung.

**Zweck:** Production-ähnliche Daten zum manuellen Testen im Browser (NICHT für automatisierte Tests!).

### **Wann was verwenden?**

| Use Case | Tool | Beispiel |
|----------|------|----------|
| **Automatisierte Tests (CI)** | TestDataBuilder | `@QuarkusTest` mit `CustomerTestDataFactory.builder()` |
| **Lokale Entwicklung (Browser)** | DEV-SEED Migrations | V90001 (5 Customers), V90002 (10 Leads) |
| **Production** | Echte Daten | - |

### **TestDataBuilder vs. DEV-SEED**

**TestDataBuilder (für @QuarkusTest):**
```java
@QuarkusTest
class LeadScoringServiceTest {

  @Test
  @Transactional
  void testHighValueLead() {
    // Programmatisch erstellen
    Lead lead = LeadTestDataFactory.builder()
      .asQualifiedLead()
      .withScore(75)
      .buildAndPersist(leadRepository);

    // Test durchführen
    int score = leadScoringService.calculateScore(lead);
    assertTrue(score >= 70);
  }
}
```

**DEV-SEED (für lokale Entwicklung):**
```sql
-- V90001__seed_dev_customers_complete.sql
-- 5 realistische Customers für manuelles Testen

INSERT INTO customers (id, company_name, business_type, ...)
VALUES
  (90001, 'Fresh Hotel GmbH', 'HOTEL', ...),
  (90002, 'Catering Excellence AG', 'CATERING', ...),
  ...
```

### **DEV-SEED Daten verwenden**

```bash
# 1. Automatisch geladen bei lokalem Dev-Server
cd backend
./mvnw quarkus:dev

# 2. Im Browser: http://localhost:5173
# → Customers: 90001-90005 sichtbar
# → Leads: 90001-90010 sichtbar

# 3. Datenbank neu aufsetzen (inklusive DEV-SEED)
PGPASSWORD=freshplan123 psql -h localhost -U freshplan_user -d freshplan_db \
  -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
./mvnw flyway:migrate
```

### **Vorhandene DEV-SEED Daten**

**V90001 - 5 Customers:**
- 90001: Fresh Hotel GmbH (Berlin, Hotel, 200 MA)
- 90002: Catering Excellence AG (München, Catering, 150 MA)
- 90003: Campus Gastro Service (Freiburg, Betriebskantine, 500 MA)
- 90004: Restaurant Bella Vista (Hamburg, Restaurant, 50 MA)
- 90005: Bäckerei Müller KG (Dresden, Bäckerei, 80 MA)

**V90002 - 10 Leads:**
- IDs 90001-90010 (verschiedene Stati, Score-Range 21-59)
- Hot Leads: 90003 (Score 59), 90007 (Score 57)
- Edge Cases: PreClaim (90006), Grace Period (90005), LOST (90004)

### **Dokumentation & Referenzen**

- [DEV-SEED README](../../backend/src/main/resources/db/dev-seed/README.md) - Vollständige Dokumentation
- [MIGRATIONS.md](../MIGRATIONS.md) - V90000-V99999 Range
- [DEV_SEED_INFRASTRUCTURE_SUMMARY.md](../features-neu/00_infrastruktur/migrationen/artefakte/DEV_SEED_INFRASTRUCTURE_SUMMARY.md) - Implementation Details

### **Best Practices**

✅ **DO:**
- DEV-SEED für manuelles Testing im Browser verwenden
- TestDataBuilder für automatisierte Tests verwenden
- IDs 90000+ für DEV-SEED Entities reservieren

❌ **DON'T:**
- DEV-SEED Daten in @QuarkusTest referenzieren (flaky tests!)
- Production-IDs in DEV-SEED verwenden
- DEV-SEED Migrations in Production deployen (wird automatisch übersprungen)

---

## 📚 **Weitere Ressourcen**

- **Backend:** [Quarkus Testing Guide](https://quarkus.io/guides/getting-started-testing)
- **Frontend:** [Vitest Docs](https://vitest.dev/guide/) + [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/)
- **Coverage:** `/docs/COVERAGE_GUIDE.md`
- **Master Plan:** `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`

---

**Letztes Update:** Sprint 2.1.6.2 - DEV-SEED Infrastructure (2025-10-13)
**Test-Count:** 103 Tests (43 Backend + 60 Frontend)
**Coverage:** Backend 85%, Frontend 82%
**DEV-SEED:** V90001 (5 Customers), V90002 (10 Leads + 21 Contacts + 21 Activities)
