/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { OpportunityMultiplierResponse } from '../models/OpportunityMultiplierResponse';
import type { SettingCreateDto } from '../models/SettingCreateDto';
import type { SettingDto } from '../models/SettingDto';
import type { SettingsScope } from '../models/SettingsScope';
import type { SettingUpdateDto } from '../models/SettingUpdateDto';
import type { UpdateMultiplierRequest } from '../models/UpdateMultiplierRequest';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class SettingsService {
  /**
   * Get a setting
   * Retrieves a setting by scope and key with ETag support
   * @returns SettingDto Setting found
   * @throws ApiError
   */
  public static getApiSettings({
    key,
    scope,
    scopeId,
    ifNoneMatch,
  }: {
    /**
     * Setting key
     */
    key: string;
    /**
     * Setting scope
     */
    scope: SettingsScope;
    /**
     * Scope identifier (e.g., tenant ID, territory code)
     */
    scopeId?: string;
    /**
     * ETag for conditional request
     */
    ifNoneMatch?: string;
  }): CancelablePromise<SettingDto> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/settings',
      headers: {
        'If-None-Match': ifNoneMatch,
      },
      query: {
        key: key,
        scope: scope,
        scopeId: scopeId,
      },
      errors: {
        304: `Not Modified (ETag matched)`,
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Setting not found`,
      },
    });
  }
  /**
   * Create a setting
   * Creates a new setting
   * @returns any Setting created
   * @throws ApiError
   */
  public static postApiSettings({
    requestBody,
  }: {
    requestBody: SettingCreateDto;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/settings',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        409: `Setting already exists`,
      },
    });
  }
  /**
   * List settings
   * Lists all settings for a given scope
   * @returns any OK
   * @throws ApiError
   */
  public static getApiSettingsList({
    scope,
    scopeId,
  }: {
    scope: SettingsScope;
    scopeId?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/settings/list',
      query: {
        scope: scope,
        scopeId: scopeId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get all opportunity multipliers
   * Returns Business-Type-Matrix for intelligent opportunity value estimation (36 entries)
   * @returns OpportunityMultiplierResponse Multipliers loaded successfully
   * @throws ApiError
   */
  public static getApiSettingsOpportunityMultipliers(): CancelablePromise<OpportunityMultiplierResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/settings/opportunity-multipliers',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Update Multiplier
   * @returns any OK
   * @throws ApiError
   */
  public static putApiSettingsOpportunityMultipliers({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: UpdateMultiplierRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/settings/opportunity-multipliers/{id}',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Resolve setting hierarchically
   * Resolves a setting using the scope hierarchy
   * @returns any OK
   * @throws ApiError
   */
  public static getApiSettingsResolve({
    key,
    accountId,
    contactRole,
    tenantId,
    territory,
    ifNoneMatch,
  }: {
    key: string;
    accountId?: string;
    contactRole?: string;
    tenantId?: string;
    territory?: string;
    ifNoneMatch?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/settings/resolve/{key}',
      path: {
        key: key,
      },
      headers: {
        'If-None-Match': ifNoneMatch,
      },
      query: {
        accountId: accountId,
        contactRole: contactRole,
        tenantId: tenantId,
        territory: territory,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get cache statistics
   * Returns cache performance metrics
   * @returns any OK
   * @throws ApiError
   */
  public static getApiSettingsStatsCache(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/settings/stats/cache',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Update a setting
   * Updates a setting with optimistic locking
   * @returns any Setting updated
   * @throws ApiError
   */
  public static putApiSettings({
    id,
    requestBody,
    ifMatch,
  }: {
    id: UUID;
    requestBody: SettingUpdateDto;
    /**
     * ETag for optimistic locking
     */
    ifMatch?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/settings/{id}',
      path: {
        id: id,
      },
      headers: {
        'If-Match': ifMatch,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Setting not found`,
        409: `Conflict (concurrent modification)`,
        412: `Precondition Failed (ETag mismatch)`,
        428: `Precondition Required (If-Match header missing)`,
      },
    });
  }
  /**
   * Get setting by ID
   * Retrieves a specific setting by its ID
   * @returns any Setting found
   * @throws ApiError
   */
  public static getApiSettings1({
    id,
    ifNoneMatch,
  }: {
    id: UUID;
    ifNoneMatch?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/settings/{id}',
      path: {
        id: id,
      },
      headers: {
        'If-None-Match': ifNoneMatch,
      },
      errors: {
        304: `Not Modified (ETag matched)`,
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Setting not found`,
      },
    });
  }
  /**
   * Delete a setting
   * Deletes a setting by ID
   * @returns any OK
   * @throws ApiError
   */
  public static deleteApiSettings({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/settings/{id}',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
