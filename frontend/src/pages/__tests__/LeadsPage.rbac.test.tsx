/**
 * Enterprise Test Suite - LeadsPage Component (RBAC)
 * Sprint 2.1.7.7 - RBAC Enhancement
 *
 * Test Coverage: Stop-the-Clock Button Permission
 *
 * Security-Critical Tests: ✅
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import LeadsPage from '../LeadsPage';

// Mock dependencies
vi.mock('@/contexts/AuthContext');
vi.mock('@/features/leads/hooks/useLeads', () => ({
  useLeads: () => ({
    data: [
      {
        id: 1,
        companyName: 'Test Company',
        stage: 'QUALIFIED',
        estimatedVolume: 10000,
        ownerUserId: 1,
      },
    ],
    isLoading: false,
    error: null,
    refetch: vi.fn(),
  }),
}));
vi.mock('@/hooks/useEnumOptions', () => ({
  useEnumOptions: () => ({
    data: [
      { value: 'BILDUNG', label: 'Bildung' },
      { value: 'GASTRONOMIE', label: 'Gastronomie' },
    ],
  }),
}));
vi.mock('@/store/focusListStore', () => ({
  useFocusListStore: () => ({
    sortBy: null,
    setSortBy: vi.fn(),
  }),
}));

import { useAuth } from '@/contexts/AuthContext';

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;

// Helper-Funktion für Test-Wrapper mit QueryClient + Router
const renderWithProviders = (component: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        {component}
      </MemoryRouter>
    </QueryClientProvider>
  );
};

describe('LeadsPage - RBAC Stop-the-Clock Button', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should show Stop-the-Clock button for ADMIN role', () => {
    mockUseAuth.mockReturnValue({
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
    });

    renderWithProviders(<LeadsPage />);

    // Should show Stop-the-Clock button (Pause icon with "Pausieren" tooltip)
    const pauseButtons = screen.queryAllByRole('button', { name: /pausieren/i });
    expect(pauseButtons.length).toBeGreaterThan(0);
  });

  it('should show Stop-the-Clock button for MANAGER role', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Manager User', roles: ['manager'] },
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
    });

    renderWithProviders(<LeadsPage />);

    // Should show Stop-the-Clock button
    const pauseButtons = screen.queryAllByRole('button', { name: /pausieren/i });
    expect(pauseButtons.length).toBeGreaterThan(0);
  });

  it('should hide Stop-the-Clock button for SALES role', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Sales User', roles: ['sales'] },
      isAuthenticated: true,
      isLoading: false,
      hasRole: (role: string) => false, // Sales has no ADMIN/MANAGER role
      token: 'mock-token',
      login: vi.fn(),
      logout: vi.fn(),
      hasAnyRole: vi.fn(),
      getValidToken: vi.fn(),
      refreshToken: vi.fn(),
      authInfo: vi.fn(),
      userRoles: ['sales'],
    });

    renderWithProviders(<LeadsPage />);

    // Should NOT show Stop-the-Clock button
    const pauseButtons = screen.queryAllByRole('button', { name: /pausieren/i });
    expect(pauseButtons).toHaveLength(0);
  });

  it('should hide Stop-the-Clock button for user without roles', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'No Role User', roles: [] },
      isAuthenticated: true,
      isLoading: false,
      hasRole: (role: string) => false,
      token: 'mock-token',
      login: vi.fn(),
      logout: vi.fn(),
      hasAnyRole: vi.fn(),
      getValidToken: vi.fn(),
      refreshToken: vi.fn(),
      authInfo: vi.fn(),
      userRoles: [],
    });

    renderWithProviders(<LeadsPage />);

    // Should NOT show Stop-the-Clock button
    const pauseButtons = screen.queryAllByRole('button', { name: /pausieren/i });
    expect(pauseButtons).toHaveLength(0);
  });

  it('should conditionally render button element (not just disable)', () => {
    // Test: Admin user sees button in DOM (not just disabled)
    mockUseAuth.mockReturnValue({
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
    });

    renderWithProviders(<LeadsPage />);

    // Button should exist in DOM (conditional rendering, not disabled state)
    const pauseButtons = screen.queryAllByRole('button', { name: /pausieren/i });
    expect(pauseButtons.length).toBeGreaterThan(0);

    // Verify button is not disabled (it's conditionally rendered, not toggled via disabled prop)
    expect(pauseButtons[0]).not.toBeDisabled();
  });
});
