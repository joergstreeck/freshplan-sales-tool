# 📈 SCALING, MONITORING & OPTIMIZATION

**Navigation:**
- **Parent:** [Performance Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/README.md)
- **Previous:** [Caching & API](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/02-caching-api.md)
- **Related:** [Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/04-INTEGRATION/README.md)

## Frontend Optimierung

### Code Splitting

```typescript
// Lazy Loading für große Komponenten
const CustomerManagement = lazy(() => 
  import(/* webpackChunkName: "customer-management" */ './CustomerManagement')
);

const LocationsStep = lazy(() => 
  import(/* webpackChunkName: "locations-step" */ './steps/LocationsStep')
);

const DetailedLocationsStep = lazy(() => 
  import(/* webpackChunkName: "detailed-locations" */ './steps/DetailedLocationsStep')
);
```

### Virtual Scrolling für große Listen

```typescript
// components/VirtualCustomerList.tsx
import { FixedSizeList } from 'react-window';
import AutoSizer from 'react-virtualized-auto-sizer';

export const VirtualCustomerList: React.FC<{ customers: Customer[] }> = ({ 
  customers 
}) => {
  const Row = ({ index, style }) => (
    <div style={style}>
      <CustomerListItem customer={customers[index]} />
    </div>
  );
  
  return (
    <AutoSizer>
      {({ height, width }) => (
        <FixedSizeList
          height={height}
          width={width}
          itemCount={customers.length}
          itemSize={80}
          overscanCount={5}
        >
          {Row}
        </FixedSizeList>
      )}
    </AutoSizer>
  );
};
```

### Debounced Search

```typescript
// hooks/useDebouncedSearch.ts
export const useDebouncedSearch = (delay = 300) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedTerm, setDebouncedTerm] = useState('');
  
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedTerm(searchTerm);
    }, delay);
    
    return () => clearTimeout(timer);
  }, [searchTerm, delay]);
  
  const { data, isLoading } = useQuery({
    queryKey: ['customers', 'search', debouncedTerm],
    queryFn: () => customerApi.search(debouncedTerm),
    enabled: debouncedTerm.length >= 2,
    keepPreviousData: true
  });
  
  return {
    searchTerm,
    setSearchTerm,
    results: data,
    isSearching: isLoading
  };
};
```

### Web Workers für Heavy Computations

```typescript
// workers/fieldValidation.worker.ts
import { buildFieldSchema } from '../schemas/validationSchemas';

self.addEventListener('message', async (event) => {
  const { fields, values } = event.data;
  
  const validationResults = new Map();
  
  for (const field of fields) {
    try {
      const schema = buildFieldSchema(field);
      await schema.parseAsync(values.get(field.key));
      validationResults.set(field.key, { valid: true });
    } catch (error) {
      validationResults.set(field.key, { 
        valid: false, 
        error: error.message 
      });
    }
  }
  
  self.postMessage({ validationResults });
});

// Usage in component
const validateFieldsInWorker = (fields, values) => {
  return new Promise((resolve) => {
    const worker = new Worker('/workers/fieldValidation.worker.js');
    worker.postMessage({ fields, values });
    worker.onmessage = (e) => {
      resolve(e.data.validationResults);
      worker.terminate();
    };
  });
};
```

## Skalierungs-Strategie

### Horizontal Scaling

```yaml
# kubernetes/customer-service.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: customer-service
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    spec:
      containers:
      - name: customer-service
        image: freshplan/customer-service:latest
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        env:
        - name: JAVA_OPTS
          value: "-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
        
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: customer-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: customer-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: "1000"
```

### Database Connection Pooling

```yaml
# application.yml
quarkus:
  datasource:
    jdbc:
      url: jdbc:postgresql://pgbouncer:6432/freshplan
      min-size: 10
      max-size: 50
      acquisition-timeout: 5
      leak-detection-interval: 30
      idle-removal-interval: 60
      max-lifetime: 1800
      
  # Read replicas for scaling reads
  datasource:
    read-only:
      jdbc:
        url: jdbc:postgresql://pgbouncer-readonly:6432/freshplan
        min-size: 20
        max-size: 100
```

### Event-Driven Architecture für Entkopplung

```java
// Asynchrone Verarbeitung für heavy operations
@ApplicationScoped
public class CustomerEventProcessor {
    
    @Inject
    @Channel("customer-events")
    Emitter<CustomerEvent> eventEmitter;
    
    @Incoming("customer-commands")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public Uni<Void> processCustomerCommand(CustomerCommand command) {
        return switch (command.getType()) {
            case BULK_UPDATE -> processBulkUpdate(command)
                .onItem().transformToUni(result -> 
                    eventEmitter.send(
                        CustomerBulkUpdateCompleted.of(result)
                    )
                );
            case MASS_DELETE -> processMassDelete(command);
            default -> Uni.createFrom().voidItem();
        };
    }
}
```

## Monitoring & Alerts

### Metriken

