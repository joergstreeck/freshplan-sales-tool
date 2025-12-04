/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { JsonObject } from './JsonObject';
import type { LocalDateTime } from './LocalDateTime';
export type Territory = {
  id?: string;
  name: string;
  countryCode: string;
  currencyCode: string;
  taxRate: number;
  languageCode: string;
  businessRules?: JsonObject;
  active?: boolean;
  createdAt?: LocalDateTime;
  updatedAt?: LocalDateTime;
  germany?: boolean;
  switzerland?: boolean;
  formattedTaxRate?: string;
  paymentTerms?: number;
};
