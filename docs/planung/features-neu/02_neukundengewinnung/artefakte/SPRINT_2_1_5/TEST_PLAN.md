---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "konzept"
status: "in_progress"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-10-01"
---

# Sprint 2.1.5 – Test Plan

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Test Plan

## Test-Strategie (gemäß ADR-004 & TEST_MIGRATION_PLAN.md)

**Grundprinzip:** Mock-First für Business-Logik, Minimal Integration für DB-Artefakte

### Unit Tests (Mock-only, @Tag("unit"))

#### Lead Protection Service (Business Logic)
```java
@Test
@Tag("unit")
void shouldCalculateProtectionUntil() {
    // Mocks: NO @QuarkusTest
    Lead lead = new Lead();
    lead.setRegisteredAt(LocalDateTime.now());
    lead.setProtectionMonths(6);

    // When: Service-Methode
    LocalDateTime protectionUntil = protectionService.calculateProtectionUntil(lead);

    // Then: registered_at + 6 Monate
    assertThat(protectionUntil).isEqualTo(lead.getRegisteredAt().plusMonths(6));
}

@Test
@Tag("unit")
void shouldCalculateProgressDeadline() {
    // Given: Lead mit last_activity_at
    Lead lead = mockLead();
    lead.setLastActivityAt(LocalDateTime.now().minusDays(30));

    // When: Deadline-Berechnung
    LocalDateTime deadline = protectionService.calculateProgressDeadline(lead);

    // Then: last_activity_at + 60 Tage
    assertThat(deadline).isEqualTo(lead.getLastActivityAt().plusDays(60));
}
```

#### Stage Validation (Business Rules)
```java
@Test
@Tag("unit")
void shouldValidateStageTransition0to1() {
    // Given: Stage 0 → 1
    LeadStage0Request stage0 = new LeadStage0Request("Hotel Berlin", "Berlin");

    // When: Transition to Stage 1
    boolean valid = leadService.canTransitionToStage(0, 1);

    // Then: Valid
    assertThat(valid).isTrue();
}

@Test
@Tag("unit")
void shouldRejectStageSkip0to2() {
    // Given: Stage 0
    // When: Direct jump to 2
    // Then: ValidationException
    assertThatThrownBy(() -> leadService.validateStageTransition(0, 2))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Cannot skip stages");
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

### Integration Tests (Minimal, @Tag("integration"), @QuarkusTest)

**LIMIT:** Nur für DB-Artefakte (Migrations, Functions, Triggers)

#### V255-V257 Migrations
```java
@QuarkusTest
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LeadProtectionMigrationsTest {

    @Test
    @Order(1)
    void v255ShouldAddProgressFields() {
        // Given: Migration V255 gelaufen
        // When: Query table structure
        // Then: progress_warning_sent_at, progress_deadline, stage vorhanden
        // Then: Check Constraint stage BETWEEN 0 AND 2 existiert
    }

    @Test
    @Order(2)
    void v256ShouldAugmentLeadActivities() {
        // Given: Migration V256 gelaufen
        // When: Query lead_activities structure
        // Then: counts_as_progress, summary, outcome, next_action, performed_by vorhanden
        // Then: DEFAULT FALSE für counts_as_progress
    }

    @Test
    @Order(3)
    void v257TriggerShouldUpdateProgressDeadline() {
        // Given: Lead ohne Progress
        // When: INSERT lead_activity mit counts_as_progress=true
        leadActivityRepo.persist(new LeadActivity(lead.getId(), "call", true));

        // Then: leads.progress_deadline = NOW() + 60 days
        Lead updated = leadRepo.findById(lead.getId());
        assertThat(updated.getProgressDeadline()).isCloseTo(
            LocalDateTime.now().plusDays(60), within(1, ChronoUnit.MINUTES)
        );
    }
}

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

## Zeitreisen-Tests (Clock Manipulation - UNIT ONLY)

**Keine Integration Tests mit Time-Travel** - nur Unit-Tests mit Clock-Mock

```java
@Test
@Tag("unit")
void shouldCalculateProtectionLifecycle() {
    // MOCK: Clock-Manipulation
    Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneId.of("UTC"));

    // T+0: Create lead
    Lead lead = new Lead();
    lead.setRegisteredAt(LocalDateTime.now(fixedClock));
    lead.setProtectionMonths(6);

    // Assert: protection_until = 2025-07-01
    assertThat(protectionService.calculateProtectionUntil(lead, fixedClock))
        .isEqualTo(LocalDateTime.parse("2025-07-01T10:00:00"));

    // T+60d: Progress-Deadline
    lead.setLastActivityAt(LocalDateTime.now(fixedClock).plusDays(60));
    assertThat(protectionService.calculateProgressDeadline(lead))
        .isEqualTo(lead.getLastActivityAt().plusDays(60));
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

## Coverage-Ziele (Sprint 2.1.5)

**Modul-Level:**
- Modul 02: ≥ 85% (Gesamt)

**Test-Typ:**
- Unit Tests: ≥ 90% (Business-Logik vollständig)
- Integration Tests: MINIMAL (nur DB-Artefakte V255-V257)
- E2E: Critical Paths (LeadWizard 3-Stage Flow)

**KEINE Verschlechterung:**
- PR muss ≥80% Coverage haben
- Kein Rückgang gegenüber main

## CI/CD Pipeline (gemäß TEST_MIGRATION_PLAN.md)

```yaml
test-stages:
  unit:
    command: ./mvnw test -Punit
    timeout: 3min
    parallel: true

  integration:
    command: ./mvnw test -Pintegration
    timeout: 5min
    scope: V255-V257 Migrations + Triggers nur

  e2e:
    command: npm run test:e2e
    timeout: 8min
    scope: LeadWizard Flow
```

## Test-Tags Verwendung

```java
// Unit Test - Schnell, Mock-only
@Test
@Tag("unit")
void shouldValidateStage() { /* ... */ }

// Integration Test - Langsam, DB-required
@QuarkusTest
@Tag("integration")
void shouldTriggerProgressUpdate() { /* ... */ }
```

**CI-Trigger:**
- `mvn test -Punit` → Läuft alle @Tag("unit")
- `mvn test -Pintegration` → Läuft alle @Tag("integration") @QuarkusTest