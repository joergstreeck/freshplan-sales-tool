# FreshPlan 2.0 - Backend (Quarkus)

## 🚀 Quarkus REST API

Enterprise-grade backend for the FreshPlan 2.0 Sales Platform.

### Tech Stack
- **Quarkus 3.7.1** - Supersonic Subatomic Java
- **Java 17** - LTS Version
- **RESTEasy Reactive** - Non-blocking REST endpoints
- **Hibernate ORM with Panache** - Simplified ORM
- **PostgreSQL** - Primary database
- **Flyway** - Database migrations
- **Keycloak OIDC** - Authentication & Authorization
- **SmallRye OpenAPI** - API documentation

### Prerequisites

1. **Docker** (for local infrastructure)
2. **Java 17** (for development)
3. **Maven 3.9+** (or use included `./mvnw`)

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

### Sprint 0 Status
- ✅ Quarkus project initialized
- ✅ OIDC security configured
- ✅ PostgreSQL + Flyway setup
- ✅ `/api/ping` endpoint
- ✅ OpenAPI documentation
- ✅ Basic test infrastructure
- 🚧 Keycloak realm configuration (manual step)