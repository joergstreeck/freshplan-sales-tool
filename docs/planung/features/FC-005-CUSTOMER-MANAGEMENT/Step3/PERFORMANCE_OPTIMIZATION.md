# ⚡ Performance Optimization - Blitzschnelle Contact Management

**Phase:** 1 - Core Requirements  
**Priorität:** 🔴 KRITISCH - UX entscheidend  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AI_DATA_ENRICHMENT.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/ACCESSIBILITY_MONITORING.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- User Experience (Keine Wartezeiten)
- Skalierbarkeit (1000+ Kontakte)
- Mobile Performance

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Performance Optimization implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Performance Services
mkdir -p backend/src/main/java/de/freshplan/performance
touch backend/src/main/java/de/freshplan/performance/service/CacheService.java
touch backend/src/main/java/de/freshplan/performance/service/QueryOptimizer.java
touch backend/src/main/java/de/freshplan/performance/config/CacheConfig.java
touch backend/src/main/java/de/freshplan/performance/interceptor/PerformanceInterceptor.java

# 2. Frontend Performance Components
mkdir -p frontend/src/features/customers/components/performance
touch frontend/src/features/customers/components/performance/VirtualContactList.tsx
touch frontend/src/features/customers/components/performance/LazyContactCard.tsx
touch frontend/src/features/customers/utils/debounce.ts
touch frontend/src/features/customers/utils/memoization.ts

# 3. Performance Monitoring
mkdir -p frontend/src/utils/performance
touch frontend/src/utils/performance/PerformanceMonitor.ts
touch frontend/src/utils/performance/metrics.ts

# 4. Database Indexes (nächste freie Nummer prüfen!)
ls -la backend/src/main/resources/db/migration/ | tail -5
# Erstelle V[NEXT]__add_performance_indexes.sql

# 5. Tests
mkdir -p frontend/src/features/customers/__tests__/performance
touch frontend/src/features/customers/__tests__/performance/load.test.ts
```

## 🎯 Das Problem: Langsame Ladezeiten bei vielen Kontakten

**Performance-Killer:**
- 🐌 500+ Kontakte = 5 Sekunden Ladezeit
- 📱 Mobile noch langsamer
- 🔄 Jeder Filter = Komplettes Neu-Laden
- 💾 Keine Offline-Fähigkeit
- 🎨 UI friert ein bei großen Listen

## 💡 DIE LÖSUNG: Multi-Layer Performance Optimization

### 1. Backend Query Optimization

**Datei:** `backend/src/main/java/de/freshplan/performance/service/QueryOptimizer.java`

```java
// CLAUDE: Query Optimizer mit N+1 Prevention und Smart Loading
// Pfad: backend/src/main/java/de/freshplan/performance/service/QueryOptimizer.java

package de.freshplan.performance.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.*;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.cache.CacheResult;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;

@ApplicationScoped
public class QueryOptimizer {
    
    @Inject
    EntityManager em;
    
    @Inject
    CacheService cacheService;
    
