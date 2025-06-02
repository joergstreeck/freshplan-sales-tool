/**
 * Robust Store Tests with proper isolation
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createTestStore } from '../createStore';
import type { StoreState } from '../createStore';

describe('Store Tests (Robust)', () => {
  let store: ReturnType<typeof createTestStore>;

  beforeEach(() => {
    // Create fresh isolated store for each test
    store = createTestStore();
    vi.clearAllMocks();
  });

  describe('Calculator Functionality', () => {
    it('should update values and calculate discounts', () => {
      // Initial state
      expect(store.getState().calculator.orderValue).toBe(5000);
      
      // Update order value
      store.getState().setOrderValue(20000);
      expect(store.getState().calculator.orderValue).toBe(20000);
      
      // Set lead time
      store.getState().setLeadTime(30);
      expect(store.getState().calculator.leadTime).toBe(30);
      
      // Set pickup
      store.getState().setPickup(true);
      expect(store.getState().calculator.pickup).toBe(true);
      
      // Trigger calculation
      store.getState().updateCalculation();
      
      // Check calculation results
      const calc = store.getState().calculator.calculation;
      expect(calc).toBeDefined();
      expect(calc?.orderValue).toBe(20000);
      expect(calc?.leadTime).toBe(30);
      expect(calc?.pickup).toBe(true);
      expect(calc?.baseDiscount).toBe(6); // 20000 -> 6%
      expect(calc?.earlyDiscount).toBe(3); // 30 days -> 3%
      expect(calc?.pickupDiscount).toBe(2); // pickup -> 2%
      expect(calc?.totalDiscount).toBe(11);
      expect(calc?.discountAmount).toBe(2200);
      expect(calc?.finalPrice).toBe(17800);
    });

    it('should handle edge cases', () => {
      // Very low order value
      store.getState().setOrderValue(1000);
      store.getState().updateCalculation();
      
      let calc = store.getState().calculator.calculation;
      expect(calc?.baseDiscount).toBe(0);
      expect(calc?.pickupDiscount).toBe(0); // No pickup discount under 5000
      
      // Very high order value
      store.getState().setOrderValue(100000);
      store.getState().updateCalculation();
      
      calc = store.getState().calculator.calculation;
      expect(calc?.baseDiscount).toBe(10); // Max discount
      
      // Edge case for lead time
      store.getState().setLeadTime(15); // Exactly 15 days
      store.getState().updateCalculation();
      
      calc = store.getState().calculator.calculation;
      expect(calc?.earlyDiscount).toBe(2);
    });
  });

  describe('Customer Management', () => {
    it('should manage customer data lifecycle', () => {
      // Add customer
      store.getState().setCustomerData({
        companyName: 'Test Company',
        contactName: 'John Doe',
        contactEmail: 'john@test.com'
      });
      
      const customer = store.getState().customer;
      expect(customer.data?.companyName).toBe('Test Company');
      expect(customer.isDirty).toBe(true);
      
      // Save customer
      store.getState().saveCustomer();
      expect(store.getState().customer.isDirty).toBe(false);
      
      // Update customer type
      store.getState().setCustomerType('chain');
      expect(store.getState().customer.customerType).toBe('chain');
      expect(store.getState().customer.data?.customerType).toBe('chain');
      
      // Clear customer
      store.getState().clearCustomer();
      expect(store.getState().customer.data).toBeNull();
      expect(store.getState().customer.isDirty).toBe(false);
    });
  });

  describe('Notification System', () => {
    it('should manage notifications with auto-removal', async () => {
      // Add notification
      store.getState().addNotification({
        type: 'success',
        message: 'Operation successful',
        duration: 100 // 100ms for testing
      });
      
      let notifications = store.getState().ui.notifications;
      expect(notifications).toHaveLength(1);
      expect(notifications[0].type).toBe('success');
      expect(notifications[0].id).toBeDefined();
      expect(notifications[0].timestamp).toBeDefined();
      
      // Wait for auto-removal
      await new Promise(resolve => setTimeout(resolve, 150));
      
      notifications = store.getState().ui.notifications;
      expect(notifications).toHaveLength(0);
    });

    it('should handle multiple notifications', () => {
      // Add multiple notifications
      store.getState().addNotification({ type: 'info', message: 'Info 1' });
      store.getState().addNotification({ type: 'warning', message: 'Warning 1' });
      store.getState().addNotification({ type: 'error', message: 'Error 1' });
      
      expect(store.getState().ui.notifications).toHaveLength(3);
      
      // Remove specific notification
      const notifId = store.getState().ui.notifications[1].id;
      store.getState().removeNotification(notifId);
      
      expect(store.getState().ui.notifications).toHaveLength(2);
      expect(store.getState().ui.notifications.find(n => n.id === notifId)).toBeUndefined();
      
      // Clear all
      store.getState().clearNotifications();
      expect(store.getState().ui.notifications).toHaveLength(0);
    });
  });

  describe('Settings Management', () => {
    it('should update settings correctly', () => {
      // Update salesperson
      store.getState().updateSalesperson({
        name: 'Jane Smith',
        email: 'jane@company.com',
        phone: '+49 123 456789'
      });
      
      const salesperson = store.getState().settings.salesperson;
      expect(salesperson.name).toBe('Jane Smith');
      expect(salesperson.email).toBe('jane@company.com');
      expect(salesperson.phone).toBe('+49 123 456789');
      
      // Update integrations
      store.getState().updateIntegration('monday', {
        token: 'test-token-123',
        boardId: 'board-456'
      });
      
      const monday = store.getState().settings.integrations.monday;
      expect(monday.token).toBe('test-token-123');
      expect(monday.boardId).toBe('board-456');
    });
  });

  describe('Store Reset', () => {
    it('should reset to initial state', () => {
      // Make various changes
      store.getState().setOrderValue(50000);
      store.getState().setPickup(true);
      store.getState().setCustomerData({ companyName: 'Test' });
      store.getState().addNotification({ type: 'info', message: 'Test' });
      store.getState().setLoading(true);
      
      // Reset
      store.getState().reset();
      
      // Check reset values
      const state = store.getState();
      expect(state.calculator.orderValue).toBe(5000);
      expect(state.calculator.pickup).toBe(false);
      expect(state.customer.data).toBeNull();
      expect(state.ui.notifications).toHaveLength(0);
      expect(state.ui.loading).toBe(false);
    });
  });

  describe('Store Isolation', () => {
    it('should maintain isolation between store instances', () => {
      // Create second store
      const store2 = createTestStore();
      
      // Update first store
      store.getState().setOrderValue(25000);
      store.getState().setCustomerData({ companyName: 'Store 1' });
      
      // Update second store
      store2.getState().setOrderValue(35000);
      store2.getState().setCustomerData({ companyName: 'Store 2' });
      
      // Verify isolation
      expect(store.getState().calculator.orderValue).toBe(25000);
      expect(store2.getState().calculator.orderValue).toBe(35000);
      expect(store.getState().customer.data?.companyName).toBe('Store 1');
      expect(store2.getState().customer.data?.companyName).toBe('Store 2');
    });
  });

  describe('Subscriptions', () => {
    it('should notify subscribers on changes', () => {
      const listener = vi.fn();
      
      // Subscribe
      const unsubscribe = store.subscribe(listener);
      
      // Make changes
      store.getState().setOrderValue(15000);
      store.getState().setLeadTime(20);
      
      // Verify calls
      expect(listener).toHaveBeenCalledTimes(2);
      
      // Unsubscribe
      unsubscribe();
      
      // Further changes should not trigger
      store.getState().setPickup(true);
      expect(listener).toHaveBeenCalledTimes(2);
    });

    it('should support selective subscriptions', () => {
      const calculatorListener = vi.fn();
      const uiListener = vi.fn();
      
      // Subscribe to specific slices
      const unsub1 = store.subscribe(
        state => state.calculator,
        calculatorListener
      );
      
      const unsub2 = store.subscribe(
        state => state.ui,
        uiListener
      );
      
      // Calculator change
      store.getState().setOrderValue(20000);
      expect(calculatorListener).toHaveBeenCalledTimes(1);
      expect(uiListener).toHaveBeenCalledTimes(0);
      
      // UI change
      store.getState().setLoading(true);
      expect(calculatorListener).toHaveBeenCalledTimes(1);
      expect(uiListener).toHaveBeenCalledTimes(1);
      
      unsub1();
      unsub2();
    });
  });
});