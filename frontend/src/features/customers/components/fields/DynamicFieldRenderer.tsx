/**
 * Dynamic Field Renderer Component
 *
 * Core rendering engine for the Field-Based Architecture.
 * Renders fields dynamically based on their type and configuration.
 *
 * COMPATIBILITY: Supports both legacy (field.key) and new (field.fieldKey) formats
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
import { EnumField } from './fieldTypes/EnumField';
import { GroupField } from './fieldTypes/GroupField';
import { ArrayField } from './fieldTypes/ArrayField';
import { BooleanField } from './fieldTypes/BooleanField';
import { getVisibleFields } from '../../utils/conditionEvaluator';
import { getFieldSize } from '../../utils/fieldSizeCalculator';
import { useCustomerFieldTheme } from '../../theme';

/**
 * Helper: Get field key (supports both legacy and new formats)
 * - Legacy: field.key
 * - New (Server-Driven): field.fieldKey
 */
const getFieldKey = (field: FieldDefinition | Record<string, unknown>): string => {
  const f = field as Record<string, unknown>;
  return (f.fieldKey as string) || (f.key as string) || '';
};

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
  /** Force adaptive layout override (true=adaptive, false=grid, undefined=theme) */
  useAdaptiveLayout?: boolean;
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
  useAdaptiveLayout: useAdaptiveLayoutProp,
}) => {
  const { theme } = useCustomerFieldTheme();
  // Prop überschreibt Theme-Einstellung (undefined = Theme-basiert)
  const useAdaptiveLayout = useAdaptiveLayoutProp ?? theme.darstellung === 'anpassungsfähig';

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
    const fieldKey = getFieldKey(field);
    const value = values[fieldKey] ?? field.defaultValue ?? '';
    const error = errors[fieldKey];

    // Für adaptive Felder (alle Text-basierten Typen)
    if (useAdaptiveLayout && ['TEXT', 'EMAIL', 'NUMBER'].includes(field.type)) {
      return (
        <FieldWrapper field={field} error={error}>
          <AdaptiveField
            field={field}
            value={value}
            onChange={newValue => onChange(fieldKey, newValue)}
            onBlur={() => onBlur(fieldKey)}
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
      onChange: (newValue: unknown) => onChange(fieldKey, newValue),
      onBlur: () => onBlur(fieldKey),
      error: !!error,
      helperText: error || undefined,
      disabled: loading || field.disabled,
      readOnly: readOnly || field.readonly,
      required: field.required,
    };

    // Render field component based on type
    let fieldComponent: React.ReactElement;

    switch (field.type) {
      case 'TEXT':
        fieldComponent = <TextField {...commonProps} />;
        break;

      case 'EMAIL':
        fieldComponent = <EmailField {...commonProps} />;
        break;

      case 'NUMBER':
      case 'DECIMAL':
      case 'CURRENCY':
        fieldComponent = <NumberFieldV2 {...commonProps} />;
        break;

      case 'SELECT':
        fieldComponent = <SelectField {...commonProps} />;
        break;

      case 'MULTISELECT':
        fieldComponent = <MultiSelectField {...commonProps} />;
        break;

      case 'TEXTAREA':
        fieldComponent = <TextAreaField {...commonProps} />;
        break;

      case 'ENUM':
        fieldComponent = <EnumField {...commonProps} />;
        break;

      case 'BOOLEAN':
        // BooleanField hat eigenes Label via FormControlLabel - kein FieldWrapper nötig!
        return <BooleanField {...commonProps} />;

      case 'GROUP':
        fieldComponent = (
          <GroupField
            field={field}
            values={values}
            errors={errors}
            onChange={onChange}
            onBlur={onBlur}
            readOnly={readOnly || field.readonly}
            disabled={loading || field.disabled}
          />
        );
        break;

      case 'ARRAY':
        fieldComponent = (
          <ArrayField
            field={field}
            values={values}
            errors={errors}
            onChange={onChange}
            onBlur={onBlur}
            readOnly={readOnly || field.readonly}
            disabled={loading || field.disabled}
          />
        );
        break;

      default:
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
        {visibleFields.map((field, index) => {
          // Spezielle CSS-Klasse für Dropdowns zur automatischen Breitenberechnung
          const sizeClass =
            field.type === 'SELECT' || field.type === 'DROPDOWN'
              ? 'field-dropdown-auto'
              : (() => {
                  const sizeInfo = getFieldSize(field);
                  const sizeCategory = getSizeCategoryFromGrid(sizeInfo.md || 6);
                  return `field-${sizeMap[sizeCategory] || 'mittel'}`;
                })();

          const style: React.CSSProperties = {};

          return (
            <Box
              key={getFieldKey(field) || `field-adaptive-${index}`}
              className={sizeClass}
              sx={style}
            >
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
      {visibleFields.map((field, index) => {
        const themeSize = getFieldSize(field);
        const gridSize = themeSize;

        return (
          <Grid
            key={getFieldKey(field) || `field-grid-${index}`}
            size={{ xs: gridSize.xs, sm: gridSize.sm, md: gridSize.md }}
          >
            {renderField(field)}
          </Grid>
        );
      })}
    </Grid>
  );
};
