/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { LocalDate } from './LocalDate';
import type { OpportunityStage } from './OpportunityStage';
import type { UUID } from './UUID';
export type CreateOpportunityForCustomerRequest = {
  name?: string;
  description?: string;
  stage?: OpportunityStage;
  opportunityType?: string;
  timeframe?: string;
  expectedValue?: number;
  expectedCloseDate?: LocalDate;
  assignedTo?: UUID;
};
