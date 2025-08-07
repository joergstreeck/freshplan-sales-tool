import { Box, Typography, Button, Chip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useEffect } from 'react';

interface CustomerListHeaderProps {
  totalCount: number;
  onAddCustomer: () => void;
}

export function CustomerListHeader({ totalCount, onAddCustomer }: CustomerListHeaderProps) {
  // Listen for global shortcut event
  useEffect(() => {
    const handler = () => onAddCustomer();
    window.addEventListener('freshplan:new-customer', handler);
    return () => window.removeEventListener('freshplan:new-customer', handler);
  }, [onAddCustomer]);

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        p: 3,
        borderBottom: 1,
        borderColor: 'divider',
        bgcolor: 'background.paper',
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <Typography variant="h4" component="h1">
          Kunden
        </Typography>
        <Chip
          label={totalCount}
          size="small"
          sx={{ bgcolor: 'primary.light', color: 'primary.contrastText' }}
        />
      </Box>

      <Button
        variant="contained"
        startIcon={<AddIcon />}
        onClick={onAddCustomer}
        sx={{
          bgcolor: '#94C456', // Freshfoodz Grün
          '&:hover': {
            bgcolor: '#7BA545',
          },
        }}
      >
        Neuer Kunde
        <Typography
          component="span"
          sx={{
            ml: 1,
            fontSize: '0.75rem',
            opacity: 0.7,
          }}
        >
          Strg+N
        </Typography>
      </Button>
    </Box>
  );
}
