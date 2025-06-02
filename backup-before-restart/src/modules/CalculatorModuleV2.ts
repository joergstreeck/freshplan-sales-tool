/**
 * Calculator Module V2 - Using Zustand directly
 * Modern implementation without legacy StateManager
 */

import Module from '../core/Module';
import { useStore, selectors } from '../store';
import type { CalculationResult } from '../types';

interface Scenario {
  name: string;
  orderValue: number;
  leadTime: number;
  pickup: boolean;
  chain: boolean;
}

export default class CalculatorModuleV2 extends Module {
  private readonly scenarios: Record<string, Scenario> = {
    spontan: {
      name: 'Spontanbestellung',
      orderValue: 8000,
      leadTime: 3,
      pickup: false,
      chain: false
    },
    geplant: {
      name: 'Geplante Bestellung',
      orderValue: 25000,
      leadTime: 14,
      pickup: false,
      chain: false
    },
    optimal: {
      name: 'Optimale Bestellung',
      orderValue: 50000,
      leadTime: 30,
      pickup: true,
      chain: false
    }
  };

  // Store subscriptions
  private unsubscribeStore?: () => void;

  constructor() {
    super('calculator');
  }

  async setup(): Promise<void> {
    // Initial calculation
    const store = useStore.getState();
    store.updateCalculation();
    
    // Initialize UI with current values
    this.initializeUI();
  }

  bindEvents(): void {
    // Get actions
    const actions = this.getActions();

    // Order value slider
    this.on('#orderValue', 'input', (e: Event) => {
      const input = e.target as HTMLInputElement;
      const value = parseInt(input.value, 10);
      
      actions.updateOrderValue(value);
      this.updateSliderProgress(input);
    });

    // Lead time slider
    this.on('#leadTime', 'input', (e: Event) => {
      const input = e.target as HTMLInputElement;
      const value = parseInt(input.value, 10);
      
      actions.updateLeadTime(value);
      this.updateSliderProgress(input);
    });

    // Pickup checkbox
    this.on('#pickupToggle', 'change', (e: Event) => {
      const checkbox = e.target as HTMLInputElement;
      actions.updatePickup(checkbox.checked);
    });

    // Chain checkbox
    this.on('#chainToggle', 'change', (e: Event) => {
      const checkbox = e.target as HTMLInputElement;
      actions.updateChain(checkbox.checked);
    });

    // Scenario cards
    this.on(document, 'click', '.scenario-card', (e: Event) => {
      const card = (e.target as Element).closest('.scenario-card') as HTMLElement;
      const scenarioKey = card?.dataset.scenario;
      
      if (scenarioKey && this.scenarios[scenarioKey]) {
        this.loadScenario(scenarioKey);
      }
    });

    // Reset button
    this.on('#resetCalculator', 'click', () => {
      this.resetCalculator();
    });
  }

  subscribeToState(): void {
    // Subscribe to calculation changes
    this.unsubscribeStore = useStore.subscribe(
      (state) => state.calculator.calculation,
      (calculation) => {
        if (calculation) {
          this.updateUI(calculation);
        }
      }
    );

    // Also subscribe to individual value changes for UI updates
    useStore.subscribe(
      (state) => state.calculator.orderValue,
      (value) => this.updateDisplay('#orderValueDisplay', `€${this.formatNumber(value)}`)
    );

    useStore.subscribe(
      (state) => state.calculator.leadTime,
      (value) => this.updateDisplay('#leadTimeDisplay', `${value} Tage`)
    );
  }


  /**
   * Get calculator actions
   */
  private getActions() {
    // Create bound action functions
    const store = useStore.getState();
    return {
      updateOrderValue: (value: number) => {
        store.setOrderValue(value);
        store.updateCalculation();
      },
      updateLeadTime: (value: number) => {
        store.setLeadTime(value);
        store.updateCalculation();
      },
      updatePickup: (pickup: boolean) => {
        store.setPickup(pickup);
        store.updateCalculation();
      },
      updateChain: (chain: boolean) => {
        store.setChain(chain);
        store.updateCalculation();
      }
    };
  }

  /**
   * Initialize UI elements
   */
  private initializeUI(): void {
    const state = useStore.getState().calculator;
    
    // Set slider values
    const orderValueSlider = this.dom.$<HTMLInputElement>('#orderValue');
    if (orderValueSlider) {
      orderValueSlider.value = String(state.orderValue);
      this.updateSliderProgress(orderValueSlider);
    }
    
    const leadTimeSlider = this.dom.$<HTMLInputElement>('#leadTime');
    if (leadTimeSlider) {
      leadTimeSlider.value = String(state.leadTime);
      this.updateSliderProgress(leadTimeSlider);
    }
    
    // Set checkboxes
    const pickupCheckbox = this.dom.$<HTMLInputElement>('#pickupToggle');
    if (pickupCheckbox) {
      pickupCheckbox.checked = state.pickup;
    }
    
    const chainCheckbox = this.dom.$<HTMLInputElement>('#chainToggle');
    if (chainCheckbox) {
      chainCheckbox.checked = state.chain;
    }

    // Update displays
    if (state.calculation) {
      this.updateUI(state.calculation);
    }
  }

