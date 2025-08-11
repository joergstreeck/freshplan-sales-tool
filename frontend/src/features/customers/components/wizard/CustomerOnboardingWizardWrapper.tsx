import React from 'react';
import { Dialog, DialogContent, useMediaQuery, useTheme, Drawer } from '@mui/material';
import { CustomerOnboardingWizard as OriginalWizard } from './CustomerOnboardingWizard';

interface CustomerOnboardingWizardProps {
  open: boolean;
  onClose: () => void;
  onComplete: (customer: unknown) => void;
}

export function CustomerOnboardingWizard({
  open,
  onClose,
  onComplete: _onComplete,
}: CustomerOnboardingWizardProps) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  // TODO: Connect onComplete callback to the wizard store
  // This will be implemented when we understand the store structure better

  const content = <OriginalWizard />;

  // Use Drawer on mobile, Dialog on desktop
  if (isMobile) {
    return (
      <Drawer
        anchor="bottom"
        open={open}
        onClose={onClose}
        sx={{
          '& .MuiDrawer-paper': {
            height: '90vh',
            borderTopLeftRadius: 16,
            borderTopRightRadius: 16,
          },
        }}
      >
        {content}
      </Drawer>
    );
  }

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      sx={{
        '& .MuiDialog-paper': {
          height: '90vh',
          maxHeight: 800,
        },
      }}
    >
      <DialogContent sx={{ p: 0 }}>{content}</DialogContent>
    </Dialog>
  );
}
