/**
 * Step 2: Herausforderungen & Potenzial (V2)
 *
 * Neue Struktur: Pain Points → Umsatzerwartung → Zusatzgeschäft → Angebotsstruktur pro Filiale
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS_V2.md
 */

import React, { useCallback, useMemo } from 'react';
import { Box, Typography, Divider, Card, CardContent, Grid, Chip } from '@mui/material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';

// Import neue Komponenten
import { LocationSelector } from '../location/LocationSelector';
import { GlobalChallengesSection } from '../sections/GlobalChallengesSection';
import { RevenueExpectationSectionV2 } from '../sections/RevenueExpectationSectionV2';
import { AdditionalBusinessSection } from '../sections/AdditionalBusinessSection';
import { LocationServicesSection } from '../sections/LocationServicesSection';

// Import Store Extensions
import type { FieldDefinition } from '../../types/field.types';

// Pain Point Solutions Mapping (unverändert)
const PAIN_POINT_SOLUTIONS = {
  hasStaffingIssues: {
    title: 'Personalmangel',
    solution: 'Cook&Fresh® - Keine Fachkräfte nötig!',
    products: ['Convenience Line', 'Ready-to-Serve'],
    impact: '30% weniger Personalkosten',
  },
  hasQualityIssues: {
    title: 'Schwankende Qualität',
    solution: 'Immer gleiche Premium-Qualität',
    products: ['Standardisierte Rezepturen'],
    impact: 'Konstante Gästezufriedenheit',
  },
  hasFoodWasteIssues: {
    title: 'Lebensmittelverschwendung',
    solution: '40 Tage Haltbarkeit ohne Qualitätsverlust',
    products: ['Cook&Fresh® Verfahren'],
    impact: 'Bis zu 50% weniger Abfall',
  },
  hasCostPressure: {
    title: 'Kostendruck',
    solution: 'Kalkulierbare Kosten, weniger Personal',
    products: ['Efficiency Line'],
    impact: 'Transparente Preisgestaltung',
  },
  hasFlexibilityIssues: {
    title: 'Schwankende Gästezahlen',
    solution: 'Portionsgenaue Bestellung möglich',
    products: ['Flexible Packaging'],
    impact: 'Keine Überproduktion',
  },
  hasDietComplexity: {
    title: 'Diät-/Allergieanforderungen',
    solution: 'Komplettes Sortiment für alle Anforderungen',
    products: ['Special Diet Line', 'Allergen-Free'],
    impact: 'Alle Gäste zufrieden',
  },
};

export const Step2AngebotPainpointsV2: React.FC = () => {
  const {
    customerData,
    locations,
    validationErrors,
    setCustomerField,
    validateField,
    // Store Extensions
    selectedLocationId,
    applyToAllLocations,
    completedLocationIds,
    setSelectedLocation,
    setApplyToAll,
    saveLocationServices,
    getLocationServices,
  } = useCustomerOnboardingStore();

  const { getFieldByKey, fieldDefinitions: _fieldDefinitions } = useFieldDefinitions();

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

  // Service Field Groups basierend auf Branche
  const serviceFieldGroups = useMemo(() => {
    const industry = customerData.industry;
    if (!industry) return [];

    if (industry === 'hotel') {
      return [
        {
          title: 'Frühstückgeschäft',
          icon: '☕',
          fields: ['offersBreakfast', 'breakfastWarm', 'breakfastGuestsPerDay']
            .map(key => getFieldByKey(key))
            .filter(isFieldDefinition),
        },
        {
          title: 'Mittag- und Abendessen',
          icon: '🍽️',
          fields: ['offersLunch', 'offersDinner']
            .map(key => getFieldByKey(key))
            .filter(isFieldDefinition),
        },
        {
          title: 'Zusatzservices',
          icon: '🛎️',
          fields: ['offersRoomService', 'offersEvents', 'eventCapacity']
            .map(key => getFieldByKey(key))
            .filter(isFieldDefinition),
        },
        {
          title: 'Kapazität',
          icon: '🏨',
          fields: ['roomCount', 'averageOccupancy']
            .map(key => getFieldByKey(key))
            .filter(isFieldDefinition),
        },
      ].filter(group => group.fields.length > 0);
    }

    // TODO: Andere Branchen implementieren
    return [];
  }, [customerData.industry, getFieldByKey]);

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

  const handleLocationChange = useCallback(
    (locationId: string | 'all') => {
      setSelectedLocation(locationId);
    },
    [setSelectedLocation]
  );

  const handleApplyToAllChange = useCallback(
    (value: boolean) => {
      setApplyToAll(value);
    },
    [setApplyToAll]
  );

  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Schritt 2: Herausforderungen & Potenzial
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Identifizieren Sie Ihre Herausforderungen und entdecken Sie Ihr Potenzial mit Freshfoodz.
      </Typography>

      {/* TEIL 1: Globale Unternehmensebene */}

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
        <>
          <Divider sx={{ my: 3 }} />
          <Box sx={{ mb: 4 }}>
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
        </>
      )}

      <Divider sx={{ my: 4 }} />

      {/* TEIL 2: Filialspezifische Ebene */}

      {/* 4. Standortauswahl */}
      {customerData.chainCustomer === 'ja' && locations.length > 0 && (
        <LocationSelector
          locations={locations}
          selectedLocationId={selectedLocationId}
          onLocationChange={handleLocationChange}
          applyToAll={applyToAllLocations}
          onApplyToAllChange={handleApplyToAllChange}
          totalLocations={customerData.totalLocationsEU || locations.length}
          completedLocationIds={completedLocationIds}
        />
      )}

      {/* 5. Angebotsstruktur */}
      {serviceFieldGroups.length > 0 && (
        <LocationServicesSection
          serviceFieldGroups={serviceFieldGroups}
          values={{
            ...customerData,
            ...getLocationServices(selectedLocationId),
          }}
          errors={validationErrors}
          onChange={(fieldKey, value) => {
            // Speichere in locationServices statt customerData
            const currentServices = getLocationServices(selectedLocationId);
            saveLocationServices({
              ...currentServices,
              [fieldKey]: value,
            });
          }}
          onBlur={handleFieldBlur}
          selectedLocationId={selectedLocationId}
          locationName={
            selectedLocationId === 'all'
              ? undefined
              : locations.find(l => l.id === selectedLocationId)?.name || 'Standort'
          }
        />
      )}
    </Box>
  );
};
