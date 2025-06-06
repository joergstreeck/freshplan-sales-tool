# Changelog

Alle bemerkenswerten Änderungen an diesem Projekt werden in dieser Datei dokumentiert.

Das Format basiert auf [Keep a Changelog](https://keepachangelog.com/de/1.0.0/),
und dieses Projekt hält sich an [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0-T1-green] - 2025-01-06

### Added
- **Expand-Path T-1** ✅ - Repository-Schicht Tests aktiviert
  - UserRepositoryTest mit 14 Tests erfolgreich integriert
  - Gesamt: 17 Tests (3 Green Path + 14 Repository)
  - CI bleibt stabil grün

### Technical
- Maven Profile `green` erweitert um `**/*RepositoryTest.java`
- Alle Repository-Tests laufen ohne Anpassungen durch

## [2.0.0-beta-green] - 2025-01-06

### Added
- **Green Path CI Strategy** ✅ - CI durchläuft mit minimaler Suite
  - Maven Profile `green` für selektive Test-Ausführung
  - 3 Smoke-Tests implementiert:
    - `AppStartsIT`: Verifiziert Application-Start und Health-Endpoint
    - `UserRepoSaveLoadIT`: Testet Repository Save/Load Funktionalität
    - `UserServiceUpdateIT`: Prüft Service-Update mit Timestamp-Verifikation
  - `quarkus-smallrye-health` Dependency für Health-Checks

### Fixed
- Entity Lifecycle Management mit EntityManager flush/clear Pattern
- Timestamp-Synchronisation bei Create/Update Operationen
- Validation-Constraints in Tests (Username min. 3 Zeichen)

### Technical
- CI-Workflows nutzen jetzt das `green` Profile mit `CI_GREEN` Environment Variable
- Basis für schrittweisen Ausbau der Test-Suite geschaffen

## [2.0.0-alpha] - 2025-01-05

### Added
- Initial User Management Feature
  - User Entity mit UUID Primary Key
  - UserRepository mit Panache
  - UserService mit CRUD-Operationen
  - REST API Endpoints für User-Verwaltung
  - Exception Mapper für konsistente Error Responses

### Technical
- Quarkus 3.7.1 Backend Setup
- PostgreSQL mit Flyway Migrations
- JUnit 5 + AssertJ + Mockito Test-Setup
- GitHub Actions CI/CD Pipeline