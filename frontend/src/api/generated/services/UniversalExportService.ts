/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AuditSource } from '../models/AuditSource';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class UniversalExportService {
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
  /**
   * Export customers in specified format
   * @returns any OK
   * @throws ApiError
   */
  public static getApiV2ExportCustomers({
    format,
    includeContacts = true,
    industry,
    status,
  }: {
    format: string;
    includeContacts?: boolean;
    industry?: string;
    status?: Array<string>;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/v2/export/customers/{format}',
      path: {
        format: format,
      },
      query: {
        includeContacts: includeContacts,
        industry: industry,
        status: status,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get list of supported export formats
   * @returns any OK
   * @throws ApiError
   */
  public static getApiV2ExportFormats(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/v2/export/formats',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Export any entity in specified format
   * @returns any OK
   * @throws ApiError
   */
  public static getApiV2Export({
    entity,
    format,
  }: {
    entity: string;
    format: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/v2/export/{entity}/{format}',
      path: {
        entity: entity,
        format: format,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
