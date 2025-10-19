import React, { useState } from 'react';
import {
  Box,
  TextField,
  InputAdornment,
  Chip,
  Button,
  IconButton,
  Badge,
  Tooltip,
  Typography,
} from '@mui/material';
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  Clear as ClearIcon,
  Save as SaveIcon,
  ViewModule as ViewModuleIcon,
  ViewList as ViewListIcon,
  TrendingUp as TrendingUpIcon,
  Warning as WarningIcon,
  FiberNew as NewIcon,
} from '@mui/icons-material';
import { useFocusListStore } from '../store/focusListStore';
import { AdvancedFilterDialog } from './AdvancedFilterDialog';
import { TableColumnSettings } from './TableColumnSettings';

interface QuickFilterChipProps {
  label: string;
  active: boolean;
  onClick: () => void;
  color?: string;
  icon?: React.ReactNode;
  count?: number;
}

const QuickFilterChip: React.FC<QuickFilterChipProps> = ({
  label,
  active,
  onClick,
  color = '#94C456',
  icon,
  count,
}) => (
  <Chip
    label={
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
        {label}
        {count !== undefined && (
          <Typography
            component="span"
            variant="caption"
            sx={{
              ml: 0.5,
              fontWeight: 600,
              opacity: 0.8,
            }}
          >
            ({count})
          </Typography>
        )}
      </Box>
    }
    icon={icon}
    onClick={onClick}
    variant={active ? 'filled' : 'outlined'}
    size="small"
    sx={{
      backgroundColor: active ? color : 'transparent',
      borderColor: active ? color : 'divider',
      color: active ? '#fff' : 'text.primary',
      '& .MuiChip-icon': {
        color: active ? '#fff' : color,
      },
      '&:hover': {
        backgroundColor: active ? color : `${color}20`,
        borderColor: color,
      },
    }}
  />
);

export const FilterBar: React.FC = () => {
  const globalSearch = useFocusListStore(state => state.globalSearch);
  const setGlobalSearch = useFocusListStore(state => state.setGlobalSearch);
  const activeFilters = useFocusListStore(state => state.activeFilters);
  const viewMode = useFocusListStore(state => state.viewMode);
  const setViewMode = useFocusListStore(state => state.setViewMode);
  const toggleQuickFilter = useFocusListStore(state => state.toggleQuickFilter);
  const clearAllFilters = useFocusListStore(state => state.clearAllFilters);
  const hasFilter = useFocusListStore(state => state.hasFilter);

  const [filterDialogOpen, setFilterDialogOpen] = useState(false);

  const filterCount = activeFilters.length;

  return (
    <Box>
      {/* Erste Zeile: Suche + Erweiterte Filter + View Toggle */}
      <Box
        sx={{
          display: 'flex',
          gap: 2,
          alignItems: 'center',
          mb: 1.5,
          flexWrap: 'wrap',
        }}
      >
        {/* Suchfeld */}
        <TextField
          size="small"
          placeholder="Kunde, Nummer oder Handelsname suchen..."
          value={globalSearch}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => setGlobalSearch(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon fontSize="small" sx={{ color: 'text.secondary' }} />
              </InputAdornment>
            ),
          }}
          sx={{
            flex: '1 1 300px',
            maxWidth: 400,
          }}
        />

        {/* Erweiterte Filter Button */}
        <Badge badgeContent={filterCount} color="primary">
          <Button
            variant="outlined"
            size="small"
            startIcon={<FilterIcon />}
            onClick={() => setFilterDialogOpen(true)}
          >
            Erweiterte Filter
          </Button>
        </Badge>

        {/* View Mode Toggle */}
        <Box sx={{ display: 'flex', bgcolor: 'grey.100', borderRadius: 1, p: 0.5 }}>
          <Tooltip title="Kartenansicht">
            <IconButton
              size="small"
              onClick={() => setViewMode('cards')}
              sx={{
                bgcolor: viewMode === 'cards' ? 'white' : 'transparent',
                boxShadow: viewMode === 'cards' ? 1 : 0,
              }}
            >
              <ViewModuleIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Tabellenansicht">
            <IconButton
              size="small"
              onClick={() => setViewMode('table')}
              sx={{
                bgcolor: viewMode === 'table' ? 'white' : 'transparent',
                boxShadow: viewMode === 'table' ? 1 : 0,
              }}
            >
              <ViewListIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>

        {/* Tabellen-Einstellungen nur bei Tabellenansicht */}
        {viewMode === 'table' && <TableColumnSettings />}
      </Box>

      {/* Zweite Zeile: Quick Filters als eigene Gruppe */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          flexWrap: 'wrap',
          pt: 1.5,
          borderTop: 1,
          borderColor: 'divider',
        }}
      >
        <Typography
          variant="caption"
          sx={{
            color: 'text.secondary',
            fontWeight: 500,
            mr: 1,
          }}
        >
          Quick-Filter:
        </Typography>

        <QuickFilterChip
          label="Aktive Kunden"
          active={hasFilter('status', 'AKTIV')}
          onClick={() => toggleQuickFilter('status', 'AKTIV')}
          color="#94C456"
          icon={<TrendingUpIcon fontSize="small" />}
        />

        <QuickFilterChip
          label="Risiko > 70"
          active={hasFilter('riskScore', '>70')}
          onClick={() => toggleQuickFilter('riskScore', '>70')}
          color="warning.main"
          icon={<WarningIcon fontSize="small" />}
        />

        <QuickFilterChip
          label="Neue Leads"
          active={hasFilter('status', 'LEAD')}
          onClick={() => toggleQuickFilter('status', 'LEAD')}
          color="#004F7B"
          icon={<NewIcon fontSize="small" />}
        />

        {/* Spacer */}
        <Box sx={{ flex: 1 }} />

        {/* Clear All + Save am Ende */}
        {(activeFilters.length > 0 || globalSearch) && (
          <>
            <Button
              variant="text"
              size="small"
              onClick={clearAllFilters}
              startIcon={<ClearIcon />}
              sx={{ color: 'text.secondary' }}
            >
              Alle zurücksetzen
            </Button>

            {activeFilters.length > 0 && (
              <Button
                variant="text"
                size="small"
                startIcon={<SaveIcon />}
                sx={{ color: 'primary.main' }}
              >
                Filter speichern
              </Button>
            )}
          </>
        )}
      </Box>

      {/* Erweiterte Filter Dialog */}
      <AdvancedFilterDialog open={filterDialogOpen} onClose={() => setFilterDialogOpen(false)} />
    </Box>
  );
};
