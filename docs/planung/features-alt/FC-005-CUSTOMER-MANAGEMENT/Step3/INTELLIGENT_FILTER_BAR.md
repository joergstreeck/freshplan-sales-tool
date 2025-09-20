# 🔍 Intelligent Filter Bar - Erweiterte Kundenlisten-Filterung

**Feature:** FC-005 PR4 - Phase 1  
**Status:** 📋 BEREIT ZUR IMPLEMENTIERUNG  
**Geschätzter Aufwand:** 6-8 Stunden  
**Priorität:** 🥇 HÖCHSTE - Kernfeature für User Experience  

## 🧭 NAVIGATION

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PR4_ENHANCED_FEATURES_COMPLETE.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**→ Nächstes:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MINI_AUDIT_TIMELINE.md`  

## 🎯 VISION

Eine intelligente, performante Filterbar die es Nutzern ermöglicht, aus tausenden von Kunden und Kontakten in Millisekunden die relevanten Datensätze zu finden. Die Suche durchsucht sowohl Kunden- als auch Kontaktdaten und bietet erweiterte Filter-, Sortier- und Gruppierungsfunktionen.

## 📊 FEATURE-ÜBERSICHT

### Core Features:
1. **Universelle Freitext-Suche** - Durchsucht Kunden UND Kontakte gleichzeitig
2. **Multi-Kriterien Filter** - Kombinierbare Filter mit AND/OR Logik
3. **Spalten-Manager** - Dynamisches Ein-/Ausblenden von Spalten
4. **Intelligente Sortierung** - Multi-Column mit Drag & Drop
5. **Gespeicherte Filtersets** - Persönliche und Team-Filter
6. **Quick Filter Chips** - Vordefinierte Ein-Klick-Filter

## 🏗️ TECHNISCHE IMPLEMENTIERUNG

### 1. IntelligentFilterBar Component

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/filter/IntelligentFilterBar.tsx`

