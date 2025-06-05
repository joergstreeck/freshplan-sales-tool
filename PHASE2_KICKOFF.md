# Phase 2 Kickoff - FreshPlan Web App

## 🎯 Sprint 1 Goals (KW 10-11)

### Infrastructure Setup
- [ ] Keycloak Integration finalisieren
  - Realm: `freshplan`
  - Rollen: `admin`, `manager`, `sales`, `viewer`
  - Tenant-Attribute im Token
  
- [ ] Quarkus Backend Bootstrap
  ```bash
  mvn io.quarkus:quarkus-maven-plugin:create \
    -DprojectGroupId=com.freshplan \
    -DprojectArtifactId=freshplan-api \
    -Dextensions="resteasy-reactive-jackson,hibernate-orm-panache,jdbc-postgresql,oidc,smallrye-openapi"
  ```

- [ ] React Frontend Setup
  ```bash
  npm create vite@latest freshplan-web -- --template react-ts
  npm install @reduxjs/toolkit react-redux @mui/material @emotion/react @emotion/styled
  ```

### First Vertical Slice: Calculator
1. **Backend**: `/api/calculations` endpoint
2. **Frontend**: Calculator als React Component
3. **Auth**: Keycloak-geschützt
4. **Test**: E2E mit Playwright

## 📊 Architektur-Entscheidungen

| Bereich | Entscheidung | Begründung |
|---------|--------------|------------|
| Mandanten | Single-Realm + Attribute | Flexibilität & Einfachheit |
| DB-Isolation | Row-Level-Security | Performance & Wartbarkeit |
| Repo | Mono-Repo | Shared Code & CI/CD |
| Migration | Strangler-Fig | Risikoarm & iterativ |
| Tests | Playwright ab sofort | Zukunftssicher |

## 🏃‍♂️ Wochenziele

### Montag
- [x] Git-Tag v1.0.0-alpha.1
- [ ] Keycloak-Workshop (14:00)
- [ ] Tenant-Modell dokumentieren

### Dienstag  
- [ ] Quarkus-Projekt initialisieren
- [ ] OpenAPI-Spec für Calculator
- [ ] GitHub Actions für Backend

### Mittwoch
- [ ] React-Projekt Setup
- [ ] MUI Theme (FreshPlan Colors)
- [ ] Calculator Component (static)

### Donnerstag
- [ ] Keycloak-Integration Frontend
- [ ] API-Anbindung Calculator
- [ ] Erste E2E-Tests

### Freitag
- [ ] Demo vorbereiten
- [ ] Performance-Metriken
- [ ] Go/No-Go Entscheidung

## 🔗 Links & Resources

- [Quarkus + Keycloak Guide](https://quarkus.io/guides/security-oidc)
- [React + Keycloak](https://www.keycloak.org/securing-apps/react)
- [PostgreSQL RLS](https://www.postgresql.org/docs/current/ddl-rowsecurity.html)
- [MUI Components](https://mui.com/material-ui/getting-started/)

## 📝 Notizen für Daily Standup

```
Yesterday: Logo-Fix deployed, CI stabil
Today: Keycloak-Workshop, Tenant-Strategie
Blockers: Keycloak-Realm-Config von DevOps Team
```