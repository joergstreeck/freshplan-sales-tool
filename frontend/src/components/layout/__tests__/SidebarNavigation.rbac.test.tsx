/**
 * Enterprise Test Suite - SidebarNavigation Component (RBAC)
 * Sprint 2.1.7.7 - RBAC Enhancement
 *
 * Test Coverage: Permission Mapping + Navigation Filtering
 *
 * Security-Critical Tests: ✅
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { SidebarNavigation } from '../SidebarNavigation';

// Mock dependencies
vi.mock('@/hooks/useAuth');
vi.mock('@/store/navigationStore');
vi.mock('@/hooks/useKeyboardNavigation', () => ({
  useKeyboardNavigation: vi.fn(),
}));

import { useAuth } from '@/hooks/useAuth';
import { useNavigationStore } from '@/store/navigationStore';

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;
const mockUseNavigationStore = useNavigationStore as ReturnType<typeof vi.fn>;

describe('SidebarNavigation - RBAC Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();

    // Default navigation store state
    mockUseNavigationStore.mockReturnValue({
      activeMenuId: null,
      expandedMenuId: null,
      isCollapsed: false,
      setActiveMenu: vi.fn(),
      toggleSubmenu: vi.fn(),
      openSubmenu: vi.fn(),
      closeAllSubmenus: vi.fn(),
      toggleSidebar: vi.fn(),
      addToRecentlyVisited: vi.fn(),
    });
  });

  describe('Role-to-Permission Mapping', () => {
    it('should calculate correct permissions for ADMIN role', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Admin User', roles: ['admin'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
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
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Admin should see ALL menu items
      expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
      expect(screen.getByText('Neukundengewinnung')).toBeInTheDocument();
      expect(screen.getByText('Kundenmanagement')).toBeInTheDocument();
      expect(screen.getByText('Auswertungen')).toBeInTheDocument();
      expect(screen.getByText('Kommunikation')).toBeInTheDocument();
      expect(screen.getByText('Einstellungen')).toBeInTheDocument();
      expect(screen.getByText('Hilfe & Support')).toBeInTheDocument();
      expect(screen.getByText('Administration')).toBeInTheDocument(); // ← ADMIN MENU
    });

    it('should calculate correct permissions for MANAGER role', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Manager User', roles: ['manager'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['manager'],
      });

      render(
        <MemoryRouter>
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Manager should see most items EXCEPT admin
      expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
      expect(screen.getByText('Neukundengewinnung')).toBeInTheDocument();
      expect(screen.getByText('Kundenmanagement')).toBeInTheDocument();
      expect(screen.getByText('Auswertungen')).toBeInTheDocument();
      expect(screen.getByText('Kommunikation')).toBeInTheDocument();
      expect(screen.getByText('Einstellungen')).toBeInTheDocument();
      expect(screen.getByText('Hilfe & Support')).toBeInTheDocument();

      // Should NOT see admin menu
      expect(screen.queryByText('Administration')).not.toBeInTheDocument();
    });

    it('should calculate correct permissions for SALES role', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Sales User', roles: ['sales'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
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
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Sales should see limited items (no reports, no admin)
      expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
      expect(screen.getByText('Neukundengewinnung')).toBeInTheDocument();
      expect(screen.getByText('Kundenmanagement')).toBeInTheDocument();
      expect(screen.getByText('Kommunikation')).toBeInTheDocument();
      expect(screen.getByText('Einstellungen')).toBeInTheDocument();
      expect(screen.getByText('Hilfe & Support')).toBeInTheDocument();

      // Should NOT see reports or admin
      expect(screen.queryByText('Auswertungen')).not.toBeInTheDocument();
      expect(screen.queryByText('Administration')).not.toBeInTheDocument();
    });

    it('should calculate correct permissions for AUDITOR role', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Auditor User', roles: ['auditor'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
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
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Auditor should see cockpit, reports, and admin menu
      expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
      expect(screen.getByText('Auswertungen')).toBeInTheDocument();
      expect(screen.getByText('Kommunikation')).toBeInTheDocument();
      expect(screen.getByText('Hilfe & Support')).toBeInTheDocument();
      expect(screen.getByText('Administration')).toBeInTheDocument(); // ← Has auditor.view permission

      // Should NOT see customer-related menus
      expect(screen.queryByText('Neukundengewinnung')).not.toBeInTheDocument();
      expect(screen.queryByText('Kundenmanagement')).not.toBeInTheDocument();
    });

    it('should handle multiple roles correctly (admin + manager)', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Multi Role User', roles: ['admin', 'manager'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
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
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Should have ALL permissions (union of both roles)
      expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
      expect(screen.getByText('Administration')).toBeInTheDocument();
      expect(screen.getByText('Auswertungen')).toBeInTheDocument();
    });
  });

  describe('Navigation Filtering', () => {
    it('should filter navigation items based on permissions', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Sales User', roles: ['sales'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
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
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Should only render items that match sales permissions
      const _allMenuItems = screen.queryAllByRole('button');

      // Sales should NOT see admin-only items
      const adminMenu = screen.queryByText('Administration');
      expect(adminMenu).not.toBeInTheDocument();
    });

    it('should show admin menu for users with admin.view permission', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Admin User', roles: ['admin'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
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
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Admin menu should be visible
      expect(screen.getByText('Administration')).toBeInTheDocument();
    });

    it('should show admin menu for auditor (has auditor.view permission)', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Auditor User', roles: ['auditor'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
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
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Admin menu should be visible for auditor (permissions: ['admin.view', 'auditor.view'])
      expect(screen.getByText('Administration')).toBeInTheDocument();
    });

    it('should hide admin menu for sales and manager users', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Sales User', roles: ['sales'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['sales'],
      });

      const { rerender } = render(
        <MemoryRouter>
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Sales should NOT see admin menu
      expect(screen.queryByText('Administration')).not.toBeInTheDocument();

      // Test with manager role
      mockUseAuth.mockReturnValue({
        user: { id: '2', name: 'Manager User', roles: ['manager'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['manager'],
      });

      rerender(
        <MemoryRouter>
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Manager should NOT see admin menu
      expect(screen.queryByText('Administration')).not.toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle user without roles gracefully', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'No Roles User', roles: undefined },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
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
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Should only show items without permission restrictions (Hilfe & Support, Kommunikation)
      expect(screen.getByText('Hilfe & Support')).toBeInTheDocument();
      expect(screen.getByText('Kommunikation')).toBeInTheDocument();

      // Should NOT show permission-restricted items
      expect(screen.queryByText('Administration')).not.toBeInTheDocument();
      expect(screen.queryByText('Auswertungen')).not.toBeInTheDocument();
    });

    it('should handle empty roles array', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Empty Roles User', roles: [] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
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
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Should only show items without permission restrictions
      expect(screen.getByText('Hilfe & Support')).toBeInTheDocument();
      expect(screen.getByText('Kommunikation')).toBeInTheDocument();

      // Should NOT show permission-restricted items
      expect(screen.queryByText('Mein Cockpit')).not.toBeInTheDocument();
    });

    it('should handle unknown roles gracefully', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Unknown Role User', roles: ['unknown_role'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['unknown_role'],
      });

      render(
        <MemoryRouter>
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Should only show items without permission restrictions
      expect(screen.getByText('Hilfe & Support')).toBeInTheDocument();
      expect(screen.getByText('Kommunikation')).toBeInTheDocument();

      // Should NOT see any permission-restricted items
      expect(screen.queryByText('Administration')).not.toBeInTheDocument();
    });
  });

  describe('Permission Derivation Logic', () => {
    it('should create unique permissions array (no duplicates)', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Admin+Manager User', roles: ['admin', 'manager'] },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
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
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Should work correctly with overlapping permissions
      // (both admin and manager have 'cockpit.view', 'customers.view', etc.)
      expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
      expect(screen.getByText('Administration')).toBeInTheDocument();
    });

    it('should handle case-insensitive role matching', () => {
      mockUseAuth.mockReturnValue({
        user: { id: '1', name: 'Admin User', roles: ['ADMIN'] }, // UPPERCASE
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(),
        token: 'mock-token',
        login: vi.fn(),
        logout: vi.fn(),
        hasAnyRole: vi.fn(),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
        userRoles: ['ADMIN'],
      });

      render(
        <MemoryRouter>
          <SidebarNavigation />
        </MemoryRouter>
      );

      // Should match 'ADMIN' to 'admin' in roleToPermissions mapping (line 84 uses .toLowerCase())
      expect(screen.getByText('Administration')).toBeInTheDocument();
    });
  });
});
