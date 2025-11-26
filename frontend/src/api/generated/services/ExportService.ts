/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ExportOptions } from '../models/ExportOptions';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ExportService {
  /**
   * Export audit trail as CSV
   * @returns any OK
   * @throws ApiError
   */
  public static getApiExportAuditCsv({
    entityId,
    entityType,
    eventType,
    from,
    to,
    userId,
  }: {
    entityId?: UUID;
    entityType?: string;
    eventType?: string;
    from?: string;
    to?: string;
    userId?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/export/audit/csv',
      query: {
        entityId: entityId,
        entityType: entityType,
        eventType: eventType,
        from: from,
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
   * Export audit trail as Excel workbook
   * @returns any OK
   * @throws ApiError
   */
  public static getApiExportAuditExcel({
    entityId,
    entityType,
    from,
    groupBy = 'day',
    to,
  }: {
    entityId?: UUID;
    entityType?: string;
    from?: string;
    groupBy?: string;
    to?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/export/audit/excel',
      query: {
        entityId: entityId,
        entityType: entityType,
        from: from,
        groupBy: groupBy,
        to: to,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Export audit trail as JSON
   * @returns any OK
   * @throws ApiError
   */
  public static getApiExportAuditJson({
    entityId,
    entityType,
    from,
    page,
    size = 1000,
    to,
  }: {
    entityId?: UUID;
    entityType?: string;
    from?: string;
    page?: number;
    size?: number;
    to?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/export/audit/json',
      query: {
        entityId: entityId,
        entityType: entityType,
        from: from,
        page: page,
        size: size,
        to: to,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Export audit trail as HTML report (printable to PDF)
   * @returns any OK
   * @throws ApiError
   */
  public static getApiExportAuditPdf({
    entityId,
    entityType,
    from,
    includeDetails = true,
    to,
  }: {
    entityId?: UUID;
    entityType?: string;
    from?: string;
    includeDetails?: boolean;
    to?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/export/audit/pdf',
      query: {
        entityId: entityId,
        entityType: entityType,
        from: from,
        includeDetails: includeDetails,
        to: to,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Generate compliance report HTML (printable to PDF)
   * @returns any OK
   * @throws ApiError
   */
  public static postApiExportCompliancePdf({
    requestBody,
  }: {
    requestBody: ExportOptions;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/export/compliance/pdf',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Export customers and contacts as CSV
   * @returns any OK
   * @throws ApiError
   */
  public static getApiExportCustomersCsv({
    includeContacts = true,
    industry,
    status,
  }: {
    includeContacts?: boolean;
    industry?: string;
    status?: Array<string>;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/export/customers/csv',
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
   * Export customers as Excel workbook
   * @returns any OK
   * @throws ApiError
   */
  public static getApiExportCustomersExcel({
    businessType,
    includeContacts = true,
    includeStats = true,
    status,
  }: {
    businessType?: string;
    includeContacts?: boolean;
    includeStats?: boolean;
    status?: Array<string>;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/export/customers/excel',
      query: {
        businessType: businessType,
        includeContacts: includeContacts,
        includeStats: includeStats,
        status: status,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Export customers as JSON
   * @returns any OK
   * @throws ApiError
   */
  public static getApiExportCustomersJson({
    businessType,
    includeContacts = true,
    page,
    size = 1000,
    status,
  }: {
    businessType?: string;
    includeContacts?: boolean;
    page?: number;
    size?: number;
    status?: Array<string>;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/export/customers/json',
      query: {
        businessType: businessType,
        includeContacts: includeContacts,
        page: page,
        size: size,
        status: status,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Export customers as HTML report (printable to PDF)
   * @returns any OK
   * @throws ApiError
   */
  public static getApiExportCustomersPdf({
    businessType,
    format = 'list',
    status,
  }: {
    businessType?: string;
    format?: string;
    status?: Array<string>;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/export/customers/pdf',
      query: {
        businessType: businessType,
        format: format,
        status: status,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
