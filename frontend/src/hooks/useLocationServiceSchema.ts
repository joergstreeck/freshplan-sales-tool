import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders } from '../features/leads/hooks/shared';
import type { FieldDefinition } from '../features/customers/types/field.types';

/**
 * Service Field Group (Server-Driven UI)
 *
 * Sprint 2.1.7.x: Location Service Schema - Server-Driven UI
 *
 * Groups related service fields together with visual metadata.
 */
export interface ServiceFieldGroup {
  id: string;
  title: string;
  icon: string;
  fields: FieldDefinition[];
}

/**
 * Fetch location service schema from backend API
 *
 * GET /api/locations/service-schema?industry={industry}
 *
 * Returns: [{ id: "breakfast", title: "Frühstücksgeschäft", icon: "☕", fields: [...] }, ...]
 *
 * Sprint 2.1.7.x: fieldCatalog.json Migration - Location Service Fields
 *
 * Replaces hardcoded field definitions in ServiceFieldsContainer.tsx with dynamic
 * Server-Driven UI.
 */
async function fetchLocationServiceSchema(industry: string): Promise<ServiceFieldGroup[]> {
  const response = await fetch(
    `${BASE_URL}/api/locations/service-schema?industry=${encodeURIComponent(industry)}`,
    {
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
    }
  );

  if (!response.ok) {
    throw new Error(`Failed to fetch location service schema: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch location service schema for a specific industry
 *
 * Sprint 2.1.7.x: fieldCatalog.json Migration - Server-Driven UI
 *
 * Industry-specific field groups:
 * - hotel: Frühstücksgeschäft, Mittag- und Abendessen, Zusatzservices, Kapazität
 * - krankenhaus: Patientenverpflegung, Diätformen
 * - betriebsrestaurant: Betriebszeiten
 *
 * @param industry Industry type (hotel, krankenhaus, betriebsrestaurant)
 * @returns Query result with service field groups
 */
export function useLocationServiceSchema(industry: string) {
  return useQuery({
    queryKey: ['locationServiceSchema', industry],
    queryFn: () => fetchLocationServiceSchema(industry),
    enabled: !!industry, // Only fetch if industry is provided
    staleTime: 5 * 60 * 1000, // 5 minutes (schema rarely changes)
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
}
