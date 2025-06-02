/**
 * Calculator Module - TypeScript version
 * Handles discount calculations with proper state management
 */

import Module from '../core/Module';
import { useStore } from '../store';
import type { CalculatorCalculation } from '../types';

interface Scenario {
  orderValue: number;
  leadTime: number;
  pickup: boolean;
  chain: boolean;
}

interface DiscountRule {
  min: number;
  discount: number;
}

interface EarlyBookingRule {
  days: number;
  discount: number;
}

interface DiscountRules {
  base: DiscountRule[];
  earlyBooking: EarlyBookingRule[];
  pickup: number;
  maxTotal: number;
}

export default class CalculatorModule extends Module {
  private rules: DiscountRules;

  constructor() {
    super('calculator');
    // Initialize discount rules
    this.rules = {
      base: [
        { min: 75000, discount: 10 },
        { min: 50000, discount: 9 },
        { min: 30000, discount: 8 },
        { min: 15000, discount: 6 },
        { min: 5000, discount: 3 },
        { min: 0, discount: 0 }
      ],
      earlyBooking: [
        { days: 30, discount: 3 },
        { days: 15, discount: 2 },
        { days: 10, discount: 1 },
        { days: 0, discount: 0 }
      ],
      pickup: 2,
      maxTotal: 15
    };
  }

  async setup(): Promise<void> {
    // Get current state or set defaults
    const currentState = useStore.getState().calculator;
    
    if (currentState.calculation === null) {
      // Set default values and calculate
      const orderValue = 15000;
      const leadTime = 14;
      const pickup = false;
      const chain = false;
      
      this.calculate(orderValue, leadTime, pickup, chain);
      
      useStore.getState().setOrderValue(orderValue);
      useStore.getState().setLeadTime(leadTime);
      useStore.getState().setPickup(pickup);
      useStore.getState().setChain(chain);
      useStore.getState().updateCalculation();
    }
    
    // Initialize UI
    this.updateUI();
  }

  bindEvents(): void {
    // Order value slider
    this.on('#orderValue', 'input', (e: Event) => {
      const input = e.target as HTMLInputElement;
      const value = parseInt(input.value);
      useStore.getState().setOrderValue(value);
      this.updateSliderProgress(input);
    });

    // Lead time slider
    this.on('#leadTime', 'input', (e: Event) => {
      const input = e.target as HTMLInputElement;
      const value = parseInt(input.value);
      useStore.getState().setLeadTime(value);
      this.updateSliderProgress(input);
    });

    // Pickup checkbox
    this.on('#pickupToggle', 'change', (e: Event) => {
      const checkbox = e.target as HTMLInputElement;
      useStore.getState().setPickup(checkbox.checked);
    });

    // Chain checkbox
    this.on('#chainToggle', 'change', (e: Event) => {
      const checkbox = e.target as HTMLInputElement;
      useStore.getState().setChain(checkbox.checked);
    });

    // Scenario clicks
    this.on(document, 'click', '.scenario-card', (e: Event) => {
      const card = (e.target as Element).closest('.scenario-card') as HTMLElement;
      const scenario = card?.dataset.scenario;
      if (scenario) {
        this.loadScenario(scenario);
      }
    });
  }

  subscribeToState(): void {
    // Subscribe to calculator state changes
    useStore.subscribe(
      (state) => state.calculator,
      (calculatorState) => {
        this.updateUI();
        this.emit('calculated', calculatorState.calculation);
      }
    );
  }

