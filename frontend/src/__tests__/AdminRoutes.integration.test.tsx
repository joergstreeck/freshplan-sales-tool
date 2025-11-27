/**
 * Enterprise Integration Test Suite - Admin Routes
 * Sprint 2.1.7.7 - RBAC Enhancement
 *
 * Test Coverage: Admin Route Protection across multiple roles
 *
 * Integration-Critical Tests: ✅
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProtectedRoute } from '@/components/auth/ProtectedRoute';

// Mock dependencies
vi.mock('@/hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));
vi.mock('@/config/featureFlags');
vi.mock('@/components/layout/MainLayoutV2', () => ({
  MainLayoutV2: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

import { useAuth } from '@/hooks/useAuth';
import * as featureFlags from '@/config/featureFlags';

const mockUseAuth = vi.mocked(useAuth);

// Mock Admin Pages
const AdminDashboardMock = () => <div data-testid="admin-dashboard">Admin Dashboard</div>;
const SystemDashboardMock = () => <div data-testid="system-dashboard">System Dashboard</div>;
const UserManagementMock = () => <div data-testid="user-management">User Management</div>;
const AuditDashboardMock = () => <div data-testid="audit-dashboard">Audit Dashboard</div>;
const UnauthorizedPage = () => <div data-testid="unauthorized">Keine Berechtigung</div>;

// Helper function to render routes with QueryClient
const renderWithRoutes = (initialRoute: string, userConfig: ReturnType<typeof useAuth>) => {
  mockUseAuth.mockReturnValue(userConfig);
  vi.mocked(featureFlags.isFeatureEnabled).mockReturnValue(false); // Disable authBypass

  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[initialRoute]}>
        <Routes>
          {/* Admin Routes - Protected */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AdminDashboardMock />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/system"
            element={
              <ProtectedRoute allowedRoles={['admin']}>
                <SystemDashboardMock />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/users"
            element={
              <ProtectedRoute allowedRoles={['admin']}>
                <UserManagementMock />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/audit"
            element={
              <ProtectedRoute allowedRoles={['admin', 'auditor']}>
                <AuditDashboardMock />
              </ProtectedRoute>
            }
          />

          {/* Unauthorized fallback */}
          <Route path="/unauthorized" element={<UnauthorizedPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  );
};

describe('Admin Routes - Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('ADMIN Role - Full Access', () => {
    const adminUser = {
      user: { id: '1', name: 'Admin User', roles: ['admin'] },
      isAuthenticated: true,
      isLoading: false,
      hasRole: (role: string) => role.toUpperCase() === 'ADMIN',
      token: 'mock-token',
      login: vi.fn(),
      logout: vi.fn(),
      hasAnyRole: vi.fn(),
      getValidToken: vi.fn(),
      refreshToken: vi.fn(),
      authInfo: vi.fn(),
      userRoles: ['admin'],
    };

    it('should allow access to /admin dashboard', () => {
      renderWithRoutes('/admin', adminUser);

      expect(screen.getByTestId('admin-dashboard')).toBeInTheDocument();
      expect(screen.getByText('Admin Dashboard')).toBeInTheDocument();
    });

    it('should allow access to /admin/system', () => {
      renderWithRoutes('/admin/system', adminUser);

      expect(screen.getByTestId('system-dashboard')).toBeInTheDocument();
      expect(screen.getByText('System Dashboard')).toBeInTheDocument();
    });

    it('should allow access to /admin/users', () => {
      renderWithRoutes('/admin/users', adminUser);

      expect(screen.getByTestId('user-management')).toBeInTheDocument();
      expect(screen.getByText('User Management')).toBeInTheDocument();
    });

    it('should allow access to /admin/audit', () => {
      renderWithRoutes('/admin/audit', adminUser);

      expect(screen.getByTestId('audit-dashboard')).toBeInTheDocument();
      expect(screen.getByText('Audit Dashboard')).toBeInTheDocument();
    });
  });

  describe('AUDITOR Role - Limited Access', () => {
    const auditorUser = {
      user: { id: '2', name: 'Auditor User', roles: ['auditor'] },
      isAuthenticated: true,
      isLoading: false,
      hasRole: (role: string) => role.toUpperCase() === 'AUDITOR',
      token: 'mock-token',
      login: vi.fn(),
      logout: vi.fn(),
      hasAnyRole: vi.fn(),
      getValidToken: vi.fn(),
      refreshToken: vi.fn(),
      authInfo: vi.fn(),
      userRoles: ['auditor'],
    };

    it('should DENY access to /admin dashboard', () => {
      renderWithRoutes('/admin', auditorUser);

      expect(screen.queryByTestId('admin-dashboard')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });

    it('should DENY access to /admin/system', () => {
      renderWithRoutes('/admin/system', auditorUser);

      expect(screen.queryByTestId('system-dashboard')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });

    it('should DENY access to /admin/users', () => {
      renderWithRoutes('/admin/users', auditorUser);

      expect(screen.queryByTestId('user-management')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });

    it('should ALLOW access to /admin/audit (auditor has access)', () => {
      renderWithRoutes('/admin/audit', auditorUser);

      expect(screen.getByTestId('audit-dashboard')).toBeInTheDocument();
      expect(screen.getByText('Audit Dashboard')).toBeInTheDocument();
    });
  });

  describe('MANAGER Role - No Admin Access', () => {
    const managerUser = {
      user: { id: '3', name: 'Manager User', roles: ['manager'] },
      isAuthenticated: true,
      isLoading: false,
      hasRole: (role: string) => role.toUpperCase() === 'MANAGER',
      token: 'mock-token',
      login: vi.fn(),
      logout: vi.fn(),
      hasAnyRole: vi.fn(),
      getValidToken: vi.fn(),
      refreshToken: vi.fn(),
      authInfo: vi.fn(),
      userRoles: ['manager'],
    };

    it('should DENY access to /admin dashboard', () => {
      renderWithRoutes('/admin', managerUser);

      expect(screen.queryByTestId('admin-dashboard')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });

    it('should DENY access to /admin/system', () => {
      renderWithRoutes('/admin/system', managerUser);

      expect(screen.queryByTestId('system-dashboard')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });

    it('should DENY access to /admin/users', () => {
      renderWithRoutes('/admin/users', managerUser);

      expect(screen.queryByTestId('user-management')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });

    it('should DENY access to /admin/audit', () => {
      renderWithRoutes('/admin/audit', managerUser);

      expect(screen.queryByTestId('audit-dashboard')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });
  });

  describe('SALES Role - No Admin Access', () => {
    const salesUser = {
      user: { id: '4', name: 'Sales User', roles: ['sales'] },
      isAuthenticated: true,
      isLoading: false,
      hasRole: (role: string) => role.toUpperCase() === 'SALES',
      token: 'mock-token',
      login: vi.fn(),
      logout: vi.fn(),
      hasAnyRole: vi.fn(),
      getValidToken: vi.fn(),
      refreshToken: vi.fn(),
      authInfo: vi.fn(),
      userRoles: ['sales'],
    };

    it('should DENY access to /admin dashboard', () => {
      renderWithRoutes('/admin', salesUser);

      expect(screen.queryByTestId('admin-dashboard')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });

    it('should DENY access to /admin/system', () => {
      renderWithRoutes('/admin/system', salesUser);

      expect(screen.queryByTestId('system-dashboard')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });

    it('should DENY access to /admin/users', () => {
      renderWithRoutes('/admin/users', salesUser);

      expect(screen.queryByTestId('user-management')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });

    it('should DENY access to /admin/audit', () => {
      renderWithRoutes('/admin/audit', salesUser);

      expect(screen.queryByTestId('audit-dashboard')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle unauthenticated user attempting admin routes', () => {
      const unauthenticatedUser = {
        user: null,
        isAuthenticated: false,
        isLoading: false,
        hasRole: vi.fn(() => false),
        token: null,
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: [],
      };

      renderWithRoutes('/admin', unauthenticatedUser);

      // Should show loading or redirect (depends on implementation)
      // For now, check that admin content is NOT shown
      expect(screen.queryByTestId('admin-dashboard')).not.toBeInTheDocument();
    });

    it('should handle user with no roles attempting admin routes', () => {
      const noRoleUser = {
        user: { id: '5', name: 'No Role User', roles: [] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => false),
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: [],
      };

      renderWithRoutes('/admin', noRoleUser);

      expect(screen.queryByTestId('admin-dashboard')).not.toBeInTheDocument();
      expect(screen.getByText(/Sie haben keine Berechtigung/i)).toBeInTheDocument();
    });
  });

  describe('Multi-Role Access (OR Logic)', () => {
    it('should allow access to /admin/audit for ADMIN OR AUDITOR', () => {
      // Test with ADMIN
      const adminUser = {
        user: { id: '1', name: 'Admin User', roles: ['admin'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role.toUpperCase() === 'ADMIN',
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['admin'],
      };

      const { unmount } = renderWithRoutes('/admin/audit', adminUser);
      expect(screen.getByTestId('audit-dashboard')).toBeInTheDocument();
      unmount();

      // Test with AUDITOR
      const auditorUser = {
        user: { id: '2', name: 'Auditor User', roles: ['auditor'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role.toUpperCase() === 'AUDITOR',
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['auditor'],
      };

      renderWithRoutes('/admin/audit', auditorUser);
      expect(screen.getByTestId('audit-dashboard')).toBeInTheDocument();
    });
  });
});
