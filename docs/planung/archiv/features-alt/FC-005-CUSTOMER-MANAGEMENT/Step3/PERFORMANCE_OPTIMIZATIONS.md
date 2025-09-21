# ⚡ Performance Optimizations für Audit Timeline

**Feature:** FC-005 Step3 - Contact Management UI  
**Status:** 📋 GEPLANT für PR 4  
**Priorität:** 🥉 Priorität 3  
**Geschätzter Aufwand:** 6-8 Stunden  

## 🧭 Navigation

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARD_AUDIT_INTEGRATION.md`  
**← Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**→ Nächstes:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_EXPORT_FEATURES.md`  
**→ FAB Integration:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FAB_DRAWER_INTEGRATION.md`  

## 🎯 Ziele

Optimiere die Audit Timeline für große Datenmengen und stelle sicher, dass die Performance auch bei tausenden von Einträgen flüssig bleibt.

## 📊 Performance-Metriken

### Ziel-KPIs
- **Initial Load:** < 200ms für erste 20 Einträge
- **Scroll Performance:** 60 FPS bei 1000+ Einträgen
- **Memory Usage:** < 50MB für 10.000 Einträge
- **Network:** Max 1 Request pro Scroll-Event

## 🏗️ Implementierungs-Strategie

### 1. Virtual Scrolling mit React Window

**Installation:**
```bash
npm install react-window react-window-infinite-loader
```

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/components/VirtualizedAuditTimeline.tsx`

```typescript
import { useCallback, useRef, useState } from 'react';
import { FixedSizeList as List } from 'react-window';
import InfiniteLoader from 'react-window-infinite-loader';
import AutoSizer from 'react-virtualized-auto-sizer';
import {
  Box,
  Paper,
  Typography,
  CircularProgress,
  Alert
} from '@mui/material';
import { AuditTimelineItem } from './AuditTimelineItem';
import { useVirtualAuditData } from '../hooks/useVirtualAuditData';
import type { AuditEntry } from '../types/audit.types';

interface VirtualizedAuditTimelineProps {
  entityType: string;
  entityId: string;
  itemHeight?: number;
  threshold?: number;
  pageSize?: number;
}

export function VirtualizedAuditTimeline({
  entityType,
  entityId,
  itemHeight = 120,
  threshold = 15,
  pageSize = 50
}: VirtualizedAuditTimelineProps) {
  const infiniteLoaderRef = useRef<InfiniteLoader>(null);
  const hasMountedRef = useRef(false);
  
  // Custom Hook für virtualisierte Daten
  const {
    items,
    hasNextPage,
    isNextPageLoading,
    loadNextPage,
    totalCount,
    error,
    refresh
  } = useVirtualAuditData({
    entityType,
    entityId,
    pageSize
  });
  
  // Prüfe ob mehr Items geladen werden müssen
  const loadMoreItems = useCallback(
    (startIndex: number, stopIndex: number) => {
      if (!isNextPageLoading && hasNextPage) {
        return loadNextPage(startIndex, stopIndex);
      }
      return Promise.resolve();
    },
    [isNextPageLoading, hasNextPage, loadNextPage]
  );
  
  // Prüfe ob ein Item geladen ist
  const isItemLoaded = useCallback(
    (index: number) => {
      return !hasNextPage || index < items.length;
    },
    [hasNextPage, items.length]
  );
  
  // Item Count für InfiniteLoader
  const itemCount = hasNextPage ? items.length + 1 : items.length;
  
  // Row Renderer
  const Row = useCallback(
    ({ index, style }: { index: number; style: React.CSSProperties }) => {
      if (!isItemLoaded(index)) {
        return (
          <div style={style}>
            <Box sx={{ 
              display: 'flex', 
              justifyContent: 'center', 
              alignItems: 'center',
              height: '100%'
            }}>
              <CircularProgress size={24} />
            </Box>
          </div>
        );
      }
      
      const item = items[index];
      return (
        <div style={style}>
          <AuditTimelineItem
            entry={item}
            isFirst={index === 0}
            isLast={index === items.length - 1}
            compact={false}
          />
        </div>
      );
    },
    [items, isItemLoaded]
  );
  
  // Effect für initiales Laden
  useEffect(() => {
    if (!hasMountedRef.current) {
      hasMountedRef.current = true;
      loadMoreItems(0, pageSize - 1);
    }
  }, []);
  
  if (error) {
    return (
      <Alert severity="error" action={
        <Button onClick={refresh}>Erneut versuchen</Button>
      }>
        Fehler beim Laden der Audit-Historie: {error}
      </Alert>
    );
  }
  
  return (
    <Paper sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header mit Anzahl */}
      <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
        <Typography variant="h6">
          Änderungshistorie
          {totalCount > 0 && (
            <Typography component="span" variant="body2" color="text.secondary" sx={{ ml: 1 }}>
              ({totalCount} Einträge)
            </Typography>
          )}
        </Typography>
      </Box>
      
      {/* Virtualisierte Liste */}
      <Box sx={{ flex: 1, minHeight: 400 }}>
        <AutoSizer>
          {({ height, width }) => (
            <InfiniteLoader
              ref={infiniteLoaderRef}
              isItemLoaded={isItemLoaded}
              itemCount={itemCount}
              loadMoreItems={loadMoreItems}
              threshold={threshold}
            >
              {({ onItemsRendered, ref }) => (
                <List
                  ref={ref}
                  height={height}
                  width={width}
                  itemCount={itemCount}
                  itemSize={itemHeight}
                  onItemsRendered={onItemsRendered}
                  overscanCount={5}
                >
                  {Row}
                </List>
              )}
            </InfiniteLoader>
          )}
        </AutoSizer>
      </Box>
      
      {/* Loading Indicator */}
      {isNextPageLoading && (
        <Box sx={{ p: 2, textAlign: 'center', borderTop: 1, borderColor: 'divider' }}>
          <CircularProgress size={20} sx={{ mr: 1 }} />
          <Typography variant="caption" color="text.secondary">
            Lade weitere Einträge...
          </Typography>
        </Box>
      )}
    </Paper>
  );
}
```