```typescript
import React, { useState, useCallback, useMemo, useRef } from 'react';
import {
  Box,
  TextField,
  InputAdornment,
  IconButton,
  Chip,
  Stack,
  Popover,
  Button,
  Badge,
  Tooltip,
  Drawer,
  Typography,
  Divider,
  FormControl,
  Select,
  MenuItem,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Autocomplete,
  ToggleButton,
  ToggleButtonGroup,
  Menu,
  ListItemIcon,
  ListItemText,
  Alert,
  Collapse,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  ViewColumn as ColumnIcon,
  Sort as SortIcon,
  Save as SaveIcon,
  Clear as ClearIcon,
  Download as ExportIcon,
  Settings as SettingsIcon,
  Close as CloseIcon,
  Add as AddIcon,
  Remove as RemoveIcon,
  DragIndicator as DragIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
  Star as StarIcon,
  StarBorder as StarBorderIcon,
  Group as GroupIcon,
  Business as BusinessIcon,
  LocationOn as LocationIcon,
  TrendingUp as RevenueIcon,
  Warning as RiskIcon,
  Schedule as RecentIcon,
} from '@mui/icons-material';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import { useDebounce } from '../../hooks/useDebounce';
import { useLocalStorage } from '../../hooks/useLocalStorage';
import type { 
  FilterConfig, 
  SortConfig, 
  ColumnConfig,
  SavedFilterSet 
} from '../../types/filter.types';

interface IntelligentFilterBarProps {
  onFilterChange: (filters: FilterConfig) => void;
  onSortChange: (sort: SortConfig[]) => void;
  onColumnChange: (columns: ColumnConfig[]) => void;
  onExport?: (format: 'csv' | 'excel' | 'json' | 'pdf') => void;
  totalCount: number;
  filteredCount: number;
  loading?: boolean;
}

export function IntelligentFilterBar({
  onFilterChange,
  onSortChange,
  onColumnChange,
  onExport,
  totalCount,
  filteredCount,
  loading = false,
}: IntelligentFilterBarProps) {
  const theme = useTheme();
  const searchInputRef = useRef<HTMLInputElement>(null);
  
  // State Management
  const [searchTerm, setSearchTerm] = useState('');
  const [filterDrawerOpen, setFilterDrawerOpen] = useState(false);
  const [columnDrawerOpen, setColumnDrawerOpen] = useState(false);
  const [activeFilters, setActiveFilters] = useState<FilterConfig>({
    text: '',
    status: [],
    industry: [],
    location: [],
    revenueRange: null,
    riskLevel: [],
    hasContacts: null,
    lastContactDays: null,
    tags: [],
  });
  
  // Gespeicherte Filtersets
  const [savedFilters, setSavedFilters] = useLocalStorage<SavedFilterSet[]>(
    'customerFilterSets',
    []
  );
  const [selectedFilterSet, setSelectedFilterSet] = useState<string | null>(null);
  
  // Debounced Search
  const debouncedSearchTerm = useDebounce(searchTerm, 300);
  
  // Quick Filter Presets
  const quickFilters = [
    { 
      id: 'active', 
      label: 'Aktive Kunden', 
      icon: <BusinessIcon />,
      filter: { status: ['AKTIV'] } 
    },
    { 
      id: 'at-risk', 
      label: 'Risiko-Kunden', 
      icon: <RiskIcon />,
      filter: { riskLevel: ['HIGH', 'MEDIUM'] } 
    },
    { 
      id: 'no-contact', 
      label: 'Lange kein Kontakt', 
      icon: <RecentIcon />,
      filter: { lastContactDays: 90 } 
    },
    { 
      id: 'high-value', 
      label: 'Top-Kunden', 
      icon: <RevenueIcon />,
      filter: { revenueRange: { min: 100000, max: null } } 
    },
    { 
      id: 'new', 
      label: 'Neue Kunden', 
      icon: <AddIcon />,
      filter: { createdDays: 30 } 
    },
  ];
  
  // Spalten-Konfiguration
  const [columns, setColumns] = useState<ColumnConfig[]>([
    { id: 'customerNumber', label: 'Kundennr.', visible: true, locked: true },
    { id: 'companyName', label: 'Firma', visible: true, locked: true },
    { id: 'status', label: 'Status', visible: true },
    { id: 'industry', label: 'Branche', visible: true },
    { id: 'location', label: 'Standort', visible: true },
    { id: 'contactCount', label: 'Kontakte', visible: true },
    { id: 'lastContact', label: 'Letzter Kontakt', visible: true },
    { id: 'revenue', label: 'Umsatz', visible: false },
    { id: 'riskLevel', label: 'Risiko', visible: true },
    { id: 'createdAt', label: 'Erstellt', visible: false },
    { id: 'tags', label: 'Tags', visible: false },
  ]);
  
  // Sortier-Konfiguration
  const [sortConfig, setSortConfig] = useState<SortConfig[]>([
    { field: 'companyName', direction: 'asc', priority: 0 },
  ]);
  
  // Universal Search Handler
  const handleSearch = useCallback((value: string) => {
    setSearchTerm(value);
    const newFilters = { ...activeFilters, text: value };
    setActiveFilters(newFilters);
    onFilterChange(newFilters);
  }, [activeFilters, onFilterChange]);
  
  // Filter Application
  const applyFilters = useCallback(() => {
    onFilterChange(activeFilters);
    setFilterDrawerOpen(false);
  }, [activeFilters, onFilterChange]);
  
  // Quick Filter Toggle
  const toggleQuickFilter = useCallback((quickFilter: any) => {
    const newFilters = { ...activeFilters, ...quickFilter.filter };
    setActiveFilters(newFilters);
    onFilterChange(newFilters);
  }, [activeFilters, onFilterChange]);
  
  // Save Current Filter Set
  const saveFilterSet = useCallback((name: string) => {
    const newSet: SavedFilterSet = {
      id: Date.now().toString(),
      name,
      filters: activeFilters,
      columns: columns,
      sort: sortConfig,
      createdAt: new Date().toISOString(),
    };
    setSavedFilters([...savedFilters, newSet]);
  }, [activeFilters, columns, sortConfig, savedFilters, setSavedFilters]);
  
  // Load Filter Set
  const loadFilterSet = useCallback((setId: string) => {
    const set = savedFilters.find(s => s.id === setId);
    if (set) {
      setActiveFilters(set.filters);
      setColumns(set.columns);
      setSortConfig(set.sort);
      onFilterChange(set.filters);
      onColumnChange(set.columns);
      onSortChange(set.sort);
      setSelectedFilterSet(setId);
    }
  }, [savedFilters, onFilterChange, onColumnChange, onSortChange]);
  
  // Clear All Filters
  const clearAllFilters = useCallback(() => {
    const emptyFilters: FilterConfig = {
      text: '',
      status: [],
      industry: [],
      location: [],
      revenueRange: null,
      riskLevel: [],
      hasContacts: null,
      lastContactDays: null,
      tags: [],
    };
    setActiveFilters(emptyFilters);
    setSearchTerm('');
    onFilterChange(emptyFilters);
    setSelectedFilterSet(null);
  }, [onFilterChange]);
  
  // Column Drag & Drop Handler
  const handleColumnDragEnd = useCallback((result: any) => {
    if (!result.destination) return;
    
    const items = Array.from(columns);
    const [reorderedItem] = items.splice(result.source.index, 1);
    items.splice(result.destination.index, 0, reorderedItem);
    
    setColumns(items);
    onColumnChange(items);
  }, [columns, onColumnChange]);
  
  // Active Filter Count
  const activeFilterCount = useMemo(() => {
    let count = 0;
    if (activeFilters.text) count++;
    if (activeFilters.status?.length) count++;
    if (activeFilters.industry?.length) count++;
    if (activeFilters.location?.length) count++;
    if (activeFilters.revenueRange) count++;
    if (activeFilters.riskLevel?.length) count++;
    if (activeFilters.hasContacts !== null) count++;
    if (activeFilters.lastContactDays) count++;
    if (activeFilters.tags?.length) count++;
    return count;
  }, [activeFilters]);
  
  return (
    <Box sx={{ mb: 3 }}>
      {/* Main Filter Bar */}
      <Stack spacing={2}>
        {/* Search & Action Bar */}
        <Stack direction="row" spacing={2} alignItems="center">
          {/* Universal Search */}
          <TextField
            ref={searchInputRef}
            fullWidth
            placeholder="Suche nach Kunden, Kontakten, E-Mails, Telefonnummern..."
            value={searchTerm}
            onChange={(e) => handleSearch(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
              endAdornment: searchTerm && (
                <InputAdornment position="end">
                  <IconButton size="small" onClick={() => handleSearch('')}>
                    <ClearIcon />
                  </IconButton>
                </InputAdornment>
              ),
            }}
            sx={{
              '& .MuiOutlinedInput-root': {
                backgroundColor: theme.palette.background.paper,
              },
            }}
          />
          
          {/* Filter Button */}
          <Tooltip title="Erweiterte Filter">
            <IconButton
              onClick={() => setFilterDrawerOpen(true)}
              color={activeFilterCount > 0 ? 'primary' : 'default'}
            >
              <Badge badgeContent={activeFilterCount} color="primary">
                <FilterIcon />
              </Badge>
            </IconButton>
          </Tooltip>
          
          {/* Column Manager */}
          <Tooltip title="Spalten verwalten">
            <IconButton onClick={() => setColumnDrawerOpen(true)}>
              <ColumnIcon />
            </IconButton>
          </Tooltip>
          
          {/* Sort Menu */}
          <Tooltip title="Sortierung">
            <IconButton>
              <SortIcon />
            </IconButton>
          </Tooltip>
          
          {/* Export Menu */}
          {onExport && (
            <Tooltip title="Exportieren">
              <IconButton>
                <ExportIcon />
              </IconButton>
            </Tooltip>
          )}
        </Stack>
        
        {/* Quick Filters */}
        <Stack direction="row" spacing={1} sx={{ overflowX: 'auto', pb: 1 }}>
          {quickFilters.map((qf) => (
            <Chip
              key={qf.id}
              icon={qf.icon}
              label={qf.label}
              onClick={() => toggleQuickFilter(qf)}
              variant="outlined"
              size="small"
              sx={{
                borderColor: theme.palette.divider,
                '&:hover': {
                  backgroundColor: alpha(theme.palette.primary.main, 0.1),
                },
              }}
            />
          ))}
          
          {/* Saved Filter Sets */}
          {savedFilters.length > 0 && (
            <>
              <Divider orientation="vertical" flexItem />
              {savedFilters.slice(0, 3).map((set) => (
                <Chip
                  key={set.id}
                  icon={<StarIcon />}
                  label={set.name}
                  onClick={() => loadFilterSet(set.id)}
                  variant={selectedFilterSet === set.id ? 'filled' : 'outlined'}
                  color="primary"
                  size="small"
                />
              ))}
            </>
          )}
        </Stack>
        
        {/* Filter Summary */}
        {activeFilterCount > 0 && (
          <Alert 
            severity="info" 
            action={
              <Button size="small" onClick={clearAllFilters}>
                Alle löschen
              </Button>
            }
          >
            {filteredCount} von {totalCount} Kunden gefiltert
          </Alert>
        )}
      </Stack>
      
      {/* Filter Drawer - würde hier fortgesetzt... */}
    </Box>
  );
}
```

