# Arbeitsrichtlinien für Claude im FreshPlan Sales Tool Projekt

## 0. Grundlegende Arbeitsphilosophie

**🎯 UNSERE DEVISE: GRÜNDLICHKEIT GEHT VOR SCHNELLIGKEIT**

- Jede Implementierung muss gründlich getestet werden
- Keine Quick-Fixes oder Workarounds ohne Dokumentation
- Denke immer an die zukünftigen Integrationen und Erweiterungen
- Was wir jetzt richtig machen, erspart uns später Arbeit
- Siehe `VISION_AND_ROADMAP.md` für die langfristige Ausrichtung des Projekts

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

### Git Workflow & Code Review:
```bash
# Feature Branch erstellen
git checkout -b feature/user-management

# Atomic Commits mit klaren Messages
git commit -m "feat(user): Add user creation endpoint

- Implement POST /api/users
- Add validation for email uniqueness
- Include unit and integration tests"

# Pull Request Checklist:
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
  - Template: `/docs/adr/template.md`
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
2. **Triage**: Severity 1-4
3. **Respond**: Runbook befolgen
4. **Resolve**: Fix + Deploy
5. **Review**: Blameless Postmortem

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

1.  **Sprache:** Deutsch.
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

* Siehe `KNOWN_ISSUES.md` für eine aktuelle Liste.
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

Siehe `VISION_AND_ROADMAP.md` für Details zu geplanten Integrationen und Features.