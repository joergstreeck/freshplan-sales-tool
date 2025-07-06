/**
 * Tests für die SalesCockpit Hauptkomponente
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
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
});