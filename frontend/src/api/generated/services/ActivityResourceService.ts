/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateActivityRequest } from '../models/CreateActivityRequest';
import type { UpdateActivityRequest } from '../models/UpdateActivityRequest';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ActivityResourceService {
  /**
   * Get Customer Activities
   * @returns any OK
   * @throws ApiError
   */
  public static getApiActivitiesCustomer({
    customerId,
  }: {
    customerId: UUID;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/activities/customer/{customerId}',
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
   * Create Customer Activity
   * @returns any OK
   * @throws ApiError
   */
  public static postApiActivitiesCustomer({
    customerId,
    requestBody,
  }: {
    customerId: UUID;
    requestBody: CreateActivityRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/activities/customer/{customerId}',
      path: {
        customerId: customerId,
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
   * Get Lead Activities
   * @returns any OK
   * @throws ApiError
   */
  public static getApiActivitiesLead({ leadId }: { leadId: number }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/activities/lead/{leadId}',
      path: {
        leadId: leadId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Create Lead Activity
   * @returns any OK
   * @throws ApiError
   */
  public static postApiActivitiesLead({
    leadId,
    requestBody,
  }: {
    leadId: number;
    requestBody: CreateActivityRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/activities/lead/{leadId}',
      path: {
        leadId: leadId,
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
   * Update Activity
   * @returns any OK
   * @throws ApiError
   */
  public static putApiActivities({
    activityId,
    requestBody,
  }: {
    activityId: UUID;
    requestBody: UpdateActivityRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/activities/{activityId}',
      path: {
        activityId: activityId,
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
   * Get Activity
   * @returns any OK
   * @throws ApiError
   */
  public static getApiActivities({ activityId }: { activityId: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/activities/{activityId}',
      path: {
        activityId: activityId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Delete Activity
   * @returns any OK
   * @throws ApiError
   */
  public static deleteApiActivities({ activityId }: { activityId: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/activities/{activityId}',
      path: {
        activityId: activityId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
