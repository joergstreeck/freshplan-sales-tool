/**
 * Isolated Store Tests using Store Factory
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createTestStore } from '../createStore';
import type { StoreState } from '../createStore';

describe('Store with Factory (Isolated)', () => {
  let store: ReturnType<typeof createTestStore>;
  let getState: () => StoreState;

  beforeEach(() => {
    // Create fresh store for each test
    store = createTestStore();
    getState = store.getState;
    vi.clearAllMocks();
  });

  describe('Calculator State', () => {
    it('should update order value', () => {
      getState().setOrderValue(10000);
      expect(getState().calculator.orderValue).toBe(10000);
    });

    it('should calculate discounts correctly', () => {
      const state = getState();
      
      state.setOrderValue(20000);
      state.setLeadTime(15);
      state.setPickup(true);
      state.updateCalculation();
      
      const calc = getState().calculator.calculation;
      expect(calc).toBeDefined();
      expect(calc?.baseDiscount).toBe(6); // 20000 = 6%
      expect(calc?.earlyDiscount).toBe(2);
      expect(calc?.pickupDiscount).toBe(2);
      expect(calc?.totalDiscount).toBe(10); // 6+2+2=10
    });

    it('should handle multiple instances independently', () => {
      // Create second store
      const store2 = createTestStore();
      
      // Update first store
      getState().setOrderValue(15000);
      
      // Update second store
      store2.getState().setOrderValue(25000);
      
      // Check they're independent
      expect(getState().calculator.orderValue).toBe(15000);
      expect(store2.getState().calculator.orderValue).toBe(25000);
    });
  });

  describe('Customer State', () => {
    it('should update customer data', () => {
      getState().setCustomerData({
        companyName: 'Test GmbH',
        contactEmail: 'test@example.com'
      });
      
      const customer = getState().customer;
      expect(customer.data?.companyName).toBe('Test GmbH');
      expect(customer.data?.contactEmail).toBe('test@example.com');
      expect(customer.isDirty).toBe(true);
    });

    it('should clear customer data', () => {
      const state = getState();
      
      state.setCustomerData({ companyName: 'Test' });
      state.clearCustomer();
      
      expect(getState().customer.data).toBeNull();
      expect(getState().customer.isDirty).toBe(false);
    });
  });

  describe('UI State', () => {
    it('should manage notifications', () => {
      getState().addNotification({
        type: 'success',
        message: 'Test notification',
        duration: 5000
      });
      
      const notifications = getState().ui.notifications;
      expect(notifications).toHaveLength(1);
      expect(notifications[0].type).toBe('success');
      expect(notifications[0].message).toBe('Test notification');
    });

    it('should update loading state', () => {
      const state = getState();
      
      state.setLoading(true);
      expect(getState().ui.loading).toBe(true);
      
      state.setLoading(false);
      expect(getState().ui.loading).toBe(false);
    });

    it('should auto-remove notifications', async () => {
      getState().addNotification({
        type: 'info',
        message: 'Auto remove',
        duration: 50
      });
      
      expect(getState().ui.notifications).toHaveLength(1);
      
      // Wait for auto-remove
      await new Promise(resolve => setTimeout(resolve, 100));
      
      expect(getState().ui.notifications).toHaveLength(0);
    });
  });

  describe('Settings State', () => {
    it('should update salesperson info', () => {
      getState().updateSalesperson({
        name: 'John Doe',
        email: 'john@example.com'
      });
      
      const salesperson = getState().settings.salesperson;
      expect(salesperson.name).toBe('John Doe');
      expect(salesperson.email).toBe('john@example.com');
    });

    it('should update integration settings', () => {
      getState().updateIntegration('monday', {
        token: 'test-token',
        boardId: 'test-board'
      });
      
      const monday = getState().settings.integrations.monday;
      expect(monday.token).toBe('test-token');
      expect(monday.boardId).toBe('test-board');
    });
  });

  describe('Global Actions', () => {
    it('should reset store to initial state', () => {
      const state = getState();
      
      // Change values
      state.setOrderValue(50000);
      state.setLanguage('en');
      state.setLoading(true);
      state.addNotification({ type: 'info', message: 'test' });
      
      // Reset
      state.reset();
      
      // Check initial values
      expect(getState().calculator.orderValue).toBe(5000);
      expect(getState().i18n.currentLanguage).toBe('en'); // i18n doesn't reset
      expect(getState().ui.loading).toBe(false);
      expect(getState().ui.notifications).toHaveLength(0);
    });
  });

  describe('Subscriptions', () => {
    it('should notify subscribers on state changes', () => {
      const callback = vi.fn();
      
      // Subscribe to changes
      const unsubscribe = store.subscribe(callback);
      
      // Make changes
      getState().setOrderValue(15000);
      getState().setLeadTime(20);
      
      // Should be called for each change
      expect(callback).toHaveBeenCalledTimes(2);
      
      // Unsubscribe
      unsubscribe();
      
      // Further changes should not trigger callback
      getState().setPickup(true);
      expect(callback).toHaveBeenCalledTimes(2);
    });

    it('should support selective subscriptions', () => {
      const callback = vi.fn();
      
      // Subscribe only to calculator changes
      const unsubscribe = store.subscribe(
        (state) => state.calculator,
        callback
      );
      
      // Calculator change should trigger
      getState().setOrderValue(20000);
      expect(callback).toHaveBeenCalledTimes(1);
      
      // UI change should not trigger
      getState().setLoading(true);
      expect(callback).toHaveBeenCalledTimes(1);
      
      unsubscribe();
    });
  });
});