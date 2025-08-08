import React, {
  createContext,
  useContext,
  useEffect,
  useState,
  useCallback,
  ReactNode,
} from 'react';
import { useAuth } from './AuthContext';
import { apiClient } from '../lib/authenticatedApiClient';

/**
 * Permission Context for FC-009 Advanced Permissions System
 *
 * Provides permission checking functionality throughout the React app.
 * Integrates with backend PermissionResource API.
 */

interface PermissionContextType {
  hasPermission: (permissionCode: string) => boolean;
  hasAnyPermission: (permissionCodes: string[]) => boolean;
  hasAllPermissions: (permissionCodes: string[]) => boolean;
  permissions: Set<string>;
  isLoading: boolean;
  refreshPermissions: () => Promise<void>;
}

const PermissionContext = createContext<PermissionContextType | undefined>(undefined);

interface PermissionProviderProps {
  children: ReactNode;
}

export const PermissionProvider: React.FC<PermissionProviderProps> = ({ children }) => {
  const [permissions, setPermissions] = useState<Set<string>>(new Set());
  const [isLoading, setIsLoading] = useState(true);
  const { user, isAuthenticated } = useAuth();

  const loadUserPermissions = useCallback(async () => {
    if (!isAuthenticated || !user) {
      setPermissions(new Set());
      setIsLoading(false);
      return;
    }

    try {
      setIsLoading(true);

      const response = await apiClient.get('/api/permissions/me');
      const userPermissions = response.data.permissions || [];

      setPermissions(new Set(userPermissions));
    } catch (error) {
      // Log errors in development only
      if (import.meta.env.DEV) {
        console.error('Failed to load user permissions:', error);
      }
      setPermissions(new Set());
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    loadUserPermissions();
  }, [loadUserPermissions]);

  const hasPermission = useCallback(
    (permissionCode: string): boolean => {
      if (isLoading) return false;

      // Super admin permission grants everything
      if (permissions.has('*:*')) {
        return true;
      }

      // Direct permission check
      if (permissions.has(permissionCode)) {
        return true;
      }

      // Wildcard resource check (e.g., "customers:*" grants "customers:read")
      const [resource, action] = permissionCode.split(':');
      if (resource && action) {
        const wildcardResource = `${resource}:*`;
        const wildcardAction = `*:${action}`;

        if (permissions.has(wildcardResource) || permissions.has(wildcardAction)) {
          return true;
        }
      }

      return false;
    },
    [permissions, isLoading]
  );

  const hasAnyPermission = useCallback(
    (permissionCodes: string[]): boolean => {
      return permissionCodes.some(code => hasPermission(code));
    },
    [hasPermission]
  );

  const hasAllPermissions = useCallback(
    (permissionCodes: string[]): boolean => {
      return permissionCodes.every(code => hasPermission(code));
    },
    [hasPermission]
  );

  const refreshPermissions = useCallback(async () => {
    await loadUserPermissions();
  }, [loadUserPermissions]);

  const contextValue: PermissionContextType = {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    permissions,
    isLoading,
    refreshPermissions,
  };

  return <PermissionContext.Provider value={contextValue}>{children}</PermissionContext.Provider>;
};

/**
 * Hook to access permission context.
 * Must be used within PermissionProvider.
 */
export const usePermissions = (): PermissionContextType => {
  const context = useContext(PermissionContext);
  if (!context) {
    throw new Error('usePermissions must be used within PermissionProvider');
  }
  return context;
};

/**
 * Hook for checking a single permission with loading state.
 */
export const usePermission = (permissionCode: string) => {
  const { hasPermission, isLoading } = usePermissions();

  return {
    hasPermission: hasPermission(permissionCode),
    isLoading,
  };
};
