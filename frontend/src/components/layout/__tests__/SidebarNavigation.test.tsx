import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { SidebarNavigation } from '../SidebarNavigation';
import { useNavigationStore } from '@/store/navigationStore';
import { useAuthStore } from '@/store/authStore';

// Mock stores
vi.mock('@/store/navigationStore');
vi.mock('@/store/authStore');

const mockUseNavigationStore = vi.mocked(useNavigationStore);
const mockUseAuthStore = vi.mocked(useAuthStore);

const theme = createTheme();

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <BrowserRouter>
    <ThemeProvider theme={theme}>{children}</ThemeProvider>
  </BrowserRouter>
);

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

const mockAuthState = {
  userPermissions: ['cockpit.view', 'customers.view'],
  setPermissions: vi.fn(),
};

describe('SidebarNavigation', () => {
  beforeEach(() => {
    mockUseNavigationStore.mockReturnValue(mockNavigationState);
    mockUseAuthStore.mockReturnValue(mockAuthState);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('sollte alle erlaubten Menüpunkte anzeigen', () => {
    render(
      <TestWrapper>
        <SidebarNavigation />
      </TestWrapper>
    );

    // Prüfe ob die erlaubten Menüpunkte angezeigt werden
    expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
    expect(screen.getByText('Kundenmanagement')).toBeInTheDocument();
  });

  it('sollte Toggle-Button zum Einklappen anzeigen', () => {
    render(
      <TestWrapper>
        <SidebarNavigation />
      </TestWrapper>
    );

    const toggleButton = screen.getByRole('button', { name: /navigation einklappen/i });
    expect(toggleButton).toBeInTheDocument();
  });

  it('sollte toggleSidebar aufrufen beim Klick auf Toggle-Button', () => {
    render(
      <TestWrapper>
        <SidebarNavigation />
      </TestWrapper>
    );

    const toggleButton = screen.getByRole('button', { name: /navigation einklappen/i });
    fireEvent.click(toggleButton);

    expect(mockNavigationState.toggleSidebar).toHaveBeenCalled();
  });

  it('sollte aktiven Menüpunkt hervorheben', () => {
    render(
      <TestWrapper>
        <SidebarNavigation />
      </TestWrapper>
    );

    const activeItem = screen.getByText('Mein Cockpit').closest('div');
    expect(activeItem?.parentElement).toHaveClass('Mui-selected');
  });

  it('sollte eingeklappte Sidebar mit Icons anzeigen', () => {
    const collapsedState = {
      ...mockNavigationState,
      isCollapsed: true,
    };
    mockUseNavigationStore.mockReturnValue(collapsedState);

    render(
      <TestWrapper>
        <SidebarNavigation />
      </TestWrapper>
    );

    // In eingeklapptem Zustand sollten die Texte nicht sichtbar sein
    expect(screen.queryByText('Mein Cockpit')).not.toBeInTheDocument();
  });

  it('sollte nur Menüpunkte mit entsprechenden Permissions anzeigen', () => {
    const limitedAuthState = {
      ...mockAuthState,
      userPermissions: ['cockpit.view'], // Nur Cockpit Permission
    };
    mockUseAuthStore.mockReturnValue(limitedAuthState);

    render(
      <TestWrapper>
        <SidebarNavigation />
      </TestWrapper>
    );

    expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
    expect(screen.queryByText('Kundenmanagement')).not.toBeInTheDocument();
  });
});
