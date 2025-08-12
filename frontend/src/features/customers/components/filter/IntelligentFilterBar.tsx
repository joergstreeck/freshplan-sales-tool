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
  Alert,
  Menu,
  MenuItem,
  Divider,
  useTheme,
  alpha,
} from '@mui/material';
import {
  FilterList as FilterIcon,
  ViewColumn as ColumnIcon,
  Sort as SortIcon,
  Star as StarIcon,
  Delete as DeleteIcon,
  ArrowUpward as ArrowUpIcon,
  ArrowDownward as ArrowDownIcon,
} from '@mui/icons-material';

import { useDebounce } from '../../hooks/useDebounce';
import { useLocalStorage } from '../../hooks/useLocalStorage';
import { useUniversalSearch } from '../../hooks/useUniversalSearch';
import { SearchResultsDropdown } from '../search/SearchResultsDropdown';
import { useNavigate } from 'react-router-dom';
import { useFocusListStore } from '../../../customer/store/focusListStore';

import type {
  FilterConfig,
  SortConfig,
  ColumnConfig,
  SavedFilterSet,
} from '../../types/filter.types';

// Import refactored components
import { FilterDrawer } from './FilterDrawer';
import { ColumnManagerDrawer } from './ColumnManagerDrawer';
import { QuickFilters, QUICK_FILTERS, type QuickFilter } from './QuickFilters';
import { SearchBar } from './SearchBar';

