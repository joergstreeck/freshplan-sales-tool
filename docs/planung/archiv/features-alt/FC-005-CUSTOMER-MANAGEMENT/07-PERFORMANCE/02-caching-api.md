# 🚀 CACHING STRATEGY & API PERFORMANCE

**Navigation:**
- **Parent:** [Performance Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/README.md)
- **Previous:** [Performance Goals](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/01-performance-goals.md)
- **Next:** [Scaling & Monitoring](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/03-scaling-monitoring.md)
- **Related:** [Services](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/02-services.md)

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

## Cache Configuration

### Redis Configuration

```yaml
# application.yml
redis:
  hosts: redis://localhost:6379
  client-type: standalone
  password: ${REDIS_PASSWORD}
  timeout: 10s
  
cache:
  field-definitions:
    expire-after-write: 5m
    max-entries: 1000
  customer-search:
    expire-after-write: 2m
    max-entries: 500
  customer-details:
    expire-after-write: 10m
    max-entries: 5000
```

### Cache Warming

```java
@ApplicationScoped
@Startup
public class CacheWarmer {
    
    @Inject
    FieldDefinitionCacheService cacheService;
    
    void onStart(@Observes StartupEvent event) {
        // Warm up frequently used field definitions
        warmUpFieldDefinitions();
    }
    
    @Scheduled(every = "30m")
    void refreshCache() {
        // Refresh cache for active industries
        getActiveIndustries().forEach(industry -> {
            cacheService.getFieldDefinitions(EntityType.CUSTOMER, industry)
                .subscribe().with(
                    definitions -> Log.info("Cached {} definitions for {}", 
                        definitions.size(), industry),
                    failure -> Log.error("Cache refresh failed", failure)
                );
        });
    }
}
```

## Best Practices

### 1. Cache Keys
- Include version in cache keys
- Use consistent naming patterns
- Consider user/tenant isolation

### 2. Cache Invalidation
- Event-driven invalidation preferred
- Time-based expiry as fallback
- Invalidate smallest scope possible

### 3. Performance Monitoring
- Track cache hit rates
- Monitor cache size
- Alert on low hit rates

---

**Next:** [Scaling & Monitoring →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/03-scaling-monitoring.md)