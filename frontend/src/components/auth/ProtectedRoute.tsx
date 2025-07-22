import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { LoadingSpinner } from '../ui/LoadingSpinner';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: string;
  requiredRoles?: string[];
}

/**
 * Protected Route Component
 * 
 * This component checks authentication and role requirements before rendering children.
 * If the user is not authenticated, they are redirected to the login page.
 * If the user doesn't have the required role, they see a forbidden message.
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  requiredRole,
  requiredRoles 
}) => {
  const { isAuthenticated, isLoading, hasRole, userRoles } = useAuth();
  const location = useLocation();

  // Show loading spinner while checking authentication
  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner />
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Check role requirements
  if (requiredRole && !hasRole(requiredRole)) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen">
        <h1 className="text-2xl font-bold text-red-600">Zugriff verweigert</h1>
        <p className="mt-2 text-gray-600">
          Sie haben nicht die erforderliche Berechtigung ({requiredRole}) für diese Seite.
        </p>
        <p className="mt-1 text-sm text-gray-500">
          Ihre Rollen: {userRoles.join(', ') || 'keine'}
        </p>
      </div>
    );
  }

  // Check if user has any of the required roles
  if (requiredRoles && requiredRoles.length > 0) {
    const hasRequiredRole = requiredRoles.some(role => hasRole(role));
    if (!hasRequiredRole) {
      return (
        <div className="flex flex-col items-center justify-center min-h-screen">
          <h1 className="text-2xl font-bold text-red-600">Zugriff verweigert</h1>
          <p className="mt-2 text-gray-600">
            Sie benötigen eine der folgenden Rollen: {requiredRoles.join(', ')}
          </p>
          <p className="mt-1 text-sm text-gray-500">
            Ihre Rollen: {userRoles.join(', ') || 'keine'}
          </p>
        </div>
      );
    }
  }

  // User is authenticated and has required role(s)
  return <>{children}</>;
};