/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BusinessType } from './BusinessType';
import type { Classification } from './Classification';
import type { CustomerHierarchyType } from './CustomerHierarchyType';
import type { CustomerLifecycleStage } from './CustomerLifecycleStage';
import type { CustomerStatus } from './CustomerStatus';
import type { CustomerType } from './CustomerType';
import type { DeliveryCondition } from './DeliveryCondition';
import type { LocalDateTime } from './LocalDateTime';
import type { PaymentTerms } from './PaymentTerms';
export type UpdateCustomerRequest = {
  companyName?: string;
  tradingName?: string;
  legalForm?: string;
  customerType?: CustomerType;
  businessType?: BusinessType;
  classification?: Classification;
  parentCustomerId?: string;
  hierarchyType?: CustomerHierarchyType;
  status?: CustomerStatus;
  lifecycleStage?: CustomerLifecycleStage;
  expectedAnnualVolume?: number;
  actualAnnualVolume?: number;
  paymentTerms?: PaymentTerms;
  creditLimit?: number;
  deliveryCondition?: DeliveryCondition;
  lastContactDate?: LocalDateTime;
  nextFollowUpDate?: LocalDateTime;
  churnThresholdDays?: number;
};
