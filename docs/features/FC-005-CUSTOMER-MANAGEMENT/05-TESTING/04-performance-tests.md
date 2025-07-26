---
Navigation: [⬅️ Previous](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/03-e2e-tests.md) | [🏠 Home](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md) | [➡️ Next](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/README.md)
Parent: [📁 Testing](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)
Related: [🔗 Performance Goals](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/01-performance-goals.md) | [🔗 CI/CD](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md#cicd-pipeline)
---

# ⚡ FC-005 PERFORMANCE TESTS

**Fokus:** Load Testing & Performance Benchmarks  
**Ziele:** < 200ms API Response, 100k+ Customers  
**Frameworks:** k6, JMeter, Artillery  

## 📋 Performance Test Strategy

### Load Test Scenarios

| Szenario | Concurrent Users | Duration | Success Criteria |
|----------|-----------------|----------|------------------|
| Normal Load | 100 | 5 min | p95 < 200ms |
| Peak Load | 500 | 10 min | p95 < 500ms |
| Stress Test | 1000 | 5 min | No crashes |
| Endurance | 200 | 2 hours | Memory stable |

## 📋 k6 Performance Tests

### Customer API Load Test

```javascript
// performance/customer-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '2m', target: 100 }, // Ramp up
    { duration: '5m', target: 100 }, // Stay at 100 users
    { duration: '2m', target: 0 },   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests under 500ms
    errors: ['rate<0.1'],             // Error rate under 10%
  },
};

export default function() {
  // Get field definitions (cached)
  const fieldDefResponse = http.get(
    `${__ENV.BASE_URL}/api/field-definitions/CUSTOMER`
  );
  
  check(fieldDefResponse, {
    'field definitions status is 200': (r) => r.status === 200,
    'field definitions response time < 200ms': (r) => r.timings.duration < 200,
  });
  
  // Create customer draft
  const customerData = {
    fieldValues: [
      { fieldKey: 'companyName', value: `Perf Test ${Date.now()}` },
      { fieldKey: 'industry', value: 'hotel' },
      { fieldKey: 'chainCustomer', value: 'nein' },
    ],
  };
  
  const createResponse = http.post(
    `${__ENV.BASE_URL}/api/customers/draft`,
    JSON.stringify(customerData),
    { headers: { 'Content-Type': 'application/json' } }
  );
  
  const success = check(createResponse, {
    'create draft status is 200': (r) => r.status === 200,
    'create draft response time < 500ms': (r) => r.timings.duration < 500,
  });
  
  errorRate.add(!success);
  
  sleep(1); // Think time
}
```

### Complex Query Performance Test

```javascript
// performance/customer-search-test.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    simple_search: {
      executor: 'constant-vus',
      vus: 50,
      duration: '5m',
      exec: 'simpleSearch',
    },
    complex_search: {
      executor: 'constant-vus',
      vus: 20,
      duration: '5m',
      exec: 'complexSearch',
    },
  },
  thresholds: {
    'http_req_duration{scenario:simple_search}': ['p(95)<300'],
    'http_req_duration{scenario:complex_search}': ['p(95)<1000'],
  },
};

export function simpleSearch() {
  const response = http.get(
    `${__ENV.BASE_URL}/api/customers?industry=hotel&page=0&size=20`
  );
  
  check(response, {
    'simple search successful': (r) => r.status === 200,
    'results returned': (r) => JSON.parse(r.body).content.length > 0,
  });
}

export function complexSearch() {
  const response = http.get(
    `${__ENV.BASE_URL}/api/customers?` +
    `industry=hotel&` +
    `chainCustomer=ja&` +
    `minVolume=100000&` +
    `city=Berlin&` +
    `sort=createdAt,desc&` +
    `page=0&size=50`
  );
  
  check(response, {
    'complex search successful': (r) => r.status === 200,
    'pagination works': (r) => JSON.parse(r.body).totalElements !== undefined,
  });
}
```

## 📋 Database Performance Tests

### Query Performance Benchmarks

```sql
-- performance/benchmark-queries.sql

-- Test 1: Customer with all field values (typical load)
EXPLAIN ANALYZE
SELECT 
    c.id,
    c.customer_number,
    c.status,
    jsonb_object_agg(fv.field_definition_id, fv.value) as field_values
FROM customer c
LEFT JOIN field_value fv ON c.id = fv.entity_id 
    AND fv.entity_type = 'CUSTOMER'
WHERE c.status = 'ACTIVE'
GROUP BY c.id
LIMIT 20;

-- Expected: < 50ms with proper indexes

-- Test 2: Complex search with multiple field filters
EXPLAIN ANALYZE
WITH customer_fields AS (
    SELECT 
        entity_id,
        jsonb_object_agg(field_definition_id, value) as fields
    FROM field_value
    WHERE entity_type = 'CUSTOMER'
    GROUP BY entity_id
)
SELECT c.*, cf.fields
FROM customer c
JOIN customer_fields cf ON c.id = cf.entity_id
WHERE 
    cf.fields->>'industry' = 'hotel'
    AND cf.fields->>'chainCustomer' = 'ja'
    AND (cf.fields->>'expectedVolume')::numeric > 100000
    AND cf.fields->>'city' ILIKE '%Berlin%'
ORDER BY c.created_at DESC
LIMIT 50;

-- Expected: < 200ms with GIN index on field_value
```

### Index Performance Test

```java
@Test
@TestTransaction
void testIndexPerformance() {
    // Create 10k test customers
    List<Customer> customers = createBulkCustomers(10000);
    
    // Warm up cache
    customerRepository.count();
    
    // Test indexed query performance
    long start = System.currentTimeMillis();
    
    List<Customer> results = customerRepository
        .findByIndustryAndChainCustomer("hotel", true)
        .page(0, 100)
        .list();
        
    long duration = System.currentTimeMillis() - start;
    
    assertThat(duration).isLessThan(100); // Should be < 100ms
    assertThat(results).hasSize(100);
}
```

## 📋 Frontend Performance Tests

### Bundle Size Analysis

```bash
# performance/bundle-analysis.sh
#!/bin/bash

# Build production bundle
npm run build

# Analyze bundle size
npm run analyze

# Check against budget
MAX_SIZE=200000 # 200KB
ACTUAL_SIZE=$(stat -f%z dist/assets/main.*.js)

if [ $ACTUAL_SIZE -gt $MAX_SIZE ]; then
    echo "❌ Bundle size exceeded: ${ACTUAL_SIZE} > ${MAX_SIZE}"
    exit 1
else
    echo "✅ Bundle size OK: ${ACTUAL_SIZE} <= ${MAX_SIZE}"
fi
```

### React Performance Profiling

```typescript
// performance/field-renderer-performance.test.tsx
import { render } from '@testing-library/react';
import { Profiler } from 'react';

test('DynamicFieldRenderer performance', () => {
  const onRender = jest.fn();
  
  // Create 50 fields (stress test)
  const manyFields = Array.from({ length: 50 }, (_, i) => ({
    key: `field_${i}`,
    label: `Field ${i}`,
    fieldType: 'text',
    required: false,
    entityType: 'customer',
    isCustom: false
  }));
  
  render(
    <Profiler id="field-renderer" onRender={onRender}>
      <DynamicFieldRenderer 
        fields={manyFields}
        values={new Map()}
        errors={new Map()}
        onChange={() => {}}
      />
    </Profiler>
  );
  
  // Check render performance
  const [id, phase, actualDuration] = onRender.mock.calls[0];
  
  expect(actualDuration).toBeLessThan(50); // Should render in < 50ms
});
```

## 📋 Memory & Resource Tests

### Memory Leak Detection

```javascript
// performance/memory-leak-test.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  stages: [
    { duration: '10m', target: 100 },
    { duration: '30m', target: 100 }, // Sustained load
    { duration: '5m', target: 0 },
  ],
};

export default function() {
  // Create and immediately delete customers
  const createResponse = http.post(
    `${__ENV.BASE_URL}/api/customers/draft`,
    JSON.stringify({
      fieldValues: [
        { fieldKey: 'companyName', value: `Memory Test ${__VU}-${__ITER}` }
      ]
    })
  );
  
  if (createResponse.status === 200) {
    const id = JSON.parse(createResponse.body).id;
    
    // Delete immediately
    http.del(`${__ENV.BASE_URL}/api/customers/draft/${id}`);
  }
}

export function handleSummary(data) {
  // Check for memory growth
  console.log('Memory usage should remain stable throughout test');
  return {
    'summary.json': JSON.stringify(data),
  };
}
```

## 🔧 Performance Monitoring

### Application Metrics

```java
@ApplicationScoped
public class CustomerPerformanceMetrics {
    
    @Inject
    MeterRegistry registry;
    
    public void recordCustomerCreation(long duration) {
        registry.timer("customer.creation.duration")
            .record(duration, TimeUnit.MILLISECONDS);
    }
    
    public void recordFieldValueQuery(String fieldKey, long duration) {
        registry.timer("field.query.duration")
            .tag("field", fieldKey)
            .record(duration, TimeUnit.MILLISECONDS);
    }
    
    @Scheduled(every = "1m")
    void reportMetrics() {
        Timer.Sample sample = Timer.start(registry);
        
        long activeCustomers = customerRepository.countByStatus(CustomerStatus.ACTIVE);
        registry.gauge("customer.active.count", activeCustomers);
        
        long draftCustomers = customerRepository.countByStatus(CustomerStatus.DRAFT);
        registry.gauge("customer.draft.count", draftCustomers);
        
        sample.stop(registry.timer("metrics.collection.duration"));
    }
}
```

## 📊 Performance Reports

### HTML Report Template

```html
<!-- performance/report-template.html -->
<!DOCTYPE html>
<html>
<head>
    <title>Customer Management Performance Report</title>
    <script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
</head>
<body>
    <h1>Performance Test Results</h1>
    
    <div id="response-time-chart"></div>
    <div id="throughput-chart"></div>
    <div id="error-rate-chart"></div>
    
    <script>
        // Response time over time
        Plotly.newPlot('response-time-chart', [{
            x: timestamps,
            y: responseTimes,
            type: 'scatter',
            name: 'Response Time (ms)'
        }]);
        
        // Throughput
        Plotly.newPlot('throughput-chart', [{
            x: timestamps,
            y: requestsPerSecond,
            type: 'bar',
            name: 'Requests/sec'
        }]);
    </script>
</body>
</html>
```

## 📚 Weiterführende Links

- [Performance Goals →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/01-performance-goals.md)
- [Database Optimization →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/02-database-optimization.md)
- [Caching Strategy →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/03-caching-strategy.md)