interface IntelligentFilterBarProps {
  onFilterChange: (filters: FilterConfig) => void;
  onSortChange: (sort: SortConfig) => void;
  onColumnChange?: (columns: ColumnConfig[]) => void;
  totalCount: number;
  filteredCount: number;
  loading?: boolean;
  enableUniversalSearch?: boolean;
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
}: IntelligentFilterBarProps) {
  const navigate = useNavigate();
  const theme = useTheme();
  const searchInputRef = useRef<HTMLInputElement>(null);
  const searchContainerRef = useRef<HTMLDivElement>(null);

  // Sort menu anchor
  const [sortMenuAnchor, setSortMenuAnchor] = useState<null | HTMLElement>(null);

  // State Management
  const [searchTerm, setSearchTerm] = useState('');
  const [showSearchResults, setShowSearchResults] = useState(false);
  const [filterDrawerOpen, setFilterDrawerOpen] = useState(false);
  const [columnDrawerOpen, setColumnDrawerOpen] = useState(false);
  const [filterSetName, setFilterSetName] = useState('');

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
  });

  // Use focus list store for columns
  const {
    tableColumns,
    toggleColumnVisibility: toggleColumnVisibilityStore,
    setColumnOrder: setColumnOrderStore,
    resetTableColumns: _resetTableColumns,
  } = useFocusListStore();

  // Convert store columns to ColumnConfig format
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
      toggleColumnVisibilityStore(columnId);
      // Call parent callback if provided
      if (onColumnChange) {
        const updatedColumns = columns.map(col =>
          col.id === columnId ? { ...col, visible: !col.visible } : col
        );
        onColumnChange(updatedColumns);
      }
    },
    [toggleColumnVisibilityStore, onColumnChange, columns]
  );

  const handleColumnMove = useCallback(
    (columnId: string, direction: 'up' | 'down') => {
      // Arbeite direkt mit tableColumns aus dem Store
      const sortedTableColumns = [...tableColumns].sort((a, b) => a.order - b.order);
      const currentIndex = sortedTableColumns.findIndex(col => col.field === columnId);
      
      if (currentIndex === -1) return;

      const newIndex = direction === 'up' ? currentIndex - 1 : currentIndex + 1;
      
      // Check bounds
      if (newIndex < 0 || newIndex >= sortedTableColumns.length) return;

      // Erstelle ein neues Array mit allen IDs in der neuen Reihenfolge
      const newOrder = [...sortedTableColumns];
      const temp = newOrder[currentIndex];
      newOrder[currentIndex] = newOrder[newIndex];
      newOrder[newIndex] = temp;

      // Extrahiere die IDs in der neuen Reihenfolge
      const newColumnIds = newOrder.map(col => col.id);
      setColumnOrderStore(newColumnIds);

      // Call parent callback if provided
      if (onColumnChange) {
        const updatedColumns = newOrder.map(col => ({
          id: col.field,
          label: col.label,
          visible: col.visible,
          locked: col.locked,
        }));
        onColumnChange(updatedColumns);
      }
    },
    [tableColumns, setColumnOrderStore, onColumnChange]
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
      navigate(`/customers/${customerId}`);
      setShowSearchResults(false);
      clearResults();
    },
    [navigate, clearResults]
  );

  const handleContactClick = useCallback(
    (customerId: string, contactId: string) => {
      navigate(`/customers/${customerId}?highlightContact=${contactId}`);
      setShowSearchResults(false);
      clearResults();
      setSearchTerm('');
    },
    [navigate, clearResults]
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
  const applyFilters = useCallback(() => {
    onFilterChange(activeFilters);
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

  // Save current filter set
  const saveFilterSet = useCallback(() => {
    if (!filterSetName) return;

    const newFilterSet: SavedFilterSet = {
      id: Date.now().toString(),
      name: filterSetName,
      filters: activeFilters,
      createdAt: new Date().toISOString(),
    };

    setSavedFilters(prev => [...prev, newFilterSet]);
    setFilterSetName('');
  }, [filterSetName, activeFilters, setSavedFilters]);

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
              startIcon={
                currentSort.direction === 'asc' ? <ArrowUpIcon /> : <ArrowDownIcon />
              }
              onClick={(e) => setSortMenuAnchor(e.currentTarget)}
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
        </Stack>

        {/* Quick Filters */}
        <QuickFilters
          activeQuickFilters={activeQuickFilters}
          onToggleQuickFilter={toggleQuickFilter}
        />

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
      />

      {/* Column Manager Drawer */}
      <ColumnManagerDrawer
        open={columnDrawerOpen}
        onClose={() => setColumnDrawerOpen(false)}
        columns={columns}
        onColumnToggle={handleColumnToggle}
        onColumnMove={handleColumnMove}
      />

      {/* Sort Menu */}
      <Menu
        anchorEl={sortMenuAnchor}
        open={Boolean(sortMenuAnchor)}
        onClose={() => setSortMenuAnchor(null)}
      >
        <MenuItem onClick={() => handleSort('name')}>
          Name {currentSort.field === 'name' && (currentSort.direction === 'asc' ? '↑' : '↓')}
        </MenuItem>
        <MenuItem onClick={() => handleSort('status')}>
          Status {currentSort.field === 'status' && (currentSort.direction === 'asc' ? '↑' : '↓')}
        </MenuItem>
        <MenuItem onClick={() => handleSort('revenue')}>
          Umsatz {currentSort.field === 'revenue' && (currentSort.direction === 'asc' ? '↑' : '↓')}
        </MenuItem>
        <MenuItem onClick={() => handleSort('riskLevel')}>
          Risiko {currentSort.field === 'riskLevel' && (currentSort.direction === 'asc' ? '↑' : '↓')}
        </MenuItem>
        <MenuItem onClick={() => handleSort('lastContact')}>
          Letzter Kontakt {currentSort.field === 'lastContact' && (currentSort.direction === 'asc' ? '↑' : '↓')}
        </MenuItem>
        <Divider />
        <MenuItem onClick={() => handleSort('created')}>
          Erstellt {currentSort.field === 'created' && (currentSort.direction === 'asc' ? '↑' : '↓')}
        </MenuItem>
        <MenuItem onClick={() => handleSort('modified')}>
          Geändert {currentSort.field === 'modified' && (currentSort.direction === 'asc' ? '↑' : '↓')}
        </MenuItem>
      </Menu>
    </Box>
  );
}