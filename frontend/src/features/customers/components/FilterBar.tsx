import React from 'react';
import {
  Box,
  TextField,
  InputAdornment,
  Chip,
  Button,
  IconButton,
  Badge,
  Tooltip,
} from '@mui/material';
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  Clear as ClearIcon,
  Save as SaveIcon,
  ViewModule as ViewModuleIcon,
  ViewList as ViewListIcon,
} from '@mui/icons-material';
import { useFocusListStore } from '../store/focusListStore';

interface QuickFilterChipProps {
  label: string;
  active: boolean;
  onClick: () => void;
  color?: string;
}

const QuickFilterChip: React.FC<QuickFilterChipProps> = ({
  label,
  active,
  onClick,
  color,
}) => (
  <Chip
    label={label}
    onClick={onClick}
    variant={active ? 'filled' : 'outlined'}
    sx={{
      backgroundColor: active ? color : 'transparent',
      borderColor: color,
      color: active ? '#fff' : color,
      '&:hover': {
        backgroundColor: active ? color : `${color}20`,
      },
    }}
  />
);

export const FilterBar: React.FC = () => {
  const globalSearch = useFocusListStore((state) => state.globalSearch);
  const setGlobalSearch = useFocusListStore((state) => state.setGlobalSearch);
  const activeFilters = useFocusListStore((state) => state.activeFilters);
  const viewMode = useFocusListStore((state) => state.viewMode);
  const setViewMode = useFocusListStore((state) => state.setViewMode);
  const clearAllFilters = useFocusListStore((state) => state.clearAllFilters);
  const toggleQuickFilter = useFocusListStore((state) => state.toggleQuickFilter);
  const hasFilter = useFocusListStore((state) => state.hasFilter);
  
  const filterCount = activeFilters.length;
  const hasActiveFilters = globalSearch !== '' || activeFilters.length > 0;

  return (
    <Box
      sx={{
        borderBottom: 1,
        borderColor: 'divider',
        p: 2,
        backgroundColor: '#f5f5f5',
      }}
    >
      {/* Hauptzeile mit Suche und Quick Filters */}
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
        {/* Globale Suche */}
        <TextField
          fullWidth
          variant="outlined"
          placeholder="Kunde, Nummer oder Handelsname suchen..."
          value={globalSearch}
          onChange={(e) => setGlobalSearch(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
            endAdornment: globalSearch && (
              <InputAdornment position="end">
                <IconButton
                  size="small"
                  onClick={() => setGlobalSearch('')}
                  edge="end"
                >
                  <ClearIcon />
                </IconButton>
              </InputAdornment>
            ),
          }}
          sx={{ maxWidth: 400 }}
        />

        {/* Quick Filter Chips */}
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
          <QuickFilterChip
            label="Aktive Kunden"
            active={hasFilter('status', 'AKTIV')}
            onClick={() => toggleQuickFilter('status', 'AKTIV')}
            color="#94C456" // Freshfoodz Grün
          />
          <QuickFilterChip
            label="Risiko > 70"
            active={hasFilter('riskScore', '>70')}
            onClick={() => toggleQuickFilter('riskScore', '>70')}
            color="#F44336"
          />
          <QuickFilterChip
            label="Neue Leads"
            active={hasFilter('status', 'LEAD')}
            onClick={() => toggleQuickFilter('status', 'LEAD')}
            color="#004F7B" // Freshfoodz Blau
          />
        </Box>
      </Box>

      {/* Steuerungszeile */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          {/* Erweiterte Filter Button */}
          <Badge badgeContent={filterCount} color="primary">
            <Button
              variant="outlined"
              startIcon={<FilterIcon />}
              onClick={() => {
                // TODO: Show advanced filters dialog
                console.log('Advanced filters clicked');
              }}
            >
              Erweiterte Filter
            </Button>
          </Badge>

          {/* Clear All Button - nur wenn Filter aktiv */}
          {hasActiveFilters && (
            <Button
              variant="text"
              color="error"
              onClick={clearAllFilters}
              startIcon={<ClearIcon />}
            >
              Alle Filter löschen
            </Button>
          )}
        </Box>

        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          {/* Gespeicherte Ansichten */}
          <Tooltip title="Ansicht speichern">
            <span>
              <IconButton disabled={!hasActiveFilters}>
                <SaveIcon />
              </IconButton>
            </span>
          </Tooltip>

          {/* View Mode Toggle */}
          <Box
            sx={{
              display: 'flex',
              border: 1,
              borderColor: 'divider',
              borderRadius: 1,
            }}
          >
            <Tooltip title="Kartenansicht">
              <IconButton
                size="small"
                onClick={() => setViewMode('cards')}
                color={viewMode === 'cards' ? 'primary' : 'default'}
              >
                <ViewModuleIcon />
              </IconButton>
            </Tooltip>
            <Tooltip title="Tabellenansicht">
              <IconButton
                size="small"
                onClick={() => setViewMode('table')}
                color={viewMode === 'table' ? 'primary' : 'default'}
              >
                <ViewListIcon />
              </IconButton>
            </Tooltip>
          </Box>
        </Box>
      </Box>

      {/* TODO: Advanced Filter Dialog */}
      {/* TODO: Active Filters Display */}
    </Box>
  );
};