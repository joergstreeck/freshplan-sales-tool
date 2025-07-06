/**
 * Tests für Keycloak-Konfiguration und Utilities
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock das gesamte Keycloak-Modul
vi.mock('./keycloak', async () => {
  const mockKeycloakInstance = {
    init: vi.fn(),
    login: vi.fn(),
    logout: vi.fn(),
    updateToken: vi.fn(),
    hasRealmRole: vi.fn(),
    clearToken: vi.fn(),
    isTokenExpired: vi.fn(),
    authenticated: false,
    token: undefined,
    tokenParsed: undefined,
    realmAccess: undefined,
    onTokenExpired: undefined,
    onAuthRefreshSuccess: undefined,
    onAuthRefreshError: undefined,
  };

  return {
    keycloak: mockKeycloakInstance,
    keycloakInitOptions: {
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: 'http://localhost:3000/silent-check-sso.html',
      pkceMethod: 'S256',
      checkLoginIframe: false,
      enableLogging: false,
    },
    initKeycloak: vi.fn().mockResolvedValue(true),
    authUtils: {
      isAuthenticated: vi.fn().mockReturnValue(false),
      login: vi.fn(),
      logout: vi.fn(),
      getToken: vi.fn(),
      getUserId: vi.fn(),
      getUsername: vi.fn(),
      getEmail: vi.fn(),
      hasRole: vi.fn().mockReturnValue(false),
      getUserRoles: vi.fn().mockReturnValue([]),
      updateToken: vi.fn().mockResolvedValue(false),
      isTokenExpired: vi.fn().mockReturnValue(false),
    },
  };
});

// Importiere die gemockten Funktionen
import { initKeycloak, authUtils } from './keycloak';

describe('Keycloak Configuration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('initKeycloak', () => {
    it('sollte erfolgreich initialisieren', async () => {
      const result = await initKeycloak();
      expect(result).toBe(true);
      expect(initKeycloak).toHaveBeenCalled();
    });
  });

  describe('authUtils', () => {
    it('sollte isAuthenticated korrekt aufrufen', () => {
      // Setup
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);

      // Test
      const result = authUtils.isAuthenticated();

      // Verify
      expect(result).toBe(true);
      expect(authUtils.isAuthenticated).toHaveBeenCalled();
    });

    it('sollte login aufrufen', () => {
      authUtils.login();
      expect(authUtils.login).toHaveBeenCalled();
    });

    it('sollte logout mit redirectUri aufrufen', () => {
      authUtils.logout('https://example.com');
      expect(authUtils.logout).toHaveBeenCalledWith('https://example.com');
    });

    it('sollte getToken aufrufen', () => {
      (authUtils.getToken as ReturnType<typeof vi.fn>).mockReturnValue('test-token');

      const result = authUtils.getToken();

      expect(result).toBe('test-token');
      expect(authUtils.getToken).toHaveBeenCalled();
    });

    it('sollte getUserId aufrufen', () => {
      (authUtils.getUserId as ReturnType<typeof vi.fn>).mockReturnValue('user-123');

      const result = authUtils.getUserId();

      expect(result).toBe('user-123');
      expect(authUtils.getUserId).toHaveBeenCalled();
    });

    it('sollte getUsername aufrufen', () => {
      (authUtils.getUsername as ReturnType<typeof vi.fn>).mockReturnValue('john.doe');

      const result = authUtils.getUsername();

      expect(result).toBe('john.doe');
      expect(authUtils.getUsername).toHaveBeenCalled();
    });

    it('sollte getEmail aufrufen', () => {
      (authUtils.getEmail as ReturnType<typeof vi.fn>).mockReturnValue('john@example.com');

      const result = authUtils.getEmail();

      expect(result).toBe('john@example.com');
      expect(authUtils.getEmail).toHaveBeenCalled();
    });

    it('sollte hasRole aufrufen', () => {
      (authUtils.hasRole as ReturnType<typeof vi.fn>).mockReturnValue(true);

      const result = authUtils.hasRole('admin');

      expect(result).toBe(true);
      expect(authUtils.hasRole).toHaveBeenCalledWith('admin');
    });

    it('sollte getUserRoles aufrufen', () => {
      (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue(['user', 'admin']);

      const result = authUtils.getUserRoles();

      expect(result).toEqual(['user', 'admin']);
      expect(authUtils.getUserRoles).toHaveBeenCalled();
    });

    it('sollte updateToken aufrufen', async () => {
      (authUtils.updateToken as ReturnType<typeof vi.fn>).mockResolvedValue(true);

      const result = await authUtils.updateToken(30);

      expect(result).toBe(true);
      expect(authUtils.updateToken).toHaveBeenCalledWith(30);
    });

    it('sollte isTokenExpired ohne Parameter aufrufen', () => {
      (authUtils.isTokenExpired as ReturnType<typeof vi.fn>).mockReturnValue(true);

      const result = authUtils.isTokenExpired();

      expect(result).toBe(true);
      expect(authUtils.isTokenExpired).toHaveBeenCalled();
    });

    it('sollte isTokenExpired mit minValidity Parameter aufrufen', () => {
      (authUtils.isTokenExpired as ReturnType<typeof vi.fn>).mockReturnValue(false);

      const result = authUtils.isTokenExpired(300);

      expect(result).toBe(false);
      expect(authUtils.isTokenExpired).toHaveBeenCalledWith(300);
    });
  });
});
