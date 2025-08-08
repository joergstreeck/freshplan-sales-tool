/**
 * Save Indicator Component
 *
 * Shows the current save status of the draft.
 * Indicates when auto-save is working and when last saved.
 */

import React from 'react';
import { Box, Typography, CircularProgress, Chip } from '@mui/material';
import {
  Save as SaveIcon,
  Check as CheckIcon,
  CloudDone as CloudDoneIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';

interface SaveIndicatorProps {
  /** Has unsaved changes */
  isDirty: boolean;
  /** Currently saving */
  isSaving: boolean;
  /** Last save timestamp */
  lastSaved: Date | null;
}

/**
 * Save Indicator
 *
 * Visual feedback for draft auto-save status.
 * Shows different states: saving, saved, unsaved changes.
 */
export const SaveIndicator: React.FC<SaveIndicatorProps> = ({ isDirty, isSaving, lastSaved }) => {
  const getStatusDisplay = () => {
    if (isSaving) {
      return {
        icon: <CircularProgress size={16} sx={{ mr: 0.5 }} />,
        text: 'Wird gespeichert...',
        color: 'primary' as const,
      };
    }

    if (isDirty) {
      return {
        icon: <SaveIcon fontSize="small" sx={{ mr: 0.5 }} />,
        text: 'Ungespeicherte Änderungen',
        color: 'warning' as const,
      };
    }

    if (lastSaved) {
      const formattedDate = format(lastSaved, 'HH:mm', { locale: de });
      return {
        icon: <CloudDoneIcon fontSize="small" sx={{ mr: 0.5 }} />,
        text: `Gespeichert um ${formattedDate}`,
        color: 'success' as const,
      };
    }

    return {
      icon: <CheckIcon fontSize="small" sx={{ mr: 0.5 }} />,
      text: 'Alle Änderungen gespeichert',
      color: 'success' as const,
    };
  };

  const status = getStatusDisplay();

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        position: 'absolute',
        top: 16,
        right: 16,
      }}
    >
      <Chip
        icon={status.icon}
        label={status.text}
        color={status.color}
        variant="outlined"
        size="small"
        sx={{
          '& .MuiChip-icon': {
            ml: 1,
          },
        }}
      />
    </Box>
  );
};
