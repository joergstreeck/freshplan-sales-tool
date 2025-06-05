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