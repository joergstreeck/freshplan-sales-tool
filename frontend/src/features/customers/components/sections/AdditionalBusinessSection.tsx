/**
 * AdditionalBusinessSection Component
 *
 * Erfasst Zusatzgeschäft wie Vending/Automaten (bonPeti)
 * auf globaler Unternehmensebene.
 */

import React from 'react';
import { Box, Typography, Paper, Chip } from '@mui/material';
import LocalDrinkIcon from '@mui/icons-material/LocalDrink';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { FieldDefinition } from '../../types/field.types';

interface AdditionalBusinessSectionProps {
  /** Zusatzgeschäft-Felder */
  additionalFields: FieldDefinition[];
  /** Aktuelle Werte */
  values: Record<string, any>;
  /** Validierungsfehler */
  errors: Record<string, string>;
  /** Change Handler */
  onChange: (fieldKey: string, value: any) => void;
  /** Blur Handler */
  onBlur: (fieldKey: string) => void;
}

export const AdditionalBusinessSection: React.FC<AdditionalBusinessSectionProps> = ({
  additionalFields,
  values,
  errors,
  onChange,
  onBlur,
}) => {
  const hasVendingInterest = values.vendingInterest === 'ja';
  const vendingLocations = values.vendingLocations || 0;

  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        🤖 Zusatzgeschäft
        <Chip label="bonPeti" size="small" color="secondary" variant="outlined" />
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        24/7 Versorgung für Mitarbeiter und Gäste
      </Typography>

      <DynamicFieldRenderer
        fields={additionalFields}
        values={values}
        errors={errors}
        onChange={onChange}
        onBlur={onBlur}
        useAdaptiveLayout={true}
      />

      {hasVendingInterest && vendingLocations > 0 && (
        <Paper
          elevation={0}
          sx={{
            p: 2,
            mt: 2,
            bgcolor: 'secondary.light',
            display: 'flex',
            alignItems: 'center',
            gap: 2,
          }}
        >
          <LocalDrinkIcon sx={{ fontSize: 40, color: 'secondary.main' }} />
          <Box>
            <Typography variant="subtitle2" color="secondary.dark">
              Großes Potenzial erkannt!
            </Typography>
            <Typography variant="body2">
              {vendingLocations} Automaten-Standorte × 12 Monate ={' '}
              <strong>{(vendingLocations * 1500).toLocaleString('de-DE')}€</strong> zusätzliches
              Jahrespotenzial
            </Typography>
            <Typography variant="caption" color="text.secondary">
              (Durchschnitt: 1.500€ pro Automat/Monat)
            </Typography>
          </Box>
        </Paper>
      )}
    </Box>
  );
};
