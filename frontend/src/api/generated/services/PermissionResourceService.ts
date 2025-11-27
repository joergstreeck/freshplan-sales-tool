/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { GrantPermissionRequest } from '../models/GrantPermissionRequest';
import type { RevokePermissionRequest } from '../models/RevokePermissionRequest';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class PermissionResourceService {
  /**
   * Get All Permissions
   * @returns any OK
   * @throws ApiError
   */
  public static getApiPermissions(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/permissions',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Check Permission
   * @returns any OK
   * @throws ApiError
   */
  public static getApiPermissionsCheck({
    permissionCode,
  }: {
    permissionCode: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/permissions/check/{permissionCode}',
      path: {
        permissionCode: permissionCode,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Grant Permission
   * @returns any OK
   * @throws ApiError
   */
  public static postApiPermissionsGrant({
    requestBody,
  }: {
    requestBody: GrantPermissionRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/permissions/grant',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Current User Permissions
   * @returns any OK
   * @throws ApiError
   */
  public static getApiPermissionsMe(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/permissions/me',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Revoke Permission
   * @returns any OK
   * @throws ApiError
   */
  public static postApiPermissionsRevoke({
    requestBody,
  }: {
    requestBody: RevokePermissionRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/permissions/revoke',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get User Permissions
   * @returns any OK
   * @throws ApiError
   */
  public static getApiPermissionsUser({ userId }: { userId: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/permissions/user/{userId}',
      path: {
        userId: userId,
      },
    });
  }
}
