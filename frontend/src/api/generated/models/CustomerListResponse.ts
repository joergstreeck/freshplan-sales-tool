/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { CustomerResponse } from './CustomerResponse';
export type CustomerListResponse = {
  content?: Array<CustomerResponse>;
  page?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
  first?: boolean;
  last?: boolean;
};
