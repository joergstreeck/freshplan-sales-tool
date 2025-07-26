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
import { Grid } from '@mui/material';
import { FieldDefinition } from '../../types/field.types';
import { FieldWrapper } from './FieldWrapper';
import { TextField } from './fieldTypes/TextField';
import { NumberField } from './fieldTypes/NumberField';
import { SelectField } from './fieldTypes/SelectField';
import { MultiSelectField } from './fieldTypes/MultiSelectField';
import { EmailField } from './fieldTypes/EmailField';
import { TextAreaField } from './fieldTypes/TextAreaField';
import { evaluateCondition } from '../../utils/conditionEvaluator';

interface DynamicFieldRendererProps {
  /** Field definitions to render */
  fields: FieldDefinition[];
  /** Current field values */
  values: Record<string, any>;
  /** Validation errors */
  errors: Record<string, string>;
  /** Field change handler */
  onChange: (fieldKey: string, value: any) => void;
  /** Field blur handler */
  onBlur: (fieldKey: string) => void;
  /** Loading state */
  loading?: boolean;
  /** Read-only mode */
  readOnly?: boolean;
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
  readOnly = false
}) => {
  /**
   * Render a single field based on its type
   */
  const renderField = (field: FieldDefinition): React.ReactElement | null => {
    const value = values[field.key] ?? field.defaultValue ?? '';
    const error = errors[field.key];
    
    // Common props for all field types
    const commonProps = {
      field,
      value,
      onChange: (newValue: any) => onChange(field.key, newValue),
      onBlur: () => onBlur(field.key),
      error: !!error,
      helperText: error || field.helpText,
      disabled: loading || field.disabled,
      readOnly: readOnly || field.readonly,
      required: field.required
    };
    
    // Render based on field type
    switch (field.fieldType) {
      case 'text':
        return <TextField {...commonProps} />;
        
      case 'email':
        return <EmailField {...commonProps} />;
        
      case 'number':
        return <NumberField {...commonProps} />;
        
      case 'select':
        return <SelectField {...commonProps} />;
        
      case 'multiselect':
        return <MultiSelectField {...commonProps} />;
        
      case 'textarea':
        return <TextAreaField {...commonProps} />;
        
      default:
        console.warn(`Unknown field type: ${field.fieldType} for field: ${field.key}`);
        return <TextField {...commonProps} />;
    }
  };
  
  /**
   * Filter visible fields based on conditions
   */
  const visibleFields = fields.filter(field => {
    // Always show fields without conditions
    if (!field.triggerWizardStep) return true;
    
    // For chain customer trigger
    if (field.key === 'chainCustomer') return true;
    
    // Evaluate other conditions (future extension)
    return true;
  });
  
  return (
    <Grid container spacing={3}>
      {visibleFields.map(field => (
        <Grid 
          key={field.key}
          item 
          xs={field.gridSize?.xs || 12}
          sm={field.gridSize?.sm || 6}
          md={field.gridSize?.md || 4}
          lg={field.gridSize?.lg}
        >
          <FieldWrapper field={field}>
            {renderField(field)}
          </FieldWrapper>
        </Grid>
      ))}
    </Grid>
  );
};