### 2. Custom Hook für Daten-Management

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/hooks/useVirtualAuditData.ts`

```typescript
import { useState, useCallback, useRef, useEffect } from 'react';
import { auditApi } from '../api/auditApi';
import type { AuditEntry } from '../types/audit.types';

interface UseVirtualAuditDataProps {
  entityType: string;
  entityId: string;
  pageSize?: number;
  cacheTime?: number;
}

interface UseVirtualAuditDataReturn {
  items: AuditEntry[];
  hasNextPage: boolean;
  isNextPageLoading: boolean;
  loadNextPage: (startIndex: number, stopIndex: number) => Promise<void>;
  totalCount: number;
  error: string | null;
  refresh: () => void;
}

// Cache für geladene Seiten
const pageCache = new Map<string, {
  data: AuditEntry[];
  timestamp: number;
}>();

export function useVirtualAuditData({
  entityType,
  entityId,
  pageSize = 50,
  cacheTime = 5 * 60 * 1000 // 5 Minuten
}: UseVirtualAuditDataProps): UseVirtualAuditDataReturn {
  const [items, setItems] = useState<AuditEntry[]>([]);
  const [hasNextPage, setHasNextPage] = useState(true);
  const [isNextPageLoading, setIsNextPageLoading] = useState(false);
  const [totalCount, setTotalCount] = useState(0);
  const [error, setError] = useState<string | null>(null);
  
  const loadedPages = useRef(new Set<number>());
  const cacheKey = `${entityType}-${entityId}`;
  
  // Lade Seite aus Cache oder API
  const loadPage = useCallback(async (pageNumber: number) => {
    const pageCacheKey = `${cacheKey}-${pageNumber}`;
    const cached = pageCache.get(pageCacheKey);
    
    // Prüfe Cache
    if (cached && Date.now() - cached.timestamp < cacheTime) {
      return {
        data: cached.data,
        totalElements: totalCount || cached.data.length * 10 // Schätzung
      };
    }
    
    // Lade von API
    try {
      const response = await auditApi.getEntityAudit(
        entityType,
        entityId,
        {
          page: pageNumber,
          size: pageSize
        }
      );
      
      // Cache speichern
      pageCache.set(pageCacheKey, {
        data: response.data,
        timestamp: Date.now()
      });
      
      return response;
    } catch (err) {
      console.error('Failed to load audit page:', err);
      throw err;
    }
  }, [entityType, entityId, pageSize, cacheTime, cacheKey, totalCount]);
  
  // Lade nächste Seite basierend auf Index
  const loadNextPage = useCallback(async (startIndex: number, stopIndex: number) => {
    if (isNextPageLoading) return;
    
    const startPage = Math.floor(startIndex / pageSize);
    const endPage = Math.floor(stopIndex / pageSize);
    
    setIsNextPageLoading(true);
    setError(null);
    
    try {
      const pagesToLoad = [];
      for (let page = startPage; page <= endPage; page++) {
        if (!loadedPages.current.has(page)) {
          pagesToLoad.push(page);
        }
      }
      
      if (pagesToLoad.length === 0) {
        setIsNextPageLoading(false);
        return;
      }
      
      // Lade alle benötigten Seiten parallel
      const results = await Promise.all(
        pagesToLoad.map(page => loadPage(page))
      );
      
      // Merge Ergebnisse
      const newItems = [...items];
      results.forEach((result, index) => {
        const pageNumber = pagesToLoad[index];
        const startIdx = pageNumber * pageSize;
        
        result.data.forEach((item, idx) => {
          newItems[startIdx + idx] = item;
        });
        
        loadedPages.current.add(pageNumber);
        
        // Update Total Count
        if (result.totalElements) {
          setTotalCount(result.totalElements);
        }
        
        // Prüfe ob es mehr Seiten gibt
        if (result.data.length < pageSize) {
          setHasNextPage(false);
        }
      });
      
      setItems(newItems);
    } catch (err) {
      setError('Fehler beim Laden der Daten');
    } finally {
      setIsNextPageLoading(false);
    }
  }, [isNextPageLoading, pageSize, items, loadPage]);
  
  // Refresh Funktion
  const refresh = useCallback(() => {
    // Clear Cache
    Array.from(pageCache.keys())
      .filter(key => key.startsWith(cacheKey))
      .forEach(key => pageCache.delete(key));
    
    // Reset State
    setItems([]);
    setHasNextPage(true);
    setIsNextPageLoading(false);
    setTotalCount(0);
    setError(null);
    loadedPages.current.clear();
    
    // Reload first page
    loadNextPage(0, pageSize - 1);
  }, [cacheKey, pageSize, loadNextPage]);
  
  return {
    items,
    hasNextPage,
    isNextPageLoading,
    loadNextPage,
    totalCount,
    error,
    refresh
  };
}
```

### 3. Lazy Loading für Initial Load

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/components/LazyAuditTimeline.tsx`

