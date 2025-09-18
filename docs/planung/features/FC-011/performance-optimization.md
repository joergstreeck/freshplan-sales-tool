# FC-011: Performance-Optimierung für Pipeline-Cockpit Integration

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-011-pipeline-cockpit-integration.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-011-pipeline-cockpit-integration.md)  
**Fokus:** Caching, Preloading und Performance-Strategien

## 🚀 Performance-Ziele

| Metrik | Ziel | Messung |
|--------|------|---------|
| **Click-to-Load Time** | < 500ms | Zeit von Klick bis Cockpit-Render |
| **Preload Hit Rate** | > 60% | Cache-Treffer bei Hover-Preload |
| **Memory Footprint** | < 50MB zusätzlich | Zusätzlicher RAM durch Caching |
| **Context Switch** | < 200ms | Zeit zwischen Opportunities |
| **Initial Page Load** | < 2s | Time to Interactive |

## 🔄 Caching-Strategie

### 1. Multi-Layer Cache Architecture

```typescript
// Layer 1: Memory Cache (React Query)
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 15 * 60 * 1000, // 15 minutes
      refetchOnWindowFocus: false,
    },
  },
});

// Layer 2: LRU Cache for Heavy Data
class CustomerDataCache {
  private cache: LRUCache<string, CustomerFullData>;
  
  constructor() {
    this.cache = new LRUCache({
      max: 50, // Max 50 customers in memory
      maxSize: 50 * 1024 * 1024, // 50MB total
      sizeCalculation: (value) => JSON.stringify(value).length,
      ttl: 1000 * 60 * 10, // 10 minutes
    });
  }
  
  async get(customerId: string): Promise<CustomerFullData | null> {
    const cached = this.cache.get(customerId);
    if (cached) {
      // Update access time
      this.cache.get(customerId);
      return cached;
    }
    return null;
  }
  
  set(customerId: string, data: CustomerFullData): void {
    this.cache.set(customerId, data);
  }
  
  preload(customerIds: string[]): void {
    // Batch preload for efficiency
    const uncached = customerIds.filter(id => !this.cache.has(id));
    if (uncached.length > 0) {
      this.batchFetch(uncached);
    }
  }
  
  private async batchFetch(customerIds: string[]): Promise<void> {
    const chunks = chunk(customerIds, 10); // Max 10 per request
    
    for (const chunk of chunks) {
      const customers = await customerApi.getBatch(chunk);
      customers.forEach(customer => {
        this.set(customer.id, customer);
      });
    }
  }
}
```

### 2. Intelligent Preloading

```typescript
// Hover-based Preloading
export const useIntelligentPreload = () => {
  const customerCache = useCustomerCache();
  const queryClient = useQueryClient();
  const [preloadHistory, setPreloadHistory] = useState<Map<string, number>>(new Map());
  
  const preloadOpportunity = useCallback((opportunity: Opportunity) => {
    const { customerId, id } = opportunity;
    const lastPreload = preloadHistory.get(id) || 0;
    const now = Date.now();
    
    // Debounce: Don't preload same opportunity within 30s
    if (now - lastPreload < 30000) return;
    
    // Priority-based preloading
    const priority = calculatePreloadPriority(opportunity);
    
    if (priority > 0.5) {
      // High priority: Preload immediately
      Promise.all([
        queryClient.prefetchQuery({
          queryKey: ['customer', customerId],
          queryFn: () => customerApi.getById(customerId),
        }),
        queryClient.prefetchQuery({
          queryKey: ['activities', customerId],
          queryFn: () => activityApi.getByCustomerId(customerId),
        }),
      ]);
      
      setPreloadHistory(prev => new Map(prev).set(id, now));
    } else {
      // Low priority: Queue for batch preload
      queueForBatchPreload(customerId);
    }
  }, [queryClient, preloadHistory]);
  
  return { preloadOpportunity };
};

// Priority Calculation
const calculatePreloadPriority = (opportunity: Opportunity): number => {
  let score = 0;
  
  // High value opportunities
  if (opportunity.value > 50000) score += 0.3;
  
  // Close to closing
  const daysToClose = differenceInDays(opportunity.expectedCloseDate, new Date());
  if (daysToClose < 7) score += 0.3;
  
  // Recent activity
  const daysSinceActivity = differenceInDays(new Date(), opportunity.lastActivityDate);
  if (daysSinceActivity < 3) score += 0.2;
  
  // Advanced stages
  if (['PROPOSAL', 'NEGOTIATION', 'RENEWAL'].includes(opportunity.stage)) {
    score += 0.2;
  }
  
  return Math.min(score, 1);
};
```

### 3. Virtual DOM Optimization

```typescript
// Virtualized Pipeline with Intersection Observer
export const VirtualizedPipelineColumn: React.FC<{
  opportunities: Opportunity[];
  stage: OpportunityStage;
}> = ({ opportunities, stage }) => {
  const [visibleRange, setVisibleRange] = useState({ start: 0, end: 10 });
  const columnRef = useRef<HTMLDivElement>(null);
  
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const index = parseInt(entry.target.getAttribute('data-index') || '0');
            
            // Expand visible range
            setVisibleRange(prev => ({
              start: Math.max(0, index - 5),
              end: Math.min(opportunities.length, index + 15)
            }));
            
            // Preload nearby opportunities
            const nearbyOpps = opportunities.slice(
              Math.max(0, index - 3),
              Math.min(opportunities.length, index + 3)
            );
            nearbyOpps.forEach(opp => preloadOpportunity(opp));
          }
        });
      },
      { root: columnRef.current, rootMargin: '100px' }
    );
    
    // Observe sentinel elements
    const sentinels = columnRef.current?.querySelectorAll('.visibility-sentinel');
    sentinels?.forEach(el => observer.observe(el));
    
    return () => observer.disconnect();
  }, [opportunities]);
  
  return (
    <div ref={columnRef} className="pipeline-column">
      {opportunities.map((opp, index) => {
        const isVisible = index >= visibleRange.start && index <= visibleRange.end;
        
        return (
          <div key={opp.id} data-index={index}>
            {index % 10 === 0 && (
              <div className="visibility-sentinel" data-index={index} />
            )}
            {isVisible ? (
              <OpportunityCard opportunity={opp} />
            ) : (
              <div className="opportunity-placeholder" style={{ height: '120px' }} />
            )}
          </div>
        );
      })}
    </div>
  );
};
```

