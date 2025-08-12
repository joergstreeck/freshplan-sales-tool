/**
 * Intelligent Filter Bar Component
 *
 * Advanced filtering interface for customer list with universal search,
 * multi-criteria filters, column management, and export capabilities.
 *
 * @module IntelligentFilterBar
 * @since FC-005 PR4
 */

import React, { useState, useCallback, useMemo, useRef } from 'react';
import {
  Box,
  TextField,
  InputAdornment,
  IconButton,
  Chip,
  Stack,
  Button,
  Badge,
  Tooltip,
  Drawer,
  Typography,
  Divider,
  FormControl,
  MenuItem,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Alert,
  useTheme,
  alpha,
  FormLabel,
  RadioGroup,
  Radio,
  Slider,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  Switch,
  Menu,
} from '@mui/material';
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  ViewColumn as ColumnIcon,
  Sort as SortIcon,
  Clear as ClearIcon,
  Close as CloseIcon,
  Add as AddIcon,
  ArrowUpward as ArrowUpIcon,
  ArrowDownward as ArrowDownIcon,
  Star as StarIcon,
  Business as BusinessIcon,
  Warning as RiskIcon,
  Schedule as RecentIcon,
  TrendingUp as RevenueIcon,
  Delete as DeleteIcon,
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
import { RiskLevel } from '../../types/filter.types';
import { CustomerStatus } from '../../types/customer.types';

// Deutsche Übersetzungen
const STATUS_LABELS: Record<CustomerStatus, string> = {
  [CustomerStatus.DRAFT]: 'Entwurf',
  [CustomerStatus.ACTIVE]: 'Aktiv',
  [CustomerStatus.INACTIVE]: 'Inaktiv',
  [CustomerStatus.DELETED]: 'Gelöscht',
};

const RISK_LABELS: Record<RiskLevel, string> = {
  [RiskLevel.LOW]: 'Niedrig',
  [RiskLevel.MEDIUM]: 'Mittel',
  [RiskLevel.HIGH]: 'Hoch',
  [RiskLevel.CRITICAL]: 'Kritisch',
};

interface IntelligentFilterBarProps {
  onFilterChange: (filters: FilterConfig) => void;
  onSortChange: (sort: SortConfig) => void;
  onColumnChange?: (columns: ColumnConfig[]) => void; // Now optional - uses store if not provided
  totalCount: number;
  filteredCount: number;
  loading?: boolean;
  enableUniversalSearch?: boolean; // Enable contact search
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
  const [_saveDialogOpen, _setSaveDialogOpen] = useState(false);
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

  // Gespeicherte Filtersets
  const [savedFilters, setSavedFilters] = useLocalStorage<SavedFilterSet[]>(
    'customerFilterSets',
    []
  );
  const [selectedFilterSet, setSelectedFilterSet] = useState<string | null>(null);

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

  // Quick Filter Presets
  const quickFilters = [
    {
      id: 'active',
      label: 'Aktive Kunden',
      icon: <BusinessIcon fontSize="small" />,
      filter: { status: [CustomerStatus.ACTIVE] },
    },
    {
      id: 'at-risk',
      label: 'Risiko-Kunden',
      icon: <RiskIcon fontSize="small" />,
      filter: { riskLevel: [RiskLevel.HIGH, RiskLevel.MEDIUM] },
    },
    {
      id: 'no-contact',
      label: 'Lange kein Kontakt',
      icon: <RecentIcon fontSize="small" />,
      filter: { lastContactDays: 90 },
    },
    {
      id: 'high-value',
      label: 'Top-Kunden',
      icon: <RevenueIcon fontSize="small" />,
      filter: { revenueRange: { min: 100000, max: null } },
    },
    {
      id: 'new',
      label: 'Neue Kunden',
      icon: <AddIcon fontSize="small" />,
      filter: { createdDays: 30 },
    },
  ];

  // Use focus list store for columns
  const {
    tableColumns,
    toggleColumnVisibility: toggleColumnVisibilityStore,
    setColumnOrder: setColumnOrderStore,
    resetTableColumns: _resetTableColumns,
  } = useFocusListStore();

