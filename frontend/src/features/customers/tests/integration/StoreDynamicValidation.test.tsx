/**
 * FC-005 CR-002 Store Dynamic Validation Integration Test
 *
 * Testet die Integration des Dynamic Zod Schema Builders in den Store.
 * Zeigt das Zusammenspiel zwischen Field Catalog, Dynamic Validation und Store Logic.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/stores/customerOnboardingStore.ts
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/validation/schemaBuilder.ts
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json
 */

import { describe, it, expect, beforeEach } from 'vitest';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { FieldDefinition } from '../../types/field.types';

// Test field definitions based on real field catalog
const testCustomerFields: FieldDefinition[] = [
  {
    key: 'companyName',
    label: 'Firmenname',
    entityType: 'customer',
    fieldType: 'text',
    required: true,
    maxLength: 100,
  },
  {
    key: 'email',
    label: 'E-Mail',
    entityType: 'customer',
    fieldType: 'email',
    required: true,
  },
  {
    key: 'industry',
    label: 'Branche',
    entityType: 'customer',
    fieldType: 'select',
    required: true,
    options: [
      { value: 'hotel', label: 'Hotel' },
      { value: 'restaurant', label: 'Restaurant' },
    ],
  },
  {
    key: 'starRating',
    label: 'Sterne-Kategorie',
    entityType: 'customer',
    fieldType: 'select',
    required: false,
    options: [
      { value: '1', label: '1 Stern' },
      { value: '5', label: '5 Sterne' },
    ],
    condition: {
      field: 'industry',
      operator: 'equals',
      value: 'hotel',
    },
  },
  {
    key: 'website',
    label: 'Website',
    entityType: 'customer',
    fieldType: 'text',
    required: false,
    validation: {
      pattern: '^https?://.+',
      message: 'Bitte geben Sie eine gültige URL ein',
    },
  },
];

