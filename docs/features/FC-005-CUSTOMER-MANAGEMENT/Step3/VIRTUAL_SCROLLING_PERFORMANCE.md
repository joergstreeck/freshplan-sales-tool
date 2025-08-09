# ⚡ Virtual Scrolling & Performance Optimizations

**Feature:** FC-005 PR4 - Phase 3  
**Status:** 📋 BEREIT ZUR IMPLEMENTIERUNG  
**Geschätzter Aufwand:** 4-6 Stunden  
**Priorität:** 🥉 HOCH - Performance bei großen Datenmengen  

## 🧭 NAVIGATION

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MINI_AUDIT_TIMELINE.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PR4_ENHANCED_FEATURES_COMPLETE.md`  
**→ Nächstes:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/BACKEND_EXPORT_ENDPOINTS.md`  
**→ Export Frontend:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_EXPORT_FEATURES.md`  

## 🎯 ZIELE

Optimiere die Performance für große Datenmengen durch Virtual Scrolling, Lazy Loading und intelligentes Caching. Stelle sicher, dass die Anwendung auch bei 10.000+ Einträgen flüssig läuft.

## 📊 PERFORMANCE-METRIKEN

### Target KPIs
- **Initial Load:** < 200ms für erste 20 Items
- **Scroll Performance:** 60 FPS bei 10.000+ Items
- **Memory Usage:** < 100MB für 10.000 Items
- **Time to Interactive:** < 1s
- **Bundle Size Impact:** < 50KB zusätzlich

## 🏗️ IMPLEMENTIERUNG

### 1. VirtualizedCustomerList Component

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/VirtualizedCustomerList.tsx`

