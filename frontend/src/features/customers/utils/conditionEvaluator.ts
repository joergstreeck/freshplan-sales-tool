/**
 * Condition Evaluator Utility
 *
 * Evaluates field visibility conditions based on other field values.
 * Supports both triggerWizardStep and generic condition logic from Field Catalog.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/03-field-rendering.md
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json
 */

import type { TriggerCondition, FieldCondition, FieldDefinition } from '../types/field.types';

/**
 * Evaluate a trigger condition (for wizard steps)
 */
export const evaluateTriggerCondition = (
  condition: TriggerCondition,
  values: Record<string, unknown>
): boolean => {
  // Handle array of trigger values
  if (Array.isArray(condition.when)) {
    return condition.when.some(value => values[condition.step] === value);
  }

  // Handle single trigger value
  return values[condition.step] === condition.when;
};

/**
 * Evaluate a generic field condition
 */
export const evaluateFieldCondition = (
  condition: FieldCondition,
  values: Record<string, unknown>
): boolean => {
  const fieldValue = values[condition.field];

  switch (condition.operator) {
    case 'equals':
      return fieldValue === condition.value;

    case 'not_equals':
      return fieldValue !== condition.value;

    case 'in':
      if (Array.isArray(condition.value)) {
        return condition.value.includes(fieldValue);
      }
      return fieldValue === condition.value;

    case 'not_in':
      if (Array.isArray(condition.value)) {
        return !condition.value.includes(fieldValue);
      }
      return fieldValue !== condition.value;

    case 'exists':
      return fieldValue !== undefined && fieldValue !== null && fieldValue !== '';

    case 'not_exists':
      return fieldValue === undefined || fieldValue === null || fieldValue === '';

    default:
      console.warn(`Unknown condition operator: ${condition.operator}`);
      return true;
  }
};

/**
 * Evaluate any condition (backwards compatibility)
 */
export const evaluateCondition = (
  condition: TriggerCondition | FieldCondition,
  values: Record<string, unknown>
): boolean => {
  // Check if it's a TriggerCondition (has 'when' and 'step' properties)
  if ('when' in condition && 'step' in condition) {
    return evaluateTriggerCondition(condition as TriggerCondition, values);
  }

  // Otherwise treat as FieldCondition
  return evaluateFieldCondition(condition as FieldCondition, values);
};

/**
 * Check if a wizard step should be shown
 */
export const shouldShowWizardStep = (
  step: string,
  fieldDefinitions: FieldDefinition[],
  values: Record<string, unknown>
): boolean => {
  // Find fields that trigger this step
  const triggerFields = fieldDefinitions.filter(field => field.triggerWizardStep?.step === step);

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
 *
 * Supports both generic field conditions and wizard step filtering.
 * This is the main function used by DynamicFieldRenderer.
 */
export const getVisibleFields = (
  fields: FieldDefinition[],
  values: Record<string, unknown>,
  currentStep?: string
): FieldDefinition[] => {
  return fields.filter(field => {
    // Filter by current wizard step if provided
    if (currentStep && field.wizardStep && field.wizardStep !== currentStep) {
      return false;
    }

    // Check generic field visibility conditions
    if (field.condition) {
      return evaluateFieldCondition(field.condition, values);
    }

    // Always show fields without conditions
    return true;
  });
};

/**
 * Get required fields that are currently visible
 */
export const getRequiredFields = (
  fields: FieldDefinition[],
  values: Record<string, unknown>
): string[] => {
  const visibleFields = getVisibleFields(fields, values);
  return visibleFields.filter(field => field.required).map(field => field.key);
};
