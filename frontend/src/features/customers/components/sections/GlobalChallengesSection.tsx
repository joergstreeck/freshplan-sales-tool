/**
 * GlobalChallengesSection Component
 *
 * Erfasst die unternehmensweiten Pain Points (Herausforderungen)
 * als ersten Teil von Step 2.
 */

import React from 'react';
import { Box, Typography, Alert } from '@mui/material';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { FieldDefinition } from '../../types/field.types';

interface GlobalChallengesSectionProps {
  /** Pain Point Felder */
  painPointFields: FieldDefinition[];
  /** Aktuelle Werte */
  values: Record<string, any>;
  /** Validierungsfehler */
  errors: Record<string, string>;
  /** Change Handler */
  onChange: (fieldKey: string, value: any) => void;
  /** Blur Handler */
  onBlur: (fieldKey: string) => void;
}

export const GlobalChallengesSection: React.FC<GlobalChallengesSectionProps> = ({
  painPointFields,
  values,
  errors,
  onChange,
  onBlur,
}) => {
  // Zähle aktive Pain Points
  const activePainPoints = painPointFields.filter(field => values[field.key] === 'ja').length;

  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h6" gutterBottom>
        🎯 Ihre aktuellen Herausforderungen
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        (Unternehmensweit - gilt für alle Standorte)
      </Typography>

      {activePainPoints > 0 && (
        <Alert severity="success" sx={{ mb: 2 }}>
          <strong>
            {activePainPoints} Herausforderung{activePainPoints > 1 ? 'en' : ''} identifiziert!
          </strong>{' '}
          Je mehr Herausforderungen, desto größer das Potenzial mit Freshfoodz.
        </Alert>
      )}

      <DynamicFieldRenderer
        fields={painPointFields}
        values={values}
        errors={errors}
        onChange={onChange}
        onBlur={onBlur}
        useAdaptiveLayout={true}
      />
    </Box>
  );
};
