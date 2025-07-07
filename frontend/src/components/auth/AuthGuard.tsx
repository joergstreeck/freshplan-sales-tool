/**
 * Auth Guard Komponente - Schützt Routen vor unauthentifizierten Benutzern
 */
import React, { ReactNode } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { LoginPage } from './LoginPage';

interface AuthGuardProps {
  children: ReactNode;
  requiredRole?: string;
  fallback?: ReactNode;
}

export const AuthGuard: React.FC<AuthGuardProps> = ({ children, requiredRole, fallback }) => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();

  // Loading state
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-indigo-600 mx-auto"></div>
          <p className="mt-4 text-lg text-gray-600">FreshPlan wird geladen...</p>
        </div>
      </div>
    );
  }

  // Not authenticated
  if (!isAuthenticated) {
    return <LoginPage />;
  }

  // Check required role
  if (requiredRole && !hasRole(requiredRole)) {
    const unauthorizedFallback = fallback || (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-6xl text-red-500 mb-4">🚫</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Zugriff verweigert</h2>
          <p className="text-gray-600">
            Sie haben nicht die erforderlichen Berechtigungen für diese Seite.
          </p>
          <p className="text-sm text-gray-500 mt-2">
            Erforderliche Rolle:{' '}
            <code className="bg-gray-200 px-2 py-1 rounded">{requiredRole}</code>
          </p>
        </div>
      </div>
    );

    return <>{unauthorizedFallback}</>;
  }

  // Authenticated and authorized
  return <>{children}</>;
};
