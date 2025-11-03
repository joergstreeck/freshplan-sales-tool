/**
 * Enterprise Test Suite - ProtectedRoute Component
 * Sprint 2.1.7.7 - RBAC Enhancement
 *
 * Test Coverage: Route Protection + Role-based Access Control
 *
 * Security-Critical Tests: ✅
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from '../ProtectedRoute';
import * as featureFlags from '@/config/featureFlags';

// Mock dependencies
vi.mock('@/hooks/useAuth');
vi.mock('@/config/featureFlags');
vi.mock('@/components/layout/MainLayoutV2', () => ({
  MainLayoutV2: ({ children }: { children: React.ReactNode }) => <div data-testid="main-layout">{children}</div>,
}));

import { useAuth } from '@/hooks/useAuth';

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;

describe('ProtectedRoute - Security Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Default: authBypass disabled
    vi.mocked(featureFlags.isFeatureEnabled).mockReturnValue(false);
  });

  describe('Authentication Flow', () => {
    it('should render children when user is authenticated and has required role', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role.toUpperCase() === 'ADMIN',
        user: { id: '1', name: 'Admin User', roles: ['admin'] },
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['admin'],
      });

      render(
        <MemoryRouter>
          <ProtectedRoute allowedRoles={['admin']}>
            <div data-testid="protected-content">Admin Content</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      expect(screen.getByTestId('protected-content')).toBeInTheDocument();
      expect(screen.getByText('Admin Content')).toBeInTheDocument();
    });

    it('should redirect to /login when user is not authenticated', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        hasRole: vi.fn(() => false),
        user: null,
        token: null,
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: [],
      });

      render(
        <MemoryRouter initialEntries={['/admin']}>
          <Routes>
            <Route path="/admin" element={<ProtectedRoute allowedRoles={['admin']}><div>Admin</div></ProtectedRoute>} />
            <Route path="/login" element={<div data-testid="login-page">Login Page</div>} />
          </Routes>
        </MemoryRouter>
      );

      // Should redirect to login
      expect(screen.getByTestId('login-page')).toBeInTheDocument();
    });

    it('should show loading spinner when authentication is in progress', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: true, // Loading state
        hasRole: vi.fn(() => false),
        user: null,
        token: null,
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: [],
      });

      render(
        <MemoryRouter>
          <ProtectedRoute allowedRoles={['admin']}>
            <div>Admin Content</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      // Should show CircularProgress (MUI renders role="progressbar")
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });
  });

  describe('Role-Based Access Control', () => {
    it('should show error alert when user lacks required role', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role.toUpperCase() === 'SALES', // Only has SALES role
        user: { id: '1', name: 'Sales User', roles: ['sales'] },
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['sales'],
      });

      render(
        <MemoryRouter>
          <ProtectedRoute allowedRoles={['admin']}>
            <div data-testid="protected-content">Admin Content</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      // Should show error alert
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
      expect(screen.getByText(/admin/i)).toBeInTheDocument();

      // Should NOT render protected content
      expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
    });

    it('should allow access with any matching role (admin OR auditor)', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role.toUpperCase() === 'AUDITOR', // Has AUDITOR role
        user: { id: '1', name: 'Auditor User', roles: ['auditor'] },
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['auditor'],
      });

      render(
        <MemoryRouter>
          <ProtectedRoute allowedRoles={['admin', 'auditor']}>
            <div data-testid="protected-content">Audit Dashboard</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      // Should allow access with AUDITOR role
      expect(screen.getByTestId('protected-content')).toBeInTheDocument();
      expect(screen.getByText('Audit Dashboard')).toBeInTheDocument();
    });

    it('should allow access when allowedRoles is empty array (no role restriction)', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => false), // No roles at all
        user: { id: '1', name: 'Basic User', roles: [] },
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: [],
      });

      render(
        <MemoryRouter>
          <ProtectedRoute allowedRoles={[]}>
            <div data-testid="protected-content">Public Authenticated Content</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      // Should allow access with no role restrictions
      expect(screen.getByTestId('protected-content')).toBeInTheDocument();
    });

    it('should handle multiple user roles correctly', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => ['ADMIN', 'MANAGER'].includes(role.toUpperCase()),
        user: { id: '1', name: 'Multi Role User', roles: ['admin', 'manager'] },
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['admin', 'manager'],
      });

      render(
        <MemoryRouter>
          <ProtectedRoute allowedRoles={['manager']}>
            <div data-testid="protected-content">Manager Dashboard</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      // Should allow access via MANAGER role
      expect(screen.getByTestId('protected-content')).toBeInTheDocument();
    });
  });

  describe('Auth-Bypass Mode (Development)', () => {
    it('should skip authentication check in authBypass mode with correct role', () => {
      // Enable authBypass
      vi.mocked(featureFlags.isFeatureEnabled).mockReturnValue(true);

      mockUseAuth.mockReturnValue({
        isAuthenticated: false, // NOT authenticated in authBypass mode
        isLoading: false,
        hasRole: (role: string) => role.toUpperCase() === 'ADMIN',
        user: { id: '1', name: 'Dev User', roles: ['admin'] },
        token: null,
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['admin'],
      });

      render(
        <MemoryRouter>
          <ProtectedRoute allowedRoles={['admin']}>
            <div data-testid="protected-content">Admin Content</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      // Should render content WITHOUT authentication
      expect(screen.getByTestId('protected-content')).toBeInTheDocument();
    });

    it('should still check roles even in authBypass mode', () => {
      // Enable authBypass
      vi.mocked(featureFlags.isFeatureEnabled).mockReturnValue(true);

      mockUseAuth.mockReturnValue({
        isAuthenticated: false, // NOT authenticated in authBypass mode
        isLoading: false,
        hasRole: (role: string) => role.toUpperCase() === 'SALES', // Only SALES role
        user: { id: '1', name: 'Dev Sales User', roles: ['sales'] },
        token: null,
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['sales'],
      });

      render(
        <MemoryRouter>
          <ProtectedRoute allowedRoles={['admin']}>
            <div data-testid="protected-content">Admin Content</div>
          </ProtectedRoute>
        </MemoryRouter>
      );

      // Should show error alert (role check still active)
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
      expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
    });
  });

  describe('Nested Routes with Outlet', () => {
    it('should render Outlet for nested routes when no children provided', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role.toUpperCase() === 'ADMIN',
        user: { id: '1', name: 'Admin User', roles: ['admin'] },
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['admin'],
      });

      render(
        <MemoryRouter initialEntries={['/admin/users']}>
          <Routes>
            <Route path="/admin" element={<ProtectedRoute allowedRoles={['admin']} />}>
              <Route path="users" element={<div data-testid="nested-content">User Management</div>} />
            </Route>
          </Routes>
        </MemoryRouter>
      );

      // Should render nested route via Outlet
      expect(screen.getByTestId('nested-content')).toBeInTheDocument();
      expect(screen.getByText('User Management')).toBeInTheDocument();
    });
  });
});
