/**
 * EmptyContactState Component
 * 
 * Displays a friendly empty state when no contacts are available.
 * Part of FC-005 Contact Management UI - Smart Contact Cards.
 * 
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md
 */

import React from 'react';
import {
  Box,
  Typography,
  Button,
  Avatar,
} from '@mui/material';
import {
  PersonAdd as PersonAddIcon,
  Add as AddIcon,
} from '@mui/icons-material';

interface EmptyContactStateProps {
  onAddContact: () => void;
}

/**
 * Empty state component with call-to-action for adding first contact
 */
export const EmptyContactState: React.FC<EmptyContactStateProps> = ({ onAddContact }) => {
  return (
    <Box
      sx={{
        textAlign: 'center',
        py: 8,
        px: 3,
        bgcolor: 'background.paper',
        borderRadius: 2,
        border: '2px dashed',
        borderColor: 'divider',
        transition: 'all 0.3s ease',
        cursor: 'pointer',
        '&:hover': {
          borderColor: 'primary.main',
          bgcolor: 'action.hover',
        },
      }}
      onClick={onAddContact}
    >
      <Avatar
        sx={{
          width: 80,
          height: 80,
          bgcolor: 'primary.light',
          margin: '0 auto',
          mb: 3,
        }}
      >
        <PersonAddIcon sx={{ fontSize: 40 }} />
      </Avatar>

      <Typography variant="h6" gutterBottom>
        Noch keine Ansprechpartner vorhanden
      </Typography>

      <Typography variant="body2" color="text.secondary" paragraph>
        Fügen Sie den ersten Kontakt hinzu, um die Beziehungspflege zu starten
      </Typography>

      <Button
        variant="contained"
        startIcon={<AddIcon />}
        onClick={(e) => {
          e.stopPropagation();
          onAddContact();
        }}
        size="large"
        sx={{ mt: 2 }}
      >
        Ersten Ansprechpartner hinzufügen
      </Button>
    </Box>
  );
};