### 2. Filter Types Definition

**Datei:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/types/filter.types.ts`

```typescript
export interface FilterConfig {
  text?: string;
  status?: CustomerStatus[];
  industry?: string[];
  location?: string[];
  revenueRange?: { min: number | null; max: number | null } | null;
  riskLevel?: RiskLevel[];
  hasContacts?: boolean | null;
  lastContactDays?: number | null;
  tags?: string[];
  createdDays?: number;
  customFields?: Record<string, any>;
}

export interface SortConfig {
  field: string;
  direction: 'asc' | 'desc';
  priority: number;
}

export interface ColumnConfig {
  id: string;
  label: string;
  visible: boolean;
  locked?: boolean;
  width?: number;
  align?: 'left' | 'center' | 'right';
}

export interface SavedFilterSet {
  id: string;
  name: string;
  filters: FilterConfig;
  columns: ColumnConfig[];
  sort: SortConfig[];
  createdAt: string;
  shared?: boolean;
  ownerId?: string;
}

export type FilterOperator = 'AND' | 'OR';

export interface AdvancedFilter {
  field: string;
  operator: 'equals' | 'contains' | 'starts_with' | 'ends_with' | 'greater' | 'less' | 'between' | 'in' | 'not_in';
  value: any;
  caseSensitive?: boolean;
}
```

## 🔍 SUCH-ALGORITHMUS

### Universal Search Implementation

```typescript
// Datei: /frontend/src/features/customers/services/universalSearchService.ts

