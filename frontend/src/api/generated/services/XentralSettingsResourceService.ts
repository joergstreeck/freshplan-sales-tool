/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { XentralSettingsDTO } from '../models/XentralSettingsDTO';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class XentralSettingsResourceService {
  /**
   * Update Settings
   * @returns any OK
   * @throws ApiError
   */
  public static putApiAdminXentralSettings({
    requestBody,
  }: {
    requestBody: XentralSettingsDTO;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/admin/xentral/settings',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Settings
   * @returns any OK
   * @throws ApiError
   */
  public static getApiAdminXentralSettings(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/admin/xentral/settings',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Test Connection
   * @returns any OK
   * @throws ApiError
   */
  public static getApiAdminXentralTestConnection(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/admin/xentral/test-connection',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
