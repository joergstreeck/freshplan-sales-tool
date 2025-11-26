/**
 * Enterprise Test Suite - App Component (RBAC)
 * Sprint 2.1.7.7 - RBAC Enhancement
 *
 * Test Coverage: Admin Card Visibility
 *
 * Security-Critical Tests: ✅
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from '../App';

// Mock dependencies
vi.mock('@/contexts/AuthContext');

import { useAuth } from '@/contexts/AuthContext';

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;

describe('App - RBAC Admin Card', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should show Admin Card for ADMIN role', () => {
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

    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>
    );

    // Should show Admin Card with title and button
    expect(screen.getByText('Benutzerverwaltung')).toBeInTheDocument();
    expect(screen.getByText('Verwalten Sie Benutzer und Zugriffsrechte')).toBeInTheDocument();
    expect(screen.getByText('Benutzerverwaltung öffnen')).toBeInTheDocument();
  });

  it('should hide Admin Card for MANAGER role', () => {
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

    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>
    );

    // Should NOT show Admin Card
    expect(screen.queryByText('Benutzerverwaltung')).not.toBeInTheDocument();
    expect(screen.queryByText('Verwalten Sie Benutzer und Zugriffsrechte')).not.toBeInTheDocument();
  });

  it('should hide Admin Card for SALES role', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Sales User', roles: ['sales'] },
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
    });

    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>
    );

    // Should NOT show Admin Card
    expect(screen.queryByText('Benutzerverwaltung')).not.toBeInTheDocument();
  });

  it('should hide Admin Card for user without roles', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'No Role User', roles: [] },
      isAuthenticated: true,
      isLoading: false,
      hasRole: (_role: string) => false,
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
        <App />
      </MemoryRouter>
    );

    // Should NOT show Admin Card
    expect(screen.queryByText('Benutzerverwaltung')).not.toBeInTheDocument();
  });

  it('should conditionally render Admin Card (not just hide via CSS)', () => {
    // Test that Card is truly removed from DOM, not just hidden
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Sales User', roles: ['sales'] },
      isAuthenticated: true,
      isLoading: false,
      hasRole: (_role: string) => false,
      token: 'mock-token',
      login: vi.fn(),
      logout: vi.fn(),
      hasAnyRole: vi.fn(),
      getValidToken: vi.fn(),
      refreshToken: vi.fn(),
      authInfo: vi.fn(),
      userRoles: ['sales'],
    });

    const { container } = render(
      <MemoryRouter>
        <App />
      </MemoryRouter>
    );

    // Check that Card is not in DOM (not just display:none)
    const adminCard = container.querySelector('[class*="card"]');
    const hasAdminContent = adminCard?.textContent?.includes('Benutzerverwaltung');

    expect(hasAdminContent).toBeFalsy();
  });
});
