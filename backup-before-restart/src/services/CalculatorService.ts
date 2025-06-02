/**
 * Calculator Service - Pure business logic for discount calculations
 * No DOM dependencies, easily testable
 */

import type { 
  CalculatorCalculation, 
  DiscountRule, 
  EarlyBookingRule 
} from '../types';

export interface DiscountRulesConfig {
  base: DiscountRule[];
  earlyBooking: EarlyBookingRule[];
  pickup: number;
  maxTotal: number;
}

export interface CalculationInput {
  orderValue: number;
  leadTime: number;
  pickup: boolean;
  chain: boolean;
}

export interface Scenario {
  name: string;
  orderValue: number;
  leadTime: number;
  pickup: boolean;
  chain: boolean;
  description?: string;
}

export class CalculatorService {
  private rules: DiscountRulesConfig;
  private scenarios: Map<string, Scenario>;

  constructor(rules?: Partial<DiscountRulesConfig>) {
    // Default discount rules
    this.rules = {
      base: rules?.base || [
        { min: 75000, discount: 10 },
        { min: 50000, discount: 9 },
        { min: 30000, discount: 8 },
        { min: 15000, discount: 6 },
        { min: 5000, discount: 3 },
        { min: 0, discount: 0 }
      ],
      earlyBooking: rules?.earlyBooking || [
        { days: 30, discount: 3 },
        { days: 15, discount: 2 },
        { days: 10, discount: 1 },
        { days: 0, discount: 0 }
      ],
      pickup: rules?.pickup || 2,
      maxTotal: rules?.maxTotal || 15
    };

    // Predefined scenarios
    this.scenarios = new Map([
      ['spontan', {
        name: 'spontan',
        orderValue: 8000,
        leadTime: 5,
        pickup: false,
        chain: false,
        description: 'Kleinbestellung mit kurzer Vorlaufzeit'
      }],
      ['geplant', {
        name: 'geplant',
        orderValue: 32000,
        leadTime: 16,
        pickup: false,
        chain: false,
        description: 'Geplante Bestellung mit mittlerem Volumen'
      }],
      ['optimal', {
        name: 'optimal',
        orderValue: 50000,
        leadTime: 30,
        pickup: true,
        chain: false,
        description: 'Optimale Bestellung mit allen Rabatten'
      }],
      ['hotel', {
        name: 'hotel',
        orderValue: 25000,
        leadTime: 14,
        pickup: false,
        chain: false,
        description: 'Hotelkette - Standard Bestellung'
      }],
      ['klinik', {
        name: 'klinik',
        orderValue: 65000,
        leadTime: 30,
        pickup: false,
        chain: true,
        description: 'Klinikgruppe - Große Bestellung mit Vorlaufzeit'
      }],
      ['restaurant', {
        name: 'restaurant',
        orderValue: 8500,
        leadTime: 7,
        pickup: true,
        chain: false,
        description: 'Restaurant - Kleine Bestellung mit Abholung'
      }]
    ]);
  }

  /**
   * Calculate discounts based on input parameters
   */
  calculateDiscount(input: CalculationInput): CalculatorCalculation {
    const { orderValue, leadTime, pickup, chain } = input;

    // Validate input
    this.validateCalculation(input);

    // Calculate base discount
    const baseDiscount = this.calculateBaseDiscount(orderValue);

    // Calculate early booking discount
    const earlyDiscount = this.calculateEarlyBookingDiscount(leadTime);

    // Calculate pickup discount (only for orders >= 5000 EUR)
    const pickupDiscount = this.calculatePickupDiscount(orderValue, pickup);

    // Chain discount is always 0 (only bundling benefit)
    const chainDiscount = 0;

    // Calculate total discount (capped at max)
    const totalDiscount = Math.min(
      baseDiscount + earlyDiscount + pickupDiscount + chainDiscount,
      this.rules.maxTotal
    );

    // Calculate amounts
    const discountAmount = Math.round(orderValue * (totalDiscount / 100));
    const finalPrice = orderValue - discountAmount;
    const savingsAmount = discountAmount;

    return {
      orderValue,
      leadTime,
      pickup,
      chain,
      baseDiscount,
      earlyDiscount,
      pickupDiscount,
      chainDiscount,
      totalDiscount,
      discountAmount,
      savingsAmount,
      finalPrice
    };
  }

