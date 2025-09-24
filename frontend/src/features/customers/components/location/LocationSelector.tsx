/**
 * LocationSelector Component
 *
 * Ermöglicht die Auswahl eines Standorts oder "Alle Standorte"
 * für die Erfassung der Angebotsstruktur in Step 2.
 */

import React from 'react';
import {
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Checkbox,
  FormControlLabel,
  Typography,
  Divider,
  Chip,
} from '@mui/material';
import type { SelectChangeEvent } from '@mui/material/Select';
import AddIcon from '@mui/icons-material/Add';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import type { CustomerLocation } from '../../types/customer.types';

interface LocationSelectorProps {
  /** Verfügbare Standorte aus Customer Entity */
  locations: CustomerLocation[];

  /** Aktuell ausgewählter Standort */
  selectedLocationId: string | 'all';

  /** Callback bei Standortauswahl */
  onLocationChange: (locationId: string | 'all') => void;

  /** Option "Für alle übernehmen" aktiviert? */
  applyToAll: boolean;

  /** Callback für "Für alle übernehmen" */
  onApplyToAllChange: (value: boolean) => void;

  /** Anzahl Gesamt-Standorte (für Display) */
  totalLocations: number;

  /** Bereits erfasste Standorte (optional) */
  completedLocationIds?: string[];
}

export const LocationSelector: React.FC<LocationSelectorProps> = ({
  locations,
  selectedLocationId,
  onLocationChange,
  applyToAll,
  onApplyToAllChange,
  totalLocations,
  completedLocationIds = [],
}) => {
  // Bei nur einem Standort automatisch auswählen
  React.useEffect(() => {
    if (locations.length === 1 && selectedLocationId === '') {
      onLocationChange(locations[0].id);
    }
  }, [locations, selectedLocationId, onLocationChange]);

  // Progress berechnen
  const completedCount = completedLocationIds.length;
  const progressText =
    totalLocations > 1 ? `${completedCount} von ${totalLocations} Standorten erfasst` : '';

  return (
    <Box sx={{ mb: 4 }}>
      <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6" gutterBottom>
          📍 Angebotsstruktur pro Standort
        </Typography>
        {progressText && (
          <Chip
            label={progressText}
            color={completedCount === totalLocations ? 'success' : 'default'}
            size="small"
          />
        )}
      </Box>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Erfassen Sie die Angebote für jeden Standort oder wählen Sie "Alle Standorte gleich" für
        einheitliche Strukturen.
      </Typography>

      <FormControl fullWidth sx={{ mb: 2 }}>
        <InputLabel id="location-selector-label">Für welchen Standort erfassen?</InputLabel>
        <Select
          labelId="location-selector-label"
          value={selectedLocationId}
          onChange={(e: SelectChangeEvent) => onLocationChange(e.target.value as string | 'all')}
          label="Für welchen Standort erfassen?"
        >
          <MenuItem value="all">
            <Box sx={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}>
              Alle {totalLocations} Standorte gleich
            </Box>
          </MenuItem>

          {locations.length > 0 && <Divider />}

          {locations.map((loc, index) => {
            const isCompleted = completedLocationIds.includes(loc.id);
            return (
              <MenuItem key={loc.id} value={loc.id}>
                <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                  <Box sx={{ flex: 1 }}>
                    Standort {index + 1}: {loc.name || loc.city || 'Unbenannt'}
                    {loc.isMainLocation && ' (Hauptsitz)'}
                  </Box>
                  {isCompleted && (
                    <CheckCircleIcon sx={{ ml: 1, color: 'success.main', fontSize: 20 }} />
                  )}
                </Box>
              </MenuItem>
            );
          })}

          <Divider />

          <MenuItem value="new">
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <AddIcon sx={{ mr: 1 }} />
              Neuer Standort hinzufügen
            </Box>
          </MenuItem>
        </Select>
      </FormControl>

      {selectedLocationId !== 'all' && totalLocations > 1 && (
        <FormControlLabel
          control={
            <Checkbox
              checked={applyToAll}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                onApplyToAllChange(e.target.checked)
              }
              color="primary"
            />
          }
          label={
            <Box>
              <Typography variant="body1">
                Für alle {totalLocations} Standorte übernehmen
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Die Angaben dieses Standorts werden für alle anderen Standorte kopiert
              </Typography>
            </Box>
          }
        />
      )}

      {selectedLocationId === 'all' && (
        <Box
          sx={{
            mt: 2,
            p: 2,
            bgcolor: 'info.light',
            borderRadius: 1,
            border: '1px solid',
            borderColor: 'info.main',
          }}
        >
          <Typography variant="body2" color="info.dark">
            <strong>Hinweis:</strong> Sie erfassen jetzt die Angebotsstruktur für alle{' '}
            {totalLocations} Standorte gleichzeitig. Individuelle Anpassungen können Sie später
            vornehmen.
          </Typography>
        </Box>
      )}
    </Box>
  );
};
