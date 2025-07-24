# FC-014: Mobile Performance Optimierung

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-014-mobile-tablet-optimization.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-014-mobile-tablet-optimization.md)  
**Fokus:** Performance auf mobilen Geräten

## 📊 Performance Ziele

### Core Web Vitals Mobile
| Metrik | Ziel | Kritisch |
|--------|------|----------|
| LCP (Largest Contentful Paint) | < 2.5s | < 4s |
| FID (First Input Delay) | < 100ms | < 300ms |
| CLS (Cumulative Layout Shift) | < 0.1 | < 0.25 |
| TTI (Time to Interactive) | < 3.8s | < 7.3s |

## 🚀 Bundle Optimierung

### Code Splitting Strategy
```typescript
// Lazy Loading für Routes
const OpportunityPipeline = lazy(() => 
  import(/* webpackChunkName: "pipeline" */ './features/opportunity/OpportunityPipeline')
);

const Calculator = lazy(() => 
  import(/* webpackChunkName: "calculator" */ './features/calculator/Calculator')
);

const CustomerCockpit = lazy(() => 
  import(/* webpackChunkName: "cockpit" */ './features/customer/CustomerCockpit')
);

// Route-based Code Splitting
export const AppRoutes = () => {
  return (
    <Suspense fallback={<MobileLoadingScreen />}>
      <Routes>
        <Route path="/pipeline" element={<OpportunityPipeline />} />
        <Route path="/calculator" element={<Calculator />} />
        <Route path="/cockpit/:id" element={<CustomerCockpit />} />
      </Routes>
    </Suspense>
  );
};
```

### Bundle Size Targets
```javascript
// webpack.config.js
module.exports = {
  performance: {
    maxAssetSize: 244000,      // 244 KB
    maxEntrypointSize: 244000, // 244 KB
    hints: 'error'
  },
  optimization: {
    splitChunks: {
      chunks: 'all',
      cacheGroups: {
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendor',
          priority: 10
        },
        common: {
          minChunks: 2,
          priority: 5,
          reuseExistingChunk: true
        }
      }
    }
  }
};
```

## 📱 Mobile-spezifische Optimierungen

### 1. Virtual Scrolling für Listen
```typescript
// React Window für große Listen
import { FixedSizeList } from 'react-window';

const OpportunityListMobile: React.FC = () => {
  const rowRenderer = ({ index, style }) => (
    <div style={style}>
      <OpportunityCard {...opportunities[index]} />
    </div>
  );
  
  return (
    <FixedSizeList
      height={window.innerHeight - 120} // Minus Header/Footer
      itemCount={opportunities.length}
      itemSize={80} // Card Height
      width="100%"
      overscanCount={3} // Pre-render 3 items
    >
      {rowRenderer}
    </FixedSizeList>
  );
};
```

### 2. Image Optimization
```typescript
// Responsive Images mit lazy loading
const ResponsiveImage: React.FC<{src: string, alt: string}> = ({src, alt}) => {
  return (
    <picture>
      <source 
        media="(max-width: 767px)" 
        srcSet={`${src}?w=400&fm=webp 1x, ${src}?w=800&fm=webp 2x`}
      />
      <source 
        media="(min-width: 768px)" 
        srcSet={`${src}?w=800&fm=webp 1x, ${src}?w=1600&fm=webp 2x`}
      />
      <img 
        src={`${src}?w=400&fm=jpg`} 
        alt={alt}
        loading="lazy"
        decoding="async"
      />
    </picture>
  );
};
```

### 3. Touch Event Optimization
```typescript
// Passive Event Listeners
useEffect(() => {
  const options = { passive: true };
  
  const handleTouchStart = (e: TouchEvent) => {
    // Handle touch without blocking scroll
  };
  
  element.addEventListener('touchstart', handleTouchStart, options);
  element.addEventListener('touchmove', handleTouchMove, options);
  
  return () => {
    element.removeEventListener('touchstart', handleTouchStart);
    element.removeEventListener('touchmove', handleTouchMove);
  };
}, []);
```

## 🎨 CSS Performance

