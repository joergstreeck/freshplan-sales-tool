/**
 * Calculator Business Logic Tests
 * Ensure discount calculations are correct according to business rules
 */

import { describe, it, expect } from 'vitest';

// Business logic for calculator
export function calculateDiscount(
  orderValue: number,
  leadTime: number,
  pickup: boolean,
  chain: boolean
) {
  // Base discount rules
  const baseRules = [
    { min: 75000, discount: 10 },
    { min: 50000, discount: 9 },
    { min: 30000, discount: 8 },
    { min: 15000, discount: 6 },
    { min: 5000, discount: 3 },
    { min: 0, discount: 0 }
  ];

  // Early booking rules
  const earlyRules = [
    { days: 30, discount: 3 },
    { days: 15, discount: 2 },
    { days: 10, discount: 1 },
    { days: 0, discount: 0 }
  ];

  // Calculate base discount
  let baseDiscount = 0;
  for (const rule of baseRules) {
    if (orderValue >= rule.min) {
      baseDiscount = rule.discount;
      break;
    }
  }

  // Calculate early booking discount
  let earlyDiscount = 0;
  for (const rule of earlyRules) {
    if (leadTime >= rule.days) {
      earlyDiscount = rule.discount;
      break;
    }
  }

  // Pickup discount (only for orders >= 5000 EUR)
  const pickupDiscount = (pickup && orderValue >= 5000) ? 2 : 0;

  // Chain discount is always 0 (they get bundling benefits instead)
  const chainDiscount = 0;

  // Total discount
  const totalDiscount = baseDiscount + earlyDiscount + pickupDiscount + chainDiscount;
  const discountAmount = orderValue * (totalDiscount / 100);
  const finalPrice = orderValue - discountAmount;

  return {
    baseDiscount,
    earlyDiscount,
    pickupDiscount,
    chainDiscount,
    totalDiscount,
    discountAmount,
    finalPrice
  };
}

