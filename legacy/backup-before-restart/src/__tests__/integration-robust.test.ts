/**
 * Robust Integration Tests
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createTestStore } from '@/store/createStore';
import { IntegrationService } from '@/services';
import EventBus from '@/core/EventBus';

describe('Integration Tests (Robust)', () => {
  let store: ReturnType<typeof createTestStore>;
  let integrationService: IntegrationService;

  beforeEach(() => {
    // Fresh instances for each test
    store = createTestStore();
    integrationService = new IntegrationService(() => store.getState());
    vi.clearAllMocks();
    
    // Clear all event listeners
    EventBus.off();
  });

  describe('Calculator + Customer Integration', () => {
    it('should calculate discounts based on customer type', () => {
      // Set up customer
      store.getState().setCustomerData({
        companyName: 'Test Chain',
        customerType: 'chain',
        contactEmail: 'chain@test.com'
      });
      
      // Set up calculation
      store.getState().setOrderValue(30000);
      store.getState().setLeadTime(20);
      store.getState().setChain(true);
      store.getState().updateCalculation();
      
      const calc = store.getState().calculator.calculation;
      expect(calc).toBeDefined();
      expect(calc?.orderValue).toBe(30000);
      expect(calc?.baseDiscount).toBe(8); // 30k = 8%
      expect(calc?.earlyDiscount).toBe(2); // 20 days = 2%
    });
  });

  describe('Event System Integration', () => {
    it('should emit and handle events correctly', async () => {
      const calculatorCallback = vi.fn();
      const customerCallback = vi.fn();
      
      // Subscribe to events
      EventBus.on('calculator:updated', calculatorCallback);
      EventBus.on('customer:saved', customerCallback);
      
      // Trigger calculator update
      store.getState().setOrderValue(15000);
      store.getState().updateCalculation();
      
      // Wait for async event
      await new Promise(resolve => setTimeout(resolve, 10));
      
      expect(calculatorCallback).toHaveBeenCalledTimes(1);
      
      const calculation = store.getState().calculator.calculation;
      expect(calculation).toBeDefined();
      expect(calculation?.orderValue).toBe(15000);
      
      // Trigger customer save
      store.getState().setCustomerData({ companyName: 'Test' });
      store.getState().saveCustomer();
      
      await new Promise(resolve => setTimeout(resolve, 10));
      
      expect(customerCallback).toHaveBeenCalledTimes(1);
    });
  });

  describe('API Integration with MSW', () => {
    beforeEach(() => {
      // Configure integrations
      store.getState().updateIntegration('monday', {
        token: 'test-token',
        boardId: 'test-board'
      });
      
      store.getState().updateIntegration('email', {
        smtpServer: 'smtp.test.com',
        smtpEmail: 'sender@test.com',
        smtpPassword: 'password'
      });
    });

    it('should create lead in Monday.com', async () => {
      const customer = {
        companyName: 'Integration Test Company',
        contactEmail: 'test@integration.com',
        contactName: 'Test User',
        contactPhone: '+49 123 456789',
        customerType: 'single' as const,
        industry: 'hotel' as const
      };
      
      const calculation = {
        orderValue: 25000,
        leadTime: 25,
        pickup: true,
        chain: false,
        baseDiscount: 8,
        earlyDiscount: 2,
        pickupDiscount: 2,
        chainDiscount: 0,
        totalDiscount: 12,
        discountAmount: 3000,
        finalPrice: 22000,
        savingsAmount: 3000
      };
      
      const result = await integrationService.createMondayLead(customer, calculation);
      
      expect(result.success).toBe(true);
      expect(result.data).toMatchObject({
        id: '123456',
        name: 'Test Customer'
      });
    });

    it('should handle API errors gracefully', async () => {
      // Remove token to trigger error
      store.getState().updateIntegration('monday', {
        token: '',
        boardId: 'test-board'
      });
      
      const result = await integrationService.createMondayLead(
        { companyName: 'Test', customerType: 'single' },
        { orderValue: 1000, totalDiscount: 0 } as any
      );
      
      expect(result.success).toBe(false);
      expect(result.error).toContain('not configured');
    });
  });

  describe('Full Workflow Integration', () => {
    it('should handle complete sales workflow', async () => {
      // 1. Set salesperson info
      store.getState().updateSalesperson({
        name: 'Max Mustermann',
        email: 'max@freshplan.de',
        phone: '+49 123 456789'
      });
      
      // 2. Configure integrations
      store.getState().updateIntegration('monday', {
        token: 'monday-token',
        boardId: 'sales-board'
      });
      
      // 3. Add customer
      store.getState().setCustomerData({
        companyName: 'Hotel Beispiel',
        customerType: 'single',
        industry: 'hotel',
        contactName: 'Herr Schmidt',
        contactEmail: 'schmidt@hotel.de',
        rooms: 50,
        occupancyRate: 70
      });
      
      // 4. Calculate offer
      store.getState().setOrderValue(45000);
      store.getState().setLeadTime(30);
      store.getState().setPickup(true);
      store.getState().updateCalculation();
      
      // 5. Create lead
      const customer = store.getState().customer.data!;
      const calculation = store.getState().calculator.calculation!;
      
      const results = await integrationService.createLeadInAllSystems(
        customer,
        calculation
      );
      
      // Verify results
      expect(results.monday).toBeDefined();
      expect(results.monday?.success).toBe(true);
      
      // 6. Add success notification
      if (results.monday?.success) {
        store.getState().addNotification({
          type: 'success',
          message: 'Lead erfolgreich erstellt!'
        });
      }
      
      expect(store.getState().ui.notifications).toHaveLength(1);
      expect(store.getState().ui.notifications[0].type).toBe('success');
    });
  });

  describe('Performance', () => {
    it('should handle rapid state updates efficiently', () => {
      const startTime = performance.now();
      
      // Perform 100 rapid updates
      for (let i = 0; i < 100; i++) {
        store.getState().setOrderValue(i * 100);
        store.getState().updateCalculation();
      }
      
      const endTime = performance.now();
      const duration = endTime - startTime;
      
      // Should complete in under 100ms
      expect(duration).toBeLessThan(100);
      
      // Final state should be correct
      expect(store.getState().calculator.orderValue).toBe(9900);
      expect(store.getState().calculator.calculation?.orderValue).toBe(9900);
    });

    it('should handle concurrent operations', async () => {
      const operations = [];
      
      // Launch multiple concurrent operations
      for (let i = 0; i < 10; i++) {
        operations.push(
          Promise.all([
            // Update calculator
            new Promise(resolve => {
              store.getState().setOrderValue(i * 1000);
              store.getState().updateCalculation();
              resolve(null);
            }),
            // Update customer
            new Promise(resolve => {
              store.getState().setCustomerData({
                companyName: `Customer ${i}`
              });
              resolve(null);
            }),
            // Add notification
            new Promise(resolve => {
              store.getState().addNotification({
                type: 'info',
                message: `Operation ${i}`
              });
              resolve(null);
            })
          ])
        );
      }
      
      await Promise.all(operations);
      
      // State should be consistent
      expect(store.getState().ui.notifications.length).toBeGreaterThan(0);
      expect(store.getState().customer.data).toBeDefined();
      expect(store.getState().calculator.calculation).toBeDefined();
    });
  });
});