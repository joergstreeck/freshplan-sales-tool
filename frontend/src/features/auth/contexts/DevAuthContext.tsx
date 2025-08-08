/**
 * FC-005 Development Auth Context
 *
 * Provides a mock authentication context for development.
 * This allows testing without Keycloak while maintaining the same API.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/pages/CustomersPage.tsx
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/config/featureFlags.ts
 */

import React, { createContext, useContext, useState, useCallback } from 'react';
import { toast } from 'react-toastify';

/**
 * Mock User for Development
 */
export interface DevUser {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  permissions: string[];
}

/**
 * Development Auth Context Type
 */
interface DevAuthContextType {
  isAuthenticated: boolean;
  user: DevUser | null;
  login: (username: string) => Promise<void>;
  logout: () => void;
  hasRole: (role: string) => boolean;
  hasPermission: (permission: string) => boolean;
  isLoading: boolean;
}

/**
 * Default mock users for different roles
 */
const mockUsers: Record<string, DevUser> = {
  admin: {
    id: 'dev-admin-001',
    username: 'admin',
    email: 'admin@freshplan.dev',
    firstName: 'Admin',
    lastName: 'User',
    roles: ['admin', 'manager', 'sales'],
    permissions: ['customers:read', 'customers:write', 'customers:delete', 'reports:view'],
  },
  manager: {
    id: 'dev-manager-001',
    username: 'manager',
    email: 'manager@freshplan.dev',
    firstName: 'Manager',
    lastName: 'User',
    roles: ['manager', 'sales'],
    permissions: ['customers:read', 'customers:write', 'reports:view'],
  },
  sales: {
    id: 'dev-sales-001',
    username: 'sales',
    email: 'sales@freshplan.dev',
    firstName: 'Sales',
    lastName: 'User',
    roles: ['sales'],
    permissions: ['customers:read', 'customers:write'],
  },
};

const DevAuthContext = createContext<DevAuthContextType | undefined>(undefined);

/**
 * Development Auth Provider
 *
 * @remarks
 * - Provides mock authentication for development
 * - Maintains same API as production auth
 * - Stores auth state in sessionStorage
 */
export const DevAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Check sessionStorage for existing auth
  const storedUser = sessionStorage.getItem('dev-auth-user');
  const initialUser = storedUser ? JSON.parse(storedUser) : null;

  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(!!initialUser);
  const [user, setUser] = useState<DevUser | null>(initialUser);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const login = useCallback(async (username: string) => {
    setIsLoading(true);

    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 500));

    const mockUser = mockUsers[username] || mockUsers.admin;
    setUser(mockUser);
    setIsAuthenticated(true);

    // Store in sessionStorage
    sessionStorage.setItem('dev-auth-user', JSON.stringify(mockUser));

    toast.success(`🚀 Logged in as ${mockUser.firstName} (${mockUser.roles.join(', ')})`);
    setIsLoading(false);
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    setIsAuthenticated(false);
    sessionStorage.removeItem('dev-auth-user');
    toast.info('👋 Logged out from dev mode');
  }, []);

  const hasRole = useCallback(
    (role: string): boolean => {
      return user?.roles.includes(role) ?? false;
    },
    [user]
  );

  const hasPermission = useCallback(
    (permission: string): boolean => {
      return user?.permissions.includes(permission) ?? false;
    },
    [user]
  );

  const value: DevAuthContextType = {
    isAuthenticated,
    user,
    login,
    logout,
    hasRole,
    hasPermission,
    isLoading,
  };

  return <DevAuthContext.Provider value={value}>{children}</DevAuthContext.Provider>;
};

/**
 * Hook to use dev auth context
 */
export const useDevAuth = (): DevAuthContextType => {
  const context = useContext(DevAuthContext);
  if (!context) {
    throw new Error('useDevAuth must be used within DevAuthProvider');
  }
  return context;
};
