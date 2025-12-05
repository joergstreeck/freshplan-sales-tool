/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DeletedLeadDTO } from '../models/DeletedLeadDTO';
import type { GdprDataRequestDTO1 } from '../models/GdprDataRequestDTO1';
import type { GdprDeletionLogDTO1 } from '../models/GdprDeletionLogDTO1';
import type { GdprStatsResponse } from '../models/GdprStatsResponse';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class GdprAdminService {
  /**
   * Alle Datenexport-Anfragen
   * Listet alle Art. 15 Datenauskunfts-Anfragen auf.
   * @returns GdprDataRequestDTO1 OK
   * @throws ApiError
   */
  public static getApiAdminGdprDataRequests({
    limit = 100,
    pending = false,
  }: {
    limit?: number;
    pending?: boolean;
  }): CancelablePromise<Array<GdprDataRequestDTO1>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/admin/gdpr/data-requests',
      query: {
        limit: limit,
        pending: pending,
      },
    });
  }
  /**
   * Alle DSGVO-gelöschten Leads
   * Listet alle Leads auf, die gemäß Art. 17 DSGVO anonymisiert wurden.
   * @returns DeletedLeadDTO OK
   * @throws ApiError
   */
  public static getApiAdminGdprDeletedLeads({
    limit = 100,
  }: {
    limit?: number;
  }): CancelablePromise<Array<DeletedLeadDTO>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/admin/gdpr/deleted-leads',
      query: {
        limit: limit,
      },
    });
  }
  /**
   * Alle DSGVO-Löschungen
   * Listet alle Art. 17 DSGVO-Löschprotokolle auf.
   * @returns GdprDeletionLogDTO1 OK
   * @throws ApiError
   */
  public static getApiAdminGdprDeletions({
    from,
    limit = 100,
    to,
  }: {
    from?: string;
    limit?: number;
    to?: string;
  }): CancelablePromise<Array<GdprDeletionLogDTO1>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/admin/gdpr/deletions',
      query: {
        from: from,
        limit: limit,
        to: to,
      },
    });
  }
  /**
   * DSGVO-Statistiken
   * Liefert Statistiken für das DSGVO-Dashboard (Löschungen, Anfragen etc.)
   * @returns GdprStatsResponse OK
   * @throws ApiError
   */
  public static getApiAdminGdprStats(): CancelablePromise<GdprStatsResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/admin/gdpr/stats',
    });
  }
}
