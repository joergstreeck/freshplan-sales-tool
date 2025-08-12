/**
 * Filter Drawer Component
 * 
 * Advanced filter options drawer for customer list
 * 
 * @module FilterDrawer
 * @since FC-005 PR4
 */

import React from 'react';
import {
  Drawer,
  Stack,
  Typography,
  IconButton,
  Divider,
  FormControl,
  FormLabel,
  FormGroup,
  FormControlLabel,
  Checkbox,
  RadioGroup,
  Radio,
  Slider,
  Button,
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { useTheme } from '@mui/material/styles';

import type { FilterConfig } from '../../types/filter.types';
import { RiskLevel } from '../../types/filter.types';
// Import the correct CustomerStatus from the customer feature
import { CustomerStatus } from '../../../customer/types/customer.types';
import { STATUS_LABELS, RISK_LABELS } from './constants';

interface FilterDrawerProps {
  open: boolean;
  onClose: () => void;
  filters: FilterConfig;
  onFiltersChange: (filters: FilterConfig) => void;
  onApply: () => void;
  onClear: () => void;
}

export function FilterDrawer({
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
              // Set the filter value without activating quick filters
              const newValue = value as number;
              onFiltersChange({ ...filters, lastContactDays: newValue });
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
          <Button 
            variant="contained" 
            fullWidth 
            onClick={onApply}
            sx={{ 
              bgcolor: theme.palette.primary.main,
              '&:hover': {
                bgcolor: theme.palette.primary.dark,
              }
            }}
          >
            Anwenden
          </Button>
        </Stack>
      </Stack>
    </Drawer>
  );
}