/**
 * Intelligent Filter Bar Component - Refactored Version
 *
 * Advanced filtering interface for customer list with universal search,
 * multi-criteria filters, column management, and export capabilities.
 *
 * This component has been refactored into smaller, more manageable pieces:
 * - FilterDrawer: Advanced filter options drawer
 * - ColumnManagerDrawer: Column visibility and order management
 * - QuickFilters: Predefined filter presets
 * - SearchBar: Universal search input
 * - constants: Shared constants and translations
 *
 * @module IntelligentFilterBar
 * @since FC-005 PR4 - Refactored in PR5
 */

import React, { useState, useCallback, useMemo, useRef } from 'react';
import {
  Box,
  Stack,
  Button,
  Badge,
  Tooltip,
  Typography,
  Chip,
  Menu,
  MenuItem,
} from '@mui/material';
import {
  FilterList as FilterIcon,
  ViewColumn as ColumnIcon,
  Star as StarIcon,
  Delete as DeleteIcon,
  ArrowUpward as ArrowUpIcon,
  ArrowDownward as ArrowDownIcon,
  Clear as ClearIcon,
} from '@mui/icons-material';

import { useDebounce } from '../../customers/hooks/useDebounce';
import { useLocalStorage } from '../../customers/hooks/useLocalStorage';
import { useUniversalSearch } from '../../customers/hooks/useUniversalSearch';
import { SearchResultsDropdown } from '../../customers/components/search/SearchResultsDropdown';
import { useNavigate } from 'react-router-dom';

import type {
  FilterConfig,
  SortConfig,
  ColumnConfig,
  SavedFilterSet,
} from '../../customers/types/filter.types';

// Import refactored components
import { FilterDrawer } from '../../customers/components/filter/FilterDrawer';
import { ColumnManagerDrawer } from '../../customers/components/filter/ColumnManagerDrawer';
import { QuickFilters, type QuickFilter } from '../../customers/components/filter/QuickFilters';
import { SearchBar } from '../../customers/components/filter/SearchBar';
import { getSortOptionsForContext, getTableColumnsForContext } from '../../customers/components/filter/contextConfig';

interface IntelligentFilterBarProps {
  onFilterChange: (filters: FilterConfig) => void;
  onSortChange: (sort: SortConfig) => void;
  onColumnChange?: (columns: ColumnConfig[]) => void;
  totalCount: number;
  filteredCount: number;
  loading?: boolean;
  enableUniversalSearch?: boolean;
  initialFilters?: FilterConfig; // Optional: Sync external filter state
  context?: 'customers' | 'leads'; // Lifecycle Context for filtering
}

/**
 * Main filter bar component with advanced filtering capabilities
 */
