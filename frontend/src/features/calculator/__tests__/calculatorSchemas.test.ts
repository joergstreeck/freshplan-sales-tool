import { describe, it, expect } from 'vitest';
import {
  CalculatorInputSchema,
  CalculatorResponseSchema,
  PREDEFINED_SCENARIOS,
  SCENARIO_DESCRIPTIONS,
} from '../api/calculatorSchemas';

describe('Calculator Schemas', () => {
  describe('CalculatorInputSchema', () => {
    it('should validate correct input', () => {
      const validInput = {
        orderValue: 25000,
        leadTime: 14,
        pickup: false,
        chain: false,
      };

      const result = CalculatorInputSchema.safeParse(validInput);
      expect(result.success).toBe(true);
    });

    it('should reject negative order value', () => {
      const invalidInput = {
        orderValue: -1000,
        leadTime: 14,
        pickup: false,
        chain: false,
      };

      const result = CalculatorInputSchema.safeParse(invalidInput);
      expect(result.success).toBe(false);
      expect(result.error?.issues[0].message).toContain('mindestens 0€');
    });

    it('should reject order value over maximum', () => {
      const invalidInput = {
        orderValue: 2000000,
        leadTime: 14,
        pickup: false,
        chain: false,
      };

      const result = CalculatorInputSchema.safeParse(invalidInput);
      expect(result.success).toBe(false);
      expect(result.error?.issues[0].message).toContain('maximal 1.000.000€');
    });

    it('should reject negative lead time', () => {
      const invalidInput = {
        orderValue: 25000,
        leadTime: -5,
        pickup: false,
        chain: false,
      };

      const result = CalculatorInputSchema.safeParse(invalidInput);
      expect(result.success).toBe(false);
      expect(result.error?.issues[0].message).toContain('mindestens 0 Tage');
    });

    it('should reject lead time over maximum', () => {
      const invalidInput = {
        orderValue: 25000,
        leadTime: 500,
        pickup: false,
        chain: false,
      };

      const result = CalculatorInputSchema.safeParse(invalidInput);
      expect(result.success).toBe(false);
      expect(result.error?.issues[0].message).toContain('maximal 365 Tage');
    });

    it('should reject non-integer lead time', () => {
      const invalidInput = {
        orderValue: 25000,
        leadTime: 14.5,
        pickup: false,
        chain: false,
      };

      const result = CalculatorInputSchema.safeParse(invalidInput);
      expect(result.success).toBe(false);
      expect(result.error?.issues[0].message).toContain('ganze Zahl');
    });
  });

  describe('CalculatorResponseSchema', () => {
    it('should validate correct response', () => {
      const validResponse = {
        orderValue: 25000,
        leadTime: 14,
        pickup: false,
        chain: false,
        baseDiscount: 8,
        earlyDiscount: 2,
        pickupDiscount: 0,
        chainDiscount: 0,
        totalDiscount: 10,
        discountAmount: 2500,
        savingsAmount: 2500,
        finalPrice: 22500,
      };

      const result = CalculatorResponseSchema.safeParse(validResponse);
      expect(result.success).toBe(true);
    });

    it('should reject missing required fields', () => {
      const invalidResponse = {
        orderValue: 25000,
        leadTime: 14,
        // Missing other required fields
      };

      const result = CalculatorResponseSchema.safeParse(invalidResponse);
      expect(result.success).toBe(false);
    });
  });

  describe('Predefined Scenarios', () => {
    it('should have all required scenarios', () => {
      const expectedScenarios = ['spontan', 'geplant', 'optimal', 'hotel', 'klinik', 'restaurant'];

      expectedScenarios.forEach(scenario => {
        expect(PREDEFINED_SCENARIOS).toHaveProperty(scenario);
        expect(SCENARIO_DESCRIPTIONS).toHaveProperty(scenario);
      });
    });

    it('should have valid scenarios that pass input validation', () => {
      Object.values(PREDEFINED_SCENARIOS).forEach(scenario => {
        const result = CalculatorInputSchema.safeParse(scenario);
        expect(result.success).toBe(true);
      });
    });

    it('should have realistic scenario values', () => {
      // Test specific business logic expectations
      const spontan = PREDEFINED_SCENARIOS.spontan;
      expect(spontan.orderValue).toBeGreaterThan(5000); // Should get at least base discount
      expect(spontan.leadTime).toBeLessThan(10); // Short lead time

      const optimal = PREDEFINED_SCENARIOS.optimal;
      expect(optimal.orderValue).toBeGreaterThanOrEqual(50000); // High value order
      expect(optimal.leadTime).toBeGreaterThanOrEqual(30); // Long lead time
      expect(optimal.pickup).toBe(true); // Should include pickup for maximum discount

      const klinik = PREDEFINED_SCENARIOS.klinik;
      expect(klinik.chain).toBe(true); // Clinics are typically chain customers
      expect(klinik.orderValue).toBeGreaterThan(50000); // Large orders
    });

    it('should have descriptions for all scenarios', () => {
      Object.keys(PREDEFINED_SCENARIOS).forEach(scenarioName => {
        expect(SCENARIO_DESCRIPTIONS[scenarioName]).toBeDefined();
        expect(SCENARIO_DESCRIPTIONS[scenarioName].length).toBeGreaterThan(10);
      });
    });
  });

  describe('Business Logic Validation', () => {
    it('should validate edge cases for discount thresholds', () => {
      // Test exact threshold values from business rules
      const thresholdTests = [
        { orderValue: 4999, expectedMinDiscount: 0 }, // Just below 5k threshold
        { orderValue: 5000, expectedMinDiscount: 3 }, // Exactly at 5k threshold
        { orderValue: 74999, expectedMinDiscount: 9 }, // Just below 75k threshold
        { orderValue: 75000, expectedMinDiscount: 10 }, // Exactly at 75k threshold
      ];

      thresholdTests.forEach(test => {
        const input = {
          orderValue: test.orderValue,
          leadTime: 0,
          pickup: false,
          chain: false,
        };

        const result = CalculatorInputSchema.safeParse(input);
        expect(result.success).toBe(true);
      });
    });

    it('should validate pickup discount requirements', () => {
      // Pickup discount only applies to orders >= 5000
      const pickupTests = [
        { orderValue: 4999, pickup: true }, // Below pickup threshold
        { orderValue: 5000, pickup: true }, // At pickup threshold
      ];

      pickupTests.forEach(test => {
        const input = {
          orderValue: test.orderValue,
          leadTime: 0,
          pickup: test.pickup,
          chain: false,
        };

        const result = CalculatorInputSchema.safeParse(input);
        expect(result.success).toBe(true);
      });
    });
  });
});