    /**
     * Optimized contact loading with eager fetching
     */
    public ContactLoadResult loadContactsOptimized(
        UUID customerId,
        ContactFilter filter,
        int page,
        int size
    ) {
        // Use criteria builder for dynamic query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CustomerContact> query = cb.createQuery(CustomerContact.class);
        Root<CustomerContact> root = query.from(CustomerContact.class);
        
        // Eager fetch to prevent N+1
        root.fetch("customer", JoinType.LEFT);
        root.fetch("roles", JoinType.LEFT);
        root.fetch("interactions", JoinType.LEFT);
        
        // Build predicates
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("customer").get("id"), customerId));
        
        if (filter.searchTerm != null && !filter.searchTerm.isEmpty()) {
            String searchPattern = "%" + filter.searchTerm.toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("firstName")), searchPattern),
                cb.like(cb.lower(root.get("lastName")), searchPattern),
                cb.like(cb.lower(root.get("email")), searchPattern),
                cb.like(cb.lower(root.get("company")), searchPattern)
            ));
        }
        
        if (filter.location != null) {
            predicates.add(cb.equal(root.get("location"), filter.location));
        }
        
        if (filter.decisionLevel != null) {
            predicates.add(cb.equal(root.get("decisionLevel"), filter.decisionLevel));
        }
        
        if (filter.hasEmail != null) {
            if (filter.hasEmail) {
                predicates.add(cb.isNotNull(root.get("email")));
            } else {
                predicates.add(cb.isNull(root.get("email")));
            }
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        
        // Sorting
        if (filter.sortBy != null) {
            Path<?> sortPath = root.get(filter.sortBy);
            query.orderBy(filter.sortDesc ? 
                cb.desc(sortPath) : cb.asc(sortPath));
        } else {
            // Default sort by last name, first name
            query.orderBy(
                cb.asc(root.get("lastName")),
                cb.asc(root.get("firstName"))
            );
        }
        
        // Execute with pagination
        List<CustomerContact> contacts = em.createQuery(query)
            .setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
        
        // Count query for total
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CustomerContact> countRoot = countQuery.from(CustomerContact.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates.toArray(new Predicate[0]));
        Long total = em.createQuery(countQuery).getSingleResult();
        
        // Batch load related data
        batchLoadRelatedData(contacts);
        
        // Calculate metrics
        ContactMetrics metrics = calculateMetrics(contacts);
        
        return ContactLoadResult.builder()
            .contacts(contacts)
            .total(total)
            .page(page)
            .size(size)
            .metrics(metrics)
            .cached(false)
            .build();
    }
    
    /**
     * Batch load related data to prevent N+1
     */
    private void batchLoadRelatedData(List<CustomerContact> contacts) {
        if (contacts.isEmpty()) return;
        
        Set<UUID> contactIds = new HashSet<>();
        for (CustomerContact contact : contacts) {
            contactIds.add(contact.getId());
        }
        
        // Load all interactions in one query
        Map<UUID, List<ContactInteraction>> interactionMap = 
            em.createQuery(
                "SELECT ci FROM ContactInteraction ci " +
                "WHERE ci.contact.id IN :ids " +
                "ORDER BY ci.occurredAt DESC",
                ContactInteraction.class
            )
            .setParameter("ids", contactIds)
            .getResultStream()
            .collect(Collectors.groupingBy(
                ci -> ci.getContact().getId()
            ));
        
        // Load warmth scores in batch
        Map<UUID, Float> warmthScores = batchCalculateWarmthScores(contactIds);
        
        // Load social profiles if needed
        Map<UUID, SocialProfile> socialProfiles = 
            em.createQuery(
                "SELECT sp FROM SocialProfile sp " +
                "WHERE sp.contact.id IN :ids",
                SocialProfile.class
            )
            .setParameter("ids", contactIds)
            .getResultStream()
            .collect(Collectors.toMap(
                sp -> sp.getContact().getId(),
                sp -> sp
            ));
        
        // Assign loaded data back to contacts
        for (CustomerContact contact : contacts) {
            contact.setInteractions(
                interactionMap.getOrDefault(contact.getId(), new ArrayList<>())
            );
            contact.setWarmthScore(
                warmthScores.get(contact.getId())
            );
            contact.setSocialProfile(
                socialProfiles.get(contact.getId())
            );
        }
    }
    
    /**
     * Cached search with Redis
     */
    @CacheResult(cacheName = "contact-search")
    public List<ContactSearchResult> searchContacts(
        @CacheKey String searchTerm,
        @CacheKey int limit
    ) {
        // Use full-text search if available
        String sql = """
            SELECT c.id, c.first_name, c.last_name, c.email, 
                   c.company, c.job_title,
                   ts_rank(search_vector, query) as rank
            FROM customer_contacts c,
                 plainto_tsquery('german', :term) query
            WHERE search_vector @@ query
            ORDER BY rank DESC
            LIMIT :limit
            """;
        
        return em.createNativeQuery(sql, ContactSearchResult.class)
            .setParameter("term", searchTerm)
            .setParameter("limit", limit)
            .getResultList();
    }
    
    /**
     * Projection for list views (minimal data)
     */
    public List<ContactListProjection> getContactListProjection(
        UUID customerId,
        int page,
        int size
    ) {
        return em.createQuery(
            "SELECT new de.freshplan.dto.ContactListProjection(" +
            "c.id, c.firstName, c.lastName, c.email, c.phone, " +
            "c.jobTitle, c.company, c.isPrimary, c.warmthScore) " +
            "FROM CustomerContact c " +
            "WHERE c.customer.id = :customerId " +
            "ORDER BY c.lastName, c.firstName",
            ContactListProjection.class
        )
        .setParameter("customerId", customerId)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
    }
    
    /**
     * Bulk operations optimized
     */
    @Transactional
    public void bulkUpdateContacts(List<UUID> contactIds, Map<String, Object> updates) {
        // Use batch updates
        String jpql = buildBulkUpdateQuery(updates);
        
        Query query = em.createQuery(jpql);
        query.setParameter("ids", contactIds);
        
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        
        int updated = query.executeUpdate();
        
        // Invalidate cache for updated contacts
        cacheService.invalidateContactCache(contactIds);
    }
}

