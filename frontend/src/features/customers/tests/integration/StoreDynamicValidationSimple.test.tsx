/**
 * FC-005 CR-002 Simple Store Dynamic Validation Test
 *
 * Einfacher Test für die Dynamic Zod Schema Builder Integration im Store.
 * Fokus auf Kernfunktionalität ohne komplexe Dependencies.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/stores/customerOnboardingStore.ts
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/validation/schemaBuilder.ts
 */

import { describe, it, expect } from 'vitest';
import { buildFieldSchema, validateField } from '../../validation/schemaBuilder';
import { FieldDefinition } from '../../types/field.types';

describe('🔄 CR-002 Dynamic Validation Integration (Simple)', () => {
  describe('🎯 Schema Builder Core Functionality', () => {
    it('should build dynamic schema for text field', () => {
      const field: FieldDefinition = {
        key: 'companyName',
        label: 'Firmenname',
        entityType: 'customer',
        fieldType: 'text',
        required: true,
        maxLength: 100,
      };

      const schema = buildFieldSchema(field);
      expect(schema).toBeDefined();
    });

    it('should validate required text field correctly', async () => {
      const field: FieldDefinition = {
        key: 'companyName',
        label: 'Firmenname',
        entityType: 'customer',
        fieldType: 'text',
        required: true,
      };

      // Test empty value
      const emptyResult = await validateField(field, '');
      expect(emptyResult.isValid).toBe(false);
      expect(emptyResult.error).toBeDefined();

      // Test valid value
      const validResult = await validateField(field, 'Test Hotel GmbH');
      expect(validResult.isValid).toBe(true);
      expect(validResult.error).toBeUndefined();
    });

    it('should validate email field with dynamic schema', async () => {
      const field: FieldDefinition = {
        key: 'email',
        label: 'E-Mail',
        entityType: 'customer',
        fieldType: 'email',
        required: true,
      };

      // Test invalid email
      const invalidResult = await validateField(field, 'invalid-email');
      expect(invalidResult.isValid).toBe(false);

      // Test valid email
      const validResult = await validateField(field, 'test@hotel.de');
      expect(validResult.isValid).toBe(true);
    });

    it('should validate select field with options', async () => {
      const field: FieldDefinition = {
        key: 'industry',
        label: 'Branche',
        entityType: 'customer',
        fieldType: 'select',
        required: true,
        options: [
          { value: 'hotel', label: 'Hotel' },
          { value: 'restaurant', label: 'Restaurant' },
        ],
      };

      // Test invalid option
      const invalidResult = await validateField(field, 'invalid-option');
      expect(invalidResult.isValid).toBe(false);

      // Test valid option
      const validResult = await validateField(field, 'hotel');
      expect(validResult.isValid).toBe(true);
    });
  });

  describe('🎪 Enterprise Flexibility Philosophy', () => {
    it('should handle unknown field types gracefully', async () => {
      const field: FieldDefinition = {
        key: 'futureField',
        label: 'Future Field',
        entityType: 'customer',
        fieldType: 'unknownType' as unknown, // This is intentional - Flexibility!
        required: false,
      };

      // Should not crash
      expect(() => buildFieldSchema(field)).not.toThrow();

      // Validation should handle gracefully
      const result = await validateField(field, 'some value');
      expect(result).toBeDefined();
      expect(typeof result.isValid).toBe('boolean');
    });

    it('should preserve any-type values as intended', async () => {
      const field: FieldDefinition = {
        key: 'flexibleField',
        label: 'Flexible Field',
        entityType: 'customer',
        fieldType: 'text',
        required: false,
      };

      // Test various value types (Enterprise Flexibility!)
      const testValues = ['string value', 123, true, { complex: 'object' }, ['array', 'value']];

      for (const value of testValues) {
        // Should not crash with any value type
        const result = await validateField(field, value);
        expect(result).toBeDefined();
        expect(typeof result.isValid).toBe('boolean');
      }
    });
  });

  describe('🚀 Performance', () => {
    it('should validate fields efficiently', async () => {
      const field: FieldDefinition = {
        key: 'testField',
        label: 'Test Field',
        entityType: 'customer',
        fieldType: 'text',
        required: true,
      };

      const startTime = performance.now();
      await validateField(field, 'test value');
      const endTime = performance.now();

      expect(endTime - startTime).toBeLessThan(50); // Should be very fast
    });
  });
});