  // Convert store columns to ColumnConfig format for compatibility
  const columns = useMemo(
    () =>
      tableColumns
        .sort((a, b) => a.order - b.order)
        .map(col => ({
          id: col.field,
          label: col.label,
          visible: col.visible,
          locked: col.field === 'companyName', // Only company name is locked
        })),
    [tableColumns]
  );

  // Sortier-Konfiguration
  const [sortConfig, setSortConfig] = useState<SortConfig>({
    field: 'companyName',
    direction: 'asc',
    priority: 0,
  });

  // Universal Search Handler
  const handleSearch = useCallback(
    (value: string) => {
      setSearchTerm(value);

      // For table filtering
      const newFilters = { ...activeFilters, text: value };
      setActiveFilters(newFilters);
      onFilterChange(newFilters);

      // For universal search
      if (enableUniversalSearch && performUniversalSearch && value.length >= 2) {
        performUniversalSearch(value);
        setShowSearchResults(true);
      } else if (clearResults && value.length < 2) {
        clearResults();
        setShowSearchResults(false);
      }
    },
    [activeFilters, onFilterChange, enableUniversalSearch, performUniversalSearch, clearResults]
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
      // Navigate to customer page with contact highlight (Deep-Linking)
      navigate(`/customers/${customerId}?highlightContact=${contactId}`);
      setShowSearchResults(false);
      clearResults();
      setSearchTerm(''); // Clear search field after navigation
    },
    [navigate, clearResults]
  );

  // Filter Application
  const applyFilters = useCallback(() => {
    onFilterChange(activeFilters);
    setFilterDrawerOpen(false);
  }, [activeFilters, onFilterChange]);

  // Quick Filter Toggle
  const toggleQuickFilter = useCallback(
    (quickFilter: unknown) => {
      const newFilters = { ...activeFilters, ...quickFilter.filter };
      setActiveFilters(newFilters);
      onFilterChange(newFilters);
    },
    [activeFilters, onFilterChange]
  );

  // Save Current Filter Set
  const _saveFilterSet = useCallback(() => {
    if (!filterSetName) return;

    const newSet: SavedFilterSet = {
      id: Date.now().toString(),
      name: filterSetName,
      filters: activeFilters,
      columns: columns,
      sort: sortConfig,
      createdAt: new Date().toISOString(),
    };
    setSavedFilters([...savedFilters, newSet]);
    setFilterSetName('');
    _setSaveDialogOpen(false);
  }, [activeFilters, columns, sortConfig, savedFilters, setSavedFilters, filterSetName]);

  // Load Filter Set
  const loadFilterSet = useCallback(
    (setId: string) => {
      const set = savedFilters.find(s => s.id === setId);
      if (set) {
        setActiveFilters(set.filters);
        // Apply columns if available
        if (set.columns && onColumnChange) {
          onColumnChange(set.columns);
        }
        setSortConfig(set.sort);
        onFilterChange(set.filters);
        onSortChange(set.sort);
        setSelectedFilterSet(setId);
      }
    },
    [savedFilters, onFilterChange, onColumnChange, onSortChange]
  );

  // Delete Filter Set
  const deleteFilterSet = useCallback(
    (setId: string) => {
      setSavedFilters(savedFilters.filter(s => s.id !== setId));
      if (selectedFilterSet === setId) {
        setSelectedFilterSet(null);
      }
    },
    [savedFilters, setSavedFilters, selectedFilterSet]
  );

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

  // Move column up/down
  const moveColumn = useCallback(
    (columnId: string, direction: 'up' | 'down') => {
      const currentIndex = columns.findIndex(c => c.id === columnId);
      if (currentIndex === -1) return;

      const newIndex = direction === 'up' ? currentIndex - 1 : currentIndex + 1;

      // Check bounds
      if (newIndex < 0 || newIndex >= columns.length) return;

      // Create new columns array with swapped positions
      const newColumns = [...columns];
      [newColumns[currentIndex], newColumns[newIndex]] = [
        newColumns[newIndex],
        newColumns[currentIndex],
      ];

      // Update store with new order
      const newColumnOrder = tableColumns
        .map(col => {
          const visibleIndex = newColumns.findIndex(c => c.id === col.field);
          if (visibleIndex !== -1) {
            return { ...col, order: visibleIndex };
          }
          return { ...col, order: col.order + newColumns.length };
        })
        .sort((a, b) => a.order - b.order)
        .map(col => col.id);

      setColumnOrderStore(newColumnOrder);

      // Call prop callback if provided
      if (onColumnChange) {
        onColumnChange(newColumns);
      }
    },
    [columns, tableColumns, setColumnOrderStore, onColumnChange]
  );

  // Toggle Column Visibility - Use store
  const toggleColumnVisibility = useCallback(
    (columnId: string) => {
      // The columnId from UI is actually the field name
      // We need to find the column by field and toggle by its id
      const column = tableColumns.find(col => col.field === columnId || col.id === columnId);
      if (column) {
        toggleColumnVisibilityStore(column.id);

        // Call prop callback if provided
        if (onColumnChange) {
          const newColumns = columns.map(col =>
            col.id === columnId ? { ...col, visible: !col.visible } : col
          );
          onColumnChange(newColumns);
        }
      }
    },
    [tableColumns, toggleColumnVisibilityStore, onColumnChange, columns]
  );

  // Handle Sort Change
  const handleSortChange = useCallback(
    (field: string) => {
      const newSort: SortConfig = {
        field,
        direction: sortConfig.field === field && sortConfig.direction === 'asc' ? 'desc' : 'asc',
        priority: 0,
      };
      setSortConfig(newSort);
      onSortChange(newSort);
    },
    [sortConfig, onSortChange]
  );

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
          <Box ref={searchContainerRef} sx={{ position: 'relative', flex: 1 }}>
            <TextField
              ref={searchInputRef}
              fullWidth
              placeholder="Suche nach Firma, Kundennummer, Kontakten..."
              value={searchTerm}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleSearch(e.target.value)}
              autoComplete="off"
              disabled={loading}
              inputProps={{
                autoComplete: 'off',
                'data-form-type': 'other',
                'data-lpignore': 'true',
                autoCorrect: 'off',
                autoCapitalize: 'off',
                spellCheck: 'false',
              }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
                endAdornment: searchTerm && (
                  <InputAdornment position="end">
                    <IconButton
                      size="small"
                      onClick={() => {
                        setSearchTerm('');
                        handleSearch('');
                        setShowSearchResults(false);
                        clearResults();
                      }}
                    >
                      <ClearIcon />
                    </IconButton>
                  </InputAdornment>
                ),
              }}
              onFocus={() => {
                if (enableUniversalSearch && searchTerm.length >= 2) {
                  setShowSearchResults(true);
                }
              }}
              sx={{
                '& .MuiOutlinedInput-root': {
                  backgroundColor: theme.palette.background.paper,
                },
              }}
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
            <IconButton
              onClick={() => setFilterDrawerOpen(true)}
              color={activeFilterCount > 0 ? 'primary' : 'default'}
            >
              <Badge badgeContent={activeFilterCount} color="primary">
                <FilterIcon />
              </Badge>
            </IconButton>
          </Tooltip>

          {/* Sort Menu */}
          <Tooltip
            title={`Sortiert nach: ${columns.find(c => c.id === sortConfig.field)?.label} (${sortConfig.direction === 'asc' ? '↑' : '↓'})`}
          >
            <IconButton onClick={(e: React.MouseEvent<HTMLButtonElement>) => setSortMenuAnchor(e.currentTarget)}>
              <SortIcon />
            </IconButton>
          </Tooltip>

          {/* Column Manager */}
          <Tooltip title="Spalten verwalten">
            <IconButton onClick={() => setColumnDrawerOpen(true)}>
              <ColumnIcon />
            </IconButton>
          </Tooltip>
        </Stack>

        {/* Quick Filters */}
        <Stack direction="row" spacing={1} sx={{ overflowX: 'auto', pb: 1 }}>
          {quickFilters.map(qf => (
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
              {savedFilters.slice(0, 3).map(set => (
                <Chip
                  key={set.id}
                  icon={<StarIcon fontSize="small" />}
                  label={set.name}
                  onClick={() => loadFilterSet(set.id)}
                  onDelete={() => deleteFilterSet(set.id)}
                  deleteIcon={<DeleteIcon fontSize="small" />}
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

      {/* Sort Menu */}
      <Menu
        anchorEl={sortMenuAnchor}
        open={Boolean(sortMenuAnchor)}
        onClose={() => setSortMenuAnchor(null)}
      >
        <MenuItem
          onClick={() => {
            handleSortChange('companyName');
            setSortMenuAnchor(null);
          }}
        >
          <ListItemText>Firma</ListItemText>
        </MenuItem>
        <MenuItem
          onClick={() => {
            handleSortChange('customerNumber');
            setSortMenuAnchor(null);
          }}
        >
          <ListItemText>Kundennummer</ListItemText>
        </MenuItem>
        <MenuItem
          onClick={() => {
            handleSortChange('status');
            setSortMenuAnchor(null);
          }}
        >
          <ListItemText>Status</ListItemText>
        </MenuItem>
        <MenuItem
          onClick={() => {
            handleSortChange('lastContactDate');
            setSortMenuAnchor(null);
          }}
        >
          <ListItemText>Letzter Kontakt</ListItemText>
        </MenuItem>
        <MenuItem
          onClick={() => {
            handleSortChange('createdAt');
            setSortMenuAnchor(null);
          }}
        >
          <ListItemText>Erstellt am</ListItemText>
        </MenuItem>
      </Menu>

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
        onColumnToggle={toggleColumnVisibility}
        onColumnMove={moveColumn}
      />
    </Box>
  );
}

/**
 * Filter Drawer Component
 */
interface FilterDrawerProps {
  open: boolean;
  onClose: () => void;
  filters: FilterConfig;
  onFiltersChange: (filters: FilterConfig) => void;
  onApply: () => void;
  onClear: () => void;
}

function FilterDrawer({
  open,
  onClose,
  filters,
  onFiltersChange,
  onApply,
  onClear,
}: FilterDrawerProps) {
  const _theme = useTheme();

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      sx={{
        '& .MuiDrawer-paper': {
          width: 360,
          p: 3,
        },
      }}
    >
      <Stack spacing={3}>
        {/* Header */}
        <Stack direction="row" alignItems="center" justifyContent="space-between">
          <Typography variant="h6">Erweiterte Filter</Typography>
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Stack>

        <Divider />

        {/* Status Filter */}
        <FormControl fullWidth>
          <FormLabel>Status</FormLabel>
          <FormGroup>
            {Object.values(CustomerStatus).map(status => (
              <FormControlLabel
                key={status}
                control={
                  <Checkbox
                    checked={filters.status?.includes(status) || false}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                      const newStatus = e.target.checked
                        ? [...(filters.status || []), status]
                        : filters.status?.filter(s => s !== status) || [];
                      onFiltersChange({ ...filters, status: newStatus });
                    }}
                  />
                }
                label={STATUS_LABELS[status] || status}
              />
            ))}
          </FormGroup>
        </FormControl>

        {/* Risk Level Filter */}
        <FormControl fullWidth>
          <FormLabel>Risiko-Level</FormLabel>
          <FormGroup>
            {Object.values(RiskLevel).map(level => (
              <FormControlLabel
                key={level}
                control={
                  <Checkbox
                    checked={filters.riskLevel?.includes(level) || false}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                      const newLevels = e.target.checked
                        ? [...(filters.riskLevel || []), level]
                        : filters.riskLevel?.filter(l => l !== level) || [];
                      onFiltersChange({ ...filters, riskLevel: newLevels });
                    }}
                  />
                }
                label={RISK_LABELS[level] || level}
              />
            ))}
          </FormGroup>
        </FormControl>

        {/* Has Contacts Filter */}
        <FormControl fullWidth>
          <FormLabel>Kontakte</FormLabel>
          <RadioGroup
            value={filters.hasContacts === null ? 'all' : filters.hasContacts ? 'yes' : 'no'}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
              const value = e.target.value;
              onFiltersChange({
                ...filters,
                hasContacts: value === 'all' ? null : value === 'yes',
              });
            }}
          >
            <FormControlLabel value="all" control={<Radio />} label="Alle" />
            <FormControlLabel value="yes" control={<Radio />} label="Mit Kontakten" />
            <FormControlLabel value="no" control={<Radio />} label="Ohne Kontakte" />
          </RadioGroup>
        </FormControl>

        {/* Last Contact Days */}
        <FormControl fullWidth>
          <FormLabel>Letzter Kontakt vor mehr als {filters.lastContactDays || 30} Tagen</FormLabel>
          <Slider
            value={filters.lastContactDays || 30}
            onChange={(_, value) => {
              onFiltersChange({ ...filters, lastContactDays: value as number });
            }}
            min={0}
            max={365}
            step={10}
            marks={[
              { value: 0, label: '0' },
              { value: 90, label: '90' },
              { value: 180, label: '180' },
              { value: 365, label: '365' },
            ]}
            valueLabelDisplay="auto"
          />
        </FormControl>

        {/* Action Buttons */}
        <Stack direction="row" spacing={2} sx={{ mt: 'auto' }}>
          <Button variant="outlined" fullWidth onClick={onClear}>
            Zurücksetzen
          </Button>
          <Button variant="contained" fullWidth onClick={onApply}>
            Anwenden
          </Button>
        </Stack>
      </Stack>
    </Drawer>
  );
}

