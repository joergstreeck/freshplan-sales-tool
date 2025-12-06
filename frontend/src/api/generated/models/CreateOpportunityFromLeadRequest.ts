/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LocalDate } from './LocalDate';
import type { OpportunityType } from './OpportunityType';
import type { UUID } from './UUID';
export type CreateOpportunityFromLeadRequest = {
  name?: string;
  description?: string;
  opportunityType?: OpportunityType;
  dealType?: string;
  timeframe?: string;
  expectedValue?: number;
  expectedCloseDate?: LocalDate;
  assignedTo?: UUID;
};
