/**
 * RevenueExpectationSectionV2 Component
 *
 * Erfasst die Umsatzerwartung mit EUR-Formatierung und Live-Calculator.
 * Nutzt den neuen EURInput Component mit verbesserter UX.
 */

import React from 'react';
import { Box, Typography, Chip, Alert, AlertTitle } from '@mui/material';
import { EURInput } from '../fields/EURInput';
import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';
import type { FieldDefinition } from '../../types/field.types';

interface RevenueExpectationSectionV2Props {
  /** Umsatzerwartungs-Feld */
  revenueField: FieldDefinition;
  /** Aktuelle Werte */
  values: Record<string, unknown>;
  /** Validierungsfehler */
  errors: Record<string, string>;
  /** Change Handler */
  onChange: (fieldKey: string, value: unknown) => void;
  /** Blur Handler */
  onBlur: (fieldKey: string) => void;
  /** Anzahl Standorte für Kalkulation */
  totalLocations?: number;
  /** Aktive Pain Points für Multiplikator */
  activePainPoints?: string[];
}

export const RevenueExpectationSectionV2: React.FC<RevenueExpectationSectionV2Props> = ({
  revenueField,
  values,
  errors,
  onChange,
  onBlur,
  totalLocations = 1,
  activePainPoints = [],
}) => {
  const currentValue = values[revenueField.key] || 0;
  const hasError = Boolean(errors[revenueField.key]);

  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        💰 Geschäftspotenzial
        <Chip label="Vertragsrelevant" size="small" color="error" variant="outlined" />
      </Typography>

      <Alert severity="info" sx={{ mb: 2 }}>
        <AlertTitle>Basis für Ihren Partnerschaftsvertrag</AlertTitle>
        Diese Zahl fließt direkt in die Rahmenkonditionen und Mengenrabatte ein.
      </Alert>

      <AdaptiveFormContainer>
        <Box sx={{ width: '100%', maxWidth: 400 }}>
          <EURInput
            value={currentValue}
            onChange={value => onChange(revenueField.key, value)}
            onBlur={() => onBlur(revenueField.key)}
            label={revenueField.label}
            required={revenueField.required}
            error={hasError}
            helperText={errors[revenueField.key] || revenueField.helpText}
            showCalculator={revenueField.showCalculator}
            calculatorHint={revenueField.calculatorHint}
            validationWarning={revenueField.validationWarning}
            fullWidth={false}
          />
        </Box>
      </AdaptiveFormContainer>
    </Box>
  );
};
