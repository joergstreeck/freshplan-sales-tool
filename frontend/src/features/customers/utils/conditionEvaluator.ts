/**
 * Condition Evaluator Utility
 * 
 * Evaluates field visibility conditions based on other field values.
 * Supports the triggerWizardStep logic from Field Catalog.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/03-field-rendering.md
 */

import { TriggerCondition } from '../types/field.types';

/**
 * Evaluate a single condition
 */
export const evaluateCondition = (
  condition: TriggerCondition,
  values: Record<string, any>
): boolean => {
  // Handle array of trigger values
  if (Array.isArray(condition.when)) {
    return condition.when.some(value => values[condition.step] === value);
  }
  
  // Handle single trigger value
  return values[condition.step] === condition.when;
};

/**
 * Check if a wizard step should be shown
 */
export const shouldShowWizardStep = (
  step: string,
  fieldDefinitions: any[],
  values: Record<string, any>
): boolean => {
  // Find fields that trigger this step
  const triggerFields = fieldDefinitions.filter(
    field => field.triggerWizardStep?.step === step
  );
  
  // If no trigger fields, always show
  if (triggerFields.length === 0) return true;
  
  // Check if any trigger condition is met
  return triggerFields.some(field => {
    const fieldValue = values[field.key];
    const trigger = field.triggerWizardStep;
    
    if (!trigger) return false;
    
    if (Array.isArray(trigger.when)) {
      return trigger.when.includes(fieldValue);
    }
    
    return fieldValue === trigger.when;
  });
};

/**
 * Get visible fields based on conditions
 */
export const getVisibleFields = (
  fields: any[],
  values: Record<string, any>,
  currentStep?: string
): any[] => {
  return fields.filter(field => {
    // Filter by current wizard step if provided
    if (currentStep && field.wizardStep && field.wizardStep !== currentStep) {
      return false;
    }
    
    // Check field conditions
    if (field.condition) {
      return evaluateCondition(field.condition, values);
    }
    
    return true;
  });
};

/**
 * Get required fields that are currently visible
 */
export const getRequiredFields = (
  fields: any[],
  values: Record<string, any>
): string[] => {
  const visibleFields = getVisibleFields(fields, values);
  return visibleFields
    .filter(field => field.required)
    .map(field => field.key);
};