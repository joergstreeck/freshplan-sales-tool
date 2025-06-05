/**
 * Simplified integration tests for CustomerModuleV2
 * Tests the module without mocking the base class
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { LocalStorageCustomerRepository } from '../../infrastructure/repositories/LocalStorageCustomerRepository';
import { CustomerServiceV2 } from '../../services/CustomerServiceV2';

describe('CustomerModuleV2 Complete Integration', () => {
  let mockAlert: any;
  let mockConfirm: any;

  beforeEach(() => {
    // Mock window functions
    mockAlert = vi.fn();
    mockConfirm = vi.fn(() => true);
    window.alert = mockAlert;
    window.confirm = mockConfirm;
    
    // Clear localStorage
    localStorage.clear();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    localStorage.clear();
  });

  describe('Repository Pattern', () => {
    it('should implement ICustomerRepository interface', async () => {
      const repo = new LocalStorageCustomerRepository();
      
      // Test interface methods exist
      expect(typeof repo.saveCustomer).toBe('function');
      expect(typeof repo.getCustomerById).toBe('function');
      expect(typeof repo.getAllCustomers).toBe('function');
      expect(typeof repo.deleteAllCustomers).toBe('function');
      expect(typeof repo.getLegacyData).toBe('function');
    });

    it('should maintain data structure compatibility', async () => {
      const repo = new LocalStorageCustomerRepository();
      const testCustomer = {
        companyName: 'Test GmbH',
        legalForm: 'gmbh',
        customerType: 'single' as const,
        customerStatus: 'neukunde' as const,
        industry: 'hotel' as const,
        street: 'Teststraße 1',
        postalCode: '12345',
        city: 'Berlin',
        contactName: 'Max Mustermann',
        contactPhone: '0123456789',
        contactEmail: 'test@example.com',
        expectedVolume: '50000',
        paymentMethod: 'vorkasse' as const
      };

      await repo.saveCustomer(testCustomer);
      
      const savedData = JSON.parse(localStorage.getItem('freshplanData') || '{}');
      expect(savedData.customer).toEqual(testCustomer);
    });
  });

  describe('Service Layer', () => {
    it('should validate required fields', async () => {
      const repo = new LocalStorageCustomerRepository();
      const service = new CustomerServiceV2(repo);
      
      const invalidData = {
        companyName: '', // Empty required field
        legalForm: 'gmbh',
        customerType: 'single' as const,
        customerStatus: 'neukunde' as const,
        industry: 'hotel' as const,
        street: 'Teststraße 1',
        postalCode: '12345',
        city: 'Berlin',
        contactName: 'Max Mustermann',
        contactPhone: '0123456789',
        contactEmail: 'test@example.com',
        expectedVolume: '50000',
        paymentMethod: 'vorkasse' as const
      };

      await expect(service.saveCurrentCustomerData(invalidData))
        .rejects.toThrow('Validierung fehlgeschlagen');
    });

    it('should validate email format', async () => {
      const repo = new LocalStorageCustomerRepository();
      const service = new CustomerServiceV2(repo);
      
      const dataWithInvalidEmail = {
        companyName: 'Test GmbH',
        legalForm: 'gmbh',
        customerType: 'single' as const,
        customerStatus: 'neukunde' as const,
        industry: 'hotel' as const,
        street: 'Teststraße 1',
        postalCode: '12345',
        city: 'Berlin',
        contactName: 'Max Mustermann',
        contactPhone: '0123456789',
        contactEmail: 'invalid-email', // Invalid email
        expectedVolume: '50000',
        paymentMethod: 'vorkasse' as const
      };

      await expect(service.saveCurrentCustomerData(dataWithInvalidEmail))
        .rejects.toThrow('Validierung fehlgeschlagen');
    });

    it('should detect payment warning conditions', () => {
      const repo = new LocalStorageCustomerRepository();
      const service = new CustomerServiceV2(repo);
      
      expect(service.shouldShowPaymentWarning('neukunde', 'rechnung')).toBe(true);
      expect(service.shouldShowPaymentWarning('bestandskunde', 'rechnung')).toBe(false);
      expect(service.shouldShowPaymentWarning('neukunde', 'vorkasse')).toBe(false);
    });

    it('should emit events on save', async () => {
      const repo = new LocalStorageCustomerRepository();
      const mockEventBus = {
        emit: vi.fn(),
        on: vi.fn(),
        off: vi.fn()
      };
      const service = new CustomerServiceV2(repo, undefined, mockEventBus as any);
      
      const validData = {
        companyName: 'Test GmbH',
        legalForm: 'gmbh',
        customerType: 'single' as const,
        customerStatus: 'neukunde' as const,
        industry: 'hotel' as const,
        street: 'Teststraße 1',
        postalCode: '12345',
        city: 'Berlin',
        contactName: 'Max Mustermann',
        contactPhone: '0123456789',
        contactEmail: 'test@example.com',
        expectedVolume: '50000',
        paymentMethod: 'vorkasse' as const
      };

      await service.saveCurrentCustomerData(validData);
      
      expect(mockEventBus.emit).toHaveBeenCalledWith('customer:saved', expect.objectContaining({
        customer: validData
      }));
    });

    it('should emit credit check event for Neukunde + Rechnung', async () => {
      const repo = new LocalStorageCustomerRepository();
      const mockEventBus = {
        emit: vi.fn(),
        on: vi.fn(),
        off: vi.fn()
      };
      const service = new CustomerServiceV2(repo, undefined, mockEventBus as any);
      
      const riskData = {
        companyName: 'Risk GmbH',
        legalForm: 'gmbh',
        customerType: 'single' as const,
        customerStatus: 'neukunde' as const,
        industry: 'hotel' as const,
        street: 'Teststraße 1',
        postalCode: '12345',
        city: 'Berlin',
        contactName: 'Max Mustermann',
        contactPhone: '0123456789',
        contactEmail: 'test@example.com',
        expectedVolume: '50000',
        paymentMethod: 'rechnung' as const // Risk condition
      };

      await service.saveCurrentCustomerData(riskData);
      
      expect(mockEventBus.emit).toHaveBeenCalledWith('customer:creditCheckRequired', expect.objectContaining({
        customer: expect.objectContaining({
          companyName: 'Risk GmbH',
          customerStatus: 'neukunde',
          paymentMethod: 'rechnung'
        })
      }));
    });
  });

  describe('Data Persistence', () => {
    it('should preserve other data sections when saving customer', async () => {
      // Setup existing data
      const existingData = {
        calculator: { orderValue: 1000 },
        locations: { totalLocations: 5 }
      };
      localStorage.setItem('freshplanData', JSON.stringify(existingData));

      // Save customer data
      const repo = new LocalStorageCustomerRepository();
      const service = new CustomerServiceV2(repo);
      
      const customerData = {
        companyName: 'Test GmbH',
        legalForm: 'gmbh',
        customerType: 'single' as const,
        customerStatus: 'neukunde' as const,
        industry: 'hotel' as const,
        street: 'Teststraße 1',
        postalCode: '12345',
        city: 'Berlin',
        contactName: 'Max Mustermann',
        contactPhone: '0123456789',
        contactEmail: 'test@example.com',
        expectedVolume: '50000',
        paymentMethod: 'vorkasse' as const
      };

      await service.saveCurrentCustomerData(customerData);

      // Verify all data is preserved
      const savedData = JSON.parse(localStorage.getItem('freshplanData') || '{}');
      expect(savedData.calculator).toEqual({ orderValue: 1000 });
      expect(savedData.locations).toEqual({ totalLocations: 5 });
      expect(savedData.customer).toEqual(customerData);
    });

    it('should handle clear operation correctly', async () => {
      // Save data first
      const repo = new LocalStorageCustomerRepository();
      const service = new CustomerServiceV2(repo);
      
      const customerData = {
        companyName: 'Test GmbH',
        legalForm: 'gmbh',
        customerType: 'single' as const,
        customerStatus: 'neukunde' as const,
        industry: 'hotel' as const,
        street: 'Teststraße 1',
        postalCode: '12345',
        city: 'Berlin',
        contactName: 'Max Mustermann',
        contactPhone: '0123456789',
        contactEmail: 'test@example.com',
        expectedVolume: '50000',
        paymentMethod: 'vorkasse' as const
      };

      await service.saveCurrentCustomerData(customerData);
      
      // Clear customer data
      await service.clearAllCustomerData();
      
      // Verify customer data is cleared
      const savedData = JSON.parse(localStorage.getItem('freshplanData') || '{}');
      expect(savedData.customer).toBeUndefined();
    });
  });
});