/**
 * Step 2: Angebot & Pain Points
 * 
 * Erfasst Angebotsstruktur, Pain Points und zeigt Lösungen.
 * Live-Potenzialberechnung basierend auf Eingaben.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS.md
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
  CardContent,
  Grid
} from '@mui/material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { FieldDefinition } from '../../types/FieldDefinition';
import { debugDropdownSizes } from '../../utils/fieldSizeCalculator';

// Pain Point Solutions Mapping
const PAIN_POINT_SOLUTIONS = {
  hasStaffingIssues: {
    title: 'Personalmangel',
    solution: 'Cook&Fresh® - Keine Fachkräfte nötig!',
    products: ['Convenience Line', 'Ready-to-Serve'],
    impact: '30% weniger Personalkosten'
  },
  hasQualityIssues: {
    title: 'Schwankende Qualität',
    solution: 'Immer gleiche Premium-Qualität',
    products: ['Standardisierte Rezepturen'],
    impact: 'Konstante Gästezufriedenheit'
  },
  hasFoodWasteIssues: {
    title: 'Lebensmittelverschwendung',
    solution: '40 Tage Haltbarkeit ohne Qualitätsverlust',
    products: ['Cook&Fresh® Verfahren'],
    impact: 'Bis zu 50% weniger Abfall'
  },
  hasCostPressure: {
    title: 'Kostendruck',
    solution: 'Kalkulierbare Kosten, weniger Personal',
    products: ['Efficiency Line'],
    impact: 'Transparente Preisgestaltung'
  },
  hasFlexibilityIssues: {
    title: 'Schwankende Gästezahlen',
    solution: 'Portionsgenaue Bestellung möglich',
    products: ['Flexible Packaging'],
    impact: 'Keine Überproduktion'
  },
  hasDietComplexity: {
    title: 'Diät-/Allergieanforderungen',
    solution: 'Komplettes Sortiment für alle Anforderungen',
    products: ['Special Diet Line', 'Allergen-Free'],
    impact: 'Alle Gäste zufrieden'
  }
};

export const Step2AngebotPainpoints: React.FC = () => {
  const {
    customerData,
    validationErrors,
    setCustomerField,
    validateField
  } = useCustomerOnboardingStore();
  
  const { getFieldByKey, fieldDefinitions } = useFieldDefinitions();
  
  // DEBUG: Dropdown-Größen analysieren
  React.useEffect(() => {
    if (process.env.NODE_ENV === 'development') {
      debugDropdownSizes(fieldDefinitions);
    }
  }, [fieldDefinitions]);
  
  // Get industry-specific service fields
  const serviceFields = useMemo(() => {
    const industry = customerData.industry;
    if (!industry) return [];
    
    // Map industry to service field keys
    const serviceFieldKeys: Record<string, string[]> = {
      hotel: [
        'offersBreakfast',
        'breakfastWarm',
        'breakfastGuestsPerDay',
        'offersLunch',
        'offersDinner',
        'offersRoomService',
        'offersEvents',
        'eventCapacity',
        'roomCount',
        'averageOccupancy'
      ],
      krankenhaus: [
        'bedCount',
        'cateringSystem',
        'dietRequirements',
        'privatePatientShare'
      ],
      // Add more industries as needed
    };
    
    const keys = serviceFieldKeys[industry] || [];
    return keys.map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
  }, [customerData.industry, getFieldByKey]);
  
  // Pain point fields
  const painPointFields = useMemo(() => {
    return Object.keys(PAIN_POINT_SOLUTIONS)
      .map(key => getFieldByKey(key))
      .filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);
  
  // Additional business fields
  const additionalFields = useMemo(() => {
    return [
      'vendingInterest',
      'vendingLocations'
    ].map(key => getFieldByKey(key)).filter(Boolean) as FieldDefinition[];
  }, [getFieldByKey]);
  
  // Calculate potential based on services and pain points
  const calculatedPotential = useMemo(() => {
    let basePotential = 0;
    
    // Hotel-specific calculation
    if (customerData.industry === 'hotel') {
      const breakfastGuests = Number(customerData.breakfastGuestsPerDay) || 0;
      const roomCount = Number(customerData.roomCount) || 0;
      const occupancy = Number(customerData.averageOccupancy) || 75;
      
      // Breakfast potential
      if (customerData.offersBreakfast) {
        basePotential += breakfastGuests * 8 * 30; // 8€ per guest, 30 days
        if (customerData.breakfastWarm) {
          basePotential *= 1.3; // 30% more for warm breakfast
        }
      }
      
      // Room service potential
      if (customerData.offersRoomService) {
        basePotential += roomCount * occupancy * 0.01 * 15 * 30; // 1% order rate, 15€ avg, 30 days
      }
    }
    
    // Apply pain point multipliers
    if (customerData.hasStaffingIssues) basePotential *= 1.3;
    if (customerData.hasQualityIssues) basePotential *= 1.1;
    
    // Chain multiplier
    if (customerData.chainCustomer === 'ja') {
      const locations = Number(customerData.totalLocationsEU) || 1;
      basePotential *= locations;
    }
    
    return Math.round(basePotential);
  }, [customerData]);
  
  // Active pain points
  const activePainPoints = useMemo(() => {
    return Object.entries(PAIN_POINT_SOLUTIONS)
      .filter(([key]) => customerData[key] === true)
      .map(([key, data]) => ({ key, ...data }));
  }, [customerData]);
  
  const handleFieldChange = useCallback((fieldKey: string, value: any) => {
    setCustomerField(fieldKey, value);
  }, [setCustomerField]);
  
  const handleFieldBlur = useCallback((fieldKey: string) => {
    validateField(fieldKey);
  }, [validateField]);
  
  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Schritt 2: Angebot & Pain Points
      </Typography>
      
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Verstehen Sie das Angebot und identifizieren Sie Vertriebschancen.
      </Typography>
      
      {/* Service Structure */}
      {serviceFields.length > 0 && (
        <Box sx={{ mb: 4 }}>
          <Typography variant="h6" gutterBottom>
            🍳 Angebotsstruktur
          </Typography>
          <Alert severity="info" sx={{ mb: 2 }}>
            Was wird angeboten? Dies hilft uns, den Bedarf einzuschätzen.
          </Alert>
          <DynamicFieldRenderer
            fields={serviceFields}
            values={customerData}
            errors={validationErrors}
            onChange={handleFieldChange}
            onBlur={handleFieldBlur}
            useAdaptiveLayout={true}
          />
        </Box>
      )}
      
      <Divider sx={{ my: 3 }} />
      
      {/* Pain Points */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          🎯 Herausforderungen (Pain Points)
        </Typography>
        <Alert severity="warning" sx={{ mb: 2 }}>
          <AlertTitle>Wo drückt der Schuh?</AlertTitle>
          Jede Herausforderung ist eine Verkaufschance!
        </Alert>
        <DynamicFieldRenderer
          fields={painPointFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
          useAdaptiveLayout={true}
        />
      </Box>
      
      {/* Solutions for selected pain points */}
      {activePainPoints.length > 0 && (
        <>
          <Divider sx={{ my: 3 }} />
          <Box sx={{ mb: 4 }}>
            <Typography variant="h6" gutterBottom>
              💡 Freshfoodz Lösungen
            </Typography>
            <Grid container spacing={2}>
              {activePainPoints.map(point => (
                <Grid item xs={12} md={6} key={point.key}>
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
                      <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>
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
      
      <Divider sx={{ my: 3 }} />
      
      {/* Additional Business */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          🤖 Zusatzgeschäft
        </Typography>
        <DynamicFieldRenderer
          fields={additionalFields}
          values={customerData}
          errors={validationErrors}
          onChange={handleFieldChange}
          onBlur={handleFieldBlur}
          useAdaptiveLayout={true}
        />
      </Box>
      
      {/* Live Potential Calculation */}
      {calculatedPotential > 0 && (
        <Card sx={{ bgcolor: 'success.light', mt: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              📊 Geschätztes Monatspotenzial
            </Typography>
            <Typography variant="h3" color="success.dark">
              {calculatedPotential.toLocaleString('de-DE')} €
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Basierend auf Ihren Angaben
            </Typography>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};