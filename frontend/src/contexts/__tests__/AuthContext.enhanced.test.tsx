/**
 * Enhanced tests for AuthContext - comprehensive coverage for FC-008 Security Foundation
 */
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { AuthProvider, useAuth } from '../AuthContext';

// Mock the entire KeycloakContext module
const mockKeycloakContext = {
  isAuthenticated: false,
  isLoading: false,
  login: vi.fn(),
  logout: vi.fn(),
  token: undefined,
  userId: undefined,
  username: undefined,
  email: undefined,
  hasRole: vi.fn(() => false),
  userRoles: [],
};

vi.mock('../KeycloakContext', () => ({
  useKeycloak: () => mockKeycloakContext,
  KeycloakProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

// Test component that uses auth
const TestComponent = () => {
  const auth = useAuth();

  return (
    <div>
      <div data-testid="authenticated">{auth.isAuthenticated.toString()}</div>
      <div data-testid="loading">{auth.isLoading.toString()}</div>
      <div data-testid="username">{auth.user?.username || 'none'}</div>
      <div data-testid="roles">{auth.user?.roles?.join(',') || 'none'}</div>
      <div data-testid="token">{auth.token || 'none'}</div>
      <button onClick={() => auth.login('test@example.com', 'password')}>Login</button>
      <button onClick={() => auth.logout()}>Logout</button>
    </div>
  );
};

const renderWithAuth = (component: React.ReactElement) => {
  return render(<AuthProvider>{component}</AuthProvider>);
};

describe('AuthContext Enhanced Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Reset mock state
    mockKeycloakContext.isAuthenticated = false;
    mockKeycloakContext.isLoading = false;
    mockKeycloakContext.token = undefined;
    mockKeycloakContext.userId = undefined;
    mockKeycloakContext.username = undefined;
    mockKeycloakContext.email = undefined;
    mockKeycloakContext.userRoles = [];
  });

  describe('Authentication State Management', () => {
    it('should handle unauthenticated state correctly', () => {
      renderWithAuth(<TestComponent />);

      expect(screen.getByTestId('authenticated')).toHaveTextContent('false');
      expect(screen.getByTestId('username')).toHaveTextContent('none');
      expect(screen.getByTestId('roles')).toHaveTextContent('none');
      expect(screen.getByTestId('token')).toHaveTextContent('none');
    });

    it('should handle authenticated state with user data', () => {
      mockKeycloakContext.isAuthenticated = true;
      mockKeycloakContext.token = 'fake-jwt-token';
      mockKeycloakContext.userId = 'user-123';
      mockKeycloakContext.username = 'testuser';
      mockKeycloakContext.email = 'test@example.com';
      mockKeycloakContext.userRoles = ['admin', 'user'];

      renderWithAuth(<TestComponent />);

      expect(screen.getByTestId('authenticated')).toHaveTextContent('true');
      expect(screen.getByTestId('username')).toHaveTextContent('testuser');
      expect(screen.getByTestId('roles')).toHaveTextContent('admin,user');
      expect(screen.getByTestId('token')).toHaveTextContent('fake-jwt-token');
    });

    it('should handle loading state', () => {
      mockKeycloakContext.isLoading = true;

      renderWithAuth(<TestComponent />);

      expect(screen.getByTestId('loading')).toHaveTextContent('true');
    });
  });

  describe('User Object Creation', () => {
    it('should create user object correctly when authenticated', () => {
      mockKeycloakContext.isAuthenticated = true;
      mockKeycloakContext.userId = 'user-456';
      mockKeycloakContext.username = 'johndoe';
      mockKeycloakContext.email = 'john@example.com';
      mockKeycloakContext.userRoles = ['manager', 'editor'];

      const TestAuth = () => {
        const auth = useAuth();
        return (
          <div>
            <div data-testid="user-id">{auth.user?.id}</div>
            <div data-testid="user-name">{auth.user?.name}</div>
            <div data-testid="user-email">{auth.user?.email}</div>
            <div data-testid="user-username">{auth.user?.username}</div>
            <div data-testid="user-roles">{auth.user?.roles?.join(',')}</div>
          </div>
        );
      };

      renderWithAuth(<TestAuth />);

      expect(screen.getByTestId('user-id')).toHaveTextContent('user-456');
      expect(screen.getByTestId('user-name')).toHaveTextContent('johndoe');
      expect(screen.getByTestId('user-email')).toHaveTextContent('john@example.com');
      expect(screen.getByTestId('user-username')).toHaveTextContent('johndoe');
      expect(screen.getByTestId('user-roles')).toHaveTextContent('manager,editor');
    });

    it('should handle missing user data gracefully', () => {
      mockKeycloakContext.isAuthenticated = true;
      mockKeycloakContext.userId = 'user-789';
      // No username or email

      const TestAuth = () => {
        const auth = useAuth();
        return (
          <div>
            <div data-testid="user-id">{auth.user?.id}</div>
            <div data-testid="user-name">{auth.user?.name}</div>
            <div data-testid="user-email">{auth.user?.email}</div>
          </div>
        );
      };

      renderWithAuth(<TestAuth />);

      expect(screen.getByTestId('user-id')).toHaveTextContent('user-789');
      expect(screen.getByTestId('user-name')).toHaveTextContent('Unknown');
      expect(screen.getByTestId('user-email')).toHaveTextContent('');
    });
  });

  describe('Authentication Functions', () => {
    it('should call keycloak login when login is invoked', () => {
      renderWithAuth(<TestComponent />);

      const loginButton = screen.getByText('Login');
      loginButton.click();

      expect(mockKeycloakContext.login).toHaveBeenCalled();
    });

    it('should call keycloak logout when logout is invoked', () => {
      renderWithAuth(<TestComponent />);

      const logoutButton = screen.getByText('Logout');
      logoutButton.click();

      expect(mockKeycloakContext.logout).toHaveBeenCalled();
    });
  });

  describe('Role-Based Access Control', () => {
    beforeEach(() => {
      mockKeycloakContext.isAuthenticated = true;
      mockKeycloakContext.userRoles = ['admin', 'manager'];
    });

    it('should check roles through hasRole function', () => {
      mockKeycloakContext.hasRole.mockImplementation((role: string) =>
        ['admin', 'manager'].includes(role)
      );

      const TestRoles = () => {
        const auth = useAuth();
        return (
          <div>
            <div data-testid="has-admin">{auth.hasRole('admin').toString()}</div>
            <div data-testid="has-user">{auth.hasRole('user').toString()}</div>
          </div>
        );
      };

      renderWithAuth(<TestRoles />);

      expect(screen.getByTestId('has-admin')).toHaveTextContent('true');
      expect(screen.getByTestId('has-user')).toHaveTextContent('false');
    });

    it('should check multiple roles with hasAnyRole', () => {
      mockKeycloakContext.hasRole.mockImplementation((role: string) =>
        ['admin', 'manager'].includes(role)
      );

      const TestAnyRoles = () => {
        const auth = useAuth();
        return (
          <div>
            <div data-testid="has-any-admin-editor">
              {auth.hasAnyRole(['admin', 'editor']).toString()}
            </div>
            <div data-testid="has-any-user-guest">
              {auth.hasAnyRole(['user', 'guest']).toString()}
            </div>
          </div>
        );
      };

      renderWithAuth(<TestAnyRoles />);

      expect(screen.getByTestId('has-any-admin-editor')).toHaveTextContent('true');
      expect(screen.getByTestId('has-any-user-guest')).toHaveTextContent('false');
    });
  });

  describe('Token Management', () => {
    it('should provide auth info for debugging', () => {
      mockKeycloakContext.isAuthenticated = true;
      mockKeycloakContext.userId = 'debug-123';
      mockKeycloakContext.username = 'debuguser';
      mockKeycloakContext.email = 'debug@example.com';
      mockKeycloakContext.userRoles = ['debug'];

      const TestAuthInfo = () => {
        const auth = useAuth();
        const info = auth.authInfo();
        return (
          <div>
            <div data-testid="info-authenticated">{info.authenticated.toString()}</div>
            <div data-testid="info-username">{info.username}</div>
            <div data-testid="info-email">{info.email}</div>
            <div data-testid="info-roles">{info.roles.join(',')}</div>
            <div data-testid="info-token">{info.tokenTimeLeft}</div>
          </div>
        );
      };

      renderWithAuth(<TestAuthInfo />);

      expect(screen.getByTestId('info-authenticated')).toHaveTextContent('true');
      expect(screen.getByTestId('info-username')).toHaveTextContent('debuguser');
      expect(screen.getByTestId('info-email')).toHaveTextContent('debug@example.com');
      expect(screen.getByTestId('info-roles')).toHaveTextContent('debug');
      expect(screen.getByTestId('info-token')).toHaveTextContent('none');
    });

    it('should handle token availability', () => {
      mockKeycloakContext.isAuthenticated = true;
      mockKeycloakContext.token = 'valid-token-123';

      const TestTokenInfo = () => {
        const auth = useAuth();
        const info = auth.authInfo();
        return <div data-testid="token-status">{info.tokenTimeLeft}</div>;
      };

      renderWithAuth(<TestTokenInfo />);

      expect(screen.getByTestId('token-status')).toHaveTextContent('available');
    });
  });
});

describe('useAuth Hook Error Handling', () => {
  it('should throw error when used outside AuthProvider', () => {
    // Suppress console.error for this test
    const originalError = console.error;
    console.error = vi.fn();

    expect(() => {
      render(<TestComponent />);
    }).toThrow('useAuth must be used within AuthProvider');

    console.error = originalError;
  });
});
