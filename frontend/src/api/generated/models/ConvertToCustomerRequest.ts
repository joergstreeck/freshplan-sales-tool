/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { UUID } from './UUID';
export type ConvertToCustomerRequest = {
  companyName?: string;
  street?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  createContactFromLead?: boolean;
  hierarchyType?: string;
  parentCustomerId?: UUID;
};
