/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CustomerCardSchema } from '../models/CustomerCardSchema';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class BusinessPotentialSchemaService {
  /**
   * Get Business Potential Schema (Assessment Dialog)
   * @returns CustomerCardSchema Business Potential Schema
   * @throws ApiError
   */
  public static getApiBusinessPotentialsSchema(): CancelablePromise<CustomerCardSchema> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/business-potentials/schema',
    });
  }
}
