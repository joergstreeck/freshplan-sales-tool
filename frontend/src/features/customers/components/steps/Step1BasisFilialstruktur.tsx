/**
 * Step 1: Basis & Filialstruktur
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Wizard (Option B)
 *
 * Server-Driven Architecture:
 * - Backend definiert ALLE Felder + Section-Struktur
 * - Frontend rendert generisch basierend auf wizardSectionId/wizardSectionTitle
 * - Keine hardcodierten Field-Arrays mehr!
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md
 */

import React, { useCallback, useMemo, useEffect } from 'react';
import {
  Box,
  Typography,
  Alert,
  AlertTitle,
  Divider,
  CircularProgress,
  Paper,
} from '@mui/material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useCustomerSchema } from '../../../../hooks/useCustomerSchema';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { FieldDefinition } from '../../../../types/customer-schema';

/**
 * Section data structure for grouping fields
 */
interface WizardSection {
  sectionId: string;
  title: string;
  fields: FieldDefinition[];
  showDividerAfter: boolean;
}

/**
 * Step 1: Basis & Filialstruktur
 *
 * Server-Driven: Backend controls sections, titles, field order via CustomerSchemaResource.java
 */
export const Step1BasisFilialstruktur: React.FC = () => {
  const { customerData, validationErrors, setCustomerField, validateField } =
    useCustomerOnboardingStore();

  // ========== SERVER-DRIVEN SCHEMA (Sprint 2.1.7.2 D11) ==========
  const { getWizardFields, isLoading, isError } = useCustomerSchema();

  /**
   * Get all Step 1 fields from backend
   *
   * Backend defines:
   * - Which fields appear in Step 1 (showInWizard=true, wizardStep=1)
   * - Field order (wizardOrder)
   * - Section grouping (wizardSectionId, wizardSectionTitle)
   * - Visual separators (showDividerAfter)
   */
  const step1Fields = useMemo(() => {
    if (isLoading || isError) return [];
    return getWizardFields(1); // Sorted by wizardOrder
  }, [getWizardFields, isLoading, isError]);

  /**
   * Group fields by section (Option B: Server-Driven Sections)
   *
   * Backend controls section structure via:
   * - wizardSectionId: "company_basic", "address", "chain_structure"
   * - wizardSectionTitle: "Unternehmensdaten", "📍 Adresse", "🏢 Filialstruktur"
   * - showDividerAfter: true/false
   */
  const sections = useMemo((): WizardSection[] => {
    const sectionMap = new Map<string, WizardSection>();

    step1Fields.forEach(field => {
      const sectionId = field.wizardSectionId || 'default';
      const sectionTitle = field.wizardSectionTitle || '';

      if (!sectionMap.has(sectionId)) {
        sectionMap.set(sectionId, {
          sectionId,
          title: sectionTitle,
          fields: [],
          showDividerAfter: false,
        });
      }

      const section = sectionMap.get(sectionId)!;
      section.fields.push(field);

      // If any field in section has showDividerAfter, set it on section
      if (field.showDividerAfter) {
        section.showDividerAfter = true;
      }
    });

    return Array.from(sectionMap.values());
  }, [step1Fields]);

  /**
   * Sprint 2.1.7.7: Auto-Summe für branchCount
   *
   * Berechnet die Gesamtzahl der Standorte aus den Länder-Feldern.
   * Diese Summe wird automatisch in branchCount gespeichert.
   */
  const totalLocations = useMemo(() => {
    const germany = Number(customerData.locationsGermany) || 0;
    const austria = Number(customerData.locationsAustria) || 0;
    const switzerland = Number(customerData.locationsSwitzerland) || 0;
    return germany + austria + switzerland;
  }, [
    customerData.locationsGermany,
    customerData.locationsAustria,
    customerData.locationsSwitzerland,
  ]);

  // Auto-Update branchCount when location sum changes
  useEffect(() => {
    if (customerData.isChain && totalLocations > 0) {
      // Nur updaten wenn sich der Wert tatsächlich geändert hat
      if (customerData.branchCount !== totalLocations) {
        setCustomerField('branchCount', totalLocations);
      }
    }
  }, [totalLocations, customerData.isChain, customerData.branchCount, setCustomerField]);

  /**
   * Business Logic: Chain Potential Indicator
   *
   * Note: This is NOT part of server-driven schema!
   * This is business logic that calculates potential based on data.
   */
  const chainPotential = useMemo(() => {
    const total = totalLocations || Number(customerData.totalLocationsEU) || 0;
    if (total >= 50) return { level: 'high', text: '🔥 Großkunden-Potenzial!' };
    if (total >= 20) return { level: 'medium', text: '💎 Rahmenvertrag möglich' };
    if (total >= 5) return { level: 'low', text: '✨ Interessante Größe' };
    return null;
  }, [totalLocations, customerData.totalLocationsEU]);

  // Field event handlers
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

  // ========== LOADING / ERROR STATES ==========

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (isError) {
    return (
      <Alert severity="error">
        <AlertTitle>Fehler beim Laden des Schemas</AlertTitle>
        Bitte versuchen Sie es später erneut oder wenden Sie sich an den Support.
      </Alert>
    );
  }

  // ========== SERVER-DRIVEN RENDERING ==========

  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Schritt 1: Basis & Filialstruktur
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Erfassen Sie die Grunddaten und erkennen Sie sofort das Vertriebspotenzial.
      </Typography>

      {/* SERVER-DRIVEN SECTIONS (Sprint 2.1.7.2 D11 Option B) */}
      {/* Backend controls: section titles, field order, dividers */}
      {sections.map((section, sectionIndex) => (
        <React.Fragment key={section.sectionId}>
          <Box sx={{ mb: 4 }}>
            {/* Section Title (from backend wizardSectionTitle) */}
            {section.title && (
              <Typography variant="h6" gutterBottom>
                {section.title}
              </Typography>
            )}

            {/* Fields in this section - Grid Layout für Backend-gridCols */}
            <DynamicFieldRenderer
              fields={section.fields}
              values={customerData}
              errors={validationErrors}
              onChange={handleFieldChange}
              onBlur={handleFieldBlur}
              useAdaptiveLayout={false}
            />

            {/* Sprint 2.1.7.7: Auto-Summe Anzeige für Filialstruktur */}
            {section.sectionId === 'chain_structure' &&
              customerData.isChain &&
              totalLocations > 0 && (
                <Paper
                  elevation={0}
                  sx={{
                    mt: 2,
                    p: 2,
                    bgcolor: 'primary.light',
                    borderRadius: 2,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                  }}
                >
                  <Typography variant="body1" fontWeight="medium">
                    Standorte gesamt:
                  </Typography>
                  <Typography variant="h5" fontWeight="bold" color="primary.dark">
                    {totalLocations}
                  </Typography>
                </Paper>
              )}

            {/* Business Logic: Chain Potential Indicator */}
            {/* Show only for chain_structure section when data indicates potential */}
            {section.sectionId === 'chain_structure' && chainPotential && (
              <Box sx={{ mt: 2, p: 2, bgcolor: 'success.light', borderRadius: 2 }}>
                <Typography variant="body1" fontWeight="bold">
                  {chainPotential.text}
                </Typography>
              </Box>
            )}
          </Box>

          {/* Divider (server-driven via showDividerAfter) */}
          {section.showDividerAfter && sectionIndex < sections.length - 1 && (
            <Divider sx={{ my: 3 }} />
          )}
        </React.Fragment>
      ))}

      {/* Info Box: Next Step */}
      <Alert severity="info" sx={{ mt: 3 }}>
        <AlertTitle>Nächster Schritt</AlertTitle>
        Im nächsten Schritt erfassen wir die Angebotsstruktur und identifizieren Vertriebschancen
        basierend auf den Pain Points.
      </Alert>
    </Box>
  );
};