```typescript
import React, { useCallback, useRef, useState, useMemo, useEffect } from 'react';
import { FixedSizeList as List, ListChildComponentProps } from 'react-window';
import InfiniteLoader from 'react-window-infinite-loader';
import AutoSizer from 'react-virtualized-auto-sizer';
import { useInView } from 'react-intersection-observer';
import {
  Box,
  Paper,
  Typography,
  CircularProgress,
  Alert,
  Skeleton,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import { SmartContactCard } from './contacts/SmartContactCard';
import { ContactGridContainer } from './contacts/ContactGridContainer';
import type { Customer } from '../types/customer.types';
import type { Contact } from '../types/contact.types';

interface VirtualizedCustomerListProps {
  customers: Customer[];
  contacts: Map<string, Contact[]>; // customerId -> contacts
  loading?: boolean;
  hasMore?: boolean;
  onLoadMore?: () => Promise<void>;
  itemHeight?: number;
  gridView?: boolean;
  pageSize?: number;
  overscan?: number;
  showAuditTrail?: boolean;
}

export function VirtualizedCustomerList({
  customers,
  contacts,
  loading = false,
  hasMore = false,
  onLoadMore,
  itemHeight = 280, // Height for card view
  gridView = true,
  pageSize = 50,
  overscan = 5,
  showAuditTrail = true,
}: VirtualizedCustomerListProps) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isTablet = useMediaQuery(theme.breakpoints.down('md'));
  
  // Calculate grid columns based on screen size
  const getColumns = useCallback(() => {
    if (isMobile) return 1;
    if (isTablet) return 2;
    return gridView ? 3 : 1;
  }, [isMobile, isTablet, gridView]);
  
  const columns = getColumns();
  const rowCount = Math.ceil(customers.length / columns);
  
  // Infinite scroll state
  const [isNextPageLoading, setIsNextPageLoading] = useState(false);
  const infiniteLoaderRef = useRef<InfiniteLoader>(null);
  
  // Memoized item data for performance
  const itemData = useMemo(() => ({
    customers,
    contacts,
    columns,
    showAuditTrail,
  }), [customers, contacts, columns, showAuditTrail]);
  
  // Check if more items need to be loaded
  const loadMoreItems = useCallback(async (startIndex: number, stopIndex: number) => {
    if (isNextPageLoading || !hasMore || !onLoadMore) return;
    
    setIsNextPageLoading(true);
    try {
      await onLoadMore();
    } finally {
      setIsNextPageLoading(false);
    }
  }, [isNextPageLoading, hasMore, onLoadMore]);
  
  // Check if an item is loaded
  const isItemLoaded = useCallback((index: number) => {
    const itemIndex = index * columns;
    return itemIndex < customers.length;
  }, [customers.length, columns]);
  
  // Grid Row Renderer
  const GridRow = React.memo(({ index, style, data }: ListChildComponentProps) => {
    const { customers, contacts, columns, showAuditTrail } = data;
    const startIndex = index * columns;
    
    return (
      <Box style={style} sx={{ p: 2 }}>
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: `repeat(${columns}, 1fr)`,
            gap: 2,
            height: '100%',
          }}
        >
          {Array.from({ length: columns }).map((_, colIndex) => {
            const customerIndex = startIndex + colIndex;
            const customer = customers[customerIndex];
            
            if (!customer) return <Box key={colIndex} />;
            
            const customerContacts = contacts.get(customer.id) || [];
            
            return (
              <CustomerCardMemo
                key={customer.id}
                customer={customer}
                contacts={customerContacts}
                showAuditTrail={showAuditTrail}
              />
            );
          })}
        </Box>
      </Box>
    );
  });
  
  GridRow.displayName = 'GridRow';
  
  // Memoized Customer Card
  const CustomerCardMemo = React.memo(({
    customer,
    contacts,
    showAuditTrail,
  }: {
    customer: Customer;
    contacts: Contact[];
    showAuditTrail: boolean;
  }) => {
    const [ref, inView] = useInView({
      triggerOnce: true,
      rootMargin: '100px',
    });
    
    // Only render full card when in view
    if (!inView) {
      return (
        <Box ref={ref} sx={{ height: itemHeight }}>
          <Skeleton variant="rectangular" height={itemHeight - 16} />
        </Box>
      );
    }
    
    return (
      <Box ref={ref}>
        <Paper
          elevation={2}
          sx={{
            height: itemHeight - 16,
            overflow: 'hidden',
            transition: 'all 0.3s ease',
            '&:hover': {
              transform: 'translateY(-2px)',
              boxShadow: theme.shadows[4],
            },
          }}
        >
          {/* Customer Header */}
          <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
            <Typography variant="h6" noWrap>
              {customer.companyName}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {customer.customerNumber}
            </Typography>
          </Box>
          
          {/* Contact Cards Grid */}
          <Box sx={{ p: 1, maxHeight: itemHeight - 100, overflowY: 'auto' }}>
            {contacts.length > 0 ? (
              <Box sx={{ display: 'grid', gap: 1 }}>
                {contacts.slice(0, 2).map((contact) => (
                  <SmartContactCard
                    key={contact.id}
                    contact={contact}
                    customerId={customer.id}
                    showAuditTrail={showAuditTrail}
                  />
                ))}
                {contacts.length > 2 && (
                  <Typography variant="caption" color="text.secondary" sx={{ p: 1 }}>
                    +{contacts.length - 2} weitere Kontakte
                  </Typography>
                )}
              </Box>
            ) : (
              <Typography variant="body2" color="text.secondary" sx={{ p: 2 }}>
                Keine Kontakte vorhanden
              </Typography>
            )}
          </Box>
        </Paper>
      </Box>
    );
  }, (prevProps, nextProps) => {
    // Custom comparison for better performance
    return (
      prevProps.customer.id === nextProps.customer.id &&
      prevProps.customer.updatedAt === nextProps.customer.updatedAt &&
      prevProps.contacts.length === nextProps.contacts.length &&
      prevProps.showAuditTrail === nextProps.showAuditTrail
    );
  });
  
  CustomerCardMemo.displayName = 'CustomerCardMemo';
  
  // Loading indicator
  if (loading && customers.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }
  
  // Empty state
  if (!loading && customers.length === 0) {
    return (
      <Alert severity="info">
        Keine Kunden gefunden. Versuchen Sie andere Filterkriterien.
      </Alert>
    );
  }
  
  return (
    <Box sx={{ height: '100%', width: '100%' }}>
      <AutoSizer>
        {({ height, width }) => (
          <InfiniteLoader
            ref={infiniteLoaderRef}
            isItemLoaded={isItemLoaded}
            itemCount={hasMore ? rowCount + 1 : rowCount}
            loadMoreItems={loadMoreItems}
          >
            {({ onItemsRendered, ref }) => (
              <List
                ref={ref}
                height={height}
                itemCount={rowCount}
                itemSize={itemHeight}
                width={width}
                overscanCount={overscan}
                onItemsRendered={onItemsRendered}
                itemData={itemData}
              >
                {GridRow}
              </List>
            )}
          </InfiniteLoader>
        )}
      </AutoSizer>
      
      {/* Loading more indicator */}
      {isNextPageLoading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
          <CircularProgress size={24} />
        </Box>
      )}
    </Box>
  );
}
```