export function IntelligentFilterBar({
  onFilterChange,
  onSortChange,
  onColumnChange,
  totalCount,
  filteredCount,
  loading = false,
  enableUniversalSearch = true,
  initialFilters,
  context = 'customers', // Default to customers context
}: IntelligentFilterBarProps) {
  const navigate = useNavigate();
  const searchInputRef = useRef<HTMLInputElement>(null);
  const searchContainerRef = useRef<HTMLDivElement>(null);

  // Sort menu anchor
  const [sortMenuAnchor, setSortMenuAnchor] = useState<null | HTMLElement>(null);

  // State Management
  const [searchTerm, setSearchTerm] = useState('');
  const [showSearchResults, setShowSearchResults] = useState(false);
  const [filterDrawerOpen, setFilterDrawerOpen] = useState(false);
  const [columnDrawerOpen, setColumnDrawerOpen] = useState(false);
  // const [filterSetName, setFilterSetName] = useState(''); // For future save filter functionality

  const [activeFilters, setActiveFilters] = useState<FilterConfig>(() => {
    // Merge initialFilters with defaults, ensuring null values for unset filters
    const defaults: FilterConfig = {
      text: '',
      status: [],
      stage: [], // Lead Stage filter (Leads context only)
      industry: [],
      location: [],
      revenueRange: null,
      riskLevel: [],
      hasContacts: null,
      lastContactDays: null,
      tags: [],
    };

    if (!initialFilters || Object.keys(initialFilters).length === 0) {
      return defaults;
    }

    return {
      ...defaults,
      ...initialFilters,
      // Ensure null values for filters that shouldn't default to false
      hasContacts: initialFilters.hasContacts ?? null,
      lastContactDays: initialFilters.lastContactDays ?? null,
      revenueRange: initialFilters.revenueRange ?? null,
    };
  });

  // Sort state
  const [currentSort, setCurrentSort] = useState<SortConfig>({
    field: 'name',
    direction: 'asc',
  });

  // Saved filter sets
  const [savedFilters, setSavedFilters] = useLocalStorage<SavedFilterSet[]>(
    'customerFilterSets',
    []
  );
  const [selectedFilterSet, setSelectedFilterSet] = useState<string | null>(null);

  // Active quick filters
  const [activeQuickFilters, setActiveQuickFilters] = useState<string[]>([]);

  // Debounced Search
  const debouncedSearchTerm = useDebounce(searchTerm, 300);

  // Universal Search Hook
  const {
    searchResults,
    isLoading: isSearching,
    error: searchError,
    search: performUniversalSearch,
    clearResults,
  } = useUniversalSearch({
    includeContacts: enableUniversalSearch,
    includeInactive: false,
    limit: 15,
    debounceMs: 300,
    minQueryLength: 2,
    context: context, // ✅ Pass context to search in correct tables
  });

  // Context-based column configuration (local state, not persisted globally)
  const [tableColumns, setTableColumns] = useState(() => getTableColumnsForContext(context));

  // Sync table columns when context changes
  React.useEffect(() => {
    setTableColumns(getTableColumnsForContext(context));
  }, [context]);

  // Convert table columns to ColumnConfig format
  const columns = useMemo(
    () =>
      tableColumns
        .sort((a, b) => a.order - b.order)
        .map(col => ({
          id: col.field,
          label: col.label,
          visible: col.visible,
          locked: col.locked,
        })),
    [tableColumns]
  );

  // Column management handlers
  const handleColumnToggle = useCallback(
    (columnId: string) => {
      setTableColumns(prevColumns =>
        prevColumns.map(col => (col.id === columnId ? { ...col, visible: !col.visible } : col))
      );
      // Call parent callback if provided
      if (onColumnChange) {
        const updatedColumns = columns.map(col =>
          col.id === columnId ? { ...col, visible: !col.visible } : col
        );
        onColumnChange(updatedColumns);
      }
    },
    [onColumnChange, columns]
  );

  const handleColumnMove = useCallback(
    (columnId: string, direction: 'up' | 'down') => {
      const sortedTableColumns = [...tableColumns].sort((a, b) => a.order - b.order);
      const currentIndex = sortedTableColumns.findIndex(col => col.field === columnId);

      if (currentIndex === -1) return;

      const newIndex = direction === 'up' ? currentIndex - 1 : currentIndex + 1;

      // Check bounds
      if (newIndex < 0 || newIndex >= sortedTableColumns.length) return;

      // Swap order values
      const updatedColumns = sortedTableColumns.map((col, idx) => {
        if (idx === currentIndex) return { ...col, order: newIndex };
        if (idx === newIndex) return { ...col, order: currentIndex };
        return col;
      });

      setTableColumns(updatedColumns);

      // Call parent callback if provided
      if (onColumnChange) {
        const formattedColumns = updatedColumns
          .sort((a, b) => a.order - b.order)
          .map(col => ({
            id: col.field,
            label: col.label,
            visible: col.visible,
            locked: col.locked,
          }));
        onColumnChange(formattedColumns);
      }
    },
    [tableColumns, onColumnChange]
  );

  // Effect for debounced search
  React.useEffect(() => {
    if (debouncedSearchTerm !== activeFilters.text) {
      const newFilters = { ...activeFilters, text: debouncedSearchTerm };
      setActiveFilters(newFilters);
      onFilterChange(newFilters);
    }
  }, [debouncedSearchTerm, activeFilters, onFilterChange]);

  // Click outside handler for search results
  React.useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        searchContainerRef.current &&
        !searchContainerRef.current.contains(event.target as Node)
      ) {
        setShowSearchResults(false);
      }
    };

    if (showSearchResults) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showSearchResults]);

  // Search Result Handlers
  const handleCustomerClick = useCallback(
    (customerId: string) => {
      const basePath = context === 'leads' ? '/lead-generation/leads' : '/customer-management/customers';
      navigate(`${basePath}/${customerId}`);
      setShowSearchResults(false);
      clearResults();
    },
    [navigate, clearResults, context]
  );

  const handleContactClick = useCallback(
    (customerId: string, contactId: string) => {
      if (context === 'leads') {
        // Leads haben Akkordeon-Struktur, keine Tabs
        navigate(`/lead-generation/leads/${customerId}`);
      } else {
        // Customers haben Tab-Struktur (Tab 2 = Kontakte)
        navigate(`/customer-management/customers/${customerId}?tab=2&highlightContact=${contactId}`);
      }
      setShowSearchResults(false);
      clearResults();
      setSearchTerm('');
    },
    [navigate, clearResults, context]
  );

  // Universal Search Handler
  const handleSearch = useCallback(
    (value: string) => {
      setSearchTerm(value);

      if (!value) {
        setShowSearchResults(false);
        clearResults();
        return;
      }

      if (enableUniversalSearch && performUniversalSearch && value.length >= 2) {
        performUniversalSearch(value);
        setShowSearchResults(true);
      }
    },
    [enableUniversalSearch, performUniversalSearch, clearResults]
  );

  // Filter Application
  const applyFilters = useCallback((filtersToApply?: FilterConfig) => {
    // When applying filters from drawer, ensure quick filters are not auto-activated
    // Quick filters should only be active when explicitly clicked
    // Use provided filters or fallback to activeFilters
    onFilterChange(filtersToApply || activeFilters);
    setFilterDrawerOpen(false);
  }, [activeFilters, onFilterChange]);

  // Quick Filter Toggle
  const toggleQuickFilter = useCallback(
    (quickFilter: QuickFilter) => {
      const isActive = activeQuickFilters.includes(quickFilter.id);

      if (isActive) {
        // Remove filter
        setActiveQuickFilters(prev => prev.filter(id => id !== quickFilter.id));
        const newFilters = { ...activeFilters };
        Object.keys(quickFilter.filter).forEach(key => {
          delete newFilters[key as keyof FilterConfig];
        });
        setActiveFilters(newFilters);
        onFilterChange(newFilters);
      } else {
        // Add filter
        setActiveQuickFilters(prev => [...prev, quickFilter.id]);
        const newFilters = { ...activeFilters, ...quickFilter.filter };
        setActiveFilters(newFilters);
        onFilterChange(newFilters);
      }
    },
    [activeQuickFilters, activeFilters, onFilterChange]
  );

  // Clear all filters
  const clearAllFilters = useCallback(() => {
    const clearedFilters: FilterConfig = {
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
    setActiveFilters(clearedFilters);
    setActiveQuickFilters([]);
    setSelectedFilterSet(null);
    setSearchTerm('');
    onFilterChange(clearedFilters);
  }, [onFilterChange]);

  // Remove single filter value
  const handleRemoveFilter = useCallback(
    (filterKey: keyof FilterConfig, value: string) => {
      const newFilters = { ...activeFilters };

      if (
        filterKey === 'status' ||
        filterKey === 'industry' ||
        filterKey === 'riskLevel' ||
        filterKey === 'tags' ||
        filterKey === 'location'
      ) {
        // Array filters
        const currentArray = newFilters[filterKey] as string[] | undefined;
        newFilters[filterKey] = currentArray?.filter(v => v !== value) || [];
      }

      setActiveFilters(newFilters);
      onFilterChange(newFilters);
    },
    [activeFilters, onFilterChange]
  );

  // Remove entire filter group
  const handleRemoveFilterGroup = useCallback(
    (filterKey: keyof FilterConfig) => {
      const newFilters = { ...activeFilters };

      if (
        filterKey === 'status' ||
        filterKey === 'industry' ||
        filterKey === 'riskLevel' ||
        filterKey === 'tags' ||
        filterKey === 'location'
      ) {
        newFilters[filterKey] = [];
      } else if (
        filterKey === 'revenueRange' ||
        filterKey === 'hasContacts' ||
        filterKey === 'lastContactDays'
      ) {
        newFilters[filterKey] = null;
      }

      setActiveFilters(newFilters);
      onFilterChange(newFilters);
    },
    [activeFilters, onFilterChange]
  );

  // Save current filter set
  // TODO: Implement save filter functionality
  // const saveFilterSet = useCallback(() => {
  //   if (!filterSetName) return;

  //   const newFilterSet: SavedFilterSet = {
  //     id: Date.now().toString(),
  //     name: filterSetName,
  //     filters: activeFilters,
  //     createdAt: new Date().toISOString(),
  //   };

  //   setSavedFilters(prev => [...prev, newFilterSet]);
  //   setFilterSetName('');
  // }, [filterSetName, activeFilters, setSavedFilters]);

  // Load saved filter set
  const loadFilterSet = useCallback(
    (filterSet: SavedFilterSet) => {
      setActiveFilters(filterSet.filters);
      setSelectedFilterSet(filterSet.id);
      onFilterChange(filterSet.filters);
    },
    [onFilterChange]
  );

  // Delete saved filter set
  const deleteFilterSet = useCallback(
    (filterSetId: string) => {
      setSavedFilters(prev => prev.filter(fs => fs.id !== filterSetId));
      if (selectedFilterSet === filterSetId) {
        setSelectedFilterSet(null);
      }
    },
    [selectedFilterSet, setSavedFilters]
  );

  // Sort handling
  const handleSort = useCallback(
    (field: string) => {
      const newSort: SortConfig = {
        field,
        direction: currentSort.field === field && currentSort.direction === 'asc' ? 'desc' : 'asc',
      };
      setCurrentSort(newSort);
      onSortChange(newSort);
      setSortMenuAnchor(null);
    },
    [currentSort, onSortChange]
  );

  // Count active filters
  const activeFilterCount = useMemo(() => {
    let count = 0;
    const filters = activeFilters;

    if (filters.status?.length) count++;
    if (filters.industry?.length) count++;
    if (filters.location?.length) count++;
    if (filters.riskLevel?.length) count++;
    if (filters.hasContacts !== null) count++;
    if (filters.lastContactDays) count++;
    if (filters.tags?.length) count++;
    if (filters.revenueRange) count++;

    return count;
  }, [activeFilters]);

  return (
    <Box sx={{ mb: 3 }}>
      {/* Main Filter Bar */}
      <Stack spacing={2}>
        {/* Search & Action Bar */}
        <Stack direction="row" spacing={2} alignItems="center">
          {/* Universal Search with Results */}
          <Box ref={searchContainerRef} sx={{ position: 'relative', flex: 1 }}>
            <SearchBar
              ref={searchInputRef}
              value={searchTerm}
              onChange={handleSearch}
              onClear={() => {
                setSearchTerm('');
                handleSearch('');
                setShowSearchResults(false);
                clearResults();
              }}
              onFocus={() => {
                if (enableUniversalSearch && searchTerm.length >= 2) {
                  setShowSearchResults(true);
                }
              }}
              loading={loading}
            />

            {/* Search Results Dropdown */}
            {enableUniversalSearch && showSearchResults && (
              <SearchResultsDropdown
                searchQuery={searchTerm}
                searchResults={searchResults}
                isLoading={isSearching}
                error={searchError}
                onCustomerClick={handleCustomerClick}
                onContactClick={handleContactClick}
                onClose={() => setShowSearchResults(false)}
              />
            )}
          </Box>

          {/* Filter Button */}
          <Tooltip title="Erweiterte Filter">
            <Button
              variant="outlined"
              startIcon={<FilterIcon />}
              onClick={() => setFilterDrawerOpen(true)}
              sx={{ minWidth: 120 }}
            >
              <Badge badgeContent={activeFilterCount} color="primary">
                Filter
              </Badge>
            </Button>
          </Tooltip>

          {/* Sort Button */}
          <Tooltip title="Sortierung">
            <Button
              variant="outlined"
              startIcon={currentSort.direction === 'asc' ? <ArrowUpIcon /> : <ArrowDownIcon />}
              onClick={e => setSortMenuAnchor(e.currentTarget)}
              sx={{ minWidth: 120 }}
            >
              Sortieren
            </Button>
          </Tooltip>

          {/* Column Manager */}
          <Tooltip title="Spalten verwalten">
            <Button
              variant="outlined"
              startIcon={<ColumnIcon />}
              onClick={() => setColumnDrawerOpen(true)}
            >
              Spalten
            </Button>
          </Tooltip>

          {/* Reset Button - nur sichtbar wenn Filter aktiv */}
          {activeFilterCount > 0 && (
            <Tooltip title="Alle Filter zurücksetzen">
              <Button
                variant="outlined"
                color="error"
                startIcon={<ClearIcon />}
                onClick={clearAllFilters}
              >
                Zurücksetzen
              </Button>
            </Tooltip>
          )}
        </Stack>

        {/* Quick Filters & Structured Filter Chips */}
        <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
          <QuickFilters
            activeQuickFilters={activeQuickFilters}
            onToggleQuickFilter={toggleQuickFilter}
            context={context}
          />

          {/* Status Filter Chips */}
          {activeFilters.status && activeFilters.status.length > 0 && (
            <>
              {activeFilters.status.map(status => (
                <Chip
                  key={status}
                  label={`Status: ${status}`}
                  onDelete={() => handleRemoveFilter('status', status)}
                  size="small"
                  color="primary"
                  variant="filled"
                />
              ))}
            </>
          )}

          {/* Industry Filter Chips */}
          {activeFilters.industry && activeFilters.industry.length > 0 && (
            <>
              {activeFilters.industry.map(industry => (
                <Chip
                  key={industry}
                  label={`Branche: ${industry}`}
                  onDelete={() => handleRemoveFilter('industry', industry)}
                  size="small"
                  color="primary"
                  variant="filled"
                />
              ))}
            </>
          )}

          {/* Risk Level Filter Chips */}
          {activeFilters.riskLevel && activeFilters.riskLevel.length > 0 && (
            <>
              {activeFilters.riskLevel.map(level => (
                <Chip
                  key={level}
                  label={`Risiko: ${level}`}
                  onDelete={() => handleRemoveFilter('riskLevel', level)}
                  size="small"
                  color="primary"
                  variant="filled"
                />
              ))}
            </>
          )}

          {/* Has Contacts Filter Chip */}
          {activeFilters.hasContacts !== null && activeFilters.hasContacts !== undefined && (
            <Chip
              label={activeFilters.hasContacts ? 'Mit Kontakten' : 'Ohne Kontakte'}
              onDelete={() => handleRemoveFilterGroup('hasContacts')}
              size="small"
              color="primary"
              variant="filled"
            />
          )}

          {/* Last Contact Days Filter Chip */}
          {activeFilters.lastContactDays && (
            <Chip
              label={`Kein Kontakt seit ${activeFilters.lastContactDays} Tagen`}
              onDelete={() => handleRemoveFilterGroup('lastContactDays')}
              size="small"
              color="primary"
              variant="filled"
            />
          )}

          {/* Revenue Range Filter Chip */}
          {activeFilters.revenueRange && (
            <Chip
              label={`Umsatz: ${activeFilters.revenueRange.min || 0}€ - ${activeFilters.revenueRange.max || '∞'}€`}
              onDelete={() => handleRemoveFilterGroup('revenueRange')}
              size="small"
              color="primary"
              variant="filled"
            />
          )}

          {/* Created Days Filter Chip */}
          {activeFilters.createdDays && (
            <Chip
              label={`Neu (letzte ${activeFilters.createdDays} Tage)`}
              onDelete={() => handleRemoveFilterGroup('createdDays')}
              size="small"
              color="primary"
              variant="filled"
            />
          )}
        </Stack>

        {/* Active Filters Display */}
        {(activeFilterCount > 0 || searchTerm) && (
          <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
            <Typography variant="body2" color="textSecondary">
              Aktive Filter:
            </Typography>
            {searchTerm && (
              <Chip
                label={`Suche: "${searchTerm}"`}
                onDelete={() => {
                  setSearchTerm('');
                  handleSearch('');
                }}
                size="small"
              />
            )}
            {activeFilterCount > 0 && (
              <Chip
                label={`${activeFilterCount} Filter aktiv`}
                onDelete={clearAllFilters}
                size="small"
                color="primary"
              />
            )}
            <Typography variant="body2" color="textSecondary">
              {filteredCount} von {totalCount} Kunden
            </Typography>
          </Stack>
        )}

        {/* Saved Filter Sets */}
        {savedFilters.length > 0 && (
          <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
            <Typography variant="body2" color="textSecondary">
              Gespeicherte Filter:
            </Typography>
            {savedFilters.map(filterSet => (
              <Chip
                key={filterSet.id}
                label={filterSet.name}
                icon={<StarIcon />}
                onClick={() => loadFilterSet(filterSet)}
                onDelete={() => deleteFilterSet(filterSet.id)}
                deleteIcon={<DeleteIcon />}
                variant={selectedFilterSet === filterSet.id ? 'filled' : 'outlined'}
                color={selectedFilterSet === filterSet.id ? 'primary' : 'default'}
                size="small"
              />
            ))}
          </Stack>
        )}
      </Stack>

      {/* Filter Drawer */}
      <FilterDrawer
        open={filterDrawerOpen}
        onClose={() => setFilterDrawerOpen(false)}
        filters={activeFilters}
        onFiltersChange={setActiveFilters}
        onApply={applyFilters}
        onClear={clearAllFilters}
        context={context}
      />

      {/* Column Manager Drawer */}
      <ColumnManagerDrawer
        open={columnDrawerOpen}
        onClose={() => setColumnDrawerOpen(false)}
        columns={columns}
        onColumnToggle={handleColumnToggle}
        onColumnMove={handleColumnMove}
      />

      {/* Sort Menu - Context-based */}
      <Menu
        anchorEl={sortMenuAnchor}
        open={Boolean(sortMenuAnchor)}
        onClose={() => setSortMenuAnchor(null)}
      >
        {getSortOptionsForContext(context).map(option => (
          <MenuItem key={option.field} onClick={() => handleSort(option.field)}>
            {option.icon && `${option.icon} `}
            {option.label}{' '}
            {currentSort.field === option.field && (currentSort.direction === 'asc' ? '↑' : '↓')}
          </MenuItem>
        ))}
      </Menu>
    </Box>
  );
}
