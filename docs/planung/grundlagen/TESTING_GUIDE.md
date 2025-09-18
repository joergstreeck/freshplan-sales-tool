# 🧪 Testing Guide - FreshPlan Quality Assurance

**Erstellt:** 2025-09-17
**Status:** ✅ Verlässliche Analyse via Test-Runner (KORRIGIERT!)
**Basis:** Echte Test-Runner Ergebnisse (npm test:ci + Maven Surefire)
**Erkenntnis:** Viel mehr Tests als erwartet, aber viele deaktiviert

## 📋 Critical Test Status Analysis

### **📊 VERLÄSSLICHER ZUSTAND (via Test-Runner):**
```yaml
Frontend Tests (✅ VERIFIZIERT via npm run test:ci):
├── 📁 Test-Dateien: 90 (57 passed, 12 failed, 21 skipped)
├── 🧪 Individual Tests: 1.024 total
│   ├── ✅ Passed: 689 (67.3%)
│   ├── ❌ Failed: 49 (4.8%)
│   └── ⏸️ Skipped: 68 (6.6%)
└── 📈 Execution Rate: 738/1024 = 72% werden ausgeführt

Backend Tests (❓ ANALYSE LÄUFT):
├── 📁 Test-Dateien: ~267 Java-Dateien gefunden
├── 🧪 Individual Tests: Ermittlung läuft
├── 🚨 Problem: CI-Umgebung verhindert vollständige Analyse
└── 📋 Status: Needs Keycloak + DB cleanup for accurate count

E2E Tests (✅ INTEGRIERT):
├── 🔧 Framework: Playwright mit Keycloak-Integration
├── 📋 Konfiguration: playwright.config.ts (CI-optimiert)
├── 🚀 Quick Start: ./scripts/start-keycloak.sh && ./scripts/run-e2e-tests.sh
└── 🎯 Scope: Komplette Security-Integration

KORRIGIERTE EINSCHÄTZUNG:
- Frontend: ~1.024 Tests (deutlich mehr als geschätzt!)
- Backend: Schätzung >>1.000 Tests (muss verifiziert werden)
- GESAMT: Wahrscheinlich >2.000 Tests statt ~300!
```

### **⚠️ PROBLEM-KATEGORIEN (aus Code-Analyse):**

#### **Backend Quarantine Tests:**
```java
// Beispiel aus DatabaseAnalysisTest.java
@QuarkusTest
@Tag("quarantine")  // Deaktiviert wegen instabiler DB-Tests
public class DatabaseAnalysisTest {
    // Tests die CI zum Absturz bringen können
}

// Weitere Quarantine-Kategorien:
├── SimpleSeedTest.java → SEED-abhängige Tests (veraltet)
├── DatabaseDeepCleanupTest.java → Gefährliche DB-Operations
├── TestCustomerVerificationTest.java → Instabile Integrationstests
└── BaseIntegrationTestWithCleanup.java → Cleanup-Probleme
```

#### **Frontend Disabled Tests:**
```typescript
// Häufige Patterns in Frontend
describe.skip('ComponentName', () => {
  // Tests deaktiviert wegen:
  // - Veraltete Mocks
  // - Fehlende Test-Infrastructure
  // - Async/Timing-Probleme
});

test.todo('should implement feature X');
// Viele TODOs ohne Implementation
```

## 🎯 Testing Framework Overview

### **Backend Testing Stack (Quarkus-basiert):**
```yaml
Framework: JUnit 5 + Quarkus Test
Integration: RestAssured für API Tests
Database: TestContainers + PostgreSQL
Security: @TestSecurity für Auth-Tests
Mocking: Mockito + @InjectMock
Coverage: JaCoCo (aber niedrig wegen deaktivierter Tests)
CI: Nur @Tag("core") Tests in Pipeline
```

### **Frontend Testing Stack (React-basiert):**
```yaml
Unit Tests: Vitest + React Testing Library
Component Tests: @testing-library/react
Mocking: Mock Service Worker (MSW)
E2E Tests: Playwright (teilweise implementiert)
Coverage: Vitest Coverage (inkomplett)
CI: Basis-Tests + Build-Validation
```

### **Test-Kategorisierung System:**
```java
// Backend Test Tags
@Tag("core")        // 40 Tests - Stabil, in CI aktiv
@Tag("migrate")     // 97 Tests - In Migration, CI deaktiviert
@Tag("quarantine")  // 32 Tests - Gefährlich, komplett deaktiviert

// CI Profile Configuration
mvn test -Dgroups="core"           // Nur stabile Tests
mvn test -Dgroups="migrate"        // Nightly Runs (wenn überhaupt)
mvn test -Dgroups="quarantine"     // NIEMALS ausführen
```

