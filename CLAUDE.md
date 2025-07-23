# Arbeitsrichtlinien für Claude im FreshPlan Sales Tool Projekt

**📅 Aktuelles Datum: <!-- AUTO_DATE --> (System: 08.06.2025)**

## 🚨 KRITISCHE REGELN (IMMER LESEN!)

### Die 17 wichtigsten Regeln für Claude:

1. **REPOSITORY SAUBER HALTEN:** VOR JEDEM GIT PUSH/COMMIT muss `./scripts/quick-cleanup.sh` ausgeführt werden!
2. **SPRACHE:** IMMER Deutsch - auch bei kurzen Antworten, Status-Updates, Zusammenfassungen!
3. **GRÜNDLICHKEIT:** Gründlichkeit geht VOR Schnelligkeit - keine Quick-Fixes ohne Dokumentation
4. **TESTS:** KEINE Implementierung ohne Tests (Unit ≥80%, Integration, Browser-Tests)
5. **DOKUMENTATION:** Strukturiert ablegen in `docs/claude-work/` mit Datum + Kategorie
6. **CODE-REVIEW:** Nach JEDEM bedeutenden Abschnitt + Two-Pass Review durchführen
7. **ARCHITEKTUR:** Strikte Trennung - Backend: domain/api/infrastructure, Frontend: features/components
8. **ZEILENLÄNGE:** Max. 80-100 Zeichen - nutze Umbrüche und Hilfsvariablen
9. **KEINE NEUEN DATEIEN:** IMMER bestehende Dateien editieren statt neue erstellen
10. **CLEAN CODE:** SOLID, DRY, KISS - Code muss selbsterklärend sein
11. **SECURITY:** Keine Hardcoded Secrets, Input Validation überall, CORS korrekt
12. **PROAKTIVITÄT:** Fasse Verständnis zusammen und frage BEVOR du codest
13. **INKREMENTELL:** Kleine, nachvollziehbare Schritte - teste häufig
14. **CI GRÜN HALTEN:** Bei roter CI selbstständig debuggen mit GitHub CLI
15. **NAMING:** PascalCase für Klassen, camelCase für Methoden, UPPER_SNAKE für Konstanten
16. **PERFORMANCE:** Bundle ≤200KB, API <200ms P95, keine N+1 Queries
17. **FRESHFOODZ CI COMPLIANCE:** ALLE sichtbaren Frontend-Elemente MÜSSEN Freshfoodz CI verwenden (#94C456, #004F7B, Antonio Bold, Poppins)

## 🆘 NOTFALL-DIAGNOSE (für Claude nach Übergabe)

### Symptom: Frontend zeigt "Failed to fetch" oder "Connection refused"
➡️ **Backend-Problem** - [Springe zu Backend-Fix](#backend-fix)

### Symptom: "Keine Kunden gefunden" aber API antwortet
➡️ **Datenbank leer** - [Springe zu Testdaten-Fix](#testdaten-fix) 

### Symptom: Tests schlagen fehl mit 401 Unauthorized
➡️ **Security-Problem** - [Springe zu Auth-Fix](#auth-fix)

### Symptom: Irgendwas funktioniert nicht
➡️ **Automatische Diagnose** - Führe aus: `./scripts/diagnose-problems.sh`

---

### 🔍 Quick Reference - Direkt zu den Details:
- [🆘 Notfall-Diagnose](#🆘-notfall-diagnose-für-claude-nach-übergabe)
- [0. Grundlegende Arbeitsphilosophie](#0-grundlegende-arbeitsphilosophie)
- [0.1 Best Practices und Architektur](#01-best-practices-und-architektur-standards)
- [0.10 Code-Review-Regel](#010-code-review-regel-gründliche-überprüfung-bei-jedem-bedeutenden-abschnitt)
- [2. Kommunikation und Vorgehensweise](#2-kommunikation-und-vorgehensweise)
- [10. Claude's Dokumentations-Ablagestruktur](#10-claudes-dokumentations-ablagestruktur)

---

<!-- Link-Definitionen für einfache Wartung -->
[vision]: ./VISION_AND_ROADMAP.md
[way-of-working]: ./WAY_OF_WORKING.md
[api-contract]: ./docs/technical/API_CONTRACT.md
[backend-guide]: ./docs/technical/BACKEND_START_GUIDE.md
[frontend-spec]: ./docs/technical/FRONTEND_BACKEND_SPECIFICATION.md
[known-issues]: ./docs/KNOWN_ISSUES.md
[adr-template]: ./docs/adr/ADR_TEMPLATE.md
[team-setup]: ./docs/team/TEAM_SETUP.md
[development-setup]: ./docs/team/DEVELOPMENT_SETUP.md
[code-review-standard]: ./docs/guides/CODE_REVIEW_STANDARD.md
[keycloak-setup]: ./docs/guides/KEYCLOAK_SETUP.md
[documentation-structure]: ./docs/DOCUMENTATION_STRUCTURE.md
[freshfoodz-ci]: ./docs/FRESH-FOODZ_CI.md

## 0. Grundlegende Arbeitsphilosophie

**🎯 UNSERE DEVISE: GRÜNDLICHKEIT GEHT VOR SCHNELLIGKEIT**

- Jede Implementierung muss gründlich getestet werden
- Keine Quick-Fixes oder Workarounds ohne Dokumentation
- Denke immer an die zukünftigen Integrationen und Erweiterungen
- Was wir jetzt richtig machen, erspart uns später Arbeit
- Siehe [Vision und Roadmap][vision] für die langfristige Ausrichtung des Projekts

### Unser Code-Qualitäts-Versprechen

**Wir verpflichten uns, bei jedem Commit auf:**

**Sauberkeit**
- Klare Modul-Grenzen, sprechende Namen, keine toten Pfade

**Robustheit**
- Unit-, Integrations- und E2E-Tests, defensives Error-Handling

**Wartbarkeit**
- SOLID, DRY, CI-Checks, lückenlose Docs & ADRs

**Transparenz**
- Unklarheiten sofort kanalisieren → Issue / Stand-up

## 0.1 Best Practices und Architektur-Standards

**ABSOLUTES ZIEL: Code, den jeder Entwickler sofort versteht - KEINE KOMPROMISSE!**

### Grundprinzipien:
1. **Clean Code** - Lesbarkeit geht vor Cleverness
2. **SOLID Principles** - Jede Klasse hat EINE klare Verantwortung
3. **DRY** - Don't Repeat Yourself, aber nicht auf Kosten der Klarheit
4. **KISS** - Keep It Simple, Stupid
5. **YAGNI** - You Aren't Gonna Need It (keine vorzeitige Abstraktion)

### Backend-Architektur (Quarkus/Java):

#### Legacy-Struktur (wird schrittweise migriert):
```
backend/
├── api/                          # REST Layer (Controllers)
│   ├── resources/               # REST Endpoints (@Path)
│   └── exception/               # Exception Handling
│       └── mapper/             # JAX-RS Exception Mappers
├── domain/                      # Business Domain (Core)
│   └── [aggregate]/            # z.B. user, order, product
│       ├── entity/             # JPA Entities
│       ├── repository/         # Data Access Layer
│       ├── service/            # Business Logic
│       │   ├── dto/           # Request/Response DTOs
│       │   ├── exception/     # Domain Exceptions
│       │   └── mapper/        # Entity-DTO Mapping
│       └── validation/         # Domain Validators
├── infrastructure/              # Technical Details
│   ├── config/                 # Configuration Classes
│   ├── security/               # Security Implementation
│   └── persistence/            # DB-specific Code
└── shared/                      # Shared Utilities
    ├── constants/              # Global Constants
    └── util/                   # Utility Classes
```

#### Neue Modulare Architektur (ab 09.07.2025):
```
backend/
├── modules/                     # Modularer Monolith
│   ├── customer/               # Customer Bounded Context
│   │   ├── core/              # Kern-Modul
│   │   │   ├── domain/        # Entities, Value Objects
│   │   │   ├── application/   # Command/Query Handlers
│   │   │   └── infrastructure/# Repositories
│   │   ├── contacts/          # Kontakt-Modul
│   │   ├── financials/        # Finanz-Modul
│   │   └── timeline/          # Event-History
│   └── shared/                # Gemeinsame Module
│       ├── events/            # Domain Events
│       └── api/               # API Gateway
└── legacy/                     # Alt-Code während Migration
```

**Migration-Strategie:**
1. Neue Module parallel zu altem Code
2. Feature Flags für schrittweise Umstellung
3. Facade Pattern für API-Kompatibilität
4. Event-Driven Communication zwischen Modulen

### Frontend-Architektur (React/TypeScript):
```
frontend/
├── components/                  # Reusable UI Components
│   ├── common/                 # Generic (Button, Input, etc.)
│   └── domain/                 # Domain-specific Components
├── features/                    # Feature-based Organization
│   └── [feature]/              # z.B. users, orders
│       ├── components/         # Feature Components
│       ├── hooks/              # Custom Hooks
│       ├── services/           # API Services
│       ├── types/              # TypeScript Types
│       └── utils/              # Feature Utilities
├── layouts/                     # Page Layouts
├── pages/                       # Route Pages
├── services/                    # Global Services
│   ├── api/                    # API Client
│   └── auth/                   # Auth Service
├── store/                       # State Management
├── types/                       # Global Types
└── utils/                       # Global Utilities
```

### Code-Standards im Detail:

#### Code-Lesbarkeit und Zeilenlänge:

**Warum kurze Zeilen?**
- **Bessere Lesbarkeit**: Kurze Zeilen lassen sich schneller erfassen und verstehen
- **Vergleichbarkeit**: In Code-Review-Tools oder bei der Versionskontrolle sind kurze Zeilen einfacher zu vergleichen
- **Kompatibilität**: Viele Editoren und Monitore zeigen lange Zeilen nicht vollständig an

**Empfehlungen zur Zeilenlänge:**
- **Standard**: Maximale Zeilenlänge von 80 bis 120 Zeichen
- **Java**: 100 Zeichen (Google Java Style Guide)
- **TypeScript/JavaScript**: 80-100 Zeichen
- **Markdown**: 80 Zeichen für bessere Diff-Ansichten

**Praktische Tipps:**

1. **Zeilenumbrüche nutzen:**
```java
// Schlecht (zu lang)
if (user.isActive() && user.hasPermission("admin") && user.getLastLogin().isAfter(yesterday) && user.getDepartment().equals("IT")) {

// Gut (umgebrochen)
if (user.isActive()
        && user.hasPermission("admin")
        && user.getLastLogin().isAfter(yesterday)
        && user.getDepartment().equals("IT")) {
```

2. **Hilfsvariablen verwenden:**
```java
// Schlecht
if (userRepository.findByEmail(email).isPresent() && userRepository.findByEmail(email).get().isActive()) {

// Gut
Optional<User> userOpt = userRepository.findByEmail(email);
boolean isActiveUser = userOpt.isPresent() && userOpt.get().isActive();
if (isActiveUser) {
```

3. **Funktionen auslagern:**
```java
// Schlecht
if (user.getAge() >= 18 && user.hasVerifiedEmail() && user.getCountry().equals("DE") && !user.isBlocked()) {

// Gut
if (isEligibleForService(user)) {

private boolean isEligibleForService(User user) {
    return user.getAge() >= 18
            && user.hasVerifiedEmail()
            && user.getCountry().equals("DE")
            && !user.isBlocked();
}
```

4. **Method Chaining aufteilen:**
```java
// Schlecht
UserResponse response = userService.findById(id).map(mapper::toResponse).orElseThrow(() -> new UserNotFoundException(id));

// Gut
UserResponse response = userService
        .findById(id)
        .map(mapper::toResponse)
        .orElseThrow(() -> new UserNotFoundException(id));
```

5. **Lange Parameter-Listen:**
```java
// Schlecht
public UserResponse createUser(String username, String firstName, String lastName, String email, String department, boolean isActive) {

// Gut - Builder Pattern oder Request Object
public UserResponse createUser(CreateUserRequest request) {
```

#### Naming Conventions:
- **Klassen**: PascalCase, beschreibende Nomen (`UserService`, `OrderRepository`)
- **Interfaces**: PascalCase, KEIN "I" Präfix (`UserRepository`, nicht `IUserRepository`)
- **Methoden**: camelCase, Verben (`createUser`, `findByEmail`)
- **Variablen**: camelCase, beschreibend (`userEmail`, nicht nur `email`)
- **Konstanten**: UPPER_SNAKE_CASE (`MAX_RETRY_ATTEMPTS`)
- **Dateien**: Wie die Hauptklasse (`UserService.java`, `UserList.tsx`)
- **Packages/Folders**: lowercase, Singular (`user`, nicht `users`)

#### JavaDoc/JSDoc Standards:
```java
/**
 * Service layer for User management operations.
 * 
 * This service encapsulates the business logic for user management,
 * providing a clean API for user operations while handling validation,
 * error cases, and data transformation.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class UserService {
    // Inline-Kommentare NUR wenn der Code nicht selbsterklärend ist
    // Bevorzuge aussagekräftige Methoden-/Variablennamen
}
```

#### Error Handling Best Practices:
```java
// Domain Exception mit klarer Bedeutung
public class UserNotFoundException extends RuntimeException {
    // Immer mit aussagekräftiger Message
    public UserNotFoundException(String userId) {
        super("User not found with ID: " + userId);
    }
}

// Exception Mapper für konsistente API Responses
@Provider
public class UserNotFoundExceptionMapper 
    implements ExceptionMapper<UserNotFoundException> {
    // Einheitliches Error Response Format
}
```

#### DTO Design:
```java
// Immutable DTOs mit Builder Pattern
public final class UserResponse {
    private final UUID id;
    private final String username;
    // ... andere fields
    
    // Private constructor
    private UserResponse(Builder builder) { /*...*/ }
    
    // Nur Getter, keine Setter
    public UUID getId() { return id; }
    
    // Builder für flexible Objekterstellung
    public static Builder builder() { return new Builder(); }
}
```

#### Repository Pattern:
```java
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {
    // Klare, aussagekräftige Methodennamen
    public Optional<User> findByUsername(String username) {
        // Defensive Programming - null checks
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return find("username", username).firstResultOptional();
    }
}
```

#### Service Layer:
```java
@ApplicationScoped
@Transactional
public class UserService {
    // Constructor Injection (nicht @Inject auf Fields)
    private final UserRepository repository;
    private final UserMapper mapper;
    
    @Inject
    public UserService(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    // Business Logic mit klaren Transaktionsgrenzen
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Validation
        // 2. Business Rules
        // 3. Persistence
        // 4. Response Mapping
    }
}
```

#### Testing Standards:
```java
// Test-Struktur
class UserServiceTest {
    @Test
    void createUser_withValidData_shouldReturnCreatedUser() {
        // Arrange
        var request = CreateUserRequest.builder()
            .username("john.doe")
            .build();
        
        // Act
        var result = userService.createUser(request);
        
        // Assert
        assertThat(result)
            .isNotNull()
            .extracting(UserResponse::getUsername)
            .isEqualTo("john.doe");
    }
}
```

#### Security Best Practices:
- **Keine Hardcoded Secrets** - Nutze Environment Variables
- **Input Validation** auf allen Ebenen (DTO, Service, Repository)
- **Prepared Statements** automatisch durch JPA/Panache
- **@RolesAllowed** für Authorization
- **CORS** nur für erlaubte Origins

#### Performance Considerations:
- **Lazy Loading** für Collections (`@OneToMany(fetch = FetchType.LAZY)`)
- **Pagination** für alle Listen (`Page`, `Pageable`)
- **Query Optimization** mit Named Queries
- **Caching** wo sinnvoll (`@CacheResult`)
- **Database Indexes** in Flyway Migrations

### Event-Driven Architecture (NEU ab 09.07.2025):
```java
// Domain Event Basis
public interface DomainEvent {
    UUID getEventId();
    UUID getAggregateId();
    LocalDateTime getOccurredAt();
}

// Event Bus für Module-Kommunikation
@ApplicationScoped
public class EventBus {
    public void publish(DomainEvent event) {
        // Sync jetzt, Async später
    }
}

// Event Handler
void onCustomerCreated(@Observes CustomerCreatedEvent e) {
    // Reagiere auf Event
}
```

### CQRS Pattern (für Read-Heavy Operations):
```java
// Command Side
CustomerCommandHandler -> Customer Entity -> Event

// Query Side  
CustomerQueryHandler -> CustomerReadModel -> Response

// Read Models für Performance
@Entity @Immutable
public class CustomerListView {
    // Denormalisierte Daten für schnelle Queries
}
```

### Git Workflow & Code Review:
```bash
# WICHTIG: VOR JEDEM COMMIT/PUSH Repository säubern!
./scripts/quick-cleanup.sh

# Feature Branch erstellen
git checkout -b feature/user-management

# Atomic Commits mit klaren Messages
git commit -m "feat(user): Add user creation endpoint

- Implement POST /api/users
- Add validation for email uniqueness
- Include unit and integration tests"

# VOR PUSH: Nochmals säubern!
./scripts/quick-cleanup.sh
git push origin feature/user-management

# Pull Request Checklist:
# - [ ] Repository mit quick-cleanup.sh gesäubert ✅
# - [ ] Tests sind grün
# - [ ] Code Coverage > 80%
# - [ ] JavaDoc/JSDoc komplett
# - [ ] Keine TODO-Kommentare
# - [ ] Security-Check durchgeführt
# - [ ] Performance akzeptabel
```

### Metriken für Code-Qualität:
- **Test Coverage**: Minimum 80% für neue Features
- **Cyclomatic Complexity**: Max 10 pro Methode
- **Method Length**: Max 20 Zeilen (ideal < 10)
- **Class Length**: Max 200 Zeilen
- **Package Dependencies**: Keine zirkulären Abhängigkeiten

### Continuous Improvement:
- **Code Reviews** sind Lernmöglichkeiten
- **Refactoring** ist Teil jeder Story
- **Tech Debt** wird dokumentiert und priorisiert
- **Pair Programming** für komplexe Features
- **Knowledge Sharing** in Team-Sessions

## 0.2 DevOps & Release-Management

### Branch- & Release-Strategie:
| Thema | Empfehlung | Nutzen |
|-------|------------|--------|
| Branching | Trunk-based mit short-lived feature branches (max. 24h offen) | Weniger Merge-Konflikte, kontinuierliche Integration |
| Commit-Konvention | Conventional Commits + Commitlint | Automatische CHANGELOGs & Releases |
| Releases | SemVer + GitHub Actions Release-Workflow | Klare Versionierung, Hot-fix-Pfad |

### Release-Workflow:
```bash
# Feature fertig? Merge to main
git checkout main && git pull
git merge --no-ff feature/user-management

# Release vorbereiten
npm version minor  # oder major/patch
git push && git push --tags

# GitHub Action erstellt automatisch:
# - Release Notes aus Commits
# - Docker Images mit Tags
# - Deployment zu Stage
```

### Dokumentation & Wissenstransfer:
- **ADRs** (Architecture Decision Records) für alle wichtigen Entscheidungen
  - Template: [ADR Template][adr-template]
  - Automatisierung: `adr-tools` → PR-Kommentar mit Diff
- **Onboarding-Playbook**: 90-Minuten "Tour de Codebase"
  - README mit Links zu Key-Files
  - Architektur-Diagramme (C4 Model)
  - Video-Walkthrough für neue Teammitglieder
- **Tech Radar**: Bewertung neuer Libraries/Tools
  - Adopt / Trial / Assess / Hold
  - Quartalsweise Review

## 0.3 Security & Compliance

### Security-Standards:
| Ebene | Regel | Automatisierung |
|-------|-------|-----------------|
| Dependencies | Snyk + Dependabot, auto-merge wenn CVSS < 4 | CI-Gate |
| Secrets | GitHub Secrets → env-subst in Docker | Keine Secrets im Code |
| API Security | OWASP Top 10 Check | ZAP-Docker nightly |
| Code Quality | SonarCloud Security Hotspots | PR-Block bei kritisch |

### Security-Checkliste:
```yaml
# .github/workflows/security.yml
- Dependency Check (Snyk)
- SAST (SonarCloud)
- Container Scan (Trivy)
- API Security Test (OWASP ZAP)
- Secret Scanning (GitGuardian)
```

### Compliance:
- **DSGVO**: Personenbezogene Daten verschlüsselt
- **Audit-Log**: Alle kritischen Operationen
- **Data Retention**: Automatisches Löschen nach X Tagen

## 0.4 Observability & Performance

### Golden Signals:
- **Latency**: < 200ms P95 für API Calls
- **Traffic**: Requests per Second
- **Errors**: < 0.1% Error Rate
- **Saturation**: CPU/Memory < 80%

### Monitoring Stack:
```yaml
# OpenTelemetry → CloudWatch/X-Ray Pipeline
- Distributed Tracing (Jaeger-kompatibel)
- Metrics (Prometheus-Format)
- Logs (strukturiert, JSON)
- Real User Monitoring (RUM)
```

### Performance Budgets:

#### Frontend:
- **Bundle Size**: ≤ 200 KB initial (gzipped)
- **LCP**: ≤ 2.5s (mobile 3G)
- **FID**: ≤ 100ms
- **CLS**: ≤ 0.1
- **Lighthouse Score**: ≥ 90

#### Backend:
- **API Response**: P95 < 200ms
- **Database Queries**: < 50ms
- **Memory per Request**: < 50MB
- **Startup Time**: < 10s

### Performance-Gates:
```bash
# Lighthouse CI als GitHub Check
lighthouse:
  assertions:
    categories:performance: ["error", {"minScore": 0.9}]
    first-contentful-paint: ["error", {"maxNumericValue": 2000}]
    interactive: ["error", {"maxNumericValue": 5000}]
```

## 0.5 Testing-Pyramide

### Test-Strategie:
| Stufe | Coverage-Ziel | Technologie | Scope |
|-------|---------------|-------------|-------|
| Unit | 80% Lines/Functions | JUnit 5 + Mockito / Vitest | Business Logic |
| Integration | 100% API Endpoints | RestAssured / MSW | API Contracts |
| E2E | Critical User Journeys | Playwright | Happy Paths |
| Performance | Key Transactions | k6 / Artillery | Load Testing |
| Security | OWASP Top 10 | ZAP / Burp | Penetration |

### Test-Patterns:
```java
// Given-When-Then für BDD
@Test
void createUser_withValidData_shouldReturnCreatedUser() {
    // Given
    var request = validUserRequest();
    
    // When
    var response = userService.createUser(request);
    
    // Then
    assertThat(response).satisfies(user -> {
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("john.doe");
    });
}
```

### CI-Monitoring und Automatisches Debugging

**NEU: Proaktive CI-Überwachung durch Claude** 🤖

Wenn die CI rot ist:
1. **Claude holt sich selbstständig die Logs** via GitHub CLI
2. **Analysiert den Fehler** und versucht eigenständig zu fixen
3. **Pusht die Lösung** und überwacht erneut
4. **Eskaliert nur bei:** 
   - Komplexen Problemen die mehrere Versuche erfordern
   - Architektur-Entscheidungen
   - Unklarheiten über Business-Logik

```bash
# Claude's CI-Workflow
gh run list --branch <branch> --status failure --limit 1
gh run view <RUN_ID> --log-failed
# Analyse → Fix → Push → Repeat
```

**Vorteile:**
- ✅ Schnellere Fixes (keine Wartezeit)
- ✅ Jörg wird nur bei echten Problemen involviert
- ✅ CI bleibt häufiger grün
- ✅ Teams können sich auf Features konzentrieren

### CI-Debugging-Strategie: "Strategie der kleinen Schritte"

**Wenn die CI nach mehreren automatischen Versuchen immer noch fehlschlägt:**

#### 1. Minimierung des Fehlerbereichs
```bash
# Beispiel: Test isoliert ausführen
./mvnw test -Dtest=UserServiceTest#testGetAllUsers

# Mit Debug-Output
./mvnw test -Dtest=UserServiceTest#testGetAllUsers -X
```

#### 2. Schrittweise Vereinfachung
```java
@Test
void debugTest() {
    // Schritt 1: Minimaler Test
    System.out.println("=== DEBUG: Test startet ===");
    assertThat(1).isEqualTo(1);
    
    // Schritt 2: Eine Komponente hinzufügen
    System.out.println("=== DEBUG: Repository-Mock ===");
    when(repository.findAll()).thenReturn(List.of());
    
    // Schritt 3: Schrittweise erweitern
    System.out.println("=== DEBUG: Service-Aufruf ===");
    var result = service.getAllUsers();
    System.out.println("=== DEBUG: Result size: " + result.size());
}
```

#### 3. Debug-Output Best Practices
```java
// Strukturierter Debug-Output
private void debugLog(String phase, Object data) {
    System.out.printf("=== DEBUG [%s]: %s ===%n", 
        phase, 
        data != null ? data.toString() : "null"
    );
}

// In Tests verwenden
debugLog("BEFORE", testUser);
var result = service.updateUser(testUser);
debugLog("AFTER", result);
```

#### 4. CI-spezifisches Debugging
```yaml
# .github/workflows/ci.yml
- name: Run failing test with debug
  if: failure()
  run: |
    echo "=== Rerunning failed test with debug ==="
    ./mvnw test -Dtest=FailingTest -X
    echo "=== Environment Info ==="
    java -version
    ./mvnw --version
```

#### 5. Systematisches Vorgehen
1. **Isolieren**: Einzelnen Test ausführen
2. **Minimieren**: Test auf Kern-Assertion reduzieren
3. **Debug-Output**: An kritischen Stellen einfügen
4. **Schrittweise erweitern**: Eine Zeile nach der anderen
5. **Vergleichen**: Lokal vs. CI Environment
6. **Dokumentieren**: Findings für Team festhalten

#### Anti-Patterns vermeiden:
- ❌ Blind try-catch ohne Logging
- ❌ System.out.println ohne Kontext
- ❌ Große Code-Blöcke auf einmal ändern
- ❌ Debug-Output in Production-Code vergessen

### Feature Flag Governance:

**Trunk-based Development erfordert strikte Feature Flag Disziplin!**

#### Namenskonvention:
```
ff_<ticket-nr>_<kurzer-name>
Beispiel: ff_FRESH-123_user_export
```

#### Feature Flag Manifest:
```java
@FeatureFlag(
    name = "ff_FRESH-123_user_export",
    description = "Enable user data export functionality",
    ticket = "FRESH-123",
    owner = "user-team",
    createdDate = "2025-01-06",
    sunsetDate = "2025-02-06", // PFLICHT: Max 30 Tage!
    defaultValue = false
)
```

#### CI-Gates für Feature Flags:
1. **Naming Convention Check**: Regex-Validation im Build
2. **Age Check**: Flags > 30 Tage → Build Warning
3. **Sunset Enforcement**: Flags > 60 Tage → Build Failure
4. **Usage Analysis**: Unused Flags → Automatic Removal PR

#### Feature Flag Lifecycle:
```yaml
1. Create: ff_TICKET_feature + Sunset Date
2. Test: Gradual Rollout (1% → 10% → 50% → 100%)
3. Monitor: Metrics & Error Rates per Flag State
4. Remove: Automated PR when 100% + 7 days stable
```

#### Anti-Patterns vermeiden:
- ❌ Permanente Feature Flags (werden zu Tech Debt)
- ❌ Verschachtelte Flags (if flag1 && flag2)
- ❌ Business Logic in Flags (nur Ein/Aus)
- ❌ Flags ohne Metriken

## 0.6 Frontend Excellence

### Design System:
- **Storybook** als Living Style Guide
  - Alle Components isoliert entwickeln
  - Visual Regression Tests
  - Auto-Deploy zu Chromatic
- **Accessibility (A11Y)**:
  - `eslint-plugin-jsx-a11y`
  - `axe-core` in CI
  - WCAG 2.1 AA Compliance
- **Component Structure**:
  ```
  components/
  └── Button/
      ├── Button.tsx         # Component
      ├── Button.test.tsx    # Tests
      ├── Button.stories.tsx # Storybook
      ├── Button.module.css  # Styles
      └── index.ts          # Export
  ```

### State Management:
- **React Query** für Server State
- **Zustand** für Client State (wenn nötig)
- **Context** nur für Cross-Cutting Concerns

## 0.7 Infrastructure as Code

### AWS CDK Setup:
```typescript
// infrastructure/cdk/lib/freshplan-stack.ts
export class FreshPlanStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);
    
    // ECS Fargate für Backend
    const backend = new ApplicationLoadBalancedFargateService(...);
    
    // CloudFront für Frontend
    const frontend = new CloudFrontWebDistribution(...);
    
    // RDS PostgreSQL
    const database = new DatabaseInstance(...);
  }
}
```

### Policy as Code:
- **Open Policy Agent** für Security Rules
- **AWS Config Rules** für Compliance
- **Drift Detection** täglich

### Disaster Recovery:
- **RTO**: 4 Stunden
- **RPO**: 1 Stunde
- **Backups**: Automated Snapshots
- **Runbooks**: Dokumentierte Prozesse

## 0.8 Team-Rituale & Workflows

### Development Workflow:
1. **Monday**: Sprint Planning & Backlog Grooming
2. **Daily**: 15-min Standup (blocker-focused)
3. **Wednesday**: Tech Debt Review (1h)
4. **Friday**: Refactoring Slot (2h) + Demos
5. **Retrospective**: Alle 2 Wochen

### Code Review Process:
```yaml
PR-Checklist:
  - [ ] Tests grün + Coverage ≥ 80%
  - [ ] Keine Security Warnings
  - [ ] Performance Budget eingehalten
  - [ ] Dokumentation aktualisiert
  - [ ] Screenshots bei UI-Änderungen
  - [ ] Changelog Entry (wenn public API)
```

### Knowledge Management:
- **ADR Reviews**: Quartalsweise
- **Tech Talks**: Jeden 2. Freitag
- **Pair Programming**: Min. 4h/Woche
- **Documentation Days**: 1x/Monat

### Incident Response:
1. **Detect**: Monitoring Alert
2. **Triage**: Severity 1-4 (siehe Matrix)
3. **Respond**: Runbook befolgen
4. **Resolve**: Fix + Deploy
5. **Review**: Blameless Postmortem

### Incident Severity Matrix:

| Severity | Impact | Beispiele | Response Time | Eskalation |
|----------|---------|-----------|---------------|------------|
| **SEV-1** | Komplettausfall Produktion | - Keine User können sich einloggen<br>- Datenverlust droht<br>- Sicherheitslücke aktiv ausgenutzt | < 15 Min | CTO + On-Call sofort |
| **SEV-2** | Teilausfall / Major Feature | - Zahlungsprozess defekt<br>- Performance < 50%<br>- Keine neuen Aufträge möglich | < 1 Std | Team Lead + On-Call |
| **SEV-3** | Minor Feature / Degradation | - PDF-Export fehlt<br>- Einzelne API langsam<br>- UI-Glitch in Firefox | < 4 Std | Team in Slack |
| **SEV-4** | Cosmetic / Low Impact | - Typo in UI<br>- Log-Spam<br>- Nicht-kritische Warnings | Next Sprint | Ticket in Backlog |

### Eskalations-Pfade:
```
SEV-1: @on-call → Team Lead → CTO → CEO
SEV-2: @on-call → Team Lead → Engineering Manager
SEV-3: Team Channel → Team Lead
SEV-4: Jira Ticket → Sprint Planning
```

### On-Call Rotation:
- **Primär**: 1 Woche Rotation (Mo-So)
- **Backup**: Immer verfügbar
- **Erreichbarkeit**: Handy + Laptop in 30 Min
- **Kompensation**: 1 Tag Ausgleich pro Woche

## 0.9 Tooling & Automation

### Development Tools:
- **IDE**: IntelliJ IDEA / VS Code mit Extensions
- **API Testing**: Insomnia / Postman
- **DB Client**: DBeaver / TablePlus
- **Git GUI**: GitKraken / SourceTree (optional)

### CI/CD Pipeline:
```yaml
# Stages
1. Lint & Format Check
2. Unit Tests + Coverage
3. Build & Package
4. Integration Tests
5. Security Scans
6. Deploy to Stage
7. E2E Tests
8. Performance Tests
9. Deploy to Production (manual approval)
```

### Automation:
- **Dependabot**: Weekly Updates
- **Renovate**: Grouped Updates
- **Release Please**: Automated Releases
- **Mergify**: Auto-merge bei grünen Checks

Diese Standards stellen sicher, dass FreshPlan 2.0 auf Enterprise-Niveau entwickelt wird - mit der Qualität, die erfahrene Entwickler erwarten und sofort verstehen.

## 0.10 Code-Review-Regel: Gründliche Überprüfung bei jedem bedeutenden Abschnitt

### 🔍 **GOLDENE REGEL: Nach jedem bedeutenden Entwicklungsschritt**

**Bei jedem bedeutenden Abschnitt gilt:**
> "Prüfe noch einmal sehr gründlich den Code auf Einhaltung unserer Programmierregeln und Logik"

**Definition "bedeutender Abschnitt":**
- Abschluss eines Features
- Ende eines Sprints
- Vor jedem Merge in main
- Nach größeren Refactorings
- Bei Architektur-Änderungen
- Nach Integration externer Services

### Prüfkriterien für Code Reviews:

#### 1. **Programmierregeln-Compliance** ✓
- [ ] Zeilenlänge eingehalten (80-100 Zeichen)
- [ ] Naming Conventions befolgt
- [ ] Proper Error Handling implementiert
- [ ] JavaDoc/JSDoc vorhanden
- [ ] DRY-Prinzip beachtet
- [ ] SOLID-Prinzipien eingehalten

#### 2. **Security-Check** 🔒
- [ ] Keine hardcoded Credentials
- [ ] Input Validation vorhanden
- [ ] Keine SQL-Injection-Anfälligkeit
- [ ] XSS-Protection implementiert
- [ ] CORS korrekt konfiguriert

#### 3. **Test-Coverage** 🧪
- [ ] Unit Tests ≥ 80%
- [ ] Integration Tests vorhanden
- [ ] Edge Cases abgedeckt
- [ ] Error Cases getestet
- [ ] Performance Tests (wenn relevant)

#### 4. **Logik-Überprüfung** 🧠
- [ ] Business Logic korrekt implementiert
- [ ] Keine Race Conditions
- [ ] Transaktionsgrenzen richtig gesetzt
- [ ] State Management konsistent
- [ ] Keine Memory Leaks

#### 5. **Performance** ⚡
- [ ] Keine N+1 Queries
- [ ] Lazy Loading wo sinnvoll
- [ ] Caching-Strategie implementiert
- [ ] Bundle Size im Budget
- [ ] Keine blockierenden Operationen

### Review-Prozess:

```bash
# 1. Automatisierte Checks
npm run lint
npm run test:coverage
npm run security:audit

# 2. Manuelle Code-Inspektion
# Verwende die Checkliste oben

# 3. Dokumentiere Findings
# Erstelle REVIEW_REPORT_<datum>.md

# 4. Behebe kritische Issues sofort
# Plane mittelfristige Verbesserungen
```

### Review-Report Template:

```markdown
# Code Review Report - [Feature/Sprint Name]
**Datum:** [YYYY-MM-DD]
**Reviewer:** Claude
**Scope:** [Beschreibung]

## Zusammenfassung
- Kritische Issues: X
- Wichtige Issues: Y
- Verbesserungsvorschläge: Z

## Kritische Findings
1. [Issue mit Code-Beispiel und Fix]

## Compliance-Status
- [ ] Programmierregeln: X%
- [ ] Security: ✓/✗
- [ ] Test Coverage: X%
- [ ] Performance: ✓/✗

## Nächste Schritte
1. ...
```

**Diese Regel ist VERPFLICHTEND und wird bei jedem Sprint-Ende automatisch ausgeführt!**

### 🔒 Der neue Two-Pass Review: Pflicht & Kür

**VERBESSERTE REGEL (ab 06.07.2025):** Unser Two-Pass-Review trennt maschinelle Hygiene von strategischer Qualität.

#### Two-Pass Review Prozess:

**Pass 1: Die "Pflicht" – Automatische Code-Hygiene**
- **Was:** Reine Code-Formatierung (Einrückungen, Leerzeichen, Imports)
- **Wer:** Claude führt automatisch `./mvnw spotless:apply` aus
- **Aufwand:** NULL für das Team - vollautomatisch!
- **Ergebnis:** Separater Commit für Formatierung

**Pass 2: Die "Kür" – Strategische Code-Qualität**
- **Was:** Alles, was wirklich zählt:
  - 🏛️ **Architektur:** Folgt der Code unserer Vision?
  - 🧠 **Logik:** Tut es was es soll laut Master Plan?
  - 📖 **Wartbarkeit:** Versteht es ein neuer Entwickler?
  - 💡 **Philosophie:** Lebt es unsere Prinzipien?
- **Wer:** Claude analysiert, Jörg entscheidet bei strategischen Fragen
- **Fokus:** Das, was Software wirklich gut macht

#### Der neue Ablauf:

```bash
# 1. Claude führt Pass 1 aus (automatisch)
cd backend && ./mvnw spotless:apply

# 2. Bei Änderungen: Separater Commit
git add -u && git commit -m "chore: apply Spotless formatting"

# 3. Pass 2: Strategische Review (siehe Template)
```

#### Review-Report Template (nur für Pass 2):

```markdown
# Strategic Code Review - [Feature]

## 🏛️ Architektur-Check
- [ ] Schichtenarchitektur eingehalten?
- [ ] Findings: ...

## 🧠 Logik-Check  
- [ ] Master Plan umgesetzt?
- [ ] Findings: ...

## 📖 Wartbarkeit
- [ ] Selbsterklärende Namen?
- [ ] Findings: ...

## 💡 Philosophie
- [ ] Unsere Prinzipien gelebt?
- [ ] Findings: ...

## 🎯 Strategische Fragen für Jörg
1. [Frage mit Kontext]
```

**Die Vorteile:**
- ✅ Keine Formatierungs-Diskussionen mehr
- ✅ Fokus auf wichtige Dinge
- ✅ Konsistenter Code automatisch
- ✅ Bessere Software durch strategischen Fokus

**Details:** Siehe `/docs/claude-work/daily-work/2025-07-06/2025-07-06_PROCESS_two-pass-review-neu.md`

## 1. Projektübersicht und Ziele

**Projektname:** FreshPlan Sales Tool 2.0
**Hauptziel:** Migration zu einer cloud-nativen Enterprise-Lösung mit React + Quarkus + Keycloak + PostgreSQL auf AWS.
**Aktuelle Phase:** Sprint 0 - Walking Skeleton (Monorepo Setup, Auth-Integration, erste API)
**Stack-Entscheidung:** 
- Frontend: React + TypeScript + Vite
- Backend: Quarkus (Java)
- Auth: Keycloak
- DB: PostgreSQL
- Cloud: AWS (ECS, RDS, S3, CloudFront)

## 2. Kommunikation und Vorgehensweise

1.  **Sprache:** Deutsch (IMMER - auch bei komprimierten Antworten oder Status-Updates).
2.  **Proaktivität:** Fasse dein Verständnis zusammen und frage nach, bevor du codest. Bei Unklarheiten oder Alternativen, stelle diese zur Diskussion.
3.  **Inkrementell Arbeiten:** Implementiere in kleinen, nachvollziehbaren Schritten. Teste häufig.
4.  **Fokus:** Konzentriere dich auf die aktuelle Aufgabe. Vermeide Scope Creep.
5.  **Claude-Protokoll:** Führe ein Markdown-Protokoll über deine Schritte, Entscheidungen und Testergebnisse für die aktuelle Aufgabe.
6.  **Gründlichkeit:** Führe IMMER umfassende Tests durch:
    - Unit-Tests für alle neuen Funktionen
    - Integration-Tests für Modul-Interaktionen
    - Manuelle Tests in verschiedenen Browsern
    - Performance-Tests bei größeren Änderungen
    - Dokumentiere alle Testergebnisse
7.  **Zusammenfassungen:** Auch bei Status-Updates, Zusammenfassungen oder kurzen Antworten IMMER auf Deutsch antworten. Die Tendenz bei komprimierten Inhalten ins Englische zu verfallen ist ein bekanntes Problem und muss aktiv vermieden werden.

## 3. Wichtige Befehle und Werkzeuge

### Legacy (im `/legacy` Ordner):
* `npm install`: Dependencies installieren
* `npm run dev`: Vite-Dev-Server für Legacy-App
* `npm run build:standalone`: Production-Build der Standalone-Version
* `npm run test`: Vitest Unit- und Integrationstests

### Frontend (React - im `/frontend` Ordner):
* `npm install`: Dependencies installieren
* `npm run dev`: Vite-Dev-Server für React-App
* `npm run build`: Production-Build
* `npm run test`: Vitest + React Testing Library
* `npm run lint`: ESLint mit React-Rules

### Backend (Quarkus - im `/backend` Ordner):
* `./mvnw quarkus:dev`: Development Mode mit Hot-Reload
* `./mvnw test`: Unit-Tests
* `./mvnw package`: Build JAR
* `./mvnw package -Pnative`: Native Build (GraalVM)

### Git-Konventionen:
* Conventional Commits: `feat:`, `fix:`, `chore:`, `docs:`
* Branch-Naming: `feature/`, `bugfix/`, `hotfix/`
* PR vor Merge, mindestens 1 Review

## 4. Architektur und Code-Struktur (Monorepo)

### Neue Struktur ab Sprint 0:
```
freshplan-sales-tool/
├── /legacy              # Alter Code (eingefroren als legacy-1.0.0)
├── /frontend            # React SPA
│   ├── src/
│   │   ├── components/
│   │   ├── contexts/    # AuthContext, etc.
│   │   ├── hooks/
│   │   ├── pages/
│   │   └── services/
│   └── package.json
├── /backend             # Quarkus API
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── /infrastructure      # Docker, K8s, AWS CDK
├── /docs               # ADRs, API-Docs
└── /.github/workflows  # CI/CD Pipelines
```

### Tech-Stack:
* **Frontend:** React 18 + TypeScript + Vite + MUI + React Query
* **Backend:** Quarkus + RESTEasy Reactive + Hibernate ORM + Flyway
* **Auth:** Keycloak mit OIDC
* **Database:** PostgreSQL mit Row-Level Security
* **Testing:** Vitest (Unit), Playwright (E2E), RestAssured (API)
* **CI/CD:** GitHub Actions + SonarCloud + AWS

## 5. Bekannte Probleme (Known Issues) und Workarounds

* Siehe [Known Issues][known-issues] für eine aktuelle Liste.
* **Übersetzung dynamischer Tabs:** Ein bekanntes Problem. Workaround wird in Phase 2 gesucht.
* **Performance bei großen Datenmengen:** Bei der Verarbeitung sehr vieler Positionen im Calculator kann es zu Verzögerungen kommen. Optimierungen sind für spätere Phasen geplant.

## 6. Test-Standards und Qualitätssicherung

**WICHTIG: Keine Implementierung ohne ausreichende Tests!**

### Minimale Test-Anforderungen:
1. **Unit-Tests**: Mindestens 80% Coverage für neue Module
2. **Integration-Tests**: Alle Modul-Interaktionen müssen getestet werden
3. **Browser-Tests**: Chrome, Firefox, Safari (mindestens)
4. **Performance-Tests**: Bei kritischen Komponenten
5. **Manuelle Tests**: Vollständige User-Flows durchspielen

### Test-Dokumentation:
- Erstelle immer einen Test-Report
- Dokumentiere gefundene Probleme
- Notiere Edge-Cases und Limitierungen

## 7. Aktueller Sprint: Sprint 0 - Walking Skeleton

### Heutige Ziele (Tag 1):
1. **09:00-09:05:** Legacy einfrieren (Tag `legacy-1.0.0`)
2. **09:05-09:30:** Monorepo-Struktur anlegen
3. **09:30-12:30:** Skeleton-PRs (Frontend + Backend)
4. **12:30-15:00:** CI/CD-Pipeline
5. **15:00-Ende:** Walking Skeleton verbinden (React → /api/ping → DB)

### Definition of Done für Sprint 0:
- [ ] User kann sich via Keycloak einloggen
- [ ] Geschützte Route `/calculator` nur mit Auth erreichbar
- [ ] API-Call `/api/ping` liefert DB-Timestamp
- [ ] E2E-Test läuft grün in GitHub Actions
- [ ] Alle Teammitglieder können lokal entwickeln

### Nächste Sprints (Preview):
- **Sprint 1:** Erste Features migrieren (Calculator, Customer-Liste)
- **Sprint 2:** API-Integration, Repository-Pattern
- **Sprint 3:** Vollständige Feature-Parität mit Legacy

## 8. Zukunftsorientierung

**Denke bei jeder Implementierung an:**
- Skalierbarkeit für große Datenmengen
- Erweiterbarkeit für neue Features
- Integration mit externen Systemen (Monday.com, Klenty, etc.)
- Wartbarkeit des Codes
- Performance-Optimierung

Siehe [Vision und Roadmap][vision] für Details zu geplanten Integrationen und Features.

## 9. Sprint 1 Status und Erfolge

### Team FRONT - Production-Ready Keycloak Integration ✅

**Erfolgreich abgeschlossen am 07.01.2025:**

1. **Keycloak-Integration für Production:**
   - ✅ Frontend konfiguriert für auth.z-catering.de
   - ✅ Runtime-Konfiguration implementiert (Docker-ready)
   - ✅ Automatisches Token-Refresh mit Axios Interceptors
   - ✅ React Query für optimales Caching

2. **Docker-Setup erstellt:**
   - ✅ Multi-Stage Dockerfile mit Nginx
   - ✅ Runtime-Konfiguration ohne Rebuilds
   - ✅ docker-compose.yml für lokales Testing
   - ✅ Optimierte Nginx-Konfiguration für SPAs

3. **Komponenten implementiert:**
   - ✅ UserList mit neuen Rollen (admin, manager, sales)
   - ✅ ErrorBoundary für globale Fehlerbehandlung
   - ✅ UserListSkeleton für Loading-States
   - ✅ Keycloak-Provider mit Auto-Refresh

4. **CI/CD Pipeline:**
   - ✅ GitHub Actions Workflow für Frontend
   - ✅ Automatische Tests mit Vitest
   - ✅ Build-Artefakte werden gespeichert

**Nächste Schritte:**
- Warten auf Keycloak-Realm-Erstellung auf auth.z-catering.de
- Legacy-Features migrieren (Calculator, Customer-Module)
- CSS/Design aus Legacy-App übernehmen

## 10. Claude's Dokumentations-Ablagestruktur

**🚨 VERBINDLICHE REGEL: Strukturierte Dokumentationsablage**

Ab sofort gilt für ALLE von Claude erstellten Dokumentationen:

### 🆕 Feature-Konzept-Prozess (Ab 07.07.2025)
Für jedes größere Feature erstellen wir ein separates technisches Konzeptdokument:

1. **Feature Code vergeben**: FC-XXX (fortlaufende Nummerierung)
2. **Technisches Konzept erstellen**: `/docs/features/YYYY-MM-DD_TECH_CONCEPT_feature-name.md`
3. **Template nutzen**: `/docs/templates/TECH_CONCEPT_TEMPLATE.md`
4. **Master Plan aktualisieren**: Nur Verweis auf Detail-Dokument, keine Details im Master Plan

**Vorteile:**
- Master Plan bleibt übersichtlich
- Detaillierte technische Diskussionen im Kontext
- Versionierte Feature-Historie
- Bessere Nachvollziehbarkeit von Entscheidungen

**🔄 AKTUALISIERUNGS-PFLICHT:**
Bei JEDER Übergabe MÜSSEN alle aktiven Feature-Konzepte (FC-XXX) auf ihren aktuellen Implementierungs-Status überprüft und aktualisiert werden! Dies beinhaltet:
- Status-Updates (✅ Abgeschlossen / 🔄 In Arbeit / ⏸️ Pausiert)
- Neue implementierte Komponenten
- Behobene Probleme
- Nächste Schritte

**🆕 MASTER PLAN SYNC (ab 22.07.2025):**
```bash
# Automatische Synchronisation des V5 Master Plans mit aktuellem Stand
./scripts/sync-master-plan.sh

# Erweiterte Übergabe mit automatischem Sync
./scripts/handover-with-sync.sh
```
**WICHTIG:** Der V5 Master Plan wird jetzt automatisch synchronisiert und zeigt den echten Projekt-Stand!

### 🔒 KRITISCHE DOKUMENTE - NIEMALS LÖSCHEN:
- `/docs/TRIGGER_TEXTS.md` - Offizielle Trigger-Texte für Übergaben
- `/docs/STANDARDUBERGABE_NEU.md` - Hauptprozess-Dokument
- `/docs/STANDARDUBERGABE_KOMPAKT.md` - Quick Reference
- `/docs/STANDARDUBERGABE.md` - Troubleshooting Guide
- `/docs/CLAUDE.md` - Diese Arbeitsrichtlinien
- `/docs/CRM_COMPLETE_MASTER_PLAN.md` - Aktueller Masterplan

**Backup:** `./scripts/backup-critical-docs.sh` regelmäßig ausführen!

### Ablagestruktur:
```
docs/
├── features/                    # Technische Feature-Konzepte (FC-XXX)
├── templates/                   # Wiederverwendbare Templates
├── claude-work/
│   ├── daily-work/YYYY-MM-DD/  # Tägliche Arbeit
│   ├── implementations/         # Umgesetzte Features
│   ├── reviews/                 # Code-Reviews
│   ├── proposals/               # Vorschläge
│   ├── maintenance/             # Wartungsarbeiten
│   └── learning/                # Erkenntnisse
└── adr/                         # Architecture Decision Records
```

### Namenskonvention:
`YYYY-MM-DD_<KATEGORIE>_<BESCHREIBUNG>.md`

Beispiel: `2025-06-08_IMPL_dynamic-links.md`

### Kategorien:
- `IMPL_` - Implementation
- `FIX_` - Bug-Fix  
- `REVIEW_` - Review/Analyse
- `CLEANUP_` - Aufräumarbeiten
- `PROPOSAL_` - Vorschlag

### ⚠️ Wichtig:
- **NIEMALS** Dokumente direkt in `/docs/` ablegen!
- **IMMER** Datum und Kategorie im Namen
- **IMMER** in passenden Unterordner ablegen

Vollständige Dokumentation: [Claude Documentation Structure][claude-doc-structure]

---

## 🚨 PROBLEMLÖSUNGEN FÜR CLAUDE

### Backend-Fix {#backend-fix}

**Problem:** Backend nicht erreichbar (Failed to fetch, Connection refused)  
**Ursache:** Java-Version + Timeout-Issue

**SOFORT-LÖSUNG:**
```bash
# 1. Java 17 setzen (KRITISCH!)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 2. Backend persistent starten (NIEMALS mit Timeout!)
cd backend
nohup mvn quarkus:dev > ../logs/backend.log 2>&1 &

# 3. Warten und testen (30 Sekunden)
sleep 30 && curl http://localhost:8080/q/health
```

**Oder verwende das Management-Script:**
```bash
./scripts/backend-manager.sh start
./scripts/backend-manager.sh status
```

---

### Testdaten-Fix {#testdaten-fix}

**Problem:** "Keine Kunden gefunden" aber Backend antwortet  
**Ursache:** Datenbank leer, CustomerDataInitializer nicht gelaufen

**LÖSUNG:**
```bash
# Backend neu starten um CustomerDataInitializer zu triggern
./scripts/backend-manager.sh restart

# Nach 30s testen:
curl http://localhost:8080/api/customers
```

---

### Auth-Fix {#auth-fix}

**Problem:** Tests schlagen fehl mit 401 Unauthorized  
**Ursache:** Security-Konfiguration oder @TestSecurity fehlt

**LÖSUNG:**
```bash
# 1. Prüfe application.properties
grep "dev.quarkus.oidc.enabled" backend/src/main/resources/application.properties
# Sollte: %dev.quarkus.oidc.enabled=false

# 2. Für Tests: @TestSecurity hinzufügen
@TestSecurity(user = "testuser", roles = {"admin", "manager", "sales"})

# 3. Quarkus 3.17.4 verwenden (für @TestSecurity Support)
```

---

### Automatische Diagnose

**Bei JEDEM unklaren Problem zuerst ausführen:**
```bash
./scripts/diagnose-problems.sh
```

Dieses Script erkennt automatisch:
- Backend Status (up/down)
- API Erreichbarkeit
- Datenbank Status (leer/gefüllt)
- Java Version (17 check)
- PostgreSQL Status

[claude-doc-structure]: ./docs/CLAUDE_DOCUMENTATION_STRUCTURE.md