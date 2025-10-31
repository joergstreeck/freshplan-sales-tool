import React from 'react';
import { Box, CircularProgress, Alert, Typography } from '@mui/material';
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';
import { CustomerTable } from '@/features/customers/components/CustomerTable';
import { useCustomers } from '@/features/customer/api/customerQueries';

const CustomersPage: React.FC = () => {
  const { data: customers, isLoading, error } = useCustomers(0, 1000);

  if (isLoading) {
    return (
      <MainLayoutV2 maxWidth="full">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
          <CircularProgress />
        </Box>
      </MainLayoutV2>
    );
  }

  if (error) {
    return (
      <MainLayoutV2 maxWidth="full">
        <Alert severity="error">
          Fehler beim Laden der Kunden: {error instanceof Error ? error.message : 'Unbekannter Fehler'}
        </Alert>
      </MainLayoutV2>
    );
  }

  return (
    <MainLayoutV2 maxWidth="full">
      <Box p={2}>
        <Typography variant="h4" component="h1" mb={3}>
          Kunden-Management
        </Typography>

        <CustomerTable
          customers={customers || []}
          context="customers"
          showActions={true}
        />
      </Box>
    </MainLayoutV2>
  );
};

export default CustomersPage;