### Critical CSS
```typescript
// Inline critical CSS für schnelleres Rendering
const CriticalStyles = () => (
  <style dangerouslySetInnerHTML={{__html: `
    /* Above-the-fold styles */
    body { margin: 0; font-family: -apple-system, sans-serif; }
    .app-header { height: 56px; background: #94C456; }
    .loading { display: flex; justify-content: center; padding: 20px; }
    /* Weitere kritische Styles */
  `}} />
);
```

### CSS Containment
```scss
// Layout-Containment für Performance
.opportunity-card {
  contain: layout style paint;
  will-change: transform; // Nur bei Animationen
}

.stage-column {
  contain: layout size style;
  overflow: hidden;
}

// Avoid expensive selectors
// Schlecht
.pipeline * > div:nth-child(odd) .card {
  // Complex selector
}

// Gut
.opportunity-card-odd {
  // Direct class
}
```

## 📊 Data Fetching Optimization

### Pagination & Infinite Scroll
```typescript
const useInfiniteOpportunities = () => {
  return useInfiniteQuery({
    queryKey: ['opportunities'],
    queryFn: ({ pageParam = 0 }) => 
      api.getOpportunities({ page: pageParam, size: 20 }),
    getNextPageParam: (lastPage) => 
      lastPage.hasMore ? lastPage.page + 1 : undefined,
    staleTime: 5 * 60 * 1000, // 5 minutes
    cacheTime: 10 * 60 * 1000 // 10 minutes
  });
};
```

### Request Deduplication
```typescript
// SWR für automatische Deduplication
const { data, error } = useSWR(
  ['customer', customerId],
  () => api.getCustomer(customerId),
  {
    dedupingInterval: 2000,
    revalidateOnFocus: false,
    revalidateOnReconnect: true
  }
);
```

## ⚡ Rendering Performance

### Memoization Strategy
```typescript
// Heavy computations memoized
const OpportunityStats = React.memo(({ opportunities }) => {
  const stats = useMemo(() => 
    calculateComplexStats(opportunities), 
    [opportunities]
  );
  
  return <StatsDisplay {...stats} />;
}, (prevProps, nextProps) => {
  // Custom comparison
  return prevProps.opportunities.length === nextProps.opportunities.length;
});
```

### Debounced Updates
```typescript
// Debounce heavy operations
const DebouncedSearch = () => {
  const [query, setQuery] = useState('');
  const debouncedQuery = useDebounce(query, 300);
  
  const results = useQuery({
    queryKey: ['search', debouncedQuery],
    queryFn: () => api.search(debouncedQuery),
    enabled: debouncedQuery.length > 2
  });
  
  return (
    <SearchInput 
      value={query}
      onChange={(e) => setQuery(e.target.value)}
    />
  );
};
```

## 📱 Device-Specific Optimizations

### Low-End Device Detection
```typescript
const useDeviceCapabilities = () => {
  const [isLowEnd, setIsLowEnd] = useState(false);
  
  useEffect(() => {
    // Check device capabilities
    const checkDevice = async () => {
      const memory = (navigator as any).deviceMemory || 4;
      const connection = (navigator as any).connection;
      const cores = navigator.hardwareConcurrency || 4;
      
      setIsLowEnd(
        memory < 4 || 
        cores < 4 || 
        connection?.effectiveType === '2g' ||
        connection?.saveData === true
      );
    };
    
    checkDevice();
  }, []);
  
  return { isLowEnd };
};

// Adaptive rendering based on device
const AdaptiveComponent = () => {
  const { isLowEnd } = useDeviceCapabilities();
  
  if (isLowEnd) {
    return <SimplifiedComponent />; // Less animations, simpler UI
  }
  
  return <FullComponent />; // Full experience
};
```

## 🔧 Performance Monitoring

### Real User Monitoring
```typescript
// Performance Observer
const measurePerformance = () => {
  if ('PerformanceObserver' in window) {
    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        // Send to analytics
        analytics.track('performance', {
          name: entry.name,
          duration: entry.duration,
          type: entry.entryType
        });
      }
    });
    
    observer.observe({ entryTypes: ['navigation', 'resource', 'paint'] });
  }
};
```

### Custom Performance Marks
```typescript
// Measure critical user flows
const measureUserFlow = (flowName: string) => {
  performance.mark(`${flowName}-start`);
  
  return () => {
    performance.mark(`${flowName}-end`);
    performance.measure(
      flowName,
      `${flowName}-start`,
      `${flowName}-end`
    );
    
    const measure = performance.getEntriesByName(flowName)[0];
    console.log(`${flowName} took ${measure.duration}ms`);
  };
};

// Usage
const endMeasure = measureUserFlow('opportunity-create');
// ... user creates opportunity
endMeasure();
```

## 📋 Performance Checklist

- [ ] Bundle size < 200KB (gzipped)
- [ ] Code splitting implemented
- [ ] Images lazy loaded & optimized
- [ ] Virtual scrolling for long lists
- [ ] Debounced search/filter inputs
- [ ] Passive touch event listeners
- [ ] CSS containment used
- [ ] Service Worker caching
- [ ] Request deduplication
- [ ] Performance monitoring active