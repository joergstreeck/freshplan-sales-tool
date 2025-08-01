# ⚡ Performance Optimization - Blitzschnelle Contact Experience

**Phase:** 2 - Intelligence Features  
**Tag:** 5 der Woche 2  
**Status:** 🎯 Ready for Implementation  

## 🧭 Navigation

**← Zurück:** [Location Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/LOCATION_INTELLIGENCE.md)  
**→ Nächster:** [DSGVO Consent](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/DSGVO_CONSENT.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  

## 🎯 Vision: Performance als Feature

**Performance Optimization** macht Contact Management **blitzschnell und butterweich**:

> "Keine Wartezeiten, keine Ruckler - nur flüssige Produktivität"

## 🚀 Performance Strategy

### Optimization Targets

```typescript
// performance/targets.ts
export const PERFORMANCE_TARGETS = {
  // Initial Load
  firstContentfulPaint: 1200, // ms
  timeToInteractive: 2500, // ms
  contactListRender: 200, // ms for 100 contacts
  
  // Runtime Performance
  searchLatency: 50, // ms
  filterLatency: 30, // ms
  scrollFPS: 60,
  
  // API Performance
  apiResponseTime: 200, // ms p95
  batchOperationTime: 500, // ms for 10 items
  
  // Memory
  maxMemoryUsage: 50, // MB
  maxContactsInMemory: 1000,
  
  // Bundle Size
  mainBundleSize: 150, // KB gzipped
  lazyChunkSize: 50 // KB gzipped
};
```

## 🎨 Frontend Optimizations

### Virtual Scrolling for Large Lists

```typescript
// components/VirtualContactList.tsx
import { VariableSizeList as List } from 'react-window';
import AutoSizer from 'react-virtualized-auto-sizer';

export const VirtualContactList: React.FC<{
  contacts: Contact[];
  warmthData?: Map<string, RelationshipWarmth>;
  onContactAction: (action: ContactAction) => void;
}> = ({ contacts, warmthData, onContactAction }) => {
  const listRef = useRef<List>(null);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Memoized filtered contacts
  const filteredContacts = useMemo(() => {
    if (!searchTerm) return contacts;
    
    const term = searchTerm.toLowerCase();
    return contacts.filter(contact =>
      `${contact.firstName} ${contact.lastName}`.toLowerCase().includes(term) ||
      contact.email?.toLowerCase().includes(term) ||
      contact.position?.toLowerCase().includes(term)
    );
  }, [contacts, searchTerm]);
  
  // Dynamic row height based on content
  const getItemSize = useCallback((index: number) => {
    const contact = filteredContacts[index];
    let baseHeight = 120; // Base card height
    
    if (contact.assignedLocationName) baseHeight += 24;
    if (warmthData?.get(contact.id)) baseHeight += 32;
    
    return baseHeight;
  }, [filteredContacts, warmthData]);
  
  // Row renderer with memoization
  const Row = memo(({ index, style }: { index: number; style: React.CSSProperties }) => {
    const contact = filteredContacts[index];
    const warmth = warmthData?.get(contact.id);
    
    return (
      <div style={style}>
        <Box px={2} py={1}>
          <SmartContactCard
            contact={contact}
            warmth={warmth}
            onEdit={(c) => onContactAction({ type: 'edit', contact: c })}
            onDelete={(id) => onContactAction({ type: 'delete', contactId: id })}
            onSetPrimary={(id) => onContactAction({ type: 'setPrimary', contactId: id })}
            onAssignLocation={(id) => onContactAction({ type: 'assignLocation', contactId: id })}
          />
        </Box>
      </div>
    );
  });
  
  // Optimized search with debouncing
  const debouncedSearch = useMemo(
    () => debounce((value: string) => {
      setSearchTerm(value);
      // Reset scroll position on search
      listRef.current?.scrollToItem(0);
    }, 300),
    []
  );
  
  return (
    <Box height="100%" display="flex" flexDirection="column">
      {/* Search Header */}
      <Box p={2} bgcolor="background.paper" position="sticky" top={0} zIndex={1}>
        <TextField
          fullWidth
          placeholder="Kontakte durchsuchen..."
          onChange={(e) => debouncedSearch(e.target.value)}
          InputProps={{
            startAdornment: <SearchIcon />,
            endAdornment: (
              <Chip
                label={`${filteredContacts.length} von ${contacts.length}`}
                size="small"
              />
            )
          }}
        />
      </Box>
      
      {/* Virtual List */}
      <Box flex={1}>
        <AutoSizer>
          {({ height, width }) => (
            <List
              ref={listRef}
              height={height}
              width={width}
              itemCount={filteredContacts.length}
              itemSize={getItemSize}
              overscanCount={5}
              itemData={{ filteredContacts, warmthData, onContactAction }}
            >
              {Row}
            </List>
          )}
        </AutoSizer>
      </Box>
    </Box>
  );
};
```

### Lazy Loading & Code Splitting

```typescript
// routes/ContactRoutes.tsx
import { lazy, Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import { ContactListSkeleton } from '../components/skeletons';

// Lazy load heavy components
const ContactDashboard = lazy(() => 
  import(/* webpackChunkName: "contact-dashboard" */ '../pages/ContactDashboard')
);

const ContactTimeline = lazy(() => 
  import(/* webpackChunkName: "contact-timeline" */ '../components/ContactTimeline')
);

const LocationIntelligence = lazy(() => 
  import(/* webpackChunkName: "location-intelligence" */ '../components/LocationIntelligence')
);

export const ContactRoutes = () => {
  return (
    <Suspense fallback={<ContactListSkeleton />}>
      <Routes>
        <Route path="/" element={<ContactDashboard />} />
        <Route path="/:contactId/timeline" element={<ContactTimeline />} />
        <Route path="/locations" element={<LocationIntelligence />} />
      </Routes>
    </Suspense>
  );
};
```

### Optimistic UI Updates

```typescript
// hooks/useOptimisticContacts.ts
export const useOptimisticContacts = () => {
  const queryClient = useQueryClient();
  
  const updateContact = useMutation({
    mutationFn: (data: { id: string; updates: Partial<Contact> }) =>
      contactApi.updateContact(data.id, data.updates),
    
    // Optimistic update
    onMutate: async ({ id, updates }) => {
      // Cancel outgoing queries
      await queryClient.cancelQueries({ queryKey: ['contacts'] });
      
      // Snapshot previous value
      const previousContacts = queryClient.getQueryData(['contacts']);
      
      // Optimistically update
      queryClient.setQueryData(['contacts'], (old: Contact[]) => {
        return old.map(contact =>
          contact.id === id ? { ...contact, ...updates } : contact
        );
      });
      
      return { previousContacts };
    },
    
    // Rollback on error
    onError: (err, variables, context) => {
      if (context?.previousContacts) {
        queryClient.setQueryData(['contacts'], context.previousContacts);
      }
    },
    
    // Refetch after success
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['contacts'] });
    }
  });
  
  return { updateContact };
};
```

## 🔧 Backend Optimizations

### Query Optimization

```java
// ContactRepository.java - Optimized queries
@ApplicationScoped
public class ContactRepository {
    
    @Inject
    EntityManager em;
    
    /**
     * Fetch contacts with warmth data in single query
     */
    public List<ContactWithWarmth> findContactsWithWarmth(UUID customerId) {
        return em.createQuery(
            "SELECT new de.freshplan.dto.ContactWithWarmth(" +
            "   c, " +
            "   w.temperature, " +
            "   w.score, " +
            "   w.lastInteraction, " +
            "   COUNT(DISTINCT i.id) " +
            ") " +
            "FROM Contact c " +
            "LEFT JOIN RelationshipWarmth w ON w.contactId = c.id " +
            "LEFT JOIN ContactInteraction i ON i.contact = c AND i.timestamp > :thirtyDaysAgo " +
            "WHERE c.customer.id = :customerId AND c.isActive = true " +
            "GROUP BY c.id, w.temperature, w.score, w.lastInteraction " +
            "ORDER BY c.isPrimary DESC, w.score DESC",
            ContactWithWarmth.class
        )
        .setParameter("customerId", customerId)
        .setParameter("thirtyDaysAgo", Instant.now().minus(30, ChronoUnit.DAYS))
        .setHint("org.hibernate.fetchSize", 50)
        .getResultList();
    }
    
    /**
     * Batch update for better performance
     */
    @Transactional
    public void batchUpdateWarmthScores(List<WarmthUpdate> updates) {
        // Use batch processing
        em.unwrap(Session.class).setJdbcBatchSize(25);
        
        for (int i = 0; i < updates.size(); i++) {
            WarmthUpdate update = updates.get(i);
            em.createQuery(
                "UPDATE RelationshipWarmth w " +
                "SET w.score = :score, w.temperature = :temp, w.updatedAt = :now " +
                "WHERE w.contactId = :contactId"
            )
            .setParameter("score", update.getScore())
            .setParameter("temp", update.getTemperature())
            .setParameter("now", Instant.now())
            .setParameter("contactId", update.getContactId())
            .executeUpdate();
            
            // Flush and clear every 25 items
            if (i % 25 == 0) {
                em.flush();
                em.clear();
            }
        }
    }
}
```

### Caching Strategy

```java
// CacheConfiguration.java
@ApplicationScoped
public class ContactCacheManager {
    
    @Inject
    @CacheName("contact-cache")
    Cache<String, Contact> contactCache;
    
    @Inject
    @CacheName("warmth-cache")
    Cache<String, RelationshipWarmth> warmthCache;
    
    /**
     * Get contact with cache
     */
    public Contact getContact(UUID contactId) {
        String key = contactId.toString();
        
        return contactCache.get(key, k -> {
            // Load from database if not in cache
            Contact contact = Contact.findById(contactId);
            if (contact == null) {
                throw new NotFoundException("Contact not found: " + contactId);
            }
            return contact;
        });
    }
    
    /**
     * Batch preload for better performance
     */
    public void preloadContactsForCustomer(UUID customerId) {
        List<Contact> contacts = Contact.find("customer.id = ?1 and isActive = true", customerId).list();
        
        // Parallel cache population
        contacts.parallelStream().forEach(contact -> {
            contactCache.put(contact.getId().toString(), contact);
        });
    }
    
    /**
     * Smart cache invalidation
     */
    @ConsumeEvent("contact.updated")
    public void onContactUpdated(ContactUpdatedEvent event) {
        // Invalidate specific cache entry
        contactCache.invalidate(event.getContactId().toString());
        warmthCache.invalidate(event.getContactId().toString());
        
        // Don't invalidate entire cache
    }
}
```

## 📊 Performance Monitoring

### Real User Monitoring (RUM)

```typescript
// monitoring/performanceMonitor.ts
export class PerformanceMonitor {
  private metrics: PerformanceMetrics = {
    fcp: 0,
    lcp: 0,
    fid: 0,
    cls: 0,
    ttfb: 0
  };
  
  initialize() {
    // First Contentful Paint
    new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        if (entry.name === 'first-contentful-paint') {
          this.metrics.fcp = entry.startTime;
          this.reportMetric('fcp', entry.startTime);
        }
      }
    }).observe({ entryTypes: ['paint'] });
    
    // Largest Contentful Paint
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const lastEntry = entries[entries.length - 1];
      this.metrics.lcp = lastEntry.renderTime || lastEntry.loadTime;
      this.reportMetric('lcp', this.metrics.lcp);
    }).observe({ entryTypes: ['largest-contentful-paint'] });
    
    // First Input Delay
    new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        this.metrics.fid = entry.processingStart - entry.startTime;
        this.reportMetric('fid', this.metrics.fid);
      }
    }).observe({ entryTypes: ['first-input'] });
    
    // Component render tracking
    this.trackComponentPerformance();
  }
  
  trackComponentPerformance() {
    // Measure contact list render time
    performance.mark('contact-list-start');
    
    // After render
    requestAnimationFrame(() => {
      performance.mark('contact-list-end');
      performance.measure(
        'contact-list-render',
        'contact-list-start',
        'contact-list-end'
      );
      
      const measure = performance.getEntriesByName('contact-list-render')[0];
      this.reportMetric('contact-list-render', measure.duration);
    });
  }
  
  private reportMetric(name: string, value: number) {
    // Send to analytics
    if (window.gtag) {
      window.gtag('event', 'performance', {
        metric_name: name,
        value: Math.round(value),
        page_path: window.location.pathname
      });
    }
    
    // Console warning if threshold exceeded
    if (value > PERFORMANCE_TARGETS[name]) {
      console.warn(`Performance warning: ${name} = ${value}ms (target: ${PERFORMANCE_TARGETS[name]}ms)`);
    }
  }
}
```

### Performance Dashboard

```typescript
// components/PerformanceDashboard.tsx
export const PerformanceDashboard: React.FC = () => {
  const [metrics, setMetrics] = useState<PerformanceMetrics>();
  const [showDetails, setShowDetails] = useState(false);
  
  useEffect(() => {
    const monitor = new PerformanceMonitor();
    monitor.initialize();
    
    // Get current metrics
    const currentMetrics = monitor.getMetrics();
    setMetrics(currentMetrics);
  }, []);
  
  const getScoreColor = (value: number, target: number): string => {
    if (value <= target) return 'success';
    if (value <= target * 1.5) return 'warning';
    return 'error';
  };
  
  if (!showDetails) {
    return (
      <IconButton
        onClick={() => setShowDetails(true)}
        sx={{ position: 'fixed', bottom: 80, right: 24 }}
      >
        <SpeedIcon />
      </IconButton>
    );
  }
  
  return (
    <Drawer
      anchor="right"
      open={showDetails}
      onClose={() => setShowDetails(false)}
    >
      <Box sx={{ width: 300, p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Performance Metrics
        </Typography>
        
        {metrics && Object.entries(metrics).map(([key, value]) => (
          <Box key={key} mb={2}>
            <Box display="flex" justifyContent="space-between" mb={0.5}>
              <Typography variant="body2">{key.toUpperCase()}</Typography>
              <Chip
                label={`${Math.round(value)}ms`}
                size="small"
                color={getScoreColor(value, PERFORMANCE_TARGETS[key])}
              />
            </Box>
            <LinearProgress
              variant="determinate"
              value={(value / PERFORMANCE_TARGETS[key]) * 100}
              color={getScoreColor(value, PERFORMANCE_TARGETS[key])}
            />
          </Box>
        ))}
        
        <Divider sx={{ my: 2 }} />
        
        <Typography variant="subtitle2" gutterBottom>
          Optimizations Active:
        </Typography>
        
        <Stack spacing={1}>
          <Chip label="Virtual Scrolling ✓" size="small" />
          <Chip label="Code Splitting ✓" size="small" />
          <Chip label="Image Lazy Loading ✓" size="small" />
          <Chip label="API Caching ✓" size="small" />
        </Stack>
      </Box>
    </Drawer>
  );
};
```

## 🧪 Performance Testing

```typescript
// __tests__/performance.test.ts
describe('Performance Tests', () => {
  it('should render 1000 contacts within performance budget', async () => {
    const contacts = Array.from({ length: 1000 }, (_, i) => 
      createMockContact({ id: `contact-${i}` })
    );
    
    const startTime = performance.now();
    
    render(<VirtualContactList contacts={contacts} onContactAction={jest.fn()} />);
    
    const endTime = performance.now();
    const renderTime = endTime - startTime;
    
    expect(renderTime).toBeLessThan(PERFORMANCE_TARGETS.contactListRender * 10); // 10x for 1000 items
  });
  
  it('should search contacts within latency target', async () => {
    const { rerender } = render(<ContactSearch contacts={mockContacts} />);
    
    const searchInput = screen.getByPlaceholderText('Kontakte durchsuchen...');
    
    const startTime = performance.now();
    await userEvent.type(searchInput, 'test');
    const endTime = performance.now();
    
    expect(endTime - startTime).toBeLessThan(PERFORMANCE_TARGETS.searchLatency);
  });
});
```

## 🎯 Success Metrics

### Core Web Vitals:
- **LCP:** < 2.5s (Good)
- **FID:** < 100ms (Good)
- **CLS:** < 0.1 (Good)

### Application Metrics:
- **Contact List Render:** < 200ms for 100 items
- **Search Responsiveness:** < 50ms
- **Memory Usage:** < 50MB
- **Bundle Size:** < 150KB gzipped

---

**Nächster Schritt:** [→ DSGVO Consent](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/DSGVO_CONSENT.md)

**Performance = User Experience! ⚡✨**