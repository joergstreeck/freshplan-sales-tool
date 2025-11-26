/**
 * Step 2: Herausforderungen & Potenzial (V3)
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Wizard (Option B)
 *
 * Server-Driven Architecture:
 * - Backend definiert ALLE Felder + Section-Struktur
 * - Frontend rendert generisch basierend auf wizardSectionId/wizardSectionTitle
 * - Keine hardcodierten Field-Arrays mehr!
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_HERAUSFORDERUNGEN_POTENZIAL_V3.md
 */

import React, { useCallback, useMemo } from 'react';
import { Box, Typography, CircularProgress, Alert, AlertTitle, Divider } from '@mui/material';
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

// Pain Point Solutions Mapping (100% Lead Parity - Sprint 2.1.7.2)
// Field names match Lead entity exactly for seamless Lead→Customer conversion
const PAIN_POINT_SOLUTIONS = {
  // OPERATIONAL PAINS (5 fields)
  painStaffShortage: {
    title: 'Personalmangel',
    solution: 'Cook&Fresh® - Keine Fachkräfte nötig!',
    products: ['Convenience Line', 'Ready-to-Serve'],
    impact: '30% weniger Personalkosten',
    points: 10,
  },
  painHighCosts: {
    title: 'Hoher Kostendruck',
    solution: 'Kalkulierbare Kosten, weniger Personal',
    products: ['Efficiency Line'],
    impact: 'Transparente Preisgestaltung',
    points: 7,
  },
  painFoodWaste: {
    title: 'Lebensmittelverschwendung',
    solution: '40 Tage Haltbarkeit ohne Qualitätsverlust',
    products: ['Cook&Fresh® Verfahren'],
    impact: 'Bis zu 50% weniger Abfall',
    points: 7,
  },
  painQualityInconsistency: {
    title: 'Schwankende Qualität',
    solution: 'Immer gleiche Premium-Qualität',
    products: ['Standardisierte Rezepturen'],
    impact: 'Konstante Gästezufriedenheit',
    points: 6,
  },
  painTimePressure: {
    title: 'Zeitdruck / Effizienz-Probleme',
    solution: 'Schnelle Zubereitung, weniger Arbeitsschritte',
    products: ['Ready-to-Serve'],
    impact: 'Schnellerer Service',
    points: 5,
  },
  // SWITCHING PAINS (3 fields)
  painSupplierQuality: {
    title: 'Qualitätsprobleme beim Lieferanten',
    solution: 'Kontrollierte Premium-Qualität',
    products: ['Qualitätssicherung'],
    impact: 'Verlässliche Qualität',
    points: 10,
  },
  painUnreliableDelivery: {
    title: 'Unzuverlässige Lieferzeiten',
    solution: 'Pünktliche Lieferung garantiert',
    products: ['Logistics Excellence'],
    impact: 'Planbare Verfügbarkeit',
    points: 8,
  },
  painPoorService: {
    title: 'Schlechter Service / Support',
    solution: 'Persönlicher Ansprechpartner',
    products: ['Premium Support'],
    impact: 'Schnelle Problemlösung',
    points: 3,
  },
};

/**
 * Step 2: Herausforderungen & Potenzial
 *
 * Server-Driven: Backend controls sections, titles, field order via CustomerSchemaResource.java
 */
export const Step2HerausforderungenPotenzialV3: React.FC = () => {
  const { customerData, validationErrors, setCustomerField, validateField } =
    useCustomerOnboardingStore();

  // ========== SERVER-DRIVEN SCHEMA (Sprint 2.1.7.2 D11) ==========
  const { getWizardFields, isLoading, isError } = useCustomerSchema();

  /**
   * Get all Step 2 fields from backend
   *
   * Backend defines:
   * - pain_points section (Order 1-9)
   * - revenue_potential section (Order 10)
   */
  const step2Fields = useMemo(() => {
    if (isLoading || isError) return [];
    return getWizardFields(2); // Sorted by wizardOrder
  }, [getWizardFields, isLoading, isError]);

  /**
   * Group fields by section (Option B: Server-Driven Sections)
   */
  const sections = useMemo((): WizardSection[] => {
    const sectionMap = new Map<string, WizardSection>();

    step2Fields.forEach(field => {
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

      if (field.showDividerAfter) {
        section.showDividerAfter = true;
      }
    });

    return Array.from(sectionMap.values());
  }, [step2Fields]);

  /**
   * Business Logic: Active Pain Points
   *
   * Note: This is NOT part of server-driven schema!
   * This is business logic that identifies selected pain points.
   */
  const activePainPoints = useMemo(() => {
    const painPointKeys = Object.keys(PAIN_POINT_SOLUTIONS) as Array<
      keyof typeof PAIN_POINT_SOLUTIONS
    >;

    return painPointKeys
      .filter(key => customerData[key] === true)
      .map(key => ({
        key,
        ...PAIN_POINT_SOLUTIONS[key],
      }));
  }, [customerData]);

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
        Schritt 2: Herausforderungen & Potenzial
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Identifizieren Sie Ihre unternehmensweiten Herausforderungen und entdecken Sie Ihr Potenzial
        mit Freshfoodz.
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
          </Box>

          {/* Divider (server-driven via showDividerAfter) */}
          {section.showDividerAfter && sectionIndex < sections.length - 1 && (
            <Divider sx={{ my: 3 }} />
          )}
        </React.Fragment>
      ))}

      {/* Business Logic: Solutions für ausgewählte Pain Points */}
      {/* Show only when pain points are selected */}
      {activePainPoints.length > 0 && (
        <Box sx={{ mt: 4, p: 3, bgcolor: 'background.paper', borderRadius: 2 }}>
          <Typography variant="h6" gutterBottom>
            💡 Freshfoodz Lösungen für Ihre Herausforderungen
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            {activePainPoints.map(point => (
              <Box
                key={point.key}
                sx={{
                  p: 2,
                  border: 1,
                  borderColor: 'divider',
                  borderRadius: 1,
                }}
              >
                <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                  {point.title}
                </Typography>
                <Typography variant="body2" color="success.main" gutterBottom>
                  ✅ {point.solution}
                </Typography>
                <Typography variant="caption" color="text.secondary" display="block">
                  {point.impact}
                </Typography>
              </Box>
            ))}
          </Box>
        </Box>
      )}

      {/* Info Box: Next Step */}
      <Alert severity="info" sx={{ mt: 3 }}>
        <AlertTitle>Nächster Schritt</AlertTitle>
        Im nächsten Schritt erfassen wir Ihre Kontakte und Ansprechpartner für eine optimale
        Zusammenarbeit.
      </Alert>
    </Box>
  );
};
