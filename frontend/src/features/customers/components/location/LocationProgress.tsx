import React from 'react';
import { Box, LinearProgress, Typography, Chip } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

interface LocationProgressProps {
  total: number;
  completed: number;
}

export const LocationProgress: React.FC<LocationProgressProps> = ({ total, completed }) => {
  const percentage = total > 0 ? (completed / total) * 100 : 0;
  const isComplete = completed === total;

  return (
    <Box sx={{ mb: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant="body2" fontWeight="medium">
            📍 Standort-Fortschritt
          </Typography>
          {isComplete && (
            <Chip icon={<CheckCircleIcon />} label="Vollständig" color="success" size="small" />
          )}
        </Box>
        <Typography variant="body2" color="text.secondary">
          {completed} von {total} erfasst
        </Typography>
      </Box>
      <LinearProgress
        variant="determinate"
        value={percentage}
        sx={{
          height: 8,
          borderRadius: 1,
          backgroundColor: 'action.hover',
          '& .MuiLinearProgress-bar': {
            borderRadius: 1,
            backgroundColor: isComplete ? 'success.main' : 'primary.main',
          },
        }}
      />
    </Box>
  );
};
