/**
 * Customer Onboarding Wizard Modal
 *
 * Modal wrapper for the CustomerOnboardingWizard.
 * Ensures the wizard opens as an overlay while keeping the sidebar visible.
 *
 * @see CRITICAL_ARCHITECTURE_FIX_PLAN.md
 */

import React from 'react';
import {
  Dialog,
  DialogContent,
  IconButton,
  useTheme,
  useMediaQuery,
  Drawer,
  Box,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { CustomerOnboardingWizard } from './CustomerOnboardingWizard';
import type { Customer } from '../../../../types/customer.types';

interface CustomerOnboardingWizardModalProps {
  open: boolean;
  onClose: () => void;
  onComplete: (customer: Customer) => void;
}

export function CustomerOnboardingWizardModal({
  open,
  onClose,
  onComplete,
}: CustomerOnboardingWizardModalProps) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  // Mobile: Use Drawer, Desktop: Use Dialog
  if (isMobile) {
    return (
      <Drawer
        anchor="bottom"
        open={open}
        onClose={onClose}
        PaperProps={{
          sx: {
            height: '90vh',
            borderTopLeftRadius: 16,
            borderTopRightRadius: 16,
            overflow: 'hidden',
          },
        }}
      >
        <Box sx={{ position: 'relative', height: '100%' }}>
          <Box
            sx={{
              position: 'sticky',
              top: 0,
              p: 2,
              borderBottom: 1,
              borderColor: 'divider',
              bgcolor: 'background.paper',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              zIndex: 1,
            }}
          >
            <Typography variant="h6">Neuen Kunden anlegen</Typography>
            <IconButton onClick={onClose} edge="end">
              <CloseIcon />
            </IconButton>
          </Box>
          <Box sx={{ p: 2, overflow: 'auto', height: 'calc(100% - 64px)' }}>
            <CustomerOnboardingWizard onComplete={onComplete} onCancel={onClose} isModal={true} />
          </Box>
        </Box>
      </Drawer>
    );
  }

  // Desktop: Dialog
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="xl"
      fullWidth
      PaperProps={{
        sx: {
          height: '95vh',
          maxHeight: 'none',
          overflow: 'hidden',
          m: 1, // Margin für kleinere Bildschirme
        },
      }}
    >
      <Box
        sx={{
          position: 'sticky',
          top: 0,
          p: 2,
          pr: 6,
          borderBottom: 1,
          borderColor: 'divider',
          bgcolor: 'background.paper',
          zIndex: 1,
        }}
      >
        <Typography variant="h5" component="h2">
          Neuen Kunden anlegen
        </Typography>
        <IconButton
          onClick={onClose}
          sx={{
            position: 'absolute',
            right: 8,
            top: 8,
          }}
        >
          <CloseIcon />
        </IconButton>
      </Box>

      <DialogContent sx={{ p: 0, overflow: 'hidden' }}>
        <Box sx={{ height: '100%', overflow: 'auto', p: 3 }}>
          <CustomerOnboardingWizard onComplete={onComplete} onCancel={onClose} isModal={true} />
        </Box>
      </DialogContent>
    </Dialog>
  );
}
