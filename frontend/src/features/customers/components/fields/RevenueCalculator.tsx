import React, { useState, useEffect } from 'react';
import { Paper, Box, Typography, Divider, Button, Chip } from '@mui/material';
import CalculateIcon from '@mui/icons-material/Calculate';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { formatEUR } from '../../utils/currencyFormatter';

interface RevenueCalculatorProps {
  currentValue: number;
  hint?: string;
  onApplyCalculation?: (value: number) => void;
}

export const RevenueCalculator: React.FC<RevenueCalculatorProps> = ({
  currentValue,
  hint,
  onApplyCalculation,
}) => {
  const { customerData, locations } = useCustomerOnboardingStore();
  const [calculation, setCalculation] = useState({
    locationCount: 0,
    avgRevenuePerLocation: 5500,
    painPointBonus: 0,
    baseRevenue: 0,
    totalRevenue: 0,
  });

  // Berechne Pain Point Bonus
  const calculatePainPointBonus = () => {
    let count = 0;
    const painPointFields = [
      'hasStaffingIssues',
      'hasQualityIssues',
      'hasFoodWasteIssues',
      'hasCostPressure',
      'hasFlexibilityIssues',
    ];

    painPointFields.forEach(field => {
      if (customerData[field] === 'ja') count++;
    });

    return count * 0.1; // 10% pro Pain Point
  };

  // Update Kalkulation
  useEffect(() => {
    const locationCount = customerData.totalLocationsEU || locations.length || 1;
    const painPointBonus = calculatePainPointBonus();
    const avgRevenue = getAverageRevenueByIndustry(customerData.industry);
    const baseRevenue = locationCount * avgRevenue * 12; // Jahresbasis
    const totalRevenue = baseRevenue * (1 + painPointBonus);

    setCalculation({
      locationCount,
      avgRevenuePerLocation: avgRevenue,
      painPointBonus,
      baseRevenue,
      totalRevenue,
    });
  }, [customerData, locations]);

  // Branchen-spezifische Durchschnittswerte
  const getAverageRevenueByIndustry = (industry?: string): number => {
    const industryAverages: Record<string, number> = {
      hotel: 5500,
      krankenhaus: 8000,
      seniorenheim: 6500,
      betriebsrestaurant: 4500,
      restaurant: 3500,
      schule: 2500,
    };

    return industryAverages[industry || ''] || 5000;
  };

  return (
    <Paper
      variant="outlined"
      sx={{
        p: 2,
        mt: 1,
        backgroundColor: 'background.default',
        border: '1px solid',
        borderColor: 'primary.light',
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
        <CalculateIcon sx={{ mr: 1, color: 'primary.main' }} />
        <Typography variant="subtitle2" color="primary">
          Kalkulationshilfe
        </Typography>
      </Box>

      {/* Basis-Kalkulation */}
      <Box sx={{ mb: 2 }}>
        <Typography variant="body2" color="text.secondary">
          {calculation.locationCount} {calculation.locationCount === 1 ? 'Standort' : 'Standorte'}
          {' × '}
          {formatEUR(calculation.avgRevenuePerLocation)}/Monat
          {' × '}
          12 Monate
        </Typography>
        <Typography variant="body1" fontWeight="bold">
          = {formatEUR(calculation.baseRevenue)}
        </Typography>
      </Box>

      {/* Pain Point Bonus */}
      {calculation.painPointBonus > 0 && (
        <>
          <Divider sx={{ my: 1 }} />
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="success.dark">
              + {(calculation.painPointBonus * 100).toFixed(0)}% Potenzial durch Herausforderungen
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mt: 1 }}>
              {customerData.hasStaffingIssues === 'ja' && (
                <Chip label="Personalmangel" size="small" color="success" />
              )}
              {customerData.hasQualityIssues === 'ja' && (
                <Chip label="Qualität" size="small" color="success" />
              )}
              {customerData.hasFoodWasteIssues === 'ja' && (
                <Chip label="Food Waste" size="small" color="success" />
              )}
              {customerData.hasCostPressure === 'ja' && (
                <Chip label="Kostendruck" size="small" color="success" />
              )}
              {customerData.hasFlexibilityIssues === 'ja' && (
                <Chip label="Flexibilität" size="small" color="success" />
              )}
            </Box>
          </Box>
        </>
      )}

      {/* Gesamt */}
      <Divider sx={{ my: 1 }} />
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mt: 2,
        }}
      >
        <Box>
          <Typography variant="caption" color="text.secondary">
            Geschätztes Potenzial:
          </Typography>
          <Typography variant="h6" color="primary" fontWeight="bold">
            {formatEUR(calculation.totalRevenue)}
          </Typography>
          <Typography variant="caption" color="text.secondary" display="block">
            → gerundet: {formatEUR(Math.round(calculation.totalRevenue / 100000) * 100000)}
          </Typography>
        </Box>
        {onApplyCalculation && (
          <Button
            size="small"
            variant="contained"
            onClick={() => {
              // Runde auf 100.000
              const roundedValue = Math.round(calculation.totalRevenue / 100000) * 100000;
              console.log('RevenueCalculator applying:', roundedValue);
              onApplyCalculation(roundedValue);
            }}
          >
            Übernehmen
          </Button>
        )}
      </Box>

      {/* Hinweis */}
      {hint && (
        <Typography
          variant="caption"
          color="text.secondary"
          sx={{ display: 'block', mt: 2, fontStyle: 'italic' }}
        >
          💡 {hint}
        </Typography>
      )}
    </Paper>
  );
};
