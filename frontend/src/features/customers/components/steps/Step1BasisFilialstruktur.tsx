/**
 * Step 1: Basis & Filialstruktur
 * 
 * Erfasst Unternehmensdaten, Geschäftsmodell und Filialstruktur.
 * Zeigt sofortiges Potenzial bei Ketten.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md
 */

import React, { useCallback, useMemo } from 'react';
import { 
  Box, 
  Typography, 
  Alert, 
  AlertTitle,
  Divider,
  Chip
} from '@mui/material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { FieldDefinition } from '../../types/FieldDefinition';

/**
 * Step 1: Basis & Filialstruktur
 * 
 * Neue verkaufsfokussierte Struktur mit:
 * - Basisdaten
 * - Geschäftsmodell
 * - Filialstruktur mit Potenzialindikator
 */
export const Step1BasisFilialstruktur: React.FC = () => {
  const {
    customerData,
    validationErrors,
    setCustomerField,
    validateField
  } = useCustomerOnboardingStore();
  
  const { customerFields, getFieldByKey } = useFieldDefinitions();
  
  // Field groups for better organization
  const baseFields = useMemo(() => {
    return [
      'customerNumber',
      'companyName',
      'legalForm',
      'industry',
      'chainCustomer'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);
  
  const addressFields = useMemo(() => {
    return [
      'street',
      'houseNumber',
      'postalCode',
      'city'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);
  
  const businessModelFields = useMemo(() => {
    return [
      'primaryFinancing',
      'currentSupplier',
      'contractEndDate',
      'switchWillingness',
      'decisionTimeline'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);
  
  const chainStructureFields = useMemo(() => {
    return [
      'totalLocationsEU',
      'locationsGermany',
      'locationsAustria',
      'locationsSwitzerland',
      'locationsRestEU',
      'expansionPlanned',
      'decisionStructure'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);
  
  // Calculate potential for chains
  const chainPotential = useMemo(() => {
    const total = Number(customerData.totalLocationsEU) || 0;
    if (total >= 50) return { level: 'high', text: '🔥 Großkunden-Potenzial!' };
    if (total >= 20) return { level: 'medium', text: '💎 Rahmenvertrag möglich' };
    if (total >= 5) return { level: 'low', text: '✨ Interessante Größe' };
    return null;
  }, [customerData.totalLocationsEU]);
  
  const handleFieldChange = useCallback((fieldKey: string, value: any) => {
    setCustomerField(fieldKey, value);
  }, [setCustomerField]);
  
  const handleFieldBlur = useCallback((fieldKey: string) => {
    validateField(fieldKey);
  }, [validateField]);
  
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
              Geben Sie die Anzahl der Standorte pro Region an. 
              Dies hilft uns, das Potenzial für Rahmenverträge zu bewerten.
            </Alert>
            
            <DynamicFieldRenderer
              fields={chainStructureFields}
              values={customerData}
              errors={validationErrors}
              onChange={handleFieldChange}
              onBlur={handleFieldBlur}
              useAdaptiveLayout={true}
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
        Im nächsten Schritt erfassen wir die Angebotsstruktur und identifizieren 
        Vertriebschancen basierend auf den Pain Points.
      </Alert>
    </Box>
  );
};