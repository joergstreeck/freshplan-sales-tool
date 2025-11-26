/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { CustomerCardSchema } from '../models/CustomerCardSchema';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class ScoreSchemaService {
  /**
   * Get Score Schemas (Pain, Revenue, Engagement)
   * @returns CustomerCardSchema Score Schemas (3 separate forms)
   * @throws ApiError
   */
  public static getApiScoresSchema(): CancelablePromise<CustomerCardSchema> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/scores/schema',
    });
  }
}
