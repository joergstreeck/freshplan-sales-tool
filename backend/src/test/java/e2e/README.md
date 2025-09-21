# 🔺 End-to-End Tests

**Purpose:** Complete user journeys and business workflows
**Speed:** <30s per test
**Coverage Target:** Critical business paths only
**Dependencies:** Full system running (frontend + backend + DB)

## Structure

```
e2e/
├── scenarios/     # Business scenario tests
│   ├── sales/     # Sales workflow tests
│   └── reporting/ # Reporting workflow tests
├── workflows/     # Multi-step process tests
│   ├── lead-to-customer/
│   └── opportunity-lifecycle/
└── contracts/     # API contract tests
    ├── consumer/  # Consumer-driven contracts
    └── provider/  # Provider verification
```

## Naming Convention

- Test files: `*E2ETest.java`
- Test methods: `journey_fromStateToState_expectedResult()`

## Example

```java
@Tag("e2e")
@DisplayName("Customer Lifecycle E2E Tests")
class CustomerLifecycleE2ETest {
    @Test
    @DisplayName("Complete customer journey from lead to active customer")
    void customerJourney_fromLeadToActive_shouldCompleteAllSteps() {
        // Full workflow test
    }
}
```

## Migration Instructions

When migrating tests from `/docs/planung/features-neu/`:
1. Only migrate tests that cover complete workflows
2. Ensure full system is running (docker-compose)
3. Add @Tag("e2e")
4. Use page objects or API clients for interaction
5. Keep test data minimal and focused