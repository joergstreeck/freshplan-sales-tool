/**
 * Step 3: Ansprechpartner V4
 *
 * Vereinfachte Version mit DynamicFieldRenderer und korrektem Layout.
 * Verwendet nur die Standard-Felder aus dem Field Catalog.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER_V2.md
 */

import React, { useCallback, useMemo } from 'react';
import { Box, Typography, Divider } from '@mui/material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { FieldDefinition } from '../../types/field.types';

export const Step3AnsprechpartnerV4: React.FC = () => {
  const { customerData, validationErrors, setCustomerField, validateField } =
    useCustomerOnboardingStore();

  const { getFieldByKey } = useFieldDefinitions();

  // Standard Contact Fields aus fieldCatalog.json
  const contactBaseFields = useMemo(() => {
    return ['contactName', 'contactEmail', 'contactPhone']
      .map(key => getFieldByKey(key))
      .filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);

  // Erweiterte Contact Fields mit custom Dropdowns
  const extendedContactFields = useMemo(() => {
    // Erstelle erweiterte Felder für bessere Erfassung
    const salutationField: FieldDefinition = {
      key: 'contactSalutation',
      label: 'Anrede',
      entityType: 'customer',
      fieldType: 'select',
      required: true,
      category: 'contact',
      options: [
        { value: 'Herr', label: 'Herr' },
        { value: 'Frau', label: 'Frau' },
        { value: 'Divers', label: 'Divers' },
      ],
      sizeHint: 'kompakt',
    };

    const titleField: FieldDefinition = {
      key: 'contactTitle',
      label: 'Titel',
      entityType: 'customer',
      fieldType: 'select',
      required: false,
      category: 'contact',
      options: [
        { value: '', label: 'Kein Titel' },
        { value: 'Dr.', label: 'Dr.' },
        { value: 'Prof.', label: 'Prof.' },
      ],
      sizeHint: 'klein',
    };

    const positionField: FieldDefinition = {
      key: 'contactPosition',
      label: 'Position/Funktion',
      entityType: 'customer',
      fieldType: 'select',
      required: false,
      category: 'contact',
      options: [
        { value: 'Geschäftsführer', label: 'Geschäftsführer' },
        { value: 'Direktor', label: 'Direktor' },
        { value: 'Inhaber', label: 'Inhaber' },
        { value: 'Vorstand', label: 'Vorstand' },
        { value: 'Hoteldirektor', label: 'Hoteldirektor' },
        { value: 'F&B Manager', label: 'F&B Manager' },
        { value: 'Küchenchef', label: 'Küchenchef' },
        { value: 'Einkaufsleiter', label: 'Einkaufsleiter' },
        { value: 'Betriebsleiter', label: 'Betriebsleiter' },
        { value: 'Verwaltungsdirektor', label: 'Verwaltungsdirektor' },
        { value: 'Küchenleitung', label: 'Küchenleitung' },
        { value: 'Verpflegungsmanager', label: 'Verpflegungsmanager' },
        { value: 'Kantinenchef', label: 'Kantinenchef' },
        { value: 'Gastronomiemanager', label: 'Gastronomiemanager' },
        { value: 'Einkäufer', label: 'Einkäufer' },
        { value: 'Prokurist', label: 'Prokurist' },
        { value: 'Assistent der Geschäftsführung', label: 'Assistent der Geschäftsführung' },
        { value: 'other', label: 'Andere Position' },
      ],
      sizeHint: 'groß',
    };

    const decisionLevelField: FieldDefinition = {
      key: 'contactDecisionLevel',
      label: 'Entscheider-Ebene',
      entityType: 'customer',
      fieldType: 'select',
      required: false,
      category: 'contact',
      options: [
        { value: 'decision_maker', label: 'Entscheider' },
        { value: 'influencer', label: 'Beeinflusser' },
        { value: 'gatekeeper', label: 'Gatekeeper' },
        { value: 'user', label: 'Nutzer' },
      ],
      sizeHint: 'mittel',
      helpText: 'Rolle im Kaufentscheidungsprozess',
    };

    const mobileField: FieldDefinition = {
      key: 'contactMobile',
      label: 'Mobil',
      entityType: 'customer',
      fieldType: 'text',
      required: false,
      category: 'contact',
      placeholder: '+49 170 123456',
      sizeHint: 'mittel',
    };

    return {
      personalFields: [salutationField, titleField],
      roleFields: [positionField, decisionLevelField],
      additionalContactFields: [mobileField],
    };
  }, []);

  const handleFieldChange = useCallback(
    (fieldKey: string, value: any) => {
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

  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Schritt 3: Ansprechpartner
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Erfassen Sie den wichtigsten Ansprechpartner für eine erfolgreiche Zusammenarbeit.
      </Typography>

      {/* Basis Kontaktdaten mit Name, Email, Telefon */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          Hauptansprechpartner
        </Typography>
        <DynamicFieldRenderer
          fields={contactBaseFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
        />
      </Box>

      <Divider sx={{ mb: 4 }} />

      {/* Erweiterte Angaben */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          Detaillierte Angaben
        </Typography>

        {/* Persönliche Angaben */}
        <Box sx={{ mb: 3 }}>
          <DynamicFieldRenderer
            fields={extendedContactFields.personalFields}
            values={customerData}
            errors={validationErrors}
            onChange={handleFieldChange}
            onBlur={handleFieldBlur}
          />
        </Box>

        {/* Position und Rolle */}
        <Box sx={{ mb: 3 }}>
          <DynamicFieldRenderer
            fields={extendedContactFields.roleFields}
            values={customerData}
            errors={validationErrors}
            onChange={handleFieldChange}
            onBlur={handleFieldBlur}
          />
        </Box>

        {/* Zusätzliche Kontaktdaten */}
        <Box>
          <DynamicFieldRenderer
            fields={extendedContactFields.additionalContactFields}
            values={customerData}
            errors={validationErrors}
            onChange={handleFieldChange}
            onBlur={handleFieldBlur}
          />
        </Box>
      </Box>
    </Box>
  );
};
