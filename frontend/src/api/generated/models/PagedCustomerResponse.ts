/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { CustomerResponse } from './CustomerResponse';
/**
 * Paginated customer search results
 */
export type PagedCustomerResponse = {
  content?: Array<CustomerResponse>;
  page?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
  first?: boolean;
  last?: boolean;
  numberOfElements?: number;
};