export class UniversalSearchService {
  /**
   * Durchsucht Kunden und Kontakte gleichzeitig
   * Verwendet Fuzzy-Matching und Relevanz-Scoring
   */
  async search(query: string, options?: SearchOptions): Promise<SearchResults> {
    // 1. Tokenize Query
    const tokens = this.tokenizeQuery(query);
    
    // 2. Detect Query Type (Email, Phone, Name, etc.)
    const queryType = this.detectQueryType(tokens);
    
    // 3. Build Search Queries
    const customerQuery = this.buildCustomerQuery(tokens, queryType);
    const contactQuery = this.buildContactQuery(tokens, queryType);
    
    // 4. Execute Parallel Search
    const [customers, contacts] = await Promise.all([
      this.searchCustomers(customerQuery),
      this.searchContacts(contactQuery),
    ]);
    
    // 5. Merge & Score Results
    const results = this.mergeAndScore(customers, contacts, tokens);
    
    // 6. Apply Relevance Sorting
    return this.sortByRelevance(results);
  }
  
  private detectQueryType(tokens: string[]): QueryType {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const phoneRegex = /^[\d\s\-\+\(\)]+$/;
    
    if (tokens.some(t => emailRegex.test(t))) return 'email';
    if (tokens.some(t => phoneRegex.test(t))) return 'phone';
    if (tokens.some(t => /^\d{4,}$/.test(t))) return 'customerNumber';
    
    return 'text';
  }
  