  /**
   * Update UI with calculation results
   */
  private updateUI(calculation: CalculationResult): void {
    // Update discount displays
    this.updateDisplay('#baseDiscount', `${calculation.baseDiscount}%`);
    this.updateDisplay('#earlyDiscount', `${calculation.earlyDiscount}%`);
    this.updateDisplay('#pickupDiscount', `${calculation.pickupDiscount}%`);
    this.updateDisplay('#chainDiscount', `${calculation.chainDiscount}%`);
    
    // Update totals
    this.updateDisplay('#totalDiscount', `${calculation.totalDiscount}%`);
    this.updateDisplay('#discountAmount', `€${this.formatNumber(Math.round(calculation.discountAmount))}`);
    this.updateDisplay('#finalPrice', `€${this.formatNumber(Math.round(calculation.finalPrice))}`);
    
    // Update visual indicators
    this.updateDiscountBar(calculation.totalDiscount);
    this.updateScenarioHighlight();
    
    // Emit event for other modules
    this.emit('calculated', calculation);
  }

  /**
   * Update display element
   */
  private updateDisplay(selector: string, text: string): void {
    const element = this.dom.$(selector);
    if (element) {
      element.textContent = text;
    }
  }

  /**
   * Format number with German locale
   */
  private formatNumber(value: number): string {
    return value.toLocaleString('de-DE');
  }

  /**
   * Update slider progress bar
   */
  private updateSliderProgress(slider: HTMLInputElement): void {
    const min = parseInt(slider.min, 10) || 0;
    const max = parseInt(slider.max, 10) || 100;
    const value = parseInt(slider.value, 10) || 0;
    
    const percentage = ((value - min) / (max - min)) * 100;
    slider.style.setProperty('--progress', `${percentage}%`);
  }

  /**
   * Update discount bar visualization
   */
  private updateDiscountBar(totalDiscount: number): void {
    const discountBar = this.dom.$<HTMLElement>('.discount-bar');
    if (discountBar) {
      discountBar.style.width = `${Math.min(totalDiscount * 5, 100)}%`;
      
      // Update color based on discount level
      this.dom.removeClass(discountBar, 'low', 'medium', 'high');
      if (totalDiscount >= 12) {
        this.dom.addClass(discountBar, 'high');
      } else if (totalDiscount >= 8) {
        this.dom.addClass(discountBar, 'medium');
      } else {
        this.dom.addClass(discountBar, 'low');
      }
    }
  }

  /**
   * Update scenario card highlighting
   */
  private updateScenarioHighlight(): void {
    const state = useStore.getState().calculator;
    
    // Remove all active states
    this.dom.$$('.scenario-card').forEach(card => {
      this.dom.removeClass(card, 'active');
    });
    
    // Find matching scenario
    Object.entries(this.scenarios).forEach(([key, scenario]) => {
      if (
        scenario.orderValue === state.orderValue &&
        scenario.leadTime === state.leadTime &&
        scenario.pickup === state.pickup &&
        scenario.chain === state.chain
      ) {
        const card = this.dom.$(`.scenario-card[data-scenario="${key}"]`);
        if (card) {
          this.dom.addClass(card, 'active');
        }
      }
    });
  }

  /**
   * Load a predefined scenario
   */
  private loadScenario(scenarioKey: string): void {
    const scenario = this.scenarios[scenarioKey];
    if (!scenario) return;
    
    const store = useStore.getState();
    
    // Update all values at once
    store.setOrderValue(scenario.orderValue);
    store.setLeadTime(scenario.leadTime);
    store.setPickup(scenario.pickup);
    store.setChain(scenario.chain);
    store.updateCalculation();
    
    // Update UI sliders
    this.initializeUI();
    
    // Show notification using Zustand store
    store.addNotification({
      type: 'info',
      message: `Szenario "${scenario.name}" geladen`,
      duration: 2000
    });
  }

  /**
   * Reset calculator to defaults
   */
  private resetCalculator(): void {
    const store = useStore.getState();
    
    // Reset to defaults
    store.setOrderValue(15000);
    store.setLeadTime(14);
    store.setPickup(false);
    store.setChain(false);
    store.updateCalculation();
    
    // Update UI
    this.initializeUI();
    
    // Show notification
    store.addNotification({
      type: 'info',
      message: 'Rechner zurückgesetzt',
      duration: 2000
    });
  }

  /**
   * Public API for other modules
   */
  
  getCalculation(): CalculationResult | null {
    return useStore.getState().calculator.calculation;
  }

  getTotalDiscount(): number {
    return selectors.getTotalDiscount(useStore.getState());
  }

  getFinalPrice(): number {
    return selectors.getFinalPrice(useStore.getState());
  }

  /**
   * Cleanup module resources
   */
  cleanup(): void {
    // Cleanup store subscription
    this.unsubscribeStore?.();
    // Module cleanup is handled by base class
  }
}