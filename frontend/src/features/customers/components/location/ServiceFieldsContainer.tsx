/**
 * Service Fields Container
 *
 * Sprint 2.1.7.x: fieldCatalog.json Migration - Server-Driven UI
 *
 * Renders industry-specific service fields based on backend schema.
 *
 * BEFORE (Legacy): Hardcoded field definitions in component (lines 38-173)
 * AFTER (Current): Field definitions loaded from backend API
 *
 * @see LocationServiceSchemaResource.java (backend endpoint)
 * @see useLocationServiceSchema.ts (frontend hook)
 */

import React from 'react';
import { Box, Typography, Paper, CircularProgress } from '@mui/material';
import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import type { CustomerLocation } from '../../types/customer.types';
import type { LocationServiceData } from '../../stores/locationServicesStore';
import { useLocationServiceSchema } from '../../../../hooks/useLocationServiceSchema';

interface ServiceFieldsContainerProps {
  location: CustomerLocation;
  services: LocationServiceData;
  onChange: (field: string, value: unknown) => void;
  onBlur?: (field: string) => void;
  industry: string;
  errors?: Record<string, string>;
}

export const ServiceFieldsContainer: React.FC<ServiceFieldsContainerProps> = ({
  location: _location,
  services,
  onChange,
  onBlur = () => {},
  industry,
  errors = {},
}) => {
  // Sprint 2.1.7.x: Load service field groups from backend (Server-Driven UI)
  const { data: serviceGroups, isLoading } = useLocationServiceSchema(industry);

  // Loading state
  if (isLoading) {
    return (
      <Paper variant="outlined" sx={{ p: 3, textAlign: 'center' }}>
        <CircularProgress size={24} />
        <Typography color="text.secondary" sx={{ mt: 2 }}>
          Service-Felder werden geladen...
        </Typography>
      </Paper>
    );
  }

  // Empty state - no service groups defined for this industry
  if (!serviceGroups || serviceGroups.length === 0) {
    return (
      <Paper variant="outlined" sx={{ p: 3, textAlign: 'center' }}>
        <Typography color="text.secondary">
          Für die Branche "{industry}" sind noch keine Service-Felder definiert.
        </Typography>
      </Paper>
    );
  }

  return (
    <AdaptiveFormContainer>
      {serviceGroups.map(group => (
        <Box key={group.id} sx={{ mb: 4 }}>
          <Typography
            variant="h6"
            gutterBottom
            sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
          >
            <span>{group.icon}</span>
            <span>{group.title}</span>
          </Typography>

          <Paper variant="outlined" sx={{ p: 2 }}>
            <DynamicFieldRenderer
              fields={group.fields}
              values={services}
              errors={errors}
              onChange={onChange}
              onBlur={onBlur}
            />
          </Paper>
        </Box>
      ))}
    </AdaptiveFormContainer>
  );
};