```typescript
import { lazy, Suspense } from 'react';
import { Box, Skeleton } from '@mui/material';

// Lazy Load der Timeline Komponente
const VirtualizedAuditTimeline = lazy(() => 
  import('./VirtualizedAuditTimeline')
    .then(module => ({ default: module.VirtualizedAuditTimeline }))
);

interface LazyAuditTimelineProps {
  entityType: string;
  entityId: string;
  height?: string | number;
}

export function LazyAuditTimeline(props: LazyAuditTimelineProps) {
  return (
    <Suspense fallback={<AuditTimelineSkeleton height={props.height} />}>
      <VirtualizedAuditTimeline {...props} />
    </Suspense>
  );
}

function AuditTimelineSkeleton({ height = 400 }: { height?: string | number }) {
  return (
    <Box sx={{ height, p: 2 }}>
      {[...Array(5)].map((_, i) => (
        <Box key={i} sx={{ mb: 2 }}>
          <Skeleton variant="text" width="30%" height={20} />
          <Skeleton variant="rectangular" height={60} sx={{ mt: 1 }} />
        </Box>
      ))}
    </Box>
  );
}
```

### 4. Caching Strategy mit React Query

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/audit/api/auditQueries.ts`

```typescript
import { useQuery, useInfiniteQuery, useQueryClient } from '@tanstack/react-query';
import { auditApi } from './auditApi';

const AUDIT_QUERY_KEYS = {
  all: ['audit'] as const,
  entity: (type: string, id: string) => 
    [...AUDIT_QUERY_KEYS.all, 'entity', type, id] as const,
  entityPage: (type: string, id: string, page: number) => 
    [...AUDIT_QUERY_KEYS.entity(type, id), 'page', page] as const,
};

// Infinite Query für Virtual Scrolling
export function useInfiniteAuditEntries(
  entityType: string,
  entityId: string,
  pageSize = 50
) {
  return useInfiniteQuery({
    queryKey: AUDIT_QUERY_KEYS.entity(entityType, entityId),
    queryFn: ({ pageParam = 0 }) => 
      auditApi.getEntityAudit(entityType, entityId, {
        page: pageParam,
        size: pageSize
      }),
    getNextPageParam: (lastPage, pages) => {
      if (lastPage.data.length < pageSize) return undefined;
      return pages.length;
    },
    staleTime: 5 * 60 * 1000, // 5 Minuten
    cacheTime: 10 * 60 * 1000, // 10 Minuten
    refetchOnWindowFocus: false,
    refetchOnMount: false
  });
}

