/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { LocalDate } from './LocalDate';
import type { UUID } from './UUID';
export type UpdateOpportunityRequest = {
  name?: string;
  description?: string;
  customerId?: UUID;
  expectedValue?: number;
  expectedCloseDate?: LocalDate;
  probability?: number;
};
