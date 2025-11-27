/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { FilterCriteria } from './FilterCriteria';
import type { LogicalOperator } from './LogicalOperator';
import type { SortCriteria } from './SortCriteria';
export type CustomerSearchRequest = {
  globalSearch?: string;
  filters?: Array<FilterCriteria>;
  sort?: SortCriteria;
  multiSort?: Array<SortCriteria>;
  logicalOperator?: LogicalOperator;
};