/**
 * Column Manager Drawer Component
 */
interface ColumnManagerDrawerProps {
  open: boolean;
  onClose: () => void;
  columns: ColumnConfig[];
  onColumnToggle: (columnId: string) => void;
  onColumnMove: (columnId: string, direction: 'up' | 'down') => void;
}

function ColumnManagerDrawer({
  open,
  onClose,
  columns,
  onColumnToggle,
  onColumnMove,
}: ColumnManagerDrawerProps) {
  const _theme = useTheme();

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      sx={{
        '& .MuiDrawer-paper': {
          width: 360,
          p: 3,
        },
      }}
    >
      <Stack spacing={3}>
        {/* Header */}
        <Stack direction="row" alignItems="center" justifyContent="space-between">
          <Typography variant="h6">Spalten verwalten</Typography>
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Stack>

        <Divider />

        {/* Column List with Arrow Controls */}
        <List>
          {columns.map((column, index) => (
            <ListItem key={column.id}>
              <ListItemText
                primary={column.label}
                secondary={column.locked ? 'Fixiert' : undefined}
              />
              <ListItemSecondaryAction>
                <Stack direction="row" spacing={1} alignItems="center">
                  {/* Move Up/Down Buttons */}
                  {!column.locked && (
                    <>
                      <IconButton
                        size="small"
                        onClick={() => onColumnMove(column.id, 'up')}
                        disabled={index === 0}
                      >
                        <ArrowUpIcon fontSize="small" />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => onColumnMove(column.id, 'down')}
                        disabled={index === columns.length - 1}
                      >
                        <ArrowDownIcon fontSize="small" />
                      </IconButton>
                    </>
                  )}
                  {/* Visibility Toggle */}
                  <Switch
                    edge="end"
                    checked={column.visible}
                    onChange={() => onColumnToggle(column.id)}
                    disabled={column.locked}
                  />
                </Stack>
              </ListItemSecondaryAction>
            </ListItem>
          ))}
        </List>

        {/* Info */}
        <Alert severity="info">
          Verwenden Sie die Pfeile, um die Reihenfolge zu ändern. Fixierte Spalten können nicht
          verschoben oder ausgeblendet werden.
        </Alert>
      </Stack>
    </Drawer>
  );
}
