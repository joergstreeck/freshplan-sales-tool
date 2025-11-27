/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { ActivityImportData } from './ActivityImportData';
import type { LocalDateTime } from './LocalDateTime';
export type LeadImportData = {
  companyName: string;
  contactPerson?: string;
  email?: string;
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
  registeredAt: LocalDateTime;
  ownerUserId?: string;
  territoryCode?: string;
  activities?: Array<ActivityImportData>;
  source?: string;
  sourceCampaign?: string;
  importReason: string;
};
