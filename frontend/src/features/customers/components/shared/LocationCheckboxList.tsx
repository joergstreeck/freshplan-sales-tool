/**
 * LocationCheckboxList Component
 * 
 * Displays a list of locations with checkboxes for selection.
 * Used in contact management for assigning contacts to locations.
 */

import React from 'react';
import {
  FormGroup,
  FormControlLabel,
  Checkbox,
  Box,
  Typography,
  Paper
} from '@mui/material';

import type { Location } from '../../types/location.types';

interface LocationCheckboxListProps {
  locations: Location[];
  selectedLocationIds: string[];
  onChange: (locationIds: string[]) => void;
  disabled?: boolean;
}

/**
 * Checkbox list for location selection
 */
export const LocationCheckboxList: React.FC<LocationCheckboxListProps> = ({
  locations,
  selectedLocationIds,
  onChange,
  disabled = false
}) => {
  const handleToggle = (locationId: string) => {
    const currentIndex = selectedLocationIds.indexOf(locationId);
    const newSelected = [...selectedLocationIds];
    
    if (currentIndex === -1) {
      newSelected.push(locationId);
    } else {
      newSelected.splice(currentIndex, 1);
    }
    
    onChange(newSelected);
  };
  
  const handleSelectAll = () => {
    if (selectedLocationIds.length === locations.length) {
      onChange([]);
    } else {
      onChange(locations.map(loc => loc.id));
    }
  };
  
  if (locations.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        Keine Standorte verfügbar
      </Typography>
    );
  }
  
  return (
    <Box>
      {locations.length > 1 && (
        <FormControlLabel
          control={
            <Checkbox
              checked={selectedLocationIds.length === locations.length}
              indeterminate={selectedLocationIds.length > 0 && selectedLocationIds.length < locations.length}
              onChange={handleSelectAll}
              disabled={disabled}
            />
          }
          label={<Typography variant="subtitle2">Alle auswählen</Typography>}
          sx={{ mb: 1 }}
        />
      )}
      
      <Paper variant="outlined" sx={{ p: 2, maxHeight: 300, overflow: 'auto' }}>
        <FormGroup>
          {locations.map((location) => {
            // Get location name from field values
            const locationName = location.name || 
              location.fieldValues?.locationName || 
              `Standort ${location.sortOrder + 1}`;
            
            const locationAddress = [
              location.fieldValues?.street,
              location.fieldValues?.postalCode,
              location.fieldValues?.city
            ].filter(Boolean).join(', ');
            
            return (
              <FormControlLabel
                key={location.id}
                control={
                  <Checkbox
                    checked={selectedLocationIds.includes(location.id)}
                    onChange={() => handleToggle(location.id)}
                    disabled={disabled}
                  />
                }
                label={
                  <Box>
                    <Typography variant="body2">
                      {locationName}
                    </Typography>
                    {locationAddress && (
                      <Typography variant="caption" color="text.secondary">
                        {locationAddress}
                      </Typography>
                    )}
                  </Box>
                }
                sx={{ mb: 1 }}
              />
            );
          })}
        </FormGroup>
      </Paper>
      
      {selectedLocationIds.length > 0 && (
        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
          {selectedLocationIds.length} von {locations.length} Standorten ausgewählt
        </Typography>
      )}
    </Box>
  );
};