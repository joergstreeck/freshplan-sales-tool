/**
 * Enum Field Component
 *
 * Renders a select field populated from backend enum endpoints.
 * Sprint 2.1.6: Single Source of Truth for enum values.
 */

import React from 'react';
import { FormControl, Select, MenuItem, FormHelperText, CircularProgress } from '@mui/material';
import type { FieldDefinition } from '../../../types/field.types';
import { useBusinessTypes } from '../../../../../hooks/useBusinessTypes';
import { useLeadSources } from '../../../../leads/hooks/useLeadSources';
import { useKitchenSizes } from '../../../../leads/hooks/useKitchenSizes';

interface EnumFieldProps {
  field: FieldDefinition;
  value: unknown;
  onChange: (value: unknown) => void;
  onBlur: () => void;
  error?: boolean;
  helperText?: string;
  disabled?: boolean;
  readOnly?: boolean;
  required?: boolean;
}

/**
 * Enum Field
 *
 * Maps enumSource to corresponding backend endpoint:
 * - "business-types" → GET /api/enums/business-types
 * - "lead-sources" → GET /api/enums/lead-sources
 * - "kitchen-sizes" → GET /api/enums/kitchen-sizes
 *
 * NOTE: React Hooks Rules require ALL hooks to be called unconditionally.
 * We call all 3 hooks, but only use the data for the active enumSource.
 * React Query handles caching, so duplicate requests across components are minimal.
 */
export const EnumField: React.FC<EnumFieldProps> = ({
  field,
  value,
  onChange,
  onBlur,
  error,
  helperText,
  disabled,
  readOnly,
  required,
}) => {
  // Determine which enumSource is active
  const enumSource = (field as { enumSource?: string }).enumSource || 'business-types';

  // Call ALL hooks unconditionally (React Hooks Rules requirement)
  const businessTypes = useBusinessTypes();
  const leadSources = useLeadSources();
  const kitchenSizes = useKitchenSizes();

  // Select the appropriate data based on enumSource
  let options: Array<{ value: string; label: string }> = [];
  let isLoading = false;

  switch (enumSource) {
    case 'business-types':
      options = businessTypes.data || [];
      isLoading = businessTypes.isLoading;
      break;
    case 'lead-sources':
      options = leadSources.data || [];
      isLoading = leadSources.isLoading;
      break;
    case 'kitchen-sizes':
      options = kitchenSizes.data || [];
      isLoading = kitchenSizes.isLoading;
      break;
    default:
      console.warn(`Unknown enumSource: ${enumSource}`);
  }

  return (
    <FormControl fullWidth error={error} disabled={disabled || readOnly} required={required}>
      <Select
        value={value || ''}
        onChange={e => onChange(e.target.value)}
        onBlur={onBlur}
        displayEmpty
        disabled={disabled || readOnly}
      >
        <MenuItem value="" disabled>
          {isLoading ? (
            <CircularProgress size={20} />
          ) : (
            field.placeholder || `${field.label} wählen...`
          )}
        </MenuItem>
        {options.map(option => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </Select>
      {helperText && <FormHelperText>{helperText}</FormHelperText>}
    </FormControl>
  );
};
