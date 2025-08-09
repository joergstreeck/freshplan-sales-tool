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
  Select,
  MenuItem,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Alert,
  useTheme,
  alpha,
  Paper,
  FormLabel,
  RadioGroup,
  Radio,
  Slider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  ListItemSecondaryAction,
  Switch,
  Menu,
} from '@mui/material';
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  ViewColumn as ColumnIcon,
  Sort as SortIcon,
  Save as SaveIcon,
  Clear as ClearIcon,
  Download as ExportIcon,
  Close as CloseIcon,
  Add as AddIcon,
  DragIndicator as DragIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
  Star as StarIcon,
  StarBorder as StarBorderIcon,
  Business as BusinessIcon,
  Warning as RiskIcon,
  Schedule as RecentIcon,
  TrendingUp as RevenueIcon,
  Delete as DeleteIcon,
  FileDownload,
  PictureAsPdf,
  TableChart,
  Code,
} from '@mui/icons-material';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import { useDebounce } from '../../hooks/useDebounce';
import { useLocalStorage } from '../../hooks/useLocalStorage';
import type { 
  FilterConfig, 
  SortConfig, 
  ColumnConfig,
  SavedFilterSet,
  RiskLevel 
} from '../../types/filter.types';
import { CustomerStatus } from '../../types/customer.types';

interface IntelligentFilterBarProps {
  onFilterChange: (filters: FilterConfig) => void;
  onSortChange: (sort: SortConfig[]) => void;
  onColumnChange: (columns: ColumnConfig[]) => void;
  onExport?: (format: 'csv' | 'excel' | 'json' | 'pdf') => void;
  totalCount: number;
  filteredCount: number;
  loading?: boolean;
}

