/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { CustomerCardSchema } from '../models/CustomerCardSchema';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ContactSchemaService {
  /**
   * Get Contact Schema (3 Sections)
   * @returns CustomerCardSchema Contact Schema
   * @throws ApiError
   */
  public static getApiContactsSchema(): CancelablePromise<CustomerCardSchema> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/contacts/schema',
    });
  }
}
