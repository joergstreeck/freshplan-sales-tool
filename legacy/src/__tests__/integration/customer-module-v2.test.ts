/**
 * Integration tests for CustomerModuleV2
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import CustomerModuleV2 from '../../modules/CustomerModuleV2';
import { LocalStorageCustomerRepository } from '../../infrastructure/repositories/LocalStorageCustomerRepository';
import { CustomerServiceV2 } from '../../services/CustomerServiceV2';
import type { CustomerData } from '../../types';

// Mock DOM environment
const createMockDOM = () => {
  document.body.innerHTML = `
    <div class="tab-panel active" id="customer">
      <form id="customerForm">
        <input id="companyName" value="Test GmbH" />
        <select id="legalForm"><option value="gmbh" selected>GmbH</option></select>
        <select id="customerType"><option value="neukunde" selected>Neukunde</option></select>
        <select id="industry"><option value="hotel" selected>Hotel</option></select>
        <input id="street" value="Teststraße 1" />
        <input id="postalCode" value="12345" />
        <input id="city" value="Berlin" />
        <input id="contactName" value="Max Mustermann" />
        <input id="contactPhone" value="0123456789" />
        <input id="contactEmail" value="test@example.com" />
        <input id="expectedVolume" value="50000" />
        <select id="paymentMethod"><option value="vorkasse" selected>Vorkasse</option></select>
        <select id="chainCustomer"><option value="nein" selected>Nein</option></select>
      </form>
    </div>
    <button class="header-btn-save">Speichern</button>
    <button class="header-btn-clear">Formular leeren</button>
    <div class="nav-tab" data-tab="locations" style="display: none;"></div>
  `;
};

describe('CustomerModuleV2 Integration', () => {
  let module: CustomerModuleV2;
  let mockAlert: any;
  let mockConfirm: any;

  beforeEach(() => {
    // Setup DOM
    createMockDOM();
    
    // Mock window functions
    mockAlert = vi.fn();
    mockConfirm = vi.fn(() => true);
    window.alert = mockAlert;
    window.confirm = mockConfirm;
    
    // Clear localStorage
    localStorage.clear();
    
    // Create module instance
    module = new CustomerModuleV2();
  });

  afterEach(() => {
    // Cleanup
    vi.restoreAllMocks();
    localStorage.clear();
  });

  describe('Module Setup', () => {
    it('should initialize with repository and service', async () => {
      await module.setup();
      expect(module['customerService']).toBeDefined();
      expect(module['customerService']).toBeInstanceOf(CustomerServiceV2);
    });

    it('should load saved customer data on setup', async () => {
      const testData = {
        customer: {
          companyName: 'Saved Company',
          contactEmail: 'saved@example.com'
        }
      };
      localStorage.setItem('freshplanData', JSON.stringify(testData));
      
      await module.setup();
      
      const companyNameInput = document.getElementById('companyName') as HTMLInputElement;
      expect(companyNameInput.value).toBe('Saved Company');
    });
  });

  describe('Save Functionality', () => {
    beforeEach(async () => {
      await module.setup();
      module.bindEvents();
      module.subscribeToState();
    });

    it('should save valid customer data', async () => {
      const saveButton = document.querySelector('.header-btn-save') as HTMLButtonElement;
      saveButton.click();
      
      // Wait for async operations
      await new Promise(resolve => setTimeout(resolve, 100));
      
      const savedData = JSON.parse(localStorage.getItem('freshplanData') || '{}');
      expect(savedData.customer).toBeDefined();
      expect(savedData.customer.companyName).toBe('Test GmbH');
      expect(mockAlert).toHaveBeenCalledWith('✓ Daten wurden erfolgreich gespeichert!');
    });

    it('should show validation errors for invalid data', async () => {
      // Clear required field
      (document.getElementById('companyName') as HTMLInputElement).value = '';
      
      const saveButton = document.querySelector('.header-btn-save') as HTMLButtonElement;
      saveButton.click();
      
      await new Promise(resolve => setTimeout(resolve, 100));
      
      expect(mockAlert).toHaveBeenCalledWith(expect.stringContaining('Pflichtfelder'));
    });

    it('should show warning for Neukunde + Rechnung', async () => {
      (document.getElementById('customerType') as HTMLSelectElement).value = 'neukunde';
      (document.getElementById('paymentMethod') as HTMLSelectElement).value = 'rechnung';
      
      const saveButton = document.querySelector('.header-btn-save') as HTMLButtonElement;
      saveButton.click();
      
      await new Promise(resolve => setTimeout(resolve, 100));
      
      expect(mockAlert).toHaveBeenCalledWith(expect.stringContaining('Bonitätsprüfung'));
    });
  });

  describe('Clear Functionality', () => {
    beforeEach(async () => {
      await module.setup();
      module.bindEvents();
      
      // Save some data first
      const testData = { customer: { companyName: 'Test' } };
      localStorage.setItem('freshplanData', JSON.stringify(testData));
    });

    it('should clear customer data when confirmed', async () => {
      const clearButton = document.querySelector('.header-btn-clear') as HTMLButtonElement;
      clearButton.click();
      
      await new Promise(resolve => setTimeout(resolve, 100));
      
      expect(mockConfirm).toHaveBeenCalled();
      const savedData = JSON.parse(localStorage.getItem('freshplanData') || '{}');
      expect(savedData.customer).toBeUndefined();
    });

    it('should not clear data when cancelled', async () => {
      mockConfirm.mockReturnValue(false);
      
      const clearButton = document.querySelector('.header-btn-clear') as HTMLButtonElement;
      clearButton.click();
      
      await new Promise(resolve => setTimeout(resolve, 100));
      
      const savedData = JSON.parse(localStorage.getItem('freshplanData') || '{}');
      expect(savedData.customer).toBeDefined();
    });
  });

  describe('Form Interactions', () => {
    beforeEach(async () => {
      await module.setup();
      module.bindEvents();
    });

    it('should toggle locations tab for chain customers', () => {
      const locationsTab = document.querySelector('.nav-tab[data-tab="locations"]') as HTMLElement;
      const chainSelect = document.getElementById('chainCustomer') as HTMLSelectElement;
      
      // Select chain customer
      chainSelect.value = 'ja';
      chainSelect.dispatchEvent(new Event('change'));
      
      expect(locationsTab.style.display).toBe('block');
      
      // Deselect chain customer
      chainSelect.value = 'nein';
      chainSelect.dispatchEvent(new Event('change'));
      
      expect(locationsTab.style.display).toBe('none');
    });

    it('should format currency input', () => {
      const volumeInput = document.getElementById('expectedVolume') as HTMLInputElement;
      volumeInput.value = '50000';
      volumeInput.dispatchEvent(new Event('input'));
      
      expect(volumeInput.value).toBe('50.000');
    });

    it('should show payment warning immediately on change', () => {
      const customerType = document.getElementById('customerType') as HTMLSelectElement;
      const paymentMethod = document.getElementById('paymentMethod') as HTMLSelectElement;
      
      customerType.value = 'neukunde';
      paymentMethod.value = 'rechnung';
      paymentMethod.dispatchEvent(new Event('change'));
      
      expect(mockAlert).toHaveBeenCalledWith(expect.stringContaining('Bonitätsprüfung'));
    });
  });
});