  /**
   * Apply a predefined scenario
   */
  applyScenario(scenarioName: string): CalculationInput | null {
    const scenario = this.scenarios.get(scenarioName);
    if (!scenario) {
      return null;
    }

    return {
      orderValue: scenario.orderValue,
      leadTime: scenario.leadTime,
      pickup: scenario.pickup,
      chain: scenario.chain
    };
  }

  /**
   * Validate calculation input
   */
  validateCalculation(input: CalculationInput): void {
    const errors: string[] = [];

    if (input.orderValue < 0) {
      errors.push('Order value must be non-negative');
    }

    if (input.orderValue > 1000000) {
      errors.push('Order value exceeds maximum allowed value');
    }

    if (input.leadTime < 0) {
      errors.push('Lead time must be non-negative');
    }

    if (input.leadTime > 365) {
      errors.push('Lead time exceeds maximum allowed days');
    }

    if (errors.length > 0) {
      throw new Error(`Validation failed: ${errors.join(', ')}`);
    }
  }

  /**
   * Get discount rules configuration
   */
  getDiscountRules(): Readonly<DiscountRulesConfig> {
    return Object.freeze({ ...this.rules });
  }

  /**
   * Update discount rules
   */
  updateDiscountRules(newRules: Partial<DiscountRulesConfig>): void {
    this.rules = {
      ...this.rules,
      ...newRules
    };
  }

  /**
   * Get all available scenarios
   */
  getScenarios(): Scenario[] {
    return Array.from(this.scenarios.values());
  }

  /**
   * Add or update a scenario
   */
  setScenario(name: string, scenario: Omit<Scenario, 'name'>): void {
    this.scenarios.set(name, { ...scenario, name });
  }

  /**
   * Remove a scenario
   */
  removeScenario(name: string): boolean {
    return this.scenarios.delete(name);
  }

  /**
   * Calculate base discount based on order value
   */
  private calculateBaseDiscount(orderValue: number): number {
    for (const rule of this.rules.base) {
      if (orderValue >= rule.min) {
        return rule.discount;
      }
    }
    return 0;
  }

  /**
   * Calculate early booking discount based on lead time
   */
  private calculateEarlyBookingDiscount(leadTime: number): number {
    for (const rule of this.rules.earlyBooking) {
      if (leadTime >= rule.days) {
        return rule.discount;
      }
    }
    return 0;
  }

  /**
   * Calculate pickup discount
   */
  private calculatePickupDiscount(orderValue: number, pickup: boolean): number {
    return (pickup && orderValue >= 5000) ? this.rules.pickup : 0;
  }

  /**
   * Calculate estimated annual savings
   */
  calculateAnnualSavings(monthlyOrderValue: number, totalDiscount: number): number {
    return Math.round(monthlyOrderValue * 12 * (totalDiscount / 100));
  }

  /**
   * Get discount breakdown for display
   */
  getDiscountBreakdown(calculation: CalculatorCalculation): {
    components: Array<{ name: string; value: number; active: boolean }>;
    total: number;
    savings: number;
  } {
    return {
      components: [
        {
          name: 'Base Discount',
          value: calculation.baseDiscount,
          active: calculation.baseDiscount > 0
        },
        {
          name: 'Early Booking',
          value: calculation.earlyDiscount,
          active: calculation.earlyDiscount > 0
        },
        {
          name: 'Pickup Discount',
          value: calculation.pickupDiscount,
          active: calculation.pickupDiscount > 0
        }
      ],
      total: calculation.totalDiscount,
      savings: calculation.savingsAmount
    };
  }

  /**
   * Check if a certain discount threshold is reached
   */
  isDiscountThresholdReached(orderValue: number, threshold: number): boolean {
    const baseDiscount = this.calculateBaseDiscount(orderValue);
    return baseDiscount >= threshold;
  }

  /**
   * Get next discount threshold
   */
  getNextDiscountThreshold(currentOrderValue: number): {
    nextThreshold: number;
    additionalDiscount: number;
    amountNeeded: number;
  } | null {
    const currentDiscount = this.calculateBaseDiscount(currentOrderValue);
    
    for (const rule of this.rules.base) {
      if (rule.discount > currentDiscount && rule.min > currentOrderValue) {
        return {
          nextThreshold: rule.min,
          additionalDiscount: rule.discount - currentDiscount,
          amountNeeded: rule.min - currentOrderValue
        };
      }
    }
    
    return null;
  }

  /**
   * Export calculation as JSON
   */
  exportCalculation(calculation: CalculatorCalculation): string {
    return JSON.stringify({
      calculation,
      rules: this.rules,
      timestamp: new Date().toISOString()
    }, null, 2);
  }
}