/**
 * Step 4: Angebot & Leistungen je Filiale
 *
 * Erfasst standortspezifische Service-Informationen mit Progress-Tracking
 * und Bulk-Actions für effiziente Dateneingabe.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP4_ANGEBOT_SERVICES.md
 */

import React, { useCallback, useMemo, useState } from 'react';
import {
  Box,
  Typography,
  Button,
  FormControlLabel,
  Checkbox,
  Alert,
  Snackbar,
} from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import CheckIcon from '@mui/icons-material/Check';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useLocationServicesStore } from '../../stores/locationServicesStore';
import { LocationProgress } from '../location/LocationProgress';
import { LocationGrid } from '../location/LocationGrid';
import { LocationNavigator } from '../location/LocationNavigator';
import { ServiceFieldsContainer } from '../location/ServiceFieldsContainer';
import type { LocationServiceData } from '../../stores/locationServicesStore';

export const Step4AngebotServices: React.FC = () => {
  const { locations, customerData, chainCustomer } = useCustomerOnboardingStore();

  const {
    currentLocationIndex,
    completedLocationIds,
    applyToAll,
    saveLocationServices,
    setCurrentLocationIndex,
    setApplyToAll,
    applyToAllRemaining,
    getLocationServices,
  } = useLocationServicesStore();

  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [errors, _setErrors] = useState<Record<string, string>>({});

  // Check if single location mode
  const isSingleLocation = chainCustomer === 'nein' || locations.length === 1;
  const currentLocation = locations[currentLocationIndex];
  const currentServices = getLocationServices(currentLocation?.id) || {};

  // Handlers
  const handleServiceChange = useCallback(
    (field: string, value: unknown) => {
      if (!currentLocation) return;

      const updatedServices: LocationServiceData = {
        ...currentServices,
        [field]: value,
      };

      saveLocationServices(currentLocation.id, updatedServices);

      // Apply to all if checkbox is checked
      if (applyToAll && locations.length > 1) {
        applyToAllRemaining(locations, updatedServices);
      }
    },
    [
      currentLocation,
      currentServices,
      saveLocationServices,
      applyToAll,
      locations,
      completedLocationIds,
      applyToAllRemaining,
    ]
  );

  const handleCopyFrom = useCallback(
    (sourceLocationId: string) => {
      if (!currentLocation) return;

      const sourceServices = getLocationServices(sourceLocationId);
      if (sourceServices) {
        saveLocationServices(currentLocation.id, sourceServices);
        setShowSuccessMessage(true);
      }
    },
    [currentLocation, getLocationServices, saveLocationServices]
  );

  const handleNavigate = useCallback(
    (index: number) => {
      if (index >= 0 && index < locations.length) {
        setCurrentLocationIndex(index);
      }
    },
    [locations.length, setCurrentLocationIndex]
  );

  const handleSaveAndContinueLater = useCallback(() => {
    // Store is already persisted, just show message
    setShowSuccessMessage(true);
  }, []);

  // Progress calculation
  const progress = useMemo(() => {
    return {
      completed: completedLocationIds.length,
      total: locations.length,
    };
  }, [completedLocationIds.length, locations.length]);

  // Check if can proceed
  const canProceed = completedLocationIds.length === locations.length;
  const isLastLocation = currentLocationIndex === locations.length - 1;

  // Single location view
  if (isSingleLocation && currentLocation) {
    return (
      <Box>
        <Typography variant="h5" component="h2" gutterBottom>
          Schritt 4: Angebot & Leistungen
        </Typography>

        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Was bieten Sie Ihren Gästen an?
        </Typography>

        <ServiceFieldsContainer
          location={currentLocation}
          services={currentServices}
          onChange={handleServiceChange}
          industry={customerData.industry}
          errors={errors}
        />

        <Snackbar
          open={showSuccessMessage}
          autoHideDuration={3000}
          onClose={() => setShowSuccessMessage(false)}
          message="Daten gespeichert"
        />
      </Box>
    );
  }

  // Multi-location view
  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Schritt 4: Angebot & Leistungen je Filiale
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Erfassen Sie die angebotenen Services für jeden Standort.
      </Typography>

      {/* Progress */}
      <LocationProgress total={progress.total} completed={progress.completed} />

      {/* Location Grid */}
      <LocationGrid
        locations={locations}
        currentIndex={currentLocationIndex}
        completedIds={completedLocationIds}
        onLocationClick={handleNavigate}
      />

      {/* Location Navigator */}
      <LocationNavigator
        locations={locations}
        currentIndex={currentLocationIndex}
        completedIds={completedLocationIds}
        onNavigate={handleNavigate}
        onCopyFrom={handleCopyFrom}
      />

      {/* Apply to All Checkbox */}
      {locations.length > 1 && (
        <FormControlLabel
          control={
            <Checkbox checked={applyToAll} onChange={e => setApplyToAll(e.target.checked)} />
          }
          label="Für alle restlichen Standorte übernehmen"
          sx={{ mb: 2 }}
        />
      )}

      {/* Service Fields */}
      {currentLocation && (
        <>
          <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
            Services für: {currentLocation.name}
          </Typography>

          <ServiceFieldsContainer
            location={currentLocation}
            services={currentServices}
            onChange={handleServiceChange}
            industry={customerData.industry}
            errors={errors}
          />
        </>
      )}

      {/* Navigation Buttons */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
        <Button
          onClick={() => handleNavigate(currentLocationIndex - 1)}
          disabled={currentLocationIndex === 0}
        >
          Zurück
        </Button>

        <Button onClick={handleSaveAndContinueLater} startIcon={<SaveIcon />} variant="outlined">
          Speichern & später fortsetzen
        </Button>

        <Button
          onClick={() => {
            if (isLastLocation || canProceed) {
              // Wizard completion will be handled by parent
            } else {
              handleNavigate(currentLocationIndex + 1);
            }
          }}
          variant="contained"
          disabled={!currentLocation}
          endIcon={canProceed ? <CheckIcon /> : undefined}
        >
          {isLastLocation ? 'Wizard abschließen' : 'Weiter →'}
        </Button>
      </Box>

      {/* Success Alert */}
      {canProceed && (
        <Alert severity="success" sx={{ mt: 2 }}>
          Alle Standorte wurden erfasst! Sie können den Wizard jetzt abschließen.
        </Alert>
      )}

      <Snackbar
        open={showSuccessMessage}
        autoHideDuration={3000}
        onClose={() => setShowSuccessMessage(false)}
        message="Daten erfolgreich kopiert"
      />
    </Box>
  );
};
