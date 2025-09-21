# 🔸 Performance Tests

**Purpose:** Load testing, stress testing, and performance benchmarking
**Speed:** 1-10min per test suite
**Coverage Target:** Critical APIs and database operations
**Dependencies:** Production-like environment

## Structure

```
performance/
├── load/        # Normal load testing
│   ├── api/     # API load tests
│   └── db/      # Database load tests
├── stress/      # Stress testing (beyond normal load)
│   └── spike/   # Sudden load increase tests
└── benchmark/   # Performance benchmarks
    ├── baseline/ # Baseline measurements
    └── regression/ # Regression detection
```

## Naming Convention

- Test files: `*PerformanceTest.java`
- Test methods: `operation_loadLevel_metric()`

## Example

```java
@Tag("performance")
@DisplayName("Customer API Performance Tests")
class CustomerAPIPerformanceTest {
    @Test
    @DisplayName("Customer list API should handle 1000 RPS")
    void getCustomerList_1000RPS_shouldMaintainP95Under200ms() {
        // JMH or custom performance test
    }
}
```

## Tools

- JMH (Java Microbenchmark Harness) for micro-benchmarks
- k6 or Artillery for API load testing
- Custom metrics collection with Micrometer

## Migration Instructions

When migrating tests from `/docs/planung/features-neu/`:
1. Copy k6 scripts to appropriate subdirectory
2. Convert to JMH benchmarks where appropriate
3. Add @Tag("performance")
4. Define clear SLOs (P95 < 200ms)
5. Setup performance baseline data