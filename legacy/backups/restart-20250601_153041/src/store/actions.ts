/**
 * Store actions factory
 */

import type { WritableDraft } from 'immer';
import type { StoreState } from './createStore';
import type { CustomerState, Notification, CalculatorCalculation } from '@/types';
import EventBus from '@/core/EventBus';

export const createActions = (
  set: (fn: (draft: WritableDraft<StoreState>) => void) => void,
  get: () => StoreState
) => ({
  // Calculator actions
  setOrderValue: (value: number) => set((state: WritableDraft<StoreState>) => {
    state.calculator.orderValue = value;
  }),

  setLeadTime: (value: number) => set((state: WritableDraft<StoreState>) => {
    state.calculator.leadTime = value;
  }),

  setPickup: (pickup: boolean) => set((state: WritableDraft<StoreState>) => {
    state.calculator.pickup = pickup;
  }),

  setChain: (chain: boolean) => set((state: WritableDraft<StoreState>) => {
    state.calculator.chain = chain;
  }),

  updateCalculation: (calculation?: CalculatorCalculation | null) => set((state: WritableDraft<StoreState>) => {
    if (calculation !== undefined) {
      state.calculator.calculation = calculation;
    } else {
      // Calculate discount
      const { orderValue, leadTime, pickup } = state.calculator;
      
      const baseRules = [
        { min: 75000, discount: 10 },
        { min: 50000, discount: 9 },
        { min: 30000, discount: 8 },
        { min: 15000, discount: 6 },
        { min: 5000, discount: 3 },
        { min: 0, discount: 0 }
      ];

      const earlyRules = [
        { days: 30, discount: 3 },
        { days: 15, discount: 2 },
        { days: 10, discount: 1 },
        { days: 0, discount: 0 }
      ];

      let baseDiscount = 0;
      for (const rule of baseRules) {
        if (orderValue >= rule.min) {
          baseDiscount = rule.discount;
          break;
        }
      }

      let earlyDiscount = 0;
      for (const rule of earlyRules) {
        if (leadTime >= rule.days) {
          earlyDiscount = rule.discount;
          break;
        }
      }

      const pickupDiscount = (pickup && orderValue >= 5000) ? 2 : 0;
      const chainDiscount = 0;

      const totalDiscount = baseDiscount + earlyDiscount + pickupDiscount + chainDiscount;
      const discountAmount = orderValue * (totalDiscount / 100);
      const finalPrice = orderValue - discountAmount;
      const savingsAmount = discountAmount;

      state.calculator.calculation = {
        baseDiscount,
        earlyDiscount,
        pickupDiscount,
        chainDiscount,
        totalDiscount,
        discountAmount,
        finalPrice,
        orderValue,
        leadTime,
        savingsAmount,
        pickup,
        chain: state.calculator.chain
      };
    }
    
    // Emit event
    EventBus.emit('calculator:updated', get().calculator.calculation);
  }),

  // Customer actions
  setCustomerData: (data: Partial<CustomerState['data']>) => set((state: WritableDraft<StoreState>) => {
    if (!state.customer.data) {
      state.customer.data = {} as CustomerState['data'];
    }
    if (state.customer.data) {
      Object.assign(state.customer.data, data);
    }
    state.customer.isDirty = true;
  }),

  setCustomerType: (type: CustomerState['customerType']) => set((state: WritableDraft<StoreState>) => {
    state.customer.customerType = type;
    if (state.customer.data) {
      state.customer.data.customerType = type;
    }
  }),

  setIndustry: (industry: CustomerState['industry']) => set((state: WritableDraft<StoreState>) => {
    state.customer.industry = industry;
    if (state.customer.data) {
      state.customer.data.industry = industry;
    }
  }),

  saveCustomer: () => set((state: WritableDraft<StoreState>) => {
    state.customer.isDirty = false;
    EventBus.emit('customer:saved', state.customer.data);
  }),

  clearCustomer: () => set((state: WritableDraft<StoreState>) => {
    state.customer.data = null;
    state.customer.isDirty = false;
    state.customer.isValid = true;
  }),

  // Settings actions
  updateSettings: (settings: Partial<StoreState['settings']>) => set((state: WritableDraft<StoreState>) => {
    Object.assign(state.settings, settings);
  }),

  updateSalesperson: (info: Partial<StoreState['settings']['salesperson']>) => set((state: WritableDraft<StoreState>) => {
    Object.assign(state.settings.salesperson, info);
  }),

  updateIntegration: <K extends keyof StoreState['settings']['integrations']>(
    integration: K,
    data: Partial<StoreState['settings']['integrations'][K]>
  ) => set((state: WritableDraft<StoreState>) => {
    Object.assign(state.settings.integrations[integration], data);
  }),

  // Profile actions
  setProfileData: (data: StoreState['profile']['data']) => set((state: WritableDraft<StoreState>) => {
    state.profile.data = data;
  }),

  generateProfile: () => set((_state: WritableDraft<StoreState>) => {
    console.log('Generating profile from customer data...');
    EventBus.emit('profile:generate');
  }),

  // PDF actions
  setPDFAvailable: (available: boolean) => set((state: WritableDraft<StoreState>) => {
    state.pdf.available = available;
  }),

  setCurrentPDF: (pdf: any, filename: string) => set((state: WritableDraft<StoreState>) => {
    state.pdf.currentPDF = pdf;
    state.pdf.filename = filename;
    state.pdf.available = true;
  }),

  clearPDF: () => set((state: WritableDraft<StoreState>) => {
    state.pdf.currentPDF = null;
    state.pdf.filename = null;
    state.pdf.available = false;
  }),

  // i18n actions
  setLanguage: (language: StoreState['i18n']['currentLanguage']) => set((state: WritableDraft<StoreState>) => {
    state.i18n.currentLanguage = language;
    EventBus.emit('language:changed', language);
  }),

  toggleAutoUpdate: () => set((state: WritableDraft<StoreState>) => {
    state.i18n.autoUpdate = !state.i18n.autoUpdate;
  }),

  // UI actions
  setCurrentTab: (tab: StoreState['ui']['currentTab']) => set((state: WritableDraft<StoreState>) => {
    state.ui.currentTab = tab;
    EventBus.emit('tab:changed', tab);
  }),

  setLoading: (loading: boolean) => set((state: WritableDraft<StoreState>) => {
    state.ui.loading = loading;
  }),

  setError: (error: StoreState['ui']['error']) => set((state: WritableDraft<StoreState>) => {
    state.ui.error = error;
  }),

  clearError: () => set((state: WritableDraft<StoreState>) => {
    state.ui.error = null;
  }),

  addNotification: (notification: Omit<Notification, 'id' | 'timestamp'>) => set((state: WritableDraft<StoreState>) => {
    const newNotification: Notification = {
      ...notification,
      id: `notif-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      timestamp: Date.now()
    };
    
    state.ui.notifications.push(newNotification);
    
    // Auto-remove after duration
    if (notification.duration && notification.duration > 0) {
      setTimeout(() => {
        get().removeNotification(newNotification.id);
      }, notification.duration);
    }
  }),

  removeNotification: (id: string) => set((state: WritableDraft<StoreState>) => {
    state.ui.notifications = state.ui.notifications.filter((n: Notification) => n.id !== id);
  }),

  clearNotifications: () => set((state: WritableDraft<StoreState>) => {
    state.ui.notifications = [];
  }),

  // Global actions
  reset: () => set((state: WritableDraft<StoreState>) => {
    // Reset to initial values
    state.calculator.orderValue = 5000;
    state.calculator.leadTime = 14;
    state.calculator.pickup = false;
    state.calculator.chain = false;
    state.calculator.calculation = null;
    
    state.customer.data = null;
    state.customer.customerType = 'single';
    state.customer.industry = '';
    state.customer.isDirty = false;
    state.customer.isValid = true;
    
    state.ui.currentTab = 'demonstrator';
    state.ui.loading = false;
    state.ui.error = null;
    state.ui.notifications = [];
  }),

  hydrate: () => {
    // This is called by persist middleware
    EventBus.emit('store:hydrated');
  }
});