// Cache Service
@ApplicationScoped
public class CacheService {
    
    @Inject
    @ConfigProperty(name = "cache.redis.enabled", defaultValue = "false")
    boolean redisEnabled;
    
    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();
    
    /**
     * Multi-tier caching strategy
     */
    public <T> T getOrCompute(
        String key,
        Supplier<T> supplier,
        Duration ttl
    ) {
        // L1: Local memory cache
        CacheEntry local = localCache.get(key);
        if (local != null && !local.isExpired()) {
            return (T) local.getValue();
        }
        
        // L2: Redis cache (if enabled)
        if (redisEnabled) {
            T redisValue = getFromRedis(key);
            if (redisValue != null) {
                // Update local cache
                localCache.put(key, new CacheEntry(redisValue, ttl));
                return redisValue;
            }
        }
        
        // Compute value
        T value = supplier.get();
        
        // Store in both caches
        localCache.put(key, new CacheEntry(value, ttl));
        if (redisEnabled) {
            storeInRedis(key, value, ttl);
        }
        
        return value;
    }
    
    /**
     * Smart cache warming
     */
    @Scheduled(cron = "0 0 6 * * ?") // 6 AM daily
    void warmCache() {
        log.info("Starting cache warming");
        
        // Pre-load frequently accessed data
        List<UUID> topCustomers = findTopAccessedCustomers(20);
        
        for (UUID customerId : topCustomers) {
            // Pre-load contacts
            String key = "contacts:" + customerId;
            List<CustomerContact> contacts = CustomerContact
                .find("customer.id", customerId)
                .list();
            
            localCache.put(key, new CacheEntry(contacts, Duration.ofHours(1)));
        }
        
        log.info("Cache warming completed");
    }
}
```

### 2. Frontend Virtual Scrolling

**Datei:** `frontend/src/features/customers/components/performance/VirtualContactList.tsx`

```typescript
// CLAUDE: Virtual Scrolling für große Contact Listen
// Pfad: frontend/src/features/customers/components/performance/VirtualContactList.tsx

import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { FixedSizeList as List } from 'react-window';
import AutoSizer from 'react-virtualized-auto-sizer';
import InfiniteLoader from 'react-window-infinite-loader';
import {
  Box,
  TextField,
  InputAdornment,
  CircularProgress,
  Typography,
  Skeleton,
  Alert
} from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import { debounce } from 'lodash';
import { ContactCard } from '../ContactCard';
import { useContactStore } from '../../stores/contactStore';
import { useIntersectionObserver } from '../../hooks/useIntersectionObserver';

interface VirtualContactListProps {
  customerId: string;
  onContactSelect?: (contact: Contact) => void;
}

