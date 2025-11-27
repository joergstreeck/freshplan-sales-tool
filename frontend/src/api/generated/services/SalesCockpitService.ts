/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { SalesCockpitDashboard } from '../models/SalesCockpitDashboard';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class SalesCockpitService {
  /**
   * Lädt Development Dashboard-Daten
   * Gibt Mock-Daten für die Entwicklungsumgebung zurück
   * @returns SalesCockpitDashboard Development Dashboard-Daten erfolgreich geladen
   * @throws ApiError
   */
  public static getApiSalesCockpitDashboardDev(): CancelablePromise<SalesCockpitDashboard> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/sales-cockpit/dashboard/dev',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        500: `Interner Serverfehler`,
      },
    });
  }
  /**
   * Lädt Dashboard-Daten
   * Aggregiert alle für die Cockpit-Startansicht notwendigen Daten
   * @returns SalesCockpitDashboard Dashboard-Daten erfolgreich geladen
   * @throws ApiError
   */
  public static getApiSalesCockpitDashboard({
    userId,
  }: {
    /**
     * ID des Benutzers
     */
    userId: string;
  }): CancelablePromise<SalesCockpitDashboard> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/sales-cockpit/dashboard/{userId}',
      path: {
        userId: userId,
      },
      errors: {
        400: `Ungültige Benutzer-ID`,
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Benutzer nicht gefunden`,
        500: `Interner Serverfehler`,
      },
    });
  }
  /**
   * Health Check
   * Prüft den Status des Sales Cockpit Service
   * @returns any Service ist verfügbar
   * @throws ApiError
   */
  public static getApiSalesCockpitHealth(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/sales-cockpit/health',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
