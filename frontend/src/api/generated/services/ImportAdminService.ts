/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ImportLogDTO } from '../models/ImportLogDTO';
import type { ImportStatsResponse } from '../models/ImportStatsResponse';
import type { RejectRequest } from '../models/RejectRequest';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ImportAdminService {
  /**
   * Alle Imports
   * Listet alle Lead-Imports auf.
   * @returns ImportLogDTO OK
   * @throws ApiError
   */
  public static getApiAdminImports({
    limit = 100,
    status,
  }: {
    limit?: number;
    status?: string;
  }): CancelablePromise<Array<ImportLogDTO>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/admin/imports',
      query: {
        limit: limit,
        status: status,
      },
    });
  }
  /**
   * Pending Approvals
   * Listet alle Imports auf, die auf Genehmigung warten (>10% Duplikate).
   * @returns ImportLogDTO OK
   * @throws ApiError
   */
  public static getApiAdminImportsPending(): CancelablePromise<Array<ImportLogDTO>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/admin/imports/pending',
    });
  }
  /**
   * Import-Statistiken
   * Liefert Statistiken für das Import-Dashboard.
   * @returns ImportStatsResponse OK
   * @throws ApiError
   */
  public static getApiAdminImportsStats(): CancelablePromise<ImportStatsResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/admin/imports/stats',
    });
  }
  /**
   * Import genehmigen
   * Genehmigt einen wartenden Import.
   * @returns any OK
   * @throws ApiError
   */
  public static postApiAdminImportsApprove({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/admin/imports/{id}/approve',
      path: {
        id: id,
      },
    });
  }
  /**
   * Import ablehnen
   * Lehnt einen wartenden Import ab.
   * @returns any OK
   * @throws ApiError
   */
  public static postApiAdminImportsReject({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: RejectRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/admin/imports/{id}/reject',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
    });
  }
}
