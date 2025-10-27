/**
 * Contact Enum Hooks
 *
 * Sprint 2.1.7.2 D11.1: Contact Management - Backend/Frontend Parity
 *
 * Provides hooks to fetch contact-related enum values from backend API.
 * - ContactRole: Realistic gastronomy roles (Küchenchef, Einkaufsleiter, etc.)
 * - Salutation: German business etiquette (Herr, Frau, Divers)
 * - DecisionLevel: Sales strategy (Executive, Manager, Operational, Influencer)
 *
 * All enum values are loaded from backend to ensure single source of truth.
 */

import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders, type EnumValue } from '../features/leads/hooks/shared';

/**
 * Fetch contact roles from backend API
 *
 * GET /api/enums/contact-roles
 * Returns: [{ value: "KUECHENCHEF", label: "Küchenchef" }, ...]
 */
async function fetchContactRoles(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}/api/enums/contact-roles`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch contact roles: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch contact roles for dropdowns
 *
 * Sprint 2.1.7.2 D11.1: 15 realistic gastronomy roles
 * - Küchenchef, Sous Chef, Einkaufsleiter, Einkäufer, Betriebsleiter,
 *   Geschäftsführer, Restaurantleiter, F&B Manager, Serviceleiter, Inhaber,
 *   Küchen-Manager, Controlling, Marketing Manager, Qualitätsmanager, Sonstiges
 *
 * @returns Query result with contact roles data
 */
export function useContactRoles() {
  return useQuery({
    queryKey: ['enums', 'contactRoles'],
    queryFn: fetchContactRoles,
    staleTime: 5 * 60 * 1000, // 5 minutes (enum values rarely change)
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
}

/**
 * Fetch salutations from backend API
 *
 * GET /api/enums/salutations
 * Returns: [{ value: "HERR", label: "Herr" }, ...]
 */
async function fetchSalutations(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}/api/enums/salutations`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch salutations: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch salutations for dropdowns
 *
 * Sprint 2.1.7.2 D11.1: German business etiquette
 * - Herr, Frau, Divers
 *
 * @returns Query result with salutations data
 */
export function useSalutations() {
  return useQuery({
    queryKey: ['enums', 'salutations'],
    queryFn: fetchSalutations,
    staleTime: 5 * 60 * 1000, // 5 minutes (enum values rarely change)
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
}

/**
 * Fetch decision levels from backend API
 *
 * GET /api/enums/decision-levels
 * Returns: [{ value: "EXECUTIVE", label: "Executive" }, ...]
 */
async function fetchDecisionLevels(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}/api/enums/decision-levels`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch decision levels: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch decision levels for dropdowns
 *
 * Sprint 2.1.7.2 D11.1: B2B sales strategy
 * - Executive (C-Level, Owner - final decision maker)
 * - Manager (Department heads - recommend/influence)
 * - Operational (Kitchen chef, buyer - day-to-day operations)
 * - Influencer (No formal authority but influences decisions)
 *
 * @returns Query result with decision levels data
 */
export function useDecisionLevels() {
  return useQuery({
    queryKey: ['enums', 'decisionLevels'],
    queryFn: fetchDecisionLevels,
    staleTime: 5 * 60 * 1000, // 5 minutes (enum values rarely change)
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
}

/**
 * Fetch titles from backend API
 *
 * GET /api/enums/titles
 * Returns: [{ value: "DR", label: "Dr." }, ...]
 */
async function fetchTitles(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}/api/enums/titles`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch titles: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch titles for dropdowns
 *
 * Sprint 2.1.7.2 D11.1: German business etiquette titles
 * - Dr., Prof., Prof. Dr., Dipl.-Ing., Dipl.-Kfm., Dipl.-Betriebswirt, M.Sc., B.Sc., MBA
 *
 * @returns Query result with titles data
 */
export function useTitles() {
  return useQuery({
    queryKey: ['enums', 'titles'],
    queryFn: fetchTitles,
    staleTime: 5 * 60 * 1000, // 5 minutes (enum values rarely change)
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
}