// Prefetch nächste Seite
export function usePrefetchNextAuditPage(
  entityType: string,
  entityId: string,
  currentPage: number,
  pageSize = 50
) {
  const queryClient = useQueryClient();
  
  useEffect(() => {
    const prefetchNextPage = async () => {
      await queryClient.prefetchQuery({
        queryKey: AUDIT_QUERY_KEYS.entityPage(entityType, entityId, currentPage + 1),
        queryFn: () => auditApi.getEntityAudit(entityType, entityId, {
          page: currentPage + 1,
          size: pageSize
        }),
        staleTime: 5 * 60 * 1000
      });
    };
    
    prefetchNextPage();
  }, [currentPage, entityType, entityId, pageSize, queryClient]);
}

// Cache Invalidation
export function useInvalidateAuditCache() {
  const queryClient = useQueryClient();
  
  return {
    invalidateEntity: (entityType: string, entityId: string) => {
      queryClient.invalidateQueries(
        AUDIT_QUERY_KEYS.entity(entityType, entityId)
      );
    },
    invalidateAll: () => {
      queryClient.invalidateQueries(AUDIT_QUERY_KEYS.all);
    }
  };
}
```

## 📊 Performance-Monitoring

### Metriken-Tracking
```typescript
// Performance Observer für Timeline
const observer = new PerformanceObserver((list) => {
  const entries = list.getEntries();
  entries.forEach(entry => {
    if (entry.name.includes('audit-timeline')) {
      // Track zu Analytics
      analytics.track('audit_timeline_performance', {
        duration: entry.duration,
        startTime: entry.startTime,
        entryCount: items.length
      });
    }
  });
});

observer.observe({ entryTypes: ['measure'] });
```

### Memory Monitoring
```typescript
// Cleanup alte Cache-Einträge
useEffect(() => {
  const cleanup = setInterval(() => {
    const now = Date.now();
    Array.from(pageCache.entries()).forEach(([key, value]) => {
      if (now - value.timestamp > cacheTime) {
        pageCache.delete(key);
      }
    });
  }, 60000); // Jede Minute
  
  return () => clearInterval(cleanup);
}, []);
```

## 🧪 Performance Tests

### Load Testing
```typescript
describe('VirtualizedAuditTimeline Performance', () => {
  it('should handle 10,000 entries smoothly', async () => {
    const startTime = performance.now();
    
    render(<VirtualizedAuditTimeline 
      entityType="customer"
      entityId="test-id"
    />);
    
    // Initial render < 200ms
    expect(performance.now() - startTime).toBeLessThan(200);
    
    // Scroll performance
    const scrollTime = await measureScrollPerformance();
    expect(scrollTime).toBeLessThan(16); // 60 FPS
  });
  
  it('should not exceed memory limit', () => {
    // Mock 10,000 entries
    const memory = performance.memory.usedJSHeapSize;
    // Load timeline
    // Check memory increase < 50MB
    expect(performance.memory.usedJSHeapSize - memory).toBeLessThan(50 * 1024 * 1024);
  });
});
```

## ✅ Optimierungs-Checkliste

- [ ] Virtual Scrolling implementiert
- [ ] Infinite Loading funktioniert
- [ ] Page Caching aktiv
- [ ] Memory Cleanup eingerichtet
- [ ] Lazy Loading für Initial Load
- [ ] React Query Integration
- [ ] Performance Monitoring
- [ ] Load Tests geschrieben
- [ ] Bundle Size optimiert

## 📈 Erwartete Verbesserungen

| Metrik | Vorher | Nachher | Verbesserung |
|--------|--------|---------|--------------|
| Initial Load | 800ms | 180ms | 77% schneller |
| 1000 Items Scroll | 45 FPS | 60 FPS | 33% flüssiger |
| Memory (10k Items) | 120MB | 45MB | 63% weniger |
| Network Requests | 200 | 20 | 90% weniger |

---

**Status:** Bereit für Implementierung in PR 4  
**Abhängigkeiten:** VirtualizedAuditTimeline Component