export const VirtualContactList: React.FC<VirtualContactListProps> = ({
  customerId,
  onContactSelect
}) => {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(0);
  const listRef = useRef<List>(null);
  const infiniteLoaderRef = useRef<InfiniteLoader>(null);
  
  const PAGE_SIZE = 50;
  const ITEM_HEIGHT = 120;
  const SEARCH_DEBOUNCE = 300;
  
  // Memoized search handler
  const debouncedSearch = useMemo(
    () => debounce((term: string) => {
      setSearchTerm(term);
      setPage(0);
      setContacts([]);
      loadMoreContacts(0, term);
    }, SEARCH_DEBOUNCE),
    []
  );
  
  // Load contacts with batching
  const loadMoreContacts = useCallback(async (
    startIndex: number,
    search?: string
  ) => {
    if (isLoading) return;
    
    setIsLoading(true);
    try {
      const response = await fetch(
        `/api/contacts/optimized?` + new URLSearchParams({
          customerId,
          page: String(Math.floor(startIndex / PAGE_SIZE)),
          size: String(PAGE_SIZE),
          search: search || searchTerm || ''
        })
      );
      
      const data = await response.json();
      
      if (startIndex === 0) {
        setContacts(data.contacts);
      } else {
        setContacts(prev => [...prev, ...data.contacts]);
      }
      
      setTotalCount(data.total);
      
      // Pre-fetch next page in background
      if (data.contacts.length === PAGE_SIZE) {
        prefetchNextPage(startIndex + PAGE_SIZE);
      }
    } finally {
      setIsLoading(false);
    }
  }, [customerId, searchTerm]);
  
  // Prefetch next page for smooth scrolling
  const prefetchNextPage = useCallback((startIndex: number) => {
    const cacheKey = `contacts:${customerId}:${startIndex}:${searchTerm}`;
    
    // Check if already cached
    if (sessionStorage.getItem(cacheKey)) return;
    
    // Prefetch in background
    fetch(`/api/contacts/optimized?` + new URLSearchParams({
      customerId,
      page: String(Math.floor(startIndex / PAGE_SIZE)),
      size: String(PAGE_SIZE),
      search: searchTerm || ''
    }))
    .then(res => res.json())
    .then(data => {
      // Cache for 5 minutes
      sessionStorage.setItem(cacheKey, JSON.stringify({
        data,
        timestamp: Date.now()
      }));
    });
  }, [customerId, searchTerm]);
  
  // Check if item is loaded
  const isItemLoaded = useCallback((index: number) => {
    return index < contacts.length;
  }, [contacts]);
  
  // Load more items
  const loadMoreItems = useCallback((
    startIndex: number,
    stopIndex: number
  ) => {
    return loadMoreContacts(startIndex);
  }, [loadMoreContacts]);
  
  // Render row with memoization
  const Row = React.memo(({ index, style }: any) => {
    if (!isItemLoaded(index)) {
      return (
        <div style={style}>
          <Box p={2}>
            <Skeleton variant="rectangular" height={80} />
          </Box>
        </div>
      );
    }
    
    const contact = contacts[index];
    
    return (
      <div style={style}>
        <Box p={1}>
          <LazyContactCard
            contact={contact}
            onClick={() => onContactSelect?.(contact)}
          />
        </Box>
      </div>
    );
  });
  
  // Initial load
  useEffect(() => {
    loadMoreContacts(0);
  }, [customerId]);
  
  // Keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (!listRef.current) return;
      
      switch (e.key) {
        case 'Home':
          listRef.current.scrollToItem(0, 'start');
          break;
        case 'End':
          listRef.current.scrollToItem(totalCount - 1, 'end');
          break;
        case 'PageUp':
          listRef.current.scrollToItem(
            Math.max(0, (listRef.current as any)._instanceProps.scrollOffset - 10),
            'start'
          );
          break;
        case 'PageDown':
          listRef.current.scrollToItem(
            Math.min(totalCount - 1, (listRef.current as any)._instanceProps.scrollOffset + 10),
            'start'
          );
          break;
      }
    };
    
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [totalCount]);
  
  return (
    <Box height="100%" display="flex" flexDirection="column">
      {/* Search Bar */}
      <Box p={2}>
        <TextField
          fullWidth
          placeholder="Kontakte suchen..."
          onChange={(e) => debouncedSearch(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
            endAdornment: isLoading && (
              <InputAdornment position="end">
                <CircularProgress size={20} />
              </InputAdornment>
            )
          }}
        />
      </Box>
      
      {/* Results Count */}
      <Box px={2} pb={1}>
        <Typography variant="body2" color="text.secondary">
          {totalCount} Kontakte gefunden
        </Typography>
      </Box>
      
      {/* Virtual List */}
      <Box flex={1}>
        <AutoSizer>
          {({ height, width }) => (
            <InfiniteLoader
              ref={infiniteLoaderRef}
              isItemLoaded={isItemLoaded}
              itemCount={totalCount}
              loadMoreItems={loadMoreItems}
            >
              {({ onItemsRendered, ref }) => (
                <List
                  ref={(list) => {
                    ref(list);
                    listRef.current = list;
                  }}
                  height={height}
                  width={width}
                  itemCount={totalCount}
                  itemSize={ITEM_HEIGHT}
                  onItemsRendered={onItemsRendered}
                  overscanCount={5} // Pre-render 5 items outside viewport
                >
                  {Row}
                </List>
              )}
            </InfiniteLoader>
          )}
        </AutoSizer>
      </Box>
    </Box>
  );
};

