/**
 * Filter Drawer Component
 *
 * Advanced filter options drawer for customer list
 *
 * @module FilterDrawer
 * @since FC-005 PR4
 */

import React, { useState, useEffect } from 'react';
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
import { STATUS_LABELS } from './constants';

interface FilterDrawerProps {
  open: boolean;
  onClose: () => void;
  filters: FilterConfig;
  onFiltersChange: (filters: FilterConfig) => void;
  onApply: () => void;
  onClear: () => void;
  context?: 'customers' | 'leads'; // Lifecycle Context for filtering
}

export function FilterDrawer({
  open,
  onClose,
  filters,
  onFiltersChange,
  onApply,
  onClear,
  context = 'customers', // Default to customers context
}: FilterDrawerProps) {
  const theme = useTheme();

  // Local state - changes are only committed when "Anwenden" is clicked
  const [localFilters, setLocalFilters] = useState<FilterConfig>(filters);

  // Sync local state when drawer opens
  useEffect(() => {
    if (open) {
      setLocalFilters(filters);
    }
  }, [open, filters]);

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

        {/* Status Filter - NUR für Customers (Leads haben Stage-Filter als Schnellfilter!) */}
        {context === 'customers' && (
          <FormControl fullWidth>
            <FormLabel>Status</FormLabel>
            <FormGroup>
              {Object.values(CustomerStatus)
                .filter(status => {
                  // Customer Lifecycle Phase: Zeige nur Erwachsenen-Status (ohne LEAD, PROSPECT, RISIKO)
                  return (
                    status !== CustomerStatus.LEAD &&
                    status !== CustomerStatus.PROSPECT &&
                    status !== CustomerStatus.RISIKO
                  );
                })
                .map(status => (
                  <FormControlLabel
                    key={status}
                    control={
                      <Checkbox
                        checked={localFilters.status?.includes(status) || false}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                          const newStatus = e.target.checked
                            ? [...(localFilters.status || []), status]
                            : localFilters.status?.filter(s => s !== status) || [];
                          setLocalFilters({ ...localFilters, status: newStatus });
                        }}
                      />
                    }
                    label={STATUS_LABELS[status] || status}
                  />
                ))}
            </FormGroup>
          </FormControl>
        )}

        {/* Risk Level / Lead Score Filter */}
        <FormControl fullWidth>
          <FormLabel>{context === 'leads' ? 'Lead-Score' : 'Risiko-Level'}</FormLabel>
          <FormGroup>
            <FormControlLabel
              control={
                <Checkbox
                  checked={localFilters.riskLevel?.includes(RiskLevel.LOW) || false}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                    const newLevels = e.target.checked
                      ? [...(localFilters.riskLevel || []), RiskLevel.LOW]
                      : localFilters.riskLevel?.filter(l => l !== RiskLevel.LOW) || [];
                    setLocalFilters({ ...localFilters, riskLevel: newLevels });
                  }}
                />
              }
              label="Niedrig (0-29)"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={localFilters.riskLevel?.includes(RiskLevel.MEDIUM) || false}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                    const newLevels = e.target.checked
                      ? [...(localFilters.riskLevel || []), RiskLevel.MEDIUM]
                      : localFilters.riskLevel?.filter(l => l !== RiskLevel.MEDIUM) || [];
                    setLocalFilters({ ...localFilters, riskLevel: newLevels });
                  }}
                />
              }
              label="Mittel (30-59)"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={localFilters.riskLevel?.includes(RiskLevel.HIGH) || false}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                    const newLevels = e.target.checked
                      ? [...(localFilters.riskLevel || []), RiskLevel.HIGH]
                      : localFilters.riskLevel?.filter(l => l !== RiskLevel.HIGH) || [];
                    setLocalFilters({ ...localFilters, riskLevel: newLevels });
                  }}
                />
              }
              label="Hoch (60-79)"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={localFilters.riskLevel?.includes(RiskLevel.CRITICAL) || false}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                    const newLevels = e.target.checked
                      ? [...(localFilters.riskLevel || []), RiskLevel.CRITICAL]
                      : localFilters.riskLevel?.filter(l => l !== RiskLevel.CRITICAL) || [];
                    setLocalFilters({ ...localFilters, riskLevel: newLevels });
                  }}
                />
              }
              label="Kritisch (80-100)"
            />
          </FormGroup>
        </FormControl>

        {/* Has Contacts Filter */}
        <FormControl fullWidth>
          <FormLabel>Kontakte</FormLabel>
          <RadioGroup
            value={
              localFilters.hasContacts === null ? 'all' : localFilters.hasContacts ? 'yes' : 'no'
            }
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
              const value = e.target.value;
              setLocalFilters({
                ...localFilters,
                hasContacts: value === 'all' ? null : value === 'yes',
              });
            }}
          >
            <FormControlLabel value="all" control={<Radio />} label="Alle" />
            <FormControlLabel value="yes" control={<Radio />} label="Mit Kontakten" />
            <FormControlLabel value="no" control={<Radio />} label="Ohne Kontakte" />
          </RadioGroup>
        </FormControl>

        {/* Revenue Range Slider */}
        <FormControl fullWidth>
          <FormLabel>
            Erwarteter Jahresumsatz
            {localFilters.revenueRange?.min || localFilters.revenueRange?.max ? (
              <Typography variant="caption" color="primary" sx={{ ml: 1 }}>
                {localFilters.revenueRange?.min
                  ? `${(localFilters.revenueRange.min / 1000).toFixed(0)}k`
                  : '0'}{' '}
                -
                {localFilters.revenueRange?.max
                  ? ` ${(localFilters.revenueRange.max / 1000).toFixed(0)}k`
                  : ' Max'}{' '}
                €
              </Typography>
            ) : null}
          </FormLabel>
          <Slider
            value={[localFilters.revenueRange?.min || 0, localFilters.revenueRange?.max || 500000]}
            onChange={(_, value) => {
              const [min, max] = value as number[];
              setLocalFilters({
                ...localFilters,
                revenueRange: {
                  min: min === 0 ? null : min,
                  max: max === 500000 ? null : max,
                },
              });
            }}
            min={0}
            max={500000}
            step={10000}
            marks={[
              { value: 0, label: '0' },
              { value: 100000, label: '100k' },
              { value: 250000, label: '250k' },
              { value: 500000, label: '500k+' },
            ]}
            valueLabelDisplay="auto"
            valueLabelFormat={value => `${(value / 1000).toFixed(0)}k €`}
          />
        </FormControl>

        {/* Last Contact Days */}
        <FormControl fullWidth>
          <FormLabel>
            Letzter Kontakt vor mehr als {localFilters.lastContactDays || 30} Tagen
          </FormLabel>
          <Slider
            value={localFilters.lastContactDays || 30}
            onChange={(_, value) => {
              const newValue = value as number;
              setLocalFilters({ ...localFilters, lastContactDays: newValue });
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
            onClick={() => {
              // Clear local state first
              setLocalFilters({
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
              // Then trigger parent's clear handler
              onClear();
            }}
          >
            Zurücksetzen
          </Button>
          <Button
            variant="contained"
            fullWidth
            onClick={() => {
              // Commit local changes to parent
              onFiltersChange(localFilters);
              // Then trigger apply handler
              onApply();
            }}
            sx={{
              bgcolor: theme.palette.primary.main,
              '&:hover': {
                bgcolor: theme.palette.primary.dark,
              },
            }}
          >
            Anwenden
          </Button>
        </Stack>
      </Stack>
    </Drawer>
  );
}