### 2. Performance Hook für Data Management

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/hooks/useVirtualizedData.ts`

```typescript
import { useState, useCallback, useRef, useEffect, useMemo } from 'react';
import { useQuery, useInfiniteQuery } from '@tanstack/react-query';
import { useDebounce } from './useDebounce';
import { customerApi } from '../services/customerApi';
import type { FilterConfig, SortConfig } from '../types/filter.types';

interface UseVirtualizedDataOptions {
  filters?: FilterConfig;
  sort?: SortConfig[];
  pageSize?: number;
  enabled?: boolean;
}

export function useVirtualizedData({
  filters = {},
  sort = [],
  pageSize = 50,
  enabled = true,
}: UseVirtualizedDataOptions) {
  // Debounce filters for performance
  const debouncedFilters = useDebounce(filters, 300);
  
  // Use infinite query for pagination
  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    error,
    refetch,
  } = useInfiniteQuery({
    queryKey: ['customers', 'virtual', debouncedFilters, sort],
    queryFn: ({ pageParam = 0 }) => 
      customerApi.getFilteredCustomers({
        filters: debouncedFilters,
        sort,
        page: pageParam,
        size: pageSize,
      }),
    getNextPageParam: (lastPage, pages) => {
      if (lastPage.length < pageSize) return undefined;
      return pages.length;
    },
    enabled,
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes cache
  });
  
  // Flatten pages into single array
  const allCustomers = useMemo(() => {
    return data?.pages.flatMap(page => page) || [];
  }, [data]);
  
  // Group contacts by customer
  const contactsByCustomer = useMemo(() => {
    const map = new Map();
    allCustomers.forEach(customer => {
      map.set(customer.id, customer.contacts || []);
    });
    return map;
  }, [allCustomers]);
  
  // Prefetch next page
  useEffect(() => {
    if (hasNextPage && !isFetchingNextPage) {
      const timeoutId = setTimeout(() => {
        fetchNextPage();
      }, 1000);
      return () => clearTimeout(timeoutId);
    }
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);
  
  return {
    customers: allCustomers,
    contactsByCustomer,
    hasMore: hasNextPage,
    loading: isLoading,
    loadingMore: isFetchingNextPage,
    error,
    refetch,
    fetchNextPage,
  };
}
```

### 3. Intersection Observer für Lazy Loading

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/hooks/useLazyLoad.ts`

```typescript
import { useRef, useCallback, useEffect } from 'react';
import { useInView } from 'react-intersection-observer';

interface UseLazyLoadOptions {
  onIntersect: () => void;
  threshold?: number;
  rootMargin?: string;
  enabled?: boolean;
}

export function useLazyLoad({
  onIntersect,
  threshold = 0.1,
  rootMargin = '100px',
  enabled = true,
}: UseLazyLoadOptions) {
  const [ref, inView] = useInView({
    threshold,
    rootMargin,
    triggerOnce: false,
  });
  
  useEffect(() => {
    if (inView && enabled) {
      onIntersect();
    }
  }, [inView, enabled, onIntersect]);
  
  return { ref, inView };
}

// Usage for Image Lazy Loading
export function LazyImage({ src, alt, ...props }: React.ImgHTMLAttributes<HTMLImageElement>) {
  const [imageSrc, setImageSrc] = useState<string | undefined>(undefined);
  const { ref, inView } = useInView({
    triggerOnce: true,
    rootMargin: '50px',
  });
  
  useEffect(() => {
    if (inView && src) {
      // Preload image
      const img = new Image();
      img.src = src;
      img.onload = () => setImageSrc(src);
    }
  }, [inView, src]);
  
  return (
    <div ref={ref}>
      {imageSrc ? (
        <img src={imageSrc} alt={alt} {...props} />
      ) : (
        <Skeleton variant="rectangular" width={props.width} height={props.height} />
      )}
    </div>
  );
}
```