  private calculateRelevanceScore(item: any, tokens: string[]): number {
    let score = 0;
    
    // Exact matches get highest score
    if (this.hasExactMatch(item, tokens)) score += 100;
    
    // Partial matches
    score += this.countPartialMatches(item, tokens) * 10;
    
    // Boost for primary fields
    if (this.matchInPrimaryFields(item, tokens)) score += 50;
    
    // Recency boost
    score += this.getRecencyScore(item);
    
    return score;
  }
}
```

## 🎨 UI/UX BEST PRACTICES

### 1. Responsive Design
- Mobile: Collapsed filter with bottom sheet
- Tablet: Side drawer
- Desktop: Inline expandable panels

### 2. Keyboard Shortcuts
- `Cmd/Ctrl + K`: Focus search
- `Cmd/Ctrl + F`: Open filter drawer
- `Cmd/Ctrl + Shift + C`: Clear filters
- `Esc`: Close drawers

### 3. Visual Feedback
- Loading skeletons während Suche
- Highlight der Suchergebnisse
- Smooth transitions
- Clear filter indicators

### 4. Accessibility
- ARIA labels für alle Controls
- Keyboard navigation
- Screen reader support
- High contrast mode

## 🚀 PERFORMANCE OPTIMIERUNG

### 1. Debouncing & Throttling
```typescript
const debouncedSearch = useDebounce(searchTerm, 300);
const throttledScroll = useThrottle(handleScroll, 100);
```

### 2. Virtual Scrolling
```typescript
// Nutze react-window für große Listen
<FixedSizeList
  height={600}
  itemCount={filteredResults.length}
  itemSize={72}
  width="100%"
>
  {CustomerRow}
</FixedSizeList>
```

### 3. Memoization
```typescript
const filteredData = useMemo(
  () => applyFilters(data, activeFilters),
  [data, activeFilters]
);
```

### 4. Lazy Loading
```typescript
const LazyCustomerDetails = React.lazy(
  () => import('./CustomerDetails')
);
```

## 🧪 TESTING STRATEGY

### Unit Tests
```typescript
describe('IntelligentFilterBar', () => {
  it('should filter customers by search term', () => {});
  it('should combine multiple filters with AND logic', () => {});
  it('should save and load filter sets', () => {});
  it('should export filtered data', () => {});
});
```

### Integration Tests
- Filter + Virtual Scrolling
- Search + Export
- Save/Load Filter Sets

### E2E Tests
- Complete filter workflow
- Performance with 10k+ records
- Mobile responsiveness

## 📊 SUCCESS METRICS

- **Search Speed:** < 100ms für 10k Records
- **Filter Application:** < 50ms
- **Relevance Score:** > 90% Accuracy
- **User Adoption:** > 80% nutzen gespeicherte Filter

## 🔗 VERWANDTE DOKUMENTE

**← Vorheriges:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PR4_ENHANCED_FEATURES_COMPLETE.md`  
**→ Nächstes:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MINI_AUDIT_TIMELINE.md`  
**→ Virtual Scrolling:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/VIRTUAL_SCROLLING_PERFORMANCE.md`  

---

**Status:** ✅ BEREIT ZUR IMPLEMENTIERUNG  
**Nächster Schritt:** Dependencies installieren und IntelligentFilterBar.tsx erstellen