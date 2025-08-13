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
  ListItemText,
} from '@mui/material';
import type { SelectChangeEvent } from '@mui/material/Select';
import { useTheme } from '@mui/material/styles';
import { useDropdownWidth } from '../../../hooks/useDropdownWidth';
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
  required,
}) => {
  const displayValue = value || '';
  const theme = useTheme();

  // Nutze den neuen Hook für automatische Breiten-Berechnung
  const dropdownWidth = useDropdownWidth({
    options: field.options,
    placeholder: field.placeholder,
  });

  return (
    <FormControl
      fullWidth
      size="small"
      error={error}
      required={required}
      disabled={disabled || readOnly}
      className="field-dropdown-auto"
      sx={{
        ...dropdownWidth.style,
        [theme.breakpoints?.down('sm')]: {
          width: '100%',
        },
      }}
    >
      <MuiSelect
        id={field.key}
        name={field.key}
        value={displayValue}
        onChange={(e: SelectChangeEvent) => onChange(e.target.value)}
        onBlur={onBlur}
        displayEmpty
        renderValue={selected => {
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
          'aria-invalid': error,
        }}
        sx={{
          backgroundColor: readOnly ? 'action.disabledBackground' : 'background.paper',
          '& .MuiSelect-select': {
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            paddingRight: '32px', // Platz für den Dropdown-Pfeil
          },
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
        {field.options?.map(option => (
          <MenuItem
            key={option.value}
            value={option.value}
            disabled={option.disabled}
            sx={{
              whiteSpace: 'normal', // Erlaube Zeilenumbruch in der Liste
              minHeight: 'auto',
              '& .MuiListItemText-primary': {
                whiteSpace: 'normal',
              },
            }}
          >
            <ListItemText primary={option.label} />
          </MenuItem>
        ))}
      </MuiSelect>

      {helperText && <FormHelperText>{helperText}</FormHelperText>}
    </FormControl>
  );
};
