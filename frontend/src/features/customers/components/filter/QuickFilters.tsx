/**
 * Quick Filters Component
 *
 * Predefined filter presets for common customer queries
 *
 * @module QuickFilters
 * @since FC-005 PR4
 */

import React from 'react';
import { Stack, Chip } from '@mui/material';
import { CheckCircle as ActiveIcon, Cancel as InactiveIcon } from '@mui/icons-material';
import { useTheme } from '@mui/material/styles';

import type { FilterConfig } from '../../types/filter.types';
// Import the correct CustomerStatus from the customer feature
import { CustomerStatus } from '../../../customer/types/customer.types';

export interface QuickFilter {
  id: string;
  label: string;
  icon: React.ReactNode;
  filter: Partial<FilterConfig>;
}

// Quick Filter Presets - exported for testing
export const QUICK_FILTERS: QuickFilter[] = [
  {
    id: 'active',
    label: 'Aktive Kunden',
    icon: <ActiveIcon fontSize="small" />,
    filter: { status: [CustomerStatus.AKTIV] },
  },
  {
    id: 'inactive',
    label: 'Inaktive Kunden',
    icon: <InactiveIcon fontSize="small" />,
    filter: { status: [CustomerStatus.INAKTIV] },
  },
];

export const LEAD_FILTERS: QuickFilter[] = [
  {
    id: 'leads',
    label: 'Leads',
    icon: <ActiveIcon fontSize="small" />,
    filter: { status: [CustomerStatus.LEAD] },
  },
  {
    id: 'prospects',
    label: 'Interessenten',
    icon: <InactiveIcon fontSize="small" />,
    filter: { status: [CustomerStatus.PROSPECT] },
  },
];

// Quick Filter Presets - Context-based
const getQuickFilters = (context: 'customers' | 'leads'): QuickFilter[] => {
  if (context === 'leads') {
    return LEAD_FILTERS;
  }

  // Default: customers context
  return QUICK_FILTERS;
};

interface QuickFiltersProps {
  activeQuickFilters: string[];
  onToggleQuickFilter: (filter: QuickFilter) => void;
  context?: 'customers' | 'leads';
}

export function QuickFilters({
  activeQuickFilters,
  onToggleQuickFilter,
  context = 'customers',
}: QuickFiltersProps) {
  const theme = useTheme();

  const quickFilters = getQuickFilters(context);

  return (
    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
      {quickFilters.map(qf => (
        <Chip
          key={qf.id}
          label={qf.label}
          icon={qf.icon}
          onClick={() => onToggleQuickFilter(qf)}
          color={activeQuickFilters.includes(qf.id) ? 'primary' : 'default'}
          variant={activeQuickFilters.includes(qf.id) ? 'filled' : 'outlined'}
          sx={{
            borderColor: activeQuickFilters.includes(qf.id)
              ? theme.palette.primary.main
              : theme.palette.divider,
            '&:hover': {
              borderColor: theme.palette.primary.main,
              bgcolor: theme.palette.action.hover,
            },
          }}
        />
      ))}
    </Stack>
  );
}
