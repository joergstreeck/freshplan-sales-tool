/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { CustomerCardSchema } from '../models/CustomerCardSchema';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class BranchSchemaService {
  /**
   * Get Branch Schema (2 Steps)
   * @returns CustomerCardSchema Branch Schema
   * @throws ApiError
   */
  public static getApiBranchesSchema(): CancelablePromise<CustomerCardSchema> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/branches/schema',
    });
  }
}
