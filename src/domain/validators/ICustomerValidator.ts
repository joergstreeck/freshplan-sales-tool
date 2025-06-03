/**
 * Validator interface for customer data validation
 */

import type { CustomerData, ValidationResult } from '../../types';

export interface ICustomerValidator {
  validate(data: CustomerData): Promise<ValidationResult>;
  validateRequiredFields(data: CustomerData): ValidationResult;
  validateBusinessRules(data: CustomerData): ValidationResult;
}