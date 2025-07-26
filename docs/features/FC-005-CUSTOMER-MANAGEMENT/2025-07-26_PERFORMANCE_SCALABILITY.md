# ⚡ FC-005 CUSTOMER MANAGEMENT - PERFORMANCE & SCALABILITY

**Datum:** 26.07.2025  
**Version:** 1.0  
**Ziel-Metriken:** < 200ms API Response, 100k+ Customers  

## 📋 Inhaltsverzeichnis

1. [Performance-Ziele](#performance-ziele)
2. [Database Optimierung](#database-optimierung)
3. [Caching-Strategie](#caching-strategie)
4. [API Performance](#api-performance)
5. [Frontend Optimierung](#frontend-optimierung)
6. [Skalierungs-Strategie](#skalierungs-strategie)
7. [Monitoring & Alerts](#monitoring--alerts)

---

## Performance-Ziele

### SLA Targets

| Operation | Target P50 | Target P95 | Target P99 | Max |
|-----------|------------|------------|------------|-----|
| Field Definitions laden | 50ms | 100ms | 200ms | 500ms |
| Customer Draft erstellen | 100ms | 200ms | 500ms | 1s |
| Customer suchen (einfach) | 50ms | 150ms | 300ms | 500ms |
| Customer suchen (komplex) | 100ms | 300ms | 500ms | 1s |
| Field Values laden | 50ms | 100ms | 200ms | 500ms |
| Bulk Operations (100 items) | 500ms | 1s | 2s | 5s |

### Skalierungsziele

- **Customers:** 100.000+ aktive Kunden
- **Field Values:** 50+ Felder pro Kunde (5M+ Einträge)
- **Locations:** Durchschnitt 5 pro Kettenkunde
- **Concurrent Users:** 1.000+ gleichzeitige Nutzer
- **API Requests:** 10.000 req/min Peak

---

## Database Optimierung

### Index-Strategie

```sql
-- Primary Indexes (automatisch durch PKs)
-- Zusätzliche Performance-Indexes

-- Field Values: Häufigste Zugriffsmuster
CREATE INDEX idx_field_values_entity_type_id 
    ON field_values(entity_id, entity_type) 
    INCLUDE (field_definition_id, value);

CREATE INDEX idx_field_values_definition_value 
    ON field_values(field_definition_id, value) 
    WHERE entity_type = 'CUSTOMER';

-- Customer Search Optimization
CREATE INDEX idx_customer_status_created 
    ON customers(status, created_at DESC) 
    WHERE status != 'DELETED';

-- GIN Index für JSONB Suche
CREATE INDEX idx_field_values_jsonb 
    ON field_values USING gin(value jsonb_path_ops);

-- Partial Indexes für häufige Queries
CREATE INDEX idx_active_customers 
    ON customers(id) 
    WHERE status = 'ACTIVE';

CREATE INDEX idx_draft_customers_user 
    ON customers(created_by, created_at DESC) 
    WHERE status = 'DRAFT';

-- Location Performance
CREATE INDEX idx_locations_customer_type 
    ON locations(customer_id, location_type);

-- Composite Index für Sortierung
CREATE INDEX idx_customer_company_name 
    ON field_values((value->>'companyName')) 
    WHERE field_definition_id = 'companyName' 
    AND entity_type = 'CUSTOMER';
```

### Query Optimization

```java
@ApplicationScoped
public class OptimizedCustomerRepository {
    
    @Inject
    EntityManager em;
    
    // Optimierte Suche mit Pagination
    public Page<CustomerSearchResult> searchCustomers(
        SearchCriteria criteria,
        Pageable pageable
    ) {
        // Build dynamic query with selective joins
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CustomerSearchResult> query = 
            cb.createQuery(CustomerSearchResult.class);
        
        Root<Customer> customer = query.from(Customer.class);
        
        // Selective field loading
        query.select(cb.construct(
            CustomerSearchResult.class,
            customer.get("id"),
            customer.get("status"),
            // Subquery for company name
            cb.literal("") // Placeholder, filled by second query
        ));
        
        // Apply filters
        List<Predicate> predicates = buildPredicates(criteria, cb, customer);
        query.where(predicates.toArray(new Predicate[0]));
        
        // Count query for pagination
        Long total = executeCountQuery(predicates);
        
        // Execute main query
        List<CustomerSearchResult> results = em.createQuery(query)
            .setFirstResult(pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();
        
        // Batch load field values
        enrichWithFieldValues(results);
        
        return new PageImpl<>(results, pageable, total);
    }
    
    // Batch loading für N+1 Prevention
    private void enrichWithFieldValues(List<CustomerSearchResult> results) {
        if (results.isEmpty()) return;
        
        Set<UUID> customerIds = results.stream()
            .map(CustomerSearchResult::getId)
            .collect(Collectors.toSet());
        
        // Single query for all field values
        Map<UUID, Map<String, Object>> fieldValueMap = em.createQuery(
            "SELECT fv.entityId, fv.fieldDefinitionId, fv.value " +
            "FROM FieldValue fv " +
            "WHERE fv.entityId IN :ids " +
            "AND fv.entityType = :type " +
            "AND fv.fieldDefinitionId IN :fields",
            Object[].class
        )
        .setParameter("ids", customerIds)
        .setParameter("type", EntityType.CUSTOMER)
        .setParameter("fields", List.of("companyName", "industry", "city"))
        .getResultStream()
        .collect(Collectors.groupingBy(
            row -> (UUID) row[0],
            Collectors.toMap(
                row -> (String) row[1],
                row -> row[2]
            )
        ));
        
        // Enrich results
        results.forEach(result -> {
            Map<String, Object> values = fieldValueMap.get(result.getId());
            if (values != null) {
                result.setCompanyName((String) values.get("companyName"));
                result.setIndustry((String) values.get("industry"));
                result.setCity((String) values.get("city"));
            }
        });
    }
}
```

### Database Partitioning

```sql
-- Partitionierung für große Tabellen
-- Field Values nach Entity Type
CREATE TABLE field_values (
    id UUID PRIMARY KEY,
    field_definition_id VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    value JSONB,
    updated_at TIMESTAMP NOT NULL
) PARTITION BY LIST (entity_type);

CREATE TABLE field_values_customer PARTITION OF field_values
    FOR VALUES IN ('CUSTOMER');

CREATE TABLE field_values_location PARTITION OF field_values
    FOR VALUES IN ('LOCATION');

CREATE TABLE field_values_detailed_location PARTITION OF field_values
    FOR VALUES IN ('DETAILED_LOCATION');

-- Time-based partitioning for audit logs
CREATE TABLE customer_data_access_log (
    id UUID PRIMARY KEY,
    accessed_at TIMESTAMP NOT NULL,
    -- other columns
) PARTITION BY RANGE (accessed_at);

CREATE TABLE customer_data_access_log_2025_q1 
    PARTITION OF customer_data_access_log
    FOR VALUES FROM ('2025-01-01') TO ('2025-04-01');
```

---

## Caching-Strategie

### Multi-Level Caching

```java
@ApplicationScoped
public class FieldDefinitionCacheService {
    
    // L1 Cache: Application Memory (Caffeine)
    private final Cache<String, List<FieldDefinition>> memoryCache = 
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();
    
    // L2 Cache: Redis
    @Inject
    @RedisClient
    ReactiveRedisCommands<String, String> redis;
    
    @Inject
    FieldDefinitionRepository repository;
    
    public Uni<List<FieldDefinition>> getFieldDefinitions(
        EntityType entityType,
        String industry
    ) {
        String cacheKey = buildCacheKey(entityType, industry);
        
        // L1 Cache Check
        List<FieldDefinition> cached = memoryCache.getIfPresent(cacheKey);
        if (cached != null) {
            return Uni.createFrom().item(cached);
        }
        
        // L2 Cache Check
        return redis.get(cacheKey)
            .onItem().ifNotNull().transform(json -> {
                List<FieldDefinition> definitions = deserialize(json);
                memoryCache.put(cacheKey, definitions);
                return definitions;
            })
            .onItem().ifNull().switchTo(() -> {
                // Load from DB
                return loadFromDatabase(entityType, industry)
                    .onItem().invoke(definitions -> {
                        // Update both caches
                        memoryCache.put(cacheKey, definitions);
                        redis.setex(
                            cacheKey, 
                            300, // 5 minutes TTL
                            serialize(definitions)
                        );
                    });
            });
    }
    
    @CacheInvalidate(cacheName = "field-definitions")
    public void invalidateFieldDefinitionCache(
        EntityType entityType,
        String industry
    ) {
        String pattern = buildCacheKeyPattern(entityType, industry);
        memoryCache.invalidateAll();
        redis.del(pattern).await().indefinitely();
    }
}
```

### Query Result Caching

```java
@ApplicationScoped
public class CustomerQueryCache {
    
    @ConfigProperty(name = "cache.customer.search.ttl")
    int searchCacheTtl;
    
    @CacheResult(cacheName = "customer-search")
    public Page<CustomerSearchResult> searchWithCache(
        @CacheKey SearchCriteria criteria,
        @CacheKey Pageable pageable
    ) {
        return optimizedCustomerRepository.searchCustomers(criteria, pageable);
    }
    
    // Intelligent cache invalidation
    @ConsumeEvent("customer.updated")
    public void onCustomerUpdated(CustomerUpdatedEvent event) {
        // Only invalidate affected search results
        Set<String> affectedCacheKeys = determineAffectedSearches(event);
        affectedCacheKeys.forEach(this::invalidateSearchCache);
    }
}
```

### Frontend Caching

```typescript
// React Query Configuration
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Field definitions rarely change
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 10 * 60 * 1000, // 10 minutes
      
      // Intelligent refetching
      refetchOnWindowFocus: false,
      refetchOnReconnect: 'always',
      retry: (failureCount, error) => {
        if (error.status === 404) return false;
        return failureCount < 3;
      }
    }
  }
});

// Optimistic Updates
const useUpdateCustomerField = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: customerApi.updateFieldValue,
    onMutate: async ({ customerId, fieldKey, value }) => {
      // Cancel in-flight queries
      await queryClient.cancelQueries(['customer', customerId]);
      
      // Snapshot previous value
      const previousCustomer = queryClient.getQueryData(['customer', customerId]);
      
      // Optimistic update
      queryClient.setQueryData(['customer', customerId], (old) => ({
        ...old,
        fieldValues: {
          ...old.fieldValues,
          [fieldKey]: value
        }
      }));
      
      return { previousCustomer };
    },
    onError: (err, variables, context) => {
      // Rollback on error
      if (context?.previousCustomer) {
        queryClient.setQueryData(
          ['customer', variables.customerId],
          context.previousCustomer
        );
      }
    },
    onSettled: (data, error, variables) => {
      // Refetch to ensure consistency
      queryClient.invalidateQueries(['customer', variables.customerId]);
    }
  });
};
```

---

## API Performance

### Response Pagination

```java
@GET
@Path("/search")
public Uni<Response> searchCustomers(
    @QueryParam("q") String query,
    @QueryParam("page") @DefaultValue("0") int page,
    @QueryParam("size") @DefaultValue("20") int size,
    @QueryParam("sort") @DefaultValue("companyName,asc") String sort,
    @QueryParam("fields") @DefaultValue("basic") String fields
) {
    // Field projection für Performance
    Set<String> requestedFields = parseFieldSelection(fields);
    
    return customerService
        .searchAsync(query, PageRequest.of(page, size, parseSort(sort)))
        .map(results -> {
            // Conditional field loading
            if ("minimal".equals(fields)) {
                return results.map(this::toMinimalDto);
            } else if ("full".equals(fields)) {
                return results.map(this::toFullDto);
            } else {
                return results.map(c -> toCustomDto(c, requestedFields));
            }
        })
        .map(page -> Response.ok(page)
            .header("X-Total-Count", String.valueOf(page.getTotalElements()))
            .header("X-Page-Count", String.valueOf(page.getTotalPages()))
            .build()
        );
}
```

### GraphQL Alternative für flexible Queries

```graphql
# schema.graphql
type Customer {
  id: ID!
  status: CustomerStatus!
  fieldValues(keys: [String!]): [FieldValue!]!
  locations(first: Int, after: String): LocationConnection!
  statistics: CustomerStatistics!
}

type FieldValue {
  key: String!
  value: JSON!
  lastUpdated: DateTime!
}

type CustomerStatistics {
  totalLocations: Int!
  totalContracts: Int!
  totalRevenue: Float!
  lastActivity: DateTime
}

type Query {
  customer(id: ID!): Customer
  searchCustomers(
    criteria: SearchCriteria!
    first: Int = 20
    after: String
  ): CustomerConnection!
}
```

### Batch API Endpoints

```java
@POST
@Path("/batch/update")
@Transactional
public Uni<Response> batchUpdateCustomers(
    @Valid List<BatchUpdateRequest> updates
) {
    return Multi.createFrom().iterable(updates)
        .onItem().transformToUniAndConcatenate(update -> 
            customerService.updateFieldsAsync(
                update.getCustomerId(),
                update.getFieldUpdates()
            )
            .onFailure().recoverWithItem(error -> 
                BatchUpdateResult.failed(
                    update.getCustomerId(),
                    error.getMessage()
                )
            )
        )
        .collect().asList()
        .map(results -> Response.ok(
            BatchUpdateResponse.of(results)
        ).build());
}
```

---

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

---

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

---

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

---

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