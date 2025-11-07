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
  isAuthenticated: vi.fn(() => mockKeycloakInstance.authenticated || false),
  login: vi.fn(() => mockKeycloakInstance.login()),
  logout: vi.fn((redirectUri?: string) => mockKeycloakInstance.logout({ redirectUri })),
  getToken: vi.fn(() => mockKeycloakInstance.token),
  getUserId: vi.fn(() => mockKeycloakInstance.tokenParsed?.sub),
  getUsername: vi.fn(() => mockKeycloakInstance.tokenParsed?.preferred_username),
  getEmail: vi.fn(() => mockKeycloakInstance.tokenParsed?.email),
  hasRole: vi.fn((role: string) => mockKeycloakInstance.hasRealmRole(role)),
  getUserRoles: vi.fn(() => mockKeycloakInstance.tokenParsed?.realm_access?.roles || []),
  updateToken: vi.fn(async (minValidity: number) => {
    return mockKeycloakInstance.updateToken(minValidity);
  }),
  isTokenExpired: vi.fn((minValidity?: number) => mockKeycloakInstance.isTokenExpired(minValidity)),
};
