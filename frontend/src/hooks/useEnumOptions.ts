/**
 * Enum Options Hook
 *
 * Sprint 2.1.7.2 D11: Server-Driven UI for Enum Options
 *
 * Fetches enum options from backend API to enable dynamic form rendering.
 * Backend: GET /api/enums/{enumType} (EnumResource.java)
 *
 * Supported Enum Endpoints:
 * - /api/enums/business-types
 * - /api/enums/kitchen-sizes
 * - /api/enums/lead-sources
 * - /api/enums/activity-outcomes
 * - /api/enums/pain-intensity
 * - /api/enums/budget-availability
 * - /api/enums/deal-sizes
 * - /api/enums/response-rates
 * - /api/enums/meeting-frequency
 * - /api/enums/engagement-levels
 * - /api/enums/decision-speeds
 * - /api/enums/relationship-status
 * - /api/enums/decision-maker-access
 * - /api/enums/decision-timeframes
 *
 * Backend Response Format:
 * ```json
 * [
 *   { "value": "RESTAURANT", "label": "Restaurant" },
 *   { "value": "HOTEL", "label": "Hotel" }
 * ]
 * ```
 *
 * Benefits:
 * - Backend controls enum values (Single Source of Truth)
 * - No frontend/backend parity issues
 * - Add new enum values in backend only
 * - Type-safe enum handling
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders } from '../features/leads/hooks/shared';

/**
 * Enum Option (Backend Response)
 */
export interface EnumOption {
  value: string;
  label: string;
}

/**
 * Backend EnumValue Format
 *
 * IMPORTANT: Backend EnumResource.EnumValue uses 'label', not 'displayName'!
 * See: EnumResource.java line 467
 */
interface BackendEnumValue {
  value: string;
  label: string;
}

/**
 * Fetch enum options from backend
 *
 * @param enumSource - Full URL path (e.g., "/api/enums/business-types")
 * @returns Array of enum options with value and label
 */
async function fetchEnumOptions(enumSource: string): Promise<EnumOption[]> {
  if (!enumSource || enumSource === '') {
    return [];
  }

  const response = await fetch(`${BASE_URL}${enumSource}`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch enum options from ${enumSource}: ${response.statusText}`);
  }

  const backendValues: BackendEnumValue[] = await response.json();

  // Transform backend format to frontend format
  // Note: Backend already uses 'label', so this is a 1:1 mapping
  return backendValues.map(enumValue => ({
    value: enumValue.value,
    label: enumValue.label,
  }));
}

/**
 * Hook to fetch enum options for dynamic form rendering
 *
 * Usage in schema-driven forms:
 * ```tsx
 * const businessTypeField = fields.find(f => f.fieldKey === 'businessType');
 * const { data: options } = useEnumOptions(businessTypeField?.enumSource || '');
 *
 * <Select value={formData.businessType} label={businessTypeField.label}>
 *   <MenuItem value="">—</MenuItem>
 *   {options?.map(option => (
 *     <MenuItem key={option.value} value={option.value}>
 *       {option.label}
 *     </MenuItem>
 *   ))}
 * </Select>
 * ```
 *
 * @param enumSource - Full URL path (e.g., "/api/enums/business-types")
 * @returns Query result with enum options array
 */
export function useEnumOptions(enumSource: string) {
  return useQuery({
    queryKey: ['enums', enumSource],
    queryFn: () => fetchEnumOptions(enumSource),
    staleTime: 10 * 60 * 1000, // 10 minutes (enums rarely change)
    gcTime: 30 * 60 * 1000, // 30 minutes
    enabled: !!enumSource && enumSource !== '', // Only fetch if enumSource is provided
  });
}
