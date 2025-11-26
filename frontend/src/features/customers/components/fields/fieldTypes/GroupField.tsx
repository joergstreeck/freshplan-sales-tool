/**
 * Group Field Component
 *
 * Renders nested fields in a group structure with visual containment.
 * Used for fieldType: 'group'
 *
 * Example: mainAddress group contains street, postalCode, city, countryCode fields
 *
 * @see Backend: CustomerSchemaResource.java lines 239-277 (mainAddress GROUP definition)
 */

import React from 'react';
import { Typography, Paper } from '@mui/material';
import Grid from '@mui/material/Grid';
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

interface GroupFieldProps {
  /** Field definition with nested fields */
  field: FieldDefinition;
  /** Current values for all fields in the group */
  values: Record<string, unknown>;
  /** Validation errors */
  errors: Record<string, string>;
  /** Change handler for nested fields */
  onChange: (fieldKey: string, value: unknown) => void;
  /** Blur handler for nested fields */
  onBlur: (fieldKey: string) => void;
  /** Read-only state */
  readOnly?: boolean;
  /** Disabled state */
  disabled?: boolean;
}

/**
 * Group Field
 *
 * Renders nested fields in a visually grouped container.
 * Supports recursive nesting of field types.
 *
 * @example
 * // Backend defines:
 * // mainAddress (GROUP) → street, postalCode, city, countryCode
 * <GroupField
 *   field={mainAddressDefinition}
 *   values={{ street: 'Hauptstr. 1', postalCode: '80331', city: 'München', countryCode: 'DEU' }}
 *   onChange={handleChange}
 * />
 */
export const GroupField: React.FC<GroupFieldProps> = ({
  field,
  values,
  errors,
  onChange,
  onBlur,
  readOnly = false,
  disabled = false,
}) => {
  const fieldKey = getFieldKey(field);

  // GROUP fields must have nested fields
  if (!field.fields || field.fields.length === 0) {
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
          Fehler: GROUP-Feld "{fieldKey}" hat keine verschachtelten Felder definiert.
        </Typography>
      </Paper>
    );
  }

  // Extract group-specific values from the parent value object
  const groupValues = (values[fieldKey] as Record<string, unknown>) || {};

  // Handle change for nested field
  const handleNestedChange = (nestedFieldKey: string, value: unknown) => {
    const updatedGroupValue = {
      ...groupValues,
      [nestedFieldKey]: value,
    };
    onChange(fieldKey, updatedGroupValue);
  };

  // Handle blur for nested field
  const handleNestedBlur = (nestedFieldKey: string) => {
    onBlur(`${fieldKey}.${nestedFieldKey}`);
  };

  /**
   * Render a nested field component based on its type
   */
  const renderNestedField = (nestedField: FieldDefinition) => {
    const nestedFieldKey = getFieldKey(nestedField);
    const nestedValue = groupValues[nestedFieldKey] ?? nestedField.defaultValue ?? '';
    const nestedError = errors[`${fieldKey}.${nestedFieldKey}`];

    // Common props for all nested field types
    const commonProps = {
      field: nestedField,
      value: nestedValue,
      onChange: (newValue: unknown) => handleNestedChange(nestedFieldKey, newValue),
      onBlur: () => handleNestedBlur(nestedFieldKey),
      error: !!nestedError,
      helperText: nestedError || undefined,
      disabled: disabled || nestedField.disabled,
      readOnly: readOnly || nestedField.readonly,
      required: nestedField.required,
    };

    // Render field component based on type
    switch (nestedField.type) {
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
      {/* Group Label */}
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

      {/* Nested Fields Grid */}
      <Grid container spacing={2}>
        {field.fields.map(nestedField => {
          const gridCols = nestedField.gridCols || 12;

          return (
            <Grid key={getFieldKey(nestedField)} size={{ xs: 12, md: gridCols }}>
              {renderNestedField(nestedField)}
            </Grid>
          );
        })}
      </Grid>

      {/* Help Text */}
      {field.helpText && (
        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
          {field.helpText}
        </Typography>
      )}
    </Paper>
  );
};
