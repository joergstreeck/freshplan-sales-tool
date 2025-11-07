import React from 'react';
import { render, screen, fireEvent } from '../../../test/test-utils';
import { describe, it, expect, vi } from 'vitest';
import { SidebarNavigation } from '../SidebarNavigation';
import { useNavigationStore } from '@/store/navigationStore';
import { useAuth } from '@/hooks/useAuth';

// Mock stores and hooks
vi.mock('@/store/navigationStore');
vi.mock('@/hooks/useAuth');

const mockUseNavigationStore = vi.mocked(useNavigationStore);
const mockUseAuth = vi.mocked(useAuth);

const mockNavigationState = {
  activeMenuId: 'cockpit',
  expandedMenuId: null,
  isCollapsed: false,
  recentlyVisited: [],
  favorites: [],
  setActiveMenu: vi.fn(),
  toggleSubmenu: vi.fn(),
  toggleSidebar: vi.fn(),
  addToRecentlyVisited: vi.fn(),
  clearRecentlyVisited: vi.fn(),
  toggleFavorite: vi.fn(),
};

const mockUser = {
  id: 'test-user',
  name: 'Test User',
  email: 'test@freshplan.de',
  username: 'testuser',
  roles: ['admin', 'sales'], // Default: both permissions
};

describe('SidebarNavigation', () => {
  beforeEach(() => {
    mockUseNavigationStore.mockReturnValue(mockNavigationState);
    mockUseAuth.mockReturnValue({
      user: mockUser,
      isAuthenticated: true,
      login: vi.fn(),
      logout: vi.fn(),
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('sollte alle erlaubten Menüpunkte anzeigen', () => {
    render(<SidebarNavigation />);

    // Prüfe ob die erlaubten Menüpunkte angezeigt werden
    expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
    expect(screen.getByText('Kundenmanagement')).toBeInTheDocument();
  });

  it('sollte Toggle-Button zum Einklappen anzeigen', () => {
    render(<SidebarNavigation />);

    const toggleButton = screen.getByRole('button', { name: /navigation einklappen/i });
    expect(toggleButton).toBeInTheDocument();
  });

  it('sollte toggleSidebar aufrufen beim Klick auf Toggle-Button', () => {
    render(<SidebarNavigation />);

    const toggleButton = screen.getByRole('button', { name: /navigation einklappen/i });
    fireEvent.click(toggleButton);

    expect(mockNavigationState.toggleSidebar).toHaveBeenCalled();
  });

  it('sollte aktiven Menüpunkt hervorheben', () => {
    render(<SidebarNavigation />);

    const activeItem = screen.getByText('Mein Cockpit').closest('div');
    expect(activeItem?.parentElement).toHaveClass('Mui-selected');
  });

  it('sollte eingeklappte Sidebar mit Icons anzeigen', () => {
    const collapsedState = {
      ...mockNavigationState,
      isCollapsed: true,
    };
    mockUseNavigationStore.mockReturnValue(collapsedState);

    render(<SidebarNavigation />);

    // In eingeklapptem Zustand sollten die Texte nicht sichtbar sein
    expect(screen.queryByText('Mein Cockpit')).not.toBeInTheDocument();
  });

  it('sollte nur Menüpunkte mit entsprechenden Permissions anzeigen', () => {
    // Override user with limited permissions (only 'auditor' role = only cockpit.view)
    const limitedUser = {
      ...mockUser,
      roles: ['auditor'], // Only auditor role = cockpit.view + reports.view
    };
    mockUseAuth.mockReturnValue({
      user: limitedUser,
      isAuthenticated: true,
      login: vi.fn(),
      logout: vi.fn(),
    });

    render(<SidebarNavigation />);

    expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
    expect(screen.queryByText('Kundenmanagement')).not.toBeInTheDocument();
  });
});
