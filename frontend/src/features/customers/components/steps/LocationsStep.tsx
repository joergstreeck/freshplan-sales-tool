/**
 * Locations Step Component
 *
 * Second step of the wizard - manages locations for chain customers.
 * Only shown when chainCustomer = 'ja'.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/01-components.md
 */

import React from 'react';
import { Box, Typography, Button, Card, CardContent, IconButton, Grid, Alert } from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Business as BusinessIcon,
} from '@mui/icons-material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';

/**
 * Locations Step
 *
 * Manages multiple locations for chain customers.
 * Each location has its own set of fields from the Field Catalog.
 */
export const LocationsStep: React.FC = () => {
  const {
    locations,
    locationFieldValues,
    customerData,
    addLocation,
    removeLocation,
    setLocationField,
    validationErrors,
  } = useCustomerOnboardingStore();

  const { locationFields, getIndustryFields: _getIndustryFields } = useFieldDefinitions();

  // Get industry-specific location fields
  const industry = customerData.industry || '';
  const industryLocationFields = React.useMemo(() => {
    // For locations, we might have different industry fields
    // This is a placeholder - adjust based on actual field catalog structure
    return [];
  }, [industry]);

  const allLocationFields = [...locationFields, ...industryLocationFields];

  /**
   * Handle location field change
   */
  const handleLocationFieldChange = (locationId: string, fieldKey: string, value: unknown) => {
    setLocationField(locationId, fieldKey, value);
  };

  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Standorte verwalten
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Fügen Sie alle Standorte Ihres Unternehmens hinzu. Jeder Standort kann eigene Kontaktdaten
        und Details haben.
      </Typography>

      {locations.length === 0 && (
        <Alert severity="info" sx={{ mb: 3 }}>
          Sie haben noch keine Standorte hinzugefügt. Klicken Sie auf "Standort hinzufügen" um zu
          beginnen.
        </Alert>
      )}

      {/* Location Cards */}
      <Grid container spacing={3}>
        {locations.map((location, index) => (
          <Grid size={{ xs: 12 }} key={location.id}>
            <Card variant="outlined">
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <BusinessIcon sx={{ mr: 1, color: 'primary.main' }} />
                  <Typography variant="h6" sx={{ flexGrow: 1 }}>
                    Standort {index + 1}
                  </Typography>
                  <IconButton
                    onClick={() => removeLocation(location.id)}
                    color="error"
                    size="small"
                    title="Standort entfernen"
                  >
                    <DeleteIcon />
                  </IconButton>
                </Box>

                {/* Location Fields */}
                <DynamicFieldRenderer
                  fields={allLocationFields}
                  values={locationFieldValues[location.id] || {}}
                  errors={validationErrors}
                  onChange={(fieldKey, value) =>
                    handleLocationFieldChange(location.id, fieldKey, value)
                  }
                  onBlur={() => {}}
                />
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Add Location Button */}
      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'center' }}>
        <Button variant="outlined" startIcon={<AddIcon />} onClick={addLocation} size="large">
          Standort hinzufügen
        </Button>
      </Box>

      {/* Summary */}
      {locations.length > 0 && (
        <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
          <Typography variant="body2" color="text.secondary">
            <strong>
              {locations.length} {locations.length === 1 ? 'Standort' : 'Standorte'}
            </strong>{' '}
            erfasst.
            {industry === 'hotel' && ' Erfassen Sie für jedes Hotel einen eigenen Standort.'}
            {industry === 'krankenhaus' && ' Erfassen Sie für jede Klinik einen eigenen Standort.'}
          </Typography>
        </Box>
      )}
    </Box>
  );
};