  /**
   * Calculate discounts
   */
  private calculate(
    orderValue: number, 
    leadTime: number, 
    pickup: boolean, 
    chain: boolean
  ): CalculatorCalculation {
    // Base discount
    let baseDiscount = 0;
    for (const rule of this.rules.base) {
      if (orderValue >= rule.min) {
        baseDiscount = rule.discount;
        break;
      }
    }

    // Early booking discount
    let earlyDiscount = 0;
    for (const rule of this.rules.earlyBooking) {
      if (leadTime >= rule.days) {
        earlyDiscount = rule.discount;
        break;
      }
    }

    // Pickup discount (only for orders >= 5000 EUR)
    const pickupDiscount = (pickup && orderValue >= 5000) ? this.rules.pickup : 0;

    // Chain discount is 0 (only bundling benefit)
    const chainDiscount = 0;

    // Total discount (capped at max)
    const totalDiscount = Math.min(
      baseDiscount + earlyDiscount + pickupDiscount + chainDiscount,
      this.rules.maxTotal
    );

    // Calculate amounts
    const savingsAmount = Math.round(orderValue * (totalDiscount / 100));
    const finalPrice = orderValue - savingsAmount;

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
      discountAmount: savingsAmount,
      savingsAmount,
      finalPrice
    };
  }

  /**
   * Update UI with calculation results
   */
  private updateUI(): void {
    const state = useStore.getState().calculator;
    const calculation = state.calculation;
    
    if (!calculation) return;

    // Update value displays
    this.dom.text('#orderValueDisplay', `€${calculation.orderValue.toLocaleString('de-DE')}`);
    this.dom.text('#leadTimeDisplay', `${calculation.leadTime} Tage`);

    // Update discount displays
    this.dom.text('#baseDiscount', `${calculation.baseDiscount}%`);
    this.dom.text('#earlyBookingDiscount', `${calculation.earlyDiscount}%`);
    this.dom.text('#pickupDiscount', `${calculation.pickupDiscount}%`);
    this.dom.text('#totalDiscount', `${calculation.totalDiscount}%`);
    this.dom.text('#savingsAmount', `€${calculation.savingsAmount.toLocaleString('de-DE')}`);
    this.dom.text('#finalPrice', `€${calculation.finalPrice.toLocaleString('de-DE')}`);

    // Update progress bars
    this.updateProgressBar('baseDiscountBar', calculation.baseDiscount);
    this.updateProgressBar('earlyDiscountBar', calculation.earlyDiscount);
    this.updateProgressBar('pickupDiscountBar', calculation.pickupDiscount);

    // Update slider values
    const orderSlider = this.dom.$('#orderValue') as HTMLInputElement;
    const leadSlider = this.dom.$('#leadTime') as HTMLInputElement;
    
    if (orderSlider && parseInt(orderSlider.value) !== state.orderValue) {
      orderSlider.value = String(state.orderValue);
      this.updateSliderProgress(orderSlider);
    }
    
    if (leadSlider && parseInt(leadSlider.value) !== state.leadTime) {
      leadSlider.value = String(state.leadTime);
      this.updateSliderProgress(leadSlider);
    }

    // Update checkboxes
    const pickupToggle = this.dom.$('#pickupToggle') as HTMLInputElement;
    const chainToggle = this.dom.$('#chainToggle') as HTMLInputElement;
    
    if (pickupToggle) pickupToggle.checked = state.pickup;
    if (chainToggle) chainToggle.checked = state.chain;
  }

  /**
   * Update discount progress bar
   */
  private updateProgressBar(barId: string, percentage: number): void {
    const bar = this.dom.$(`#${barId}`) as HTMLElement;
    if (bar) {
      // Scale to max 30% for visual representation
      const width = Math.min((percentage / 30) * 100, 100);
      bar.style.width = `${width}%`;
    }
  }

  /**
   * Update slider visual progress
   */
  private updateSliderProgress(slider: HTMLInputElement): void {
    const min = parseFloat(slider.min);
    const max = parseFloat(slider.max);
    const value = parseFloat(slider.value);
    const percentage = ((value - min) / (max - min)) * 100;
    slider.style.setProperty('--progress', `${percentage}%`);
  }

  /**
   * Load predefined scenario
   */
  private loadScenario(scenarioName: string): void {
    const scenarios: Record<string, Scenario> = {
      spontaneous: { orderValue: 8000, leadTime: 3, pickup: false, chain: false },
      planned: { orderValue: 25000, leadTime: 14, pickup: false, chain: false },
      optimal: { orderValue: 50000, leadTime: 30, pickup: true, chain: false }
    };

    const scenario = scenarios[scenarioName];
    if (scenario) {
      const store = useStore.getState();
      store.setOrderValue(scenario.orderValue);
      store.setLeadTime(scenario.leadTime);
      store.setPickup(scenario.pickup);
      store.setChain(scenario.chain);
      
      this.showSuccess(`Szenario "${scenarioName}" geladen`);
    }
  }

  /**
   * Public API
   */
  
  getCalculation(): CalculatorCalculation | null {
    return useStore.getState().calculator.calculation;
  }

  recalculate(): void {
    const state = useStore.getState().calculator;
    this.calculate(
      state.orderValue,
      state.leadTime,
      state.pickup,
      state.chain
    );
    
    useStore.getState().updateCalculation();
  }

  getDiscountRules(): DiscountRules {
    return this.rules;
  }

  /**
   * Cleanup module resources
   */
  cleanup(): void {
    // Module cleanup is handled by base class
  }
}