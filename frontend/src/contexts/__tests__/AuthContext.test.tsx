import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom';
import { AuthProvider, useAuth } from '../AuthContext';
import { KeycloakProvider } from '../KeycloakContext';

// Mock keycloak-js
vi.mock('keycloak-js', () => {
  return {
    default: vi.fn().mockImplementation(() => ({
      init: vi.fn().mockResolvedValue(true),
      authenticated: true,
      token: 'mock-token',
      tokenParsed: {
        sub: 'test-user-id',
        preferred_username: 'testuser',
        email: 'test@example.com',
        realm_access: {
          roles: ['user', 'admin'],
        },
      },
      hasRealmRole: vi.fn((role: string) => ['user', 'admin'].includes(role)),
      login: vi.fn(),
      logout: vi.fn(),
      updateToken: vi.fn().mockResolvedValue(true),
      clearToken: vi.fn(),
      isTokenExpired: vi.fn().mockReturnValue(false),
    })),
  };
});

function TestComponent() {
  const { user, isAuthenticated } = useAuth();
  return (
    <div>
      <span data-testid="user">{user ? user.name : 'no-user'}</span>
      <span data-testid="auth">{isAuthenticated ? 'authenticated' : 'not-authenticated'}</span>
    </div>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders without crashing and provides authentication state', async () => {
    const { getByTestId } = render(
      <KeycloakProvider>
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      </KeycloakProvider>
    );

    // Wait for async initialization
    await vi.waitFor(() => {
      expect(getByTestId('user')).toHaveTextContent('testuser');
      expect(getByTestId('auth')).toHaveTextContent('authenticated');
    });
  });

  it('shows not authenticated when keycloak is not authenticated', async () => {
    // Override the mock for this test
    const Keycloak = await import('keycloak-js');
    vi.mocked(Keycloak.default).mockImplementationOnce(() => ({
      init: vi.fn().mockResolvedValue(false),
      authenticated: false,
      token: undefined,
      tokenParsed: undefined,
      hasRealmRole: vi.fn().mockReturnValue(false),
      login: vi.fn(),
      logout: vi.fn(),
      updateToken: vi.fn().mockResolvedValue(false),
      clearToken: vi.fn(),
      isTokenExpired: vi.fn().mockReturnValue(true),
    }));

    const { getByTestId } = render(
      <KeycloakProvider>
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      </KeycloakProvider>
    );

    await vi.waitFor(() => {
      expect(getByTestId('user')).toHaveTextContent('no-user');
      expect(getByTestId('auth')).toHaveTextContent('not-authenticated');
    });
  });
});