### 4. Optimistic Updates

```typescript
// Optimistic UI Updates for Instant Feedback
export const useOptimisticOpportunityUpdate = () => {
  const queryClient = useQueryClient();
  
  const moveOpportunity = useMutation({
    mutationFn: ({ opportunityId, newStage }: MoveData) => 
      opportunityApi.updateStage(opportunityId, newStage),
      
    onMutate: async ({ opportunityId, newStage }) => {
      // Cancel in-flight queries
      await queryClient.cancelQueries({ queryKey: ['pipeline'] });
      
      // Snapshot current state
      const previousPipeline = queryClient.getQueryData(['pipeline']);
      
      // Optimistically update
      queryClient.setQueryData(['pipeline'], (old: PipelineData) => {
        const updated = { ...old };
        
        // Remove from old stage
        Object.keys(updated.stages).forEach(stage => {
          updated.stages[stage] = updated.stages[stage].filter(
            opp => opp.id !== opportunityId
          );
        });
        
        // Add to new stage
        const opportunity = findOpportunity(old, opportunityId);
        if (opportunity) {
          updated.stages[newStage].push({
            ...opportunity,
            stage: newStage,
            updatedAt: new Date().toISOString()
          });
        }
        
        return updated;
      });
      
      return { previousPipeline };
    },
    
    onError: (err, variables, context) => {
      // Rollback on error
      if (context?.previousPipeline) {
        queryClient.setQueryData(['pipeline'], context.previousPipeline);
      }
      
      toast.error('Fehler beim Verschieben der Opportunity');
    },
    
    onSettled: () => {
      // Refetch to ensure consistency
      queryClient.invalidateQueries({ queryKey: ['pipeline'] });
    }
  });
  
  return { moveOpportunity };
};
```

### 5. Bundle & Load Optimization

```typescript
// Lazy Load Heavy Components
const CustomerCockpit = lazy(() => 
  import(/* webpackChunkName: "cockpit" */ '../components/CustomerCockpit')
);

const OpportunityDetailModal = lazy(() =>
  import(/* webpackChunkName: "opp-detail" */ '../components/OpportunityDetailModal')
);

// Preload Critical Chunks
export const preloadCriticalChunks = () => {
  // Preload cockpit when user hovers over pipeline
  const link = document.createElement('link');
  link.rel = 'prefetch';
  link.href = '/static/js/cockpit.[hash].js';
  document.head.appendChild(link);
};
```

### 6. API Response Optimization

```yaml
# Optimized API Endpoints

# Lightweight list endpoint
GET /api/opportunities/pipeline/light
Response:
  - Only essential fields (id, name, value, stage, customerId)
  - No nested objects
  - Compressed with gzip

# Full details on demand
GET /api/opportunities/{id}/full
Response:
  - Complete opportunity data
  - Includes customer summary
  - Related activities (last 10)
  - ETags for caching

# Batch endpoint for preloading
POST /api/opportunities/batch-summary
Body: { ids: string[] }
Response:
  - Summary data for multiple opportunities
  - Optimized for cockpit display
```

### 7. Memory Management

```typescript
// Cleanup Strategy
export const useMemoryCleanup = () => {
  const queryClient = useQueryClient();
  
  useEffect(() => {
    const cleanup = setInterval(() => {
      // Remove stale queries
      queryClient.removeQueries({
        predicate: (query) => {
          const lastFetch = query.state.dataUpdatedAt;
          const now = Date.now();
          const isStale = now - lastFetch > 30 * 60 * 1000; // 30 minutes
          const isInactive = !query.getObserversCount();
          
          return isStale && isInactive;
        }
      });
      
      // Garbage collect customer cache
      if (customerCache.size > 30) {
        customerCache.prune(); // Remove least recently used
      }
    }, 5 * 60 * 1000); // Every 5 minutes
    
    return () => clearInterval(cleanup);
  }, [queryClient]);
};
```

## 📊 Performance Monitoring

```typescript
// Performance Tracking
export const trackPerformance = (metric: string, value: number) => {
  // Send to analytics
  if (window.gtag) {
    window.gtag('event', 'timing_complete', {
      name: metric,
      value: Math.round(value),
      event_category: 'Pipeline Cockpit'
    });
  }
  
  // Local monitoring
  if (process.env.NODE_ENV === 'development') {
    console.log(`[PERF] ${metric}: ${value}ms`);
  }
};

// Usage
const handleOpportunityClick = async (opportunity: Opportunity) => {
  const startTime = performance.now();
  
  await loadCustomerIntoCockpit(opportunity.customerId);
  
  const loadTime = performance.now() - startTime;
  trackPerformance('cockpit_load_time', loadTime);
};
```