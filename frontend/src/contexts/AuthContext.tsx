/**
 * Legacy AuthContext - Wrapper around KeycloakContext
 * This maintains backward compatibility while using Keycloak for authentication
 */
import { createContext, useContext } from 'react';
import type { ReactNode } from 'react';
import { useKeycloak } from './KeycloakContext';

interface User {
  id: string;
  name: string;
  email: string;
  username?: string;
  roles?: string[];
  xentralSalesRepId?: string; // Sprint 2.1.7.2 D1 - Sales Rep ID from backend
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  token: string | null;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  getValidToken: () => Promise<string | null>;
  refreshToken: () => Promise<boolean>;
  authInfo: () => Record<string, unknown>;
}

export const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  // Hook must be called unconditionally before any conditional returns
  const keycloak = useKeycloak();

  // Check for auth bypass mode first
  if (import.meta.env.VITE_AUTH_BYPASS === 'true') {
    // Check localStorage first (set by LoginBypassPage)
    const storedToken = localStorage.getItem('auth-token');
    const storedUserJson = localStorage.getItem('auth-user');
    const storedUser = storedUserJson ? JSON.parse(storedUserJson) : null;

    // Use stored user if available, otherwise fallback to default mock
    const user = storedUser || {
      id: 'dev-user',
      name: 'Dev User',
      email: 'dev@freshplan.de',
      username: 'devuser',
      roles: ['admin', 'manager', 'sales', 'auditor'],
    };

    const token = storedToken || 'mock-dev-token';

    // Provide mock auth context for development
    // NOTE: All roles are lowercase for consistency with frontend role checks
    const mockContext: AuthContextType = {
      user,
      isAuthenticated: true,
      isLoading: false,
      login: async () => {},
      logout: () => {
        // Clear localStorage on logout
        localStorage.removeItem('auth-token');
        localStorage.removeItem('auth-user');
      },
      token,
      hasRole: (role: string) => user?.roles?.includes(role.toLowerCase()) ?? false,
      hasAnyRole: (roles: string[]) =>
        roles.some(role => user?.roles?.includes(role.toLowerCase()) ?? false),
      getValidToken: async () => token,
      refreshToken: async () => true,
      authInfo: () => ({ mockAuth: true, user }),
    };

    return <AuthContext.Provider value={mockContext}>{children}</AuthContext.Provider>;
  }

  // Map Keycloak user data to legacy User interface
  const user: User | null =
    keycloak.isAuthenticated && keycloak.userId
      ? {
          id: keycloak.userId,
          name: keycloak.username || keycloak.email || 'Unknown',
          email: keycloak.email || '',
          username: keycloak.username,
          roles: keycloak.userRoles,
        }
      : null;

  // Legacy login function - redirects to Keycloak
  const login = async (_email?: string, _password?: string) => {
    // Legacy API compatibility - parameters ignored, redirects to Keycloak
    keycloak.login();
  };

  const contextValue: AuthContextType = {
    user,
    isAuthenticated: keycloak.isAuthenticated,
    isLoading: keycloak.isLoading,
    login,
    logout: keycloak.logout,
    token: keycloak.token || null,
    hasRole: (role: string) => keycloak.hasRole(role.toLowerCase()),
    hasAnyRole: (roles: string[]) => roles.some(role => keycloak.hasRole(role.toLowerCase())),
    getValidToken: async () => {
      if (!keycloak.isAuthenticated) return null;

      try {
        // Import authUtils here to avoid circular dependency
        const { authUtils } = await import('../lib/keycloak');
        return await authUtils.getValidToken();
      } catch (_error) {
        void _error;
        // Error handled silently, return null as fallback
        return null;
      }
    },
    refreshToken: async () => {
      if (!keycloak.isAuthenticated) return false;

      try {
        const { authUtils } = await import('../lib/keycloak');
        return await authUtils.updateToken(30);
      } catch (_error) {
        void _error;
        // Error handled silently, return false as fallback
        return false;
      }
    },
    authInfo: () => {
      return {
        authenticated: keycloak.isAuthenticated,
        username: user?.username,
        email: user?.email,
        roles: user?.roles || [],
        tokenTimeLeft: keycloak.token ? 'available' : 'none',
      };
    },
  };

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
