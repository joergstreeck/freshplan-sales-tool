/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { FilterOperator } from './FilterOperator';
import type { LogicalOperator } from './LogicalOperator';
export type FilterCriteria = {
  field: string;
  operator: FilterOperator;
  value?: any;
  combineWith?: LogicalOperator;
};
