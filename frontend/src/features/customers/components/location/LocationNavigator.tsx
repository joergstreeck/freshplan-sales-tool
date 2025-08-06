import React, { useState } from 'react';
import { 
  Box, 
  FormControl, 
  InputLabel, 
  Select, 
  MenuItem, 
  Button,
  Divider,
  Typography,
  IconButton,
  Tooltip
} from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import type { CustomerLocation } from '../../types/customer.types';

interface LocationNavigatorProps {
  locations: CustomerLocation[];
  currentIndex: number;
  completedIds: string[];
  onNavigate: (index: number) => void;
  onCopyFrom: (sourceLocationId: string) => void;
}

export const LocationNavigator: React.FC<LocationNavigatorProps> = ({
  locations,
  currentIndex,
  completedIds,
  onNavigate,
  onCopyFrom
}) => {
  const [copySourceId, setCopySourceId] = useState<string>('');
  const currentLocation = locations[currentIndex];
  const hasPrevious = currentIndex > 0;
  const hasNext = currentIndex < locations.length - 1;
  
  // Get completed locations that can be copied from
  const copyableLocations = locations.filter(
    loc => completedIds.includes(loc.id) && loc.id !== currentLocation?.id
  );
  
  const handleLocationChange = (event: any) => {
    const selectedId = event.target.value;
    const index = locations.findIndex(loc => loc.id === selectedId);
    if (index >= 0) {
      onNavigate(index);
    }
  };
  
  const handleCopyFrom = () => {
    if (copySourceId) {
      onCopyFrom(copySourceId);
      setCopySourceId('');
    }
  };
  
  return (
    <Box sx={{ mb: 3 }}>
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-end', flexWrap: 'wrap' }}>
        {/* Location Selector */}
        <FormControl sx={{ minWidth: 250, flex: 1 }}>
          <InputLabel>Aktueller Standort</InputLabel>
          <Select
            value={currentLocation?.id || ''}
            onChange={handleLocationChange}
            label="Aktueller Standort"
          >
            {locations.map((location, index) => (
              <MenuItem key={location.id} value={location.id}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, width: '100%' }}>
                  <Typography>
                    {location.name}
                  </Typography>
                  {completedIds.includes(location.id) && (
                    <Typography variant="caption" color="success.main">
                      ✓
                    </Typography>
                  )}
                  {location.isHeadquarter && (
                    <Typography variant="caption" color="text.secondary">
                      (Hauptsitz)
                    </Typography>
                  )}
                </Box>
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        
        {/* Navigation Buttons */}
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title="Vorheriger Standort">
            <span>
              <IconButton 
                onClick={() => onNavigate(currentIndex - 1)}
                disabled={!hasPrevious}
                size="large"
              >
                <ArrowBackIcon />
              </IconButton>
            </span>
          </Tooltip>
          
          <Tooltip title="Nächster Standort">
            <span>
              <IconButton 
                onClick={() => onNavigate(currentIndex + 1)}
                disabled={!hasNext}
                size="large"
              >
                <ArrowForwardIcon />
              </IconButton>
            </span>
          </Tooltip>
        </Box>
      </Box>
      
      {/* Copy From Section */}
      {copyableLocations.length > 0 && (
        <>
          <Divider sx={{ my: 2 }} />
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-end' }}>
            <FormControl sx={{ minWidth: 250 }}>
              <InputLabel>Daten kopieren von</InputLabel>
              <Select
                value={copySourceId}
                onChange={(e) => setCopySourceId(e.target.value)}
                label="Daten kopieren von"
                size="small"
              >
                <MenuItem value="">
                  <em>Standort wählen...</em>
                </MenuItem>
                {copyableLocations.map(location => (
                  <MenuItem key={location.id} value={location.id}>
                    {location.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            
            <Button
              variant="outlined"
              startIcon={<ContentCopyIcon />}
              onClick={handleCopyFrom}
              disabled={!copySourceId}
              size="small"
            >
              Kopieren
            </Button>
          </Box>
        </>
      )}
    </Box>
  );
};