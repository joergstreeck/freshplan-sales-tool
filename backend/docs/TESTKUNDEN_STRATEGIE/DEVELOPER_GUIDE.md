# Developer Guide: Test-Migration

## 🎯 Quick Start für neue Tests

### Neue Tests schreiben:
```java
@QuarkusTest
@Tag("core")  // Für stabile Tests
class MyNewServiceTest {
    
    @Test
    @TestTransaction  // Auto-Rollback!
    void shouldDoSomething() {
        // Use TestDataBuilder
        Customer customer = CustomerTestDataFactory.builder()
            .withCompanyName("Test Company")
            .build();
            
        // Test logic...
    }
}
```

### Bestehende Tests migrieren:
1. Add `@Tag("migrate")` 
2. Replace manual cleanup with `@TestTransaction`
3. Replace SEED-dependencies with TestDataBuilder
4. Move to `@Tag("core")` when stable

## 🚫 Forbidden Patterns

❌ **Never use in tests:**
- `DELETE FROM customers`
- `repository.deleteAll()`
- `TRUNCATE TABLE` 
- SEED-customer dependencies

✅ **Use instead:**
- `@TestTransaction` for auto-rollback
- TestDataBuilder for test data
- `@Tag("core")` for stable tests

## 📋 Test-Tagging Strategy

### Tag Categories:
- **@Tag("core")** - Stable tests that run in PR pipeline (38 tests)
- **@Tag("migrate")** - Tests being migrated from SEED (98 tests)
- **@Tag("quarantine")** - Problematic tests with dangerous patterns (23 tests)

### Migration Process:
```
[New Test] → @Tag("migrate") → Fix Issues → @Tag("core")
```

## 🏗️ TestDataBuilder Pattern

### Available Builders:
```java
// Customer with unique ID
Customer customer = CustomerTestDataFactory.builder()
    .withCompanyName("Test Company")
    .withCustomerNumber("KD-TEST-" + runId + "-00001")
    .withIsTestData(true)  // IMPORTANT!
    .build();

// Contact with unique email
CustomerContact contact = ContactTestDataFactory.builder()
    .withFirstName("John")
    .withLastName("Doe")
    .withEmail("test-" + runId + "@example.com")
    .build();

// Opportunity
Opportunity opp = OpportunityTestDataFactory.builder()
    .withCustomer(customer)
    .withTitle("Test Opportunity")
    .withValue(10000.0)
    .build();
```

### ID Generation Pattern:
```
Format: KD-TEST-{RUN_ID}-{SEQUENCE}
Example: KD-TEST-12345-00001

RUN_ID = GitHub Actions Run ID or "LOCAL"
SEQUENCE = Atomic counter per test run
```

## 🔄 Transaction Management

### Default: @TestTransaction
```java
@Test
@TestTransaction  // Automatic rollback after test
void testWithAutoRollback() {
    // All changes are rolled back automatically
}
```

### Manual Commit (rare cases):
```java
@Test
@Transactional  // Manual transaction control
void testWithCommit() {
    // Changes are committed
    // MUST set isTestData=true on all entities!
}
```

## 🛡️ Pre-Commit Hooks

### Installation:
```bash
./setup-pre-commit.sh
```

### What's blocked:
- Unfiltered DELETE statements
- New tests without @Tag
- New SEED references
- Missing isTestData flags

### Emergency bypass:
```bash
git commit --no-verify  # Use sparingly!
```

## 🔍 Debugging Failed Tests

### Check Environment:
```bash
# Run A00 diagnostic test
./mvnw test -Dtest=A00_EnvDiagTest
```

### Common Issues:

**"No SEED data found"**
- SEED strategy removed, use TestDataBuilder

**"DELETE without filter"**
- Add WHERE clause with isTestData=true

**"Test not tagged"**
- Add @Tag("core") or @Tag("migrate")

## 📊 Test Coverage Goals

### Current Status:
- **Core Tests**: 38 (stable, run in CI)
- **Migration Tests**: 98 (being fixed)
- **Quarantine Tests**: 23 (dangerous patterns)

### Target:
- Move 5-10 tests per week from "migrate" to "core"
- Reach 100+ core tests within 2 months
- Zero quarantine tests by end of quarter

## 🚀 Best Practices

1. **Always use TestDataBuilder** - Never hardcode test data
2. **Set isTestData=true** - Mark all test entities
3. **Use @TestTransaction** - Automatic cleanup preferred
4. **Tag your tests** - Help with migration tracking
5. **Small PRs** - Migrate 5-10 tests at a time

## 📝 Example: Complete Test Class

```java
package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@Tag("core")  // Stable test in CI pipeline
class CustomerServiceTest {
    
    @Inject
    CustomerService customerService;
    
    @Test
    @TestTransaction  // Auto-rollback
    void createCustomer_withValidData_shouldSucceed() {
        // Arrange - Use TestDataBuilder
        Customer customer = CustomerTestDataFactory.builder()
            .withCompanyName("ACME Corp")
            .withEmail("test@acme.com")
            .build();
        
        // Act
        Customer saved = customerService.create(customer);
        
        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIsTestData()).isTrue();
        assertThat(saved.getCustomerNumber()).startsWith("KD-TEST-");
    }
}
```

## 🆘 Getting Help

- Check CI_TROUBLESHOOTING.md for CI issues
- Run `./mvnw test -Dtest=A00_EnvDiagTest` for environment check
- Ask in #backend-help Slack channel
- Create issue with label "test-migration"