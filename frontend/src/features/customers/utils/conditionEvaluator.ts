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
 * Evaluate Server-Driven visibleWhen condition
 *
 * Sprint 2.1.7.7: Server-Driven Conditional Fields
 *
 * Checks if visibleWhenField has the expected visibleWhenValue.
 * Supports BOOLEAN (true/false as string), ENUM, and TEXT fields.
 *
 * @param visibleWhenField - The field key to check
 * @param visibleWhenValue - The expected value (as string)
 * @param values - Current form values
 * @returns True if field should be visible
 */
export const evaluateVisibleWhen = (
  visibleWhenField: string,
  visibleWhenValue: string,
  values: Record<string, unknown>
): boolean => {
  const currentValue = values[visibleWhenField];

  // Handle BOOLEAN fields: Backend sends "true"/"false" as string
  if (visibleWhenValue === 'true') {
    return currentValue === true || currentValue === 'true' || currentValue === 'ja';
  }
  if (visibleWhenValue === 'false') {
    return (
      currentValue === false || currentValue === 'false' || currentValue === 'nein' || !currentValue
    );
  }

  // Handle ENUM/TEXT fields: Direct string comparison
  return String(currentValue) === visibleWhenValue;
};

/**
 * Get visible fields based on conditions
 *
 * Supports:
 * - Generic field conditions (condition property)
 * - Wizard step filtering (currentStep parameter)
 * - Server-driven visibleWhen conditions (Sprint 2.1.7.7)
 *
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

    // Sprint 2.1.7.7: Server-Driven Conditional Visibility
    // Check visibleWhenField/visibleWhenValue from backend schema
    const fieldWithVisibility = field as Record<string, unknown>;
    const visibleWhenField = fieldWithVisibility.visibleWhenField as string | undefined;
    const visibleWhenValue = fieldWithVisibility.visibleWhenValue as string | undefined;

    if (visibleWhenField && visibleWhenValue !== undefined) {
      if (!evaluateVisibleWhen(visibleWhenField, visibleWhenValue, values)) {
        return false;
      }
    }

    // Check generic field visibility conditions (legacy support)
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
