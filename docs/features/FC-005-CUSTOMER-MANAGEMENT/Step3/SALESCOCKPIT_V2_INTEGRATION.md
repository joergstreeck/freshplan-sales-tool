# 🎯 SalesCockpitV2 Integration - Finale Zusammenführung

**Feature:** FC-005 PR4 - Phase 5  
**Status:** 📋 BEREIT ZUR IMPLEMENTIERUNG  
**Geschätzter Aufwand:** 1-2 Stunden  
**Priorität:** 🏆 FINAL - Alles zusammenführen  

## 🧭 NAVIGATION

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/BACKEND_EXPORT_ENDPOINTS.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PR4_ENHANCED_FEATURES_COMPLETE.md`  
**→ Hauptübersicht:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**← Filter Bar:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/INTELLIGENT_FILTER_BAR.md`  

## 🎯 ZIEL

Integriere alle neuen Features (Intelligent Filter Bar, Virtual Scrolling, Mini Audit Timeline, Export) in SalesCockpitV2 und stelle sicher, dass alles nahtlos zusammenarbeitet.

## 📊 INTEGRATION OVERVIEW

```
SalesCockpitV2
├── Header Section
│   ├── Dashboard Stats (existing)
│   └── Export Toolbar (new)
├── Filter Section (new)
│   ├── IntelligentFilterBar
│   ├── Quick Filters
│   └── Saved Filter Sets
├── Main Content Area
│   ├── VirtualizedCustomerList (new)
│   │   ├── SmartContactCards (enhanced)
│   │   │   └── MiniAuditTimeline (new)
│   │   └── Lazy Loading
│   └── ResizablePanels (existing)
└── Footer Section
    └── Pagination Info
```

## 🏗️ IMPLEMENTIERUNG

### Enhanced SalesCockpitV2 Component

**Datei Update:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/cockpit/components/SalesCockpitV2.tsx`

```typescript
/**
 * Sales Cockpit V2 - Enhanced with PR4 Features
 * 
 * Integrates:
 * - Intelligent Filter Bar
 * - Virtual Scrolling for Performance
 * - Mini Audit Timeline in Cards
 * - Multi-format Export
 * 
 * @since PR4 - Enhanced Features
 */

import React, { useState, useCallback, useMemo } from 'react';
import {
  Box,
  Typography,
  Card,
  Skeleton,
  Paper,
  Stack,
  Divider,
  Alert,
  Button,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import { styled } from '@mui/material/styles';
import {
  Group as GroupIcon,
  TrendingUp as TrendingUpIcon,
  Task as TaskIcon,
  Error as ErrorIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';

// Import new PR4 components
import { IntelligentFilterBar } from '../../customers/components/filter/IntelligentFilterBar';
import { VirtualizedCustomerList } from '../../customers/components/VirtualizedCustomerList';
import { ExportToolbar } from '../../audit/components/ExportToolbar';
import { useVirtualizedData } from '../../customers/hooks/useVirtualizedData';

// Import existing components
import { MyDayColumnMUI } from './MyDayColumnMUI';
import { FocusListColumnMUI } from './FocusListColumnMUI';
import { ActionCenterColumnMUI } from './ActionCenterColumnMUI';
import { ResizablePanels } from './layout/ResizablePanels';
import { useDashboardData } from '../hooks/useSalesCockpit';
import { useAuth } from '../../../hooks/useAuth';

// Import types
import type { FilterConfig, SortConfig, ColumnConfig } from '../../customers/types/filter.types';

const StatsCard = styled(Card)(({ theme }) => ({
  padding: theme.spacing(1),
  display: 'flex',
  alignItems: 'center',
  gap: theme.spacing(1),
  minWidth: 120,
  transition: 'all 0.3s ease',
  '&:hover': {
    transform: 'translateY(-1px)',
    boxShadow: theme.shadows[3],
  },
}));

const ContentContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  height: 'calc(100vh - 180px)', // Account for header and filter
  overflow: 'hidden',
  gap: theme.spacing(2),
}));

