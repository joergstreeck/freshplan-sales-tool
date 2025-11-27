/**
 * Array Field Component
 *
 * Renders a list of repeatable items with add/remove functionality.
 * Used for fieldType: 'array'
 *
 * Example: deliveryAddresses array contains multiple address objects
 *
 * @see Backend: CustomerSchemaResource.java lines 279-329 (deliveryAddresses ARRAY definition)
 */

import React from 'react';
import { Typography, Paper, Button, IconButton, Stack } from '@mui/material';
import Grid from '@mui/material/Grid';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import type { FieldDefinition } from '../../../types/field.types';
import { TextField } from './TextField';
import { NumberFieldV2 } from './NumberFieldV2';
import { SelectField } from './SelectField';
import { MultiSelectField } from './MultiSelectField';
import { EmailField } from './EmailField';
import { TextAreaField } from './TextAreaField';
import { EnumField } from './EnumField';

/**
 * Helper: Get field key (supports both legacy and new formats)
 * - Legacy: field.key
 * - New (Server-Driven): field.fieldKey
 */
const getFieldKey = (field: FieldDefinition | Record<string, unknown>): string => {
  const f = field as Record<string, unknown>;
  return (f.fieldKey as string) || (f.key as string) || '';
};

interface ArrayFieldProps {
  /** Field definition with itemSchema */
  field: FieldDefinition;
  /** Current array values */
  values: Record<string, unknown>;
  /** Validation errors */
  errors: Record<string, string>;
  /** Change handler for array items */
  onChange: (fieldKey: string, value: unknown) => void;
  /** Blur handler */
  onBlur: (fieldKey: string) => void;
  /** Read-only state */
  readOnly?: boolean;
  /** Disabled state */
  disabled?: boolean;
}

/**
 * Array Field
 *
 * Renders a dynamic list of items based on itemSchema definition.
 * Supports add/remove operations for array items.
 *
 * @example
 * // Backend defines:
 * // deliveryAddresses (ARRAY) → [{ locationName, street, postalCode, city, countryCode, isActive }]
 * <ArrayField
 *   field={deliveryAddressesDefinition}
 *   values={{ deliveryAddresses: [{ locationName: 'Filiale München', street: '...' }] }}
 *   onChange={handleChange}
 * />
 */
export const ArrayField: React.FC<ArrayFieldProps> = ({
  field,
  values,
  errors: _errors,
  onChange,
  onBlur: _onBlur,
  readOnly = false,
  disabled = false,
}) => {
  const fieldKey = getFieldKey(field);

  // ARRAY fields must have itemSchema
  if (!field.itemSchema || field.itemSchema.length === 0) {
    return (
      <Paper
        elevation={0}
        sx={{
          p: 2,
          border: '1px solid',
          borderColor: 'error.main',
          borderRadius: 1,
        }}
      >
        <Typography color="error" variant="body2">
          Fehler: ARRAY-Feld "{fieldKey}" hat kein itemSchema definiert.
        </Typography>
      </Paper>
    );
  }

  // Extract array values
  const arrayValues = (values[fieldKey] as unknown[]) || [];

  // Handle add new item
  const handleAddItem = () => {
    const newItem = {}; // Empty object for new item
    const updatedArray = [...arrayValues, newItem];
    onChange(fieldKey, updatedArray);
  };

  // Handle remove item
  const handleRemoveItem = (index: number) => {
    const updatedArray = arrayValues.filter((_, i) => i !== index);
    onChange(fieldKey, updatedArray);
  };

  // Handle change for item field
  const handleItemChange = (index: number, itemFieldKey: string, value: unknown) => {
    const updatedArray = arrayValues.map((item, i) => {
      if (i === index) {
        return {
          ...(item as Record<string, unknown>),
          [itemFieldKey]: value,
        };
      }
      return item;
    });
    onChange(fieldKey, updatedArray);
  };

  /**
   * Render an item field component based on its type
   */
  const renderItemField = (itemField: FieldDefinition, item: unknown, index: number) => {
    const itemFieldKey = getFieldKey(itemField);
    const itemValue =
      (item as Record<string, unknown>)[itemFieldKey] ?? itemField.defaultValue ?? '';

    // Common props for all item field types
    const commonProps = {
      field: itemField,
      value: itemValue,
      onChange: (newValue: unknown) => handleItemChange(index, itemFieldKey, newValue),
      onBlur: () => {},
      error: false,
      helperText: undefined,
      disabled: disabled || itemField.disabled,
      readOnly: readOnly || itemField.readonly,
      required: itemField.required,
    };

    // Render field component based on type
    switch (itemField.type) {
      case 'TEXT':
        return <TextField {...commonProps} />;

      case 'EMAIL':
        return <EmailField {...commonProps} />;

      case 'NUMBER':
      case 'DECIMAL':
      case 'CURRENCY':
        return <NumberFieldV2 {...commonProps} />;

      case 'SELECT':
        return <SelectField {...commonProps} />;

      case 'MULTISELECT':
        return <MultiSelectField {...commonProps} />;

      case 'TEXTAREA':
        return <TextAreaField {...commonProps} />;

      case 'ENUM':
        return <EnumField {...commonProps} />;

      default:
        // Fallback to TextField for unknown types
        return <TextField {...commonProps} />;
    }
  };

  return (
    <Paper
      elevation={0}
      sx={{
        p: 2,
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 1,
        backgroundColor: 'background.default',
      }}
    >
      {/* Array Label */}
      {field.label && (
        <Typography
          variant="subtitle2"
          sx={{
            mb: 2,
            fontWeight: 600,
            color: 'text.primary',
          }}
        >
          {field.label}
          {field.required && (
            <Typography component="span" color="error" sx={{ ml: 0.5 }}>
              *
            </Typography>
          )}
        </Typography>
      )}

      {/* Array Items */}
      <Stack spacing={2}>
        {arrayValues.length === 0 ? (
          <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
            Noch keine Einträge vorhanden
          </Typography>
        ) : (
          arrayValues.map((item, index) => (
            <Paper
              key={index}
              elevation={1}
              sx={{
                p: 2,
                border: '1px solid',
                borderColor: 'divider',
                position: 'relative',
              }}
            >
              {/* Delete Button */}
              {!readOnly && !disabled && (
                <IconButton
                  size="small"
                  color="error"
                  onClick={() => handleRemoveItem(index)}
                  sx={{
                    position: 'absolute',
                    top: 8,
                    right: 8,
                  }}
                  aria-label={`${field.label} Eintrag ${index + 1} löschen`}
                >
                  <DeleteIcon fontSize="small" />
                </IconButton>
              )}

              {/* Item Title */}
              <Typography variant="caption" color="text.secondary" sx={{ mb: 2, display: 'block' }}>
                Eintrag #{index + 1}
              </Typography>

              {/* Item Fields Grid */}
              <Grid container spacing={2}>
                {field.itemSchema!.map(itemField => {
                  const gridCols = itemField.gridCols || 12;

                  return (
                    <Grid key={getFieldKey(itemField)} size={{ xs: 12, sm: gridCols }}>
                      {renderItemField(itemField, item, index)}
                    </Grid>
                  );
                })}
              </Grid>
            </Paper>
          ))
        )}
      </Stack>

      {/* Add Button */}
      {!readOnly && !disabled && (
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={handleAddItem}
          size="small"
          sx={{ mt: 2 }}
        >
          {field.label} hinzufügen
        </Button>
      )}

      {/* Help Text */}
      {field.helpText && (
        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
          {field.helpText}
        </Typography>
      )}
    </Paper>
  );
};
