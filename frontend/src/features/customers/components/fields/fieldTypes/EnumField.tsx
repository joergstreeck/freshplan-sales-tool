/**
 * Enum Field Component
 *
 * Renders a select field populated from backend enum endpoints.
 * Sprint 2.1.6: Single Source of Truth for enum values.
 * Sprint 2.1.7.7: Extended for Contact enums (salutations, titles, decision-levels)
 */

import React from 'react';
import { FormControl, Select, MenuItem, FormHelperText, CircularProgress } from '@mui/material';
import type { FieldDefinition } from '../../../types/field.types';
import { useEnumOptions } from '../../../../../hooks/useEnumOptions';

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
 * Enum Field - Generic Server-Driven Implementation
 *
 * Sprint 2.1.7.7: Refactored to use generic useEnumOptions hook.
 *
 * Supports ALL backend enum endpoints dynamically:
 * - "/api/enums/business-types" → Business Types
 * - "/api/enums/legal-forms" → Legal Forms
 * - "/api/enums/salutations" → Contact Salutations
 * - "/api/enums/titles" → Contact Titles
 * - "/api/enums/decision-levels" → Contact Decision Levels
 * - ... and any future enum endpoints
 *
 * No need to add new hooks for each enum type!
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
  // Get enumSource from field definition
  // Format: "/api/enums/salutations" or "salutations"
  const enumSourceRaw = (field as { enumSource?: string }).enumSource || '';

  // Build full API path if not already a path
  const apiPath = enumSourceRaw.startsWith('/api/')
    ? enumSourceRaw
    : enumSourceRaw
      ? `/api/enums/${enumSourceRaw}`
      : '';

  // Use generic enum options hook - works with any endpoint
  const { data: options = [], isLoading } = useEnumOptions(apiPath);

  // DESIGN_SYSTEM.md: Prevent MUI warnings for out-of-range values
  // Only use value if options are loaded AND value exists in options
  const valueExists = options.some(opt => opt.value === value);
  const safeValue = isLoading || !valueExists ? '' : value || '';

  return (
    <FormControl fullWidth error={error} disabled={disabled || readOnly} required={required}>
      <Select
        value={safeValue}
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
