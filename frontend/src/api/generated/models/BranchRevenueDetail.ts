/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CustomerStatus } from './CustomerStatus';
import type { UUID } from './UUID';
export type BranchRevenueDetail = {
  branchId?: UUID;
  branchName?: string;
  city?: string;
  country?: string;
  revenue?: number;
  percentage?: number;
  openOpportunities?: number;
  status?: CustomerStatus;
};
