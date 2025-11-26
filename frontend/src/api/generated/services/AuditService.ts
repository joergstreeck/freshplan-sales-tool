/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AuditEntry } from '../models/AuditEntry';
import type { AuditEventType } from '../models/AuditEventType';
import type { AuditSource } from '../models/AuditSource';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class AuditService {
  /**
   * Get activity data for chart visualization
   * @returns any OK
   * @throws ApiError
   */
  public static getApiAuditDashboardActivityChart({
    days = 7,
    groupBy = 'hour',
  }: {
    days?: number;
    groupBy?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/audit/dashboard/activity-chart',
      query: {
        days: days,
        groupBy: groupBy,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get compliance alerts and warnings
   * @returns any OK
   * @throws ApiError
   */
  public static getApiAuditDashboardComplianceAlerts(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/audit/dashboard/compliance-alerts',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get recent critical events
   * @returns any OK
   * @throws ApiError
   */
  public static getApiAuditDashboardCriticalEvents({
    limit = 10,
  }: {
    limit?: number;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/audit/dashboard/critical-events',
      query: {
        limit: limit,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get audit dashboard metrics
   * @returns any OK
   * @throws ApiError
   */
  public static getApiAuditDashboardMetrics(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/audit/dashboard/metrics',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get audit trail for an entity
   * @returns AuditEntry Audit entries for the entity
   * @throws ApiError
   */
  public static getApiAuditEntity({
    entityId,
    entityType,
    page,
    size = 50,
  }: {
    entityId: UUID;
    entityType: string;
    page?: number;
    size?: number;
  }): CancelablePromise<Array<AuditEntry>> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/audit/entity/{entityType}/{entityId}',
      path: {
        entityId: entityId,
        entityType: entityType,
      },
      query: {
        page: page,
        size: size,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * @deprecated
   * Export audit trail data - DEPRECATED
   * @returns any OK
   * @throws ApiError
   */
  public static getApiAuditExport({
    entityType,
    eventType,
    format = 'csv',
    from,
    to,
  }: {
    entityType?: string;
    eventType?: Array<AuditEventType>;
    format?: string;
    from?: string;
    to?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/audit/export',
      query: {
        entityType: entityType,
        eventType: eventType,
        format: format,
        from: from,
        to: to,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get failed operations
   * @returns any OK
   * @throws ApiError
   */
  public static getApiAuditFailures({ hours = 24 }: { hours?: number }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/audit/failures',
      query: {
        hours: hours,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Search audit entries with filters
   * @returns any OK
   * @throws ApiError
   */
  public static getApiAuditSearch({
    entityId,
    entityType,
    eventType,
    from,
    page,
    searchText,
    size = 50,
    source,
    to,
    userId,
  }: {
    entityId?: UUID;
    entityType?: string;
    eventType?: Array<AuditEventType>;
    /**
     * ISO date-time
     */
    from?: string;
    page?: number;
    searchText?: string;
    size?: number;
    source?: Array<AuditSource>;
    /**
     * ISO date-time
     */
    to?: string;
    userId?: UUID;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/audit/search',
      query: {
        entityId: entityId,
        entityType: entityType,
        eventType: eventType,
        from: from,
        page: page,
        searchText: searchText,
        size: size,
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
   * Get security-relevant audit events
   * @returns any OK
   * @throws ApiError
   */
  public static getApiAuditSecurityEvents({
    hours = 24,
  }: {
    hours?: number;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/audit/security-events',
      query: {
        hours: hours,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get audit statistics for a time period
   * @returns any OK
   * @throws ApiError
   */
  public static getApiAuditStatistics({
    from,
    to,
  }: {
    /**
     * ISO date-time
     */
    from?: string;
    /**
     * ISO date-time
     */
    to?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/audit/statistics',
      query: {
        from: from,
        to: to,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Verify audit trail integrity
   * @returns any OK
   * @throws ApiError
   */
  public static postApiAuditVerifyIntegrity({
    from,
    to,
  }: {
    /**
     * ISO date-time
     */
    from?: string;
    /**
     * ISO date-time
     */
    to?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/audit/verify-integrity',
      query: {
        from: from,
        to: to,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