```java
@ApplicationScoped
public class CustomerMetrics {
    
    @Inject
    MeterRegistry registry;
    
    private final Map<String, Timer.Sample> activeRequests = 
        new ConcurrentHashMap<>();
    
    public void recordFieldValueLoad(int count, long duration) {
        registry.timer("customer.field.values.load",
            "count", String.valueOf(count)
        ).record(duration, TimeUnit.MILLISECONDS);
    }
    
    public void recordSearchPerformance(
        SearchCriteria criteria,
        long resultCount,
        long duration
    ) {
        registry.timer("customer.search",
            "type", criteria.getType(),
            "result_count", bucketize(resultCount)
        ).record(duration, TimeUnit.MILLISECONDS);
        
        // Alert on slow searches
        if (duration > 1000) {
            registry.counter("customer.search.slow",
                "criteria", criteria.toString()
            ).increment();
        }
    }
    
    private String bucketize(long count) {
        if (count <= 10) return "0-10";
        if (count <= 100) return "11-100";
        if (count <= 1000) return "101-1000";
        return "1000+";
    }
}
```

### Performance Dashboards

```yaml
# Grafana Dashboard Queries
- title: "API Response Time"
  query: |
    histogram_quantile(0.95,
      sum(rate(http_server_requests_seconds_bucket{
        uri=~"/api/customers.*"
      }[5m])) by (le, uri)
    )
    
- title: "Field Value Load Performance"
  query: |
    rate(customer_field_values_load_seconds_sum[5m]) /
    rate(customer_field_values_load_seconds_count[5m])
    
- title: "Cache Hit Rate"
  query: |
    sum(rate(cache_hits_total[5m])) /
    sum(rate(cache_gets_total[5m]))
    
- title: "Database Connection Pool"
  query: |
    hikari_connections_active / 
    hikari_connections_max
```

### Alerts

```yaml
# Prometheus Alert Rules
groups:
  - name: customer_performance
    rules:
      - alert: SlowCustomerSearch
        expr: |
          histogram_quantile(0.95,
            rate(customer_search_seconds_bucket[5m])
          ) > 0.5
        for: 5m
        annotations:
          summary: "Customer search P95 > 500ms"
          
      - alert: HighFieldValueLoadTime
        expr: |
          rate(customer_field_values_load_seconds_sum[5m]) /
          rate(customer_field_values_load_seconds_count[5m]) > 0.2
        for: 10m
        annotations:
          summary: "Field value loading average > 200ms"
          
      - alert: CacheHitRateLow
        expr: |
          sum(rate(cache_hits_total[5m])) /
          sum(rate(cache_gets_total[5m])) < 0.8
        for: 15m
        annotations:
          summary: "Cache hit rate below 80%"
```

## Performance Testing

### Load Test Scenarios

```javascript
// k6/scenarios/customer-load.js
import { scenario } from 'k6/execution';

export const options = {
  scenarios: {
    // Steady state load
    steady_load: {
      executor: 'constant-arrival-rate',
      rate: 100,
      timeUnit: '1s',
      duration: '10m',
      preAllocatedVUs: 50,
      maxVUs: 100,
    },
    
    // Spike test
    spike_test: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 500,
      stages: [
        { duration: '2m', target: 10 },
        { duration: '1m', target: 200 },
        { duration: '2m', target: 200 },
        { duration: '1m', target: 10 },
      ],
    },
    
    // Stress test
    stress_test: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '5m', target: 100 },
        { duration: '10m', target: 500 },
        { duration: '5m', target: 1000 },
        { duration: '10m', target: 1000 },
        { duration: '5m', target: 0 },
      ],
    },
  },
};
```

### Performance Baseline

```sql
-- Baseline Queries für Performance Testing
-- Diese müssen unter Last < 200ms bleiben

-- 1. Customer Search by Name
EXPLAIN (ANALYZE, BUFFERS) 
SELECT c.id, fv.value->>'companyName' as company_name
FROM customers c
JOIN field_values fv ON fv.entity_id = c.id
WHERE fv.field_definition_id = 'companyName'
  AND fv.entity_type = 'CUSTOMER'
  AND lower(fv.value->>'companyName') LIKE lower('%test%')
  AND c.status = 'ACTIVE'
ORDER BY fv.value->>'companyName'
LIMIT 20;

-- 2. Load Customer with all Fields
EXPLAIN (ANALYZE, BUFFERS)
SELECT fv.field_definition_id, fv.value
FROM field_values fv
WHERE fv.entity_id = '123e4567-e89b-12d3-a456-426614174000'
  AND fv.entity_type = 'CUSTOMER';

-- 3. Complex Search with Joins
EXPLAIN (ANALYZE, BUFFERS)
WITH customer_data AS (
  SELECT 
    c.id,
    c.status,
    c.created_at,
    jsonb_object_agg(fv.field_definition_id, fv.value) as fields
  FROM customers c
  LEFT JOIN field_values fv ON fv.entity_id = c.id 
    AND fv.entity_type = 'CUSTOMER'
  WHERE c.status = 'ACTIVE'
  GROUP BY c.id
)
SELECT * FROM customer_data
WHERE fields->>'industry' = 'hotel'
  AND (fields->>'chainCustomer')::text = 'ja'
ORDER BY fields->>'companyName'
LIMIT 50;
```

## Best Practices

### 1. Frontend Optimization
- Bundle size < 200KB initial
- Lazy load heavy components
- Virtual scrolling for lists > 100 items
- Debounce user inputs

### 2. Backend Scaling
- Horizontal pod autoscaling
- Read replicas for queries
- Event-driven for async ops
- Connection pooling

### 3. Monitoring
- Alert on SLA violations
- Track cache effectiveness
- Monitor resource usage
- Regular load testing

---

**Stand:** 26.07.2025