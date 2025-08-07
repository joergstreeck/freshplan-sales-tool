/**
 * LocationServicesSection Component
 *
 * Erfasst die Angebotsstruktur für den ausgewählten Standort
 * oder alle Standorte mit der AdaptiveFormContainer Theme.
 */

import React from 'react';
import { Box, Typography, Alert } from '@mui/material';
import { styled } from '@mui/material/styles';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { FieldDefinition } from '../../types/field.types';

const SectionContainer = styled(Box)(({ theme }) => ({
  marginBottom: theme.spacing(2),
}));

const FieldGroupContainer = styled(Box)(({ theme }) => ({
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: theme.shape.borderRadius,
  padding: theme.spacing(2),
  marginBottom: theme.spacing(2),
  backgroundColor: theme.palette.background.default,

  '& .group-title': {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(1),
    marginBottom: theme.spacing(2),

    '& .icon': {
      fontSize: '1.25rem',
    },
  },
}));

interface ServiceFieldGroup {
  title: string;
  icon?: string;
  fields: FieldDefinition[];
}

interface LocationServicesSectionProps {
  /** Feld-Gruppen nach Kategorie */
  serviceFieldGroups: ServiceFieldGroup[];
  /** Aktuelle Werte */
  values: Record<string, any>;
  /** Validierungsfehler */
  errors: Record<string, string>;
  /** Change Handler */
  onChange: (fieldKey: string, value: any) => void;
  /** Blur Handler */
  onBlur: (fieldKey: string) => void;
  /** Ausgewählter Standort */
  selectedLocationId: string | 'all';
  /** Name des Standorts (optional) */
  locationName?: string;
}

export const LocationServicesSection: React.FC<LocationServicesSectionProps> = ({
  serviceFieldGroups,
  values,
  errors,
  onChange,
  onBlur,
  selectedLocationId,
  locationName,
}) => {
  const isAllLocations = selectedLocationId === 'all';

  return (
    <SectionContainer>
      <Typography variant="h6" gutterBottom>
        🍳 Angebotsstruktur
        {isAllLocations ? ': Alle Standorte' : locationName ? `: ${locationName}` : ''}
      </Typography>

      <Alert severity="info" sx={{ mb: 3 }}>
        Was wird angeboten? Dies hilft uns, den spezifischen Bedarf{' '}
        {isAllLocations ? 'aller Standorte' : 'dieses Standorts'} einzuschätzen.
      </Alert>

      {serviceFieldGroups.map((group, index) => (
        <FieldGroupContainer key={index}>
          <div className="group-title">
            {group.icon && <span className="icon">{group.icon}</span>}
            <Typography variant="subtitle1" component="h4" fontWeight="medium">
              {group.title}
            </Typography>
          </div>

          <DynamicFieldRenderer
            fields={group.fields}
            values={values}
            errors={errors}
            onChange={onChange}
            onBlur={onBlur}
            useAdaptiveLayout={true}
          />
        </FieldGroupContainer>
      ))}

      {serviceFieldGroups.length === 0 && (
        <Alert severity="warning">
          Für die ausgewählte Branche sind noch keine spezifischen Angebotsfelder definiert.
        </Alert>
      )}
    </SectionContainer>
  );
};
