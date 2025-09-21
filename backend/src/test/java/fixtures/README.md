# 🧪 Test Fixtures

**Purpose:** Test data builders, mock data, and test-specific migrations
**Pattern:** SEED-free, collision-free test data generation

## Structure

```
fixtures/
├── builders/     # TestDataBuilder pattern implementations
│   ├── customer/ # Customer domain builders
│   └── [domain]/ # Other domain builders
├── data/         # Static test data files
│   ├── json/     # JSON fixtures
│   └── sql/      # SQL fixtures
└── migrations/   # Test-specific DB migrations
```

## TestDataBuilder Pattern

```java
public class CustomerTestDataBuilder {
    private String name = "Test Customer " + UUID.randomUUID();
    private String email = "test-" + System.currentTimeMillis() + "@example.com";

    public CustomerTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CustomerTestDataBuilder withRandomEmail() {
        this.email = "test-" + UUID.randomUUID() + "@example.com";
        return this;
    }

    public Customer build() {
        return new Customer(name, email);
    }
}
```

## Usage Example

```java
@Test
void testCustomerCreation() {
    // SEED-free, isolated test data
    Customer customer = new CustomerTestDataBuilder()
        .withName("John Doe")
        .withRandomEmail()
        .build();

    // Test with guaranteed unique data
}
```

## Migration Instructions

When migrating from SEED-based tests:
1. Replace SEED references with builders
2. Ensure all data is unique (use UUID/timestamp)
3. No dependencies between test data
4. Each test creates its own data
5. Cleanup handled automatically