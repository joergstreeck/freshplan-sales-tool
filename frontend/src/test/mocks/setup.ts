import { vi } from 'vitest';

// Mock Keycloak globally for all tests
vi.mock('../lib/keycloak', () => ({
  keycloak: {
    init: vi.fn().mockResolvedValue(true),
    login: vi.fn(),
    logout: vi.fn(),
    updateToken: vi.fn().mockResolvedValue(true),
    authenticated: true,
    token: 'mock-token',
    tokenParsed: {
      sub: 'user-123',
      preferred_username: 'testuser',
      email: 'test@example.com',
      realm_access: { roles: ['user', 'admin'] },
    },
  },
  initKeycloak: vi.fn().mockResolvedValue(true),
  authUtils: {
    isAuthenticated: vi.fn().mockReturnValue(true),
    login: vi.fn(),
    logout: vi.fn(),
    getToken: vi.fn().mockReturnValue('mock-token'),
    getUserId: vi.fn().mockReturnValue('user-123'),
    getUsername: vi.fn().mockReturnValue('testuser'),
    getEmail: vi.fn().mockReturnValue('test@example.com'),
    hasRole: vi.fn().mockReturnValue(true),
    getUserRoles: vi.fn().mockReturnValue(['user', 'admin']),
  },
}));

// Mock react-intersection-observer
vi.mock('react-intersection-observer', () => ({
  useInView: () => ({
    ref: vi.fn(),
    inView: true,
    entry: undefined,
  }),
  InView: ({ children }: { children: (args: { inView: boolean; ref: () => void }) => React.ReactNode }) => children({ inView: true, ref: vi.fn() }),
}));

// Mock useUniversalSearch hook
vi.mock('../features/customers/hooks/useUniversalSearch', () => ({
  useUniversalSearch: () => ({
    searchResults: null,
    isSearching: false,
    searchError: null,
    performUniversalSearch: vi.fn(),
    clearResults: vi.fn(),
    searchQuery: '',
    search: vi.fn(),
  }),
}));

// Mock MSW for API calls
vi.mock('../mocks/browser', () => ({
  worker: {
    start: vi.fn().mockResolvedValue(undefined),
    stop: vi.fn(),
    resetHandlers: vi.fn(),
  },
}));

export {};