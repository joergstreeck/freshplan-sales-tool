# 🎨 FC-005 FRONTEND - FIELD RENDERING

**Navigation:** [← State Management](02-state-management.md) | [Validation →](04-validation.md)

---

**Datum:** 26.07.2025  
**Version:** 1.0  
**Stack:** React, TypeScript, Material-UI  

## 📋 Inhaltsverzeichnis

1. [Dynamic Field Renderer](#dynamic-field-renderer)
2. [Field Type Components](#field-type-components)
3. [Field Catalog](#field-catalog)
4. [Conditional Rendering](#conditional-rendering)
5. [Custom Field Types](#custom-field-types)

## Dynamic Field Renderer

### Core Rendering Engine

```typescript
// components/fields/DynamicFieldRenderer.tsx
import React from 'react';
import { Grid } from '@mui/material';
import { FieldDefinition, FieldValue } from '../../types/field.types';
import { FieldWrapper } from './FieldWrapper';
import { TextField } from './fieldTypes/TextField';
import { NumberField } from './fieldTypes/NumberField';
import { SelectField } from './fieldTypes/SelectField';
import { DateField } from './fieldTypes/DateField';
import { BooleanField } from './fieldTypes/BooleanField';

interface Props {
  fields: FieldDefinition[];
  values: Map<string, any>;
  errors: Map<string, string>;
  onChange: (fieldKey: string, value: any) => void;
  onBlur: (fieldKey: string) => void;
}

export const DynamicFieldRenderer: React.FC<Props> = ({
  fields,
  values,
  errors,
  onChange,
  onBlur
}) => {
  const renderField = (field: FieldDefinition) => {
    const value = values.get(field.key) ?? field.defaultValue;
    const error = errors.get(field.key);
    
    const commonProps = {
      value,
      onChange: (newValue: any) => onChange(field.key, newValue),
      onBlur: () => onBlur(field.key),
      error: !!error,
      helperText: error || field.helpText,
      disabled: field.isReadOnly
    };
    
    switch (field.fieldType) {
      case 'text':
      case 'email':
      case 'phone':
        return <TextField {...commonProps} field={field} />;
        
      case 'number':
        return <NumberField {...commonProps} field={field} />;
        
      case 'select':
        return <SelectField {...commonProps} field={field} />;
        
      case 'date':
        return <DateField {...commonProps} field={field} />;
        
      case 'boolean':
      case 'checkbox':
        return <BooleanField {...commonProps} field={field} />;
        
      default:
        console.warn(`Unknown field type: ${field.fieldType}`);
        return null;
    }
  };
  
  // Filter visible fields based on conditions
  const visibleFields = fields.filter(field => {
    if (!field.condition) return true;
    return evaluateCondition(field.condition, values);
  });
  
  return (
    <Grid container spacing={3}>
      {visibleFields.map(field => (
        <Grid 
          item 
          xs={12} 
          sm={field.gridSize?.sm || 6} 
          md={field.gridSize?.md || 4}
          key={field.key}
        >
          <FieldWrapper
            field={field}
            hasError={errors.has(field.key)}
          >
            {renderField(field)}
          </FieldWrapper>
        </Grid>
      ))}
    </Grid>
  );
};
```

### Field Wrapper Component

```typescript
// components/fields/FieldWrapper.tsx
interface FieldWrapperProps {
  field: FieldDefinition;
  hasError: boolean;
  children: React.ReactNode;
}

export const FieldWrapper: React.FC<FieldWrapperProps> = ({
  field,
  hasError,
  children
}) => {
  return (
    <Box sx={{ position: 'relative' }}>
      {field.required && (
        <Typography
          component="span"
          sx={{
            position: 'absolute',
            top: -8,
            right: 0,
            color: 'error.main',
            fontSize: '1.2rem'
          }}
        >
          *
        </Typography>
      )}
      
      {children}
      
      {field.tooltip && (
        <Tooltip title={field.tooltip}>
          <IconButton
            size="small"
            sx={{
              position: 'absolute',
              top: 8,
              right: 8
            }}
          >
            <InfoIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      )}
    </Box>
  );
};
```

## Field Type Components

### TextField Component

```typescript
// components/fields/fieldTypes/TextField.tsx
import React from 'react';
import { TextField as MuiTextField, InputAdornment } from '@mui/material';
import { FieldComponentProps } from '../../../types/field.types';
import { useFieldFormatting } from '../../../hooks/useFieldFormatting';

export const TextField: React.FC<FieldComponentProps> = ({
  field,
  value,
  onChange,
  onBlur,
  error,
  helperText,
  disabled
}) => {
  const { format, parse } = useFieldFormatting(field);
  
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const parsedValue = parse(event.target.value);
    onChange(parsedValue);
  };
  
  // Special handling for different text types
  const getInputProps = () => {
    switch (field.fieldType) {
      case 'email':
        return { type: 'email', autoComplete: 'email' };
      case 'phone':
        return { type: 'tel', autoComplete: 'tel' };
      default:
        return { type: 'text' };
    }
  };
  
  return (
    <MuiTextField
      fullWidth
      label={field.label}
      value={format(value) || ''}
      onChange={handleChange}
      onBlur={onBlur}
      error={error}
      helperText={helperText}
      disabled={disabled}
      required={field.required}
      inputProps={{
        maxLength: field.maxLength,
        pattern: field.pattern,
        ...getInputProps()
      }}
      InputProps={{
        startAdornment: field.prefix && (
          <InputAdornment position="start">{field.prefix}</InputAdornment>
        ),
        endAdornment: field.suffix && (
          <InputAdornment position="end">{field.suffix}</InputAdornment>
        )
      }}
    />
  );
};
```

### SelectField Component

```typescript
// components/fields/fieldTypes/SelectField.tsx
export const SelectField: React.FC<FieldComponentProps> = ({
  field,
  value,
  onChange,
  error,
  helperText,
  disabled
}) => {
  const { data: options, isLoading } = useFieldOptions(field);
  
  return (
    <FormControl fullWidth error={error} disabled={disabled || isLoading}>
      <InputLabel required={field.required}>
        {field.label}
      </InputLabel>
      
      <Select
        value={value || ''}
        onChange={(e) => onChange(e.target.value)}
        label={field.label}
      >
        {!field.required && (
          <MenuItem value="">
            <em>Keine Auswahl</em>
          </MenuItem>
        )}
        
        {(options || field.options || []).map(option => (
          <MenuItem 
            key={option.value} 
            value={option.value}
          >
            {option.icon && (
              <ListItemIcon>
                <BranchIcon industry={option.value} />
              </ListItemIcon>
            )}
            <ListItemText primary={option.label} />
          </MenuItem>
        ))}
      </Select>
      
      {helperText && (
        <FormHelperText>{helperText}</FormHelperText>
      )}
    </FormControl>
  );
};
```

### NumberField Component

```typescript
// components/fields/fieldTypes/NumberField.tsx
export const NumberField: React.FC<FieldComponentProps> = ({
  field,
  value,
  onChange,
  onBlur,
  error,
  helperText,
  disabled
}) => {
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = event.target.value;
    
    // Allow empty string for clearing
    if (newValue === '') {
      onChange(null);
      return;
    }
    
    // Parse based on field configuration
    const parsed = field.decimals 
      ? parseFloat(newValue) 
      : parseInt(newValue, 10);
      
    if (!isNaN(parsed)) {
      onChange(parsed);
    }
  };
  
  return (
    <MuiTextField
      fullWidth
      type="number"
      label={field.label}
      value={value ?? ''}
      onChange={handleChange}
      onBlur={onBlur}
      error={error}
      helperText={helperText}
      disabled={disabled}
      required={field.required}
      inputProps={{
        min: field.min,
        max: field.max,
        step: field.step || (field.decimals ? 0.01 : 1)
      }}
      InputProps={{
        startAdornment: field.prefix && (
          <InputAdornment position="start">{field.prefix}</InputAdornment>
        ),
        endAdornment: field.suffix && (
          <InputAdornment position="end">{field.suffix}</InputAdornment>
        )
      }}
    />
  );
};
```

## Field Catalog

### Field Definition Structure

```typescript
// data/fieldCatalog.json
{
  "customer": {
    "base": [
      {
        "key": "companyName",
        "label": "Firmenname",
        "fieldType": "text",
        "required": true,
        "maxLength": 100,
        "gridSize": { "sm": 12, "md": 8 }
      },
      {
        "key": "industry",
        "label": "Branche",
        "fieldType": "select",
        "required": true,
        "options": [
          { "value": "hotel", "label": "Hotel" },
          { "value": "krankenhaus", "label": "Krankenhaus" },
          { "value": "seniorenresidenz", "label": "Seniorenresidenz" },
          { "value": "restaurant", "label": "Restaurant" },
          { "value": "betriebsrestaurant", "label": "Betriebsrestaurant" }
        ],
        "gridSize": { "sm": 12, "md": 4 }
      },
      {
        "key": "chainCustomer",
        "label": "Filialunternehmen",
        "fieldType": "select",
        "required": true,
        "options": [
          { "value": "ja", "label": "Ja" },
          { "value": "nein", "label": "Nein" }
        ],
        "helpText": "Hat Ihr Unternehmen mehrere Standorte?",
        "gridSize": { "sm": 6, "md": 3 }
      }
    ],
    "industrySpecific": {
      "hotel": [
        {
          "key": "starRating",
          "label": "Sterne-Kategorie",
          "fieldType": "select",
          "options": [
            { "value": "1", "label": "1 Stern" },
            { "value": "2", "label": "2 Sterne" },
            { "value": "3", "label": "3 Sterne" },
            { "value": "4", "label": "4 Sterne" },
            { "value": "5", "label": "5 Sterne" }
          ]
        }
      ],
      "krankenhaus": [
        {
          "key": "hospitalType",
          "label": "Krankenhaustyp",
          "fieldType": "select",
          "options": [
            { "value": "university", "label": "Universitätsklinikum" },
            { "value": "general", "label": "Allgemeinkrankenhaus" },
            { "value": "specialized", "label": "Fachklinik" }
          ]
        }
      ]
    }
  }
}
```

## Conditional Rendering

### Condition Evaluation

```typescript
// utils/conditionEvaluator.ts
interface FieldCondition {
  field: string;
  operator: 'equals' | 'notEquals' | 'contains' | 'greaterThan';
  value: any;
}

export const evaluateCondition = (
  condition: FieldCondition | FieldCondition[],
  values: Map<string, any>
): boolean => {
  // Handle array of conditions (AND logic)
  if (Array.isArray(condition)) {
    return condition.every(c => evaluateSingleCondition(c, values));
  }
  
  return evaluateSingleCondition(condition, values);
};

const evaluateSingleCondition = (
  condition: FieldCondition,
  values: Map<string, any>
): boolean => {
  const fieldValue = values.get(condition.field);
  
  switch (condition.operator) {
    case 'equals':
      return fieldValue === condition.value;
      
    case 'notEquals':
      return fieldValue !== condition.value;
      
    case 'contains':
      return Array.isArray(fieldValue) 
        ? fieldValue.includes(condition.value)
        : String(fieldValue).includes(condition.value);
        
    case 'greaterThan':
      return Number(fieldValue) > Number(condition.value);
      
    default:
      return false;
  }
};
```

## Custom Field Types

### Implementation Guide

```typescript
// Custom field type example: ColorPicker
interface ColorPickerFieldProps extends FieldComponentProps {
  field: FieldDefinition & {
    presetColors?: string[];
  };
}

export const ColorPickerField: React.FC<ColorPickerFieldProps> = ({
  field,
  value,
  onChange,
  error,
  helperText
}) => {
  const [showPicker, setShowPicker] = useState(false);
  
  return (
    <Box>
      <Typography variant="caption" color="textSecondary">
        {field.label}
        {field.required && ' *'}
      </Typography>
      
      <Box
        onClick={() => setShowPicker(true)}
        sx={{
          width: '100%',
          height: 40,
          backgroundColor: value || '#FFFFFF',
          border: '1px solid',
          borderColor: error ? 'error.main' : 'divider',
          borderRadius: 1,
          cursor: 'pointer',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}
      >
        <Typography variant="caption">
          {value || 'Farbe wählen'}
        </Typography>
      </Box>
      
      {helperText && (
        <FormHelperText error={error}>{helperText}</FormHelperText>
      )}
      
      <Dialog open={showPicker} onClose={() => setShowPicker(false)}>
        <DialogTitle>Farbe auswählen</DialogTitle>
        <DialogContent>
          <ChromePicker
            color={value || '#FFFFFF'}
            onChange={(color) => onChange(color.hex)}
          />
        </DialogContent>
      </Dialog>
    </Box>
  );
};
```

### Registration

```typescript
// Register custom field types
const CUSTOM_FIELD_TYPES = {
  colorPicker: ColorPickerField,
  fileUpload: FileUploadField,
  richText: RichTextField,
  multiSelect: MultiSelectField
};

// Update DynamicFieldRenderer to include custom types
const renderField = (field: FieldDefinition) => {
  // Check custom types first
  const CustomComponent = CUSTOM_FIELD_TYPES[field.fieldType];
  if (CustomComponent) {
    return <CustomComponent {...commonProps} field={field} />;
  }
  
  // Continue with standard types...
};
```

---

**Parent:** [Frontend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)  
**Related:** [State Management](02-state-management.md) | [Validation](04-validation.md) | [Components](01-components.md)