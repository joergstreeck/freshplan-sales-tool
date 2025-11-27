/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { FilterCriteria } from './FilterCriteria';
/**
 * Smart search request with predefined sorting strategies
 */
export type SmartSearchRequest = {
  /**
   * Global search term
   */
  globalSearch?: string;
  /**
   * Additional filters to apply
   */
  filters?: Array<FilterCriteria>;
  /**
   * Smart sorting strategy (SALES_PRIORITY, RISK_MITIGATION, ENGAGEMENT_FOCUS, REVENUE_POTENTIAL, CONTACT_FREQUENCY)
   */
  strategy?: string;
};
