---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "konzept"
status: "draft"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-09-28"
---

# Sprint 2.1.5 – Test Plan

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Test Plan

## Test-Strategie

### Unit Tests

#### Lead Protection Service
```java
@Test
void shouldCreateProtectionFor6Months() {
    // Given: New lead
    // When: Protection created
    // Then: protection_until = now + 6 months
}

@Test
void shouldTriggerWarningAfter60Days() {
    // Time-travel test mit Clock-Mock
    // Given: Lead ohne Activity seit 53 Tagen
    // When: Check-Job läuft
    // Then: Status = warning, deadline = 7 days
}

@Test
void shouldApplyStopTheClock() {
    // Given: Active protection
    // When: Stop-the-clock mit Grund
    // Then: Deadline pausiert
}
```

#### Progressive Profiling
```typescript
describe('LeadWizard', () => {
  it('Stage 0: requires only company + city', () => {})
  it('Stage 1: optional contact fields', () => {})
  it('Stage 2: validates VAT ID format', () => {})
  it('prevents stage skip', () => {})
})
```

### Integration Tests

#### Flyway Migrations
```java
@Test
@TestTransaction
void v249ShouldCreateProtectionTables() {
    // Run migration
    // Assert tables exist with correct constraints
    // Assert triggers work
}
```

#### API Responses
```java
@Test
void shouldReturn202WithCandidates() {
    // Given: Existing lead "Hotel Berlin"
    // When: POST similar "Hotel Berlin GmbH"
    // Then: 202 with candidates list
}

@Test
void shouldReturn409ForExactDuplicate() {
    // Given: lead@example.com exists
    // When: POST same email
    // Then: 409 with RFC7807 problem
}
```

### E2E Tests (Cypress)

```javascript
describe('Lead Protection Flow', () => {
  it('shows protection badge with days remaining', () => {})
  it('warns at 7 days before deadline', () => {})
  it('allows stop-the-clock with reason', () => {})
})

describe('Progressive Profiling', () => {
  it('completes 3-stage wizard', () => {})
  it('handles duplicate review flow', () => {})
})
```

### Performance Tests

```yaml
scenarios:
  fuzzy_match:
    vus: 50
    duration: 5m
    thresholds:
      - p95 < 200ms
      - error_rate < 0.1%

  batch_protection_check:
    vus: 10
    duration: 10m
    thresholds:
      - p95 < 500ms
```

## Zeitreisen-Tests (Clock Manipulation)

```java
@Test
void protectionLifecycle() {
    // T+0: Create lead → protected
    // T+30d: Add activity → deadline reset
    // T+60d: No activity → warning
    // T+67d: No activity → expired
    // T+180d: Protection ends → released
}
```

## Test-Daten

### Fixtures
```sql
-- Test leads mit verschiedenen Stati
INSERT INTO leads_test_fixture VALUES
  ('protected-fresh', created_at = now()),
  ('warning-soon', created_at = now() - 53 days),
  ('expired-old', created_at = now() - 70 days),
  ('stop-clock', stop_reason = 'Customer vacation');
```

## Coverage-Ziele

- Unit: ≥ 90%
- Integration: ≥ 85%
- E2E: Critical Paths only
- Mutation: ≥ 75%

## CI/CD Pipeline

```yaml
test-stages:
  - unit: 2min max
  - integration: 5min max
  - e2e: 10min max (parallel)
  - performance: nightly only
```