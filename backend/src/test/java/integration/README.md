# 🔷 Integration Tests

**Purpose:** Module interactions and external integrations
**Speed:** <2s per test
**Coverage Target:** API contracts, DB layer, external services
**Dependencies:** Real DB (TestContainers), mocked external APIs

## Structure

```
integration/
├── api/            # REST API endpoint tests
│   ├── customer/   # Customer resource tests
│   └── [domain]/   # Other domain APIs
├── database/       # Repository and DB tests
│   ├── migration/  # Migration tests
│   └── queries/    # Complex query tests
└── external/       # 3rd party integrations
    ├── keycloak/   # Auth integration tests
    └── email/      # Email service tests
```

## Naming Convention

- Test files: `*IntegrationTest.java`
- Test methods: `methodName_scenario_expectedOutcome()`

## Example

```java
@Tag("integration")
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
class CustomerResourceIntegrationTest {
    @Test
    @DisplayName("Should return customer list with ABAC filtering")
    void getCustomers_withTerritoryScope_shouldFilterByTerritory() {
        // Test implementation with real DB
    }
}
```

## Migration Instructions

When migrating tests from `/docs/planung/features-neu/`:
1. Copy integration test files to appropriate subdirectory
2. Ensure TestContainers or H2 setup is configured
3. Add @Tag("integration")
4. Use TestDataBuilder pattern from `../fixtures/builders/`
5. Verify cleanup after each test