## 🎨 OPTIMIZATION TECHNIQUES

### 1. React.memo & useMemo
```typescript
// Memoize expensive computations
const filteredData = useMemo(
  () => applyComplexFilters(data, filters),
  [data, filters]
);

// Memoize components
const MemoizedCard = React.memo(ContactCard, (prev, next) => {
  return prev.contact.id === next.contact.id &&
         prev.contact.updatedAt === next.contact.updatedAt;
});
```

### 2. Code Splitting
```typescript
// Lazy load heavy components
const HeavyComponent = lazy(() => import('./HeavyComponent'));

// Route-based splitting
const CustomerDetailPage = lazy(() => 
  import(/* webpackChunkName: "customer-detail" */ './CustomerDetailPage')
);
```

### 3. Debouncing & Throttling
```typescript
// Debounce search input
const debouncedSearch = useDebounce(searchTerm, 300);

// Throttle scroll events
const throttledScroll = useThrottle(handleScroll, 100);
```

### 4. Web Workers für Heavy Computing
```typescript
// worker.ts
self.addEventListener('message', (e) => {
  const { data, filter } = e.data;
  const filtered = performHeavyFiltering(data, filter);
  self.postMessage(filtered);
});

// Component
const worker = new Worker('/filter.worker.js');
worker.postMessage({ data, filter });
worker.onmessage = (e) => setFilteredData(e.data);
```

## 📊 PERFORMANCE MONITORING

### 1. Performance Observer
```typescript
useEffect(() => {
  const observer = new PerformanceObserver((list) => {
    for (const entry of list.getEntries()) {
      if (entry.entryType === 'measure') {
        console.log(`${entry.name}: ${entry.duration}ms`);
        // Send to analytics
        analytics.track('performance', {
          metric: entry.name,
          duration: entry.duration,
        });
      }
    }
  });
  
  observer.observe({ entryTypes: ['measure'] });
  
  return () => observer.disconnect();
}, []);
```

### 2. React DevTools Profiler
```typescript
import { Profiler } from 'react';

function onRenderCallback(id, phase, actualDuration) {
  console.log(`${id} (${phase}) took ${actualDuration}ms`);
}

<Profiler id="CustomerList" onRender={onRenderCallback}>
  <VirtualizedCustomerList {...props} />
</Profiler>
```

## 🧪 PERFORMANCE TESTING

### Load Testing
```typescript
describe('VirtualizedCustomerList Performance', () => {
  it('should render 10,000 items in < 1s', async () => {
    const start = performance.now();
    const { container } = render(
      <VirtualizedCustomerList customers={generate10kCustomers()} />
    );
    const end = performance.now();
    expect(end - start).toBeLessThan(1000);
  });
  
  it('should maintain 60fps while scrolling', async () => {
    // Use puppeteer for real browser testing
    const metrics = await page.metrics();
    expect(metrics.JSHeapUsedSize).toBeLessThan(100 * 1024 * 1024); // < 100MB
  });
});
```

## 📈 OPTIMIZATION CHECKLIST

### Before Optimization
- [ ] Measure current performance baseline
- [ ] Identify bottlenecks with Profiler
- [ ] Set performance budget

### During Implementation
- [ ] Virtual scrolling for lists > 20 items
- [ ] Lazy loading for images and components
- [ ] Memoization for expensive calculations
- [ ] Debouncing for user input
- [ ] Code splitting for routes

### After Implementation
- [ ] Measure improvements
- [ ] Run Lighthouse audit
- [ ] Test on low-end devices
- [ ] Monitor real user metrics

## 🔗 VERWANDTE DOKUMENTE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MINI_AUDIT_TIMELINE.md`  
**→ Nächstes:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/BACKEND_EXPORT_ENDPOINTS.md`  
**→ Integration:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SALESCOCKPIT_V2_INTEGRATION.md`  

---

**Status:** ✅ BEREIT ZUR IMPLEMENTIERUNG  
**Nächster Schritt:** react-window Dependencies installieren und VirtualizedCustomerList implementieren