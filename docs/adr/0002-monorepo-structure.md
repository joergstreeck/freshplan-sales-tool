# 2. Use monorepo structure for FreshPlan 2.0

Date: 2025-06-05

## Status

Accepted

## Context

We are migrating from a standalone TypeScript application to a full-stack enterprise solution with:
- React frontend
- Quarkus backend
- Infrastructure as Code
- Shared documentation

We need to decide how to organize these components in our repository.

## Decision

We will use a monorepo structure with the following layout:

```
freshplan-sales-tool/
├── /legacy              # Frozen legacy code (tagged as legacy-1.0.0)
├── /frontend            # React SPA
├── /backend             # Quarkus API
├── /infrastructure      # Docker, AWS CDK, Kubernetes configs
├── /docs               # Shared documentation, ADRs
└── /.github/workflows  # Shared CI/CD pipelines
```

Each subdirectory will have its own:
- `package.json` (frontend/legacy) or `pom.xml` (backend)
- README with setup instructions
- Tests co-located with source code

## Consequences

### Positive
- Single source of truth for the entire system
- Easier cross-component refactoring
- Shared CI/CD pipelines and tooling
- Atomic commits across frontend/backend
- Simplified dependency management between components

### Negative
- Larger repository size
- More complex CI/CD configuration (path filters needed)
- Potential for accidental cross-component dependencies
- All developers need full repository even if working on one component

### Mitigation
- Use path filters in CI/CD to only run relevant jobs
- Clear module boundaries and linting rules
- Good documentation for component-specific development