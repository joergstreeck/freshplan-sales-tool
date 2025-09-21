# Modul 02 – Foundation Standards Aktualisierung
Stand: 2025-09-19

## Struktur
- design-system/: Theme V2 Tokens + MUI Theme
- openapi/: Leads API inkl. Export & WS
- backend/: LeadResource, LeadExportAdapter, LeadScoringService, Security (ABAC)
- frontend/: SmartLayout Files, Tests
- ws/: Topics
- sql/: Seasonal Views
- k6/: Performance Test
- docs/: Performance Budget, Compliance Matrix

## Quick Start
1. CSS Tokens einbinden (`theme-v2.tokens.css`), App mit `theme-v2.mui.ts` wrappen.
2. OpenAPI importieren → Controller/Repo ableiten.
3. SecurityScopeFilter an JWT anbinden (Territories/chain_id Claims).
4. Tests ausführen (JUnit, Jest, Playwright) – Coverage > 80% sicherstellen.
5. k6 laufen lassen (P95 < 200ms).

## Quality Gates
- Coverage 80%+ (Jacoco/Jest)
- P95 < 200ms (k6 + Prometheus)
- WCAG 2.1 AA (Focus Styles, Kontraste via Tokens)
- Enterprise Security (ABAC + Named Params)