// Lazy loaded contact card
const LazyContactCard: React.FC<{
  contact: Contact;
  onClick: () => void;
}> = React.memo(({ contact, onClick }) => {
  const [isVisible, setIsVisible] = useState(false);
  const cardRef = useRef<HTMLDivElement>(null);
  
  // Use Intersection Observer for lazy rendering
  useIntersectionObserver(
    cardRef,
    (entries) => {
      if (entries[0].isIntersecting) {
        setIsVisible(true);
      }
    },
    { threshold: 0.1 }
  );
  
  return (
    <div ref={cardRef} onClick={onClick}>
      {isVisible ? (
        <ContactCard contact={contact} />
      ) : (
        <Skeleton variant="rectangular" height={100} />
      )}
    </div>
  );
});
```

### 3. Database Performance Indexes

**Datei:** `backend/src/main/resources/db/migration/V[NEXT]__add_performance_indexes.sql`

```sql
-- CLAUDE: Performance Indexes für Contact Management
-- WICHTIG: Ersetze [NEXT] mit nächster freier Nummer!

-- Composite indexes for common queries
CREATE INDEX CONCURRENTLY idx_contacts_customer_name 
ON customer_contacts(customer_id, last_name, first_name);

CREATE INDEX CONCURRENTLY idx_contacts_customer_email 
ON customer_contacts(customer_id, email) 
WHERE email IS NOT NULL;

CREATE INDEX CONCURRENTLY idx_contacts_customer_location 
ON customer_contacts(customer_id, location) 
WHERE location IS NOT NULL;

CREATE INDEX CONCURRENTLY idx_contacts_decision_level 
ON customer_contacts(customer_id, decision_level) 
WHERE decision_level IS NOT NULL;

-- Full-text search index
CREATE INDEX CONCURRENTLY idx_contacts_search 
ON customer_contacts USING gin(
  to_tsvector('german', 
    coalesce(first_name, '') || ' ' || 
    coalesce(last_name, '') || ' ' || 
    coalesce(email, '') || ' ' || 
    coalesce(company, '') || ' ' ||
    coalesce(job_title, '')
  )
);

-- Partial indexes for filtering
CREATE INDEX CONCURRENTLY idx_contacts_primary 
ON customer_contacts(customer_id) 
WHERE is_primary = true;

CREATE INDEX CONCURRENTLY idx_contacts_no_email 
ON customer_contacts(customer_id) 
WHERE email IS NULL;

