/**
 * Settings API Client
 * Handles communication with the Settings Registry backend
 */

import { getJSON, putJSON, postJSON } from '../apiClient';

export interface Setting {
  key: string;
  value: Record<string, unknown>;
  scope: 'GLOBAL' | 'USER' | 'TENANT';
  userId?: string;
  tenantId?: string;
  etag?: string;
}

export interface SettingsContext {
  userId?: string;
  tenantId?: string;
  roles?: string[];
}

/**
 * Get a setting value from the registry
 */
export async function getSetting(
  key: string,
  scope: 'GLOBAL' | 'USER' | 'TENANT' = 'GLOBAL',
  context?: SettingsContext
): Promise<Setting | null> {
  try {
    const params = new URLSearchParams({
      key,
      scope,
      ...(context?.userId && { userId: context.userId }),
      ...(context?.tenantId && { tenantId: context.tenantId }),
    });

    const response = await getJSON<Setting>(`/api/settings?${params}`);
    return response;
  } catch (error) {
    if (error instanceof Error && error.message.includes('404')) {
      return null;
    }
    throw error;
  }
}

/**
 * Create or update a setting
 */
export async function saveSetting(setting: Setting): Promise<Setting> {
  if (setting.etag) {
    return await putJSON<Setting>('/api/settings', setting, {
      headers: {
        'If-Match': setting.etag,
      },
    });
  }
  return await postJSON<Setting>('/api/settings', setting);
}

/**
 * Get multiple settings by pattern
 */
export async function getSettings(
  pattern: string,
  scope?: 'GLOBAL' | 'USER' | 'TENANT',
  context?: SettingsContext
): Promise<Setting[]> {
  const params = new URLSearchParams({
    pattern,
    ...(scope && { scope }),
    ...(context?.userId && { userId: context.userId }),
    ...(context?.tenantId && { tenantId: context.tenantId }),
  });

  return await getJSON<Setting[]>(`/api/settings/search?${params}`);
}

/**
 * Generate a cache key for React Query
 */
export function getSettingQueryKey(
  key: string,
  scope: 'GLOBAL' | 'USER' | 'TENANT' = 'GLOBAL',
  context?: SettingsContext
): (string | SettingsContext | undefined)[] {
  // Sort context keys for stable cache key
  const sortedContext = context
    ? Object.keys(context)
        .sort()
        .reduce((acc, key) => {
          const contextKey = key as keyof SettingsContext;
          if (context[contextKey] !== undefined) {
            (acc as Record<string, unknown>)[key] = context[contextKey];
          }
          return acc;
        }, {} as SettingsContext)
    : undefined;

  return ['setting', key, scope, sortedContext];
}

/**
 * Check if a feature flag is enabled
 */
export async function isFeatureEnabled(flag: string, context?: SettingsContext): Promise<boolean> {
  const setting = await getSetting(`feature.${flag}`, 'GLOBAL', context);
  return setting?.value?.enabled === true;
}

/**
 * Get theme settings
 */
export async function getThemeSettings(
  context?: SettingsContext
): Promise<Record<string, unknown>> {
  const setting = await getSetting('ui.theme', 'GLOBAL', context);
  return setting?.value || getDefaultTheme();
}

/**
 * Get default theme
 */
function getDefaultTheme(): Record<string, unknown> {
  return {
    mode: 'light',
    primaryColor: 'primary.main',
    secondaryColor: 'secondary.main',
    fontFamily: theme => theme.typography.body1.fontFamily,
  };
}

/**
 * Validate ETag for conditional requests
 */
export function validateETag(etag: string | null | undefined): string | undefined {
  if (!etag) return undefined;
  // Remove quotes if present
  return etag.replace(/^"(.+)"$/, '$1');
}