export function SalesCockpitV2() {
  const theme = useTheme();
  const { user, userId } = useAuth();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isTablet = useMediaQuery(theme.breakpoints.down('md'));
  
  // View state
  const [viewMode, setViewMode] = useState<'cockpit' | 'customers'>('cockpit');
  const [selectedCustomerId, setSelectedCustomerId] = useState<string | undefined>(undefined);
  
  // Filter state
  const [filters, setFilters] = useState<FilterConfig>({});
  const [sortConfig, setSortConfig] = useState<SortConfig[]>([
    { field: 'companyName', direction: 'asc', priority: 0 },
  ]);
  const [columnConfig, setColumnConfig] = useState<ColumnConfig[]>([]);
  
  // Dashboard data for statistics
  const { data: dashboardData, isLoading: dashboardLoading, refetch } = useDashboardData(userId);
  
  // Virtualized customer data with filters
  const {
    customers,
    contactsByCustomer,
    hasMore,
    loading: customersLoading,
    loadingMore,
    fetchNextPage,
    refetch: refetchCustomers,
  } = useVirtualizedData({
    filters,
    sort: sortConfig,
    pageSize: 50,
    enabled: viewMode === 'customers',
  });
  
  // Export handlers
  const handleExport = useCallback(async (format: 'csv' | 'excel' | 'json' | 'pdf') => {
    try {
      // Call export API
      const response = await fetch(`/api/export/customers/${format}?${new URLSearchParams({
        ...filters,
      })}`);
      
      if (!response.ok) throw new Error('Export failed');
      
      // Download file
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `customers_${new Date().toISOString()}.${format}`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Export error:', error);
    }
  }, [filters]);
  
  // Calculate filtered count
  const filteredCount = customers.length;
  const totalCount = dashboardData?.statistics?.totalCustomers || 0;
  
  // Permission checks
  const canExport = user?.roles?.some(role => 
    ['admin', 'manager', 'auditor'].includes(role)
  );
  
  const canViewAudit = user?.roles?.some(role =>
    ['admin', 'manager', 'auditor'].includes(role)
  );
  
  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header with Statistics */}
      <Paper elevation={0} sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
        <Stack spacing={2}>
          {/* Title and Actions */}
          <Stack direction="row" justifyContent="space-between" alignItems="center">
            <Typography variant="h5" fontWeight="bold">
              Sales Cockpit {viewMode === 'customers' && '- Kundenübersicht'}
            </Typography>
            
            <Stack direction="row" spacing={1}>
              {/* View Toggle */}
              <Button
                variant={viewMode === 'cockpit' ? 'contained' : 'outlined'}
                onClick={() => setViewMode('cockpit')}
                size="small"
              >
                Cockpit
              </Button>
              <Button
                variant={viewMode === 'customers' ? 'contained' : 'outlined'}
                onClick={() => setViewMode('customers')}
                size="small"
              >
                Kunden
              </Button>
              
              {/* Refresh Button */}
              <Button
                startIcon={<RefreshIcon />}
                onClick={() => {
                  refetch();
                  refetchCustomers();
                }}
                size="small"
              >
                Aktualisieren
              </Button>
              
              {/* Export Toolbar */}
              {canExport && viewMode === 'customers' && (
                <ExportToolbar
                  onExport={handleExport}
                  disabled={customersLoading}
                  filteredCount={filteredCount}
                  totalCount={totalCount}
                />
              )}
            </Stack>
          </Stack>
          
          {/* Statistics Cards */}
          {!isMobile && (
            <Stack direction="row" spacing={2} sx={{ overflowX: 'auto' }}>
              <StatsCard>
                <GroupIcon color="primary" />
                <Box>
                  <Typography variant="h6">
                    {dashboardData?.statistics?.totalCustomers || 0}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Kunden
                  </Typography>
                </Box>
              </StatsCard>
              
              <StatsCard>
                <TrendingUpIcon color="success" />
                <Box>
                  <Typography variant="h6">
                    {dashboardData?.statistics?.activeCustomers || 0}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Aktiv
                  </Typography>
                </Box>
              </StatsCard>
              
              <StatsCard>
                <TaskIcon color="info" />
                <Box>
                  <Typography variant="h6">
                    {dashboardData?.statistics?.openTasks || 0}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Aufgaben
                  </Typography>
                </Box>
              </StatsCard>
              
              <StatsCard>
                <ErrorIcon color="warning" />
                <Box>
                  <Typography variant="h6">
                    {dashboardData?.statistics?.customersAtRisk || 0}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Risiko
                  </Typography>
                </Box>
              </StatsCard>
            </Stack>
          )}
        </Stack>
      </Paper>
      
      {/* Filter Bar - Only in Customer View */}
      {viewMode === 'customers' && (
        <Box sx={{ px: 2, pt: 2 }}>
          <IntelligentFilterBar
            onFilterChange={setFilters}
            onSortChange={setSortConfig}
            onColumnChange={setColumnConfig}
            onExport={canExport ? handleExport : undefined}
            totalCount={totalCount}
            filteredCount={filteredCount}
            loading={customersLoading}
          />
        </Box>
      )}
      
      {/* Main Content Area */}
      <ContentContainer sx={{ flex: 1, p: 2 }}>
        {viewMode === 'cockpit' ? (
          // Original Cockpit View with Resizable Panels
          <ResizablePanels
            leftPanel={
              <MyDayColumnMUI
                tasks={dashboardData?.todaysTasks || []}
                loading={dashboardLoading}
                onTaskComplete={(taskId) => console.log('Complete task:', taskId)}
              />
            }
            centerPanel={
              <FocusListColumnMUI
                customers={dashboardData?.riskCustomers || []}
                loading={dashboardLoading}
                onCustomerSelect={setSelectedCustomerId}
                selectedCustomerId={selectedCustomerId}
              />
            }
            rightPanel={
              <ActionCenterColumnMUI
                alerts={dashboardData?.alerts || []}
                loading={dashboardLoading}
                onAlertAction={(alertId) => console.log('Alert action:', alertId)}
              />
            }
            defaultSizes={[30, 40, 30]}
            minSizes={[200, 300, 200]}
          />
        ) : (
          // Enhanced Customer List View with Virtual Scrolling
          <Box sx={{ height: '100%' }}>
            {customersLoading && customers.length === 0 ? (
              // Loading skeleton
              <Stack spacing={2}>
                {[1, 2, 3].map((i) => (
                  <Skeleton key={i} variant="rectangular" height={200} />
                ))}
              </Stack>
            ) : customers.length === 0 ? (
              // Empty state
              <Alert severity="info">
                Keine Kunden gefunden. Versuchen Sie andere Filterkriterien.
              </Alert>
            ) : (
              // Virtualized Customer List with Contact Cards
              <VirtualizedCustomerList
                customers={customers}
                contacts={contactsByCustomer}
                loading={customersLoading}
                hasMore={hasMore}
                onLoadMore={fetchNextPage}
                gridView={!isMobile}
                showAuditTrail={canViewAudit}
                itemHeight={isMobile ? 320 : 280}
                pageSize={50}
                overscan={5}
              />
            )}
          </Box>
        )}
      </ContentContainer>
      
      {/* Footer with Info */}
      {viewMode === 'customers' && filteredCount > 0 && (
        <Paper elevation={0} sx={{ p: 1, borderTop: 1, borderColor: 'divider' }}>
          <Typography variant="caption" color="text.secondary" align="center">
            Zeige {filteredCount} von {totalCount} Kunden
            {loadingMore && ' - Lade weitere...'}
          </Typography>
        </Paper>
      )}
    </Box>
  );
}
```

## 🎨 RESPONSIVE DESIGN

### Mobile Anpassungen
```typescript
// Mobile: Single column, bottom sheet filter
if (isMobile) {
  return <MobileSalesCockpit />;
}