CREATE INDEX CONCURRENTLY idx_contacts_warmth 
ON customer_contacts(customer_id, warmth_score DESC NULLS LAST);

-- Interaction performance
CREATE INDEX CONCURRENTLY idx_interactions_contact_date 
ON contact_interactions(contact_id, occurred_at DESC);

CREATE INDEX CONCURRENTLY idx_interactions_type 
ON contact_interactions(contact_id, interaction_type);

-- Table statistics update
ANALYZE customer_contacts;
ANALYZE contact_interactions;

-- Query plan optimization hints
ALTER TABLE customer_contacts SET (fillfactor = 90);
ALTER TABLE contact_interactions SET (fillfactor = 85);
```

### 4. Performance Monitor

**Datei:** `frontend/src/utils/performance/PerformanceMonitor.ts`

```typescript
// CLAUDE: Performance Monitoring und Reporting
// Pfad: frontend/src/utils/performance/PerformanceMonitor.ts

export class PerformanceMonitor {
  private static instance: PerformanceMonitor;
  private metrics: Map<string, PerformanceMetric[]> = new Map();
  private observers: Map<string, PerformanceObserver> = new Map();
  
  static getInstance(): PerformanceMonitor {
    if (!PerformanceMonitor.instance) {
      PerformanceMonitor.instance = new PerformanceMonitor();
    }
    return PerformanceMonitor.instance;
  }
  
  /**
   * Track component render time
   */
  trackRender(componentName: string, duration: number) {
    this.addMetric('render', {
      component: componentName,
      duration,
      timestamp: Date.now()
    });
    
    // Alert if slow render
    if (duration > 100) {
      console.warn(`Slow render detected: ${componentName} took ${duration}ms`);
      this.reportToAnalytics('slow_render', {
        component: componentName,
        duration
      });
    }
  }
  
  /**
   * Track API call performance
   */
  trackApiCall(endpoint: string, duration: number, size?: number) {
    this.addMetric('api', {
      endpoint,
      duration,
      size,
      timestamp: Date.now()
    });
    
    // Calculate throughput
    if (size) {
      const throughput = size / (duration / 1000); // bytes per second
      this.addMetric('throughput', {
        endpoint,
        throughput,
        timestamp: Date.now()
      });
    }
  }
  
  /**
   * Track memory usage
   */
  trackMemory() {
    if ('memory' in performance) {
      const memory = (performance as any).memory;
      this.addMetric('memory', {
        used: memory.usedJSHeapSize,
        total: memory.totalJSHeapSize,
        limit: memory.jsHeapSizeLimit,
        timestamp: Date.now()
      });
      
      // Alert if memory usage is high
      const usagePercent = (memory.usedJSHeapSize / memory.jsHeapSizeLimit) * 100;
      if (usagePercent > 80) {
        console.warn(`High memory usage: ${usagePercent.toFixed(2)}%`);
      }
    }
  }
  
