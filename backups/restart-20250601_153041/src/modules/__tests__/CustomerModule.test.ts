/**
 * Tests for CustomerModule with Credit Check functionality
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { JSDOM } from 'jsdom';
import CustomerModule from '../CustomerModule';
import { useStore } from '../../store';

describe('CustomerModule - Credit Check Integration', () => {
  let dom: JSDOM;
  let module: CustomerModule;
  let document: Document;

  beforeEach(() => {
    // Setup DOM
    dom = new JSDOM(`
      <!DOCTYPE html>
      <html>
        <body>
          <div id="customer" class="tab-panel" role="tabpanel">
            <div class="customer-container">
              <div id="newCustomerAlert" class="alert-box warning hidden">
                <button type="button" id="requestCreditCheck">Bonitätsprüfung anfragen</button>
                <button type="button" id="requestManagement">Anfrage an Geschäftsleitung</button>
              </div>
              
              <form id="customerForm" class="customer-form">
                <input type="text" id="companyName" name="companyName" required>
                <select id="legalForm" name="legalForm" required>
                  <option value="">Bitte wählen</option>
                  <option value="gmbh">GmbH</option>
                </select>
                <select id="customerType" name="customerType" required>
                  <option value="">Bitte wählen</option>
                  <option value="neukunde">Neukunde</option>
                  <option value="bestandskunde">Bestandskunde</option>
                </select>
                <select id="industry" name="industry" required>
                  <option value="">Bitte wählen</option>
                  <option value="hotel">Hotel</option>
                </select>
                <input type="text" id="ustId" name="ustId" required>
                <input type="text" id="contactName" name="contactName" required>
                <input type="email" id="contactEmail" name="contactEmail" required>
                <input type="tel" id="contactPhone" name="contactPhone" required>
                <input type="text" id="street" name="street" required>
                <input type="text" id="postalCode" name="postalCode" required>
                <input type="text" id="city" name="city" required>
                <input type="number" id="expectedVolume" name="expectedVolume" required>
                <select id="paymentTerms" name="paymentTerms">
                  <option value="0">Sofort (Vorkasse/Bar)</option>
                  <option value="30">30 Tage</option>
                </select>
                
                <div id="creditCheckStatus" class="credit-status">
                  <span id="creditStatus" class="status-value">Nicht geprüft</span>
                  <span id="creditLimit" class="status-value">-</span>
                  <span id="creditRating" class="status-value">-</span>
                </div>
                
                <button type="submit">Speichern</button>
                <button type="button" id="clearForm">Formular leeren</button>
              </form>
            </div>
          </div>
          
          <div id="notifications" class="notifications-container"></div>
        </body>
      </html>
    `);

    document = dom.window.document;
    global.document = document as any;
    global.window = dom.window as any;
    global.HTMLElement = dom.window.HTMLElement as any;

    // Reset store
    useStore.getState().clearCustomer();

    // Create module instance
    module = new CustomerModule();
  });

  afterEach(() => {
    vi.clearAllMocks();
    module.cleanup();
  });

  describe('New Customer Alert', () => {
    it('should show alert when selecting Neukunde', async () => {
      await module.setup();
      module.bindEvents();
      module.subscribeToState();

      const customerTypeSelect = document.getElementById('customerType') as HTMLSelectElement;
      const alert = document.getElementById('newCustomerAlert');

      // Initially hidden
      expect(alert?.classList.contains('hidden')).toBe(true);

      // Select Neukunde
      customerTypeSelect.value = 'neukunde';
      customerTypeSelect.dispatchEvent(new dom.window.Event('change', { bubbles: true }));

      // Alert should be visible
      expect(alert?.classList.contains('hidden')).toBe(false);
    });

    it('should hide alert when selecting Bestandskunde', async () => {
      await module.setup();
      module.bindEvents();
      module.subscribeToState();

      const customerTypeSelect = document.getElementById('customerType') as HTMLSelectElement;
      const alert = document.getElementById('newCustomerAlert');

      // First select Neukunde
      customerTypeSelect.value = 'neukunde';
      customerTypeSelect.dispatchEvent(new dom.window.Event('change', { bubbles: true }));
      expect(alert?.classList.contains('hidden')).toBe(false);

      // Then select Bestandskunde
      customerTypeSelect.value = 'bestandskunde';
      customerTypeSelect.dispatchEvent(new dom.window.Event('change', { bubbles: true }));
      expect(alert?.classList.contains('hidden')).toBe(true);
    });
  });

  describe('Form Validation', () => {
    it('should validate required fields', async () => {
      await module.setup();
      module.bindEvents();

      const isValid = module.isValid();
      expect(isValid).toBe(false); // Empty form should be invalid
    });

    it('should validate email format', async () => {
      await module.setup();
      module.bindEvents();

      const emailInput = document.getElementById('contactEmail') as HTMLInputElement;
      
      // Invalid email
      emailInput.value = 'invalid-email';
      emailInput.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
      
      // Should have error class
      expect(emailInput.classList.contains('error')).toBe(true);

      // Valid email
      emailInput.value = 'test@example.com';
      emailInput.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
      
      // Should not have error class
      expect(emailInput.classList.contains('error')).toBe(false);
    });

    it('should validate postal code format', async () => {
      await module.setup();
      module.bindEvents();

      const postalCodeInput = document.getElementById('postalCode') as HTMLInputElement;
      
      // Invalid postal code
      postalCodeInput.value = '123';
      postalCodeInput.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
      
      // Should have error class
      expect(postalCodeInput.classList.contains('error')).toBe(true);

      // Valid postal code
      postalCodeInput.value = '12345';
      postalCodeInput.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
      
      // Should not have error class
      expect(postalCodeInput.classList.contains('error')).toBe(false);
    });
  });

  describe('Form Data Management', () => {
    it('should save form data to store', async () => {
      await module.setup();
      module.bindEvents();

      // Fill form
      (document.getElementById('companyName') as HTMLInputElement).value = 'Test GmbH';
      (document.getElementById('contactEmail') as HTMLInputElement).value = 'test@example.com';
      
      // Trigger save
      const form = document.getElementById('customerForm') as HTMLFormElement;
      
      // Fill all required fields
      (document.getElementById('legalForm') as HTMLSelectElement).value = 'gmbh';
      (document.getElementById('customerType') as HTMLSelectElement).value = 'neukunde';
      (document.getElementById('industry') as HTMLSelectElement).value = 'hotel';
      (document.getElementById('ustId') as HTMLInputElement).value = 'DE123456789';
      (document.getElementById('contactName') as HTMLInputElement).value = 'Max Mustermann';
      (document.getElementById('contactPhone') as HTMLInputElement).value = '+49 30 12345678';
      (document.getElementById('street') as HTMLInputElement).value = 'Teststraße 1';
      (document.getElementById('postalCode') as HTMLInputElement).value = '12345';
      (document.getElementById('city') as HTMLInputElement).value = 'Berlin';
      (document.getElementById('expectedVolume') as HTMLInputElement).value = '10000';

      // Submit form
      form.dispatchEvent(new dom.window.Event('submit', { bubbles: true, cancelable: true }));

      // Check store
      const state = useStore.getState();
      expect(state.customer.data?.companyName).toBe('Test GmbH');
      expect(state.customer.data?.contactEmail).toBe('test@example.com');
    });

    it('should clear form data', async () => {
      await module.setup();
      module.bindEvents();

      // Fill some data
      (document.getElementById('companyName') as HTMLInputElement).value = 'Test GmbH';
      
      // Mock confirm dialog
      global.confirm = vi.fn(() => true);

      // Clear form
      const clearButton = document.getElementById('clearForm');
      clearButton?.dispatchEvent(new dom.window.Event('click', { bubbles: true }));

      // Check form is cleared
      expect((document.getElementById('companyName') as HTMLInputElement).value).toBe('');
      
      // Check store is cleared
      const state = useStore.getState();
      expect(state.customer.data).toBeNull();
    });
  });
});