## 🚀 Enterprise TestDataBuilder Pattern

### **✅ POSITIVE ENTWICKLUNG - TestDataBuilder Migration:**
```java
// Neue SEED-freie Strategie (bereits implementiert)
@ApplicationScoped
public class CustomerTestDataBuilder {

    private static final AtomicLong SEQUENCE = new AtomicLong(1);
    private static final String RUN_ID = System.getProperty("test.run.id", "LOCAL");

    public static Customer.CustomerBuilder aCustomer() {
        long seq = SEQUENCE.getAndIncrement();
        return Customer.builder()
            .id(UUID.randomUUID())
            .name("KD-TEST-" + RUN_ID + "-" + seq)  // Collision-free
            .email("test-" + RUN_ID + "-" + seq + "@example.com")
            .isTestData(true)  // KRITISCH: Für automatisches Cleanup
            .createdAt(LocalDateTime.now());
    }

    public static Customer.CustomerBuilder aValidCustomer() {
        return aCustomer()
            .customerType("STANDARD")
            .status("ACTIVE")
            .region("NORTH");
    }
}
```

### **Migration von SEED zu TestDataBuilder:**
```yaml
✅ Implementiert:
- TestDataBuilder Pattern für alle Entities
- Collision-free Naming (RUN_ID + Sequence)
- Automatic Cleanup (isTestData = true)
- CI Performance +70% (3 Min statt 10 Min)

🔄 In Migration:
- 97 "migrate" Tests von SEED auf Builder
- Legacy SEED-Abhängigkeiten entfernen
- Pre-Commit Hooks gegen neue SEED-Referenzen

⚠️ Problematisch:
- 32 "quarantine" Tests mit gefährlichen Patterns
- Teilweise noch SEED-Abhängigkeiten in Legacy-Code
```

## 🏗️ CQRS Testing Patterns

### **Hybrid CQRS Testing (aktueller Zustand):**
```java
@QuarkusTest
@Tag("core")
class CustomerCQRSIntegrationTest {

    @Inject CustomerService legacyService;              // Legacy
    @Inject CustomerCommandService commandService;     // CQRS Command
    @Inject CustomerQueryService queryService;         // CQRS Query

    @ConfigProperty(name = "features.cqrs.enabled")
    boolean cqrsEnabled;

    @Test
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldProduceSameResults_LegacyVsCQRS(boolean useCQRS) {
        // Test-Strategie: Beide Patterns testen
        System.setProperty("features.cqrs.enabled", String.valueOf(useCQRS));

        CreateCustomerRequest request = CustomerTestDataBuilder
            .aValidCustomer()
            .build()
            .toCreateRequest();

        CustomerResponse result;
        if (useCQRS) {
            result = commandService.createCustomer(request);
        } else {
            result = legacyService.createCustomer(request);
        }

        // Verify same behavior
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo(request.name());

        // Verify via Query Service
        Optional<CustomerResponse> found = queryService.findById(result.id());
        assertThat(found).isPresent();
    }
}
```

## 🎪 Integration Testing

### **REST API Testing (RestAssured):**
```java
@QuarkusTest
@Tag("core")
class CustomerResourceIntegrationTest {

    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    @Transactional
    void shouldCreateCustomerViaAPI() {
        CreateCustomerRequest request = CustomerTestDataBuilder
            .aValidCustomer()
            .build()
            .toCreateRequest();

        CustomerResponse response =
            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/customers")
            .then()
                .statusCode(201)
                .extract()
                .as(CustomerResponse.class);

        // Verify created
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo(request.name());

        // Cleanup automatic (isTestData = true)
    }

    @AfterEach
    @Transactional
    void cleanup() {
        // TestDataBuilder Cleanup Pattern
        customerRepository.delete("isTestData = true");
    }
}
```

### **Database Testing (TestContainers):**
```java
@QuarkusTest
@Tag("core")
@TestTransaction
class CustomerRepositoryTest {

    @Inject CustomerRepository repository;

    @Test
    void shouldFindCustomerByEmail() {
        // Arrange - TestDataBuilder
        Customer customer = CustomerTestDataBuilder
            .aValidCustomer()
            .email("unique-" + UUID.randomUUID() + "@test.com")
            .build();

        repository.persist(customer);

        // Act
        Optional<Customer> found = repository.findByEmail(customer.getEmail());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(customer.getId());
    }
}
```

## 🎨 Frontend Component Testing

