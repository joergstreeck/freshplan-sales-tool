/**
 * Tests für AuthGuard Komponente
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AuthGuard } from './AuthGuard';

// Mock für useAuth Hook
const mockAuth = {
  isAuthenticated: false,
  isLoading: false,
  hasRole: vi.fn(),
};

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => mockAuth,
}));

// Mock für LoginPage
vi.mock('./LoginPage', () => ({
  LoginPage: () => <div data-testid="login-page">Login Page</div>,
}));

describe('AuthGuard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Reset mock defaults
    mockAuth.isAuthenticated = false;
    mockAuth.isLoading = false;
    mockAuth.hasRole.mockReturnValue(false);
  });

  describe('Loading State', () => {
    it('sollte Loading-Spinner zeigen wenn isLoading true ist', () => {
      mockAuth.isLoading = true;

      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      );

      expect(screen.getByText('FreshPlan wird geladen...')).toBeInTheDocument();
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });

    it('sollte animierten Spinner beim Laden haben', () => {
      mockAuth.isLoading = true;

      const { container } = render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      );

      const spinner = container.querySelector('.animate-spin');
      expect(spinner).toBeInTheDocument();
      expect(spinner).toHaveClass(
        'rounded-full',
        'h-32',
        'w-32',
        'border-b-2',
        'border-indigo-600'
      );
    });
  });

  describe('Authentication', () => {
    it('sollte LoginPage zeigen wenn nicht authentifiziert', () => {
      mockAuth.isAuthenticated = false;
      mockAuth.isLoading = false;

      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      );

      expect(screen.getByTestId('login-page')).toBeInTheDocument();
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });

    it('sollte geschützte Inhalte zeigen wenn authentifiziert', () => {
      mockAuth.isAuthenticated = true;
      mockAuth.isLoading = false;

      render(
        <AuthGuard>
          <div>Protected Content</div>
        </AuthGuard>
      );

      expect(screen.getByText('Protected Content')).toBeInTheDocument();
      expect(screen.queryByTestId('login-page')).not.toBeInTheDocument();
    });
  });

  describe('Role-based Authorization', () => {
    it('sollte Inhalte zeigen wenn Benutzer die erforderliche Rolle hat', () => {
      mockAuth.isAuthenticated = true;
      mockAuth.isLoading = false;
      mockAuth.hasRole.mockReturnValue(true);

      render(
        <AuthGuard requiredRole="admin">
          <div>Admin Content</div>
        </AuthGuard>
      );

      expect(screen.getByText('Admin Content')).toBeInTheDocument();
      expect(mockAuth.hasRole).toHaveBeenCalledWith('admin');
    });

    it('sollte Zugriff verweigert zeigen wenn Benutzer nicht die erforderliche Rolle hat', () => {
      mockAuth.isAuthenticated = true;
      mockAuth.isLoading = false;
      mockAuth.hasRole.mockReturnValue(false);

      render(
        <AuthGuard requiredRole="admin">
          <div>Admin Content</div>
        </AuthGuard>
      );

      expect(screen.getByText('Zugriff verweigert')).toBeInTheDocument();
      expect(
        screen.getByText('Sie haben nicht die erforderlichen Berechtigungen für diese Seite.')
      ).toBeInTheDocument();
      expect(screen.getByText('admin')).toBeInTheDocument();
      expect(screen.queryByText('Admin Content')).not.toBeInTheDocument();
    });

    it('sollte benutzerdefiniertes Fallback rendern wenn angegeben', () => {
      mockAuth.isAuthenticated = true;
      mockAuth.isLoading = false;
      mockAuth.hasRole.mockReturnValue(false);

      const customFallback = <div>Custom Unauthorized Message</div>;

      render(
        <AuthGuard requiredRole="admin" fallback={customFallback}>
          <div>Admin Content</div>
        </AuthGuard>
      );

      expect(screen.getByText('Custom Unauthorized Message')).toBeInTheDocument();
      expect(screen.queryByText('Zugriff verweigert')).not.toBeInTheDocument();
      expect(screen.queryByText('Admin Content')).not.toBeInTheDocument();
    });
  });

  describe('Different Scenarios', () => {
    it('sollte ohne requiredRole nur Authentication prüfen', () => {
      mockAuth.isAuthenticated = true;
      mockAuth.isLoading = false;

      render(
        <AuthGuard>
          <div>Public Authenticated Content</div>
        </AuthGuard>
      );

      expect(screen.getByText('Public Authenticated Content')).toBeInTheDocument();
      expect(mockAuth.hasRole).not.toHaveBeenCalled();
    });

    it('sollte mehrere Kinder korrekt rendern', () => {
      mockAuth.isAuthenticated = true;
      mockAuth.isLoading = false;

      render(
        <AuthGuard>
          <div>Child 1</div>
          <div>Child 2</div>
          <div>Child 3</div>
        </AuthGuard>
      );

      expect(screen.getByText('Child 1')).toBeInTheDocument();
      expect(screen.getByText('Child 2')).toBeInTheDocument();
      expect(screen.getByText('Child 3')).toBeInTheDocument();
    });

    it('sollte richtige CSS-Klassen für unauthorisierte Anzeige haben', () => {
      mockAuth.isAuthenticated = true;
      mockAuth.isLoading = false;
      mockAuth.hasRole.mockReturnValue(false);

      const { container } = render(
        <AuthGuard requiredRole="admin">
          <div>Admin Content</div>
        </AuthGuard>
      );

      const unauthorizedContainer = container.querySelector('.min-h-screen.bg-gray-50');
      expect(unauthorizedContainer).toBeInTheDocument();

      const errorIcon = screen.getByText('🚫');
      expect(errorIcon).toHaveClass('text-6xl', 'text-red-500');
    });
  });

  describe('Edge Cases', () => {
    it('sollte mit leeren children umgehen können', () => {
      mockAuth.isAuthenticated = true;
      mockAuth.isLoading = false;

      const { container } = render(<AuthGuard>{null}</AuthGuard>);

      // React Fragment wird gerendert, das keine DOM-Knoten erstellt
      expect(container.childElementCount).toBe(0);
    });

    it('sollte Loading priorisieren über Authentication-Check', () => {
      mockAuth.isAuthenticated = false;
      mockAuth.isLoading = true;

      render(
        <AuthGuard>
          <div>Content</div>
        </AuthGuard>
      );

      expect(screen.getByText('FreshPlan wird geladen...')).toBeInTheDocument();
      expect(screen.queryByTestId('login-page')).not.toBeInTheDocument();
    });
  });
});
