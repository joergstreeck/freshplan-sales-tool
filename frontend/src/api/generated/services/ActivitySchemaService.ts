/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { CustomerCardSchema } from '../models/CustomerCardSchema';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ActivitySchemaService {
  /**
   * Get Activity Schema (Activity Management Dialog)
   * @returns CustomerCardSchema Activity Schema
   * @throws ApiError
   */
  public static getApiActivitiesSchema(): CancelablePromise<CustomerCardSchema> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/activities/schema',
    });
  }
}
