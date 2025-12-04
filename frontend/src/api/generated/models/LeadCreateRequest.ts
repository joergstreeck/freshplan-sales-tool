/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ActivityData } from './ActivityData';
import type { ContactData } from './ContactData';
export type LeadCreateRequest = {
  companyName: string;
  stage?: number;
  contact?: ContactData;
  /**
   * @deprecated
   */
  contactPerson?: string;
  /**
   * @deprecated
   */
  email?: string;
  /**
   * @deprecated
   */
  phone?: string;
  website?: string;
  street?: string;
  postalCode?: string;
  city?: string;
  countryCode?: string;
  businessType?: string;
  kitchenSize?: string;
  employeeCount?: number;
  estimatedVolume?: number;
  industry?: string;
  source?: string;
  sourceCampaign?: string;
  activities?: Array<ActivityData>;
};
