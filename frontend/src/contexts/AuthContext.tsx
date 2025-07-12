/* eslint-disable react-refresh/only-export-components */
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
  const login = async (_email: string, _password: string) => {
    // Email and password are ignored - Keycloak handles authentication
    keycloak.login();
  };

  const contextValue: AuthContextType = {
    user,
    isAuthenticated: keycloak.isAuthenticated,
    isLoading: keycloak.isLoading,
    login,
    logout: keycloak.logout,
    token: keycloak.token || null,
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