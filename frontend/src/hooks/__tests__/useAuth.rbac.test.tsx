/**
 * Enterprise Test Suite - useAuth Hook (RBAC)
 * Sprint 2.1.7.7 - Auth-Bypass & RBAC Enhancement
 *
 * Test Coverage: hasRole case-sensitivity + Role-based Permissions
 *
 * Security-Critical Tests: ✅
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useAuth } from '../useAuth';
import { AuthContext } from '../../contexts/AuthContext';
import { KeycloakContext } from '../../contexts/KeycloakContext';
import React from 'react';

// Mock environment variables
const originalEnv = import.meta.env;

describe('useAuth Hook - RBAC Tests', () => {
  beforeEach(() => {
    // Reset mocks
    vi.clearAllMocks();
    // Mock environment
    import.meta.env = { ...originalEnv, VITE_AUTH_BYPASS: 'true', DEV: true };
  });

  afterEach(() => {
    import.meta.env = originalEnv;
  });

  describe('hasRole - Case Sensitivity Fix', () => {
    it('should handle uppercase role checks (ADMIN) for lowercase user roles (admin)', () => {
      const mockUser = {
        id: 'user-1',
        name: 'Admin User',
        email: 'admin@test.com',
        roles: ['admin'], // lowercase
      };

      const mockAuthContext = {
        user: mockUser,
        isAuthenticated: true,
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        token: 'mock-token',
        hasRole: (role: string) => mockUser.roles?.includes(role.toLowerCase()) ?? false,
        hasAnyRole: (roles: string[]) =>
          roles.some(role => mockUser.roles?.includes(role.toLowerCase()) ?? false),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
      };

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <KeycloakContext.Provider
          value={{
            isAuthenticated: false,
            isLoading: false,
            token: null,
            userId: null,
            username: null,
            email: null,
            userRoles: [],
            hasRole: vi.fn(),
            login: vi.fn(),
            logout: vi.fn(),
          }}
        >
          <AuthContext.Provider value={mockAuthContext}>{children}</AuthContext.Provider>
        </KeycloakContext.Provider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      // Test case-insensitive role check
      expect(result.current.hasRole('ADMIN')).toBe(true); // UPPERCASE
      expect(result.current.hasRole('admin')).toBe(true); // lowercase
      expect(result.current.hasRole('Admin')).toBe(true); // Mixed case
    });

    it('should return false for roles user does not have (case-insensitive)', () => {
      const mockUser = {
        id: 'user-1',
        name: 'Sales User',
        email: 'sales@test.com',
        roles: ['sales'],
      };

      const mockAuthContext = {
        user: mockUser,
        isAuthenticated: true,
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        token: 'mock-token',
        hasRole: (role: string) => mockUser.roles?.includes(role.toLowerCase()) ?? false,
        hasAnyRole: (roles: string[]) =>
          roles.some(role => mockUser.roles?.includes(role.toLowerCase()) ?? false),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
      };

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <KeycloakContext.Provider
          value={{
            isAuthenticated: false,
            isLoading: false,
            token: null,
            userId: null,
            username: null,
            email: null,
            userRoles: [],
            hasRole: vi.fn(),
            login: vi.fn(),
            logout: vi.fn(),
          }}
        >
          <AuthContext.Provider value={mockAuthContext}>{children}</AuthContext.Provider>
        </KeycloakContext.Provider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      // Should return false for roles not assigned
      expect(result.current.hasRole('ADMIN')).toBe(false);
      expect(result.current.hasRole('admin')).toBe(false);
      expect(result.current.hasRole('MANAGER')).toBe(false);
    });
  });

  describe('User Object - Full Fields', () => {
    it('should return user object with firstName, lastName, name fields', () => {
      const mockUser = {
        id: 'user-1',
        name: 'John Doe',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@test.com',
        username: 'johndoe',
        roles: ['admin'],
      };

      const mockAuthContext = {
        user: mockUser,
        isAuthenticated: true,
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        token: 'mock-token',
        hasRole: (role: string) => mockUser.roles?.includes(role.toLowerCase()) ?? false,
        hasAnyRole: (roles: string[]) =>
          roles.some(role => mockUser.roles?.includes(role.toLowerCase()) ?? false),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
      };

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <KeycloakContext.Provider
          value={{
            isAuthenticated: false,
            isLoading: false,
            token: null,
            userId: null,
            username: null,
            email: null,
            userRoles: [],
            hasRole: vi.fn(),
            login: vi.fn(),
            logout: vi.fn(),
          }}
        >
          <AuthContext.Provider value={mockAuthContext}>{children}</AuthContext.Provider>
        </KeycloakContext.Provider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      // Verify all user fields are present
      expect(result.current.user).toBeDefined();
      expect(result.current.user?.id).toBe('user-1');
      expect(result.current.user?.name).toBe('John Doe');
      expect(result.current.user?.firstName).toBe('John');
      expect(result.current.user?.lastName).toBe('Doe');
      expect(result.current.user?.email).toBe('john@test.com');
      expect(result.current.user?.username).toBe('johndoe');
      expect(result.current.user?.roles).toEqual(['admin']);
    });

    it('should not display "Gast" when user fields are present', () => {
      const mockUser = {
        id: 'user-1',
        name: 'Admin User',
        firstName: 'Admin',
        lastName: 'User',
        email: 'admin@test.com',
        username: 'admin',
        roles: ['admin'],
      };

      const mockAuthContext = {
        user: mockUser,
        isAuthenticated: true,
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        token: 'mock-token',
        hasRole: (role: string) => mockUser.roles?.includes(role.toLowerCase()) ?? false,
        hasAnyRole: (roles: string[]) =>
          roles.some(role => mockUser.roles?.includes(role.toLowerCase()) ?? false),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
      };

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <KeycloakContext.Provider
          value={{
            isAuthenticated: false,
            isLoading: false,
            token: null,
            userId: null,
            username: null,
            email: null,
            userRoles: [],
            hasRole: vi.fn(),
            login: vi.fn(),
            logout: vi.fn(),
          }}
        >
          <AuthContext.Provider value={mockAuthContext}>{children}</AuthContext.Provider>
        </KeycloakContext.Provider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      // Should have proper name, not "Gast"
      expect(result.current.user?.firstName).not.toBe('Gast');
      expect(result.current.user?.name).not.toBe('Gast');
      expect(result.current.user?.firstName).toBe('Admin');
      expect(result.current.user?.lastName).toBe('User');
    });
  });

  describe('Edge Cases', () => {
    it('should handle user without roles array', () => {
      const mockUser = {
        id: 'user-1',
        name: 'No Roles User',
        email: 'noroles@test.com',
        // No roles field
      };

      const mockAuthContext = {
        user: mockUser,
        isAuthenticated: true,
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        token: 'mock-token',
        hasRole: (role: string) => mockUser.roles?.includes(role.toLowerCase()) ?? false,
        hasAnyRole: (roles: string[]) =>
          roles.some(role => mockUser.roles?.includes(role.toLowerCase()) ?? false),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
      };

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <KeycloakContext.Provider
          value={{
            isAuthenticated: false,
            isLoading: false,
            token: null,
            userId: null,
            username: null,
            email: null,
            userRoles: [],
            hasRole: vi.fn(),
            login: vi.fn(),
            logout: vi.fn(),
          }}
        >
          <AuthContext.Provider value={mockAuthContext}>{children}</AuthContext.Provider>
        </KeycloakContext.Provider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      // Should handle missing roles gracefully
      expect(result.current.hasRole('ADMIN')).toBe(false);
      expect(result.current.userRoles).toEqual([]);
    });

    it('should handle multiple roles correctly', () => {
      const mockUser = {
        id: 'user-1',
        name: 'Multi Role User',
        email: 'multi@test.com',
        roles: ['admin', 'auditor', 'manager'],
      };

      const mockAuthContext = {
        user: mockUser,
        isAuthenticated: true,
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        token: 'mock-token',
        hasRole: (role: string) => mockUser.roles?.includes(role.toLowerCase()) ?? false,
        hasAnyRole: (roles: string[]) =>
          roles.some(role => mockUser.roles?.includes(role.toLowerCase()) ?? false),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
      };

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <KeycloakContext.Provider
          value={{
            isAuthenticated: false,
            isLoading: false,
            token: null,
            userId: null,
            username: null,
            email: null,
            userRoles: [],
            hasRole: vi.fn(),
            login: vi.fn(),
            logout: vi.fn(),
          }}
        >
          <AuthContext.Provider value={mockAuthContext}>{children}</AuthContext.Provider>
        </KeycloakContext.Provider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      // Should check all roles
      expect(result.current.hasRole('ADMIN')).toBe(true);
      expect(result.current.hasRole('AUDITOR')).toBe(true);
      expect(result.current.hasRole('MANAGER')).toBe(true);
      expect(result.current.hasRole('SALES')).toBe(false);
    });
  });

  describe('Role-Based Permissions', () => {
    it('should return correct userRoles array', () => {
      const mockUser = {
        id: 'user-1',
        name: 'Admin User',
        email: 'admin@test.com',
        roles: ['admin', 'manager'],
      };

      const mockAuthContext = {
        user: mockUser,
        isAuthenticated: true,
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        token: 'mock-token',
        hasRole: (role: string) => mockUser.roles?.includes(role.toLowerCase()) ?? false,
        hasAnyRole: (roles: string[]) =>
          roles.some(role => mockUser.roles?.includes(role.toLowerCase()) ?? false),
        getValidToken: vi.fn(),
        refreshToken: vi.fn(),
        authInfo: vi.fn(),
      };

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <KeycloakContext.Provider
          value={{
            isAuthenticated: false,
            isLoading: false,
            token: null,
            userId: null,
            username: null,
            email: null,
            userRoles: [],
            hasRole: vi.fn(),
            login: vi.fn(),
            logout: vi.fn(),
          }}
        >
          <AuthContext.Provider value={mockAuthContext}>{children}</AuthContext.Provider>
        </KeycloakContext.Provider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      // Verify userRoles array matches user.roles
      expect(result.current.userRoles).toEqual(['admin', 'manager']);
    });
  });
});
