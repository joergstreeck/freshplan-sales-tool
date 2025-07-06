/**
 * React Query Hooks für Sales Cockpit Daten
 *
 * Bietet effizientes Caching und State Management
 */
import { useQuery } from '@tanstack/react-query';
import { salesCockpitService } from '../services/salesCockpitService';
import type { SalesCockpitDashboard } from '../types/salesCockpit';

// Query Keys für Cache-Invalidierung
export const salesCockpitKeys = {
  all: ['salesCockpit'] as const,
  dashboard: (userId: string) => ['salesCockpit', 'dashboard', userId] as const,
  health: () => ['salesCockpit', 'health'] as const,
};

/**
 * Hook zum Laden der Dashboard-Daten für einen Benutzer
 *
 * @param userId Die ID des Benutzers
 * @param enabled Ob die Query ausgeführt werden soll (default: true)
 */
export function useDashboardData(userId: string | null, enabled = true) {
  return useQuery<SalesCockpitDashboard, Error>({
    queryKey: salesCockpitKeys.dashboard(userId || ''),
    queryFn: () => {
      if (!userId) {
        throw new Error('User ID is required');
      }
      return salesCockpitService.getDashboardData(userId);
    },
    enabled: enabled && !!userId,
    staleTime: 30 * 1000, // Daten sind 30 Sekunden lang "frisch"
    refetchInterval: 60 * 1000, // Automatisches Refresh alle 60 Sekunden
    refetchOnWindowFocus: true,
  });
}

/**
 * Hook für Health-Check des Sales Cockpit Service
 */
export function useSalesCockpitHealth() {
  return useQuery({
    queryKey: salesCockpitKeys.health(),
    queryFn: () => salesCockpitService.checkHealth(),
    staleTime: 5 * 60 * 1000, // 5 Minuten
    refetchOnWindowFocus: false,
  });
}
