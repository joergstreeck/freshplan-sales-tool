# 📆 Tag 3: Performance Optimization

**Datum:** Mittwoch, 28. August 2025  
**Fokus:** Performance Tuning  
**Ziel:** Skalierbare und schnelle Contact Management  

## 🧭 Navigation

**← Vorheriger Tag:** [Tag 2: DSGVO](./DAY2_DSGVO.md)  
**↑ Woche 4 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 4: Resilience](./DAY4_RESILIENCE.md)  
**📘 Spec:** [Performance Specification](./specs/PERFORMANCE_SPEC.md)  

## 🎯 Tagesziel

- Backend: Query Optimization & Caching
- Database: Indexes & Projections
- Frontend: Virtual Scrolling & Lazy Loading
- Monitoring: Performance Metrics

## ⚡ Performance Architecture

```
Request → Cache Layer → Query Optimizer → Database
           │                    │            │
           ├── Redis           ├── CQRS    ├── Indexes
           └── CDN             └── Views    └── Partitions
```

## 💻 Backend Implementation

### 1. Query Optimization Service

```java
// ContactQueryOptimizer.java
@ApplicationScoped
public class ContactQueryOptimizer {
    
    @Inject
    @RedisClient
    ReactiveRedisClient redis;
    
    @Inject
    ContactProjectionRepository projectionRepo;
    
    // Optimized search with caching
    public Uni<SearchResult> searchContacts(SearchCriteria criteria) {
        String cacheKey = buildCacheKey(criteria);
        
        return redis.get(cacheKey)
            .onItem().ifNotNull().transform(cached -> 
                Json.decodeValue(cached.toString(), SearchResult.class))
            .onItem().ifNull().switchTo(() -> 
                performOptimizedSearch(criteria)
                    .onItem().call(result -> 
                        redis.setex(cacheKey, 300, Json.encode(result))
                    )
            );
    }
    
    private Uni<SearchResult> performOptimizedSearch(SearchCriteria criteria) {
        // Build optimized query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ContactProjection> query = cb.createQuery(ContactProjection.class);
        Root<ContactProjection> root = query.from(ContactProjection.class);
        
        // Add indexes hints
        query.hint("javax.persistence.index", "idx_contact_search");
        
        // Build predicates
        List<Predicate> predicates = buildPredicates(criteria, cb, root);
        
        // Add fetch joins for eager loading
        if (criteria.includeRelationships()) {
            root.fetch("relationships", JoinType.LEFT);
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        
        // Apply sorting
        if (criteria.getSortBy() != null) {
            query.orderBy(buildOrderBy(criteria, cb, root));
        }
        
        // Execute with pagination
        TypedQuery<ContactProjection> typedQuery = em.createQuery(query);
        typedQuery.setFirstResult(criteria.getOffset());
        typedQuery.setMaxResults(criteria.getLimit());
        
        // Use query result cache
        typedQuery.setHint("org.hibernate.cacheable", true);
        
        return Uni.createFrom().item(() -> {
            List<ContactProjection> results = typedQuery.getResultList();
            long total = getTotalCount(criteria);
            
            return SearchResult.builder()
                .items(results)
                .total(total)
                .offset(criteria.getOffset())
                .limit(criteria.getLimit())
                .build();
        });
    }
}
```

**Vollständiger Code:** [backend/ContactQueryOptimizer.java](./code/backend/ContactQueryOptimizer.java)

### 2. Read Model Projections

```java
// ContactSearchProjection.java
@Entity
@Table(name = "contact_search_view")
@Immutable
@Cacheable
@NamedQueries({
    @NamedQuery(
        name = "ContactSearch.findByWarmth",
        query = "SELECT c FROM ContactSearchProjection c WHERE c.warmthScore BETWEEN :min AND :max",
        hints = @QueryHint(name = "org.hibernate.cacheable", value = "true")
    ),
    @NamedQuery(
        name = "ContactSearch.findStale",
        query = "SELECT c FROM ContactSearchProjection c WHERE c.lastInteraction < :date",
        hints = @QueryHint(name = "org.hibernate.cacheable", value = "true")
    )
})
public class ContactSearchProjection {
    
    @Id
    private UUID id;
    
    @Column(name = "full_name")
    @FullTextField(analyzer = "name_analyzer")
    private String fullName;
    
    @Column(name = "company_name")
    @FullTextField
    private String companyName;
    
    @Column(name = "warmth_score")
    private Integer warmthScore;
    
    @Column(name = "last_interaction")
    private Instant lastInteraction;
    
    @Column(name = "tags", columnDefinition = "text[]")
    @Type(type = "string-array")
    private String[] tags;
    
    @Column(name = "location_count")
    private Integer locationCount;
    
    // Denormalized for performance
    @Column(name = "primary_email")
    private String primaryEmail;
    
    @Column(name = "primary_phone")
    private String primaryPhone;
}
```

