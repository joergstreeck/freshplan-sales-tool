/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AuditSource } from '../models/AuditSource';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class AuditExportService {
  /**
   * Export audit trail in specified format
   * @returns any OK
   * @throws ApiError
   */
  public static getApiV2ExportAudit({
    format,
    entityId,
    entityType,
    eventType,
    from,
    searchText,
    source,
    to,
    userId,
  }: {
    format: string;
    entityId?: UUID;
    entityType?: string;
    eventType?: any[] | string;
    from?: string;
    searchText?: string;
    source?: Array<AuditSource>;
    to?: string;
    userId?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/v2/export/audit/{format}',
      path: {
        format: format,
      },
      query: {
        entityId: entityId,
        entityType: entityType,
        eventType: eventType,
        from: from,
        searchText: searchText,
        source: source,
        to: to,
        userId: userId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
