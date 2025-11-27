/**
 * Date Field Component
 *
 * Renders a date picker field using MUI DatePicker.
 * Sprint 2.1.7.7: Added for Contact birthday and other date fields.
 */

import React from 'react';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { de } from 'date-fns/locale';
import type { FieldDefinition } from '../../../types/field.types';

interface DateFieldProps {
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
 * Date Field
 *
 * Uses MUI DatePicker with German locale.
 * Value format: ISO date string (YYYY-MM-DD) or Date object
 */
export const DateField: React.FC<DateFieldProps> = ({
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
  // Parse value to Date object
  const dateValue = value ? new Date(value as string) : null;

  // Handle date change
  const handleChange = (newValue: Date | null) => {
    if (newValue) {
      // Format as ISO date string (YYYY-MM-DD)
      const isoDate = newValue.toISOString().split('T')[0];
      onChange(isoDate);
    } else {
      onChange(null);
    }
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={de}>
      <DatePicker
        value={dateValue}
        onChange={handleChange}
        disabled={disabled || readOnly}
        slotProps={{
          textField: {
            fullWidth: true,
            error: error,
            helperText: helperText,
            required: required,
            onBlur: onBlur,
            placeholder: field.placeholder,
          },
        }}
        format="dd.MM.yyyy"
      />
    </LocalizationProvider>
  );
};