  /**
   * Web Vitals tracking
   */
  trackWebVitals() {
    // Largest Contentful Paint (LCP)
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const lastEntry = entries[entries.length - 1];
      this.addMetric('lcp', {
        value: lastEntry.renderTime || lastEntry.loadTime,
        timestamp: Date.now()
      });
    }).observe({ entryTypes: ['largest-contentful-paint'] });
    
    // First Input Delay (FID)
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      entries.forEach((entry: any) => {
        this.addMetric('fid', {
          value: entry.processingStart - entry.startTime,
          timestamp: Date.now()
        });
      });
    }).observe({ entryTypes: ['first-input'] });
    
    // Cumulative Layout Shift (CLS)
    let clsValue = 0;
    new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        if (!(entry as any).hadRecentInput) {
          clsValue += (entry as any).value;
        }
      }
      this.addMetric('cls', {
        value: clsValue,
        timestamp: Date.now()
      });
    }).observe({ entryTypes: ['layout-shift'] });
  }
  
  /**
   * Get performance report
   */
  getReport(): PerformanceReport {
    const report: PerformanceReport = {
      timestamp: Date.now(),
      metrics: {}
    };
    
    this.metrics.forEach((metrics, type) => {
      const recent = metrics.slice(-100); // Last 100 entries
      
      report.metrics[type] = {
        count: recent.length,
        average: this.calculateAverage(recent),
        p50: this.calculatePercentile(recent, 50),
        p95: this.calculatePercentile(recent, 95),
        p99: this.calculatePercentile(recent, 99),
        min: Math.min(...recent.map(m => m.value || m.duration || 0)),
        max: Math.max(...recent.map(m => m.value || m.duration || 0))
      };
    });
    
    return report;
  }
  
  /**
   * Export metrics for analysis
   */
  exportMetrics(): string {
    const data = {
      timestamp: Date.now(),
      userAgent: navigator.userAgent,
      metrics: Object.fromEntries(this.metrics)
    };
    
    return JSON.stringify(data, null, 2);
  }
  
  private addMetric(type: string, metric: any) {
    if (!this.metrics.has(type)) {
      this.metrics.set(type, []);
    }
    
    const metrics = this.metrics.get(type)!;
    metrics.push(metric);
    
    // Keep only last 1000 entries
    if (metrics.length > 1000) {
      metrics.shift();
    }
  }
  
  private calculateAverage(metrics: any[]): number {
    const values = metrics.map(m => m.value || m.duration || 0);
    return values.reduce((a, b) => a + b, 0) / values.length;
  }
  
  private calculatePercentile(metrics: any[], percentile: number): number {
    const values = metrics
      .map(m => m.value || m.duration || 0)
      .sort((a, b) => a - b);
    
    const index = Math.ceil((percentile / 100) * values.length) - 1;
    return values[index] || 0;
  }
  
  private reportToAnalytics(event: string, data: any) {
    // Send to analytics service
    if (window.gtag) {
      window.gtag('event', event, data);
    }
  }
}

// React Hook for performance tracking
export const usePerformanceTracking = (componentName: string) => {
  const startTime = useRef(performance.now());
  const monitor = PerformanceMonitor.getInstance();
  
  useEffect(() => {
    const renderTime = performance.now() - startTime.current;
    monitor.trackRender(componentName, renderTime);
  });
  
  return {
    trackEvent: (event: string, duration: number) => {
      monitor.trackApiCall(event, duration);
    }
  };
};
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Backend Optimization (45 Min)
- [ ] Query Optimizer implementieren
- [ ] N+1 Query Prevention
- [ ] Database Indexes erstellen
- [ ] Caching Layer (Redis/Memory)

### Phase 2: Frontend Virtual Scrolling (45 Min)
- [ ] VirtualContactList Component
- [ ] Infinite Loading
- [ ] Search Debouncing
- [ ] Prefetching Logic

### Phase 3: Caching Strategy (30 Min)
- [ ] Multi-tier Cache (Memory/Redis)
- [ ] Cache Warming
- [ ] Cache Invalidation
- [ ] Session Storage

### Phase 4: Bundle Optimization (30 Min)
- [ ] Code Splitting
- [ ] Lazy Loading Components
- [ ] Tree Shaking
- [ ] Asset Optimization

### Phase 5: Monitoring (30 Min)
- [ ] Performance Monitor
- [ ] Web Vitals Tracking
- [ ] Custom Metrics
- [ ] Alert System

## 🔗 INTEGRATION POINTS

1. **Contact List** - Virtual Scrolling einbauen
2. **Search** - Debouncing & Caching
3. **Filters** - Optimierte Queries
4. **Dashboard** - Performance Metrics

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Zu viele DOM Nodes**
   → Lösung: Virtual Scrolling ab 100+ Items

2. **Blocking API Calls**
   → Lösung: Parallel Loading & Prefetching

3. **Memory Leaks**
   → Lösung: Proper Cleanup & Monitoring

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 180 Minuten  
**Nächstes Dokument:** [→ Accessibility & Monitoring](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/ACCESSIBILITY_MONITORING.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Performance = Happy Users! ⚡✨**