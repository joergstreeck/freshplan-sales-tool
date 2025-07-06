/**
 * Globale Konstanten für die FreshPlan Anwendung
 */

/**
 * Fallback-User-ID für Entwicklung und Testing
 * Diese ID wird verwendet, wenn Keycloak nicht verfügbar ist
 * @deprecated Use getCurrentUserId() from auth utils instead
 */
export const FALLBACK_USER_ID = '00000000-0000-0000-0000-000000000000';

/**
 * Development Mode Check
 * In Dev-Mode können wir zwischen Keycloak und Fallback wechseln
 */
export const IS_DEV_MODE = import.meta.env.DEV;

/**
 * Keycloak Development Toggle
 * Kann über Environment Variable gesteuert werden
 */
export const USE_KEYCLOAK_IN_DEV = import.meta.env.VITE_USE_KEYCLOAK_IN_DEV === 'true';
