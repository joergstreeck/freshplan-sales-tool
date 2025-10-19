/**
 * Step 2: Herausforderungen & Potenzial (V3)
 *
 * FINALE VERSION - NUR globale Themen
 * Keine standortbezogenen Felder mehr!
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_HERAUSFORDERUNGEN_POTENZIAL_V3.md
 */

import React, { useCallback, useMemo } from 'react';
import { Box, Typography, Card, CardContent, Grid, Chip } from '@mui/material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';

// Import Sections
import { GlobalChallengesSection } from '../sections/GlobalChallengesSection';
import { RevenueExpectationSectionV2 } from '../sections/RevenueExpectationSectionV2';
import { AdditionalBusinessSection } from '../sections/AdditionalBusinessSection';
import { isFieldDefinition } from '../../types/field.types';

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

export const Step2HerausforderungenPotenzialV3: React.FC = () => {
  const { customerData, validationErrors, setCustomerField, validateField } =
    useCustomerOnboardingStore();

  const { getFieldByKey } = useFieldDefinitions();

  // Field Groups
  const painPointFields = useMemo(() => {
    return Object.keys(PAIN_POINT_SOLUTIONS)
      .map(key => getFieldByKey(key))
      .filter(isFieldDefinition);
  }, [getFieldByKey]);

  const revenueField = useMemo(() => {
    return getFieldByKey('expectedAnnualRevenue');
  }, [getFieldByKey]);

  const additionalFields = useMemo(() => {
    return ['vendingInterest', 'vendingLocations']
      .map(key => getFieldByKey(key))
      .filter(isFieldDefinition);
  }, [getFieldByKey]);

  // Active Pain Points
  const activePainPoints = useMemo(() => {
    return Object.entries(PAIN_POINT_SOLUTIONS)
      .filter(([key]) => customerData[key] === 'ja')
      .map(([key, data]) => ({ key, ...data }));
  }, [customerData]);

  // Handlers
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

  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Schritt 2: Herausforderungen & Potenzial
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Identifizieren Sie Ihre unternehmensweiten Herausforderungen und entdecken Sie Ihr Potenzial
        mit Freshfoodz.
      </Typography>

      {/* 1. Pain Points */}
      <GlobalChallengesSection
        painPointFields={painPointFields}
        values={customerData}
        errors={validationErrors}
        onChange={handleFieldChange}
        onBlur={handleFieldBlur}
      />

      {/* 2. Umsatzerwartung */}
      {revenueField && (
        <RevenueExpectationSectionV2
          revenueField={revenueField}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
          totalLocations={customerData.totalLocationsEU || 1}
          activePainPoints={activePainPoints.map(p => p.key)}
        />
      )}

      {/* 3. Zusatzgeschäft */}
      <AdditionalBusinessSection
        additionalFields={additionalFields}
        values={customerData}
        errors={validationErrors}
        onChange={handleFieldChange}
        onBlur={handleFieldBlur}
      />

      {/* Solutions für ausgewählte Pain Points */}
      {activePainPoints.length > 0 && (
        <Box sx={{ mt: 4 }}>
          <Typography variant="h6" gutterBottom>
            💡 Freshfoodz Lösungen für Ihre Herausforderungen
          </Typography>
          <Grid container spacing={2}>
            {activePainPoints.map(point => (
              <Grid size={{ xs: 12, md: 6 }} key={point.key}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                      {point.title}
                    </Typography>
                    <Typography variant="body2" color="success.main" gutterBottom>
                      ✅ {point.solution}
                    </Typography>
                    <Box sx={{ mt: 1 }}>
                      {point.products.map(product => (
                        <Chip
                          key={product}
                          label={product}
                          size="small"
                          sx={{ mr: 0.5, mb: 0.5 }}
                        />
                      ))}
                    </Box>
                    <Typography
                      variant="caption"
                      color="text.secondary"
                      display="block"
                      sx={{ mt: 1 }}
                    >
                      {point.impact}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>
      )}
    </Box>
  );
};