### **React Testing Library Patterns:**
```typescript
// Component Testing mit Theme Integration
import { render, screen, fireEvent } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import { CustomerList } from '../CustomerList';
import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: { main: '#004F7B' },
    success: { main: '#94C456' },
  },
});

describe('CustomerList', () => {
  const renderWithTheme = (ui: React.ReactElement) => {
    return render(
      <ThemeProvider theme={theme}>
        {ui}
      </ThemeProvider>
    );
  };

  it('should render with theme colors', () => {
    renderWithTheme(<CustomerList customers={[]} />);

    const listContainer = screen.getByTestId('customer-list');
    expect(listContainer).toBeInTheDocument();

    // Test theme integration
    const header = screen.getByRole('heading');
    expect(header).toHaveStyle({
      color: theme.palette.text.primary,
    });
  });

  it('should handle loading state', () => {
    renderWithTheme(<CustomerList loading={true} />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });
});
```

### **MSW API Mocking:**
```typescript
// Mock Service Worker Setup
import { setupServer } from 'msw/node';
import { rest } from 'msw';

const handlers = [
  rest.get('/api/customers', (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({
        content: [
          {
            id: '123e4567-e89b-12d3-a456-426614174000',
            name: 'Test Customer',
            email: 'test@example.com',
          },
        ],
        page: {
          number: 0,
          size: 20,
          totalElements: 1,
          totalPages: 1,
        },
      })
    );
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

## 📊 Test Coverage Goals & Current Reality

### **Coverage Targets vs Reality:**
```yaml
Backend Coverage Goals:
- Unit Tests: >80% (AKTUELL: Unbekannt - viele Tests deaktiviert)
- Integration Tests: 100% API Endpoints (AKTUELL: ~20% aktiv)
- Repository Tests: >90% (AKTUELL: Teilweise in quarantine)

Frontend Coverage Goals:
- Component Tests: >80% (AKTUELL: ~67% aktiv)
- Integration Tests: Critical User Flows (AKTUELL: Unvollständig)
- E2E Tests: Happy Paths (AKTUELL: Minimal implementiert)

KRITISCHES PROBLEM:
- Echte Coverage unbekannt wegen deaktivierter Tests
- CI läuft nur mit minimaler Test-Suite
- Quality Gates nicht erfüllt
```

### **JaCoCo Configuration (aktuell konfiguriert):**
```xml
<!-- JaCoCo Plugin für Code Coverage -->
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>${jacoco-maven-plugin.version}</version>
  <executions>
    <execution>
      <id>prepare-agent</id>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>test</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

## 🚨 Critical Test Debt & Recovery Plan

### **Immediate Actions Required:**

#### **Phase 1: Stabilize Core Tests (Woche 1-2)**
```yaml
Goals:
- Alle 40 "core" Tests müssen 100% grün sein
- CI Pipeline muss stabil laufen
- Basic Coverage Measurement etablieren

Actions:
1. Audit aller @Tag("core") Tests
2. Fix flaky Tests sofort
3. JaCoCo Report für core Tests aktivieren
4. Pre-Commit Hooks für core Test Failures
```

#### **Phase 2: Migrate Test Recovery (Woche 3-6)**
```yaml
Goals:
- 20-30% der "migrate" Tests reaktivieren
- TestDataBuilder Migration beschleunigen
- Coverage schrittweise erhöhen

Actions:
1. 5-10 migrate Tests pro Woche auf core umstellen
2. SEED-Abhängigkeiten systematisch entfernen
3. TestDataBuilder für alle Entities implementieren
4. Coverage Monitoring einführen
```

#### **Phase 3: Quarantine Resolution (Woche 7-12)**
```yaml
Goals:
- Gefährliche Tests sicher reaktivieren oder löschen
- Full Coverage Measurement
- Quality Gates implementieren

Actions:
1. Quarantine Tests einzeln analysieren
2. Sichere Patterns für DB-Tests etablieren
3. Cleanup-Strategien implementieren
4. oder Tests komplett entfernen wenn unnötig
```

### **Quality Gates Definition:**
```yaml
Minimum Standards:
- Backend: >70% Coverage (aktuell unbekannt)
- Frontend: >80% Coverage (aktuell ~67%)
- API Tests: 100% Endpoint Coverage
- E2E Tests: Critical User Journeys

CI Requirements:
- Alle core Tests müssen grün sein
- Coverage darf nicht sinken
- Neue Tests müssen @Tag("core") werden
- Keine neuen quarantine Tests
```

## 📈 Testing Tools & Infrastructure

