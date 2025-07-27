/**
 * Email Field Component
 * 
 * Specialized text field for email addresses with validation.
 * Used for fieldType: 'email'
 */

import React from 'react';
import { TextField as MuiTextField, InputAdornment } from '@mui/material';
import { Email as EmailIcon } from '@mui/icons-material';
import type { FieldDefinition } from '../../../types/field.types';

interface EmailFieldProps {
  /** Field definition */
  field: FieldDefinition;
  /** Current value */
  value: string;
  /** Change handler */
  onChange: (value: string) => void;
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
 * Email Field
 * 
 * Email input with built-in validation and icon.
 * Validates email format on blur.
 */
export const EmailField: React.FC<EmailFieldProps> = ({
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
  return (
    <MuiTextField
      id={field.key}
      name={field.key}
      type="email"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      onBlur={onBlur}
      error={error}
      helperText={helperText}
      disabled={disabled}
      required={required}
      placeholder={field.placeholder || 'beispiel@firma.de'}
      fullWidth
      size="small"
      variant="outlined"
      InputProps={{
        startAdornment: (
          <InputAdornment position="start">
            <EmailIcon fontSize="small" color={error ? 'error' : 'action'} />
          </InputAdornment>
        )
      }}
      inputProps={{
        maxLength: field.maxLength || 100,
        readOnly,
        'aria-label': field.label,
        'aria-required': required,
        'aria-invalid': error,
        autoComplete: 'email'
      }}
      sx={{
        '& .MuiInputBase-root': {
          backgroundColor: readOnly ? 'action.disabledBackground' : 'background.paper'
        }
      }}
    />
  );
};