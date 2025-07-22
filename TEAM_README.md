# Team BACK Workspace

## Quick Start
```bash
cd backend
./mvnw quarkus:dev
```

## Aktueller Sprint – User Management API
- [x] User CRUD Endpoints  
- [x] Repository Pattern mit Panache
- [x] PUT /api/users/{id}/roles implementiert
- [x] Role Validation (admin, manager, sales, viewer)
- [x] PostgreSQL Migration (V3__add_user_roles.sql)
- [x] Comprehensive Test Coverage

## API Endpoints (Sprint 1)
```bash
# User Management
GET    /api/users           # List all users
GET    /api/users/{id}      # Get user by ID  
POST   /api/users           # Create new user
PUT    /api/users/{id}      # Update user
DELETE /api/users/{id}      # Delete user
PUT    /api/users/{id}/roles # Update user roles (NEW!)

# System
GET    /api/ping            # Health check
```

## Development
```bash
# Dev Mode mit Hot-Reload
./mvnw quarkus:dev

# Alle Tests
./mvnw test

# Nur Backend Tests
./mvnw -Pgreen clean verify
```

## Kontakt
- Lead: Team BACK (@Max)
- Slack: #team-back
- Wiki: [Backend Architecture](/docs/features/docs/adr/)