### 3. Batch Processing Optimization

```java
// ContactBatchProcessor.java
@ApplicationScoped
public class ContactBatchProcessor {
    
    @ConfigProperty(name = "batch.size", defaultValue = "100")
    int batchSize;
    
    @Inject
    @Channel("contact-updates")
    Multi<ContactUpdate> updates;
    
    @Incoming("contact-updates")
    @Outgoing("processed-updates")
    public Multi<List<ContactUpdate>> processBatches(Multi<ContactUpdate> stream) {
        return stream
            .group().intoLists().of(batchSize)
            .onOverflow().buffer(1000)
            .onItem().transformToUniAndMerge(batch -> 
                processBatch(batch)
                    .onFailure().retry().withBackOff(Duration.ofSeconds(1)).atMost(3)
            );
    }
    
    private Uni<List<ContactUpdate>> processBatch(List<ContactUpdate> batch) {
        return Uni.createFrom().item(() -> {
            // Bulk update in single transaction
            em.getTransaction().begin();
            
            try {
                // Use batch inserts/updates
                String sql = "UPDATE contacts SET warmth_score = ?, last_updated = ? WHERE id = ?";
                PreparedStatement ps = em.unwrap(Connection.class).prepareStatement(sql);
                
                for (ContactUpdate update : batch) {
                    ps.setInt(1, update.getWarmthScore());
                    ps.setTimestamp(2, Timestamp.from(Instant.now()));
                    ps.setObject(3, update.getContactId());
                    ps.addBatch();
                }
                
                ps.executeBatch();
                em.getTransaction().commit();
                
                // Invalidate caches
                invalidateCaches(batch);
                
                return batch;
                
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw new BatchProcessingException("Failed to process batch", e);
            }
        });
    }
}
```

## 🎨 Frontend Implementation

### Virtual Scrolling Table

```typescript
// components/performance/VirtualContactTable.tsx
export const VirtualContactTable: React.FC<VirtualTableProps> = ({
  filters,
  onSelectionChange
}) => {
  const rowVirtualizer = useVirtualizer({
    count: 10000, // Total possible rows
    getScrollElement: () => parentRef.current,
    estimateSize: () => 53, // Row height
    overscan: 5
  });
  
  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage
  } = useInfiniteQuery({
    queryKey: ['contacts', filters],
    queryFn: ({ pageParam = 0 }) => 
      contactApi.search({
        ...filters,
        offset: pageParam,
        limit: 50
      }),
    getNextPageParam: (lastPage, pages) => 
      lastPage.hasMore ? pages.length * 50 : undefined,
    refetchInterval: 30000 // Refresh every 30s
  });
  
  // Flatten pages
  const allRows = data?.pages.flatMap(page => page.items) ?? [];
  
  // Trigger fetch when scrolling near bottom
  React.useEffect(() => {
    const lastItem = rowVirtualizer.getVirtualItems().at(-1);
    
    if (!lastItem) return;
    
    if (
      lastItem.index >= allRows.length - 1 &&
      hasNextPage &&
      !isFetchingNextPage
    ) {
      fetchNextPage();
    }
  }, [
    hasNextPage,
    fetchNextPage,
    allRows.length,
    isFetchingNextPage,
    rowVirtualizer.getVirtualItems()
  ]);
  
  return (
    <TableContainer
      ref={parentRef}
      sx={{ height: '600px', overflow: 'auto' }}
    >
      <Table stickyHeader>
        <TableHead>
          <TableRow>
            <TableCell padding="checkbox">
              <Checkbox />
            </TableCell>
            <TableCell>Name</TableCell>
            <TableCell>Unternehmen</TableCell>
            <TableCell>Wärme</TableCell>
            <TableCell>Letzte Interaktion</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          <div
            style={{
              height: `${rowVirtualizer.getTotalSize()}px`,
              width: '100%',
              position: 'relative'
            }}
          >
            {rowVirtualizer.getVirtualItems().map(virtualRow => {
              const contact = allRows[virtualRow.index];
              if (!contact) return null;
              
              return (
                <TableRow
                  key={contact.id}
                  style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '100%',
                    height: `${virtualRow.size}px`,
                    transform: `translateY(${virtualRow.start}px)`
                  }}
                >
                  <TableCell padding="checkbox">
                    <Checkbox value={contact.id} />
                  </TableCell>
                  <TableCell>{contact.fullName}</TableCell>
                  <TableCell>{contact.companyName}</TableCell>
                  <TableCell>
                    <WarmthIndicator score={contact.warmthScore} />
                  </TableCell>
                  <TableCell>
                    {formatRelativeTime(contact.lastInteraction)}
                  </TableCell>
                </TableRow>
              );
            })}
          </div>
        </TableBody>
      </Table>
    </TableContainer>
  );
};
```

