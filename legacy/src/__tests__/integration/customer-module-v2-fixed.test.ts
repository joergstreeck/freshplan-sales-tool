/**
 * Fixed integration tests for CustomerModuleV2
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
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
        <select id="customerType"><option value="single" selected>Einzelstandort</option></select>
        <select id="customerStatus"><option value="neukunde" selected>Neukunde</option></select>
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

// Helper to get form data
const getFormData = (): CustomerData => {
  return {
    companyName: (document.getElementById('companyName') as HTMLInputElement).value,
    legalForm: (document.getElementById('legalForm') as HTMLSelectElement).value,
    customerType: (document.getElementById('customerType') as HTMLSelectElement).value,
    customerStatus: (document.getElementById('customerStatus') as HTMLSelectElement).value as 'neukunde' | 'bestandskunde',
    industry: (document.getElementById('industry') as HTMLSelectElement).value as any,
    street: (document.getElementById('street') as HTMLInputElement).value,
    postalCode: (document.getElementById('postalCode') as HTMLInputElement).value,
    city: (document.getElementById('city') as HTMLInputElement).value,
    contactName: (document.getElementById('contactName') as HTMLInputElement).value,
    contactPhone: (document.getElementById('contactPhone') as HTMLInputElement).value,
    contactEmail: (document.getElementById('contactEmail') as HTMLInputElement).value,
    expectedVolume: (document.getElementById('expectedVolume') as HTMLInputElement).value,
    paymentMethod: (document.getElementById('paymentMethod') as HTMLSelectElement).value as any,
  };
};

describe('CustomerModuleV2 Service Integration', () => {
  let repository: LocalStorageCustomerRepository;
  let service: CustomerServiceV2;
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
    
    // Create service instances
    repository = new LocalStorageCustomerRepository();
    service = new CustomerServiceV2(repository);
  });

  afterEach(() => {
    // Cleanup
    vi.restoreAllMocks();
    localStorage.clear();
  });

  describe('Service Functionality', () => {
    it('should save valid customer data', async () => {
      const formData = getFormData();
      await service.saveCurrentCustomerData(formData);
      
      const savedData = JSON.parse(localStorage.getItem('freshplanData') || '{}');
      expect(savedData.customer).toBeDefined();
      expect(savedData.customer.companyName).toBe('Test GmbH');
    });

    it('should throw validation error for invalid data', async () => {
      const invalidData = { companyName: '' } as CustomerData;
      
      await expect(service.saveCurrentCustomerData(invalidData))
        .rejects.toThrow('Validierung fehlgeschlagen');
    });

    it('should detect payment warning condition', () => {
      const shouldWarn = service.shouldShowPaymentWarning('neukunde', 'rechnung');
      expect(shouldWarn).toBe(true);
      
      const shouldNotWarn = service.shouldShowPaymentWarning('bestandskunde', 'rechnung');
      expect(shouldNotWarn).toBe(false);
    });

    it('should clear customer data', async () => {
      // Save data first
      const formData = getFormData();
      await service.saveCurrentCustomerData(formData);
      
      // Clear data
      await service.clearAllCustomerData();
      
      const savedData = JSON.parse(localStorage.getItem('freshplanData') || '{}');
      expect(savedData.customer).toBeUndefined();
    });

    it('should load saved customer data', async () => {
      const testData = {
        customer: {
          companyName: 'Saved Company',
          contactEmail: 'saved@example.com'
        }
      };
      localStorage.setItem('freshplanData', JSON.stringify(testData));
      
      const loadedData = await service.loadInitialCustomerData();
      expect(loadedData).toBeDefined();
      expect(loadedData?.companyName).toBe('Saved Company');
    });
  });

  describe('Repository Functionality', () => {
    it('should maintain backward compatibility with legacy data structure', async () => {
      const customerData = getFormData();
      await repository.saveCustomer(customerData);
      
      const rawData = localStorage.getItem('freshplanData');
      expect(rawData).toBeDefined();
      
      const parsedData = JSON.parse(rawData!);
      expect(parsedData.customer).toBeDefined();
      expect(parsedData.customer.companyName).toBe('Test GmbH');
    });

    it('should preserve other data when saving customer', async () => {
      // Set up existing data
      const existingData = {
        calculator: { someData: 'test' },
        locations: { otherData: 'test' }
      };
      localStorage.setItem('freshplanData', JSON.stringify(existingData));
      
      // Save customer data
      const customerData = getFormData();
      await repository.saveCustomer(customerData);
      
      // Check that other data is preserved
      const savedData = JSON.parse(localStorage.getItem('freshplanData')!);
      expect(savedData.calculator).toEqual({ someData: 'test' });
      expect(savedData.locations).toEqual({ otherData: 'test' });
      expect(savedData.customer).toBeDefined();
    });
  });

  describe('Validation', () => {
    it('should validate required fields', async () => {
      const incompleteData = {
        companyName: 'Test',
        // Missing other required fields
      } as CustomerData;
      
      await expect(service.saveCurrentCustomerData(incompleteData))
        .rejects.toThrow();
    });

    it('should validate email format', async () => {
      const dataWithInvalidEmail = {
        ...getFormData(),
        contactEmail: 'invalid-email'
      };
      
      await expect(service.saveCurrentCustomerData(dataWithInvalidEmail))
        .rejects.toThrow();
    });

    it('should validate postal code format', async () => {
      const dataWithInvalidPostalCode = {
        ...getFormData(),
        postalCode: '123' // Too short
      };
      
      await expect(service.saveCurrentCustomerData(dataWithInvalidPostalCode))
        .rejects.toThrow();
    });
  });
});