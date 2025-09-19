# Modul 03 – Kundenmanagement (Foundation & Advanced)
Stand: 2025-09-19

## Struktur (monolithisch)
- artefakte/sql-schemas/: field_bridge_and_projection.sql, samples.sql, activities.sql, opportunities.sql
- artefakte/api-specs/: customers.yaml, samples.yaml, activities.yaml, common-errors.yaml
- backend/: de.freshplan.* (CustomerResource, SampleManagementService, ActivityService, ABAC Security, Problem Mapper)
- frontend/: CustomerList, ActivityTimeline, OpportunityDashboard, Routen
- ws/: Topics
- docs/: technical-concept.md, performance_budget.md
- design-system/: theme-v2.tokens.css, theme-v2.mui.ts
- k6/: customers_load_test.js
- ci/: github-actions.yml

## Foundation Standards erfüllt
- Design System V2 (keine Hardcodes)
- API Standards (JavaDoc-Refs, RFC7807)
- SQL Standards (Indexes, RLS mit Doku)
- Security (ABAC via JWT + Dev-Fallback-Header)
- Testing (BDD/IT + k6 Smoke)
- Package: de.freshplan.*