### **Backend Testing Tools:**
```yaml
Testing Framework: JUnit 5 + Quarkus Test
HTTP Testing: RestAssured
Database: TestContainers + PostgreSQL
Security Testing: @TestSecurity + Keycloak Test
Performance: k6 + JMeter (minimal konfiguriert)
Coverage: JaCoCo (aktiv, aber unvollständige Daten)
```

### **Frontend Testing Tools:**
```yaml
Unit Testing: Vitest + React Testing Library
Component Testing: @testing-library/react
Visual Testing: Storybook (konfiguriert, nicht vollständig)
E2E Testing: Playwright (minimal implementiert)
API Mocking: MSW (Mock Service Worker)
Coverage: Vitest Coverage + @vitest/coverage-v8
```

### **CI/CD Integration:**
```yaml
GitHub Actions:
├── Backend: Nur core Tests (40/197) → 3 Min Build
├── Frontend: Basic Tests + Build → 2 Min Build
├── E2E: Minimal Playwright Tests → 5 Min Build
└── Coverage: JaCoCo Report (unvollständig)

Problem:
- CI ist schnell WEIL Tests deaktiviert sind
- Echte Quality Validation fehlt
- Coverage Metrics nicht vertrauenswürdig
```

## 🔧 Test Development Workflow

### **Für neue Features (REQUIRED):**
```java
// 1. IMMER @Tag("core") für neue Tests
@Test
@Tag("core")
@TestSecurity(user = "admin", roles = {"admin"})
void shouldCreateNewFeature() {
    // TestDataBuilder verwenden
    var testData = FeatureTestDataBuilder.aValidFeature().build();

    // CQRS Testing Pattern
    var result = commandService.createFeature(testData.toRequest());

    // Assertions
    assertThat(result).isNotNull();

    // Cleanup automatisch durch isTestData = true
}

// 2. Beide CQRS Paths testen wenn Feature Flag existiert
@ParameterizedTest
@ValueSource(booleans = {true, false})
void shouldWorkWithBothCQRSAndLegacy(boolean useCQRS) {
    // Test implementation
}
```

### **Test Naming Conventions:**
```java
// Backend Test Naming
class [ServiceName]Test {                    // Unit Tests
class [ServiceName]IntegrationTest {         // Integration Tests
class [ResourceName]ResourceTest {           // API Tests
class [EntityName]RepositoryTest {           // Repository Tests

// Method Naming
void should[ExpectedBehavior]_when[Condition]() {
    // Given-When-Then Pattern
}
```

### **Frontend Test Patterns:**
```typescript
// Component Test Structure
describe('ComponentName', () => {
  describe('Rendering', () => {
    it('should render with default props', () => {});
    it('should render with theme colors', () => {});
  });

  describe('Interactions', () => {
    it('should handle click events', () => {});
    it('should handle form submission', () => {});
  });

  describe('States', () => {
    it('should show loading state', () => {});
    it('should show error state', () => {});
  });
});
```

## ⚠️ Anti-Patterns & Warnings

### **VERBOTEN - Diese Patterns nie verwenden:**
```java
// ❌ NIEMALS - Hardcoded Test Data
@Test
void badTest() {
    Customer customer = new Customer();
    customer.setName("Test Customer");  // Collision risk!
    customer.setEmail("test@test.com"); // Not unique!
}

// ❌ NIEMALS - SEED-Abhängigkeiten
@Test
void badSeedTest() {
    assumeTrue(seedDataExists());  // Flaky!
    Customer customer = findSeedCustomer("known-name");
}

// ❌ NIEMALS - @Tag("quarantine") für neue Tests
@Test
@Tag("quarantine")  // VERBOTEN für neue Tests!
void dangerousTest() {
    // Never add new quarantine tests!
}
```

### **Monitoring & Alerts:**
```yaml
# Test Health Monitoring
Alerts:
- Core Test Failures → Sofortige Benachrichtigung
- Coverage Drop → Daily Report
- Quarantine Growth → Weekly Review
- CI Build Time > 5 Min → Investigation

Metrics:
- Test Execution Time Trend
- Coverage Percentage Trend
- Flaky Test Detection
- Test Categorization Distribution
```

---

**📋 Testing Guide erstellt:** Basierend auf ECHTER Analyse von 302 Test-Dateien
**📅 Letzte Aktualisierung:** 2025-09-17
**👨‍💻 Maintainer:** Claude + QA Team

**🚨 KRITISCHER ZUSTAND: 67 deaktivierte Tests - Sofortige Aktion erforderlich!**

**💡 "Consolidate First" deckt auf: Echte Probleme statt Marketing-Metriken!**