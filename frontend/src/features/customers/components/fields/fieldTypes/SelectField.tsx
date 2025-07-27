/**
 * Select Field Component
 * 
 * Renders a dropdown select field with predefined options.
 * Used for fieldType: 'select'
 */

import React from 'react';
import { 
  FormControl, 
  Select as MuiSelect, 
  MenuItem, 
  FormHelperText,
  ListItemText
} from '@mui/material';
import type { FieldDefinition } from '../../../types/field.types';

interface SelectFieldProps {
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
 * Select Field
 * 
 * Dropdown select with Material-UI styling.
 * Supports placeholder and disabled options.
 */
export const SelectField: React.FC<SelectFieldProps> = ({
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
  const displayValue = value || '';

  return (
    <FormControl 
      fullWidth 
      size="small" 
      error={error}
      required={required}
      disabled={disabled || readOnly}
    >
      <MuiSelect
        id={field.key}
        name={field.key}
        value={displayValue}
        onChange={(e) => onChange(e.target.value as string)}
        onBlur={onBlur}
        displayEmpty
        renderValue={(selected) => {
          if (!selected) {
            return (
              <span style={{ color: 'rgba(0, 0, 0, 0.38)' }}>
                {field.placeholder || 'Bitte wählen...'}
              </span>
            );
          }
          const option = field.options?.find(opt => opt.value === selected);
          return option?.label || selected;
        }}
        inputProps={{
          'aria-label': field.label,
          'aria-required': required,
          'aria-invalid': error
        }}
        sx={{
          backgroundColor: readOnly ? 'action.disabledBackground' : 'background.paper'
        }}
      >
        {/* Placeholder option */}
        <MenuItem value="" disabled>
          <ListItemText 
            primary={field.placeholder || 'Bitte wählen...'} 
            sx={{ color: 'text.disabled' }}
          />
        </MenuItem>
        
        {/* Regular options */}
        {field.options?.map((option) => (
          <MenuItem 
            key={option.value} 
            value={option.value}
            disabled={option.disabled}
          >
            <ListItemText primary={option.label} />
          </MenuItem>
        ))}
      </MuiSelect>
      
      {helperText && (
        <FormHelperText>{helperText}</FormHelperText>
      )}
    </FormControl>
  );
};