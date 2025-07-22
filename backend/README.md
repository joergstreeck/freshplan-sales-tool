# FreshPlan 2.0 - Backend (Quarkus)

**📅 Aktuelles Datum: <!-- AUTO_DATE --> (System: 08.06.2025)**

## 🚀 Quarkus REST API

Enterprise-grade backend for the FreshPlan 2.0 Sales Platform.

### Tech Stack
- **Quarkus 3.7.1** - Supersonic Subatomic Java
- **Java 17** - LTS Version ⚠️ **REQUIRED - Tests fail with Java 24**
- **RESTEasy Reactive** - Non-blocking REST endpoints
- **Hibernate ORM with Panache** - Simplified ORM
- **PostgreSQL** - Primary database
- **Flyway** - Database migrations
- **Keycloak OIDC** - Authentication & Authorization
- **SmallRye OpenAPI** - API documentation

### Prerequisites

1. **Docker** (for local infrastructure)
2. **Java 17** (REQUIRED - ByteBuddy in Quarkus 3.7.1 doesn't support Java 24)
3. **Maven 3.9+** (or use included `./mvnw`)

⚠️ **Java Version Hinweis:**
```bash
# Prüfe deine Java Version
java -version

# Falls Java 24 installiert ist, wechsle zu Java 17:
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Oder installiere Java 17:
brew install openjdk@17
```

### Quick Start

```bash
# 1. Start local infrastructure (PostgreSQL + Keycloak)
cd ../infrastructure
./start-local-env.sh

# 2. Create Maven wrapper (first time only)
mvn wrapper:wrapper -Dmaven=3.9.6

# 3. Run in development mode
./mvnw quarkus:dev

# API will be available at http://localhost:8080
# Swagger UI at http://localhost:8080/q/swagger-ui
```

### Available Endpoints

- `GET /api/ping` - Health check endpoint (requires authentication)
- `GET /openapi` - OpenAPI 3.0 specification
- `GET /q/health` - Quarkus health checks
- `GET /q/metrics` - Application metrics

### Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

### Configuration

Key configuration in `src/main/resources/application.properties`:
- Database: PostgreSQL on `localhost:5432`
- Keycloak: `http://localhost:8180/realms/freshplan`
- CORS: Enabled for `http://localhost:5173` (Frontend dev)

### Docker Build

```bash
# Build native executable (requires GraalVM)
./mvnw package -Pnative

# Or build JVM-based Docker image
./mvnw clean package
docker build -f src/main/docker/Dockerfile.jvm -t freshplan/backend .
```

### Environment Variables

- `KEYCLOAK_CLIENT_SECRET` - Keycloak client secret (required in production)
- `QUARKUS_DATASOURCE_JDBC_URL` - Override database URL
- `QUARKUS_OIDC_AUTH_SERVER_URL` - Override Keycloak URL

### Team

- Backend Team + ChatGPT AI Assistant

## 📚 Wichtige Dokumentation für Backend-Entwickler

### Essenzielle Docs (auch ohne Root-Zugriff)

#### 🏗️ Architektur & Standards
- **Domain-Driven Design**: Package-Struktur nach Domänen (users, calculator)
- **REST API Standards**: JAX-RS Annotations, konsistente Responses
- **Java Code Standards**: 
  - Max. 100 Zeichen pro Zeile
  - JavaDoc für alle public APIs
  - SOLID Principles

#### 🔧 Setup & Development
- **Local Development**:
  ```bash
  # PostgreSQL & Keycloak starten
  cd ../infrastructure && ./start-local-env.sh
  
  # Backend mit Hot-Reload
  ./mvnw quarkus:dev
  ```
- **Test-Daten**: DevDataInitializer erstellt 6 Test-User
- **Dev-Endpoints**: `/api/dev/users` für Mock-JWT-Tokens

#### 🌐 API Dokumentation
- **OpenAPI/Swagger**: http://localhost:8080/q/swagger-ui
- **Endpoints**:
  ```
  GET    /api/ping              # Health Check
  GET    /api/users             # List Users (Auth required)
  POST   /api/users             # Create User
  PUT    /api/users/{id}        # Update User
  DELETE /api/users/{id}        # Delete User
  POST   /api/calculator        # Calculator Service
  ```

#### 🧪 Testing
- **Unit Tests**: `./mvnw test`
- **Integration Tests**: `*IT.java` Dateien
- **Test Coverage**: Min. 80% für neue Features
- **Mock JWT**: Siehe `DevUserResource` für Test-Tokens

#### ⚠️ Known Issues
- Keycloak Mock-Mode: Nur für Development
- H2 Console: Deaktiviert in Production
- CORS: Nur localhost:5173 erlaubt

#### 🚀 Deployment
- **JVM Build**: `./mvnw clean package`
- **Native Build**: `./mvnw package -Pnative`
- **Docker**: `docker build -f src/main/docker/Dockerfile.jvm`

### Wo finde ich was? (Backend-Perspektive)
```
backend/
├── src/main/java/de/freshplan/
│   ├── api/              # REST Resources
│   ├── domain/           # Business Logic
│   │   ├── user/         # User Domain
│   │   └── calculator/   # Calculator Domain
│   └── infrastructure/   # Technical Services
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/     # Flyway Scripts
└── src/test/             # Tests
```

### Support & Hilfe
- **Team Chat**: Slack #backend-dev
- **API Changes**: Immer API_CONTRACT.md updaten
- **DB Changes**: Flyway Migration erstellen

## 📖 Quick Links (Backend-spezifisch)

- Database Guide - Schema in src/main/resources/db/migration/
- API Testing Guide - Swagger UI unter http://localhost:8080/q/swagger-ui
- Test Coverage Report - Führe `./mvnw test` aus
- Backend Team + ChatGPT AI Assistant

### Sprint 0 Status
- ✅ Quarkus project initialized
- ✅ OIDC security configured
- ✅ PostgreSQL + Flyway setup
- ✅ `/api/ping` endpoint
- ✅ OpenAPI documentation
- ✅ Basic test infrastructure
- 🚧 Keycloak realm configuration (manual step)