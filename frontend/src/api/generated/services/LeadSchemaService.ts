/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CustomerCardSchema } from '../models/CustomerCardSchema';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class LeadSchemaService {
  /**
   * Get Lead Schema (Progressive Profiling + Edit Dialog)
   * @returns CustomerCardSchema Lead Schema (2 cards: Progressive Profiling + Edit)
   * @throws ApiError
   */
  public static getApiLeadsSchema(): CancelablePromise<CustomerCardSchema> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/leads/schema',
    });
  }
}
