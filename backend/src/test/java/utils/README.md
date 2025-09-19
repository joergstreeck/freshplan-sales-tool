# 🔧 Test Utilities

**Purpose:** Shared test infrastructure and helper utilities
**Usage:** Common functionality across all test types

## Structure

```
utils/
├── containers/   # TestContainers setup
│   ├── PostgresTestContainer.java
│   └── KeycloakTestContainer.java
├── security/     # Auth test utilities
│   ├── TestSecurityContext.java
│   └── JWTTestHelper.java
├── database/     # DB test utilities
│   ├── DatabaseCleaner.java
│   └── TestTransaction.java
└── assertions/   # Custom assertions
    ├── CustomerAssertions.java
    └── ApiResponseAssertions.java
```

## Example Utilities

### TestContainers Setup
```java
public class PostgresTestContainer {
    private static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    static {
        POSTGRES.start();
    }

    public static String getJdbcUrl() {
        return POSTGRES.getJdbcUrl();
    }
}
```

### Custom Assertions
```java
public class CustomerAssertions {
    public static void assertValidCustomer(Customer customer) {
        assertThat(customer).isNotNull();
        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getEmail()).contains("@");
        // More domain-specific assertions
    }
}
```

## Migration Instructions

When consolidating test utilities:
1. Extract common setup code to this directory
2. Create domain-specific assertion helpers
3. Centralize TestContainer configurations
4. Share security/auth test helpers
5. Remove duplication across test files