import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom';
import { AuthProvider, useAuth } from '../AuthContext';

function TestComponent() {
  const { user, isAuthenticated } = useAuth();
  return (
    <div>
      <span data-testid="user">{user ? user.name : 'no-user'}</span>
      <span data-testid="auth">
        {isAuthenticated ? 'authenticated' : 'not-authenticated'}
      </span>
    </div>
  );
}

describe('AuthContext', () => {
  it('renders without crashing and provides default state', () => {
    const { getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(getByTestId('user')).toHaveTextContent('no-user');
    expect(getByTestId('auth')).toHaveTextContent('not-authenticated');
  });
});