describe('Calculator Business Logic', () => {
  describe('Base Discount Rules', () => {
    const testCases = [
      { orderValue: 1000, expected: 0 },
      { orderValue: 5000, expected: 3 },
      { orderValue: 14999, expected: 3 },
      { orderValue: 15000, expected: 6 },
      { orderValue: 29999, expected: 6 },
      { orderValue: 30000, expected: 8 },
      { orderValue: 49999, expected: 8 },
      { orderValue: 50000, expected: 9 },
      { orderValue: 74999, expected: 9 },
      { orderValue: 75000, expected: 10 },
      { orderValue: 100000, expected: 10 }
    ];

    testCases.forEach(({ orderValue, expected }) => {
      it(`should return ${expected}% base discount for ${orderValue} EUR`, () => {
        const result = calculateDiscount(orderValue, 0, false, false);
        expect(result.baseDiscount).toBe(expected);
      });
    });
  });

  describe('Early Booking Discount Rules', () => {
    const testCases = [
      { leadTime: 0, expected: 0 },
      { leadTime: 5, expected: 0 },
      { leadTime: 10, expected: 1 },
      { leadTime: 14, expected: 1 },
      { leadTime: 15, expected: 2 },
      { leadTime: 29, expected: 2 },
      { leadTime: 30, expected: 3 },
      { leadTime: 45, expected: 3 }
    ];

    testCases.forEach(({ leadTime, expected }) => {
      it(`should return ${expected}% early booking discount for ${leadTime} days lead time`, () => {
        const result = calculateDiscount(10000, leadTime, false, false);
        expect(result.earlyDiscount).toBe(expected);
      });
    });
  });

  describe('Pickup Discount Rules', () => {
    it('should apply 2% pickup discount for orders >= 5000 EUR', () => {
      const result = calculateDiscount(5000, 0, true, false);
      expect(result.pickupDiscount).toBe(2);
    });

    it('should not apply pickup discount for orders < 5000 EUR', () => {
      const result = calculateDiscount(4999, 0, true, false);
      expect(result.pickupDiscount).toBe(0);
    });

    it('should not apply pickup discount when pickup is false', () => {
      const result = calculateDiscount(10000, 0, false, false);
      expect(result.pickupDiscount).toBe(0);
    });
  });

  describe('Chain Customer Rules', () => {
    it('should always apply 0% chain discount', () => {
      const result = calculateDiscount(50000, 0, false, true);
      expect(result.chainDiscount).toBe(0);
    });

    it('should not affect other discounts when chain is true', () => {
      const chainResult = calculateDiscount(50000, 30, true, true);
      const nonChainResult = calculateDiscount(50000, 30, true, false);
      
      expect(chainResult.totalDiscount).toBe(nonChainResult.totalDiscount);
    });
  });

  describe('Combined Discount Scenarios', () => {
    it('should calculate scenario: Spontanbestellung correctly', () => {
      const result = calculateDiscount(8000, 3, false, false);
      
      expect(result.baseDiscount).toBe(3);
      expect(result.earlyDiscount).toBe(0);
      expect(result.pickupDiscount).toBe(0);
      expect(result.chainDiscount).toBe(0);
      expect(result.totalDiscount).toBe(3);
      expect(result.discountAmount).toBe(240);
      expect(result.finalPrice).toBe(7760);
    });

    it('should calculate scenario: Geplante Bestellung correctly', () => {
      const result = calculateDiscount(25000, 14, false, false);
      
      expect(result.baseDiscount).toBe(6);
      expect(result.earlyDiscount).toBe(1);
      expect(result.pickupDiscount).toBe(0);
      expect(result.chainDiscount).toBe(0);
      expect(result.totalDiscount).toBe(7);
      expect(result.discountAmount).toBeCloseTo(1750, 2);
      expect(result.finalPrice).toBeCloseTo(23250, 2);
    });

    it('should calculate scenario: Optimale Bestellung correctly', () => {
      const result = calculateDiscount(50000, 30, true, false);
      
      expect(result.baseDiscount).toBe(9);
      expect(result.earlyDiscount).toBe(3);
      expect(result.pickupDiscount).toBe(2);
      expect(result.chainDiscount).toBe(0);
      expect(result.totalDiscount).toBe(14);
      expect(result.discountAmount).toBeCloseTo(7000, 2);
      expect(result.finalPrice).toBeCloseTo(43000, 2);
    });
  });

  describe('Maximum Discount', () => {
    it('should calculate maximum possible discount', () => {
      const result = calculateDiscount(100000, 45, true, false);
      
      expect(result.baseDiscount).toBe(10);
      expect(result.earlyDiscount).toBe(3);
      expect(result.pickupDiscount).toBe(2);
      expect(result.totalDiscount).toBe(15);
      expect(result.discountAmount).toBe(15000);
      expect(result.finalPrice).toBe(85000);
    });
  });

  describe('Edge Cases', () => {
    it('should handle zero order value', () => {
      const result = calculateDiscount(0, 30, true, false);
      
      expect(result.baseDiscount).toBe(0);
      expect(result.earlyDiscount).toBe(3);
      expect(result.pickupDiscount).toBe(0); // No pickup discount under 5000
      expect(result.totalDiscount).toBe(3);
      expect(result.discountAmount).toBe(0);
      expect(result.finalPrice).toBe(0);
    });

    it('should handle exactly threshold values', () => {
      // Exactly at 5000 EUR threshold
      const result1 = calculateDiscount(5000, 0, true, false);
      expect(result1.baseDiscount).toBe(3);
      expect(result1.pickupDiscount).toBe(2);
      
      // Exactly at 15000 EUR threshold
      const result2 = calculateDiscount(15000, 0, false, false);
      expect(result2.baseDiscount).toBe(6);
      
      // Exactly at 10 days threshold
      const result3 = calculateDiscount(10000, 10, false, false);
      expect(result3.earlyDiscount).toBe(1);
    });

    it('should handle very large order values', () => {
      const result = calculateDiscount(1000000, 30, true, false);
      
      expect(result.baseDiscount).toBe(10);
      expect(result.totalDiscount).toBe(15);
      expect(result.discountAmount).toBe(150000);
      expect(result.finalPrice).toBe(850000);
    });
  });

  describe('Calculation Precision', () => {
    it('should handle decimal order values correctly', () => {
      const result = calculateDiscount(15000.50, 14, false, false);
      
      expect(result.baseDiscount).toBe(6);
      expect(result.earlyDiscount).toBe(1);
      expect(result.totalDiscount).toBe(7);
      expect(result.discountAmount).toBeCloseTo(1050.035, 2);
      expect(result.finalPrice).toBeCloseTo(13950.465, 2);
    });
  });

  describe('Business Rule Validation', () => {
    it('should never return negative discounts', () => {
      const testValues = [
        { orderValue: -1000, leadTime: -10 },
        { orderValue: 0, leadTime: 0 },
        { orderValue: 1000000, leadTime: 100 }
      ];

      testValues.forEach(({ orderValue, leadTime }) => {
        const result = calculateDiscount(orderValue, leadTime, true, true);
        
        expect(result.baseDiscount).toBeGreaterThanOrEqual(0);
        expect(result.earlyDiscount).toBeGreaterThanOrEqual(0);
        expect(result.pickupDiscount).toBeGreaterThanOrEqual(0);
        expect(result.chainDiscount).toBeGreaterThanOrEqual(0);
        expect(result.totalDiscount).toBeGreaterThanOrEqual(0);
      });
    });

    it('should never exceed reasonable discount limits', () => {
      // Even with all discounts, should not exceed 15%
      const result = calculateDiscount(100000, 45, true, true);
      expect(result.totalDiscount).toBeLessThanOrEqual(15);
    });
  });
});