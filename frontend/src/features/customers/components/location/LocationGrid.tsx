import React from 'react';
import { Box, Grid, Paper, Typography, Chip } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import type { CustomerLocation } from '../../types/customer.types';

interface LocationGridProps {
  locations: CustomerLocation[];
  currentIndex: number;
  completedIds: string[];
  onLocationClick: (index: number) => void;
}

export const LocationGrid: React.FC<LocationGridProps> = ({
  locations,
  currentIndex,
  completedIds,
  onLocationClick,
}) => {
  const getLocationStatus = (location: CustomerLocation, index: number) => {
    if (completedIds.includes(location.id)) return 'completed';
    if (index === currentIndex) return 'current';
    return 'pending';
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed':
        return <CheckCircleIcon sx={{ fontSize: 16 }} />;
      case 'current':
        return <PlayArrowIcon sx={{ fontSize: 16 }} />;
      default:
        return <RadioButtonUncheckedIcon sx={{ fontSize: 16 }} />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed':
        return 'success';
      case 'current':
        return 'primary';
      default:
        return 'default';
    }
  };

  return (
    <Grid container spacing={2} sx={{ mb: 3 }}>
      {locations.map((location, index) => {
        const status = getLocationStatus(location, index);
        const isClickable = status !== 'current';

        return (
          <Grid size={{ xs: 12, sm: 6, md: 3 }} key={location.id}>
            <Paper
              variant="outlined"
              sx={{
                p: 2,
                cursor: isClickable ? 'pointer' : 'default',
                borderColor: status === 'current' ? 'primary.main' : 'divider',
                borderWidth: status === 'current' ? 2 : 1,
                backgroundColor: status === 'completed' ? 'success.lighter' : 'background.paper',
                transition: 'all 0.2s',
                '&:hover': isClickable
                  ? {
                      borderColor: 'primary.light',
                      backgroundColor: 'action.hover',
                    }
                  : {},
              }}
              onClick={() => isClickable && onLocationClick(index)}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Chip
                  icon={getStatusIcon(status)}
                  label={location.name}
                  color={getStatusColor(status) as any}
                  size="small"
                  sx={{ flexGrow: 1, justifyContent: 'flex-start' }}
                />
              </Box>
              {location.isHeadquarter && (
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ mt: 0.5, display: 'block' }}
                >
                  Hauptsitz
                </Typography>
              )}
            </Paper>
          </Grid>
        );
      })}
    </Grid>
  );
};
