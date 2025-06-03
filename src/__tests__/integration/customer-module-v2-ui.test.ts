/**
 * UI Integration tests for CustomerModuleV2
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import CustomerModuleV2 from '../../modules/CustomerModuleV2';
import { MockEventBus } from '../../test/mocks/Module.mock';

// Mock the Module base class
vi.mock('../../core/Module', () => {
  return {
    default: class MockModule {
      protected events = new MockEventBus();
      protected dom = {
        $: (selector: string) => document.querySelector(selector),
        $$: (selector: string) => Array.from(document.querySelectorAll(selector)),
        ready: (cb: Function) => cb()
      };
      private listeners = new Map<string, Map<EventTarget, EventListener>>();

      protected on(target: EventTarget | null, event: string, handler: EventListener): void {
        if (!target) return;
        target.addEventListener(event, handler);
        
        // Track for cleanup
        if (!this.listeners.has(event)) {
          this.listeners.set(event, new Map());
        }
        this.listeners.get(event)!.set(target, handler);
      }

      protected emit(event: string, data?: any): void {
        this.events.emit(event, data);
      }

      cleanup(): void {
        this.listeners.forEach((targets, event) => {
          targets.forEach((handler, target) => {
            target.removeEventListener(event, handler);
          });
        });
        this.listeners.clear();
      }

      async setup(): Promise<void> {}
      bindEvents(): void {}
      subscribeToState(): void {}
    }
  };
});

// Mock EventBus
vi.mock('../../core/EventBus', () => ({
  default: MockEventBus
}));

// Mock DOM environment
const createMockDOM = () => {
  document.body.innerHTML = `
    <div class="tab-panel active" id="customer">
      <form id="customerForm">
        <input id="companyName" />
        <select id="legalForm">
          <option value="">Bitte wählen</option>
          <option value="gmbh">GmbH</option>
        </select>
        <select id="customerType">
          <option value="">Bitte wählen</option>
          <option value="single">Einzelstandort</option>
          <option value="chain">Kette</option>
        </select>
        <select id="customerStatus">
          <option value="">Bitte wählen</option>
          <option value="neukunde">Neukunde</option>
          <option value="bestandskunde">Bestandskunde</option>
        </select>
        <select id="industry">
          <option value="">Bitte wählen</option>
          <option value="hotel">Hotel</option>
        </select>
        <input id="street" />
        <input id="postalCode" />
        <input id="city" />
        <input id="contactName" />
        <input id="contactPhone" />
        <input id="contactEmail" />
        <input id="expectedVolume" />
        <select id="paymentMethod">
          <option value="">Bitte wählen</option>
          <option value="vorkasse">Vorkasse</option>
          <option value="rechnung">Rechnung</option>
        </select>
        <select id="chainCustomer">
          <option value="nein">Nein</option>
          <option value="ja">Ja</option>
        </select>
      </form>
    </div>
    <button class="header-btn-save">Speichern</button>
    <button class="header-btn-clear">Formular leeren</button>
    <div class="nav-tab" data-tab="locations" style="display: none;"></div>
  `;
};

describe('CustomerModuleV2 UI Integration', () => {
  let module: CustomerModuleV2;
  let mockAlert: any;
  let mockConfirm: any;

  beforeEach(async () => {
    // Setup DOM
    createMockDOM();
    
    // Mock window functions
    mockAlert = vi.fn();
    mockConfirm = vi.fn(() => true);
    window.alert = mockAlert;
    window.confirm = mockConfirm;
    
    // Clear localStorage
    localStorage.clear();
    
    // Create and setup module
    module = new CustomerModuleV2();
    await module.setup();
    module.bindEvents();
  });

  afterEach(() => {
    // Cleanup
    if (module && typeof module.cleanup === 'function') {
      module.cleanup();
    }
    vi.restoreAllMocks();
    localStorage.clear();
  });

  describe('Form Interactions', () => {
    it('should save form data when save button is clicked', async () => {
      // Fill form
      (document.getElementById('companyName') as HTMLInputElement).value = 'Test Company';
      (document.getElementById('legalForm') as HTMLSelectElement).value = 'gmbh';
      (document.getElementById('customerType') as HTMLSelectElement).value = 'single';
      (document.getElementById('customerStatus') as HTMLSelectElement).value = 'neukunde';
      (document.getElementById('industry') as HTMLSelectElement).value = 'hotel';
      (document.getElementById('street') as HTMLInputElement).value = 'Test Street';
      (document.getElementById('postalCode') as HTMLInputElement).value = '12345';
      (document.getElementById('city') as HTMLInputElement).value = 'Test City';
      (document.getElementById('contactName') as HTMLInputElement).value = 'Test Contact';
      (document.getElementById('contactPhone') as HTMLInputElement).value = '0123456789';
      (document.getElementById('contactEmail') as HTMLInputElement).value = 'test@example.com';
      (document.getElementById('expectedVolume') as HTMLInputElement).value = '10000';
      (document.getElementById('paymentMethod') as HTMLSelectElement).value = 'vorkasse';

      // Click save
      const saveButton = document.querySelector('.header-btn-save') as HTMLButtonElement;
      saveButton.click();

      // Wait for async operations
      await new Promise(resolve => setTimeout(resolve, 100));

      // Check localStorage
      const savedData = JSON.parse(localStorage.getItem('freshplanData') || '{}');
      expect(savedData.customer).toBeDefined();
      expect(savedData.customer.companyName).toBe('Test Company');
      expect(mockAlert).toHaveBeenCalledWith('✓ Daten wurden erfolgreich gespeichert!');
    });

    it('should show validation error for empty required fields', async () => {
      // Leave form empty and click save
      const saveButton = document.querySelector('.header-btn-save') as HTMLButtonElement;
      saveButton.click();

      await new Promise(resolve => setTimeout(resolve, 100));

      expect(mockAlert).toHaveBeenCalledWith(expect.stringContaining('Pflichtfelder'));
    });

    it('should clear form when clear button is clicked', async () => {
      // Fill some data
      (document.getElementById('companyName') as HTMLInputElement).value = 'Test Company';

      // Click clear
      const clearButton = document.querySelector('.header-btn-clear') as HTMLButtonElement;
      clearButton.click();

      await new Promise(resolve => setTimeout(resolve, 100));

      expect(mockConfirm).toHaveBeenCalled();
      expect((document.getElementById('companyName') as HTMLInputElement).value).toBe('');
    });

    it('should toggle locations tab visibility based on chain customer selection', () => {
      const chainSelect = document.getElementById('chainCustomer') as HTMLSelectElement;
      const locationsTab = document.querySelector('.nav-tab[data-tab="locations"]') as HTMLElement;

      // Select chain customer
      chainSelect.value = 'ja';
      chainSelect.dispatchEvent(new Event('change'));
      expect(locationsTab.style.display).toBe('block');

      // Deselect chain customer
      chainSelect.value = 'nein';
      chainSelect.dispatchEvent(new Event('change'));
      expect(locationsTab.style.display).toBe('none');
    });

    it('should show payment warning for Neukunde + Rechnung', () => {
      const customerStatus = document.getElementById('customerStatus') as HTMLSelectElement;
      const paymentMethod = document.getElementById('paymentMethod') as HTMLSelectElement;

      customerStatus.value = 'neukunde';
      paymentMethod.value = 'rechnung';
      paymentMethod.dispatchEvent(new Event('change'));

      expect(mockAlert).toHaveBeenCalledWith(expect.stringContaining('Bonitätsprüfung'));
    });

    it('should format currency input', () => {
      const volumeInput = document.getElementById('expectedVolume') as HTMLInputElement;
      
      volumeInput.value = '50000';
      volumeInput.dispatchEvent(new Event('input'));
      
      expect(volumeInput.value).toBe('50.000');
    });
  });

  describe('Data Persistence', () => {
    it('should load saved data on setup', async () => {
      // Save data to localStorage
      const testData = {
        customer: {
          companyName: 'Existing Company',
          contactEmail: 'existing@example.com',
          legalForm: 'gmbh'
        }
      };
      localStorage.setItem('freshplanData', JSON.stringify(testData));

      // Create new module instance
      const newModule = new CustomerModuleV2();
      await newModule.setup();

      // Check if form is populated
      expect((document.getElementById('companyName') as HTMLInputElement).value).toBe('Existing Company');
      expect((document.getElementById('contactEmail') as HTMLInputElement).value).toBe('existing@example.com');
      expect((document.getElementById('legalForm') as HTMLSelectElement).value).toBe('gmbh');
    });
  });
});