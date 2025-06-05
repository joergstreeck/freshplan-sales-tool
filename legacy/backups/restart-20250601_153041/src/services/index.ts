/**
 * Services barrel export
 * Pure business logic services with no DOM dependencies
 */

export { CalculatorService } from './CalculatorService';
export type { 
  DiscountRulesConfig, 
  CalculationInput, 
  Scenario 
} from './CalculatorService';

export { CustomerService } from './CustomerService';
export type { 
  CustomerValidationRules, 
  IndustryRequirements 
} from './CustomerService';

export { ProfileService } from './ProfileService';
export type { 
  ProfileGenerationOptions, 
  IndustryProfile 
} from './ProfileService';

export { IntegrationService } from './IntegrationService';
export type {
  IntegrationResult,
  MondayItem,
  EmailMessage,
  XentralCustomer
} from './IntegrationService';