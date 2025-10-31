/**
 * useCurrentUser Hook
 * Sprint 2.1.7.2 D1 - Get full user profile with xentralSalesRepId
 *
 * @description Fetches current user from /api/users/me including xentralSalesRepId
 * @since 2025-10-24
 */

import { useState, useEffect } from 'react';
import { httpClient } from '../lib/apiClient';

/**
 * Full User Response from Backend (UserResponse.java)
 */
export interface CurrentUser {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
  xentralSalesRepId?: string; // Xentral Sales Rep ID (nullable)
  enabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Hook to fetch current user profile with xentralSalesRepId
 *
 * @returns {CurrentUser | null} Current user or null if not loaded
 * @returns {boolean} loading - True if fetching user
 * @returns {Error | null} error - Error if fetch failed
 *
 * @example
 * ```tsx
 * const { user, loading, error } = useCurrentUser();
 *
 * if (loading) return <CircularProgress />;
 * if (error) return <Alert severity="error">{error.message}</Alert>;
 * if (!user?.xentralSalesRepId) return <Alert severity="warning">No Xentral Sales Rep ID</Alert>;
 *
 * // Use user.xentralSalesRepId for API calls
 * const url = `/api/xentral/customers?salesRepId=${user.xentralSalesRepId}`;
 * ```
 */
export function useCurrentUser() {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    let cancelled = false;

    const fetchUser = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await httpClient.get<CurrentUser>('/api/users/me');

        if (!cancelled) {
          setUser(response.data);
        }
      } catch (err) {
        if (!cancelled) {
          console.error('Failed to fetch current user:', err);
          setError(err instanceof Error ? err : new Error('Failed to fetch user'));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    fetchUser();

    return () => {
      cancelled = true;
    };
  }, []);

  return { user, loading, error };
}
