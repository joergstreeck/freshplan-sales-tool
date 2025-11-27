/**
 * Text Field Component
 *
 * Renders a text input field with validation support.
 * Used for fieldType: 'text'
 */

import React from 'react';
import { TextField as MuiTextField } from '@mui/material';
import type { FieldDefinition } from '../../../types/field.types';

interface TextFieldProps {
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
 * Helper: Get field key (supports both legacy and Server-Driven formats)
 * - Legacy: field.key
 * - Server-Driven: field.fieldKey
 */
const getFieldKey = (field: FieldDefinition): string => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const f = field as any;
  return f.fieldKey || f.key || '';
};

/**
 * Text Field
 *
 * Standard text input with German locale support.
 * Supports maxLength validation and placeholder text.
 */
export const TextField: React.FC<TextFieldProps> = ({
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
  const fieldKey = getFieldKey(field);

  return (
    <MuiTextField
      id={fieldKey}
      name={fieldKey}
      value={value}
      onChange={(e: React.ChangeEvent<HTMLInputElement>) => onChange(e.target.value)}
      onBlur={onBlur}
      error={error}
      helperText={helperText}
      disabled={disabled}
      required={required}
      placeholder={field.placeholder}
      fullWidth
      size="small"
      variant="outlined"
      inputProps={{
        maxLength: field.maxLength,
        readOnly,
        'aria-label': field.label,
        'aria-required': required,
        'aria-invalid': error,
      }}
      sx={{
        '& .MuiInputBase-root': {
          backgroundColor: readOnly ? 'action.disabledBackground' : 'background.paper',
        },
        // Für lange Texte: Kleinere Schrift bei Bedarf
        '& .MuiInputBase-input': {
          ...(fieldKey === 'companyName' &&
            value.length > 30 && {
              fontSize: '0.875rem',
            }),
          ...(fieldKey === 'companyName' &&
            value.length > 50 && {
              fontSize: '0.8rem',
            }),
        },
      }}
    />
  );
};
