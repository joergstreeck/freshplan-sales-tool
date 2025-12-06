/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AddressDto } from './AddressDto';
import type { BusinessType } from './BusinessType';
import type { ContactDto } from './ContactDto';
import type { CustomerStatus } from './CustomerStatus';
import type { CustomerType } from './CustomerType';
export type CreateBranchRequest = {
  companyName: string;
  tradingName?: string;
  customerType?: CustomerType;
  businessType?: BusinessType;
  status?: CustomerStatus;
  expectedAnnualVolume?: number;
  address?: AddressDto;
  contact?: ContactDto;
};
