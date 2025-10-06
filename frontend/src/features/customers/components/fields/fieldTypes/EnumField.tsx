/**
 * Enum Field Component
 *
 * Renders a select field populated from backend enum endpoints.
 * Sprint 2.1.6: Single Source of Truth for enum values.
 */

import React from 'react';
import {
  FormControl,
  Select,
  MenuItem,
  FormHelperText,
  CircularProgress,
} from '@mui/material';
import type { FieldDefinition } from '../../../types/field.types';
import { useBusinessTypes } from '../../../../leads/hooks/useBusinessTypes';
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
  // Determine which hook to use based on enumSource
  const enumSource = (field as any).enumSource || 'business-types';

  // Only call the relevant hook for the current enumSource (avoid unnecessary requests)
  let options: Array<{ value: string; label: string }> = [];
  let isLoading = false;

  if (enumSource === 'business-types') {
    const { data, isLoading: loading } = useBusinessTypes();
    options = data || [];
    isLoading = loading;
  } else if (enumSource === 'lead-sources') {
    const { data, isLoading: loading } = useLeadSources();
    options = data || [];
    isLoading = loading;
  } else if (enumSource === 'kitchen-sizes') {
    const { data, isLoading: loading } = useKitchenSizes();
    options = data || [];
    isLoading = loading;
  } else {
    console.warn(`Unknown enumSource: ${enumSource}`);
  }

  return (
    <FormControl
      fullWidth
      error={error}
      disabled={disabled || readOnly}
      required={required}
    >
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