describe.skip('🔄 CR-002 Store Dynamic Validation Integration', () => {
  beforeEach(() => {
    // Reset store before each test
    useCustomerOnboardingStore.getState().reset();

    // Set up field definitions
    useCustomerOnboardingStore.getState().setFieldDefinitions(testCustomerFields, []);
  });

  describe.skip('🎯 Dynamic Field Validation', () => {
    it('should validate required text field using Dynamic Zod Schema', async () => {
      const store = useCustomerOnboardingStore.getState();

      // Test empty required field
      store.setCustomerField('companyName', '');
      await store.validateField('companyName');

      const errors = useCustomerOnboardingStore.getState().validationErrors;
      expect(errors.companyName).toBeDefined();
      expect(errors.companyName).toContain('erforderlich');

      // Test valid field
      store.setCustomerField('companyName', 'Test Hotel GmbH');
      await store.validateField('companyName');

      const updatedErrors = useCustomerOnboardingStore.getState().validationErrors;
      expect(updatedErrors.companyName).toBeUndefined();
    });

    it('should validate email field using Dynamic Email Schema', async () => {
      const store = useCustomerOnboardingStore.getState();

      // Test invalid email
      store.setCustomerField('email', 'invalid-email');
      await store.validateField('email');

      const errors = useCustomerOnboardingStore.getState().validationErrors;
      expect(errors.email).toBeDefined();
      expect(errors.email.toLowerCase()).toContain('email');

      // Test valid email
      store.setCustomerField('email', 'test@hotel.de');
      await store.validateField('email');

      const updatedErrors = useCustomerOnboardingStore.getState().validationErrors;
      expect(updatedErrors.email).toBeUndefined();
    });

    it('should validate select field options using Dynamic Schema', async () => {
      const store = useCustomerOnboardingStore.getState();

      // Test invalid option (not in enum)
      store.setCustomerField('industry', 'invalid-industry');
      await store.validateField('industry');

      const errors = useCustomerOnboardingStore.getState().validationErrors;
      expect(errors.industry).toBeDefined();

      // Test valid option
      store.setCustomerField('industry', 'hotel');
      await store.validateField('industry');

      const updatedErrors = useCustomerOnboardingStore.getState().validationErrors;
      expect(updatedErrors.industry).toBeUndefined();
    });

    it('should handle custom validation patterns', async () => {
      const store = useCustomerOnboardingStore.getState();

      // Test invalid URL pattern
      store.setCustomerField('website', 'not-a-url');
      await store.validateField('website');

      const errors = useCustomerOnboardingStore.getState().validationErrors;
      expect(errors.website).toBeDefined();
      expect(errors.website).toContain('URL');

      // Test valid URL
      store.setCustomerField('website', 'https://test-hotel.de');
      await store.validateField('website');

      const updatedErrors = useCustomerOnboardingStore.getState().validationErrors;
      expect(updatedErrors.website).toBeUndefined();
    });
  });

  describe.skip('🔄 Dynamic Step Validation mit Conditional Fields', () => {
    it('should validate only visible fields in current step', async () => {
      const store = useCustomerOnboardingStore.getState();

      // Set industry to hotel (makes starRating visible)
      store.setCustomerField('industry', 'hotel');

      // Set required fields but leave starRating empty
      store.setCustomerField('companyName', 'Test Hotel');
      store.setCustomerField('email', 'test@hotel.de');

      // starRating is conditional and optional, so step should be valid
      const isValid = await store.validateCurrentStep();
      expect(isValid).toBe(true);

      // Now change industry to restaurant (hides starRating)
      store.setCustomerField('industry', 'restaurant');

      const isStillValid = await store.validateCurrentStep();
      expect(isStillValid).toBe(true);
    });

    it('should fail validation when required visible fields are missing', async () => {
      const store = useCustomerOnboardingStore.getState();

      // Only set some required fields
      store.setCustomerField('companyName', 'Test Hotel');
      // email is required but missing
      // industry is required but missing

      const isValid = await store.validateCurrentStep();
      expect(isValid).toBe(false);

      const errors = useCustomerOnboardingStore.getState().validationErrors;
      expect(errors.email).toBeDefined();
      expect(errors.industry).toBeDefined();
      expect(errors.companyName).toBeUndefined(); // This one is valid
    });

    it('should handle validation errors gracefully with fallback', async () => {
      const store = useCustomerOnboardingStore.getState();

      // Mock a scenario that might cause validation to throw
      // (This is defensive programming for Enterprise Flexibility)

      // Set invalid data
      store.setCustomerField('companyName', ''); // Required field empty

      const isValid = await store.validateCurrentStep();
      expect(isValid).toBe(false);

      const errors = useCustomerOnboardingStore.getState().validationErrors;
      expect(errors.companyName).toBeDefined();
    });
  });

  describe.skip('🎪 Enterprise Flexibility Philosophy', () => {
    it('should handle unknown field types gracefully', async () => {
      const store = useCustomerOnboardingStore.getState();

      // Add a field definition with unknown type (simulating future extensibility)
      const unknownField: FieldDefinition = {
        key: 'futureField',
        label: 'Future Field Type',
        entityType: 'customer',
        fieldType: 'unknownType' as any, // This is intentional - Enterprise Flexibility!
        required: false,
      };

      store.setFieldDefinitions([...testCustomerFields, unknownField], []);

      // Should not crash
      expect(() => {
        store.setCustomerField('futureField', 'some value');
      }).not.toThrow();

      // Validation should handle gracefully
      await expect(store.validateField('futureField')).resolves.not.toThrow();
    });

    it('should preserve any-type values as intended by team philosophy', async () => {
      const store = useCustomerOnboardingStore.getState();

      // Test various value types (respecting our flexibility philosophy)
      const testValues = [
        'string value',
        123,
        true,
        { complex: 'object' },
        ['array', 'value'],
        null,
      ];

      for (const value of testValues) {
        store.setCustomerField('companyName', value);

        // Should store the value as-is (any type flexibility)
        const storedValue = useCustomerOnboardingStore.getState().customerData.companyName;
        expect(storedValue).toBe(value);

        // Validation should handle gracefully (may fail validation, but shouldn't crash)
        await expect(store.validateField('companyName')).resolves.not.toThrow();
      }
    });

    it('should handle conditional fields that reference non-existent fields', async () => {
      const problematicField: FieldDefinition = {
        key: 'conditionalField',
        label: 'Conditional Field',
        entityType: 'customer',
        fieldType: 'text',
        required: false,
        condition: {
          field: 'nonExistentField',
          operator: 'equals',
          value: 'someValue',
        },
      };

      store.setFieldDefinitions([...testCustomerFields, problematicField], []);

      // Should not crash during step validation
      await expect(store.validateCurrentStep()).resolves.not.toThrow();
    });
  });

  describe.skip('🚀 Performance und Robustheit', () => {
    it('should handle validation of many fields efficiently', async () => {
      const store = useCustomerOnboardingStore.getState();

      // Set all test fields with valid data
      store.setCustomerField('companyName', 'Test Hotel GmbH');
      store.setCustomerField('email', 'test@hotel.de');
      store.setCustomerField('industry', 'hotel');
      store.setCustomerField('website', 'https://test-hotel.de');

      // Measure validation time for step validation
      const startTime = performance.now();
      const isValid = await store.validateCurrentStep();
      const endTime = performance.now();

      expect(isValid).toBe(true);
      expect(endTime - startTime).toBeLessThan(100); // Should be fast
    });

    it('should handle concurrent field validations without race conditions', async () => {
      const store = useCustomerOnboardingStore.getState();

      // Trigger multiple validations concurrently
      const validationPromises = [
        store.validateField('companyName'),
        store.validateField('email'),
        store.validateField('industry'),
      ];

      // Should all complete without throwing
      await expect(Promise.all(validationPromises)).resolves.not.toThrow();

      // Store should be in consistent state
      const finalState = useCustomerOnboardingStore.getState();
      expect(finalState.validationErrors).toBeDefined();
    });
  });
});
