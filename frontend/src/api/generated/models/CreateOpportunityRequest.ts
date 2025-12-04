/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LocalDate } from './LocalDate';
import type { OpportunityType } from './OpportunityType';
import type { UUID } from './UUID';
export type CreateOpportunityRequest = {
  name: string;
  description?: string;
  opportunityType?: OpportunityType;
  customerId?: UUID;
  assignedTo?: UUID;
  expectedValue?: number;
  expectedCloseDate?: LocalDate;
};