**Vollständiger Code:** [frontend/VirtualContactTable.tsx](./code/frontend/performance/VirtualContactTable.tsx)

### Optimized Image Loading

```typescript
// components/performance/OptimizedAvatar.tsx
export const OptimizedAvatar: React.FC<OptimizedAvatarProps> = ({
  contactId,
  name,
  size = 40
}) => {
  const [imageError, setImageError] = useState(false);
  const [isIntersecting, setIsIntersecting] = useState(false);
  const imgRef = useRef<HTMLDivElement>(null);
  
  // Intersection Observer for lazy loading
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsIntersecting(true);
          observer.disconnect();
        }
      },
      { threshold: 0.1 }
    );
    
    if (imgRef.current) {
      observer.observe(imgRef.current);
    }
    
    return () => observer.disconnect();
  }, []);
  
  const imageUrl = `${CDN_URL}/avatars/${contactId}.webp?size=${size}`;
  const fallbackUrl = `${CDN_URL}/avatars/default.webp?size=${size}`;
  
  return (
    <Avatar
      ref={imgRef}
      sx={{ width: size, height: size }}
      src={isIntersecting && !imageError ? imageUrl : undefined}
      onError={() => setImageError(true)}
    >
      {(!isIntersecting || imageError) && getInitials(name)}
    </Avatar>
  );
};
```

## 📊 Performance Monitoring

### Metrics Collection

```java
// PerformanceMetricsCollector.java
@ApplicationScoped
public class PerformanceMetricsCollector {
    
    @Inject
    MeterRegistry registry;
    
    @Inject
    EventBus eventBus;
    
    public void recordQueryPerformance(String queryName, long duration) {
        registry.timer("contact.query.duration", 
            "query", queryName
        ).record(duration, TimeUnit.MILLISECONDS);
        
        if (duration > 1000) {
            // Alert on slow queries
            SlowQueryAlert alert = SlowQueryAlert.builder()
                .queryName(queryName)
                .duration(duration)
                .timestamp(Instant.now())
                .build();
            
            eventBus.publish(alert);
        }
    }
    
    @Scheduled(every = "5m")
    void collectSystemMetrics() {
        // JVM metrics
        registry.gauge("jvm.heap.used", 
            Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        
        // Database pool metrics
        HikariDataSource ds = (HikariDataSource) dataSource;
        registry.gauge("db.connections.active", ds.getHikariPoolMXBean().getActiveConnections());
        registry.gauge("db.connections.idle", ds.getHikariPoolMXBean().getIdleConnections());
        
        // Cache metrics
        Cache cache = cacheManager.getCache("contacts");
        registry.gauge("cache.size", cache.size());
        registry.gauge("cache.hit.ratio", cache.getStatistics().getHitRatio());
    }
}
```

## 🧪 Performance Tests

### Load Testing

```java
@Test
void shouldHandleConcurrentSearches() throws Exception {
    // Given
    int concurrentUsers = 100;
    CountDownLatch latch = new CountDownLatch(concurrentUsers);
    List<Future<SearchResult>> futures = new ArrayList<>();
    
    // When
    ExecutorService executor = Executors.newFixedThreadPool(10);
    
    for (int i = 0; i < concurrentUsers; i++) {
        futures.add(executor.submit(() -> {
            try {
                SearchCriteria criteria = SearchCriteria.builder()
                    .searchTerm("test")
                    .limit(50)
                    .build();
                    
                return contactService.search(criteria).await().indefinitely();
            } finally {
                latch.countDown();
            }
        }));
    }
    
    // Then
    assertTrue(latch.await(10, TimeUnit.SECONDS));
    
    // Verify all searches completed successfully
    for (Future<SearchResult> future : futures) {
        SearchResult result = future.get();
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isNotEmpty();
    }
    
    // Check performance metrics
    Timer.Sample sample = Timer.start(registry);
    double avgDuration = sample.stop(registry.timer("search.duration"));
    assertThat(avgDuration).isLessThan(200); // < 200ms average
}
```

## 📝 Checkliste

- [ ] Query Optimizer implementiert
- [ ] Caching Layer eingerichtet
- [ ] Database Indexes erstellt
- [ ] Read Projections optimiert
- [ ] Virtual Scrolling Frontend
- [ ] Lazy Loading implementiert
- [ ] Performance Monitoring aktiv

## 🔗 Weiterführende Links

- **Performance Guide:** [Performance Best Practices](./guides/PERFORMANCE_BEST_PRACTICES.md)
- **Caching Strategy:** [Cache Design](./guides/CACHE_DESIGN.md)
- **Nächster Schritt:** [→ Tag 4: Error Handling & Recovery](./DAY4_RESILIENCE.md)

---

**Status:** 📋 Bereit zur Implementierung