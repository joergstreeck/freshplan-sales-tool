/**
 * Tests für auth.ts - Authentication Utilities
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { authUtils } from './keycloak';

// Mock für keycloak module
vi.mock('./keycloak', () => ({
  authUtils: {
    getUserId: vi.fn(),
    isAuthenticated: vi.fn(),
    getToken: vi.fn(),
    getUsername: vi.fn(),
    getEmail: vi.fn(),
    getUserRoles: vi.fn(),
  },
}));

// Mock constants module
vi.mock('./constants', () => {
  let mockIsDev = false;
  let mockUseKeycloak = false;

  return {
    get IS_DEV_MODE() {
      return mockIsDev;
    },
    get USE_KEYCLOAK_IN_DEV() {
      return mockUseKeycloak;
    },
    FALLBACK_USER_ID: '00000000-0000-0000-0000-000000000000',
    __setMockValues: (isDev: boolean, useKeycloak: boolean) => {
      mockIsDev = isDev;
      mockUseKeycloak = useKeycloak;
    },
  };
});

// Import after mocks
import { getCurrentUserId, isAuthenticated, getAuthToken, getAuthInfo } from './auth';
import { FALLBACK_USER_ID } from './constants';

// Helper für Test-Setup
const constants = await import('./constants');
const setMockConstants = (isDev: boolean, useKeycloak: boolean) => {
  (
    constants as { __setMockValues: (isDev: boolean, useKeycloak: boolean) => void }
  ).__setMockValues(isDev, useKeycloak);
};

describe('auth', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    setMockConstants(false, false); // Default: Production, no Keycloak
  });

  describe('getCurrentUserId', () => {
    it('sollte Fallback-ID in Dev-Mode ohne Keycloak zurückgeben', () => {
      setMockConstants(true, false);

      const userId = getCurrentUserId();

      expect(userId).toBe(FALLBACK_USER_ID);
      expect(authUtils.getUserId).not.toHaveBeenCalled();
    });

    it('sollte Keycloak User-ID zurückgeben wenn verfügbar', () => {
      setMockConstants(false, false);
      (authUtils.getUserId as ReturnType<typeof vi.fn>).mockReturnValue('keycloak-user-123');

      const userId = getCurrentUserId();

      expect(userId).toBe('keycloak-user-123');
      expect(authUtils.getUserId).toHaveBeenCalled();
    });

    it('sollte Fallback-ID in Dev-Mode zurückgeben wenn Keycloak keine ID hat', () => {
      setMockConstants(true, true);
      (authUtils.getUserId as ReturnType<typeof vi.fn>).mockReturnValue(undefined);

      // Mock console.warn
      const consoleWarn = vi.spyOn(console, 'warn').mockImplementation(() => {});

      const userId = getCurrentUserId();

      expect(userId).toBe(FALLBACK_USER_ID);
      expect(authUtils.getUserId).toHaveBeenCalled();
      expect(consoleWarn).toHaveBeenCalledWith('Using fallback user ID in development mode');

      consoleWarn.mockRestore();
    });

    it('sollte Error werfen in Production ohne authentifizierten User', () => {
      setMockConstants(false, false);
      (authUtils.getUserId as ReturnType<typeof vi.fn>).mockReturnValue(undefined);

      expect(() => getCurrentUserId()).toThrow(
        'No authenticated user found and not in development mode'
      );
    });
  });

  describe('isAuthenticated', () => {
    it('sollte true in Dev-Mode ohne Keycloak zurückgeben', () => {
      setMockConstants(true, false);

      const authenticated = isAuthenticated();

      expect(authenticated).toBe(true);
      expect(authUtils.isAuthenticated).not.toHaveBeenCalled();
    });

    it('sollte Keycloak Auth-Status zurückgeben wenn Keycloak verwendet wird', () => {
      setMockConstants(false, false);
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);

      const authenticated = isAuthenticated();

      expect(authenticated).toBe(true);
      expect(authUtils.isAuthenticated).toHaveBeenCalled();
    });

    it('sollte Keycloak Auth-Status in Dev-Mode mit Keycloak zurückgeben', () => {
      setMockConstants(true, true);
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(false);

      const authenticated = isAuthenticated();

      expect(authenticated).toBe(false);
      expect(authUtils.isAuthenticated).toHaveBeenCalled();
    });
  });

  describe('getAuthToken', () => {
    it('sollte undefined in Dev-Mode ohne Keycloak zurückgeben', () => {
      setMockConstants(true, false);

      const token = getAuthToken();

      expect(token).toBeUndefined();
      expect(authUtils.getToken).not.toHaveBeenCalled();
    });

    it('sollte Keycloak Token zurückgeben wenn Keycloak verwendet wird', () => {
      setMockConstants(false, false);
      (authUtils.getToken as ReturnType<typeof vi.fn>).mockReturnValue('test-token-123');

      const token = getAuthToken();

      expect(token).toBe('test-token-123');
      expect(authUtils.getToken).toHaveBeenCalled();
    });

    it('sollte undefined zurückgeben wenn Keycloak kein Token hat', () => {
      setMockConstants(false, false);
      (authUtils.getToken as ReturnType<typeof vi.fn>).mockReturnValue(undefined);

      const token = getAuthToken();

      expect(token).toBeUndefined();
      expect(authUtils.getToken).toHaveBeenCalled();
    });
  });

  describe('getAuthInfo', () => {
    it('sollte vollständige Auth-Info in Dev-Mode ohne Keycloak zurückgeben', () => {
      setMockConstants(true, false);
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(false);
      (authUtils.getUserId as ReturnType<typeof vi.fn>).mockReturnValue(undefined);
      (authUtils.getUsername as ReturnType<typeof vi.fn>).mockReturnValue(undefined);
      (authUtils.getEmail as ReturnType<typeof vi.fn>).mockReturnValue(undefined);
      (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue(undefined);

      const info = getAuthInfo();

      expect(info).toEqual({
        isDevMode: true,
        useKeycloakInDev: false,
        isAuthenticated: true,
        userId: FALLBACK_USER_ID,
        hasToken: false,
        keycloakStatus: {
          authenticated: false,
          userId: undefined,
          username: undefined,
          email: undefined,
          roles: undefined,
        },
      });
    });

    it('sollte vollständige Auth-Info mit Keycloak-Daten zurückgeben', () => {
      setMockConstants(false, false);
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);
      (authUtils.getUserId as ReturnType<typeof vi.fn>).mockReturnValue('user-123');
      (authUtils.getToken as ReturnType<typeof vi.fn>).mockReturnValue('token-123');
      (authUtils.getUsername as ReturnType<typeof vi.fn>).mockReturnValue('john.doe');
      (authUtils.getEmail as ReturnType<typeof vi.fn>).mockReturnValue('john@example.com');
      (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue(['user', 'admin']);

      const info = getAuthInfo();

      expect(info).toEqual({
        isDevMode: false,
        useKeycloakInDev: false,
        isAuthenticated: true,
        userId: 'user-123',
        hasToken: true,
        keycloakStatus: {
          authenticated: true,
          userId: 'user-123',
          username: 'john.doe',
          email: 'john@example.com',
          roles: ['user', 'admin'],
        },
      });
    });

    it('sollte korrekte Info in Dev-Mode mit Keycloak zurückgeben', () => {
      setMockConstants(true, true);
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);
      (authUtils.getUserId as ReturnType<typeof vi.fn>).mockReturnValue('keycloak-user');
      (authUtils.getToken as ReturnType<typeof vi.fn>).mockReturnValue('keycloak-token');

      const info = getAuthInfo();

      expect(info.isDevMode).toBe(true);
      expect(info.useKeycloakInDev).toBe(true);
      expect(info.isAuthenticated).toBe(true);
      expect(info.userId).toBe('keycloak-user');
      expect(info.hasToken).toBe(true);
    });
  });
});
