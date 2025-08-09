import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { Box, Alert, CircularProgress } from '@mui/material';
import { useAuth } from '@/hooks/useAuth';
import { isFeatureEnabled } from '@/config/featureFlags';

interface ProtectedRouteProps {
  allowedRoles?: string[];
  children?: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  allowedRoles = [], 
  children 
}) => {
  const { isAuthenticated, hasRole, userRoles, isLoading } = useAuth();
  
  // Helper function to check if user has any of the allowed roles
  const hasAnyRole = (roles: string[]) => {
    return roles.length === 0 || roles.some(role => hasRole(role));
  };
  
  // In development with authBypass, skip authentication check
  if (isFeatureEnabled('authBypass')) {
    // Still check roles even with authBypass
    if (!hasAnyRole(allowedRoles)) {
      return (
        <Box sx={{ p: 3 }}>
          <Alert 
            severity="error"
            sx={{
              '& .MuiAlert-icon': {
                color: '#d32f2f'
              }
            }}
          >
            Sie haben keine Berechtigung für diesen Bereich. 
            Erforderliche Rolle(n): {allowedRoles.join(', ')}
          </Alert>
        </Box>
      );
    }
    return children ? <>{children}</> : <Outlet />;
  }
  
  if (isLoading) {
    return (
      <Box 
        sx={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          minHeight: '100vh' 
        }}
      >
        <CircularProgress sx={{ color: '#94C456' }} />
      </Box>
    );
  }
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  if (!hasAnyRole(allowedRoles)) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert 
          severity="error"
          sx={{
            '& .MuiAlert-icon': {
              color: '#d32f2f'
            }
          }}
        >
          Sie haben keine Berechtigung für diesen Bereich. 
          Erforderliche Rolle(n): {allowedRoles.join(', ')}
        </Alert>
      </Box>
    );
  }
  
  return children ? <>{children}</> : <Outlet />;
};