/**
 * Settings React Query Hooks
 * Part of Sprint 1.3 PR #3 - Frontend Settings Integration (FP-233)
 *
 * Provides React Query hooks for Settings with ETag/304 support
 * Implements useSetting and useFeatureFlag hooks
 */

import { useQuery, UseQueryOptions } from '@tanstack/react-query';
import { fetchSetting, resolveSetting, SettingResponse } from './api';

/**
 * React Query hook for fetching a specific setting.
 * Implements caching with staleTime: 60s and gcTime: 10min.
 * Automatically handles ETag/304 responses through the API client.
 */
export function useSetting(
  key: string,
  scope: 'GLOBAL' | 'TENANT' | 'TERRITORY' | 'ACCOUNT' | 'CONTACT_ROLE' = 'GLOBAL',
  scopeId?: string,
  options?: Omit<
    UseQueryOptions<SettingResponse | null, Error>,
    'queryKey' | 'queryFn' | 'staleTime' | 'gcTime' | 'retry'
  >
) {
  return useQuery<SettingResponse | null, Error>({
    queryKey: ['setting', scope, scopeId, key],
    queryFn: () => fetchSetting(key, scope, scopeId),
    staleTime: 60_000, // Consider data fresh for 1 minute
    gcTime: 600_000, // Keep in cache for 10 minutes
    retry: 1, // Retry once on failure
    ...options,
  });
}

/**
 * React Query hook for resolving a setting hierarchically.
 * Finds the most specific setting based on the provided context.
 */
export function useResolvedSetting(
  key: string,
  context?: {
    tenantId?: string;
    territory?: string;
    accountId?: string;
    contactRole?: string;
  },
  options?: Omit<
    UseQueryOptions<SettingResponse | null, Error>,
    'queryKey' | 'queryFn' | 'staleTime' | 'gcTime' | 'retry'
  >
) {
  return useQuery<SettingResponse | null, Error>({
    queryKey: ['setting', 'resolve', key, context],
    queryFn: () => resolveSetting(key, context),
    staleTime: 60_000, // Consider data fresh for 1 minute
    gcTime: 600_000, // Keep in cache for 10 minutes
    retry: 1, // Retry once on failure
    ...options,
  });
}

/**
 * Hook for accessing feature flags from the settings.
 * Returns the value of a specific feature flag, with a default fallback.
 */
export function useFeatureFlag(
  flag: string,
  defaultValue: boolean = false
): {
  isEnabled: boolean;
  isLoading: boolean;
  error: Error | null;
} {
  const { data, isLoading, error } = useSetting('system.feature_flags', 'GLOBAL');

  // Extract the flag value with type checking
  const flagValue = data?.value?.[flag];
  const isEnabled = typeof flagValue === 'boolean' ? flagValue : defaultValue;

  return {
    isEnabled,
    isLoading,
    error,
  };
}

/**
 * Hook for accessing theme settings.
 * Returns theme configuration with FreshFoodz defaults.
 */
export function useThemeSettings(): {
  theme: {
    primary: string;
    secondary: string;
    [key: string]: unknown;
  };
  isLoading: boolean;
  error: Error | null;
} {
  const { data, isLoading, error } = useSetting('ui.theme', 'GLOBAL');

  // FreshFoodz brand colors as defaults
  const DEFAULT_THEME = {
    primary: theme.palette.primary.main, // FreshFoodz Green
    secondary: theme.palette.secondary.main, // FreshFoodz Blue
  };

  // Type-safe theme merging
  const remoteTheme = data?.value || {};
  const theme = {
    ...DEFAULT_THEME,
    primary: typeof remoteTheme.primary === 'string' ? remoteTheme.primary : DEFAULT_THEME.primary,
    secondary:
      typeof remoteTheme.secondary === 'string' ? remoteTheme.secondary : DEFAULT_THEME.secondary,
    // Include any additional properties from remote
    ...Object.entries(remoteTheme).reduce(
      (acc, [key, value]) => {
        if (key !== 'primary' && key !== 'secondary') {
          acc[key] = value;
        }
        return acc;
      },
      {} as Record<string, unknown>
    ),
  };

  return {
    theme,
    isLoading,
    error,
  };
}

/**
 * Hook for accessing user-specific settings.
 * Resolves settings hierarchically based on user context.
 */
export function useUserSettings(
  key: string,
  userContext?: {
    tenantId?: string;
    territory?: string;
    accountId?: string;
    contactRole?: string;
  }
): {
  value: unknown;
  isLoading: boolean;
  error: Error | null;
  scope?: 'GLOBAL' | 'TENANT' | 'TERRITORY' | 'ACCOUNT' | 'CONTACT_ROLE';
} {
  const { data, isLoading, error } = useResolvedSetting(key, userContext);

  return {
    value: data?.value || null,
    isLoading,
    error,
    scope: data?.scope,
  };
}

/**
 * Hook for checking multiple feature flags at once.
 * Returns an object with all flag states.
 */
export function useFeatureFlags(
  flags: string[],
  defaults: Record<string, boolean> = {}
): {
  flags: Record<string, boolean>;
  isLoading: boolean;
  error: Error | null;
} {
  const { data, isLoading, error } = useSetting('system.feature_flags', 'GLOBAL');

  const flagStates: Record<string, boolean> = {};

  for (const flag of flags) {
    const flagValue = data?.value?.[flag];
    flagStates[flag] = typeof flagValue === 'boolean' ? flagValue : (defaults[flag] ?? false);
  }

  return {
    flags: flagStates,
    isLoading,
    error,
  };
}

/**
 * Export all hooks for convenience
 */
export { type SettingResponse } from './api';
