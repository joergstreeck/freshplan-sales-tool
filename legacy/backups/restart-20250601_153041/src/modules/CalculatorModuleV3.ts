/**
 * Calculator Module V3 - Using CalculatorService
 * Demonstrates separation of business logic from DOM manipulation
 */

import Module from '../core/Module';
import { useStore } from '../store';
import { CalculatorService } from '../services';
import type { CalculatorCalculation } from '../types';

export default class CalculatorModuleV3 extends Module {
  private calculatorService: CalculatorService;

  constructor() {
    super('calculator');
    // Initialize the service with default rules
    this.calculatorService = new CalculatorService();
  }

  async setup(): Promise<void> {
    // Get current state
    const currentState = useStore.getState().calculator;
    
    if (currentState.calculation === null) {
      // Set default values and calculate using the service
      const calculation = this.calculatorService.calculateDiscount({
        orderValue: 15000,
        leadTime: 14,
        pickup: false,
        chain: false
      });
      
      // Update store with calculation
      useStore.getState().updateCalculation(calculation);
      useStore.getState().setOrderValue(calculation.orderValue);
      useStore.getState().setLeadTime(calculation.leadTime);
      useStore.getState().setPickup(calculation.pickup);
      useStore.getState().setChain(calculation.chain);
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
      this.recalculate();
    });

    // Lead time slider
    this.on('#leadTime', 'input', (e: Event) => {
      const input = e.target as HTMLInputElement;
      const value = parseInt(input.value);
      useStore.getState().setLeadTime(value);
      this.updateSliderProgress(input);
      this.recalculate();
    });

    // Pickup checkbox
    this.on('#pickupToggle', 'change', (e: Event) => {
      const checkbox = e.target as HTMLInputElement;
      useStore.getState().setPickup(checkbox.checked);
      this.recalculate();
    });

    // Chain checkbox
    this.on('#chainToggle', 'change', (e: Event) => {
      const checkbox = e.target as HTMLInputElement;
      useStore.getState().setChain(checkbox.checked);
      this.recalculate();
    });

    // Scenario clicks
    this.on(document, 'click', '.scenario-card', (e: Event) => {
      const card = (e.target as Element).closest('.scenario-card') as HTMLElement;
      const scenarioName = card?.dataset.scenario;
      if (scenarioName) {
        this.loadScenario(scenarioName);
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
   * Recalculate using the service
   */
  private recalculate(): void {
    const state = useStore.getState().calculator;
    
    try {
      const calculation = this.calculatorService.calculateDiscount({
        orderValue: state.orderValue,
        leadTime: state.leadTime,
        pickup: state.pickup,
        chain: state.chain
      });
      
      useStore.getState().updateCalculation(calculation);
    } catch (error) {
      this.showError(`Calculation error: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  }

  /**
   * Load predefined scenario using the service
   */
  private loadScenario(scenarioName: string): void {
    const scenarioInput = this.calculatorService.applyScenario(scenarioName);
    
    if (scenarioInput) {
      const store = useStore.getState();
      store.setOrderValue(scenarioInput.orderValue);
      store.setLeadTime(scenarioInput.leadTime);
      store.setPickup(scenarioInput.pickup);
      store.setChain(scenarioInput.chain);
      
      // Recalculate with new values
      this.recalculate();
      
      this.showSuccess(`Scenario "${scenarioName}" loaded`);
    } else {
      this.showError(`Unknown scenario: ${scenarioName}`);
    }
  }

  /**
   * Update UI with calculation results
   */
  private updateUI(): void {
    const state = useStore.getState().calculator;
    const calculation = state.calculation;
    
    if (!calculation) return;

    // Update value displays
    this.updateValueDisplays(calculation);
    
    // Update discount displays
    this.updateDiscountDisplays(calculation);
    
    // Update progress bars
    this.updateProgressBars(calculation);
    
    // Update form controls
    this.updateFormControls(state);
    
    // Show next threshold hint
    this.updateThresholdHint(calculation.orderValue);
  }

  /**
   * Update value displays
   */
  private updateValueDisplays(calculation: CalculatorCalculation): void {
    this.dom.text('#orderValueDisplay', `€${calculation.orderValue.toLocaleString('de-DE')}`);
    this.dom.text('#leadTimeDisplay', `${calculation.leadTime} Tage`);
    this.dom.text('#savingsAmount', `€${calculation.savingsAmount.toLocaleString('de-DE')}`);
    this.dom.text('#finalPrice', `€${calculation.finalPrice.toLocaleString('de-DE')}`);
  }

  /**
   * Update discount displays
   */
  private updateDiscountDisplays(calculation: CalculatorCalculation): void {
    const breakdown = this.calculatorService.getDiscountBreakdown(calculation);
    
    this.dom.text('#baseDiscount', `${calculation.baseDiscount}%`);
    this.dom.text('#earlyBookingDiscount', `${calculation.earlyDiscount}%`);
    this.dom.text('#pickupDiscount', `${calculation.pickupDiscount}%`);
    this.dom.text('#totalDiscount', `${calculation.totalDiscount}%`);
    
    // Update discount component visibility
    breakdown.components.forEach(component => {
      const elementId = this.getDiscountElementId(component.name);
      const element = this.dom.$(`#${elementId}`);
      if (element) {
        this.dom.toggleClass(element, 'active', component.active);
      }
    });
  }

  /**
   * Update progress bars
   */
  private updateProgressBars(calculation: CalculatorCalculation): void {
    this.updateProgressBar('baseDiscountBar', calculation.baseDiscount);
    this.updateProgressBar('earlyDiscountBar', calculation.earlyDiscount);
    this.updateProgressBar('pickupDiscountBar', calculation.pickupDiscount);
  }

  /**
   * Update form controls
   */
  private updateFormControls(state: any): void {
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

    const pickupToggle = this.dom.$('#pickupToggle') as HTMLInputElement;
    const chainToggle = this.dom.$('#chainToggle') as HTMLInputElement;
    
    if (pickupToggle) pickupToggle.checked = state.pickup;
    if (chainToggle) chainToggle.checked = state.chain;
  }

  /**
   * Update threshold hint
   */
  private updateThresholdHint(currentValue: number): void {
    const nextThreshold = this.calculatorService.getNextDiscountThreshold(currentValue);
    const hintElement = this.dom.$('#thresholdHint');
    
    if (hintElement && nextThreshold) {
      const hint = `Add €${nextThreshold.amountNeeded.toLocaleString('de-DE')} to reach ${nextThreshold.additionalDiscount}% more discount`;
      this.dom.text(hintElement, hint);
      this.dom.show(hintElement);
    } else if (hintElement) {
      this.dom.hide(hintElement);
    }
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
   * Get discount element ID from component name
   */
  private getDiscountElementId(componentName: string): string {
    const idMap: Record<string, string> = {
      'Base Discount': 'baseDiscountComponent',
      'Early Booking': 'earlyBookingComponent',
      'Pickup Discount': 'pickupDiscountComponent'
    };
    
    return idMap[componentName] || '';
  }

  /**
   * Public API
   */
  
  getCalculation(): CalculatorCalculation | null {
    return useStore.getState().calculator.calculation;
  }

  getDiscountRules() {
    return this.calculatorService.getDiscountRules();
  }

  exportCalculation(): string | null {
    const calculation = this.getCalculation();
    if (!calculation) return null;
    
    return this.calculatorService.exportCalculation(calculation);
  }

  /**
   * Cleanup module resources
   */
  cleanup(): void {
    // Module cleanup is handled by base class
  }
}