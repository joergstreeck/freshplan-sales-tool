/**
 * Customer Data Step Component
 *
 * First step of the wizard - collects basic customer information.
 * Uses dynamic field rendering based on the Field Catalog.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/01-components.md
 */

import React, { useCallback } from 'react';
import { Box, Typography, Alert, AlertTitle } from '@mui/material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';

/**
 * Customer Data Step
 *
 * Renders customer base fields and industry-specific fields.
 * Handles field validation and updates the store.
 */
export const CustomerDataStep: React.FC = () => {
  const { customerData, validationErrors, setCustomerField, validateField } =
    useCustomerOnboardingStore();

  const { customerFields, getIndustryFields } = useFieldDefinitions();

  // Get current industry selection
  const selectedIndustry = customerData.industry || '';

  // Combine base fields with industry-specific fields
  const allFields = React.useMemo(() => {
    const industryFields = getIndustryFields(selectedIndustry);
    return [...customerFields, ...industryFields];
  }, [customerFields, selectedIndustry, getIndustryFields]);

  /**
   * Handle field value change
   */
  const handleFieldChange = useCallback(
    (fieldKey: string, value: unknown) => {
      setCustomerField(fieldKey, value);

      // If industry changed, clear industry-specific field values
      if (fieldKey === 'industry' && value !== selectedIndustry) {
        const oldIndustryFields = getIndustryFields(selectedIndustry);
        oldIndustryFields.forEach(field => {
          setCustomerField(field.key, undefined);
        });
      }
    },
    [setCustomerField, selectedIndustry, getIndustryFields]
  );

  /**
   * Handle field blur for validation
   */
  const handleFieldBlur = useCallback(
    (fieldKey: string) => {
      validateField(fieldKey);
    },
    [validateField]
  );

  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Kundenstammdaten
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Bitte füllen Sie die Grunddaten des Kunden aus. Felder mit * sind Pflichtfelder.
      </Typography>

      {/* Info for chain customers */}
      {customerData.chainCustomer === 'ja' && (
        <Alert severity="info" sx={{ mb: 3 }}>
          <AlertTitle>Filialunternehmen</AlertTitle>
          Sie haben angegeben, dass es sich um ein Filialunternehmen handelt. Im nächsten Schritt
          können Sie die einzelnen Standorte erfassen.
        </Alert>
      )}

      {/* Dynamic field rendering */}
      <DynamicFieldRenderer
        fields={allFields}
        values={customerData}
        errors={validationErrors}
        onChange={handleFieldChange}
        onBlur={handleFieldBlur}
      />

      {/* Industry-specific info */}
      {selectedIndustry && (
        <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
          <Typography variant="body2" color="text.secondary">
            <strong>Branche {getIndustryLabel(selectedIndustry)}:</strong> Es wurden
            branchenspezifische Felder hinzugefügt.
          </Typography>
        </Box>
      )}
    </Box>
  );
};

/**
 * Get human-readable industry label
 */
function getIndustryLabel(industry: string): string {
  const labels: Record<string, string> = {
    hotel: 'Hotel',
    krankenhaus: 'Krankenhaus',
    seniorenresidenz: 'Seniorenresidenz',
    restaurant: 'Restaurant',
    betriebsrestaurant: 'Betriebsrestaurant',
    kantine: 'Kantine',
    catering: 'Catering',
    sonstige: 'Sonstige',
  };
  return labels[industry] || industry;
}
