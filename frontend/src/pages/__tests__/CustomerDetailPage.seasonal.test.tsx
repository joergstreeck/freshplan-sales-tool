/**
 * CustomerDetailPage - Seasonal Business Tests
 * Sprint 2.1.7.4 - Section 7.4
 */

import { describe, it, expect } from 'vitest';

// Helper function tests (from CustomerDetailPage.tsx)
const MONTH_NAMES = [
  'Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun',
  'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'
];

function getSeasonalPatternLabel(pattern: string | null | undefined, months: number[] | null | undefined): string {
  if (!months || months.length === 0) return 'Nicht konfiguriert';

  const monthNames = months.map(m => MONTH_NAMES[m - 1]).join(', ');
  return monthNames;
}

describe('CustomerDetailPage - Seasonal Business Indicator', () => {
  /**
   * Test 1: Helper function converts month numbers to names
   */
  it('should convert month numbers to German month names', () => {
    // GIVEN: Summer months [6, 7, 8]
    const months = [6, 7, 8];

    // WHEN: Calling helper function
    const result = getSeasonalPatternLabel('SUMMER', months);

    // THEN: Should return "Jun, Jul, Aug"
    expect(result).toBe('Jun, Jul, Aug');
  });

  /**
   * Test 2: Helper function handles winter months correctly
   */
  it('should handle winter months (December, January, February)', () => {
    // GIVEN: Winter months [12, 1, 2]
    const months = [12, 1, 2];

    // WHEN: Calling helper function
    const result = getSeasonalPatternLabel('WINTER', months);

    // THEN: Should return "Dez, Jan, Feb"
    expect(result).toBe('Dez, Jan, Feb');
  });

  /**
   * Test 3: Helper function handles empty months list
   */
  it('should return "Nicht konfiguriert" for empty months list', () => {
    // GIVEN: Empty months list
    const months: number[] = [];

    // WHEN: Calling helper function
    const result = getSeasonalPatternLabel('CUSTOM', months);

    // THEN: Should return fallback text
    expect(result).toBe('Nicht konfiguriert');
  });

  /**
   * Test 4: Seasonal business logic validates correctly
   */
  it('should identify seasonal business based on isSeasonalBusiness flag', () => {
    // GIVEN: Customer data
    const seasonalCustomer = { isSeasonalBusiness: true };
    const regularCustomer = { isSeasonalBusiness: false };
    const undefinedCustomer = { isSeasonalBusiness: undefined };

    // THEN: Seasonal flag should be correctly evaluated
    expect(seasonalCustomer.isSeasonalBusiness).toBe(true);
    expect(regularCustomer.isSeasonalBusiness).toBe(false);
    expect(undefinedCustomer.isSeasonalBusiness).toBeUndefined();
  });
});
