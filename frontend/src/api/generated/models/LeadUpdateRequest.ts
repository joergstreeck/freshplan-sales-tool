/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LeadStatus } from './LeadStatus';
export type LeadUpdateRequest = {
  companyName?: string;
  contactPerson?: string;
  email?: string;
  phone?: string;
  website?: string;
  street?: string;
  postalCode?: string;
  city?: string;
  businessType?: string;
  kitchenSize?: string;
  employeeCount?: number;
  estimatedVolume?: number;
  budgetConfirmed?: boolean;
  dealSize?: string;
  status?: LeadStatus;
  stopClock?: boolean;
  stopReason?: string;
  addCollaborators?: Array<string>;
  removeCollaborators?: Array<string>;
  relationshipStatus?: string;
  decisionMakerAccess?: string;
  competitorInUse?: string;
  internalChampionName?: string;
  painStaffShortage?: boolean;
  painHighCosts?: boolean;
  painFoodWaste?: boolean;
  painQualityInconsistency?: boolean;
  painUnreliableDelivery?: boolean;
  painPoorService?: boolean;
  painSupplierQuality?: boolean;
  painTimePressure?: boolean;
  urgencyLevel?: string;
  multiPainBonus?: number;
  painNotes?: string;
};