/**
 * Main filter bar component with advanced filtering capabilities
 */
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
  
  // Export menu anchor
  const [exportAnchorEl, setExportAnchorEl] = useState<null | HTMLElement>(null);
  
  // State Management
  const [searchTerm, setSearchTerm] = useState('');
  const [filterDrawerOpen, setFilterDrawerOpen] = useState(false);
  const [columnDrawerOpen, setColumnDrawerOpen] = useState(false);
  const [saveDialogOpen, setSaveDialogOpen] = useState(false);
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
  
  // Quick Filter Presets
  const quickFilters = [
    { 
      id: 'active', 
      label: 'Aktive Kunden', 
      icon: <BusinessIcon fontSize="small" />,
      filter: { status: [CustomerStatus.ACTIVE] } 
    },
    { 
      id: 'at-risk', 
      label: 'Risiko-Kunden', 
      icon: <RiskIcon fontSize="small" />,
      filter: { riskLevel: [RiskLevel.HIGH, RiskLevel.MEDIUM] } 
    },
    { 
      id: 'no-contact', 
      label: 'Lange kein Kontakt', 
      icon: <RecentIcon fontSize="small" />,
      filter: { lastContactDays: 90 } 
    },
    { 
      id: 'high-value', 
      label: 'Top-Kunden', 
      icon: <RevenueIcon fontSize="small" />,
      filter: { revenueRange: { min: 100000, max: null } } 
    },
    { 
      id: 'new', 
      label: 'Neue Kunden', 
      icon: <AddIcon fontSize="small" />,
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
  
  // Effect for debounced search
  React.useEffect(() => {
    if (debouncedSearchTerm !== activeFilters.text) {
      const newFilters = { ...activeFilters, text: debouncedSearchTerm };
      setActiveFilters(newFilters);
      onFilterChange(newFilters);
    }
  }, [debouncedSearchTerm]);
  
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
  const saveFilterSet = useCallback(() => {
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
    setSaveDialogOpen(false);
  }, [activeFilters, columns, sortConfig, savedFilters, setSavedFilters, filterSetName]);
  
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
  
  // Delete Filter Set
  const deleteFilterSet = useCallback((setId: string) => {
    setSavedFilters(savedFilters.filter(s => s.id !== setId));
    if (selectedFilterSet === setId) {
      setSelectedFilterSet(null);
    }
  }, [savedFilters, setSavedFilters, selectedFilterSet]);
  
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
  
  // Toggle Column Visibility
  const toggleColumnVisibility = useCallback((columnId: string) => {
    const newColumns = columns.map(col => 
      col.id === columnId ? { ...col, visible: !col.visible } : col
    );
    setColumns(newColumns);
    onColumnChange(newColumns);
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
  
  // Handle Export
  const handleExport = (format: 'csv' | 'excel' | 'json' | 'pdf') => {
    if (onExport) {
      onExport(format);
    }
    setExportAnchorEl(null);
  };
  
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
            onChange={(e) => setSearchTerm(e.target.value)}
            disabled={loading}
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
                    }}
                  >
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
          
          {/* Export Menu */}
          {onExport && (
            <Tooltip title="Exportieren">
              <IconButton onClick={(e) => setExportAnchorEl(e.currentTarget)}>
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
      
      {/* Export Menu */}
      <Menu
        anchorEl={exportAnchorEl}
        open={Boolean(exportAnchorEl)}
        onClose={() => setExportAnchorEl(null)}
      >
        <MenuItem onClick={() => handleExport('csv')}>
          <ListItemIcon>
            <TableChart fontSize="small" />
          </ListItemIcon>
          <ListItemText>CSV Export</ListItemText>
        </MenuItem>
        <MenuItem onClick={() => handleExport('excel')}>
          <ListItemIcon>
            <TableChart fontSize="small" />
          </ListItemIcon>
          <ListItemText>Excel Export</ListItemText>
        </MenuItem>
        <MenuItem onClick={() => handleExport('pdf')}>
          <ListItemIcon>
            <PictureAsPdf fontSize="small" />
          </ListItemIcon>
          <ListItemText>PDF Export</ListItemText>
        </MenuItem>
        <MenuItem onClick={() => handleExport('json')}>
          <ListItemIcon>
            <Code fontSize="small" />
          </ListItemIcon>
          <ListItemText>JSON Export</ListItemText>
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
        onDragEnd={handleColumnDragEnd}
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
  const theme = useTheme();
  
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
            {Object.values(CustomerStatus).map((status) => (
              <FormControlLabel
                key={status}
                control={
                  <Checkbox
                    checked={filters.status?.includes(status) || false}
                    onChange={(e) => {
                      const newStatus = e.target.checked
                        ? [...(filters.status || []), status]
                        : filters.status?.filter(s => s !== status) || [];
                      onFiltersChange({ ...filters, status: newStatus });
                    }}
                  />
                }
                label={status}
              />
            ))}
          </FormGroup>
        </FormControl>
        
        {/* Risk Level Filter */}
        <FormControl fullWidth>
          <FormLabel>Risiko-Level</FormLabel>
          <FormGroup>
            {Object.values(RiskLevel).map((level) => (
              <FormControlLabel
                key={level}
                control={
                  <Checkbox
                    checked={filters.riskLevel?.includes(level) || false}
                    onChange={(e) => {
                      const newLevels = e.target.checked
                        ? [...(filters.riskLevel || []), level]
                        : filters.riskLevel?.filter(l => l !== level) || [];
                      onFiltersChange({ ...filters, riskLevel: newLevels });
                    }}
                  />
                }
                label={level}
              />
            ))}
          </FormGroup>
        </FormControl>
        
        {/* Has Contacts Filter */}
        <FormControl fullWidth>
          <FormLabel>Kontakte</FormLabel>
          <RadioGroup
            value={filters.hasContacts === null ? 'all' : filters.hasContacts ? 'yes' : 'no'}
            onChange={(e) => {
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
          <FormLabel>
            Letzter Kontakt vor mehr als {filters.lastContactDays || 30} Tagen
          </FormLabel>
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
          <Button 
            variant="outlined" 
            fullWidth 
            onClick={onClear}
          >
            Zurücksetzen
          </Button>
          <Button 
            variant="contained" 
            fullWidth 
            onClick={onApply}
          >
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
  onDragEnd: (result: any) => void;
}

function ColumnManagerDrawer({
  open,
  onClose,
  columns,
  onColumnToggle,
  onDragEnd,
}: ColumnManagerDrawerProps) {
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
        
        {/* Column List with Drag & Drop */}
        <DragDropContext onDragEnd={onDragEnd}>
          <Droppable droppableId="columns">
            {(provided) => (
              <List {...provided.droppableProps} ref={provided.innerRef}>
                {columns.map((column, index) => (
                  <Draggable 
                    key={column.id} 
                    draggableId={column.id} 
                    index={index}
                    isDragDisabled={column.locked}
                  >
                    {(provided, snapshot) => (
                      <ListItem
                        ref={provided.innerRef}
                        {...provided.draggableProps}
                        sx={{
                          backgroundColor: snapshot.isDragging 
                            ? alpha(theme.palette.primary.main, 0.1)
                            : 'transparent',
                        }}
                      >
                        <ListItemIcon {...provided.dragHandleProps}>
                          <DragIcon color={column.locked ? 'disabled' : 'inherit'} />
                        </ListItemIcon>
                        <ListItemText 
                          primary={column.label}
                          secondary={column.locked ? 'Fixiert' : undefined}
                        />
                        <ListItemSecondaryAction>
                          <Switch
                            edge="end"
                            checked={column.visible}
                            onChange={() => onColumnToggle(column.id)}
                            disabled={column.locked}
                          />
                        </ListItemSecondaryAction>
                      </ListItem>
                    )}
                  </Draggable>
                ))}
                {provided.placeholder}
              </List>
            )}
          </Droppable>
        </DragDropContext>
        
        {/* Info */}
        <Alert severity="info">
          Ziehen Sie die Spalten, um die Reihenfolge zu ändern. 
          Fixierte Spalten können nicht verschoben oder ausgeblendet werden.
        </Alert>
      </Stack>
    </Drawer>
  );
}