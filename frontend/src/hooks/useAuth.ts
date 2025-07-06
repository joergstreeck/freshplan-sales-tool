/**
 * Unified Auth Hook that works with both Keycloak and Fallback Auth
 * 
 * This hook abstracts the authentication mechanism and provides
 * a consistent interface regardless of which auth provider is active.
 */
import { useContext } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import { KeycloakContext } from '../contexts/KeycloakContext';
import { USE_KEYCLOAK_IN_DEV, IS_DEV_MODE } from '../lib/constants';

export function useAuth() {
  // Determine which auth context to use
  const useKeycloak = !IS_DEV_MODE || USE_KEYCLOAK_IN_DEV;
  
  // Try to get the appropriate context
  let authData;
  
  if (useKeycloak) {
    try {
      // Try Keycloak context
      const keycloakContext = useContext(KeycloakContext);
      if (keycloakContext) {
        return {
          isAuthenticated: keycloakContext.isAuthenticated,
          isLoading: keycloakContext.isLoading,
          login: keycloakContext.login,
          logout: keycloakContext.logout,
          token: keycloakContext.token,
          userId: keycloakContext.userId,
          username: keycloakContext.username,
          email: keycloakContext.email,
          hasRole: keycloakContext.hasRole,
          userRoles: keycloakContext.userRoles,
        };
      }
    } catch {
      // Keycloak context not available, fall through to AuthContext
    }
  }
  
  // Fallback to AuthContext
  try {
    const authContext = useContext(AuthContext);
    if (authContext) {
      // Map AuthContext to the same interface
      return {
        isAuthenticated: authContext.isAuthenticated,
        isLoading: authContext.isLoading,
        login: authContext.login,
        logout: authContext.logout,
        token: authContext.token,
        userId: authContext.user?.id,
        username: authContext.user?.username,
        email: authContext.user?.email,
        hasRole: (role: string) => authContext.user?.roles?.includes(role) || false,
        userRoles: authContext.user?.roles || [],
      };
    }
  } catch {
    // AuthContext not available either
  }
  
  // Return default values if no auth context is available
  return {
    isAuthenticated: false,
    isLoading: false,
    login: () => console.warn('No auth provider available'),
    logout: () => console.warn('No auth provider available'),
    token: undefined,
    userId: undefined,
    username: undefined,
    email: undefined,
    hasRole: () => false,
    userRoles: [],
  };
}