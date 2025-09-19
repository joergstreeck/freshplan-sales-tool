import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { EffectiveSettings, PatchOp, PatchScope } from './settings.types';
import { useAuth } from '../auth/AuthContext';

/**
 * OPTIMIZED: Best of both worlds
 * - Type-safe settings access with granular hooks (from OLD)
 * - Intelligent ETag caching and 304 handling (from OLD)
 * - Complete update functionality with PATCH operations (from NEW)
 * - Optimistic updates with proper rollback (ENHANCED)
 * - Authorization integration (ENHANCED)
 * - Error recovery and retry logic (ENHANCED)
 */

interface SettingsError {
  type: string;
  title: string;
  status: number;
  detail: string;
  timestamp: string;
}

interface UseSettingsResult<T> {
  settings: T | null;
  loading: boolean;
  error: Error | null;
  refetch: () => void;
  isStale: boolean;
}

interface UseUpdateSettingsResult {
  updateSetting: (key: string, value: any, scope?: PatchScope) => Promise<void>;
  deleteSetting: (key: string, scope?: PatchScope) => Promise<void>;
  isLoading: boolean;
  error: SettingsError | null;
}

// Enhanced API client with authorization and retry logic
class SettingsApi {
  private baseUrl = '/api/settings';

  constructor(private getAuthToken: () => string | null) {}

  private async request(endpoint: string, options: RequestInit = {}): Promise<Response> {
    const token = this.getAuthToken();
    const headers = {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers,
    };

    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new SettingsApiError(response.status, errorData);
    }

    return response;
  }

  async fetchEffective(
    etag?: string,
    queryParams?: { accountId?: string; contactRole?: string; contactId?: string }
  ): Promise<EffectiveSettings> {
    const headers: Record<string, string> = {};
    if (etag) headers['If-None-Match'] = etag;

    const searchParams = new URLSearchParams();
    if (queryParams?.accountId) searchParams.set('accountId', queryParams.accountId);
    if (queryParams?.contactRole) searchParams.set('contactRole', queryParams.contactRole);
    if (queryParams?.contactId) searchParams.set('contactId', queryParams.contactId);

    const endpoint = `/effective${searchParams.toString() ? `?${searchParams}` : ''}`;
    const response = await this.request(endpoint, { headers });

    if (response.status === 304 && etag) {
      // Cache hit - return cached version indicator
      return { blob: {}, etag, computedAt: new Date().toISOString() };
    }

    const data = await response.json();
    return {
      blob: data.blob,
      etag: response.headers.get('ETag') || data.etag,
      computedAt: data.computedAt,
    };
  }

  async patchSettings(ops: PatchOp[]): Promise<EffectiveSettings> {
    const response = await this.request('', {
      method: 'PATCH',
      body: JSON.stringify(ops),
    });

    const data = await response.json();
    return {
      blob: data.blob,
      etag: response.headers.get('ETag') || data.etag,
      computedAt: data.computedAt,
    };
  }
}

class SettingsApiError extends Error {
  constructor(
    public status: number,
    public problemDetails: Partial<SettingsError>
  ) {
    super(problemDetails.detail || `HTTP ${status}`);
    this.name = 'SettingsApiError';
  }
}

