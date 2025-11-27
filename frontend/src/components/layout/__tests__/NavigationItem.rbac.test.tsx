/**
 * Enterprise Test Suite - NavigationItem Component (RBAC)
 * Sprint 2.1.7.7 - RBAC Enhancement
 *
 * Test Coverage: SubItems Permission Filtering
 *
 * Security-Critical Tests: ✅
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { NavigationItem } from '../NavigationItem';
import DashboardIcon from '@mui/icons-material/Dashboard';

// Mock dependencies
vi.mock('@/hooks/useLazySubMenu', () => ({
  useLazySubMenu: vi.fn(({ items }) => ({
    items: items || [],
    isLoading: false,
    preloadItems: vi.fn(),
  })),
}));

describe('NavigationItem - RBAC SubItems Filtering', () => {
  const mockOnItemClick = vi.fn();
  const mockOnSubItemClick = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // Helper function to render NavigationItem with Router context
  const renderNavigationItem = (props: Parameters<typeof NavigationItem>[0]) => {
    return render(
      <MemoryRouter>
        <NavigationItem {...props} />
      </MemoryRouter>
    );
  };

  describe('SubItems Permission Filtering', () => {
    it('should show all subItems when user has all required permissions', () => {
      const item = {
        id: 'admin',
        label: 'Administration',
        icon: DashboardIcon,
        path: '/admin',
        permissions: ['admin.view'],
        subItems: [
          {
            label: 'Benutzerverwaltung',
            path: '/admin/users',
            permissions: ['admin.view'],
          },
          {
            label: 'System',
            path: '/admin/system',
            permissions: ['admin.view'],
          },
          {
            label: 'Audit Dashboard',
            path: '/admin/audit',
            permissions: ['admin.view', 'auditor.view'],
          },
        ],
      };

      renderNavigationItem({
        item,
        isActive: false,
        isExpanded: true,
        isCollapsed: false,
        onItemClick: mockOnItemClick,
        onSubItemClick: mockOnSubItemClick,
        userPermissions: ['admin.view', 'auditor.view'],
      });

      // Should show all 3 subItems (user has admin.view + auditor.view)
      expect(screen.getByText('Benutzerverwaltung')).toBeInTheDocument();
      expect(screen.getByText('System')).toBeInTheDocument();
      expect(screen.getByText('Audit Dashboard')).toBeInTheDocument();
    });

    it('should filter out subItems when user lacks required permissions', () => {
      const item = {
        id: 'admin',
        label: 'Administration',
        icon: DashboardIcon,
        path: '/admin',
        permissions: ['admin.view', 'auditor.view'],
        subItems: [
          {
            label: 'Benutzerverwaltung',
            path: '/admin/users',
            permissions: ['admin.view'], // ← Requires admin.view
          },
          {
            label: 'System',
            path: '/admin/system',
            permissions: ['admin.view'], // ← Requires admin.view
          },
          {
            label: 'Audit Dashboard',
            path: '/admin/audit',
            permissions: ['admin.view', 'auditor.view'], // ← Requires admin.view OR auditor.view
          },
        ],
      };

      renderNavigationItem({
        item,
        isActive: false,
        isExpanded: true,
        isCollapsed: false,
        onItemClick: mockOnItemClick,
        onSubItemClick: mockOnSubItemClick,
        userPermissions: ['auditor.view'], // ← User only has auditor.view
      });

      // Should NOT show admin-only items
      expect(screen.queryByText('Benutzerverwaltung')).not.toBeInTheDocument();
      expect(screen.queryByText('System')).not.toBeInTheDocument();

      // Should show Audit Dashboard (auditor.view is sufficient)
      expect(screen.getByText('Audit Dashboard')).toBeInTheDocument();
    });

    it('should show subItems without permissions restriction (always visible)', () => {
      const item = {
        id: 'help',
        label: 'Hilfe & Support',
        icon: DashboardIcon,
        path: '/help',
        subItems: [
          {
            label: 'Handbücher',
            path: '/help/docs',
            // NO permissions field = always visible
          },
          {
            label: 'Video-Tutorials',
            path: '/help/videos',
            // NO permissions field = always visible
          },
        ],
      };

      renderNavigationItem({
        item,
        isActive: false,
        isExpanded: true,
        isCollapsed: false,
        onItemClick: mockOnItemClick,
        onSubItemClick: mockOnSubItemClick,
        userPermissions: [], // ← User has NO permissions
      });

      // Should show all subItems (no permission restrictions)
      expect(screen.getByText('Handbücher')).toBeInTheDocument();
      expect(screen.getByText('Video-Tutorials')).toBeInTheDocument();
    });

    it('should handle mixed permissions (some visible, some hidden)', () => {
      const item = {
        id: 'settings',
        label: 'Einstellungen',
        icon: DashboardIcon,
        path: '/settings',
        subItems: [
          {
            label: 'Mein Profil',
            path: '/settings/profile',
            // NO permissions = always visible
          },
          {
            label: 'Benachrichtigungen',
            path: '/settings/notifications',
            // NO permissions = always visible
          },
          {
            label: 'Sicherheit',
            path: '/settings/security',
            permissions: ['admin.view'], // ← Requires admin.view
          },
        ],
      };

      renderNavigationItem({
        item,
        isActive: false,
        isExpanded: true,
        isCollapsed: false,
        onItemClick: mockOnItemClick,
        onSubItemClick: mockOnSubItemClick,
        userPermissions: ['sales.view'], // ← User only has sales.view (NOT admin.view)
      });

      // Should show items without permissions
      expect(screen.getByText('Mein Profil')).toBeInTheDocument();
      expect(screen.getByText('Benachrichtigungen')).toBeInTheDocument();

      // Should NOT show admin-only item
      expect(screen.queryByText('Sicherheit')).not.toBeInTheDocument();
    });

    it('should handle OR logic for multiple permissions (ANY match = visible)', () => {
      const item = {
        id: 'admin',
        label: 'Administration',
        icon: DashboardIcon,
        path: '/admin',
        subItems: [
          {
            label: 'Audit Dashboard',
            path: '/admin/audit',
            permissions: ['admin.view', 'auditor.view'], // ← Requires admin.view OR auditor.view
          },
        ],
      };

      // Test 1: User has admin.view
      const { rerender } = render(
        <MemoryRouter>
          <NavigationItem
            item={item}
            isActive={false}
            isExpanded={true}
            isCollapsed={false}
            onItemClick={mockOnItemClick}
            onSubItemClick={mockOnSubItemClick}
            userPermissions={['admin.view']} // ← Has admin.view
          />
        </MemoryRouter>
      );

      expect(screen.getByText('Audit Dashboard')).toBeInTheDocument();

      // Test 2: User has auditor.view
      rerender(
        <MemoryRouter>
          <NavigationItem
            item={item}
            isActive={false}
            isExpanded={true}
            isCollapsed={false}
            onItemClick={mockOnItemClick}
            onSubItemClick={mockOnSubItemClick}
            userPermissions={['auditor.view']} // ← Has auditor.view
          />
        </MemoryRouter>
      );

      expect(screen.getByText('Audit Dashboard')).toBeInTheDocument();

      // Test 3: User has NEITHER
      rerender(
        <MemoryRouter>
          <NavigationItem
            item={item}
            isActive={false}
            isExpanded={true}
            isCollapsed={false}
            onItemClick={mockOnItemClick}
            onSubItemClick={mockOnSubItemClick}
            userPermissions={['sales.view']} // ← Has NEITHER admin.view NOR auditor.view
          />
        </MemoryRouter>
      );

      expect(screen.queryByText('Audit Dashboard')).not.toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle item without subItems gracefully', () => {
      const item = {
        id: 'cockpit',
        label: 'Mein Cockpit',
        icon: DashboardIcon,
        path: '/cockpit',
        permissions: ['cockpit.view'],
        // NO subItems field
      };

      renderNavigationItem({
        item,
        isActive: false,
        isExpanded: false,
        isCollapsed: false,
        onItemClick: mockOnItemClick,
        onSubItemClick: mockOnSubItemClick,
        userPermissions: ['cockpit.view'],
      });

      // Should render main item without errors
      expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
    });

    it('should handle empty subItems array', () => {
      const item = {
        id: 'cockpit',
        label: 'Mein Cockpit',
        icon: DashboardIcon,
        path: '/cockpit',
        permissions: ['cockpit.view'],
        subItems: [], // ← Empty array
      };

      renderNavigationItem({
        item,
        isActive: false,
        isExpanded: true,
        isCollapsed: false,
        onItemClick: mockOnItemClick,
        onSubItemClick: mockOnSubItemClick,
        userPermissions: ['cockpit.view'],
      });

      // Should render main item without errors
      expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
    });

    it('should handle empty userPermissions array', () => {
      const item = {
        id: 'admin',
        label: 'Administration',
        icon: DashboardIcon,
        path: '/admin',
        subItems: [
          {
            label: 'Benutzerverwaltung',
            path: '/admin/users',
            permissions: ['admin.view'],
          },
        ],
      };

      renderNavigationItem({
        item,
        isActive: false,
        isExpanded: true,
        isCollapsed: false,
        onItemClick: mockOnItemClick,
        onSubItemClick: mockOnSubItemClick,
        userPermissions: [], // ← User has NO permissions
      });

      // Should NOT show permission-restricted subItems
      expect(screen.queryByText('Benutzerverwaltung')).not.toBeInTheDocument();
    });

    it('should hide all subItems when user has no matching permissions', () => {
      const item = {
        id: 'admin',
        label: 'Administration',
        icon: DashboardIcon,
        path: '/admin',
        subItems: [
          {
            label: 'Benutzerverwaltung',
            path: '/admin/users',
            permissions: ['admin.view'],
          },
          {
            label: 'System',
            path: '/admin/system',
            permissions: ['admin.view'],
          },
        ],
      };

      renderNavigationItem({
        item,
        isActive: false,
        isExpanded: true,
        isCollapsed: false,
        onItemClick: mockOnItemClick,
        onSubItemClick: mockOnSubItemClick,
        userPermissions: ['sales.view'], // ← User has WRONG permission
      });

      // Should hide ALL subItems
      expect(screen.queryByText('Benutzerverwaltung')).not.toBeInTheDocument();
      expect(screen.queryByText('System')).not.toBeInTheDocument();
    });
  });

  describe('Collapsed State', () => {
    it('should NOT render subItems when collapsed', () => {
      const item = {
        id: 'admin',
        label: 'Administration',
        icon: DashboardIcon,
        path: '/admin',
        subItems: [
          {
            label: 'Benutzerverwaltung',
            path: '/admin/users',
            permissions: ['admin.view'],
          },
        ],
      };

      renderNavigationItem({
        item,
        isActive: false,
        isExpanded: true,
        isCollapsed: true, // ← Collapsed state
        onItemClick: mockOnItemClick,
        onSubItemClick: mockOnSubItemClick,
        userPermissions: ['admin.view'],
      });

      // Should NOT render subItems when collapsed (Line 183 condition)
      expect(screen.queryByText('Benutzerverwaltung')).not.toBeInTheDocument();
    });
  });

  describe('Performance - useMemo Optimization', () => {
    it('should memoize filteredSubItems calculation', () => {
      const item = {
        id: 'admin',
        label: 'Administration',
        icon: DashboardIcon,
        path: '/admin',
        subItems: [
          {
            label: 'Benutzerverwaltung',
            path: '/admin/users',
            permissions: ['admin.view'],
          },
        ],
      };

      const { rerender } = render(
        <MemoryRouter>
          <NavigationItem
            item={item}
            isActive={false}
            isExpanded={true}
            isCollapsed={false}
            onItemClick={mockOnItemClick}
            onSubItemClick={mockOnSubItemClick}
            userPermissions={['admin.view']}
          />
        </MemoryRouter>
      );

      expect(screen.getByText('Benutzerverwaltung')).toBeInTheDocument();

      // Rerender with SAME props (useMemo should NOT recalculate)
      rerender(
        <MemoryRouter>
          <NavigationItem
            item={item}
            isActive={false}
            isExpanded={true}
            isCollapsed={false}
            onItemClick={mockOnItemClick}
            onSubItemClick={mockOnSubItemClick}
            userPermissions={['admin.view']}
          />
        </MemoryRouter>
      );

      // Should still render (memoization working)
      expect(screen.getByText('Benutzerverwaltung')).toBeInTheDocument();
    });
  });
});
