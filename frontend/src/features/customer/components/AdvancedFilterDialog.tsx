import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Chip,
  Stack,
  Slider,
  FormLabel,
  IconButton,
  Divider,
} from '@mui/material';
import type { SelectChangeEvent } from '@mui/material/Select';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { de } from 'date-fns/locale';
import CloseIcon from '@mui/icons-material/Close';
import AddIcon from '@mui/icons-material/Add';
import { useFocusListStore } from '../store/focusListStore';
import {
  CustomerStatus,
  CustomerType,
  Industry,
  Classification,
  customerStatusLabels,
  customerTypeLabels,
  industryLabels,
} from '../types/customer.types';

// Temporäre Labels für Classification bis sie in customer.types.ts hinzugefügt werden
const classificationLabels: Record<Classification, string> = {
  [Classification.A_KUNDE]: 'A-Kunde',
  [Classification.B_KUNDE]: 'B-Kunde',
  [Classification.C_KUNDE]: 'C-Kunde',
  [Classification.NEUKUNDE]: 'Neukunde',
  [Classification.VIP]: 'VIP',
};

interface AdvancedFilterDialogProps {
  open: boolean;
  onClose: () => void;
}

interface FilterCriteria {
  status: CustomerStatus[];
  customerType: CustomerType[];
  industry: Industry[];
  classification: Classification[];
  riskScoreRange: [number, number];
  volumeRange: [number, number];
  lastContactRange: [Date | null, Date | null];
  hasOpenTasks: boolean | null;
  salesRep: string[];
}

const initialFilters: FilterCriteria = {
  status: [],
  customerType: [],
  industry: [],
  classification: [],
  riskScoreRange: [0, 100],
  volumeRange: [0, 1000000],
  lastContactRange: [null, null],
  hasOpenTasks: null,
  salesRep: [],
};

