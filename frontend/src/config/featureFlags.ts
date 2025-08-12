/**
 * FC-005 Feature Flags Configuration
 *
 * Zentrale Verwaltung von Feature Flags für Development und Production.
 * Ermöglicht kontrolliertes Ein-/Ausschalten von Features ohne Code-Änderungen.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/02-optimization-strategies.md
 * @see /Users/joergstreeck/freshplan-sales-tool/CLAUDE.md#feature-flag-governance
 */

/**
 * Feature Flag Type Definition
 */
export interface FeatureFlag {
  /** Unique identifier for the feature flag */
  name: string;
  /** Human-readable description */
  description: string;
  /** Whether the feature is enabled */
  enabled: boolean;
  /** When this flag should be removed (sunset date) */
  sunsetDate?: string;
  /** Additional metadata */
  metadata?: Record<string, unknown>;
}

/**
 * Environment-based feature flag configuration
 *
 * @remarks
 * - Production flags are controlled via environment variables
 * - Development flags can be overridden locally
 * - All flags must have a sunset date (max 30 days)
 */
export const featureFlags = {
  /**
   * Auth Bypass for Development
   *
   * @deprecated Remove after Keycloak integration is stable
   * @sunsetDate 2025-08-27
   */
  authBypass: {
    name: 'ff_fc005_auth_bypass',
    description: 'Bypass authentication in development mode for faster testing',
    enabled: import.meta.env.DEV && import.meta.env.VITE_AUTH_BYPASS !== 'false',
    sunsetDate: '2025-08-27',
    metadata: {
      createdBy: 'FC-005',
      reason: 'Development velocity during initial implementation',
      securityRisk: 'medium',
      productionSafe: false,
    },
  },

  /**
   * Mock Data in Development
   *
   * @sunsetDate 2025-08-15
   */
  useMockData: {
    name: 'ff_fc005_mock_data',
    description: 'Use mock data instead of real API calls',
    enabled: import.meta.env.DEV && import.meta.env.VITE_USE_MOCK_DATA === 'true',
    sunsetDate: '2025-08-15',
    metadata: {
      createdBy: 'FC-005',
      reason: 'Testing without backend dependency',
    },
  },

  /**
   * Enhanced Debug Mode
   *
   * @sunsetDate 2025-09-01
   */
  enhancedDebug: {
    name: 'ff_fc005_enhanced_debug',
    description: 'Show additional debug information in UI',
    enabled: import.meta.env.DEV && import.meta.env.VITE_ENHANCED_DEBUG === 'true',
    sunsetDate: '2025-09-01',
    metadata: {
      createdBy: 'FC-005',
      reason: 'Debugging complex customer data flows',
    },
  },
} as const;

/**
 * Type-safe feature flag names
 */
export type FeatureFlagName = keyof typeof featureFlags;

/**
 * Check if a feature flag is enabled
 *
 * @param flagName - The name of the feature flag
 * @returns Whether the feature is enabled
 *
 * @example
 * ```typescript
 * if (isFeatureEnabled('authBypass')) {
 *   // Show development UI
 * }
 * ```
 */
export function isFeatureEnabled(flagName: FeatureFlagName): boolean {
  const flag = featureFlags[flagName];
  return flag?.enabled ?? false;
}

/**
 * Get feature flag metadata
 *
 * @param flagName - The name of the feature flag
 * @returns The feature flag object or undefined
 */
export function getFeatureFlag(flagName: FeatureFlagName): FeatureFlag | undefined {
  return featureFlags[flagName];
}

/**
 * Check if a feature flag is past its sunset date
 *
 * @param flagName - The name of the feature flag
 * @returns Whether the flag should be removed
 */
export function isFeatureFlagExpired(flagName: FeatureFlagName): boolean {
  const flag = featureFlags[flagName];
  if (!flag?.sunsetDate) return false;

  const sunsetDate = new Date(flag.sunsetDate);
  const today = new Date();
  return today > sunsetDate;
}

/**
 * Development helper to list all active feature flags
 */
export function listActiveFeatureFlags(): string[] {
  return Object.entries(featureFlags)
    .filter(([_, flag]) => flag.enabled)
    .map(([name, _]) => name);
}

// Log active feature flags in development
if (import.meta.env.DEV) {
  // Warn about expired flags
  Object.entries(featureFlags).forEach(([name, flag]) => {
    if (isFeatureFlagExpired(name as FeatureFlagName)) {
      // Feature flag expired check
    }
  });
}
