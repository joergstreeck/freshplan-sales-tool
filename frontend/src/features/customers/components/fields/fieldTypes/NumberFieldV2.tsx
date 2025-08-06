/**
 * Number Field V2 Component
 * 
 * Erweiterte Version mit EUR-Formatierung und Live-Calculator Support.
 * Renders a numeric input field with min/max validation and currency formatting.
 */

import React from 'react';
import { TextField as MuiTextField, InputAdornment } from '@mui/material';
import { EURInput } from '../EURInput';
import type { FieldDefinition } from '../../../types/field.types';

interface NumberFieldV2Props {
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
 * Number Field V2
 * 
 * Numeric input with currency formatting support.
 * Automatically uses EURInput for currency fields.
 */
export const NumberFieldV2: React.FC<NumberFieldV2Props> = ({
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
  // Check if this is a currency field
  const isCurrencyField = field.format === 'currency' && field.currency === 'EUR';
  
  // Use EURInput for currency fields
  if (isCurrencyField) {
    return (
      <EURInput
        value={typeof value === 'number' ? value : null}
        onChange={(newValue) => onChange(newValue)}
        onBlur={onBlur}
        label={field.label}
        required={required}
        error={error}
        helperText={helperText || field.helpText}
        showCalculator={field.showCalculator}
        disabled={disabled || readOnly}
        fullWidth={field.width !== 'large'}
        calculatorHint={field.calculatorHint}
        validationWarning={field.validationWarning}
      />
    );
  }
  
  // Standard number field handling
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
    if (field.suffix) return field.suffix;
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
      helperText={helperText || field.helpText}
      disabled={disabled}
      required={required}
      placeholder={field.placeholder}
      fullWidth
      size="small"
      variant="outlined"
      InputProps={{
        endAdornment: suffix ? (
          <InputAdornment position="end">{suffix}</InputAdornment>
        ) : undefined,
        sx: field.fontSize ? {
          '& input': {
            fontSize: field.fontSize,
            textAlign: field.textAlign || 'left'
          }
        } : undefined
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