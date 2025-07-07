/**
 * Service für Sales Cockpit BFF-Integration
 *
 * Ruft aggregierte Dashboard-Daten vom Backend-for-Frontend ab
 */
import { httpClient } from '../../../lib/apiClient';
import type { SalesCockpitDashboard } from '../types/salesCockpit';
import { IS_DEV_MODE, USE_KEYCLOAK_IN_DEV } from '../../../lib/constants';

class SalesCockpitService {
  /**
   * Lädt alle Dashboard-Daten für einen bestimmten Benutzer
   *
   * @param userId Die ID des Benutzers
   * @returns Aggregierte Dashboard-Daten
   */
  async getDashboardData(userId: string): Promise<SalesCockpitDashboard> {
    // In Development-Mode ohne Keycloak: Nutze den /dev Endpunkt
    const endpoint = IS_DEV_MODE && !USE_KEYCLOAK_IN_DEV 
      ? '/api/sales-cockpit/dashboard/dev'
      : `/api/sales-cockpit/dashboard/${userId}`;
    
    const response = await httpClient.get<SalesCockpitDashboard>(endpoint);
    return response.data;
  }

  /**
   * Health-Check für den Sales Cockpit Service
   *
   * @returns Status des Services
   */
  async checkHealth(): Promise<{ status: string; service: string }> {
    const response = await httpClient.get<{ status: string; service: string }>(
      '/api/sales-cockpit/health'
    );
    return response.data;
  }
}

// Singleton-Instanz
export const salesCockpitService = new SalesCockpitService();
