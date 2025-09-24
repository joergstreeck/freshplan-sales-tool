/**
 * Settings Module Exports
 * Part of Sprint 1.3 PR #3 - Frontend Settings Integration (FP-233)
 */

// API functions
export {
  fetchSetting,
  resolveSetting,
  clearSettingsCache,
  getCacheStats,
  type SettingResponse,
} from './api';

// React Query hooks
export {
  useSetting,
  useResolvedSetting,
  useFeatureFlag,
  useFeatureFlags,
  useThemeSettings,
  useUserSettings,
} from './hooks';
