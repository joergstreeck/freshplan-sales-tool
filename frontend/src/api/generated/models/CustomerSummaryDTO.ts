/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { Instant } from './Instant';
export type CustomerSummaryDTO = {
  companyName?: string;
  status?: string;
  expectedAnnualVolume?: number;
  locationCount?: number;
  locationNames?: Array<string>;
  primaryContactName?: string;
  primaryContactEmail?: string;
  riskScore?: number;
  lastContactDate?: Instant;
  nextSteps?: Array<string>;
};
