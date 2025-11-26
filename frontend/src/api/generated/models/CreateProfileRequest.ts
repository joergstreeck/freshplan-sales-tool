/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { CompanyInfo } from './CompanyInfo';
import type { ContactInfo } from './ContactInfo';
import type { FinancialInfo } from './FinancialInfo';
export type CreateProfileRequest = {
  customerId: string;
  companyInfo?: CompanyInfo;
  contactInfo?: ContactInfo;
  financialInfo?: FinancialInfo;
  notes?: string;
};
