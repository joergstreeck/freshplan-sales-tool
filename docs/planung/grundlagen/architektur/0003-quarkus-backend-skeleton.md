# 3. Quarkus Backend Skeleton

Date: 2025-01-05

## Status

Accepted

## Context

Das Backend-Team ist aktuell in einem anderen Projekt gebunden. Um den Sprint 0 "Walking Skeleton" nicht zu blockieren, wurde entschieden, dass das Frontend-Team temporär das Backend-Skeleton erstellt.

## Decision

Wir erstellen ein minimales Quarkus-Backend mit folgenden Komponenten:
- Quarkus 3.7.1 als Framework
- OIDC/Keycloak für Authentication
- PostgreSQL als Datenbank
- Flyway für Datenbank-Migrationen
- Ein gesicherter `/api/ping` Endpoint als Proof of Concept
- Docker-Compose für lokale Entwicklungsumgebung

## Consequences

### Positive
- Sprint 0 kann ohne Verzögerung abgeschlossen werden
- Klare Trennung zwischen Frontend und Backend von Anfang an
- Backend-Team kann später nahtlos übernehmen
- CI/CD Pipeline ist von Anfang an aktiv

### Negative
- Temporäre Verantwortung beim Frontend-Team
- Eventuell Refactoring nötig, wenn Backend-Team andere Patterns bevorzugt

### Mitigation
- Minimaler Scope (nur /api/ping)
- Klare Dokumentation und Standard-Quarkus-Patterns
- Übergabe-Session mit Backend-Team geplant