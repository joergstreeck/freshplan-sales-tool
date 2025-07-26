# Navigation Template für FC-005

## Standard Navigation Header
```markdown
---
Navigation: [⬅️ Previous](/absolute/path/to/previous.md) | [🏠 Home](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md) | [➡️ Next](/absolute/path/to/next.md)
Parent: [📁 FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)
Related: [🔗 Master Plan V5](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md) | [🔗 CLAUDE.md](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md)
---
```

## Neue Dokumentenstruktur
```
FC-005-CUSTOMER-MANAGEMENT/
├── README.md (186 Zeilen - optimal)
├── 01-TECH-CONCEPT/
│   ├── README.md (Übersicht)
│   ├── 01-executive-summary.md
│   ├── 02-architecture-decisions.md
│   ├── 03-data-model.md
│   └── 04-implementation-plan.md
├── 02-BACKEND/
│   ├── README.md (Übersicht)
│   ├── 01-entities.md
│   ├── 02-services.md
│   ├── 03-rest-api.md
│   └── 04-database.md
├── 03-FRONTEND/
│   ├── README.md (Übersicht)
│   ├── 01-components.md
│   ├── 02-state-management.md
│   ├── 03-field-rendering.md
│   └── 04-validation.md
├── 04-INTEGRATION/
│   ├── README.md (Übersicht)
│   ├── 01-module-dependencies.md
│   ├── 02-event-system.md
│   └── 03-api-gateway.md
├── 05-TESTING/
│   ├── README.md (Übersicht)
│   ├── 01-unit-tests.md
│   ├── 02-integration-tests.md
│   ├── 03-e2e-tests.md
│   └── 04-performance-tests.md
├── 06-SECURITY/
│   ├── README.md (Übersicht)
│   ├── 01-dsgvo-compliance.md
│   ├── 02-encryption.md
│   └── 03-permissions.md
├── 07-PERFORMANCE/
│   ├── README.md (Übersicht)
│   ├── 01-performance-goals.md
│   ├── 02-database-optimization.md
│   └── 03-caching-strategy.md
└── 08-IMPLEMENTATION/
    ├── README.md (Übersicht)
    ├── 01-day-1-backend.md
    ├── 02-day-2-persistence.md
    ├── 03-day-3-frontend.md
    ├── 04-day-4-integration.md
    └── 05-day-5-testing.md
```

## Zielgrößen
- Max 500 Zeilen pro Dokument
- Klare thematische Trennung
- Vollständige Navigation in jedem Dokument