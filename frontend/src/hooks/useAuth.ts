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
  // Always call both hooks to satisfy React's rules
  const keycloakContext = useContext(KeycloakContext);
  const authContext = useContext(AuthContext);

  // Determine which auth context to use
  const useKeycloak = !IS_DEV_MODE || USE_KEYCLOAK_IN_DEV;

  if (useKeycloak && keycloakContext) {
    // Construct user object from Keycloak context
    const user =
      keycloakContext.isAuthenticated && keycloakContext.userId
        ? {
            id: keycloakContext.userId,
            name: keycloakContext.username || keycloakContext.email || 'Unknown',
            email: keycloakContext.email || '',
            username: keycloakContext.username,
            roles: keycloakContext.userRoles,
            // Note: Keycloak may not provide firstName/lastName - they're optional in User interface
          }
        : null;

    return {
      isAuthenticated: keycloakContext.isAuthenticated,
      isLoading: keycloakContext.isLoading,
      login: keycloakContext.login,
      logout: keycloakContext.logout,
      token: keycloakContext.token,
      user, // Full user object
      userId: keycloakContext.userId,
      username: keycloakContext.username,
      email: keycloakContext.email,
      hasRole: keycloakContext.hasRole,
      userRoles: keycloakContext.userRoles,
    };
  }

  if (authContext) {
    // Map AuthContext to the same interface
    return {
      isAuthenticated: authContext.isAuthenticated,
      isLoading: authContext.isLoading,
      login: authContext.login,
      logout: authContext.logout,
      token: authContext.token,
      user: authContext.user, // Full user object (includes firstName, lastName, name, etc.)
      userId: authContext.user?.id,
      username: authContext.user?.username,
      email: authContext.user?.email,
      hasRole: (role: string) => authContext.user?.roles?.includes(role) || false,
      userRoles: authContext.user?.roles || [],
    };
  }

  // Return default values if no auth context is available
  return {
    isAuthenticated: false,
    isLoading: false,
    login: () => {
      // No auth provider available - handled silently
    },
    logout: () => {
      // No auth provider available - handled silently
    },
    token: undefined,
    user: null, // No user when not authenticated
    userId: undefined,
    username: undefined,
    email: undefined,
    hasRole: () => false,
    userRoles: [],
  };
}
