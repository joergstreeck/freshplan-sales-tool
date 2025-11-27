/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { CustomerCardSchema } from '../models/CustomerCardSchema';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class CustomerSchemaService {
  /**
   * Get Customer Card Schema (7 Cards)
   * @returns CustomerCardSchema Customer Card Schema
   * @throws ApiError
   */
  public static getApiCustomersSchema(): CancelablePromise<CustomerCardSchema> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/schema',
    });
  }
}
