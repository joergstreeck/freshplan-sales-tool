/**
 * RevenueExpectationSection Component
 * 
 * Erfasst die Umsatzerwartung mit automatischer Kalkulation
 * basierend auf Standorten und Pain Points.
 */

import React from 'react';
import { Box, Typography, Paper, Chip, Alert, AlertTitle } from '@mui/material';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { FieldDefinition } from '../../types/field.types';

interface RevenueExpectationSectionProps {
  /** Umsatzerwartungs-Feld */
  revenueField: FieldDefinition;
  /** Aktuelle Werte */
  values: Record<string, any>;
  /** Validierungsfehler */
  errors: Record<string, string>;
  /** Change Handler */
  onChange: (fieldKey: string, value: any) => void;
  /** Blur Handler */
  onBlur: (fieldKey: string) => void;
  /** Anzahl Standorte für Kalkulation */
  totalLocations?: number;
  /** Aktive Pain Points für Multiplikator */
  activePainPoints?: string[];
}

export const RevenueExpectationSection: React.FC<RevenueExpectationSectionProps> = ({
  revenueField,
  values,
  errors,
  onChange,
  onBlur,
  totalLocations = 1,
  activePainPoints = []
}) => {
  // Kalkulationshilfe
  const calculateSuggestion = () => {
    const basePerLocation = 5500; // Durchschnitt pro Standort/Monat
    const annualBase = totalLocations * basePerLocation * 12;
    
    // Pain Point Multiplikatoren
    let multiplier = 1;
    if (activePainPoints.includes('hasStaffingIssues')) multiplier += 0.3;
    if (activePainPoints.includes('hasQualityIssues')) multiplier += 0.1;
    if (activePainPoints.includes('hasFoodWasteIssues')) multiplier += 0.15;
    if (activePainPoints.includes('hasCostPressure')) multiplier += 0.2;
    
    return Math.round(annualBase * multiplier);
  };

  const suggestion = calculateSuggestion();
  const currentValue = values[revenueField.key];

  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        💰 Geschäftspotenzial
        <Chip 
          label="Vertragsrelevant" 
          size="small" 
          color="error"
          variant="outlined"
        />
      </Typography>

      <Alert severity="info" sx={{ mb: 2 }}>
        <AlertTitle>Basis für Ihren Partnerschaftsvertrag</AlertTitle>
        Diese Zahl fließt direkt in die Rahmenkonditionen und Mengenrabatte ein.
      </Alert>

      <DynamicFieldRenderer
        fields={[revenueField]}
        values={values}
        errors={errors}
        onChange={onChange}
        onBlur={onBlur}
        useAdaptiveLayout={true}
      />

      {/* Kalkulationshilfe */}
      <Paper 
        variant="outlined" 
        sx={{ 
          p: 2, 
          mt: 2, 
          bgcolor: 'background.default',
          borderColor: 'primary.main',
          borderWidth: 2
        }}
      >
        <Typography variant="subtitle2" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <TrendingUpIcon color="primary" />
          Automatische Kalkulation
        </Typography>
        
        <Box sx={{ mt: 1 }}>
          <Typography variant="body2" color="text.secondary">
            • {totalLocations} Standort{totalLocations > 1 ? 'e' : ''} × Ø 5.500€/Monat = {(totalLocations * 5500 * 12).toLocaleString('de-DE')}€
          </Typography>
          
          {activePainPoints.length > 0 && (
            <Typography variant="body2" color="text.secondary">
              • Mit {activePainPoints.length} Pain Points: 
              {' '}<strong>+{Math.round((calculateSuggestion() / (totalLocations * 5500 * 12) - 1) * 100)}%</strong>
            </Typography>
          )}
          
          <Typography variant="body1" sx={{ mt: 1, fontWeight: 'bold', color: 'primary.main' }}>
            Empfohlenes Jahresvolumen: {suggestion.toLocaleString('de-DE')}€
          </Typography>
        </Box>

        {currentValue && currentValue !== suggestion && (
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
            Ihre Eingabe: {Number(currentValue).toLocaleString('de-DE')}€
            {currentValue > suggestion && ' (ambitioniert 🚀)'}
            {currentValue < suggestion && ' (konservativ)'}
          </Typography>
        )}
      </Paper>
    </Box>
  );
};