/**
 * Step 3: Ansprechpartner V3
 * 
 * Field-basierte Implementierung mit DynamicFieldRenderer für konsistentes Layout.
 * Nutzt die existierenden Field-Definitionen aus fieldCatalogExtensions.json.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER_V2.md
 */

import React, { useCallback, useMemo } from 'react';
import { 
  Box, 
  Typography,
  Autocomplete,
  TextField
} from '@mui/material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { FieldDefinition } from '../../types/field.types';

// Vordefinierte Titel-Optionen (nur Dr. und Prof.)
const TITLE_OPTIONS = ['Dr.', 'Prof.'];

// Vordefinierte Positionen/Funktionen (nur männliche Form)
const POSITION_OPTIONS = [
  // Management
  'Geschäftsführer',
  'Direktor',
  'Inhaber',
  'Vorstand',
  
  // Hotel-spezifisch
  'Hoteldirektor',
  'F&B Manager',
  'Küchenchef',
  'Einkaufsleiter',
  'Betriebsleiter',
  
  // Krankenhaus-spezifisch
  'Verwaltungsdirektor',
  'Küchenleitung',
  'Verpflegungsmanager',
  
  // Betriebsrestaurant
  'Kantinenchef',
  'Gastronomiemanager',
  
  // Allgemein
  'Einkäufer',
  'Prokurist',
  'Assistent der Geschäftsführung'
];

export const Step3AnsprechpartnerV3: React.FC = () => {
  const {
    customerData,
    validationErrors,
    setCustomerField,
    validateField
  } = useCustomerOnboardingStore();
  
  const { getFieldByKey, getFieldsByCategory } = useFieldDefinitions();
  
  // Hole die Contact-Felder aus der Field Catalog Extension
  const contactFields = useMemo(() => {
    // Die wichtigsten Kontaktfelder für die erste Zeile
    const nameFields = [
      'salutation',
      'title', 
      'firstName',
      'lastName'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
    
    // Position und Entscheider-Ebene
    const roleFields = [
      'position',
      'decisionLevel'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
    
    // Kontaktdaten
    const contactInfoFields = [
      'contactEmail',
      'contactPhone',
      'contactMobile'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
    
    return {
      nameFields,
      roleFields,
      contactInfoFields
    };
  }, [getFieldByKey]);
  
  const handleFieldChange = useCallback((fieldKey: string, value: any) => {
    setCustomerField(fieldKey, value);
  }, [setCustomerField]);
  
  const handleFieldBlur = useCallback((fieldKey: string) => {
    validateField(fieldKey);
  }, [validateField]);
  
  // Custom Component für Titel mit Autocomplete
  const TitleAutocomplete = useCallback(() => {
    return (
      <Autocomplete
        value={customerData.title || ''}
        onChange={(_, newValue) => handleFieldChange('title', newValue || '')}
        onInputChange={(_, newValue) => handleFieldChange('title', newValue)}
        options={TITLE_OPTIONS}
        freeSolo
        fullWidth
        size="small"
        renderInput={(params) => (
          <TextField 
            {...params} 
            label="Titel" 
            placeholder="Dr., Prof."
            error={!!validationErrors.title}
            helperText={validationErrors.title}
            onBlur={() => handleFieldBlur('title')}
          />
        )}
      />
    );
  }, [customerData.title, validationErrors.title, handleFieldChange, handleFieldBlur]);
  
  // Custom Component für Position mit Autocomplete
  const PositionAutocomplete = useCallback(() => {
    return (
      <Autocomplete
        value={customerData.position || ''}
        onChange={(_, newValue) => handleFieldChange('position', newValue || '')}
        onInputChange={(_, newValue) => handleFieldChange('position', newValue)}
        options={POSITION_OPTIONS}
        freeSolo
        fullWidth
        size="small"
        renderInput={(params) => (
          <TextField 
            {...params} 
            label="Position/Funktion" 
            placeholder="z.B. Geschäftsführer, Einkaufsleiter"
            required
            error={!!validationErrors.position}
            helperText={validationErrors.position}
            onBlur={() => handleFieldBlur('position')}
          />
        )}
      />
    );
  }, [customerData.position, validationErrors.position, handleFieldChange, handleFieldBlur]);
  
  // Erweitere die Field-Definitionen mit custom components
  const enhancedNameFields = useMemo(() => {
    return contactFields.nameFields.map(field => {
      if (field.key === 'title') {
        return {
          ...field,
          customComponent: TitleAutocomplete
        };
      }
      return field;
    });
  }, [contactFields.nameFields, TitleAutocomplete]);
  
  const enhancedRoleFields = useMemo(() => {
    return contactFields.roleFields.map(field => {
      if (field.key === 'position') {
        return {
          ...field,
          customComponent: PositionAutocomplete
        };
      }
      return field;
    });
  }, [contactFields.roleFields, PositionAutocomplete]);
  
  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Schritt 3: Ansprechpartner
      </Typography>
      
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Erfassen Sie den wichtigsten Ansprechpartner für eine erfolgreiche Zusammenarbeit.
      </Typography>
      
      {/* Name Fields */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="subtitle2" gutterBottom>
          Persönliche Angaben
        </Typography>
        <DynamicFieldRenderer
          fields={enhancedNameFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
        />
      </Box>
      
      {/* Role Fields */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="subtitle2" gutterBottom>
          Position und Rolle
        </Typography>
        <DynamicFieldRenderer
          fields={enhancedRoleFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
        />
      </Box>
      
      {/* Contact Info Fields */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="subtitle2" gutterBottom>
          Kontaktdaten
        </Typography>
        <DynamicFieldRenderer
          fields={contactFields.contactInfoFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
        />
      </Box>
    </Box>
  );
};