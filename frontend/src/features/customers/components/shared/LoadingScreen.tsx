/**
 * Loading Screen Component
 *
 * Full-screen loading indicator for initial data loading.
 */

import React from 'react';
import { Box, CircularProgress, Typography } from '@mui/material';

interface LoadingScreenProps {
  /** Loading message */
  message?: string;
}

/**
 * Loading Screen
 *
 * Centered loading spinner with optional message.
 * Used while loading field definitions or draft data.
 */
export const LoadingScreen: React.FC<LoadingScreenProps> = ({
  message = 'Daten werden geladen...',
}) => {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '60vh',
        gap: 2,
      }}
    >
      <CircularProgress size={48} />
      <Typography variant="body1" color="text.secondary">
        {message}
      </Typography>
    </Box>
  );
};
