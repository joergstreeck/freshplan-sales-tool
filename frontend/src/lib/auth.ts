/**
 * Authentication Utilities
 * Zentrale Stelle für Auth-Management und User-ID-Bestimmung
 */
import { authUtils } from './keycloak';
import { FALLBACK_USER_ID, IS_DEV_MODE, USE_KEYCLOAK_IN_DEV } from './constants';

/**
 * Ermittelt die aktuelle User-ID basierend auf Auth-Status
 * @returns string - User-ID (von Keycloak oder Fallback)
 */
export const getCurrentUserId = (): string => {
  // Prüfe ob Keycloak in Dev-Mode verwendet werden soll
  if (IS_DEV_MODE && !USE_KEYCLOAK_IN_DEV) {
    return FALLBACK_USER_ID;
  }

  // Versuche User-ID von Keycloak zu bekommen
  const keycloakUserId = authUtils.getUserId();

  if (keycloakUserId) {
    return keycloakUserId;
  }

  // Fallback für Dev-Mode oder wenn Keycloak nicht verfügbar
  if (IS_DEV_MODE) {
    return FALLBACK_USER_ID;
  }

  // In Production sollte das nie passieren
  throw new Error('No authenticated user found and not in development mode');
};

/**
 * Prüft ob ein Benutzer authentifiziert ist
 * @returns boolean
 */
export const isAuthenticated = (): boolean => {
  // In Dev-Mode ohne Keycloak sind wir immer "authentifiziert"
  if (IS_DEV_MODE && !USE_KEYCLOAK_IN_DEV) {
    return true;
  }

  return authUtils.isAuthenticated();
};

/**
 * Holt das aktuelle Auth-Token für API-Calls
 * @returns string | undefined
 */
export const getAuthToken = (): string | undefined => {
  // In Dev-Mode ohne Keycloak kein Token nötig
  if (IS_DEV_MODE && !USE_KEYCLOAK_IN_DEV) {
    return undefined;
  }

  return authUtils.getToken();
};

/**
 * Auth-Informationen für Debugging
 */
export const getAuthInfo = () => {
  return {
    isDevMode: IS_DEV_MODE,
    useKeycloakInDev: USE_KEYCLOAK_IN_DEV,
    isAuthenticated: isAuthenticated(),
    userId: getCurrentUserId(),
    hasToken: !!getAuthToken(),
    keycloakStatus: {
      authenticated: authUtils.isAuthenticated(),
      userId: authUtils.getUserId(),
      username: authUtils.getUsername(),
      email: authUtils.getEmail(),
      roles: authUtils.getUserRoles(),
    },
  };
};
