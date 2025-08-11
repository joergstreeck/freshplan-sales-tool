/**
 * Step 1: Basis & Filialstruktur
 *
 * Erfasst Unternehmensdaten, Geschäftsmodell und Filialstruktur.
 * Zeigt sofortiges Potenzial bei Ketten.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md
 */

import React, { useCallback, useMemo } from 'react';
import { Box, Typography, Alert, AlertTitle, Divider } from '@mui/material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import { FilialstrukturLayout } from '../layout/FilialstrukturLayout';
import { TextField } from '../fields/fieldTypes/TextField';
import { NumberField } from '../fields/fieldTypes/NumberField';
import { SelectField } from '../fields/fieldTypes/SelectField';
import type { FieldDefinition } from '../../types/field.types';
import { getFieldSize } from '../../utils/fieldSizeCalculator';

/**
 * Step 1: Basis & Filialstruktur
 *
 * Neue verkaufsfokussierte Struktur mit:
 * - Basisdaten
 * - Geschäftsmodell
 * - Filialstruktur mit Potenzialindikator
 */
export const Step1BasisFilialstruktur: React.FC = () => {
  const { customerData, validationErrors, setCustomerField, validateField } =
    useCustomerOnboardingStore();

  const { customerFields: _customerFields, getFieldByKey } = useFieldDefinitions();

  // Field groups for better organization
  const baseFields = useMemo(() => {
    return ['customerNumber', 'companyName', 'legalForm', 'industry', 'chainCustomer']
      .map(key => getFieldByKey(key))
      .filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);

  const addressFields = useMemo(() => {
    return ['street', 'houseNumber', 'postalCode', 'city']
      .map(key => getFieldByKey(key))
      .filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);

  const businessModelFields = useMemo(() => {
    const fields = [
      'financingType',
      'currentSupplier',
      'contractEndDate',
      'switchWillingness',
      'decisionTimeline',
    ]
      .map(key => getFieldByKey(key))
      .filter(Boolean) as FieldDefinition[];

    // DEBUG: Log die berechneten Größen
    if (process.env.NODE_ENV === 'development') {
      fields.forEach(field => {
        const size = getFieldSize(field);
        console.log(
          `Field: ${field.key}, Grid size: ${size.md}, Options:`,
          field.options?.map(o => o.label)
        );
      });
    }

    return fields;
  }, [getFieldByKey]);

  const chainStructureFields = useMemo(() => {
    return [
      'totalLocationsEU',
      'locationsGermany',
      'locationsAustria',
      'locationsSwitzerland',
      'locationsRestEU',
      'expansionPlanned',
      'decisionStructure',
    ]
      .map(key => getFieldByKey(key))
      .filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);

  // Calculate potential for chains
  const chainPotential = useMemo(() => {
    const total = Number(customerData.totalLocationsEU) || 0;
    if (total >= 50) return { level: 'high', text: '🔥 Großkunden-Potenzial!' };
    if (total >= 20) return { level: 'medium', text: '💎 Rahmenvertrag möglich' };
    if (total >= 5) return { level: 'low', text: '✨ Interessante Größe' };
    return null;
  }, [customerData.totalLocationsEU]);

  const handleFieldChange = useCallback(
    (fieldKey: string, value: unknown) => {
      setCustomerField(fieldKey, value);
    },
    [setCustomerField]
  );

  const handleFieldBlur = useCallback(
    (fieldKey: string) => {
      validateField(fieldKey);
    },
    [validateField]
  );

  // Render function for FilialstrukturLayout (without labels - labels are rendered by FilialstrukturLayout)
  const renderField = useCallback(
    (field: FieldDefinition) => {
      const fieldValue = customerData[field.key];
      const fieldError = validationErrors[field.key];

      // Handle field change
      const handleChange = (value: unknown) => {
        handleFieldChange(field.key, value);
      };

      // Handle field blur
      const handleBlur = () => {
        handleFieldBlur(field.key);
      };

      // Render field directly without FieldWrapper (no label)
      switch (field.fieldType) {
        case 'text':
          return (
            <TextField
              field={field}
              value={fieldValue || ''}
              onChange={handleChange}
              onBlur={handleBlur}
              error={!!fieldError}
              helperText={fieldError}
            />
          );
        case 'number':
          return (
            <NumberField
              field={field}
              value={fieldValue || ''}
              onChange={handleChange}
              onBlur={handleBlur}
              error={!!fieldError}
              helperText={fieldError}
            />
          );
        case 'select':
        case 'dropdown':
          return (
            <SelectField
              field={field}
              value={fieldValue || ''}
              onChange={handleChange}
              onBlur={handleBlur}
              error={!!fieldError}
              helperText={fieldError}
            />
          );
        default:
          return (
            <DynamicFieldRenderer
              fields={[field]}
              values={customerData}
              errors={validationErrors}
              onChange={handleFieldChange}
              onBlur={handleFieldBlur}
              useAdaptiveLayout={false}
            />
          );
      }
    },
    [customerData, validationErrors, handleFieldChange, handleFieldBlur]
  );

  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Schritt 1: Basis & Filialstruktur
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Erfassen Sie die Grunddaten und erkennen Sie sofort das Vertriebspotenzial.
      </Typography>

      {/* Base Fields */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          Unternehmensdaten
        </Typography>
        <DynamicFieldRenderer
          fields={baseFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
          useAdaptiveLayout={true}
        />
      </Box>

      <Divider sx={{ my: 3 }} />

      {/* Address */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          📍 Adresse Hauptstandort
        </Typography>
        <DynamicFieldRenderer
          fields={addressFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
          useAdaptiveLayout={true}
        />
      </Box>

      {/* Chain Structure - Show only if chain customer */}
      {customerData.chainCustomer === 'ja' && (
        <>
          <Divider sx={{ my: 3 }} />
          <Box sx={{ mb: 4 }}>
            <Typography variant="h6" gutterBottom>
              🏢 Filialstruktur
            </Typography>

            <Alert severity="info" sx={{ mb: 3 }}>
              <AlertTitle>Standortverteilung erfassen</AlertTitle>
              Geben Sie die Anzahl der Standorte pro Region an. Dies hilft uns, das Potenzial für
              Rahmenverträge zu bewerten.
            </Alert>

            <FilialstrukturLayout
              fields={chainStructureFields}
              renderField={renderField}
              values={customerData}
            />

            {/* Potential Indicator */}
            {chainPotential && (
              <Box sx={{ mt: 2, p: 2, bgcolor: 'success.light', borderRadius: 2 }}>
                <Typography variant="body1" fontWeight="bold">
                  {chainPotential.text}
                </Typography>
              </Box>
            )}
          </Box>
        </>
      )}

      <Divider sx={{ my: 3 }} />

      {/* Business Model */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          💰 Geschäftsmodell & Wettbewerb
        </Typography>
        <DynamicFieldRenderer
          fields={businessModelFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
          useAdaptiveLayout={true}
        />
      </Box>

      {/* Info Box */}
      <Alert severity="info" sx={{ mt: 3 }}>
        <AlertTitle>Nächster Schritt</AlertTitle>
        Im nächsten Schritt erfassen wir die Angebotsstruktur und identifizieren Vertriebschancen
        basierend auf den Pain Points.
      </Alert>
    </Box>
  );
};
