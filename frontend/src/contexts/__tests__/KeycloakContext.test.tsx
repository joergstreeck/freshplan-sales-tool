import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, waitFor, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import { KeycloakProvider, useKeycloak } from '../KeycloakContext';

// Mock the keycloak library
vi.mock('../../lib/keycloak');

function TestComponent() {
  const auth = useKeycloak();
  return (
    <div>
      <span data-testid="authenticated">{auth.isAuthenticated.toString()}</span>
      <span data-testid="loading">{auth.isLoading.toString()}</span>
      <span data-testid="username">{auth.username || 'no-user'}</span>
      <span data-testid="email">{auth.email || 'no-email'}</span>
      <span data-testid="roles">{auth.userRoles.join(',')}</span>
      <button onClick={auth.login} data-testid="login-btn">Login</button>
      <button onClick={() => auth.logout()} data-testid="logout-btn">Logout</button>
    </div>
  );
}

describe('KeycloakContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('provides authentication state after initialization', async () => {
    const { getByTestId } = render(
      <KeycloakProvider>
        <TestComponent />
      </KeycloakProvider>
    );

    // Initially loading
    expect(getByTestId('loading')).toHaveTextContent('true');

    // Wait for initialization
    await waitFor(() => {
      expect(getByTestId('loading')).toHaveTextContent('false');
    });

    // Check authenticated state
    expect(getByTestId('authenticated')).toHaveTextContent('true');
    expect(getByTestId('username')).toHaveTextContent('johndoe');
    expect(getByTestId('email')).toHaveTextContent('john.doe@example.com');
    expect(getByTestId('roles')).toHaveTextContent('admin,sales');
  });

  it('handles login action', async () => {
    const { authUtils, keycloak } = await import('../../lib/keycloak');
    
    const { getByTestId } = render(
      <KeycloakProvider>
        <TestComponent />
      </KeycloakProvider>
    );

    await waitFor(() => {
      expect(getByTestId('loading')).toHaveTextContent('false');
    });

    const loginBtn = getByTestId('login-btn');
    act(() => {
      loginBtn.click();
    });

    expect(keycloak.login).toHaveBeenCalledTimes(1);
  });

  it('handles logout action', async () => {
    const { authUtils, keycloak } = await import('../../lib/keycloak');
    
    const { getByTestId } = render(
      <KeycloakProvider>
        <TestComponent />
      </KeycloakProvider>
    );

    await waitFor(() => {
      expect(getByTestId('loading')).toHaveTextContent('false');
    });

    const logoutBtn = getByTestId('logout-btn');
    act(() => {
      logoutBtn.click();
    });

    expect(keycloak.logout).toHaveBeenCalled();
  });

  it('provides hasRole functionality', async () => {
    const TestRoleComponent = () => {
      const { hasRole } = useKeycloak();
      return (
        <div>
          <span data-testid="is-admin">{hasRole('admin').toString()}</span>
          <span data-testid="is-viewer">{hasRole('viewer').toString()}</span>
        </div>
      );
    };

    const { getByTestId } = render(
      <KeycloakProvider>
        <TestRoleComponent />
      </KeycloakProvider>
    );

    await waitFor(() => {
      expect(getByTestId('is-admin')).toHaveTextContent('true');
      expect(getByTestId('is-viewer')).toHaveTextContent('false');
    });
  });
});