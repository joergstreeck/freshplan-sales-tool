# 🏗️ FreshPlan Coding Standards

**Zweck:** Detaillierte Code-Standards für konsistente, wartbare Entwicklung
**Zielgruppe:** Entwickler (Claude + Team)
**Aktualisiert:** 2025-09-18

## 🎯 Grundprinzipien

1. **Clean Code** - Lesbarkeit geht vor Cleverness
2. **SOLID Principles** - Jede Klasse hat EINE klare Verantwortung
3. **DRY** - Don't Repeat Yourself, aber nicht auf Kosten der Klarheit
4. **KISS** - Keep It Simple, Stupid
5. **YAGNI** - You Aren't Gonna Need It (keine vorzeitige Abstraktion)

## 🏛️ Backend-Architektur (Quarkus/Java)

### Legacy-Struktur (wird schrittweise migriert):
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

### Neue Modulare Architektur (ab 09.07.2025):
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

## 🎨 Frontend-Architektur (React/TypeScript)

### TypeScript Import/Export Strategie (KRITISCH bei Vite):
Bei `verbatimModuleSyntax: true` in tsconfig.json MÜSSEN alle Type-Imports explizit sein:

```typescript
// ✅ RICHTIG - Direkte Exports
export interface FieldDefinition { ... }
export type FieldCatalog = { ... }

// ✅ RICHTIG - Type Imports verwenden
import type { FieldDefinition, FieldCatalog } from './types';

// ❌ FALSCH - Keine Re-Exports für Types
type Foo = { ... }
export { Foo };  // NICHT SO!

// ❌ FALSCH - Normale Imports für Types
import { FieldDefinition } from './types';  // Führt zu Build-Fehlern!
```

### Projekt-Struktur:
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

## 📏 Code-Lesbarkeit und Zeilenlänge

**Warum kurze Zeilen?**
- **Bessere Lesbarkeit**: Kurze Zeilen lassen sich schneller erfassen
- **Vergleichbarkeit**: In Code-Review-Tools einfacher zu vergleichen
- **Kompatibilität**: Viele Editoren zeigen lange Zeilen nicht vollständig an

**Standards:**
- **Java**: 100 Zeichen (Google Java Style Guide)
- **TypeScript/JavaScript**: 80-100 Zeichen
- **Markdown**: 80 Zeichen für bessere Diff-Ansichten

### Praktische Techniken:

#### 1. Zeilenumbrüche nutzen:
```java
// Schlecht (zu lang)
if (user.isActive() && user.hasPermission("admin") && user.getLastLogin().isAfter(yesterday)) {

// Gut (umgebrochen)
if (user.isActive()
        && user.hasPermission("admin")
        && user.getLastLogin().isAfter(yesterday)) {
```

#### 2. Hilfsvariablen verwenden:
```java
// Schlecht
if (userRepository.findByEmail(email).isPresent() && userRepository.findByEmail(email).get().isActive()) {

// Gut
Optional<User> userOpt = userRepository.findByEmail(email);
boolean isActiveUser = userOpt.isPresent() && userOpt.get().isActive();
if (isActiveUser) {
```

#### 3. Funktionen auslagern:
```java
// Schlecht
if (user.getAge() >= 18 && user.hasVerifiedEmail() && user.getCountry().equals("DE")) {

// Gut
if (isEligibleForService(user)) {

private boolean isEligibleForService(User user) {
    return user.getAge() >= 18
            && user.hasVerifiedEmail()
            && user.getCountry().equals("DE");
}
```

## 🏷️ Naming Conventions

- **Klassen**: PascalCase, beschreibende Nomen (`UserService`, `OrderRepository`)
- **Interfaces**: PascalCase, KEIN "I" Präfix (`UserRepository`, nicht `IUserRepository`)
- **Methoden**: camelCase, Verben (`createUser`, `findByEmail`)
- **Variablen**: camelCase, beschreibend (`userEmail`, nicht nur `email`)
- **Konstanten**: UPPER_SNAKE_CASE (`MAX_RETRY_ATTEMPTS`)
- **Dateien**: Wie die Hauptklasse (`UserService.java`, `UserList.tsx`)
- **Packages/Folders**: lowercase, Singular (`user`, nicht `users`)

## 📖 Dokumentation Standards

### JavaDoc/JSDoc:
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

## 🚨 Error Handling

```java
// Domain Exception mit klarer Bedeutung
public class UserNotFoundException extends RuntimeException {
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

## 🏗️ Design Patterns

### DTO Design:
```java
// Immutable DTOs mit Builder Pattern
public final class UserResponse {
    private final UUID id;
    private final String username;

    private UserResponse(Builder builder) { /*...*/ }

    public UUID getId() { return id; }

    public static Builder builder() { return new Builder(); }
}
```

### Repository Pattern:
```java
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return find("username", username).firstResultOptional();
    }
}
```

### Service Layer:
```java
@ApplicationScoped
@Transactional
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Inject
    public UserService(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public UserResponse createUser(CreateUserRequest request) {
        // 1. Validation
        // 2. Business Rules
        // 3. Persistence
        // 4. Response Mapping
    }
}
```

## 🧪 Testing Standards

```java
// Test-Struktur: Given-When-Then
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

## 🔒 Security Best Practices

- **Keine Hardcoded Secrets** - Nutze Environment Variables
- **Input Validation** auf allen Ebenen (DTO, Service, Repository)
- **Prepared Statements** automatisch durch JPA/Panache
- **@RolesAllowed** für Authorization
- **CORS** nur für erlaubte Origins

## ⚡ Performance Guidelines

- **Lazy Loading** für Collections (`@OneToMany(fetch = FetchType.LAZY)`)
- **Pagination** für alle Listen (`Page`, `Pageable`)
- **Query Optimization** mit Named Queries
- **Caching** wo sinnvoll (`@CacheResult`)
- **Database Indexes** in Flyway Migrations

## 📊 Code-Qualitäts-Metriken

- **Test Coverage**: Minimum 80% für neue Features
- **Cyclomatic Complexity**: Max 10 pro Methode
- **Method Length**: Max 20 Zeilen (ideal < 10)
- **Class Length**: Max 200 Zeilen
- **Package Dependencies**: Keine zirkulären Abhängigkeiten

## 🔄 Git Workflow

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
```

## 🎯 Continuous Improvement

- **Code Reviews** sind Lernmöglichkeiten
- **Refactoring** ist Teil jeder Story
- **Tech Debt** wird dokumentiert und priorisiert
- **Pair Programming** für komplexe Features
- **Knowledge Sharing** in Team-Sessions