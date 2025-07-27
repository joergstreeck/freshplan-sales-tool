/**
 * TextArea Field Component
 * 
 * Renders a multi-line text input field.
 * Used for fieldType: 'textarea'
 */

import React from 'react';
import { TextField as MuiTextField } from '@mui/material';
import type { FieldDefinition } from '../../../types/field.types';

interface TextAreaFieldProps {
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
 * TextArea Field
 * 
 * Multi-line text input for longer text content.
 * Useful for notes, descriptions, or comments.
 */
export const TextAreaField: React.FC<TextAreaFieldProps> = ({
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
      value={value}
      onChange={(e) => onChange(e.target.value)}
      onBlur={onBlur}
      error={error}
      helperText={helperText}
      disabled={disabled}
      required={required}
      placeholder={field.placeholder}
      fullWidth
      size="small"
      variant="outlined"
      multiline
      rows={field.rows || 4}
      inputProps={{
        maxLength: field.maxLength || 1000,
        readOnly,
        'aria-label': field.label,
        'aria-required': required,
        'aria-invalid': error,
        'aria-multiline': true
      }}
      sx={{
        '& .MuiInputBase-root': {
          backgroundColor: readOnly ? 'action.disabledBackground' : 'background.paper'
        }
      }}
    />
  );
};