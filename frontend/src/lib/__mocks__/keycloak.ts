import { vi } from 'vitest';

export const mockKeycloakInstance = {
  init: vi.fn().mockResolvedValue(true),
  authenticated: true,
  token: 'mock-jwt-token',
  tokenParsed: {
    sub: 'user-123',
    preferred_username: 'johndoe',
    email: 'john.doe@example.com',
    realm_access: {
      roles: ['admin', 'sales'],
    },
  },
  hasRealmRole: vi.fn((role: string) => ['admin', 'sales'].includes(role)),
  login: vi.fn(),
  logout: vi.fn(),
  updateToken: vi.fn().mockResolvedValue(true),
  clearToken: vi.fn(),
  isTokenExpired: vi.fn().mockReturnValue(false),
  onTokenExpired: undefined as (() => void) | undefined,
  onAuthSuccess: undefined as (() => void) | undefined,
  onAuthError: undefined as (() => void) | undefined,
  onAuthLogout: undefined as (() => void) | undefined,
};

export const keycloak = mockKeycloakInstance;

export const initKeycloak = vi.fn().mockResolvedValue(true);

export const authUtils = {
  isAuthenticated: () => mockKeycloakInstance.authenticated || false,
  login: () => mockKeycloakInstance.login(),
  logout: (redirectUri?: string) => mockKeycloakInstance.logout({ redirectUri }),
  getToken: () => mockKeycloakInstance.token,
  getUserId: () => mockKeycloakInstance.tokenParsed?.sub,
  getUsername: () => mockKeycloakInstance.tokenParsed?.preferred_username,
  getEmail: () => mockKeycloakInstance.tokenParsed?.email,
  hasRole: (role: string) => mockKeycloakInstance.hasRealmRole(role),
  getUserRoles: () => mockKeycloakInstance.tokenParsed?.realm_access?.roles || [],
  updateToken: async (minValidity: number) => {
    return mockKeycloakInstance.updateToken(minValidity);
  },
  isTokenExpired: (minValidity?: number) => mockKeycloakInstance.isTokenExpired(minValidity),
};