export const AdvancedFilterDialog: React.FC<AdvancedFilterDialogProps> = ({ open, onClose }) => {
  const { setSearchCriteria, clearAllFilters } = useFocusListStore();
  const [filters, setFilters] = useState<FilterCriteria>(initialFilters);
  const [savedFilters, setSavedFilters] = useState<
    Array<{ name: string; filters: FilterCriteria }>
  >([]);

  const handleApplyFilters = () => {
    // Konvertiere Filter zu Search Criteria
    const criteria: Record<string, unknown> = {};

    if (filters.status.length > 0) {
      criteria.status = filters.status;
    }

    if (filters.customerType.length > 0) {
      criteria.customerType = filters.customerType;
    }

    if (filters.industry.length > 0) {
      criteria.industry = filters.industry;
    }

    if (filters.riskScoreRange[0] > 0 || filters.riskScoreRange[1] < 100) {
      criteria.minRiskScore = filters.riskScoreRange[0];
      criteria.maxRiskScore = filters.riskScoreRange[1];
    }

    if (filters.volumeRange[0] > 0 || filters.volumeRange[1] < 1000000) {
      criteria.minVolume = filters.volumeRange[0];
      criteria.maxVolume = filters.volumeRange[1];
    }

    setSearchCriteria(criteria);
    onClose();
  };

  const handleReset = () => {
    setFilters(initialFilters);
    clearAllFilters();
  };

  const handleSaveFilter = () => {
    const name = prompt('Name für diese Filtereinstellung:');
    if (name) {
      setSavedFilters([...savedFilters, { name, filters: { ...filters } }]);
      // TODO: In localStorage oder Backend speichern
    }
  };

  const handleLoadFilter = (savedFilter: { name: string; filters: FilterCriteria }) => {
    setFilters(savedFilter.filters);
  };

  const handleDeleteSavedFilter = (index: number) => {
    setSavedFilters(savedFilters.filter((_, i) => i !== index));
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={de}>
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
        <DialogTitle>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h6">Erweiterte Filter</Typography>
            <IconButton onClick={onClose} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>

        <DialogContent dividers>
          <Stack spacing={3}>
            {/* Gespeicherte Filter */}
            {savedFilters.length > 0 && (
              <Box>
                <Typography variant="subtitle2" gutterBottom>
                  Gespeicherte Filter
                </Typography>
                <Stack direction="row" spacing={1} flexWrap="wrap">
                  {savedFilters.map((saved, index) => (
                    <Chip
                      key={index}
                      label={saved.name}
                      onClick={() => handleLoadFilter(saved)}
                      onDelete={() => handleDeleteSavedFilter(index)}
                      color="primary"
                      variant="outlined"
                    />
                  ))}
                </Stack>
              </Box>
            )}

            {/* Status Filter */}
            <FormControl fullWidth>
              <InputLabel>Status</InputLabel>
              <Select
                multiple
                value={filters.status}
                onChange={e =>
                  setFilters({ ...filters, status: e.target.value as CustomerStatus[] })
                }
                renderValue={selected => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selected.map(value => (
                      <Chip key={value} label={customerStatusLabels[value]} size="small" />
                    ))}
                  </Box>
                )}
              >
                {Object.entries(customerStatusLabels).map(([value, label]) => (
                  <MenuItem key={value} value={value}>
                    {label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {/* Kundentyp Filter */}
            <FormControl fullWidth>
              <InputLabel>Kundentyp</InputLabel>
              <Select
                multiple
                value={filters.customerType}
                onChange={e =>
                  setFilters({ ...filters, customerType: e.target.value as CustomerType[] })
                }
                renderValue={selected => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selected.map(value => (
                      <Chip key={value} label={customerTypeLabels[value]} size="small" />
                    ))}
                  </Box>
                )}
              >
                {Object.entries(customerTypeLabels).map(([value, label]) => (
                  <MenuItem key={value} value={value}>
                    {label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {/* Branche Filter */}
            <FormControl fullWidth>
              <InputLabel>Branche</InputLabel>
              <Select
                multiple
                value={filters.industry}
                onChange={(e: SelectChangeEvent<Industry[]>) =>
                  setFilters({ ...filters, industry: e.target.value as Industry[] })
                }
                renderValue={selected => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selected.map(value => (
                      <Chip key={value} label={industryLabels[value]} size="small" />
                    ))}
                  </Box>
                )}
              >
                {Object.entries(industryLabels).map(([value, label]) => (
                  <MenuItem key={value} value={value}>
                    {label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {/* Klassifizierung Filter */}
            <FormControl fullWidth>
              <InputLabel>Klassifizierung</InputLabel>
              <Select
                multiple
                value={filters.classification}
                onChange={e =>
                  setFilters({ ...filters, classification: e.target.value as Classification[] })
                }
                renderValue={selected => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selected.map(value => (
                      <Chip key={value} label={classificationLabels[value]} size="small" />
                    ))}
                  </Box>
                )}
              >
                {Object.entries(classificationLabels).map(([value, label]) => (
                  <MenuItem key={value} value={value}>
                    {label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {/* Risiko-Score Range */}
            <Box>
              <FormLabel component="legend">
                Risiko-Score: {filters.riskScoreRange[0]}% - {filters.riskScoreRange[1]}%
              </FormLabel>
              <Slider
                value={filters.riskScoreRange}
                onChange={(_, value) =>
                  setFilters({ ...filters, riskScoreRange: value as [number, number] })
                }
                valueLabelDisplay="auto"
                min={0}
                max={100}
                marks={[
                  { value: 0, label: '0%' },
                  { value: 50, label: '50%' },
                  { value: 100, label: '100%' },
                ]}
              />
            </Box>

            {/* Umsatz Range */}
            <Box>
              <FormLabel component="legend">
                Jahresumsatz: {(filters.volumeRange[0] / 1000).toFixed(0)}k € -{' '}
                {(filters.volumeRange[1] / 1000).toFixed(0)}k €
              </FormLabel>
              <Slider
                value={filters.volumeRange}
                onChange={(_, value) =>
                  setFilters({ ...filters, volumeRange: value as [number, number] })
                }
                valueLabelDisplay="auto"
                min={0}
                max={1000000}
                step={10000}
                marks={[
                  { value: 0, label: '0' },
                  { value: 500000, label: '500k' },
                  { value: 1000000, label: '1M' },
                ]}
              />
            </Box>

            {/* Letzter Kontakt */}
            <Box>
              <FormLabel component="legend" sx={{ mb: 1 }}>
                Letzter Kontakt
              </FormLabel>
              <Stack direction="row" spacing={2}>
                <DatePicker
                  label="Von"
                  value={filters.lastContactRange[0]}
                  onChange={date =>
                    setFilters({
                      ...filters,
                      lastContactRange: [date, filters.lastContactRange[1]],
                    })
                  }
                  slotProps={{ textField: { fullWidth: true } }}
                />
                <DatePicker
                  label="Bis"
                  value={filters.lastContactRange[1]}
                  onChange={date =>
                    setFilters({
                      ...filters,
                      lastContactRange: [filters.lastContactRange[0], date],
                    })
                  }
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Stack>
            </Box>

            <Divider />

            {/* Filter-Zusammenfassung */}
            <Box>
              <Typography variant="subtitle2" gutterBottom>
                Aktive Filter
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {
                  Object.values(filters).filter(v => (Array.isArray(v) ? v.length > 0 : v !== null))
                    .length
                }{' '}
                Filter aktiv
              </Typography>
            </Box>
          </Stack>
        </DialogContent>

        <DialogActions>
          <Button onClick={handleSaveFilter} startIcon={<AddIcon />}>
            Filter speichern
          </Button>
          <Box sx={{ flex: 1 }} />
          <Button onClick={handleReset}>Zurücksetzen</Button>
          <Button onClick={onClose}>Abbrechen</Button>
          <Button onClick={handleApplyFilters} variant="contained" color="primary">
            Anwenden
          </Button>
        </DialogActions>
      </Dialog>
    </LocalizationProvider>
  );
};
