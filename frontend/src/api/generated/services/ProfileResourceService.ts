/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateProfileRequest } from '../models/CreateProfileRequest';
import type { UpdateProfileRequest } from '../models/UpdateProfileRequest';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ProfileResourceService {
  /**
   * Get All Profiles
   * @returns any OK
   * @throws ApiError
   */
  public static getApiProfiles(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/profiles',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Create Profile
   * @returns any OK
   * @throws ApiError
   */
  public static postApiProfiles({
    requestBody,
  }: {
    requestBody: CreateProfileRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/profiles',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Profile By Customer Id
   * @returns any OK
   * @throws ApiError
   */
  public static getApiProfilesCustomer({
    customerId,
  }: {
    customerId: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/profiles/customer/{customerId}',
      path: {
        customerId: customerId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Check Profile Exists
   * @returns any OK
   * @throws ApiError
   */
  public static headApiProfilesCustomer({
    customerId,
  }: {
    customerId: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'HEAD',
      url: '/api/profiles/customer/{customerId}',
      path: {
        customerId: customerId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Update Profile
   * @returns any OK
   * @throws ApiError
   */
  public static putApiProfiles({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: UpdateProfileRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/profiles/{id}',
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
   * Get Profile
   * @returns any OK
   * @throws ApiError
   */
  public static getApiProfiles1({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/profiles/{id}',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Delete Profile
   * @returns any OK
   * @throws ApiError
   */
  public static deleteApiProfiles({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/profiles/{id}',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Export Profile Pdf
   * @returns any OK
   * @throws ApiError
   */
  public static getApiProfilesExportPdf({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/profiles/{id}/export/pdf',
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
