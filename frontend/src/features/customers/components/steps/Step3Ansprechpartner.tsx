/**
 * Step 3: Ansprechpartner
 * 
 * Erfasst strukturierte Kontaktdaten mit Fokus auf Beziehungsaufbau.
 * Optionaler Step mit erweiterten Kommunikationspräferenzen.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER.md
 */

import React, { useCallback, useMemo } from 'react';
import { 
  Box, 
  Typography, 
  Alert, 
  AlertTitle,
  Divider,
  Chip,
  Card,
  CardContent
} from '@mui/material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { FieldDefinition } from '../../types/FieldDefinition';

export const Step3Ansprechpartner: React.FC = () => {
  const {
    customerData,
    validationErrors,
    setCustomerField,
    validateField
  } = useCustomerOnboardingStore();
  
  const { getFieldByKey } = useFieldDefinitions();
  
  // Contact base fields
  const contactBaseFields = useMemo(() => {
    return [
      'salutation',
      'title',
      'firstName',
      'lastName',
      'position',
      'decisionLevel'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);
  
  // Contact method fields
  const contactMethodFields = useMemo(() => {
    return [
      'emailBusiness',
      'phoneBusiness',
      'phoneMobile',
      'preferredChannel',
      'bestCallTime'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);
  
  // Relationship building fields
  const relationshipFields = useMemo(() => {
    return [
      'birthday',
      'contactNotes',
      'contactTags',
      'nextContactDate'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);
  
  // Social media fields (optional)
  const socialMediaFields = useMemo(() => {
    return [
      'linkedIn',
      'xing'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);
  
  const handleFieldChange = useCallback((fieldKey: string, value: any) => {
    setCustomerField(fieldKey, value);
  }, [setCustomerField]);
  
  const handleFieldBlur = useCallback((fieldKey: string) => {
    validateField(fieldKey);
  }, [validateField]);
  
  // Build contact display name
  const contactDisplayName = useMemo(() => {
    const parts = [];
    if (customerData.salutation && customerData.salutation !== 'keine') {
      parts.push(customerData.salutation === 'herr' ? 'Herr' : 
                  customerData.salutation === 'frau' ? 'Frau' : '');
    }
    if (customerData.title) parts.push(customerData.title);
    if (customerData.firstName) parts.push(customerData.firstName);
    if (customerData.lastName) parts.push(customerData.lastName);
    return parts.join(' ') || 'Noch kein Name erfasst';
  }, [customerData]);
  
  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Schritt 3: Ansprechpartner (Optional)
      </Typography>
      
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Erfassen Sie Kontaktdaten für besseren Beziehungsaufbau. 
        Dieser Schritt kann auch später ergänzt werden.
      </Typography>
      
      <Alert severity="info" sx={{ mb: 3 }}>
        <AlertTitle>Tipp für erfolgreichen Vertrieb</AlertTitle>
        Je mehr Sie über Ihre Ansprechpartner wissen, desto besser können Sie 
        auf deren Bedürfnisse eingehen. Die zusätzlichen Felder helfen beim 
        Beziehungsaufbau.
      </Alert>
      
      {/* Contact Base Information */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          👤 Kontaktdaten
        </Typography>
        <DynamicFieldRenderer
          fields={contactBaseFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
          useAdaptiveLayout={true}
        />
        
        {/* Display formatted name */}
        {(customerData.firstName || customerData.lastName) && (
          <Card variant="outlined" sx={{ mt: 2, bgcolor: 'grey.50' }}>
            <CardContent>
              <Typography variant="body2" color="text.secondary">
                Ansprechpartner:
              </Typography>
              <Typography variant="subtitle1" fontWeight="bold">
                {contactDisplayName}
              </Typography>
              {customerData.position && (
                <Typography variant="body2">
                  {customerData.position}
                </Typography>
              )}
            </CardContent>
          </Card>
        )}
      </Box>
      
      <Divider sx={{ my: 3 }} />
      
      {/* Contact Methods */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          📞 Kontaktmöglichkeiten
        </Typography>
        <DynamicFieldRenderer
          fields={contactMethodFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
          useAdaptiveLayout={true}
        />
      </Box>
      
      <Divider sx={{ my: 3 }} />
      
      {/* Relationship Building */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          🤝 Beziehungsaufbau
        </Typography>
        <Alert severity="success" sx={{ mb: 2 }}>
          Diese Informationen helfen, eine persönliche Beziehung aufzubauen.
        </Alert>
        <DynamicFieldRenderer
          fields={relationshipFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
          useAdaptiveLayout={true}
        />
      </Box>
      
      {/* Social Media (Optional) */}
      {socialMediaFields.length > 0 && (
        <>
          <Divider sx={{ my: 3 }} />
          <Box sx={{ mb: 4 }}>
            <Typography variant="h6" gutterBottom>
              📱 Social Media (Optional)
            </Typography>
            <DynamicFieldRenderer
              fields={socialMediaFields}
              values={customerData}
              errors={validationErrors}
              onChange={handleFieldChange}
              onBlur={handleFieldBlur}
              useAdaptiveLayout={true}
            />
          </Box>
        </>
      )}
      
      {/* Summary Card */}
      <Card sx={{ bgcolor: 'primary.light', mt: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            ✅ Fast geschafft!
          </Typography>
          <Typography variant="body2">
            Nach dem Speichern wird automatisch eine Aufgabe "Neukunde begrüßen" 
            erstellt, damit der erste Kontakt nicht vergessen wird.
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
};