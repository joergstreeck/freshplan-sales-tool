/**
 * Boolean Field Component
 *
 * Renders a checkbox for boolean fields (like Pain Points).
 * Best Practice: Checkboxes instead of text inputs for true/false values.
 */

import React from 'react';
import { FormControlLabel, Checkbox, FormHelperText, Box } from '@mui/material';
import type { FieldDefinition } from '../../../types/field.types';

interface BooleanFieldProps {
  /** Field definition */
  field: FieldDefinition;
  /** Current value */
  value: boolean;
  /** Change handler */
  onChange: (value: boolean) => void;
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
 * Boolean Field
 *
 * Renders a checkbox with label for boolean values.
 * Used for Pain Points, flags, and other yes/no fields.
 */
export const BooleanField: React.FC<BooleanFieldProps> = ({
  field,
  value,
  onChange,
  onBlur,
  error,
  helperText,
  disabled,
  readOnly,
}) => {
  const fieldKey = field.key || field.fieldKey || `boolean-field-${Date.now()}`;

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange(event.target.checked);
  };

  return (
    <Box>
      <FormControlLabel
        control={
          <Checkbox
            id={fieldKey}
            name={fieldKey}
            checked={Boolean(value)}
            onChange={handleChange}
            onBlur={onBlur}
            disabled={disabled || readOnly}
            color="primary"
            sx={{
              '&.Mui-checked': {
                color: 'primary.main',
              },
            }}
          />
        }
        label={field.label || ''}
        sx={{
          '& .MuiFormControlLabel-label': {
            color: error ? 'error.main' : 'text.primary',
          },
        }}
      />
      {(helperText || field.helpText) && (
        <FormHelperText error={error} sx={{ ml: 4 }}>
          {helperText || field.helpText}
        </FormHelperText>
      )}
    </Box>
  );
};
