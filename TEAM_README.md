# Frontend Team Workspace

## Quick Start
```bash
cd frontend
npm ci
npm run dev
```

## Aktueller Sprint – React Migration
- [ ] Calculator Module portieren
- [ ] Customer Module migrieren
- [ ] API Integration Layer
- [ ] Keycloak Auth Context

## Migration Status

| Legacy Module | React Component | Status |
|--------------|-----------------|--------|
| Calculator | CalculatorForm | 🚧 |
| Customer | CustomerList | 📋 |
| Profile | UserProfile | 📋 |
| Settings | AppSettings | 📋 |

## Development
```bash
# Dev Server
npm run dev

# Tests
npm test

# E2E Tests
npm run test:e2e

# Build
npm run build
```

## API Integration
- Base URL: `http://localhost:8080/api`
- Auth: Keycloak OIDC
- Client: React Query + Axios

## Team-Rituale
- Daily Sync: 09:00 CET (10 min)
- UI Review: Do 15:00 CET
- Siehe `.github/TEAM_SYNC.yml`

## Design System
- Component Library: MUI v5
- Storybook: `npm run storybook`
- Figma: [Design Files](link)

## Kontakt
- Lead: Frontend Team
- Slack: #freshplan-frontend
- Wiki: [Frontend Guidelines](../docs/)