/**
 * Keycloak Configuration and Instance
 */
import Keycloak from 'keycloak-js';

// Keycloak Konfiguration
const keycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8180',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'freshplan',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'freshplan-frontend',
};

// Keycloak Instance erstellen
export const keycloak = new Keycloak(keycloakConfig);

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
  try {
    const authenticated = await keycloak.init(keycloakInitOptions);

    // Token-Refresh-Setup - Event-basiert
    if (authenticated) {
      // Token automatisch erneuern wenn es in 5 Minuten abläuft
      keycloak.onTokenExpired = () => {
        keycloak
          .updateToken(300) // 5 Minuten Buffer
          .then(() => {
            // Token successfully refreshed
            console.log('Token successfully refreshed');
          })
          .catch(error => {
            console.error('Token refresh failed:', error);
            keycloak.login();
          });
      };

      // Vor jedem authentifizierten Request prüfen
      keycloak.onAuthRefreshSuccess = () => {
        // Auth refresh successful
      };

      keycloak.onAuthRefreshError = () => {
        keycloak.clearToken();
      };
    }

    return authenticated;
  } catch (error) {
    console.error('Keycloak initialization failed:', error);
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
};

/**
 * Hilfsfunktionen für Auth-Zustand
 */
export const authUtils = {
  isAuthenticated: () => keycloak.authenticated || false,
  login: () => keycloak.login(),
  logout: (redirectUri?: string) =>
    keycloak.logout({
      redirectUri:
        redirectUri || import.meta.env.VITE_LOGOUT_REDIRECT_URI || window.location.origin,
    }),
  getToken: () => keycloak.token,
  getUserId: () => keycloak.tokenParsed?.sub,
  getUsername: () => keycloak.tokenParsed?.preferred_username,
  getEmail: () => keycloak.tokenParsed?.email,
  hasRole: (role: string) => keycloak.hasRealmRole(role),
  getUserRoles: () => keycloak.tokenParsed?.realm_access?.roles || [],
  updateToken: async (minValidity: number) => {
    try {
      const refreshed = await keycloak.updateToken(minValidity);
      return refreshed;
    } catch {
      return false;
    }
  },
  isTokenExpired: (minValidity?: number) => (keycloak.isTokenExpired ? keycloak.isTokenExpired(minValidity) : true),
};
