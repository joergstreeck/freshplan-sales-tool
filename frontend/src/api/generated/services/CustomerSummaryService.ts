/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { CustomerSummaryDTO } from '../models/CustomerSummaryDTO';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class CustomerSummaryService {
  /**
   * Get customer summary for compact view
   * @returns CustomerSummaryDTO Customer summary retrieved successfully
   * @throws ApiError
   */
  public static getApiCustomersSummary({
    id,
  }: {
    /**
     * Customer ID
     */
    id: UUID;
  }): CancelablePromise<CustomerSummaryDTO> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{id}/summary',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Customer not found`,
      },
    });
  }
}
