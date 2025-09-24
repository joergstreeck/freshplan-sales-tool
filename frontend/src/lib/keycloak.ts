/**
 * Keycloak Configuration and Instance
 */
import Keycloak from 'keycloak-js';

// Keycloak Konfiguration
const keycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8180',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'freshplan-realm', // Updated to match production
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'freshplan-frontend',
};

// Keycloak Instance erstellen
export const keycloak = new Keycloak(keycloakConfig);

// Track initialization state
let isInitialized = false;
let initializationPromise: Promise<boolean> | null = null;

// Keycloak Initialisierungsoptionen
export const keycloakInitOptions = {
  onLoad: 'check-sso' as const,
  silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
  pkceMethod: 'S256' as const,
  checkLoginIframe: false, // Für Dev-Mode
  enableLogging: import.meta.env.DEV,
};

/**
 * Initialisiert Keycloak
 * @returns Promise<boolean> - true wenn erfolgreich initialisiert
 */
export const initKeycloak = async (): Promise<boolean> => {
  // Check for auth bypass FIRST
  if (import.meta.env.DEV && import.meta.env.VITE_AUTH_BYPASS === 'true') {
    isInitialized = true;
    return false; // Not authenticated, but that's OK in bypass mode
  }

  // Prevent multiple initializations
  if (isInitialized) {
    return keycloak.authenticated || false;
  }

  // If initialization is already in progress, wait for it
  if (initializationPromise) {
    return initializationPromise;
  }

  // Start initialization
  initializationPromise = (async () => {
    try {
      const authenticated = await keycloak.init(keycloakInitOptions);
      isInitialized = true;

      // Enhanced Token-Refresh-Setup with better error handling
      if (authenticated) {
        // Proactive token refresh - 2 minutes before expiry
        keycloak.onTokenExpired = async () => {
          try {
            const refreshed = await keycloak.updateToken(120); // 2 minutes buffer
            if (refreshed) {
              // Dispatch success event for app-wide notification
              window.dispatchEvent(
                new CustomEvent('auth-token-refreshed', {
                  detail: { timestamp: new Date().toISOString() },
                })
              );
            }
          } catch (_error) {
            void _error;
            // Dispatch error event before redirect
            window.dispatchEvent(
              new CustomEvent('auth-error', {
                detail: {
                  type: 'token-refresh-failed',
                  message: 'Ihre Sitzung ist abgelaufen. Sie werden zur Anmeldung weitergeleitet.',
                  error,
                },
              })
            );
            // Graceful redirect after 2 seconds
            setTimeout(() => keycloak.login(), 2000);
          }
        };

        // Success handler with token validation
        keycloak.onAuthRefreshSuccess = () => {
          window.dispatchEvent(new CustomEvent('auth-refresh-success'));
        };

        // Enhanced error handling
        keycloak.onAuthRefreshError = () => {
          keycloak.clearToken();
          window.dispatchEvent(
            new CustomEvent('auth-error', {
              detail: {
                type: 'refresh-error',
                message: 'Authentifizierung fehlgeschlagen. Bitte melden Sie sich erneut an.',
              },
            })
          );
        };

        // Setup periodic token validation (every 30 seconds)
        setInterval(() => {
          if (keycloak.authenticated && keycloak.isTokenExpired(180)) {
            // Token expires in less than 3 minutes - refresh it
            keycloak.updateToken(180).catch(() => {});
          }
        }, 30000);
      }

      return authenticated;
    } catch (_error) {
      void _error;
      isInitialized = false;
      initializationPromise = null;
      // Dispatch error event for global handling
      const event = new CustomEvent('auth-error', {
        detail: {
          type: 'init-failed',
          message: 'Authentifizierungssystem konnte nicht initialisiert werden.',
          error,
        },
      });
      window.dispatchEvent(event);
      return false;
    }
  })();

  return initializationPromise;
};

/**
 * Enhanced authentication utilities with robust error handling and validation
 */
export const authUtils = {
  isAuthenticated: () => keycloak.authenticated || false,

  login: (options?: { redirectUri?: string; prompt?: string }) => {
    return keycloak.login({
      redirectUri: options?.redirectUri || window.location.origin,
      prompt: options?.prompt || 'login',
    });
  },

  logout: (redirectUri?: string) => {
    // Clear any app-specific storage before logout
    window.dispatchEvent(new CustomEvent('auth-logout-initiated'));
    return keycloak.logout({
      redirectUri:
        redirectUri || import.meta.env.VITE_LOGOUT_REDIRECT_URI || window.location.origin,
    });
  },

  getToken: () => {
    if (!keycloak.authenticated || !keycloak.token) {
      return null;
    }
    // Validate token is not expired
    if (keycloak.isTokenExpired(30)) {
      return null;
    }
    return keycloak.token;
  },

  getValidToken: async () => {
    if (!keycloak.authenticated) {
      return null;
    }

    try {
      // Ensure token is valid for at least 30 seconds
      if (keycloak.isTokenExpired(30)) {
        await keycloak.updateToken(30);
      }
      return keycloak.token;
    } catch (_error) {
      void _error;
      return null;
    }
  },

  getUserId: () => keycloak.tokenParsed?.sub,
  getUsername: () => keycloak.tokenParsed?.preferred_username,
  getEmail: () => keycloak.tokenParsed?.email,
  getFullName: () => keycloak.tokenParsed?.name,

  hasRole: (role: string) => {
    if (!keycloak.authenticated) return false;
    return keycloak.hasRealmRole(role) || keycloak.hasResourceRole(role);
  },

  hasAnyRole: (roles: string[]) => {
    if (!keycloak.authenticated) return false;
    return roles.some(role => authUtils.hasRole(role));
  },

  getUserRoles: () => {
    if (!keycloak.authenticated || !keycloak.tokenParsed) return [];
    const realmRoles = keycloak.tokenParsed.realm_access?.roles || [];
    const resourceRoles = Object.values(keycloak.tokenParsed.resource_access || {}).flatMap(
      (resource: { roles?: string[] }) => resource.roles || []
    );
    return [...new Set([...realmRoles, ...resourceRoles])];
  },

  updateToken: async (minValidity: number = 30) => {
    if (!keycloak.authenticated) {
      return false;
    }

    try {
      const refreshed = await keycloak.updateToken(minValidity);
      if (refreshed) {
        window.dispatchEvent(
          new CustomEvent('auth-token-updated', {
            detail: { timestamp: new Date().toISOString() },
          })
        );
      }
      return refreshed;
    } catch (_error) {
      void _error;
      return false;
    }
  },

  isTokenExpired: (minValidity?: number) => {
    if (!keycloak.authenticated) return true;
    return keycloak.isTokenExpired ? keycloak.isTokenExpired(minValidity) : true;
  },

  getTokenTimeLeft: () => {
    if (!keycloak.authenticated || !keycloak.tokenParsed?.exp) {
      return 0;
    }
    const exp = keycloak.tokenParsed.exp * 1000; // Convert to milliseconds
    const now = Date.now();
    return Math.max(0, exp - now);
  },

  // Enhanced debugging and monitoring
  getAuthInfo: () => ({
    authenticated: keycloak.authenticated,
    username: authUtils.getUsername(),
    email: authUtils.getEmail(),
    roles: authUtils.getUserRoles(),
    tokenTimeLeft: authUtils.getTokenTimeLeft(),
    tokenExpired: authUtils.isTokenExpired(),
  }),
};
