import { Box, Typography, Button, Chip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { UniversalExportButton } from '../../../components/export';

interface CustomerListHeaderProps {
  totalCount: number;
  onAddCustomer?: () => void;
}

export function CustomerListHeader({ totalCount, onAddCustomer }: CustomerListHeaderProps) {
  const navigate = useNavigate();

  // Default to navigation if no onAddCustomer provided
  const handleAddCustomer = React.useMemo(
    () => onAddCustomer || (() => navigate('/customers/new')),
    [onAddCustomer, navigate]
  );

  useEffect(() => {
    // Track page view
    console.log('Customer list viewed');
  }, []);

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        mb: 3,
        flexWrap: 'wrap',
        gap: 2,
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <Typography variant="h4" component="h1">
          Kunden
        </Typography>
        <Chip label={`${totalCount} gesamt`} color="primary" size="small" />
      </Box>

      <Box sx={{ display: 'flex', gap: 2 }}>
        <UniversalExportButton entity="customer" filters={{}} variant="outlined" />
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleAddCustomer}
          sx={{
            bgcolor: '#94C456',
            '&:hover': {
              bgcolor: '#7BA345',
            },
          }}
        >
          Neuer Kunde
        </Button>
      </Box>
    </Box>
  );
}
