import React from 'react';
import { CustomerList } from '@/features/customer/components';
import { AuthenticatedLayout } from '@/components/layout/AuthenticatedLayout';
import { isFeatureEnabled } from '@/config/featureFlags';
import { DevAuthProvider } from '@/features/auth/contexts/DevAuthContext';
import { DevAuthPanel } from '@/features/auth/components/DevAuthPanel';
import { Box, Container, Alert, AlertTitle } from '@mui/material';

const CustomersPage: React.FC = () => {
  // Check if auth bypass is enabled via feature flag
  const authBypassEnabled = isFeatureEnabled('authBypass');
  
  if (authBypassEnabled) {
    // Development: Provide mock auth context with role simulation
    return (
      <DevAuthProvider>
        <Box sx={{ minHeight: '100vh', bgcolor: 'background.default', pt: 2 }}>
          <Container maxWidth="xl">
            {/* Development Warning Banner */}
            <Alert 
              severity="warning" 
              sx={{ mb: 2 }}
              action={
                <DevAuthPanel compact />
              }
            >
              <AlertTitle>Development Mode - Auth Bypass Active</AlertTitle>
              This is a temporary development feature. Use the auth panel to test different roles.
              Set VITE_AUTH_BYPASS=false to use real authentication.
            </Alert>
            
            {/* Main Content */}
            <CustomerList />
          </Container>
        </Box>
      </DevAuthProvider>
    );
  }
  
  // Production: Use real authentication
  return (
    <AuthenticatedLayout>
      <div className="page-container">
        <CustomerList />
      </div>
    </AuthenticatedLayout>
  );
};

export default CustomersPage;
