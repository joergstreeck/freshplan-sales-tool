/**
 * Cockpit Page with Auth Guard
 */
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { SalesCockpit } from '../features/cockpit/components/SalesCockpit';

export function CockpitPage() {
  const { isAuthenticated, isLoading } = useAuth();
  
  console.log('CockpitPage - Auth state:', { isAuthenticated, isLoading });
  
  if (isLoading) {
    return (
      <div style={{ padding: '20px', textAlign: 'center' }}>
        <h2>Loading authentication...</h2>
      </div>
    );
  }
  
  if (!isAuthenticated) {
    console.log('Not authenticated, redirecting to home');
    return <Navigate to="/" replace />;
  }
  
  return <SalesCockpit />;
}