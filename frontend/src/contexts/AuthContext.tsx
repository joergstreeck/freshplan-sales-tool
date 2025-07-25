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
  const keycloak = useKeycloak();

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
    hasRole: keycloak.hasRole,
    hasAnyRole: (roles: string[]) => roles.some(role => keycloak.hasRole(role)),
    getValidToken: async () => {
      if (!keycloak.isAuthenticated) return null;
      
      try {
        // Import authUtils here to avoid circular dependency
        const { authUtils } = await import('../lib/keycloak');
        return await authUtils.getValidToken();
      } catch (error) {
        console.error('Failed to get valid token:', error);
        return null;
      }
    },
    refreshToken: async () => {
      if (!keycloak.isAuthenticated) return false;
      
      try {
        const { authUtils } = await import('../lib/keycloak');
        return await authUtils.updateToken(30);
      } catch (error) {
        console.error('Failed to refresh token:', error);
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