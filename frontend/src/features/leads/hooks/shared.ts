/**
 * Shared utilities for enum hooks
 *
 * Sprint 2.1.6: Single Source of Truth pattern
 * Centralized auth logic and types to avoid duplication
 */

/**
 * Enum value returned by backend API
 */
export interface EnumValue {
  value: string;
  label: string;
}

/**
 * Base API URL
 */
export const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

/**
 * Get authentication headers for API requests
 *
 * Dev Mode: Uses dev-auth-user from sessionStorage (bypass Keycloak)
 * Production: Uses JWT from localStorage (Keycloak)
 */
export function getAuthHeaders() {
  // Dev Mode: Use dev-auth-user from sessionStorage (bypass Keycloak)
  if (import.meta.env.DEV) {
    const devUser = sessionStorage.getItem('dev-auth-user');
    if (devUser) {
      const user = JSON.parse(devUser);
      const mockToken = `dev.${user.id}.${user.username}`;
      return { Authorization: `Bearer ${mockToken}` };
    }
  }

  // Production: JWT from localStorage (Keycloak)
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}