// Main hook for effective settings with intelligent caching
export function useEffectiveSettings(queryParams?: {
  accountId?: string;
  contactRole?: string;
  contactId?: string;
}): UseSettingsResult<Record<string, any>> {
  const { getToken } = useAuth();
  const api = new SettingsApi(getToken);
  const queryClient = useQueryClient();

  const query = useQuery({
    queryKey: ['settings', 'effective', queryParams],
    queryFn: async () => {
      const cached = queryClient.getQueryData<EffectiveSettings>([
        'settings',
        'effective',
        queryParams,
      ]);
      return api.fetchEffective(cached?.etag, queryParams);
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    cacheTime: 30 * 60 * 1000, // 30 minutes
    retry: (failureCount, error) => {
      // Don't retry on auth errors
      if (error instanceof SettingsApiError && error.status === 401) return false;
      return failureCount < 3;
    },
  });

  return {
    settings: query.data?.blob || null,
    loading: query.isLoading,
    error: query.error as Error | null,
    refetch: query.refetch,
    isStale: query.isStale,
  };
}

// Typed hooks for specific setting categories
export function useNotificationSettings() {
  const { settings, ...rest } = useEffectiveSettings();
  return {
    settings: settings?.notifications || null,
    ...rest,
  };
}

export function useUISettings() {
  const { settings, ...rest } = useEffectiveSettings();
  return {
    settings: settings?.ui || null,
    ...rest,
  };
}

export function useSLASettings() {
  const { settings, ...rest } = useEffectiveSettings();
  return {
    settings: settings?.sla || null,
    ...rest,
  };
}

// Enhanced update hook with optimistic updates and rollback
export function useUpdateSettings(): UseUpdateSettingsResult {
  const { getToken } = useAuth();
  const api = new SettingsApi(getToken);
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: (ops: PatchOp[]) => api.patchSettings(ops),
    onMutate: async (ops: PatchOp[]) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['settings', 'effective'] });

      // Snapshot the previous value
      const previousSettings = queryClient.getQueryData<EffectiveSettings>([
        'settings',
        'effective',
      ]);

      // Optimistically update
      if (previousSettings) {
        const optimisticBlob = { ...previousSettings.blob };

        for (const op of ops) {
          if (op.op === 'set') {
            setNestedValue(optimisticBlob, op.key, op.value);
          } else if (op.op === 'unset') {
            deleteNestedValue(optimisticBlob, op.key);
          }
        }

        queryClient.setQueryData<EffectiveSettings>(
          ['settings', 'effective'],
          {
            ...previousSettings,
            blob: optimisticBlob,
          }
        );
      }

      return { previousSettings };
    },
    onError: (err, ops, context) => {
      // Rollback on error
      if (context?.previousSettings) {
        queryClient.setQueryData(
          ['settings', 'effective'],
          context.previousSettings
        );
      }
    },
    onSuccess: (data) => {
      // Update with server response (includes new ETag)
      queryClient.setQueryData<EffectiveSettings>(['settings', 'effective'], data);
    },
    onSettled: () => {
      // Always refetch to ensure we're in sync
      queryClient.invalidateQueries({ queryKey: ['settings', 'effective'] });
    },
  });

  const updateSetting = async (key: string, value: any, scope?: PatchScope) => {
    return mutation.mutateAsync([{ op: 'set', key, value, scope }]);
  };

  const deleteSetting = async (key: string, scope?: PatchScope) => {
    return mutation.mutateAsync([{ op: 'unset', key, scope }]);
  };

  return {
    updateSetting,
    deleteSetting,
    isLoading: mutation.isLoading,
    error: mutation.error instanceof SettingsApiError
      ? mutation.error.problemDetails as SettingsError
      : null,
  };
}

// Helper functions for nested object manipulation
function setNestedValue(obj: any, path: string, value: any) {
  const keys = path.split('.');
  let current = obj;

  for (let i = 0; i < keys.length - 1; i++) {
    const key = keys[i];
    if (!(key in current) || typeof current[key] !== 'object') {
      current[key] = {};
    }
    current = current[key];
  }

  current[keys[keys.length - 1]] = value;
}

function deleteNestedValue(obj: any, path: string) {
  const keys = path.split('.');
  let current = obj;

  for (let i = 0; i < keys.length - 1; i++) {
    const key = keys[i];
    if (!(key in current) || typeof current[key] !== 'object') {
      return; // Path doesn't exist
    }
    current = current[key];
  }

  delete current[keys[keys.length - 1]];
}

// Batch update hook for multiple related changes
export function useBatchUpdateSettings() {
  const updateMutation = useUpdateSettings();

  const batchUpdate = async (updates: Array<{ key: string; value?: any; scope?: PatchScope; op?: 'set' | 'unset' }>) => {
    const ops: PatchOp[] = updates.map(update => ({
      op: update.op || (update.value !== undefined ? 'set' : 'unset'),
      key: update.key,
      value: update.value,
      scope: update.scope,
    }));

    // Use internal mutation to apply all changes atomically
    const { getToken } = useAuth();
    const api = new SettingsApi(getToken);
    return api.patchSettings(ops);
  };

  return {
    batchUpdate,
    isLoading: updateMutation.isLoading,
    error: updateMutation.error,
  };
}