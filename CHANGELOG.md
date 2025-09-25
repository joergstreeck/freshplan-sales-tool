# Changelog

Alle bemerkenswerten Änderungen an diesem Projekt werden in dieser Datei dokumentiert.

Das Format basiert auf [Keep a Changelog](https://keepachangelog.com/de/1.0.0/),
und dieses Projekt hält sich an [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Sprint 1.6] - 2025-09-25

### Added
- RLS Module Adoption: @RlsContext CDI-Interceptor für alle DB-Services
- CI-Guard für RLS-Compliance (tools/rls-guard.sh)
- RLS-Badge in allen Module 01-08 Technical Concepts
- ADR-0007 für RLS Connection Affinity Pattern
- RLS_COMPLIANCE_CHECKLIST.md für Migration Guide

### Fixed
- Modul 02 Services ohne @RlsContext (P0 - blockierte Sprint 2.1)
- 5 Lead-Services mit @RlsContext annotiert
- GAP-Analysis dokumentiert 33+ Services ohne RLS-Schutz

### Security
- Alle transaktionalen DB-Methoden in Modul 02 jetzt mit @RlsContext
- Fail-closed Security Pattern durchgängig implementiert
- Connection Affinity garantiert für GUC-Variablen

## [Sprint 2.1] - 2025-09-25

### Added
- Territory Management (ohne Gebietsschutz) für Währung/Steuer/Business Rules
- Thread-sicherer `UserLeadSettingsService` mit echter `@Transactional`-Unterstützung (Pessimistic Locking)
- Lead-Verwaltung mit User-Protection (6M/60T/10T) inkl. State-Machine und Stop-the-Clock
- Dev/Prod-Migrationspfade getrennt (Flyway Locations), CORS nur im Dev-Profil aktiv
- REST-Endpoints für Territory-Verwaltung mit ETag-Support

### Fixed
- Fehlende `@ElementCollection`-Join-Tabellen (Migration **V231**)
- Dev-Migrationskonflikte (Policies) mit `DROP IF EXISTS`
- Backend-Startup-Probleme (Hibernate validate), CORS-Fehler Frontend↔Backend
- Race Conditions durch statische Methode + `@Transactional` eliminiert

### Quality
- 85%+ Test Coverage (Lead/Territory), 100% für neuen UserLeadSettingsService
- Spotless-Formatierung, PR-Template-Compliance, CI grün
- Comprehensive Tests inkl. Race-Condition-Case

### Technical
- Migrations: V229 (Territory & Lead Schema), V230 (Remove redundant triggers), V231 (ElementCollection tables)
- JPA Entities mit Panache für Lead-Domain
- CORS-Konfiguration nur im Dev-Profil aktiv
- Pessimistic Locking für Thread-Safety

## [2.0.0-T3-green] - 2025-01-06

### Added
- **Expand-Path T-3** ✅ - REST Controller Tests aktiviert
  - UserResourceTest und UserResourceIT mit vollständiger API-Test-Coverage
  - Search-Endpoint `/api/users/search?email=` implementiert
  - Gesamt: 55 Tests (alle grün!)
  - CI bleibt weiterhin stabil grün

### Fixed
- Mock-Setup in UserServiceTest mit Answer-Pattern für unterschiedliche Responses
- Transaction-Handling in UserResourceIT mit @Transactional
- REST-konforme Status-Codes (204 No Content für enable/disable)
- ValidationExceptionMapper für konsistente Validation-Error-Responses

### Technical
- Maven Profile `green` erweitert um `**/*Resource*Test.java` und `**/*Resource*IT.java`
- @UpdateTimestamp Annotation eliminiert manuelle Timestamp-Updates
- ErrorResponse-Konstruktor mit korrekten Parametern

## [2.0.0-T2-green] - 2025-01-06

### Added
- **Expand-Path T-2** ✅ - Service-Layer Tests aktiviert
  - UserServiceTest mit Mock-basierten Unit-Tests erfolgreich integriert
  - Gesamt: ~30 Tests (17 aus T-1 + ~13 Service-Tests)
  - CI bleibt weiterhin stabil grün

### Technical
- Maven Profile `green` erweitert um `**/*Service*Test.java` und `**/*Service*IT.java`
- Service-Tests nutzen Mockito für isolierte Unit-Tests

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