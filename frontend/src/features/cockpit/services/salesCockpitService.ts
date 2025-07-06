/**
 * Service für Sales Cockpit BFF-Integration
 *
 * Ruft aggregierte Dashboard-Daten vom Backend-for-Frontend ab
 */
import { httpClient } from '../../../lib/apiClient';
import type { SalesCockpitDashboard } from '../types/salesCockpit';

class SalesCockpitService {
  /**
   * Lädt alle Dashboard-Daten für einen bestimmten Benutzer
   *
   * @param userId Die ID des Benutzers
   * @returns Aggregierte Dashboard-Daten
   */
  async getDashboardData(userId: string): Promise<SalesCockpitDashboard> {
    const response = await httpClient.get<SalesCockpitDashboard>(
      `/api/sales-cockpit/dashboard/${userId}`
    );
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
