import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { SidebarNavigation } from './SidebarNavigation';
import { useNavigationStore } from '@/store/navigationStore';
import { useAuthStore } from '@/store/authStore';
import '@testing-library/jest-dom';

// Mock stores
vi.mock('@/store/navigationStore');
vi.mock('@/store/authStore');

// Mock router
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('SidebarNavigation', () => {
  const mockNavigationStore = {
    activeMenuId: 'cockpit',
    expandedMenuId: null,
    isCollapsed: false,
    setActiveMenu: vi.fn(),
    toggleSubmenu: vi.fn(),
    closeAllSubmenus: vi.fn(),
    toggleSidebar: vi.fn(),
    addToRecentlyVisited: vi.fn(),
  };

  const mockAuthStore = {
    userPermissions: ['cockpit.view', 'customers.create', 'customers.view', 'reports.view', 'settings.view'],
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (useNavigationStore as unknown as ReturnType<typeof vi.fn>).mockReturnValue(mockNavigationStore);
    (useAuthStore as unknown as ReturnType<typeof vi.fn>).mockReturnValue(mockAuthStore);
  });

  it('renders navigation items', () => {
    render(
      <MemoryRouter>
        <SidebarNavigation />
      </MemoryRouter>
    );
    
    expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
    expect(screen.getByText('Neukundengewinnung')).toBeInTheDocument();
    expect(screen.getByText('Kundenmanagement')).toBeInTheDocument();
    expect(screen.getByText('Auswertungen')).toBeInTheDocument();
    expect(screen.getByText('Einstellungen')).toBeInTheDocument();
  });

  it('highlights active menu item', () => {
    render(
      <MemoryRouter initialEntries={['/cockpit']}>
        <SidebarNavigation />
      </MemoryRouter>
    );
    
    // Check if cockpit is highlighted (has selected class)
    const cockpitButton = screen.getByText('Mein Cockpit').closest('div[role="button"]');
    expect(cockpitButton).toHaveClass('Mui-selected');
  });

  it('expands submenu when parent is clicked', () => {
    render(
      <MemoryRouter>
        <SidebarNavigation />
      </MemoryRouter>
    );
    
    const kundenmanagement = screen.getByText('Kundenmanagement');
    fireEvent.click(kundenmanagement);
    
    expect(mockNavigationStore.toggleSubmenu).toHaveBeenCalledWith('kundenmanagement');
  });

  it('navigates to page when menu item without submenu is clicked', () => {
    render(
      <MemoryRouter>
        <SidebarNavigation />
      </MemoryRouter>
    );
    
    const cockpit = screen.getByText('Mein Cockpit');
    fireEvent.click(cockpit);
    
    expect(mockNavigate).toHaveBeenCalledWith('/cockpit');
    expect(mockNavigationStore.setActiveMenu).toHaveBeenCalled();
  });

  it('closes all submenus when clicking item without submenu', () => {
    render(
      <MemoryRouter>
        <SidebarNavigation />
      </MemoryRouter>
    );
    
    const einstellungen = screen.getByText('Einstellungen');
    fireEvent.click(einstellungen);
    
    expect(mockNavigationStore.closeAllSubmenus).toHaveBeenCalled();
  });

  it('toggles sidebar collapse state', () => {
    render(
      <MemoryRouter>
        <SidebarNavigation />
      </MemoryRouter>
    );
    
    const toggleButton = screen.getByLabelText('Navigation einklappen');
    fireEvent.click(toggleButton);
    
    expect(mockNavigationStore.toggleSidebar).toHaveBeenCalled();
  });

  it('shows collapsed state correctly', () => {
    mockNavigationStore.isCollapsed = true;
    
    render(
      <MemoryRouter>
        <SidebarNavigation />
      </MemoryRouter>
    );
    
    // Logo and text should be hidden
    expect(screen.queryByText('FreshPlan')).not.toBeInTheDocument();
    
    // Toggle button should show expand icon
    expect(screen.getByLabelText('Navigation erweitern')).toBeInTheDocument();
  });

  it('tracks visited pages', () => {
    render(
      <MemoryRouter initialEntries={['/kundenmanagement/opportunities']}>
        <SidebarNavigation />
      </MemoryRouter>
    );
    
    expect(mockNavigationStore.addToRecentlyVisited).toHaveBeenCalledWith('/kundenmanagement/opportunities');
  });

  it.todo('auto-expands submenu when on sub-page');

  it('filters navigation items based on permissions', () => {
    // Reset store with different permissions and ensure sidebar is not collapsed
    (useNavigationStore as unknown as ReturnType<typeof vi.fn>).mockReturnValue({
      ...mockNavigationStore,
      isCollapsed: false,
    });
    (useAuthStore as unknown as ReturnType<typeof vi.fn>).mockReturnValue({
      userPermissions: ['cockpit.view'], // Only cockpit permission
    });
    
    render(
      <MemoryRouter>
        <SidebarNavigation />
      </MemoryRouter>
    );
    
    // Should only see cockpit, not other items
    expect(screen.getByText('Mein Cockpit')).toBeInTheDocument();
    expect(screen.queryByText('Neukundengewinnung')).not.toBeInTheDocument();
    expect(screen.queryByText('Kundenmanagement')).not.toBeInTheDocument();
    expect(screen.queryByText('Auswertungen')).not.toBeInTheDocument();
    expect(screen.queryByText('Einstellungen')).not.toBeInTheDocument();
  });
});