// Tablet: 2 columns, side drawer filter
if (isTablet) {
  return <TabletSalesCockpit />;
}

// Desktop: Full experience
return <DesktopSalesCockpit />;
```

## ⚡ PERFORMANCE OPTIMIERUNG

### 1. Lazy Loading Routes
```typescript
const SalesCockpitV2 = lazy(() => 
  import(/* webpackChunkName: "cockpit-v2" */ './SalesCockpitV2')
);
```

### 2. Prefetching
```typescript
// Prefetch customer data when hovering over button
onMouseEnter={() => {
  queryClient.prefetchQuery(['customers', 'virtual']);
}}
```

### 3. Optimistic Updates
```typescript
// Update UI immediately, sync with server later
const optimisticUpdate = useMutation({
  onMutate: async (newData) => {
    // Cancel queries
    await queryClient.cancelQueries(['customers']);
    
    // Optimistic update
    queryClient.setQueryData(['customers'], (old) => {
      return [...old, newData];
    });
  },
});
```

## 🧪 INTEGRATION TESTING

### E2E Test Scenarios
```typescript
describe('SalesCockpitV2 Integration', () => {
  it('should filter customers and maintain performance', async () => {
    // Load page
    await page.goto('/cockpit-v2');
    
    // Switch to customer view
    await page.click('[data-testid="view-customers"]');
    
    // Apply filter
    await page.type('[data-testid="search-input"]', 'Test');
    
    // Verify virtual scrolling
    const items = await page.$$('[data-testid="customer-card"]');
    expect(items.length).toBeLessThanOrEqual(50); // Only visible items
    
    // Scroll and verify lazy loading
    await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
    await page.waitForTimeout(500);
    
    // Export filtered data
    await page.click('[data-testid="export-csv"]');
    
    // Verify download
    const download = await page.waitForEvent('download');
    expect(download.suggestedFilename()).toContain('customers');
  });
});
```

## 📊 SUCCESS METRICS

- **Load Time:** < 1s für initiale Ansicht
- **Filter Response:** < 300ms
- **Scroll FPS:** > 55 FPS
- **Memory:** < 150MB bei 10k Einträgen
- **User Satisfaction:** > 90%

## ✅ INTEGRATION CHECKLIST

### Components
- [ ] IntelligentFilterBar integriert
- [ ] VirtualizedCustomerList funktioniert
- [ ] MiniAuditTimeline in Cards sichtbar
- [ ] ExportToolbar funktioniert
- [ ] View Toggle implementiert

### Performance
- [ ] Virtual Scrolling aktiv bei > 20 Items
- [ ] Lazy Loading für Bilder
- [ ] Debounced Search
- [ ] Memoized Components

### Features
- [ ] Universal Search funktioniert
- [ ] Filter kombinierbar
- [ ] Export in alle Formate
- [ ] Audit Trail sichtbar (role-based)
- [ ] Responsive Design

### Testing
- [ ] Unit Tests grün
- [ ] Integration Tests grün
- [ ] E2E Tests grün
- [ ] Performance Tests bestanden

## 🚀 DEPLOYMENT

### 1. Build Optimization
```bash
# Production build with optimizations
npm run build -- --mode production

# Analyze bundle size
npm run analyze
```

### 2. Route Configuration
```typescript
// Update route to use V2
{
  path: '/cockpit',
  element: <SalesCockpitV2 />, // Use V2 instead of V1
}
```

### 3. Feature Flag (Optional)
```typescript
const useCockpitV2 = useFeatureFlag('cockpit-v2');
return useCockpitV2 ? <SalesCockpitV2 /> : <SalesCockpit />;
```

## 🔗 VERWANDTE DOKUMENTE

**← Filter Bar:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/INTELLIGENT_FILTER_BAR.md`  
**← Virtual Scrolling:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/VIRTUAL_SCROLLING_PERFORMANCE.md`  
**← Mini Timeline:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MINI_AUDIT_TIMELINE.md`  
**← Export Backend:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/BACKEND_EXPORT_ENDPOINTS.md`  
**↑ Übersicht:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PR4_ENHANCED_FEATURES_COMPLETE.md`  

---

**Status:** ✅ BEREIT ZUR IMPLEMENTIERUNG  
**Nächster Schritt:** Alle Komponenten in SalesCockpitV2 integrieren und testen  
**Timeline:** Nach Implementierung aller Einzelkomponenten