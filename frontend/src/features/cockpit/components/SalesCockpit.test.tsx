/**
 * Tests für die SalesCockpit Hauptkomponente
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { SalesCockpit } from './SalesCockpit';
import { useCockpitStore } from '../../../store/cockpitStore';

// Mock für die Child-Komponenten
vi.mock('./CockpitHeader', () => ({
  CockpitHeader: () => <div data-testid="cockpit-header">Header</div>
}));

vi.mock('./MyDayColumn', () => ({
  MyDayColumn: () => <div data-testid="my-day-column">My Day</div>
}));

vi.mock('./FocusListColumn', () => ({
  FocusListColumn: () => <div data-testid="focus-list-column">Focus List</div>
}));

vi.mock('./ActionCenterColumn', () => ({
  ActionCenterColumn: () => <div data-testid="action-center-column">Action Center</div>
}));

vi.mock('./DashboardStats', () => ({
  DashboardStats: ({ statistics }: any) => (
    <div data-testid="dashboard-stats">
      <span>Total Revenue: {statistics?.totalRevenue}</span>
      <span>Active Deals: {statistics?.activeDeals}</span>
    </div>
  )
}));

// Mock für useDashboardData hook
vi.mock('../hooks/useSalesCockpit', () => ({
  useDashboardData: vi.fn(() => ({
    data: {
      statistics: {
        totalRevenue: 250000,
        activeDeals: 15,
        newLeads: 8
      },
      todaysTasks: [],
      alerts: []
    },
    isLoading: false,
    isError: false,
    error: null
  }))
}));

// Mock für useAuth hook
vi.mock('../../../hooks/useAuth', () => ({
  useAuth: () => ({ userId: 'test-user-123' })
}));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
  },
});

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        {component}
      </BrowserRouter>
    </QueryClientProvider>
  );
};

describe('SalesCockpit', () => {
  beforeEach(() => {
    // Reset store
    const { setState } = useCockpitStore;
    setState({
      activeColumn: 'focus-list',
      isMobileMenuOpen: false,
      isCompactMode: false
    });
  });

  it('sollte alle drei Spalten rendern', () => {
    renderWithProviders(<SalesCockpit />);
    
    expect(screen.getByTestId('cockpit-header')).toBeInTheDocument();
    expect(screen.getByTestId('my-day-column')).toBeInTheDocument();
    expect(screen.getByTestId('focus-list-column')).toBeInTheDocument();
    expect(screen.getByTestId('action-center-column')).toBeInTheDocument();
  });

  it('sollte die aktive Spalte hervorheben', () => {
    const { container } = renderWithProviders(<SalesCockpit />);
    
    const focusListColumn = container.querySelector('.column-focus-list');
    expect(focusListColumn).toHaveClass('active');
  });

  it('sollte auf Spalten-Klicks reagieren', () => {
    const { container } = renderWithProviders(<SalesCockpit />);
    const { setActiveColumn } = useCockpitStore.getState();
    
    const myDayColumn = container.querySelector('.column-my-day');
    fireEvent.click(myDayColumn!);
    
    expect(setActiveColumn).toBeDefined();
  });

  it('sollte Keyboard Navigation unterstützen', () => {
    renderWithProviders(<SalesCockpit />);
    
    // Alt + 1
    fireEvent.keyDown(document, { key: '1', altKey: true });
    expect(useCockpitStore.getState().activeColumn).toBe('my-day');
    
    // Alt + 2
    fireEvent.keyDown(document, { key: '2', altKey: true });
    expect(useCockpitStore.getState().activeColumn).toBe('focus-list');
    
    // Alt + 3
    fireEvent.keyDown(document, { key: '3', altKey: true });
    expect(useCockpitStore.getState().activeColumn).toBe('action-center');
  });

  it('sollte mobile menu class anwenden', () => {
    const { setState } = useCockpitStore;
    setState({ isMobileMenuOpen: true });
    
    const { container } = renderWithProviders(<SalesCockpit />);
    const columns = container.querySelector('.cockpit-columns');
    
    expect(columns).toHaveClass('mobile-menu-open');
  });

  it('sollte compact mode class anwenden', () => {
    const { setState } = useCockpitStore;
    setState({ isCompactMode: true });
    
    const { container } = renderWithProviders(<SalesCockpit />);
    const cockpit = container.querySelector('.sales-cockpit');
    
    expect(cockpit).toHaveClass('compact-mode');
  });

  it('sollte cleanup bei unmount durchführen', () => {
    const { unmount } = renderWithProviders(<SalesCockpit />);
    
    const removeEventListenerSpy = vi.spyOn(window, 'removeEventListener');
    
    unmount();
    
    expect(removeEventListenerSpy).toHaveBeenCalledWith('keydown', expect.any(Function));
  });

  it('sollte Dashboard-Statistiken anzeigen wenn Daten vorhanden sind', () => {
    renderWithProviders(<SalesCockpit />);
    
    const stats = screen.getByTestId('dashboard-stats');
    expect(stats).toBeInTheDocument();
    expect(stats).toHaveTextContent('Total Revenue: 250000');
    expect(stats).toHaveTextContent('Active Deals: 15');
  });

  it('sollte mobile navigation hints anzeigen', () => {
    renderWithProviders(<SalesCockpit />);
    
    expect(screen.getByText('Alt+1: Mein Tag')).toBeInTheDocument();
    expect(screen.getByText('Alt+2: Fokus-Liste')).toBeInTheDocument();
    expect(screen.getByText('Alt+3: Aktions-Center')).toBeInTheDocument();
  });

  it('sollte mit loading state umgehen können', () => {
    const { useDashboardData } = await import('../hooks/useSalesCockpit');
    vi.mocked(useDashboardData).mockReturnValueOnce({
      data: null,
      isLoading: true,
      isError: false,
      error: null,
      refetch: vi.fn()
    } as any);

    renderWithProviders(<SalesCockpit />);
    
    // Dashboard stats sollten nicht angezeigt werden während loading
    expect(screen.queryByTestId('dashboard-stats')).not.toBeInTheDocument();
  });

  it('sollte mit error state umgehen können', () => {
    const { useDashboardData } = await import('../hooks/useSalesCockpit');
    vi.mocked(useDashboardData).mockReturnValueOnce({
      data: null,
      isLoading: false,
      isError: true,
      error: new Error('Failed to load dashboard data'),
      refetch: vi.fn()
    } as any);

    renderWithProviders(<SalesCockpit />);
    
    // Dashboard stats sollten nicht angezeigt werden bei error
    expect(screen.queryByTestId('dashboard-stats')).not.toBeInTheDocument();
  });

  it('sollte die 3-Spalten-Struktur korrekt implementieren', () => {
    const { container } = renderWithProviders(<SalesCockpit />);
    
    const columns = container.querySelectorAll('.cockpit-column');
    expect(columns).toHaveLength(3);
    
    // Überprüfe Klassen für jede Spalte
    expect(columns[0]).toHaveClass('column-my-day');
    expect(columns[1]).toHaveClass('column-focus-list');
    expect(columns[2]).toHaveClass('column-action-center');
  });

  it('sollte responsive auf verschiedene Bildschirmgrößen reagieren', () => {
    // Simuliere mobile Ansicht
    Object.defineProperty(window, 'innerWidth', { writable: true, value: 375 });
    
    const { container } = renderWithProviders(<SalesCockpit />);
    
    // In mobiler Ansicht sollte nur die aktive Spalte sichtbar sein
    const activeColumn = container.querySelector('.cockpit-column.active');
    expect(activeColumn).toBeInTheDocument();
  });
});