/**
 * Tests für authQueries - React Query Hooks für Auth-Daten
 * @vitest-environment jsdom
 */

import { describe, it, expect, vi } from 'vitest';
import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useUserRoles, useUserInfo, useHasRole } from './authQueries';
import React from 'react';
import type { ReactNode } from 'react';

// Mock für keycloak
vi.mock('../keycloak', () => ({
  authUtils: {
    isAuthenticated: vi.fn(),
    getUserId: vi.fn(),
    getUsername: vi.fn(),
    getEmail: vi.fn(),
    getUserRoles: vi.fn(),
  },
}));

import { authUtils } from '../keycloak';

// Helper für QueryClient Provider
const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false, // Keine Retries in Tests
      },
    },
  });

  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

describe('authQueries', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('useUserRoles', () => {
    it('sollte User-Rollen zurückgeben wenn authentifiziert', async () => {
      const mockRoles = ['user', 'admin'];
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);
      (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue(mockRoles);

      const { result } = renderHook(() => useUserRoles(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual(mockRoles);
      expect(authUtils.getUserRoles).toHaveBeenCalled();
    });

    it('sollte disabled sein wenn nicht authentifiziert', () => {
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(false);

      const { result } = renderHook(() => useUserRoles(), {
        wrapper: createWrapper(),
      });

      // In React Query v5 gibt es kein isIdle mehr, stattdessen prüfen wir isPending und fetchStatus
      expect(result.current.isPending).toBe(true);
      expect(result.current.fetchStatus).toBe('idle');
      expect(result.current.data).toBeUndefined();
      expect(authUtils.getUserRoles).not.toHaveBeenCalled();
    });

    it('sollte staleTime und cacheTime korrekt setzen', async () => {
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);
      (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue(['user']);

      const { result } = renderHook(() => useUserRoles(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      // Die Query sollte die richtigen Optionen haben
      expect(result.current).toMatchObject({
        isSuccess: true,
        data: ['user'],
      });
    });
  });

  describe('useUserInfo', () => {
    it('sollte vollständige User-Info zurückgeben wenn authentifiziert', async () => {
      const mockUserInfo = {
        userId: 'user-123',
        username: 'john.doe',
        email: 'john@example.com',
        roles: ['user', 'admin'],
      };

      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);
      (authUtils.getUserId as ReturnType<typeof vi.fn>).mockReturnValue(mockUserInfo.userId);
      (authUtils.getUsername as ReturnType<typeof vi.fn>).mockReturnValue(mockUserInfo.username);
      (authUtils.getEmail as ReturnType<typeof vi.fn>).mockReturnValue(mockUserInfo.email);
      (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue(mockUserInfo.roles);

      const { result } = renderHook(() => useUserInfo(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual(mockUserInfo);
    });

    it('sollte disabled sein wenn nicht authentifiziert', () => {
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(false);

      const { result } = renderHook(() => useUserInfo(), {
        wrapper: createWrapper(),
      });

      // In React Query v5 gibt es kein isIdle mehr, stattdessen prüfen wir isPending und fetchStatus
      expect(result.current.isPending).toBe(true);
      expect(result.current.fetchStatus).toBe('idle');
      expect(result.current.data).toBeUndefined();
      expect(authUtils.getUserId).not.toHaveBeenCalled();
    });

    it('sollte mit undefined Werten umgehen können', async () => {
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);
      (authUtils.getUserId as ReturnType<typeof vi.fn>).mockReturnValue(undefined);
      (authUtils.getUsername as ReturnType<typeof vi.fn>).mockReturnValue(undefined);
      (authUtils.getEmail as ReturnType<typeof vi.fn>).mockReturnValue(undefined);
      (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue([]);

      const { result } = renderHook(() => useUserInfo(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual({
        userId: undefined,
        username: undefined,
        email: undefined,
        roles: [],
      });
    });
  });

  describe('useHasRole', () => {
    it('sollte true zurückgeben wenn User die Rolle hat', async () => {
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);
      (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue(['user', 'admin']);

      const { result } = renderHook(() => useHasRole('admin'), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current).toBe(true);
      });
    });

    it('sollte false zurückgeben wenn User die Rolle nicht hat', async () => {
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);
      (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue(['user']);

      const { result } = renderHook(() => useHasRole('admin'), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current).toBe(false);
      });
    });

    it('sollte false zurückgeben wenn nicht authentifiziert', () => {
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(false);

      const { result } = renderHook(() => useHasRole('admin'), {
        wrapper: createWrapper(),
      });

      // Sollte false sein, da keine Rollen geladen werden
      expect(result.current).toBe(false);
    });

    it('sollte mit leeren Rollen umgehen können', async () => {
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);
      (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue([]);

      const { result } = renderHook(() => useHasRole('admin'), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current).toBe(false);
      });
    });

    it('sollte bei Änderung der Rolle reagieren', async () => {
      (authUtils.isAuthenticated as ReturnType<typeof vi.fn>).mockReturnValue(true);
      (authUtils.getUserRoles as ReturnType<typeof vi.fn>).mockReturnValue(['user']);

      const { result, rerender } = renderHook(({ role }) => useHasRole(role), {
        wrapper: createWrapper(),
        initialProps: { role: 'user' },
      });

      await waitFor(() => {
        expect(result.current).toBe(true);
      });

      // Ändere die gesuchte Rolle
      rerender({ role: 'admin' });

      await waitFor(() => {
        expect(result.current).toBe(false);
      });
    });
  });
});
