# Testing Guide - FreshPlan Sales Tool

**Stand:** 2025-12-05
**Sprint:** Sprint 2.1.8 - Self-Service Lead-Import
**Speicherort:** `docs/planung/grundlagen/testing_guide.md`

> **💡 Hinweis:** Für Coverage-Tools, CI/CD Setup & Monitoring siehe: `TESTING_INFRASTRUCTURE.md`

---

## 📑 Inhaltsverzeichnis

- [🎯 Wichtigster Grundsatz](#-wichtigster-grundsatz-tests-sind-kein-selbstzweck)
- [🐳 3-Stage CI Pipeline](#-3-stage-ci-pipeline) ⭐ NEU!
- [🔬 E2E-Tests gegen echte Datenbank](#-e2e-tests-gegen-echte-datenbank) ⭐ NEU!
  - [🔐 Security/Auth für E2E-Tests (Dev-Mode)](#-securityauth-für-e2e-tests-dev-mode) ⭐ NEU!
- [📊 Test-Strategie](#-test-strategie-was-soll-getestet-werden)
- [🔍 Test-Gap-Analyse](#-test-gap-analyse-warum-fanden-tests-bugs-nicht)
- [🛠️ Test-Typen im Detail](#️-test-typen-im-detail)
- [🧪 Container/Presentational Pattern](#-containerpresentational-pattern-für-testbarkeit) ⭐ NEU!
- [🏭 TestDataFactory Pattern](#-testdatafactory-pattern)
- [🌱 DEV-SEED](#-dev-seed-testdaten-für-lokale-entwicklung)
- [📋 Test-Checklist](#-test-checklist-neue-features)
- [🚀 Commands](#-commands)
- [📈 Coverage-Ziele](#-coverage-ziele)
- [🔬 Test-Debugging](#-test-debugging)
- [📚 Weitere Ressourcen](#-weitere-ressourcen)

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

## 🐳 **3-Stage CI Pipeline**

> **Eingeführt in PR #150** (2025-11-30) - Kritische Business-Flows gegen echte Datenbank testen

### Die 3 Stages im Überblick

| Stage | Beschreibung | Dauer | Tools |
|-------|-------------|-------|-------|
| **Stage 1** | Unit Tests (Backend + Frontend) | ~5 min | JUnit 5, Vitest |
| **Stage 2** | UI Smoke Tests mit MSW | ~3 min | Playwright + MSW |
| **Stage 3** | Critical Path E2E gegen echte DB | ~10 min | Playwright + Docker Compose |

### Warum 3 Stages?

```
┌─────────────────────────────────────────────────────────────────┐
│  Stage 1: Unit Tests (schnell, isoliert)                       │
│  ├── Backend: 1826 Tests (JUnit + H2 in-memory)                │
│  └── Frontend: 1399 Tests (Vitest + MSW Mocks)                 │
│                                                                 │
│  → Findet: Logikfehler, Regressions, Type-Fehler               │
│  → Findet NICHT: DB-Constraints, RLS, Migrations               │
├─────────────────────────────────────────────────────────────────┤
│  Stage 2: UI Smoke Tests (optional)                            │
│  ├── Playwright mit MSW (Mock Service Worker)                  │
│  └── Testet UI-Flows ohne echtes Backend                       │
│                                                                 │
│  → Findet: UI-Bugs, Rendering-Probleme, Navigation             │
│  → Findet NICHT: API-Integration, Datenbank-Verhalten          │
├─────────────────────────────────────────────────────────────────┤
│  Stage 3: E2E gegen echte DB (kritisch!)                       │
│  ├── Docker Compose: PostgreSQL + Quarkus                      │
│  └── Playwright testet komplette Business-Flows                │
│                                                                 │
│  → Findet: DB-Constraints, RLS-Policies, Timezone-Bugs,        │
│            Flyway-Migrations, Race Conditions                  │
└─────────────────────────────────────────────────────────────────┘
```

### Stage 3: Kritische Business-Flows (PFLICHT)

Diese Flows MÜSSEN gegen echte Datenbank getestet werden:

1. **Lead → Opportunity → Customer Conversion** (`lead-conversion-flow.spec.ts`)
   - Lead erstellen → Qualifizieren → Zu Kunde konvertieren
   - Testet: Stage-Transitions, Duplicate-Detection, RLS

2. **Customer Onboarding Wizard** (`customer-onboarding.spec.ts`)
   - Multi-Step-Wizard durchlaufen
   - Testet: Form-Validation, API-Integration, Xentral-Sync

3. **Validation & Error Handling** (`validation-flow.spec.ts`)
   - Invalid Stage Transitions (z.B. LEAD → CLOSED ohne Qualifikation)
   - Duplicate Customer Detection (gleiche Email/Firmenname)
   - Hierarchy Validation (FILIALE kann keine Sub-Branches haben)

---

## 🔬 **E2E-Tests gegen echte Datenbank**

### Warum echte DB statt Mocks?

**Mocks fangen NICHT:**

| Problem | Beispiel | Mock findet? | Echte DB findet? |
|---------|----------|--------------|------------------|
| DB Constraints | `UNIQUE(email)` verletzt | ❌ | ✅ |
| CHECK Constraints | `registered_at <= NOW()` | ❌ | ✅ |
| RLS Policies | User sieht fremde Leads | ❌ | ✅ |
| Flyway Migrations | Spalte fehlt nach Rename | ❌ | ✅ |
| Timezone-Bugs | JVM UTC vs. PostgreSQL local | ❌ | ✅ |
| Trigger/Events | NOTIFY/LISTEN funktioniert nicht | ❌ | ✅ |
| N+1 Queries | Performance-Problem | ❌ | ✅ |

### Self-Contained Test Pattern

> **Goldene Regel:** Jeder Test erstellt seine eigenen Daten mit UUID-Präfix

```typescript
// ✅ RICHTIG: Self-Contained Test
test('Lead → Customer Conversion', async ({ page }) => {
  // Unique ID für diesen Testlauf
  const uniqueId = crypto.randomUUID().slice(0, 8);
  const companyName = `E2E-Test-${uniqueId}`;

  // 1. Lead erstellen
  await page.goto('/leads/new');
  await page.fill('[name="companyName"]', companyName);
  await page.click('button[type="submit"]');

  // 2. Lead qualifizieren
  await page.click('[data-testid="qualify-button"]');
  await page.fill('[name="contactPerson"]', 'Max Mustermann');
  await page.click('button[type="submit"]');

  // 3. Zu Kunde konvertieren
  await page.click('[data-testid="convert-button"]');
  await page.waitForURL('/customers/*');

  // 4. Assert: Kunde existiert
  await expect(page.locator('h1')).toContainText(companyName);
});
```

```typescript
// ❌ FALSCH: Abhängig von DEV-SEED Daten
test('Bad: Uses DEV-SEED data', async ({ page }) => {
  // NIEMALS DEV-SEED IDs in E2E-Tests!
  await page.goto('/leads/90001');  // ❌ Flaky! Existiert nicht in CI

  // Stattdessen: Eigene Daten erstellen!
});
```

### Timezone-Konfiguration (KRITISCH)

> **Problem aus PR #150:** `registered_at <= NOW()` Check schlug fehl wegen JVM/DB Timezone-Differenz

**Lösung: UTC überall**

```properties
# application.properties
quarkus.hibernate-orm.jdbc.timezone=UTC
```

```typescript
// playwright.config.ts
export default defineConfig({
  use: {
    timezoneId: 'UTC',
  },
});
```

```yaml
# docker-compose.yml
services:
  postgres:
    environment:
      - TZ=UTC
```

### Rate-Limiting Awareness

```typescript
// E2E Tests haben max. 26 Requests pro Flow
// Limit: 50 writes/min, 100 reads/min

// ✅ Bei vielen Tests: Zwischen Flows warten
afterEach(async () => {
  if (process.env.CI) {
    await new Promise(r => setTimeout(r, 500)); // 500ms Pause
  }
});
```

### Docker Compose Setup für Stage 3

```yaml
# e2e/docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: freshplan_test
      POSTGRES_USER: test
      POSTGRES_PASSWORD: test
      TZ: UTC
    ports:
      - "5433:5432"  # Anderer Port als lokal!
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U test"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: ../backend
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://postgres:5432/freshplan_test
      QUARKUS_DATASOURCE_USERNAME: test
      QUARKUS_DATASOURCE_PASSWORD: test
      QUARKUS_HIBERNATE_ORM_JDBC_TIMEZONE: UTC
    ports:
      - "8081:8080"
```

### Flaky Test Prevention

| Problem | Lösung |
|---------|--------|
| Tests abhängig von Reihenfolge | Self-Contained: Jeder Test erstellt eigene Daten |
| Race Conditions | `await page.waitForResponse()` statt `sleep()` |
| Stale Data | Unique IDs pro Testlauf (UUID) |
| Timezone-Differenzen | UTC überall (JVM, PostgreSQL, Playwright) |
| DEV-SEED Abhängigkeit | NIEMALS DEV-SEED IDs in Tests referenzieren |

### 🔐 Security/Auth für E2E-Tests (Dev-Mode)

> **WICHTIG:** E2E-Tests laufen im Dev-Mode ohne echte Keycloak-Authentifizierung.
> Das Backend verwendet einen Fallback-Mechanismus für User-ID und Rollen.

#### Dev-Mode Auth-Bypass Pattern

Im Dev-Mode (`quarkus.profile=dev`) ist Keycloak deaktiviert:
- `SecurityIdentity.getPrincipal()` gibt `null` zurück
- `SecurityIdentity.hasRole()` gibt `false` zurück

**Lösung: Fallback-Pattern in jedem Resource:**

```java
@Path("/api/resource")
@RolesAllowed({"USER", "MANAGER", "ADMIN"})  // ⚠️ IMMER ohne ROLE_ Prefix!
public class MyResource {

  @Inject SecurityIdentity securityIdentity;

  @ConfigProperty(name = "app.dev.fallback-user-id", defaultValue = "dev-admin-001")
  String fallbackUserId;

  /**
   * Get current user ID with dev mode fallback.
   * In dev mode, auth is disabled and SecurityContext returns null.
   */
  private String getCurrentUserId() {
    if (securityIdentity.getPrincipal() != null
        && securityIdentity.getPrincipal().getName() != null
        && !securityIdentity.getPrincipal().getName().isBlank()) {
      return securityIdentity.getPrincipal().getName();
    }
    return fallbackUserId; // Fallback for dev mode
  }

  /**
   * Get current user role with dev mode fallback.
   * Checks both UPPER and lowercase role names for flexibility.
   */
  private UserRole getCurrentUserRole() {
    if (securityIdentity.hasRole("ADMIN") || securityIdentity.hasRole("admin")) {
      return UserRole.ADMIN;
    } else if (securityIdentity.hasRole("MANAGER") || securityIdentity.hasRole("manager")) {
      return UserRole.MANAGER;
    } else if (securityIdentity.hasRole("USER") || securityIdentity.hasRole("sales")) {
      return UserRole.SALES;
    }
    // Default: ADMIN in dev mode for full access
    return UserRole.ADMIN;
  }
}
```

#### Konsistente Rollen-Benennung (KRITISCH!)

| ✅ RICHTIG | ❌ FALSCH |
|-----------|----------|
| `@RolesAllowed({"USER", "MANAGER", "ADMIN"})` | `@RolesAllowed({"ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN"})` |
| `securityIdentity.hasRole("ADMIN")` | `securityIdentity.hasRole("ROLE_ADMIN")` |

**Problem:** Verschiedene Konventionen führen zu 401 Unauthorized in E2E-Tests!

**Prüfe bei neuen Resources:**
1. `@RolesAllowed` verwendet **keine** `ROLE_` Prefixe
2. `hasRole()` prüft **beide** Varianten (upper + lower case)
3. Fallback auf `ADMIN` im Dev-Mode für maximale Test-Abdeckung

#### E2E-Tests ohne Browser-UI (Pure API)

Für maximale CI-Stabilität verwenden wir Pure API Tests:

```typescript
// e2e/helpers/api-helpers.ts
export const API_BASE = 'http://localhost:8081';

export async function getImportQuota(request: APIRequestContext): Promise<QuotaInfoResponse> {
  const response = await request.get(`${API_BASE}/api/leads/import/quota`);
  // Dev-Mode: Keine Auth-Header nötig, Backend verwendet Fallback
  expect(response.ok()).toBe(true);
  return response.json();
}
```

**Vorteile:**
- Keine Browser-Interaktionen → schneller, stabiler
- Keine Login-UI → keine Keycloak-Abhängigkeit
- Backend-Fallback → Tests funktionieren im Dev-Mode

#### Referenz-Implementierung

Siehe: `SelfServiceImportResource.java` (Sprint 2.1.8)
- Vollständiges Fallback-Pattern für userId und userRole
- Konsistente `@RolesAllowed` ohne `ROLE_` Prefix
- `@ConfigProperty` für konfigurierbaren Fallback-User

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

## 🧪 **Container/Presentational Pattern für Testbarkeit**

> **Eingeführt in Sprint 2.1.8** (2025-12-05) - Lösung für schwer testbare Komponenten mit useEffect-API-Calls

### Das Problem

Komponenten mit `useEffect`-API-Calls sind schwer testbar:

```typescript
// ❌ SCHLECHT: API-Call in der Komponente
function PreviewStep({ uploadId, mapping, onComplete }) {
  const [data, setData] = useState(null);

  useEffect(() => {
    async function load() {
      const result = await createPreview(uploadId, mapping);  // API-Call!
      setData(result);
    }
    load();
  }, [uploadId, mapping]);

  return <div>{data?.validRows} gültige Zeilen</div>;
}
```

**Probleme beim Testen:**
- MSW-Mocking ist instabil (Timing-Issues)
- Tests brauchen lange Timeouts (10+ Sekunden)
- Race Conditions zwischen Render und API-Response
- Coverage bleibt niedrig (~47%)

### Die Lösung: Container/Presentational Pattern

**Architektur-Prinzip: "Lift State Up"**

```
┌─────────────────────────────────────┐
│ Container (Parent)                  │
│   └── API-Call + State Management   │
│         └── Daten als Props         │
└─────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────┐
│ Presentational (Child)              │
│   └── Nur Props → UI                │  ← Einfach testbar!
└─────────────────────────────────────┘
```

```typescript
// ✅ GUT: API-Call im Parent (Container)
function LeadImportWizard() {
  const [previewData, setPreviewData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleMappingComplete = async (mapping) => {
    setIsLoading(true);
    const data = await createPreview(uploadId, mapping);
    setPreviewData(data);
    setIsLoading(false);
  };

  return (
    <PreviewStep
      previewData={previewData}
      isLoading={isLoading}
      onContinue={handleContinue}
    />
  );
}

// ✅ GUT: Presentational Component (nur Props)
function PreviewStep({ previewData, isLoading, onContinue }) {
  if (isLoading) return <Loading />;
  return <div>{previewData?.validRows} gültige Zeilen</div>;
}
```

### Vorteile für Tests

```typescript
// Test ist jetzt trivial - keine API-Calls, keine Mocks!
describe('PreviewStep', () => {
  it('zeigt Validierungsergebnis', () => {
    const mockData = {
      validation: { validRows: 85, errorRows: 5 },
      quotaCheck: { approved: true }
    };

    render(<PreviewStep previewData={mockData} isLoading={false} />);

    expect(screen.getByText('85')).toBeInTheDocument();
  });
});
```

**Ergebnis:** Coverage von 47% → 97%!

### Wann dieses Pattern anwenden?

| Situation | Pattern anwenden? |
|-----------|-------------------|
| Komponente hat `useEffect` mit API-Call | ✅ JA |
| Komponente rendert nur Props | ❌ NEIN (schon gut) |
| Komponente verwendet React Query Hook | ⚠️ PRÜFEN (meist ok) |
| Multi-Step Wizard mit API zwischen Schritten | ✅ JA |

### Best Practices

1. **Loading-State als Prop**: Nicht intern verwalten, vom Parent übergeben
2. **Error-Handling im Parent**: Fehler im Container abfangen, nicht im Child
3. **Callback-Props für Interaktionen**: `onContinue`, `onBack`, nicht `navigate()`
4. **Separate Loading-Komponente**: Für bessere Testbarkeit exportieren

### Referenz-Implementierung

**Dateien:**
- `frontend/src/features/leads/components/import/PreviewStep.tsx` - Presentational
- `frontend/src/features/leads/components/import/LeadImportWizard.tsx` - Container
- `frontend/src/features/leads/components/import/__tests__/PreviewStep.test.tsx` - 22 Tests

---

## 🏭 **TestDataFactory Pattern**

### **Übersicht: SEED vs Faker vs TestDataFactories**

| Use Case | Tool | Wann verwenden? |
|----------|------|-----------------|
| **Automatisierte Tests (CI)** | TestDataFactory | Immer in `@QuarkusTest` - Thread-safe, isoliert, schnell |
| **Deterministische Tests** | Seeded Builder | Tests die EXAKT gleiche Daten brauchen: `builder(42L)` |
| **Lokale Entwicklung (Browser)** | DEV-SEED Migrations | Manuelles Testen im Browser (V90001, V90002) |
| **Production** | Echte Daten | - |

### **Warum TestDataFactory?**

**Problem:** Hardcoded Test Arrays (altmodisch):
```java
// ❌ ALT: Hardcoded, unflexibel, nicht realistisch
Customer customer = new Customer();
customer.setCompanyName("Test Company GmbH");
customer.setCustomerNumber("TST-12345");
customer.setStatus(CustomerStatus.LEAD);
```

**Lösung:** TestDataFactory mit RealisticDataGenerator (Track 2C):
```java
// ✅ NEU: Builder Pattern + Faker Integration
Customer customer = CustomerTestDataFactory.builder()
  .build(); // Generiert realistische deutsche Firma!

// Result: "[TEST] Müller Catering GmbH", "TST-8A3F2B1C-00001", Hamburg, 80331
```

---

### **RealisticDataGenerator**

**Zweck:** Realistische deutsche Testdaten mit Datafaker (net.datafaker:datafaker:2.0.2)

**Features:**
- 50+ Generator-Methoden für deutschen Markt
- Email-Normalisierung (ä→ae, ö→oe, ü→ue, ß→ss)
- Seeded Random für deterministische Tests
- Thread-safe ThreadLocal Pattern

#### **Beispiele:**

```java
// 1. Unseed (zufällig)
RealisticDataGenerator gen = new RealisticDataGenerator();

String company = gen.germanCateringCompanyName();
// → "Frische Küche Müller GmbH"

String person = gen.germanFullName();
// → "Anna Schmidt"

String email = gen.email("Max", "Müller", "example.com");
// → "max.mueller@example.com" (Umlaute normalisiert!)

String phone = gen.germanPhoneNumber();
// → "+49 089 12345678"

String city = gen.germanCity();
// → "München"

String plz = gen.germanPostalCode();
// → "80331"
```

```java
// 2. Seeded (deterministische Tests)
RealisticDataGenerator gen1 = new RealisticDataGenerator(42L);
RealisticDataGenerator gen2 = new RealisticDataGenerator(42L);

assertThat(gen1.germanCompanyName())
  .isEqualTo(gen2.germanCompanyName()); // ✅ Gleicher Seed = gleiche Daten
```

**Best Practices:**
- ✅ Unseed für normale Tests (Variety)
- ✅ Seeded für Assertions auf exakte Werte
- ✅ ThreadLocal in Factories (Thread-Safety)

---

### **CustomerTestDataFactory**

**Zweck:** Customer-Entity-Testdaten mit realistischen deutschen Firmennamen

#### **Grundlegende Verwendung:**

```java
@QuarkusTest
class CustomerServiceTest {

  @Inject CustomerRepository customerRepository;

  @Test
  @Transactional
  void testCreateCustomer() {
    // Mit Defaults (realistische deutsche Firma)
    Customer customer = CustomerTestDataFactory.builder().build();

    // Result:
    // - companyName: "[TEST] Müller Catering GmbH"
    // - customerNumber: "TST-8A3F2B1C-00001"
    // - city: "München"
    // - postalCode: "80331"
    // - riskScore: 2 (Low-Risk Default)
    // - isTestData: true (IMMER true in Tests!)
  }
}
```

#### **Custom Values:**

```java
Customer customer = CustomerTestDataFactory.builder()
  .withCompanyName("Custom Catering GmbH")
  .withCity("Berlin")
  .withPostalCode("10115")
  .withRiskScore(5)
  .build();
```

#### **Seeded Builder (deterministische Tests):**

```java
@Test
void testWithSeededData() {
  // Gleicher Seed = gleiche Daten
  Customer c1 = CustomerTestDataFactory.builder(42L).build();
  Customer c2 = CustomerTestDataFactory.builder(42L).build();

  assertThat(c1.getCompanyName()).isEqualTo(c2.getCompanyName());
}
```

#### **Convenience Methods:**

```java
// Minimal Customer (nur Required Fields)
Customer minimal = CustomerTestDataFactory.builder().buildMinimal();
// → "Test Company GmbH", CustomerStatus.LEAD
```

#### **Persistence (Integration Tests):**

```java
@QuarkusTest
class CustomerRepositoryTest {

  @Inject CustomerRepository customerRepository;

  @Test
  @Transactional
  void testFindByName() {
    // Build and persist in one step
    Customer customer = CustomerTestDataFactory.builder()
      .withCompanyName("Test Catering GmbH")
      .buildAndPersist(customerRepository);

    // Now searchable in DB
    List<Customer> results = customerRepository.findByName("Test Catering");
    assertThat(results).hasSize(1);
  }
}
```

---

### **LeadTestDataFactory**

**Zweck:** Lead-Entity-Testdaten mit realistischen deutschen Catering-Firmennamen

#### **Grundlegende Verwendung:**

```java
@QuarkusTest
class LeadServiceTest {

  @Test
  @Transactional
  void testCreateLead() {
    // Mit Defaults (realistische Catering-Firma)
    Lead lead = LeadTestDataFactory.builder().build();

    // Result:
    // - companyName: "[TEST] Frische Küche Schmidt GmbH"
    // - contactPerson: "Anna Müller"
    // - email: "anna.mueller@example.com" (aus contactPerson generiert!)
    // - phone: "+49 089 1234567"
    // - city: "München"
    // - postalCode: "80331"
    // - employeeCount: 25 (realistisch aus Faker)
    // - status: LeadStatus.REGISTERED
    // - stage: LeadStage.REGISTRIERUNG
  }
}
```

#### **Convenience Methods (Presets):**

```java
// 1. Pre-Claim Lead (Vormerkung)
Lead preClaimLead = LeadTestDataFactory.builder().buildMinimal();
// → status = REGISTERED
// → stage = VORMERKUNG
// → firstContactDocumentedAt = NULL (10-day window!)

// 2. Qualified Lead (Registrierung)
Lead qualifiedLead = LeadTestDataFactory.builder().buildQualified();
// → status = REGISTERED
// → stage = REGISTRIERUNG
// → firstContactDocumentedAt = now() - 5 days
```

#### **Business Fields:**

```java
Lead lead = LeadTestDataFactory.builder()
  .withBusinessType(BusinessType.RESTAURANT)
  .withKitchenSize(KitchenSize.GROSS)
  .withEmployeeCount(50)
  .withEstimatedVolume(BigDecimal.valueOf(100000))
  .withDealSize(DealSize.LARGE)
  .withBudgetConfirmed(true)
  .build();
```

#### **Scoring & Pain Points:**

```java
Lead lead = LeadTestDataFactory.builder()
  .withPainStaffShortage(true)
  .withUrgencyLevel(UrgencyLevel.HIGH)
  .withRelationshipStatus(RelationshipStatus.TRUSTED)
  .build();
```

---

### **LeadActivityTestDataFactory**

**Zweck:** LeadActivity-Testdaten mit realistischen Notizen

#### **Required Pattern (forLead):**

```java
@QuarkusTest
class LeadActivityServiceTest {

  @Test
  @Transactional
  void testCreateActivity() {
    // Lead REQUIRED!
    Lead lead = LeadTestDataFactory.builder().build();

    // Activity erstellen
    LeadActivity activity = LeadActivityTestDataFactory.builder()
      .forLead(lead) // ❗ PFLICHT - wirft Exception wenn vergessen!
      .withActivityType(ActivityType.CALL)
      .withDescription("Telefonat mit Kunden")
      .build();

    // Auto-Set: isMeaningfulContact + resetsTimer basierend auf ActivityType
    assertThat(activity.isMeaningfulContact).isTrue(); // CALL is meaningful
    assertThat(activity.resetsTimer).isTrue();
  }
}
```

#### **Convenience Methods:**

```java
Lead lead = LeadTestDataFactory.builder().build();

// 1. Phone Call (meaningful contact)
LeadActivity call = LeadActivityTestDataFactory.builder()
  .forLead(lead)
  .buildCall();
// → activityType = CALL
// → isMeaningfulContact = true
// → resetsTimer = true
// → outcome = SUCCESSFUL
// → description = "Telefonat mit [contactPerson]"

// 2. Email
LeadActivity email = LeadActivityTestDataFactory.builder()
  .forLead(lead)
  .buildEmail();
// → activityType = EMAIL
// → outcome = INFO_SENT
// → description = "E-Mail versendet an [email]"

// 3. Note (non-meaningful)
LeadActivity note = LeadActivityTestDataFactory.builder()
  .forLead(lead)
  .buildNote();
// → activityType = NOTE
// → isMeaningfulContact = false
// → description = [Realistische Notiz aus Faker]

// 4. Meeting
LeadActivity meeting = LeadActivityTestDataFactory.builder()
  .forLead(lead)
  .buildMeeting();
// → activityType = MEETING
// → isMeaningfulContact = true
// → outcome = SUCCESSFUL

// 5. First Contact (counts as progress!)
LeadActivity firstContact = LeadActivityTestDataFactory.builder()
  .forLead(lead)
  .buildFirstContact();
// → activityType = FIRST_CONTACT_DOCUMENTED
// → isMeaningfulContact = true
// → countsAsProgress = true ✅
```

#### **Activity Outcomes (Sprint 2.1.7 Issue #126):**

```java
LeadActivity activity = LeadActivityTestDataFactory.builder()
  .forLead(lead)
  .withActivityType(ActivityType.CALL)
  .withOutcome(ActivityOutcome.NO_ANSWER)
  .withNextAction("Rückruf in 2 Tagen")
  .withNextActionDate(LocalDate.now().plusDays(2))
  .build();
```

---

### **Best Practices: TestDataFactory**

#### ✅ **DO:**

```java
// 1. Immer TestDataFactory in @QuarkusTest verwenden
@QuarkusTest
class MyTest {
  @Test
  @Transactional
  void testSomething() {
    Customer c = CustomerTestDataFactory.builder().build();
    Lead l = LeadTestDataFactory.builder().build();
  }
}

// 2. Seeded Builder für deterministische Assertions
@Test
void testExactValue() {
  RealisticDataGenerator gen = new RealisticDataGenerator(42L);
  String expected = gen.germanCompanyName();

  // Jetzt können wir exakten Wert asserten
  assertThat(expected).isEqualTo("Müller Catering GmbH");
}

// 3. Convenience Methods nutzen für häufige Patterns
Lead preClaimLead = LeadTestDataFactory.builder().buildMinimal();
Lead qualifiedLead = LeadTestDataFactory.builder().buildQualified();
```

#### ❌ **DON'T:**

```java
// 1. NICHT DEV-SEED IDs in Tests referenzieren (flaky!)
@Test
void testBadPractice() {
  Lead lead = Lead.findById(90001L); // ❌ DEV-SEED ID - NICHT IN TESTS!
  // Problem: DEV-SEED nicht in CI verfügbar → Flaky Test!
}

// 2. NICHT hardcoded Test Arrays verwenden (altmodisch)
Customer[] customers = {
  new Customer("Test 1", "TST-001"),
  new Customer("Test 2", "TST-002")
}; // ❌ Unflexibel, nicht realistisch

// 3. NICHT manuell IDs setzen (Kollisionsgefahr!)
Customer c = new Customer();
c.setId(123L); // ❌ Kann mit anderen Tests kollidieren!
```

---

### **TestDataFactory Testing**

**Track 2C - Test Coverage:**

```bash
# Alle TestDataFactory Tests ausführen
cd backend
./mvnw test -Dtest="RealisticDataGeneratorTest,CustomerTestDataFactoryTest,LeadTestDataFactoryTest,LeadActivityTestDataFactoryTest"

# Result: 90/90 Tests GREEN
# - 25 RealisticDataGeneratorTest
# - 15 CustomerTestDataFactoryTest
# - 28 LeadTestDataFactoryTest
# - 22 LeadActivityTestDataFactoryTest
```

**Test-Dateien:**
- `backend/src/test/java/de/freshplan/test/builders/RealisticDataGenerator.java`
- `backend/src/test/java/de/freshplan/test/builders/RealisticDataGeneratorTest.java`
- `backend/src/test/java/de/freshplan/test/builders/CustomerTestDataFactory.java`
- `backend/src/test/java/de/freshplan/test/builders/CustomerTestDataFactoryTest.java`
- `backend/src/test/java/de/freshplan/test/builders/LeadTestDataFactory.java`
- `backend/src/test/java/de/freshplan/test/builders/LeadTestDataFactoryTest.java`
- `backend/src/test/java/de/freshplan/test/builders/LeadActivityTestDataFactory.java`
- `backend/src/test/java/de/freshplan/test/builders/LeadActivityTestDataFactoryTest.java`

---

## 📋 **Test-Checklist: Neue Features**

Wenn du ein neues Feature implementierst, stelle dir diese Fragen:

### ✅ **Stage 1: Backend Unit Tests**
- [ ] Habe ich Business-Logic-Tests für die Kernfunktion?
- [ ] Habe ich Integration-Tests für alle API-Endpoints?
- [ ] Habe ich RBAC-Tests für alle Rollen (ADMIN, MANAGER, USER)?
- [ ] Habe ich Edge-Case-Tests (NULL, negative Werte, leere Strings)?
- [ ] Habe ich DTO-Completeness-Tests für neue Felder?
- [ ] Habe ich Optimistic-Locking-Tests (ETag, If-Match)?

### ✅ **Stage 1: Frontend Unit Tests**
- [ ] Habe ich Component-Tests für alle UI-Elemente?
- [ ] Habe ich RBAC-Tests für verschiedene User-Rollen?
- [ ] Habe ich Interaction-Tests (Click, Input, Submit)?
- [ ] Habe ich Validation-Tests (Required Fields, Formats)?
- [ ] Habe ich Error-Handling-Tests (API-Fehler, Network-Fehler)?

### ✅ **Stage 3: E2E Tests (für kritische Features)**
- [ ] Ist dies ein **kritischer Business-Flow**? (Conversion, Wizard, Validation)
- [ ] Habe ich Self-Contained Tests mit UUID-Isolation?
- [ ] Teste ich gegen **echte Datenbank** (nicht Mocks)?
- [ ] Sind meine Tests **unabhängig von DEV-SEED Daten**?
- [ ] Habe ich Timezone-Handling berücksichtigt (UTC)?
- [ ] Respektiere ich Rate-Limits (max. 50 writes/min)?

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

### **Frontend Tests ausführen (Stage 1)**
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

### **E2E Tests ausführen (Stage 3)**
```bash
# 1. Docker Compose starten (PostgreSQL + Backend)
cd e2e
docker compose up -d

# 2. Warten bis Backend ready ist
until curl -s http://localhost:8081/q/health/ready; do sleep 2; done

# 3. E2E Tests ausführen
npx playwright test

# 4. Einzelner Test
npx playwright test lead-conversion-flow.spec.ts

# 5. Mit UI (Debug-Mode)
npx playwright test --ui

# 6. Aufräumen
docker compose down -v
```

### **Vollständige 3-Stage Pipeline (lokal)**
```bash
# Stage 1: Unit Tests
cd backend && ./mvnw test
cd frontend && npm test

# Stage 2: UI Smoke Tests (optional)
cd frontend && npm run test:e2e:msw

# Stage 3: E2E gegen echte DB
cd e2e && docker compose up -d
npx playwright test
docker compose down -v
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

| Use Case | Tool | Wann verwenden? | Beispiel |
|----------|------|-----------------|----------|
| **Automatisierte Tests (CI)** | TestDataFactory | Immer in `@QuarkusTest` | `LeadTestDataFactory.builder().build()` |
| **Deterministische Tests** | Seeded Builder | Exakte Wert-Assertions | `builder(42L).build()` |
| **Lokale Entwicklung (Browser)** | DEV-SEED Migrations | Manuelles UI-Testing | V90001 (5 Customers), V90002 (10 Leads) |
| **Production** | Echte Daten | - | - |

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
- **E2E:** [Playwright Docs](https://playwright.dev/docs/intro)
- **Coverage:** `/docs/COVERAGE_GUIDE.md`
- **Master Plan:** `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`
- **PR #150:** 3-Stage Pipeline Implementierung (2025-11-30)

---

**Letztes Update:** Sprint 2.1.8 - Self-Service Lead-Import (2025-12-05)

### Test-Statistiken

| Kategorie | Anzahl | Details |
|-----------|--------|---------|
| **Backend Unit Tests** | 1826 | JUnit 5 + RestAssured |
| **Frontend Unit Tests** | 1399 | Vitest + React Testing Library |
| **E2E Tests (Stage 3)** | 37 | Playwright + Docker Compose (inkl. Lead-Import) |
| **Gesamt** | 3262 | |

### Coverage-Ziele

| Layer | Minimum | Aktuell |
|-------|---------|---------|
| Backend | ≥80% | 87% |
| Frontend | ≥80% | 82% |
| Kritische Module | ≥90% | ✅ |

### Infrastruktur

- **3-Stage Pipeline:** ✅ Aktiv seit PR #150 (2025-11-30)
- **DEV-SEED:** V90001-V90004 (Customers, Leads, Users)
- **TestDataFactories:** Customer, Lead, LeadActivity, User
- **E2E Docker Compose:** PostgreSQL 15 + Quarkus
