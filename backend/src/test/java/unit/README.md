# 🔶 Unit Tests

**Purpose:** Isolated component/class testing
**Speed:** <50ms per test
**Coverage Target:** 80%+ business logic
**Dependencies:** All external dependencies mocked

## Structure

```
unit/
├── domain/           # Domain logic tests
│   ├── customer/    # Customer domain tests
│   ├── opportunity/ # Opportunity domain tests
│   └── audit/       # Audit domain tests
└── infrastructure/  # Infrastructure tests
    ├── validation/  # Input validation tests
    └── utils/       # Utility class tests
```

## Naming Convention

- Test files: `*UnitTest.java`
- Test methods: `methodName_condition_expectedResult()`

## Example

```java
@Tag("unit")
@DisplayName("CustomerService Unit Tests")
class CustomerServiceUnitTest {
    @Test
    @DisplayName("Should create customer when valid data provided")
    void createCustomer_withValidData_shouldReturnCreatedCustomer() {
        // Test implementation
    }
}
```

## Migration Instructions

When migrating tests from `/docs/planung/features-neu/`:
1. Copy test files to appropriate subdirectory here
2. Update package declarations
3. Ensure @Tag("unit") is present
4. Remove any real database dependencies
5. Update imports to use test fixtures from `../fixtures/`