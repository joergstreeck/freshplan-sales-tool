# The FreshPlan Way - Unser Weg zu exzellenter Software

> **"Code, den jeder Entwickler sofort versteht - KEINE KOMPROMISSE!"**

## 🔍 NEU: Code-Review-Regel (ab 07.01.2025)

**GOLDENE REGEL:** Bei jedem bedeutenden Entwicklungsabschnitt gilt:
> "Prüfe noch einmal sehr gründlich den Code auf Einhaltung unserer Programmierregeln und Logik"

Diese Regel ist **VERPFLICHTEND** und wird automatisch bei jedem Sprint-Ende durchgeführt. Details siehe CLAUDE.md Abschnitt 0.10.

## 🎯 Unsere Mission

Wir entwickeln FreshPlan 2.0 als moderne, cloud-native Enterprise-Lösung mit höchsten Qualitätsstandards. Dieses Dokument definiert unsere Arbeitsweise und ist für ALLE Projektbeteiligten verbindlich.

## 📋 Inhaltsverzeichnis

1. [Grundprinzipien](#grundprinzipien)
2. [Code-Qualität](#code-qualität)
3. [Architektur](#architektur)
4. [Development Workflow](#development-workflow)
5. [Testing](#testing)
6. [Security & Compliance](#security--compliance)
7. [DevOps & Releases](#devops--releases)
8. [Team-Rituale](#team-rituale)
9. [Aktueller Projektstatus](#aktueller-projektstatus)

---

## Grundprinzipien

### Die 5 Säulen unserer Entwicklung

1. **Clean Code** - Lesbarkeit geht vor Cleverness
2. **SOLID Principles** - Jede Klasse hat EINE klare Verantwortung
3. **Test-Driven** - Kein Code ohne Tests
4. **Security-First** - Sicherheit von Anfang an
5. **Performance-Aware** - Geschwindigkeit ist ein Feature

### Unser Qualitätsversprechen

Bei jedem Commit verpflichten wir uns zu:

- **Sauberkeit**: Klare Modul-Grenzen, sprechende Namen, keine toten Pfade
- **Robustheit**: Umfassende Tests, defensives Error-Handling
- **Wartbarkeit**: SOLID, DRY, CI-Checks, lückenlose Dokumentation
- **Transparenz**: Unklarheiten sofort ansprechen

---

## Code-Qualität

### Naming Conventions

| Element | Convention | Beispiel |
|---------|------------|----------|
| Klassen | PascalCase | `UserService`, `OrderRepository` |
| Methoden | camelCase (Verben) | `createUser`, `findByEmail` |
| Variablen | camelCase (beschreibend) | `userEmail`, `totalAmount` |
| Konstanten | UPPER_SNAKE_CASE | `MAX_RETRY_ATTEMPTS` |
| Packages | lowercase, Singular | `user`, `order` |

### Code-Lesbarkeit und Zeilenlänge

**Maximale Zeilenlänge**: 80-120 Zeichen

**Warum?**
- Bessere Lesbarkeit und schnelleres Erfassen
- Einfacherer Code-Review und Diff-Ansichten
- Kompatibilität mit allen Bildschirmen

**Best Practices:**

1. **Zeilenumbrüche bei langen Bedingungen:**
```java
// ❌ Schlecht
if (user.isActive() && user.hasPermission("admin") && user.getLastLogin().isAfter(yesterday)) {

// ✅ Gut
if (user.isActive()
        && user.hasPermission("admin")
        && user.getLastLogin().isAfter(yesterday)) {
```

2. **Hilfsvariablen für Klarheit:**
```java
// ❌ Schlecht
if (userRepository.findByEmail(email).isPresent() && userRepository.findByEmail(email).get().isActive()) {

// ✅ Gut
Optional<User> userOpt = userRepository.findByEmail(email);
boolean isActiveUser = userOpt.isPresent() && userOpt.get().isActive();
if (isActiveUser) {
```

3. **Method Chaining aufteilen:**
```java
// ✅ Gut
UserResponse response = userService
        .findById(id)
        .map(mapper::toResponse)
        .orElseThrow(() -> new UserNotFoundException(id));
```

### Code-Standards

```java
/**
 * Service layer for User management operations.
 * 
 * Clear purpose, clean API, comprehensive docs.
 * 
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class UserService {
    // Constructor Injection only
    private final UserRepository repository;
    
    // Clear method names, single responsibility
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Validate
        // 2. Apply business rules
        // 3. Persist
        // 4. Return response
    }
}
```

### Metriken

- **Test Coverage**: ≥ 80% für neue Features
- **Cyclomatic Complexity**: ≤ 10 pro Methode
- **Method Length**: ≤ 20 Zeilen
- **Class Length**: ≤ 200 Zeilen

---

## Architektur

### Backend (Quarkus/Java)

```
backend/
├── api/                    # REST Layer
│   └── exception/         # Exception Handling
├── domain/                # Business Domain
│   └── [aggregate]/      # z.B. user, order
│       ├── entity/       # JPA Entities
│       ├── repository/   # Data Access
│       ├── service/      # Business Logic
│       │   ├── dto/     # DTOs
│       │   └── mapper/  # Mappings
│       └── validation/   # Validators
└── infrastructure/        # Technical Details
```

### Frontend (React/TypeScript)

```
frontend/
├── components/            # Reusable UI
│   ├── common/           # Generic
│   └── domain/           # Domain-specific
├── features/             # Feature Modules
│   └── [feature]/       
│       ├── components/   
│       ├── hooks/        
│       └── services/     
├── pages/                # Route Pages
└── services/             # Global Services
```

---

## Development Workflow

### Branch-Strategie

- **Trunk-based Development**: Feature Branches max. 24h
- **Branch Naming**: `feature/`, `bugfix/`, `hotfix/`
- **Commit Convention**: [Conventional Commits](https://www.conventionalcommits.org/)

```bash
# Beispiel Workflow
git checkout -b feature/user-management
# ... Entwicklung ...
git commit -m "feat(user): Add email validation"
git push origin feature/user-management
# Create Pull Request
```

### Pull Request Checklist

- [ ] Tests sind grün (Coverage ≥ 80%)
- [ ] Keine Security Warnings
- [ ] Performance Budget eingehalten
- [ ] Dokumentation aktualisiert
- [ ] Code Review durch min. 1 Person
- [ ] Screenshots bei UI-Änderungen

---

## Testing

### Testing-Pyramide

| Level | Coverage | Tools | Focus |
|-------|----------|-------|-------|
| Unit | 80% | JUnit/Vitest | Business Logic |
| Integration | 100% API | RestAssured | Contracts |
| E2E | Happy Paths | Playwright | User Journeys |
| Performance | Critical | k6 | Load Testing |

### Test-Pattern

```java
@Test
void createUser_withValidData_shouldReturnCreatedUser() {
    // Given - Arrange
    var request = validUserRequest();
    
    // When - Act
    var response = userService.createUser(request);
    
    // Then - Assert
    assertThat(response)
        .isNotNull()
        .hasFieldOrPropertyWithValue("username", "john.doe");
}
```

### Feature Flag Governance

**Trunk-based Development = Feature Flags sind PFLICHT!**

#### Namenskonvention
```
ff_<ticket-nr>_<kurzer-name>
Beispiel: ff_FRESH-123_user_export
```

#### Regeln
- **Sunset Date**: Max. 30 Tage nach Erstellung
- **Owner**: Team/Person muss definiert sein
- **Metrics**: Jedes Flag braucht Monitoring
- **Cleanup**: Automatische Removal PRs

#### CI-Enforcement
- Flags > 30 Tage: ⚠️ Warning
- Flags > 60 Tage: ❌ Build Failure
- Ungenutztes Flag: 🤖 Auto-PR zur Entfernung

---

## Security & Compliance

### Security-Standards

| Bereich | Maßnahme | Tool |
|---------|----------|------|
| Dependencies | Vulnerability Scanning | Snyk/Dependabot |
| Code | SAST Analysis | SonarCloud |
| API | OWASP Top 10 | ZAP |
| Secrets | Secret Scanning | GitGuardian |

### Compliance

- **DSGVO**: Verschlüsselung personenbezogener Daten
- **Audit-Trail**: Logging aller kritischen Operationen
- **Data Retention**: Automatische Löschfristen

---

## DevOps & Releases

### CI/CD Pipeline

```yaml
Stages:
1. Lint & Format
2. Unit Tests
3. Build
4. Integration Tests
5. Security Scan
6. Deploy Stage
7. E2E Tests
8. Performance Tests
9. Deploy Production (manual)
```

### Release-Strategie

- **Versioning**: [Semantic Versioning](https://semver.org/)
- **Releases**: Automated via GitHub Actions
- **Rollback**: Immer möglich binnen 5 Minuten

### Performance Budgets

#### Frontend
- Bundle Size: ≤ 200KB (gzipped)
- LCP: ≤ 2.5s
- Lighthouse Score: ≥ 90

#### Backend
- API Response: P95 < 200ms
- Startup Time: < 10s

---

## Team-Rituale

### Wöchentlicher Rhythmus

| Tag | Aktivität | Dauer |
|-----|-----------|-------|
| Montag | Sprint Planning | 2h |
| Täglich | Standup | 15min |
| Mittwoch | Tech Debt Review | 1h |
| Freitag | Refactoring + Demo | 3h |

### Knowledge Sharing

- **Pair Programming**: Min. 4h/Woche
- **Tech Talks**: Jeden 2. Freitag
- **Documentation Day**: 1x/Monat

### Incident Management

#### Severity Matrix

| Level | Impact | Beispiele | Response | Eskalation |
|-------|--------|-----------|----------|------------|
| **SEV-1** | Totalausfall | Login down, Datenverlust, Security Breach | < 15 Min | CTO + On-Call |
| **SEV-2** | Major Feature | Payment kaputt, API > 5s, Keine Orders | < 1 Std | Team Lead |
| **SEV-3** | Minor Feature | PDF fehlt, Firefox Bug, Slow Query | < 4 Std | Team Slack |
| **SEV-4** | Kosmetik | Typo, Logging, Warnings | Sprint | Jira Ticket |

#### On-Call
- Rotation: 1 Woche (Mo-So)
- Erreichbar: 30 Min
- Backup: Immer definiert
- Ausgleich: 1 Tag/Woche

---

## Aktueller Projektstatus

### Sprint 1: User Management (Aktuell)

**Zeitraum**: 06.01. - 09.01.2025

**Ziele**:
- [x] User Entity & Repository
- [ ] REST API für User CRUD
- [ ] Frontend User-Liste
- [ ] Keycloak Integration
- [ ] E2E Tests

**Team**:
- Backend: [Name]
- Frontend: [Name]
- DevOps: [Name]

### Nächste Sprints

- **Sprint 2**: Customer Management Migration
- **Sprint 3**: Calculator Feature
- **Sprint 4**: PDF Generation

### Metriken Dashboard

| Metrik | Aktuell | Ziel |
|--------|---------|------|
| Test Coverage | 0% | 80% |
| Tech Debt Ratio | 0% | <5% |
| Build Zeit | - | <5min |
| Deploy Zeit | - | <10min |

---

## 📝 Änderungshistorie

| Datum | Version | Änderung | Autor |
|-------|---------|----------|-------|
| 06.01.2025 | 1.0 | Initial Version | Team |

---

## 🤝 Commitment

Mit meiner Arbeit am FreshPlan-Projekt verpflichte ich mich, diese Standards einzuhalten und kontinuierlich zur Verbesserung beizutragen.

**"Wir bauen Software, auf die wir stolz sind!"** 🚀