/**
 * Multi-Select Field Component
 *
 * Renders a multi-select dropdown with checkboxes.
 * Used for fieldType: 'multiselect'
 */

import React from 'react';
import {
  FormControl,
  Select as MuiSelect,
  MenuItem,
  FormHelperText,
  Checkbox,
  ListItemText,
  Chip,
  Box,
} from '@mui/material';
import type { FieldDefinition } from '../../../types/field.types';

interface MultiSelectFieldProps {
  /** Field definition */
  field: FieldDefinition;
  /** Current values array */
  value: string[];
  /** Change handler */
  onChange: (value: string[]) => void;
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
 * Multi-Select Field
 *
 * Multiple selection dropdown with checkboxes and chip display.
 * Ideal for selecting multiple options like operating days or care levels.
 */
export const MultiSelectField: React.FC<MultiSelectFieldProps> = ({
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
  const selectedValues = value || [];

  const handleChange = (event: any) => {
    const newValue = event.target.value;
    onChange(typeof newValue === 'string' ? newValue.split(',') : newValue);
  };

  const renderValue = (selected: string[]) => {
    if (selected.length === 0) {
      return (
        <span style={{ color: 'rgba(0, 0, 0, 0.38)' }}>
          {field.placeholder || 'Bitte wählen...'}
        </span>
      );
    }

    return (
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
        {selected.map(value => {
          const option = field.options?.find(opt => opt.value === value);
          return (
            <Chip key={value} label={option?.label || value} size="small" sx={{ height: 24 }} />
          );
        })}
      </Box>
    );
  };

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
        multiple
        value={selectedValues}
        onChange={handleChange}
        onBlur={onBlur}
        displayEmpty
        renderValue={renderValue}
        inputProps={{
          'aria-label': field.label,
          'aria-required': required,
          'aria-invalid': error,
        }}
        MenuProps={{
          PaperProps: {
            style: {
              maxHeight: 48 * 4.5 + 8,
              width: 250,
            },
          },
        }}
        sx={{
          backgroundColor: readOnly ? 'action.disabledBackground' : 'background.paper',
          '& .MuiSelect-select': {
            paddingTop: 1,
            paddingBottom: 1,
            minHeight: 'auto',
          },
        }}
      >
        {/* Placeholder option */}
        <MenuItem value="" disabled>
          <ListItemText
            primary={field.placeholder || 'Mehrere auswählen...'}
            sx={{ color: 'text.disabled' }}
          />
        </MenuItem>

        {/* Regular options with checkboxes */}
        {field.options?.map(option => (
          <MenuItem key={option.value} value={option.value} disabled={option.disabled}>
            <Checkbox checked={selectedValues.indexOf(option.value) > -1} size="small" />
            <ListItemText primary={option.label} />
          </MenuItem>
        ))}
      </MuiSelect>

      {helperText && <FormHelperText>{helperText}</FormHelperText>}
    </FormControl>
  );
};
