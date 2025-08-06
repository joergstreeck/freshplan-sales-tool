/**
 * Number Field Component
 * 
 * Renders a numeric input field with min/max validation.
 * Used for fieldType: 'number'
 */

import React from 'react';
import { TextField as MuiTextField, InputAdornment } from '@mui/material';
import type { FieldDefinition } from '../../../types/field.types';

interface NumberFieldProps {
  /** Field definition */
  field: FieldDefinition;
  /** Current value */
  value: number | '';
  /** Change handler */
  onChange: (value: number | '') => void;
  /** Blur handler */
  onBlur: () => void;
  /** Error state */
  error?: boolean;
  /** Helper/error text */
  helperText?: string;
  /** Disabled state */
  disabled?: boolean;
  /** Read-only state */
  readOnly?: boolean;
  /** Required field */
  required?: boolean;
}

/**
 * Number Field
 * 
 * Numeric input with min/max validation and German number formatting.
 * Prevents non-numeric input.
 */
export const NumberField: React.FC<NumberFieldProps> = ({
  field,
  value,
  onChange,
  onBlur,
  error,
  helperText,
  disabled,
  readOnly,
  required
}) => {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = e.target.value;
    
    // Allow empty value
    if (inputValue === '') {
      onChange('');
      return;
    }
    
    // Parse and validate number
    const numValue = parseFloat(inputValue);
    if (!isNaN(numValue)) {
      // Apply min/max constraints
      let constrainedValue = numValue;
      if (field.min !== undefined && numValue < field.min) {
        constrainedValue = field.min;
      }
      if (field.max !== undefined && numValue > field.max) {
        constrainedValue = field.max;
      }
      onChange(constrainedValue);
    }
  };

  // Get appropriate suffix based on field key
  const getSuffix = () => {
    if (field.key.includes('Count') || field.key.includes('Anzahl')) return '';
    if (field.key.includes('Capacity')) return 'pro Tag';
    if (field.key.includes('percent') || field.key.includes('Anteil')) return '%';
    return '';
  };

  const suffix = getSuffix();

  return (
    <MuiTextField
      id={field.key}
      name={field.key}
      type="number"
      value={value}
      onChange={handleChange}
      onBlur={onBlur}
      error={error}
      helperText={helperText}
      disabled={disabled}
      required={required}
      placeholder={field.placeholder}
      fullWidth
      size="small"
      variant="outlined"
      InputProps={{
        endAdornment: suffix ? (
          <InputAdornment position="end">{suffix}</InputAdornment>
        ) : undefined
      }}
      inputProps={{
        min: field.min,
        max: field.max,
        step: 1,
        readOnly,
        'aria-label': field.label,
        'aria-required': required,
        'aria-invalid': error,
        'aria-valuemin': field.min,
        'aria-valuemax': field.max
      }}
      sx={{
        '& .MuiInputBase-root': {
          backgroundColor: readOnly ? 'action.disabledBackground' : 'background.paper'
        },
        // Hide number spinner in some browsers
        '& input[type=number]::-webkit-inner-spin-button': {
          WebkitAppearance: 'none',
          margin: 0
        },
        '& input[type=number]::-webkit-outer-spin-button': {
          WebkitAppearance: 'none',
          margin: 0
        }
      }}
    />
  );
};