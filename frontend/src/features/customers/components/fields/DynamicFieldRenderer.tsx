/**
 * Dynamic Field Renderer Component
 *
 * Core rendering engine for the Field-Based Architecture.
 * Renders fields dynamically based on their type and configuration.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/03-field-rendering.md
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json
 */

import React from 'react';
import { Grid, Box } from '@mui/material';
import type { FieldDefinition } from '../../types/field.types';
import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';
import { AdaptiveField } from '../adaptive/AdaptiveField';
import { FieldWrapper } from './FieldWrapper';
import { TextField } from './fieldTypes/TextField';
import { NumberFieldV2 } from './fieldTypes/NumberFieldV2';
import { SelectField } from './fieldTypes/SelectField';
import { MultiSelectField } from './fieldTypes/MultiSelectField';
import { EmailField } from './fieldTypes/EmailField';
import { TextAreaField } from './fieldTypes/TextAreaField';
import { getVisibleFields } from '../../utils/conditionEvaluator';
import { getFieldSize } from '../../utils/fieldSizeCalculator';
import { useCustomerFieldTheme } from '../../theme';

interface DynamicFieldRendererProps {
  /** Field definitions to render */
  fields: FieldDefinition[];
  /** Current field values */
  values: Record<string, unknown>;
  /** Validation errors */
  errors: Record<string, string>;
  /** Field change handler */
  onChange: (fieldKey: string, value: unknown) => void;
  /** Field blur handler */
  onBlur: (fieldKey: string) => void;
  /** Loading state */
  loading?: boolean;
  /** Read-only mode */
  readOnly?: boolean;
  /** Current wizard step (for step-based filtering) */
  currentStep?: string;
}

/**
 * Dynamic Field Renderer
 *
 * Renders fields based on their type definition from the Field Catalog.
 * Supports conditional rendering, validation, and responsive layouts.
 */
export const DynamicFieldRenderer: React.FC<DynamicFieldRendererProps> = ({
  fields,
  values,
  errors,
  onChange,
  onBlur,
  loading = false,
  readOnly = false,
  currentStep,
}) => {
  const { theme } = useCustomerFieldTheme();
  const useAdaptiveLayout = theme.darstellung === 'anpassungsfähig';

  // Size mapping für deutsche CSS-Klassen
  const sizeMap: Record<string, string> = {
    compact: 'kompakt',
    small: 'klein',
    medium: 'mittel',
    large: 'groß',
    full: 'voll',
  };

  /**
   * Render a single field based on its type
   */
  const renderField = (field: FieldDefinition): React.ReactElement | null => {
    const value = values[field.key] ?? field.defaultValue ?? '';
    const error = errors[field.key];

    // Für adaptive Felder (alle Text-basierten Typen)
    if (useAdaptiveLayout && ['text', 'email', 'number'].includes(field.fieldType)) {
      return (
        <FieldWrapper field={field} error={error}>
          <AdaptiveField
            field={field}
            value={value}
            onChange={newValue => onChange(field.key, newValue)}
            onBlur={() => onBlur(field.key)}
            error={error}
            disabled={loading || field.disabled}
            readOnly={readOnly || field.readonly}
          />
        </FieldWrapper>
      );
    }

    // Common props for all field types
    const commonProps = {
      field,
      value,
      onChange: (newValue: unknown) => onChange(field.key, newValue),
      onBlur: () => onBlur(field.key),
      error: !!error,
      helperText: error || undefined,
      disabled: loading || field.disabled,
      readOnly: readOnly || field.readonly,
      required: field.required,
    };

    // Render field component based on type
    let fieldComponent: React.ReactElement;

    switch (field.fieldType) {
      case 'text':
        fieldComponent = <TextField {...commonProps} />;
        break;

      case 'email':
        fieldComponent = <EmailField {...commonProps} />;
        break;

      case 'number':
        fieldComponent = <NumberFieldV2 {...commonProps} />;
        break;

      case 'select':
        fieldComponent = <SelectField {...commonProps} />;
        break;

      case 'multiselect':
        fieldComponent = <MultiSelectField {...commonProps} />;
        break;

      case 'textarea':
        fieldComponent = <TextAreaField {...commonProps} />;
        break;

      default:
        console.warn(`Unknown field type: ${field.fieldType} for field: ${field.key}`);
        fieldComponent = <TextField {...commonProps} />;
    }

    // Wrap ALL field types with FieldWrapper for consistent labels and icons
    return (
      <FieldWrapper field={field} error={error}>
        {fieldComponent}
      </FieldWrapper>
    );
  };

  /**
   * Filter visible fields based on conditions
   *
   * Uses the sophisticated condition evaluator to handle:
   * - Generic field conditions (condition property)
   * - Wizard step filtering (currentStep parameter)
   * - Always shows fields without conditions
   *
   * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/utils/conditionEvaluator.ts
   */
  const visibleFields = getVisibleFields(fields, values, currentStep);

  // Helper-Funktion für Size-Kategorie Mapping
  const getSizeCategoryFromGrid = (gridSize: number): string => {
    if (gridSize <= 2) return 'compact';
    if (gridSize <= 3) return 'small';
    if (gridSize <= 6) return 'medium';
    if (gridSize <= 10) return 'large';
    return 'full';
  };

  // Verwende adaptive Layout wenn aktiviert
  if (useAdaptiveLayout) {
    return (
      <AdaptiveFormContainer variant="flexbox">
        {visibleFields.map(field => {
          // Spezielle CSS-Klasse für Dropdowns zur automatischen Breitenberechnung
          const sizeClass =
            field.fieldType === 'select' || field.fieldType === 'dropdown'
              ? 'field-dropdown-auto'
              : (() => {
                  const sizeInfo = getFieldSize(field);
                  const sizeCategory = getSizeCategoryFromGrid(sizeInfo.md || 6);
                  return `field-${sizeMap[sizeCategory] || 'mittel'}`;
                })();

          const style: React.CSSProperties = {};

          return (
            <Box key={field.key} className={sizeClass} sx={style}>
              {renderField(field)}
            </Box>
          );
        })}
      </AdaptiveFormContainer>
    );
  }

  // Traditionelles Grid Layout
  return (
    <Grid container spacing={3}>
      {visibleFields.map(field => {
        const themeSize = getFieldSize(field);
        const gridSize = themeSize;

        return (
          <Grid key={field.key} size={{ xs: gridSize.xs, sm: gridSize.sm, md: gridSize.md }}>
            {renderField(field)}
          </Grid>
        );
      })}
    </Grid>
  );
};
