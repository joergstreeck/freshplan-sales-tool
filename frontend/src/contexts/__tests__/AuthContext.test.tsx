import { describe, it, expect, vi } from 'vitest';
import { render, waitFor } from '../../test/test-utils';
import '@testing-library/jest-dom';
import { AuthProvider, useAuth } from '../AuthContext';
import { KeycloakProvider } from '../KeycloakContext';

// Mock keycloak module - uses __mocks__/keycloak.ts
vi.mock('../../lib/keycloak');

import { initKeycloak, authUtils } from '../../lib/keycloak';

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
    // Mock environment variables to disable auth bypass in tests
    vi.stubEnv('DEV', 'false');
    vi.stubEnv('VITE_AUTH_BYPASS', 'false');
    // Reset mock implementations after clearAllMocks
    (initKeycloak as ReturnType<typeof vi.fn>).mockResolvedValue(true);
    (authUtils.getToken as ReturnType<typeof vi.fn>).mockReturnValue('mock-token');
    (authUtils.getUserId as ReturnType<typeof vi.fn>).mockReturnValue('test-user-id');
    (authUtils.getUsername as ReturnType<typeof vi.fn>).mockReturnValue('testuser');
    (authUtils.getEmail as ReturnType<typeof vi.fn>).mockReturnValue('test@example.com');
    (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue(['user', 'admin']);
    (authUtils.hasRole as ReturnType<typeof vi.fn>).mockImplementation((role: string) =>
      ['user', 'admin'].includes(role)
    );
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllEnvs();
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
    await waitFor(() => {
      expect(getByTestId('user')).toHaveTextContent('testuser');
      expect(getByTestId('auth')).toHaveTextContent('authenticated');
    });
  });

  it('shows not authenticated when keycloak is not authenticated', async () => {
    // Configure mocks for non-authenticated state
    (initKeycloak as ReturnType<typeof vi.fn>).mockResolvedValue(false);
    (authUtils.getToken as ReturnType<typeof vi.fn>).mockReturnValue(undefined);
    (authUtils.getUserId as ReturnType<typeof vi.fn>).mockReturnValue(undefined);
    (authUtils.getUsername as ReturnType<typeof vi.fn>).mockReturnValue(undefined);
    (authUtils.getEmail as ReturnType<typeof vi.fn>).mockReturnValue(undefined);
    (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue([]);

    const { getByTestId } = render(
      <KeycloakProvider>
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      </KeycloakProvider>
    );

    await waitFor(() => {
      expect(getByTestId('user')).toHaveTextContent('no-user');
      expect(getByTestId('auth')).toHaveTextContent('not-authenticated');
    });
  });
});
